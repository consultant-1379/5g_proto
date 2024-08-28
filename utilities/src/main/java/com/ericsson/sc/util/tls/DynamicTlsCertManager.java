/** 
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 21, 2021
 *     Author: echfari
 */
package com.ericsson.sc.util.tls;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager.Verification;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.KeyCertProvider;
import com.ericsson.utilities.file.TrustedCert;
import com.ericsson.utilities.file.TrustedCertProvider;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.subjects.CompletableSubject;

/**
 * Manages TLS certificate updates
 */
public class DynamicTlsCertManager
{
    private static final Logger log = LoggerFactory.getLogger(DynamicTlsCertManager.class);

    private final Type type;
    private final Flowable<Boolean> chain;
    private final CompletableSubject closed = CompletableSubject.create();
    private final FlowableTransformer<Boolean, Boolean> tr = b -> b.takeUntil(closed.toFlowable())
                                                                   .doOnSubscribe(sub -> log.debug("Started"))
                                                                   .doOnTerminate(() -> log.info("Terminated"))
                                                                   .doOnError(err -> log.error("Unexpected error,retrying in 10 seconds: {}", err.getMessage()))
                                                                   .retryWhen(p -> p.delay(10, TimeUnit.SECONDS)) // retry forever
                                                                   .replay(1)
                                                                   .autoConnect();
    private final IdentityUpdater ip;
    private final TrustedCertUpdater tcu;

    public enum Type
    {
        /**
         * Mutual TLS
         */
        MUTUAL,
        /**
         * Trusted certificate updates only
         */
        TRUSTED_CERT,
        /**
         * TLS credential updates only
         */
        KEYCERT
    }

    /**
     * @return The configured TLS type
     */
    public Type getType()
    {
        return this.type;
    }

    public static DynamicTlsCertManager create(KeyCertProvider kcp)
    {
        return new DynamicTlsCertManager(kcp.watchKeyCert());
    }

    public static DynamicTlsCertManager create(KeyCertProvider kcp,
                                               TrustedCertProvider tcp)
    {
        return new DynamicTlsCertManager(kcp.watchKeyCert(), tcp.watchTrustedCerts());
    }

    public static DynamicTlsCertManager create(TrustedCertProvider tcp,
                                               Type type)
    {
        return new DynamicTlsCertManager(tcp.watchTrustedCerts(), type);
    }

    public static DynamicTlsCertManager create(KeyCertProvider kcp,
                                               TrustedCertProvider tcp,
                                               AdvancedTlsX509TrustManager tm)
    {
        return new DynamicTlsCertManager(kcp.watchKeyCert(), tcp.watchTrustedCerts(), tm);
    }

    public X509KeyManager getKeyManager()
    {
        return this.ip.keyManager;
    }

    public X509TrustManager getTrustManager()
    {
        return this.tcu.trustManager;
    }

    /**
     * Subscribe to certificate flows and complete when the first update has been
     * performed. TLS connections can only be established after the returned
     * Completable completes.
     * 
     * @return
     */
    public Completable start()
    {
        return this.chain.firstOrError().ignoreElement();
    }

    /**
     * Supervise TLS certificate updates
     * 
     * @return A Flowable that completes as soon as {@link #terminate()} has
     *         completed
     */
    public Completable run()
    {
        return this.chain.ignoreElements();
    }

    /**
     * 
     * @return A Completable that terminates all certificate updates upon
     *         subscription
     */
    public Completable terminate()
    {
        return Completable.defer(() ->
        {
            this.closed.onComplete();
            return this.chain.ignoreElements();
        });
    }

    public DynamicTlsCertManager(Flowable<KeyCert> certFlow,
                                 Flowable<TrustedCert> caFlow)
    {
        this(certFlow, caFlow, AdvancedTlsX509TrustManager.newBuilder().setVerification(Verification.DELEGATE).build());
    }

    public DynamicTlsCertManager(Flowable<KeyCert> certFlow,
                                 Flowable<TrustedCert> caFlow,
                                 AdvancedTlsX509TrustManager trustManager)
    {
        this.type = Type.MUTUAL;
        this.ip = new IdentityUpdater(certFlow);
        this.tcu = new TrustedCertUpdater(caFlow, trustManager);
        this.chain = Flowable //
                             .combineLatest(ip.events, tcu.events, Pair::of)
                             .concatMap(tuple -> Flowable.just(tuple.getFirst(), tuple.getSecond()))
                             .compose(tr);
    }

    public DynamicTlsCertManager(Flowable<KeyCert> certFlow)
    {
        this.type = Type.KEYCERT;
        this.ip = new IdentityUpdater(certFlow);
        this.tcu = new TrustedCertUpdater(AdvancedTlsX509TrustManager.newBuilder().setVerification(Verification.DELEGATE).build());
        this.chain = ip.events //
                              .compose(tr);
    }

    public DynamicTlsCertManager(Flowable<TrustedCert> caFlow,
                                 Type type)
    {
        this.type = type;
        this.tcu = new TrustedCertUpdater(caFlow, AdvancedTlsX509TrustManager.newBuilder().setVerification(Verification.DELEGATE).build());
        this.ip = null;
        this.chain = tcu.events //
                               .compose(tr);
    }

    private static class IdentityUpdater
    {
        private final AdvancedTlsX509KeyManager keyManager = new AdvancedTlsX509KeyManager();
        private final Flowable<Boolean> events;

        public IdentityUpdater(Flowable<KeyCert> keyCertFlow)
        {
            events = keyCertFlow.doOnNext(updated ->
            {
                final var key = CertificateUtils.getPrivateKey(updated.getPrivateKey());
                final var certs = CertificateUtils.getX509Certificates(updated.getCertificate());
                keyManager.updateIdentityCredentials(key, certs);
                log.info("Updated TLS identity credentials: {}",
                         Arrays.stream(certs) //
                               .map(cert -> String.format("subjectDn: %s serialNumber: %s", cert.getSubjectX500Principal(), cert.getSerialNumber()))
                               .collect(Collectors.toUnmodifiableList()));
            })
                                .doOnError(err -> log.warn("Failed to update update TLS identity credentials", err)) //
                                .map(p -> true);
        }
    }

    private static class TrustedCertUpdater
    {
        private final AdvancedTlsX509TrustManager trustManager;
        private final Flowable<Boolean> events;

        public TrustedCertUpdater(AdvancedTlsX509TrustManager trustManager)
        {
            this.trustManager = trustManager;
            events = Flowable.never();
        }

        public TrustedCertUpdater(Flowable<TrustedCert> trustedCertFlow,
                                  AdvancedTlsX509TrustManager trustManager)
        {
            this.trustManager = trustManager;
            events = trustedCertFlow.doOnNext(caCert ->
            {
                final var x509Certs = new ArrayList<X509Certificate>();
                for (var tc : caCert.getTrustedCertificate())
                {
                    final var x509 = CertificateUtils.getX509Certificates(tc);
                    x509Certs.addAll(Arrays.asList(x509));
                }
                trustManager.updateTrustCredentials(x509Certs);
                log.info("Updated trusted certificates: {}",
                         x509Certs.stream() //
                                  .map(cert -> String.format("subjectDn: %s serialNumber: %s", cert.getSubjectX500Principal(), cert.getSerialNumber()))
                                  .collect(Collectors.toUnmodifiableList()));
            }).doOnError(err -> log.warn("Failed to update update TLS trusted certificates", err)).map(p -> true);
        }
    }

}
