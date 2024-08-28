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
 * Created on: Feb 4, 2022
 *     Author: eedstl
 */

package com.ericsson.utilities.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.MovingAverage;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Encapsulates statistics related to latency measurements.
 */

@JsonPropertyOrder({ "samples", "average", "histogram" })
public class SampleStatistics
{
    private static final Logger log = LoggerFactory.getLogger(SampleStatistics.class);
    private static final ObjectMapper json = Jackson.om();

    private static final Map<String, SampleStatistics> registry = new ConcurrentSkipListMap<>();

    /**
     * Map sample to buckets of multiple of 5 (0 5 10 15 ...)
     */
    public static final LongUnaryOperator MAP_TO_MULTIPLE_OF_5 = s -> s / 5 * 5;

    /**
     * Map sample to buckets of multiple of 10 (0 10 20 ...)
     */
    public static final LongUnaryOperator MAP_TO_MULTIPLE_OF_10 = s -> s / 10 * 10;

    /**
     * Map sample to buckets of the power of 2 (0 1 2 4 8 ...)
     */
    public static final LongUnaryOperator MAP_TO_POWER_OF_2 = s -> s < 1 ? 0l : (1l << Math.getExponent(s));

    /**
     * Map sample to buckets of the power of 10 (0 1 10 100 ...)
     */
    public static final LongUnaryOperator MAP_TO_POWER_OF_10 = s -> s < 1 ? 0l : (long) Math.pow(10, (int) Math.log10(s));

    /**
     * Map sample to buckets of the power of x.
     */
    public static final LongBinaryOperator MAP_TO_POWER_OF_X = (s,
                                                                x) -> s < 1 ? 0l : (long) Math.pow(x, (int) (Math.log(s) / Math.log(x)));

    public static void main(String[] args) throws InterruptedException
    {
        {
            final SampleStatistics lm = SampleStatistics.of("a", MAP_TO_MULTIPLE_OF_10);

            for (int i = 0; i < 100; ++i)
            {
                lm.add(i);
            }
        }

        {
            final SampleStatistics lm = SampleStatistics.of("b", MAP_TO_POWER_OF_2);

            for (int i = 0; i < 100; ++i)
            {
                lm.add(i);
            }
        }

        {
            final SampleStatistics lm = SampleStatistics.of("c", MAP_TO_POWER_OF_10);

            for (int i = 0; i < 1000; ++i)
            {
                lm.add(i);
            }
        }

        log.info("Statistics={}", SampleStatistics.print());
    }

    /**
     * Create a new instance of {@code SampleStatistics}.
     * <p>
     * Shorthand for {@code SampleStatistics.of(name, MAP_TO_POWER_OF_2}).
     * 
     * @param name The name of the statistics.
     * @return A new instance of {@code SampleStatistics}.
     */
    public static SampleStatistics of(final String name)
    {
        return new SampleStatistics(name, MAP_TO_POWER_OF_2);
    }

    /**
     * Create a new instance of {@code SampleStatistics}.
     * <p>
     * Parameter {@code sampleMapper} determines how a sample is mapped to the
     * buckets of the internal histogram. For your convenience, you may use these:
     * <ul>
     * <li>{@link #MAP_TO_MULTIPLE_OF_5}</li>
     * <li>{@link #MAP_TO_MULTIPLE_OF_10}</li>
     * <li>{@link #MAP_TO_POWER_OF_2}</li>
     * <li>{@link #MAP_TO_POWER_OF_10}</li>
     * </ul>
     * 
     * @param name         The name of the statistics.
     * @param sampleMapper Function that maps a sample to a bucket in the histogram.
     * @return A new instance of {@code SampleStatistics}.
     */
    public static SampleStatistics of(final String name,
                                      final LongUnaryOperator sampleMapper)
    {
        return new SampleStatistics(name, sampleMapper);
    }

    public static void reset()
    {
        registry.values().forEach(SampleStatistics::clear);
    }

    public static String print()
    {
        return print(false);
    }

    public static String print(final boolean clearAfterwards)
    {
        final String s = registry.toString();

        if (clearAfterwards)
            registry.values().forEach(SampleStatistics::clear);

        return s;
    }

    @JsonProperty("samples")
    private final AtomicLong samples = new AtomicLong();

    @JsonIgnore
    private final AtomicDouble sum = new AtomicDouble();

    @JsonIgnore
    private final MovingAverage avg = new MovingAverage(1000);

    @JsonIgnore
    private final AtomicReference<Instant> start = new AtomicReference<>();

    @JsonIgnore
    private final Map<Long, Long> histogram = new ConcurrentSkipListMap<>();

    @JsonIgnore
    private final LongUnaryOperator sampleMapper;

    private SampleStatistics(final String name,
                             final LongUnaryOperator sampleMapper)
    {
        this.sampleMapper = sampleMapper;

        this.start.set(Instant.now());
        registry.put(name, this);
    }

    /**
     * Adds a time-based sample to the statistics. The sample is the timespan [ms]
     * from {@code startTime} till now.
     * 
     * @param startTime The time when the measurement started.
     */
    public void add(final Instant startTime)
    {
        final long sample = Duration.between(startTime, Instant.now()).toMillis();
        this.samples.incrementAndGet();
        this.sum.addAndGet(sample);
        this.avg.add(sample);
        this.histogram.compute(this.sampleMapper.applyAsLong(sample),
                               (k,
                                v) -> (v == null) ? 1 : v + 1);
    }

    /**
     * Adds a sample to the statistics.
     * 
     * @param sample The sample to be added.
     */
    public void add(final long sample)
    {
        this.samples.incrementAndGet();
        this.sum.addAndGet(sample);
        this.avg.add(sample);
        this.histogram.compute(this.sampleMapper.applyAsLong(sample),
                               (k,
                                v) -> (v == null) ? 1 : v + 1);
    }

    public void clear()
    {
        this.histogram.clear();
        this.samples.set(0);
        this.sum.set(0);
        this.start.set(Instant.now());
    }

    @Override
    public String toString()
    {
        final long samples = this.samples.get();
        final double average = samples == 0 ? 0 : this.sum.get() / samples;
        final double average1000 = this.avg.get();
        long duration = Duration.between(this.start.get(), Instant.now()).toMillis();
        final double rate = (double) samples / duration * 1000;
        final Map<Long, String> percentile = this.toPercentile();

        return new StringBuilder().append("{")
                                  .append("samples=")
                                  .append(samples)
                                  .append(",duration[ms]=")
                                  .append(duration)
                                  .append(",rate[Hz]=")
                                  .append(String.format("%.2f", rate))
                                  .append(",average[ms]=")
                                  .append(String.format("%.2f", average))
                                  .append(",average1000[ms]=")
                                  .append(String.format("%.2f", average1000))
                                  .append(",percentile[ms][%]=")
                                  .append(percentile)
                                  .append(",histogram[ms][1]=")
                                  .append(this.histogram)
                                  .append("}")
                                  .toString();
    }

    @JsonProperty("histogram")
    private Object getHistogram()
    {
        final Map<Long, Long> copy = this.histogram.entrySet()
                                                   .stream()
                                                   .collect(Collectors.toMap(Entry::getKey,
                                                                             Entry::getValue,
                                                                             (o1,
                                                                              o2) -> o1,
                                                                             TreeMap::new));

        final long sum = copy.entrySet().stream().map(Entry::getValue).collect(Collectors.summingLong(Long::longValue));
        final AtomicLong partialSum = new AtomicLong();

        return copy.entrySet()
                   .stream()//
                   .collect(Collectors.toMap(Entry::getKey, entry ->
                   {
                       final SortedMap<String, Object> o = new TreeMap<>();
                       long p = partialSum.addAndGet(entry.getValue());
                       o.put("#", entry.getValue());
                       o.put("%", Double.parseDouble(String.format("%.2f", p * 100d / sum)));
                       return o;
                   },
                                             (o1,
                                              o2) -> o1,
                                             TreeMap::new));
    }

    private Map<Long, String> toPercentile()
    {
        final Map<Long, Long> copy = this.histogram.entrySet()
                                                   .stream()
                                                   .collect(Collectors.toMap(Entry::getKey,
                                                                             Entry::getValue,
                                                                             (o1,
                                                                              o2) -> o1,
                                                                             TreeMap::new));

        final long sum = copy.entrySet().stream().map(Entry::getValue).collect(Collectors.summingLong(Long::longValue));

        final Map<Long, String> percentile = new TreeMap<>();

        long partialSum = 0;

        for (Entry<Long, Long> e : copy.entrySet())
        {
            partialSum += e.getValue();
            percentile.put(e.getKey(), String.format("%.2f", partialSum * 100d / sum));
        }

        return percentile;
    }
}
