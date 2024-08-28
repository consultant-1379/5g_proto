/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 1, 2021
 *     Author: echaias
 */

package com.ericsson.sc.externalcertificates;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.certmcrhandler.data.TlsData;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class ExternalCertificatesAlarmHandler extends ExternalCertificateController
{
    private static final String NO_EXT_CERTIFICATE_ALARM = "MissingExternalCertificate";
    private static final String NO_ASYM_KEY_DESC = "Asymmetric Key is unavailable";
    private static final String NO_CA_DESC = "Certificate Authority is unavailable";
    private static final String SEPP_INSTANCE_YANG_PATH = "/sepp-function/nf-instance/";
    private static final String ASYM_KEY_LIST_YANG = "asym-key-list";
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME");

    private static final Logger log = LoggerFactory.getLogger(ExternalCertificatesAlarmHandler.class);

    private FmAlarmService alarmService;
    private BehaviorSubject<Optional<EricssonSepp>> configFlow;
    private String serviceName;
    private Disposable alarmsHandlingDisposable;
    private Set<String> activeAlarms = new HashSet<>();

    public ExternalCertificatesAlarmHandler(String serviceName,
                                            FmAlarmService alarmService,
                                            BehaviorSubject<Optional<EricssonSepp>> configFlow)
    {
        super();
        this.serviceName = serviceName;
        this.alarmService = alarmService;
        this.configFlow = configFlow;

        startSecretWatcher();

        this.configFlow.toFlowable(BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).distinctUntilChanged().subscribe(optEricssonSepp ->
        {
            if (optEricssonSepp.isPresent() && optEricssonSepp.get().getEricssonSeppSeppFunction() != null
                && !optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().isEmpty())
            {
                this.disposeAlarmHandlerCompletable()
                    .doOnComplete(() -> this.handleAllCertificates(optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().get(0)))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
            }
            else
            {
                resetAlarms();
            }
        }, error -> log.error(error.getMessage()));
    }

    private void handleAllCertificates(NfInstance nfInstance)
    {
        final var asymKeys = new AtomicReference<List<Pair<String, String>>>(List.copyOf(nfInstance.getAsymKeyList()
                                                                                                   .stream()
                                                                                                   .map(key -> Pair.of("asym-key-list/"
                                                                                                                       + key.getName(),
                                                                                                                       CR_PREFIX + "-sepp-extcert-"
                                                                                                                                        + key.getAsymmetricKey()
                                                                                                                                        + "-"
                                                                                                                                        + key.getCertificate()
                                                                                                                                        + "-certificate"))
                                                                                                   .toList()));

        final var trustedCerts = new AtomicReference<List<Pair<String, String>>>(List.copyOf(nfInstance.getTrustedCertList()
                                                                                                       .stream()
                                                                                                       .map(cert -> Pair.of("trusted-cert-list/"
                                                                                                                            + cert.getName(),
                                                                                                                            CR_PREFIX + "-sepp-extcert-"
                                                                                                                                              + cert.getTrustedCertListRef()
                                                                                                                                              + "-ca-certificate"))
                                                                                                       .toList()));

        // current list -> remove everything that is not on new list -> raise alarms
        if (!asymKeys.get().isEmpty() || !trustedCerts.get().isEmpty())
        {
            this.alarmsHandlingDisposable = this.secretWatcher.getTrafficSecretDataSbj()
                                                              .toFlowable(BackpressureStrategy.LATEST)
                                                              .delay(5, TimeUnit.SECONDS)
                                                              .map(Optional::ofNullable)
                                                              .filter(Optional::isPresent)
                                                              .map(secrets -> secrets.get().getSecrets())
                                                              .subscribeOn(Schedulers.io())
                                                              .doOnNext(secretsMap -> processAlarms(secretsMap, asymKeys, trustedCerts))
                                                              .subscribe();
        }
        else
        {
            resetAlarms();
        }
    }

    private synchronized void resetAlarms()
    {
        disposeAlarmHandler();

        activeAlarms.forEach(this::alarmNoExtCertificateCease);
        activeAlarms.clear();
    }

    private synchronized void disposeAlarmHandler()
    {
        if (this.alarmsHandlingDisposable != null && !this.alarmsHandlingDisposable.isDisposed())
        {
            this.alarmsHandlingDisposable.dispose();
        }
    }

    private Completable disposeAlarmHandlerCompletable()
    {
        return Completable.defer(() -> Completable.fromAction(this::disposeAlarmHandler));
    }

    private void processAlarms(Map<String, TlsData> secretsMap,
                               AtomicReference<List<Pair<String, String>>> asymKeys,
                               AtomicReference<List<Pair<String, String>>> trustedCerts)
    {
        final var newAlarmsToRaise = new HashSet<String>();

        asymKeys.get().stream().filter(asymKey ->
        {
            final var tlsData = secretsMap.get(asymKey.getSecond());
            return tlsData == null || tlsData.getKeyValue() == null || tlsData.getCertValue() == null;
        }).forEach(asymKey -> newAlarmsToRaise.add(asymKey.getFirst()));

        trustedCerts.get().stream().filter(trustCa ->
        {
            final var tlsData = secretsMap.get(trustCa.getSecond());
            return tlsData == null || tlsData.getCaCertValue() == null;
        }).forEach(trustCa -> newAlarmsToRaise.add(trustCa.getFirst()));

        newAlarmsToRaise.stream().filter(alarm -> !activeAlarms.contains(alarm)).forEach(this::alarmNoExtCertificateRaise);
        activeAlarms.stream().filter(alarm -> !newAlarmsToRaise.contains(alarm)).forEach(this::alarmNoExtCertificateCease);

        activeAlarms = new HashSet<>(newAlarmsToRaise);
    }

    private void alarmNoExtCertificateRaise(String alarmRdn)
    {
        var alarmRdnBuilder = new StringBuilder();
        alarmRdnBuilder.append(SEPP_INSTANCE_YANG_PATH);
        alarmRdnBuilder.append(alarmRdn);

        var faultIndication = new FaultIndicationBuilder().withFaultName(NO_EXT_CERTIFICATE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withDescription(alarmRdn.contains(ASYM_KEY_LIST_YANG) ? NO_ASYM_KEY_DESC : NO_CA_DESC)
                                                          .withFaultyResource(alarmRdnBuilder.toString())
                                                          .withSeverity(Severity.CRITICAL)
                                                          .withExpiration(0L)
                                                          .build();
        log.debug("Raise alarm with FaultIndication: {}", faultIndication);
        try
        {
            this.alarmService.raise(faultIndication)
                             .subscribe(() -> log.info("raised alarm {}", NO_EXT_CERTIFICATE_ALARM),
                                        t -> log.error("Error raising alarm. Cause: {}", t.toString()));
        }
        catch (JsonProcessingException e)
        {
            log.error("Error during alarm raise {}.", e);
        }
    }

    private void alarmNoExtCertificateCease(String alarmRdn)
    {
        var alarmRdnBuilder = new StringBuilder();
        alarmRdnBuilder.append(SEPP_INSTANCE_YANG_PATH);
        alarmRdnBuilder.append(alarmRdn);

        var faultIndication = new FaultIndicationBuilder().withFaultName(NO_EXT_CERTIFICATE_ALARM)
                                                          .withServiceName(this.serviceName)
                                                          .withFaultyResource(alarmRdnBuilder.toString())
                                                          .withDescription(alarmRdn.contains(ASYM_KEY_LIST_YANG) ? NO_ASYM_KEY_DESC : NO_CA_DESC)
                                                          .build();
        log.debug("Cease alarm with FaultIndication: {}", faultIndication);

        try
        {
            this.alarmService.cease(faultIndication)
                             .subscribe(() -> log.info("ceased alarm {}", NO_EXT_CERTIFICATE_ALARM),
                                        t -> log.error("Error raising alarm. Cause: {}", t.toString()));
        }
        catch (JsonProcessingException e)
        {
            log.error("Error during alarm cease {}.", e);
        }
    }
}
