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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.gs.tm.diameter.service.grpc.IncomingEvent;
import com.ericsson.gs.tm.diameter.service.grpc.KeepaliveArguments;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.PeerDownEvent;
import com.ericsson.gs.tm.diameter.service.grpc.PeerUpEvent;
import com.ericsson.gs.tm.diameter.service.grpc.PollArguments;
import com.ericsson.gs.tm.diameter.service.grpc.RegisterArguments;
import com.ericsson.gs.tm.diameter.service.grpc.RegisterResult;
import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterServiceGrpc;
import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterServiceGrpc.RxDiameterServiceStub;
import com.ericsson.sc.diameter.avp.Avps;
import com.ericsson.sc.diameter.avp.MessageParser;

import io.grpc.ManagedChannel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public final class DiaGrpcClient
{
    private static final Logger log = LoggerFactory.getLogger(DiaGrpcClient.class);
    private final AaaService aaaService;
    private final ManagedChannel channel;
    private final RxDiameterServiceStub stub;
    private final long keepAlivePeriodMillis;
    private final Subject<DiaGrpcClientContext> subject;
    private final Flowable<Context> run;
    private final MessageParser messageParser;

    /**
     * Provides access to the Diameter Service API Handles registration, health
     * checks, and event polling Constructs the diameter peer table
     * 
     * @param aaaService
     * @param keepAlivePeriodMillis Health check period
     */
    public DiaGrpcClient(AaaService aaaService,
                         long keepAlivePeriodMillis,
                         ManagedChannel channel)
    {
        this.aaaService = aaaService;
        this.keepAlivePeriodMillis = keepAlivePeriodMillis;
        this.channel = channel;
        this.subject = BehaviorSubject.<DiaGrpcClientContext>create().toSerialized();
        this.stub = RxDiameterServiceGrpc.newRxStub(channel);
        this.messageParser = Avps.messageParser();

        this.run = register() //
                             .flatMapPublisher(this::doAfterRegister)
                             .doOnNext(this.subject::onNext)
                             .doOnError(error -> log.warn("Disconnected from Diameter gRPC ADP service", error))
                             .doOnComplete(() ->
                             {
                                 throw new IllegalStateException("Unexpected gRPC client termination");
                             })
                             .replay(1)
                             .refCount();
    }

    /**
     * Start registration, healthcheck and polling. Only one subscriber should
     * subsribe to the returned flowable. The flowable does not complete if there
     * are no errors Users should call shutdown() to cleanup connections after
     * flowable disposal.
     * 
     * @return
     */
    public Flowable<Context> run()
    {
        return this.run;
    }

    /**
     * Gracefully shutdown all connection towards the diameter stack
     * 
     * @return
     */
    public Completable shutdown()
    {
        return Completable.fromAction(() ->
        {
            log.info("Shuting down Diameter gRPC channel");
            if (!this.channel.isTerminated())
            {
                this.channel.shutdown();

                try
                {
                    this.channel.awaitTermination(10, TimeUnit.SECONDS);
                }
                catch (InterruptedException e)
                {
                    log.warn("Gracefull Diameter gRPC channel termination timed out", e);
                    this.channel.shutdownNow();
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                    try
                    {
                        this.channel.awaitTermination(5, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException e1)
                    {
                        log.warn("Forced Diameter gRPC channel termination timed out", e);
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<DiaGrpcClientContext> getContext()
    {
        return this.subject;
    }

    private Completable keepalive(Long serviceId)
    {
        final var args = KeepaliveArguments.newBuilder().setServiceId(serviceId).build();
        return Completable.defer(() -> this.stub.keepalive(args).ignoreElement());
    }

    private Single<Long> register()
    {
        final var registerArgs = RegisterArguments.newBuilder()
                                                  .setServiceName(aaaService.getServiceName())
                                                  .setClientHostname(aaaService.getClientHostName())
                                                  .setClientPort(aaaService.getClientPort())
                                                  .build();

        return Single.defer(() -> this.stub.register(registerArgs))
                     .map(RegisterResult::getServiceId)
                     .doOnSuccess(id -> log.info("Registered AAA Service {} serviceId {}", this.aaaService, id));
    }

    private Flowable<IncomingEvent> pollEvents(long serviceId)
    {
        return Flowable.defer(() -> this.stub.pollEvents(PollArguments.newBuilder().setServiceId(serviceId).build()));

    }

    private Flowable<Context> doAfterRegister(long serviceId)
    {
        final var keepAliveCheck = Flowable.interval(keepAlivePeriodMillis, TimeUnit.MILLISECONDS)
                                           .onBackpressureDrop(tick -> log.warn("Skipped keepalive call due to backpressure"))
                                           .doOnError(err -> log.error("Keep alive check failed, diameter proxy gRPC service is dead, serviceId: {}",
                                                                       serviceId,
                                                                       err))
                                           .flatMapCompletable(tick -> keepalive(serviceId), false, 10);
        final var poller = pollEvents(serviceId).doOnNext(event -> log.info("Received PollEvent message {}", event))
                                                .doOnError(err -> log.warn("Event polling stopped due to error", err))
                                                .doOnComplete(() -> log.warn("Diameter proxy gRPC terminated event polling, will restart polling after 500ms"))
                                                .repeatWhen(complete -> complete.delay(500, TimeUnit.MILLISECONDS)) // Retry after 500ms upon graceful stream
                                                                                                                    // termination
                                                .scan(new Context(serviceId), this::processIncomingEvent)
                                                .switchMapSingle(Single::just); // Use of switchMap helps in memory handling.

        return keepAliveCheck.<Context>toFlowable().mergeWith(poller);
    }

    private Context processIncomingEvent(Context oldState,
                                         IncomingEvent pollEvent)
    {
        try
        {
            switch (pollEvent.getEventCase())
            {
                case PEERUPEVENT:
                    return oldState.updatePeerTable(peerUpEvent(oldState.peerTable, pollEvent.getPeerUpEvent()));
                case PEERDOWNEVENT:
                    return oldState.updatePeerTable(peerDownEvent(oldState.peerTable, pollEvent.getPeerDownEvent()));
                case INITIALSYNCDONE:
                    log.info("Received complete peertable from Diameter gRPC proxy");
                    return oldState.updateInitialSync(true);
                default:
                    log.warn("Ignored unknown PollEvent message {}", pollEvent);
                    return oldState;
            }
        }
        catch (Exception e)
        {
            // Log any message processing errors and ignore
            log.error("Failed to process PollEvent message {}", pollEvent, e);
            return oldState;
        }
    }

    private PeerTable peerUpEvent(PeerTable oldPeerTable,
                                  PeerUpEvent upEvent)
    {
        final var dm = messageParser.parse(upEvent.getCapabilityExchangeMessage());
        return oldPeerTable.addPeer(new DiameterPeer(upEvent.getPeerId(), dm.getOriginHost(), dm.getOriginRealm()));
    }

    private PeerTable peerDownEvent(PeerTable oldPeerTable,
                                    PeerDownEvent downEvent)
    {
        return oldPeerTable.removePeer(downEvent.getPeerId());
    }

    /**
     * Provides access to the client gRPC API Contains the latest snapshot of the
     * peer table
     */
    public class Context implements DiaGrpcClientContext
    {
        private final long serviceId;
        private final PeerTable peerTable;
        private final boolean initialSyncDone;

        /*
         * (non-Javadoc)
         * 
         * @see com.ericsson.sc.diameter.DiaGrpcClientContext#getServiceId()
         */
        @Override
        public long getServiceId()
        {
            return serviceId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ericsson.sc.diameter.DiaGrpcClientContext#getPeerTable()
         */
        @Override
        public PeerTable getPeerTable()
        {
            return peerTable;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ericsson.sc.diameter.DiaGrpcClientContext#isInitialSyncDone()
         */
        @Override
        public boolean isInitialSyncDone()
        {
            return this.initialSyncDone;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.ericsson.sc.diameter.DiaGrpcClientContext#sendRequest(com.ericsson.gs.tm.
         * diameter.service.grpc.OutgoingRequest)
         */
        @Override
        public Single<OutgoingRequestResult> sendRequest(OutgoingRequest rxRequest)
        {
            return DiaGrpcClient.this.stub.sendRequest(rxRequest);
        }

        /**
         * Create an empty grpcClientContext, without peerTable
         * 
         * @param serviceId The serviceId received from diametr proxy gRPC
         */
        private Context(long serviceId)
        {
            this.serviceId = serviceId;
            this.peerTable = new PeerTable();
            this.initialSyncDone = false;
        }

        /**
         * Create a new grpcClientContext
         * 
         * @param serviceId       The serviceId received from diametr proxy gRPC
         * @param peerTable       The peerTable
         * @param initialSyncDone The initial sync status
         */
        protected Context(long serviceId,
                          PeerTable peerTable,
                          boolean initialSyncDone)
        {
            this.serviceId = serviceId;
            this.peerTable = peerTable;
            this.initialSyncDone = initialSyncDone;
        }

        /**
         * Update the peer table
         * 
         * @param The new peer table
         * @return a new Context with updated peer table
         */
        private Context updatePeerTable(PeerTable newPeerTable)
        {
            return new Context(this.serviceId, newPeerTable, this.initialSyncDone);
        }

        /**
         * Update the initial sync status
         * 
         * @param initialSyncDone The new initial sync status
         * @return a new Context with updated sync status
         */
        private Context updateInitialSync(boolean initialSyncDone)
        {
            return new Context(this.serviceId, this.peerTable, initialSyncDone);
        }

        @Override
        public String toString()
        {
            var builder = new StringBuilder();
            builder.append("Context [serviceId=");
            builder.append(serviceId);
            builder.append(", peerTable=");
            builder.append(peerTable);
            builder.append(", initialSyncDone=");
            builder.append(initialSyncDone);
            builder.append("]");
            return builder.toString();
        }

    }

}
