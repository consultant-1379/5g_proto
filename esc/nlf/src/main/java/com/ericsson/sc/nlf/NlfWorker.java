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
 * Created on: Apr 12, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nlf;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.cnal.common.CertificateObserver;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config;
import com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup;
import com.ericsson.cnal.nrf.r17.NrfAdapter;
import com.ericsson.cnal.nrf.r17.NrfAdapter.RequestContext;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.sc.common.alarm.AlarmHandler;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.common.alarm.IfAlarmHandler;
import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.FmAlarmServiceImpl;
import com.ericsson.sc.nrf.r17.Nrf;
import com.ericsson.sc.nrf.r17.Nrf.Pool;
import com.ericsson.sc.pm.ScPmbrConfigHandler;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxLeaderElection;
import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebClientProvider.Builder;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.PatchUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.TimeoutException;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class NlfWorker
{
    public static class NlfDiscovery
    {
        public static class Statistics
        {
            private static final String LN_NAMESPACE = "namespace";
            private static final String LN_OPERATION = "operation";
            private static final String LN_STATUS = "status";

            private static final Statistics singleton = new Statistics();

            private static Statistics singleton()
            {
                return singleton;
            }

            private final io.prometheus.client.Counter ccInReqDisc = MetricRegister.singleton()
                                                                                   .register(io.prometheus.client.Counter.build()
                                                                                                                         .namespace("nlf")
                                                                                                                         .name("in_requests_nnlf_nfdiscovery_total")
                                                                                                                         .labelNames(LN_NAMESPACE, LN_OPERATION)
                                                                                                                         .help("Number of incoming requests on the internal nnlf_nfdiscovery interface")
                                                                                                                         .register());

            private final io.prometheus.client.Counter ccOutAnsDisc = MetricRegister.singleton()
                                                                                    .register(io.prometheus.client.Counter.build()
                                                                                                                          .namespace("nlf")
                                                                                                                          .name("out_answers_nnlf_nfdiscovery_total")
                                                                                                                          .labelNames(LN_NAMESPACE,
                                                                                                                                      LN_OPERATION,
                                                                                                                                      LN_STATUS)
                                                                                                                          .help("Number of outgoing answers on the internal nnlf_nfdiscovery interface")
                                                                                                                          .register());

            private final io.prometheus.client.Counter ccInReqOam = MetricRegister.singleton()
                                                                                  .register(io.prometheus.client.Counter.build()
                                                                                                                        .namespace("nlf")
                                                                                                                        .name("in_requests_nnlf_oam_total")
                                                                                                                        .labelNames(LN_NAMESPACE, LN_OPERATION)
                                                                                                                        .help("Number of incoming requests on the internal nnlf_oam interface")
                                                                                                                        .register());

            private final io.prometheus.client.Counter ccOutAnsOam = MetricRegister.singleton()
                                                                                   .register(io.prometheus.client.Counter.build()
                                                                                                                         .namespace("nlf")
                                                                                                                         .name("out_answers_nnlf_oam_total")
                                                                                                                         .labelNames(LN_NAMESPACE,
                                                                                                                                     LN_OPERATION,
                                                                                                                                     LN_STATUS)
                                                                                                                         .help("Number of outgoing answers on the internal nnlf_oam interface")
                                                                                                                         .register());

            private Statistics()
            {
            }

            public Statistics stepCcInReqDisc(final String namespace,
                                              final String operation)
            {
                this.ccInReqDisc.labels(namespace, operation).inc();
                return this;
            }

            public Statistics stepCcOutAnsDisc(final String namespace,
                                               final String operation,
                                               final int status)
            {
                this.ccOutAnsDisc.labels(namespace, operation, Integer.toString(status)).inc();
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
        }

        private static class CommandConfig extends MonitorAdapter.CommandBase
        {
            private Map<String, AtomicReference<Map<String, NrfPoolWithConfig>>> configs;

            public CommandConfig(final Map<String, AtomicReference<Map<String, NrfPoolWithConfig>>> configs)
            {
                super("config", "Usage: command=config");
                this.configs = configs;
            }

            @Override
            public HttpResponseStatus execute(final Result result,
                                              final Command request)
            {
                result.setAdditionalProperty("config",
                                             this.configs.entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Entry::getKey,
                                                                                   e -> Optional.ofNullable(e.getValue().get())
                                                                                                .orElse(Map.of())
                                                                                                .values()
                                                                                                .stream()
                                                                                                .map(NrfPoolWithConfig::getConfig)
                                                                                                .sorted((l,
                                                                                                         r) -> l.getName().compareTo(r.getName()))
                                                                                                .collect(Collectors.toList()))));

                return HttpResponseStatus.OK;
            }
        }

        private static class NrfPoolWithConfig
        {
            final Nrf.Pool pool;
            final NrfGroup config;

            public NrfPoolWithConfig(final Nrf.Pool pool,
                                     final NrfGroup config)
            {
                this.pool = pool;
                this.config = config;
            }

            public NrfGroup getConfig()
            {
                return this.config;
            }

            public Nrf.Pool getPool()
            {
                return this.pool;
            }
        }

        /**
         * Causes taken from TS 29.500.
         */
        private static final String CAUSE_MANDATORY_QUERY_PARAM_MISSING = "MANDATORY_QUERY_PARAM_MISSING";

        private static final String PAR_NAMESPACE = "namespace";
        private static final String PAR_NRF_GROUP = "nrf-group";

        private static final String OP_CONFIG_UPDATE = "UpdateConfig";
        private static final String OP_NF_INSTANCES_SEARCH = "SearchNFInstances";

        private static final String ERR_UNKNOWN = "UNKNOWN";

        private static Completable forwardExternalProblem(final RoutingContext context,
                                                          final Event event,
                                                          final com.ericsson.cnal.nrf.r17.NrfAdapter.Result<SearchResult> result)
        {
            // Mirror the header as received from the server.
            Optional.ofNullable(result.getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

            Optional.ofNullable(result.getHeader(HD_3GPP_SBI_NF_PEER_INFO)).ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, header));

            log.debug("Response headers: {}", context.response().headers());

            event.setResponse(HttpResponseStatus.valueOf(result.getStatusCode()), result.getBodyAsString());

            if (event.getResponse().getResultDetails() == null)
                return context.response().setStatusCode(event.getResponse().getResultCode()).rxEnd(Buffer.buffer());

            final String contentType = result.getHeader(HD_CONTENT_TYPE);

            if (contentType == null)
                return context.response().setStatusCode(event.getResponse().getResultCode()).rxEnd(event.getResponse().getResultDetails());

            return context.response()
                          .setStatusCode(event.getResponse().getResultCode())
                          .putHeader(HD_CONTENT_TYPE, contentType)
                          .rxEnd(event.getResponse().getResultDetails());
        }

        private static Completable replyWithSearchResult(final RoutingContext context,
                                                         final NrfAdapter.Result<SearchResult> result) throws JsonProcessingException
        {
            // Mirror the header as received from the server.
            Optional.ofNullable(result.getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

            Optional.ofNullable(result.getHeader(HD_3GPP_SBI_NF_PEER_INFO)).ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, header));

            log.debug("Response headers: {}", context.response().headers());

            return context.response()
                          .setStatusCode(HttpResponseStatus.OK.code())
                          .putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_JSON)
                          .rxEnd(json.writeValueAsString(result.getBody()));
        }

        private static Completable replyWithEmptySearchResult(final RoutingContext context) throws JsonProcessingException
        {
            // Mirror the header as received from the client.
            Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

            log.debug("Response headers: {}", context.response().headers());

            return context.response()
                          .setStatusCode(HttpResponseStatus.OK.code())
                          .putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_JSON)
                          .rxEnd(json.writeValueAsString(new SearchResult()));
        }

        private final NlfWorker owner;

        /**
         * The configurations per namespace.
         * <p>
         * Key of the outer map is the namespace.
         * <p>
         * Key of the inner map is the NrfGroup ID.
         */
        private ConcurrentHashMap<String, AtomicReference<Map<Integer, NrfPoolWithConfig>>> configsById = new ConcurrentHashMap<>();

        /**
         * The configurations per namespace.
         * <p>
         * Key of the outer map is the namespace.
         * <p>
         * Key of the inner map is the NrfGroup name.
         */
        private ConcurrentHashMap<String, AtomicReference<Map<String, NrfPoolWithConfig>>> configsByName = new ConcurrentHashMap<>();

        public NlfDiscovery(final NlfWorker owner)
        {
            this.owner = owner;
        }

        public CommandConfig createCommandConfig()
        {
            return new CommandConfig(this.configsByName);
        }

        private Nrf.Configuration convert(final com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Nrf nrf,
                                          final com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup group,
                                          final Nrf.Pool nrfGroup)
        {
            final List<Url> urls = new ArrayList<>();

            nrf.getIpEndpoint().forEach(endpoint ->
            {
                if (endpoint.getIpv4Address() != null)
                    urls.add(new Url(nrf.getScheme().getValue(), nrf.getFqdn(), endpoint.getPort(), "", endpoint.getIpv4Address()));

                if (endpoint.getIpv6Address() != null)
                    urls.add(new Url(nrf.getScheme().getValue(), nrf.getFqdn(), endpoint.getPort(), "", endpoint.getIpv6Address()));
            });

            return new Nrf.Configuration(nrfGroup.getRdn().add("nrf", nrf.getName()), nrf.getPriority(), 0, 0, urls, null, nrf.getSrcSbiNfPeerInfo(), null);
        }

        private Completable handleSearchNfInstances(final RoutingContext context)
        {
            String envNamespace = context.request().getParam(PAR_NAMESPACE);
            final String parNamespace = envNamespace != null && !envNamespace.isEmpty() ? envNamespace : ERR_UNKNOWN;

            final Event event = new Event(OP_NF_INSTANCES_SEARCH, String.class.getName(), context.request().path());
            event.setResponse(HttpResponseStatus.OK, "");

            return Completable.defer(() ->
            {
                final String parNrfGroup = context.request().getHeader(PAR_NRF_GROUP);
                final String query = context.request().query();

                log.debug("Received {} request, namespace='{}', nrfGroup='{}', query='{}'", OP_NF_INSTANCES_SEARCH, parNamespace, parNrfGroup, query);
                log.debug("Request headers: {}", context.request().headers());

                try
                {
                    if (query == null || query.isEmpty())
                        throw new IllegalArgumentException("Invalid or missing mandatory parameter.",
                                                           "requester-nf-type,target-nf-type",
                                                           CAUSE_MANDATORY_QUERY_PARAM_MISSING);

                    if (parNamespace.equals(ERR_UNKNOWN))
                        throw new IllegalArgumentException("Invalid or missing mandatory parameter.", PAR_NAMESPACE, "");

                    if (parNrfGroup == null || parNrfGroup.isEmpty())
                        throw new IllegalArgumentException("Invalid or missing mandatory parameter.", PAR_NRF_GROUP, "");

                    NrfPoolWithConfig nrfPoolWithConfig = null;

                    try
                    {
                        nrfPoolWithConfig = this.configsById.computeIfAbsent(parNamespace, v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                            .get()
                                                            .get(Integer.parseInt(parNrfGroup));
                    }
                    catch (NumberFormatException e)
                    {
                        // Intentionally empty.
                    }

                    if (nrfPoolWithConfig == null)
                        nrfPoolWithConfig = this.configsByName.computeIfAbsent(parNamespace, v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                              .get()
                                                              .get(parNrfGroup);

                    if (nrfPoolWithConfig == null)
                    {
                        if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                            log.warn("NRF group '{}' not found in namespace '{}'.", parNrfGroup, parNamespace);

                        return replyWithEmptySearchResult(context);
                    }

                    final Pool nrfGroup = nrfPoolWithConfig.getPool();

                    if (nrfGroup == null)
                    {
                        if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                            log.warn("NRF group '{}' not found in namespace '{}'.", parNrfGroup, parNamespace);

                        return replyWithEmptySearchResult(context);
                    }

                    return nrfGroup.nfInstancesSearch(RequestContext.of(query)
                                                                    .addHeader(HD_USER_AGENT, context.request().getHeader(HD_USER_AGENT))
                                                                    .addHeader(HD_3GPP_SBI_CORRELATION_INFO,
                                                                               context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO)))
                                   .flatMapCompletable(result ->
                                   {
                                       if (result.hasProblem())
                                           return forwardExternalProblem(context, event, result);

                                       return replyWithSearchResult(context, result);
                                   })
                                   .onErrorResumeNext(e ->
                                   {
                                       if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                                           log.error("Error processing SearchNFInstances request. Cause: {}",
                                                     com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                                       HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                                       String standardCause = null;

                                       if (e instanceof TimeoutException)
                                       {
                                           status = HttpResponseStatus.GATEWAY_TIMEOUT;
                                           standardCause = "";
                                       }

                                       return replyWithError(context,
                                                             event.setResponse(status, "Error processing SearchNFInstances request. Cause: " + e.toString()),
                                                             null,
                                                             standardCause);
                                   });
                }
                catch (final IllegalArgumentException e)
                {
                    if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.error("Error processing SearchNFInstances request. Cause: {}",
                                  com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                    return replyWithError(context,
                                          event.setResponse(HttpResponseStatus.BAD_REQUEST,
                                                            "Error processing SearchNFInstances request. Cause: " + e.toString()),
                                          e.getIllegalArgument(),
                                          e.getStandardCause());
                }
                catch (final Exception e)
                {
                    if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.error("Error processing SearchNFInstances request. Cause: {}",
                                  com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                    return replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing SearchNFInstances request. Cause: " + e.toString()),
                                          null,
                                          null);
                }
            })
                              .onErrorComplete()
                              .doOnComplete(() -> Statistics.singleton()
                                                            .stepCcOutAnsDisc(parNamespace, OP_NF_INSTANCES_SEARCH, event.getResponse().getResultCode()))
                              .doOnSubscribe(d -> Statistics.singleton().stepCcInReqDisc(parNamespace, OP_NF_INSTANCES_SEARCH));
        }

        private Completable handleUpdateConfig(final RoutingContext context)
        {
            String envNamespace = context.request().getParam(PAR_NAMESPACE);
            final String parNamespace = envNamespace != null && !envNamespace.isEmpty() ? envNamespace : ERR_UNKNOWN;

            final Event event = new Event(OP_CONFIG_UPDATE, String.class.getName(), context.request().path());
            event.setResponse(HttpResponseStatus.OK, "");

            return Completable.defer(() ->
            {
                try
                {
                    if (parNamespace.equals(ERR_UNKNOWN))
                        throw new IllegalArgumentException("Invalid or missing mandatory parameter.", PAR_NAMESPACE, "");

                    return Completable.defer(() -> Flowable.fromCallable(() -> Optional.ofNullable(context.body().asString()).orElse(new String("{}")))
                                                           .doOnNext(body -> log.debug("body: {}", body))
                                                           .map(body -> json.readValue(body, Config.class))
                                                           .doOnNext(config -> this.update(parNamespace, config))
                                                           .ignoreElements())
                                      .andThen(context.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).rxEnd(Buffer.buffer()))
                                      .doOnSubscribe(x -> log.debug("Received {} request, namespace={}", OP_CONFIG_UPDATE, parNamespace))
                                      .doOnError(e ->
                                      {
                                          if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                                              log.error("Error processing {} request for namespace {}. Cause: {}",
                                                        OP_CONFIG_UPDATE,
                                                        parNamespace,
                                                        e.toString());
                                      })
                                      .doOnError(e -> replyWithError(context,
                                                                     event.setResponse(HttpResponseStatus.BAD_REQUEST,
                                                                                       "Error processing " + OP_CONFIG_UPDATE + " request for namespace + "
                                                                                                                       + parNamespace + ". Cause: "
                                                                                                                       + e.toString()),
                                                                     null,
                                                                     null));
                }
                catch (final IllegalArgumentException e)
                {
                    if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.error("Error processing UpdateConfig request. Cause: {}",
                                  com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                    return replyWithError(context,
                                          event.setResponse(HttpResponseStatus.BAD_REQUEST,
                                                            "Error processing " + OP_CONFIG_UPDATE + " request. Cause: " + e.toString()),
                                          e.getIllegalArgument(),
                                          e.getStandardCause());
                }
                catch (final Exception e)
                {
                    if (logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.error("Error processing UpdateConfig request. Cause: {}",
                                  com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                    return replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + OP_CONFIG_UPDATE + " request. Cause: " + e.toString()),
                                          null,
                                          null);
                }
            })
                              .onErrorComplete()
                              .doOnComplete(() -> Statistics.singleton().stepCcOutAnsOam(parNamespace, OP_CONFIG_UPDATE, event.getResponse().getResultCode()))
                              .doOnSubscribe(d -> Statistics.singleton().stepCcInReqOam(parNamespace, OP_CONFIG_UPDATE));
        }

        private synchronized void update(final String namespace,
                                         final Config config)
        {
            log.debug("namespace={}, config={}", namespace, config);

            final Set<String> configuredKeys = new HashSet<>();

            final Map<String, NrfPoolWithConfig> activeConfig = this.configsByName.computeIfAbsent(namespace,
                                                                                                   v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                                                                                  .get();

            if (config != null)
            {
                config.getNrfGroup().forEach(group ->
                {
                    log.debug("namespace={}, group={}", namespace, group);

                    configuredKeys.add(group.getName());

                    final NrfPoolWithConfig oldPool = activeConfig.get(group.getName());

                    if (oldPool != null && !oldPool.getConfig().getPath().equals(group.getPath()))
                    {
                        // Limitation as long as Envoy only passes the NRF group name in the header
                        // (without the path to the group in the model): the NRF group name must be
                        // unique within a namespace.
                        // Stop the old pool and create a new one for the same NRF group name but with
                        // different path.

                        final Nrf.Pool pool = oldPool.getPool();
                        pool.publish(Optional.<com.ericsson.sc.nrf.r17.Nrf.Pool.Configuration>empty());
                        pool.stop()
                            .subscribe(() -> MetricRegister.singleton().registerForRemoval(pool.getRdn()),
                                       t -> log.error("Error stopping NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
                    }

                    if (oldPool == null || !oldPool.getConfig().getPath().equals(group.getPath()))
                    {
                        final NrfPoolWithConfig newPool = new NrfPoolWithConfig(new Nrf.Pool(null,
                                                                                             Alarm.Context.of(this.owner.ah, "ScNlf", group.getSource()),
                                                                                             null,
                                                                                             this.owner.certificateObserver.getSecrets(),
                                                                                             Rdn.fromString(group.getPath()).add("nrf-group", group.getName())),
                                                                                group);
                        activeConfig.put(group.getName(), newPool);

                        newPool.getPool().start().subscribe(() ->
                        {
                        }, t -> log.error("Error starting NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
                    }
                });
            }

            final Set<String> groupsNotInConfig = new HashSet<>();

            activeConfig.entrySet().forEach(entry ->
            {
                if (!configuredKeys.contains(entry.getKey()))
                {
                    groupsNotInConfig.add(entry.getKey());
                    final Nrf.Pool pool = entry.getValue().getPool();
                    pool.publish(Optional.<com.ericsson.sc.nrf.r17.Nrf.Pool.Configuration>empty());
                    pool.stop()
                        .subscribe(() -> MetricRegister.singleton().registerForRemoval(pool.getRdn()),
                                   t -> log.error("Error stopping NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
                }
            });

            activeConfig.keySet().removeIf(groupsNotInConfig::contains);

            if (config != null)
            {
                config.getNrfGroup().forEach(group ->
                {
                    if (activeConfig.containsKey(group.getName()))
                    {
                        final List<Nrf.Configuration> allNrfs = new ArrayList<>();

                        group.getNrf().forEach(nrf ->
                        {
                            final Nrf.Configuration convertedNrf = this.convert(nrf, group, activeConfig.get(group.getName()).getPool());

                            if (convertedNrf != null)
                                allNrfs.add(convertedNrf);
                        });

                        log.debug("namespace={}, allNrfs={}", namespace, allNrfs);
                        final Nrf.Pool nrfGroup = activeConfig.get(group.getName()).getPool();
                        nrfGroup.publish(Optional.of(new Nrf.Pool.Configuration(false, true, allNrfs)));
                    }
                });
            }

            this.configsById.computeIfAbsent(namespace, v -> new AtomicReference<>(new ConcurrentHashMap<>()))
                            .set(activeConfig.entrySet()
                                             .stream()
                                             .map(e -> Map.entry(e.getValue().getConfig().getId(), e.getValue()))
                                             .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue)));
        }
    }

    private static class IllegalArgumentException extends java.lang.IllegalArgumentException
    {
        private static final long serialVersionUID = 1L;

        private final String illegalArgument;
        private final String standardCause;

        public IllegalArgumentException(final String message,
                                        final String invalidArgument,
                                        final String standardCause)
        {
            super(message);
            this.illegalArgument = invalidArgument;
            this.standardCause = standardCause;
        }

        String getIllegalArgument()
        {
            return this.illegalArgument;
        }

        String getStandardCause()
        {
            return this.standardCause;
        }
    }

    private static final String DCEDSC_CERTIFICATE = "/run/secrets/dcedsc/certificates";
    private static final String FHAH_CLIENT_CERTIFICATE = "/run/secrets/fhah/certificates";
    private static final String MEDIATOR_CA = "/run/secrets/mediator/ca";
    private static final String MEDIATOR_CERTIFICATE = "/run/secrets/mediator/certificates";
    private static final String NLF_CLIENT_CA = "/run/secrets/client-ca";
    private static final String NLF_SERVER_CERTIFICATE = "/run/secrets/nlf-server-cert";
    private static final String PM_CA = "/run/secrets/pm/ca";
    private static final String SIPTLS_CA = "/run/secrets/siptls/ca";

    private static final String HD_CONTENT_TYPE = "content-type";
    private static final String HD_USER_AGENT = "user-agent";
    private static final String HD_3GPP_SBI_CORRELATION_INFO = "3gpp-sbi-correlation-info";
    private static final String HD_3GPP_SBI_NF_PEER_INFO = "3gpp-sbi-nf-peer-info";

    private static final String VAL_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String VAL_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";

    private static final String ETCD_ENDPOINT = "ETCD_ENDPOINT";
    private static final String ETCD_PASSWORD = "ETCD_PASSWORD";
    private static final String ETCD_USERNAME = "ETCD_USERNAME";
    private static final int ETCD_REQUEST_TIMEOUT = 2;

    private static final String ENV_POD_NAME = "POD_NAME";
    private static final String ENV_NAMESPACE = "NAMESPACE";
    private static final String ENV_LEADER_ELECTION_ENABLED = "LEADER_ELECTION_ENABLED";

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String LE_STATUS_CONTENDER = "eric-sc-nlf-contender";
    private static final String LE_STATUS_LEADER = "eric-sc-nlf-leader";
    private static final String LE_LEADER_KEY = "/ericsson-sc/nlf-leadership";
    private static final int LE_LEADER_TTL = 13;
    private static final int LE_RENEW_INTERVAL = 4;
    private static final int LE_CLAIM_INTERVAL = 3;
    private static final int LE_RECOVERY_DELAY = 12;
    private static final float LE_REQUEST_LATENCY = 0.5f;
    private static final long LE_NUM_RETRIES = 10l;

    private static final String MP_NNLF_DISC_V0 = "/nnlf-disc/v0";
    private static final String MP_NNLF_OAM_V0 = "/nnlf-oam/v0";

    private static final Logger log = LoggerFactory.getLogger(NlfWorker.class);
    private static final LogThrottler logThrottler = new LogThrottler();
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    private static final NlfWorkerInterfacesParameters params = NlfWorkerInterfacesParameters.instance;

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/nlf/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Starting NLF worker, version: {}", VersionInfo.get());
        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            final NlfWorker nlf = new NlfWorker(shutdownHook);
            nlf.run().blockingAwait();
        }
        catch (final Exception t)
        {
            log.error("Exception caught, exiting.", t);
            exitStatus = 1;
        }

        log.info("Stopped NLF worker.");

        System.exit(exitStatus);
    }

    private static Completable replyWithError(final RoutingContext context,
                                              final Event event,
                                              final String invalidParameters,
                                              final String standardCause)
    {
        final ProblemDetails problem = new ProblemDetails();

        problem.setStatus(event.getResponse().getResultCode());
        problem.setCause(standardCause != null ? standardCause : event.getResponse().getResultReasonPhrase());

        if (event.getResponse().getResultDetails() != null)
            problem.setDetail(event.getResponse().getResultDetails());

        for (String invalidParameter : invalidParameters.split(","))
            problem.addInvalidParamsItem(new InvalidParam().param(invalidParameter));

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
            if (400 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 500)
            {
                log.warn(problemStr);
            }
            else if (500 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 600)
            {
                log.error(problemStr);
            }
        }

        // Mirror the header as received from the client.
        Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

        return context.response().setStatusCode(event.getResponse().getResultCode()).putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_PROBLEM_JSON).rxEnd(problemStr);
    }

    private FmAlarmService fmAlarmService;
    private final IfAlarmHandler ah;
    private final CertificateObserver certificateObserver;
    private final RxShutdownHook shutdownHook;
    private final WebClientProvider alarmHandlerClient;
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
    private final NlfDiscovery nlfDiscovery;

    public NlfWorker(RxShutdownHook shutdownHook) throws IOException
    {
        this.certificateObserver = new CertificateObserver("/run/secrets/nlf/certificates");
        this.shutdownHook = shutdownHook;
        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();

        final String localAddress = Utils.getLocalAddress();

        log.info("localAddress={}, portExtOam={}, portExt={}, portInt={}, concurrentStreamsMax={}",
                 localAddress,
                 params.portRestOam,
                 params.portRest,
                 params.portInternal,
                 params.concurrentStreamsMax);

        // create client for fault indications to alarm handler
        final Builder ahClient = WebClientProvider.builder().withHostName(params.serviceHostname);

        if (params.globalTlsEnabled)
            ahClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(FHAH_CLIENT_CERTIFICATE), SipTlsCertWatch.trustedCert(SIPTLS_CA)));

        this.alarmHandlerClient = ahClient.build(VertxInstance.get());

        // create alarm service for updating the alarm through alarm handler service
        this.fmAlarmService = new FmAlarmServiceImpl(new FmAlarmHandler(this.alarmHandlerClient, // web client to be used for alarm raise/cease
                                                                        params.alarmHandlerHostName, // alarm handler service server hostname
                                                                        params.alarmHandlerPort, // alarm handler service server port
                                                                        params.globalTlsEnabled)); // indication if tls is enabled

        this.ah = AlarmHandler.of(this.fmAlarmService);

        this.nlfDiscovery = new NlfDiscovery(this);

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
                                                    .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(NLF_SERVER_CERTIFICATE),
                                                                                                 SipTlsCertWatch.trustedCert(NLF_CLIENT_CA)))
                                                    .build(VertxInstance.get()));

        this.webServerExtOam = this.addRoutesOam(WebServer.builder()
                                                          .withHost(DEFAULT_ROUTE_ADDRESS)
                                                          .withPort(params.portRestOam)
                                                          .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.2"))
                                                          .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.3"))
                                                          .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(NLF_SERVER_CERTIFICATE),
                                                                                                       SipTlsCertWatch.trustedCert(NLF_CLIENT_CA)))
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
            iws.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(NLF_SERVER_CERTIFICATE), // nlf worker server certificate
                                                            // pm server ca for verification of client certificates during scraping of
                                                            // metrics
                                                            SipTlsCertWatch.trustedCert(PM_CA)));
        }

        this.oamWebServer = iws.build(VertxInstance.get());

        PmAdapter.configureMetricsHandler(this.oamWebServer);

        final WebClientProvider.Builder wcb = WebClientProvider.builder().withHostName(params.serviceHostname);

        if (params.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(MEDIATOR_CERTIFICATE), SipTlsCertWatch.trustedCert(MEDIATOR_CA)));

        this.webClientProvider = wcb.build(VertxInstance.get());

        this.cmPatch = new CmmPatch(params.mediatorPort, params.mediatorHostname, this.webClientProvider, params.globalTlsEnabled);
        this.pmbrCfgHandler = new ScPmbrConfigHandler(this.cmPatch);

        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.webServerInt).register(KubeProbe.of().setAlive(true).setReady(true));
        this.disposables = new ArrayList<>();
        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(), this.nlfDiscovery.createCommandConfig()),
                                            Arrays.asList());
        this.leaderElectionEnabled = Boolean.valueOf(EnvVars.get(ENV_LEADER_ELECTION_ENABLED));

        final com.ericsson.sc.rxetcd.RxEtcd.Builder edb = RxEtcd.newBuilder()
                                                                .withEndpoint(EnvVars.get(ETCD_ENDPOINT))
                                                                .withConnectionRetries(10)
                                                                .withRequestTimeout(ETCD_REQUEST_TIMEOUT, TimeUnit.SECONDS);

        if (params.dcedTlsEnabled)
            edb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(DCEDSC_CERTIFICATE), SipTlsCertWatch.trustedCert(SIPTLS_CA)));
        else
            edb.withUser(EnvVars.get(ETCD_USERNAME)).withPassword(EnvVars.get(ETCD_PASSWORD));

        this.rxEtcd = Optional.of(edb.build());

        this.election = this.leaderElectionEnabled ? rxEtcd.map(etcd -> new RxLeaderElection.Builder(etcd,
                                                                                                     ownId,
                                                                                                     LE_LEADER_KEY).leaderInterval(LE_LEADER_TTL)
                                                                                                                   .renewInterval(LE_RENEW_INTERVAL)
                                                                                                                   .claimInterval(LE_CLAIM_INTERVAL)
                                                                                                                   .recoveryDelay(LE_RECOVERY_DELAY)
                                                                                                                   .requestLatency(LE_REQUEST_LATENCY)
                                                                                                                   .retries(LE_NUM_RETRIES)
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

        final Completable nlfWorkerChain = Completable.complete()
                                                      .andThen(rxEtcdConnection) // wait until etcd is up
                                                      .andThen(this.certificateObserver.start())
                                                      .andThen(this.webServerExt.startListener())
                                                      .andThen(this.webServerExtOam.startListener())
                                                      .andThen(this.webServerInt.startListener())
                                                      .andThen(this.oamWebServer.startListener())
                                                      .andThen(this.monitored.start().onErrorComplete())
                                                      .andThen(MetricRegister.singleton().start())
                                                      .andThen(this.ah.start())
                                                      .andThen(waitUntilLeader)// Wait until we are leader
                                                      .andThen(this.leaderElectionEnabled ? this.updatePodLabel(LeaderStatus.LEADER) : Completable.complete())
                                                      .andThen(this.pmbrCfgHandler.createPmbrJobPatches())
                                                      .andThen(this.pmbrCfgHandler.createPmbrGroupPatches())
                                                      .andThen(Completable.ambArray(this.shutdownHook.get(), leadershipLost))
                                                      .onErrorResumeNext(e -> this.stop().andThen(Completable.error(e)))
                                                      .andThen(this.stop());

        return (this.leaderElectionEnabled ? election.map(RxLeaderElection::run).orElse(Completable.never()) : Completable.never()).ambWith(nlfWorkerChain); // Start
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
                          .andThen(MetricRegister.singleton().stop().onErrorComplete(logErr))
                          .andThen(this.ah.stop().onErrorComplete(logErr))
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.certificateObserver.stop().onErrorComplete(logErr))
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

        router.get("/nf-instances/:namespace").handler(BodyHandler.create());
        router.get("/nf-instances/:namespace").handler(ctx -> this.nlfDiscovery.handleSearchNfInstances(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        server.mountRouter(MP_NNLF_DISC_V0, router);

        return server;
    }

    private WebServer addRoutesOam(final WebServer server)
    {
        final Router router = Router.router(server.getVertx());

        router.put("/config/:namespace").handler(BodyHandler.create());
        router.put("/config/:namespace").handler(ctx -> this.nlfDiscovery.handleUpdateConfig(ctx).subscribe(() ->
        {
        }, e ->
        {
            // Do not log error on traffic path.
        }));

        server.mountRouter(MP_NNLF_OAM_V0, router);

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
        final String status = leaderStatus == LeaderStatus.LEADER ? LE_STATUS_LEADER : LE_STATUS_CONTENDER;

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
