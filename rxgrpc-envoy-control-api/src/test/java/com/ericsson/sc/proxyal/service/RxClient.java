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
 * Created on: Apr 10, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.proxyal.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.service.CdsHelper;
import com.salesforce.rxgrpc.GrpcRetry;

//import io.envoyproxy.envoy.api.v2.DiscoveryRequest;
//import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
//import io.envoyproxy.envoy.api.v2.core.Node;
import io.envoyproxy.envoy.config.core.v3.Node;
//import io.envoyproxy.envoy.service.discovery.v2.RxAggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.RxAggregatedDiscoveryServiceGrpc;
//import io.envoyproxy.envoy.service.discovery.v2.RxAggregatedDiscoveryServiceGrpc.RxAggregatedDiscoveryServiceStub;
import io.envoyproxy.envoy.service.discovery.v3.RxAggregatedDiscoveryServiceGrpc.RxAggregatedDiscoveryServiceStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Envoy test client.
 */
public class RxClient
{
    private static final Logger log = LoggerFactory.getLogger(RxClient.class);

    private static class Logic
    {
        private DiscoveryResponse lastReceivedResponse;
        private AtomicLong requestCount;
        private String name;

        public Logic()
        {
            this.lastReceivedResponse = null;
            this.requestCount = new AtomicLong();
        }

        public DiscoveryRequest createDiscoveryRequest()
        {
            final DiscoveryRequest request = DiscoveryRequest.newBuilder()
//          .setNode(Node.newBuilder().setId("envoy-1").build())
                                                             .setResponseNonce(this.lastReceivedResponse == null ? "" : this.lastReceivedResponse.getNonce())
                                                             .setVersionInfo(this.lastReceivedResponse == null ? ""
                                                                                                               : this.lastReceivedResponse.getVersionInfo())
                                                             .setTypeUrl(CdsHelper.TYPE_URL)
                                                             .setNode(Node.newBuilder().setId(String.valueOf(this.requestCount.incrementAndGet())))
                                                             .build();

            log.info("Created request:\n{}", request);

            return request;
        }

        public void processDiscoveryResponse(DiscoveryResponse response)
        {
            log.info("{}, Processing response:\n{}", name, response);
            this.lastReceivedResponse = response;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    private final Logic logic = new Logic();

    private ManagedChannel channel = null;
    private RxAggregatedDiscoveryServiceStub stub = null;
    private BehaviorSubject<DiscoveryRequest> requests = BehaviorSubject.createDefault(this.logic.createDiscoveryRequest());

    private String serverAddress = "localhost";
    private int serverPort = 8888;

    private String name;

    public RxClient()
    {
        this("default_client");
    }

    public RxClient(String name)
    {
        this.name = name;
        this.logic.setName(name);
    }

    public RxClient(String aServerAddress,
                    int aServerPort)
    {
        serverAddress = aServerAddress;
        serverPort = aServerPort;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            this.channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
            this.stub = RxAggregatedDiscoveryServiceGrpc.newRxStub(this.channel);
        })//
                          .doOnSubscribe(__ -> log.info("Starting."))
                          .doOnComplete(() -> log.info("Started."));
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.channel.shutdown()).doOnSubscribe(__ -> log.info("Stopping.")).doOnComplete(() -> log.info("Stopped."));
    }

    public Flowable<DiscoveryResponse> update()
    {
        Flowable<DiscoveryResponse> flowable = this.requests.toFlowable(BackpressureStrategy.LATEST)
                                                            .compose(GrpcRetry.ManyToMany.retryAfter(this.stub::streamAggregatedResources,
                                                                                                     100,
                                                                                                     TimeUnit.MILLISECONDS));

        return flowable.doOnSubscribe(__ -> log.info("{}, Sending request", name))//
                       .doOnNext(resp -> this.logic.processDiscoveryResponse(resp))
                       .doOnNext(resp -> this.requests.toSerialized().onNext(this.logic.createDiscoveryRequest()))
//                       .doAfterNext(resp -> this.requests.toSerialized().onNext(this.logic.createDiscoveryRequest()))
                       .doOnCancel(() -> log.info("Cancelling."))
                       .doOnComplete(() -> log.info("Complete"))
                       .doOnError(e -> log.error("Error sending request:{}", e.toString()));
    }

}
