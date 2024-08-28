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
package com.ericsson.sc.ratelimiting;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.sc.ratelimiting.service.grpc.PullTokensContext;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensRequest;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensResponse;
import com.ericsson.sc.ratelimiting.service.grpc.RxRateLimitingServiceGrpc;
import com.ericsson.sc.ratelimiting.service.grpc.RxRateLimitingServiceGrpc.RateLimitingServiceImplBase;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.metrics.SampleStatistics;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RateLimitingServerTest
{
    private static final Logger log = LoggerFactory.getLogger(RateLimitingServerTest.class);

    private static PullTokensRequest createPullTokensRequest()
    {
        return PullTokensRequest.newBuilder()
                                .setNamespace("scp")
                                .addContexts(PullTokensContext.newBuilder().setName("ingress=GRLname.own=internalNetwork").setWatermark(99.0).setAmount(1))
                                .build();
    }

//    @Test
    public void rateLimitingTest() throws Exception
    {
        final RateLimitingServiceImplBase rateLimitingService = new RxRateLimitingServiceGrpc.RateLimitingServiceImplBase()
        {
            final RateLimitingService service = new RateLimitingService();

            @Override
            public Single<PullTokensResponse> pullTokens(Single<PullTokensRequest> request)
            {
                final Single<PullTokensRequest> req = request.doOnSuccess(r -> log.info("MOCK received received pullToken {}", r));
                return req.flatMap(r -> this.service.pullTokens(r));
            }
        };

        Server server = InProcessServerBuilder.forName("RateLimitingService").directExecutor().addService(rateLimitingService).build();
        server.start();
        ManagedChannel channel = InProcessChannelBuilder.forName("RateLimitingService").directExecutor().build();

        RxRateLimitingServiceGrpc.RxRateLimitingServiceStub stub = RxRateLimitingServiceGrpc.newRxStub(channel);

        for (int i = 0; i < 10; ++i)
            stub.pullTokens(createPullTokensRequest())
                .doOnSuccess(r -> r.getResultList().stream().forEach(rr -> log.info("{}, {}", rr.getRc().name(), rr.getRa())))
                .blockingGet();

        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
        server.shutdown();
        server.awaitTermination();
    }

//    @Test
    public void rateLimitingTestNetty() throws Exception
    {
//        final RateLimitingServiceImplBase rateLimitingService = new RxRateLimitingServiceGrpc.RateLimitingServiceImplBase()
//        {
//            final RateLimitingService service = new RateLimitingService();
//
//            @Override
//            public Single<PullTokensResponse> pullTokens(Single<PullTokensRequest> request)
//            {
//                final Single<PullTokensRequest> req = request;// .doOnSuccess(r -> log.info("MOCK received received pullToken {}", r));
//                return req.flatMap(r -> this.service.pullTokens(r));
//            }
//        };
//
//        Server server = NettyServerBuilder.forPort(4711).directExecutor().addService(rateLimitingService).build();
//        server.start();
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 4711)
                                                    .usePlaintext() // Do not use TLS (which is default)
                                                    .directExecutor()
                                                    .build();

        RxRateLimitingServiceGrpc.RxRateLimitingServiceStub stub = RxRateLimitingServiceGrpc.newRxStub(channel);

        final SampleStatistics lm = SampleStatistics.of("Processing time of requests in RLF");

        Flowable.interval(1000, TimeUnit.MICROSECONDS)
                .flatMapSingle(i -> Single.just(Instant.now())//
                                          .subscribeOn(Schedulers.io())
                                          .zipWith(stub.pullTokens(createPullTokensRequest()), Pair::of)
//                                          .delay(1, TimeUnit.MILLISECONDS)
                                          .doOnSuccess(p -> lm.add(p.getFirst()))
                                          .map(Pair::getSecond)
//                                          .doOnSuccess(r -> r.getResultList().stream().forEach(rr -> log.info("{}, {}", rr.getRc().name(), rr.getRa())))//
                )
//                .repeat(10000)
                .toList()
                .timeout(600, TimeUnit.SECONDS)
                .doOnError(x -> log.info("latency={}", lm))
                .blockingGet();

        channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
//        server.shutdown().awaitTermination();
    }
}
