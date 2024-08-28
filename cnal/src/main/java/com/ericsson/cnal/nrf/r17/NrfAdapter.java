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
 * Created on: Jan 8, 2019
 *     Author: eedstl
 */

package com.ericsson.cnal.nrf.r17;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.CertificateObserver;
import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.cnal.common.NrfCertificateInfo;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenReq;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenRsp;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.BootstrappingInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInfoSubscription;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInformation;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.StoredSearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ServiceName;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchItem;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.metrics.MetricRegister;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.exceptions.ProtocolViolationException;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;

public class NrfAdapter
{
    @SuppressWarnings("serial")
    public static class ProcessingException extends RuntimeException
    {
        public ProcessingException(final String message)
        {
            super(message);
        }

        public ProcessingException(final Throwable e)
        {
            super(e);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder({ "query" })
    public static class Query implements Comparable<Query>
    {
        public static class Builder
        {
            private final Map<String, Object> params;
            private StringBuilder paramNames;
            private StringBuilder query;
            private String separator;

            public Builder()
            {
                this.params = new HashMap<>();
                this.paramNames = new StringBuilder();
                this.query = new StringBuilder();
                this.separator = "?";
            }

            public Builder(final RoutingContext context)
            {
                this.params = new HashMap<>();
                this.paramNames = new StringBuilder();
                final String query = context.request().query();

                if (query.isEmpty())
                {
                    this.query = new StringBuilder();
                    this.separator = "?";
                }
                else
                {
                    this.query = new StringBuilder(query);

                    final SortedSet<String> names = new TreeSet<>(context.queryParams().names());

                    for (String name : names)
                    {
                        this.paramNames.append(this.paramNames.length() > 0 ? "," : "").append(name);

                        final List<String> queryParam = context.queryParam(name);

                        if (!queryParam.isEmpty())
                            this.params.put(name, queryParam);
                    }

                    this.separator = "&";
                }
            }

            public Builder add(final String name,
                               final Object value)
            {
                if (value != null)
                {
                    this.query.append(this.separator);
                    this.query.append(name);

                    final String paramVal = value.toString();

                    if (!paramVal.isEmpty())
                        this.query.append("=").append(paramVal);

                    this.separator = "&";

                    this.params.put(name, List.of(value));
                }

                return this;
            }

            public Query build()
            {
                return new Query(this.query.toString(), this.paramNames.toString(), this.params);
            }

            public Builder remove(final String name)
            {
                {
                    final String[] split = this.query.toString().split("&");

                    final StringBuilder b = new StringBuilder();

                    for (int i = 0; i < split.length; i++)
                    {
                        if (!split[i].startsWith(name + "="))
                            b.append(b.length() > 0 ? "&" : "").append(split[i]);
                    }

                    this.query = b;
                }

                {
                    final String[] split = this.paramNames.toString().split(",");

                    final StringBuilder b = new StringBuilder();

                    for (int i = 0; i < split.length; i++)
                    {
                        if (!split[i].equals(name))
                            b.append(b.length() > 0 ? "," : "").append(split[i]);
                    }

                    this.paramNames = b;
                }

                return this;
            }
        }

        @JsonIgnore
        private final Map<String, Object> params;

        @JsonIgnore
        private String paramNames;

        @JsonProperty("query")
        private String query;

        private Query(final String query,
                      final String paramNames,
                      final Map<String, Object> params)
        {
            this.params = params;
            this.paramNames = paramNames;
            this.query = query;
        }

        @Override
        public int compareTo(Query o)
        {
            return this.query.compareToIgnoreCase(o.query);
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
                return true;

            if (!(other instanceof Query))
                return false;

            final Query rhs = ((Query) other);
            return this.query == rhs.query || this.query != null && this.query.equals(rhs.query);
        }

        public Object getParam(final String name)
        {
            return this.params.get(name);
        }

        public Integer getParamAsInteger(final String name)
        {
            @SuppressWarnings("unchecked")
            final List<String> l = (List<String>) this.getParam(name);

            return l == null || l.isEmpty() ? null : Integer.parseInt(l.get(0));
        }

        public String getParamAsString(final String name)
        {
            @SuppressWarnings("unchecked")
            final List<String> l = (List<String>) this.getParam(name);

            return l == null || l.isEmpty() ? null : l.get(0);
        }

        public String getParamNames()
        {
            return this.paramNames;
        }

        @Override
        public int hashCode()
        {
            int result = 1;
            result = ((result * 31) + ((this.query == null) ? 0 : this.query.hashCode()));
            return result;
        }

        @Override
        public String toString()
        {
            log.debug("query={}", this.query);
            return this.query;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "query", "headers", "requestTimeoutMillis", "sbiNfPeerInfo" })
    public static class RequestContext
    {
        public static RequestContext of()
        {
            return new RequestContext();
        }

        public static RequestContext of(final int requestTimeoutMillis)
        {
            return new RequestContext().setRequestTimeoutMillis(requestTimeoutMillis);
        }

        public static RequestContext of(final int requestTimeoutMillis,
                                        final String query)
        {
            return new RequestContext().setRequestTimeoutMillis(requestTimeoutMillis).setQuery(query);
        }

        public static RequestContext of(final String query)
        {
            return new RequestContext().setQuery(query);
        }

        @JsonProperty("query")
        private String query = null;

        @JsonProperty("headers")
        private MultiMap headers = null;

        @JsonProperty("requestTimeoutMillis")
        private int requestTimeoutMillis = 0;

        @JsonProperty("sbiNfPeerInfo")
        private String sbiNfPeerInfo = null;

        private RequestContext()
        {
        }

        /**
         * Add a header for <code>name</code> and <code>value</code> passed if
         * <code>value</code> is not <code>null</code>.
         * 
         * @param name
         * @param value
         * @return This <code>RequestContext</code> object.
         */
        public RequestContext addHeader(final String name,
                                        final String value)
        {
            if (value != null)
            {
                if (this.headers == null)
                    this.headers = MultiMap.caseInsensitiveMultiMap();

                this.headers.add(name, value);
            }

            return this;
        }

        public MultiMap getHeaders()
        {
            return this.headers;
        }

        public String getQuery()
        {
            return this.query;
        }

        public int getRequestTimeoutMillis()
        {
            return this.requestTimeoutMillis;
        }

        public String getSbiNfPeerInfo()
        {
            return this.sbiNfPeerInfo;
        }

        public RequestContext setHeaders(final MultiMap headers)
        {
            this.headers = headers;
            return this;
        }

        public RequestContext setQuery(final String query)
        {
            this.query = query;
            return this;
        }

        public RequestContext setRequestTimeoutMillis(final int requestTimeoutMillis)
        {
            this.requestTimeoutMillis = requestTimeoutMillis;
            return this;
        }

        public RequestContext setSbiNfPeerInfo(final String sbiNfPeerInfo)
        {
            this.sbiNfPeerInfo = sbiNfPeerInfo;
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
            }

            return "";
        }
    }

    public static class Result<T>
    {
        private final HttpResponse<Buffer> response;
        private final T body;
        private final int statusCode;

        public Result(HttpResponse<Buffer> response)
        {
            this.response = response;
            this.body = null;
            this.statusCode = response.statusCode();
        }

        public Result(final HttpResponse<Buffer> response,
                      final T body)
        {
            this.response = response;
            this.body = body;
            this.statusCode = response.statusCode();
        }

        public Result(final int statusCode)
        {
            this.response = null;
            this.body = null;
            this.statusCode = statusCode;
        }

        public Result(final int statusCode,
                      final T body)
        {
            this.response = null;
            this.body = body;
            this.statusCode = statusCode;
        }

        /**
         * @return The body or null if there is no body.
         */
        public T getBody()
        {
            return this.body;
        }

        /**
         * If hasProblem() == true, this method must be used to get the body. Otherwise,
         * use getBody().
         * 
         * @return The body of the response or null.
         */
        public String getBodyAsString()
        {
            return this.response != null ? this.response.bodyAsString() : null;
        }

        /**
         * @param name The name of the header to be returned.
         * @return The value of the header or null if there is no header of the name
         *         passed.
         */
        public String getHeader(final String name)
        {
            return this.response != null ? this.response.getHeader(name) : null;
        }

        /**
         * @return The status code.
         */
        public int getStatusCode()
        {
            return this.statusCode;
        }

        /**
         * @return True if not (200 <= statusCode < 300).
         */
        public boolean hasProblem()
        {
            return this.statusCode < 200 || this.statusCode > 299;
        }

        @Override
        public String toString()
        {
            return new StringBuilder("statusCode=").append(this.statusCode)
                                                   .append(", response=")
                                                   .append(this.response)
                                                   .append(", body=")
                                                   .append(this.body)
                                                   .toString();
        }
    }

    private static class Redirector
    {
        private static final String ENV_NUM_REDIRECTS_MAX = "NUM_REDIRECTS_MAX";
        private static final int NUM_REDIRECTS_MAX_DEFAULT = 10;
        private final AtomicReference<HttpResponse<Buffer>> currResult = new AtomicReference<>();
        private final Url origUrl;
        private final AtomicReference<Url> currUrl;
        private final Map<URI, URI> urlHistory;

        public Redirector(final Url origUrl) throws URISyntaxException
        {
            this.origUrl = origUrl;
            this.currUrl = new AtomicReference<>(origUrl);
            this.urlHistory = List.of(origUrl.getUrl().toURI()).stream().collect(Collectors.toConcurrentMap(Function.identity(), Function.identity()));
        }

        public Url getCurrUrl()
        {
            return this.currUrl.get();
        }

        public BooleanSupplier isDone()
        {
            return () -> true; // DND-29176: decision to do no automatic redirection. Rather, trigger fail-over
                               // to another NRF of the NRF group for both 307 and 308.
//            return () ->
//            {
//                final HttpResponse<Buffer> result = this.currResult.get();
//                final int statusCode = result.statusCode();
//
//                return (statusCode != 307/* && statusCode != 308 trigger fail-over to another NRF of the NRF group */)
//                       || validateNewLocation(result.getHeader(HD_LOCATION), statusCode);
//            };
        }

        public void setCurrResult(final HttpResponse<Buffer> result)
        {
            this.currResult.set(result);
        }

        private boolean validateNewLocation(final String location,
                                            final int statusCode)
        {
            try
            {
                if (location == null)
                    throw new ProtocolViolationException(statusCode + ": response does not contain mandatory location header.");

                final URI redirectUri = new URL(Url.merge(location, this.origUrl.getUrl().toString())).toURI();
                log.debug("redirectUri={}", redirectUri);

                if (this.urlHistory.size() > Integer.parseInt(EnvVars.get(ENV_NUM_REDIRECTS_MAX, NUM_REDIRECTS_MAX_DEFAULT)))
                    throw new ProcessingException(statusCode + ": too many redirections: " + this.urlHistory.size());

                if (this.urlHistory.keySet().contains(redirectUri))
                    throw new ProcessingException(statusCode + ": redirection loop detected for location: '" + location + "'.");

                this.urlHistory.put(redirectUri, redirectUri);
                this.currUrl.set(new Url(redirectUri.toURL()));

                return false; // Repeat request to new location.
            }
            catch (MalformedURLException | URISyntaxException e)
            {
                throw new ProcessingException(statusCode + ": response contains invalid URL in location header: '" + location + "'. Details: " + e);
            }
        }
    }

    private static final String CT_JSON = "application/json";
    private static final String CT_JSON_PATCH_JSON = "application/json-patch+json";
    private static final String CT_3GPP_HAL_JSON = "application/3gppHal+json";

    private static final String ENV_EGRESS_NRF_DSCP = "EGRESS_NRF_DSCP";
    private static final String ENV_NRF_WAIT_QUEUE_SIZE_MAX = "NRF_WAIT_QUEUE_SIZE_MAX";

    private static final String HD_CONTENT_TYPE = "Content-Type";
//    private static final String HD_LOCATION = "Location";
    private static final String HD_3GPP_SBI_NF_PEER_INFO = "3gpp-Sbi-NF-Peer-Info";

    public static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 24 * 60 * 60; // [s] Maximum time with no traffic on the line.
    public static final int HTTP_CONNECT_TIMEOUT_MILLIS = 10000; // [ms]

    private static final int NRF_WAIT_QUEUE_SIZE_MAX = 50;

    private static final String ROUTE_ACCESS_TOKEN = "/oauth2/token";
    private static final String ROUTE_BOOTSTRAPPING = "/bootstrapping";
    private static final String ROUTE_NF_INSTANCES = "/nf-instances";
    private static final String ROUTE_SEARCHES = "/searches";
    private static final String ROUTE_SUBSCRIPTIONS = "/subscriptions";
    private static final String ROUTE_SCP_DOMAIN_ROUTING_INFO = "/scp-domain-routing-info";
    private static final String ROUTE_SCP_DOMAIN_ROUTING_INFO_SUBS = "/scp-domain-routing-info-subs";

    private static final Logger log = LoggerFactory.getLogger(NrfAdapter.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    private static final io.prometheus.client.Counter ccOutReq = MetricRegister.singleton()
                                                                               .register(io.prometheus.client.Counter.build()
                                                                                                                     .namespace("nrf")
                                                                                                                     .name("out_requests_total")
                                                                                                                     .labelNames("service",
                                                                                                                                 "nf",
                                                                                                                                 "nf_instance",
                                                                                                                                 "nrf_group",
                                                                                                                                 "nrf",
                                                                                                                                 "method",
                                                                                                                                 "path")
                                                                                                                     .help("Number of outgoing HTTP requests on the Nnrf interface")
                                                                                                                     .register());

    private static final io.prometheus.client.Counter ccInAns = MetricRegister.singleton()
                                                                              .register(io.prometheus.client.Counter.build()
                                                                                                                    .namespace("nrf")
                                                                                                                    .name("in_answers_total")
                                                                                                                    .labelNames("service",
                                                                                                                                "nf",
                                                                                                                                "nf_instance",
                                                                                                                                "nrf_group",
                                                                                                                                "nrf",
                                                                                                                                "method",
                                                                                                                                "path",
                                                                                                                                "status")
                                                                                                                    .help("Number of incoming HTTP answers on the Nnrf interface")
                                                                                                                    .register());

    private static URL createUrl(final Url url,
                                 final String service,
                                 final String route,
                                 final String instance,
                                 final String query)
    {
        final StringBuilder path = new StringBuilder();

        if (!service.isEmpty())
            path.append("/").append(service).append("/v1");

        path.append(route);

        log.debug("url={}", url);

        if (!instance.isEmpty())
            path.append("/").append(instance).toString();

        if (!query.isEmpty())
            path.append(query.startsWith("?") ? "" : "?").append(query);

        URL result = url.getUrl();

        try
        {
            result = new URL(result.getProtocol(), result.getHost(), result.getPort(), path.toString());
        }
        catch (MalformedURLException e)
        {
            log.error("Error creating new URL from url='{}', instance='{}', query='{}'. Cause: {}",
                      url,
                      instance,
                      query,
                      Utils.toString(e, log.isDebugEnabled()));
        }

        return result;
    }

    private static void stepCcInAns(final Rdn nrf,
                                    final String service,
                                    final HttpMethod method,
                                    final String path,
                                    final Integer statusCode)
    {
        log.debug("nrf={}, method={}, path={}, statusCode={}", nrf, method, path, statusCode);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add(service);
        labelValues.addAll(MetricRegister.rdnToLabelValues(nrf)); // nf, nf_instance, nrf_group
        labelValues.add(method.name()); // method
        labelValues.add(path); // path
        labelValues.add(HttpResponseStatus.valueOf(statusCode).toString()); // status

        ccInAns.labels(labelValues.toArray(new String[0])).inc();
    }

    private static void stepCcOutReq(final Rdn nrf,
                                     final String service,
                                     final HttpMethod method,
                                     final String path)
    {
        log.debug("nrf={}, method={}, path={}", nrf, method, path);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add(service);
        labelValues.addAll(MetricRegister.rdnToLabelValues(nrf)); // nf, nf_instance, nrf_group
        labelValues.add(method.name()); // method
        labelValues.add(path); // path

        ccOutReq.labels(labelValues.toArray(new String[0])).inc();
    }

    /**
     * Map the JSON body passed to an instance of class SearchResult.
     * <p>
     * But, rather than doing it at once, map the NFProfiles received in property
     * nfInstances one by one and, thereby, catch possible mapping problems
     * separately for each profile. This to minimize the impact on the discovery,
     * profiles that are okay are returned in the SearchResult while those with
     * errors are skipped.
     * 
     * @param body The JSON body to be mapped.
     * @return The mapped instance of class SearchResult.
     */
    static SearchResult toSearchResult(final JsonObject body)
    {
        final JsonArray nfInstances = body.getJsonArray(SearchResult.JSON_PROPERTY_NF_INSTANCES);

        body.putNull(SearchResult.JSON_PROPERTY_NF_INSTANCES);

        final SearchResult result = json.convertValue(body.getMap(), SearchResult.class);

        if (nfInstances != null)
        {
            final Map<String, String> skipped = new TreeMap<>();
            final List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile> nfProfiles = new ArrayList<>();

            for (int i = 0; i < nfInstances.size(); ++i)
            {
                final JsonObject nfInstance = nfInstances.getJsonObject(i);

                try
                {
                    nfProfiles.add(json.convertValue(nfInstance.getMap(), com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile.class));
                }
                catch (Exception e)
                {
                    final String nfInstanceId = nfInstance.getString(NFProfile.JSON_PROPERTY_NF_INSTANCE_ID);
                    skipped.put(nfInstanceId != null ? nfInstanceId : "null", e.toString());
                }
            }

            if (!skipped.isEmpty())
                log.error("Skipping search result for one or more nfInstances: {}", skipped);

            result.setNfInstances(nfProfiles);
        }

        return result;
    }

    private static void validateResponse(final HttpResponse<Buffer> resp,
                                         final String requiredContentType,
                                         final String body)
    {
        final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();

        final String contentType = resp.getHeader(HD_CONTENT_TYPE);

        if (contentType == null)
            throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

        if (!contentType.toLowerCase().contains(requiredContentType.toLowerCase()))
            throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + resp.getHeader(HD_CONTENT_TYPE)
                                                 + "', expected: '" + requiredContentType + "'.");

        if (body == null || body.isEmpty())
            throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");
    }

    private final Rdn nrf;

    private final Url url;

    private final WebClient client;

    public NrfAdapter(final Rdn nrf,
                      final Url url)
    {
        this(nrf, url, null);
    }

    public NrfAdapter(final Rdn nrf,
                      final Url url,
                      final Flowable<Secret> secrets)
    {
        this.nrf = nrf;
        this.url = url;

        this.client = new WebClient(secrets)
        {
            @Override
            protected WebClientOptions modifyOptions(final WebClientOptions options)
            {
                log.debug("HTTP_CONNECT_TIMEOUT_MILLIS={}", NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS);

                int egressNrfDscp = 0; // Default

                try
                {
                    egressNrfDscp = Integer.parseInt(EnvVars.get(ENV_EGRESS_NRF_DSCP, 0));
                    egressNrfDscp = Math.min(Math.max(0, egressNrfDscp), 63); // 0 <= dscp <= 63
                }
                catch (NumberFormatException e)
                {
                    // Intentionally empty, default value 0 applies.
                }

                return options.setUserAgentEnabled(false)
                              .setKeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setHttp2KeepAliveTimeout(NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setMaxWaitQueueSize(Integer.parseInt(EnvVars.get(ENV_NRF_WAIT_QUEUE_SIZE_MAX, NRF_WAIT_QUEUE_SIZE_MAX)))
                              .setConnectTimeout(NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS)
                              .setFollowRedirects(false)
                              .setTrafficClass((options.getTrafficClass() & 0x03) | (egressNrfDscp << 2)); // DSCP are the upper 6 bits of the traffic class
            }
        };
    }

    public NrfAdapter(final Rdn nrf,
                      final Url url,
                      final Flowable<CertificateObserver.Secret> secrets,
                      final Flowable<NrfCertificateInfo> nrfExtCertInfo)
    {
        this.nrf = nrf;
        this.url = url;

        log.debug("protocol is: {}", this.url.getUrl().getProtocol());

        this.client = this.url != null && this.url.getUrl() != null && "https".equals(this.url.getUrl().getProtocol()) ? new WebClient(secrets, nrfExtCertInfo)
        {
            @Override
            protected WebClientOptions modifyOptions(final WebClientOptions options)
            {
                log.debug("HTTP_CONNECT_TIMEOUT_MILLIS={}", NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS);

                int egressNrfDscp = 0; // Default

                try
                {
                    egressNrfDscp = Integer.parseInt(EnvVars.get(ENV_EGRESS_NRF_DSCP, 0));
                    egressNrfDscp = Math.min(Math.max(0, egressNrfDscp), 63); // 0
                                                                              // <=
                                                                              // dscp
                                                                              // <=
                                                                              // 63
                }
                catch (NumberFormatException e)
                {
                    // Intentionally empty,
                    // default
                    // value 0 applies.
                }

                return options.setUserAgentEnabled(false)
                              .setKeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setHttp2KeepAliveTimeout(NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setMaxWaitQueueSize(Integer.parseInt(EnvVars.get(ENV_NRF_WAIT_QUEUE_SIZE_MAX, NRF_WAIT_QUEUE_SIZE_MAX)))
                              .setConnectTimeout(NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS)
                              .setFollowRedirects(false)
                              .setTrafficClass((options.getTrafficClass() & 0x03) | (egressNrfDscp << 2)); // DSCP
                                                                                                           // are
                                                                                                           // the
                                                                                                           // upper
                                                                                                           // 6
                                                                                                           // bits
                                                                                                           // of
                                                                                                           // the
                                                                                                           // traffic
                                                                                                           // class
            }
        } : new WebClient()
        {
            @Override
            protected WebClientOptions modifyOptions(final WebClientOptions options)
            {
                log.debug("HTTP_CONNECT_TIMEOUT_MILLIS={}", NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS);

                int egressNrfDscp = 0; // Default

                try
                {
                    egressNrfDscp = Integer.parseInt(EnvVars.get(ENV_EGRESS_NRF_DSCP, 0));
                    egressNrfDscp = Math.min(Math.max(0, egressNrfDscp), 63); // 0
                                                                              // <=
                                                                              // dscp
                                                                              // <=
                                                                              // 63
                }
                catch (NumberFormatException e)
                {
                    // Intentionally empty,
                    // default
                    // value 0 applies.
                }

                return options.setUserAgentEnabled(false)
                              .setKeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setHttp2KeepAliveTimeout(NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setMaxWaitQueueSize(Integer.parseInt(EnvVars.get(ENV_NRF_WAIT_QUEUE_SIZE_MAX, NRF_WAIT_QUEUE_SIZE_MAX)))
                              .setConnectTimeout(NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS)
                              .setFollowRedirects(false)
                              .setTrafficClass((options.getTrafficClass() & 0x03) | (egressNrfDscp << 2)); // DSCP
                                                                                                           // are
                                                                                                           // the
                                                                                                           // upper
                                                                                                           // 6
                                                                                                           // bits
                                                                                                           // of
                                                                                                           // the
                                                                                                           // traffic
                                                                                                           // class
            }
        };
    }

    public Single<Result<AccessTokenRsp>> accessTokenRequest(final int requestTimeoutMillis,
                                                             final AccessTokenReq data)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, "", ROUTE_ACCESS_TOKEN, "", ""), this.url.getAddr()));

            return Single.defer(this.accessTokenRequest(requestTimeoutMillis, redirector, data))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, AccessTokenRsp.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         })
                         .doOnError(t -> log.error("Error processing request:", t))
                         .onErrorReturnItem(new Result<>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<BootstrappingInfo>> bootstrappingGet(final int requestTimeoutMillis)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, "", ROUTE_BOOTSTRAPPING, "", ""), this.url.getAddr()));

            return Single.defer(this.bootstrappingGet(requestTimeoutMillis, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_3GPP_HAL_JSON, body);
                                     return new Result<>(resp, json.readValue(body, BootstrappingInfo.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         })
                         .doOnError(t -> log.error("Error processing request: {}", t.toString()))
                         .onErrorReturnItem(new Result<>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public void close()
    {
        this.client.dispose();
    }

    public Single<Result<NFProfile>> nfInstanceDeregister(final RequestContext context,
                                                          final UUID nfInstanceId)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_NF_INSTANCES, nfInstanceId.toString(), ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstanceDeregister(context, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200: // Treat the same as 204, ignore body
                                 case 204:
                                 {
                                     return new Result<>(resp);
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<NFProfile>> nfInstanceGet(final RequestContext context,
                                                   final String nfInstanceId)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_NF_INSTANCES, nfInstanceId, ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstanceGet(context, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, NFProfile.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         })
                         .doOnError(t -> log.error("Error processing request: {}", t.toString()))
                         .onErrorReturnItem(new Result<>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<NFProfile>> nfInstanceRegister(final RequestContext context,
                                                        final UUID nfInstanceId,
                                                        final NFProfile nfProfile)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_NF_INSTANCES, nfInstanceId.toString(), ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstanceRegister(context, redirector, nfProfile))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 case 201:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, NFProfile.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<Links>> nfInstancesGet(final RequestContext context,
                                                final String nfType,
                                                final Integer limit)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url,
                                                                           ServiceName.NNRF_NFM,
                                                                           ROUTE_NF_INSTANCES,
                                                                           "",
                                                                           new Query.Builder().add("nf-type", nfType).add("limit", limit).build().toString()),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstancesGetNfm(context, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_3GPP_HAL_JSON, resp.bodyAsString());

                                     final JsonObject bodyAsJsonObject = resp.bodyAsJsonObject();

                                     if (bodyAsJsonObject.getJsonObject("_links") == null)
                                         throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: no '_links' in body.");

                                     return new Result<>(resp, json.readValue(bodyAsJsonObject.getJsonObject("_links").toString(), Links.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + resp.bodyAsString() + "'.");
                                 }
                             }
                         })
                         .doOnError(t -> log.error("Error processing request: {}", t.toString()))
                         .onErrorReturnItem(new Result<>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<StoredSearchResult>> nfInstancesRetrieveStored(final int requestTimeoutMillis,
                                                                        final String searchId,
                                                                        final boolean complete)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url,
                                                                           ServiceName.NNRF_DISC,
                                                                           ROUTE_SEARCHES,
                                                                           searchId + (complete ? "/complete" : ""),
                                                                           ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstancesGetDisc(RequestContext.of(requestTimeoutMillis), redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             if (resp.statusCode() == 200)
                             {
                                 validateResponse(resp, CT_JSON, body);
                                 return new Result<>(resp, json.readValue(body, StoredSearchResult.class));
                             }

                             if (resp.statusCode() < 300)
                                 throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: unexpected status-code.");

                             return new Result<>(resp);
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<SearchResult>> nfInstancesSearch(final RequestContext context)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_DISC, ROUTE_NF_INSTANCES, "", context.getQuery()),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstancesGetDisc(context, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             if (resp.statusCode() == 200)
                             {
                                 final JsonObject body = resp.bodyAsJsonObject();

                                 validateResponse(resp, CT_JSON, body == null ? null : "ok");

                                 return new Result<>(resp, toSearchResult(body));
                             }

                             if (resp.statusCode() < 300)
                                 throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: unexpected status-code.");

                             return new Result<>(resp);
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<NFProfile>> nfInstanceUpdate(final RequestContext context,
                                                      final UUID nfInstanceId,
                                                      final PatchItem... nfProfilePatch)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_NF_INSTANCES, nfInstanceId.toString(), ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstanceUpdate(context, redirector, nfProfilePatch))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, NFProfile.class));
                                 }

                                 case 204:
                                 case 404: // Allow the user to invoke nfInstanceRegister
                                 {
                                     return new Result<>(resp);
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public void renew()
    {
        this.client.close(true); // Renew connection
    }

    public Single<Result<ScpDomainRoutingInformation>> scpDomainRoutingInfoGet(final int requestTimeoutMillis,
                                                                               final Query query)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url,
                                                                           ServiceName.NNRF_DISC,
                                                                           ROUTE_SCP_DOMAIN_ROUTING_INFO,
                                                                           "",
                                                                           query.toString()),
                                                                 this.url.getAddr()));

            return Single.defer(this.nfInstancesGetDisc(RequestContext.of(requestTimeoutMillis), redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             if (resp.statusCode() == 200)
                             {
                                 validateResponse(resp, CT_JSON, body);
                                 return new Result<>(resp, json.readValue(body, ScpDomainRoutingInformation.class));
                             }

                             if (resp.statusCode() < 300)
                                 throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: unexpected status-code.");

                             return new Result<>(resp);
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<ScpDomainRoutingInfoSubscription>> scpDomainRoutingInfoSubscriptionCreate(final int requestTimeoutMillis,
                                                                                                   final ScpDomainRoutingInfoSubscription data)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_DISC, ROUTE_SCP_DOMAIN_ROUTING_INFO_SUBS, "", ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.scpDomainRoutingInfoSubscriptionCreate(requestTimeoutMillis, redirector, data))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 201:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, ScpDomainRoutingInfoSubscription.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful or unexpected response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<ScpDomainRoutingInfoSubscription>> scpDomainRoutingInfoSubscriptionRemove(final int requestTimeoutMillis,
                                                                                                   final URL location)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(location, this.url.getAddr()));

            return Single.defer(this.scpDomainRoutingInfoSubscriptionRemove(requestTimeoutMillis, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200: // Treat the same as 204, ignore body
                                 case 204:
                                 {
                                     return new Result<>(resp);
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<SubscriptionData>> subscriptionCreate(final int requestTimeoutMillis,
                                                               final SubscriptionData data)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_SUBSCRIPTIONS, "", ""), this.url.getAddr()));

            return Single.defer(this.subscriptionCreate(requestTimeoutMillis, redirector, data))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 201:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, SubscriptionData.class));
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful or unexpected response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<SubscriptionData>> subscriptionRemove(final int requestTimeoutMillis,
                                                               final String subscriptionId)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_SUBSCRIPTIONS, subscriptionId, ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.subscriptionRemove(requestTimeoutMillis, redirector))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200: // Treat the same as 204, ignore body
                                 case 204:
                                 {
                                     return new Result<>(resp);
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    public Single<Result<SubscriptionData>> subscriptionUpdate(final int requestTimeoutMillis,
                                                               final String subscriptioneId,
                                                               final PatchItem... patch)
    {
        try
        {
            final Redirector redirector = new Redirector(new Url(createUrl(this.url, ServiceName.NNRF_NFM, ROUTE_SUBSCRIPTIONS, subscriptioneId, ""),
                                                                 this.url.getAddr()));

            return Single.defer(this.subscriptionUpdate(requestTimeoutMillis, redirector, patch))//
                         .repeatUntil(redirector.isDone())
                         .lastOrError()
                         .map(resp ->
                         {
                             final String body = resp.bodyAsString();

                             switch (resp.statusCode())
                             {
                                 case 200:
                                 {
                                     validateResponse(resp, CT_JSON, body);
                                     return new Result<>(resp, json.readValue(body, SubscriptionData.class));
                                 }

                                 case 204:
                                 case 404: // Allow the user to invoke subscriptionCreate
                                 {
                                     return new Result<>(resp);
                                 }

                                 default:
                                 {
                                     final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                     throw new ProcessingException(status + ": unsuccessful response received: '" + body + "'.");
                                 }
                             }
                         });
        }
        catch (final Exception e)
        {
            throw new ProcessingException(e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Callable<SingleSource<HttpResponse<Buffer>>> accessTokenRequest(final int requestTimeoutMillis,
                                                                            final Redirector redirector,
                                                                            final AccessTokenReq data)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.POST, currUrl.getUrl());

            final String dataAsJsonStr = json.writeValueAsString(data);

            log.debug("data={}", dataAsJsonStr);

            final MultiMap form = MultiMap.caseInsensitiveMultiMap();

            json.readValue(dataAsJsonStr, LinkedHashMap.class).entrySet().forEach(e ->
            {
                final String name = ((Entry<String, Object>) e).getKey();
                final Object value = ((Entry<String, Object>) e).getValue();

                if (value != null)
                {
                    if (value instanceof String && !((String) value).isEmpty())
                    {
                        form.set(name, (String) value);
                    }
                    else if (name.equals("targetNsiList")) // explode
                    {
                        ((List<String>) value).forEach(item -> form.add(name, item));
                    }
                    else
                    {
                        try
                        {
                            form.set(name, json.writeValueAsString(value));
                        }
                        catch (JsonProcessingException je)
                        {
                            log.error("Error converting object to JSON string", je);
                        }
                    }
                }
            });

            log.info("form={}", form);

            return this.client.requestAbs(HttpMethod.POST, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSendForm(form)
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.POST, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.POST, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.POST, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> bootstrappingGet(final int requestTimeoutMillis,
                                                                          final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.GET, currUrl.getUrl());

            return this.client.requestAbs(HttpMethod.GET, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSend()
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.GET, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstanceDeregister(final RequestContext context,
                                                                              final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.DELETE, currUrl.getUrl(), context);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.DELETE, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.timeout(context.getRequestTimeoutMillis())
                             .rxSend()
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.DELETE, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.DELETE, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.DELETE, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstanceGet(final RequestContext context,
                                                                       final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.GET, currUrl.getUrl(), context);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.GET, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.timeout(context.getRequestTimeoutMillis())
                             .rxSend()
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.GET, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstanceRegister(final RequestContext context,
                                                                            final Redirector redirector,
                                                                            final NFProfile nfProfile)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.PUT, currUrl.getUrl(), context);

            final String nfProfileAsJsonStr = json.writeValueAsString(nfProfile);

            log.debug("nfProfile={}", nfProfileAsJsonStr);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.PUT, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.timeout(context.getRequestTimeoutMillis())
                             .rxSendJsonObject(new JsonObject(nfProfileAsJsonStr))
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.PUT, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PUT, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PUT, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstancesGetDisc(final RequestContext context,
                                                                            final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.GET, currUrl.getUrl(), context);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.GET, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.timeout(context.getRequestTimeoutMillis())
                             .rxSend()
                             .subscribeOn(Schedulers.io())
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.GET, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_DISC, HttpMethod.GET, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_DISC, HttpMethod.GET, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstancesGetNfm(final RequestContext context,
                                                                           final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.GET, currUrl.getUrl(), context);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.GET, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.timeout(context.getRequestTimeoutMillis())
                             .rxSend()
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.GET, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.GET, currUrl.getUrl().getPath()))
                             .doOnError(t -> log.error("Error processing request: {}", t.toString()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> nfInstanceUpdate(final RequestContext context,
                                                                          final Redirector redirector,
                                                                          final PatchItem... nfProfilePatch)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}, context={}", HttpMethod.PATCH, currUrl.getUrl(), context);

            final String nfProfilePatchAsJsonStr = json.writeValueAsString(nfProfilePatch);

            log.debug("nfProfilePatch={}", nfProfilePatchAsJsonStr);

            final JsonArray nfProfilePatchAsJson = new JsonArray(nfProfilePatchAsJsonStr);

            final HttpRequest<Buffer> requestAbs = this.client.requestAbs(HttpMethod.PATCH, currUrl.getAddr(), currUrl.getUrl().toString());

            Optional.ofNullable(context.getHeaders()).ifPresent(requestAbs::putHeaders);
            Optional.ofNullable(context.getSbiNfPeerInfo()).ifPresent(sbiNfPeerInfo -> requestAbs.putHeader(HD_3GPP_SBI_NF_PEER_INFO, sbiNfPeerInfo));

            return requestAbs.putHeader(HD_CONTENT_TYPE, CT_JSON_PATCH_JSON)
                             .timeout(context.getRequestTimeoutMillis())
                             .rxSendJson(nfProfilePatchAsJson)
                             .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.PATCH, resp.statusCode(), resp.bodyAsString()))
                             .doOnSuccess(redirector::setCurrResult)
                             .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PATCH, currUrl.getUrl().getPath(), resp.statusCode()))
                             .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PATCH, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> scpDomainRoutingInfoGet(final int requestTimeoutMillis,
                                                                                 final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.GET, currUrl.getUrl());

            return this.client.requestAbs(HttpMethod.GET, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSend()
                              .subscribeOn(Schedulers.io())
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.GET, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_DISC, HttpMethod.GET, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_DISC, HttpMethod.GET, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> scpDomainRoutingInfoSubscriptionCreate(final int requestTimeoutMillis,
                                                                                                final Redirector redirector,
                                                                                                final ScpDomainRoutingInfoSubscription data)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.POST, currUrl.getUrl());

            final String dataAsJsonStr = json.writeValueAsString(data);

            log.debug("data={}", dataAsJsonStr);

            return this.client.requestAbs(HttpMethod.POST, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSendJsonObject(new JsonObject(dataAsJsonStr))
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.POST, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_DISC, HttpMethod.POST, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_DISC, HttpMethod.POST, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> scpDomainRoutingInfoSubscriptionRemove(final int requestTimeoutMillis,
                                                                                                final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.DELETE, currUrl.getUrl());

            return this.client.requestAbs(HttpMethod.DELETE, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSend()
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.DELETE, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf,
                                                               ServiceName.NNRF_DISC,
                                                               HttpMethod.DELETE,
                                                               currUrl.getUrl().getPath(),
                                                               resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_DISC, HttpMethod.DELETE, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> subscriptionCreate(final int requestTimeoutMillis,
                                                                            final Redirector redirector,
                                                                            final SubscriptionData data)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.POST, currUrl.getUrl());

            final String dataAsJsonStr = json.writeValueAsString(data);

            log.debug("data={}", dataAsJsonStr);

            return this.client.requestAbs(HttpMethod.POST, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSendJsonObject(new JsonObject(dataAsJsonStr))
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.POST, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.POST, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.POST, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> subscriptionRemove(final int requestTimeoutMillis,
                                                                            final Redirector redirector)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.DELETE, currUrl.getUrl());

            return this.client.requestAbs(HttpMethod.DELETE, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .timeout(requestTimeoutMillis)
                              .rxSend()
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.DELETE, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf,
                                                               ServiceName.NNRF_NFM,
                                                               HttpMethod.DELETE,
                                                               currUrl.getUrl().getPath(),
                                                               resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.DELETE, currUrl.getUrl().getPath()));
        };
    }

    private Callable<SingleSource<HttpResponse<Buffer>>> subscriptionUpdate(final int requestTimeoutMillis,
                                                                            final Redirector redirector,
                                                                            final PatchItem... patch)
    {
        return () ->
        {
            final Url currUrl = redirector.getCurrUrl();

            log.debug("Request: {} {}", HttpMethod.PATCH, currUrl.getUrl());

            final String patchAsJsonStr = json.writeValueAsString(patch);

            log.debug("patch={}", patchAsJsonStr);

            final JsonArray patchAsJson = new JsonArray(patchAsJsonStr);

            return this.client.requestAbs(HttpMethod.PATCH, currUrl.getAddr(), currUrl.getUrl().toString()) //
                              .putHeader(HD_CONTENT_TYPE, CT_JSON_PATCH_JSON)
                              .timeout(requestTimeoutMillis)
                              .rxSendJson(patchAsJson)
                              .doOnSuccess(resp -> log.debug("{} response: {} {}", HttpMethod.PATCH, resp.statusCode(), resp.bodyAsString()))
                              .doOnSuccess(redirector::setCurrResult)
                              .doOnSuccess(resp -> stepCcInAns(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PATCH, currUrl.getUrl().getPath(), resp.statusCode()))
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, HttpMethod.PATCH, currUrl.getUrl().getPath()));
        };
    }
}
