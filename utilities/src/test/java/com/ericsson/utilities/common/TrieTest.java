package com.ericsson.utilities.common;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.Trie.Range;
import com.ericsson.utilities.common.Trie.Strategy;

public class TrieTest
{
    private static final Logger log = LoggerFactory.getLogger(TrieTest.class);

    @Test
    void test_0_Pattern() throws IOException
    {
        log.info("===== test_0_Pattern =====");

        final List<String> positives = List.of("imsi-122",
                                               "imsi-1220",
                                               "imsi-1221",
                                               "imsi-123",
                                               "imsi-1230",
                                               "imsi-1239",
                                               "imsi-124",
                                               "imsi-1240",
                                               "imsi-1241",
                                               "imsi-1243",
                                               "imsi-1249",
                                               "imsi-125",
                                               "imsi-1250",
                                               "imsi-1259",
                                               "imsi-126",
                                               "imsi-1260");
        final Trie<Range> t = new Trie<>();

        final List<Range> ranges = List.of(Range.of("imsi-1[0-9]+"),
                                           Range.of("imsi-12[12][01]?"),
                                           Range.of("imsi-123"),
                                           Range.of("imsi-123[0-9]"),
                                           Range.of("imsi-124[0-9]"),
                                           Range.of("imsi-125[0-9]+"),
                                           Range.of("imsi-126[0-9]?"));

        ranges.forEach(r ->
        {
            final String trieKey = r.toTrieKey();
            log.info("trieKey={}", trieKey);
            t.add(trieKey, r);
        });

        log.info("trie={}", t);

        positives.forEach(n ->
        {
            final Set<Range> result = t.get(n, Strategy.BEST_MATCH, r -> r.includes(n));
            log.info("BEST: {} --> {}", String.format("%-4s", n), result);

            Assertions.assertFalse(result.isEmpty(), "No element found for needle=" + n + ", haystack=" + t);
            result.forEach(r -> Assertions.assertTrue(n.matches(r.pattern), "Wrong element found for needle=" + n + ", pattern=" + r.pattern));
        });
    }

    @Test
    void test_2_Range() throws IOException
    {
        log.info("===== test_2_Range =====");

        final List<String> positives = List.of("123", "124", "1241", "1243", "125", "126");
        final List<String> negatives = List.of("122", "1220", "1230", "1239", "1240", "1249", "1250", "1259", "1260");

        final Trie<Range> t = new Trie<>('*');

        final List<Range> ranges = List.of(Range.of("123", "123"), Range.of("1241", "1245"), Range.of("124", "125"), Range.of("126", "127"));

        ranges.forEach(r ->
        {
            final String trieKey = r.toTrieKey();
            log.info("trieKey={}", trieKey);
            t.add(trieKey, r);
        });

        log.info("trie={}", t);

        positives.forEach(n ->
        {
            final Set<Range> result = t.get(n, Strategy.FULL_MATCH, r -> r.includes(n));
            log.info("FULL: {} --> {}", String.format("%-4s", n), result);

            Assertions.assertFalse(result.isEmpty(), "No element found for needle=" + n + ", haystack=" + t);
            result.forEach(r -> Assertions.assertTrue(r.includes(n), "Wrong element found for needle=" + n + ", range=" + r));
        });

        negatives.forEach(n ->
        {
            final Set<Range> result = t.get(n, Strategy.FULL_MATCH, r -> r.includes(n));
            log.info("FULL: {} --> {}", String.format("%-4s", n), result);

            Assertions.assertTrue(result.isEmpty(), "Element(s) found for needle=" + n + ", haystack=" + t);
        });
    }
}
