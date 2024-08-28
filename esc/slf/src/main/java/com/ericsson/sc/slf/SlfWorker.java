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

package com.ericsson.sc.slf;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandTestAlarm;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Instance;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.cnal.common.CertificateObserver;
import com.ericsson.cnal.nrf.r17.NnrfDiscSearchResultDb2;
import com.ericsson.cnal.nrf.r17.NnrfDiscSearchResultDb2.Item;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.nrf.r17.NrfAdapter.RequestContext;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.sc.common.alarm.AlarmHandler;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.common.alarm.IfAlarmHandler;
import com.ericsson.sc.common.alarm.UnresolvableHostsAlarmHandler;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.FmAlarmServiceImpl;
import com.ericsson.sc.nrf.r17.ConfigComparators;
import com.ericsson.sc.nrf.r17.Nrf;
import com.ericsson.sc.nrf.r17.Nrf.Pool;
import com.ericsson.sc.nrf.r17.NrfDnsCache;
import com.ericsson.sc.pm.ScPmbrConfigHandler;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxLeaderElection;
import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.scp.model.DnsProfile;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.glue.NfFunction;
import com.ericsson.sc.scp.model.glue.NfInstance;
import com.ericsson.sc.slf.model.nslf_discovery.Address;
import com.ericsson.sc.slf.model.nslf_discovery.SearchResult;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.utilities.common.CacheSweeper;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.http.openapi.OpenApiServer;
import com.ericsson.utilities.http.openapi.OpenApiServer.IfApiHandler;
import com.ericsson.utilities.http.openapi.OpenApiServer.IpFamily;
import com.ericsson.utilities.http.openapi.OpenApiTask;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.json.Json.Patch;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;

public class SlfWorker implements MonitorAdapter.CommandCounter.Provider
{
    public static class SlfDiscovery extends ApiHandler
    {
        public enum Operation
        {
            ADDRESSES_SEARCH("SearchAddresses");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        public static class Statistics
        {
            private static final String LN_OPERATION = "operation";
            private static final String LN_STATUS = "status";

            private static final Statistics singleton = new Statistics();

            private static Statistics singleton()
            {
                return singleton;
            }

            private final io.prometheus.client.Counter ccInReqDisc = MetricRegister.singleton()
                                                                                   .register(io.prometheus.client.Counter.build()
                                                                                                                         .namespace("slf")
                                                                                                                         .name("in_requests_nslf_nfdiscovery_total")
                                                                                                                         .labelNames(LN_OPERATION)
                                                                                                                         .help("Number of incoming requests on the internal nslf_nfdiscovery interface")
                                                                                                                         .register());

            private final io.prometheus.client.Counter ccOutAnsDisc = MetricRegister.singleton()
                                                                                    .register(io.prometheus.client.Counter.build()
                                                                                                                          .namespace("slf")
                                                                                                                          .name("out_answers_nslf_nfdiscovery_total")
                                                                                                                          .labelNames(LN_OPERATION, LN_STATUS)
                                                                                                                          .help("Number of outgoing answers on the internal nslf_nfdiscovery interface")
                                                                                                                          .register());

            private Statistics()
            {
            }

            public Statistics stepCcInReqDisc(final String operation)
            {
                this.ccInReqDisc.labels(operation).inc();
                return this;
            }

            public Statistics stepCcOutAnsDisc(final String operation,
                                               final int status)
            {
                this.ccOutAnsDisc.labels(operation, Integer.toString(status)).inc();
                return this;
            }
        }

        private static String getGpsi(final RoutingContext params)
        {
            String result = null;

            final String param = params.request().getParam("gpsi");

            if (param != null)
            {
                result = param;

                final String expression = "^(msisdn-[0-9]{5,15})$";

                if (!result.matches(expression))
                    throw new IllegalArgumentException("Parameter '" + result + "' does not match expression '" + expression + "'.", "gpsi");
            }

            return result;
        }

        private static int getLimit(final RoutingContext params)
        {
            final String paramLimit = params.request().getParam("limit");

            Integer limit = -2;

            if (paramLimit != null)
            {
                try
                {
                    limit = Integer.parseInt(paramLimit);
                }
                catch (NumberFormatException e)
                {
                    limit = -1;
                }
            }

            return limit; // limit < 0 means all
        }

        private static String getRequesterNfType(final RoutingContext params)
        {
            return params.request().getParam("requester-nf-type");
        }

        private static ArrayList<String> getServiceNames(final RoutingContext params)
        {
            ArrayList<String> result = null;

            final String param = params.request().getParam("service-names");

            if (param != null)
            {
                result = new ArrayList<>();

                for (String p : param.split(","))
                    result.add(p);
            }

            return result;
        }

        private static String getSupi(final RoutingContext params)
        {
            String result = null;

            final String param = params.request().getParam("supi");

            if (param != null)
            {
                result = param;

                final String expression = "^(imsi-[0-9]{5,15})$";

                if (!result.matches(expression))
                    throw new IllegalArgumentException("Parameter '" + result + "' does not match expression '" + expression + "'.", "supi");
            }

            return result;
        }

        private static String getTargetNfType(final RoutingContext params)
        {
            return params.request().getParam("target-nf-type");
        }

        private BiConsumer<RoutingContext, Event> handleSearchAddresses = (context,
                                                                           event) ->
        {
            Statistics.singleton().stepCcInReqDisc(Operation.ADDRESSES_SEARCH.value());

            Completable action = Completable.complete();
            String requesterNfType = null;

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Received SearchAddresses request, query={}", context.request().query());

//                this.owner.getNfInstance(null).getHistoryOfEvents().put(event);

                requesterNfType = getRequesterNfType(context);
                getTargetNfType(context);
                final int limit = getLimit(context);
                final int numQueryParams = 2 // Mandatory: requester-nf-type, target-nf-type
                                           + (limit > -2 ? 1 : 0) + (getServiceNames(context) != null ? 1 : 0) + (getGpsi(context) != null ? 1 : 0)
                                           + (getSupi(context) != null ? 1 : 0);

                if (log.isDebugEnabled())
                    log.debug("queryParams().size()={}, numQueryParams={}", context.queryParams().names().size(), numQueryParams);

                if (context.queryParams().names().size() > numQueryParams)
                {
                    throw new IllegalArgumentException("Unsupported query. Supported optional parameters are: gpsi, limit, service-names, supi",
                                                       context.request().query());
                }

                final NfInstance nfInstance = this.owner.scpFunction.getNfInstance(0);

                if (nfInstance == null)
                {
                    if (this.owner.logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.warn("Configuration 'ericsson-scp': no nf-instance in ericsson-scp:scp-function.");

                    action = this.replyWithSearchResult(context, event, limit, null);
                    return;
                }

                final Pool nrfGroup = this.owner.nrfGroupForDiscovery.apply(context.request().getHeader(PAR_NRF_GROUP))
                                                                     .orElse(nfInstance.getNrfGroupForNfDiscovery());

                if (nrfGroup == null)
                {
                    if (this.owner.logThrottler.loggingIsDue(log.isDebugEnabled()))
                        log.warn("Configuration 'ericsson-scp': no nrf-group has been referenced for service Nnrf_NFDiscovery in nrf-service/nf-discovery nor in the slf-lookup-profile.");

                    action = this.replyWithSearchResult(context, event, limit, null);
                    return;
                }

                final Query.Builder queryBuilder = new Query.Builder(context).remove("limit");

                final Query query = queryBuilder.build();
                final Item cachedResult;

                cachedResult = this.owner.cache.get(nrfGroup.getRdn().value(), query);

                if (cachedResult != null && cachedResult.isValid())
                {
                    log.debug("Returning cached result.");
                    action = this.replyWithSearchResult(context, event, limit, cachedResult.getData());
                }
                else
                {
                    action = nrfGroup.nfInstancesSearch(RequestContext.of(query.toString())
                                                                      .addHeader(HD_USER_AGENT, context.request().getHeader(HD_USER_AGENT))
                                                                      .addHeader(HD_3GPP_SBI_CORRELATION_INFO,
                                                                                 context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO)))
                                     .flatMapCompletable(result ->
                                     {
                                         if (result.hasProblem())
                                             return this.forwardExternalProblem(context, event, result);

                                         com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult data = result.getBody();

                                         if (!data.getNfInstances().isEmpty()) // Only cache results with contents.
                                             this.owner.cache.put(nrfGroup.getRdn().value(), query, data);

                                         return this.replyWithSearchResult(context, event, limit, data);
                                     })
                                     .onErrorResumeNext(e ->
                                     {
                                         if (this.owner.logThrottler.loggingIsDue(log.isDebugEnabled()))
                                             log.error("Error processing SearchAddresses request. Cause: {}",
                                                       com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                                         return this.owner.replyWithError(context,
                                                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                                                            "Error processing SearchAddresses request. Cause: " + e.toString()),
                                                                          null,
                                                                          null,
                                                                          null);
                                     });
                }
            }
            catch (final IllegalArgumentException e)
            {
                if (this.owner.logThrottler.loggingIsDue(log.isDebugEnabled()))
                    log.error("Error processing SearchAddresses request. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                action = this.owner.replyWithError(context,
                                                   event.setResponse(HttpResponseStatus.BAD_REQUEST,
                                                                     "Error processing SearchAddresses request. Cause: " + e.toString()),
                                                   null,
                                                   null,
                                                   e.getIllegalArgument());
            }
            catch (final Exception e)
            {
                if (this.owner.logThrottler.loggingIsDue(log.isDebugEnabled()))
                    log.error("Error processing SearchAddresses request. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));

                action = this.owner.replyWithError(context,
                                                   event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                                     "Error processing SearchAddresses request. Cause: " + e.toString()),
                                                   null,
                                                   requesterNfType,
                                                   null);
            }
            finally
            {
                action.subscribeOn(Schedulers.io()).subscribe(() ->
                {
                    Statistics.singleton().stepCcOutAnsDisc(Operation.ADDRESSES_SEARCH.value(), event.getResponse().getResultCode());
                }, t ->
                {
                    // Do not log error on traffic path.
                });
            }
        };

        public SlfDiscovery(final SlfWorker owner)
        {
            super(owner);

            this.getHandlerByOperationId().put(Operation.ADDRESSES_SEARCH.value, this.handleSearchAddresses);
        }

        private Completable forwardExternalProblem(final RoutingContext context,
                                                   final Event event,
                                                   com.ericsson.cnal.nrf.r17.NrfAdapter.Result<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult> result)
        {
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

        private Completable replyWithSearchResult(final RoutingContext context,
                                                  final Event event,
                                                  final int limit,
                                                  com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult nrfResult) throws JsonProcessingException
        {

            final SortedMap<Integer, List<Address>> addressesByPriority = new TreeMap<>();

            if (nrfResult != null && nrfResult.getNfInstances() != null)
            {
                AtomicInteger lowestPrio = new AtomicInteger(0);

                nrfResult.getNfInstances().forEach(profile ->
                {
                    final Integer priority = profile.getPriority();

                    if (priority != null && lowestPrio.get() < priority)
                        lowestPrio.set(priority);
                });

                nrfResult.getNfInstances().forEach(profile ->
                {
                    final Address address = new Address();
                    address.setIpv4Addresses(profile.getIpv4Addresses());
                    address.setIpv6Addresses(profile.getIpv6Addresses());
                    address.setFqdn(profile.getFqdn());

                    Integer priority = profile.getPriority();

                    if (priority == null)
                        priority = lowestPrio.incrementAndGet();

                    address.setPriority(priority);
                    addressesByPriority.computeIfAbsent(priority, v -> new ArrayList<>()).add(address);
                });
            }

            final SearchResult slfResult = new SearchResult();

            Iterator<List<Address>> iterator = addressesByPriority.values().iterator();

            for (int i = 0; (i < limit || limit < 0) && iterator.hasNext();)
            {
                final List<Address> addresses = iterator.next();

                for (int j = 0; (i < limit || limit < 0) && j < addresses.size(); ++i, ++j)
                    slfResult.addAddressesItem(addresses.get(j));
            }

            return context.response()
                          .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                          .putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_JSON)
                          .rxEnd(json.writeValueAsString(slfResult));
        }
    }

    private static abstract class ApiHandler implements IfApiHandler
    {
        protected final SlfWorker owner;
        protected final Map<String, BiConsumer<RoutingContext, Event>> handlerByOperationId;

        protected ApiHandler(final SlfWorker owner)
        {
            this.owner = owner;
            this.handlerByOperationId = new TreeMap<>();
        }

        public Map<String, BiConsumer<RoutingContext, Event>> getHandlerByOperationId()
        {
            return this.handlerByOperationId;
        }

        public void handle(final RoutingContext context)
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final Event event = new Event(operationId, String.class.getName(), context.request().path());
            final BiConsumer<RoutingContext, Event> handler = context.get(OpenApiTask.DataIndex.HANDLER.name());

            log.debug("Request headers: {}", context.request().headers());

            handler.accept(context, event);

            log.debug("Response headers: {}", context.request().headers());
        }
    }

    private static class CommandCache extends MonitorAdapter.CommandBase
    {
        private Cache cache;

        public CommandCache(final Cache cache)
        {
            super("cache", "Usage: command=cache[&clear[=<true|false>]]");
            this.cache = cache;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            if (Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear")))
                this.cache.clear();

            result.setAdditionalProperty("cache", this.cache);

            return HttpResponseStatus.OK;
        }
    }

    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private final BehaviorSubject<Optional<EricssonScp>> config;

        public CommandConfig(final BehaviorSubject<Optional<EricssonScp>> config)
        {
            super("config", "Usage: command=config");
            this.config = config;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            this.config.getValue().ifPresent(cfg -> result.setAdditionalProperty("config", cfg));
            return HttpResponseStatus.OK;
        }
    }

    private static class ConfigContext extends ConfigComparators.ChangeFlags
    {
        public static ConfigContext empty()
        {
            return new ConfigContext(Optional.empty(), Optional.of(List.of()));
        }

        public static ConfigContext of(final Optional<EricssonScp> config)
        {
            return new ConfigContext(config, Optional.empty());
        }

        public static ConfigContext of(final Optional<EricssonScp> config,
                                       final Optional<List<Json.Patch>> diff)
        {
            return new ConfigContext(config, diff);
        }

        private final Optional<EricssonScp> config;

        private ConfigContext(final Optional<EricssonScp> config,
                              final Optional<List<Json.Patch>> diff)
        {
            super(diff, Flags.F_ALL, Flags.F_NNRF_NFM, Flags.F_NNRF_NFM_NRF_GROUP_INST_ID);

            this.config = config;
        }

        public Optional<EricssonScp> getConfig()
        {
            return this.config;
        }

        public boolean isChangedAll()
        {
            return this.changeFlags.get(Flags.F_ALL);
        }

        public boolean isChangedNnrfNfm()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM);
        }

        public boolean isChangedNnrfNfmNrfGroupInstId()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM_NRF_GROUP_INST_ID);
        }
    }

    private static class IllegalArgumentException extends java.lang.IllegalArgumentException
    {
        private static final long serialVersionUID = 1L;

        private final String illegalArgument;

        public IllegalArgumentException(final String message,
                                        final String invalidArgument)
        {
            super(message);
            this.illegalArgument = invalidArgument;
        }

        String getIllegalArgument()
        {
            return this.illegalArgument;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "sizeMax", "sweeper", "statistics", "db" })
    static class Cache
    {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "hitRatio", "size", "gets", "puts", "rems", "getLatency", "putLatency" })
        public static class Statistics
        {
            @JsonProperty("gets")
            public Count numberOfGets;

            @JsonProperty("puts")
            public Count numberOfPuts;

            @JsonIgnore
            public Count numberOfHits;

            @JsonProperty("rems")
            public Count numberOfRems;

            @JsonProperty("hitRatio")
            public AtomicDouble hitRatio;

            @JsonProperty("size")
            public AtomicLong size;

            // @JsonProperty("getLatency")
            // public MovingAverage getLatency;

            // @JsonProperty("putLatency")
            // public MovingAverage putLatency;

            @JsonIgnore
            private final String id;

            public Statistics(final String id)
            {
                this.numberOfGets = new Count();
                this.numberOfPuts = new Count();
                this.numberOfHits = new Count();
                this.numberOfRems = new Count();
                this.size = new AtomicLong(0l);
                this.hitRatio = new AtomicDouble(0d);
                // this.getLatency = new MovingAverage(1000, "ms");
                // this.putLatency = new MovingAverage(1000, "ms");
                this.id = id;
            }

            @JsonIgnore
            public synchronized void clear()
            {
                this.numberOfGets.clear();
                this.numberOfPuts.clear();
                this.numberOfHits.clear();
                this.numberOfRems.clear();
                this.size.set(0l);
                this.hitRatio.set(0d);
                // this.getLatency.clear();
                // this.putLatency.clear();
            }

            @JsonIgnore
            public String getId()
            {
                return this.id;
            }

            @JsonIgnore
            public Statistics setHitRatio()
            {
                long numGets = this.numberOfGets.get();
                this.hitRatio.set(numGets > 0 ? ((double) this.numberOfHits.get() / (double) numGets) : 0d);
                return this;
            }

            @JsonIgnore
            public Statistics setSize(final long size)
            {
                this.size.set(size);
                return this;
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

        @JsonIgnore
        private final List<Disposable> disposables = new ArrayList<>();

        @JsonIgnore
        private final Map<Item, Item> index;

        @JsonProperty("sizeMax")
        private final int sizeMax;

        @JsonIgnore
//        @JsonProperty("db")
        private final NnrfDiscSearchResultDb2 db;

        @JsonIgnore
        private final Statistics counts;

        @JsonProperty("sweeper")
        private final CacheSweeper<Item, Item> sweeper;

        public Cache(int sizeMax,
                     int sweepItemsMax,
                     int sweepPeriodMillis)
        {
            this.index = new ConcurrentHashMap<>();
            this.sizeMax = sizeMax > 0 ? sizeMax : 2;
            this.db = new NnrfDiscSearchResultDb2();
            this.counts = new Statistics("searchResults");
            this.sweeper = new CacheSweeper<>("Cache of search results",
                                              this.index,
                                              this.sizeMax,
                                              sweepItemsMax > 0 ? sweepItemsMax : 2,
                                              sweepPeriodMillis > 0 ? sweepPeriodMillis : 1000,
                                              Item::isValid,
                                              (k,
                                               v) ->
                                              {
                                                  this.counts.numberOfRems.inc();
                                                  this.db.remove(v);
                                              });

//            Flowable.interval(5, TimeUnit.SECONDS).doOnNext(x -> log.info("cache={}", this)).subscribe();
        }

        @JsonIgnore
        public void clear()
        {
            this.index.clear();
            this.db.clear();
            this.sweeper.reset();
        }

        @JsonIgnore
        public Item get(final String nrfGroup,
                        final Query query)
        {
            this.counts.numberOfGets.inc();

//            final Instant start = Instant.now();

            final Item itemFound = this.db.get(nrfGroup, query)
                                          .stream()
                                          .reduce(null,
                                                  (result,
                                                   item) -> item.getNewer(result));

//            final Duration latency = Duration.between(start, Instant.now());
//            this.counts.getLatency.add(latency.getSeconds() * 1000d + latency.getNano() / 1000000d);

            if (itemFound != null)
                this.counts.numberOfHits.inc();

            return itemFound;
        }

        @JsonProperty("statistics")
        public Statistics getStatistics()
        {
            return this.counts.setHitRatio().setSize(this.index.size());
        }

        @JsonIgnore
        public void put(final Item item)
        {
//            final Instant start = Instant.now();

            final Item replacedItem = this.index.put(item, item);
            this.counts.numberOfPuts.inc();

            // Remove old item as searchResults.add(item) does not overwrite identical
            // items.
            if (replacedItem != null)
                this.db.remove(replacedItem);

            this.db.add(item);

//            final Duration latency = Duration.between(start, Instant.now());
//            this.counts.putLatency.add(latency.getSeconds() * 1000d + latency.getNano() / 1000000d);
        }

        @JsonIgnore
        public void put(final String nrfGroup,
                        final Query query,
                        final com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult data)
        {
            final Item newResult = new Item(nrfGroup, query.getParamNames(), data);
            this.put(newResult);
        }

        public Completable start()
        {
            return this.sweeper.start();
        }

        public Completable stop()
        {
            return this.sweeper.stop()//
                               .andThen(Completable.fromAction(() ->
                               {
                                   this.disposables.forEach(Disposable::dispose);
                                   this.disposables.clear();
                               }));
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

    private static final String PAR_NRF_GROUP = "nrf-group";

    private static final int CACHE_SIZE_MAX = 5;
    private static final int CACHE_SWEEP_ITEMS_MAX = 2;
    private static final int CACHE_SWEEP_PERIOD_MILLIS = 2000;

    private static final String ENV_CACHE_SWEEP_PERIOD_MS = "CACHE_SWEEP_PERIOD_MS";
    private static final String ENV_CACHE_SIZE_MAX = "CACHE_SIZE_MAX";
    private static final String ENV_CACHE_SWEEP_ITEMS_MAX = "CACHE_SWEEP_ITEMS_MAX";
    private static final String ENV_CM_MEDIATOR = "CM_MEDIATOR";
    private static final String ENV_CONCURRENT_STREAMS_MAX = "CONCURRENT_STREAMS_MAX";
    private static final String ENV_INTERNAL_PORT = "INTERNAL_PORT";
    private static final String ENV_SERVICE_PORT_TLS = "SERVICE_PORT_TLS";

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String ETCD_ENDPOINT = "ETCD_ENDPOINT";
    private static final String ETCD_PASSWORD = "ETCD_PASSWORD";
    private static final String ETCD_USERNAME = "ETCD_USERNAME";
    private static final int ETCD_REQUEST_TIMEOUT = 2;

    private static final String ENV_POD_NAME = "POD_NAME";
    private static final String ENV_NAMESPACE = "NAMESPACE";
    private static final String ENV_LEADER_ELECTION_ENABLED = "LEADER_ELECTION_ENABLED";

    private static final String LE_STATUS_CONTENDER = "eric-sc-slf-contender";
    private static final String LE_STATUS_LEADER = "eric-sc-slf-leader";
    private static final String LE_LEADER_KEY = "/ericsson-sc/slf-leadership";
    private static final int LE_LEADER_TTL = 13;
    private static final int LE_RENEW_INTERVAL = 4;
    private static final int LE_CLAIM_INTERVAL = 3;
    private static final int LE_RECOVERY_DELAY = 12;
    private static final float LE_REQUEST_LATENCY = 0.5f;
    private static final long LE_NUM_RETRIES = 10l;

    private static final String HD_CONTENT_TYPE = "content-type";
    private static final String HD_USER_AGENT = "user-agent";
    private static final String HD_3GPP_SBI_CORRELATION_INFO = "3gpp-sbi-correlation-info";

    private static final String VAL_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String VAL_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";

    private static final String HOSTNAME = "HOSTNAME";
    private static final String PATH_NSLF_NF_DISCOVERY_YAML = "3gpp/Nslf_NfDiscovery.yaml";
    private static final String SCHEMA_ERICSSON_SCP = "ericsson-scp";

    private static final Logger log = LoggerFactory.getLogger(SlfWorker.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse

    private static final SlfWorkerInterfacesParameters params = SlfWorkerInterfacesParameters.instance;

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/slf/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Starting SLF worker, version: {}", VersionInfo.get());

        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            final SlfWorker slf = new SlfWorker(shutdownHook);
            slf.run().blockingAwait();
        }
        catch (final Exception t)
        {
            log.error("Exception caught, exiting.", t);
            exitStatus = 1;
        }

        log.info("Stopped SLF worker.");

        System.exit(exitStatus);
    }

    private final RxShutdownHook shutdownHook;
    private final CertificateObserver certificateObserver;
    private final FmAlarmService fmAlarmService;
    private final WebClientProvider alarmHandlerClient;
    private final IfAlarmHandler ah;
    private final UnresolvableHostsAlarmHandler unresolvableHostsAh;
    private final WebServer webServerExtTls;
    private final WebServer probeWebServer;
    private final RouterHandler oamWebServer;
    @SuppressWarnings("unused")
    private final KubeProbe kubeProbe;
    private final CmAdapter<EricssonScp> cm;
    private final BehaviorSubject<ConfigContext> configFlow;
    private final NfFunction scpFunction;
    private final List<Disposable> disposables;
    private final LoadMeter loadMeter;
    private final Cache cache;
    private final MonitorAdapter monitored;
    private final LogThrottler logThrottler;
    private final boolean leaderElectionEnabled;
    private final Optional<RxEtcd> rxEtcd;
    private final Optional<RxLeaderElection> election;
    private final ApiClient client;
    private final String ownId;
    private final CoreV1Api api;
    private final ScPmbrConfigHandler pmbrCfgHandler;
    private final CmmPatch cmPatch;
    private final Function<String, Optional<Nrf.Pool>> nrfGroupForDiscovery;

    private final WebClientProvider webClientProvider;
    private final Optional<TlsKeylogger> tlsKeyLogger;

    public SlfWorker(RxShutdownHook shutdownHook) throws IOException
    {
        this.certificateObserver = new CertificateObserver("/run/secrets/slf/certificates");
        this.shutdownHook = shutdownHook;
        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();

        final var wcb = WebClientProvider.builder().withHostName(params.serviceHostname);
        if (params.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.slfMediatorClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        this.webClientProvider = wcb.build(VertxInstance.get());

        // create client for fault indications to alarm handler
        final var ahClient = WebClientProvider.builder().withHostName(params.serviceHostname);
        if (params.globalTlsEnabled)
            ahClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.alarmHandlerClientCertPath), //
                                                                 SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        this.alarmHandlerClient = ahClient.build(VertxInstance.get());

        // create alarm handler for requests to alarm handler service
        var fmAlarmHandler = new FmAlarmHandler(this.alarmHandlerClient, // web client to be used for alarm raise/cease
                                                params.alarmHandlerHostName, // alarm handler service server hostname
                                                params.alarmHandlerPort, // alarm handler service server port
                                                params.globalTlsEnabled); // indication if tls is enabled

        // create alarm service for updating the alarm through alarm handler service
        this.fmAlarmService = new FmAlarmServiceImpl(fmAlarmHandler);
        this.ah = AlarmHandler.of(this.fmAlarmService);

        this.ownId = EnvVars.get(ENV_POD_NAME);

        this.client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(this.client);
        this.api = new CoreV1Api();

        this.cmPatch = new CmmPatch(params.mediatorPort, //
                                    EnvVars.get(ENV_CM_MEDIATOR), //
                                    this.webClientProvider, //
                                    params.globalTlsEnabled);

        this.cm = new CmAdapter<>(EricssonScp.class,
                                  params.schemaName,
                                  VertxInstance.get(),
                                  params.mediatorPort,
                                  EnvVars.get(ENV_CM_MEDIATOR),
                                  this.webClientProvider,
                                  params.globalTlsEnabled,
                                  params.subscribeValidity,
                                  params.subscribeRenewal,
                                  params.subscribeHeartbeat);

        this.configFlow = BehaviorSubject.<ConfigContext>create();
        this.cm.getNotificationHandler() //
               .getConfiguration() //
               .subscribeOn(Schedulers.io())
               .scan(ConfigContext.empty(),
                     (prev,
                      curr) ->
                     {
                         log.info("Processing config update.");

                         if (prev.getConfig().isPresent() && curr.isPresent())
                         {
                             final List<Patch> diffs = Json.diff(prev.getConfig().get(), curr.get());
                             log.debug("#diffs={}, diffs={}", diffs.size(), diffs);
                             return ConfigContext.of(curr, Optional.of(diffs));
                         }

                         if (prev.getConfig().isEmpty() && curr.isEmpty())
                             return ConfigContext.empty();

                         return ConfigContext.of(curr);
                     })
               .filter(ConfigContext::isChangedAll)
               .subscribe(this.configFlow);

        final String localAddress = Utils.getLocalAddress();
        final int portExtTls = (int) Float.parseFloat(EnvVars.get(ENV_SERVICE_PORT_TLS, 8443));
        // TODO: Use params.k8sProbeIfPort instead?
        final int portInt = (int) Float.parseFloat(EnvVars.get(ENV_INTERNAL_PORT, 8081));
        final int concurrentStreamsMax = (int) Float.parseFloat(EnvVars.get(ENV_CONCURRENT_STREAMS_MAX, 1000));
        final int cacheSizeMax = (int) Float.parseFloat(EnvVars.get(ENV_CACHE_SIZE_MAX, CACHE_SIZE_MAX));
        final int cacheSweepMax = (int) Float.parseFloat(EnvVars.get(ENV_CACHE_SWEEP_ITEMS_MAX, CACHE_SWEEP_ITEMS_MAX));
        final int cacheSweepPeriodMillis = (int) Float.parseFloat(EnvVars.get(ENV_CACHE_SWEEP_PERIOD_MS, CACHE_SWEEP_PERIOD_MILLIS));

        log.info("localAddress={}, portExtTls={}, portInt={}, concurrentStreamsMax={}, cacheSizeMax={}, cacheSweepMax={}, cacheSweepPeriodMillis={}",
                 localAddress,
                 portExtTls,
                 portInt,
                 concurrentStreamsMax,
                 cacheSizeMax,
                 cacheSweepMax,
                 cacheSweepPeriodMillis);

        this.webServerExtTls = WebServer.builder()
                                        .withHost(DEFAULT_ROUTE_ADDRESS)
                                        .withPort(portExtTls)
                                        .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(concurrentStreamsMax))
                                        .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.2"))
                                        .withOptions(options -> options.addEnabledSecureTransportProtocol("TLSv1.3"))
                                        .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.oamServerCertPath), // slf worker server
                                                                                                                                        // certificate
                                                                                     SipTlsCertWatch.combine(SipTlsCertWatch.trustedCert(params.workerIfClientCaPath))))
                                        .build(VertxInstance.get());

        final SlfDiscovery handlerSlfDiscovery = new SlfDiscovery(this);
        final OpenApiServer.Context3 contextNfDiscovery = new OpenApiServer.Context3(PATH_NSLF_NF_DISCOVERY_YAML, handlerSlfDiscovery);

        new OpenApiServer(this.webServerExtTls).configure2(IpFamily.of(this.webServerExtTls.getHttpOptions().getHost()), List.of(contextNfDiscovery));

        this.probeWebServer = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(portInt).build(VertxInstance.get());

        // create web server for mediator notifications and pm-server metrics scraping
        final WebServerBuilder iws = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(params.oamServerPort);

        if (params.globalTlsEnabled)
        {
            iws.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.oamServerCertPath), // slf worker server certificate
                                                            SipTlsCertWatch.combine(// mediator server ca for verification of client certificates during
                                                                                    // notifications
                                                                                    SipTlsCertWatch.trustedCert(params.mediatorServerCaPath),
                                                                                    // pm server ca for verification of client certificates during scraping of
                                                                                    // metrics
                                                                                    SipTlsCertWatch.trustedCert(params.pmServerCaPath))));
        }

        this.oamWebServer = iws.build(VertxInstance.get());

        PmAdapter.configureMetricsHandler(this.oamWebServer);

        this.pmbrCfgHandler = new ScPmbrConfigHandler(this.cmPatch);
        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.probeWebServer).register(KubeProbe.of().setAlive(true).setReady(true));
        this.loadMeter = new LoadMeter(VertxInstance.get(), this.cm.getNotificationHandler().getConfiguration());

        Alarm.Context alarmCtx = Alarm.Context.of(ah, "ScpSlf", SCHEMA_ERICSSON_SCP);

        this.unresolvableHostsAh = new UnresolvableHostsAlarmHandler(alarmCtx);

        this.scpFunction = new NfFunction(alarmCtx, this.loadMeter, this.certificateObserver.getSecrets(), new Rdn("nf", "scp-function"));
        this.nrfGroupForDiscovery = nrfGroup ->
        {
            final Optional<Pool> o = Optional.ofNullable(nrfGroup)
                                             .map(group -> Optional.ofNullable(this.scpFunction.getNfInstance(0)).map(i -> i.getNrfGroups().get(group)))
                                             .orElse(Optional.empty());
            log.debug("nrfGroup={}, pool={}", nrfGroup, o);
            return o;
        };

        this.disposables = new ArrayList<>();
        this.cache = new Cache(cacheSizeMax, cacheSweepMax, cacheSweepPeriodMillis);
        this.monitored = new MonitorAdapter(this.probeWebServer,
                                            Arrays.asList(new CommandCache(this.cache),
                                                          new CommandConfig(this.cm.getNotificationHandler().getConfiguration()),
                                                          new MonitorAdapter.CommandCounter(this)),
                                            Arrays.asList(new CommandCache(this.cache),
                                                          new CommandTestAlarm(SCHEMA_ERICSSON_SCP, this.fmAlarmService, NFType.CHF, EnvVars.get(HOSTNAME))));

        this.leaderElectionEnabled = Boolean.valueOf(EnvVars.get(ENV_LEADER_ELECTION_ENABLED));

        final com.ericsson.sc.rxetcd.RxEtcd.Builder edb = RxEtcd.newBuilder()
                                                                .withEndpoint(EnvVars.get(ETCD_ENDPOINT))
                                                                .withConnectionRetries(10)
                                                                .withRequestTimeout(ETCD_REQUEST_TIMEOUT, TimeUnit.SECONDS);

        if (params.dcedTlsEnabled)
            edb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.dcedClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
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

        this.logThrottler = new LogThrottler();
    }

    /**
     * @see com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandCounter.Provider#
     *      getCounters()
     */
    @Override
    public List<Counter> getCounters(final boolean readThenClear)
    {
        final List<Counter> result = new ArrayList<>();

        final List<Cache.Statistics> statistics = Arrays.asList(this.cache.getStatistics());

        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), (double) counts.numberOfGets.get(readThenClear)));
            }

            result.add(new Counter("slf_cache_gets_total", "Number of times to get an item from the cache", instances));
        }
        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), (double) counts.numberOfHits.get(readThenClear)));
            }

            result.add(new Counter("slf_cache_hits_total", "Number of cache hits", instances));
        }
        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), (double) counts.numberOfPuts.get(readThenClear)));
            }

            result.add(new Counter("slf_cache_puts_total", "Number of times to put an item into the cache", instances));
        }
        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), (double) counts.numberOfRems.get(readThenClear)));
            }

            result.add(new Counter("slf_cache_rems_total", "Number of items removed from the cache", instances));
        }
        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), counts.hitRatio.get() * 100));
            }

            result.add(new Counter("slf_cache_hit_ratio", "Ratio [%] of number of hits by number of gets", instances));
        }
        {
            final List<Instance> instances = new ArrayList<>();

            for (Cache.Statistics counts : statistics)
            {
                final StringBuilder b = new StringBuilder();
                b.append(Arrays.asList("cache")).append('=').append(Arrays.asList(counts.getId()));
                instances.add(new Instance(b.toString(), (double) counts.size.get()));
            }

            result.add(new Counter("slf_cache_size", "The size of the cache", instances));
        }

        return result;
    }

    public Completable run()
    {
        final Function<Pair<Pair<ConfigContext, ConfigContext>, Pair<ConfigContext, ConfigContext>>, Completable> updateNfFunction = pair ->
        {
            final Optional<EricssonScp> config = pair.getSecond().getSecond().getConfig();

            log.info("updateNfFunction={}", log.isDebugEnabled() ? config : "<config not printed on info level>");

            // DNS related change when this function was called because of an update from
            // the DNS cache (then the previous and current configuration are the same).
            // User related change when there was change related to NF management.
            final boolean dnsRelated = pair.getFirst() == pair.getSecond();
            final boolean userRelated = !dnsRelated && pair.getSecond().getSecond().isChangedNnrfNfm();
            final boolean instIdRelated = !dnsRelated && pair.getSecond().getSecond().isChangedNnrfNfmNrfGroupInstId();
            log.info("userRelated={}, dnsRelated={}, instIdRelated={}", userRelated, dnsRelated, instIdRelated);

            try
            {
                if (config.isPresent())
                {
                    final EricssonScpScpFunction curr = config.get().getEricssonScpScpFunction();

                    log.debug("curr={}", curr);

                    if (curr != null)
                    {
                        // Config is empty -> update.
                        if (curr.getNfInstance() == null || curr.getNfInstance().isEmpty())
                        {
                            this.scpFunction.update(curr);
                            return Completable.complete();
                        }

                        // Only nfInstanceId of NRF group is changed, which was initiated by ourselves
                        // -> ignore.
                        if (instIdRelated && !userRelated)
                            return Completable.complete();

                        // Take a copy of curr. This is needed as curr should remain unchanged; the copy
                        // is used for modifications and will be used for further processing.
                        final EricssonScpScpFunction copy = Json.copy(curr, EricssonScpScpFunction.class);

                        // If the change is not DNS related:
                        // Calling update(copy) below may change copy, due to the update of the
                        // NF-instance ID of the NRF-groups. The delta of copy and curr reflects that
                        // change (only changed NF-instance IDs are considered) and can then be used for
                        // patching the configuration in CMM.
                        this.scpFunction.update(copy, userRelated, dnsRelated);

                        return Completable.complete();
                    }
                }

                this.scpFunction.stop();
            }
            catch (final Exception t)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
            }

            return Completable.complete();
        };

        final Consumer<Optional<EricssonScp>> registerAllUnresolvedNrfFqdnsWithDnsCache = o ->
        {
            final Set<IfDnsLookupContext> unresolvedNrfFqdns = new HashSet<>();

            o.ifPresent(config -> Optional.ofNullable(config.getEricssonScpScpFunction())
                                          .ifPresent(function -> unresolvedNrfFqdns.addAll(function.getNfInstance()
                                                                                                   .stream()
                                                                                                   .flatMap(inst -> inst.getNrfGroup()
                                                                                                                        .stream()
                                                                                                                        .flatMap(group -> group.getNrf()
                                                                                                                                               .stream()
                                                                                                                                               .filter(nrf -> nrf.getFqdn() != null
                                                                                                                                                              && !nrf.getFqdn()
                                                                                                                                                                     .isEmpty()
                                                                                                                                                              && (nrf.getIpEndpoint()
                                                                                                                                                                     .isEmpty()
                                                                                                                                                                  || nrf.getIpEndpoint()
                                                                                                                                                                        .stream()
                                                                                                                                                                        .allMatch(ep -> (ep.getIpv4Address() == null
                                                                                                                                                                                         || ep.getIpv4Address()
                                                                                                                                                                                              .isEmpty())
                                                                                                                                                                                        && (ep.getIpv6Address() == null
                                                                                                                                                                                            || ep.getIpv6Address()
                                                                                                                                                                                                 .isEmpty()))))
                                                                                                                                               .map(nrf ->
                                                                                                                                               {
                                                                                                                                                   if (group.getDnsProfileRef() != null)
                                                                                                                                                   {
                                                                                                                                                       final DnsProfile dnsProfile = Utils.getByName(inst.getDnsProfile(),
                                                                                                                                                                                                     group.getDnsProfileRef());

                                                                                                                                                       if (dnsProfile != null)
                                                                                                                                                       {
                                                                                                                                                           return DnsCache.LookupContext.of(nrf.getFqdn(),
                                                                                                                                                                                            dnsProfile.getIpFamilyResolution()
                                                                                                                                                                                                      .stream()
                                                                                                                                                                                                      .map(r -> com.ericsson.sc.utilities.dns.IpFamily.fromValue(r.value()))
                                                                                                                                                                                                      .collect(Collectors.toSet()));
                                                                                                                                                       }
                                                                                                                                                   }

                                                                                                                                                   return DnsCache.LookupContext.of(nrf.getFqdn(),
                                                                                                                                                                                    CommonConfigUtils.getDefaultIpFamilies(inst));
                                                                                                                                               })))
                                                                                                   .collect(Collectors.toSet()))));

            log.info("Found NRF FQDNs to be resolved: {}", unresolvedNrfFqdns);
            NrfDnsCache.singleton().publishHostsToResolve(unresolvedNrfFqdns);
        };

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

        final Completable slfWorkerChain = Completable.complete()
                                                      .andThen(rxEtcdConnection) // wait until etcd is up
                                                      .andThen(this.cache.start())
                                                      .andThen(this.certificateObserver.start())
                                                      .andThen(this.webServerExtTls.startListener())
                                                      .andThen(this.probeWebServer.startListener())
                                                      .andThen(this.oamWebServer.startListener())
                                                      .andThen(this.monitored.start().onErrorComplete())
                                                      .andThen(MetricRegister.singleton().start())
                                                      .andThen(this.loadMeter.start())
                                                      .andThen(this.ah.start())
                                                      .andThen(this.unresolvableHostsAh.start())
                                                      .andThen(this.cm.getNotificationHandler().start(this.oamWebServer))
                                                      .andThen(Completable.ambArray(this.configFlow.subscribeOn(Schedulers.io())
                                                                                                   .map(ConfigContext::getConfig)
                                                                                                   .distinctUntilChanged()
                                                                                                   .doOnNext(registerAllUnresolvedNrfFqdnsWithDnsCache)
                                                                                                   .ignoreElements(),
                                                                                    Observable.combineLatest(this.configFlow.filter(ConfigContext::isChangedNnrfNfm)
                                                                                                                            .scan(Pair.of(ConfigContext.empty(),
                                                                                                                                          ConfigContext.empty()),
                                                                                                                                  (r,
                                                                                                                                   o) -> Pair.of(r.getSecond(),
                                                                                                                                                 o)),
                                                                                                             NrfDnsCache.singleton()
                                                                                                                        .getResolvedHosts()
                                                                                                                        .toObservable(),
                                                                                                             (config,
                                                                                                              dnsResults) -> config)
                                                                                              .scan(Pair.of(Pair.of(ConfigContext.empty(),
                                                                                                                    ConfigContext.empty()),
                                                                                                            Pair.of(ConfigContext.empty(),
                                                                                                                    ConfigContext.empty())),
                                                                                                    (r,
                                                                                                     o) -> Pair.of(r.getSecond(), o))
                                                                                              .subscribeOn(Schedulers.io())
                                                                                              .flatMapCompletable(updateNfFunction),
                                                                                    waitUntilLeader.andThen(this.leaderElectionEnabled ? this.updatePodLabel(LeaderStatus.LEADER)
                                                                                                                                       : Completable.complete())
                                                                                                   .andThen(this.pmbrCfgHandler.createPmbrJobPatches())
                                                                                                   .andThen(this.pmbrCfgHandler.createPmbrGroupPatches())
                                                                                                   .andThen(leadershipLost),
                                                                                    this.shutdownHook.get()))
                                                      .onErrorResumeNext(e -> this.stop().andThen(Completable.error(e)))
                                                      .andThen(this.stop());

        return (this.leaderElectionEnabled ? election.map(RxLeaderElection::run).orElse(Completable.never()) : Completable.never()).ambWith(slfWorkerChain);
    }

    public Completable stop()
    {

        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()
                          .andThen(this.cache.stop().onErrorComplete())
                          .andThen(this.certificateObserver.stop().onErrorComplete())
                          .andThen(this.loadMeter.stop().onErrorComplete())
                          .andThen(MetricRegister.singleton().stop().onErrorComplete())
                          .andThen(this.monitored.stop().onErrorComplete())
                          .andThen(this.unresolvableHostsAh.stop().onErrorComplete())
                          .andThen(this.ah.stop().onErrorComplete())
                          .andThen(this.cm.getNotificationHandler().stop().onErrorComplete())
                          .andThen(this.webServerExtTls.stopListener().onErrorComplete())
                          .andThen(this.probeWebServer.stopListener().onErrorComplete())
                          .andThen(this.oamWebServer.stopListener().onErrorComplete())
                          .andThen(this.webClientProvider.close().onErrorComplete())
                          .andThen(this.tlsKeyLogger.map(TlsKeylogger::stop).orElse(Completable.complete()))
                          .andThen(this.alarmHandlerClient.close().onErrorComplete())
                          .andThen(this.rxEtcd.map(RxEtcd::close).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)));
    }

    private Completable replyWithError(final RoutingContext context,
                                       final Event event,
                                       final String nfInstanceId,
                                       final String nfType,
                                       final String invalidParameter)
    {
        final ProblemDetails problem = new ProblemDetails();

        problem.setStatus(event.getResponse().getResultCode());
        problem.setCause(event.getResponse().getResultReasonPhrase());

        if (nfInstanceId != null)
            problem.setInstance(nfInstanceId);

        if (nfType != null)
            problem.setType(nfType);

        if (event.getResponse().getResultDetails() != null)
            problem.setDetail(event.getResponse().getResultDetails());

        if (invalidParameter != null)
        {
            InvalidParam i = new InvalidParam();
            i.setParam(invalidParameter);
            problem.addInvalidParamsItem(i);
        }

        String problemStr;

        try
        {
            problemStr = json.writeValueAsString(problem);
        }
        catch (final JsonProcessingException e)
        {
            problemStr = e.toString();
        }

        if (this.logThrottler.loggingIsDue(log.isDebugEnabled()))
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

        return context.response().setStatusCode(event.getResponse().getResultCode()).putHeader(HD_CONTENT_TYPE, VAL_APPLICATION_PROBLEM_JSON).rxEnd(problemStr);
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
