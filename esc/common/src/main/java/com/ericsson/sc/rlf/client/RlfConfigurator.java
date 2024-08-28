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
 * Created on: Mar 7, 2022
 *     Author: eedstl
 */

package com.ericsson.sc.rlf.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketConfig;
import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketState;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.ext.web.client.HttpResponse;

/**
 * Encapsulates the automatic configuration of eric-sc-rlf pods.
 */
public class RlfConfigurator
{
    private static final String ERIC_SC_RLF = "eric-sc-rlf";
    private static final String SCHEME_HTTPS = "https://";
    private static final String PATH_BUCKETS = "/nrlf-oam/v0/buckets";
    private static final String PATH_BUCKETS_STATE = PATH_BUCKETS + "/state";

    private static final int REQUEST_TIMEOUT_MILLIS = Integer.parseInt(EnvVars.get("RLF_REQUEST_TIMEOUT_MILLIS", 4000)); // [ms]
    private static final int SYNCHRONIZE_PERIOD_SECS = Integer.parseInt(EnvVars.get("RLF_SYNCHRONIZE_PERIOD_SECS", 5)); // [s]
    private static final int UPDATE_PERIOD_SECS = Integer.parseInt(EnvVars.get("RLF_UPDATE_PERIOD_SECS", 1)); // [s]

    private static final String RLF_TLS_CERT_PATH = EnvVars.get("RLF_INTERFACE_CLIENT_CERTIFICATES_PATH", "/run/secrets/eric-manager-rlf");
    private static final String RLF_TLS_ROOTCA_PATH = EnvVars.get("RLF_INTERFACE_ROOTCA_PATH", "/run/secrets/eric-manager-rlf/rootca");
    private static final int RLF_PORT_REST_OAM = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST_OAM", 8080));

    private static final RlfConfigurator singleton = new RlfConfigurator();
    private static final Logger log = LoggerFactory.getLogger(RlfConfigurator.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse

    public static void dispatchBucketIds(final List<BucketConfig> buckets)
    {
        // Check buckets for duplicate IDs and dispatch them if necessary.
        // Retry dispatching the IDs as long as there is a conflict.

        boolean retry;

        do
        {
            retry = false;

            final Map<Integer, BucketConfig> bps = new HashMap<>();

            for (final BucketConfig bc : buckets)
            {
                final BucketConfig bp = bps.put(bc.getName().hashCode(), bc);

                if (bp != null && !bp.getName().equals(bc.getName()))
                {
                    log.info("Bucket names '{}' and '{}' yield the same ID. Appending ' ' to both and trying again.", bp.getName(), bc.getName());

                    bp.setName(new StringBuilder(bp.getName()).append(" ").toString());
                    bc.setName(new StringBuilder(bc.getName()).append(" ").toString());

                    retry = true;
                    break;
                }
            }
        }
        while (retry);
    }

    public static RlfConfigurator singleton()
    {
        return singleton;
    }

    private static String buildUrl(final String scheme,
                                   String host,
                                   final int port,
                                   final String path,
                                   final String namespace)
    {
        host = host.strip();
        final String wrappedHost = !host.startsWith("[") && host.contains(":") ? new StringBuilder("[").append(host).append("]").toString() : host;

        return new StringBuilder(scheme).append(wrappedHost).append(":").append(port).append(path).append("/").append(namespace).toString();
    }

    private static WebClientProvider createWebClient()
    {
        final WebClientProvider.Builder wcb = WebClientProvider.builder();
        wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(RLF_TLS_CERT_PATH), SipTlsCertWatch.trustedCert(RLF_TLS_ROOTCA_PATH)));
        return wcb.build(VertxInstance.get());
    }

    private final Subject<Pair<String, List<BucketConfig>>> bucketsSubject = BehaviorSubject.<Pair<String, List<BucketConfig>>>create().toSerialized();
    private final AtomicReference<Optional<Pair<Set<String>, Pair<String, List<BucketConfig>>>>> cachedCtx = new AtomicReference<>(Optional.empty());

    private List<Disposable> disposables = new ArrayList<>();

    private WebClientProvider webClientProvider = createWebClient();

    private RlfConfigurator()
    {
    }

    public void publish(final String namespace,
                        List<BucketConfig> buckets)
    {
        log.debug("Publishing new configuration to RLF, namespace={}", namespace);
        this.bucketsSubject.onNext(Pair.of(namespace, buckets));
    }

    public Completable start()
    {
        var GRLEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_RATE_LIMIT_ENABLED", false));
        if (GRLEnabled == false)
        {
            log.debug("GRL is disabled");
            return Completable.complete();

        }
        return Completable.defer(() ->
        {
            if (this.disposables.isEmpty())
            {
                this.disposables.add(Completable.complete()
                                                .andThen(RlfEndpointsRetriever.singleton().start())
                                                .andThen(Completable.ambArray(this.updater(), this.synchronizer()))
                                                .doOnSubscribe(d -> log.info("Started configuring RLF pods."))
                                                .doOnDispose(() -> log.info("Stopped configuring RLF pods."))
                                                .subscribe(() -> log.info("Stopped configuring RLF pods."),
                                                           t -> log.error("Stopped configuring RLF pods. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));
            }

            return Completable.complete();
        });
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();

            return Completable.complete();
        });
    }

    private Completable synchronizer()
    {
        // In a loop:
        // Get the bucket states from all RLF endpoints.
        // Aggregate those in a map of bucket states.
        // Calculate the average of all bucket states aggregated.
        // Set the new bucket states in all RLF endpoints.

        return Flowable.interval(SYNCHRONIZE_PERIOD_SECS, TimeUnit.SECONDS)
                       .observeOn(Schedulers.single())
                       .onBackpressureDrop()
                       .map(x -> this.cachedCtx.get())
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .filter(ctx -> !ctx.getFirst().isEmpty()) // Continue only if there are endpoints
                       .flatMapCompletable(ctx -> Flowable.fromIterable(ctx.getFirst())
                                                          .subscribeOn(Schedulers.io())
                                                          .flatMapSingle(rlfPodIp -> this.webClientProvider.getWebClient()
                                                                                                           .flatMap(wc -> wc.requestAbs(HttpMethod.GET,
                                                                                                                                        SocketAddress.inetSocketAddress(RLF_PORT_REST_OAM,
                                                                                                                                                                        rlfPodIp),
                                                                                                                                        buildUrl(SCHEME_HTTPS,
                                                                                                                                                 ERIC_SC_RLF,
                                                                                                                                                 RLF_PORT_REST_OAM,
                                                                                                                                                 PATH_BUCKETS_STATE,
                                                                                                                                                 ctx.getSecond()
                                                                                                                                                    .getFirst()))
                                                                                                                            .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                                                                            .rxSend()
                                                                                                                            .doOnError(error -> log.warn("Problem retrieving bucket states from RLF pod. Cause: {}",
                                                                                                                                                         error.toString()))
                                                                                                                            .doOnSuccess(resp -> log.debug("{} response: {} {}",
                                                                                                                                                           HttpMethod.GET,
                                                                                                                                                           resp.statusCode(),
                                                                                                                                                           resp.bodyAsString()))
                                                                                                                            .map(HttpResponse::bodyAsJsonArray)))
                                                          .flatMap(Flowable::fromIterable)
                                                          .map(object -> json.readValue(object.toString(), BucketState.class))
                                                          .toMultimap(BucketState::getId,
                                                                      BucketState::getFillGrade,
                                                                      ConcurrentHashMap::new,
                                                                      key -> new ArrayList<>())
                                                          .map(mm -> mm.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey, v ->
                                                          {
                                                              final int delta = ctx.getFirst().size() - v.getValue().size();

                                                              if (delta > 0)
                                                                  v.getValue().add(delta * 100d);

                                                              return v.getValue();
                                                          })))
                                                          .doOnSuccess(mm -> log.debug("Current buckets state={}", mm))
                                                          .map(mm -> mm.entrySet()
                                                                       .stream()
                                                                       .collect(Collectors.toConcurrentMap(Entry::getKey,
                                                                                                           entry -> new BucketState().id(entry.getKey())
                                                                                                                                     .fillGrade(entry.getValue()
                                                                                                                                                     .stream()
                                                                                                                                                     .collect(Collectors.averagingDouble(d -> d)))))
                                                                       .values())
                                                          .doOnSuccess(result -> log.debug("Synchronized buckets state={}", result))
                                                          .flatMapCompletable(states -> Flowable.fromIterable(ctx.getFirst())
                                                                                                .subscribeOn(Schedulers.io())
                                                                                                .flatMapSingle(rlfPodIp -> this.webClientProvider.getWebClient()
                                                                                                                                                 .flatMap(wc -> wc.requestAbs(HttpMethod.PUT,
                                                                                                                                                                              SocketAddress.inetSocketAddress(RLF_PORT_REST_OAM,
                                                                                                                                                                                                              rlfPodIp),
                                                                                                                                                                              buildUrl(SCHEME_HTTPS,
                                                                                                                                                                                       ERIC_SC_RLF,
                                                                                                                                                                                       RLF_PORT_REST_OAM,
                                                                                                                                                                                       PATH_BUCKETS_STATE,
                                                                                                                                                                                       ctx.getSecond()
                                                                                                                                                                                          .getFirst()))
                                                                                                                                                                  .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                                                                                                                  .rxSendJson(states)
                                                                                                                                                                  .doOnError(error -> log.warn("Problem sending bucket states to RLF pod. Cause: {}",
                                                                                                                                                                                               error.toString()))
                                                                                                                                                                  .doOnSuccess(resp -> log.debug("{} response: {} {}",
                                                                                                                                                                                                 HttpMethod.PUT,
                                                                                                                                                                                                 resp.statusCode(),
                                                                                                                                                                                                 resp.bodyAsString()))))
                                                                                                .ignoreElements()))
                       .doOnError(error -> log.warn("Problem synchronizing buckets in RLF pods. Cause: {}", Utils.toString(error, log.isDebugEnabled())))
                       .retryWhen(h -> h.delay(SYNCHRONIZE_PERIOD_SECS, TimeUnit.SECONDS));
    }

    private Completable updater()
    {
        return Flowable.combineLatest(RlfEndpointsRetriever.singleton().getEndpoints(),
                                      this.bucketsSubject.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged(),
                                      Pair::of)
                       .observeOn(Schedulers.single())
                       .map(ctx -> Pair.of(ctx.getFirst().getSecond(), ctx.getSecond())) // Strip-off the sessionId.
                       .doOnNext(ctx -> this.cachedCtx.set(Optional.of(ctx)))
                       .filter(ctx -> !ctx.getFirst().isEmpty()) // Continue only if there are endpoints
                       .map(ctx -> Pair.of(ctx.getFirst(), Pair.of(ctx.getSecond().getFirst(), ctx.getSecond().getSecond().stream().map(bc ->
                       {
                           final int numEndpoints = ctx.getFirst().size();
                           return new BucketConfig().name(bc.getName())
                                                    .capacity(Math.round((double) bc.getCapacity() / numEndpoints))
                                                    .fillRate(bc.getFillRate() / numEndpoints);
                       }).collect(Collectors.toList()))))
                       .flatMapCompletable(ctx -> Flowable.fromIterable(ctx.getFirst())
                                                          .subscribeOn(Schedulers.io())
                                                          .flatMapSingle(rlfPodIp -> this.webClientProvider.getWebClient()
                                                                                                           .flatMap(wc -> wc.requestAbs(HttpMethod.PUT,
                                                                                                                                        SocketAddress.inetSocketAddress(RLF_PORT_REST_OAM,
                                                                                                                                                                        rlfPodIp),
                                                                                                                                        buildUrl(SCHEME_HTTPS,
                                                                                                                                                 ERIC_SC_RLF,
                                                                                                                                                 RLF_PORT_REST_OAM,
                                                                                                                                                 PATH_BUCKETS,
                                                                                                                                                 ctx.getSecond()
                                                                                                                                                    .getFirst()))
                                                                                                                            .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                                                                            .rxSendJson(ctx.getSecond()
                                                                                                                                           .getSecond())
                                                                                                                            .doOnError(error -> log.warn("Problem sending configuration to RLF pod. Cause: {}",
                                                                                                                                                         error.toString()))
                                                                                                                            .doOnSuccess(resp -> log.debug("{} response: {} {}",
                                                                                                                                                           HttpMethod.PUT,
                                                                                                                                                           resp.statusCode(),
                                                                                                                                                           resp.bodyAsString()))))
                                                          .ignoreElements())
                       .doOnError(error -> log.warn("Problem configuring RLF pods. Cause: {}", Utils.toString(error, log.isDebugEnabled())))
                       .retryWhen(h -> h.delay(UPDATE_PERIOD_SECS, TimeUnit.SECONDS));
    }
}
