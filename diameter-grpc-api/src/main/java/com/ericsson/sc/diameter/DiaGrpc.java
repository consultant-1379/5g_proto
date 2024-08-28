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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterClientGrpc.DiameterClientImplBase;
import com.ericsson.sc.diameter.DiaGrpcClient.Context;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Notification;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * A reactive gRPC server for Diameter proxy gRPC
 */
public final class DiaGrpc
{
    private static final Logger log = LoggerFactory.getLogger(DiaGrpc.class);

    private static final int SERVER_SHUTDOWN_GRACE_PERIOD = 60; // 20 Seconds
    private final DiaGrpcClient client;
    private final ServerBuilder<?> server;
    private final AtomicReference<State> oldState = new AtomicReference<State>(State.empty());

    /**
     * 
     * @param client  the reactive Diameter proxy gRPC client
     * @param sb      A gRPC {@link ServerBuilder} with the desired server
     *                configuration
     * @param handler A handler implementing the server's business logic
     */
    public DiaGrpc(final DiaGrpcClient client,
                   final ServerBuilder<?> sb,
                   final DiaGrpcHandler handler)
    {
        this.client = client;
        this.server = sb.addService(new DiameterClientImplBase()

        {
            @Override
            public Single<IncomingRequestResult> processRequest(Single<IncomingRequest> request)
            {
                return handler.processRequest(request, client.getContext());
            }
        });
    }

    /**
     * Create the gRPC server and run until completion. The server is shutdown upon
     * errors or when the given Completable completes
     * 
     * @param stop A Completable that is used to trigger stopping of the server
     * @return The state of the server. Note that server is created upon
     *         subscription
     */
    public Flowable<State> run(Completable stop)
    {

        return this.client.run()
                          .doOnNext(ctx -> log.info("Processing new diameter client context: {}", ctx))
                          .takeUntil(stop.toFlowable().doOnComplete(() -> log.info("Stop indication received")))
                          .materialize()
                          .concatMapSingle(this::stateMachine)
                          .filter(State::isNotEmpty)
                          .doOnNext(next ->
                          {
                              log.info("New state: {}", next);
                              oldState.set(next);
                          });

    }

    /**
     * 
     * @return The reactive gRPC client associated with this server
     */
    public DiaGrpcClient getClient()
    {
        return this.client;
    }

    private Single<State> stateMachine(Notification<Context> ctx)
    {

        final var previousState = oldState.get();
        final var previousSrv = previousState.getGrpcServer();
        final var error = ctx.getError();
        if (error != null)
        {
            log.warn("gRPC client terminated due to error, shutting down gRPC server");

            return previousSrv != null ? serverShutdown(previousSrv, SERVER_SHUTDOWN_GRACE_PERIOD).andThen(updateSrvDownRef(previousState))
                                                                                                  .andThen(Single.<State>error(error))
                                       : Single.<State>error(error);
        }

        // No error from upstream
        final var newContextEmission = ctx.getValue();
        if (newContextEmission == null)
        {

            if (previousSrv != null)
            {
                log.info("Gracefully shutting down gRPC server");
                return serverShutdown(previousSrv, SERVER_SHUTDOWN_GRACE_PERIOD).<State>toSingleDefault(previousState.updateServerShutdown());
            }
            else
            {
                return Single.just(previousState);
            }
        }

        else
        {
            log.info("Calculating new state {} {}", previousState, ctx);
            if (previousSrv == null && newContextEmission.isInitialSyncDone())
            {
                // Create and start a gRPC server
                return this.newServer() //
                           .<State>map(grpcServer -> previousState.update(newContextEmission, grpcServer));
            }
            else
            {

                return Single.<State>just(previousState.update(newContextEmission));
            }
        }
    }

    private static Completable startServer(Server server)
    {
        return Completable.fromAction(server::start) //
                          .subscribeOn(Schedulers.io())
                          .doOnSubscribe(disp -> log.info("Starting new gRPC server"));
    }

    private Single<Server> newServer()
    {
        return Single.fromCallable(this.server::build)
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe(disp -> log.info("Building new gRPC server"))
                     //
                     .flatMap(srv -> startServer(srv).toSingleDefault(srv));
    }

    private static Completable serverShutdown(Server server,
                                              int gracePeriod)
    {
        return Completable.fromAction(() ->
        {
            log.info("Shutting down gRPC server {}", server);
            if (!server.isTerminated())
            {
                server.shutdown();
                try
                {
                    server.awaitTermination(gracePeriod, TimeUnit.SECONDS);
                }
                catch (InterruptedException e)
                {
                    log.warn("Gracefull Diameter gRPC grpcServer termination timed out after {} seconds", gracePeriod);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                    server.shutdownNow();
                    try
                    {
                        server.awaitTermination(5, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException e1)
                    {
                        log.warn("Forced Diameter gRPC grpcServer termination timed out");
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                }
            }
        })
                          .subscribeOn(Schedulers.io()) //
                          .doOnError(err -> log.warn("Error during gRPC server shutdown", err))
                          .onErrorComplete();
    }

    private Completable updateSrvDownRef(State prevState)
    {
        log.info("Updating gRPC server status to off");
        return Completable.fromAction(() -> oldState.set(prevState.updateServerShutdown()));
    }

    /**
     * the gRPC server state
     */
    public static class State
    {
        private final DiaGrpcClientContext grpcClientContext;
        private final Server grpcServer;

        /**
         * 
         * @return the reactive gRPC client context
         */
        public DiaGrpcClientContext getGrpcClientContext()
        {
            return grpcClientContext;
        }

        /**
         * 
         * @return The gRPC server or null if the server has been stopped
         */
        public Server getGrpcServer()
        {
            return grpcServer;
        }

        private State(DiaGrpcClientContext context,
                      Server server)
        {
            this.grpcClientContext = context;
            this.grpcServer = server;
        }

        private State()
        {
            this.grpcClientContext = null;
            this.grpcServer = null;
        }

        /**
         * Create an empty initial state
         * 
         * @return The initial empty state
         */
        private static State empty()
        {
            return new State();
        }

        private State update(Context context)
        {
            Objects.requireNonNull(context);
            return new State(context, this.grpcServer);
        }

        private State updateServerShutdown()
        {
            return new State(this.grpcClientContext, null);
        }

        private boolean isNotEmpty()
        {
            return this.grpcClientContext != null;
        }

        private State update(Context context,
                             Server newServer)
        {
            Objects.requireNonNull(context);
            Objects.requireNonNull(newServer);
            if (this.grpcServer != null)
                throw new IllegalStateException("Old server expected null " + this.grpcServer);
            return new State(context, newServer);
        }

        @Override
        public String toString()
        {
            var builder = new StringBuilder();
            builder.append("State [grpcClientContext=");
            builder.append(grpcClientContext);
            builder.append(", grpcServer=");
            builder.append(grpcServer);
            builder.append("]");
            return builder.toString();
        }
    }
}
