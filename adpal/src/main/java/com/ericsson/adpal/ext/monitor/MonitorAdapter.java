package com.ericsson.adpal.ext.monitor;

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
 * Created on: Nov 14, 2018
 *     Author: eedstl
 */

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Commands;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Instance;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.ext.monitor.api.v0.register.Callback;
import com.ericsson.adpal.ext.monitor.api.v0.register.Register;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.metrics.SampleStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

/**
 * Encapsulates the periodic registration of a monitored source with its
 * monitor.
 */
public class MonitorAdapter
{
    public abstract static class CommandBase
    {
        private final String id;
        private final String description;

        protected CommandBase(final String id,
                              final String description)
        {
            this.id = id;
            this.description = description;
        }

        public String description()
        {
            return this.description;
        }

        public abstract HttpResponseStatus execute(final Result result,
                                                   final Command request);

        public String id()
        {
            return this.id;
        }
    }

    public static class CommandCounter extends MonitorAdapter.CommandBase
    {
        public interface Provider
        {
            List<Counter> getCounters(final boolean readThenClear);
        }

        private static class Family
        {
            public final String name;
            public final String help;

            public Family(final String name,
                          final String help)
            {
                this.name = name;
                this.help = help;
            }

            @Override
            public boolean equals(final Object obj)
            {
                if (obj == null)
                    return false;

                if (this.getClass() != obj.getClass())
                    return false;

                return this.name.equals(((Family) obj).name);
            }

            @Override
            public int hashCode()
            {
                return this.name.hashCode();
            }
        }

        Map<Family, Map<String, Double>> previousValues;

        private final Provider provider;

        public CommandCounter()
        {
            this(null);
        }

        public CommandCounter(final Provider owner)
        {
            super("counter", "Usage: command=counter[&clear[=<true|false>]]");
            this.provider = owner;
            this.previousValues = new ConcurrentHashMap<>();
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            result.setAdditionalProperty("counters", this.getCounters(Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"))));
            return HttpResponseStatus.OK;
        }

        public synchronized List<Counter> getCounters(final boolean readThenClear)
        {
            final Map<Family, Map<String, Double>> currentValues = new HashMap<>();
            final List<Counter> counters = new ArrayList<>();

            for (Enumeration<MetricFamilySamples> e = CollectorRegistry.defaultRegistry.metricFamilySamples(); e.hasMoreElements();)
            {
                final MetricFamilySamples family = e.nextElement();

                int i = 0;

                for (final Sample s : family.samples)
                {
                    // Hack for the time being: family.samples seem to contain the value of a sample
                    // and its time stamp. The problem is that one doesn't know which sample is the
                    // value and which is the time stamp. It looks like that the time stamp comes
                    // always after the value (i.e. on odd indices)?
                    if ((i++ % 2) == 1 || s.labelValues.isEmpty())
                    {
                        continue;
                    }

                    final String name = new StringBuilder().append(s.labelNames).append('=').append(s.labelValues).toString();
                    currentValues.computeIfAbsent(new Family(family.getNames()[0], family.help), k -> new ConcurrentHashMap<>()).put(name, s.value);
                }
            }

            currentValues.entrySet().forEach(ef ->
            {
                final List<Instance> instances = new ArrayList<>();

                ef.getValue().entrySet().forEach(ev ->
                {
                    final Double previousValue = this.previousValues.getOrDefault(ef.getKey(), new ConcurrentHashMap<>()).getOrDefault(ev.getKey(), 0d);
                    instances.add(new Instance(ev.getKey(), ev.getValue() - previousValue));
                });

                counters.add(new Counter(ef.getKey().name, ef.getKey().help, instances));
            });

            if (readThenClear)
                this.previousValues = currentValues;

            if (this.provider != null)
                counters.addAll(this.provider.getCounters(readThenClear));

            return counters;
        }
    }

    public static class CommandEsa extends MonitorAdapter.CommandBase
    {
        public interface Provider
        {
            List<Event.Sequence> getEsa();
        }

        private final Provider owner;

        public CommandEsa(Provider owner)
        {
            super("esa", "Usage: command=esa");
            this.owner = owner;
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            result.setAdditionalProperty("events", this.owner.getEsa());
            return HttpResponseStatus.OK;
        }
    }

    public static class CommandRegistry
    {
        private final SortedMap<String, CommandBase> commands = new TreeMap<>();

        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            String id = request.getId();

            if (id.isEmpty() || id.equals("list"))
            {
                for (CommandBase c : this.commands.values())
                    result.setAdditionalProperty(c.id(), c.description());

                return HttpResponseStatus.OK;
            }

            CommandBase command = this.commands.get(id);

            if (command == null)
            {
                result.setAdditionalProperty(ERROR_MESSAGE, HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Unknown command: '" + id + "'.");
                return HttpResponseStatus.BAD_REQUEST;
            }

            return command.execute(result, request);
        }

        void add(CommandBase command)
        {
            this.commands.put(command.id(), command);
        }
    }

    public static class CommandTestAlarm extends MonitorAdapter.CommandBase
    {
        private static final String SEVERITIES = new StringBuilder().append(Severity.CLEAR.name())
                                                                    .append("|")
                                                                    .append(Severity.WARNING.name())
                                                                    .append("|")
                                                                    .append(Severity.MINOR.name())
                                                                    .append("|")
                                                                    .append(Severity.MAJOR.name())
                                                                    .append("|")
                                                                    .append(Severity.CRITICAL.name())
                                                                    .toString();

        private final FmAlarmService alarmService;
        @SuppressWarnings("unused")
        private final String nfType;
        private final String nfInstance;
        private final String serviceName;

        public CommandTestAlarm(String serviceName,
                                final FmAlarmService alarmService,
                                final String nfType,
                                final String nfInstance)
        {
            super("testAlarm", new StringBuilder("Usage: command=testAlarm&severity=<").append(SEVERITIES).append(">").toString());
            this.serviceName = serviceName;
            this.alarmService = alarmService;
            this.nfType = nfType;
            this.nfInstance = nfInstance;
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            final String paramSeverity = (String) request.getAdditionalProperties().get("severity");

            if (paramSeverity == null)
            {
                result.setAdditionalProperty(ERROR_MESSAGE, HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Missing argument: 'severity'");
                return HttpResponseStatus.BAD_REQUEST;
            }

            Severity severity = null;

            try
            {
                severity = Severity.valueOf(paramSeverity);
            }
            catch (IllegalArgumentException e)
            {
                result.setAdditionalProperty(ERROR_MESSAGE,
                                             HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'severity=" + paramSeverity
                                                            + "'. Must be one of '" + SEVERITIES + "'.");
                return HttpResponseStatus.BAD_REQUEST;
            }
            catch (Exception err)
            {
                log.error("Unexpected error, clearing alarm", err);
                severity = Severity.CLEAR;
            }

            try
            {
                final var faultIndication = new FaultIndicationBuilder().withFaultName("Test") //
                                                                        .withFaultyResource(this.nfInstance) //
                                                                        .withServiceName(this.serviceName)
                                                                        .withSeverity(severity) //
                                                                        .withDescription("This is a test alarm, please ignore.") //
                                                                        .withExpiration(60L) //
                                                                        .build();

                this.alarmService.raise(faultIndication).subscribe(() ->
                {
                }, t -> log.error("Error raising alarm. Cause: {}", log.isDebugEnabled() ? t : t.toString()));

                return HttpResponseStatus.OK;
            }
            catch (Exception err)
            {
                log.error("Failed to raise/cease alarm", err);
                result.setAdditionalProperty(ERROR_MESSAGE, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase() + ": Unexpected error:" + err.getMessage());
                return HttpResponseStatus.BAD_REQUEST;
            }
        }
    }

    public static class CommandVersion extends MonitorAdapter.CommandBase
    {
        public CommandVersion()
        {
            super("version", "Usage: command=version");
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            result.setAdditionalProperty("version", VersionInfo.get());
            return HttpResponseStatus.OK;
        }
    }

    private static class CommandCpu extends MonitorAdapter.CommandBase
    {
        public CommandCpu()
        {
            super("cpu", "Usage: command=cpu");
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            result.setAdditionalProperty("availableProcessors", osBean.getAvailableProcessors());
            result.setAdditionalProperty("processCpuLoad", osBean.getProcessCpuLoad());
            result.setAdditionalProperty("systemCpuLoad", osBean.getSystemCpuLoad());
            result.setAdditionalProperty("systemLoadAverage", osBean.getSystemLoadAverage());
            return HttpResponseStatus.OK;
        }
    }

    private static class CommandLog extends MonitorAdapter.CommandBase
    {
        public CommandLog()
        {
            super("log",
                  "Usage: command=log or command=log&logger=<name> or command=log&logger=<name>&level=<OFF|ERROR|WARN|INFO|DEBUG|TRACE> or command=log&interval=<logging interval[s], default: 5>");
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            final String paramLogger = (String) request.getAdditionalProperties().get("logger");
            final String paramLevel = (String) request.getAdditionalProperties().get("level");
            final String paramInterval = (String) request.getAdditionalProperties().get("interval");

            if (paramInterval != null && !paramInterval.isEmpty())
            {
                LogThrottler.setLoggingIntervalSecs(Integer.parseInt(paramInterval));
            }

            if (paramLevel != null && !paramLevel.isEmpty())
            {
                // Presence of level -> set log level

                Level level = Level.valueOf(paramLevel);

                if (!level.toString().equals(paramLevel))
                {
                    result.setAdditionalProperty(ERROR_MESSAGE,
                                                 HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'level=" + paramLevel
                                                                + "'. Must be one of 'OFF|ERROR|WARN|INFO|DEBUG|TRACE'.");
                    return HttpResponseStatus.BAD_REQUEST;
                }

                if (paramLogger == null || paramLogger.isEmpty())
                {
                    result.setAdditionalProperty(ERROR_MESSAGE, HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Missing argument: 'logger'.");
                    return HttpResponseStatus.BAD_REQUEST;
                }

                ch.qos.logback.classic.Logger logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).exists(paramLogger);

                if (logger == null)
                {
                    result.setAdditionalProperty(ERROR_MESSAGE,
                                                 HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'logger=" + paramLogger
                                                                + "'. Use 'command=log' to see all valid loggers.");
                    return HttpResponseStatus.BAD_REQUEST;
                }

                logger.setLevel(level);

                // Reset log level of all the children of the logger (which would otherwise hide
                // the parent's log level)

                final List<ch.qos.logback.classic.Logger> loggers = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList();
                final String pattern = new StringBuilder("^").append(paramLogger).append("[.].+$").toString();

                loggers.forEach(l ->
                {
                    final String name = l.getName();

                    if (!name.equals("ROOT") && (paramLogger.equals("ROOT") || name.matches(pattern)))
                        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).setLevel(null);
                });
            }

            result.setAdditionalProperty("interval", LogThrottler.getLoggingIntervalSecs());

            {
                final Set<String> prefixes = new TreeSet<>();

                Pattern pattern = Pattern.compile("^(.*)[.][^.]+$");

                List<ch.qos.logback.classic.Logger> loggerList = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList();

                loggerList.forEach(l ->
                {
                    Matcher matcher = pattern.matcher(l.getName());

                    if (matcher.matches())
                        prefixes.add(matcher.group(1));
                });

                loggerList.forEach(l ->
                {
                    if (!prefixes.contains(l.getName()) && (paramLogger == null || paramLogger.equals("ROOT") || l.getName().startsWith(paramLogger)))
                        result.setAdditionalProperty(l.getName(), l.getEffectiveLevel().toString());
                });
            }

            return HttpResponseStatus.OK;
        }
    }

    private static class CommandMemory extends MonitorAdapter.CommandBase
    {
        public CommandMemory()
        {
            super("memory", "Usage: command=memory[&gc[=<true|false>]]");
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            if (Boolean.parseBoolean((String) request.getAdditionalProperties().get("gc")))
                System.gc();

            result.setAdditionalProperty("heapMemoryUsage", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString());
            result.setAdditionalProperty("nonHeapMemoryUsage", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().toString());
            return HttpResponseStatus.OK;
        }
    }

    public static class CommandStatistics extends MonitorAdapter.CommandBase
    {
        public CommandStatistics()
        {
            super("statistics", "Usage: command=statistics[&clear[=<true|false>]]");
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            result.setAdditionalProperty("statistics", SampleStatistics.print(Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"))));
            return HttpResponseStatus.OK;
        }
    }

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String UNKNOWN = "Unknown";

    private static final Logger log = LoggerFactory.getLogger(MonitorAdapter.class);

    private final ObjectMapper mapper;
    private final RouterHandler server;
    private final URI baseUri;
    private final String id;

    private WebClient client = null;
    private String monitorHost = null;
    private int monitorPort = 0;
    private CommandRegistry commandsGet = null;
    private CommandRegistry commandsPut = null;

    private final List<Disposable> disposables = new ArrayList<>();

    public MonitorAdapter(final RouterHandler server,
                          final List<CommandBase> commandsGet,
                          final List<CommandBase> commandsPut) throws UnknownHostException
    {
        this.mapper = Jackson.om();
        this.server = server;
        this.baseUri = determineAndRegisterNotificationCallbackUri(server);
        log.debug("baseUri={}", this.baseUri);
        this.id = InetAddress.getLocalHost().getHostName();

        try
        {
            this.monitorHost = MonitorContext.MONITOR_SERVICE + "."
                               + new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
            this.monitorPort = MonitorContext.MONITOR_PORT_HTTP_INTERNAL;
            this.client = WebClient.create(server.getVertx(), new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1));

            // Add routes for all publicly supported Monitor operations to the web-server

            {
                final var router = Router.router(server.getVertx());
                router.get(MonitorContext.Operation.COMMANDS.getName()).handler(this::handlerCommandsGet);
                router.put(MonitorContext.Operation.COMMANDS.getName()).handler(this::handlerCommandsPut);
                this.server.mountRouter(MonitorContext.MONITOR_API, router);
            }

            {
                final var router = Router.router(server.getVertx());
                router.route(MonitorContext.Operation.SUBSCRIPTIONS.getName()).handler(this::handlerSubscriptions);
                this.server.mountRouter(MonitorContext.MONITOR_API, router);
            }

            {
                // Register all commands for GET operation.

                this.commandsGet = new CommandRegistry();
                this.commandsGet.add(new CommandCpu());
                this.commandsGet.add(new CommandLog());
                this.commandsGet.add(new CommandMemory());
                this.commandsGet.add(new CommandStatistics());
                this.commandsGet.add(new CommandVersion());
                commandsGet.forEach(c -> this.commandsGet.add(c));
            }

            {
                // Register all commands for PUT operation.

                this.commandsPut = new CommandRegistry();
                this.commandsPut.add(new CommandLog());
                commandsPut.forEach(c -> this.commandsPut.add(c));
            }
        }
        catch (Exception e)
        {
            log.info("Problems instantiating MonitorAdapter: {}", e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public Completable start()
    {
        final AtomicReference<Map<String, Map<String, Instance>>> buf = new AtomicReference<>(new HashMap<>());

        return Completable.fromAction(() ->
        {
            if (this.disposables.isEmpty() && this.monitorHost != null /* running in Kubernetes environment */)
            {
                // Start the registration handler. Re-subscribe after the given time interval,
                // ignoring any errors

                this.disposables.add(this.update() //
                                         .timeout(1500, TimeUnit.MILLISECONDS)
                                         .doOnSubscribe(e -> log.debug("Updating monitor."))
                                         .doOnError(e -> log.debug("Updating monitor failed. Cause: {}", e.toString()))
                                         .onErrorReturn(t -> HttpResponseStatus.OK.code()) /* Ignore errors here, as the Monitor may not be deployed. */
                                         .repeatWhen(handler -> handler.delay(10, TimeUnit.SECONDS))
                                         .ignoreElements()
                                         .doOnSubscribe(d -> log.info("Started updating monitor."))
                                         .subscribe(() -> log.info("Stopped updating monitor."),
                                                    t -> log.error("Stopped updating monitor. Cause: {}", t.toString())));
                this.disposables.add(Completable.fromAction(() ->
                {
                    try
                    {
                        final Result result = new Result();
                        final HttpResponseStatus status = this.commandsGet.execute(result, new Command(0l, "counter"));

                        if (status.code() == HttpResponseStatus.OK.code())
                        {
                            final Map<String, Map<String, Instance>> curr = new TreeMap<>();

                            Optional.ofNullable(((List<Counter>) result.getAdditionalProperties().get("counters")))
                                    .ifPresent(counters -> counters.stream()
                                                                   // Take only CC counters (e.g., count_total or count_total_unit)
                                                                   .filter(counter -> counter.getName().matches("^.+_total(?:_[^_]+)?$"))
                                                                   .forEach(counter -> counter.getInstances()
                                                                                              .stream()
                                                                                              .forEach(instance -> curr.computeIfAbsent(counter.getName(),
                                                                                                                                        k -> new TreeMap<>())
                                                                                                                       .put(instance.getName(), instance))));

                            final Map<String, Map<String, Instance>> prev = buf.getAndSet(curr);

                            final List<Counter> deltaCounters = new ArrayList<>();

                            curr.entrySet()
                                .stream()//
                                .forEach(ec ->
                                {
                                    final String currName = ec.getKey();
                                    final Map<String, Instance> cInstances = ec.getValue();

                                    final List<Instance> deltaInstances = new ArrayList<>();

                                    Optional.ofNullable(prev.get(currName))//
                                            .ifPresentOrElse(pInstances -> cInstances.values()
                                                                                     .stream()
                                                                                     .forEach(cInstance -> Optional.ofNullable(pInstances.get(cInstance.getName()))
                                                                                                                   .ifPresentOrElse(pInstance ->
                                                                                                                   {
                                                                                                                       final double delta = cInstance.getValue()
                                                                                                                                            - pInstance.getValue();

                                                                                                                       if (delta != 0)
                                                                                                                           deltaInstances.add(new Instance(cInstance.getName(),
                                                                                                                                                           delta));
                                                                                                                   }, () -> deltaInstances.add(cInstance))),
                                                             () -> deltaInstances.addAll(cInstances.values()));

                                    if (!deltaInstances.isEmpty())
                                        deltaCounters.add(new Counter(currName, null, deltaInstances));
                                });

                            if (!deltaCounters.isEmpty())
                                log.info("{}", this.mapper.writeValueAsString(Map.of("deltaCounters", deltaCounters)));
                        }
                    }
                    catch (Exception e)
                    {
                        // Just ignore.
                        log.error("Exception caught", e);
                    }
                })
                                                .repeatWhen(handler -> handler.delay(60, TimeUnit.SECONDS))
                                                .doOnSubscribe(d -> log.info("Started logging counters."))
                                                .subscribe(() -> log.info("Stopped logging counters."),
                                                           t -> log.error("Stopped logging counters. Cause: {}", t.toString())));
            }
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();
        });
    }

    private void handlerCommandsGet(final RoutingContext routingContext)
    {
        this.handlerCommands(routingContext, this.commandsGet);
    }

    private void handlerCommands(final RoutingContext routingContext,
                                 final CommandRegistry registry)
    {
        routingContext.request().bodyHandler(buffer ->
        {
            try
            {
                log.debug("buffer={}", buffer);

                final Commands request = this.mapper.readValue(buffer.toJsonObject().toString(), Commands.class);
                log.debug("Command request received: {}", request);

                this.server.getVertx().<Pair<HttpResponseStatus, Result>>executeBlocking(() ->
                {
                    final Result result = new Result(System.currentTimeMillis(), this.id, HttpResponseStatus.OK.code());
                    final HttpResponseStatus status = registry.execute(result, request.getCommand());
                    result.setStatusCode(status.code());
                    return Pair.of(status, result);
                }, false, res ->
                {
                    try
                    {
                        if (res.succeeded())
                        {
                            final List<Result> results = new ArrayList<>();
                            results.add(res.result().getSecond());
                            request.setResults(results);
                            routingContext.response().setStatusCode(res.result().getFirst().code()).end(this.mapper.writeValueAsString(request));
                        }
                        else
                        {
                            this.replyWithError(routingContext, request, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unexpected exception: " + res.cause());
                        }
                    }
                    catch (final JsonProcessingException jpe)
                    {
                        log.error("Unexpected JSON error during monitor command execution", jpe);
                        this.replyWithError(routingContext, request, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unexpected JSON exception: " + jpe);
                    }
                });
            }
            catch (final Exception e)
            {
                log.error("Unexpected JSON error during monitor command execution", e);
                this.replyWithError(routingContext,
                                    new Commands(new Command(System.currentTimeMillis(), UNKNOWN), new ArrayList<>()),
                                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                    "Unexpected exception while processing command: " + e);
            }
        });
    }

    private void handlerCommandsPut(final RoutingContext routingContext)
    {
        this.handlerCommands(routingContext, this.commandsPut);
    }

    private void handlerSubscriptions(final RoutingContext routingContext)
    {
        final Commands commands = new Commands(new Command(System.currentTimeMillis(), UNKNOWN), new ArrayList<>());
        this.replyWithError(routingContext, commands, HttpResponseStatus.NOT_IMPLEMENTED, "Not implemented, yet.");
    }

    private Single<Integer> post()
    {
        final List<Callback> cbs = new ArrayList<>();
        cbs.add(new Callback(MonitorContext.Operation.COMMANDS.getName(), this.id, this.baseUri));
        final Register request = new Register(this.id, cbs, "");
        final String requestUri = MonitorContext.MONITOR_API + MonitorContext.Operation.REGISTER.getName();

        return this.client.post(this.monitorPort, this.monitorHost, requestUri)
                          .rxSendJson(request)
                          .doOnSubscribe(d -> log.debug("POST request: {}:{}{} {}",
                                                        this.monitorHost,
                                                        this.monitorPort,
                                                        requestUri,
                                                        this.mapper.writeValueAsString(request)))
                          .doOnSuccess(resp -> log.debug("POST response: {} {}", resp.statusCode(), resp.bodyAsString()))
                          .map(HttpResponse::statusCode);
    }

    private void replyWithError(final RoutingContext routingContext,
                                final Commands commands,
                                final HttpResponseStatus status,
                                final String errorMsg)
    {
        final Result result = new Result(System.currentTimeMillis(), this.id, status.code());
        result.setAdditionalProperty(ERROR_MESSAGE, errorMsg);
        commands.getResults().add(result);

        try
        {
            routingContext.response().setStatusCode(status.code()).end(this.mapper.writeValueAsString(commands));
        }
        catch (JsonProcessingException e)
        {
            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
        }
    }

    private Single<Integer> update()
    {
        return this.post();
    }

    private URI determineAndRegisterNotificationCallbackUri(final RouterHandler routerHandler) throws UnknownHostException
    {
        final String notificationRelativeUrl = MonitorContext.MONITOR_API + MonitorContext.Operation.COMMANDS.getName();

        final String host = routerHandler.baseUri().getHost();

        if (host.equals("[::]") || host.equals("::") || host.equals("0.0.0.0"))
        {
            // Wildcarded host address, needs to be replaced by a real address. Make a copy
            // of the base URI and replace the host part by the local host address. This
            // is possible as the web server is listening on all interfaces.

            try
            {
                return new URI(routerHandler.baseUri().getScheme(),
                               null,
                               InetAddress.getLocalHost().getHostAddress(),
                               routerHandler.baseUri().getPort(),
                               notificationRelativeUrl,
                               routerHandler.baseUri().getQuery(),
                               routerHandler.baseUri().getFragment());
            }
            catch (UnknownHostException | URISyntaxException e)
            {
                log.warn("Unable to register callback URI to monitor.");
                return null;
            }

        }
        else
        {
            return routerHandler.baseUri().resolve(notificationRelativeUrl);
        }
    }
}
