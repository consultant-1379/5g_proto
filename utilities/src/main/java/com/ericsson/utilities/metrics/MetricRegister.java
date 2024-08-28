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
 * Created on: Dec 4, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.metrics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Singleton that holds all Prometheus metrics (Counter, Gauge) used throughout
 * the application. This is needed to have access to a metric from everywhere
 * and to have the possibility to remove instances of a metric.
 */
public class MetricRegister
{
    private static class Entry
    {
        private static final int DELAY_IN_MILLIS = 20 * 1000; // 20 s

        private final long expiresAtMillis;
        private final List<String> labelValues;

        public Entry(final Rdn rdn)
        {
            this(rdnToLabelValues(rdn), DELAY_IN_MILLIS);
        }

        public Entry(final List<String> labelValues,
                     int delayMillis)
        {
            this.expiresAtMillis = System.currentTimeMillis() + delayMillis;
            this.labelValues = labelValues;
        }

        public List<String> getLabelValues()
        {
            return this.labelValues;
        }

        public boolean isExpired(final long timeStampMillis)
        {
            return timeStampMillis > this.expiresAtMillis;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("expiresAtMillis=")
                                      .append(this.expiresAtMillis)
                                      .append(", labelValues=")
                                      .append(this.labelValues)
                                      .append("}")
                                      .toString();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MetricRegister.class);
    private static final MetricRegister singleton = new MetricRegister();

    public static void main(String[] args) throws Throwable
    {
        MetricRegister.singleton().start().blockingAwait();
        Thread.sleep(2000);

        final Gauge gcScpLoad = MetricRegister.singleton()
                                              .register(Gauge.build()
                                                             .namespace("scp")
                                                             .name("load")
                                                             .labelNames("nf", "nf_instance", "nrf_group")
                                                             .help("The SCP load [%].")
                                                             .register());

        gcScpLoad.describe().forEach(mfs -> log.info("name={}", mfs.name));

        Rdn rdn = new Rdn("nf", "scp-function").add("nf-instance", "instance-1").add("nrf-group", "group-1");
        Rdn rdn1 = new Rdn("nf", "scp-function").add("nf-instance", "instance-1").add("nrf-group", "group-2");
        Rdn rdn2 = new Rdn("nf", "scp-function").add("nf-instance", "instance-2").add("nrf-group", "group-1");

        List<String> needle = new ArrayList<>();
        needle.add(rdn.last().value().split("-")[0]);
        needle.addAll(rdn.values(false, null, "nf-instance")); // Parent-first is the order of the counter RDN-labels.

        gcScpLoad.labels(rdnToLabelValues(rdn).toArray(new String[0])).set(44);
        gcScpLoad.labels(rdnToLabelValues(rdn1).toArray(new String[0])).set(44);
        gcScpLoad.labels(rdnToLabelValues(rdn2).toArray(new String[0])).set(44);

        MetricRegister.singleton().registerForRemoval(rdn);
        Thread.sleep(1500);
        MetricRegister.singleton().registerForRemoval(new Rdn("nf", "scp-function").add("nf-instance", "instance-1"));

        rdnToLabelValues(rdn.last()).forEach(log::info);

        Rdn rdn3 = new Rdn("namespace", "scp").add("bucket", "bucket1");
        rdnToLabelValues(rdn3).forEach(log::info);
        rdn3.values(false, null, null).forEach(log::info);

        Thread.sleep(6000);

        MetricRegister.singleton().stop().blockingAwait();

        Thread.sleep(2000);
    }

    public static List<String> rdnToLabelValues(final Rdn rdn)
    {
        final List<String> labelValues = new ArrayList<>();

        labelValues.add(rdn.last().value().split("-")[0]); // Prometheus metric-labels all start with nf which is equal to the first
                                                           // part of the rdn's root value: nf=scp-function --> nf=scp.
        labelValues.addAll(rdn.values(false, null, "nf-instance")); // Parent-first is the order of the counter RDN-labels.

        return labelValues;
    }

    public static MetricRegister singleton()
    {
        return singleton;
    }

    private Disposable updater;
    private Map<String, SimpleCollector<?>> collectors;
    private List<Entry> toBeRemoved;

    private MetricRegister()
    {
        this.updater = null;
        this.collectors = new HashMap<>();
        this.toBeRemoved = new ArrayList<>();
    }

    public Counter register(final Counter metric)
    {
        this.put(metric);
        return metric;
    }

    public Gauge register(final Gauge metric)
    {
        this.put(metric);
        return metric;
    }

    public synchronized void registerForRemoval(final Rdn rdn)
    {
        final Entry e = new Entry(rdn);
        log.info("Registering for removal: {}", e);
        this.toBeRemoved.add(e);
    }

    public synchronized void registerForRemoval(final List<String> labelValues,
                                                final int delayMillis)
    {
        final Entry e = new Entry(labelValues, delayMillis);
        log.info("Registering for removal: {}", e);
        this.toBeRemoved.add(e);
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (this.updater == null)
            {
                this.updater = Flowable.fromCallable(this::update)
                                       .subscribeOn(Schedulers.io())
                                       .repeatWhen(h -> h.delay(1, TimeUnit.SECONDS))
                                       .ignoreElements()
                                       .onErrorComplete()
                                       .doOnSubscribe(d -> log.info("Started updating metric instances."))
                                       .doOnDispose(() -> log.info("Stopped updating metric instances."))
                                       .subscribe(() -> log.info("Stopped updating metric instances."),
                                                  t -> log.error("Stopped updating metric instances. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
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

    private void put(final SimpleCollector<?> metric)
    {
        synchronized (this.collectors)
        {
            for (MetricFamilySamples mfs : metric.collect())
            {
                log.info("Registering metric: {}", mfs.name);

                if (this.collectors.put(mfs.name, metric) != null)
                    log.error("Metric has already been registered: {}", mfs.name);
            }
        }
    }

    private void removeMetricInstances(final List<String> needle)
    {
        log.info("Removing all metric instances matching '{}'", needle);

        for (Enumeration<MetricFamilySamples> e = CollectorRegistry.defaultRegistry.metricFamilySamples(); e.hasMoreElements();)
        {
            MetricFamilySamples mfs = e.nextElement();

            for (Sample s : mfs.samples)
            {
                if (s.labelValues.isEmpty())
                    continue;

                if (s.labelValues.containsAll(needle))
                {
                    final SimpleCollector<?> metric = this.collectors.get(mfs.name);

                    if (metric != null)
                    {
                        final StringBuilder b = new StringBuilder();
                        b.append(s.labelNames).append('=').append(s.labelValues);
                        log.info("Removing instance {}{}", mfs.name, b);
                        metric.remove(s.labelValues.toArray(new String[0]));
                    }
                }
            }
        }
    }

    private synchronized Boolean update()
    {
        final long nowInMmillis = System.currentTimeMillis();
        final List<Entry> removed = new ArrayList<>();

        for (final Entry entry : this.toBeRemoved)
        {
            if (entry.isExpired(nowInMmillis))
            {
                log.debug("timestamp={}, entryForRemoval: {}", nowInMmillis, entry);
                removed.add(entry);
                this.removeMetricInstances(entry.getLabelValues());
            }
        }

        this.toBeRemoved.removeAll(removed);

        return true;
    }
}
