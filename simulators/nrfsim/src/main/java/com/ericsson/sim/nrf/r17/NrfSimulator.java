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
 * Created on: Jan 16, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.nrf.r17;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Instance;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.Specs3gpp;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.nrf.r17.NnrfNfProfileDb;
import com.ericsson.cnal.openapi.r17.SbiNfPeerInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenRsp;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenRsp.TokenTypeEnum;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.BootstrappingInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.Status;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NfInstanceInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainConnectivity;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInfoSubscription;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInformation;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.StoredSearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.AusfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ConditionEventType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NefInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NotifCondition;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NotificationData;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NotificationEventType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.PcfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ServiceName;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SubscrCond;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdmInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdrInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdsfInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ChangeItem;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Link;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.LinksValueSchema;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.RedirectResponse;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sim.nrf.r17.NrfSimulator.Configuration.Response;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Registry;
import com.ericsson.utilities.common.Trie.Range;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.KeyCertProvider;
import com.ericsson.utilities.file.TrustedCert;
import com.ericsson.utilities.file.TrustedCertProvider;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.openapi.OpenApiServer;
import com.ericsson.utilities.http.openapi.OpenApiServer.Context3;
import com.ericsson.utilities.http.openapi.OpenApiServer.IfApiHandler;
import com.ericsson.utilities.http.openapi.OpenApiServer.IpFamily;
import com.ericsson.utilities.http.openapi.OpenApiTask;
import com.ericsson.utilities.http.openapi.OpenApiTask.DataIndex;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.ParsedHeaderValue;
import io.vertx.reactivex.ext.web.ParsedHeaderValues;
import io.vertx.reactivex.ext.web.RoutingContext;

public class NrfSimulator implements Runnable, MonitorAdapter.CommandCounter.Provider, MonitorAdapter.CommandEsa.Provider
{
    public static class Builder
    {
        /**
         * @param hosts Hosts for IPv4 or IPv6 or both, at most one of each kind
         * @param port  Common port
         */
        public static Builder of(final List<String> hosts,
                                 final Integer port)
        {
            return new Builder(hosts, port);
        }

        public static Builder of(final String host,
                                 final Integer port)
        {
            return new Builder(List.of(host), port);
        }

        private final List<String> hosts;
        private final Integer port;

        private Configuration.LoadTestMode defaultLoadTestMode = new Configuration.LoadTestMode();
        private Integer portTls;
        private String certificatesPath;
        private KeyCert keyCert;
        private TrustedCert trustedCert;

        private Builder(final List<String> hosts,
                        final Integer port)
        {
            this.hosts = hosts;
            this.port = port;
            this.portTls = 443;
            this.certificatesPath = EnvVars.get("CERTIFICATES_PATH", "");
        }

        public NrfSimulator build() throws IOException
        {
            return new NrfSimulator(this.defaultLoadTestMode, this.hosts, this.port, this.portTls, this.certificatesPath, this.keyCert, this.trustedCert);
        }

        public Builder withCertificatesPath(String certificatesPath)
        {
            this.certificatesPath = certificatesPath;
            return this;
        }

        public Builder withDefaultLoadTestMode(Configuration.LoadTestMode defaultLoadTestMode)
        {
            this.defaultLoadTestMode = defaultLoadTestMode;
            return this;
        }

        public Builder withKeyCert(KeyCert keyCert)
        {
            this.keyCert = keyCert;
            return this;
        }

        public Builder withPortTls(Integer portTls)
        {
            this.portTls = portTls;
            return this;
        }

        public Builder withTrustedCert(TrustedCert trustedCert)
        {
            this.trustedCert = trustedCert;
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "loadTestMode", "nnrfAccessToken", "nnrfBootstrapping", "nnrfNfDiscovery", "nnrfNfManagement" })
    public static class Configuration
    {
        @JsonPropertyOrder({ "isEnabled" })
        public static class LoadTestMode
        {
            boolean isEnabled = false;

            @JsonProperty("isEnabled")
            public boolean isEnabled()
            {
                return this.isEnabled;
            }

            @JsonProperty("isEnabled")
            public LoadTestMode setEnabled(boolean isEnabled)
            {
                this.isEnabled = isEnabled;
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

        @JsonPropertyOrder({ "replicator", "patches" })
        public static class Modifier
        {
            @JsonProperty("patches")
            private List<Json.Patch> patches = new ArrayList<>();

            @JsonProperty("replicator")
            private int replicator = 0;

            public List<Json.Patch> getPatches()
            {
                return this.patches;
            }

            public int getReplicator()
            {
                return this.replicator;
            }

            public Modifier setPatches(final List<Json.Patch> patches)
            {
                this.patches = patches;
                return this;
            }

            public Modifier setReplicator(final int replicator)
            {
                this.replicator = replicator;
                return this;
            }
        }

        @JsonPropertyOrder({ "response" })
        public static class NnrfAccessToken
        {
            @JsonProperty("response")
            private final Response response = new Response();

            @JsonIgnore
            private boolean isDirty = false;

            public Response getResponse()
            {
                return this.response;
            }

            @JsonIgnore
            public synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
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

        @JsonPropertyOrder({ "response" })
        public static class NnrfBootstrapping
        {
            @JsonProperty("response")
            private final Response response = new Response();

            @JsonIgnore
            private boolean isDirty = false;

            public Response getResponse()
            {
                return this.response;
            }

            @JsonIgnore
            public synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
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

        @JsonPropertyOrder({ "validityPeriodInSecs", "response", "maxPayloadSize" })
        public static class NnrfNfDiscovery
        {
            @JsonProperty("response")
            private final Response response = new Response();

            @JsonIgnore
            private boolean isDirty = false;

            @JsonProperty("validityPeriodInSecs")
            private int validityPeriodInSecs = REGISTRY_TIMEOUT_SECS;

            @JsonProperty("maxPayloadSize")
            private int maxPayloadSize = DEFAULT_MAX_PAYLOAD_SIZE;

            public synchronized int getMaxPayloadSize()
            {
                return this.maxPayloadSize;
            }

            public Response getResponse()
            {
                return this.response;
            }

            public synchronized int getValidityPeriodInSecs()
            {
                return this.validityPeriodInSecs;
            }

            @JsonIgnore
            public synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
            }

            @JsonIgnore
            public synchronized NnrfNfDiscovery setMaxPayloadSize(final int maxPayloadSize)
            {
                log.info("NF discovery: new max payload size [kilo octets]: {}", maxPayloadSize);
                this.maxPayloadSize = maxPayloadSize;
                this.isDirty = true;

                return this;
            }

            @JsonIgnore
            public synchronized NnrfNfDiscovery setValidityPeriodInSecs(final int validityPeriodInSecs)
            {
                log.info("NF discovery: new validity period [s]: {}", validityPeriodInSecs);
                this.validityPeriodInSecs = validityPeriodInSecs;
                this.isDirty = true;

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

        @JsonPropertyOrder({ "heartBeatTimerInSecs", "validityPeriodInSecs", "response", "provisioning" })
        public static class NnrfNfManagement
        {
            @JsonPropertyOrder({ "modifiers", "nfProfile" })
            public static class Provisioning
            {
                @JsonProperty("modifiers")
                private List<Modifier> modifiers = new ArrayList<>();

                @JsonProperty("nfProfile")
                private NFProfile nfProfile = null;

                public List<Modifier> getModifiers()
                {
                    return this.modifiers;
                }

                public NFProfile getNfProfile()
                {
                    return this.nfProfile;
                }

                public Provisioning setNfProfile(final NFProfile nfProfile)
                {
                    this.nfProfile = nfProfile;
                    return this;
                }

                public Provisioning setPatches(final List<Modifier> modifiers)
                {
                    this.modifiers = modifiers;
                    return this;
                }
            }

            @JsonProperty("response")
            public final Response response = new Response();

            @JsonIgnore
            private boolean isDirty = false;

            @JsonProperty("heartBeatTimerInSecs")
            private int heartBeatTimerInSecs = REGISTRY_TIMEOUT_SECS / 2; // Nyquist-Shannon sampling theorem

            @JsonProperty("validityPeriodInSecs")
            private int validityPeriodInSecs = VALIDITY_PERIOD_SECS;

            @JsonProperty("provisioning")
            private List<Provisioning> provisioning = new ArrayList<>();

            public synchronized int getHeartBeatTimerInSecs()
            {
                return this.heartBeatTimerInSecs;
            }

            public synchronized List<Provisioning> getProvisioning()
            {
                return this.provisioning;
            }

            public Response getResponse()
            {
                return this.response;
            }

            public synchronized int getValidityPeriodInSecs()
            {
                return this.validityPeriodInSecs;
            }

            @JsonIgnore
            public synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
            }

            @JsonIgnore
            public synchronized NnrfNfManagement setHeartBeatTimerInSecs(final int heartBeatTimerInSecs)
            {
                log.info("NF management: new heartbeat timer [s]: {}", heartBeatTimerInSecs);
                this.heartBeatTimerInSecs = heartBeatTimerInSecs;
                this.isDirty = true;

                return this;
            }

            @JsonIgnore
            public synchronized NnrfNfManagement setProvisioning(final List<Provisioning> provisioning)
            {
                log.info("NF management: new provisioning: {}", provisioning);
                this.provisioning = provisioning;

                return this;
            }

            @JsonIgnore
            public synchronized NnrfNfManagement setValidityPeriodInSecs(final int validityPeriodInSecs)
            {
                log.info("NF management: new validity period [s]: {}", validityPeriodInSecs);
                this.validityPeriodInSecs = validityPeriodInSecs;
                this.isDirty = true;

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

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "status", "delayInMillis", "doDrop", "redirectUrl" })
        public static class Response
        {
            @JsonIgnore
            private HttpResponseStatus status = null;

            @JsonIgnore
            private Long delayInMillis = 0l;

            @JsonIgnore
            private boolean drop = false;

            @JsonIgnore
            private String redirectUrl = null; // Only for use with 300 <= status < 400

            @JsonProperty("doDrop")
            public synchronized boolean doDrop()
            {
                return this.drop;
            }

            @JsonProperty("doDrop")
            public synchronized Response doDrop(boolean drop)
            {
                log.info("HTTP response: will{}be dropped", (drop ? " " : " not "));
                this.drop = drop;

                return this;
            }

            @JsonProperty("delayInMillis")
            public synchronized Long getDelayInMillis()
            {
                return this.delayInMillis;
            }

            @JsonProperty("redirectUrl")
            public synchronized String getRedirectUrl()
            {
                return this.redirectUrl;
            }

            @JsonIgnore
            public synchronized HttpResponseStatus getStatus()
            {
                return this.status;
            }

            @JsonProperty("delayInMillis")
            public synchronized Response setDelayInMillis(Long delayInMillis)
            {
                if (delayInMillis == null)
                    delayInMillis = 0l;

                log.info("HTTP response: new delay [ms]: {}", delayInMillis);
                this.delayInMillis = delayInMillis;

                return this;
            }

            @JsonProperty("redirectUrl")
            public synchronized Response setRedirectUrl(String redirectUrl)
            {
                log.info("HTTP response: new redirectUrl: {}", redirectUrl);
                this.redirectUrl = redirectUrl;

                return this;
            }

            @JsonIgnore
            public synchronized Response setStatus(HttpResponseStatus status)
            {
                log.info("HTTP response: new status: {}", status);
                this.status = status;

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

            @JsonProperty("status")
            private Integer getStatusAsInteger()
            {
                return this.getStatus() != null ? this.getStatus().code() : null;
            }

            @JsonProperty("status")
            private void setStatusFromInteger(final Integer status)
            {
                this.setStatus(HttpResponseStatus.valueOf(status));
            }
        }

        @JsonProperty("loadTestMode")
        private LoadTestMode loadTestMode = null;

        @JsonProperty("nnrfAccessToken")
        private final NnrfAccessToken nnrfAccessToken = new NnrfAccessToken();

        @JsonProperty("nnrfBootstrapping")
        private final NnrfBootstrapping nnrfBootstrapping = new NnrfBootstrapping();

        @JsonProperty("nnrfNfDiscovery")
        private final NnrfNfDiscovery nnrfNfDiscovery = new NnrfNfDiscovery();

        @JsonProperty("nnrfNfManagement")
        private final NnrfNfManagement nnrfNfManagement = new NnrfNfManagement();

        public synchronized LoadTestMode getLoadTestMode()
        {
            return this.loadTestMode;
        }

        public NnrfBootstrapping getNnrfAccessToken()
        {
            return this.nnrfBootstrapping;
        }

        public NnrfBootstrapping getNnrfBootstrapping()
        {
            return this.nnrfBootstrapping;
        }

        public NnrfNfDiscovery getNnrfNfDiscovery()
        {
            return this.nnrfNfDiscovery;
        }

        public NnrfNfManagement getNnrfNfManagement()
        {
            return this.nnrfNfManagement;
        }

        public synchronized Configuration setLoadTestMode(final LoadTestMode loadTestMode)
        {
            this.loadTestMode = loadTestMode;
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

    @JsonPropertyOrder({ "nfInstance" })
    public static class NfInstance
    {
        public static class Pool
        {
            @JsonProperty("pool")
            private final Map<String, NfInstance> pool = new ConcurrentHashMap<>();

            public void clear()
            {
                this.pool.values().forEach(instance ->
                {
                    instance.getStatistics().clear();
                    instance.getContexts().clear();
                });
            }

            public NfInstance get(String nfInstanceId)
            {
                if (nfInstanceId == null)
                    nfInstanceId = DEFAULT_NF_INSTANCE_ID;

                if (this.pool.containsKey(nfInstanceId))
                    return this.pool.get(nfInstanceId);

                NfInstance value = new NfInstance(nfInstanceId);
                NfInstance prev = this.pool.putIfAbsent(nfInstanceId, value);
                return prev != null ? prev : value;
            }

            public Iterator<Entry<String, NfInstance>> iterator()
            {
                return this.pool.entrySet().iterator();
            }

            /**
             * Returns a JSON representation of this object.
             */
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

        private static final String DEFAULT_NF_INSTANCE_ID = "<default>";

        @JsonProperty("statistics")
        private final Statistics statistics;

        @JsonIgnore
        private List<RoutingContext> contexts;

        public NfInstance(final String nfInstanceId)
        {
            this.statistics = new Statistics(nfInstanceId);
            this.contexts = new ArrayList<>();
        }

        public synchronized void addContext(RoutingContext context)
        {
            if (this.contexts.size() > 32)
            {
                log.warn("Already 32 contexts in list. Current context not added: {}", context);
                return;
            }

            final String body = context.getBodyAsString();

            if (body != null)
                context.put(DataIndex.REQUEST_BODY.name(), body);

            this.contexts.add(context);
        }

        @JsonIgnore
        public synchronized Map<String, Set<String>> getAllRequestHeaders()
        {
            return this.contexts.stream()
                                .flatMap(rc -> rc.request().headers().entries().stream())
                                .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));
        }

        @JsonIgnore
        public synchronized Map<String, Set<String>> getAllResponseHeaders()
        {
            return this.contexts.stream()
                                .flatMap(rc -> rc.response().headers().entries().stream())
                                .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));
        }

        @JsonIgnore
        public synchronized List<String> getAuthorityHeader()
        {
            return this.contexts.stream().map(c -> c.request().authority().toString()).collect(Collectors.toList());
        }

        @JsonIgnore
        public synchronized List<RoutingContext> getContexts()
        {
            return this.contexts;
        }

        @JsonIgnore
        public synchronized List<String> getRequestBody()
        {
            return this.contexts.stream()
                                .map(rc -> Optional.ofNullable((String) rc.get(DataIndex.REQUEST_BODY.name())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());
        }

        @JsonIgnore
        public synchronized List<String> getRequestHeader(String headerName)
        {
            return this.contexts.stream()
                                .filter(rc -> rc.request().getHeader(headerName) != null)
                                .flatMap(rc -> rc.request().headers().getAll(headerName).stream())
                                .collect(Collectors.toList());
        }

        @JsonIgnore
        public synchronized List<String> getRequestQuery()

        {
            return this.contexts.stream().map(rc -> rc.request().query()).collect(Collectors.toList());
        }

        @JsonIgnore
        public synchronized List<String> getResponseHeader(String headerName)
        {
            return this.contexts.stream().flatMap(rc -> rc.response().headers().getAll(headerName).stream()).collect(Collectors.toList());
        }

        @JsonIgnore
        public Statistics getStatistics()
        {
            return this.statistics;
        }
    }

    public static class NnrfAccessToken extends ApiHandler
    {
        public enum Operation
        {
            ACCESS_TOKEN_REQUEST("AccessTokenRequest");

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

        private BiConsumer<RoutingContext, Event> handleAccessTokenRequest = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            String nfType = null;

            try
            {
                final AccessTokenRsp result = new AccessTokenRsp().accessToken("access_token").tokenType(TokenTypeEnum.BEARER);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, " max-age=86400")
                       .end(json.writeValueAsString(result));
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          nfType,
                                          null);
            }
        };

        public NnrfAccessToken(final NrfSimulator owner)
        {
            super(owner);

            final String baseUri = (owner.webServerExtTls != null ? owner.webServerExtTls.get(0) : owner.webServerExt.get(0)).baseUri().toString();

            owner.links.put("self", new LinksValueSchema().href(new StringBuilder(baseUri).append(API_ROOT_ACCESS_TOKEN).append("bootstrapping").toString()));

            this.getHandlerByOperationId().put(Operation.ACCESS_TOKEN_REQUEST.value, this.handleAccessTokenRequest);
        }

        @Override
        protected Response getResponse()
        {
            return this.owner.getConfiguration().getNnrfAccessToken().getResponse();
        }
    }

    public static class NnrfBootstrapping extends ApiHandler
    {
        public enum Operation
        {
            BOOTSTRAPPING_INFO_REQUEST("BootstrappingInfoRequest");

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

        private BiConsumer<RoutingContext, Event> handleBootstrappingInfoRequest = (context,
                                                                                    event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            String nfType = null;

            try
            {
                final BootstrappingInfo result = new BootstrappingInfo().status(Status.OPERATIVE)
                                                                        .links(this.owner.links) // NRF property but filled by NRF services
                                                                        .nrfFeatures(this.owner.nrfFeatures) // NRF property but filled by NRF services
                                                                        .nrfInstanceId(owner.nfInstanceId) // NRF property
                                                                        .nrfSetId("set12.nrfset.5gc.mnc012.mcc345") // NRF property
                                                                        .oauth2Required(this.owner.oauth2Required); // NRF property but filled by NRF services

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .putHeader(HD_CACHE_CONTROL, " max-age=86400")
                       .putHeader(HD_ETAG, this.owner.etag.get().toString())
                       .end(json.writeValueAsString(result));
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          nfType,
                                          null);
            }
        };

        public NnrfBootstrapping(final NrfSimulator owner)
        {
            super(owner);

            final String baseUri = (owner.webServerExtTls != null ? owner.webServerExtTls.get(0) : owner.webServerExt.get(0)).baseUri().toString();

            owner.links.put("self", new LinksValueSchema().href(new StringBuilder(baseUri).append(API_ROOT_BOOTSTRAPPING).append("bootstrapping").toString()));

            this.getHandlerByOperationId().put(Operation.BOOTSTRAPPING_INFO_REQUEST.value, this.handleBootstrappingInfoRequest);
        }

        @Override
        protected Response getResponse()
        {
            return this.owner.getConfiguration().getNnrfBootstrapping().getResponse();
        }
    }

    public static class NnrfNfDiscovery extends ApiHandler
    {
        public enum Operation
        {
            NF_INSTANCES_SEARCH("SearchNFInstances"),
            SEARCH_RETRIEVE_COMPLETE("RetrieveCompleteSearch"),
            SEARCH_RETRIEVE_STORED("RetrieveStoredSearch"),
            SCP_DOMAIN_ROUTING_INFO_GET("SCPDomainRoutingInfoGet"),
            SCP_DOMAIN_ROUTING_INFO_SUBSCRIBE("ScpDomainRoutingInfoSubscribe"),
            SCP_DOMAIN_ROUTING_INFO_UNSUBSCRIBE("ScpDomainRoutingInfoUnsubscribe");

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

        private static class CacheOfSearchResults
        {
            public static class Item
            {
                public static Item of()
                {
                    return of(new SearchResult().validityPeriod(0));
                }

                public static Item of(final SearchResult result)
                {
                    return new Item(result);
                }

                final long expirationTimeMillis;
                final StoredSearchResult result;

                private Item(final SearchResult result)
                {
                    this.expirationTimeMillis = Instant.now().toEpochMilli() + 1000 * result.getValidityPeriod();
                    this.result = new StoredSearchResult().nfInstances(result.getNfInstances());
                }

                public long getExpirationTimeMillis()
                {
                    return this.expirationTimeMillis;
                }

                public StoredSearchResult getResult()
                {
                    return this.result;
                }

                public long getValidityPeriodSecs()
                {
                    return Math.max(0, this.expirationTimeMillis / 1000 - Instant.now().getEpochSecond());
                }
            }

            private final AtomicInteger searchId = new AtomicInteger(0);
            private final Map<String /* searchId */, Item> cache = new ConcurrentHashMap<>();
            private final Map<Long /* expirationTimeMillis */, List<String> /* searchId */> history = new ConcurrentSkipListMap<>();

            public CacheOfSearchResults()
            {
                Completable.fromAction(() ->
                {
                    final long now = Instant.now().toEpochMilli();

                    for (Iterator<Entry<Long, List<String>>> it = this.history.entrySet().iterator(); it.hasNext();)
                    {
                        final Entry<Long, List<String>> entry = it.next();

                        if (entry.getKey() > now)
                            break;

                        log.debug("CacheOfSearchResults: removing expired StoredSearchResults, entry: {}", entry);

                        it.remove();
                        entry.getValue().forEach(this.cache::remove);

                        log.debug("CacheOfSearchResults: size={} after partial clean up.", this.cache.size());
                    }
                }).repeatWhen(h -> h.delay(20, TimeUnit.MILLISECONDS)).subscribe();
            }

            public Item get(final String searchId)
            {
                return this.cache.getOrDefault(searchId, Item.of());
            }

            public String put(final SearchResult result)
            {
                final String searchId = String.valueOf(this.searchId.getAndIncrement());
                final Item item = Item.of(result);
                this.cache.put(searchId, item);
                this.history.computeIfAbsent(item.getExpirationTimeMillis(), key -> new ArrayList<>()).add(searchId);

                return searchId;
            }
        }

        private static List<String> getAcceptEncoding(final ParsedHeaderValues headers)
        {
            ArrayList<String> result = null;

            List<ParsedHeaderValue> acceptEncodingHeaders = headers.acceptEncoding();

            if (acceptEncodingHeaders != null)
            {
                result = new ArrayList<>();

                for (ParsedHeaderValue header : acceptEncodingHeaders)
                    result.add(header.value());
            }

            return result;
        }

        private static String getGpsi(final RoutingContext context)
        {
            String result = null;

            final String param = context.request().getParam("gpsi");

            if (param != null)
            {
                result = param;

                final String expression = "^(msisdn-[0-9]{5,15})$";

                if (!result.matches(expression))
                    throw new IllegalArgumentException("Parameter '" + result + "' does not match expression '" + expression + "'.", "gpsi");
            }

            return result;
        }

        private static String getPreferredLocality(final RoutingContext context)
        {
            return context.request().getParam("preferred-locality");
        }

        private static String getRequesterNfType(final RoutingContext context)
        {
            return context.request().getParam("requester-nf-type");
        }

        private static List<PlmnId> getRequesterPlmnList(final RoutingContext context)
        {
            final String param = context.request().getParam("requester-plmn-list");

            if (param != null)
            {
                try
                {
                    return json.readValue(param, new TypeReference<List<PlmnId>>()
                    {
                    });
                }
                catch (Exception e)
                {
                    log.error("Ignoring invalid parameter requester-plmn-list: {}. Cause: {}", param, e.toString());
                }
            }

            return null;
        }

        private static List<ExtSnssai> getRequesterSnssais(final RoutingContext context)
        {
            final String param = context.request().getParam("requester-snssais");

            if (param != null)
            {
                try
                {
                    return json.readValue(param, new TypeReference<List<ExtSnssai>>()
                    {
                    });
                }
                catch (Exception e)
                {
                    log.error("Ignoring invalid parameter requester-snssais: {}. Cause: {}", param, e.toString());
                }
            }

            return null;
        }

        private static List<String> getScpDomainList(final RoutingContext context)
        {
            ArrayList<String> result = null;

            final String param = context.request().getParam("scp-domain-list");

            log.debug("scp-domain-list='{}'", param);

            if (param != null)
            {
                result = new ArrayList<>();

                for (String p : param.split(","))
                    result.add(p.strip());
            }

            return result;
        }

        private static List<String> getServiceNames(final RoutingContext context)
        {
            ArrayList<String> result = null;

            final String param = context.request().getParam("service-names");

            log.debug("service-names='{}'", param);

            if (param != null)
            {
                result = new ArrayList<>();

                for (String p : param.split(","))
                    result.add(p.strip());
            }

            return result;
        }

        private static String getSupi(final RoutingContext context)
        {
            String result = null;

            final String param = context.request().getParam("supi");

            if (param != null)
            {
                result = param;

                final String expression = "^(imsi-[0-9]{5,15})$";

                if (!result.matches(expression))
                    throw new IllegalArgumentException("Parameter '" + result + "' does not match expression '" + expression + "'.", "supi");
            }

            return result;
        }

        private static String getTargetNfServiceSetId(final RoutingContext context)
        {
            return context.request().getParam("target-nf-service-set-id");
        }

        private static String getTargetNfSetId(final RoutingContext context)
        {
            return context.request().getParam("target-nf-set-id");
        }

        private static String getTargetNfType(final RoutingContext context)
        {
            return context.request().getParam("target-nf-type");
        }

        private static List<PlmnId> getTargetPlmnList(final RoutingContext context)
        {
            final String param = context.request().getParam("target-plmn-list");

            if (param != null)
            {
                try
                {
                    return json.readValue(param, new TypeReference<List<PlmnId>>()
                    {
                    });
                }
                catch (Exception e)
                {
                    log.error("Ignoring invalid parameter target-plmn-list: {}. Cause: {}", param, e.toString());
                }
            }

            return null;
        }

        private static byte[] gzipCompress(final String result)
        {
            try
            {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

                gzipOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
                gzipOutputStream.flush();
                gzipOutputStream.close();

                return outputStream.toByteArray();
            }
            catch (IOException e)
            {
                log.error("Error compressing result", e);
                return null;
            }
        }

        /**
         * Compare the requesterSnssai with the allowedNssai.
         * <p>
         * The comparison of allowedNssai and requesterSnssai only considers the
         * properties wildcardSd and sdRanges if one of them is provided in the
         * requesterSnssai (backward compatibility).
         * 
         * @param requesterSnssai The requesterSnssai to be compared.
         * @return The result of the comparison.
         */
        private static Predicate<ExtSnssai> match(final ExtSnssai requesterSnssai)
        {
            return allowedNssai -> Objects.equals(allowedNssai.getSst(), requesterSnssai.getSst())
                                   && Objects.equals(allowedNssai.getSd(), requesterSnssai.getSd())
                                   && (requesterSnssai.getWildcardSd() == null && requesterSnssai.getSdRanges() == null
                                       || Objects.equals(allowedNssai.getWildcardSd(), requesterSnssai.getWildcardSd())
                                          && Objects.equals(allowedNssai.getSdRanges(), requesterSnssai.getSdRanges()));
        }

        private static boolean matchGpsi(final String gpsi,
                                         final ChfInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRangeList()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final List<IdentityRange> ranges)
        {
            boolean matches = true;

            if (ranges != null)
            {
                for (final IdentityRange range : ranges)
                {
                    matches = range.getPattern() != null ? gpsi.matches(range.getPattern())
                                                         : Range.includes(range.getStart(), range.getEnd(), gpsi.substring(gpsi.indexOf('-') + 1), true);

                    if (matches)
                        return true;
                }
            }

            return matches;
        }

        private static boolean matchGpsi(final String gpsi,
                                         final NefInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final PcfInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final UdmInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final UdrInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchLocality(final String preferredLocality,
                                             final NFProfile nfProfile)
        {
            boolean matches = true;

            if (preferredLocality != null)
                matches = nfProfile.getLocality() != null && nfProfile.getLocality().contains(preferredLocality);

            return matches;
        }

        @SuppressWarnings("deprecation")
        private static boolean matchRequesterNfType(final String requesterNfType,
                                                    final NFProfile nfProfile)
        {
            // From TS 29.510 regarding attribute allowedNfTypes:
            // Note 5: If this attribute is present in the NFService and in the NF profile,
            // the attribute from the NFService shall prevail. The absence of this attribute
            // in the NFService and in the NFProfile indicates that there is no
            // corresponding restriction to access the service instance. If this attribute
            // is absent in the NF Service, but it is present in the NF Profile, the
            // attribute from the NF Profile shall be applied.

            boolean matches = true;

            if (requesterNfType != null && !requesterNfType.isEmpty())
            {
                matches = false;

                // Property service-list takes precedence over deprecated property services.
                final List<NFService> nfServices = nfProfile.getNfServiceList() != null
                                                   && !nfProfile.getNfServiceList().isEmpty() ? List.copyOf(nfProfile.getNfServiceList().values())
                                                                                              : nfProfile.getNfServices();

                if (nfServices != null && !nfServices.isEmpty())
                {
                    for (NFService nfService : nfServices)
                    {
                        if (nfProfile.getAllowedNfTypes() == null && nfService.getAllowedNfTypes() == null)
                        {
                            return true;
                        }

                        if (nfService.getAllowedNfTypes() != null)
                        {
                            if (nfService.getAllowedNfTypes().contains(requesterNfType))
                            {
                                return true;
                            }
                        }
                        else if (nfProfile.getAllowedNfTypes() != null && nfProfile.getAllowedNfTypes().contains(requesterNfType))
                        {
                            return true;
                        }
                    }
                }
                else
                {
                    if (nfProfile.getAllowedNfTypes() == null || nfProfile.getAllowedNfTypes().contains(requesterNfType))
                    {
                        return true;
                    }
                }
            }

            return matches;
        }

        @SuppressWarnings("deprecation")
        private static boolean matchRequesterPlmnList(final List<PlmnId> requesterPlmns,
                                                      final NFProfile nfProfile)
        {
            // From TS 29.510 regarding attribute allowedPlmns:
            // PLMNs allowed to access the service instance (NOTE 5).
            // The absence of this attribute indicates that any PLMN is allowed to access
            // the service instance.
            // When included, the allowedPlmns attribute needs not include the PLMN ID(s)
            // registered in the plmnList attribute of the NF Profile, i.e. the PLMN ID(s)
            // registered in the NF Profile shall be considered to be allowed to access the
            // service instance.
            // Note 5: If this attribute is present in the NFService and in the NF profile,
            // the attribute from the NFService shall prevail. The absence of this attribute
            // in the NFService and in the NFProfile indicates that there is no
            // corresponding restriction to access the service instance. If this attribute
            // is absent in the NF Service, but it is present in the NF Profile, the
            // attribute from the NF Profile shall be applied.

            boolean matches = true;

            if (requesterPlmns != null && !requesterPlmns.isEmpty())
            {
                matches = false;

                // Property service-list takes precedence over deprecated property services.
                final List<NFService> nfServices = nfProfile.getNfServiceList() != null
                                                   && !nfProfile.getNfServiceList().isEmpty() ? List.copyOf(nfProfile.getNfServiceList().values())
                                                                                              : nfProfile.getNfServices();

                if (nfServices != null && !nfServices.isEmpty())
                {
                    for (PlmnId requesterPlmn : requesterPlmns)
                    {
                        for (NFService nfService : nfServices)
                        {
                            if (nfProfile.getAllowedPlmns() == null && nfService.getAllowedPlmns() == null)
                            {
                                return true;
                            }

                            if (nfService.getAllowedPlmns() != null)
                            {
                                if (nfService.getAllowedPlmns().contains(requesterPlmn)
                                    || nfProfile.getPlmnList() != null && nfProfile.getPlmnList().contains(requesterPlmn))
                                {
                                    return true;
                                }
                            }
                            else if (nfProfile.getAllowedPlmns() != null && nfProfile.getAllowedPlmns().contains(requesterPlmn))
                            {
                                return true;
                            }
                        }
                    }
                }
                else
                {
                    for (PlmnId requesterPlmn : requesterPlmns)
                    {
                        if (nfProfile.getAllowedPlmns() == null || nfProfile.getAllowedPlmns().contains(requesterPlmn)
                            || nfProfile.getPlmnList() != null && nfProfile.getPlmnList().contains(requesterPlmn)) // <- not sure if that's according to
                                                                                                                   // standard
                        {
                            return true;
                        }
                    }
                }
            }

            return matches;
        }

        @SuppressWarnings("deprecation")
        private static boolean matchRequesterSnssais(final List<ExtSnssai> requesterSnssais,
                                                     final NFProfile nfProfile)
        {
            // From TS 29.510 regarding attribute allowedNssais:
            // Note 5: If this attribute is present in the NFService and in the NF profile,
            // the attribute from the NFService shall prevail. The absence of this attribute
            // in the NFService and in the NFProfile indicates that there is no
            // corresponding restriction to access the service instance. If this attribute
            // is absent in the NF Service, but it is present in the NF Profile, the
            // attribute from the NF Profile shall be applied.

            boolean matches = true;

            if (requesterSnssais != null && !requesterSnssais.isEmpty())
            {
                matches = false;

                // Property service-list takes precedence over deprecated property services.
                final List<NFService> nfServices = nfProfile.getNfServiceList() != null
                                                   && !nfProfile.getNfServiceList().isEmpty() ? List.copyOf(nfProfile.getNfServiceList().values())
                                                                                              : nfProfile.getNfServices();

                if (nfServices != null && !nfServices.isEmpty())
                {
                    for (ExtSnssai requesterSnssai : requesterSnssais)
                    {
                        for (NFService nfService : nfServices)
                        {
                            if (nfProfile.getAllowedNssais() == null && nfService.getAllowedNssais() == null)
                            {
                                return true;
                            }

                            if (nfService.getAllowedNssais() != null)
                            {
                                if (nfService.getAllowedNssais().stream().anyMatch(match(requesterSnssai)))
                                {
                                    return true;
                                }
                            }
                            else if (nfProfile.getAllowedNssais() != null && nfProfile.getAllowedNssais().stream().anyMatch(match(requesterSnssai)))
                            {
                                return true;
                            }
                        }
                    }
                }
                else
                {
                    for (ExtSnssai requesterSnssai : requesterSnssais)
                    {
                        if (nfProfile.getAllowedNssais() == null || nfProfile.getAllowedNssais().stream().anyMatch(match(requesterSnssai)))
                        {
                            return true;
                        }
                    }
                }
            }

            return matches;
        }

        private static boolean matchScpDomainList(final List<String> scpDomainList,
                                                  final NFProfile nfProfile)
        {
            // If scpDomainList from the query is null, no filtering will be done. All
            // nfProfiles will match the query.
            boolean matches = true;

            if (scpDomainList != null)
            {
                // If scpDomainList is not null, all nfProfiles will be filtered out, except the
                // NfProfiles that include at least one scpDomain from the query, or SCP
                // NFProfiles that include all scpDomains from the query

                matches = false;

                final List<String> scpDomainsInProfile = nfProfile.getScpDomains();
                final String nfTypeInProfile = nfProfile.getNfType().toUpperCase();

                if (scpDomainsInProfile != null && !scpDomainsInProfile.isEmpty() && !nfTypeInProfile.isEmpty())
                {
                    if (nfTypeInProfile.equals(NFType.SCP))
                    {
                        // SCP NfProfile should include all domains in the query to be discovered
                        matches = matchScpDomainListAllElements(scpDomainList, scpDomainsInProfile);
                    }
                    else
                    {
                        // All NfProfiles except SCP should include at least one domain in the query to
                        // be discovered
                        matches = matchScpDomainListAtLeastOneElement(scpDomainList, scpDomainsInProfile);
                    }
                }
            }

            return matches;
        }

        private static boolean matchScpDomainListAllElements(final List<String> scpDomainList,
                                                             final List<String> scpDomainsInProfile)
        {
            for (String scpDomainInQuery : scpDomainList)
            {
                if (!scpDomainsInProfile.contains(scpDomainInQuery))
                {
                    return false;
                }
            }

            return true;
        }

        private static boolean matchScpDomainListAtLeastOneElement(final List<String> scpDomainList,
                                                                   final List<String> scpDomainsInProfile)
        {
            for (String scpDomainInQuery : scpDomainList)
            {
                if (scpDomainsInProfile.contains(scpDomainInQuery))
                {
                    return true;
                }
            }

            return false;
        }

        @SuppressWarnings("deprecation")
        private static boolean matchServiceNames(final List<String> serviceNames,
                                                 final NFProfile nfProfile)
        {
            boolean matches = true;

            if (serviceNames != null)
            {
                matches = false;

                // Property service-list takes precedence over deprecated property services.
                final List<NFService> nfServices = nfProfile.getNfServiceList() != null
                                                   && !nfProfile.getNfServiceList().isEmpty() ? List.copyOf(nfProfile.getNfServiceList().values())
                                                                                              : nfProfile.getNfServices();

                if (nfServices != null && !nfServices.isEmpty())
                {

                    for (String serviceName : serviceNames)
                    {
                        for (NFService nfService : nfServices)
                        {
                            if (nfService.getServiceName().equals(serviceName))
                                return true;
                        }
                    }
                }
            }

            return matches;
        }

        private static boolean matchSpecificInfo(final String gpsi,
                                                 final String supi,
                                                 final NFProfile nfProfile)
        {
            boolean matches = true;

            switch (nfProfile.getNfType().toUpperCase())
            {
                case NFType.AUSF:
                    matches = matchSupi(supi, nfProfile.getAusfInfo());
                    break;

                case NFType.CHF:
                    matches = matchGpsi(gpsi, nfProfile.getChfInfo()) && matchSupi(supi, nfProfile.getChfInfo());
                    break;

                case NFType.NEF:
                    matches = matchGpsi(gpsi, nfProfile.getNefInfo());
                    break;

                case NFType.PCF:
                    matches = matchGpsi(gpsi, nfProfile.getPcfInfo()) && matchSupi(supi, nfProfile.getPcfInfo());
                    break;

                case NFType.UDM:
                    matches = matchGpsi(gpsi, nfProfile.getUdmInfo()) && matchSupi(supi, nfProfile.getUdmInfo());
                    break;

                case NFType.UDR:
                    matches = matchGpsi(gpsi, nfProfile.getUdrInfo()) && matchSupi(supi, nfProfile.getUdrInfo());
                    break;

                case NFType.UDSF:
                    matches = matchSupi(supi, nfProfile.getUdsfInfo());
                    break;

                default:
                    break;
            }

            return matches;
        }

        private static boolean matchSupi(final String supi,
                                         final AusfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final ChfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRangeList()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final List<SupiRange> ranges)
        {
            boolean matches = true;

            if (ranges != null)
            {
                for (final SupiRange range : ranges)
                {
                    matches = range.getPattern() != null ? supi.matches(range.getPattern())
                                                         : Range.includes(range.getStart(), range.getEnd(), supi.substring(supi.indexOf('-') + 1), true);

                    if (matches)
                        return true;
                }
            }

            return matches;
        }

        private static boolean matchSupi(final String supi,
                                         final PcfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdmInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdrInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdsfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        @SuppressWarnings("deprecation")
        private static boolean matchTargetNfServiceSetId(final String targetNfServiceSetId,
                                                         final com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile nfProfile)
        {
            boolean matches = true;

            if (targetNfServiceSetId != null)
            {
                // Implementation assumes that both nf-services and nf-service-list contain the
                // same nf-services.
                // Hence, only one of them needs to be searched for the set-ID.

                matches = nfProfile.getNfServices() != null && !nfProfile.getNfServices().isEmpty();

                if (matches)
                {
                    for (com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService nfService : nfProfile.getNfServices())
                    {
                        matches = nfService.getNfServiceSetIdList() != null && nfService.getNfServiceSetIdList().contains(targetNfServiceSetId);

                        if (matches)
                            break;
                    }
                }
                else
                {
                    matches = nfProfile.getNfServiceList() != null && !nfProfile.getNfServiceList().isEmpty();

                    if (matches)
                    {
                        for (com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService nfService : nfProfile.getNfServiceList().values())
                        {
                            matches = nfService.getNfServiceSetIdList() != null && nfService.getNfServiceSetIdList().contains(targetNfServiceSetId);

                            if (matches)
                                break;
                        }
                    }
                }
            }

            return matches;
        }

        private static boolean matchTargetNfSetId(final String targetNfSetId,
                                                  final NFProfile nfProfile)
        {
            boolean matches = true;

            if (targetNfSetId != null)
                matches = nfProfile.getNfSetIdList() != null && nfProfile.getNfSetIdList().contains(targetNfSetId);

            return matches;
        }

        private static boolean matchTargetPlmnList(final List<PlmnId> targetPlmns,
                                                   final NFProfile nfProfile)
        {
            boolean matches = true;

            if (nfProfile.getPlmnList() != null && targetPlmns != null && !targetPlmns.isEmpty())
            {
                matches = false;

                for (PlmnId targetPlmn : targetPlmns)
                {
                    if (nfProfile.getPlmnList().contains(targetPlmn))
                    {
                        return true;
                    }
                }
            }

            return matches;
        }

        @SuppressWarnings("deprecation")
        private static com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile toDiscoveryNfProfile(final NFProfile managementNfProfile,
                                                                                                             final List<String> serviceNames,
                                                                                                             final Integer diffPriority)
        {
            com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile discoveryNfProfile = new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile();

            discoveryNfProfile.setAmfInfo(managementNfProfile.getAmfInfo());
            discoveryNfProfile.setAmfInfoList(managementNfProfile.getAmfInfoList());
            discoveryNfProfile.setAusfInfo(managementNfProfile.getAusfInfo());
            discoveryNfProfile.setBsfInfo(managementNfProfile.getBsfInfo());
            discoveryNfProfile.setBsfInfoList(managementNfProfile.getBsfInfoList());
            discoveryNfProfile.setCapacity(managementNfProfile.getCapacity());
            discoveryNfProfile.setChfInfo(managementNfProfile.getChfInfo());
            discoveryNfProfile.setChfInfoList(managementNfProfile.getChfInfoList());
            discoveryNfProfile.setCustomInfo(managementNfProfile.getCustomInfo());
            discoveryNfProfile.setDefaultNotificationSubscriptions(managementNfProfile.getDefaultNotificationSubscriptions());
            discoveryNfProfile.setFqdn(managementNfProfile.getFqdn());
            discoveryNfProfile.setGmlcInfo(managementNfProfile.getGmlcInfo());
            discoveryNfProfile.setHssInfoList(managementNfProfile.getHssInfoList());
            discoveryNfProfile.setInterPlmnFqdn(managementNfProfile.getInterPlmnFqdn());
            discoveryNfProfile.setIpv4Addresses(managementNfProfile.getIpv4Addresses());
            discoveryNfProfile.setIpv6Addresses(managementNfProfile.getIpv6Addresses());
            discoveryNfProfile.setLcHSupportInd(managementNfProfile.getLcHSupportInd());
            discoveryNfProfile.setLmfInfo(managementNfProfile.getLmfInfo());
            discoveryNfProfile.setLoad(managementNfProfile.getLoad());
            discoveryNfProfile.setLoadTimeStamp(managementNfProfile.getLoadTimeStamp());
            discoveryNfProfile.setLocality(managementNfProfile.getLocality());
            discoveryNfProfile.setNefInfo(managementNfProfile.getNefInfo());
            discoveryNfProfile.setNfInstanceId(managementNfProfile.getNfInstanceId());
            discoveryNfProfile.setNfInstanceName(managementNfProfile.getNfInstanceName());

            if (managementNfProfile.getNfServiceList() != null)
            {
                managementNfProfile.getNfServiceList().entrySet().forEach(entry ->
                {
                    if (serviceNames == null || serviceNames.isEmpty())
                    {
                        discoveryNfProfile.putNfServiceListItem(entry.getKey(), toDiscoveryNfService(entry.getValue()));
                    }
                    else
                    {
                        for (String serviceName : serviceNames)
                        {
                            if (entry.getValue().getServiceName().equals(serviceName))
                                discoveryNfProfile.putNfServiceListItem(entry.getKey(), toDiscoveryNfService(entry.getValue()));
                        }
                    }
                });
            }

            discoveryNfProfile.setNfServicePersistence(managementNfProfile.getNfServicePersistence());

            if (managementNfProfile.getNfServices() != null)
            {
                managementNfProfile.getNfServices().forEach(managementNfService ->
                {
                    if (serviceNames == null || serviceNames.isEmpty())
                    {
                        discoveryNfProfile.addNfServicesItem(toDiscoveryNfService(managementNfService));
                    }
                    else
                    {
                        for (String serviceName : serviceNames)
                        {
                            if (managementNfService.getServiceName().equals(serviceName))
                                discoveryNfProfile.addNfServicesItem(toDiscoveryNfService(managementNfService));
                        }
                    }
                });
            }

            discoveryNfProfile.setNfSetIdList(managementNfProfile.getNfSetIdList());
            discoveryNfProfile.setNfSetRecoveryTimeList(managementNfProfile.getNfSetRecoveryTimeList());
            discoveryNfProfile.setNfStatus(managementNfProfile.getNfStatus());
            discoveryNfProfile.setNfType(managementNfProfile.getNfType());
            discoveryNfProfile.setNsiList(managementNfProfile.getNsiList());
            discoveryNfProfile.setNwdafInfo(managementNfProfile.getNwdafInfo());
            discoveryNfProfile.setOlcHSupportInd(managementNfProfile.getOlcHSupportInd());
            discoveryNfProfile.setPcfInfo(managementNfProfile.getPcfInfo());
            discoveryNfProfile.setPcfInfoList(managementNfProfile.getPcfInfoList());
            discoveryNfProfile.setPcscfInfoList(managementNfProfile.getPcscfInfoList());
            discoveryNfProfile.setPerPlmnSnssaiList(managementNfProfile.getPerPlmnSnssaiList());
            discoveryNfProfile.setPlmnList(managementNfProfile.getPlmnList());

            if (managementNfProfile.getPriority() != null)
            {
                discoveryNfProfile.setPriority(managementNfProfile.getPriority() + diffPriority);
            }
            else
            {
                discoveryNfProfile.setPriority(managementNfProfile.getPriority());
            }

            discoveryNfProfile.setRecoveryTime(managementNfProfile.getRecoveryTime());
            discoveryNfProfile.setScpDomains(managementNfProfile.getScpDomains());
            discoveryNfProfile.setScpInfo(managementNfProfile.getScpInfo());
            discoveryNfProfile.setServiceSetRecoveryTimeList(managementNfProfile.getServiceSetRecoveryTimeList());
            discoveryNfProfile.setServingScope(managementNfProfile.getServingScope());
            discoveryNfProfile.setSmfInfo(managementNfProfile.getSmfInfo());
            discoveryNfProfile.setSmfInfoList(managementNfProfile.getSmfInfoList());
            discoveryNfProfile.setSnpnList(managementNfProfile.getSnpnList());
            discoveryNfProfile.setsNssais(managementNfProfile.getsNssais());
            discoveryNfProfile.setUdmInfo(managementNfProfile.getUdmInfo());
            discoveryNfProfile.setUdmInfoList(managementNfProfile.getUdmInfoList());
            discoveryNfProfile.setUdrInfo(managementNfProfile.getUdrInfo());
            discoveryNfProfile.setUdrInfoList(managementNfProfile.getUdrInfoList());
            discoveryNfProfile.setUdsfInfo(managementNfProfile.getUdsfInfo());
            discoveryNfProfile.setUdsfInfoList(managementNfProfile.getUdsfInfoList());
            discoveryNfProfile.setUpfInfo(managementNfProfile.getUpfInfo());
            discoveryNfProfile.setUpfInfoList(managementNfProfile.getUpfInfoList());

            return discoveryNfProfile;
        }

        private static com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFService toDiscoveryNfService(com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService managementNfService)
        {
            com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFService discoveryNfService = new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFService();

            discoveryNfService.setAllowedOperationsPerNfInstance(managementNfService.getAllowedOperationsPerNfInstance());
            discoveryNfService.setAllowedOperationsPerNfType(managementNfService.getAllowedOperationsPerNfType());
            discoveryNfService.setApiPrefix(managementNfService.getApiPrefix());
            discoveryNfService.setCapacity(managementNfService.getCapacity());
            discoveryNfService.setDefaultNotificationSubscriptions(managementNfService.getDefaultNotificationSubscriptions());
            discoveryNfService.setFqdn(managementNfService.getFqdn());
            discoveryNfService.setInterPlmnFqdn(managementNfService.getInterPlmnFqdn());
            discoveryNfService.setIpEndPoints(managementNfService.getIpEndPoints());
            discoveryNfService.setLoad(managementNfService.getLoad());
            discoveryNfService.setLoadTimeStamp(managementNfService.getLoadTimeStamp());
            discoveryNfService.setNfServiceSetIdList(managementNfService.getNfServiceSetIdList());
            discoveryNfService.setNfServiceStatus(managementNfService.getNfServiceStatus());
            discoveryNfService.setOauth2Required(managementNfService.getOauth2Required());
            discoveryNfService.setPerPlmnSnssaiList(managementNfService.getPerPlmnSnssaiList());
            discoveryNfService.setPriority(managementNfService.getPriority());
            discoveryNfService.setRecoveryTime(managementNfService.getRecoveryTime());
            discoveryNfService.setScheme(managementNfService.getScheme());
            discoveryNfService.setServiceInstanceId(managementNfService.getServiceInstanceId());
            discoveryNfService.setServiceName(managementNfService.getServiceName());
            discoveryNfService.setsNssais(managementNfService.getsNssais());
            discoveryNfService.setSupportedFeatures(managementNfService.getSupportedFeatures());
            discoveryNfService.setSupportedVendorSpecificFeatures(managementNfService.getSupportedVendorSpecificFeatures());
            discoveryNfService.setVendorId(managementNfService.getVendorId());
            discoveryNfService.setVersions(managementNfService.getVersions());

            return discoveryNfService;
        }

        private final CacheOfSearchResults storedSearchResults = new CacheOfSearchResults();

        private String lastSearchId;

        private BiConsumer<RoutingContext, Event> handleSearchNfInstances = (context,
                                                                             event) ->
        {
            log.debug("query='{}'", context.request().query());
            log.debug("params=\n{}", context.request().params());

            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String requesterNfType = getRequesterNfType(context);
            final List<PlmnId> requesterPlmnList = getRequesterPlmnList(context);
            final List<ExtSnssai> requesterSnssais = getRequesterSnssais(context);
            final String targetNfSetId = getTargetNfSetId(context);
            final String targetNfServiceSetId = getTargetNfServiceSetId(context);
            final String targetNfType = getTargetNfType(context);
            final List<PlmnId> targetPlmnList = getTargetPlmnList(context);
            final String preferredLocality = getPreferredLocality(context);
            final List<String> scpDomainList = getScpDomainList(context);
            final Integer maxPayloadSize = getMaxPayloadSize(context);
            final Integer maxPayloadSizeInBytes = (maxPayloadSize != null) ? (maxPayloadSize * 1024) : null; // convert from kilobytes to bytes
            final List<String> serviceNames = getServiceNames(context);
            final String gpsi = getGpsi(context);
            final String supi = getSupi(context);
            final Integer noChangePriority = 0;
            final boolean malformed = getMalformedFlag(context.request().params());

            final ParsedHeaderValues headers = context.parsedHeaders();
            final List<String> acceptEncodingList = getAcceptEncoding(headers);
            final boolean gzipSupported = acceptEncodingList.stream().anyMatch("gzip"::equalsIgnoreCase);

            final String apiUri = context.request().absoluteURI().substring(0, context.request().absoluteURI().indexOf("/v1/") + 3);

            try
            {
                log.debug("apiUri={}, requester-nf-type={}, requester-plmn-list={}, requester-snssais={}, target-nf-type={}, target-nf-set-id={}, target-nf-service-set-id={}, target-plmn-list={}, preferred-locality={}, scp-domain-list={}, service-names={}, gpsi={}, supi={}, max-payload-size={}kB, accept-encoding={}",
                          apiUri,
                          requesterNfType,
                          requesterPlmnList,
                          requesterSnssais,
                          targetNfType,
                          targetNfSetId,
                          targetNfServiceSetId,
                          targetPlmnList,
                          preferredLocality,
                          scpDomainList,
                          serviceNames,
                          gpsi,
                          supi,
                          maxPayloadSize,
                          acceptEncodingList,
                          malformed);

                final SearchResult result = new SearchResult().validityPeriod(this.owner.getConfiguration().getNnrfNfDiscovery().getValidityPeriodInSecs());
                // result.setNrfSupportedFeatures(nrfSupportedFeatures); // Not implemented, yet

                final List<Predicate<NFProfile>> filters = List.of(nfProfile -> nfProfile.getNfType().equalsIgnoreCase(targetNfType)
                                                                                && matchServiceNames(serviceNames, nfProfile)//
                                                                                && matchTargetNfSetId(targetNfSetId, nfProfile)
                                                                                && matchTargetNfServiceSetId(targetNfServiceSetId, nfProfile)
                                                                                && matchTargetPlmnList(targetPlmnList, nfProfile)
                                                                                && matchLocality(preferredLocality, nfProfile)//
                                                                                && matchScpDomainList(scpDomainList, nfProfile)//
                                                                                && matchSpecificInfo(gpsi, supi, nfProfile),
                                                                   nfProfile -> matchRequesterNfType(requesterNfType, nfProfile),
                                                                   nfProfile -> matchRequesterPlmnList(requesterPlmnList, nfProfile),
                                                                   nfProfile -> matchRequesterSnssais(requesterSnssais, nfProfile));

                if (gpsi != null)
                {
                    this.owner.nfInstancesGetByGpsi(targetNfType, gpsi, filters).forEach(nfProfile ->
                    {
                        result.addNfInstancesItem(toDiscoveryNfProfile(nfProfile, serviceNames, noChangePriority));
                        result.putNfInstanceListItem(nfProfile.getNfInstanceId().toString(), new NfInstanceInfo().nrfDiscApiUri(apiUri));
                    });
                }
                else if (supi != null)
                {
                    this.owner.nfInstancesGetBySupi(targetNfType, supi, filters).forEach(nfProfile ->
                    {
                        result.addNfInstancesItem(toDiscoveryNfProfile(nfProfile, serviceNames, noChangePriority));
                        result.putNfInstanceListItem(nfProfile.getNfInstanceId().toString(), new NfInstanceInfo().nrfDiscApiUri(apiUri));
                    });
                }
                else if (targetNfSetId != null)
                {
                    this.owner.nfInstancesGetByNfSetId(targetNfType, targetNfSetId, filters).forEach(nfProfile ->
                    {
                        result.addNfInstancesItem(toDiscoveryNfProfile(nfProfile, serviceNames, noChangePriority));
                        result.putNfInstanceListItem(nfProfile.getNfInstanceId().toString(), new NfInstanceInfo().nrfDiscApiUri(apiUri));
                    });
                }
                else
                {
                    this.owner.nfInstancesGet(targetNfType, filters, null).forEach(nfProfile ->
                    {
                        result.addNfInstancesItem(toDiscoveryNfProfile(nfProfile, serviceNames, noChangePriority));
                        result.putNfInstanceListItem(nfProfile.getNfInstanceId().toString(), new NfInstanceInfo().nrfDiscApiUri(apiUri));
                    });
                }

                // Add in the result the NF instances (if any) with different locality from
                // the preferred locality when the result with the preferred locality is empty.

                if (result.getNfInstances().isEmpty() && preferredLocality != null && !preferredLocality.isEmpty())
                {
                    final List<Predicate<NFProfile>> filtersWithoutLocality = List.of(nfProfile -> nfProfile.getNfType().equalsIgnoreCase(targetNfType)
                                                                                                   && matchServiceNames(serviceNames, nfProfile)
                                                                                                   && matchTargetNfSetId(targetNfSetId, nfProfile)
                                                                                                   && matchTargetNfServiceSetId(targetNfServiceSetId, nfProfile)
                                                                                                   && matchScpDomainList(scpDomainList, nfProfile)
                                                                                                   && matchSpecificInfo(gpsi, supi, nfProfile),
                                                                                      nfProfile -> matchRequesterNfType(requesterNfType, nfProfile),
                                                                                      nfProfile -> matchRequesterPlmnList(requesterPlmnList, nfProfile),
                                                                                      nfProfile -> matchRequesterSnssais(requesterSnssais, nfProfile));

                    // The NRF set a lower priority for any additional NFs on
                    // the response not matching the preferred target NF location than
                    // those matching the preferred target NF location.
                    final Integer lowerPriority = new SecureRandom().nextInt(10) + 1;

                    this.owner.nfInstancesGet(targetNfType, filtersWithoutLocality, null).forEach(nfProfile ->
                    {
                        result.addNfInstancesItem(toDiscoveryNfProfile(nfProfile, serviceNames, lowerPriority));
                        result.putNfInstanceListItem(nfProfile.getNfInstanceId().toString(), new NfInstanceInfo().nrfDiscApiUri(apiUri));
                    });
                }

                /*
                 * Store the SearchResult in our internal DB for later retrieval by means of
                 * operations RetrieveCompleteSearch or RetrieveStoredSearch.
                 */
                result.setSearchId(this.storedSearchResults.put(result));
                this.lastSearchId = result.getSearchId();

                /*
                 * If max-payload-size query parameter is configured, limit NF profiles sent to
                 * consumer in order not to exceed max-payload-size. Set numNfInstComplete as
                 * the total number of NFInstances found by NRF.
                 */
                final List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> foundNfInstances = result.getNfInstances();
                final List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> limitedNfInstances = limitNfInstances(foundNfInstances,
                                                                                                                                   maxPayloadSizeInBytes);

                if (foundNfInstances.size() > limitedNfInstances.size() && maxPayloadSizeInBytes != null)
                {
                    result.setNumNfInstComplete(foundNfInstances.size());
                    result.setNfInstances(limitedNfInstances);
                }

                if (malformed)
                {
                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CACHE_CONTROL, " max-age=" + result.getValidityPeriod())
                           .putHeader(HD_ETAG, this.owner.etag.get().toString())
                           .end(json.writeValueAsString(result.getNfInstances().get(0).nfType(null)));
                }
                /*
                 * If "accept-encoding=gzip" header is received and the size of the result is
                 * greater than or equal to COMPRESSION_REQUIRED_SIZE (512 kilobytes), send
                 * compressed payload. Add "content-encoding=gzip" header in response to inform
                 * the client.
                 */
                else if (gzipSupported && json.writeValueAsBytes(result).length >= COMPRESSION_REQUIRED_SIZE)
                {
                    final byte[] compressedStream = gzipCompress(json.writeValueAsString(result));

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CACHE_CONTROL, " max-age=" + result.getValidityPeriod())
                           .putHeader(HD_ETAG, this.owner.etag.get().toString())
                           .putHeader(HD_CONTENT_ENCODING, "gzip")
                           .end(Buffer.buffer(compressedStream));
                }
                else
                {
                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CACHE_CONTROL, " max-age=" + result.getValidityPeriod())
                           .putHeader(HD_ETAG, this.owner.etag.get().toString())
                           .end(json.writeValueAsString(result));
                }
            }
            catch (final IllegalArgumentException e)
            {
                log.error("Error handling {} request. Cause: {}", operationId, e.toString());

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.BAD_REQUEST, "Invalid parameter value: " + e.toString()),
                                          null,
                                          null,
                                          e.getIllegalArgument());
            }
            catch (final Exception e)
            {
                log.error("Error handling {} request.", operationId, e);

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          requesterNfType,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleSearchRetrieve = (context,
                                                                          event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String searchId = context.request().getParam("searchId");

            final ParsedHeaderValues headers = context.parsedHeaders();
            final List<String> acceptEncodingList = getAcceptEncoding(headers);
            final boolean gzipSupported = acceptEncodingList.stream().anyMatch("gzip"::equalsIgnoreCase);

            try
            {
                log.debug("searchId={}, max-payload-size={}kB, accept-encoding={}", searchId, acceptEncodingList);

                final CacheOfSearchResults.Item item = this.storedSearchResults.get(searchId.equals("-1") ? this.lastSearchId : searchId);

                /*
                 * If "accept-encoding=gzip" header is received and the size of the result is
                 * greater than or equal to COMPRESSION_REQUIRED_SIZE (512 kilobytes), send
                 * compressed payload. Add "content-encoding=gzip" header in response to inform
                 * the client.
                 */
                if (gzipSupported && json.writeValueAsBytes(item.getResult()).length >= COMPRESSION_REQUIRED_SIZE)
                {
                    final byte[] compressedStream = gzipCompress(json.writeValueAsString(item.getResult()));

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CACHE_CONTROL, " max-age=" + item.getValidityPeriodSecs())
                           .putHeader(HD_ETAG, this.owner.etag.get().toString())
                           .putHeader(HD_CONTENT_ENCODING, "gzip")
                           .end(Buffer.buffer(compressedStream));
                }
                else
                {
                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CACHE_CONTROL, " max-age=" + item.getValidityPeriodSecs())
                           .putHeader(HD_ETAG, this.owner.etag.get().toString())
                           .end(json.writeValueAsString(item.getResult()));
                }
            }
            catch (final IllegalArgumentException e)
            {
                log.error("Error handling {} request. Cause: {}", operationId, e.toString());

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.BAD_REQUEST, "Invalid parameter value: " + e.toString()),
                                          null,
                                          null,
                                          e.getIllegalArgument());
            }
            catch (final Exception e)
            {
                log.error("Error handling {} request.", operationId, e);

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          null,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleScpDomainRoutingInfoGet = (context,
                                                                                   event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final boolean local = Boolean.parseBoolean(context.request().getParam("local", "false"));

            final ParsedHeaderValues headers = context.parsedHeaders();
            final List<String> acceptEncodingList = getAcceptEncoding(headers);
            final boolean gzipSupported = acceptEncodingList.stream().anyMatch("gzip"::equalsIgnoreCase);

            try
            {
                log.debug("local={}, accept-encoding={}", local, acceptEncodingList);

                final ScpDomainRoutingInformation result = new ScpDomainRoutingInformation();
                result.scpDomainList(Map.of("domain1", new ScpDomainConnectivity().connectedScpDomainList(List.of("cd1", "cd2"))));

                /*
                 * If "accept-encoding=gzip" header is received and the size of the result is
                 * greater than or equal to COMPRESSION_REQUIRED_SIZE (512 kilobytes), send
                 * compressed payload. Add "content-encoding=gzip" header in response to inform
                 * the client.
                 */
                if (gzipSupported && json.writeValueAsBytes(result).length >= COMPRESSION_REQUIRED_SIZE)
                {
                    final byte[] compressedStream = gzipCompress(json.writeValueAsString(result));

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_CONTENT_ENCODING, "gzip")
                           .end(Buffer.buffer(compressedStream));
                }
                else
                {
                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .end(json.writeValueAsString(result));
                }
            }
            catch (final IllegalArgumentException e)
            {
                log.error("Error handling {} request. Cause: {}", operationId, e.toString());

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.BAD_REQUEST, "Invalid parameter value: " + e.toString()),
                                          null,
                                          null,
                                          e.getIllegalArgument());
            }
            catch (final Exception e)
            {
                log.error("Error handling {} request.", operationId, e);

                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          null,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleScpDomainRoutingInfoSubscribe = (context,
                                                                                         event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            String subscriptionId = nextSubscriptionId();

            try
            {
                final ScpDomainRoutingInfoSubscription data = json.readValue(context.getBodyAsString(), ScpDomainRoutingInfoSubscription.class);

                final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis()
                                                             + 1000 * this.owner.getConfiguration().getNnrfNfManagement().getValidityPeriodInSecs());
                data.setValidityTime(instant.atZone(ZoneOffset.UTC).toOffsetDateTime());

                this.owner.subscriptionCreate(subscriptionId, data);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI() + "/" + subscriptionId)
                                           .getResponse()
                                           .getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(data));
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          subscriptionId,
                                          null,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleScpDomainRoutingInfoUnsubscribe = (context,
                                                                                           event) ->
        {
            final String subscriptionId = context.request().getParam("subscriptionID");

            this.owner.subscriptionRemove(subscriptionId);

            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NnrfNfDiscovery(final NrfSimulator owner)
        {
            super(owner);

            final String baseUri = (owner.webServerExtTls != null ? owner.webServerExtTls.get(0) : owner.webServerExt.get(0)).baseUri().toString();

            owner.links.put("discover",
                            new LinksValueSchema().href(new StringBuilder(baseUri).append(API_ROOT_NNRF_DISC_V1).append("/nf-instances").toString()));
            owner.nrfFeatures.put(ServiceName.NNRF_DISC, "D");
            owner.oauth2Required.put(ServiceName.NNRF_DISC, false);

            this.getHandlerByOperationId().put(Operation.NF_INSTANCES_SEARCH.value, this.handleSearchNfInstances);
            this.getHandlerByOperationId().put(Operation.SEARCH_RETRIEVE_COMPLETE.value, this.handleSearchRetrieve);
            this.getHandlerByOperationId().put(Operation.SEARCH_RETRIEVE_STORED.value, this.handleSearchRetrieve);
            this.getHandlerByOperationId().put(Operation.SCP_DOMAIN_ROUTING_INFO_GET.value, this.handleScpDomainRoutingInfoGet);
            this.getHandlerByOperationId().put(Operation.SCP_DOMAIN_ROUTING_INFO_SUBSCRIBE.value, this.handleScpDomainRoutingInfoSubscribe);
            this.getHandlerByOperationId().put(Operation.SCP_DOMAIN_ROUTING_INFO_UNSUBSCRIBE.value, this.handleScpDomainRoutingInfoUnsubscribe);
        }

        private boolean getMalformedFlag(final MultiMap multiMap)
        {
            final String param = multiMap.get("malformed-flag");
            boolean result = false;
            if (param != null)
            {
                result = Boolean.parseBoolean(param);
            }
            return result;
        }

        private Integer getMaxPayloadSize(final RoutingContext context)
        {
            final String param = context.request().getParam("max-payload-size");
            // Maximum payload size of the response expressed in kilo octets.
            // Has default value 124 and maximum value 2000.

            // Default value can be overwritten for testing purpose.
            Integer maxPayloadSize = this.owner.getConfiguration().getNnrfNfDiscovery().getMaxPayloadSize();

            Integer result = null;
            if (param != null)
            {
                result = Integer.parseInt(param);
                if (result == DEFAULT_MAX_PAYLOAD_SIZE && maxPayloadSize != DEFAULT_MAX_PAYLOAD_SIZE)
                    result = maxPayloadSize;
            }
            return result;
        }

        private List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> limitNfInstances(List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> foundNfInstances,
                                                                                                        Integer maxPayloadSizeInBytes)
        {
            final ArrayList<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> limitedNfInstances = new ArrayList<>();
            int limitedNfInstancesPayloadSize = 0;

            if (maxPayloadSizeInBytes == null)
                return limitedNfInstances;

            try
            {
                for (com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile nfInstance : foundNfInstances)
                {
                    final int nfInstanceSize = json.writeValueAsBytes(nfInstance).length;

                    if (limitedNfInstancesPayloadSize + nfInstanceSize <= maxPayloadSizeInBytes)
                    {
                        limitedNfInstances.add(nfInstance);
                        limitedNfInstancesPayloadSize = limitedNfInstancesPayloadSize + nfInstanceSize;
                    }
                    else
                        break;
                }
            }
            catch (JsonProcessingException e)
            {
                log.error("Error deserializing NFProfile", e);
            }

            return limitedNfInstances;
        }

        @Override
        protected Response getResponse()
        {
            return this.owner.getConfiguration().getNnrfNfDiscovery().getResponse();
        }
    }

    public static class NnrfNfManagement extends ApiHandler
    {
        public enum Operation
        {
            NF_INSTANCE_DEREGISTER("DeregisterNFInstance"),
            NF_INSTANCE_REGISTER("RegisterNFInstance"),
            NF_INSTANCE_UPDATE("UpdateNFInstance"),
            NF_INSTANCE_GET("GetNFInstance"),
            NF_INSTANCES_GET("GetNFInstances"),
            SUBSCRIPTION_CREATE("CreateSubscription"),
            SUBSCRIPTION_UPDATE("UpdateSubscription"),
            SUBSCRIPTION_REMOVE("RemoveSubscription");

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

        private static final String JP_NF_INSTANCE_ID = "/nfInstanceId";
        private static final String JP_NF_SET_ID_LIST = "/nfSetIdList";
        private static final String JP_NF_TYPE = "/nfType";

        private BiConsumer<RoutingContext, Event> handleNfInstanceDeregister = (context,
                                                                                event) ->
        {
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            final NFProfile nfProfile = this.owner.nfInstanceDeregister(nfInstanceId);
            this.sendNotificationIfNfDeregistered.accept(nfProfile);

            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();

        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceGet = (context,
                                                                         event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            try
            {
                final NFProfile nfProfile = this.owner.nfInstanceGet(nfInstanceId);

                if (nfProfile == null)
                {
                    this.owner.replyWithError(context,
                                              event.setResponse(HttpResponseStatus.NOT_FOUND, "NF instance has not been registered"),
                                              nfInstanceId,
                                              null,
                                              null);
                    return;
                }

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(nfProfile));
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          nfInstanceId != null ? nfInstanceId : null,
                                          null,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstancesGet = (context,
                                                                          event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            String nfType = null;

            try
            {
                final String paramNfType = context.request().getParam("nf-type");
                final String paramLimit = context.request().getParam("limit");

                if (paramNfType != null)
                {
                    try
                    {
                        nfType = paramNfType;
                    }
                    catch (IllegalArgumentException e)
                    {
                        this.owner.replyWithError(context,
                                                  event.setResponse(HttpResponseStatus.BAD_REQUEST, "Invalid parameter value: " + e.toString()),
                                                  null,
                                                  null,
                                                  "nf-type");
                        return;
                    }
                }

                Integer limit = null;

                if (paramLimit != null && !paramLimit.isEmpty())
                    limit = Integer.valueOf(paramLimit);

                final Set<NFProfile> list = this.owner.nfInstancesGet(nfType, limit);
                final List<Link> links = new ArrayList<>();

                list.forEach(p ->
                {
                    Link l = new Link();
                    l.setHref(p.getNfInstanceId().toString());
                    links.add(l);
                });

                final Link self = new Link();
                self.setHref(this.owner.nfInstanceId.toString());

                final Links data = new Links();
                data.setItem(links);
                data.setSelf(self);
                final JsonObject tmp = new JsonObject(json.writeValueAsString(data));
                final JsonObject result = new JsonObject();
                result.put("_links", tmp);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(result.toString());
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          null,
                                          nfType,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceRegister = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            String nfType = null;

            try
            {
                final NFProfile nfProfile = json.readValue(context.getBodyAsString(), NFProfile.class);

                nfType = nfProfile.getNfType();

                if (!nfProfile.getNfInstanceId().toString().equals(nfInstanceId))
                {
                    this.owner.replyWithError(context,
                                              event.setResponse(HttpResponseStatus.BAD_REQUEST,
                                                                "Parameter nfInstanceID does not match nfInstanceID in NFProfile"),
                                              nfInstanceId,
                                              nfType,
                                              "nfInstanceID");
                    return;
                }

                final NFProfile nfProfilePrev = this.owner.nfInstanceRegister(nfProfile);

                if (nfProfilePrev != null)
                {
                    nfProfile.setHeartBeatTimer(this.owner.getConfiguration().getNnrfNfManagement().getHeartBeatTimerInSecs());

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .end(json.writeValueAsString(nfProfile));

                    this.sendNotificationIfNfReregistered.accept(nfProfile, nfProfilePrev);
                }
                else
                {
                    nfProfile.setHeartBeatTimer(this.owner.getConfiguration().getNnrfNfManagement().getHeartBeatTimerInSecs());

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI()).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .putHeader(HD_LOCATION, context.request().absoluteURI()) // URI is already the location (PUT not POST).
                           .end(json.writeValueAsString(nfProfile));

                    this.sendNotificationIfNfRegistered.accept(nfProfile);
                }
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          nfInstanceId,
                                          nfType,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceUpdate = (context,
                                                                            event) ->
        {
            // This is only called for heart-beat purposes, i.e. when there was no change of
            // the NF profile.
            // Otherwise handleNfInstanceRegister() is called.

            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            String nfType = null;

            try
            {
                NFProfile nfProfile = this.owner.nfProfileDb.get(UUID.fromString(nfInstanceId));

                if (nfProfile == null)
                {
                    this.owner.replyWithError(context,
                                              event.setResponse(HttpResponseStatus.NOT_FOUND, "NF instance has not been registered"),
                                              nfInstanceId,
                                              null,
                                              null);
                    return;
                }

                nfType = nfProfile.getNfType();

                final HttpResponseStatus status = this.owner.getConfiguration().getNnrfNfManagement().getResponse().getStatus();

                if (status != null && 400 <= status.code())
                {
                    this.owner.replyWithError(context, event.setResponse(status, "Result code set by test case."), nfInstanceId, nfType, "nfInstanceID");
                    return;
                }

                nfProfile = Json.patch(nfProfile, context.getBodyAsString(), NFProfile.class);

                this.owner.nfInstanceRegister(nfProfile);

                // Heart-beat request shall return 204 (No content) if the NF profile has not
                // been changed meanwhile (by another client?).
                // If it has been changed, or if the heart-beat interval was modified by O&M
                // operation on NRF side, the full NF profile shall be returned in a 200 (OK)
                // response.

                if (this.owner.getConfiguration().getNnrfNfManagement().hasBeenUpdated() || status == HttpResponseStatus.OK)
                {
                    nfProfile.setHeartBeatTimer(this.owner.getConfiguration().getNnrfNfManagement().getHeartBeatTimerInSecs());

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .end(json.writeValueAsString(nfProfile));
                }
                else if (!this.owner.getConfiguration().getNnrfNfManagement().hasBeenUpdated() || status == HttpResponseStatus.NO_CONTENT)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
                }
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          nfInstanceId,
                                          nfType,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionCreate = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            String subscriptionId = nextSubscriptionId();

            try
            {
                final SubscriptionData data = json.readValue(context.getBodyAsString(), SubscriptionData.class);

                // For test only: if subscription ID in request starts with "test", take it.
                if (data.getSubscriptionId() != null && data.getSubscriptionId().startsWith("test"))
                    subscriptionId = data.getSubscriptionId();

                final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis()
                                                             + 1000 * this.owner.getConfiguration().getNnrfNfManagement().getValidityPeriodInSecs());
                data.setValidityTime(instant.atZone(ZoneOffset.UTC).toOffsetDateTime());

                this.owner.subscriptionCreate(subscriptionId, data.subscriptionId(subscriptionId));

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI() + "/" + subscriptionId)
                                           .getResponse()
                                           .getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(data));
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          subscriptionId,
                                          null,
                                          null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionRemove = (context,
                                                                              event) ->
        {
            final String subscriptionId = context.request().getParam("subscriptionID");

            this.owner.subscriptionRemove(subscriptionId);

            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionUpdate = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String subscriptionId = context.request().getParam("subscriptionID");

            try
            {
                SubscriptionData data = (SubscriptionData) this.owner.subscriptions.get(subscriptionId);

                if (data == null)
                {
                    this.owner.replyWithError(context,
                                              event.setResponse(HttpResponseStatus.NOT_FOUND, "Subscription does not exist"),
                                              subscriptionId,
                                              null,
                                              null);
                    return;
                }

                final HttpResponseStatus status = this.owner.getConfiguration().getNnrfNfManagement().getResponse().getStatus();

                if (status != null && 400 <= status.code())
                {
                    this.owner.replyWithError(context, event.setResponse(status, "Result code set by test case."), subscriptionId, null, "subscriptionID");
                    return;
                }

                data = Json.patch(data, context.getBodyAsString(), SubscriptionData.class);

                this.owner.subscriptionUpdate(subscriptionId, data);

                // TS 29.510, section 5.2.2.5.6:
                // If the NRF accepts the extension of the lifetime of the
                // subscription, and it accepts the requested value for the "validityTime"
                // attribute, a response with status code "204 No Content" shall be returned.
                // If the NRF accepts the extension of the lifetime of the
                // subscription, but it assigns a validity time different than the value
                // suggested by the NF Service Consumer, a "200 OK" response code shall be
                // returned. The response shall contain the new resource representation of the
                // "subscription" resource, which includes the new validity time, as determined
                // by the NRF, after which the subscription becomes invalid.

                if (this.owner.getConfiguration().getNnrfNfManagement().hasBeenUpdated() || status != HttpResponseStatus.OK)
                {
                    final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis()
                                                                 + 1000 * this.owner.getConfiguration().getNnrfNfManagement().getValidityPeriodInSecs());
                    data.setValidityTime(instant.atZone(ZoneOffset.UTC).toOffsetDateTime());

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                           .end(json.writeValueAsString(data));
                }
                else if (!this.owner.getConfiguration().getNnrfNfManagement().hasBeenUpdated() || status == HttpResponseStatus.NO_CONTENT)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
                }
            }
            catch (final Exception e)
            {
                this.owner.replyWithError(context,
                                          event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            "Error processing " + operationId + " request. Cause: " + e.toString()),
                                          subscriptionId.toString(),
                                          null,
                                          null);
            }
        };

        private final String uriNfInstances;

        private Consumer<NFProfile> sendNotificationIfNfDeregistered = nfProfile ->
        {
            if (nfProfile == null)
                return;

            for (Entry<String, Object> entry : this.owner.subscriptions.entrySet())
            {
                final SubscriptionData data = (SubscriptionData) entry.getValue();

                if (data.getValidityTime() != null && data.getValidityTime().isAfter(OffsetDateTime.now())
                    && (data.getReqNotifEvents() == null || data.getReqNotifEvents().contains(NotificationEventType.NF_DEREGISTERED)))
                {
                    final String uri = ((SubscriptionData) entry.getValue()).getNfStatusNotificationUri();
                    final NotificationData ntf = new NotificationData();
                    ntf.setEvent(NotificationEventType.NF_DEREGISTERED);

                    final SubscrCond subscrCond = data.getSubscrCond();

                    if (subscrCond == null)
                    {
                        this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                    }
                    else
                    {
                        if (this.attributeIsMonitored(JP_NF_INSTANCE_ID, data.getNotifCondition())//
                            && nfProfile.getNfInstanceId() != null && nfProfile.getNfInstanceId().equals(subscrCond.getNfInstanceId()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                        else if (this.attributeIsMonitored(JP_NF_SET_ID_LIST, data.getNotifCondition())//
                                 && nfProfile.getNfSetIdList() != null && nfProfile.getNfSetIdList().contains(subscrCond.getNfSetId()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                        else if (this.attributeIsMonitored(JP_NF_TYPE, data.getNotifCondition())//
                                 && nfProfile.getNfType() != null && nfProfile.getNfType().equalsIgnoreCase(subscrCond.getNfType()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                    }
                }
            }
        };

        private BiConsumer<NFProfile, NFProfile> sendNotificationIfNfReregistered = (nfProfile,
                                                                                     nfProfilePrev) ->
        {
            if (nfProfile == null)
                return;

            List<Json.Patch> patches = null;

            try
            {
                patches = Json.diff(nfProfilePrev, nfProfile);
                log.info("patches={}", patches);
            }
            catch (IOException e)
            {
                log.error("Error comparing NF profiles. Cause: {}", e.toString());
                return;
            }

            for (Entry<String, Object> entry : this.owner.subscriptions.entrySet())
            {
                final SubscriptionData data = (SubscriptionData) entry.getValue();

                if (data.getValidityTime() != null && data.getValidityTime().isAfter(OffsetDateTime.now())
                    && (data.getReqNotifEvents() == null || data.getReqNotifEvents().contains(NotificationEventType.NF_PROFILE_CHANGED)))
                {
                    final List<ChangeItem> changes = this.adjustDiff(data.getNotifCondition(), patches);

                    try
                    {
                        log.info("changes={}", json.writeValueAsString(changes));
                    }
                    catch (JsonProcessingException e)
                    {
                    }

                    if (changes.isEmpty())
                        continue;

                    final String uri = ((SubscriptionData) entry.getValue()).getNfStatusNotificationUri();
                    final NotificationData ntf = new NotificationData();
                    ntf.setEvent(NotificationEventType.NF_PROFILE_CHANGED);

                    final SubscrCond subscrCond = data.getSubscrCond();

                    if (subscrCond == null)
                    {
                        log.info("subscrCond == null");
                        ntf.setNfProfile(nfProfile); // Send profile rather than patches as there could be a lot of them.
                        this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                    }
                    else
                    {
                        if (this.attributeIsMonitored(JP_NF_INSTANCE_ID, data.getNotifCondition())//
                            && nfProfile.getNfInstanceId() != null && nfProfile.getNfInstanceId().equals(subscrCond.getNfInstanceId()))
                        {
                            ntf.setProfileChanges(changes);
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                        else if (this.attributeIsMonitored(JP_NF_SET_ID_LIST, data.getNotifCondition()))
                        {
                            boolean isInSetIds = nfProfile.getNfSetIdList() != null && nfProfile.getNfSetIdList().contains(subscrCond.getNfSetId());
                            boolean isInPrevSetIds = nfProfilePrev.getNfSetIdList() != null && nfProfilePrev.getNfSetIdList().contains(subscrCond.getNfSetId());
                            log.info("nfProfile.getNfSetIdList()={}, nfProfilePrev.getNfSetIdList()={}",
                                     nfProfile.getNfSetIdList(),
                                     nfProfilePrev.getNfSetIdList());

                            log.info("isInSetIds={}, isInPrevSetIds={}", isInSetIds, isInPrevSetIds);

                            if (isInSetIds && !isInPrevSetIds)
                            {
                                ntf.setNfProfile(nfProfile); // Send profile rather than patches (according to standard).
                                ntf.setConditionEvent(ConditionEventType.NF_ADDED);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                            else if (!isInSetIds && isInPrevSetIds)
                            {
                                ntf.setConditionEvent(ConditionEventType.NF_REMOVED);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                            else if (isInSetIds && isInPrevSetIds)
                            {
                                ntf.setProfileChanges(changes);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                        }
                        else if (this.attributeIsMonitored(JP_NF_TYPE, data.getNotifCondition()))
                        {
                            boolean nfTypeMeetsCondition = nfProfile.getNfType() != null && nfProfile.getNfType().equalsIgnoreCase(subscrCond.getNfType());
                            boolean nfTypePrevMeetsCondition = nfProfilePrev.getNfType() != null
                                                               && nfProfilePrev.getNfType().equalsIgnoreCase(subscrCond.getNfType());

                            log.info("nfTypeMeetsCondition={}, nfTypePrevMeetsCondition={}", nfTypeMeetsCondition, nfTypePrevMeetsCondition);

                            if (nfTypeMeetsCondition && !nfTypePrevMeetsCondition)
                            {
                                ntf.setNfProfile(nfProfile); // Send profile rather than patches (according to standard).
                                ntf.setConditionEvent(ConditionEventType.NF_ADDED);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                            else if (!nfTypeMeetsCondition && nfTypePrevMeetsCondition)
                            {
                                ntf.setConditionEvent(ConditionEventType.NF_REMOVED);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                            else if (nfTypeMeetsCondition && nfTypePrevMeetsCondition)
                            {
                                ntf.setProfileChanges(changes);
                                this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                            }
                        }
                    }
                }
            }
        };

        private Consumer<NFProfile> sendNotificationIfNfRegistered = nfProfile ->
        {
            if (nfProfile == null)
                return;

            for (Entry<String, Object> entry : this.owner.subscriptions.entrySet())
            {
                final SubscriptionData data = (SubscriptionData) entry.getValue();

                if (data.getValidityTime() != null && data.getValidityTime().isAfter(OffsetDateTime.now())
                    && (data.getReqNotifEvents() == null || data.getReqNotifEvents().contains(NotificationEventType.NF_REGISTERED)))
                {
                    final String uri = ((SubscriptionData) entry.getValue()).getNfStatusNotificationUri();
                    final NotificationData ntf = new NotificationData();
                    ntf.setEvent(NotificationEventType.NF_REGISTERED);
                    ntf.setNfProfile(nfProfile);

                    final SubscrCond subscrCond = data.getSubscrCond();

                    if (subscrCond == null)
                    {
                        this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                    }
                    else
                    {
                        if (this.attributeIsMonitored(JP_NF_INSTANCE_ID, data.getNotifCondition())//
                            && nfProfile.getNfInstanceId() != null && nfProfile.getNfInstanceId().equals(subscrCond.getNfInstanceId()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                        else if (this.attributeIsMonitored(JP_NF_SET_ID_LIST, data.getNotifCondition())//
                                 && nfProfile.getNfSetIdList() != null && nfProfile.getNfSetIdList().contains(subscrCond.getNfSetId()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                        else if (this.attributeIsMonitored(JP_NF_TYPE, data.getNotifCondition())//
                                 && nfProfile.getNfType() != null && nfProfile.getNfType().equalsIgnoreCase(subscrCond.getNfType()))
                        {
                            this.sendNotification(uri, nfProfile.getNfInstanceId(), ntf, entry.getKey());
                        }
                    }
                }
            }
        };

        public NnrfNfManagement(final NrfSimulator owner)
        {
            super(owner);

            final String baseUri = (owner.webServerExtTls != null ? owner.webServerExtTls.get(0) : owner.webServerExt.get(0)).baseUri().toString();

            owner.links.put("manage", new LinksValueSchema().href(new StringBuilder(baseUri).append(API_ROOT_NNRF_NFM_V1).append("/nf-instances").toString()));
            owner.links.put("subscribe",
                            new LinksValueSchema().href(new StringBuilder(baseUri).append(API_ROOT_NNRF_NFM_V1).append("/subscriptions").toString()));
            owner.nrfFeatures.put(ServiceName.NNRF_NFM, "1");
            owner.oauth2Required.put(ServiceName.NNRF_NFM, false);

            this.uriNfInstances = new StringBuilder(baseUri).append(API_ROOT_NNRF_NFM_V1).append("/nf-instances").toString();
            owner.webClient.dispose();

            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_DEREGISTER.value, this.handleNfInstanceDeregister);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_REGISTER.value, this.handleNfInstanceRegister);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_UPDATE.value, this.handleNfInstanceUpdate);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_GET.value, this.handleNfInstanceGet);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCES_GET.value, this.handleNfInstancesGet);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_CREATE.value, this.handleSubscriptionCreate);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_REMOVE.value, this.handleSubscriptionRemove);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_UPDATE.value, this.handleSubscriptionUpdate);
        }

        private List<ChangeItem> adjustDiff(final NotifCondition cond,
                                            final List<Json.Patch> patches)
        {
            final List<ChangeItem> result = new ArrayList<>();

            final List<String> monitoredAttributes = cond.getMonitoredAttributes();
            final List<String> unmonitoredAttributes = cond.getUnmonitoredAttributes();

            if (monitoredAttributes != null)
            {
                monitoredAttributes.forEach(attr -> patches//
                                                           .forEach(patch ->
                                                           {
                                                               if (patch.getPath().matches(attr + "(/[0-9]+)?"))
                                                                   result.add(new ChangeItem().op(patch.getOp().name())
                                                                                              .path(patch.getPath())
                                                                                              .newValue(patch.getValue()));
                                                           }));
            }
            else if (unmonitoredAttributes != null)
            {
                patches.forEach(patch -> unmonitoredAttributes//
                                                              .forEach(attr ->
                                                              {
                                                                  if (!patch.getPath().equals(attr))
                                                                      result.add(new ChangeItem().op(patch.getOp().name())
                                                                                                 .path(patch.getPath())
                                                                                                 .newValue(patch.getValue()));
                                                              }));
            }
            else
            {
                patches.forEach(patch -> result.add(new ChangeItem().op(patch.getOp().name()).path(patch.getPath()).newValue(patch.getValue())));
            }

            return result;
        }

        private boolean attributeIsMonitored(final String jsonPointer /* e.g. "/nfType" */,
                                             final NotifCondition cond)
        {
            if (cond == null)
                return true;

            final List<String> monitoredAttributes = cond.getMonitoredAttributes();
            final List<String> unmonitoredAttributes = cond.getUnmonitoredAttributes();

            if (monitoredAttributes != null)
            {
                return monitoredAttributes.contains(jsonPointer);
            }
            else if (unmonitoredAttributes != null)
            {
                return !unmonitoredAttributes.contains(jsonPointer);
            }
            else
            {
                return true;
            }
        }

        private void sendNotification(final String uri,
                                      final UUID nfInstanceId,
                                      final NotificationData ntf,
                                      String subscriptionId)
        {
            try
            {
                ntf.setNfInstanceUri(new StringBuilder(this.uriNfInstances).append("/").append(nfInstanceId.toString()).toString());

                final String body = json.writeValueAsString(ntf);

                log.info("Subscription '{}': sending notification to '{}', body={}", subscriptionId, uri, body);

                this.owner.webClient.postAbs(uri)
                                    .putHeader(HD_CONTENT_TYPE, ntf.getProfileChanges() != null ? CT_APPLICATION_JSON_PATCH_JSON : CT_APPLICATION_JSON)
                                    .rxSendBuffer(Buffer.buffer(body))
                                    .subscribeOn(Schedulers.io())
                                    .timeout(3, TimeUnit.SECONDS)
                                    .subscribe(resp -> log.info("Subscription '{}': notification sent to '{}', result={}",
                                                                subscriptionId,
                                                                uri,
                                                                HttpResponseStatus.valueOf(resp.statusCode())),
                                               err -> log.error("Subscription '{}': error sending notification to '{}'. Cause: {}",
                                                                subscriptionId,
                                                                uri,
                                                                err.toString()));
            }
            catch (JsonProcessingException e)
            {
                log.error("Subscription '{}': could not send notification to '{}'. Cause: {}", subscriptionId, uri, e.toString());
            }
        }

        @Override
        protected Response getResponse()
        {
            return this.owner.getConfiguration().getNnrfNfManagement().getResponse();
        }
    }

    @JsonPropertyOrder({ "countInHttpRequestsPerIpFamily",
                         "countOutHttpResponsesPerIpFamily",
                         "countInHttpRequests",
                         "countOutHttpResponsesPerStatus",
                         "historyOfEvents" })
    public static class Statistics
    {
        // To keep the log readable, just print countInHttpRequests and
        // countOutHttpResponsesPerStatus.

        @JsonIgnore
        private final Count.Pool countInHttpRequestsPerIpFamily;

        @JsonIgnore
        private final Count.Pool countOutHttpResponsesPerIpFamily;

        @JsonProperty("countInHttpRequests")
        private final Count countInHttpRequests;

        @JsonProperty("countOutHttpResponsesPerStatus")
        private final Count.Pool countOutHttpResponsesPerStatus;

        @JsonIgnore
        private final Event.Sequence historyOfEvents;

        public Statistics(final String nfInstanceId)
        {
            this.countInHttpRequestsPerIpFamily = new Count.Pool();
            this.countOutHttpResponsesPerIpFamily = new Count.Pool();
            this.countInHttpRequests = new Count();
            this.countOutHttpResponsesPerStatus = new Count.Pool();
            this.historyOfEvents = new Event.Sequence(nfInstanceId);
        }

        public void clear()
        {
            this.countInHttpRequestsPerIpFamily.clear();
            this.countOutHttpResponsesPerIpFamily.clear();
            this.countInHttpRequests.clear();
            this.countOutHttpResponsesPerStatus.clear();
            this.historyOfEvents.clear();
        }

        @JsonIgnore
        public Count getCountInHttpRequests()
        {
            return this.countInHttpRequests;
        }

        @JsonIgnore
        public Count.Pool getCountInHttpRequestsPerIpFamily()
        {
            return this.countInHttpRequestsPerIpFamily;
        }

        @JsonIgnore
        public Count.Pool getCountOutHttpResponsesPerIpFamily()
        {
            return this.countOutHttpResponsesPerIpFamily;
        }

        @JsonIgnore
        public Count.Pool getCountOutHttpResponsesPerStatus()
        {
            return this.countOutHttpResponsesPerStatus;
        }

        @JsonIgnore
        public Event.Sequence getHistoryOfEvents()
        {
            return this.historyOfEvents;
        }
    }

    private static abstract class ApiHandler implements IfApiHandler
    {
        protected final NrfSimulator owner;

        protected final Map<String, BiConsumer<RoutingContext, Event>> handlerByOperationId;

        protected ApiHandler(final NrfSimulator owner)
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
            final IpFamily ipFamily = context.get(OpenApiTask.DataIndex.IP_FAMILY.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final Event event = new Event(operationId, String.class.getName(), context.request().path());

            if (nfInstanceId != null)
                log.debug("{}: Received {} request for NF-instance {}.", ipFamily, operationId, nfInstanceId);
            else
                log.debug("{}: Received {} request.", ipFamily, operationId);

            this.owner.getNfInstances().get(nfInstanceId).getStatistics().getCountInHttpRequests().inc();
            this.owner.getNfInstances().get(null).getStatistics().getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            log.debug("counters={}", this.owner.getNfInstances());

            if (!Optional.ofNullable(this.owner.getConfiguration().getLoadTestMode()).orElse(this.owner.defaultLoadTestMode).isEnabled())
            {
                this.owner.getNfInstances().get(nfInstanceId).getStatistics().getHistoryOfEvents().put(event);
                this.owner.getNfInstances().get(nfInstanceId).addContext(context);
            }

            if (this.getResponse().doDrop())
                return;

            if (this.getResponse().getDelayInMillis() > 0)
            {
                context.vertx().setTimer(this.getResponse().getDelayInMillis(), t ->
                {
                    log.info("{} response has been delayed by {} ms", operationId, this.getResponse().getDelayInMillis());

                    this.handle(context, event);
                });
            }
            else
            {
                this.handle(context, event);
            }
        }

        private void handle(final RoutingContext context,
                            final Event event)
        {
            log.debug("Request headers: {}", context.request().headers());

            // Mirror the header as received from the client.
            Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

            Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

            try
            {
                final HttpResponseStatus status = this.getResponse().getStatus();

                if (status != null && 300 <= status.code())
                {
                    switch (status.code())
                    {
                        case 301:
                        case 302:
                        case 303:
                        case 305:
                        case 307:
                        case 308:
                            // Add mandatory location header containing the URL to redirect the request to.
                            context.response().putHeader(HD_LOCATION, Url.merge(this.getResponse().getRedirectUrl(), context.request().absoluteURI()));
                            this.owner.replyWithRedirect(context, event.setResponse(status, "Result code set by test case.").getResponse().getResult());
                            break;

                        default:
                            this.owner.replyWithError(context, event.setResponse(status, "Result code set by test case."), null, null, null);
                            break;
                    }

                    return;
                }

                final BiConsumer<RoutingContext, Event> handler = context.get(OpenApiTask.DataIndex.HANDLER.name());
                handler.accept(context, event);
            }
            catch (MalformedURLException e)
            {
                this.owner.replyWithError(context, event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.toString()), null, null, null);
            }
            finally
            {
                log.debug("Response headers: {}", context.response().headers());

                final IpFamily ipFamily = context.get(OpenApiTask.DataIndex.IP_FAMILY.name());
                final String nfInstanceId = context.request().getParam("nfInstanceID");

                try
                {
                    this.owner.getNfInstances()
                              .get(nfInstanceId)
                              .getStatistics()
                              .getCountOutHttpResponsesPerStatus()
                              .get(event.getResponse().getResultCode())
                              .inc();
                    this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();

                    log.debug("counters={}", this.owner.getNfInstances());
                }
                catch (Exception e)
                {
                    log.error("Exception while stepping counter", e);
                }
            }
        }

        protected abstract Response getResponse();
    }

    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private NrfSimulator handler;

        public CommandConfig(final NrfSimulator handler)
        {
            super("config", "Usage: command=config[&data=<json-formatted-config-data>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            final String data = (String) request.getAdditionalProperties().get("data");

            log.info("data='{}'", data);

            if (data != null)
            {
                try
                {
                    this.handler.setConfiguration(json.readValue(data, Configuration.class));
                }
                catch (Exception e)
                {
                    log.error("Error deserializing configuration", e);

                    result.setAdditionalProperty("errorMessage",
                                                 HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'data='" + data + "'. Details: "
                                                                 + e.getMessage() + ".");
                    return HttpResponseStatus.BAD_REQUEST;
                }
            }

            result.setAdditionalProperty("config", this.handler.getConfiguration());

            if (request.getAdditionalProperties().containsKey("nfInstanceId") && request.getAdditionalProperties().containsKey("timeSpanInMillis"))
            {
                final String nfInstanceId = (String) request.getAdditionalProperties().get("nfInstanceId");
                final long timeSpanInMillis = (long) request.getAdditionalProperties().get("timeSpanInMillis");

                result.setAdditionalProperty("updateHistory",
                                             this.handler.getNfInstances()
                                                         .get(nfInstanceId)
                                                         .getStatistics()
                                                         .getHistoryOfEvents()
                                                         .get(timeSpanInMillis, "UpdateNFInstance"));
                result.setAdditionalProperty("numberOfHeartbeats",
                                             this.handler.getNfInstances()
                                                         .get(nfInstanceId)
                                                         .getStatistics()
                                                         .getHistoryOfEvents()
                                                         .get(timeSpanInMillis, "UpdateNFInstance")
                                                         .size());
            }

            return HttpResponseStatus.OK;
        }
    }

    private static class CommandInfo extends MonitorAdapter.CommandBase
    {
        private NrfSimulator handler;

        public CommandInfo(final NrfSimulator handler)
        {
            super("info",
                  "Usage: command=info[&requestHeader=<name> | &allRequestHeaders[=<true|false>] | &responseHeader=<name> | &allResponseHeaders[=<true|false>] | &inRequests[=<true|false>] | &outResponses[=<opId>] | &clear[=<true|false>] | &authorityHeader[=<true|false>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            final String requestHeaderName = (String) request.getAdditionalProperties().get("requestHeader");
            final boolean requestBody = Boolean.parseBoolean((String) request.getAdditionalProperties().get("requestBody"));
            final String responseHeaderName = (String) request.getAdditionalProperties().get("responseHeader");
            final boolean inRequests = Boolean.parseBoolean((String) request.getAdditionalProperties().get("inRequests"));
            final String outResponses = (String) request.getAdditionalProperties().get("outResponses");
            final boolean clear = Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"));
            final boolean allRequestHeaders = Boolean.parseBoolean((String) request.getAdditionalProperties().get("allRequestHeaders"));
            final boolean allResponseHeaders = Boolean.parseBoolean((String) request.getAdditionalProperties().get("allResponseHeaders"));
            final boolean authorityHeader = Boolean.parseBoolean((String) request.getAdditionalProperties().get("authorityHeader"));

            log.debug("requestHeader='{}'", requestHeaderName);
            log.debug("requestBody='{}'", requestBody);
            log.debug("responseHeader='{}'", responseHeaderName);
            log.debug("inRequests='{}'", inRequests);
            log.debug("outResponses='{}'", outResponses);
            log.debug("clear='{}'", clear);
            log.debug("allRequestHeaders='{}'", allRequestHeaders);
            log.debug("allResponseHeaders='{}'", allResponseHeaders);
            log.debug("authorityHeader='{}'", authorityHeader);

            if (clear)
            {
                this.handler.getNfInstances().clear();
                return HttpResponseStatus.OK;
            }

            if (requestHeaderName != null)
            {
                result.setAdditionalProperty("requestHeader", this.handler.getNfInstances().get(null).getRequestHeader(requestHeaderName));
                return HttpResponseStatus.OK;
            }

            if (responseHeaderName != null)
            {
                result.setAdditionalProperty("responseHeader", this.handler.getNfInstances().get(null).getResponseHeader(responseHeaderName));
                return HttpResponseStatus.OK;
            }

            if (inRequests)
            {
                result.setAdditionalProperty("inRequests", this.handler.getNfInstances().get(null).getStatistics().getCountInHttpRequests());
                return HttpResponseStatus.OK;
            }

            if (allRequestHeaders)
            {
                result.setAdditionalProperty("allRequestHeaders", this.handler.getNfInstances().get(null).getAllRequestHeaders());
                return HttpResponseStatus.OK;
            }

            if (allResponseHeaders)
            {
                result.setAdditionalProperty("allResponseHeaders", this.handler.getNfInstances().get(null).getAllResponseHeaders());
                return HttpResponseStatus.OK;
            }

            if (authorityHeader)
            {
                result.setAdditionalProperty("authorityHeader", this.handler.getNfInstances().get(null).getAuthorityHeader());
                return HttpResponseStatus.OK;
            }

            if (outResponses != null)
            {
                result.setAdditionalProperty("outResponses", this.handler.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerStatus());
                return HttpResponseStatus.OK;
            }

            if (requestBody)
            {
                result.setAdditionalProperty("requestBody", this.handler.getNfInstances().get(null).getRequestBody());
                return HttpResponseStatus.OK;
            }

            result.setAdditionalProperty("errorMessage", HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'header'.");
            return HttpResponseStatus.BAD_REQUEST;
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

    private static final String API_ROOT_ACCESS_TOKEN = "/";
    private static final String API_ROOT_BOOTSTRAPPING = "/";
    private static final String API_ROOT_NNRF_DISC_V1 = "/nnrf-disc/v1";
    private static final String API_ROOT_NNRF_NFM_V1 = "/nnrf-nfm/v1";

    private static final String CT_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CT_APPLICATION_JSON_PATCH_JSON = "application/json-patch+json; charset=utf-8";
    private static final String CT_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";
    private static final String CT_APPLICATION_3GPP_HAL_JSON = "application/3gppHal+json; charset=utf-8";

    private static final int COMPRESSION_REQUIRED_SIZE = 512 * 1024; // 512 kilobytes
    private static final int DEFAULT_MAX_PAYLOAD_SIZE = 124; // kilobytes

    private static final String ENV_POD_IPS = "POD_IPS";

    private static final String HD_AUTHORITY = ":authority";
    private static final String HD_CACHE_CONTROL = "cache-control";
    private static final String HD_CONTENT_ENCODING = "content-encoding";
    private static final String HD_CONTENT_TYPE = "content-type";
    private static final String HD_ETAG = "etag";
    private static final String HD_HOST = "host";
    private static final String HD_LOCATION = "location";
    private static final String HD_3GPP_SBI_CORRELATION_INFO = "3gpp-sbi-correlation-info";
    private static final String HD_3GPP_SBI_NF_PEER_INFO = "3gpp-sbi-nf-peer-info";

    private static final int REGISTRY_TIMEOUT_SECS = 60;
    private static final int VALIDITY_PERIOD_SECS = 2 * 60 * 60; // 2 hours

    private static final Logger log = LoggerFactory.getLogger(NrfSimulator.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    private static final AtomicInteger subscriptionIdCnt = new AtomicInteger(0);

    public static void main(final String[] args)
    {
        int exitStatus = 0;

        log.info("Starting NRF simulator (R17), version: {}", VersionInfo.get());

        try
        {
            final Map<String, String> opts = new HashMap<>();

            for (String arg : args)
            {
                String[] tokens = arg.split("=");

                if (tokens.length != 2) // Arguments must look like that: host=127.0.0.1
                    continue;

                opts.put(tokens[0], tokens[1]);
            }

            final List<String> hosts = Stream.of(EnvVars.get(ENV_POD_IPS, opts.containsKey("host") ? opts.get("host") : isIpv6() ? "::" : "0.0.0.0").split(","))
                                             .map(ip -> ip.strip())
                                             .map(ip -> ip.contains(".") ? ip : "[" + ip + "]")
                                             .collect(Collectors.toList());

            log.info("POD_IPS={}, host={}, hosts={}", EnvVars.get(ENV_POD_IPS), opts.get("host"), hosts);

            NrfSimulator.Builder.of(hosts, opts.containsKey("port") ? Integer.valueOf(opts.get("port")) : 80)
                                .withPortTls(opts.containsKey("portTls") ? Integer.valueOf(opts.get("portTls")) : 443)
                                .withDefaultLoadTestMode(new Configuration.LoadTestMode().setEnabled(true)) // Enable load test mode when deployed.
                                .build()
                                .run();
        }
        catch (final Exception e)
        {
            log.error("Exception caught", e);
            exitStatus = 1;
        }

        log.info("Stopped NRF simulator.");

        System.exit(exitStatus);
    }

    private static boolean isIpv6() throws UnknownHostException
    {
        boolean result = InetAddress.getByName(EnvVars.get("ERIC_NRFSIM_PORT_80_TCP_ADDR")) instanceof Inet6Address;
        log.info("result={}", result);
        return result;
    }

    private static String nextSubscriptionId()
    {
        return new StringBuilder().append("subscription.").append(subscriptionIdCnt.getAndIncrement()).toString();
    }

    private final Configuration.LoadTestMode defaultLoadTestMode;
    private final NfInstance.Pool nfInstances;
    private final UUID nfInstanceId;
    private final Map<String, LinksValueSchema> links = new TreeMap<>();
    private final Map<String, String> nrfFeatures = new TreeMap<>();
    private final Map<String, Boolean> oauth2Required = new TreeMap<>();
    private final WebClient webClient;
    private final List<WebServer> webServerExt;
    private final List<WebServer> webServerExtTls;
    private final WebServer webServerInt;
    private final NnrfNfProfileDb nfProfileDb;
    private final Registry<String, Object> subscriptions;
    private final List<Disposable> disposables;
    private final MonitorAdapter monitored;
    private final BehaviorSubject<Configuration> configFlow;
    private final TrustedCert trustedCert;
    private final KeyCert keyCert;
    private final Object configLock;

    private Configuration config;
    private AtomicReference<UUID> etag;
    private Long lastUpdateInMillis;

    private NrfSimulator(final Configuration.LoadTestMode defaultLoadTestMode,
                         final List<String> hosts,
                         final Integer port,
                         final Integer portTls,
                         final String certificatesPath,
                         final KeyCert keyCert,
                         final TrustedCert trustedCert) throws IOException
    {
        this.defaultLoadTestMode = defaultLoadTestMode;
        this.lastUpdateInMillis = 0l;
        this.nfInstances = new NfInstance.Pool();
        this.nfInstanceId = UUID.randomUUID();
        this.keyCert = keyCert;
        this.trustedCert = trustedCert;

        final boolean useTls = (certificatesPath != null && !certificatesPath.isEmpty()) || (keyCert != null && trustedCert != null);

        log.info("loadTestMode={}, hosts={}, port={}, portTls={}, useTls={}", this.defaultLoadTestMode, hosts, port, portTls, useTls);

        this.webServerExt = hosts.stream()
                                 .map(host -> WebServer.builder().withHost(host).withPort(port).build(VertxInstance.get()))
                                 .collect(Collectors.toList());

        if (keyCert != null && trustedCert != null)
        {
            log.info("Creating tls server with DynamicTlsCertManager");
            final DynamicTlsCertManager certManager = DynamicTlsCertManager.create(new KeyCertProvider()
            {
                @Override
                public Flowable<KeyCert> watchKeyCert()
                {
                    return Single.just(keyCert).toFlowable();
                }
            }, new TrustedCertProvider()
            {

                @Override
                public Flowable<TrustedCert> watchTrustedCerts()
                {
                    return Single.just(trustedCert).toFlowable();
                }
            });

            this.webServerExtTls = hosts.stream()
                                        .map(host -> WebServer.builder()
                                                              .withHost(host)
                                                              .withPort(portTls)
                                                              .withDynamicTls(certManager)
                                                              .build(VertxInstance.get()))
                                        .collect(Collectors.toList());
        }
        else
        {
            log.info("Creating tls server with cert path");
            this.webServerExtTls = useTls ? hosts.stream()
                                                 .map(host -> WebServer.builder()
                                                                       .withHost(host)
                                                                       .withPort(portTls)
                                                                       .withTls(certificatesPath)
                                                                       .build(VertxInstance.get()))
                                                 .collect(Collectors.toList())
                                          : null;
        }

        this.webServerInt = WebServer.builder().withHost(Utils.getLocalAddress()).withPort(8080).build(VertxInstance.get());

        if (this.keyCert != null && this.trustedCert != null)
        {
            this.webClient = new WebClient(this.keyCert.getCertificate(), this.keyCert.getPrivateKey(), this.trustedCert.getTrustedCertificate());
        }
        else
        {
            this.webClient = new WebClient(certificatesPath + "/certificate.pem", certificatesPath + "/key.pem", certificatesPath + "/ca.pem");
        }

        final NnrfAccessToken handlerAccessToken = new NnrfAccessToken(this);
        final OpenApiServer.Context3 contextAccessToken = new OpenApiServer.Context3(Specs3gpp.R17_NNRF_ACCESS_TOKEN, handlerAccessToken);
        final NnrfBootstrapping handlerBootstrapping = new NnrfBootstrapping(this);
        final OpenApiServer.Context3 contextBootstrapping = new OpenApiServer.Context3(Specs3gpp.R17_NNRF_BOOTSTRAPPING, handlerBootstrapping);
        final NnrfNfDiscovery handlerNfDiscovery = new NnrfNfDiscovery(this);
        final OpenApiServer.Context3 contextNfDiscovery = new OpenApiServer.Context3(Specs3gpp.R17_NNRF_NF_DISCOVERY, handlerNfDiscovery);
        final NnrfNfManagement handlerNfManagement = new NnrfNfManagement(this);
        final OpenApiServer.Context3 contextNfManagement = new OpenApiServer.Context3(Specs3gpp.R17_NNRF_NF_MANAGEMENT, handlerNfManagement);

        final List<Context3> contexts = List.of(contextBootstrapping, contextAccessToken, contextNfDiscovery, contextNfManagement);

        this.webServerExt.forEach(webServer -> new OpenApiServer(webServer).configure2(IpFamily.of(webServer.getHttpOptions().getHost()), contexts));

        if (useTls)
            this.webServerExtTls.forEach(webServer ->
            {
                new OpenApiServer(webServer).configure2(IpFamily.of(webServer.getHttpOptions().getHost()), contexts);
                webServer.getHttpOptions().setSni(true);
            });

        this.nfProfileDb = new NnrfNfProfileDb(List.of(handlerNfManagement.sendNotificationIfNfDeregistered));
        this.subscriptions = new Registry<>(VALIDITY_PERIOD_SECS * 1000l);
        this.etag = new AtomicReference<>(UUID.randomUUID());

        this.disposables = new ArrayList<>();
        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(this),
                                                          new MonitorAdapter.CommandEsa(this),
                                                          new CommandConfig(this),
                                                          new CommandInfo(this)),
                                            Arrays.asList(new CommandConfig(this)));

        this.configLock = new Object();
        this.config = new Configuration();
        this.configFlow = BehaviorSubject.create();
        this.configFlow.observeOn(Schedulers.single()).subscribe(config ->
        {
            log.info("Applying new configuration: {}", config);

            final Instant start = Instant.now();

            this.nfProfileDb.clear();

            config.getNnrfNfManagement()
                  .getProvisioning()
                  .forEach(provisioning -> provisioning.getModifiers()//
                                                       .forEach(modifier ->
                                                       {
                                                           final AtomicInteger i = new AtomicInteger(-1);

                                                           while (i.incrementAndGet() < modifier.getReplicator())
                                                           {
                                                               final List<Json.Patch> patches = modifier.getPatches()
                                                                                                        .stream()
                                                                                                        .map(patch -> Json.Patch.of()
                                                                                                                                .op(patch.getOp())
                                                                                                                                .path(patch.getPath())
                                                                                                                                .value(String.format((String) patch.getValue(),
                                                                                                                                                     i.get())))
                                                                                                        .collect(Collectors.toList());
                                                               try
                                                               {
                                                                   @SuppressWarnings("unchecked")
                                                                   final LinkedHashMap<String, Object> copy = json.convertValue(provisioning.getNfProfile(),
                                                                                                                                LinkedHashMap.class);
                                                                   final NFProfile patched = Json.patch(copy, patches, NFProfile.class);
                                                                   this.nfInstanceRegister(patched);
                                                               }
                                                               catch (IOException e)
                                                               {
                                                                   log.error("New configuration is invalid. Cause: {}", e.toString());
                                                               }
                                                           }
                                                       }));

            log.info("Applying new configuration took {} s", Duration.between(start, Instant.now()).getSeconds());
        }, t -> log.error("Error applying new configuration. Cause: {}", t.toString()));
    }

    public Configuration getConfiguration()
    {
        synchronized (this.configLock)
        {
            return this.config;
        }
    }

    /**
     * @see com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandCounter.Provider#
     *      getCounters()
     */
    @Override
    public List<Counter> getCounters(final boolean readThenClear)
    {
        final List<Counter> result = new ArrayList<>();

        {
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequestsPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswersPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswers = new ArrayList<>();

            for (Iterator<Entry<String, NfInstance>> itInstance = this.nfInstances.iterator(); itInstance.hasNext();)
            {
                final Entry<String, NfInstance> instance = itInstance.next();

                final String[] key = instance.getKey().split(",");

                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList("nfInstanceId")).append('=').append(Arrays.asList(instance.getKey()));
                    inRequests.add(new Instance(b.toString(), (double) instance.getValue().getStatistics().getCountInHttpRequests().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountOutHttpResponsesPerStatus().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();

                    b.append(Arrays.asList("nfInstanceId", "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    outAnswers.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountInHttpRequestsPerIpFamily().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();

                    b.append(Arrays.asList("nfInstanceId", "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    inRequestsPerIpFamily.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountOutHttpResponsesPerIpFamily().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();

                    b.append(Arrays.asList("nfInstanceId", "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    outAnswersPerIpFamily.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }
            }

            result.add(new Counter("eric_nrfsim_http_in_requests_ipfamily_total", "Number of incoming HTTP requests per IP family", inRequestsPerIpFamily));
            result.add(new Counter("eric_nrfsim_http_out_answers_ipfamily_total", "Number of outgoing HTTP answers per IP family", outAnswersPerIpFamily));
            result.add(new Counter("eric_nrfsim_http_in_requests_total", "Number of incoming HTTP requests", inRequests));
            result.add(new Counter("eric_nrfsim_http_out_answers_total", "Number of outgoing HTTP answers", outAnswers));
        }

        return result;
    }

    @Override
    public List<Event.Sequence> getEsa()
    {
        final List<Event.Sequence> result = new ArrayList<>();

        for (Iterator<Entry<String, NfInstance>> it = this.nfInstances.iterator(); it.hasNext();)
            result.add(it.next().getValue().getStatistics().getHistoryOfEvents());

        return result;
    }

    public NfInstance.Pool getNfInstances()
    {
        return this.nfInstances;
    }

    public NFProfile nfInstanceDeregister(final String nfInstanceId)
    {
        this.etag.set(UUID.randomUUID());
        return this.nfProfileDb.remove(UUID.fromString(nfInstanceId));
    }

    public NFProfile nfInstanceGet(final String nfInstanceId)
    {
        return this.nfProfileDb.get(UUID.fromString(nfInstanceId));
    }

    public NFProfile nfInstanceGet(final UUID nfInstanceId)
    {
        return this.nfProfileDb.get(nfInstanceId);
    }

    public NFProfile nfInstanceRegister(final NFProfile nfProfile)
    {
        this.etag.set(UUID.randomUUID());
        return this.nfProfileDb.put(nfProfile.getNfInstanceId(), nfProfile);
    }

    public Set<NFProfile> nfInstancesGet(final String nfType,
                                         final Integer limit)
    {
        return this.nfInstancesGet(nfType, null, limit);
    }

    public Set<NFProfile> nfInstancesGet(final String nfType,
                                         final List<Predicate<NFProfile>> filters,
                                         final Integer limit)
    {
        final Set<NFProfile> nfProfiles = this.nfProfileDb.get(nfType, filters);

        if (limit == null || nfProfiles.size() <= limit)
            return nfProfiles;

        final Set<NFProfile> limitedNfProfiles = new HashSet<>();

        for (NFProfile p : nfProfiles)
        {
            if (limitedNfProfiles.size() >= limit)
                break;

            limitedNfProfiles.add(p);
        }

        return limitedNfProfiles;
    }

    public Set<NFProfile> nfInstancesGetByGpsi(final String nfType,
                                               final String gpsi,
                                               final List<Predicate<NFProfile>> filters)
    {
        return this.nfProfileDb.getByGpsi(nfType, gpsi, filters);
    }

    public Set<NFProfile> nfInstancesGetByNfSetId(final String nfType,
                                                  final String nfSetId,
                                                  final List<Predicate<NFProfile>> filters)
    {
        return this.nfProfileDb.getByNfSetId(nfType, nfSetId, filters);
    }

    public Set<NFProfile> nfInstancesGetBySupi(final String nfType,
                                               final String supi,
                                               final List<Predicate<NFProfile>> filters)
    {
        return this.nfProfileDb.getBySupi(nfType, supi, filters);
    }

    public void run()
    {
        this.disposables.add(this.update()
                                 .timeout(1500, TimeUnit.MILLISECONDS)
                                 .doOnError(t -> log.error("Error updating NF-register.", t))
                                 .onErrorReturn(e -> true)
                                 .repeatWhen(handler -> handler.delay(1, TimeUnit.SECONDS))
                                 .doOnSubscribe(d -> log.info("Started updating NF-register."))
                                 .doOnComplete(() -> log.info("Stopped updating NF-register."))
                                 .subscribe(d ->
                                 {
                                 }, t -> log.error("Stopped updating NF-register.", t)));

        Completable.complete()
                   .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(webServer -> webServer.startListener()))
                   .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls)
                                                                   .flatMapCompletable(webServer -> webServer.startListener())
                                                         : Completable.complete())
                   .andThen(this.webServerInt.startListener())
                   .andThen(this.monitored.start())
                   .andThen(this.nfProfileDb.start())
                   .andThen(this.subscriptions.start())
                   .andThen(Completable.create(emitter ->
                   {
                       log.info("Registering shutdown hook");
                       Runtime.getRuntime().addShutdownHook(new Thread(() ->
                       {
                           log.info("Shutdown hook called");
                           this.stop().blockingAwait();
                           emitter.onComplete();
                       }));
                   }))
                   .blockingAwait();
    }

    public Completable stop()
    {
        return Completable.complete()
                          .andThen(this.monitored.stop().onErrorComplete())
                          .andThen(this.nfProfileDb.stop().onErrorComplete())
                          .andThen(this.subscriptions.stop().onErrorComplete())
                          .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete()))
                          .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls)
                                                                          .flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete())
                                                                : Completable.complete())
                          .andThen(this.webServerInt.stopListener().onErrorComplete())
                          .andThen(Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)));
    }

    public Object subscriptionCreate(final String subscriptionId,
                                     final Object data)
    {
        return this.subscriptionUpdate(subscriptionId, data);
    }

    public void subscriptionRemove(final String subscriptionId)
    {
        this.subscriptions.remove(subscriptionId);
    }

    public Object subscriptionUpdate(final String subscriptionId,
                                     final Object data)
    {
        return this.subscriptions.put(subscriptionId, data);
    }

    private void replyWithError(final RoutingContext context,
                                final Event event,
                                final String instanceId,
                                final String type,
                                final String invalidParameter)
    {
        final ProblemDetails problem = new ProblemDetails();

        problem.setStatus(event.getResponse().getResultCode());
        problem.setCause(event.getResponse().getResultReasonPhrase());

        if (instanceId != null)
            problem.setInstance(instanceId);

        if (type != null)
            problem.setType(type);

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

        if (400 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 500)
        {
            log.warn(problemStr);
        }
        else if (500 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 600)
        {
            log.error(problemStr);
        }

        context.response().setStatusCode(event.getResponse().getResultCode()).putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON).end(problemStr);
    }

    private void replyWithRedirect(final RoutingContext context,
                                   final HttpResponseStatus status)
    {
        final RedirectResponse redirect = new RedirectResponse();

        String redirectStr;

        try
        {
            redirectStr = json.writeValueAsString(redirect);
        }
        catch (final JsonProcessingException e)
        {
            redirectStr = e.toString();
        }

        context.response().setStatusCode(status.code()).putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON).end(redirectStr);
    }

    private Single<Boolean> update()
    {
        return Single.just(true).doOnSuccess(b ->
        {
            final Integer heartBeatIntervalInMillis = this.getConfiguration().getNnrfNfManagement().getHeartBeatTimerInSecs() * 1000;

            // Make sure that the update interval is always twice the configured heart-beat
            // interval.
            this.nfProfileDb.setUpdateDelayInMillis(heartBeatIntervalInMillis * 2l);

            final Long now = System.currentTimeMillis();
            final boolean result = this.lastUpdateInMillis + heartBeatIntervalInMillis < now;

            if (result)
                this.lastUpdateInMillis = now;
            else
                return;

            log.debug("Updating.");

            final NFProfile nfProfile = new NFProfile();
            nfProfile.setNfInstanceId(this.nfInstanceId);
            nfProfile.setNfType(NFType.NRF);
            nfProfile.setNfStatus(NFStatus.REGISTERED);

            this.nfInstanceRegister(nfProfile);
        });
    }

    void setConfiguration(final Configuration config)
    {
        synchronized (this.configLock)
        {
            this.config = config;
        }

        this.configFlow.toSerialized().onNext(config);
    }
}
