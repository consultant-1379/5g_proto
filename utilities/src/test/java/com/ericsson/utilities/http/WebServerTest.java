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
 * Created on: Oct 7, 2020
 *     Author: echfari
 */
package com.ericsson.utilities.http;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

public class WebServerTest
{

    private static final Logger log = LoggerFactory.getLogger(WebServerTest.class);
    private static final String LOCAL_HOST = "127.0.0.69";
    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();

    private WebServer buildServer(int port)
    {
        final var srv = WebServer.builder() //
                                 .withHost(LOCAL_HOST)
                                 .withPort(port)
                                 .build(vertx);
        srv.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end();
            // Observable.timer(500, TimeUnit.MILLISECONDS).doOnNext(tick ->
            // rc.response().end()).subscribe();
        }));
        return srv;
    }

    @Test(groups = "functest", enabled = true)
    public void shutdownTest()
    {
        final int localPort = HelperHttp.getAvailablePort(LOCAL_HOST);
        int cnt = 10000;
        AtomicInteger ans = new AtomicInteger(0);
        final var server2 = buildServer(localPort);
        server2.startListener().blockingAwait();

        final var server1 = buildServer(localPort);
        server1.startListener().blockingAwait();

        /*
         * 
         * HttpClient httpClient = HttpClient.newHttpClient(); final var request =
         * HttpRequest.newBuilder()
         * .uri(URI.create("http://"+LOCAL_HOST+":"+LOCAL_PORT+"/test")) .build();
         */

        WebClient client = WebClient.create(vertx,
                                            new WebClientOptions() //
                                                                  .setProtocolVersion(HttpVersion.HTTP_2)
                                                                  .setHttp2ClearTextUpgrade(false)
                                                                  .setHttp2MaxPoolSize(4)
                                                                  .setDefaultHost(LOCAL_HOST)
                                                                  .setDefaultPort(server2.actualPort()));

        final var clientSide = Flowable.range(0, cnt).flatMapSingle(tick ->
        {

            return client.get("/test/" + tick)
                         .rxSend() //
                         .map(res -> res.statusCode())
                         .doAfterSuccess(res ->
                         {
                             final var count = ans.incrementAndGet();
                             if (count == 10)
                             {
                                 Completable.complete().andThen(server1.shutdown()).subscribe(() ->
                                 {
                                 }, err -> log.error("Unexpected error during server shutdown", err));
                             }
                         })
                         .doOnError(err -> log.error("Error after request {}: {}", tick, err.toString()))
                         .retry(20) //
                         .onErrorReturnItem(777);
        })
                                       .filter(statusCode -> statusCode != 200)
                                       .toList() //
                                       .blockingGet();

        log.info("Stopping server 1");
        server1.stopListener().blockingAwait();
        log.info("Stopping server 2");
        server2.stopListener().blockingAwait();
        log.info("Failed requests: {}", clientSide);
        client.close();
        assertEquals(clientSide.size(), 0); // No failures

    }

    @AfterClass
    void cleanup()
    {
        log.info("Cleaning up {}, closing vertx", WebServerTest.class);
        vertx.close();
        log.info("Done");
    }
}
