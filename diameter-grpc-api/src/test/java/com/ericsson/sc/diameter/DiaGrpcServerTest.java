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

import static com.google.protobuf.ByteString.copyFromUtf8;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvpHeader;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessage;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingEvent;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.InitialSyncDone;
import com.ericsson.gs.tm.diameter.service.grpc.KeepaliveArguments;
import com.ericsson.gs.tm.diameter.service.grpc.KeepaliveResult;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.PeerDownEvent;
import com.ericsson.gs.tm.diameter.service.grpc.PeerUpEvent;
import com.ericsson.gs.tm.diameter.service.grpc.PollArguments;
import com.ericsson.gs.tm.diameter.service.grpc.RegisterArguments;
import com.ericsson.gs.tm.diameter.service.grpc.RegisterResult;
import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterClientGrpc;
import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterServiceGrpc;
import com.ericsson.sc.diameter.avp.Avps;
import com.google.protobuf.ByteString;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;

public class DiaGrpcServerTest
{
    private static final Logger log = LoggerFactory.getLogger(DiaGrpcServerTest.class);
    static final long assignedServiceId = 100;

    @Test
    public void DiaGrpcClientTest() throws Exception
    {
        List<IncomingEvent> pEvents = new ArrayList<>();
        this.pt.peers.values().forEach(v -> pEvents.add(toPollEvent(v)));
        pEvents.add(removeAPeer(pt.peers.get("1")));

        final var mockDiameterServiceImpl = new RxDiameterServiceGrpc.DiameterServiceImplBase()
        {

            @Override
            public Single<RegisterResult> register(Single<RegisterArguments> request)
            {
                var req = request.doOnSuccess(r -> log.info("MOCK received register {}", r));
                return req.flatMap(r -> Single.just(RegisterResult.newBuilder().setServiceId(assignedServiceId).build()))
                          .doOnSuccess(resp -> log.info("MOCK sent register response {}", resp));
            }

            @Override
            public Single<KeepaliveResult> keepalive(Single<KeepaliveArguments> request)
            {
                var req = request.doOnSuccess(r -> log.info("MOCK received received keepalive {}", r));
                return req //
                          .flatMap(id -> id.getServiceId() == assignedServiceId ? Single.just(KeepaliveResult.newBuilder().build())
                                                                                : Single.error(new StatusException(Status.UNAVAILABLE)));
            }

            @Override
            public Single<OutgoingRequestResult> sendRequest(Single<OutgoingRequest> request)
            {
                var req = request.doOnSuccess(r -> log.info("MOCK received sendRequest {}", r));
                return req.map(r -> OutgoingRequestResult.newBuilder().setMessage(r.getMessage()).build());
            }

            @Override
            public Flowable<IncomingEvent> pollEvents(Single<PollArguments> request)
            {
                var req = request.doOnSuccess(r -> log.info("MOCK received received pollEvents {}", r)).toFlowable();

//                return req.concatMap(r -> Flowable.fromIterable(pt.peers.values()) //
//                                                  .map(p -> toPollEvent(p)))
                return req.concatMap(r -> Flowable.fromIterable(pEvents).startWith(initialSyncDone()))
//                          .doOnNext(x -> removeAPeer(pt.peers.get("host1")))
                          .doOnNext(resp -> log.info("Mock diameter proxy sent response {}", resp))
                          .doOnError(err -> log.error("Mock diameter proy failed to send pollEvents response", err))
                          .concatWith(Completable.never());
            }
        };

        var dsServerMock = InProcessServerBuilder.forName("DiameterService").directExecutor().addService(mockDiameterServiceImpl).build();
        dsServerMock.start();
        var dsChannel = InProcessChannelBuilder.forName("DiameterService").directExecutor().build();
        var dcServerBuilder = InProcessServerBuilder.forName("DiameterClient").directExecutor();
        var dcChannelMock = InProcessChannelBuilder.forName("DiameterClient").directExecutor().build();
        try
        {
            var aaaService = new AaaService("bsf.grpc", "localhost", 9090);
            var diaGrpcClient = new DiaGrpcClient(aaaService, 100000, dsChannel);
            var diaGrpc = new DiaGrpc(diaGrpcClient, dcServerBuilder, new DiaGrpcHandler()
            {

                @Override
                public Single<IncomingRequestResult> processRequest(Single<IncomingRequest> request,
                                                                    Observable<DiaGrpcClientContext> ctxObs)
                {
                    return request //
                                  .toObservable()
                                  .withLatestFrom(ctxObs, RequestContext::new)
                                  .flatMapSingle(ctx ->
                                  {
                                      final var response = OutgoingRequest.newBuilder() //
                                                                          .setServiceId(ctx.diaContext.getServiceId())
                                                                          .setMessage(ctx.incomingRequest.getMessage())
                                                                          .build();
                                      return ctx.diaContext.sendRequest(response) //
                                                           .map(ans -> IncomingRequestResult.newBuilder() //
                                                                                            .setMessage(ans.getMessage())
                                                                                            .build());
                                  })
                                  .singleOrError();
                }

            });

            final var stop = CompletableSubject.create();
            var testSub = diaGrpc.run(stop) //
                                 .map(ctx -> ctx.getGrpcClientContext().getPeerTable())
                                 .test()
                                 .awaitCount(pt.peers.size() + 1)
                                 .assertValueAt(pt.peers.size(), assertPeerTable);

            var dcStubMock = RxDiameterClientGrpc.newRxStub(dcChannelMock);

            // Simulate incoming Diameter message
            final var peerHostId = "testHost";
            final var peerId = "peerId," + peerHostId;
            final var msg = cer(copyFromUtf8(peerHostId), copyFromUtf8("testRealm"));
            var resp = dcStubMock.processRequest(IncomingRequest.newBuilder() //
                                                                .setMessage(msg) //
                                                                .setPeerId(peerId)
                                                                .build()) //
                                 .blockingGet();
            assertEquals(resp.getMessage(), msg);
            log.info("Shutting down");

            stop.onComplete(); // indicate termination
            testSub.await();
            testSub.assertComplete();
            testSub.assertNoErrors();
            testSub.dispose();

            diaGrpcClient.shutdown().blockingAwait();
        }
        finally
        {
            // Thread.sleep(2000);
            dsChannel.shutdown();
            dsChannel.awaitTermination(10, TimeUnit.SECONDS);
            dsServerMock.shutdown();
            dsServerMock.awaitTermination();
            log.info("Finished");
        }
    }

    PeerTable pt = peerTable(Arrays.asList("0", "1", "2", "3"));
    PeerTable assertPeerTable = peerTable(Arrays.asList("0", "2", "3"));

    private static IncomingEvent peerUpEvent(String peerId,
                                             DiameterMessage msg)
    {
        return IncomingEvent.newBuilder()
                            .setPeerUpEvent(PeerUpEvent.newBuilder()
                                                       .setCapabilityExchangeMessage(msg)

                                                       .setPeerId(peerId))
                            .build();
    }

    public static IncomingEvent initialSyncDone()
    {
        return IncomingEvent.newBuilder().setInitialSyncDone(InitialSyncDone.newBuilder()).build();
    }

    private static IncomingEvent peerDownEvent(String peerId)
    {
        return IncomingEvent.newBuilder().setPeerDownEvent(PeerDownEvent.newBuilder().setPeerId(peerId)).build();
    }

    private static DiameterMessage cer(ByteString originHost,
                                       ByteString originRealm)
    {
        var ohAvp = DiameterAvp.newBuilder()
                               .setHeader(DiameterAvpHeader.newBuilder().setCode(Avps.ORIGIN_HOST.getId().getAvpCode()))
                               .setDiameterIdentity(originHost);
        var orAvp = DiameterAvp.newBuilder()
                               .setHeader(DiameterAvpHeader.newBuilder().setCode(Avps.ORIGIN_REALM.getId().getAvpCode()))
                               .setDiameterIdentity(originRealm);
        return DiameterMessage.newBuilder() //
                              .addAvps(ohAvp)
                              .addAvps(orAvp)
                              .build();
    }

    private static PeerTable peerTable(Iterable<String> ids)
    {
        var pt = new PeerTable();
        for (var id : ids)
            pt.addPeer(new DiameterPeer(id, "host" + id, "realm" + id));
        return pt;
    }

    private static IncomingEvent toPollEvent(DiameterPeer dp)
    {
        return peerUpEvent(dp.getPeerId(), cer(copyFromUtf8(dp.getHostIdentity()), copyFromUtf8(dp.getRealm())));
    }

    private static IncomingEvent removeAPeer(DiameterPeer dp)
    {
        log.info("PeerDown Event will be triggered");
        return peerDownEvent(dp.getPeerId());
    }
}
