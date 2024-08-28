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
 * Created on: Apr 16, 2020
 *     Author: eedstl
 */

package com.ericsson.sc.rlf;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.sc.pm.ScPmbrConfigHandler;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketConfig;
import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketState;
import com.ericsson.cnal.internal.nrlf.ratelimiting.PullTokensContext;
import com.ericsson.cnal.internal.nrlf.ratelimiting.PullTokensResult;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxLeaderElection;
import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.PatchUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class RlfWorker
{
    public static class RlfRateLimiting
    {
        public static class Statistics
        {
            private static final String LN_BUCKET = "bucket";
            private static final String LN_NAMESPACE = "namespace";
            private static final String LN_OPERATION = "operation";
            private static final String LN_STATUS = "status";
            private static final String LN_WATERMARK = "watermark";

            private static final Statistics singleton = new Statistics();

            private static Statistics singleton()
            {
                return singleton;
            }

            private final io.prometheus.client.Counter ccInReqRateLim = MetricRegister.singleton()
                                                                                      .register(io.prometheus.client.Counter.build()
                                                                                                                            .namespace("rlf")
                                                                                                                            .name("in_requests_nrlf_ratelimiting_total")
                                                                                                                            .labelNames(LN_NAMESPACE,
                                                                                                                                        LN_OPERATION)
                                                                                                                            .help("Number of incoming requests on the internal nrlf_ratelimiting interface")
                                                                                                                            .register());

            private final io.prometheus.client.Counter ccOutAnsRateLim = MetricRegister.singleton()
                                                                                       .register(io.prometheus.client.Counter.build()
                                                                                                                             .namespace("rlf")
                                                                                                                             .name("out_answers_nrlf_ratelimiting_total")
                                                                                                                             .labelNames(LN_NAMESPACE,
                                                                                                                                         LN_OPERATION,
                                                                                                                                         LN_STATUS)
                                                                                                                             .help("Number of outgoing answers on the internal nrlf_ratelimiting interface")
                                                                                                                             .register());

            private final io.prometheus.client.Counter ccInReqOam = MetricRegister.singleton()
                                                                                  .register(io.prometheus.client.Counter.build()
                                                                                                                        .namespace("rlf")
                                                                                                                        .name("in_requests_nrlf_oam_total")
                                                                                                                        .labelNames(LN_NAMESPACE, LN_OPERATION)
                                                                                                                        .help("Number of incoming requests on the internal nrlf_oam interface")
                                                                                                                        .register());

            private final io.prometheus.client.Counter ccOutAnsOam = MetricRegister.singleton()
                                                                                   .register(io.prometheus.client.Counter.build()
                                                                                                                         .namespace("rlf")
                                                                                                                         .name("out_answers_nrlf_oam_total")
                                                                                                                         .labelNames(LN_NAMESPACE,
                                                                                                                                     LN_OPERATION,
                                                                                                                                     LN_STATUS)
                                                                                                                         .help("Number of outgoing answers on the internal nrlf_oam interface")
                                                                                                                         .register());

            private final io.prometheus.client.Counter ccTbPulls = MetricRegister.singleton()
                                                                                 .register(io.prometheus.client.Counter.build()
                                                                                                                       .namespace("rlf")
                                                                                                                       .name("tb_pulls_total")
                                                                                                                       .labelNames(LN_NAMESPACE,
                                                                                                                                   LN_BUCKET,
                                                                                                                                   LN_WATERMARK,
                                                                                                                                   LN_STATUS)
                                                                                                                       .help("Number of pulls per token bucket and watermark")
                                                                                                                       .register());

            private final io.prometheus.client.Gauge gcTbFillGrade = MetricRegister.singleton()
                                                                                   .register(io.prometheus.client.Gauge.build()
                                                                                                                       .namespace("rlf")
                                                                                                                       .name("tb_fill_grade_percents")
                                                                                                                       .labelNames(LN_NAMESPACE, LN_BUCKET)
                                                                                                                       .help("Token bucket fill grade [%]")
                                                                                                                       .register());

            private Statistics()
            {
            }

            public Statistics stepCcInReqRateLim(final String namespace,
                                                 final String operation)
            {
                this.ccInReqRateLim.labels(namespace, operation).inc();
                return this;
            }

            public Statistics stepCcOutAnsRateLim(final String namespace,
                                                  final String operation,
                                                  final int status)
            {
                this.ccOutAnsRateLim.labels(namespace, operation, Integer.toString(status)).inc();
                return this;
            }

            public Statistics stepCcInReqOam(final String namespace,
                                             final String operation)
            {
                this.ccInReqOam.labels(namespace, operation).inc();
                return this;
            }

            public Statistics stepCcOutAnsOam(final String namespace,
                                              final String operation,
                                              final int status)
            {
                this.ccOutAnsOam.labels(namespace, operation, Integer.toString(status)).inc();
                return this;
            }

            public Statistics stepCcTbPulls(final String namespace,
                                            final String bucket,
                                            final double watermark,
                                            final int status)
            {
                this.ccTbPulls.labels(namespace, bucket, Double.toString(watermark), Integer.toString(status)).inc();
                return this;
            }

            public Statistics stepGcTbFillGrade(final String namespace,
                                                final String bucket,
                                                final double fillGrade)
            {
                this.gcTbFillGrade.labels(namespace, bucket).set(fillGrade);
                return this;
            }
        }

        @JsonPropertyOrder({ "config", "fillGrade", "tokens", "statistics" })
        public static class TokenBucket
        {
            @JsonPropertyOrder({ "pulls", "underflows", "pullSuccessRatio", "pullRate", "underflowRate" })
            private static class Statistics
            {
                private static double round(double number)
                {
                    return Math.round(10d * number) / 10d;
                }

                @JsonIgnore
                public final AtomicLong numPulls = new AtomicLong();

                @JsonIgnore
                public final AtomicLong numUnderflows = new AtomicLong();

                @JsonProperty("pulls")
                private final AtomicLong numPullsTotal = new AtomicLong();

                @JsonProperty("underflows")
                private final AtomicLong numUnderflowsTotal = new AtomicLong();

                @JsonProperty("pullSuccessRatio")
                private final AtomicDouble pullSuccessRatio = new AtomicDouble(); // [%]

                @JsonProperty("pullRate")
                private final AtomicDouble pullRate = new AtomicDouble(); // [Hz]

                @JsonProperty("underflowRate")
                private final AtomicDouble underflowRate = new AtomicDouble(); // [Hz]

                @JsonIgnore
                private final AtomicLong lastPoll = new AtomicLong(System.currentTimeMillis());

                @JsonIgnore
                public Statistics sample()
                {
                    final long now = System.currentTimeMillis();
                    final long dt = Math.max(1, now - this.lastPoll.getAndSet(now));

                    final long numPulls = this.numPulls.getAndSet(0);
                    this.numPullsTotal.set(numPulls);
                    final long numUnderflows = this.numUnderflows.getAndSet(0);
                    this.numUnderflowsTotal.set(numUnderflows);

                    this.pullSuccessRatio.set(round(100 * (1d - 1d * numUnderflows / Math.max(1, numPulls))));
                    this.pullRate.set(round(numPulls * 1000d / dt));
                    this.underflowRate.set(round(numUnderflows * 1000d / dt));

                    return this;
                }
            }

            public static TokenBucket of(final BucketConfigWithId config)
            {
                return new TokenBucket(config);
            }

            @JsonProperty("config")
            private final BucketConfigWithId config;

            @JsonProperty("tokens")
            private double tokens;

            @JsonIgnore
            private Instant lastPull;

            @JsonIgnore
            private Statistics statistics;

            @JsonIgnore
            private Object lock = new Object();

            private TokenBucket(final BucketConfigWithId config)
            {
                this.config = config;
                this.tokens = this.config.getCapacity();
                this.lastPull = Instant.MIN;
                this.statistics = new Statistics();
            }

            public BucketConfigWithId getConfig()
            {
                return this.config;
            }

            /**
             * At first refill the bucket with the amount of tokens since the last pull.
             * Then calculate the fill grade and return it.
             * 
             * @return The fill grade [%] of the bucket.
             */
            @JsonProperty("fillGrade")
            public double getFillGrade()
            {
                final double fillRate = this.config.getFillRate() > 0 ? this.config.getFillRate() : 1;
                final double capacity = this.config.getCapacity() > 0 ? this.config.getCapacity() : 1;

                synchronized (this.lock)
                {
                    double tokens = Math.min(this.tokens, capacity);

                    final Instant now = Instant.now();
                    final Duration duration = Duration.between(this.lastPull, Instant.now());
                    final double timeSpan = duration.getSeconds() + duration.getNano() / 1e9d; // [s]

                    tokens = Math.min(tokens + fillRate * timeSpan, capacity); // Can be < 0

                    final double fillGrade = 100 * tokens / capacity; // [%]

                    if (fillGrade >= 0)
                    {
                        this.tokens = tokens;
                        this.lastPull = now;

                        return Math.round(10d * fillGrade) / 10d;
                    }

                    return 0;
                }
            }

            @JsonProperty("statistics")
            public Statistics getStatistics()
            {
                return this.statistics.sample();
            }

            /**
             * Pull given amount tokens if the bucket's fill grade is not smaller than the
             * given watermark [%] and return the result of the operation.
             * 
             * @param watermark [%] The watermark below which the bucket is considered
             *                  empty.
             * @param amount    The amount of tokens to be pulled.
             * @return PullTokensResult
             */
            public PullTokensResult pull(final double inWatermark,
                                         final int inAmount)
            {
                this.statistics.numPulls.incrementAndGet();

                final double watermark = Math.max(0, inWatermark);
                final int amount = Math.max(0, inAmount);
                final double fillRate = this.config.getFillRate() > 0 ? this.config.getFillRate() : 1;
                final double capacity = this.config.getCapacity() > 0 ? this.config.getCapacity() : 1;

                synchronized (this.lock)
                {
                    final Instant lastPull = this.lastPull;
                    double tokens = Math.min(this.tokens, capacity);

                    final Instant now = Instant.now();
                    final Duration duration = Duration.between(lastPull, Instant.now());
                    final double timeSpan = duration.getSeconds() + duration.getNano() / 1e9d; // [s]

                    tokens = Math.min(tokens + fillRate * timeSpan, capacity) - amount; // Can be < 0

                    final double fillGrade = 100 * tokens / capacity; // [%]
//                        log.info("lastPull={}, tokens={}, fillGrade={}", lastPull, tokens, fillGrade);

                    if (watermark > fillGrade)
                    {
                        this.statistics.numUnderflows.incrementAndGet();

                        // retryAfter [ms] is the time needed to refill the missing tokens up to the
                        // amount requested. For example, there are 3 tokens in the bucket, requested
                        // are 4. Then retryAfter is the time to fill the gap of 1 token.
                        final int ra = (int) Math.round(10 * (watermark - fillGrade) * capacity / fillRate); // [ms]
                        return new PullTokensResult().rc(PullTokensResult.RcEnum.TOO_MANY_REQUESTS).ra(ra);
                    }

                    this.tokens = tokens;
                    this.lastPull = now;

//                        log.info("{}", this);

                    return new PullTokensResult().rc(PullTokensResult.RcEnum.OK);
                }
            }

            /**
             * @param fillGrade The new fill grade [%] of the bucket.
             */
            @JsonProperty("fillGrade")
            public void setFillGrade(double fillGrade)
            {
                synchronized (this.lock)
                {
                    this.tokens = this.config.getCapacity() * fillGrade / 100d;
                }
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

// EEDSTL: gRPC
//        public static class GrpcService
//        {
        // private Map<String, AtomicReference<Map<String, TokenBucket>>> buckets;
        // private final RedisClient redisClient;
        // private final Statistics.Pool statistics;
        // private final SampleStatistics lm = SampleStatistics.of("Processing time of
        // requests in RLF (gRPC)");
        //
        // public GrpcService(final Map<String, AtomicReference<Map<String,
        // TokenBucket>>> buckets,
        // final RedisClient redisClient,
        // final Statistics.Pool statistics)
        // {
        // this.buckets = buckets;
        // this.redisClient = redisClient;
        // this.statistics = statistics;
        // }
        //
        // public Single<PullTokensResponse> pullTokens(final PullTokensRequest request)
        // {
        //// log.info("pullTokens");
        //
        // final Statistics cntPullTokens = this.statistics.get(new
        // StringBuilder(request.getNamespace()).append(":").append("PullTokens").toString());
        //
        // return Single.defer(() -> Single.just(Instant.now())
        // .zipWith(Flowable.fromIterable(request.getContextsList())
        // .concatMapSingle(ctx ->
        // Optional.ofNullable(this.buckets.computeIfAbsent(request.getNamespace(),
        // v -> new AtomicReference<>(new ConcurrentHashMap<>()))
        // .get()
        // .get(ctx.getName()))
        // .map(bucket -> bucket.pullGrpc(this.redisClient,
        // request.getNamespace(),
        // ctx.getWatermark(),
        // ctx.getAmount()))
        // .orElse(Single.just(com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult.newBuilder()
        // .setRc(com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult.ResultCode.NotFound)
        // .build()))
        // .doOnError(e ->
        // {
        // if (logThrottler.loggingIsDue(log.isDebugEnabled())
        // && !(e instanceof NoSuchElementException))
        // {
        // log.error("handlePullTokens: error pulling tokens: {}",
        // e.toString());
        // }
        // })
        // .onErrorReturnItem(com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult.newBuilder()
        // .setRc(com.ericsson.sc.ratelimiting.service.grpc.PullTokensResult.ResultCode.InternalServerError)
        // .build()))
        // .reduce(PullTokensResponse.newBuilder(),
        // (r,
        // v) -> r.addResult(v))
        // .map(b -> b.build()),
        // Pair::of)
        // .doOnSuccess(p -> this.lm.add(p.getFirst()))
        // .map(Pair::getSecond)
        // .doOnSubscribe(x ->
        // {
        // log.debug("Received PullTokens request.");
        // cntPullTokens.getCountInHttpRequests().inc();
        //// this.owner.getNfInstance(null).getHistoryOfEvents().put(event);
        // })
        // .doOnSuccess(r -> log.debug("result={}", r.getResult(0).getRc()))
        // .doOnSuccess(r ->
        // cntPullTokens.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.OK.code()).inc())
        // .doOnError(e -> cntPullTokens.getCountOutHttpResponsesPerStatus()
        // .get(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        // .inc())
        // .doOnError(e -> log.error("{}", e)));
        // }
        // }

        @JsonPropertyOrder({ BucketConfig.JSON_PROPERTY_NAME, "id", BucketConfig.JSON_PROPERTY_CAPACITY, BucketConfig.JSON_PROPERTY_FILL_RATE })
        private static class BucketConfigWithId extends BucketConfig
        {
            @JsonProperty("id")
            public int getId()
            {
                return this.getName().hashCode();
            }
        }

        private static class CommandBuckets extends MonitorAdapter.CommandBase
        {
            private Map<String, AtomicReference<Map<Integer, TokenBucket>>> buckets;

            public CommandBuckets(final Map<String, AtomicReference<Map<Integer, TokenBucket>>> buckets)
            {
                super("buckets", "Usage: command=buckets");
                this.buckets = buckets;
            }

            @Override
            public HttpResponseStatus execute(final Result result,
                                              final Command request)
            {
                result.setAdditionalProperty("buckets",
                                             this.buckets.entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Entry::getKey,
                                                                                   e -> Optional.ofNullable(e.getValue().get())
                                                                                                .orElse(Map.of())
                                                                                                .values()
                                                                                                .stream()
                                                                                                .sorted((l,
                                                                                                         r) -> l.getConfig()
                                                                                                                .getName()
                                                                                                                .compareTo(r.getConfig().getName()))
                                                                                                .collect(Collectors.toList()))));

                return HttpResponseStatus.OK;
            }
        }

        private static class CommandConfig extends MonitorAdapter.CommandBase
        {
            private Map<String, AtomicReference<Map<Integer, TokenBucket>>> buckets;

            public CommandConfig(final Map<String, AtomicReference<Map<Integer, TokenBucket>>> buckets)
            {
                super("config", "Usage: command=config");
                this.buckets = buckets;
            }

            @Override
            public HttpResponseStatus execute(final Result result,
                                              final Command request)
            {
                result.setAdditionalProperty("config",
                                             this.buckets.entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Entry::getKey,
                                                                                   e -> Optional.ofNullable(e.getValue().get())
                                                                                                .orElse(Map.of())
                                                                                                .values()
                                                                                                .stream()
                                                                                                .map(TokenBucket::getConfig)
                                                                                                .sorted((l,
                                                                                                         r) -> l.getName().compareTo(r.getName()))
                                                                                                .collect(Collectors.toList()))));

                return HttpResponseStatus.OK;
            }
        }

        private static final String PAR_NAMESPACE = "namespace";
        private static final String OP_GET_BUCKETS_STATE = "GetBucketsState";
        private static final String OP_SET_BUCKETS_STATE = "SetBucketsState";
        private static final String OP_UPDATE_BUCKETS = "UpdateBuckets";

        private static final String OP_PULL_TOKENS = "PullTokens";

        private static final RlfRateLimiting singleton = new RlfRateLimiting();

        public static RlfRateLimiting singleton()
        {
            return singleton;
        }

        /**
         * The buckets indexed by ID.
         * <p>
         * Key of the outer map is the namespace.
         * <p>
         * Key of the inner map is the bucket ID.
         */
        private ConcurrentHashMap<String, AtomicReference<Map<Integer, TokenBucket>>> bucketsById = new ConcurrentHashMap<>();

// EEDSTL: gRPC
//        public final GrpcService grpcService = new GrpcService(this.buckets, this.redisClient, this.statistics);

        /**
         * The buckets indexed by name.
         * <p>
         * Key of the outer map is the namespace.
         * <p>
         * Key of the inner map is the bucket name.
         */
        private ConcurrentHashMap<String, AtomicReference<Map<String, TokenBucket>>> bucketsByName = new ConcurrentHashMap<>();

        private RlfRateLimiting()
        {
// EEDSTL: gRPC
//            final RateLimitingServiceImplBase rateLimitingService = new RxRateLimitingServiceGrpc.RateLimitingServiceImplBase()
//            {
//                final GrpcService service = new GrpcService(buckets, redisClient, statistics);
//
//                @Override
//                public Single<PullTokensResponse> pullTokens(Single<PullTokensRequest> request)
//                {
//                    final Single<PullTokensRequest> req = request;// .doOnSuccess(r -> log.info("MOCK received received pullToken {}", r));
//                    return req.flatMap(r -> this.service.pullTokens(r));
//                }
//            };
//
//            Server server = NettyServerBuilder.forPort(params.portGrpc).directExecutor().addService(rateLimitingService).build();
//
//            try
//            {
//                server.start();
//            }
//            catch (IOException e)
//            {
//                log.error("Could not start gRPC server. Cause: {}", e.toString());
//            }
        }

        public CommandBuckets createCommandBuckets()
        {
            return new CommandBuckets(this.bucketsById);
        }

        public CommandConfig createCommandConfig()
        {
            return new CommandConfig(this.bucketsById);
        }

        private Completable handleGetBucketsState(final RoutingContext context)
        {
            final String namespace = context.request().getParam(PAR_NAMESPACE);

            return Flowable.fromCallable(() -> this.bucketsById.computeIfAbsent(namespace, v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                               .get()
                                                               .entrySet()
                                                               .stream()
                                                               .filter(e -> e.getValue().getFillGrade() < 100d) // Skip full buckets
                                                               .map(e -> new BucketState().id(e.getKey()).fillGrade(e.getValue().getFillGrade()))
                                                               .collect(Collectors.toSet()))
                           .doOnNext(states -> log.debug("Old buckets state={}", states))
                           .flatMapCompletable(bucketsState -> context.response()
                                                                      .setStatusCode(HttpResponseStatus.OK.code())
                                                                      .putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_JSON)
                                                                      .rxEnd(json.writeValueAsString(bucketsState)))
                           .doOnSubscribe(x -> log.debug("Received GetBucketsState request, namespace={}", namespace))
                           .doOnSubscribe(x -> Statistics.singleton().stepCcInReqOam(namespace, OP_GET_BUCKETS_STATE))
                           .doOnComplete(() -> Statistics.singleton().stepCcOutAnsOam(namespace, OP_GET_BUCKETS_STATE, HttpResponseStatus.OK.code()))
                           .doOnError(e -> Statistics.singleton()
                                                     .stepCcOutAnsOam(namespace, OP_GET_BUCKETS_STATE, HttpResponseStatus.INTERNAL_SERVER_ERROR.code()))
                           .doOnError(e -> replyWithError(context,
                                                          HttpResponseStatus.BAD_REQUEST,
                                                          "Error processing GetBucketsState request. Cause: " + e.toString()));
        }

        private Completable handlePullTokens(final RoutingContext context)
        {
            final String namespace = context.request().getParam(PAR_NAMESPACE);

            return Flowable.fromCallable(() -> Optional.ofNullable(context.getBodyAsJsonArray()).orElse(new JsonArray()))
                           .doOnNext(array -> log.debug("body: {}", array))
                           .map(array ->
                           {
                               final List<PullTokensResult> results = new ArrayList<>();

                               for (Object item : array)
                               {
                                   final String ctxName = ((JsonObject) item).getString(PullTokensContext.JSON_PROPERTY_NAME);

                                   final TokenBucket bucket = ctxName != null ? this.bucketsByName.computeIfAbsent(namespace,
                                                                                                                   v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                                                                  .get()
                                                                                                  .get(ctxName)
                                                                              : this.bucketsById.computeIfAbsent(namespace,
                                                                                                                 v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                                                                .get()
                                                                                                .get(((JsonObject) item).getInteger(PullTokensContext.JSON_PROPERTY_ID));

                                   final PullTokensResult result;
                                   final double watermark = ((JsonObject) item).getDouble(PullTokensContext.JSON_PROPERTY_WATERMARK);
                                   final int amount = ((JsonObject) item).getInteger(PullTokensContext.JSON_PROPERTY_AMOUNT);

                                   if (bucket != null)
                                   {
                                       result = bucket.pull(watermark, amount);

                                       Statistics.singleton()
                                                 .stepCcTbPulls(namespace, ctxName, watermark, result.getRc().getValue())
                                                 .stepGcTbFillGrade(namespace, ctxName, bucket.getFillGrade());
                                   }
                                   else
                                   {
                                       result = new PullTokensResult().rc(PullTokensResult.RcEnum.NOT_FOUND);

                                       Statistics.singleton()
                                                 .stepCcTbPulls(namespace, ctxName != null ? ctxName : "unknown", watermark, result.getRc().getValue());
                                   }

                                   results.add(result);
                               }

                               return results;
                           })
                           .doOnNext(r -> log.debug("{}", r))
                           .flatMapCompletable(rlfResult -> context.response()
                                                                   .setStatusCode(HttpResponseStatus.OK.code())
                                                                   .putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_JSON)
                                                                   .rxEnd(json.writeValueAsString(rlfResult)))
                           .doOnSubscribe(x -> log.debug("Received PullTokens request, namespace={}", namespace))
                           .doOnSubscribe(x -> Statistics.singleton().stepCcInReqRateLim(namespace, OP_PULL_TOKENS))
                           .doOnComplete(() -> Statistics.singleton().stepCcOutAnsRateLim(namespace, OP_PULL_TOKENS, HttpResponseStatus.OK.code()))
                           .doOnError(e -> Statistics.singleton()
                                                     .stepCcOutAnsRateLim(namespace, OP_PULL_TOKENS, HttpResponseStatus.INTERNAL_SERVER_ERROR.code()))
                           .doOnError(e -> replyWithError(context,
                                                          HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                          "Error processing PullTokens request. Cause: " + e.toString()));
        }

        private Completable handleSetBucketsState(final RoutingContext context)
        {
            final String namespace = context.request().getParam(PAR_NAMESPACE);

            return Completable.defer(() -> Flowable.fromCallable(() -> Optional.ofNullable(context.getBodyAsJsonArray()).orElse(new JsonArray()))
                                                   .doOnNext(array -> log.debug("New buckets state: {}", array))
                                                   .flatMap(Flowable::fromIterable)
                                                   .map(object -> json.readValue(object.toString(), BucketState.class))
                                                   .doOnNext(bucketState -> Optional.ofNullable(this.bucketsById.computeIfAbsent(namespace,
                                                                                                                                 v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                                                                                .get()
                                                                                                                .get(bucketState.getId()))
                                                                                    .ifPresent(b -> b.setFillGrade(bucketState.getFillGrade())))
                                                   .ignoreElements())
                              .andThen(context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).rxEnd(Buffer.buffer()))
                              .doOnSubscribe(x -> log.debug("Received SetBucketsState request, namespace={}", namespace))
                              .doOnSubscribe(x -> Statistics.singleton().stepCcInReqOam(namespace, OP_SET_BUCKETS_STATE))
                              .doOnComplete(() -> Statistics.singleton().stepCcOutAnsOam(namespace, OP_SET_BUCKETS_STATE, HttpResponseStatus.OK.code()))
                              .doOnError(e -> Statistics.singleton()
                                                        .stepCcOutAnsOam(namespace, OP_SET_BUCKETS_STATE, HttpResponseStatus.INTERNAL_SERVER_ERROR.code()))
                              .doOnError(e -> replyWithError(context,
                                                             HttpResponseStatus.BAD_REQUEST,
                                                             "Error processing SetBucketsState request. Cause: " + e.toString()));
        }

        private Completable handleUpdateBuckets(final RoutingContext context)
        {
            final String namespace = context.request().getParam(PAR_NAMESPACE);

            return Completable.defer(() -> Flowable.fromCallable(() -> Optional.ofNullable(context.getBodyAsJsonArray()).orElse(new JsonArray()))
                                                   .doOnNext(array -> log.debug("body: {}", array))
                                                   .flatMap(Flowable::fromIterable)
                                                   .map(object -> TokenBucket.of(json.readValue(object.toString(), BucketConfigWithId.class)))
                                                   .toMap(bucket -> bucket.getConfig().getName(), bucket -> bucket, ConcurrentHashMap::new)
                                                   .doOnSuccess(newBuckets ->
                                                   {
                                                       final AtomicReference<Map<String, TokenBucket>> oldBucketsRef = this.bucketsByName.computeIfAbsent(namespace,
                                                                                                                                                          v -> new AtomicReference<>(new ConcurrentHashMap<>()));

                                                       oldBucketsRef.get()
                                                                    .entrySet()
                                                                    .stream()
                                                                    .filter(e -> !newBuckets.containsKey(e.getKey()))
                                                                    .forEach(e -> MetricRegister.singleton()
                                                                                                .registerForRemoval(List.of(namespace, e.getKey()), 1000));

                                                       oldBucketsRef.set(newBuckets.entrySet()//
                                                                                   .stream()
                                                                                   .map(newEntry ->
                                                                                   {
                                                                                       final TokenBucket oldBucket = oldBucketsRef.get().get(newEntry.getKey());
                                                                                       final TokenBucket newBucket = newEntry.getValue();

                                                                                       return (oldBucket == null
                                                                                               || !oldBucket.getConfig().equals(newBucket.getConfig()))
                                                                                                                                                        ? newEntry
                                                                                                                                                        : Map.entry(newEntry.getKey(),
                                                                                                                                                                    oldBucket);
                                                                                   })
                                                                                   .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue)));

                                                       // Now create the mapping of bucket ID to bucket. The new map then atomically
                                                       // replaces the old one as there can only be one configuration.

                                                       this.bucketsById.computeIfAbsent(namespace, v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                                       .set(oldBucketsRef.get()
                                                                                         .entrySet()
                                                                                         .stream()
                                                                                         .map(e -> Map.entry(e.getValue().getConfig().getId(), e.getValue()))
                                                                                         .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue)));

                                                       log.debug("bucketsByName={}", this.bucketsByName);
                                                       log.debug("bucketsById={}", this.bucketsById);
                                                   })
                                                   .ignoreElement())
                              .andThen(context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).rxEnd(Buffer.buffer()))
                              .doOnSubscribe(x -> log.debug("Received UpdateBuckets request, namespace={}", namespace))
                              .doOnSubscribe(x -> Statistics.singleton().stepCcInReqOam(namespace, OP_UPDATE_BUCKETS))
                              .doOnComplete(() -> Statistics.singleton().stepCcOutAnsOam(namespace, OP_UPDATE_BUCKETS, HttpResponseStatus.OK.code()))
                              .doOnError(e -> Statistics.singleton().stepCcOutAnsOam(namespace, OP_UPDATE_BUCKETS, HttpResponseStatus.BAD_REQUEST.code()))
                              .doOnError(e -> replyWithError(context,
                                                             HttpResponseStatus.BAD_REQUEST,
                                                             "Error processing UpdateBuckets request. Cause: " + e.toString()));
        }
    }

    private static final String HD_CONTENT_TYPE = "content-type";

    private static final String VAL_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String VAL_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";

    private static final String ETCD_ENDPOINT = "ETCD_ENDPOINT";
    private static final String ETCD_PASSWORD = "ETCD_PASSWORD";
    private static final String ETCD_USERNAME = "ETCD_USERNAME";

    private static final String ENV_POD_NAME = "POD_NAME";
    private static final String ENV_NAMESPACE = "NAMESPACE";
    private static final String ENV_LEADER_ELECTION_ENABLED = "LEADER_ELECTION_ENABLED";
    private static final String STATUS_CONTENDER = "eric-sc-rlf-contender";
    private static final String STATUS_LEADER = "eric-sc-rlf-leader";

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String LE_LEADER_KEY = "/ericsson-sc/rlf-leadership";
    private static final int LE_LEADER_TTL = 13;
    private static final int LE_RENEW_INTERVAL = 4;
    private static final int LE_CLAIM_INTERVAL = 3;
    private static final int LE_RECOVERY_DELAY = 12;
    private static final float LE_REQUEST_LATENCY = 0.5f;
    private static final long RETRIES_FOR_LEADER_ELECTION = 10l;
    private static final int ETCD_REQUEST_TIMEOUT = 2;

    private static final String MP_NRLF_OAM_V0 = "/nrlf-oam/v0";
    private static final String MP_NRLF_RATELIMITING_V0 = "/nrlf-ratelimiting/v0";

    private static final Logger log = LoggerFactory.getLogger(RlfWorker.class);
    private static final LogThrottler logThrottler = new LogThrottler();
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    private static final RlfWorkerInterfacesParameters params = RlfWorkerInterfacesParameters.instance;

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/rlf/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Starting RLF worker, version: {}", VersionInfo.get());
        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            final RlfWorker rlf = new RlfWorker(shutdownHook);
            rlf.run().blockingAwait();
        }
        catch (final Exception t)
        {
            log.error("Exception caught, exiting.", t);
            exitStatus = 1;
        }

        log.info("Stopped RLF worker.");

        System.exit(exitStatus);
    }

    private static Completable replyWithError(final RoutingContext context,
                                              final HttpResponseStatus status,
                                              final String details)
    {
        final ProblemDetails problem = new ProblemDetails();

        problem.setStatus(status.code());
        problem.setCause(status.reasonPhrase());

        if (details != null)
            problem.setDetail(details);

        String problemStr;

        try
        {
            problemStr = json.writeValueAsString(problem);
        }
        catch (final JsonProcessingException e)
        {
            problemStr = e.toString();
        }

        if (logThrottler.loggingIsDue(log.isDebugEnabled()))
        {
            if (400 <= status.code() && status.code() < 500)
            {
                log.warn(problemStr);
            }
            else if (500 <= status.code() && status.code() < 600)
            {
                log.error(problemStr);
            }
        }

        return context.response().setStatusCode(status.code()).putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_PROBLEM_JSON).rxEnd(problemStr);
    }

    private final RxShutdownHook shutdownHook;
    private final WebServer webServerExt;
    private final WebServer webServerExtOam;
    private final WebServer webServerInt;
    private final RouterHandler oamWebServer;
    private final WebClientProvider webClientProvider;
    private final CmmPatch cmPatch;
    private final ScPmbrConfigHandler pmbrCfgHandler;
    @SuppressWarnings("unused")
    private final KubeProbe kubeProbe;
    private final List<Disposable> disposables;
    private final MonitorAdapter monitored;
    private final Optional<TlsKeylogger> tlsKeyLogger;
    private final boolean leaderElectionEnabled;
    private final Optional<RxEtcd> rxEtcd;
    private final Optional<RxLeaderElection> election;
    private final ApiClient client;
    private final String ownId;
    private final CoreV1Api api;

    public RlfWorker(RxShutdownHook shutdownHook) throws IOException
    {
        this.shutdownHook = shutdownHook;
        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();

        final String localAddress = (InetAddress.getByName(Utils.getLocalAddress()) instanceof Inet6Address) ? "[::]" : "0.0.0.0";

        log.info("localAddress={}, portExtOam={}, portExt={}, portInt={}, concurrentStreamsMax={}",
                 localAddress,
                 params.portRestOam,
                 params.portRest,
                 params.portInternal,
                 params.concurrentStreamsMax);

        this.ownId = EnvVars.get(ENV_POD_NAME);

        this.client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(this.client);
        this.api = new CoreV1Api();

        this.webServerExt = this.addRoutes(WebServer.builder()
                                                    .withHost(DEFAULT_ROUTE_ADDRESS)
                                                    .withPort(params.portRest)
                                                    .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(params.concurrentStreamsMax))
                                                    .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.2"))
                                                    .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.3"))
                                                    .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.managerIfServerCertPath), // rlf
                                                                                                                                                          // worker
                                                                                                                                                          // server
                                                                                                                                                          // certificate
                                                                                                 SipTlsCertWatch.trustedCert(params.managerIfClientCaPath)))
                                                    .build(VertxInstance.get()));

        this.webServerExtOam = this.addRoutesOam(WebServer.builder()
                                                          .withHost(DEFAULT_ROUTE_ADDRESS)
                                                          .withPort(params.portRestOam)
                                                          .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.2"))
                                                          .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.3"))
                                                          .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.managerIfServerCertPath), // rlf
                                                                                                                                                                // worker
                                                                                                                                                                // server
                                                                                                                                                                // certificate
                                                                                                       SipTlsCertWatch.trustedCert(params.managerIfClientCaPath)))
                                                          .build(VertxInstance.get()));

        this.webServerInt = WebServer.builder()
                                     .withHost(DEFAULT_ROUTE_ADDRESS)
                                     .withPort(params.portInternal)
                                     .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.2"))
                                     .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.3"))
                                     .build(VertxInstance.get());

        // create web server for mediator notifications and pm-server metrics scraping
        final WebServerBuilder iws = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(params.oamServerPort);

        if (params.globalTlsEnabled)
        {
            iws.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.managerIfServerCertPath), // rlf worker server certificate
                                                            // pm server ca for verification of client certificates during scraping of
                                                            // metrics
                                                            SipTlsCertWatch.trustedCert(params.pmServerCaPath)));
        }

        this.oamWebServer = iws.build(VertxInstance.get());

        PmAdapter.configureMetricsHandler(this.oamWebServer);

        final WebClientProvider.Builder wcb = WebClientProvider.builder().withHostName(params.serviceHostname);

        if (params.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.mediatorIfClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.mediatorIfClientCaPath)));

        this.webClientProvider = wcb.build(VertxInstance.get());

        this.cmPatch = new CmmPatch(params.mediatorPort, params.mediatorHostname, this.webClientProvider, params.globalTlsEnabled);
        this.pmbrCfgHandler = new ScPmbrConfigHandler(this.cmPatch);

        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.webServerInt).register(KubeProbe.of().setAlive(true).setReady(true));
        this.disposables = new ArrayList<>();
        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(),
                                                          RlfRateLimiting.singleton().createCommandBuckets(),
                                                          RlfRateLimiting.singleton().createCommandConfig()),
                                            Arrays.asList());

        this.leaderElectionEnabled = Boolean.valueOf(EnvVars.get(ENV_LEADER_ELECTION_ENABLED));

        final var edb = RxEtcd.newBuilder()
                              .withEndpoint(EnvVars.get(ETCD_ENDPOINT))
                              .withConnectionRetries(10)
                              .withRequestTimeout(ETCD_REQUEST_TIMEOUT, TimeUnit.SECONDS);

        if (params.dcedTlsEnabled)
        {
            edb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.dcedClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        }
        else
        {
            edb.withUser(EnvVars.get(ETCD_USERNAME)).withPassword(EnvVars.get(ETCD_PASSWORD));
        }
        this.rxEtcd = Optional.of(edb.build());

        this.election = this.leaderElectionEnabled ? rxEtcd.map(etcd -> new RxLeaderElection.Builder(etcd,
                                                                                                     ownId,
                                                                                                     LE_LEADER_KEY).leaderInterval(LE_LEADER_TTL)
                                                                                                                   .renewInterval(LE_RENEW_INTERVAL)
                                                                                                                   .claimInterval(LE_CLAIM_INTERVAL)
                                                                                                                   .recoveryDelay(LE_RECOVERY_DELAY)
                                                                                                                   .requestLatency(LE_REQUEST_LATENCY)
                                                                                                                   .retries(RETRIES_FOR_LEADER_ELECTION)
                                                                                                                   .build()
                                                                                                                   .blockingGet())
                                                   : Optional.empty();
    }

    public Completable run()
    {
        final Completable waitUntilLeader = this.leaderElectionEnabled ? updatePodLabel(LeaderStatus.CONTENDER).andThen(this.election.map(RxLeaderElection::leaderStatusUpdates)
                                                                                                                                     .orElse(Observable.just(LeaderStatus.CONTENDER))
                                                                                                                                     .filter(status -> status.equals(LeaderStatus.LEADER))
                                                                                                                                     .firstOrError()
                                                                                                                                     .ignoreElement()
                                                                                                                                     .doOnSubscribe(disp -> log.info("Waiting to become leader"))
                                                                                                                                     .doOnComplete(() -> log.info("I am the leader")))
                                                                       : Completable.complete();

        final Completable leadershipLost = this.leaderElectionEnabled ? this.election.map(RxLeaderElection::leaderStatusUpdates)
                                                                                     .orElse(Observable.just(LeaderStatus.LEADER))
                                                                                     .filter(status -> status.equals(LeaderStatus.CONTENDER))
                                                                                     .firstOrError()
                                                                                     .ignoreElement()
                                                                                     .doOnComplete(() -> log.error("Lost leadership, shutting down"))
                                                                                     .andThen(this.updatePodLabel(LeaderStatus.CONTENDER))
                                                                      : Completable.never();

        final Completable rxEtcdConnection = this.leaderElectionEnabled ? this.rxEtcd.map(RxEtcd::ready).orElse(Completable.complete())
                                                                        : Completable.complete();

        final Completable rlfWorkerChain = Completable.complete()
                                                      .andThen(rxEtcdConnection) // wait until etcd is up
                                                      .andThen(this.webServerExt.startListener())
                                                      .andThen(this.webServerExtOam.startListener())
                                                      .andThen(this.webServerInt.startListener())
                                                      .andThen(this.oamWebServer.startListener())
                                                      .andThen(this.monitored.start().onErrorComplete())
                                                      .andThen(MetricRegister.singleton().start())
                                                      .andThen(waitUntilLeader)// Wait until we are leader
                                                      .andThen(this.leaderElectionEnabled ? this.updatePodLabel(LeaderStatus.LEADER) : Completable.complete())
                                                      .andThen(this.pmbrCfgHandler.createPmbrJobPatches())
                                                      .andThen(this.pmbrCfgHandler.createPmbrGroupPatches())
                                                      .andThen(Completable.ambArray(this.shutdownHook.get(), leadershipLost))
                                                      .onErrorResumeNext(e -> this.stop().andThen(Completable.error(e)))
                                                      .andThen(this.stop());

        return (this.leaderElectionEnabled ? election.map(RxLeaderElection::run).orElse(Completable.never()) : Completable.never()).ambWith(rlfWorkerChain); // Start
                                                                                                                                                             // leader
                                                                                                                                                             // election
                                                                                                                                                             // subsystem
    }

    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()
                          .andThen(MetricRegister.singleton().stop())
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.webServerExt.stopListener().onErrorComplete(logErr))
                          .andThen(this.webServerExtOam.stopListener().onErrorComplete(logErr))
                          .andThen(this.webServerInt.stopListener().onErrorComplete(logErr))
                          .andThen(this.oamWebServer.stopListener().onErrorComplete())
                          .andThen(this.webClientProvider.close().onErrorComplete(logErr))
                          .andThen(this.rxEtcd.map(RxEtcd::close).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.tlsKeyLogger.map(TlsKeylogger::stop).orElse(Completable.complete()))
                          .andThen(Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)));
    }

    private WebServer addRoutes(final WebServer server)
    {
        final Router router = Router.router(server.getVertx());

        router.post("/tokens/:namespace").handler(BodyHandler.create());
        router.post("/tokens/:namespace").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handlePullTokens(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        server.mountRouter(MP_NRLF_RATELIMITING_V0, router);

        return server;
    }

    private WebServer addRoutesOam(final WebServer server)
    {
        final Router router = Router.router(server.getVertx());

        // Deprecated
        router.get("/buckets/state/:namespace/:version").handler(BodyHandler.create());
        router.get("/buckets/state/:namespace/:version").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleGetBucketsState(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        // Deprecated
        router.put("/buckets/state/:namespace/:version").handler(BodyHandler.create());
        router.put("/buckets/state/:namespace/:version").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleSetBucketsState(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        router.get("/buckets/state/:namespace").handler(BodyHandler.create());
        router.get("/buckets/state/:namespace").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleGetBucketsState(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        router.put("/buckets/state/:namespace").handler(BodyHandler.create());
        router.put("/buckets/state/:namespace").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleSetBucketsState(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        // Deprecated
        router.put("/buckets/:namespace/:version").handler(BodyHandler.create());
        router.put("/buckets/:namespace/:version").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleUpdateBuckets(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        router.put("/buckets/:namespace").handler(BodyHandler.create());
        router.put("/buckets/:namespace").handler(ctx -> RlfWorker.RlfRateLimiting.singleton().handleUpdateBuckets(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        server.mountRouter(MP_NRLF_OAM_V0, router);

        return server;
    }

    /**
     * Update POD label with leadership status using the k8s API
     * 
     * @param leaderStatus
     * @return
     */
    private Completable updatePodLabel(LeaderStatus leaderStatus)
    {
        final String status = leaderStatus == LeaderStatus.LEADER ? STATUS_LEADER : STATUS_CONTENDER;

        return Completable.fromAction(() ->
        {
            log.info("[ID: {}] leaderStatus: {}", ownId, status);

            try
            {
                PatchUtils.patch(V1Pod.class, () ->
                // This is a blocking operation
                this.api.patchNamespacedPodCall(EnvVars.get(ENV_POD_NAME),
                                                EnvVars.get(ENV_NAMESPACE),
                                                new V1Patch("[{ \"op\": \"add\", \"path\": \"/metadata/labels/leader\", \"value\": \"" + status + "\" }]"),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null),
                                 V1Patch.PATCH_FORMAT_JSON_PATCH,
                                 this.api.getApiClient());
            }
            catch (ApiException e)
            {
                log.error("Failed to update POD leadership label, responseBody: {}", e.getResponseBody(), e);
                // Ignore Failure
            }
            catch (Exception e)
            {
                log.error("Failed to update POD leadership  label ", e);
                // Ignore failure
            }
        }).subscribeOn(Schedulers.io());
    }
}
