/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 20, 2018
 *     Author: xkorpap
 */

package com.ericsson.adpal.pm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter.Query.Response;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.ProtocolViolationException;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpVersion;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;

public class PmAdapter
{
    public static interface IfQuery
    {
        public String toString();
    }

    public static class Inquisitor
    {
        private final PmAdapter pmAdapter;
        private final List<PmAdapter.Query.Element> queries;
        private final int intervalInMillis;
        private final BehaviorSubject<List<PmAdapter.Query.Response.Data>> subject;

        private Disposable updater;

        public Inquisitor(final PmAdapter pmAdapter,
                          final int intervalInMillis,
                          final PmAdapter.Query.Element... query)
        {
            this.pmAdapter = pmAdapter;
            this.queries = new ArrayList<>(Arrays.asList(query));
            this.intervalInMillis = intervalInMillis;
            this.subject = BehaviorSubject.create();
            this.updater = null;
        }

        public Inquisitor(final PmAdapter pmAdapter,
                          final PmAdapter.Query.Element... query)
        {
            this(pmAdapter, POLLING_INTERVAL_MILLIS, query);
        }

        public BehaviorSubject<List<PmAdapter.Query.Response.Data>> getData()
        {
            return this.subject;
        }

        public Completable start()
        {
            return Completable.fromAction(() ->
            {
                if (this.updater == null)
                {
                    this.updater = this.update()
                                       .doOnSubscribe(e -> log.debug("Updating load counters."))
                                       .repeatWhen(handler -> handler.delay(this.intervalInMillis, TimeUnit.MILLISECONDS))
                                       .ignoreElements()
                                       .doOnSubscribe(d -> log.info("Started updating load counters."))
                                       .subscribe(() -> log.info("Stopped updating load counters."),
                                                  t -> log.error("Stopped updating load counters. Cause: {}", t.toString()));
                }
            });
        }

        public Completable stop()
        {
            return Completable.fromAction(() ->
            {
                if (this.updater != null)
                {
                    this.updater.dispose();
                    this.updater = null;
                }
            });
        }

        private Single<List<Optional<Data>>> update()
        {
            return Observable.fromIterable(this.queries) //
                             .concatMapSingle(this.pmAdapter::post) //
                             .filter(Optional::isPresent) //
                             .toList()
                             .doOnSuccess(l ->
                             {
                                 final List<Data> data = new ArrayList<>();

                                 l.forEach(i ->
                                 {
                                     PmAdapter.Query.Response.Data result = i.get();
                                     log.debug("data={}", i.get());
                                     data.add(result);
                                 });

                                 this.subject.toSerialized().onNext(data);
                             });
        }
    }

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

    public static class Query
    {
        public abstract static class Element
        {
            public abstract String toString();
        }

        public static class FunctionSum extends Function
        {
            final List<String> byLabels;

            private FunctionSum(final Element e)
            {
                super("sum", e);
                this.byLabels = new ArrayList<>();
            }

            public FunctionSum by(final String label)
            {
                this.byLabels.add(label);
                return this;
            }

            @Override
            public String toString()
            {
                if (this.op.isEmpty())
                    return this.e.toString();

                final StringBuilder b = new StringBuilder(this.op).append("(").append(this.e.toString()).append(")");

                if (!this.byLabels.isEmpty())
                {
                    b.append(" by (");

                    boolean isFirst = true;

                    for (final String l : this.byLabels)
                    {
                        if (l.isEmpty())
                            continue;

                        if (isFirst)
                            isFirst = false;
                        else
                            b.append(",");

                        b.append(l);
                    }

                    b.append(")");
                }

                return b.toString();
            }
        }

        public static class Metric extends Element
        {
            private static class Parameter
            {
                private String name;
                private String value;
                private boolean regexMatch;

                public Parameter(final String name,
                                 final String value)
                {
                    this.name = name;
                    this.value = value;
                }

                public Parameter(final String name,
                                 final String value,
                                 final boolean regexMatch)
                {
                    this.name = name;
                    this.value = value;
                    this.regexMatch = regexMatch;
                }

                @Override
                public String toString()
                {
                    return new StringBuilder(this.name).append(regexMatch ? "=~\"" : "=\"").append(this.value).append("\"").toString();
                }
            }

            final String name;
            private final List<Parameter> params;

            private Metric(final String name)
            {
                this.name = name;
                this.params = new ArrayList<>();
            }

            public Metric param(final String name,
                                final String value)
            {
                this.params.add(new Parameter(name, value));
                return this;
            }

            public Metric param(final String name,
                                final String value,
                                final boolean regexMatch)
            {
                this.params.add(new Parameter(name, value, regexMatch));
                return this;
            }

            @Override
            public String toString()
            {
                final StringBuilder b = new StringBuilder(this.name);

                if (!this.params.isEmpty())
                {
                    b.append("{");

                    boolean isFirst = true;

                    for (final Parameter p : this.params)
                    {
                        final String s = p.toString();

                        if (s.isEmpty())
                            continue;

                        if (isFirst)
                            isFirst = false;
                        else
                            b.append(",");

                        b.append(s);
                    }

                    b.append("}");
                }

                return b.toString();
            }
        }

        public static class Operator extends Element
        {
            private String name;
            protected Element left;
            protected Element right;

            public Operator(final String operator,
                            final Element left,
                            final Element right)
            {
                this.name = operator;
                this.left = left;
                this.right = right;
            }

            @Override
            public String toString()
            {
                return new StringBuilder().append("(")
                                          .append(this.left.toString())
                                          .append(" ")
                                          .append(this.name)
                                          .append(" ")
                                          .append(this.right.toString())
                                          .append(")")
                                          .toString();
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "status", "data", "errorType", "error", "warnings" })
        public static class Response
        {
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonPropertyOrder({ "resultType", "result" })
            public static class Data
            {
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @JsonPropertyOrder({ "metric", "value" })
                public static class Result
                {
                    public enum Type
                    {
                        MATRIX("matrix"),
                        VECTOR("vector"),
                        SCALAR("scalar"),
                        STRING("string");

                        @JsonCreator
                        public static Type fromValue(String value)
                        {
                            for (Type b : Type.values())
                                if (b.value.equals(value))
                                    return b;

                            throw new IllegalArgumentException("Unexpected value '" + value + "'");
                        }

                        private String value;

                        Type(String value)
                        {
                            this.value = value;
                        }
                    }

                    @JsonProperty("metric")
                    private Map<String, String> metric = new TreeMap<>();

                    @JsonProperty("value")
                    private List<Double> value = new ArrayList<>();

                    public Map<String, String> getMetric()
                    {
                        return this.metric;
                    }

                    public List<Double> getValue()
                    {
                        return this.value;
                    }
                }

                @JsonProperty("resultType")
                private Result.Type resultType;

                @JsonProperty("result")
                private List<Result> result = new ArrayList<>();

                public List<Result> getResult()
                {
                    return this.result;
                }

                public Result.Type getResultType()
                {
                    return this.resultType;
                }

                /**
                 * Returns a JSON representation of this object.
                 * 
                 * @return A JSON representation of this object.
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

            public enum Status
            {
                SUCEESS("success"),
                ERROR("error");

                @JsonCreator
                public static Status fromValue(String value)
                {
                    for (Status b : Status.values())
                        if (b.value.equals(value))
                            return b;

                    throw new IllegalArgumentException("Unexpected value '" + value + "'");
                }

                private String value;

                Status(String value)
                {
                    this.value = value;
                }
            }

            @JsonProperty("status")
            private Status status;

            @JsonProperty("data")
            private Data data;

            @JsonProperty("errorType")
            private String errorType;

            @JsonProperty("error")
            private String error;

            @JsonProperty("warnings")
            private List<String> warnings;

            public Data getData()
            {
                return data;
            }

            public String getError()
            {
                return this.error;
            }

            public String getErrorType()
            {
                return this.errorType;
            }

            public Status getStatus()
            {
                return this.status;
            }

            public List<String> getWarnings()
            {
                return this.warnings;
            }

            /**
             * Returns a JSON representation of this object.
             * 
             * @return A JSON representation of this object.
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

        private static class Function extends Element
        {
            protected final String op;
            protected Element e;

            protected Function(final String op,
                               final Element e)
            {
                this.op = op;
                this.e = e;
            }

            @Override
            public String toString()
            {
                if (this.op.isEmpty())
                    return this.e.toString();

                return new StringBuilder(this.op).append("(").append(this.e.toString()).append(")").toString();
            }
        }

        private static class FunctionMaxOverTime extends Function
        {
            final int durationInSecs;
            final int stepInSecs;

            private FunctionMaxOverTime(final Element e,
                                        int durationInSecs,
                                        int stepInSecs)
            {
                super("max_over_time", e);
                this.durationInSecs = durationInSecs;
                this.stepInSecs = stepInSecs;
            }

            @Override
            public String toString()
            {
                if (this.op.isEmpty())
                    return this.e.toString();

                return new StringBuilder(this.op).append("(")
                                                 .append(this.e.toString())
                                                 .append("[")
                                                 .append(this.durationInSecs)
                                                 .append("s")
                                                 .append(":")
                                                 .append(this.stepInSecs)
                                                 .append("s])")
                                                 .toString();
            }
        }

        private static class FunctionRate extends Function
        {
            final int durationInSecs;

            private FunctionRate(final Element e,
                                 int durationInSecs)
            {
                super("rate", e);
                this.durationInSecs = durationInSecs;
            }

            @Override
            public String toString()
            {
                if (this.op.isEmpty())
                    return this.e.toString();

                return new StringBuilder(this.op).append("(")
                                                 .append(this.e.toString())
                                                 .append("[")
                                                 .append(this.durationInSecs)
                                                 .append("s]")
                                                 .append(")")
                                                 .toString();
            }
        }

        private static class Value<T> extends Element
        {
            final T value;

            private Value(final T value)
            {
                this.value = value;
            }

            @Override
            public String toString()
            {
                return this.value.toString();
            }
        }

        /**
         * left and right
         */
        public static Element and(final Element left,
                                  final Element right)
        {
            return new Operator("and", left, right);
        }

        /**
         * avg(e)
         */
        public static Element avg(final Element e)
        {
            return new Function("avg", e);
        }

        /**
         * left / right
         */
        public static Element div(final Element left,
                                  final Element right)
        {
            return new Operator("/", left, right);
        }

        /**
         * max(e)
         */
        public static Element max(final Element e)
        {
            return new Function("max", e);
        }

        /**
         * max_over_time(e)<br>
         * max_over_time(e)[durationInSecs:stepsInSecs]
         */
        public static Element max_over_time(final Element e,
                                            final int durationInSecs,
                                            final int stepsInSecs)
        {
            return new FunctionMaxOverTime(e, durationInSecs, stepsInSecs);
        }

        /**
         * name<br>
         * name{parameter, ...)}
         */
        public static Metric metric(final String name)
        {
            return new Metric(name);
        }

        /**
         * min(e)
         */
        public static Element min(final Element e)
        {
            return new Function("min", e);
        }

        /**
         * left * right
         */
        public static Element mul(final Element left,
                                  final Element right)
        {
            return new Operator("*", left, right);
        }

        /**
         * left or right
         */
        public static Element or(final Element left,
                                 final Element right)
        {
            return new Operator("or", left, right);
        }

        /**
         * (e)
         */
        public static Element precede(final Element e)
        {
            return new Function("", e);
        }

        /**
         * rate(e)<br>
         * rate(e)[durationInSecss]
         */
        public static Element rate(final Element e,
                                   final int durationInSecs)
        {
            return new FunctionRate(e, durationInSecs);
        }

        /**
         * sum(e)<br>
         * sum(e) by (label, ...)
         */
        public static FunctionSum sum(final Element e)
        {
            return new FunctionSum(e);
        }

        /**
         * value
         */
        public static <T> Element value(final T value)
        {
            return new Value<T>(value);
        }

        /**
         * vector (value)
         */
        public static Element vector(final Element value)
        {
            return new Function("vector", value);
        }

        private Query()
        {
        }
    }

    public static final int POLLING_INTERVAL_MILLIS = 5000;
    public static final int REQUEST_TIMEOUT_MILLIS = POLLING_INTERVAL_MILLIS - 100;
    private static final String PM_API = "/api/v1";
    private static final String QUERY = "/query";

    private static final Logger log = LoggerFactory.getLogger(PmAdapter.class);
    private static final ObjectMapper json = Jackson.om();

    public static void configureMetricsHandler(final RouterHandler server)
    {
        configureMetricsHandler(server, false);
    }

    public static void configureMetricsHandler(final RouterHandler server,
                                               boolean withMicrometerRegistry)
    {
        configureMetricsHandler(server,
                                () -> new PmMetricsHandler(withMicrometerRegistry ? List.of(CollectorRegistry.defaultRegistry,
                                                                                            ((PrometheusMeterRegistry) BackendRegistries.getDefaultNow()).getPrometheusRegistry())
                                                                                  : List.of(CollectorRegistry.defaultRegistry)));
    }

    public static void configureMetricsHandler(final RouterHandler server,
                                               final Supplier<Handler<RoutingContext>> handlerFactory)
    {
        server.configureRouter(router -> router.get("/metrics").handler(handlerFactory.get()));
    }

    private final String pmHost;
    private final int pmPort;
    private final String pmUrl;
    private final WebClientProvider pmQueryClient;
    private static final URI pmQueryClientCertsUri = URI.create("/run/secrets/pms/query/certificates");
    private static final URI sipTlsTrustedCaUri = URI.create("/run/secrets/siptls/ca");

    public PmAdapter(final Vertx vertx,
                     final int pmPort,
                     final String pmHost)
    {
        this.pmUrl = PM_API;
        this.pmHost = pmHost;
        this.pmPort = pmPort;

        this.pmQueryClient = WebClientProvider.builder() //
                                              .withOptions(options -> options.setProtocolVersion(HttpVersion.HTTP_1_1)) //
                                              .build(vertx);
    }

    public PmAdapter(final Vertx vertx,
                     final int pmPort,
                     final String pmHost,
                     final String serviceName,
                     final boolean tlsEnabled)
    {
        this.pmUrl = PM_API;
        this.pmHost = pmHost;
        this.pmPort = pmPort;

        // create client for fault indications to alarm handler
        final var tmpClient = WebClientProvider.builder() //
                                               .withOptions(options -> options.setProtocolVersion(HttpVersion.HTTP_1_1)) //
                                               .withHostName(serviceName);

        if (tlsEnabled)
            tmpClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(pmQueryClientCertsUri.getPath()), //
                                                                  SipTlsCertWatch.trustedCert(sipTlsTrustedCaUri.getPath())));
        this.pmQueryClient = tmpClient.build(vertx);
    }

    public Completable stop()
    {
        return Completable.complete().andThen(this.pmQueryClient.close().onErrorComplete());
    }

    public Single<Optional<Query.Response.Data>> post(final Query.Element query)
    {
        return this.pmQueryClient.getWebClient()
                                 .flatMap(client -> client.post(this.pmPort, this.pmHost, this.pmUrl + QUERY)
                                                          .putHeader("Content-Type", "application/x-www-form-urlencoded")
                                                          .addQueryParam("query", query.toString())
                                                          .rxSend()
                                                          .timeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                                          .doOnSubscribe(d -> log.debug("POST request: {}:{}{}{}{}{}",
                                                                                        this.pmHost,
                                                                                        this.pmPort,
                                                                                        this.pmUrl,
                                                                                        QUERY,
                                                                                        "?query=",
                                                                                        query))
                                                          .map(resp ->
                                                          {
                                                              final String body = resp.bodyAsString();
                                                              log.debug("POST response: {} {}", resp.statusCode(), body);

                                                              final String contentType = resp.getHeader("Content-Type");

                                                              if (contentType == null)
                                                                  throw new ProtocolViolationException("Invalid response. Cause: unspecified content-type.");

                                                              if (!contentType.toLowerCase().contains("application/json"))
                                                                  throw new ProtocolViolationException("Invalid response. Cause: invalid content-type: '"
                                                                                                       + resp.getHeader("Content-Type") + "'.");

                                                              if (body == null)
                                                                  throw new ProtocolViolationException("Invalid response. Cause: no body in response.");

                                                              if (resp.statusCode() == 200)
                                                              {
                                                                  final Response response = json.readValue(body, Query.Response.class);

                                                                  if (response.getStatus().equals(Response.Status.ERROR))
                                                                      throw new ProcessingException("Error response received: '" + body + "'.");

                                                                  return Optional.of(response.getData());
                                                              }

                                                              throw new ProcessingException("Unexpected response received: '" + body + "'.");
                                                          })
                                                          .doOnError(e ->
                                                          {
                                                              if (e instanceof TimeoutException)
                                                                  log.warn("Problem sending PM data to PM server: request timed out after {} ms.",
                                                                           REQUEST_TIMEOUT_MILLIS);
                                                              else
                                                                  log.error("Error sending PM data to PM server: {}", Utils.toString(e, log.isDebugEnabled()));
                                                          })
                                                          .onErrorResumeNext(Single.just(Optional.<Query.Response.Data>empty())));
    }
}
