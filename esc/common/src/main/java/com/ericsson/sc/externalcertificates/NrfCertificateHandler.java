/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 27, 2024
 *     Author: zavvann
 */

package com.ericsson.sc.externalcertificates;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.NrfCertificateInfo;
import com.ericsson.cnal.common.NrfCertificateInfo.ExternalCertificate;
import com.ericsson.cnal.common.NrfCertificateInfo.NrfCertificateHandling;
import com.ericsson.sc.certmcrhandler.data.TlsData;
import com.ericsson.sc.certmcrhandler.k8s.io.K8sClient;

import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * 
 */
public class NrfCertificateHandler extends ExternalCertificateController
{
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME");
    private AtomicReference<String> nrfCert;
    private AtomicReference<String> nrfCa;
    private Subject<NrfCertificateInfo> nrfCertInfoSubject;
    private Disposable nrfCertHandlingDisposable;

    private static final Logger log = LoggerFactory.getLogger(NrfCertificateHandler.class);

    public NrfCertificateHandler(BehaviorSubject<Optional<EricssonSepp>> ericssonSepp)
    {
        this.nrfCertHandlingDisposable = null;
        this.nrfCertInfoSubject = BehaviorSubject.create();

        startSecretWatcher();

        ericssonSepp.toFlowable(BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).distinctUntilChanged().subscribe(optEricssonSepp ->
        {
            if (optEricssonSepp.isPresent() && optEricssonSepp.get().getEricssonSeppSeppFunction() != null
                && !optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().isEmpty())
            {
                log.info("New configuration received");

                this.nrfCert = new AtomicReference<>();
                this.nrfCa = new AtomicReference<>();

                this.handleNrfCertificates(optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().get(0));
            }
            else
            {
                resetNrfCertChain();
            }
        }, error -> log.error(error.getMessage()));
    }

    public Flowable<NrfCertificateInfo> getNrfExternalCertificateInfo()
    {
        return Flowable.defer(() -> this.nrfCertInfoSubject.toFlowable(BackpressureStrategy.LATEST));
    }

    private void setNrfSecretNames(NfInstance instance)
    {
        final var svcRef = instance.getOwnNetwork().get(0).getServiceAddressRef();
        final var trustCaRef = instance.getOwnNetwork().get(0).getTrustedCertInListRef();

        instance.getServiceAddress()
                .stream()
                .filter(svc -> svc.getName().equals(svcRef) && svc.getAsymKeyInRef() != null && svc.getAsymKeyOutRef() != null)
                .findAny()
                .ifPresent(svc -> instance.getAsymKeyList()
                                          .stream()
                                          .filter(asymKey -> asymKey.getName().equals(svc.getAsymKeyInRef()))
                                          .findAny()
                                          .ifPresent(asymKey -> this.nrfCert.set(CR_PREFIX + "-sepp-extcert-" + asymKey.getAsymmetricKey() + "-"
                                                                                 + asymKey.getCertificate() + "-certificate")));
        instance.getTrustedCertList()
                .stream()
                .filter(trustCa -> trustCaRef != null && trustCa.getName().equals(trustCaRef))
                .findAny()
                .ifPresent(tc -> this.nrfCa.set(CR_PREFIX + "-sepp-extcert-" + tc.getTrustedCertListRef() + "-ca-certificate"));
    }

    private synchronized void handleNrfCertificates(NfInstance nfInstance)
    {
        this.setNrfSecretNames(nfInstance);

        log.debug("Nrf asymKey secret name is: {}", this.nrfCert.get());
        log.debug("Nrf ca secret name is: {}", this.nrfCa.get());

        if (this.nrfCert.get() != null || this.nrfCa.get() != null)
        {
            this.nrfCertHandlingDisposable = this.checkNrfCertificateCrs()
                                                 .subscribeOn(Schedulers.io())
                                                 .retryWhen(new RetryFunction().withDelay(2 * 1000L) // retry after 2 seconds
                                                                               .withRetries(60) // 60 retries
                                                                               .withRetryAction((error,
                                                                                                 retry) -> log.warn("Unable to retrieve NRF certificates, retrying: {}",
                                                                                                                    retry,
                                                                                                                    error))
                                                                               .create())
                                                 .onErrorComplete()
                                                 .doOnComplete(() -> log.debug("Completed search of deployed NRF secrets"))
                                                 .andThen(this.updateNrfCertificates())
                                                 .doOnError(error -> log.warn(error.getMessage()))
                                                 .subscribe();
        }
        else
        {
            resetNrfCertChain();

            final var restoredCert = new NrfCertificateInfo(NrfCertificateHandling.OLD, Optional.empty());
            this.nrfCertInfoSubject.toSerialized().onNext(restoredCert);
        }
    }

    private Completable checkNrfCertificateCrs()
    {
        return Completable.defer(() -> Completable.fromAction(() ->
        {
            final var client = new K8sClient();

            final var nrfSecretCertFound = this.nrfCert.get() == null || client.getSecrets(this.nrfCert.get()) != null;
            final var nrfSecretCaFound = this.nrfCa.get() == null || client.getSecrets(this.nrfCa.get()) != null;

            if (!nrfSecretCertFound || !nrfSecretCaFound || this.secretWatcher == null)
            {
                final var errorMsg = this.secretWatcher == null ? "Secret watcher not initialized" : "NRF secrets not found deployed";
                throw new IllegalStateException(errorMsg);
            }
        }));
    }

    private Flowable<NrfCertificateInfo> updateNrfCertificates()
    {
        return this.secretWatcher.getTrafficSecretDataSbj()
                                 .takeWhile(a -> !this.nrfCertHandlingDisposable.isDisposed())
                                 .toFlowable(BackpressureStrategy.LATEST)
                                 .map(Optional::ofNullable)
                                 .filter(Optional::isPresent)
                                 .map(secrets -> com.ericsson.utilities.common.Pair.of(Optional.ofNullable(secrets.get().getTlsData(this.nrfCert.get())),
                                                                                       Optional.ofNullable(secrets.get().getTlsData(this.nrfCa.get()))))
                                 .distinctUntilChanged()
                                 .map(this::handleNrfSecrets)
                                 .subscribeOn(Schedulers.io())
                                 .doOnNext(cert -> this.nrfCertInfoSubject.toSerialized().onNext(cert))
                                 .doOnError(error -> log.warn(error.getMessage()));
    }

    private synchronized void resetNrfCertChain()
    {
        if (this.nrfCertHandlingDisposable != null && !this.nrfCertHandlingDisposable.isDisposed())
        {
            this.nrfCertHandlingDisposable.dispose();
        }
    }

    private NrfCertificateInfo handleNrfSecrets(final Pair<Optional<TlsData>, Optional<TlsData>> secretsContent)
    {
        final var asymKeyCertValue = secretsContent.getFirst();
        final var caCertValue = secretsContent.getSecond();

        final var nrfCertificate = new ExternalCertificate(asymKeyCertValue.map(TlsData::getCertValue).orElse(null),
                                                           asymKeyCertValue.map(TlsData::getKeyValue).orElse(null),
                                                           caCertValue.map(TlsData::getCaCertValue).orElse(null));

        return new NrfCertificateInfo(this.nrfCert.get() == null || this.nrfCa.get() == null ? NrfCertificateHandling.HYBRID : NrfCertificateHandling.NEW,
                                      Optional.ofNullable(nrfCertificate));
    }
}
