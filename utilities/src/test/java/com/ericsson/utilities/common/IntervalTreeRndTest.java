package com.ericsson.utilities.common;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.IntervalTreeRnd.Interval;
import com.ericsson.utilities.common.IntervalTreeRnd.Strategy;

public class IntervalTreeRndTest
{
    private static final Logger log = LoggerFactory.getLogger(IntervalTreeRndTest.class);

//    @Test
    void test_0_General() throws IOException
    {
        log.info("===== test_0_General =====");

        int n = 100;

        final IntervalTreeRnd<String> tree = new IntervalTreeRnd<String>();

        for (int i = 0; i < n; ++i)
        {
            final Interval interval = Interval.of(10 * i, 10 * (i + 1) - 1);
            final String value = "" + i;
            tree.add(interval, value);
            tree.add(interval, value + "x");
            tree.add(interval, value + "y");
//                st.remove(interval, value);
        }

        log.info("height={}, size={}, treeSize={}, check={}", tree.height(), tree.size(), tree.treeSize(), tree.check());
        log.info("st={}", tree);
        log.info("------");

        for (int i = 0; i < n; ++i)
        {
            final Interval interval = Interval.of(10 * i, 10 * (i + 1) - 1);
            tree.remove(interval);
        }

        log.info("height={}, size={}, treeSize={}, check={}", tree.height(), tree.size(), tree.treeSize(), tree.check());
        log.info("st={}", tree);
        log.info("------");

        for (int i = 0; i < n; ++i)
        {
            final Interval interval = Interval.of(10 * i, 10 * (i + 1) - 1);
            final String value = "" + i;
            tree.add(interval, value);
            tree.add(interval, value + "x");
        }

        log.info("height={}, size={}, treeSize={}, check={}", tree.height(), tree.size(), tree.treeSize(), tree.check());
        log.info("st={}", tree);
        log.info("------");

        for (int i = 0; i < 100; i++)
        {
            final Interval p = Interval.of(i);
            log.info(p + ":  " + tree.find(p));
            log.info(p + ":  " + tree.get(p, Strategy.FIRST_MATCH));
            log.info(p + ":  " + tree.get(p, Strategy.FIRST_MATCH, v -> v.endsWith("x")));
        }

        log.info("=====");
    }

    @Test
    void test_1_Regression() throws IOException
    {
        log.info("===== test_1_Regression =====");

        final IntervalTreeRnd<Interval> tree = new IntervalTreeRnd<>();

        final List<Interval> intervals = List.of(Interval.of("460110120000000", "460110130999999"),
                                                 Interval.of("460110130000000", "460110139999999"),
                                                 Interval.of("460110131000000", "460110138999999"));

        intervals.forEach(i -> tree.add(i, i));
        log.info("tree={}", tree);

        final Interval needle = Interval.of(460110125417328l);
        final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
        log.info("Result for needle={}: {}", needle, result);

        assertFalse(result.isEmpty(), "Needle " + needle + " not found in haystack " + tree);
    }

    @Test
    void test_2_OverlappingIntervals() throws IOException
    {
        log.info("===== test_2_OverlappingIntervals =====");

        final IntervalTreeRnd<Interval> tree = new IntervalTreeRnd<>();

        Interval i_10_20 = Interval.of("10", "20");
        Interval i_10_30 = Interval.of("10", "30");
        Interval i_11_15 = Interval.of("11", "15");
        final List<Interval> intervals = List.of(i_10_20, i_10_30, i_11_15);

        intervals.forEach(i -> tree.add(i, i));
        log.info("tree={}", tree);

        for (int i = 0; i < 10; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.isEmpty(), "Result is not empty for needle " + needle + ": " + result);
        }

        for (int i = 10; i < 11; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.size() == 1 && result.stream().allMatch(x -> x.equals(i_10_20)),
                       "Needle " + needle + " not found in interval " + i_10_20 + ": " + result);
        }

        for (int i = 11; i < 16; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.size() == 1 && result.stream().allMatch(x -> x.equals(i_11_15)),
                       "Needle " + needle + " not found in interval " + i_11_15 + ": " + result);
        }

        for (int i = 16; i < 21; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.size() == 1 && result.stream().allMatch(x -> x.equals(i_10_20)),
                       "Needle " + needle + " not found in interval " + i_10_20 + ": " + result);
        }

        for (int i = 21; i < 31; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.size() == 1 && result.stream().allMatch(x -> x.equals(i_10_30)),
                       "Needle " + needle + " not found in interval " + i_10_30 + ": " + result);
        }

        for (int i = 31; i < 40; ++i)
        {
            final Interval needle = Interval.of(i);
            final Set<Interval> result = tree.get(needle, Strategy.BEST_MATCH, r -> r.intersects(needle));
            log.info("Result for needle={}: {}", needle, result);
            assertTrue(result.isEmpty(), "Result is not empty for needle " + needle + ": " + result);
        }
    }

    @Test
    void test_3_GetAll() throws IOException
    {
        log.info("===== test_3_GetAll =====");

        final IntervalTreeRnd<AtomicLong> tree = new IntervalTreeRnd<>();

        Interval i_00_09 = Interval.of("0", "9");
        Interval i_10_19 = Interval.of("10", "19");
        Interval i_20_29 = Interval.of("20", "29");
        Interval i_30_39 = Interval.of("30", "39");
        final List<Interval> intervals = List.of(i_00_09, i_10_19, i_20_29, i_30_39);

        intervals.forEach(i -> tree.add(i, new AtomicLong()));
        tree.get(Interval.of(11), Strategy.FIRST_MATCH).forEach(l -> l.incrementAndGet());

        tree.toList().forEach(n -> log.info("key={}, value={}", n.interval, n.data));

        log.info("tree={}", tree.toList());
    }
}
