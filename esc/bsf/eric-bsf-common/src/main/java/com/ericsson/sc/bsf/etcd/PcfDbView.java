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
 * Created on: Apr 1, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Optimize search
/**
 * A consistent view of the PCF database. Objects of this class can be used to
 * perform database queries
 */
public class PcfDbView
{
    private static final Logger log = LoggerFactory.getLogger(PcfDbView.class);

    private final Map<String, Set<PcfNf>> pcfsBySetId;
    private final Map<Pair<String, String>, PcfNf> pcfsByHostAndRealm;

    /**
     * 
     * @param pcfsBySetId A mapping between PCF set IDs and PCFs
     */
    public PcfDbView(final Map<String, Set<PcfNfRecord>> pcfRecordsBySetId)
    {
        this.pcfsBySetId = new HashMap<>();

        this.pcfsByHostAndRealm = pcfRecordsBySetId.entrySet()
                                                   .stream()
                                                   .map(Map.Entry::getValue)
                                                   .flatMap(Set::stream)
                                                   .collect(Collectors.toUnmodifiableMap(pcfrec -> Pair.with(pcfrec.pcfNf().getRxDiamHost(),
                                                                                                             pcfrec.pcfNf().getRxDiamRealm()),
                                                                                         pcfrec -> pcfrec,
                                                                                         (pcfrec1,
                                                                                          pcfrec2) ->
                                                                                         {
                                                                                             final var retrievedPcf = pcfrec1.modRevision() >= pcfrec2.modRevision() ? pcfrec1
                                                                                                                                                                     : pcfrec2;

                                                                                             log.debug("Duplicate PCFs found in database: {} - {}, retrieved PCF: {}",
                                                                                                       pcfrec1.pcfNf(),
                                                                                                       pcfrec2.pcfNf(),
                                                                                                       retrievedPcf.pcfNf());

                                                                                             return retrievedPcf;
                                                                                         }))
                                                   .entrySet()
                                                   .stream()
                                                   .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().pcfNf()));

        // Collect all latest PCFs and group them by PCF set id

        this.pcfsByHostAndRealm.values()
                               .stream()
                               .flatMap(pcfnf -> pcfnf.getNfSetIdList().stream().map(id -> new AbstractMap.SimpleEntry<>(id, pcfnf)))
                               .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toSet())))
                               .forEach((k,
                                         v) -> this.pcfsBySetId.merge(k,
                                                                      v,
                                                                      (existingPcfs,
                                                                       newPcfs) ->
                                                                      {
                                                                          existingPcfs.addAll(newPcfs);
                                                                          return existingPcfs;
                                                                      }));

    }

    /**
     * Get all PCFs having the given set ID
     * 
     * @param setId The Set ID
     * @return The set of PCFs having the given set ID
     */
    public final Set<PcfNf> getPcfsBySetId(final String setId)
    {
        return this.pcfsBySetId.getOrDefault(setId, Set.of());
    }

    /**
     * Find a PCF having the given Diameter identity
     * 
     * @param diameterHost  The Diameter Host
     * @param diameterRealm The Diameter Realm
     * @return A non empty optional if PCF is found, empty optional otherwise
     */
    public final Optional<PcfNf> findPcf(final String diameterHost,
                                         final String diameterRealm)
    {
        return Optional.ofNullable(this.pcfsByHostAndRealm.get(Pair.with(diameterHost, diameterRealm)));
    }

    /**
     * Find all PCFs having the same set ID with the given PCF
     * 
     * @param diameterHost  The Diameter Host of the PCF
     * @param diameterRealm The Diameter Realm of the PCF
     * @return A set of PCFs belonging the same set, or an empty set if set member
     *         is not found
     */
    public final Set<PcfNf> findPcfsInSet(final String diameterHost,
                                          final String diameterRealm,
                                          final String pcfSetId)
    {
        final var pcfsFoundUsingSetId = pcfSetId != null ? this.pcfsBySetId.get(pcfSetId) : Set.<PcfNf>of();

        return Objects.nonNull(pcfsFoundUsingSetId) && !pcfsFoundUsingSetId.isEmpty() ? pcfsFoundUsingSetId
                                                                                      : findPcf(diameterHost, diameterRealm) // this is the legacy way we find
                                                                                                                             // Pcf using diameterHost and
                                                                                                                             // DiameterRealm
                                                                                                                            .map(pcf -> pcf.getNfSetIdList()
                                                                                                                                           .stream()
                                                                                                                                           .map(this::getPcfsBySetId) //
                                                                                                                                           .flatMap(Set::stream) //
                                                                                                                                           .collect(Collectors.toUnmodifiableSet()))
                                                                                                                            .orElse(Set.<PcfNf>of());
    }

}
