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
 * Created on: Jun 17, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Count
{
    public static class Pool
    {
        @JsonProperty("pool")
        private final Map<Integer, Count> pool = new ConcurrentHashMap<>();

        /**
         * Clears all the Counts.
         */
        @JsonIgnore
        public void clear()
        {
            this.pool.values().stream().forEach(Count::clear);
        }

        /**
         * Returns a Count for the key passed. If the key does not yet exist, a new
         * Count is inserted in the Pool and returned.
         * 
         * @param key The key of the Count to be returned.
         * @return A Count for the key passed.
         */
        @JsonIgnore
        public Count get(final Integer key)
        {
            if (this.pool.containsKey(key))
                return this.pool.get(key);

            Count value = new Count();
            Count prev = this.pool.putIfAbsent(key, value);
            return prev != null ? prev : value;
        }

        /**
         * Returns a new Count that holds the sum of all existing Counts matching the
         * range of keys passed.
         * 
         * @param keyMin Lower boundary of the range of keys.
         * @param keyMax Upper boundary of the range of keys.
         * @return A new Count that holds the sum of all existing Counts matching the
         *         range of keys passed.
         */
        @JsonIgnore
        public Count get(final Integer keyMin,
                         final Integer keyMax)
        {
            final Count result = new Count();
            this.pool.entrySet().stream().filter(e -> !(e.getKey() < keyMin || e.getKey() > keyMax)).forEach(e -> result.inc(e.getValue().get()));
            return result;
        }

        /**
         * Returns an Iterator over all Entries in the Pool.
         * 
         * @return An Iterator over all Entries in the Pool.
         */
        @JsonIgnore
        public Iterator<Entry<Integer, Count>> iterator()
        {
            return this.pool.entrySet().iterator();
        }

        /**
         * Returns a stream of all Entries in the Pool.
         * 
         * @return A stream of all Entries in the Pool.
         */
        @JsonIgnore
        public Stream<Entry<Integer, Count>> stream()
        {
            return this.pool.entrySet().stream();
        }

        /**
         * Returns a JSON representation of this object.
         */
        @Override
        public String toString()
        {
            try
            {
                return mapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    private static final ObjectMapper mapper = Jackson.om();

    /**
     * Disable pretty-printing of JSON output.
     */
    public static void prettyPrintingDisable()
    {
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Enable pretty-printing of JSON output.
     */
    public static void prettyPrintingEnable()
    {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @JsonProperty("count")
    private long cnt = 0L;

    @JsonIgnore
    public synchronized void clear()
    {
        this.cnt = 0L;
    }

    @JsonProperty("count")
    public synchronized long get()
    {
        return this.get(false);
    }

    @JsonIgnore
    public synchronized long get(boolean readThenClear)
    {
        final long cnt = this.cnt;

        if (readThenClear)
            this.clear();

        return cnt;
    }

    @JsonIgnore
    public synchronized void inc()
    {
        this.cnt++;
    }

    @JsonIgnore
    public synchronized void inc(long c)
    {
        this.cnt += c;
    }

    /**
     * Returns a JSON representation of this object.
     * 
     * @see Count#prettyPrintingDisable
     * @see Count#prettyPrintingEnable
     * @return A JSON representation of this object.
     */
    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }
}
