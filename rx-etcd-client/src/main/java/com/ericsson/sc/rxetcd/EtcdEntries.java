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
 * Created on: Jan 15, 2020
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import java.util.List;

/**
 * A list of etcd entries, for the same database revision
 * 
 * @param <K> The key type
 * @param <V> the value type
 */
public class EtcdEntries<K, V>
{
    private final long revision;
    private final List<EtcdEntry<K, V>> entries;

    /**
     * @param entries  The entries
     * @param revision The etcd database revision
     */
    public EtcdEntries(long revision,
                       List<EtcdEntry<K, V>> entries)
    {
        this.revision = revision;
        this.entries = entries;
    }

    /**
     * @return the revision
     */
    public long getRevision()
    {
        return revision;
    }

    /**
     * @return the entries
     */
    public List<EtcdEntry<K, V>> getEntries()
    {
        return entries;
    }

    @Override
    public String toString()
    {
        return "EtcdEntries [revision=" + revision + ", entries=" + entries + "]";
    }
}
