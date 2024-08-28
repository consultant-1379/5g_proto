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

package com.ericsson.sim.loadgen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Instance;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.cnal.internal.nrlf.ratelimiting.PullTokensContext;
import com.ericsson.sim.loadgen.rlf.RlfLoad;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

public class LoadGenerator implements Runnable, MonitorAdapter.CommandCounter.Provider, MonitorAdapter.CommandEsa.Provider
{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "rlf" })
    public static class Configuration
    {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "numInstances", "roundRobin", "pullRequest" })
        public static class Rlf
        {
            @JsonPropertyOrder({ "context", "rate" })
            public static class PullRequest
            {
                @JsonProperty("context")
                private PullTokensContext context;

                @JsonProperty("rate")
                private long rate;

                public PullTokensContext getContext()
                {
                    return this.context;
                }

                public long getRate()
                {
                    return this.rate;
                }

                public PullRequest setContext(final PullTokensContext context)
                {
                    this.context = context;
                    return this;
                }

                public PullRequest setRate(final long rate)
                {
                    this.rate = rate;
                    return this;
                }
            }

            @JsonProperty("numInstances")
            private int numInstances = 1;

            /**
             * Default (false) means random access of RLF workers.
             */
            @JsonProperty("roundRobin")
            private boolean roundRobin = false;

            @JsonProperty("pullRequest")
            private PullRequest pullRequest;

            public int getNumInstances()
            {
                return this.numInstances;
            }

            public PullRequest getPullRequest()
            {
                return this.pullRequest;
            }

            public boolean getRoundRobin()
            {
                return this.roundRobin;
            }

            public Rlf setNumInstances(final int numInstances)
            {
                this.numInstances = numInstances;
                return this;
            }

            public Rlf setPullRequest(final PullRequest pullRequest)
            {
                this.pullRequest = pullRequest;
                return this;
            }

            public boolean setRoundRobin(boolean roundRobin)
            {
                return this.roundRobin = roundRobin;
            }
        }

        @JsonProperty("rlf")
        private Rlf rlf;

        public Configuration()
        {
        }

        public Rlf getRlf()
        {
            return this.rlf;
        }

        public Configuration setRlf(final Rlf rlf)
        {
            this.rlf = rlf;
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

    @JsonPropertyOrder({ "countInHttpRequests", "countOutHttpResponsesPerStatus", "historyOfEvents" })
    public static class Statistics
    {
        public static class Pool
        {
            @JsonProperty("pool")
            private final Map<String, Statistics> pool = new ConcurrentHashMap<>();

            public void clear()
            {
                this.pool.values().forEach(Statistics::clear);
            }

            public Statistics get(String nfInstanceId)
            {
                if (nfInstanceId == null)
                    nfInstanceId = DEFAULT_NF_INSTANCE_ID;

                if (this.pool.containsKey(nfInstanceId))
                    return this.pool.get(nfInstanceId);

                Statistics value = new Statistics(nfInstanceId);
                Statistics prev = this.pool.putIfAbsent(nfInstanceId, value);
                return prev != null ? prev : value;
            }

            public Iterator<Entry<String, Statistics>> iterator()
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

        @JsonProperty("countInHttpRequests")
        private final Count countInHttpRequests;
        @JsonProperty("countOutHttpResponsesPerStatus")
        private final Count.Pool countOutHttpResponsesPerStatus;
        @JsonProperty("historyOfEvents")
        private final Event.Sequence historyOfEvents;

        public Statistics(final String nfInstanceId)
        {
            this.countInHttpRequests = new Count();
            this.countOutHttpResponsesPerStatus = new Count.Pool();
            this.historyOfEvents = new Event.Sequence(nfInstanceId);
        }

        public void clear()
        {
            this.countInHttpRequests.clear();
            this.countOutHttpResponsesPerStatus.clear();
            this.historyOfEvents.clear();
        }

        public Count getCountInHttpRequests()
        {
            return this.countInHttpRequests;
        }

        public Count.Pool getCountOutHttpResponsesPerStatus()
        {
            return this.countOutHttpResponsesPerStatus;
        }

        public Event.Sequence getHistoryOfEvents()
        {
            return this.historyOfEvents;
        }
    }

    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private LoadGenerator handler;

        public CommandConfig(final LoadGenerator handler)
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
                    this.handler.setConfiguration(Optional.ofNullable(json.readValue(data, Configuration.class)));
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

            return HttpResponseStatus.OK;
        }
    }

    private static class CommandInfo extends MonitorAdapter.CommandBase
    {
        private LoadGenerator handler;

        public CommandInfo(final LoadGenerator handler)
        {
            super("info", "Usage: command=info[&inRequests[=<true|false>] | &outResponses[=<opId>] | &clear[=<true|false>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {

            final var inRequests = Boolean.parseBoolean((String) request.getAdditionalProperties().get("inRequests"));
            final var outResponses = (String) request.getAdditionalProperties().get("outResponses");
            final var clear = Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"));

            log.info("inRequests='{}'", inRequests);
            log.info("outResponses='{}'", outResponses);
            log.info("clear='{}'", clear);

            if (clear)
            {
                this.handler.getStatistics().clear();
                return HttpResponseStatus.OK;
            }
            else if (inRequests)
            {
                result.setAdditionalProperty("inRequests", this.handler.getStatistics().get(null).getCountInHttpRequests());
                return HttpResponseStatus.OK;
            }
            else if (outResponses != null)
            {
                result.setAdditionalProperty("outResponses", this.handler.getStatistics().get(null).getCountOutHttpResponsesPerStatus());
                return HttpResponseStatus.OK;
            }
            else
            {
                result.setAdditionalProperty("errorMessage", HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'header'.");
                return HttpResponseStatus.BAD_REQUEST;
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(LoadGenerator.class);
    private static final ObjectMapper json = Jackson.om();

    public static void main(final String[] args)
    {
        int exitStatus = 0;

        log.info("Starting load generator, version: {}", VersionInfo.get());

        try
        {
            final Map<String, String> opts = new HashMap<>();

            for (String arg : args)
            {
                String[] tokens = arg.split("=");

                if (tokens.length != 2) // Arguments must look like that: data=<json>
                    continue;

                opts.put(tokens[0], tokens[1]);
            }

            log.info("opts={}", opts);

            final String data = opts.get("data");
            new LoadGenerator(Optional.ofNullable(data != null ? json.readValue(data, Configuration.class) : null)).run();
        }
        catch (final Exception e)
        {
            log.error("Exception caught", e);
        }

        log.info("Stopped load generator.");

        System.exit(exitStatus);
    }

    private final Statistics.Pool statistics;
    private final WebServer webServerInt;
    private final List<Disposable> disposables;
    private final MonitorAdapter monitored;
    private final AtomicReference<Optional<Configuration>> config = new AtomicReference<>();

    public LoadGenerator(final Optional<Configuration> config) throws IOException
    {
        this.statistics = new Statistics.Pool();

        this.webServerInt = WebServer.builder().withHost(Utils.getLocalAddress()).withPort(8080).build(VertxInstance.get());
        PmAdapter.configureMetricsHandler(this.webServerInt);

        this.disposables = new ArrayList<>();
        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(this), new CommandConfig(this), new CommandInfo(this)),
                                            Arrays.asList(new CommandConfig(this)));

        this.config.set(config);
    }

    public Optional<Configuration> getConfiguration()
    {
        return this.config.get();
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
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswers = new ArrayList<>();

            for (Iterator<Entry<String, Statistics>> itInstance = this.statistics.iterator(); itInstance.hasNext();)
            {
                final Entry<String, Statistics> instance = itInstance.next();

                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList("nfInstanceId")).append('=').append(Arrays.asList(instance.getKey()));
                    inRequests.add(new Instance(b.toString(), (double) instance.getValue().getCountInHttpRequests().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getCountOutHttpResponsesPerStatus().iterator(); itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList("nfInstanceId", "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    outAnswers.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }
            }

            result.add(new Counter("eric_loadgen_nnrf_http_in_requests_total", "Number of incoming HTTP requests on the Nnrf interface", inRequests));
            result.add(new Counter("eric_loadgen_nnrf_http_out_answers_total", "Number of outgoing HTTP answers on the Nnrf interface", outAnswers));
        }

        return result;
    }

    @Override
    public List<Event.Sequence> getEsa()
    {
        final List<Event.Sequence> result = new ArrayList<>();

        for (Iterator<Entry<String, Statistics>> it = this.statistics.iterator(); it.hasNext();)
            result.add(it.next().getValue().getHistoryOfEvents());

        return result;
    }

    public Statistics.Pool getStatistics()
    {
        return this.statistics;
    }

    public void run()
    {
        Completable.complete()//
                   .andThen(this.webServerInt.startListener())
                   .andThen(this.monitored.start())
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
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.webServerInt.stopListener().onErrorComplete(logErr))
                          .andThen(Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)));
    }

    void setConfiguration(final Optional<Configuration> config)
    {
        this.config.set(config);
        RlfLoad.Controller.singleton().publish(config);
    }
}
