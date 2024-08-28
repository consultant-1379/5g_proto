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
 * Created on: Nov 04, 2021
 *     Author: zpavcha
 */

package com.ericsson.sc.rxetcd;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.util.EtcdTestBed;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager.Verification;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.TrustedCert;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Test class for RxEtcd TLS operations.
 */
public class RxEtcdTlsTest
{
    private static final String KEYSPACE = "/eric-sc-spr/test/";
    private static final Logger log = LoggerFactory.getLogger(RxEtcdTlsTest.class);

    private static EtcdTestBed etcdTestBed;

    private static final int RETRIES = 3;
    private static final int REQUEST_TIMEOUT = 3;

    private GeneratedCert generatedRootCa;
    private GeneratedCert generatedValidClientCert;
    private GeneratedCert serverGeneratedCert;
    private GeneratedCert generatedRootCa2;

    @BeforeClass
    private void setUpTestEnvironment() throws Exception
    {
        log.info("Executing Before Class... ");
        generateCaCert();
        generateCaCert2();
        serverGeneratedCert = generateNewCert();
        generatedValidClientCert = generateNewCert();
        etcdTestBed = new EtcdTestBed(KEYSPACE,
                                      true,
                                      serverGeneratedCert.getCertificate(),
                                      serverGeneratedCert.getPrivateKey(),
                                      generatedRootCa.getCertificate());
        etcdTestBed.start();
    }

    @Test(enabled = true, groups = "functest")
    public void hostVerification()
    {
        // Create an EtcdClient with specific TLS configuration
        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                                            .copy()
                                            .withDynamicTls(DynamicTlsCertManager.create(() -> Flowable.just(KeyCert.create(generatedValidClientCert.getPrivateKey(),
                                                                                                                            generatedValidClientCert.getCertificate())),
                                                                                         () -> Flowable.just(TrustedCert.create(List.of(generatedRootCa.getCertificate()))),
                                                                                         AdvancedTlsX509TrustManager.newBuilder().build()))
                                            .build();

        // Check that an exception is thrown due to host verification
        // Note: The default verification method in AdvancedTlsX509TrustManager builder
        // is both certs and host
        assertThrows(IllegalArgumentException.class, () -> rxEtcdClient.ready().blockingAwait());

        rxEtcdClient.close();
    }

    @Test(enabled = true, groups = "functest")
    public void clientCertWithDifferentPrivateKey()
    {
        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                                            .copy()
                                            .withDynamicTls(DynamicTlsCertManager.create(() -> Flowable.just(KeyCert.create(generateNewCert().getPrivateKey(),
                                                                                                                            generateNewCert().getCertificate())), // This
                                                                                                                                                                  // will
                                                                                                                                                                  // cause
                                                                                                                                                                  // error,
                                                                                                                                                                  // key
                                                                                                                                                                  // does
                                                                                                                                                                  // not
                                                                                                                                                                  // correspond
                                                                                                                                                                  // to
                                                                                                                                                                  // cert
                                                                                         () -> Flowable.just(TrustedCert.create(List.of(generatedRootCa.getCertificate()))),
                                                                                         AdvancedTlsX509TrustManager.newBuilder()
                                                                                                                    .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                                                    .build()))
                                            .build();

        // Check that an exception is thrown due to wrong certificate - different
        // private key
        assertThrows(IllegalArgumentException.class, () -> rxEtcdClient.ready().blockingAwait());

        rxEtcdClient.close();
    }

    @Test(enabled = true, groups = "functest")
    public void clientCertWithDifferentCaSignature()
    {
        final var newClientCert = generateNewCert2(); // Client cert from CA that etcd server does not trust

        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                                            .copy()
                                            .withDynamicTls(DynamicTlsCertManager.create(() -> Flowable.just(KeyCert.create(newClientCert.getPrivateKey(),
                                                                                                                            newClientCert.getCertificate())),
                                                                                         () -> Flowable.just(TrustedCert.create(List.of(generatedRootCa.getCertificate()))),
                                                                                         AdvancedTlsX509TrustManager.newBuilder()
                                                                                                                    .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                                                    .build()))
                                            .build();

        // Check that an exception is thrown due to wrong certificate - certificate is
        // signed by a different rootCA
        assertThrows(IllegalArgumentException.class, () -> rxEtcdClient.ready().blockingAwait());

        rxEtcdClient.close();
    }

    @Test(enabled = true, groups = "functest")
    public void wrongTrustCa()
    {
        final var newClientCert = generateNewCert(); // Correct client cert, etcd server trusts it

        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS)
                                            .copy()
                                            .withDynamicTls(DynamicTlsCertManager.create(() -> Flowable.just(KeyCert.create(newClientCert.getPrivateKey(),
                                                                                                                            newClientCert.getCertificate())),
                                                                                         () -> Flowable.just(TrustedCert.create(List.of(generatedRootCa2.getCertificate()))),
                                                                                         AdvancedTlsX509TrustManager.newBuilder()
                                                                                                                    .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                                                    .build()))
                                            .build();

        // Check that an exception is thrown due to wrong trusted CA file name
        assertThrows(IllegalArgumentException.class, () -> rxEtcdClient.ready().blockingAwait());
        rxEtcdClient.close();
    }

    @Test(enabled = true, groups = "functest")
    public void fastChangingCerts()
    {
        // Certs flowable
        final Flowable<KeyCert> keyCertFlow = Flowable.interval(1000, TimeUnit.MILLISECONDS)
                                                      .map(tick -> (int) (tick.longValue() % 3))
                                                      .map(id -> id + 1) // Index change in order to begin with set1
                                                      .doOnNext(id -> log.info("Configuration change,client cert {}", id))
                                                      .map(id -> changeCerts(id));

        final Flowable<TrustedCert> trustedCertFlow = createTrustedCertFlow();

        // TLS config
        final var tlsManager = DynamicTlsCertManager.create(() -> keyCertFlow,
                                                            () -> trustedCertFlow,
                                                            AdvancedTlsX509TrustManager.newBuilder()
                                                                                       .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                       .build());
        // Etcd client
        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS).copy().withDynamicTls(tlsManager).build();

        // Create a key-value pair
        final var key = EtcdTestBed.btSqn("testKey");
        final var value = EtcdTestBed.btSqn("testValue");

        try
        {
            rxEtcdClient.ready().blockingAwait();

            rxEtcdClient.put(key, value).blockingGet();

            // Read operation for the same key
            Flowable.interval(1000, TimeUnit.MILLISECONDS).onBackpressureBuffer().take(5).flatMapCompletable(tick ->
            {
                return rxEtcdClient.get(key).ignoreElement();

            }).blockingGet();

        }
        finally
        {
            rxEtcdClient.close();
        }

    }

    @Test(enabled = true, groups = "functest")
    private void certUpdateBeforeServerRestarts()
    {
        // Subject for certs
        final BehaviorSubject<KeyCert> keyCertSubject = BehaviorSubject.create();

        // Convert to flowable
        final Flowable<KeyCert> keyCertFlow = keyCertSubject.toFlowable(BackpressureStrategy.BUFFER);

        // Trusted CA flowable
        final Flowable<TrustedCert> trustedCertFlow = createTrustedCertFlow();

        // TLS config
        final var tlsManager = DynamicTlsCertManager.create(() -> keyCertFlow,
                                                            () -> trustedCertFlow,
                                                            AdvancedTlsX509TrustManager.newBuilder()
                                                                                       .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                       .build());
        // Etcd client
        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS).copy().withDynamicTls(tlsManager).build();
        try
        {
            // Add first set of certificates
            keyCertSubject.onNext(changeCerts(1));
            rxEtcdClient.ready().blockingAwait();

            // Add a key-value pair
            final var key = EtcdTestBed.btSqn("testKey");
            final var value = EtcdTestBed.btSqn("testValue");
            rxEtcdClient.put(key, value).blockingGet();

            // Update certificates
            keyCertSubject.onNext(changeCerts(2));

            // Restart the Etcd Container
            etcdTestBed.getEtcdContainer().restart(1);

            // Check the key-value pair
            final var getResponse = rxEtcdClient.get(key).blockingGet();
            assertTrue(getResponse.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

        }
        finally
        {
            rxEtcdClient.close();
        }
    }

    @Test(enabled = true, groups = "functest")
    private void noCertificateSetup()
    {
        // Create an empty Subject
        final BehaviorSubject<KeyCert> keyCertSubject = BehaviorSubject.create();

        // Convert to Flowable
        final Flowable<KeyCert> keyCertFlow = keyCertSubject.toFlowable(BackpressureStrategy.BUFFER);

        // Trusted CA flowable
        final Flowable<TrustedCert> trustedCertFlow = createTrustedCertFlow();

        // TLS config
        final var tlsManager = DynamicTlsCertManager.create(() -> keyCertFlow,
                                                            () -> trustedCertFlow,
                                                            AdvancedTlsX509TrustManager.newBuilder()
                                                                                       .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                       .build());
        // Client build
        final var rxEtcdClient = etcdTestBed.createEtcdClientBuilder(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS).copy().withDynamicTls(tlsManager).build();

        // Check that the client is not ready - no client certs
        assertFalse(rxEtcdClient.ready().blockingAwait(3, TimeUnit.SECONDS));

    }

    /**
     * Creates rootCA for TLS
     * 
     * @return rootCA flowable
     */
    private Flowable<TrustedCert> createTrustedCertFlow()
    {
        return Flowable.just(TrustedCert.create(List.of(this.generatedRootCa.getCertificate())));
    }

    /**
     * Gets the new client certificates for TLS
     * 
     * @param id It is used to point to the certs folder. For example, id = 1 points
     *           to folder "set1"
     * @return Both private key and certificate
     */
    private KeyCert changeCerts(long id)
    {
        return new KeyCert()
        {
            final GeneratedCert generatedCrt = generateNewCert();

            @Override
            public String getPrivateKey()
            {
                return generatedCrt.getPrivateKey();
            }

            @Override
            public String getCertificate()
            {
                return generatedCrt.getCertificate();
            }
        };
    }

    @AfterClass
    void cleanup()
    {
        RxEtcdTlsTest.etcdTestBed.stopEtcdServers();
    }

    private void generateCaCert() throws Exception
    {
        this.generatedRootCa = CertificateTool.createCertificateAuthority("testca", 2048, 365, "rootca.ericsson.com");
    }

    private void generateCaCert2() throws Exception
    {
        this.generatedRootCa2 = CertificateTool.createCertificateAuthority("testca2", 2048, 365, "rootca2.ericsson.com");
    }

    private GeneratedCert generateNewCert()
    {
        try
        {
            return CertificateTool.createCertificateSignedByRoot("testtest", 2048, 365, "client.ericsson.com", List.of(), this.generatedRootCa);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private GeneratedCert generateNewCert2()
    {
        try
        {
            return CertificateTool.createCertificateSignedByRoot("testtest2", 2048, 365, "client2.ericsson.com", List.of(), this.generatedRootCa2);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
