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
 * Created on: Jul 21, 2022
 *     Author: ekoteva
 */

package com.ericsson.sc.fm;

import java.net.URI;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;

/**
 * Handler needed for the client requests towards Alarm Handler service using
 * the FIAPI
 */
public class FmAlarmHandler
{
    private static final Logger log = LoggerFactory.getLogger(FmAlarmHandler.class);
    private static final ObjectMapper json = Jackson.om();
    private static final String FI_API_PATH = URI.create("/alarm-handler/v1/fault-indications").getPath();
    private static final int REQUEST_TIMEOUT_MILLIS = 5000;
    private WebClientProvider webClientProvider;
    private final String serverHost;
    private final Integer serverPort;
    private final Boolean tlsEnabled;

    public FmAlarmHandler(WebClientProvider webClientProvider, // the client provider to be used
                          String alarmHandlerHost, // alarm handler server hostname
                          int alarmHandlerPort, // alarm handler server port
                          boolean tlsEnabled) // indication if tls is enabled for both client/server
    {
        this.webClientProvider = webClientProvider;
        this.serverHost = alarmHandlerHost;
        this.serverPort = alarmHandlerPort;
        this.tlsEnabled = tlsEnabled;
    }

    public Completable indicatingFault(FaultIndication faultIndication) throws JsonProcessingException
    {
        // create new fault indication object and set current system time for the alarm
        // to be raised towards alarm handler service, transform faultindication to json
        // body
        var jsonBody = json.registerModule(new JodaModule()) //
                           .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                           .writeValueAsString(new FaultIndicationBuilder(faultIndication).withCreatedAt((new DateTime())) //
                                                                                          .build());
        log.debug("Indicate fault: {}", jsonBody);

        JsonObject jo = new JsonObject(jsonBody);
        jo.encodePrettily();

        log.debug("Pretty print FI: {}", jsonBody);

        // send fault indication to alarm handler service
        return this.webClientProvider.getWebClient()
                                     .flatMapCompletable(client -> client.post(this.serverPort, // alarm handler service port
                                                                               this.serverHost, // alarm handler service hostname
                                                                               FI_API_PATH) // alarm handler FI API path
                                                                         .ssl(this.tlsEnabled)
                                                                         .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                         .rxSendJson(jo)
                                                                         .doOnSubscribe(req -> faultIndication.getSeverity()//
                                                                                                              .ifPresent(fi ->
                                                                                                              {
                                                                                                                  if (fi.equals(Severity.CLEAR))
                                                                                                                      log.debug("Updating fault: {}", jo);
                                                                                                                  else
                                                                                                                      log.info("Updating fault: {}", jo);
                                                                                                              }))
                                                                         .doOnSuccess(resp -> log.debug("Fault updated with response code:{}, cause:{}",
                                                                                                        resp.statusCode(),
                                                                                                        resp.statusMessage()))
                                                                         .doOnError(e -> log.error("Error updating fault: {}", jo, e))
                                                                         .flatMapCompletable(resp -> resp.statusCode() == HttpResponseStatus.NO_CONTENT.code() ? Completable.complete()
                                                                                                                                                               : Completable.error(new FmAlarmServiceException("Failed to update fault "
                                                                                                                                                                                                               + jsonBody
                                                                                                                                                                                                               + " with response "
                                                                                                                                                                                                               + resp.bodyAsString()))));
    }
}
