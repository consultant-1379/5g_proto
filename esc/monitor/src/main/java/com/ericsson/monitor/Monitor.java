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
 * Created on: Nov 13, 2018
 *     Author: eedstl
 */

package com.ericsson.monitor;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.MonitorContext;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Commands;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.ext.monitor.api.v0.register.Callback;
import com.ericsson.adpal.ext.monitor.api.v0.register.Register;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Registry;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.auth.authentication.AuthenticationProvider;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.handler.AuthenticationHandler;
import io.vertx.reactivex.ext.web.handler.BasicAuthHandler;

/**
 * 
 */
public class Monitor
{
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME", "eric-sc-monitor");
    private static final String LOG_CONTROL_FILE = "logcontrol.json";

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);
    private static final URI logControlUri = URI.create("/monitor/config/logcontrol");
    private static final URI externalServerCertsUri = URI.create("/run/secrets/monitor/external/certificates");
    private static final URI externalClientCaUri = URI.create("/run/secrets/iccr/ca");

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Starting Monitor, version: {}", VersionInfo.get());
        try (var llc = new LogLevelChanger(ConfigmapWatch.builder()
                                                         .withFileName(LOG_CONTROL_FILE) //
                                                         .withRoot(logControlUri.getPath())
                                                         .build(),
                                           CONTAINER_NAME))
        {
            Monitor app = new Monitor();
            app.run();
        }
        catch (Exception e)
        {
            log.error("Error starting monitor.", e);
            exitStatus = 1;
        }

        log.info("Stopped Monitor.");

        System.exit(exitStatus);
    }

    private final MonitorParameters params;
    private final ObjectMapper mapper;
    private final RouterHandler webServerExternal;
    private final RouterHandler webServerInternal;
    private final WebClient client;
    private final String id;
    private final Registry<URI, Callback> register;

    public Monitor() throws UnknownHostException
    {
        this.mapper = Jackson.om();

        this.params = MonitorParameters.fromEnvironment();

        // interface towards iccr, supports service authentication with tls enabled
        this.webServerExternal = this.params.isTlsEnabled() ? WebServer.builder()
                                                                       .withHost(DEFAULT_ROUTE_ADDRESS)
                                                                       .withPort(this.params.getExternalPort())
                                                                       .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(externalServerCertsUri.getPath()),
                                                                                                                    SipTlsCertWatch.trustedCert(externalClientCaUri.getPath())))
                                                                       .build(VertxInstance.get())
                                                            : WebServer.builder()
                                                                       .withHost(DEFAULT_ROUTE_ADDRESS)
                                                                       .withPort(this.params.getExternalPort())
                                                                       .build(VertxInstance.get());

        this.webServerInternal = WebServer.builder() //
                                          .withHost(DEFAULT_ROUTE_ADDRESS)
                                          .withPort(this.params.getInternalPort())
                                          .build(VertxInstance.get());

        this.client = WebClient.create(VertxInstance.get(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1));

        this.id = InetAddress.getLocalHost().getHostName();

        {
            var router = Router.router(this.webServerExternal.getVertx());
            router.route(MonitorContext.Operation.COMMANDS.getName()).handler(this::handleAuth);
            router.get(MonitorContext.Operation.COMMANDS.getName()).handler(this::handlerCommands);
            router.put(MonitorContext.Operation.COMMANDS.getName()).handler(this::handlerCommands);
            this.webServerExternal.mountRouter(MonitorContext.MONITOR_API, router);
        }

        {
            var router = Router.router(this.webServerInternal.getVertx());
            router.route(MonitorContext.Operation.REGISTER.getName()).handler(this::handlerRegister);
            this.webServerInternal.mountRouter(MonitorContext.MONITOR_API, router);
        }

        this.register = new Registry<>(20000);
    }

    public void run()
    {
        log.info("Running...");

        try
        {
            Completable.complete()//
                       .andThen(this.webServerExternal.startListener())
                       .andThen(this.webServerInternal.startListener())
                       .andThen(this.register.start())
                       .andThen(Completable.create(emitter ->
                       {
                           log.info("Registering shutdown hook.");
                           Runtime.getRuntime().addShutdownHook(new Thread(() ->
                           {
                               log.info("Shutdown hook called.");
                               this.stop().blockingAwait();
                               emitter.onComplete();
                           }));
                       }))
                       .blockingAwait();
        }
        catch (Exception e)
        {
            log.error("Exception caught, stopping monitor.", e);
        }

        log.info("Stopped.");
    }

    public Completable stop()
    {
        return Completable.complete()//
                          .andThen(this.register.stop().onErrorComplete())
                          .andThen(this.webServerExternal.stopListener().onErrorComplete())
                          .andThen(this.webServerInternal.stopListener().onErrorComplete());
    }

    private void handleAuth(final RoutingContext routingContext)
    {
        final JsonObject credentials = new JsonObject();
        credentials.put("username", this.params.getUsername());
        credentials.put("password", this.params.getPassword());
        final SimpleAuthProvider simpleAuthProvider = new SimpleAuthProvider(credentials);
        final AuthenticationProvider authProvider = new AuthenticationProvider(simpleAuthProvider);
        final AuthenticationHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
        basicAuthHandler.handle(routingContext);
    }

    private void handlerCommands(final RoutingContext routingContext)
    {
        routingContext.request().bodyHandler(buffer ->
        {
            final Commands commands = new Commands();
            final List<Result> results = new ArrayList<>();
            commands.setResults(results);

            try
            {
                final String paramCommand = routingContext.request().getParam("command");
                final Command command = new Command(System.currentTimeMillis(), paramCommand);
                commands.setCommand(command);

                final String paramTarget = routingContext.request().getParam("target");

                routingContext.request().params().entries().forEach(e ->
                {
                    if (!e.getKey().equals("command") && !e.getKey().equals("target"))
                        command.setAdditionalProperty(e.getKey(), e.getValue().isEmpty() ? "true" : e.getValue()); // Assume boolean parameter if value is
                                                                                                                   // empty.
                                                                                                                   // Presence of a boolean parameter means
                                                                                                                   // true.
                });

                final String body = buffer.toString();

                log.debug("body={}", body);

                if (body != null && !body.isEmpty())
                    command.setAdditionalProperty("data", body);

                this.sendCommands(routingContext, commands, paramTarget);
            }
            catch (IllegalArgumentException e)
            {
                this.replyWithError(routingContext, commands, HttpResponseStatus.BAD_REQUEST, "Invalid argument: 'command=" + e.getMessage() + "'.");
            }
            catch (Exception e)
            {
                this.replyWithError(routingContext, commands, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });
    }

    private void handlerRegister(final RoutingContext routingContext)
    {
        routingContext.request().bodyHandler(buffer ->
        {
            log.debug("buffer={}", buffer);

            try
            {
                final Register request = this.mapper.readValue(buffer.toJsonObject().toString(), Register.class);
                log.info("Registration request received: {}", request);

                request.getCallbacks().forEach(cb -> this.register.put(cb.getUri(), cb));
                log.debug("register={}", this.register);

                request.setResult("Registered callbacks for ID=" + request.getId());
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(this.mapper.writeValueAsString(request));
            }
            catch (Exception e)
            {
                log.error("Exception while processing registration request: {}", e.toString());
            }
        });
    }

    private void replyWithError(final RoutingContext routingContext,
                                final Commands commands,
                                final HttpResponseStatus status,
                                final String errorMsg)
    {
        final Result result = new Result(System.currentTimeMillis(), this.id, status.code());
        result.setAdditionalProperty("errorMessage", errorMsg);
        commands.getResults().add(result);

        try
        {
            routingContext.response().setStatusCode(status.code()).end(this.mapper.writeValueAsString(commands));
        }
        catch (JsonProcessingException e)
        {
            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
        }
    }

    private void sendCommands(final RoutingContext routingContext,
                              final Commands commands,
                              final String target)
    {

        Observable.fromIterable(this.register.entrySet())//
                  .flatMap(cb -> Observable.just(cb)//
                                           .subscribeOn(Schedulers.io())
                                           .filter(f -> target == null || target.isEmpty() ? true : f.getValue().getSource().startsWith(target))
                                           .map(m ->
                                           {
                                               final String uri = m.getValue().getUri().toString();
                                               log.debug("requestUri={}", uri);

                                               if (routingContext.request().method().equals(HttpMethod.GET))
                                               {
                                                   return this.sendGet(commands, uri).blockingGet();
                                               }
                                               else if (routingContext.request().method().equals(HttpMethod.PUT))
                                               {
                                                   return this.sendPut(commands, uri).blockingGet();
                                               }
                                               else
                                               {
                                                   log.error("Method not supported: '{}'.", routingContext.request().method());
                                                   return Optional.<Commands>empty(); // Will not happen as no handler registered for other methods.
                                               }
                                           }))
                  .filter(Optional::isPresent)
                  .toList()
                  .doOnSuccess(l ->
                  {
                      final SortedMap<String, Commands> m = new TreeMap<>();

                      for (Optional<Commands> c : l)
                          m.put(c.get().getResults().get(0).getSource(), c.get());

                      for (Commands c : m.values())
                          commands.getResults().add(c.getResults().get(0));

                      log.debug("Sending response: result={}", commands);
                      routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(this.mapper.writeValueAsString(commands));
                  })
                  .subscribe(d ->
                  {
                  }, t -> log.error("Error sending command. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
    }

    private Single<Optional<Commands>> sendGet(Commands commands,
                                               String uri)
    {
        return this.client.getAbs(uri)
                          .timeout(10000)
                          .rxSendJson(commands) //
                          .map(resp ->
                          {
                              log.debug("GET response: {} {}", resp.statusCode(), resp.bodyAsString());
                              return Optional.of(this.mapper.readValue(resp.bodyAsString(), Commands.class));
                          })
                          .onErrorReturnItem(Optional.empty());
    }

    private Single<Optional<Commands>> sendPut(Commands commands,
                                               String uri)
    {
        return this.client.putAbs(uri)
                          .timeout(10000)
                          .rxSendJson(commands) //
                          .map(resp ->
                          {
                              log.debug("PUT response: {} {}", resp.statusCode(), resp.bodyAsString());
                              return Optional.of(this.mapper.readValue(resp.bodyAsString(), Commands.class));
                          })
                          .onErrorReturnItem(Optional.empty());
    }
}
