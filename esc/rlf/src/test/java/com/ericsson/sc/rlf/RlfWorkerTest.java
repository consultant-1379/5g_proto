package com.ericsson.sc.rlf;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nrlf.ratelimiting.PullTokensContext;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensRequest;
import com.ericsson.sc.ratelimiting.service.grpc.RxRateLimitingServiceGrpc;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.metrics.SampleStatistics;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.web.client.WebClient;

class RlfWorkerTest
{
//    private static class TestRlfCommunication
//    {
    private static final int TEST_RUN_TIMEOUT_SECS = 3600 * 1;
    private static final String RLF_IP = "192.168.136.249";

    final WebClientOptions options = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2)
                                                           .setHttp2ClearTextUpgrade(false)
                                                           .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3"));
//                                                           .setHttp2MultiplexingLimit(10);

    final WebClient client = WebClient.create(VertxInstance.get(), options);

    private static PullTokensContext createPullTokensRequest()
    {
        return new PullTokensContext().name("ingress=GRLname.own=internalNetwork").watermark(99.0).amount(1);
    }

    private static PullTokensRequest createPullTokensRequestGrpc()
    {
        return PullTokensRequest.newBuilder()
                                .setNamespace("scp")
                                .addContexts(com.ericsson.sc.ratelimiting.service.grpc.PullTokensContext.newBuilder()
                                                                                                        .setName("ingress=GRLname.own=internalNetwork")
                                                                                                        .setWatermark(99.0)
                                                                                                        .setAmount(1))
                                .build();
    }

    public Completable testRest()
    {
        return Completable.defer(() ->
        {
            final Url url = new Url(RLF_IP, 80, "/nrlf-ratelimiting/v0/tokens/scp");

            final SampleStatistics lm = SampleStatistics.of("RTT RLF (REST)");

            return Flowable.interval(1, TimeUnit.MILLISECONDS)
                           .onBackpressureBuffer()
                           .flatMapSingle(n -> Single.fromCallable(() -> java.time.Instant.now())
                                                     .zipWith(this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                                                         .rxSendJson(new JsonArray().add(createPullTokensRequest()))
                                                                         .subscribeOn(Schedulers.io())
//                                                   .doOnSuccess(resp -> log.info("url={}, resp={}", url, resp.bodyAsString()))
                                                                         .doOnError(err -> log.error("Error sending HTTP request to {}: {}", url, err)),
                                                              Pair::of)
//                                                       .doOnSuccess(p ->
//                                                       {
//                                                           if ((n % 1000) == 0)
//                                                               SampleStatistics.print();
//                                                       })
                                                     .doOnSuccess(p -> lm.add(p.getFirst()))
                                                     .map(p -> p.getSecond()))
                           .lastOrError()
                           .timeout(TEST_RUN_TIMEOUT_SECS, TimeUnit.SECONDS)
                           .ignoreElement()
                           .onErrorComplete()
                           .doFinally(() -> log.info("latencyRest={}", lm));
        }).subscribeOn(Schedulers.io());
    }

    public Completable testGrpc()
    {
        return Completable.defer(() ->
        {
            final ManagedChannel channel = NettyChannelBuilder.forAddress(RLF_IP, 4711)
                                                              .usePlaintext() // Do not use TLS (which is default)
                                                              .directExecutor()
                                                              .build();

            final RxRateLimitingServiceGrpc.RxRateLimitingServiceStub stub = RxRateLimitingServiceGrpc.newRxStub(channel);

            final SampleStatistics lm = SampleStatistics.of("RTT RLF (gRPC)");

            return Flowable.interval(1, TimeUnit.MILLISECONDS)
                           .onBackpressureBuffer()
                           .flatMapSingle(i -> Single.fromCallable(() -> Instant.now())//
                                                     .subscribeOn(Schedulers.io())
                                                     .zipWith(stub.pullTokens(createPullTokensRequestGrpc()), Pair::of)
                                                     .doOnSuccess(p -> lm.add(p.getFirst()))
//                                                         .doOnSuccess(p -> log.info("result={}", p.getSecond()))
                                                     .map(Pair::getSecond)
//                                                         .doOnSuccess(r -> r.getResultList()
//                                                                            .stream()
//                                                                            .forEach(rr -> log.info("{}, {}", rr.getRc().name(), rr.getRa())))//
            )
                           .lastOrError()
                           .timeout(TEST_RUN_TIMEOUT_SECS, TimeUnit.SECONDS)
                           .ignoreElement()
                           .onErrorComplete()
                           .doFinally(() -> log.info("latencyGrpc={}", lm))
                           .andThen(Completable.fromCallable(() -> channel.shutdown().awaitTermination(1, TimeUnit.SECONDS)));
        }).subscribeOn(Schedulers.io());
    }

    public static void main(String[] args)
    {
        final RlfWorkerTest c = new RlfWorkerTest();
//            Completable.ambArray(c.testRest(), c.testRest(), c.testRest(), c.testRest(), c.testRest(), c.testRest(), c.testRest()).blockingAwait();
        Completable.ambArray(c.testRest(), c.testRest()).blockingAwait();
        Completable.ambArray(c.testGrpc(), c.testGrpc()).blockingAwait();
    }
//    }

    private static final Logger log = LoggerFactory.getLogger(RlfWorkerTest.class);

    @Test
    void test_0_General() throws IOException, InterruptedException
    {
    }
}
