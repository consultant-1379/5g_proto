package com.ericsson.sc.proxyal.service;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxInstance;

class PvtbServiceTest
{
    private static final Logger log = LoggerFactory.getLogger(PvtbApiService.class);
    private static final String PVTB_RESPONSE_POSITIVE = "{\"config\":{\"protocols\":[\"raw-sbi\", \"raw-http\"]}}";
    private static final String PVTB_RESPONSE_NEGATIVE = "{\"config\":{\"protocols\":[\"raw-bi\", \"raw-http\"]}}";
    private static final String PVTB_RESPONSE_EMPTY = "{}";
    private static final String PVTB_RESPONSE_DUMMY = "abcde";
    private static final String PVTB_PROTOCOL = "raw-sbi";
    private static final String WEBSERVER_URI = "/api/v1/config/scp/scp-pod";
    private static final String TEST_HOST = "127.0.0.1";
    private static final Integer TEST_PORT = 8888;
    private static final String DOMAIN = "scp";
    private static final String POD_NAME = "scp-pod";

    @Test(groups = "functest", enabled = true)
    void testPositiveResponseFromPvtb()
    {
        final var mockServer = startServer(WEBSERVER_URI, PVTB_RESPONSE_POSITIVE, 200, 1);
        final var pvtbService = new PvtbApiService(TEST_HOST, TEST_PORT, DOMAIN, PVTB_PROTOCOL, POD_NAME, 1000L, VertxInstance.get());

        var result = pvtbService.requestPvtbConfig().andThen(pvtbService.getPvtbConfigs().map(res -> res.get().getIsConfigured())).blockingFirst();

        assertTrue(result);
        stopServer(mockServer);
    }

    @Test(groups = "functest", enabled = true)
    void testNegativeResponseFromPvtb()
    {
        final var mockServer = startServer(WEBSERVER_URI, PVTB_RESPONSE_NEGATIVE, 200, 1);
        final var pvtbService = new PvtbApiService(TEST_HOST, TEST_PORT, DOMAIN, PVTB_PROTOCOL, POD_NAME, 1000L, VertxInstance.get());

        var result = pvtbService.requestPvtbConfig().andThen(pvtbService.getPvtbConfigs().map(res -> res.get().getIsConfigured())).blockingFirst();

        assertFalse(result);
        stopServer(mockServer);
    }

    @Test(groups = "functest", enabled = true)
    void testEmptyResponseToPvtb()
    {
        final var mockServer = startServer(WEBSERVER_URI, PVTB_RESPONSE_EMPTY, 200, 1);
        final var pvtbService = new PvtbApiService(TEST_HOST, TEST_PORT, DOMAIN, PVTB_PROTOCOL, POD_NAME, 1000L, VertxInstance.get());

        var result = pvtbService.requestPvtbConfig().andThen(pvtbService.getPvtbConfigs().map(res -> res.get().getIsConfigured())).blockingFirst();

        assertFalse(result);
        stopServer(mockServer);
    }

    @Test(groups = "functest", enabled = true)
    void testDummyResponseToPvtb()
    {
        final var mockServer = startServer(WEBSERVER_URI, PVTB_RESPONSE_DUMMY, 200, 1);
        final var pvtbService = new PvtbApiService(TEST_HOST, TEST_PORT, DOMAIN, PVTB_PROTOCOL, POD_NAME, 1000L, VertxInstance.get());

        var result = pvtbService.requestPvtbConfig().andThen(pvtbService.getPvtbConfigs().map(res -> res.get().getIsConfigured())).blockingFirst();

        assertFalse(result);
        stopServer(mockServer);
    }

    @Test(groups = "functest", enabled = true)
    void testErrorCodeStatusToPvtb()
    {
        final var mockServer = startServer(WEBSERVER_URI, PVTB_RESPONSE_EMPTY, 404, 1);
        final var pvtbService = new PvtbApiService(TEST_HOST, TEST_PORT, DOMAIN, PVTB_PROTOCOL, POD_NAME, 1000L, VertxInstance.get());

        var result = pvtbService.requestPvtbConfig().andThen(pvtbService.getPvtbConfigs().map(res -> res.get().getIsConfigured())).blockingFirst();

        assertFalse(result);
        stopServer(mockServer);
    }

    private WebServer startServer(String path,
                                  String message,
                                  Integer statusCode,
                                  Integer timeout)
    {
        var webServerExt = WebServer.builder()
                                    .withHost(TEST_HOST)
                                    .withPort(TEST_PORT)
                                    .withOptions(options -> options.getInitialSettings())
                                    .build(VertxInstance.get());
        webServerExt.configureRouter(router ->
        {
            router.route(path).handler((rc) ->
            {
                log.info("Request came to server");
                try
                {
                    log.info("Request: Delay for {}(s)", timeout);
                    Thread.sleep(timeout * 1000);
                }
                catch (InterruptedException e)
                {
                    log.error("Error received {}", e);
                    Thread.currentThread().interrupt();
                }
                rc.response().putHeader("content-type", "text/html").setStatusCode(statusCode).end(message);
            });
        });
        webServerExt.startListener().blockingAwait();
        return webServerExt;
    }

    private void stopServer(WebServer ws)
    {
        ws.getRouter().clear();
        ws.shutdown().blockingGet();
        log.info("Webserver closed");
    }
}