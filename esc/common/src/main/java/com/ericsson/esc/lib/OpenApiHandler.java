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
 * Created on: Oct 16, 2018
 *     Author: xchrfar
 */

package com.ericsson.esc.lib;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.ericsson.utilities.http.WebServerRouter;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

/**
 * A Reactive OpenApi web server based on Vertx
 */
public class OpenApiHandler
{
    private enum Lbl // Log limiter labels
    {
        THROTTLE_ERROR,
    }

    private static final Logger log = LoggerFactory.getLogger(OpenApiHandler.class);
    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    private final WebServerRouter webServerRouter;
    private final String mountPoint;
    private final Map<String, Completable> registeredOperations = new ConcurrentHashMap<>();
    private final Map<String, Handler<RoutingContext>> subscribedOperations = new ConcurrentHashMap<>();
    private final Single<Router> router;
    private final CompletableSubject initialized = CompletableSubject.create();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Completable mainChain;
    private final CompletableSubject stopIndication = CompletableSubject.create();
    private final int maxOutstandingOps;

    /**
     * Builder for OpenApiHandler objects
     */
    public static class Builder
    {
        private WebServerRouter router;
        private URL specUrl = null;
        private String pathToSpec = null;
        private String mountPoint = "/";
        private int maxOutstandingOperations = Integer.MAX_VALUE;
        private Path directoryTempPath = null;

        /**
         * 
         * @return A new OpenApiHandler instance
         */
        public OpenApiHandler build()
        {
            return new OpenApiHandler(this);
        }

        public WebServerRouter getRouter()
        {
            return router;
        }

        /**
         * 
         * @param router The router to use. Mandatory.
         * @return
         */
        public Builder setRouter(WebServerRouter router)
        {
            this.router = router;
            return this;
        }

        public URL getSpecUrl()
        {
            return specUrl;
        }

        /**
         * The openapi YAML spec. The file should be self contained, i.e should have no
         * reference to other files.Mutual exclusive with {@link #setPathToSpec(String)}
         * 
         * @return
         */
        public Builder setSpecUrl(URL specUrl)
        {
            this.specUrl = specUrl;
            return this;
        }

        public String getPathToSpec()
        {
            return pathToSpec;
        }

        /**
         * @param pathToSpec The openapi YAML spec. File can have references to other
         *                   files. Mutual exclusive with {@link #setSpecUrl(URL)}
         * @return
         */
        public Builder setPathToSpec(String pathToSpec)
        {
            this.pathToSpec = pathToSpec;
            return this;
        }

        public String getMountPoint()
        {
            return mountPoint;
        }

        /**
         * 
         * @param mountPoint The mount point on the given router, where the service
         *                   shall be attached. Default value is "/"
         * @return
         */
        public Builder setMountPoint(String mountPoint)
        {
            this.mountPoint = mountPoint;
            return this;
        }

        public int getMaxOutstandingOperations()
        {
            return maxOutstandingOperations;
        }

        /**
         * 
         * @param maxOutstandingOperations The maximum number of Outstanding operations
         *                                 that may exist for each openapi operation.
         *                                 Incoming requests are rejected when maximum
         *                                 number is reached. Default value is
         *                                 #Integer.MAX_VALUE, which indicates no limit.
         * @return
         */
        public Builder setMaxOutstandingOperations(int maxOutstandingOperations)
        {
            this.maxOutstandingOperations = maxOutstandingOperations;
            return this;
        }

        public Builder setDirectoryTempPath(Path directoryTempPath)
        {
            this.directoryTempPath = directoryTempPath;
            return this;
        }
    }

    /**
     * 
     * @return A new Builder
     */
    public static Builder builder()
    {
        return new Builder();
    }

    private OpenApiHandler(Builder builder)
    {

        if (builder.maxOutstandingOperations <= 0)
            throw new IllegalArgumentException("Invalid maxPendingOperations");
        if ((builder.pathToSpec != null && builder.specUrl != null) || (builder.pathToSpec == null && builder.specUrl == null))
            throw new IllegalArgumentException("Invalid specUrl/pathToSpec");
        Objects.requireNonNull(builder.router);
        Objects.requireNonNull(builder.mountPoint);

        final var specUrl = builder.specUrl;
        final var pathToSpec = builder.pathToSpec;
        this.webServerRouter = builder.router;
        this.mountPoint = builder.mountPoint;
        this.maxOutstandingOps = builder.maxOutstandingOperations;
        final var tempDirectoryPath = builder.directoryTempPath;

        this.router = initialized.andThen(specUrl != null ? createRouterFactory(this.webServerRouter.getVertx(), specUrl, tempDirectoryPath)
                                                          : createRouterFactory(this.webServerRouter.getVertx(), pathToSpec)) //
                                 .map(factory ->
                                 {
                                     // FIXME This is needed for SLF yaml. Why? Make it configurable
                                     factory.setOptions(new RouterFactoryOptions() //
                                                                                  .setMountNotImplementedHandler(true)
                                                                                  .setRequireSecurityHandlers(false));
                                     this.subscribedOperations.forEach(factory::addHandlerByOperationId);
                                     return factory.getRouter();
                                 })
                                 .cache();

        this.mainChain = Completable.defer(() -> Completable.merge(this.registeredOperations.values()) //
                                                            .takeUntil(this.stopIndication))
                                    .doOnSubscribe(disp -> log.info("Starting"))
                                    .doFinally(() -> log.info("Stopped"))
                                    .cache();
    }

    /**
     * Start the Handler. This method is designed to only be called once
     * 
     * @return A cahced Completable indicating starting completion
     */
    public Completable start()
    {
        return mainChain.ambWith(this.initialized //
                                                 .doOnComplete(() -> this.started.set(true))
                                                 .andThen(this.router.doOnSuccess(vertxRouter -> this.webServerRouter.mountRouter(mountPoint, vertxRouter)))
                                                 .ignoreElement())
                        .cache();
    }

    /**
     * Stop the Handler. This method is designed to only be called once
     * 
     * @return A cached Completable indicating stopping completion
     */
    public Completable stop()
    {

        return Completable.complete()
                          .doOnComplete(this.stopIndication::onComplete)
                          .andThen(this.mainChain)
                          .andThen(this.router.doOnSuccess(Router::clear).ignoreElement())
                          .cache();
    }

    public Completable run()
    {
        return this.mainChain;
    }

    public <T extends OpenApiReqParams> OpenApiHandler addRequestHandler(OpenApiOp<T> definition,
                                                                         Function<? super OpenApiReq<T>, ? extends Completable> requestHandler,
                                                                         BiFunction<RoutingContext, Throwable, Completable> errorHandler)
    {
        if (this.started.get())
        {
            throw new IllegalStateException("OpenApiHandler already started");
        }
        final var op = processRequests(createFlowable(definition, errorHandler), requestHandler, errorHandler);
        this.registeredOperations.put(definition.getOperationId(), op);
        return this;
    }

    public <T extends OpenApiReqParams, U> OpenApiHandler addRequestHandler(OpenApiOp<T> definition,
                                                                            BiFunction<? super OpenApiReq<T>, U, ? extends Completable> requestHandler,
                                                                            Flowable<U> context,
                                                                            BiFunction<RoutingContext, Throwable, Completable> errorHandler)
    {
        if (this.started.get())
        {
            throw new IllegalStateException("OpenApiHandler already started");
        }
        final var op = createFlowable(definition, errorHandler) //
                                                               .withLatestFrom(context, Pair::of) //
                                                               .flatMapCompletable(req ->
                                                               {
                                                                   Completable handlerResult;
                                                                   try
                                                                   {
                                                                       handlerResult = requestHandler.apply(req.t1, req.t2);
                                                                   }
                                                                   catch (Exception e)
                                                                   {
                                                                       // The handler is not supposed to throw exceptions
                                                                       handlerResult = Completable.error(e);
                                                                   }
                                                                   return handlerResult.onErrorResumeNext(error -> errorHandler.apply(req.t1.getRoutingContext(),
                                                                                                                                      error))
                                                                                       .onErrorComplete();

                                                               });
        this.registeredOperations.put(definition.getOperationId(), op);
        return this;
    }

    private <T extends OpenApiReqParams> Flowable<OpenApiReq<T>> createFlowable(OpenApiOp<T> definition,
                                                                                BiFunction<RoutingContext, Throwable, Completable> errorHandler)
    {
        return Flowable.<Triplet<OpenApiReq<T>, RoutingContext, Throwable>>create(emitter ->
        {
            final Handler<RoutingContext> handler = event ->
            {
                try
                {
                    final var request = definition.create(event); // throws if request has syntactic-semantic errors
                    emitter.onNext(Triplet.of(request, null, null));
                }
                catch (Exception ex)
                {
                    emitter.onNext(Triplet.of(null, event, ex));
                }
            };

            afterSubscribe(definition.getOperationId(), handler);

            emitter.setCancellable(this.initialized::onComplete);
            log.info("Registered operation {}", definition.operationId);
        }, BackpressureStrategy.ERROR)
                       .flatMapMaybe(re -> re.t1 != null ? Maybe.just(re.t1)
                                                         : errorHandler.apply(re.t2, re.t3) //
                                                                       .onErrorComplete() //

                                                                       .toMaybe());
    }

    private static Single<OpenAPI3RouterFactory> createRouterFactory(Vertx vertx,
                                                                     URL specUrl,
                                                                     Path directoryTempPath)
    {
        // Workaround for the broken OpenApi3Router url resolution
        return Single.defer(() ->
        {
            try (var is = specUrl.openStream())
            {
                final var tmpFile = directoryTempPath != null ? Files.createTempFile(directoryTempPath,
                                                                                     "spec",
                                                                                     ".yaml", //
                                                                                     PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
                                                              : Files.createTempFile("spec",
                                                                                     ".yaml", //
                                                                                     PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
                tmpFile.toFile().deleteOnExit();
                try (final var output = new FileOutputStream(tmpFile.toFile(), false))
                {
                    is.transferTo(output);
                }
                return OpenAPI3RouterFactory.rxCreate(vertx, tmpFile.toUri().toURL().toString());

            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Unable copy specification url to local file: " + specUrl.toString(), e);
            }
        }).subscribeOn(Schedulers.io());
    }

    private static Single<OpenAPI3RouterFactory> createRouterFactory(Vertx vertx,
                                                                     String pathToSpec)
    {
        return OpenAPI3RouterFactory.rxCreate(vertx, pathToSpec);
    }

    private <T extends OpenApiReqParams> Completable processRequests(Flowable<OpenApiReq<T>> requests,
                                                                     Function<? super OpenApiReq<T>, ? extends Completable> requestHandler,
                                                                     BiFunction<RoutingContext, Throwable, Completable> errorHandler)
    {
        final var reqs = maxOutstandingOps == Integer.MAX_VALUE ? requests
                                                                : requests.onBackpressureDrop(droppedRequest -> errorHandler.apply(droppedRequest.routingContext,
                                                                                                                                   new IllegalStateException("Server congestion"))
                                                                                                                            .subscribe(() -> log.debug("Request throttled"),
                                                                                                                                       err -> safeLog.log(Lbl.THROTTLE_ERROR,
                                                                                                                                                          logger -> logger.error("Failed to throttle request",
                                                                                                                                                                                 err))));
        return reqs.flatMapCompletable(req ->
        {
            Completable handlerResult;
            try
            {
                handlerResult = requestHandler.apply(req);
            }
            catch (Exception e)
            {
                handlerResult = Completable.error(e);
            }
            return handlerResult //
                                .onErrorResumeNext(error -> errorHandler.apply(req.getRoutingContext(), error))
                                .onErrorComplete();
        }, false, this.maxOutstandingOps);
    }

    private synchronized void afterSubscribe(String operationId,
                                             Handler<RoutingContext> handler)
    {
        this.subscribedOperations.put(operationId, handler);
        if (this.registeredOperations.keySet().equals(this.subscribedOperations.keySet()))
        {
            log.debug("Subscribed to all handlers");
            initialized.onComplete();
        }
    }

    private static final class Pair<T1, T2>
    {
        public final T1 t1;
        public final T2 t2;

        private Pair(T1 t1,
                     T2 t2)
        {
            this.t1 = t1;
            this.t2 = t2;
        }

        public static <T1, T2> Pair<T1, T2> of(T1 t1,
                                               T2 t2)
        {
            return new Pair<>(t1, t2);
        }
    }

    private static final class Triplet<T1, T2, T3>
    {
        public final T1 t1;
        public final T2 t2;
        public final T3 t3;

        private Triplet(T1 t1,
                        T2 t2,
                        T3 t3)
        {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
        }

        public static <T1, T2, T3> Triplet<T1, T2, T3> of(T1 t1,
                                                          T2 t2,
                                                          T3 t3)
        {
            return new Triplet<>(t1, t2, t3);
        }
    }

}
