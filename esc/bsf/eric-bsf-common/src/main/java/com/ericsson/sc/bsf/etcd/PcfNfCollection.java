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
 * Created on: Mar 8, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class PcfNfCollection
{
    private static final Logger log = LoggerFactory.getLogger(PcfNfCollection.class);

    private final Map<String, Set<PcfNfRecord>> bySetId;
    private final Map<UUID, PcfNfRecord> byUuid;

    /**
     * Holder for PcfNf objects
     */
    public PcfNfCollection()
    {
        this.bySetId = new HashMap<>();
        this.byUuid = new HashMap<>();
    }

    /**
     * Copy constructor. Create a deep copy of given object
     * 
     * @param other The object to copy
     */
    public PcfNfCollection(final PcfNfCollection other)
    {
        this.byUuid = new HashMap<>(other.byUuid);
        this.bySetId = other.bySetId.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> Sets.newHashSet(entry.getValue())));
    }

    /**
     * Static factory method
     * 
     * @param pcfNfs
     * @return
     */
    static PcfNfCollection create(final Stream<PcfNfRecord> pcfNfsWithRev)
    {
        final var collection = new PcfNfCollection();
        pcfNfsWithRev.forEach(collection::add);
        return collection;
    }

    /**
     * Remove a PcfNfRecord object by UUID
     * 
     * @param uuid
     */
    public synchronized void remove(final UUID uuid)
    {
        Objects.requireNonNull(uuid);

        final var removed = this.byUuid.remove(uuid);

        if (removed == null)
        {
            log.warn("Not removing non existent PcfNf with nfInstanceId={}", uuid);
            return;
        }

        removed.pcfNf().getNfSetIdList().stream().map(this.bySetId::get).filter(Objects::nonNull).forEach(set -> set.remove(removed));

        this.bySetId.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Add A PcfNfRecord object to collection
     * 
     * @param PcfNfRecord
     */
    public synchronized void add(final PcfNfRecord pcfNfWithRev)
    {
        Objects.requireNonNull(pcfNfWithRev);

        final var prev = this.byUuid.get(pcfNfWithRev.pcfNf().getNfInstanceId());
        if (prev != null)
        {
            log.info("Updating PcfNf {}", prev.pcfNf());
            this.remove(prev.pcfNf().getNfInstanceId());
        }

        this.byUuid.put(pcfNfWithRev.pcfNf().getNfInstanceId(), pcfNfWithRev);

        pcfNfWithRev.pcfNf()
                    .getNfSetIdList()
                    .forEach(id -> this.bySetId.compute(id,
                                                        (k,
                                                         v) ->
                                                        {
                                                            if (v != null)
                                                            {
                                                                v.add(pcfNfWithRev);
                                                                return v;
                                                            }
                                                            else
                                                            {
                                                                return Sets.newHashSet(pcfNfWithRev);
                                                            }
                                                        }));

    }

    /**
     * 
     * @return A Map that contains all PcfNfRecord objects indexed by their NfSet ID
     */
    public Map<String, Set<PcfNfRecord>> getBySetId()
    {
        return this.bySetId;
    }

}
