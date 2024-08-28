/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 19, 2022
 *     Author: eedstl
 */

package com.ericsson.sim.loadgen.rlf;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nrlf.ratelimiting.PullTokensContext;
import com.ericsson.sc.ratelimiting.service.grpc.PullTokensRequest;
import com.ericsson.sc.ratelimiting.service.grpc.RxRateLimitingServiceGrpc;
import com.ericsson.sc.rlf.client.RlfEndpointsRetriever;
import com.ericsson.sim.loadgen.LoadGenerator.Configuration;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.metrics.SampleStatistics;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.web.client.WebClient;

public class RlfLoad
{
    public static class Controller
    {
        private static final Controller singleton = new Controller();

        public static Controller singleton()
        {
            return singleton;
        }

        private final List<Disposable> loadGenerators = Collections.synchronizedList(new ArrayList<>());

        private final Subject<Optional<Configuration>> configSubject;

        private Controller()
        {
            this.configSubject = BehaviorSubject.<Optional<Configuration>>create().toSerialized();
            this.configSubject.observeOn(Schedulers.single()).distinctUntilChanged().subscribe(c ->
            {
                synchronized (this.loadGenerators)
                {
                    log.info("Applying new configuration: {}", c);

                    if (c.isPresent())
                    {
                        final Configuration.Rlf rlf = c.get().getRlf();

                        if (rlf != null && rlf.getNumInstances() > 0)
                        {
                            RlfEndpointsRetriever.singleton().start().blockingAwait();

                            final int delta = c.get().getRlf().getNumInstances() - this.loadGenerators.size();

                            if (delta > 0)
                            {
                                for (int i = 0; i < delta; ++i)
                                    this.loadGenerators.add(new RlfLoad(this.configSubject.toFlowable(BackpressureStrategy.LATEST)).loadRest(this.loadGenerators.size())
                                                                                                                                   .subscribe());
                            }
                            else if (delta < 0)
                            {
                                for (int i = delta; i < 0; ++i)
                                    this.loadGenerators.remove(this.loadGenerators.size() - 1).dispose();
                            }
                        }
                        else
                        {
                            for (int i = 0; i < this.loadGenerators.size(); ++i)
                                this.loadGenerators.remove(this.loadGenerators.size() - 1).dispose();
                        }
                    }
                    else
                    {
                        for (int i = 0; i < this.loadGenerators.size(); ++i)
                            this.loadGenerators.remove(this.loadGenerators.size() - 1).dispose();
                    }

                    if (this.loadGenerators.isEmpty())
                        RlfEndpointsRetriever.singleton().stop().blockingAwait();
                }
            }, t -> log.error("Error applying new configuration. Cause: {}", t.toString()));
        }

        public void publish(final Optional<Configuration> config)
        {
            this.configSubject.onNext(config);
        }
    }

    private static class TokenBucket
    {
        private final long capacity;
        private final double fillRate;
        private double tokens;
        private Instant lastPull;
        private Object lock = new Object();

        public TokenBucket(final long capacity,
                           final double fillRate)
        {
            this.capacity = capacity;
            this.fillRate = fillRate;
            this.tokens = capacity;
            this.lastPull = Instant.now();
        }

        /**
         * Pull one token if the bucket's fill grade is not smaller than the given water
         * mark [%] and return the result of the operation.
         * 
         * @return Boolean True if the token could be pulled, false otherwise.
         */
        public boolean pull()
        {
            synchronized (this.lock)
            {
                final Instant lastPull = this.lastPull;
                double tokens = Math.min(this.tokens, this.capacity);

                final Instant now = Instant.now();
                final Duration duration = Duration.between(lastPull, Instant.now());
                final double timeSpan = duration.getSeconds() + duration.getNano() / 1e9; // [s]

                tokens = Math.min(tokens + this.fillRate * timeSpan, this.capacity) - 1;

                if (0 > 100 * tokens / this.capacity)
                    return false;

                this.tokens = tokens;
                this.lastPull = now;

                return true;
            }
        }

        @Override
        public String toString()
        {
            return new StringBuilder("{").append(", lastPull=").append(this.lastPull).append(", tokens=").append(this.tokens).append("}").toString();
        }
    }

    private static class RxRateLimitingServiceStubDispatcher
    {
        private final AtomicReference<List<ManagedChannel>> channels = new AtomicReference<>(new ArrayList<>());
        private final AtomicReference<List<RxRateLimitingServiceGrpc.RxRateLimitingServiceStub>> stubs = new AtomicReference<>(new ArrayList<>());
        private final AtomicInteger cnt = new AtomicInteger();

        public void update(final List<String> ips)
        {
            this.channels.set(ips.stream()
                                 .map(ip -> NettyChannelBuilder.forAddress(ip, 82)
                                                               .usePlaintext() // Do not use TLS (which is default)
                                                               .directExecutor()
                                                               .build())
                                 .collect(Collectors.toList()));

            this.stubs.set(this.channels.get().stream().map(c -> RxRateLimitingServiceGrpc.newRxStub(c)).collect(Collectors.toList()));
        }

        public RxRateLimitingServiceGrpc.RxRateLimitingServiceStub getNext()
        {
            final int cnt = this.cnt.getAndIncrement();
            final List<RxRateLimitingServiceGrpc.RxRateLimitingServiceStub> stubs = this.stubs.get();
            return stubs.get(cnt % stubs.size());
        }

        public Completable close()
        {
            return Completable.fromAction(() -> this.channels.get().stream().forEach(c ->
            {
                try
                {
                    c.shutdown();

                    while (!c.awaitTermination(5, TimeUnit.SECONDS))
                        log.info("Still awaiting termination of channel {}", c);
                }
                catch (InterruptedException e)
                {
                }
            }));
        }
    }

    public static class Statistics
    {
        private static final Statistics singleton = new Statistics();

        private static Statistics singleton()
        {
            return singleton;
        }

        private final io.prometheus.client.Counter ccOutReq = MetricRegister.singleton()
                                                                            .register(io.prometheus.client.Counter.build()
                                                                                                                  .namespace("rlf")
                                                                                                                  .name("out_requests_nrlf_ratelimiting_total")
                                                                                                                  .labelNames("namespace", "subject")
                                                                                                                  .help("Number of outgoing requests on the internal nrlf_ratelimiting interface")
                                                                                                                  .register());

        private final io.prometheus.client.Counter ccInAns = MetricRegister.singleton()
                                                                           .register(io.prometheus.client.Counter.build()
                                                                                                                 .namespace("rlf")
                                                                                                                 .name("in_answers_nrlf_ratelimiting_total")
                                                                                                                 .labelNames("namespace", "subject", "status")
                                                                                                                 .help("Number of incoming answers on the internal nrlf_ratelimiting interface")
                                                                                                                 .register());

        private Statistics()
        {
        }

        public Statistics stepCcOutReq(final String namespace,
                                       final String subject)
        {
            this.ccOutReq.labels(namespace, subject).inc();
            return this;
        }

        public Statistics stepCcInAns(final String namespace,
                                      final String subject,
                                      final int status)
        {
            this.ccInAns.labels(namespace, subject, Integer.toString(status)).inc();
            return this;
        }
    }

    private static class UrlDispatcher
    {
        private final AtomicReference<List<Url>> urls = new AtomicReference<>(new ArrayList<>());
        private final AtomicInteger cnt = new AtomicInteger();
        private final AtomicReference<Configuration> config;
        private final SecureRandom rand = new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes());

        public UrlDispatcher(final AtomicReference<Configuration> config)
        {
            this.config = config;
        }

        public void update(final List<String> ips)
        {
            this.urls.set(ips.stream().map(ip -> new Url(ip, 81, "/nrlf-ratelimiting/v0/tokens/scp")).collect(Collectors.toList()));
        }

        public Url getNext()
        {
            final int cnt = this.cnt.getAndIncrement();
            final List<Url> urls = this.urls.get();

            return urls.get(this.config.get().getRlf().getRoundRobin() ? (cnt % urls.size()) : this.rand.nextInt(urls.size()));
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RlfLoad.class);

    private static PullTokensRequest createPullTokensRequestGrpc()
    {
        return PullTokensRequest.newBuilder()
                                .setNamespace("scp")
                                .addContexts(com.ericsson.sc.ratelimiting.service.grpc.PullTokensContext.newBuilder()
                                                                                                        .setName("ingress=GRLname.own=internalNetwork")
                                                                                                        .setWatermark(0d)
                                                                                                        .setAmount(1))
                                .build();
    }

    private final WebClient client = WebClient.create(VertxInstance.get(),
                                                      new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2)
                                                                            .setHttp2ClearTextUpgrade(false)
                                                                            .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3")));

    final AtomicReference<TokenBucket> tbGrpc = new AtomicReference<>(new TokenBucket(1, 0));
    final AtomicReference<TokenBucket> tbRest = new AtomicReference<>(new TokenBucket(1, 0));

    final AtomicReference<Configuration> config = new AtomicReference<>(new Configuration().setRlf(new Configuration.Rlf().setNumInstances(1)
                                                                                                                          .setPullRequest(new Configuration.Rlf.PullRequest().setContext(new PullTokensContext().name("ingress=GRLname.own=internalNetwork")
                                                                                                                                                                                                                .watermark(10d)
                                                                                                                                                                                                                .amount(1)))));
    final UrlDispatcher urls = new UrlDispatcher(config);
    final RxRateLimitingServiceStubDispatcher stubs = new RxRateLimitingServiceStubDispatcher();

    public RlfLoad(final Flowable<Optional<Configuration>> configFlow)
    {
        RlfEndpointsRetriever.singleton()
                             .getEndpoints()//
                             .map(p -> p.getSecond().stream().collect(Collectors.toList()))
                             .distinctUntilChanged()
                             .subscribe(ips ->
                             {
                                 log.info("Updating: ips={}", ips);
                                 this.urls.update(ips);
                                 this.stubs.update(ips);
                             });

        configFlow.filter(Optional::isPresent)//
                  .switchIfEmpty(Flowable.just(Optional.of(new Configuration())))
                  .map(Optional::get)
                  .subscribe(c ->
                  {
                      log.info("Updating: config={}", c);

                      if (c.getRlf() != null)
                      {
                          this.config.set(c);
                          final long rate = c.getRlf().getPullRequest().getRate();
                          this.tbGrpc.set(new TokenBucket(9, rate));
                          this.tbRest.set(new TokenBucket(9, rate));
                      }
                      else
                      {
                          this.tbGrpc.set(new TokenBucket(1, 0));
                          this.tbRest.set(new TokenBucket(1, 0));
                      }
                  });
    }

    public Completable loadGrpc(final int num)
    {
        return Completable.defer(() ->
        {
            final SampleStatistics lm = SampleStatistics.of(num + ": RTT RLF (gRPC)");

            return Flowable.interval(1, TimeUnit.MILLISECONDS)
                           .subscribeOn(Schedulers.computation())
                           .onBackpressureLatest()
                           .filter(i -> this.tbGrpc.get().pull())
                           .flatMapSingle(i -> Single.fromCallable(() -> Instant.now())//
                                                     .subscribeOn(Schedulers.io())
                                                     .zipWith(this.stubs.getNext().pullTokens(createPullTokensRequestGrpc()), Pair::of)
                                                     .doOnSuccess(p -> lm.add(p.getFirst()))
//                                                         .doOnSuccess(p -> log.info("result={}", p.getSecond()))
                                                     .map(Pair::getSecond)
//                                                         .doOnSuccess(r -> r.getResultList()
//                                                                            .stream()
//                                                                            .forEach(rr -> log.info("{}, {}", rr.getRc().name(), rr.getRa())))//
            )
                           .lastOrError()
                           .ignoreElement()
                           .doFinally(() -> log.info("latencyGrpc={}", lm))
                           .retryWhen(h -> h.delay(1, TimeUnit.SECONDS));
        });
    }

    public Completable loadRest(final int num)
    {
        return Completable.defer(() ->
        {
            final SampleStatistics lm = SampleStatistics.of(num + ": RTT RLF (REST)");
            final PullTokensContext context = this.config.get().getRlf().getPullRequest().getContext();
            final String bucketName = context.getName();

            return Flowable.interval(1, TimeUnit.MILLISECONDS)//
                           .subscribeOn(Schedulers.computation())
                           .onBackpressureLatest()
                           .filter(i -> this.tbRest.get().pull())
                           .flatMapSingle(n ->
                           {
                               final Url url = this.urls.getNext();

                               return Single.fromCallable(() -> java.time.Instant.now())
                                            .zipWith(this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                                                .rxSendJson(new JsonArray().add(context))
                                                                .subscribeOn(Schedulers.io())
                                                                .doOnSubscribe(x -> Statistics.singleton().stepCcOutReq("scp", bucketName))
                                                                .doOnSuccess(r -> Statistics.singleton().stepCcInAns("scp", bucketName, r.statusCode()))
                                                                .doOnError(err -> log.error("Error sending HTTP request to {}: {}", url, err)),
                                                     Pair::of)
                                            .doOnSuccess(p -> lm.add(p.getFirst()))
                                            .map(p -> p.getSecond());
                           })
                           .lastOrError()
                           .ignoreElement()
                           .doFinally(() -> log.info("latencyRest={}", lm))
                           .retryWhen(h -> h.delay(1, TimeUnit.SECONDS));
        });
    }
}
