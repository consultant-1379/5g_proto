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
 * Created on: Nov 17, 2020
 *     Author: echfari
 */
package com.ericsson.adpal.cm.actions;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Implements YANG actions for a specific CM schema
 */
public class ActionImplementer
{

    public static final Logger log = LoggerFactory.getLogger(ActionImplementer.class);
    static final String CMM_API_BASE = "/cm/api/v1.9/schemas";
    private final String schemaName;
    private final Vertx vertx;
    private final RouterHandler webServer;
    private final String actionsRelativeUri;
    private final List<ActionSpec> actions;
    private final List<Completable> completables;
    private final String cmHost;
    private final int cmPort;
    private final URI baseUri;
    private final WebClientProvider webClientSubject;

    public ActionImplementer(String schemaName,
                             RouterHandler webServer,
                             URI baseUri,
                             List<ActionSpec> actions,
                             String cmHost,
                             int cmPort,
                             WebClientProvider webClientSubject)
    {
        this.schemaName = schemaName;
        this.actionsRelativeUri = "/yang-actions/" + schemaName;
        this.webServer = webServer;
        this.baseUri = baseUri;
        this.vertx = webServer.getVertx();
        this.actions = actions;
        this.cmHost = cmHost;
        this.cmPort = cmPort;
        this.webClientSubject = webClientSubject;

        Objects.requireNonNull(baseUri);
        Objects.requireNonNull(schemaName);
        Objects.requireNonNull(webServer);
        Objects.requireNonNull(actions);
        Objects.requireNonNull(cmHost);
        Objects.requireNonNull(baseUri);
        if (cmPort <= 0)
        {
            throw new IllegalArgumentException("Invalid cmPort: " + cmPort);
        }
        this.completables = this.actions.stream().map(this::actionImplementer).collect(Collectors.toUnmodifiableList());
    }

    public Completable run()
    {
        return Completable.merge(completables);
    }

    JsonNode actionRegistrationBody()
    {
        final var implementerDefinedBaseUri = baseUri //
                                                     .resolve(actionsRelativeUri)
                                                     .toString();
        return Jackson.om().getNodeFactory().textNode(implementerDefinedBaseUri);
    }

    public Completable registerActionImplementer()
    {
        return Completable.defer(() ->
        {

            final var registerActionMessage = actionRegistrationBody();

            final var cmmApiUri = CMM_API_BASE + "/" + this.schemaName + "/actions";
            log.info("Registering YANG action implementer: {}", registerActionMessage);
            return this.webClientSubject.getWebClient()
                                        .flatMapCompletable(webClient -> webClient.put(this.cmPort, this.cmHost, cmmApiUri)
                                                                                  .putHeader("Content-Type", "application/json")
                                                                                  .rxSendBuffer(Buffer.buffer(registerActionMessage.toPrettyString()))
                                                                                  .flatMapCompletable(resp ->
                                                                                  {
                                                                                      if (resp.statusCode() < 200 || resp.statusCode() > 300)
                                                                                      {
                                                                                          return Completable.error(new IllegalArgumentException("statusCode: "
                                                                                                                                                + resp.statusCode()
                                                                                                                                                + " body: "
                                                                                                                                                + resp.bodyAsString()));
                                                                                      }
                                                                                      else
                                                                                      {
                                                                                          return Completable.complete();
                                                                                      }
                                                                                  })
                                                                                  .doOnError(e -> log.error("Failed to register action implementers. Request: {}",
                                                                                                            registerActionMessage,
                                                                                                            e)))
                                        .retryWhen(new RetryFunction().withDelay(1 * 1000L).withRetries(5).create());
        });
    }

    private Completable actionImplementer(ActionSpec action)
    {
        return Flowable.<RoutingContext>create(emitter ->
        {
            final var actionUri = Pattern.quote(this.actionsRelativeUri + "/" + action.getActionId());
            log.info("Registering HTTP PUT {} for YANG action {}", actionUri, action.getActionId());
            // XXX: need to use putWithRegex, put(actionUri) not working when uri contains
            // colons
            log.info("Registered HTTP PUT {} for YANG action {}", actionUri, action.getActionId());
            this.webServer.configureRouter(cons -> cons.putWithRegex(actionUri).handler(emitter::onNext));
            emitter.setCancellable(() ->
            // FIXME remove the route from the router somehow
            log.info("Deregistered HTTP PUT {}", actionUri));
        }, BackpressureStrategy.ERROR)
                       // TODO limit number of pending requesting
                       .concatMapCompletable(ctx ->
                       {
                           final var actionCtxSingle = this.toActionContext(ctx);
                           return action.getActionHandler() //
                                        .executeAction(actionCtxSingle)
                                        .flatMapCompletable(result ->
                                        {
                                            ctx.response().setStatusCode(result.resultCode());
                                            if (result.resultCode() != 204)
                                            {
                                                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                                                return ctx.response().rxEnd(Buffer.buffer(result.toJson().toString()));
                                            }
                                            else
                                            {
                                                return ctx.response().rxEnd(Buffer.buffer());
                                            }
                                        });
                       });
    }

    private Single<ActionInput> toActionContext(RoutingContext ctx)
    {
        return Single.<Buffer>create(emitter -> ctx.request().bodyHandler(emitter::onSuccess))
                     .map(buff -> Jackson.om().readTree(buff.getBytes()))
                     .map(ActionInput::fromJson);
    }
}
