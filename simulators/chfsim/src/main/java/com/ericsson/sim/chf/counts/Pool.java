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
 * Created on: Jul 2, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.chf.counts;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Represents a map of entries of type T.
 * 
 * @param <T> The type of the entries in the Pool.
 */
public class Pool<T>
{
    private final ConcurrentHashMap<String, T> pool = new ConcurrentHashMap<>();
    private final Factory<T> factory;

    /**
     * Constructor taking an entry-factory as argument.
     * 
     * @param factory The entry-factory.
     */
    public Pool(Factory<T> factory)
    {
        this.factory = factory;
    }

    /**
     * Returns the default entry (mapped to key "NULL"). Same as calling get(null).
     * 
     * @return The default entry (mapped to key "NULL").
     */
    public T get()
    {
        return this.get(null);
    }

    /**
     * Returns an entry for the key passed. If the key does not yet exist, a new
     * entry is inserted in the Pool and returned.
     * 
     * @param key The key of the entry to be returned. If it is null, it is mapped
     *            to key "NULL".
     * @return An entry for the key passed.
     */
    public T get(String key)
    {
        if (key == null)
            key = "NULL";

        if (this.pool.containsKey(key))
            return this.pool.get(key);

        T value = this.factory.create(key);
        T prev = this.pool.putIfAbsent(key, value);
        return prev != null ? prev : value;
    }

    /**
     * Returns an Iterator over all mappings in the Pool.
     * 
     * @return An Iterator over all mappings in the Pool.
     */
    public Iterator<Entry<String, T>> iterator()
    {
        return this.pool.entrySet().iterator();
    }

    /**
     * Returns a stream of all mappings in the Pool.
     * 
     * @return A stream of all mappings in the Pool.
     */
    public Stream<Entry<String, T>> stream()
    {
        return this.pool.entrySet().stream();
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
        StringBuilder b = new StringBuilder();

        b.append("[");

        Iterator<Entry<String, T>> it = this.iterator();
        Entry<String, T> entry = null;

        while (it.hasNext())
        {
            if (entry != null)
                b.append(",");

            entry = it.next();
            b.append("key=").append(entry.getKey().toString()).append(",value=").append(entry.getValue().toString());
        }

        b.append("]");

        return b.toString();
    }
}
