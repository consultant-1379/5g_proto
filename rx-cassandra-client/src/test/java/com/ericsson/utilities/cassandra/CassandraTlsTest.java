/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 29, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.cassandra;

import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.TrustedCert;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

public class CassandraTlsTest
{

    private static final String LOCAL_DC_NAME = "datacenter1";
    private static final String CERT_PATH = Path.of(Paths.get("").toAbsolutePath().toString(), "src/test/resources/certificates").toString();
    private static final Logger log = LoggerFactory.getLogger(CassandraTlsTest.class);
    private final CassandraTestServer testBed = new CassandraTestServer();

    @BeforeClass
    public void beforeClass() throws IOException
    {
        log.info("Executing Before Class... ");
        testBed.startCassandra();
    }

    private ProgrammaticDriverConfigLoaderBuilder baseConfig()
    {
        return DriverConfigLoader.programmaticBuilder() //
                                 .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(testBed.getContactPoint()))
                                 .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                 .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, LOCAL_DC_NAME)
                                 .withString(DefaultDriverOption.REQUEST_CONSISTENCY, "ONE")
                                 .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                 .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, testBed.getUsername())
                                 .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, testBed.getPassword())
                                 .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                                 .withBoolean(EnhancedDriverOption.TLS_ENABLED, true)
                                 .withBoolean(EnhancedDriverOption.VERIFY_HOST, false) // disable hostname verification
        ;

    }

    private DynamicTlsCertManager dynamicTls(boolean erroneousCa)
    {
        final Flowable<KeyCert> keyCertFlow = Flowable.interval(2000, TimeUnit.MILLISECONDS)
                                                      .map(tick -> (int) (tick.longValue() % 3))
                                                      .doOnNext(id -> log.info("Configuration change,client cert {}", id))
                                                      .map(id -> new KeyCert()
                                                      {

                                                          @Override
                                                          public String getPrivateKey()
                                                          {
                                                              try
                                                              {
                                                                  return Files.readString(Path.of(CERT_PATH, String.format("set1/client%s/key.pem", id + 1)));
                                                              }
                                                              catch (IOException e)
                                                              {
                                                                  throw new UncheckedIOException(e);
                                                              }
                                                          }

                                                          @Override
                                                          public String getCertificate()
                                                          {
                                                              try
                                                              {
                                                                  return Files.readString(Path.of(CERT_PATH, String.format("set1/client%d/cert.pem", id + 1)));
                                                              }
                                                              catch (IOException e)
                                                              {
                                                                  throw new UncheckedIOException(e);
                                                              }
                                                          }
                                                      });
        final Flowable<TrustedCert> trustedCertFlow = Flowable.<TrustedCert>just(() ->
        {
            try
            {
                return List.of(Files.readString(Path.of(CERT_PATH, erroneousCa ? "set1/wrong-rootCA.pem" : "set1/rootCA.pem")));
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        });
        return DynamicTlsCertManager.create(() -> keyCertFlow, () -> trustedCertFlow);
    }

    @Test(groups = "functest")
    public void certificateChangeTest()
    {
        CompositeDisposable disps = new CompositeDisposable();

        final var rxCassandra = RxSession //
                                         .builder()
                                         .withConfig(baseConfig().build()) //
                                         .withDynamicTls(dynamicTls(false))
                                         .build();

        try
        {
            Flowable.interval(100, TimeUnit.MILLISECONDS)
                    .onBackpressureBuffer()
                    .take(80)
                    .flatMapSingle(tick -> rxCassandra.sessionHolder()
                                                      .flatMap(sh -> sh.executeReactive(SimpleStatement.newInstance("SELECT host_id FROM system.local"))
                                                                       .doOnNext(e -> log.info("Received result", e))
                                                                       .toList()),
                                   false,
                                   100)
                    .blockingSubscribe();
        }
        catch (Exception ex)
        {
            log.error("Exception received ", ex);
            throw new RuntimeException(ex);
        }
        finally
        {
            rxCassandra.close();
            disps.dispose();
        }
    }

    @Test(groups = "functest")
    public void tlsFailTest()
    {
        final var failedConfig = baseConfig().withBoolean(EnhancedDriverOption.VERIFY_HOST, true) // Enable hostname verification
                                             .build();
        final var rxCassandra = RxSession.builder().withConfig(failedConfig).withDynamicTls(dynamicTls(false)).withInitTimeoutMillis(5 * 1000).build();
        try
        {
            final var error = rxCassandra.sessionHolder().ignoreElement().blockingGet();
            assertTrue(error instanceof TimeoutException);
        }
        finally
        {
            rxCassandra.close();
        }
    }

    @Test(groups = "functest")
    public void tlsFailInitConnectionTest()
    {
        final var failedConfig = baseConfig().withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, false) // No retries, fail immediately
                                             .withBoolean(EnhancedDriverOption.VERIFY_HOST, true) // Enable hostname verification
                                             .build();
        final var rxCassandra = RxSession.builder().withConfig(failedConfig).withDynamicTls(dynamicTls(false)).withInitRetries(1).build();
        try
        {
            assertThrows(AllNodesFailedException.class, () -> rxCassandra.sessionHolder().ignoreElement().blockingAwait());
        }
        finally
        {
            rxCassandra.close();
        }
    }

    @Test(groups = "functest")
    public void tlsFailInitConnectionTest2()
    {
        final var failedConfig = baseConfig().withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, false) // No retries, fail immediately
                                             .withBoolean(EnhancedDriverOption.VERIFY_HOST, false) // Enable hostname verification
                                             .build();
        final var rxCassandra = RxSession.builder().withConfig(failedConfig).withDynamicTls(dynamicTls(true)).withInitRetries(1).build();
        try
        {
            rxCassandra.sessionHolder().ignoreElement().blockingAwait();
            fail("No exception thrown");
        }
        catch (AllNodesFailedException e)
        {
            final var err = (SSLHandshakeException) e.getAllErrors().values().iterator().next().get(0).getCause().getCause().getCause();
            log.info("err:", err);
            assertTrue(err.getCause().getMessage().contains("PKIX path validation failed"));
        }
        finally
        {
            rxCassandra.close();
        }
    }

    @AfterClass
    void cleanup()
    {
        this.testBed.stopCassandra();
    }

}
