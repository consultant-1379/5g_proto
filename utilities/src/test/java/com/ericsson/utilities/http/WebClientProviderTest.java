package com.ericsson.utilities.http;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.multipart.MultipartForm;

public class WebClientProviderTest
{
    private static final Logger log = LoggerFactory.getLogger(WebClientProviderTest.class);
    private static final String LOCAL_HOST = "127.0.0.50";
    private static final String CLIENT_CERTS_PATH = "src/test/resources/certificates/client";
    private static final String SERVER_CERTS_PATH = "src/test/resources/certificates/server";
    private static final String TRUST_CA_PATH = "src/test/resources/certificates/trustCA";
    private static final String CLIENT_CERT = "client-cert.pem";
    private static final String CLIENT_KEY = "client-key.pem";
    private static final String SERVER_CERT = "server-cert.pem";
    private static final String SERVER_KEY = "server-key.pem";
    private static final String TRUST_CA = "rootCA.pem";
    private static final RetryFunction DEFAULT_RETRY_FUNCTION = new RetryFunction().withDelay(1 * 1000L) // retry after 1
                                                                                                         // seconds
                                                                                   .withRetries(10); // give up after 10 sec
    TlsKeylogger kl = new TlsKeylogger("/tmp/testfifo", -1, 1000);

    // timings are dependent to the
    // client/server and the bulk of request
    // sent every ms

    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();
    private final int localPort = HelperHttp.getAvailablePort(LOCAL_HOST);

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        log.info("Start test: {}", method);
        assertServerDown();
    }

    @Test(enabled = true, groups = "functest")
    private void httpRequestWithoutTls() throws IOException
    {
        // Web server setup
        final var server = WebServer.builder().withHost(LOCAL_HOST).withPort(localPort).build(vertx);
        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            log.debug("Server got request");
            rc.response().end();
        })));
        server.startListener().blockingAwait();
        assertServerUp();

        // Web client setup
        final var webClientProvider = WebClientProvider.builder().build(vertx);
        int responseCode = webClientProvider.getWebClient()
                                            .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                             .rxSend() //
                                                             .map(res -> res.statusCode())
                                                             .doOnSuccess(res -> log.info("Response for tick->{} = {}", 1, res))
                                                             .doOnError(err -> log.error("Error after request {}", 1, err)))
                                            .blockingGet();

        // Check if the response is OK(200)
        assertEquals(responseCode, 200);
        server.stopListener().blockingAwait();
        assertServerDown();
    }

    @Test(enabled = true, groups = "functest")
    private void httpRequestWithTls() throws IOException
    {
        // Web server with TLS
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Web client with TLS
        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA);

        final var responseCode = webClientProvider.getWebClient()
                                                  .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                                   .rxSend() //
                                                                   .map(res -> res.statusCode())
                                                                   .doOnSuccess(res -> log.info("Response for tick->{} = {}", 1, res))
                                                                   .doOnError(err -> log.error("Error after request {}", 1, err)))
                                                  .blockingGet();

        assertTrue(responseCode == 200);
        webClientProvider.close().blockingAwait();

        server.stopListener().blockingAwait();
        assertServerDown();
    }

    @Test(enabled = true, groups = "functest")
    private void stressNoChange() throws IOException
    {
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);
        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA);

        int cnt = 5000;

        // Lots of HTTP requests
        final var responsesLists = Flowable.intervalRange(0, cnt, 0, 1, TimeUnit.MILLISECONDS).flatMapSingle(tick ->
        {

            return webClientProvider.getWebClient()
                                    .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                     .ssl(true)
                                                     .rxSend() //
                                                     .map(res -> res.statusCode())
                                                     .doOnSuccess(res -> log.info("Response for tick->{} = {}", tick, res))
                                                     .doOnError(err -> log.error("Error after request {}", tick, err))
                                                     .timeout(100, TimeUnit.MILLISECONDS))
                                    .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                    .onErrorReturnItem(777);
        })
                                           .filter(statusCode -> statusCode != 200)
                                           .toList() //
                                           .blockingGet();

        assertTrue(responsesLists.stream().allMatch(result -> result == 200));

        server.stopListener().blockingAwait();
        assertServerDown();
    }

    @Test(enabled = true, groups = "functest")
    private void simpleCertificatesChange() throws IOException
    {
        // Create temp directories
        final var servercerts = Files.createTempDirectory("servercerts").toString();
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        final var server = buildTlsServer(1, servercerts, trustca, SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Copy certs from actual path to temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, servercerts);
        log.info("clientcerts dir: {}", clientcerts);
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        log.info("servercerts dir: {}", servercerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        server.startListener().blockingAwait();
        assertServerUp();

        // Web client TLS setup
        final var watchBuild = SipTlsCertWatch.builder().withCertFileName(CLIENT_CERT).withKeyFileName(CLIENT_KEY).withTrustedCertFileName(TRUST_CA);

        final var webClientProvider = WebClientProvider.builder() //
                                                       .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts),
                                                                                                    watchBuild.buildTrustedCert(trustca)))
                                                       .withOptions(opts -> opts.setTrustAll(true) //
                                                                                .setVerifyHost(false))
                                                       .build(vertx);

        // HTTP requests
        int cnt = 5000;
        final var responsesLists = Flowable.intervalRange(0, cnt, 0, 1, TimeUnit.MILLISECONDS).flatMapSingle(tick ->
        {

            if (tick == 2000)
            {
                this.copyCertFiles(2, CLIENT_CERTS_PATH, clientcerts); // Certificate change
            }

            return webClientProvider.getWebClient()
                                    .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                     .ssl(true)
                                                     .rxSend() //
                                                     .map(res -> res.statusCode())
                                                     .doOnSuccess(res -> log.info("Response for tick->{} = {}", tick, res))
                                                     .doOnError(err -> log.error("Error after request {}", tick, err))
                                                     .timeout(100, TimeUnit.MILLISECONDS))
                                    .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                    .onErrorReturnItem(777);
        })
                                           .toList() //
                                           .blockingGet();

        int totalResponses = responsesLists.size();
        int succesfull = responsesLists.stream().filter(result -> result == 200).collect(Collectors.toList()).size();
        int unsuccesfull = responsesLists.stream().filter(result -> result == 777).collect(Collectors.toList()).size();
        log.info("Total requests sent: {}", cnt);
        log.info("Total responses receivd: {}", totalResponses);
        log.info("Succesful/200 result: {}", succesfull);
        log.info("Unsuccesful/200 result: {}", unsuccesfull);
        assertTrue(unsuccesfull == 0);

        server.stopListener().blockingAwait();
        assertServerDown();
    }

    @Test(enabled = false)
    private void stressCertificateChange() throws IOException
    {
        // Create temp directories
        final var servercerts = Files.createTempDirectory("servercerts").toString();
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        final var server = buildTlsServer(1, servercerts, trustca, SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Copy certs from actual path to temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, servercerts);
        log.info("servercerts dir: {}", servercerts);
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        log.info("clientcerts dir: {}", clientcerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        server.startListener().blockingAwait();
        assertServerUp();

        // Web client TLS setup
        final var watchBuild = SipTlsCertWatch.builder().withCertFileName(CLIENT_CERT).withKeyFileName(CLIENT_KEY).withTrustedCertFileName(TRUST_CA);

        final var webClientProvider = WebClientProvider.builder() //
                                                       .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts),
                                                                                                    watchBuild.buildTrustedCert(trustca)))
                                                       .withOptions(opts -> opts.setTrustAll(true) //
                                                                                .setVerifyHost(false))
                                                       .build(vertx);

        final var client = webClientProvider.getWebClient();
        client.blockingGet();
        log.info("Starting traffic");

        // Change of certificates
        int cnt = 100 * 10;// = 10 seconds
        final var certChange = Flowable.interval(3, TimeUnit.SECONDS)
                                       .observeOn(Schedulers.io())
                                       .doOnNext(tick -> copyCertFiles(2, CLIENT_CERTS_PATH, clientcerts)) // Certs change
                                       .subscribe();

        AtomicInteger failed = new AtomicInteger();

        // HTTP traffic
        try
        {
            Flowable.intervalRange(0, cnt, 0, 10, TimeUnit.MILLISECONDS)
                    .onBackpressureDrop()
                    .flatMapSingle(tick -> client.flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                                  .ssl(true)
                                                                  .rxSend() //
                                                                  .map(res -> res.statusCode())),
                                   false,
                                   100

                    )
                    .filter(sc -> sc != 200)
                    .doOnNext(nxt -> failed.incrementAndGet())
                    .ignoreElements() //
                    .blockingAwait();
        }
        finally
        {
            certChange.dispose();
            webClientProvider.close().blockingGet();
            server.stopListener().blockingAwait();
        }
        int unsuccesfull = failed.get();
        assertTrue(unsuccesfull == 0);
        assertServerDown();
    }

    @Test(enabled = true, groups = "functest")
    private void clientCertWithDifferentPrivateKey() throws IOException
    {
        // TLS setup
        final var webClientProvider = initializeClientWithTls("diff-key-client-cert.pem", CLIENT_KEY, TRUST_CA);
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);

        assertThrows(SSLHandshakeException.class, () ->
        {
            try
            {
                webClientProvider.getWebClient().flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1).rxSend()).blockingGet();
            }
            catch (RuntimeException e)
            {
                throw e.getCause();
            }
            finally
            {
                webClientProvider.close().blockingGet();
                server.stopListener().blockingAwait();
                assertServerDown();
            }
        });

    }

    @Test(enabled = true, groups = "functest")
    private void clientCertsWithDifferentCaSignature() throws IOException
    {
        // TLS setup
        final var webClientProvider = initializeClientWithTls("diff-ca-client-cert.pem", "diff-ca-client-key.pem", TRUST_CA);
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);
        assertFalse(webClientProvider.getWebClient()
                                     .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1).rxSend())
                                     .ignoreElement()
                                     .blockingAwait(3, TimeUnit.SECONDS));

        webClientProvider.close().blockingGet();
        server.stopListener().blockingAwait();
        assertServerDown();

    }

    @Test(enabled = false, groups = "functest")
    public void wrongClientTrustCa() throws IOException
    {
        // Create temp directories
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Copy cert files to temp
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        log.info("clientcerts dir: {}", clientcerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        final var watchBuild = SipTlsCertWatch.builder().withCertFileName(CLIENT_CERT).withKeyFileName(CLIENT_KEY).withTrustedCertFileName("wrong-rootCA.pem");

        final var webClientProvider = WebClientProvider.builder() //
                                                       .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts),
                                                                                                    watchBuild.buildTrustedCert(trustca)))
                                                       .withOptions(opts -> opts.setTrustAll(false) // Set to false
                                                                                .setVerifyHost(false))
                                                       .build(vertx);

        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Check if client can perform an HTTP request
        assertFalse(webClientProvider.getWebClient()
                                     .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1).rxSend())
                                     .ignoreElement()
                                     .blockingAwait(2, TimeUnit.SECONDS));

        webClientProvider.close().blockingGet();
        server.stopListener().blockingAwait();
        assertServerDown();

    }

    @Test(enabled = true)
    private void clientScenario_01() throws IOException, InterruptedException
    {
        // Client and server with TLS
        final var server = initializeServerWithTls(SERVER_CERT, SERVER_KEY, TRUST_CA);
        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA);

        int responseCode = webClientProvider.getWebClient()
                                            .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + 1)
                                                             .ssl(true)
                                                             .rxSend() //
                                                             .map(res -> res.statusCode())
                                                             .doOnSuccess(res -> log.info("Response for tick->{}", 1))
                                                             .doOnError(err -> log.error("Error after request {}", 1, err))
                                                             .onErrorReturnItem(777))
                                            .retry(20) //
                                            .blockingGet();

        log.info("GET Request response is {}", responseCode);

        server.stopListener().blockingAwait();
        webClientProvider.close().blockingAwait();
        assertServerDown();

    }

    @Test(enabled = false)
    private void clientScenario_02() throws IOException, InterruptedException
    {
        // Create temp directories
        final var servercerts = Files.createTempDirectory("servercerts").toString();
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Server with TLS and 1 replica
        final var server = buildTlsServer(1, servercerts, trustca, SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Copy files into temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, servercerts);
        log.info("servercerts dir: {}", servercerts);
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        log.info("clientcerts dir: {}", clientcerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        server.startListener().blockingAwait();
        assertServerUp();

        final var watchBuild = SipTlsCertWatch.builder().withCertFileName(CLIENT_CERT).withKeyFileName(CLIENT_KEY).withTrustedCertFileName(TRUST_CA);

        final var webClientProvider = WebClientProvider.builder() //
                                                       .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts),
                                                                                                    watchBuild.buildTrustedCert(trustca)))
                                                       .withOptions(opts -> opts.setTrustAll(true) //
                                                                                .setVerifyHost(false))
                                                       .build(vertx);

        int cnt = 15000;
        final var clientSide = Flowable.intervalRange(0, cnt, 0, 1, TimeUnit.MILLISECONDS).flatMapSingle(tick ->
        {
            if (tick == 3000)
            {
                log.info("===================== TICK ====================");
                this.copyCertFiles(2, CLIENT_CERTS_PATH, clientcerts);
            }

            return webClientProvider.getWebClient()
                                    .flatMap(wc -> wc.get(localPort, LOCAL_HOST, "/test/" + tick)
                                                     .rxSend()
                                                     .map(res -> res.statusCode())
                                                     .doOnSuccess(ar -> log.debug("Result code: {}", ar)));
        })
                                       .timeout(1, TimeUnit.SECONDS)
                                       .retryWhen(new RetryFunction().withDelay(2 * 1000L) // retry after 2 seconds
                                                                     .withRetries(5) // give up after 10 sec
                                                                     .create())
                                       .doOnError(e -> log.error("Error occured while sending he request", e))
                                       .onErrorReturnItem(777)
                                       .filter(statusCode -> statusCode != 200)
                                       .toList()
                                       .blockingGet();
        log.info("Test finished");
        server.stopListener().blockingAwait();
        assertServerDown();

        log.debug("Failed requests: {}", clientSide.size());
    }

    @Test(enabled = false)
    private void multipartServerTest() throws IOException, InterruptedException
    {
        // Multipart server setup
        final var multipartServer = multipartServer();
        multipartServer.startListener().blockingAwait();

        // Upload some files to multipart server
        MultipartForm form = MultipartForm.create()
                                          .attribute("title", "ericsson-csa")
                                          .textFileUpload("file",
                                                          "ericsson-csa" + ".json",
                                                          "src/test/resources/schemapath" + "/" + "ericsson-csa.json",
                                                          "application/json")
                                          .binaryFileUpload("yangArchive",
                                                            "ericsson-csa.tar.gz",
                                                            "src/test/resources/schemapath" + "/" + "ericsson-csa.tar.gz",
                                                            "application/gzip");

        final var webClientProvider = initializeClientWithTls(CLIENT_CERT, CLIENT_KEY, TRUST_CA);

        TimeUnit.SECONDS.sleep(5);

        webClientProvider.getWebClient().flatMap(wc ->
        {
            log.info("LATEST UPDATED WC IS {}", wc);
            return wc.put(5010, "localhost", "/cm/api/v1.2/schemas/ericsson-csa")//
                     .ssl(true)
                     .rxSendMultipartForm(form)
                     .map(response ->
                     {
                         log.info("response body is {}", response.bodyAsString());
                         log.info("response code is {}", response.statusCode());
                         return response;
                     })
                     .doOnError(e ->
                     {
                         log.error("TESTERROR", e);
                     })
                     .doOnSubscribe(value ->
                     {
                         log.info("SUBSCRIBED {}", value);

                     });
        })
                         .retryWhen(new RetryFunction().withDelay(1 * 1000L) // retry after 2
                                                       // seconds
                                                       .withRetries(5) // give up after 5 sec
                                                       .create())
                         .blockingGet();

        multipartServer.stopListener().blockingAwait();
    }

    @Test(enabled = true)
    private void webServerStopTest() throws IOException
    {
        // Temp directories
        final var serverCertPath = Files.createTempDirectory("cert").toString();
        final var trustCaPath = Files.createTempDirectory("ca").toString();

        // Server with TLS and 1 replica
        final var server = buildTlsServer(1, serverCertPath, trustCaPath, SERVER_CERT, SERVER_KEY, TRUST_CA);

        // Copy certs to temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, serverCertPath);
        log.info("servercerts dir: {}", serverCertPath);
        this.copyCertFiles(1, TRUST_CA_PATH, trustCaPath);
        log.info("trustca dir: {}", trustCaPath);

        // Check if starting/stopping the server doesn't throw any exceptions
        server.startListener().blockingAwait();
        server.stopListener().blockingAwait();
        assertServerDown();

    }

    private String readFromInputStream(InputStream inputStream) throws IOException
    {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
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
                                                  String trustCaFileName) throws IOException
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
        final var server = buildTlsServer(1, servercerts, trustca, certFileName, keyFileName, trustCaFileName);
        server.startListener().blockingAwait();
        assertServerUp();
        return server;

    }

    private WebClientProvider initializeClientWithTls(String certFileName,
                                                      String keyFileName,
                                                      String trustCaFileName) throws IOException
    {
        // Create temp directory
        final var clientcerts = Files.createTempDirectory("clientcerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Copy certs to temp
        this.copyCertFiles(1, CLIENT_CERTS_PATH, clientcerts);
        log.info("clientcerts dir: {}", clientcerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        final var watchBuild = SipTlsCertWatch.builder().withCertFileName(certFileName).withKeyFileName(keyFileName).withTrustedCertFileName(trustCaFileName);

        return WebClientProvider.builder() //
                                .withDynamicTls(DynamicTlsCertManager.create(watchBuild.buildKeyCert(clientcerts), watchBuild.buildTrustedCert(trustca)))
                                .withOptions(opts -> opts.setTrustAll(true).setVerifyHost(false))
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
                                     .withPort(localPort);

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
            rc.response().end();
        })));
        return server;
    }

    private WebServer multipartServer() throws InterruptedException, IOException
    {
        // Create temp directories
        final var servercerts = Files.createTempDirectory("servercerts").toString();
        final var trustca = Files.createTempDirectory("trustca").toString();

        // Copy certs into temp
        this.copyCertFiles(1, SERVER_CERTS_PATH, servercerts);
        log.info("servercerts dir: {}", servercerts);
        this.copyCertFiles(1, TRUST_CA_PATH, trustca);
        log.info("trustca dir: {}", trustca);

        // Multipart server setup with TLS
        final var builder = WebServer.builder() //
                                     .withHost("127.0.0.1")
                                     .withPort(5010);

        final var server = builder.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.builder() //
                                                                                              .withKeyFileName(SERVER_KEY)
                                                                                              .withCertFileName(SERVER_CERT)
                                                                                              .buildKeyCert(servercerts),
                                                                               SipTlsCertWatch.builder() //
                                                                                              .withTrustedCertFileName(TRUST_CA)
                                                                                              .buildTrustedCert(trustca),
                                                                               AdvancedTlsX509TrustManager.newBuilder().build()))
                                  .build(vertx);

        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.route("/cm/api/v1.2/schemas/ericsson-csa").handler(rc ->
        {
            try
            {

                log.info("multimapsize {}", rc.request().params().size());
                rc.request().setExpectMultipart(true);
                log.info("is expect multipart {}", rc.request().isExpectMultipart());
                MultiMap attributes = rc.request().formAttributes();
                log.info("attribute.size() {}", attributes.size());
                log.info("attribute {}", attributes);
                var uploads = rc.fileUploads();
                // List<FileUpload> uploads = rc.fileUploads();
                uploads.forEach(f ->
                {
                    log.info("FILENAME {} UPLOADED", f.fileName());
                    ClassLoader classLoader = getClass().getClassLoader();
                    InputStream inputStream = classLoader.getResourceAsStream(f.uploadedFileName());
                    try
                    {
                        String data = readFromInputStream(inputStream);
                        log.info("FileContent {}", data);
                    }
                    catch (IOException e)
                    {
                        log.error("error reading uploaded file");
                    }
                });

                log.info("The title read is: {}", attributes.get("title"));
                rc.response().setStatusCode(200);
            }
            catch (Exception e)
            {
                rc.response().setStatusCode(500);
            }
            finally
            {
                rc.response().end();
            }
        })));
        return server;
    }

    private void assertServerDown()
    {
        assertTrue(HelperHttp.isPortAvailable(localPort, LOCAL_HOST));
    }

    private void assertServerUp()
    {
        assertFalse(HelperHttp.isPortAvailable(localPort, LOCAL_HOST));
    }

    @AfterMethod
    public void afterMethod(ITestResult result)
    {
        log.info("Finish test: {}", result.getMethod().getMethodName());
        assertServerDown();
    }

    @AfterClass
    void cleanup()
    {
        log.info("Cleaning up {}, closing vertx", WebClientProviderTest.class);
        vertx.close();
        log.info("Done");
    }

}
