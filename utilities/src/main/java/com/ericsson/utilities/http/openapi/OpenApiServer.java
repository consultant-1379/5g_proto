/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 16, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.http.openapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.openapi.OpenApiTask.DataIndex;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class OpenApiServer
{
    public static class Context
    {
        private final String mountPoint;
        private final String pathToSpec;
        private final OpenApiTask.Factory[] factories;

        public Context(final String mountPoint,
                       final String pathToSpec,
                       final OpenApiTask.Factory... factories)
        {
            this.mountPoint = mountPoint;
            this.pathToSpec = pathToSpec;
            this.factories = factories;
        }

        public OpenApiTask.Factory[] getFactories()
        {
            return this.factories;
        }

        public String getMountPoint()
        {
            return this.mountPoint;
        }

        public String getPathToSpec()
        {
            return this.pathToSpec;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("mountPoint=")
                                      .append(this.mountPoint)
                                      .append(", pathToSpec=")
                                      .append(this.pathToSpec)
                                      .append(", factories=")
                                      .append(Arrays.toString(this.factories))
                                      .toString();
        }
    }

    public static class Context2
    {
        private final String mountPoint;
        private final String pathToSpec;
        private final IfApiHandler handler;

        public Context2(final String mountPoint,
                        final String pathToSpec,
                        final IfApiHandler handler)
        {
            this.mountPoint = mountPoint;
            this.pathToSpec = pathToSpec;
            this.handler = handler;
        }

        public IfApiHandler getHandler()
        {
            return this.handler;
        }

        public String getMountPoint()
        {
            return this.mountPoint;
        }

        public String getPathToSpec()
        {
            return this.pathToSpec;
        }

        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("mountPoint=")
                                      .append(this.mountPoint)
                                      .append(", pathToSpec=")
                                      .append(this.pathToSpec)
                                      .append(", handler=")
                                      .append(this.handler)
                                      .toString();
        }
    }

    public static class Context3
    {
        private final String pathToSpec;
        private final IfApiHandler handler;

        public Context3(final String pathToSpec,
                        final IfApiHandler handler)
        {
            this.pathToSpec = pathToSpec;
            this.handler = handler;
        }

        public IfApiHandler getHandler()
        {
            return this.handler;
        }

        public String getPathToSpec()
        {
            return this.pathToSpec;
        }

        public String toString()
        {
            return new StringBuilder().append("{").append("pathToSpec=").append(this.pathToSpec).append(", handler=").append(this.handler).toString();
        }
    }

    public interface IfApiHandler extends Handler<RoutingContext>
    {
        Map<String, BiConsumer<RoutingContext, Event>> getHandlerByOperationId();
    }

    public static class RouterFactory
    {
        @JsonPropertyOrder({ "route", "method", "operationId", "handler" })
        private static class Context
        {
            private static final ObjectMapper json = Jackson.om();

            public static Context of()
            {
                return new Context();
            }

            public static Context of(final Context other)
            {
                return new Context(other);
            }

            private String url = "";
            private String path = null;
            private HttpMethod method = null;
            private String operationId = null;
            private Handler<RoutingContext> handler = null;

            private Context()
            {
            }

            private Context(final Context other)
            {
                this.url = other.url;
                this.path = other.path;
                this.method = other.method;
                this.operationId = other.operationId;
                this.handler = other.handler;
            }

            @JsonProperty("handler")
            public Handler<RoutingContext> getHandler()
            {
                return this.handler;
            }

            @JsonIgnore
            public HttpMethod getMethod()
            {
                return this.method;
            }

            @JsonProperty("method")
            public String getMethodAsString()
            {
                return this.method.name();
            }

            @JsonProperty("operationId")
            public String getOperationId()
            {
                return this.operationId;
            }

            @JsonProperty("route")
            public String getRoute()
            {
                return ".*" + this.url + this.path;
            }

            public void setHandler(Handler<RoutingContext> handler)
            {
                this.handler = handler;
            }

            public void setMethod(String method)
            {
                this.method = HttpMethod.valueOf(method.toUpperCase());
            }

            public void setOperationId(String operationId)
            {
                this.operationId = operationId;
            }

            public void setPath(String path)
            {
                this.path = path;
            }

            public void setUrl(String url)
            {
                this.url = url;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        public static Single<RouterFactory> rxCreate(final Vertx vertx,
                                                     final String url)
        {
            return Single.fromCallable(() -> new RouterFactory(vertx, url));
        }

        private final Vertx vertx;
        private final Map<String /* operationId */, Context> contexts = new TreeMap<>();

        private String url = "/";

        private RouterFactory(final Vertx vertx,
                              final String urlOfOpenApiResource)
        {
            this.vertx = vertx;

            final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(urlOfOpenApiResource);

            try
            {
                this.analyzeOpenApiDoc(systemResourceAsStream);
            }
            catch (IOException e)
            {
                log.error("Error reading file {}. Cause: {}", urlOfOpenApiResource, e.toString());
            }
        }

        public RouterFactory addHandlerByOperationId(final String operationId,
                                                     final Handler<RoutingContext> handler)
        {
            final Context context = this.contexts.get(operationId);

            if (context != null)
            {
                log.info("Setting handler for operationId '{}'", operationId);
                context.setHandler(handler);
            }
            else
            {
                log.error("Invalid operationId '{}'", operationId);
            }

            return this;
        }

        public Router build()
        {
            final Router router = Router.router(this.vertx);

            this.contexts.values()
                         .stream()
                         .sorted((l,
                                  r) -> 0 - l.getRoute().compareTo(r.getRoute()))
                         .forEach(ctx ->
                         {
                             if (ctx.getHandler() != null)
                             {
                                 log.info("Adding router for context '{}'", ctx);

                                 if (ctx.getMethod().equals(HttpMethod.CONNECT))
                                 {
                                     router.connectWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.connectWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.DELETE))
                                 {
                                     router.deleteWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.deleteWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.GET))
                                 {
                                     router.getWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.getWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.HEAD))
                                 {
                                     router.headWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.headWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.OPTIONS))
                                 {
                                     router.optionsWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.optionsWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.PATCH))
                                 {
                                     router.patchWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.patchWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.POST))
                                 {
                                     router.postWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.postWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.PUT))
                                 {
                                     router.putWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.putWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                                 else if (ctx.getMethod().equals(HttpMethod.TRACE))
                                 {
                                     router.traceWithRegex(ctx.getRoute()).handler(BodyHandler.create());
                                     router.traceWithRegex(ctx.getRoute()).handler(ctx.getHandler());
                                 }
                             }
                         });

            return router;
        }

        private void analyzeOpenApiDoc(final InputStream inputStream) throws IOException
        {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String line;

                final Pattern pUrl = Pattern.compile("^[ ]{0,2}-[ ]*+url:[^/]+?(/[^']+).*+$");
                final Pattern pRoute = Pattern.compile("^[ ]{0,2}(/[^:]+):[ ]*$");
                final Pattern pMethod = Pattern.compile("^[ ]{2,4}([a-z]+):[ ]*$");
                final Pattern pOpId = Pattern.compile("^[ ]{4,6}operationId:[ ]*([^ ]+(?: +[^ ]+)*+)[ ]*$");
                final Pattern pVar = Pattern.compile("\\{(.+?)\\}");

                final Context ctx = Context.of();

                int state = 0;

                while ((line = br.readLine()) != null)
                {
                    if (line.matches("^servers:[ ]*$"))
                    {
                        state = 1;
                        continue;
                    }

                    if (line.matches("^paths:[ ]*$"))
                    {
                        state = 2;
                        continue;
                    }

                    if (line.matches("^components:[ ]*$"))
                        break;

                    if (state == 1)
                    {
                        final Matcher mUrl = pUrl.matcher(line);

                        if (mUrl.matches())
                        {
                            ctx.setUrl(mUrl.group(1));
                        }
                    }
                    else if (state == 2)
                    {
                        final Matcher mRoute = pRoute.matcher(line);

                        if (mRoute.matches())
                        {
                            final String route = mRoute.group(1);
                            final Matcher mVar = pVar.matcher(route);

                            final StringBuilder b = new StringBuilder();
                            int pos = 0;

                            while (mVar.find())
                            {
                                b.append(route.substring(pos, mVar.start())).append("(?<").append(mVar.group(1)).append(">[^/]+)");
                                pos = mVar.end();
                            }

                            b.append(route.substring(pos));

                            ctx.setPath(b.toString());
                        }
                        else
                        {
                            final Matcher mMethod = pMethod.matcher(line);

                            if (mMethod.matches())
                            {
                                ctx.setMethod(mMethod.group(1));
                            }
                            else
                            {
                                final Matcher mOpId = pOpId.matcher(line);

                                if (mOpId.matches())
                                {
                                    ctx.setOperationId(mOpId.group(1));
                                    log.debug("ctx={}", ctx);
                                    this.contexts.put(ctx.getOperationId(), Context.of(ctx));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(OpenApiServer.class);

    private final Vertx vertx;
    private final RouterHandler webServer;
    private final List<Router> routers;

    public OpenApiServer(final RouterHandler webServer)
    {
        this.vertx = webServer.getVertx();
        this.webServer = webServer;
        this.routers = new ArrayList<>();
    }

    public final void configure(final List<Context2> contexts)
    {
        this.cleanupRouters();

        Flowable.fromIterable(contexts)//
                .flatMapSingle(context ->
                {
                    log.info("context={}", context);

                    final Consumer<? super OpenAPI3RouterFactory> onSuccess = f ->
                    {
                        f.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(true).setRequireSecurityHandlers(false));

                        for (Entry<String, BiConsumer<RoutingContext, Event>> handler : context.getHandler().getHandlerByOperationId().entrySet())
                        {
                            log.info("Creating Vertx handler for operation {}", handler.getKey());
                            f.addHandlerByOperationId(handler.getKey(),
                                                      routingContext -> context.getHandler()
                                                                               .handle(routingContext.put(DataIndex.OPERATION_ID.name(), handler.getKey())
                                                                                                     .put(DataIndex.HANDLER.name(), handler.getValue())));
                        }

                        final Router router = f.getRouter();
                        this.routers.add(router);
                        this.webServer.mountRouter(context.getMountPoint(), router);
                    };

                    final Consumer<? super Throwable> onError = t -> log.error("Could not create router factory.", t);

                    return OpenAPI3RouterFactory.rxCreate(this.vertx, context.getPathToSpec()).doOnSuccess(onSuccess).doOnError(onError);
                })
                .toList()
                .ignoreElement()
                .blockingAwait();
    }

    public enum IpFamily
    {
        IPv4,
        IPv6;

        public static IpFamily of(final String ipAddress)
        {
            return ipAddress.contains(".") ? IPv4 : IPv6;
        }
    }

    public final void configure2(final IpFamily ipFamily,
                                 final List<Context3> contexts)
    {
        this.cleanupRouters();

        Flowable.fromIterable(contexts)//
                .flatMapSingle(context ->
                {
                    log.info("context={}", context);

                    final Consumer<RouterFactory> onSuccess = f ->
                    {
                        for (Entry<String, BiConsumer<RoutingContext, Event>> handler : context.getHandler().getHandlerByOperationId().entrySet())
                        {
                            f.addHandlerByOperationId(handler.getKey(),
                                                      routingContext -> context.getHandler()
                                                                               .handle(routingContext.put(DataIndex.IP_FAMILY.name(), ipFamily)
                                                                                                     .put(DataIndex.OPERATION_ID.name(), handler.getKey())
                                                                                                     .put(DataIndex.HANDLER.name(), handler.getValue())));
                        }

                        final Router router = f.build();
                        this.routers.add(router);
                        this.webServer.mountRouter("/", router);
                    };

                    final Consumer<? super Throwable> onError = t -> log.error("Could not create router factory.", t);

                    return RouterFactory.rxCreate(this.vertx, context.getPathToSpec()).doOnSuccess(onSuccess).doOnError(onError);
                })
                .toList()
                .ignoreElement()
                .blockingAwait();
    }

    @SafeVarargs
    public final Flowable<OpenApiTask> create(final Context... contexts)
    {
        final Flowable<OpenApiTask> flowable = Flowable.create(emitter ->
        {
            for (Context context : contexts)
            {
                log.info("context={}", context);

                final Consumer<? super OpenAPI3RouterFactory> onSuccess = f ->
                {
                    f.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(true).setRequireSecurityHandlers(false));

                    for (OpenApiTask.Factory factory : context.getFactories())
                    {
                        log.info("Creating Vertx handler for operation {}", factory.operationId());
                        f.addHandlerByOperationId(factory.operationId(), routingContext -> emitter.onNext(factory.create(routingContext)));
                    }

                    final Router router = f.getRouter();
                    this.routers.add(router);
                    this.webServer.mountRouter(context.getMountPoint(), router);

                    log.info("Registered operations {}", Arrays.toString(context.getFactories()));
                };

                final Consumer<? super Throwable> onError = t -> log.error("Could not create router factory.", t);

                OpenAPI3RouterFactory.rxCreate(this.vertx, context.getPathToSpec()).doOnSuccess(onSuccess).doOnError(onError).blockingGet();
            }
        }, BackpressureStrategy.ERROR);

        return flowable.doOnSubscribe(s -> this.cleanupRouters());
    }

    private void cleanupRouters()
    {
        log.info("Cleaning up {} router{}.", this.routers.size(), this.routers.size() != 1 ? "s" : "");
        this.routers.forEach(Router::clear);
        this.routers.clear();
    }
}
