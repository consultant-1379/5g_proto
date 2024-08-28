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
 * Created on: Jan 22, 2022
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interval search tree implemented using a randomized BST.
 */
@JsonPropertyOrder({ "size", "root" })
public class IntervalTreeRnd<V>
{
    @JsonPropertyOrder({ "min", "max" })
    public static class Interval implements Comparable<Interval>
    {
        public static Interval of(final long point)
        {
            return new Interval(point, point);
        }

        public static Interval of(final long min,
                                  final long max)
        {
            return new Interval(min, max);
        }

        public static Interval of(final String point)
        {
            return Interval.of(Long.parseLong(point));
        }

        public static Interval of(final String min,
                                  final String max)
        {
            return new Interval(Long.parseLong(min), Long.parseLong(max));
        }

        @JsonProperty("min")
        private final long min;

        @JsonProperty("max")
        private final long max;

        private Interval(final long min,
                         final long max)
        {
            if (min <= max)
            {
                this.min = min;
                this.max = max;
            }
            else
            {
                throw new IllegalArgumentException("Illegal interval: " + this);
            }
        }

        @Override
        public int compareTo(final Interval that)
        {
            if (this.min < that.min)
                return -1;

            if (this.min > that.min)
                return +1;

            if (this.max < that.max)
                return -1;

            if (this.max > that.max)
                return +1;

            return 0;
        }

        public boolean contains(final long x)
        {
            return (this.min <= x) && (x <= this.max);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof Interval))
                return false;

            final Interval that = (Interval) o;
            return this.min == that.min && this.max == that.max;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.min, this.max);
        }

        public boolean intersects(final Interval that)
        {
            return !(that.max < this.min || this.max < that.min);
        }

        public boolean isNarrowerThan(final Interval that)
        {
            return this.width() < that.width();
        }

        public long max()
        {
            return this.max;
        }

        public long min()
        {
            return this.min;
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

        public long width()
        {
            return this.max - this.min;
        }
    }

    public enum Strategy
    {
        FIRST_MATCH,
        BEST_MATCH
    }

    @JsonPropertyOrder({ "interval", "size", "max", "data", "left", "right" })
    class Node
    {
        @JsonProperty("interval")
        final Interval interval; // Key

        @JsonIgnore
//        @JsonProperty("data")
        final Set<V> data; // Associated data

        @JsonProperty("size")
        int size; // The size of the subtree rooted at this node

        @JsonProperty("max")
        long max; // Max endpoint in the subtree rooted at this node

        @JsonProperty("left")
        Node left; // Left subtree

        @JsonProperty("right")
        Node right; // Right subtree

        Node(final Interval interval,
             final V value)
        {
            this.interval = interval;
            this.data = new HashSet<>();
            this.data.add(value);
            this.size = 1;
            this.max = interval.max;
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

    private static final ObjectMapper json = Jackson.om();

    @JsonProperty("size")
    private int size;  // The number of values stored in the tree

    @JsonProperty("root")
    private Node root; // Root of the BST

    @JsonIgnore
    private final Lock readLock;

    @JsonIgnore
    private final Lock writeLock;

    public IntervalTreeRnd()
    {
        this.size = 0;
        this.root = null;
        final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
    }

    public void add(final Interval interval,
                    final V value)
    {
        this.writeLock.lock();

        try
        {
            final Node node = this.lookup(this.root, interval);

            if (node != null)
            {
                if (node.data.add(value))
                    this.size++;
            }
            else
            {
                this.root = this.randomizedInsert(this.root, interval, value);
                this.size++;
            }
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    /**
     * Find the data associated to the given interval and return the result.
     * 
     * @param interval The interval for which to return the associated data.
     * @param strategy In case of overlapping intervals: FIRST_MATCH: return the
     *                 data of the first matching interval, BEST_MATCH: return the
     *                 data of the best matching interval.
     * @return The data found.
     */
    public Set<V> get(final Interval interval,
                      final Strategy strategy)
    {
        return this.get(interval, strategy, v -> true);
    }

    /**
     * Find the data associated to the given interval, apply the given filter and
     * return the result.
     * 
     * @param interval The interval for which to return the associated data.
     * @param strategy In case of overlapping intervals: FIRST_MATCH: return the
     *                 data of the first matching interval, BEST_MATCH: return the
     *                 data of the best matching interval.
     * @param filter   The filter applied on the data found.
     * @return The data found, filtered according to the provided filter.
     */
    public Set<V> get(final Interval interval,
                      final Strategy strategy,
                      final Predicate<V> filter)
    {
        this.readLock.lock();

        try
        {
            final Node node = strategy == Strategy.FIRST_MATCH ? this.find(this.root, interval)
                                                               : this.findAll(interval)
                                                                     .stream()
                                                                     .reduce(null,
                                                                             (r,
                                                                              i) -> r == null ? i : i.interval.isNarrowerThan(r.interval) ? i : r);

            return node == null ? Set.of() : node.data.stream().filter(filter::test).collect(Collectors.toSet());
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    /**
     * Remove the value associated with the given interval.
     * 
     * @param interval The interval to search for.
     */
    public void remove(final Interval interval)
    {
        this.remove(interval, null);
    }

    /**
     * Remove the value from the associated data of the given interval. If the
     * associated data is empty, the interval is removed as well.
     * 
     * @param interval The interval to search for.
     * @param value    The value to be removed from the associated data of the given
     *                 interval.
     */
    public void remove(final Interval interval,
                       final V value)
    {
        this.writeLock.lock();

        try
        {
            if (value == null)
            {
                this.root = this.remove(this.root, interval);
            }
            else
            {
                final Node node = this.find(this.root, interval);

                if (node != null)
                {
                    if (node.data.remove(value))
                        this.size--;

                    if (node.data.isEmpty())
                        this.root = this.remove(this.root, interval);
                }
            }
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    /**
     * @return The number of values stored in the nodes of this tree.
     */
    public int size()
    {
        this.readLock.lock();

        try
        {
            return this.size;
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    public List<Node> toList()
    {
        this.readLock.lock();

        try
        {
            final List<Node> result = new LinkedList<>();

            if (this.root != null)
                this.toList(result, this.root);

            return result;
        }
        finally
        {
            this.readLock.unlock();
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

    private boolean checkCount()
    {
        return this.checkCount(this.root);
    }

    private boolean checkCount(final Node x)
    {
        if (x == null)
            return true;

        return this.checkCount(x.left) && this.checkCount(x.right) && (x.size == 1 + size(x.left) + size(x.right));
    }

    private boolean checkMax()
    {
        return this.checkMax(root);
    }

    private boolean checkMax(Node x)
    {
        if (x == null)
            return true;

        return x.max == this.max3(x.interval.max, this.max(x.left), this.max(x.right));
    }

    /**
     * Find all nodes intersecting the given interval.
     * 
     * @param result   The nodes found.
     * @param x        The root for the search.
     * @param interval The interval to search for.
     * @return Indication whether a matching node was found in the subtree.
     */
    private boolean find(final LinkedList<Node> result,
                         final Node x,
                         final Interval interval)
    {
        if (x == null)
            return false;

        boolean foundHere = false;
        boolean foundLeft = false;
        boolean foundRight = false;

        if (interval.intersects(x.interval))
        {
            result.add(x);
            foundHere = true;
        }

        if (x.left != null && x.left.max >= interval.min)
            foundLeft = this.find(result, x.left, interval);

        if (foundLeft || x.left == null || x.left.max < interval.min)
            foundRight = this.find(result, x.right, interval);

        return foundHere || foundLeft || foundRight;
    }

    /**
     * Find and return a node intersecting the given interval in the subtree rooted
     * at x.
     * 
     * @param x        The root for the search.
     * @param interval The interval to search an intersecting node for.
     * @return The node found or null if no such node exists.
     */
    private Node find(Node x,
                      final Interval interval)
    {
        while (x != null)
        {
            if (interval.intersects(x.interval))
                return x;

            if (x.left == null || x.left.max < interval.min)
                x = x.right;
            else
                x = x.left;
        }

        return null;
    }

    /**
     * Adjust auxiliary information (subtree count and max fields).
     */
    private void fix(final Node x)
    {
        if (x == null)
            return;

        x.size = 1 + this.size(x.left) + this.size(x.right);
        x.max = this.max3(x.interval.max, max(x.left), max(x.right));
    }

    private int height(final Node x)
    {
        if (x == null)
            return 0;

        return 1 + Math.max(this.height(x.left), this.height(x.right));
    }

    private Node join(final Node a,
                      final Node b)
    {
        if (a == null)
            return b;

        if (b == null)
            return a;

        if (Math.random() * (this.size(a) + this.size(b)) < this.size(a))
        {
            a.right = this.join(a.right, b);
            this.fix(a);
            return a;
        }

        b.left = this.join(a, b.left);
        this.fix(b);
        return b;
    }

    /**
     * Lookup and return the node matching the given interval.
     * 
     * @param x        The root of the search.
     * @param interval The interval for which to find the matching node.
     * @return The matching node or null if no such node exists.
     */
    private Node lookup(Node x,
                        Interval interval)
    {
        if (x == null)
            return null;

        int cmp = interval.compareTo(x.interval);

        if (cmp < 0)
            return this.lookup(x.left, interval);

        if (cmp > 0)
            return this.lookup(x.right, interval);

        return x;
    }

    private long max(final Node x)
    {
        if (x == null)
            return Long.MIN_VALUE;

        return x.max;
    }

    private long max3(final long a,
                      final long b,
                      final long c)
    {
        return Math.max(a, Math.max(b, c));
    }

    private Node randomizedInsert(final Node x,
                                  final Interval interval,
                                  final V value)
    {
        if (x == null)
            return new Node(interval, value);

        if (Math.random() * this.size(x) < 1.0)
            return this.rootInsert(x, interval, value);

        int cmp = interval.compareTo(x.interval);

        if (cmp < 0)
            x.left = this.randomizedInsert(x.left, interval, value);
        else
            x.right = this.randomizedInsert(x.right, interval, value);

        this.fix(x);

        return x;
    }

    private Node remove(Node h,
                        final Interval interval)
    {
        if (h == null)
            return null;

        int cmp = interval.compareTo(h.interval);

        if (cmp < 0)
        {
            h.left = this.remove(h.left, interval);
        }
        else if (cmp > 0)
        {
            h.right = this.remove(h.right, interval);
        }
        else
        {
            this.size -= h.data.size();
            h = this.join(h.left, h.right);
        }

        this.fix(h);

        return h;
    }

    private Node rootInsert(Node x,
                            final Interval interval,
                            final V value)
    {
        if (x == null)
            return new Node(interval, value);

        int cmp = interval.compareTo(x.interval);

        if (cmp < 0)
        {
            x.left = this.rootInsert(x.left, interval, value);
            x = this.rotateRight(x);
        }
        else
        {
            x.right = this.rootInsert(x.right, interval, value);
            x = this.rotateLeft(x);
        }

        return x;
    }

    private Node rotateLeft(final Node h)
    {
        final Node x = h.right;

        h.right = x.left;
        x.left = h;

        this.fix(h);
        this.fix(x);

        return x;
    }

    private Node rotateRight(final Node h)
    {
        final Node x = h.left;

        h.left = x.right;
        x.right = h;

        this.fix(h);
        this.fix(x);

        return x;
    }

    /**
     * @return The number of nodes in subtree rooted at x.
     */
    private int size(final Node x)
    {
        if (x == null)
            return 0;

        return x.size;
    }

    private void toList(final List<Node> result,
                        final Node x)
    {
        if (x.left != null)
        {
            this.toList(result, x.left);

            result.add(x);

            if (x.right != null)
                this.toList(result, x.right);
        }
        else
        {
            result.add(x);

            if (x.right != null)
                this.toList(result, x.right);
        }
    }

    /**
     * Check integrity of subtree count fields.
     */
    boolean check()
    {
        return this.checkCount() && this.checkMax();
    }

    /**
     * Find and return a node intersecting the given interval.
     * 
     * @param interval The interval to search an intersecting node for.
     * @return The node found or null if no such node exists.
     */
    Node find(final Interval interval)
    {
        this.readLock.lock();

        try
        {
            return this.find(this.root, interval);
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    /**
     * Find all nodes intersecting the given interval.
     * 
     * @param interval The interval to search intersecting nodes for.
     * @return The nodes found.
     */
    LinkedList<Node> findAll(final Interval interval)
    {
        this.readLock.lock();

        try
        {
            final LinkedList<Node> result = new LinkedList<>();
            this.find(result, this.root, interval);
            return result;
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    /**
     * @return The height of tree (empty tree height = 0).
     */
    int height()
    {
        this.readLock.lock();

        try
        {
            return this.height(this.root);
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    /**
     * @return The number of nodes in this tree.
     */
    int treeSize()
    {
        this.readLock.lock();

        try
        {
            return this.size(this.root);
        }
        finally
        {
            this.readLock.unlock();
        }
    }
}
