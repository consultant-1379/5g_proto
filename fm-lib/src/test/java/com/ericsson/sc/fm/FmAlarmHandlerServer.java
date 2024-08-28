/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 24, 2022
 *     Author: ekoteva
 */

package com.ericsson.sc.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.functions.Predicate;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

/**
 * 
 */
public class FmAlarmHandlerServer
{
    private Vertx vertx;
    private WebServer server;
    private HttpResponseStatus statusCode;
    private String statusMessage;
    private static final Logger log = LoggerFactory.getLogger(FmAlarmHandlerServer.class);
    private JsonObject lastReqBody;

    public FmAlarmHandlerServer(String hostname,
                                Integer port,
                                Boolean tlsEnabled,
                                String keyCertPath,
                                String trustedCertPath,
                                Vertx vertx)
    {
        this.vertx = vertx == null ? VertxInstance.get() : vertx;
        var fmahserver = WebServer.builder().withHost(hostname).withPort(port);
        if (tlsEnabled)
            fmahserver.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(keyCertPath), //
                                                                   SipTlsCertWatch.trustedCert(trustedCertPath)));
        this.server = fmahserver.build(this.vertx);
        this.server = WebServer.builder().withHost(hostname).withPort(port).build(this.vertx);

        this.server.getRouter().route().handler(rc ->
        {
            BodyHandler.create();
            rc.request().bodyHandler(bh ->
            {
                this.lastReqBody = bh.toJsonObject();
            });
        });
        this.server.configureRouter(router -> router.post().handler(this::fiapi));

    }

    public Completable start()
    {
        return this.server.startListener() //
                          .doOnComplete(() -> log.info("FmAlarmServer successfully started"))
                          .doOnError(error -> log.error("Fatal error, shutting down FmAlarmServer", error))
                          .onErrorResumeNext(throwable -> this.stop().andThen(Completable.error(throwable)));
    }

    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };
        return Completable.complete() //
                          .andThen(this.server.stopListener() //
                                              .onErrorComplete(logErr));
    }

    public int getPort()
    {
        return this.server.actualPort();
    }

    public void setStatusCode(HttpResponseStatus statusCode)
    {
        this.statusCode = statusCode;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

    public JsonObject getLastReqBody()
    {
        return this.lastReqBody;
    }

    private void fiapi(final RoutingContext rc)
    {
        rc.response() //
          .setStatusCode(this.statusCode.code()) //
          .setStatusMessage(this.statusMessage) //
          .end();
    }

}
