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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;

/**
 * 
 */
public class CertificateAlarmHandlerClient
{
    private static final Logger log = LoggerFactory.getLogger(CertificateAlarmHandlerClient.class);

    private final Flowable<ListNotification> certificateWatcher;
    private final WebClientProvider webClientProvider;
    private final String certHandlerSvc;
    private final int certHandlerport;
    private final ObjectMapper om = Jackson.om().setSerializationInclusion(Include.NON_EMPTY);

    public CertificateAlarmHandlerClient(final String certHandlerSvc,
                                         final int certHandlerport,
                                         final Flowable<ListNotification> certificateWatcher)
    {
        this.certHandlerSvc = certHandlerSvc;
        this.certHandlerport = certHandlerport;
        this.certificateWatcher = certificateWatcher;

        this.webClientProvider = WebClientProvider.builder().build(VertxInstance.get());
    }

    public Completable run()
    {
        this.sendWarmupNotification();
        return this.certificateWatcher.map(config -> new JsonObject(this.om.writeValueAsString(config)))
                                      .distinctUntilChanged()
                                      .debounce(2, TimeUnit.SECONDS)
                                      .observeOn(Schedulers.newThread())
                                      .flatMapSingle(this::sendNotification) //
                                      .doOnNext(resp ->
                                      {
                                          log.info("POST request response code: {}", resp.statusCode());

                                          if (resp.statusCode() != HttpResponseStatus.OK.code())
                                          {
                                              log.error("POST response message: {}", resp.statusMessage());
                                          }
                                      })
                                      .doOnError(err -> log.error("POST request has failed.", err))
                                      .map(HttpResponse::statusCode)
                                      .onErrorReturnItem(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) // this is here, in order to avoid restarting
                                                                                                          // container due to connection to sc-manager and
                                                                                                          // disturb traffic
                                      .ignoreElements();
    }

    public Completable stop()
    {
        return this.webClientProvider.close();
    }

    private static final String CERT_ALARM_HANDLER_V1 = "/cert-alarm-handler/v1";
    private static final String NF = EnvVars.get("NF");
    private static final String CERTS_UPDATE = "/certs-update";
    private static final String URI = new StringBuilder(CERT_ALARM_HANDLER_V1).append("/").append(NF).append(CERTS_UPDATE).toString();

    private Single<HttpResponse<Buffer>> sendNotification(JsonObject body)
    {
        log.info("Notification will be sent using uri {} and body:{}", URI, body);

        return this.webClientProvider.getWebClient()
                                     .flatMap(webClient -> webClient.post(this.certHandlerport, this.certHandlerSvc, URI) //
                                                                    .rxSendJsonObject(body))
                                     .retryWhen(new RetryFunction().withDelay(2 * 1000L).withRetries(20).create());
    }

    private void sendWarmupNotification()
    {
        // First WebClient request is slow compared to subsequent requests
        // Send a warmup request before starting the flowable to avoid ThreadBlocked
        // errors on certnotifier startup.

        var dummy = "{\"asymmetric-keys\":,\"trusted-authorities\":}";
        log.debug("Warming up client with dummy Notification using uri {} and body:{}", URI, dummy);

        this.webClientProvider.getWebClient()
                              .flatMap(webClient -> webClient.post(this.certHandlerport, this.certHandlerSvc, URI)
                                                             .rxSendJsonObject(new JsonObject(this.om.writeValueAsString(dummy))));

    }
}
