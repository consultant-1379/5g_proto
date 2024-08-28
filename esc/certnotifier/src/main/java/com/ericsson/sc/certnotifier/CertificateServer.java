/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 4, 2020
 *     Author: eaopmrk
 */

package com.ericsson.sc.certnotifier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * 
 */
public class CertificateServer
{
    private static final Logger log = LoggerFactory.getLogger(CertificateServer.class);

    private final RouterHandler routerHandler;
    private static final String CERTNOTIFIER_V1 = "/cert-notifier/v1";
    private static final String ALLCERTS = "/all-certs";
    private static final ObjectMapper om = Jackson.om().setSerializationInclusion(Include.NON_EMPTY);

    private final BehaviorSubject<ListNotification> certificates = BehaviorSubject.create();
    private final Observable<ListNotification> notifications;

    public CertificateServer(RouterHandler routerHandler,
                             Flowable<ListNotification> notifications)
    {
        this.routerHandler = routerHandler;
        this.setupRouter(CERTNOTIFIER_V1 + ALLCERTS);
        this.notifications = notifications.toObservable();
    }

    public Completable start()
    {
        return Completable.complete()
                          .andThen(routerHandler.startListener())
                          .andThen(Completable.fromAction(() -> this.notifications.subscribe(this.certificates)));
    }

    public Completable stop()
    {
        return Completable.complete().andThen(routerHandler.stopListener().onErrorComplete());
    }

    private void setupRouter(final String allCertsUri)
    {
        this.routerHandler.configureRouter(router -> router.get(allCertsUri).handler(this::handleGetAllCertificates));
    }

    private void handleGetAllCertificates(final RoutingContext routingContext)
    {
        this.certificates.map(cert -> new JsonObject(om.writeValueAsString(cert)))
                         .flatMapSingle(cert -> Single.just(cert.toString()))
                         .doOnNext(cert -> log.info("Sending response {}", cert))//
                         .take(1)
                         .doOnError(e -> log.warn("Error encountered: ", e))
                         .onErrorReturnItem(StringUtils.EMPTY)
                         .doOnSubscribe(s -> log.info("all-certs get request has been received."))
                         .subscribe(cert ->
                         {
                             if (!cert.equals(StringUtils.EMPTY))
                             {
                                 routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(cert);
                             }
                             else
                             {
                                 log.warn("Bad request. Certificates were empty");
                                 routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                             }
                         }, e ->
                         {
                             log.warn("Bad request", e);
                             routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                         }, () -> log.info("all-certs get completed"));
    }

}
