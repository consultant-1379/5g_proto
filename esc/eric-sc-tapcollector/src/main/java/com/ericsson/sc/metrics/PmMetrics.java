package com.ericsson.sc.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PmMetrics
{
    private static final Logger log = LoggerFactory.getLogger(PmMetrics.class);
    static PmMetrics metrics;
    private final HashMap<String, String> replace = new HashMap<>();
    private final HashMap<String, Object> counters = new HashMap<>();
    private final ArrayList<String> deny = new ArrayList<>();
    private final ArrayList<String> accept = new ArrayList<>();
    private final ArrayList<String> acceptStartWith = new ArrayList<>();
    private final PrometheusMeterRegistry pmRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private final ArrayList<String> defaultTags = new ArrayList<>();

    public static PmMetrics factory()
    {
        if (PmMetrics.metrics == null)
            PmMetrics.metrics = new PmMetrics();
        return PmMetrics.metrics;
    }

    public Object getMeter(String mapEntryName) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        return this.counters.get(mapEntryName);
    }

    public PmMetrics()
    {
        Metrics.addRegistry(pmRegistry);
        pmRegistry.config().meterFilter(filter);
        log.debug("New PmMetrics registry has been created");
    }

    public PrometheusMeterRegistry getRegistry()
    {
        return this.pmRegistry;
    }

    public void replaceWith(String key,
                            String value)
    {
        this.replace.put(key, value);
    }

    public void addCounter(String name,
                           Meter meter)
    {
        this.counters.put(name, meter);
    }

    public void createGauge(String name,
                            String mapEntryName)
    {
        this.counters.put(mapEntryName, this.pmRegistry.gauge(name, Tags.of(defaultTags.toArray(new String[defaultTags.size()])), 0));
        log.debug("New gauge meter has been create with name {}", name);
    }

    public void createGauge(String name,
                            int startingValue,
                            String mapEntryName)
    {
        this.counters.put(mapEntryName,
                          this.pmRegistry.gauge(mapEntryName, Tags.of(defaultTags.toArray(new String[defaultTags.size()])), new AtomicInteger(startingValue)));
        log.debug("New gauge meter has been create with name {}", name);
    }

    public int increaseGauge(String mapEntryName) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        return ((AtomicInteger) this.counters.get(mapEntryName)).incrementAndGet();
    }

    public int setGaugeValue(String mapEntryName,
                             int value) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        return ((AtomicInteger) this.counters.get(mapEntryName)).getAndSet(value);
    }

    public void createCounter(String name,
                              String mapEntryName,
                              String... tags)
    {
        this.counters.put(mapEntryName, this.pmRegistry.counter(name, Tags.concat(Tags.of(defaultTags.toArray(new String[defaultTags.size()])), tags)));
        log.debug("New counter meter has been create with name {}", name);
    }

    public void increaseCounter(String mapEntryName) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        ((Counter) this.counters.get(mapEntryName)).increment();
    }

    public void increaseCounter(String mapEntryName,
                                double value) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        ((Counter) this.counters.get(mapEntryName)).increment(value);
    }

    public void createDistributionSummary(String name,
                                          String mapEntryName,
                                          String... tags)
    {
        this.counters.put(mapEntryName,
                          this.pmRegistry.newDistributionSummary(new Meter.Id(name,
                                                                              Tags.concat(Tags.of(defaultTags.toArray(new String[defaultTags.size()])), tags),
                                                                              null,
                                                                              null,
                                                                              Meter.Type.DISTRIBUTION_SUMMARY).withName(name),
                                                                 DistributionStatisticConfig.DEFAULT,
                                                                 1));
        log.debug("New distribution Summary meter has been create with name {}", name);
    }

    public void increaseDistributionSummary(String mapEntryName,
                                            double value) throws PmMetricsException
    {
        if (!this.counters.containsKey(mapEntryName))
            throw new PmMetricsException("Counter with name {} does not exist", mapEntryName);
        ((DistributionSummary) this.counters.get(mapEntryName)).record(value);
    }

    private MeterFilter filter = new MeterFilter()
    {

        @Override
        public Meter.Id map(Meter.Id id)
        {
            if (replace.containsKey(id.getName()))
                return id.withName(replace.get(id.getName())).withTags(Tags.of(defaultTags.toArray(new String[defaultTags.size()])));
            return id;
        }

        @Override
        public MeterFilterReply accept(Meter.Id id)
        {
            if (deny.contains(id.getName()))
            {
                log.debug("Denied: {}", id.getName());
                return MeterFilterReply.DENY;
            }
            if (accept.contains(id.getName()))
            {
                log.debug("Accepted: {}", id.getName());
                return MeterFilterReply.ACCEPT;
            }
            if (acceptStartWith.stream().filter(prefix -> (id.getName().startsWith(prefix))).count() > 0)
            {
                log.debug("Accepted: {} because it starts with acceptable prefix", id.getName());
                return MeterFilterReply.ACCEPT;
            }
            log.debug("Denied: {}", id.getName());
            return MeterFilterReply.DENY;
        }

    };

    public void addAccept(String name)
    {
        this.accept.add(name);
    }

    public void removeAccept(String name)
    {
        this.accept.remove(name);
    }

    public void addAcceptStartsWith(String name)
    {
        this.acceptStartWith.add(name);
    }

    public void removeAcceptStartsWith(String name)
    {
        this.acceptStartWith.remove(name);
    }

    public void addFilter(MeterFilter filter)
    {
        this.pmRegistry.config().meterFilter(filter);

    }

    public void addDefaultTag(String key,
                              String value)
    {
        this.defaultTags.add(key);
        this.defaultTags.add(value);
    }

    public void removeDefaultTag(String key)
    {
        int pos = this.defaultTags.indexOf(key);
        this.defaultTags.remove(pos + 1);
        this.defaultTags.remove(pos);
    }

    public static void close()
    {
        PmMetrics.metrics = null;
    }

    // TODO create class or abstract class for this
    private static class PmMetricsException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private static String expand(final String msg,
                                     final Object... args)
        {
            final String PATTERN = "{}";
            final StringBuilder b = new StringBuilder();

            for (int i = 0, start = 0;;)
            {
                int stop = msg.indexOf(PATTERN, start);

                if (stop >= 0)
                {
                    b.append(msg.substring(start, stop));

                    if (i < args.length)
                        b.append(args[i++]);
                    else
                        b.append(PATTERN);

                    start = stop + PATTERN.length();
                }
                else
                {
                    b.append(msg.substring(start));
                    break;
                }
            }

            return b.toString();
        }

        /**
         * Create a new RequestException.
         * 
         * @param errorMsg     The message for the exception. Format is like for the
         *                     logger.
         * @param errorMsgArgs The arguments for the error-message.
         */
        public PmMetricsException(final String errorMsg,
                                  final Object... errorMsgArgs)
        {
            super(expand(errorMsg, errorMsgArgs));
        }

        public PmMetricsException(final Throwable e,
                                  final String errorMsg,
                                  final Object... errorMsgArgs)
        {
            super(expand(errorMsg, errorMsgArgs), e);
        }

    }

}
