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
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.sc.rxetcd;

import java.util.Objects;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KeyValue;

/**
 * An etcd key-value mapping retrieved from the database.
 * 
 * @param <K> The key type
 * @param <V> The value type
 */
public class EtcdEntry<K, V> implements EtcdKv<K, V>
{
    private final EtcdSerializer<K, V> serializer;
    private final V value;
    private final long modRevision;
    private final long lease;
    private final ByteSequence etcdKey;

    public EtcdEntry(EtcdSerializer<K, V> serializer,
                     KeyValue kv)
    {
        this(serializer, serializer.value(kv.getValue()), kv.getModRevision(), kv.getLease(), kv.getKey());
    }

    private EtcdEntry(EtcdSerializer<K, V> serializer,
                      V value,
                      long modRevision,
                      long lease,
                      ByteSequence key)
    {
        Objects.requireNonNull(serializer);
        Objects.requireNonNull(value);
        Objects.requireNonNull(key);

        this.serializer = serializer;
        this.value = value;
        this.modRevision = modRevision;
        this.lease = lease;
        this.etcdKey = key;
    }

    @Override
    public V getValue()
    {
        return this.value;
    }

    @Override
    public long getModRevision()
    {
        return this.modRevision;
    }

    @Override
    public ByteSequence getEtcdKey()
    {
        return etcdKey;
    }

    @Override
    public K getKey()
    {
        return this.serializer.key(this.etcdKey);
    }

    @Override
    public ByteSequence getEtcdValue()
    {
        return this.serializer.valueBytes(this.value);
    }

    /**
     * 
     * @return The etcd lease associated with this mapping
     */
    public long getLease()
    {
        return this.lease;
    }

    EtcdSerializer<K, V> getSerializer()
    {
        return this.serializer;
    }
}
