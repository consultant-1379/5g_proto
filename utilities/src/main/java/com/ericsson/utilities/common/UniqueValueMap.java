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
 * Created on: Feb 15, 2022
 *     Author: eaoknkr
 */

package com.ericsson.utilities.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @param <U>
 * 
 */
public final class UniqueValueMap<K extends Comparable<K>, V extends Set<U>, U> extends TreeMap<K, V>
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private boolean keepLarger = false;
    private boolean keepSmaller = true;
    private Supplier<V> supplier;

    /**
     * @param uniqueValueMapBuilder
     */
    private UniqueValueMap(UniqueValueMapBuilder<K, V, U> uniqueValueMapBuilder)
    {
        this.keepLarger = uniqueValueMapBuilder.keepLarger;
        this.keepSmaller = uniqueValueMapBuilder.keepSmaller;
        this.supplier = uniqueValueMapBuilder.supplier;
    }

    /**
     * @return
     */
    public boolean isKeepSmaller()
    {
        return this.keepSmaller;
    }

    /**
     * @return
     */
    public boolean isKeepLarger()
    {
        return this.keepLarger;
    }

    /*
     * If the new key, value entry is not present in the structure, then the
     * structure is updated. If the new value, is already present in the whole
     * structure but with a different key, then the old key-value is removed and the
     * new key-value is added, based on the preset flag of keepSmaller or
     * keepLarger.
     * 
     * If keepSmaller is set and the old key is larger than the new key, the old key
     * and value are removed and the new key with the new value are added. If
     * keepLarger is set and the old key is smaller than the new key, the old key
     * and value are removed and the new key with the new value are added.
     * 
     */
    public U put(K key,
                 U value)
    {
        return this.keepSmaller ? putKeepSmaller(key, value) : putKeepLarger(key, value);
    }

    private U putKeepLarger(K newKey,
                            U newValue)
    {
        var valueFound = new AtomicBoolean(false);

        // search if the newValue is already contained in a value set and the newKey is
        // larger than the old key
        var oldSmallEntry = super.entrySet().stream().filter(entry ->
        {
            var newKeyLarger = newKey.compareTo(entry.getKey()) > 0;
            var valueIsContained = entry.getValue().contains(newValue);

            if (valueIsContained)
            {
                valueFound.set(true);
            }

            return newKeyLarger && valueIsContained;
        }).findFirst();

        if (oldSmallEntry.isPresent())
        {
            // the newValue is already present in the structure, with a newkey that is
            // smaller than the old one. So, remove the old entry and keep the new one
            var oldEntry = oldSmallEntry.get();
            super.get(oldEntry.getKey()).remove(newValue);

            if (super.get(oldEntry.getKey()).isEmpty())
            {
                // set is empty so remove key
                super.remove(oldEntry.getKey());
            }

            super.computeIfAbsent(newKey, k -> this.supplier.get()).add(newValue);
        }
        else
        {
            if (!valueFound.get())
            {
                // the new value is not present in the structure at all. So update the structure
                super.computeIfAbsent(newKey, k -> this.supplier.get()).add(newValue);
            }
            // else: the new value is already present in the structure but the newKey is
            // smaller or equal to the old key. Then do nothing
        }

        return newValue;
    }

    private U putKeepSmaller(K newKey,
                             U newValue)
    {
        var valueFound = new AtomicBoolean(false);

        // search if the newValue is already contained in a value set and the newKey is
        // smaller than the old key
        var oldLargeEntry = super.entrySet().stream().filter(entry ->
        {
            var newKeySmaller = newKey.compareTo(entry.getKey()) < 0;
            var valueIsContained = entry.getValue().contains(newValue);

            if (valueIsContained)
            {
                valueFound.set(true);
            }

            return newKeySmaller && valueIsContained;
        }).findFirst();

        if (oldLargeEntry.isPresent())
        {
            // the newValue is already present in the structure, with a newkey that is
            // larger than the old one. So, remove the old entry and keep the new one
            var oldEntry = oldLargeEntry.get();
            super.get(oldEntry.getKey()).remove(newValue);

            if (super.get(oldEntry.getKey()).isEmpty())
            {
                // set is empty so remove key
                super.remove(oldEntry.getKey());
            }

            super.computeIfAbsent(newKey, k -> this.supplier.get()).add(newValue);
        }
        else
        {
            if (!valueFound.get())
            {
                // the new value is not present in the structure at all. So update the structure
                super.computeIfAbsent(newKey, k -> this.supplier.get()).add(newValue);
            }
            // else: the new value is already present in the structure but the newKey is
            // larger or equal to the old key. Then do nothing
        }

        return newValue;
    }

    public void putAllItems(Map<? extends K, ? extends Collection<U>> map)
    {
        if (this.keepSmaller)
        {
            map.forEach((k,
                         v) -> v.forEach(value -> putKeepSmaller(k, value)));
        }
        else
        {
            map.forEach((k,
                         v) -> v.forEach(value -> putKeepLarger(k, value)));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TreeMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map)
    {
        throw new UnsupportedOperationException("putAll is not applicable for UniqueValueMap. Please use putAllItems instead");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    public static class UniqueValueMapBuilder<K extends Comparable<K>, V extends Set<U>, U>
    {
        private boolean keepLarger;
        private boolean keepSmaller;
        private Supplier<V> supplier;

        public UniqueValueMapBuilder(Supplier<V> supplier)
        {
            this.supplier = supplier;
        }

        public UniqueValueMapBuilder<K, V, U> keepLarger()
        {
            this.keepLarger = true;
            return this;
        }

        public UniqueValueMapBuilder<K, V, U> keepSmaller()
        {
            this.keepSmaller = true;
            return this;
        }

        public UniqueValueMap<K, V, U> create()
        {
            var map = new UniqueValueMap<>(this);
            validate(map);
            return map;
        }

        /**
         * @param map
         */
        private void validate(UniqueValueMap<K, V, U> map)
        {
            if (map.isKeepSmaller() == map.isKeepLarger())
            {
                throw new IllegalArgumentException("UniqueValueMap cannot have same values for keepSmaller and keepLarger attributes.");
            }
        }
    }

}
