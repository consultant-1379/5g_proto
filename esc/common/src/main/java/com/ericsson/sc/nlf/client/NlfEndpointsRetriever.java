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
 * Created on: May 25, 2022
 *     Author: eedstl
 */

package com.ericsson.sc.nlf.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.Utils;
import com.google.gson.Gson;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1EndpointAddress;
import io.kubernetes.client.openapi.models.V1EndpointsList;
import io.kubernetes.client.util.Config;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;

/**
 * Encapsulates the periodic retrieval of endpoints of eric-sc-nlf pods.
 */
public class NlfEndpointsRetriever
{
    private static final String ENV_NAMESPACE = "NAMESPACE";

    private static final int POLLING_DELAY_SECS = 5; // [s]
    private static final int RETRY_DELAY_SECS = 1; // [s]

    private static final NlfEndpointsRetriever singleton = new NlfEndpointsRetriever();
    private static final Logger log = LoggerFactory.getLogger(NlfEndpointsRetriever.class);

    public static NlfEndpointsRetriever singleton()
    {
        return singleton;
    }

    private static String extractResourceVersionFromException(final ApiException ex)
    {
        final String body = ex.getResponseBody();

        if (body == null)
            return null;

        final Gson gson = new Gson();
        final Map<?, ?> st = gson.fromJson(body, Map.class);
        final Pattern p = Pattern.compile("Timeout: Too large resource version: (\\d+), current: (\\d+)");
        final String msg = (String) st.get("message");
        final Matcher m = p.matcher(msg);

        if (!m.matches())
            return null;

        return m.group(2);
    }

    private Subject<Pair<Integer /* sessionId */, Set<String>>> endpointsSubject = BehaviorSubject.createDefault(Pair.of(0, Set.<String>of())).toSerialized();

    private List<Disposable> disposables = new ArrayList<>();

    private String resourceVersion = null;

    private NlfEndpointsRetriever()
    {
    }

    public Flowable<Pair<Integer, Set<String>>> getEndpoints()
    {
        return this.endpointsSubject.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged();
    }

    public Completable start()
    {
        return Completable.defer(() ->
        {
            if (this.disposables.isEmpty())
            {
                this.disposables.add(Completable.ambArray(this.poller())
                                                .doOnSubscribe(d -> log.info("Started retrieving endpoints of NLF pods."))
                                                .doOnDispose(() -> log.info("Stopped retrieving endpoints of NLF pods."))
                                                .subscribe(() -> log.info("Stopped retrieving endpoints of NLF pods."),
                                                           t -> log.error("Stopped retrieving endpoints of NLF pods. Cause: {}",
                                                                          Utils.toString(t, log.isDebugEnabled()))));

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

    private Completable poller()
    {
        final AtomicInteger pollingCnt = new AtomicInteger(0);

        return this.retrieveNlfEndpoints()
                   .doOnSuccess(ips -> log.debug("ips={}", ips))
                   // Trigger update unconditionally every 6th poll to overcome pod restarts (IPs
                   // are unchanged then).
                   .doOnSuccess(ips -> this.endpointsSubject.onNext(Pair.of(pollingCnt.getAndIncrement() / 6, ips)))
                   .subscribeOn(Schedulers.io())
                   .repeatWhen(handler -> handler.delay(POLLING_DELAY_SECS, TimeUnit.SECONDS))
                   .ignoreElements()
                   .retryWhen(h -> h.delay(RETRY_DELAY_SECS, TimeUnit.SECONDS));
    }

    private Single<Set<String>> retrieveNlfEndpoints()
    {
        return Single.defer(() ->
        {
            final ApiClient client;

            try
            {
                client = Config.defaultClient();
            }
            catch (IOException e)
            {
                log.error("Exception while initializing K8s API", e);
                return Single.error(e);
            }

            // Optional, put helpful during tests: disable client timeout and enable
            // HTTP wire-level logs
//            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> log.info(message));
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            final OkHttpClient newClient = client.getHttpClient().newBuilder().addInterceptor(interceptor).readTimeout(0, TimeUnit.SECONDS).build();
            final OkHttpClient newClient = client.getHttpClient().newBuilder().build();

            client.setHttpClient(newClient);
            final CoreV1Api api = new CoreV1Api(client);

            try
            {
                final V1EndpointsList eps = api.listNamespacedEndpoints(EnvVars.get(ENV_NAMESPACE, "default"), // String namespace
                                                                        null, // String pretty
                                                                        null, // Boolean allowWatchBookmarks
                                                                        null, // String _continue
                                                                        null, // String fieldSelector
                                                                        "app.kubernetes.io/name=eric-sc-nlf", // String labelSelector
                                                                        null, // Integer limit
                                                                        this.resourceVersion, // String resourceVersion
                                                                        null, // String resourceVersionMatch
                                                                        null, // Integer timeoutSeconds
                                                                        null); // Boolean watch

                this.resourceVersion = eps.getMetadata().getResourceVersion();

                return Single.just(Optional.ofNullable(eps.getItems())
                                           .orElse(List.of())
                                           .stream()
                                           .flatMap(l -> Optional.ofNullable(l.getSubsets())
                                                                 .orElse(List.of())
                                                                 .stream()
                                                                 .flatMap(ss -> Optional.ofNullable(ss.getAddresses())
                                                                                        .orElse(List.of())
                                                                                        .stream()
                                                                                        .map(V1EndpointAddress::getIp)))
                                           .collect(Collectors.toCollection(TreeSet::new)));
            }
            catch (ApiException ex)
            {
                log.error("ApiException", ex);

                if (ex.getCode() == 504 || ex.getCode() == 410)
                {
                    this.resourceVersion = extractResourceVersionFromException(ex);
                }
                else
                {
                    // Reset resource version
                    this.resourceVersion = null;
                }

                return Single.error(ex);
            }
        });
    }
}
