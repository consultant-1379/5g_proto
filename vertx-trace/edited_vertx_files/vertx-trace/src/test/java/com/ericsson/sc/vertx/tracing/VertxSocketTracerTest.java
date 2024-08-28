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
 * Created on: Oct 21, 2022
 *     Author: echfari
 */
package com.ericsson.sc.vertx.tracing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.sockettrace.RxSocketTraceHandler;
import com.ericsson.sc.sockettrace.TapcolTraceSink;
import com.ericsson.sc.vertx.trace.VertxSocketTracer;
import com.ericsson.sc.tapcol.TapCollectorReceiverTcp;
import com.ericsson.sc.tapcol.file.FilePacketSink;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.HelperHttp;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerPool;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Flowable;
import io.vertx.core.http.HttpVersion;
import io.vertx.reactivex.core.Vertx;
import reactor.core.publisher.Flux;

public class VertxSocketTracerTest
{
    private static final Logger log = LoggerFactory.getLogger(VertxSocketTracerTest.class);
    private static final String LOCAL_HOST = "127.0.0.50";
    private static final String CLIENT_CERTS_PATH = "src/test/resources/certificates/client";
    private static final String SERVER_CERTS_PATH = "src/test/resources/certificates/server";
    private static final String TRUST_CA_PATH = "src/test/resources/certificates/trustCA";
    private static final String CLIENT_CERT = "client-cert.pem";
    private static final String CLIENT_KEY = "client-key.pem";
    private static final String SERVER_CERT = "server-cert.pem";
    private static final String SERVER_KEY = "server-key.pem";
    private static final String TRUST_CA = "rootCA.pem";

    // timings are dependent to the
    // client/server and the bulk of request
    // sent every ms

    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();
    private final int localPort = 1111;
    private TapCollectorReceiverTcp tapcolReceiver;
    private RxSocketTraceHandler socketTraceHandler;

    @BeforeClass
    private void setupTracer()
    {
        final var fps = new FilePacketSink();// .combineWith(pkt->pkt(p->p.toString()).then());
        this.tapcolReceiver = new TapCollectorReceiverTcp(9999, //
                                                          "127.0.0.2",
                                                          fps, //
                                                          false,
                                                          Flux.empty());
        tapcolReceiver.start().block();
        TapcolTraceSink tapcolSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999);
        tapcolSink.init().blockingAwait();
        this.socketTraceHandler = new RxSocketTraceHandler(tapcolSink,500,dropped->log.info("****DROPPED PACKETS: {}",dropped));
        VertxSocketTracer.setGlobalTracer(this.socketTraceHandler);
    }

    @AfterClass
    private void cleanup() throws InterruptedException
    {
        Thread.sleep(5 * 1000);
        log.info("Dropped traces : {}", this.socketTraceHandler.getTotalPacketsDropped());
        tapcolReceiver.stop().block();
        log.info("Cleaning up {}, closing vertx", VertxSocketTracerTest.class);
        vertx.close();
        log.info("Done");
    }

    @Test(enabled = true)
    private void httpRequestWithoutTls() throws IOException
    {

        // Web server setup
        final var server = WebServer.builder()
                                    .withOptions(opts -> opts.setLogActivity(false)) //
                                    .withHost(LOCAL_HOST)
                                    .withPort(localPort)
                                    .build(vertx);
        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end("Non-TLS HTTP2 response");
        })));
        server.startListener().blockingAwait();
        assertServerUp();

        // Web client setup
        final var webClientProvider = WebClientProvider.builder().build(vertx);
        int responseCode = webClientProvider.getWebClient()
                                            .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                             .rxSend() //
                                                             .map(res -> res.statusCode()))
                                            .blockingGet();

        assertEquals(responseCode, 200);
        server.stopListener().blockingAwait();
    }

    @Test(enabled = true)
    private void httpRequestWithTls() throws IOException
    {
        // Web server with TLS
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA, 4);

        // Web client with TLS
        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA,8);

        final var responseCode = webClientProvider.getWebClient()
                                                  .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                                   .rxSend() //
                                                                   .map(res -> res.statusCode()))
                                                  .blockingGet();

        assertTrue(responseCode == 200);
        webClientProvider.close().blockingAwait();

        server.stopListener().blockingAwait();
    }

    @Test(enabled = true)
    private void stressTls() throws IOException
    {
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA, 4);
        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA,20);

        long cnt = 40000;

        // Lots of HTTP requests
        Flowable //
                .intervalRange(0, cnt, 0, 1, TimeUnit.MILLISECONDS) // 1000 packets/sec rate
                .flatMapSingle(tick ->
                {

                    return webClientProvider.getWebClient()
                                            .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + tick)
                                                             .ssl(true)
                                                             .rxSend() //
                                                             .map(res -> res.statusCode()));
                })
                .doOnNext(statusCode ->
                {
                    if (statusCode != 200)
                        throw new RuntimeException("Wrong result");
                })
                .ignoreElements()
                .blockingAwait();

        server.stopListener().blockingAwait();
    }

    private void copyCertFiles(long id,
                               String source,
                               String dest) throws IOException
    {

        final var destinationDir = Path.of(dest);

        Path sourceDir = Path.of(source, String.format("set%s", id)); // Points to set1, set2, etc

        Files.walk(sourceDir).forEach(sourcePath ->
        {
            if (!sourcePath.equals(sourceDir))
            {
                try
                {
                    Path targetPath = destinationDir.resolve(sourceDir.relativize(sourcePath));
                    log.debug("Copying {} --> {}", sourcePath, targetPath);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (DirectoryNotEmptyException dne)
                {
                    log.info("Directory not empty", dne);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException("Failed to copy certificate files", ex);
                }
            }
        });

    }

    private WebServerPool initializeServerWithTls(String certFileName,
                                                  String keyFileName,
                                                  String trustCaFileName,
                                                  int serverCount
                                                  ) throws IOException
    {
        // Create temp directory
        final var servercerts = Files.createTempDirectory("servercerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Copy certs to temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, servercerts);
        log.info("servercerts dir: {}", servercerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        // Server with 1 replica
        final var server = buildTlsServer(serverCount, servercerts, trustca, certFileName, keyFileName, trustCaFileName);
        server.startListener().blockingAwait();
        assertServerUp();
        return server;

    }

    private WebClientProvider initializeClientWithTls(String certFileName,
                                                      String keyFileName,
                                                      String trustCaFileName,
                                                      int connectionPoolSize
                                                      ) throws IOException
    {
        // Create temp directory
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Copy certs to temp
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);

        final var watchBuild = SipTlsCertWatch.builder() //
                                              .withCertFileName(certFileName)
                                              .withKeyFileName(keyFileName)
                                              .withTrustedCertFileName(trustCaFileName);

        return WebClientProvider.builder() //
                                .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts), watchBuild.buildTrustedCert(trustca)))
                                .withOptions(opts -> opts.setTrustAll(true)
                                                         .setVerifyHost(false)
                                                         .setHttp2MaxPoolSize(connectionPoolSize)
                                                         .setProtocolVersion(HttpVersion.HTTP_2)
                                                         .setUseAlpn(true)
                                                         .setHttp2ClearTextUpgrade(false))
                                .build(vertx);
    }

    private WebServerPool buildTlsServer(int replicas,
                                         String certPath,
                                         String trustCaPath,
                                         String certFileName,
                                         String keyFileName,
                                         String trustCaFileName)
    {

        final var builder = WebServer.builder() //
                                     .withHost(LOCAL_HOST)
                                     .withPort(localPort)
                                     .withOptions(opts -> opts.setLogActivity(false));

        final var server = builder.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.builder() //
                                                                                              .withKeyFileName(keyFileName)
                                                                                              .withCertFileName(certFileName)
                                                                                              .buildKeyCert(certPath),
                                                                               SipTlsCertWatch.builder() //
                                                                                              .withTrustedCertFileName(trustCaFileName)
                                                                                              .buildTrustedCert(trustCaPath),
                                                                               AdvancedTlsX509TrustManager.newBuilder().build()))
                                  .build(vertx, replicas);
        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end("ACK " + rc.request().path());
        })));
        return server;
    }

    private void assertServerUp()
    {
        assertFalse(HelperHttp.isPortAvailable(localPort, LOCAL_HOST));
    }

    public static void main(String[] args)
    {

        final var fps = new FilePacketSink();
        final var tapcolReceiver = new TapCollectorReceiverTcp(9999, //
                                                               "127.0.0.2",
                                                               fps, //
                                                               false,
                                                               Flux.empty());
        tapcolReceiver.start().block().onDispose().block();
    }

}
