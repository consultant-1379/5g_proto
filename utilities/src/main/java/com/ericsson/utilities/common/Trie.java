/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 15, 2020
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "size", "data" })
public class Trie<T>
{
    @JsonPropertyOrder({ "start", "end", "pattern" })
    public static class Range
    {
        public static boolean includes(final String pattern,
                                       final String number)
        {
            return includes(null, null, pattern, number, true);
        }

        public static boolean includes(final String start,
                                       final String end,
                                       final String number)
        {
            return includes(start, end, null, number, true);
        }

        public static boolean includes(final String start,
                                       final String end,
                                       final String number,
                                       final boolean exactMatch)
        {
            return includes(start, end, null, number, exactMatch);
        }

        public static boolean includes(final String start,
                                       final String end,
                                       final String pattern,
                                       final String number,
                                       final boolean exactMatch)
        {
            if (pattern != null)
                return number.matches(pattern);

            if (start != null && end != null)
            {
                final long n;
                final long s;
                final long e;

                if (exactMatch)
                {
                    n = Math.abs(Long.valueOf(number));
                    s = Math.abs(Long.valueOf(start));
                    e = Math.abs(Long.valueOf(end));
                }
                else
                {
                    // For the comparison, the number and the borders of the range must have the
                    // same length. Hence, normalize all of {needle, start, end} to the max length
                    // of them.
                    // number and start are padded with "0", end with "9".
                    // Example:
                    // n="123", s="1234", e="1235" --> n="1230", s="1234", e="1235"
                    // n="1234", s="123", e="12" --> n="1234", s="1230", e="1299"

                    final int maxLen = Math.max(number.length(), Math.max(start.length(), end.length()));

                    n = normalize(number, maxLen - number.length(), false);
                    s = normalize(start, maxLen - start.length(), false);
                    e = normalize(end, maxLen - end.length(), true);
                }

                return !(s > n || e < n);
            }

            return false;
        }

        public static Range of(final String pattern)
        {
            return new Range(null, null, pattern);
        }

        public static Range of(final String start,
                               final String end)
        {
            return new Range(start, end, null);
        }

        public static Range of(final String start,
                               final String end,
                               final String pattern)
        {
            return new Range(start, end, pattern);
        }

        public static String toTrieKey(final String start,
                                       final String end,
                                       final String pattern)
        {
            return toTrieKey(start, end, pattern, true);
        }

        private static long normalize(final String number,
                                      final int exponent,
                                      boolean numberIsEndOfRange)
        {
            final long factor = Math.round(Math.pow(10, exponent));
            return Math.abs(Long.valueOf(number)) * factor + (numberIsEndOfRange ? factor - 1 : 0);
        }

        /**
         * Split-up a range such that the resulting Trie-key has all numbers first,
         * optionally followed by wildcards only.
         * <p>
         * Example:
         * 
         * <pre>
         * s=460115917050000
         * e=460115923049999
         * k=4601159**0*****
         * </pre>
         * 
         * is converted to
         * 
         * <pre>
         * s1=460115917050000
         * e1=460115922999999
         * k1=4601159********
         * 
         * s2=460115923000000
         * e2=460115923049999
         * k2=4601159230*****
         * </pre>
         * 
         * Splitting-up ranges with Trie-keys not having all numbers first is an
         * optional step that may help to speed-up the retrieval of data from a Trie,
         * because the date are distributed over more ranges.
         * </p>
         * 
         * @param range The range to be split.
         * @param level The level of recursion.
         * @return
         */
        private static List<Range> split(final Range range,
                                         final int level)
        {
            // Ranges with pattern may not be split:

            if (range.pattern != null)
                return List.of(range);

            final List<Range> ranges = new ArrayList<>();
            final String start = range.start;
            final String end = range.end;
            final String trieKey = range.toTrieKey(false /* no sticky wildcard */);
            final StringBuilder b = new StringBuilder();
            int state = 0; // 0: before first '*', 1: '*' found, 2: first [0-9] found after '*'

            for (int i = 0; trieKey != null && i < trieKey.length(); ++i)
            {
                char tc = trieKey.charAt(i);
                char ec = end.charAt(i);

                switch (state)
                {
                    case 2:
                        break;

                    case 1:
                        if (tc != '*')
                            state = 2;
                        break;

                    case 0:
                    default:
                        if (tc == '*')
                            state = 1;
                        break;
                }

                b.append(state == 2 ? '0' : ec);
            }

//            if (level == 0)
//            {
//                log.info(" original={}", range);
//            }

//            log.info("s{}={}", level, start);
//            log.info("e{}={}", level, end);
//            log.info("k{}={}", level, trieKey);

            if (state == 2)
            {
                if (level > 20)
                {
                    log.debug("Skipped splitting range, too many subranges detected: {}. Last range subject to split: {}", level, range);
                    ranges.add(range);
                }
                else
                {
                    long num = Long.parseLong(b.toString());

                    ranges.addAll(split(Range.of(start, Long.toString(num - 1)), level + 1));
                    ranges.addAll(split(Range.of(Long.toString(num), end), level + 1));
                }
            }
            else
            {
                ranges.add(range);
            }

//            if (level == 0)
//            {
//                log.info("converted={}", ranges);
//            }

            return ranges;
        }

        private static String toTrieKey(final String start,
                                        final String end,
                                        final String pattern,
                                        final boolean stickyWildcard)
        {
            final StringBuilder b = new StringBuilder();

            if (pattern != null)
            {
                // Transform the pattern to a Trie key:
                // "^imsi-012345[6,7]0{2}[0-9]{3}$" --> "imsi-012345"
                // This key is to be used in a Trie with strategy BEST_MATCH.

                final String[] tokens = pattern.split("-");

                if (tokens.length > 1)
                {
                    for (int i = 0; i < tokens[0].length(); i++)
                    {
                        final char c = pattern.charAt(i);

                        if (Character.isLowerCase(c))
                            b.append(c);
                    }

                    b.append('-');

                    for (int i = 0; i < tokens[1].length(); i++)
                    {
                        final char c = tokens[1].charAt(i);

                        if (!Character.isDigit(c))
                            break;

                        b.append(c);
                    }
                }
            }
            else if (start != null && end != null)
            {
                if (start.length() != end.length())
                {
                    log.error("Range must have borders of equal length: {}", Range.of(start, end, pattern));
                    return null;
                }

                boolean useWildcard = false;

                for (int i = 0; i < start.length() && i < end.length(); i++)
                {
                    final char cb = start.charAt(i);
                    final char ce = end.charAt(i);

                    useWildcard = stickyWildcard ? (useWildcard || cb != ce) : (cb != ce);
                    b.append(useWildcard ? '*' : cb);
                }
            }
            else
            {
                log.error("Either 'pattern' or both 'start' and 'stop' must be provided.");
                return null;
            }

            return b.toString();
        }

        @JsonProperty("start")
        public final String start;

        @JsonProperty("end")
        public final String end;

        @JsonProperty("pattern")
        public final String pattern;

        private Range(final String start,
                      final String end,
                      final String pattern)
        {
            this.start = start;
            this.end = end;
            this.pattern = pattern;
        }

        public boolean includes(final String number)
        {
            return includes(this.start, this.end, this.pattern, number, true);
        }

        public boolean includes(final String number,
                                final boolean exactMatch)
        {
            return includes(this.start, this.end, this.pattern, number, exactMatch);
        }

        public List<Range> split(int level)
        {
            return split(this, level);
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

        public String toTrieKey()
        {
            return toTrieKey(this.start, this.end, this.pattern, true);
        }

        private String toTrieKey(final boolean stickyWildcard)
        {
            return toTrieKey(this.start, this.end, this.pattern, stickyWildcard);
        }
    }

    public enum Strategy
    {
        FULL_MATCH,
        BEST_MATCH;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "d", "c" })
    private class Node<U>
    {
        @JsonProperty("d")
        private final Set<U> data = new HashSet<>();

        @JsonIgnore
        private final Node<U> parent;

        @JsonProperty("c")
        private final Map<Character, Node<U>> children = new HashMap<>();

        public Node(final Node<U> parent)
        {
            this.parent = parent;
        }

        public boolean add(final U datum)
        {
            return this.getData().add(datum);
        }

        public Map<Character, Node<U>> getChildren()
        {
            return this.children;
        }

        public Set<U> getData()
        {
            return this.data;
        }

        public Node<U> getParent()
        {
            return this.parent;
        }

        @JsonIgnore
        public boolean isEmpty()
        {
            return this.getData().isEmpty() && this.getChildren().isEmpty();
        }

        public boolean remove(final U datum)
        {
            return this.getData().remove(datum);
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

//    @SuppressWarnings("unused")
//    private static class Test
//    {
//        private static class Item extends Pair<Integer, Integer> implements Predicate<String>
//        {
//            public static Item of(final Integer first,
//                                  final Integer second)
//            {
//                return new Item(first, second);
//            }
//
//            private Item(final Integer first,
//                         final Integer second)
//            {
//                super(first, second);
//            }
//
//            @Override
//            public boolean test(final String key)
//            {
//                final int i = Integer.parseInt(key);
//                return this.getFirst() <= i && i <= this.getSecond();
//            }
//        }
//
//        public static void main(String[] args)
//        {
//            final Pattern p = Pattern.compile("(\\(?(\\[([^-]-[^]])+\\]|\\\\[bdDsSwW])?(\\{([0-9])+\\})?\\)?)");
//            final String input = "imsi-02235([0-9a-zA-Z]{7,})?666\\s{1}x\\S?y{5}";
//            final Matcher m = p.matcher(input);
//            final StringBuilder b = new StringBuilder();
//
//            int beginIndex = 0;
//            int endIndex = 0;
//
//            while (m.find())
//            {
//                if (m.group() == null)
//                    continue;
//
//                if (m.end() + 1 < input.length() && input.charAt(m.end()) == '?')
//                    break;
//
//                b.append(input, beginIndex, m.start());
//
//                final String charset = m.group(2) != null ? m.group(2) : m.group(3);
//                final String quantifier = m.group(5);
//
//                if (quantifier == null)
//                {
//                    if (charset != null)
//                        b.append('.');
//                }
//                else
//                {
//                    if (charset != null)
//                    {
//                        for (int i = 0; i < Integer.parseInt(quantifier); ++i)
//                            b.append('.');
//                    }
//                    else
//                    {
//                        for (int i = 1; i < Integer.parseInt(quantifier); ++i)
//                            b.append(input.charAt(m.start(5) - 2));
//                    }
//                }
//
//                beginIndex = m.end(0);
//
//                for (int i = 0; i <= m.groupCount(); ++i)
//                {
//                    if (m.group(i) == null)
//                        continue;
//
//                    log.info("group({})={}, start={}, end={}", i, m.group(i), m.start(i), m.end(i));
//                }
//            }
//
//            log.info("key={}", b.toString());
//
////            if (true)
////                return;
//
//            Trie<Item> trie = new Trie<Item>('*');
//
//            trie.add("10*", Item.of(100, 103));
//            trie.add("10*", Item.of(104, 106));
//            trie.add("11*", Item.of(110, 113));
//            trie.add("1**", Item.of(140, 152));
//            trie.add("15*", Item.of(153, 155));
//            log.info("trie={}", trie);
//            log.info("result({})={}", 101, trie.get("101", i -> i.test("101")));
//            log.info("result({})={}", 105, trie.get("105", i -> i.test("105")));
//            log.info("result({})={}", 111, trie.get("111", i -> i.test("111")));
//            log.info("result({})={}", 142, trie.get("142", i -> i.test("142")));
//            log.info("result({})={}", 154, trie.get("154", i -> i.test("154")));
//
//            trie.add("1**", Item.of(120, 130));
//            log.info("trie={}", trie);
//            log.info("result({})={}", 101, trie.get("101", i -> i.test("101")));
//            log.info("result({})={}", 105, trie.get("105", i -> i.test("105")));
//            log.info("result({})={}", 111, trie.get("111", i -> i.test("111")));
//            log.info("result({})={}", 142, trie.get("142", i -> i.test("142")));
//            log.info("result({})={}", 154, trie.get("154", i -> i.test("154")));
//
//            trie.remove("1**", Item.of(120, 130));
//            trie.add("1**", Item.of(120, 139));
//            trie.add("1***", Item.of(1403, 1514));
//            trie.add("151*", Item.of(1515, 1519));
//            log.info("trie={}", trie);
//            log.info("result({})={}", 101, trie.get("101", i -> i.test("101")));
//            log.info("result({})={}", 105, trie.get("105", i -> i.test("105")));
//            log.info("result({})={}", 111, trie.get("111", i -> i.test("111")));
//            log.info("result({})={}", 142, trie.get("142", i -> i.test("142")));
//            log.info("result({})={}", 154, trie.get("154", i -> i.test("154")));
//            log.info("result({})={}", 1514, trie.get("1514", i -> i.test("1514")));
//            log.info("result({})={}", 1515, trie.get("1515", i -> i.test("1515")));
//        }
//    }

    private static final Logger log = LoggerFactory.getLogger(Trie.class);
    private static final ObjectMapper json = Jackson.om();

    private final Character wildcard;
    private final Node<T> root;
    private final Lock readLock;
    private final Lock writeLock;

    private long size;

    public Trie()
    {
        this(null);
    }

    public Trie(final Character wildcard)
    {
        this.size = 0l;
        this.wildcard = wildcard;
        this.root = new Node<>(null);

        final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
    }

    public void add(final Function<T, String> datumToKey,
                    final T datum)
    {
        this.add(datumToKey.apply(datum), datum);
    }

    public void add(final Object key,
                    final T datum)
    {
        this.writeLock.lock();

        try
        {
            if (this.insert(key.toString()).add(datum))
            {
//                log.info("numData={}", this.countData(this.root));
                this.size++;
            }
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

//    private int countData(final Node<T> root)
//    {
//        return root.getData().size() + root.getChildren().values().stream().collect(Collectors.summingInt(child-> this.countData(child)));
//    }

    public void clear()
    {
        this.writeLock.lock();

        try
        {
            this.getRoot().data.clear();
            this.getRoot().children.clear();
            this.size = 0;
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    public Set<T> get(final String key)
    {
        return this.get(key, Strategy.FULL_MATCH, null);
    }

    public Set<T> get(final String key,
                      final Predicate<T> filter)
    {
        return this.get(key, Strategy.FULL_MATCH, filter);
    }

    public Set<T> get(final String key,
                      final Strategy strategy)
    {
        return this.get(key, strategy, null);
    }

    public Set<T> get(final String key,
                      final Strategy strategy,
                      final Predicate<T> filter)
    {
        this.readLock.lock();

        try
        {
            final Trie<T>.Node<T> node = this.find(this.getRoot(), key, strategy, Optional.ofNullable(filter).orElse(v -> true), 0);

            return node == null ? Set.of() : node.getData().stream().collect(Collectors.toSet());
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    @JsonProperty("size")
    public long getSize()
    {
        return this.size;
    }

    @JsonIgnore
    public boolean isEmpty()
    {
        this.readLock.lock();

        try
        {
            return this.getRoot().data.isEmpty() && this.getRoot().children.isEmpty();
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    public void remove(final Function<T, String> datumToKey,
                       final T datum)
    {
        this.remove(datumToKey.apply(datum), datum);
    }

    public void remove(final Object key,
                       final T datum)
    {
        this.writeLock.lock();

        try
        {
            final String keyStr = key.toString();

            Node<T> node = this.getRoot();

            for (int i = 0; i < keyStr.length() && node != null; i++)
                node = node.getChildren().get(keyStr.charAt(i));

            if (node != null)
            {
                if (node.remove(datum))
                    this.size--;

                for (int i = keyStr.length() - 1; i >= 0 && node.isEmpty(); i--)
                {
                    node = node.getParent();
                    node.getChildren().remove(keyStr.charAt(i));
                }
//                
//                log.info("numData={}", this.countData(this.root));
            }
        }
        finally
        {
            this.writeLock.unlock();
        }
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

    /**
     * @param root   The Node where to start the search.
     * @param filter The filter applied on the data attached to a Node.
     * @param index  The index of the current character in key.
     * @return
     */
    private Node<T> find(final Node<T> root,
                         final Predicate<T> filter,
                         final int index)
    {
//        log.info("index={}, root={}", index, root);
        final Set<T> filteredData = root.getData().stream().filter(filter::test).collect(Collectors.toSet());

        if (filteredData.isEmpty())
            return null;

        final Node<T> result = new Node<>(null);
        result.getData().addAll(filteredData);

        return result;
    }

    /**
     * Finds and returns the Node for the key passed and matching the filter
     * criteria. Backtracking is used if no matching node could be found on the
     * current level.
     * 
     * @param root     The Node where to start the search.
     * @param key      The key used for the retrieval of a Node.
     * @param strategy The strategy applied on the search.
     * @param filter   The filter applied on the data attached to a Node.
     * @param index    The index of the current character in key.
     * @return The Node found or null.
     */
    private Node<T> find(final Node<T> root,
                         final String key,
                         final Strategy strategy,
                         final Predicate<T> filter,
                         final int index)
    {
        if (root == null)
            return null;

        if (index == key.length() || strategy == Strategy.BEST_MATCH && root.getChildren().isEmpty())
            return this.find(root, filter, index);

//        log.info("c={}, root={}", key.charAt(index), root);

        Node<T> child = root.getChildren().get(key.charAt(index));

        boolean wildcardSearch = false;

        if (child == null && this.wildcard != null)
        {
            child = root.getChildren().get(this.wildcard);
            wildcardSearch = true;
        }

        Node<T> found = this.find(child, key, strategy, filter, index + 1);

        if (found == null && this.wildcard != null && !wildcardSearch)
            found = this.find(root.getChildren().get(this.wildcard), key, strategy, filter, index + 1);

        if (found == null && strategy == Strategy.BEST_MATCH)
            found = this.find(root, filter, index);

        return found;
    }

    @JsonProperty("data")
    private Node<T> getData()
    {
        return this.size > 100 ? null : this.root;
    }

    private Node<T> getRoot()
    {
        return this.root;
    }

    private Node<T> insert(final String key)
    {
        Node<T> current = this.getRoot();

        for (int i = 0; i < key.length(); i++)
        {
            final Node<T> x = current;
            current = current.getChildren().computeIfAbsent(key.charAt(i), c -> new Node<T>(x));
        }

        return current;
    }
}