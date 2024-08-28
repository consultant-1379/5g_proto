/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 19, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nlf.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config;
import com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.google.common.base.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.net.SocketAddress;

/**
 * Encapsulates the automatic configuration of eric-sc-nlf pods.
 */
public class NlfConfigurator
{
    private static final String ERIC_SC_NLF = "eric-sc-nlf";
    private static final String SCHEME_HTTPS = "https://";
    private static final String PATH_CONFIG = "/nnlf-oam/v0/config";

    private static final int REQUEST_TIMEOUT_MILLIS = Integer.parseInt(EnvVars.get("NLF_REQUEST_TIMEOUT_MILLIS", 4000)); // [ms]
    private static final int UPDATE_PERIOD_SECS = Integer.parseInt(EnvVars.get("NLF_UPDATE_PERIOD_SECS", 1)); // [s]

    private static final String NLF_TLS_CERT_PATH = EnvVars.get("NLF_INTERFACE_CLIENT_CERTIFICATES_PATH", "/run/secrets/eric-manager-nlf");
    private static final String NLF_TLS_ROOTCA_PATH = EnvVars.get("NLF_INTERFACE_ROOTCA_PATH", "/run/secrets/eric-manager-nlf/rootca");
    private static final int NLF_PORT_REST_OAM = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST_OAM", 8080));

    private static final NlfConfigurator singleton = new NlfConfigurator();
    private static final Logger log = LoggerFactory.getLogger(NlfConfigurator.class);

    public static void dispatchNrfGroupIds(final List<NrfGroup> config)
    {
        // Check NRF groups for duplicate IDs and dispatch them if necessary.
        // Retry dispatching the IDs as long as there is a conflict.

        boolean retry;

        do
        {
            retry = false;

            final Map<Integer, NrfGroup> gps = new HashMap<>();

            for (final NrfGroup gc : config)
            {
                final NrfGroup gp = gps.put(Objects.hashCode(gc.getPath(), gc.getName()), gc);

                // Consider both path and name for the ID generation. name alone is not enough.

                if (gp != null && !(gp.getPath().equals(gc.getPath()) && gp.getName().equals(gc.getName())))
                {
                    log.info("NRF group names '{}' and '{}' yield the same ID. Appending ' ' to both and trying again.", gp.getName(), gc.getName());

                    gp.setName(new StringBuilder(gp.getName()).append(" ").toString());
                    gc.setName(new StringBuilder(gc.getName()).append(" ").toString());

                    retry = true;
                    break;
                }
            }
        }
        while (retry);
    }

    public static NlfConfigurator singleton()
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
        wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(NLF_TLS_CERT_PATH), SipTlsCertWatch.trustedCert(NLF_TLS_ROOTCA_PATH)));
        return wcb.build(VertxInstance.get());
    }

    private final Subject<Pair<String, Config>> configsSubject = BehaviorSubject.<Pair<String, Config>>create().toSerialized();
    private final AtomicReference<Optional<Pair<Set<String>, Pair<String, Config>>>> cachedCtx = new AtomicReference<>(Optional.empty());

    private List<Disposable> disposables = new ArrayList<>();

    private WebClientProvider webClientProvider = createWebClient();

    private NlfConfigurator()
    {
    }

    public void publish(final String namespace,
                        final Config config)
    {
        log.debug("Publishing new configuration to NLF, namespace={}", namespace);
        this.configsSubject.onNext(Pair.of(namespace, config));
    }

    public Completable start()
    {
        return Completable.defer(() ->
        {
            if (this.disposables.isEmpty())
            {
                this.disposables.add(Completable.complete()
                                                .andThen(NlfEndpointsRetriever.singleton().start())
                                                .andThen(Completable.ambArray(this.updater()))
                                                .doOnSubscribe(d -> log.info("Started configuring NLF pods."))
                                                .doOnDispose(() -> log.info("Stopped configuring NLF pods."))
                                                .subscribe(() -> log.info("Stopped configuring NLF pods."),
                                                           t -> log.error("Stopped configuring NLF pods. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));
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

    private Completable updater()
    {
        return Flowable.combineLatest(NlfEndpointsRetriever.singleton().getEndpoints(),
                                      this.configsSubject.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged(),
                                      Pair::of)
                       .observeOn(Schedulers.single())
                       .map(ctx -> Pair.of(ctx.getFirst().getSecond(), ctx.getSecond())) // Strip-off the sessionId.
                       .doOnNext(ctx -> this.cachedCtx.set(Optional.of(ctx)))
                       .filter(ctx -> !ctx.getFirst().isEmpty()) // Continue only if there are endpoints
                       .flatMapCompletable(ctx -> Flowable.fromIterable(ctx.getFirst())
                                                          .subscribeOn(Schedulers.io())
                                                          .flatMapSingle(nlfPodIp -> this.webClientProvider.getWebClient()
                                                                                                           .flatMap(wc -> wc.requestAbs(HttpMethod.PUT,
                                                                                                                                        SocketAddress.inetSocketAddress(NLF_PORT_REST_OAM,
                                                                                                                                                                        nlfPodIp),
                                                                                                                                        buildUrl(SCHEME_HTTPS,
                                                                                                                                                 ERIC_SC_NLF,
                                                                                                                                                 NLF_PORT_REST_OAM,
                                                                                                                                                 PATH_CONFIG,
                                                                                                                                                 ctx.getSecond()
                                                                                                                                                    .getFirst()))
                                                                                                                            .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                                                                            .rxSendJson(ctx.getSecond()
                                                                                                                                           .getSecond())
                                                                                                                            .doOnError(error -> log.warn("Problem sending configuration to NLF pod. Cause: {}",
                                                                                                                                                         error.toString()))
                                                                                                                            .doOnSuccess(resp -> log.debug("{} response: {} {}",
                                                                                                                                                           HttpMethod.PUT,
                                                                                                                                                           resp.statusCode(),
                                                                                                                                                           resp.bodyAsString()))))
                                                          .ignoreElements())
                       .doOnError(error -> log.warn("Problem configuring NLF pods. Cause: {}", Utils.toString(error, log.isDebugEnabled())))
                       .retryWhen(h -> h.delay(UPDATE_PERIOD_SECS, TimeUnit.SECONDS));
    }
}
