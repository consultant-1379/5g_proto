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
 * Created on: Feb 15, 2020
 *     Author: echfari
 */
package com.ericsson.sc.rxetcd;

import java.util.Optional;

import io.etcd.jetcd.ByteSequence;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * An etcd key-value mapping that is either new, or an update from an entry
 * retrieved from the database previously.
 * 
 * @param <K> The key type
 * @param <V> The value type
 */
public class UpdatedEntry<K, V> implements EtcdKv<K, V>
{
    private final Optional<EtcdEntry<K, V>> dbEntry;
    private final V value;
    private final ByteSequence etcdValue;
    private final EtcdSerializer<K, V> serializer;
    private final ByteSequence prefixedKey;
    private final LeasePolicy leasePolicy;

    public enum LeasePolicy
    {
        RENEW,
        SKIP_RENEWAL;
    }

    public static <K, V> Single<UpdatedEntry<K, V>> fromPreviousEntry(V pending,
                                                                      K key,
                                                                      LeasePolicy leasePolicy,
                                                                      Maybe<EtcdEntry<K, V>> maybePreviousEntry,
                                                                      EtcdSerializer<K, V> serializer)
    {
        return maybePreviousEntry //
                                 .doOnSuccess(prev ->
                                 {
                                     // Sanity check
                                     if (!prev.getKey().equals(key))
                                         throw new IllegalArgumentException("Key mismatch: " + key + " previous key:" + prev.getKey());
                                 })
                                 .map(curr -> new UpdatedEntry<K, V>(curr, pending, leasePolicy)) //
                                 .switchIfEmpty(Single.fromCallable(() -> new UpdatedEntry<K, V>(serializer, key, pending)));
    }

    public static <K, V> Maybe<UpdatedEntry<K, V>> fromPreviousEntry(Function<V, V> updateFunction,
                                                                     Maybe<EtcdEntry<K, V>> maybeCurrent,
                                                                     LeasePolicy leasePolicy)
    {
        return maybeCurrent //
                           .map(curr -> new UpdatedEntry<K, V>(curr, updateFunction.apply(curr.getValue()), leasePolicy)); //
    }

    public UpdatedEntry(EtcdEntry<K, V> existingEntry,
                        V updatedValue,
                        LeasePolicy leasePolicy)
    {
        this.dbEntry = Optional.of(existingEntry);
        this.value = updatedValue;
        this.serializer = existingEntry.getSerializer();
        this.prefixedKey = existingEntry.getEtcdKey();
        this.etcdValue = serializer.valueBytes(updatedValue);
        this.leasePolicy = leasePolicy;
    }

    public UpdatedEntry(EtcdSerializer<K, V> serializer,
                        K key,
                        V newValue)
    {
        this.value = newValue;
        this.serializer = serializer;
        this.prefixedKey = serializer.keyBytes(key);
        this.etcdValue = serializer.valueBytes(newValue);
        this.dbEntry = Optional.empty();
        // lease renewal is not applicable in this case, the entry is new
        this.leasePolicy = LeasePolicy.SKIP_RENEWAL;
    }

    /**
     * 
     * @return The entry retrieved from database or an empty Optional if there is no
     *         entry stored in database.
     */
    public Optional<EtcdEntry<K, V>> getPreviousEntry()
    {
        return dbEntry;
    }

    public LeasePolicy getLeasePolicy()
    {
        return this.leasePolicy;
    }

    /**
     * @return true if the entry is new and not an update to an existing one
     */
    public boolean isNew()
    {
        return this.dbEntry.isEmpty();
    }

    @Override
    public ByteSequence getEtcdKey()
    {
        return this.prefixedKey;
    }

    @Override
    public long getModRevision()
    {
        return dbEntry.map(EtcdEntry::getModRevision).orElse(0L);
    }

    @Override
    public ByteSequence getEtcdValue()
    {
        return this.etcdValue;
    }

    @Override
    public V getValue()
    {
        return this.value;
    }

    @Override
    public K getKey()
    {
        return this.serializer.key(prefixedKey);
    }
}
