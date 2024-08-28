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
 *     Author: echfari
 */
package com.ericsson.sc.rxetcd;

import io.etcd.jetcd.ByteSequence;

/**
 * Represents an etcd key-value The mapping might have been retrieved from the
 * database, or it might be a new entry
 * 
 * @param <K> The key type
 * @param <V> The value type
 */
public interface EtcdKv<K, V>
{
    /**
     * 
     * @return The key, as Java object
     * @see #getEtcdValue
     */
    public K getKey();

    /**
     * 
     * @return The value, as Java object
     * @see #getEtcdValue()
     */
    public V getValue();

    /**
     * 
     * @return The prefixed etcd key in binary format
     * @see #getKey()
     */
    public ByteSequence getEtcdKey();

    /**
     * 
     * @return The etcd modification revision. For new entries, the modification
     *         revision is zero.
     */
    public long getModRevision();

    /**
     * 
     * @return The value as stored in etcd, binary format
     * @see #getValue()
     */
    public ByteSequence getEtcdValue();

}
