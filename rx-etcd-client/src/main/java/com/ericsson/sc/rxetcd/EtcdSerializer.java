/**
 * CO7PYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 12, 2020
 *     Author: echfari
 */
package com.ericsson.sc.rxetcd;

import java.nio.charset.StandardCharsets;

import com.google.protobuf.ByteString;

import io.etcd.jetcd.ByteSequence;
import io.reactivex.functions.Function;

/**
 * Serializes/deserializes java objects as etcd key-values
 * 
 * @param <K> The key type
 * @param <V> The value type
 */
public class EtcdSerializer<K, V>
{
    private final String prefix;
    private final ByteString prefixBytes;
    private final ByteSequence prefixByteSequence;
    private final Function<V, ByteString> valueSerializer;
    private final Function<K, ByteString> keySerializer;
    private final Function<byte[], V> valueDeSerializer;
    private final Function<byte[], K> keyDeserializer;

    public EtcdSerializer(String prefix,
                          Function<K, ByteString> keySerializer,
                          Function<byte[], K> keyDeserializer,
                          Function<V, ByteString> valueSerializer,
                          Function<byte[], V> valueDeserializer)

    {
        this.prefix = prefix;
        this.prefixBytes = ByteString.copyFrom(prefix, StandardCharsets.UTF_8);
        this.prefixByteSequence = ByteSequence.from(this.prefixBytes);

        this.valueSerializer = valueSerializer;
        this.valueDeSerializer = valueDeserializer;
        this.keySerializer = keySerializer;
        this.keyDeserializer = keyDeserializer;
    }

    /**
     * 
     * @return The etcd prefix as java string
     */
    public String getPrefix()
    {
        return this.prefix;
    }

    /**
     * 
     * @return The etcd prefix in binary format
     */
    public ByteSequence getPrefixBytes()
    {
        return this.prefixByteSequence;
    }

    /**
     * Serializes a key to etcd binary format
     * 
     * @param key The key to serialize
     * @return The converted key bytes
     * @throws IllegalArgumentException if serialization fails
     */
    public ByteSequence keyBytes(K key)
    {
        try
        {
            final var keyb = keySerializer.apply(key);
            if (keyb.isEmpty())
                throw new IllegalArgumentException("Empty unprefixed etcd key not allowed");
            return ByteSequence.from(prefixBytes.concat(keyb));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Serializes a value to etcd binary format
     * 
     * @param value The value to serialize
     * @return The serialized value
     * @throws IllegalArgumentException if serialization fails
     */
    public ByteSequence valueBytes(V value)
    {
        try
        {
            final var valueb = valueSerializer.apply(value);
            return ByteSequence.from(valueb);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Deserializes etcd bytes as value
     * 
     * @param valBytes The bytes to deserialize
     * @return The deserialized java object
     * @throws IllegalArgumentException if deserialization fails
     */
    public V value(ByteSequence valBytes)
    {
        try
        {
            return valueDeSerializer.apply(valBytes.getBytes());
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Deserializes etcd bytes as key
     * 
     * @param prefixedKey The bytes to deserialize into key
     * @return The deserialized key
     * @throws IllegalArgumentException if deserialization fails
     */
    public K key(ByteSequence prefixedKey)
    {
        try
        {
            var prefixedKeyBytes = ByteString.copyFrom(prefixedKey.getBytes());
            // Ensure that input is a prefixed key for this prefix
            if (prefixedKeyBytes.startsWith(prefixBytes))
            {
                final var keyBytes = prefixedKeyBytes.substring(prefixBytes.size(), prefixedKeyBytes.size());
                if (keyBytes.size() > 0)
                {
                    // Ensure that unprefixed key is not empty string
                    return keyDeserializer.apply((keyBytes.toByteArray()));
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid etcd prefixed key", e);
        }
        throw new IllegalArgumentException("Invalid etcd prefixed key");
    }
}
