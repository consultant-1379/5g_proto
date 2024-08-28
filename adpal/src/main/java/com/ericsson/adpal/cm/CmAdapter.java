/**
 * COPYRIGHT ERICSSON GMBH 2018
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Oct 5, 2018
 * Author: eedstl
 */

package com.ericsson.adpal.cm;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.model.Data;
import com.ericsson.adpal.cm.model.Event;
import com.ericsson.adpal.cm.model.JsonSchema;
import com.ericsson.adpal.cm.model.Notification;
import com.ericsson.adpal.cm.model.SchemaUpdate;
import com.ericsson.adpal.cm.model.Subscription;
import com.ericsson.adpal.cm.model.SubscriptionUpdate;
import com.ericsson.adpal.cm.validator.Validator;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServerRouter;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.multipart.MultipartForm;

public class CmAdapter<T> implements CmmApi<T>
{
    public final class Configuration
    {
        private static final String ROUTE_CONFIGURATIONS = "/configurations";
        private final String configPath = CmAdapter.this.cmPath + ROUTE_CONFIGURATIONS + '/' + CmAdapter.this.schema.getName();

        public Single<Integer> delete()
        {
            return CmAdapter.this.delete(ROUTE_CONFIGURATIONS, CmAdapter.this.schema.getName());
        }

        /**
         * Get configuration from CM Mediator
         *
         * @return The configuration, or an empty Optional if configurations does not
         *         exist
         */
        public Single<Optional<T>> get()
        {
            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.get(CmAdapter.this.cmPort,
                                                                                   CmAdapter.this.cmHost,
                                                                                   CmAdapter.this.cmPath + ROUTE_CONFIGURATIONS + "/"
                                                                                                          + CmAdapter.this.schema.getName())
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSend()
                                                                              .doOnSubscribe(d -> log.debug("GET request: {}:{}{}{}/{}",
                                                                                                            CmAdapter.this.cmHost,
                                                                                                            CmAdapter.this.cmPort,
                                                                                                            CmAdapter.this.cmPath,
                                                                                                            ROUTE_CONFIGURATIONS,
                                                                                                            CmAdapter.this.schema.getName()))
                                                                              .doOnSuccess(resp ->
                                                                              {
                                                                                  if (CmAdapter.this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                                      && (log.isDebugEnabled() || resp.statusCode() < 200
                                                                                          || resp.statusCode() >= 300))
                                                                                  {
                                                                                      log.info("GET configuration response: {} {}",
                                                                                               resp.statusCode(),
                                                                                               resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .map(resp ->
                                                                              {
                                                                                  switch (resp.statusCode())
                                                                                  {
                                                                                      case 200:
                                                                                          // Configuration exists
                                                                                          final var jsonBody = resp.bodyAsJsonObject(); // non null,
                                                                                                                                        // expected JSON
                                                                                                                                        // response
                                                                                          final var jsonData = jsonBody.getJsonObject("data");// null data is
                                                                                                                                              // not expected,
                                                                                                                                              // will be
                                                                                                                                              // interpreted as
                                                                                          // non-existent configuration
                                                                                          return Optional.<T>ofNullable(jsonData == null ? null
                                                                                                                                         : CmAdapter.json.convertValue(jsonData.getMap(),
                                                                                                                                                                       CmAdapter.this.clazz));
                                                                                      case 404:
                                                                                          // Configuration does not exist, mapped to empty Optional
                                                                                          return Optional.<T>empty();
                                                                                      default:
                                                                                          throw new CmmApiException("GET configuration",
                                                                                                                    resp.statusCode(),
                                                                                                                    resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .doOnError(e -> log.error("Failed to get configuration from CM", e)))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                               .doOnError(e -> log.error("Error on get", e));
        }

        /**
         * Get configuration from CMM
         *
         * @return The configuration and relevant eTag, or an empty Optional if
         *         configuration does not exist
         */
        public Single<Optional<CmConfig<T>>> getCmConfig()
        {
            return this.getCmJsonConfig().map(optionalJsonConfig -> optionalJsonConfig.map(jsonConfig ->
            {
                final var configDataObject = CmAdapter.json.convertValue(jsonConfig.get().getMap(), CmAdapter.this.clazz);
                return new CmConfig<>(configDataObject, jsonConfig.getETag());
            }));
        }

        /**
         * Get configuration from CMM
         *
         * @return The configuration as {@link JsonObject} and relevant eTag, or an
         *         empty Optional if configuration does not exist
         */
        public Single<Optional<CmConfig<JsonObject>>> getCmJsonConfig()
        {
            final var port = CmAdapter.this.cmPort;
            final var host = CmAdapter.this.cmHost;

            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.get(port, host, this.configPath)
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSend()
                                                                              .doOnSubscribe(disp -> log.debug("GET {}:{} {}", host, port, this.configPath))
                                                                              .doOnSuccess(resp ->
                                                                              {
                                                                                  if (CmAdapter.this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                                      && (log.isDebugEnabled() || resp.statusCode() < 200
                                                                                          || resp.statusCode() >= 300))
                                                                                  {
                                                                                      log.info("GET response: {} {}", resp.statusCode(), resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .map(resp ->
                                                                              {
                                                                                  log.debug("RESPONSE CODE: {}, RESPONSE MSG: {}, RESPONSE BODY: {}",
                                                                                            resp.statusCode(),
                                                                                            resp.statusMessage(),
                                                                                            resp.bodyAsString());
                                                                                  switch (resp.statusCode())
                                                                                  {
                                                                                      case 200: // Configuration exists
                                                                                          final String eTag = resp.getHeader("ETag");
                                                                                          final var configDataJson = resp.bodyAsJsonObject()
                                                                                                                         .getJsonObject("data");
                                                                                          return Optional.of(new CmConfig<>(configDataJson, eTag));
                                                                                      case 404: // Configuration does not exist
                                                                                          return Optional.<CmConfig<JsonObject>>empty();
                                                                                      default: // Error
                                                                                          throw new CmmApiException("GET config ",
                                                                                                                    resp.statusCode(),
                                                                                                                    resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .doOnError(e -> log.error("Failed to get configuration from CM", e)))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                               .doOnError(e -> log.error("Error on getCmJsonConfig", e));
        }

        public Single<Integer> update(T config)
        {
            try
            {
                return this.updateRaw(CmAdapter.json.readValue(CmAdapter.json.writeValueAsString(config), Data.class));
            }
            catch (Exception e)
            {
                // Handle Jackson checked exceptions
                return Single.error(e);
            }
        }

        /**
         * Update CM configuration
         *
         * @param config
         * @return True if successful, false if there is an eTag mismatch or
         *         configuration does not exist
         */
        public Completable updateCmConfig(CmConfig<T> config)
        {
            try
            {
                // TODO find a more performant way to convert to Data
                final var data = CmAdapter.json.readValue(CmAdapter.json.writeValueAsString(config.get()), Data.class);
                final var newCfg = new com.ericsson.adpal.cm.model.Configuration();
                newCfg.setTitle(CmAdapter.this.schema.getName());
                newCfg.setData(data);
                newCfg.setBaseETag(config.getETag());

                return rxSendJson(CmAdapter.this.clientSubject.getWebClient()//
                                                              .map(webClient -> webClient.put(CmAdapter.this.cmPort, CmAdapter.this.cmHost, this.configPath)
                                                                                         .ssl(CmAdapter.this.sslEnabled)
                                                                                         .timeout(REQUEST_TIMEOUT_MILLIS)),
                                  newCfg).retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                         .doOnError(e -> log.error("Error on uodateCmConfig", e))//
                                         .flatMapCompletable(resp ->
                                         {
                                             log.debug("RESPONSE CODE: {}, RESPONSE MSG: {}, RESPONSE BODY: {}",
                                                       resp.statusCode(),
                                                       resp.statusMessage(),
                                                       resp.bodyAsString());
                                             var errorMsg = "";
                                             switch (resp.statusCode())
                                             {
                                                 case 409: // Conflict: ETag value not current
                                                     errorMsg = "eTag mismatch";
                                                     break;
                                                 case 404: // Configuration no longer exists
                                                     errorMsg = "configuration no longer exists";
                                                     break;
                                                 case 200: // OK
                                                     return Completable.complete();
                                                 default:
                                                     return Completable.error(new CmmApiException("PUT config", resp.statusCode(), resp.bodyAsString()));
                                             }
                                             return Completable.error(new CmmTransactionException(errorMsg, resp.statusCode(), resp.bodyAsString()));
                                         });
            }
            catch (Exception e)
            {
                return Completable.error(e);
            }
        }

        public Single<Integer> updateRaw(Data config)
        {
            return this.put(config).filter(result -> result != HttpResponseStatus.NOT_FOUND.code()).switchIfEmpty(this.post(config));
        }

        private Single<Integer> post(Data config)
        {
            com.ericsson.adpal.cm.model.Configuration c = new com.ericsson.adpal.cm.model.Configuration();
            c.setName(CmAdapter.this.schema.getName());
            c.setTitle(CmAdapter.this.schema.getName());
            c.setData(config);

            return CmAdapter.this.post(ROUTE_CONFIGURATIONS, c);
        }

        private Single<Integer> put(Data config)
        {
            com.ericsson.adpal.cm.model.Configuration c = new com.ericsson.adpal.cm.model.Configuration();
            c.setTitle(CmAdapter.this.schema.getName());
            c.setData(config);

            return CmAdapter.this.put(ROUTE_CONFIGURATIONS, CmAdapter.this.schema.getName(), c);
        }
    }

    public class NotificationHandler
    {
        private static final String ROUTE_NOTIFICATIONS = "/notifications";
        private static final String ROUTE_SUBSCRIPTIONS = "/subscriptions";
        private final String id = CmAdapter.this.schema.getName() + "-" + UUID.randomUUID().toString();

        private Disposable updater = null;
        private Notification notification = null;
        private final BehaviorSubject<Optional<T>> subject = BehaviorSubject.create();
        private final BehaviorSubject<Optional<CmConfig<T>>> notifications = BehaviorSubject.create();
        private URI cmBaseUri = null;

        public BehaviorSubject<Optional<T>> getConfiguration()
        {
            return this.subject;
        }

        public BehaviorSubject<Optional<CmConfig<T>>> getNotification()
        {
            return this.notifications;
        }

        private final void determineAndRegisterNotificationCallbackUri(final WebServerRouter routerHandler) throws UnknownHostException, URISyntaxException
        {
            final String notificationRelativeUrl = CM_API + ROUTE_NOTIFICATIONS + "/"
                                                   + URLEncoder.encode(CmAdapter.this.schema.getName(), StandardCharsets.UTF_8);

            final String host = routerHandler.baseUri().getHost();

            if (host.equals("[::]") || host.equals("::") || host.equals("0.0.0.0"))
            {
                // Wildcarded host address, needs to be replaced by a real address. Make a copy
                // of the base URI and replace the host part by the local host address. This
                // is possible as the web server is listening on all interfaces.

                this.cmBaseUri = new URI(routerHandler.baseUri().getScheme(),
                                         null,
                                         InetAddress.getLocalHost().getHostAddress(),
                                         routerHandler.baseUri().getPort(),
                                         notificationRelativeUrl,
                                         routerHandler.baseUri().getQuery(),
                                         routerHandler.baseUri().getFragment());
            }
            else
            {
                this.cmBaseUri = routerHandler.baseUri().resolve(notificationRelativeUrl);
            }

            log.info("Registering notification URL for receiving CMM notifications: {}", this.cmBaseUri);
            routerHandler.configureRouter(router -> router.route(notificationRelativeUrl).handler(this::handler));
        }

        public Completable start(final WebServerRouter routerHandler)
        {
            return Completable.fromAction(() ->
            {
                if (this.updater == null)
                {
                    determineAndRegisterNotificationCallbackUri(routerHandler);

                    this.updater = //
                            CmAdapter.this.config.get() //
                                                 .doOnSuccess(cfg ->
                                                 {
                                                     log.info("Configuration fetched for the first time: {}", cfg);
                                                     this.subject.onNext(cfg);
                                                 })
                                                 .retryWhen(errors -> errors.flatMap(e ->
                                                 {
                                                     log.warn("Failed to fetch configuration for the first time, retrying.", e);
                                                     return Flowable.timer(10, TimeUnit.SECONDS);
                                                 }))
                                                 .ignoreElement()
                                                 .andThen(
                                                          // Re-subscribe after the given time interval, ignoring any errors
                                                          this.update().doOnSuccess(response ->
                                                          {
                                                              if (response == HttpResponseStatus.CREATED.code() || response == HttpResponseStatus.OK.code())
                                                              {
                                                                  // Either renewed subscription or initialize subscription succeeded
                                                                  log.debug("Configuration changes subscription renewed successfully.");
                                                              }
                                                              else
                                                              {
                                                                  // POST success is 201 & PUT success is 200
                                                                  // https://adp-api-repo.sero.wh.rnd.internal.ericsson.com/home/interfaces/CM/latest/API/documentation/cm-mediator-service.html#
                                                                  // Request reached to CMM API but the status code is not successful
                                                                  // and the subscription cannot either be renewed or initialized
                                                                  log.error("Failed to create/renew subscription for configuration changes, update response code: {}",
                                                                            response);
                                                              }
                                                          })
                                                              .doOnError(e -> log.error("Renewing/Initializing CM subscription failed due to unexpected error",
                                                                                        e))
                                                              .toFlowable()
                                                              .concatMap(event ->
                                                              {

                                                                  log.debug("Starting heartbeat.....");
                                                                  return this.heartbeat(ROUTE_SUBSCRIPTIONS, this.id)
                                                                             .repeatWhen(handler -> handler.delay(CmAdapter.this.getSubscribeHeartbeat(),
                                                                                                                  TimeUnit.SECONDS))
                                                                             .take(Math.round(CmAdapter.this.getSubscribeRenewal()
                                                                                              / CmAdapter.this.getSubscribeHeartbeat()));
                                                              })
                                                              .retryWhen(new RetryFunction().withDelay(5000)// time in ms
                                                                                            .withRetries(-1) // infinite retries to subscribe to CMM API
                                                                                            .create())

                                                              .repeat()
                                                              .ignoreElements())
                                                 .doOnSubscribe(d -> log.info("Started updating CM subscriptions"))
                                                 .doOnError(e -> log.error("Stopped updating CM subscriptions due to unexpected error", e))
                                                 .subscribe();

                }
            });
        }

        public Completable stop()
        {
            return CmAdapter.this.delete(ROUTE_SUBSCRIPTIONS, CmAdapter.this.schema.getName())//
                                 .ignoreElement()
                                 .onErrorComplete(exception ->
                                 {
                                     log.warn("Failed to delete subscription", exception);
                                     return true;
                                 })
                                 .andThen(Completable.fromAction(() ->
                                 {
                                     if (this.updater != null)
                                     {
                                         this.updater.dispose();
                                         this.updater = null;
                                     }
                                 }));
        }

        private void handleConfigCreated(Notification n)
        {
            this.notification = n;

            CmAdapter.this.config.get()//
                                 .subscribe(cfg ->
                                 {
                                     log.info("Configuration created");

                                     this.subject.onNext(cfg);
                                 }, t -> log.error("Failed to fetch new configuration", t));
        }

        private void handleConfigDeleted()
        {
            log.info("Configuration deleted");
            this.notification = null;
            this.subject.onNext(Optional.<T>empty());
        }

        private void handleConfigUpdated(JsonObject msg,
                                         Notification n) throws IOException
        {
            if (this.notification == null || n.getBaseETag().equals(this.notification.getConfigETag()))
            {
                this.notification = n;
                T newCfg = CmAdapter.json.readValue(msg.getJsonObject("data").toString(), CmAdapter.this.clazz);

                log.debug("Configuration updated: {}", newCfg);

                this.subject.onNext(Optional.of(newCfg));
            }
            else
            {
                log.warn("Invalid configuration change: baseEtag={} does not match configEtag={}. Fetching complete configuration.",
                         n.getBaseETag(),
                         this.notification.getConfigETag());

                this.notification = n;
                CmAdapter.this.config.get()//
                                     .subscribe(cfg ->
                                     {
                                         log.info("Configuration updated");

                                         this.subject.onNext(cfg);
                                     }, t -> log.error("Failed to fetch configuration", t));
            }
        }

        private synchronized void handler(RoutingContext routingContext)
        {
            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();

            routingContext.request().bodyHandler(buffer ->
            {
                log.debug("buffer={}", buffer);

                try
                {
                    JsonObject msg = buffer.toJsonObject();

                    Notification n = CmAdapter.json.readValue(msg.toString(), Notification.class);
                    log.info("CM notification received: configuration={}, event={}", n.getConfigName(), n.getEvent());

                    switch (n.getEvent())
                    {
                        case CONFIG_CREATED:
                            this.handleConfigCreated(n);
                            break;

                        case CONFIG_DELETED:
                            this.handleConfigDeleted();
                            break;

                        case CONFIG_UPDATED:
                            this.handleConfigUpdated(msg, n);
                            break;

                        default:
                            break;
                    }
                }
                catch (Exception e)
                {
                    log.error("Exception while processing CM notification", e);
                    routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                }
            });
        }

        private Single<Integer> post()
        {
            Subscription sub = new Subscription();
            sub.setId(this.id);
            sub.setConfigName(CmAdapter.this.schema.getName());
            sub.setEvent(new HashSet<>(Arrays.asList(Event.CONFIG_CREATED, Event.CONFIG_DELETED, Event.CONFIG_UPDATED)));
            sub.setUpdateNotificationFormat(Subscription.UpdateNotificationFormat.FULL);
            sub.setLeaseSeconds(CmAdapter.this.getSubscribeValidity());
            sub.setCallback(this.cmBaseUri);
            sub.setServerName(clientSubject.getHostName());
            return CmAdapter.this.post(ROUTE_SUBSCRIPTIONS, sub);
        }

        private Single<Integer> put()
        {
            SubscriptionUpdate sub = new SubscriptionUpdate();

            sub.setConfigName(CmAdapter.this.schema.getName());
            sub.setEvent(new HashSet<>(Arrays.asList(Event.CONFIG_CREATED, Event.CONFIG_DELETED, Event.CONFIG_UPDATED)));
            sub.setUpdateNotificationFormat(SubscriptionUpdate.UpdateNotificationFormat.FULL);
            sub.setLeaseSeconds(CmAdapter.this.getSubscribeValidity());
            sub.setCallback(this.cmBaseUri);
            sub.setServerName(clientSubject.getHostName());

            return CmAdapter.this.put(ROUTE_SUBSCRIPTIONS, this.id, sub);
        }

        private Single<Integer> update()
        {
            return this.put().filter(result -> result >= 200 && result < 300).switchIfEmpty(this.post());
        }

        private Single<Integer> heartbeat(String route,
                                          String item)
        {

            final String path = CmAdapter.this.cmPath + route + "/" + item;
            final int port = CmAdapter.this.cmPort;
            final String host = CmAdapter.this.cmHost;
            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.get(port, host, path)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSend()
                                                                              .doOnSubscribe(disp -> log.debug("GET {}:{} {}", host, port, path))
                                                                              .map(resp ->
                                                                              {
                                                                                  log.debug("RESPONSE CODE: {}, RESPONSE MSG: {}, RESPONSE BODY: {}",
                                                                                            resp.statusCode(),
                                                                                            resp.statusMessage(),
                                                                                            resp.bodyAsString());

                                                                                  if (resp.statusCode() != HttpResponseStatus.OK.code())
                                                                                  {

                                                                                      throw new CmmApiException("Heartbeat failed!Subscription is not healthy!",
                                                                                                                resp.statusCode(),
                                                                                                                resp.bodyAsString());

                                                                                  }
                                                                                  return resp.statusCode();
                                                                              }));

        }

    }

    public class Schema
    {
        private static final String ROUTE_SCHEMAS = "/schemas";
        private static final String APPLICATION_JSON = "application/json";

        private final String name;

        public Schema(String name)
        {
            this.name = name;
        }

        public Single<Integer> create(InputStream schema)
        {
            return this.post(schema);
        }

        public Single<Integer> create(String schemaPath,
                                      String schemaName,
                                      String yangPath,
                                      String yangName)
        {
            MultipartForm form = MultipartForm.create()
                                              .attribute("name", this.name)
                                              .attribute("title", this.name)
                                              .textFileUpload("file", this.name + ".json", schemaPath + "/" + schemaName, APPLICATION_JSON)
                                              .binaryFileUpload("yangArchive", this.name + ".tar.gz", yangPath + "/" + yangName, "application/gzip");

            final String requestUri = CmAdapter.this.cmPath + ROUTE_SCHEMAS;

            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.post(CmAdapter.this.cmPort, CmAdapter.this.cmHost, requestUri)
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .rxSendMultipartForm(form)
                                                                              .doOnSubscribe(d -> log.debug("POST request: {}:{}{}\n{}",
                                                                                                            CmAdapter.this.cmHost,
                                                                                                            CmAdapter.this.cmPort,
                                                                                                            requestUri,
                                                                                                            form))
                                                                              .doOnSuccess(resp ->
                                                                              {
                                                                                  if (CmAdapter.this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                                      && (log.isDebugEnabled() || resp.statusCode() < 200
                                                                                          || resp.statusCode() >= 300))
                                                                                  {
                                                                                      log.info("POST response: {} {}", resp.statusCode(), resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .map(HttpResponse::statusCode)
                                                                              .onErrorReturnItem(HttpResponseStatus.BAD_REQUEST.code()))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create());
        }

        public Single<Integer> delete()
        {
            return CmAdapter.this.delete(ROUTE_SCHEMAS, this.name);
        }

        /**
         * Get schema from CM Mediator
         *
         * @return The schema, or an empty Optional if schema does not exist
         */
        public Single<Optional<com.ericsson.adpal.cm.model.JsonSchema>> get()
        {
            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.get(CmAdapter.this.cmPort,
                                                                                   CmAdapter.this.cmHost,
                                                                                   CmAdapter.this.cmPath + ROUTE_SCHEMAS + "/"
                                                                                                          + CmAdapter.this.schema.getName())
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSend()

                                                                              .doOnSubscribe(d -> log.debug("GET request: {}:{}{}{}/{}",
                                                                                                            CmAdapter.this.cmHost,
                                                                                                            CmAdapter.this.cmPort,
                                                                                                            CmAdapter.this.cmPath,
                                                                                                            ROUTE_SCHEMAS,
                                                                                                            CmAdapter.this.schema.getName()))
                                                                              .doOnSuccess(resp ->
                                                                              {
                                                                                  if (CmAdapter.this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                                      && (log.isDebugEnabled() || resp.statusCode() < 200
                                                                                          || resp.statusCode() >= 300))
                                                                                  {
                                                                                      log.info("GET response: {} {}", resp.statusCode(), resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .map(resp ->
                                                                              {
                                                                                  switch (resp.statusCode())
                                                                                  {
                                                                                      case 200:
                                                                                          // Schema exists
                                                                                          final var jsonBody = resp.bodyAsJsonObject(); // non null,
                                                                                                                                        // expected JSON
                                                                                                                                        // response
                                                                                          final var jsonData = jsonBody.getJsonObject("jsonSchema"); // null
                                                                                                                                                     // data
                                                                                                                                                     // is
                                                                                                                                                     // not
                                                                                                                                                     // expected,
                                                                                                                                                     // will
                                                                                                                                                     // be
                                                                                          // interpreted as non-existent schema

                                                                                          return Optional.<com.ericsson.adpal.cm.model.JsonSchema>ofNullable(jsonData == null ? null
                                                                                                                                                                              : CmAdapter.json.convertValue(jsonData.getMap(),
                                                                                                                                                                                                            com.ericsson.adpal.cm.model.JsonSchema.class));

                                                                                      case 404:
                                                                                          // Schema does not exist, mapped to empty Optional
                                                                                          return Optional.<com.ericsson.adpal.cm.model.JsonSchema>empty();

                                                                                      default:
                                                                                          throw new CmmApiException("GET schema",
                                                                                                                    resp.statusCode(),
                                                                                                                    resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .doOnError(e -> log.error("Failed to get schema from CM", e)))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                               .doOnError(e -> log.error("Error on jsonSchema get", e));
        }

        public String getName()
        {
            return this.name;
        }

        public Single<Integer> update(InputStream schema)
        {
            return this.put(schema);
        }

        public Single<Integer> update(final JsonSchema jsonSchema)
        {
            return this.put(jsonSchema);
        }

        /**
         * Warning: Blocking operation
         *
         * @param schemaPath
         * @param schemaName
         * @param yangPath
         * @param yangName
         * @return
         */
        public Single<Integer> update(String schemaPath,
                                      String schemaName,
                                      String yangPath,
                                      String yangName)
        {
            MultipartForm form = MultipartForm.create()
                                              .attribute("title", this.name)
                                              .textFileUpload("file", this.name + ".json", schemaPath + "/" + schemaName, APPLICATION_JSON)
                                              .binaryFileUpload("yangArchive", this.name + ".tar.gz", yangPath + "/" + yangName, "application/gzip");

            final String requestUri = CmAdapter.this.cmPath + ROUTE_SCHEMAS + "/" + this.name;

            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.put(CmAdapter.this.cmPort, CmAdapter.this.cmHost, requestUri)
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .rxSendMultipartForm(form)
                                                                              .doOnSubscribe(d -> log.debug("PUT request: {}:{}{}\n{}",
                                                                                                            CmAdapter.this.cmHost,
                                                                                                            CmAdapter.this.cmPort,
                                                                                                            requestUri,
                                                                                                            form))
                                                                              .doOnSuccess(resp ->
                                                                              {
                                                                                  if (CmAdapter.this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                                      && (log.isDebugEnabled() || resp.statusCode() < 200
                                                                                          || resp.statusCode() >= 300))
                                                                                  {
                                                                                      log.info("PUT response: {} {}", resp.statusCode(), resp.bodyAsString());
                                                                                  }
                                                                              })
                                                                              .map(HttpResponse::statusCode)
                                                                              .onErrorReturnItem(HttpResponseStatus.BAD_REQUEST.code()))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create());
        }

        public Single<Integer> updateIfNeeded(JsonSchema jsonSchema)
        {
            return this.get()
                       .filter(o -> !o.isEmpty() && Json.isEqual(o.get(), jsonSchema))
                       .map(res -> HttpResponseStatus.OK.code())
                       .switchIfEmpty(this.update(jsonSchema));
        }

        public Single<Integer> updateIfNeeded(JsonSchema jsonSchema,
                                              String schemaPath,
                                              String schemaName,
                                              String yangPath,
                                              String yangName)
        {
            return this.get()
                       .filter(o -> !o.isEmpty() && Json.isEqual(o.get(), jsonSchema))
                       .map(res -> HttpResponseStatus.OK.code())
                       .switchIfEmpty(Single.defer(() -> this.update(schemaPath, schemaName, yangPath, yangName)));
        }

        private Single<Integer> post(InputStream schema)
        {
            try
            {
                JsonSchema jsonSchema = CmAdapter.json.readValue(schema, JsonSchema.class);
                com.ericsson.adpal.cm.model.Schema body = new com.ericsson.adpal.cm.model.Schema();
                body.setJsonSchema(jsonSchema);
                body.setName(this.name);
                body.setTitle(this.name);
                String bodyAsString = CmAdapter.json.writeValueAsString(body);

                return CmAdapter.this.post(ROUTE_SCHEMAS, bodyAsString);
            }
            catch (Exception e)
            {
                // Handle Jackson checked exceptions
                return Single.error(e);
            }
        }

        private Single<Integer> put(InputStream schema)
        {
            try
            {
                return this.put(CmAdapter.json.readValue(schema, JsonSchema.class));
            }
            catch (Exception e)
            {
                // Handle Jackson checked exception
                return Single.error(e);
            }
        }

        private Single<Integer> put(final JsonSchema jsonSchema)
        {
            try
            {
                SchemaUpdate body = new SchemaUpdate();
                body.setJsonSchema(jsonSchema);
                body.setTitle(this.name);

                return CmAdapter.this.put(ROUTE_SCHEMAS, this.name, body);
            }
            catch (Exception e)
            {
                // Handle Jackson checked exception
                return Single.error(e);
            }
        }
    }

    /**
     * {@link ValidationHandler} is a partial implementation of the CM Dynamic
     * Validation Interface. Offer the method to
     * {@link #registerValidator(String, String, String, int)}, to
     * {@link #applyPatch(String)} to the currently committed configuration and a
     * {@link #handler(RoutingContext, Validator)} for the incoming Validation
     * Requests
     */
    public class ValidationHandler
    {
        private static final String ROUTE_SCHEMAS = "/schemas";
        private static final String ROUTE_VALIDATOR = "/validator";
        private static final String ROUTE_VALIDATORS = "/validators";
        private static final String APPLICATION_JSON = "application/json";

        /**
         * Creates the validator path and binds it with a handler and also registers the
         * validator for the caller Network Function with its schema name and a
         * validator name
         *
         * @param routerHandler the web server
         * @param validator     interface for the validation
         * @param schemaName    the name of the schema to bind the validator with
         * @param validatorName the name of the validator to register it in the mediator
         * @param hostIp        the IP of the host container
         * @param hostPort      the port of web server
         */
        public Completable start(final RouterHandler routerHandler,
                                 Validator<T> validator,
                                 String schemaName,
                                 String validatorName,
                                 URI uri)
        {
            return Completable.fromAction(() ->
            {
                final String validatorRelativeUrl = CM_API + ROUTE_VALIDATOR;
                log.info("Registering validator URL for receiving CMM validation requests: {}", uri);
                routerHandler.configureRouter(router -> router.route(validatorRelativeUrl).handler(r -> this.handler(r, validator)));
            }).doOnError(e -> log.error("Error registering validator CmAdapter", e)).andThen(registerValidator(schemaName, validatorName, uri).ignoreElement());
        }

        /**
         * Gets the current configuration committed in CM Mediator for the caller
         * Network Function and applies to it the given Json Patch coming from the
         * validation request
         *
         * @param jsonPatch string coming from the validation request
         * @return the {@link T} Network Function POJO Schema result of the json patch
         *         apply function wrapped in as {@link Optional}
         */
        private Single<T> applyPatch(String jsonPatch)
        {
            return CmAdapter.this.config.getCmJsonConfig().map(optionalCmJsonConfig ->
            {
                log.debug("CmJsonConfig is present {}", optionalCmJsonConfig.isPresent());
                JsonNode existingConfig;
                if (optionalCmJsonConfig.isPresent())
                {
                    if (!optionalCmJsonConfig.get().getETag().equals(Jackson.om().readTree(jsonPatch).get("configurations").get(0).get("configETag").asText()))
                    {
                        throw new IllegalArgumentException("ETag mismatch. Validation request is not valid.");
                    }
                    log.debug("CmJsonConfig value {}", optionalCmJsonConfig.get().get().encode());
                    existingConfig = Jackson.om().readTree(optionalCmJsonConfig.get().get().encode());
                }
                else
                {
                    existingConfig = Jackson.om().readTree(CmAdapter.this.clazz.getDeclaredConstructor().newInstance().toString());
                }

                JsonNode patch = Jackson.om().readTree(jsonPatch).get("configurations").get(0).get("patch");
                log.debug("Existing config is: {}", existingConfig);
                if (patch == null)
                {
                    throw new IllegalArgumentException("The Json Patch is malformed.");
                }
                else
                {
                    log.debug("Patch came is: {}", patch);
                    return Jackson.om().convertValue(JsonPatch.apply(patch, existingConfig), CmAdapter.this.clazz);
                }
            });
        }

        private JsonObject formRegistrationBody(URI uri)
        {
            JsonObject registrationBody = new JsonObject();
            registrationBody.put("uri", uri.resolve(CM_API + ROUTE_VALIDATOR).toString());
            return registrationBody;
        }

        /**
         * Handler of the incoming validation requests
         *
         * @param routingContext the web server
         * @param validator      the validator to serve the request
         */
        private void handler(RoutingContext routingContext,
                             Validator<T> validator)
        {
            routingContext.request().bodyHandler(buffer ->
            {
                log.debug("The configuration received is: {}", buffer);

                applyPatch(buffer.toString()).flatMap(validator::validate).doOnSuccess(validationResult ->
                {
                    if (!validationResult.getResult())
                    {
                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(validationResult.getBody());
                    }
                    else
                    {
                        routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                    }
                }).subscribe(result -> log.info("Successful validation request."), e ->
                {
                    if (e instanceof JsonProcessingException || e instanceof IllegalArgumentException)
                    {
                        JsonObject errorMsgBody = new JsonObject();
                        errorMsgBody.put("message", "Syntax Error in the submitted request for validation.");

                        log.error("Syntax Error. Answering with 400 Bad Request Error", e);
                        routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(errorMsgBody.toString());
                    }
                    else
                    {
                        log.error("Error on validation. Answering with 500 Internal Server Error", e);
                        JsonObject errorMsgBody = new JsonObject();
                        errorMsgBody.put("message", "Internal Server Error.");
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(errorMsgBody.toString());
                    }
                });
            });
        }

        /**
         * Registers the validator for the caller Network Function
         *
         * @param schemaName    the schema to bind the validator with
         * @param validatorName the name of the validator to register it in the mediator
         * @param hostIp        the IP of the host container
         */
        private Single<HttpResponse<Buffer>> registerValidator(String schemaName,
                                                               String validatorName,
                                                               URI uri)
        {

            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.put(CmAdapter.this.cmPort,
                                                                                   CmAdapter.this.cmHost,
                                                                                   CM_API + ROUTE_SCHEMAS + "/" + schemaName + ROUTE_VALIDATORS + "/"
                                                                                                          + validatorName)
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .putHeader("Content-Type", APPLICATION_JSON)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSendJson(formRegistrationBody(uri))

                                                                              .retry(1)
                                                                              .doOnError(e -> log.error("An error has occurred while registering the validator: ",
                                                                                                        e))
                                                                              .doOnSuccess(response -> log.info("The Validation response is: {}, with code: {}",
                                                                                                                response.statusMessage(),
                                                                                                                response.statusCode())))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                               .doOnError(e -> log.error("Error on registerValidator", e));
        }

        public boolean checkValidator(String schemaName,
                                      String validatorName,
                                      URI uri)
        {

            var validatorResponse = CmAdapter.this.clientSubject.getWebClient()
                                                                .flatMap(webClient -> webClient.get(CmAdapter.this.cmPort,
                                                                                                    CmAdapter.this.cmHost,
                                                                                                    CM_API + ROUTE_SCHEMAS + "/" + schemaName
                                                                                                                           + ROUTE_VALIDATORS)
                                                                                               .ssl(CmAdapter.this.sslEnabled)
                                                                                               .putHeader("Content-Type", APPLICATION_JSON)
                                                                                               .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                                               .rxSendJson(formRegistrationBody(uri))

                                                                                               .retry(1)
                                                                                               .doOnError(e -> log.error("An error has occurred while fetching the validator: ",
                                                                                                                         e))
                                                                                               .doOnSuccess(response -> log.info("The Validator Request response is: {}, with code: {}",
                                                                                                                                 response.statusMessage(),
                                                                                                                                 response.statusCode())

                                                                                               ))
                                                                .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                                                .doOnError(e -> log.error("Error on fetching Validator", e))
                                                                .blockingGet();

            if (!validatorResponse.bodyAsJsonArray().isEmpty())
            {
                var valName = validatorResponse.bodyAsJsonArray().getJsonObject(0).getString("name");
                if (validatorResponse.statusCode() == HttpResponseStatus.OK.code() && valName.equals(validatorName))
                    return true;
            }
            return false;

        }

        public HttpResponse<Buffer> deleteValidator(String schemaName,
                                                    String validatorName,
                                                    URI uri)
        {

            return CmAdapter.this.clientSubject.getWebClient()
                                               .flatMap(webClient -> webClient.delete(CmAdapter.this.cmPort,
                                                                                      CmAdapter.this.cmHost,
                                                                                      CM_API + ROUTE_SCHEMAS + "/" + schemaName + ROUTE_VALIDATORS + "/"
                                                                                                             + validatorName)
                                                                              .ssl(CmAdapter.this.sslEnabled)
                                                                              .putHeader("Content-Type", APPLICATION_JSON)
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSendJson(formRegistrationBody(uri))

                                                                              .retry(1)
                                                                              .doOnError(e -> log.error("An error occurred while trying to delete the validator: ",
                                                                                                        e))
                                                                              .doOnSuccess(response ->
                                                                              {
                                                                                  if (response.statusCode() == HttpResponseStatus.OK.code())
                                                                                      log.info("{} has been successfully deleted!", validatorName);
                                                                              }))
                                               .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                               .doOnError(e -> log.error("Error on Deleting the Validator", e))
                                               .blockingGet();

        }

    }

    private static final int REQUEST_TIMEOUT_MILLIS = 5000;
    private static final String CM_API = "/cm/api/v1.2";
    private static final Logger log = LoggerFactory.getLogger(CmAdapter.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse
    private static final String APPLICATION_JSON = "application/json";

    //
    private static final RetryFunction DEFAULT_RETRY_FUNCTION = new RetryFunction().withDelay(2 * 1000L) // retry after 2
                                                                                                         // seconds
                                                                                   .withRetries(5); // give up after 10 secs

    /**
     * Copies CM configuration object using Jackson serialization/deserialization
     *
     * @param <T>    The CM Object Type
     * @param source The CM object
     * @param clazz  The CM object class
     * @return A copy of the source CM object or null if object cannot be copied.
     */
    public static <T> T copyConfig(T source,
                                   Class<T> clazz)
    {
        try
        {
            return json.treeToValue(json.valueToTree(source), clazz);
        }
        catch (Exception e)
        {
            log.error("Unexpected error while copying CM configuration object {}", source, e);
            return null;
        }
    }

    private final Class<T> clazz;
    private final String cmHost;
    private final int cmPort;
    private final String cmPath;
    private final Configuration config;
    private final Schema schema;
    private final NotificationHandler notificationHandler;
    private final ValidationHandler validationHandler;
    private final LogThrottler logThrottler;
    private final Boolean sslEnabled;

    private WebClientProvider clientSubject;
    private int subscribeValidity; // subscribeSeconds
    private float subscribeRenewal; // subRenewalSeconds
    private int subscribeHeartbeat; // heartbeatSeconds

    public CmAdapter(Class<T> clazz,
                     String schema,
                     Vertx vertx,
                     int cmPort,
                     String cmHost,
                     String cmUrlPrefix,
                     WebClientProvider subject,
                     boolean sslEnabled,
                     int subscribeValidity,
                     float subscribeRenewalRatio,
                     int subscribeHeartbeat)

    {
        this.cmPath = cmUrlPrefix + CM_API;
        this.clazz = clazz;
        this.schema = new Schema(schema);
        this.cmHost = cmHost;
        this.cmPort = cmPort;

        this.sslEnabled = sslEnabled;

        this.subscribeValidity = subscribeValidity;
        this.subscribeRenewal = subscribeRenewalRatio * subscribeValidity;
        this.subscribeHeartbeat = subscribeHeartbeat;

        // Initialize dependent inner classes after parent class initialization
        this.config = new Configuration();
        this.notificationHandler = new NotificationHandler();
        this.validationHandler = new ValidationHandler();
        this.clientSubject = subject;
        this.logThrottler = new LogThrottler();

    }

    public CmAdapter(Class<T> clazz,
                     String schema,
                     Vertx vertx,
                     int cmPort,
                     String cmHost,
                     WebClientProvider subject,

                     boolean sslEnabled,
                     int subscribeValidity,
                     float subscribeRenewal,
                     int subscribeHeartbeat)

    {
        this(clazz, schema, vertx, cmPort, cmHost, "", subject, sslEnabled, subscribeValidity, subscribeRenewal, subscribeHeartbeat);

    }

    @Override
    public Observable<Optional<T>> configUpdates()
    {
        return this.notificationHandler.getConfiguration().hide();
    }

    @Override
    public Single<Optional<CmConfig<T>>> getCmConfig()
    {
        return this.config.getCmConfig();
    }

    public final Configuration getConfiguration()
    {
        return this.config;
    }

    public final NotificationHandler getNotificationHandler()
    {
        return this.notificationHandler;
    }

    public final Schema getSchema()
    {
        return this.schema;
    }

    public final int getSubscribeHeartbeat()
    {
        return this.subscribeHeartbeat;
    }

    public final float getSubscribeRenewal()
    {
        return this.subscribeRenewal;
    }

    public final int getSubscribeValidity()
    {
        return this.subscribeValidity;
    }

    public final ValidationHandler getValidationHandler()
    {
        return this.validationHandler;
    }

    @Override
    public Completable updateCmConfig(CmConfig<T> config)
    {
        return this.config.updateCmConfig(config);
    }

    private Single<Integer> delete(String route,
                                   String item)
    {
        final String path = this.cmPath + route + "/" + item;

        return this.clientSubject.getWebClient()
                                 .flatMap(webClient -> webClient.delete(this.cmPort, this.cmHost, path)
                                                                .ssl(this.sslEnabled)
                                                                .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                .rxSend()
                                                                .doOnSubscribe(d -> log.debug("DELETE request: {}:{}{}", this.cmHost, this.cmPort, path))
                                                                .doOnSuccess(resp ->
                                                                {
                                                                    if (this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                                                        && (log.isDebugEnabled() || resp.statusCode() < 200 || resp.statusCode() >= 300))
                                                                    {
                                                                        log.info("DELETE response: {} {}", resp.statusCode(), resp.bodyAsString());
                                                                    }
                                                                })
                                                                .map(HttpResponse::statusCode))
                                 .retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                 .doOnError(e -> log.error("Error on delete", e));
    }

    private Single<Integer> post(String route,
                                 Object jsonBody)
    {
        final String path = this.cmPath + route;

        return rxSendJson(this.clientSubject.getWebClient()
                                            .map(webClient -> webClient.post(this.cmPort, this.cmHost, path)
                                                                       .ssl(this.sslEnabled)
                                                                       .timeout(REQUEST_TIMEOUT_MILLIS)),
                          jsonBody).retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                   .doOnError(e -> log.error("Error on post", e))
                                   .doOnSubscribe(d -> log.debug("POST request: {}:{}{}\n{}",
                                                                 this.cmHost,
                                                                 this.cmPort,
                                                                 path,
                                                                 json.writeValueAsString(jsonBody)))
                                   .doOnSuccess(resp ->
                                   {
                                       if (this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                           && (log.isDebugEnabled() || resp.statusCode() < 200 || resp.statusCode() >= 300))
                                       {
                                           log.info("POST response: {} {}", resp.statusCode(), resp.bodyAsString());
                                       }
                                   })
                                   .map(HttpResponse::statusCode);

    }

    private Single<Integer> put(String route,
                                String item,
                                Object jsonBody)
    {
        final String path = this.cmPath + route + "/" + item;

        return rxSendJson(this.clientSubject.getWebClient()
                                            .map(webClient -> webClient.put(this.cmPort, this.cmHost, path)
                                                                       .ssl(this.sslEnabled)
                                                                       .timeout(REQUEST_TIMEOUT_MILLIS)),
                          jsonBody).retryWhen(DEFAULT_RETRY_FUNCTION.create())
                                   .doOnError(e -> log.error("Error on put", e))//
                                   .doOnSubscribe(d -> log.debug("PUT request: {}:{}{}\n{}", this.cmHost, this.cmPort, path, json.writeValueAsString(jsonBody)))
                                   .doOnSuccess(resp ->
                                   {
                                       if (this.logThrottler.loggingIsDue(log.isDebugEnabled())
                                           && (log.isDebugEnabled() || resp.statusCode() < 200 || resp.statusCode() >= 300))
                                       {
                                           log.info("PUT response: {} {}", resp.statusCode(), resp.bodyAsString());
                                       }
                                   })
                                   .map(HttpResponse::statusCode);
    }

    /**
     * @param req
     * @param body
     * @return
     * @throws IllegalArgumentException
     */
    private Single<HttpResponse<Buffer>> rxSendJson(Single<HttpRequest<Buffer>> req,
                                                    Object body)
    {
        try
        {
            final byte[] jsonBytes = json.writeValueAsBytes(body);
            return req.flatMap(webClient -> webClient.putHeader("Content-Type", APPLICATION_JSON).rxSendBuffer(Buffer.buffer(jsonBytes)));
        }
        catch (JsonProcessingException e)
        {
            return Single.error(e);
        }
    }

}
