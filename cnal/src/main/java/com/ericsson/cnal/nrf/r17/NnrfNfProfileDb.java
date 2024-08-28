/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 9, 2021
 *     Author: eedstl
 */

package com.ericsson.cnal.nrf.r17;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.utilities.common.IntervalTreeRnd;
import com.ericsson.utilities.common.Registry;
import com.ericsson.utilities.common.Trie;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;

public class NnrfNfProfileDb
{
    private class Db
    {
        private final Map<UUID, NFProfile> all = new ConcurrentHashMap<>();
        private final Map<String, ConcurrentSkipListSet<NFProfile>> byNfSetId = new ConcurrentHashMap<>();
        private final Trie<NFProfile> byGpsiPattern = new Trie<>();
        private final Trie<NFProfile> bySupiPattern = new Trie<>();
        private final IntervalTreeRnd<NFProfile> byGpsiRange = new IntervalTreeRnd<>();
        private final IntervalTreeRnd<NFProfile> bySupiRange = new IntervalTreeRnd<>();

        public void add(final NFProfile p)
        {
            this.all.put(p.getNfInstanceId(), p);

            // Store NF profile by its NF set IDs:

            if (p.getNfSetIdList() != null)
            {
                // Replace the currently stored NF profile:

                p.getNfSetIdList().forEach(nfSetId -> this.byNfSetId.computeIfAbsent(nfSetId, v -> new ConcurrentSkipListSet<>()).remove(p));
                p.getNfSetIdList().forEach(nfSetId -> this.byNfSetId.computeIfAbsent(nfSetId, v -> new ConcurrentSkipListSet<>()).add(p));
            }

            // Store NF profile by its GPSI ranges:

            if (p.getChfInfo() != null && p.getChfInfo().getGpsiRangeList() != null)
                p.getChfInfo().getGpsiRangeList().forEach(r -> this.addByGpsi(r, p));

            if (p.getNefInfo() != null && p.getNefInfo().getGpsiRanges() != null)
                p.getNefInfo().getGpsiRanges().forEach(r -> this.addByGpsi(r, p));

            if (p.getPcfInfo() != null && p.getPcfInfo().getGpsiRanges() != null)
                p.getPcfInfo().getGpsiRanges().forEach(r -> this.addByGpsi(r, p));

            if (p.getUdmInfo() != null && p.getUdmInfo().getGpsiRanges() != null)
                p.getUdmInfo().getGpsiRanges().forEach(r -> this.addByGpsi(r, p));

            if (p.getUdrInfo() != null && p.getUdrInfo().getGpsiRanges() != null)
                p.getUdrInfo().getGpsiRanges().forEach(r -> this.addByGpsi(r, p));

            // Store NF profile by its SUPI ranges:

            if (p.getAusfInfo() != null && p.getAusfInfo().getSupiRanges() != null)
                p.getAusfInfo().getSupiRanges().forEach(r -> this.addBySupi(r, p));

            if (p.getChfInfo() != null && p.getChfInfo().getSupiRangeList() != null)
                p.getChfInfo().getSupiRangeList().forEach(r -> this.addBySupi(r, p));

            if (p.getPcfInfo() != null && p.getPcfInfo().getSupiRanges() != null)
                p.getPcfInfo().getSupiRanges().forEach(r -> this.addBySupi(r, p));

            if (p.getUdmInfo() != null && p.getUdmInfo().getSupiRanges() != null)
                p.getUdmInfo().getSupiRanges().forEach(r -> this.addBySupi(r, p));

            if (p.getUdrInfo() != null && p.getUdrInfo().getSupiRanges() != null)
                p.getUdrInfo().getSupiRanges().forEach(r -> this.addBySupi(r, p));

            if (p.getUdsfInfo() != null && p.getUdsfInfo().getSupiRanges() != null)
                p.getUdsfInfo().getSupiRanges().forEach(r -> this.addBySupi(r, p));
        }

        public Set<NFProfile> getAll()
        {
            return this.all.values().stream().collect(Collectors.toSet());
        }

        public Trie<NFProfile> getByGpsiPattern()
        {
            return this.byGpsiPattern;
        }

        public IntervalTreeRnd<NFProfile> getByGpsiRange()
        {
            return this.byGpsiRange;
        }

        public Map<String, ConcurrentSkipListSet<NFProfile>> getByNfSetId()
        {
            return this.byNfSetId;
        }

        public Trie<NFProfile> getBySupiPattern()
        {
            return this.bySupiPattern;
        }

        public IntervalTreeRnd<NFProfile> getBySupiRange()
        {
            return this.bySupiRange;
        }

        public void remove(final NFProfile p)
        {
            this.all.remove(p.getNfInstanceId());

            // Remove NF profile by its NF set IDs:

            if (p.getNfSetIdList() != null)
            {
                p.getNfSetIdList().forEach(nfSetId -> this.byNfSetId.computeIfAbsent(nfSetId, v -> new ConcurrentSkipListSet<>()).remove(p));
                p.getNfSetIdList()
                 .forEach(nfSetId -> this.byNfSetId.computeIfPresent(nfSetId,
                                                                     (k,
                                                                      v) -> v.isEmpty() ? null : v));
            }

            // Remove NF profile by its GPSI ranges:

            if (p.getChfInfo() != null && p.getChfInfo().getGpsiRangeList() != null)
                p.getChfInfo().getGpsiRangeList().forEach(r -> this.removeByGpsi(r, p));

            if (p.getNefInfo() != null && p.getNefInfo().getGpsiRanges() != null)
                p.getNefInfo().getGpsiRanges().forEach(r -> this.removeByGpsi(r, p));

            if (p.getPcfInfo() != null && p.getPcfInfo().getGpsiRanges() != null)
                p.getPcfInfo().getGpsiRanges().forEach(r -> this.removeByGpsi(r, p));

            if (p.getUdmInfo() != null && p.getUdmInfo().getGpsiRanges() != null)
                p.getUdmInfo().getGpsiRanges().forEach(r -> this.removeByGpsi(r, p));

            if (p.getUdrInfo() != null && p.getUdrInfo().getGpsiRanges() != null)
                p.getUdrInfo().getGpsiRanges().forEach(r -> this.removeByGpsi(r, p));

            // Remove NF profile by its SUPI ranges:

            if (p.getAusfInfo() != null && p.getAusfInfo().getSupiRanges() != null)
                p.getAusfInfo().getSupiRanges().forEach(r -> this.removeBySupi(r, p));

            if (p.getChfInfo() != null && p.getChfInfo().getSupiRangeList() != null)
                p.getChfInfo().getSupiRangeList().forEach(r -> this.removeBySupi(r, p));

            if (p.getPcfInfo() != null && p.getPcfInfo().getSupiRanges() != null)
                p.getPcfInfo().getSupiRanges().forEach(r -> this.removeBySupi(r, p));

            if (p.getUdmInfo() != null && p.getUdmInfo().getSupiRanges() != null)
                p.getUdmInfo().getSupiRanges().forEach(r -> this.removeBySupi(r, p));

            if (p.getUdrInfo() != null && p.getUdrInfo().getSupiRanges() != null)
                p.getUdrInfo().getSupiRanges().forEach(r -> this.removeBySupi(r, p));

            if (p.getUdsfInfo() != null && p.getUdsfInfo().getSupiRanges() != null)
                p.getUdsfInfo().getSupiRanges().forEach(r -> this.removeBySupi(r, p));
        }

        @Override
        public String toString()
        {
            return this.all.toString();
        }

        private void addByGpsi(final IdentityRange range,
                               final NFProfile profile)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot add invalid GSPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.byGpsiPattern.add(trieKey, profile);
            else if (range.getStart() != null && range.getEnd() != null)
                this.byGpsiRange.add(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), profile);
        }

        private void addBySupi(final SupiRange range,
                               final NFProfile profile)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot add invalid SUPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.bySupiPattern.add(trieKey, profile);
            else if (range.getStart() != null && range.getEnd() != null)
                this.bySupiRange.add(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), profile);
        }

        private void removeByGpsi(final IdentityRange range,
                                  final NFProfile profile)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot remove invalid GPSI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.byGpsiPattern.remove(trieKey, profile);
            else if (range.getStart() != null && range.getEnd() != null)
                this.byGpsiRange.remove(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), profile);
        }

        private void removeBySupi(final SupiRange range,
                                  final NFProfile profile)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot remove invalid SUPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.bySupiPattern.remove(trieKey, profile);
            else if (range.getStart() != null && range.getEnd() != null)
                this.bySupiRange.remove(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), profile);
        }
    }

    @SuppressWarnings("unused")
    private static class Test
    {
        public static void main(String[] args)
        {
            NnrfNfProfileDb db = new NnrfNfProfileDb(List.of());
            UUID nfInstanceId = UUID.fromString("2ec8ac0b-265e-4165-86e9-e0735e6ce100");

            {
                ChfInfo chfInfo = new ChfInfo().supiRangeList(List.of(new SupiRange().start("460001357924600").end("460001357924699")));
                NFProfile nfProfile = new NFProfile().nfInstanceId(nfInstanceId)
                                                     .nfType(NFType.CHF)
                                                     .nfStatus(NFStatus.REGISTERED)
                                                     .fqdn("Aachen-1")
                                                     .priority(1)
                                                     .capacity(99)
                                                     .load(33)
                                                     .nfSetIdList(List.of("Set-1"))
                                                     .chfInfo(chfInfo);
                db.put(nfInstanceId, nfProfile);

                {
                    NFProfile profile = db.get(nfInstanceId);
                    log.info("11 profile={}", profile);
                }

                {
                    Set<NFProfile> profiles = db.get(NFType.CHF, List.of(p -> p.getNfSetIdList().contains("Set-1")));
                    log.info("12 profiles={}", profiles);
                }

                {
                    Set<NFProfile> profiles = db.get(NFType.CHF,
                                                     List.of(p -> p.getChfInfo()
                                                                   .getSupiRangeList()
                                                                   .stream()
                                                                   .anyMatch(r -> r.getStart().equals("460001357924600"))));
                    log.info("13 profiles={}", profiles);
                }
            }

            {
                ChfInfo chfInfo = new ChfInfo().supiRangeList(List.of(new SupiRange().start("460001357924600").end("460001357924699")));
                NFProfile nfProfile = new NFProfile().nfInstanceId(nfInstanceId)
                                                     .nfType(NFType.CHF)
                                                     .nfStatus(NFStatus.REGISTERED)
                                                     .fqdn("Aachen-1")
                                                     .priority(1)
                                                     .capacity(98)
                                                     .load(33)
                                                     .nfSetIdList(List.of("Set-1"))
                                                     .chfInfo(chfInfo);
                db.put(nfInstanceId, nfProfile);

                {
                    NFProfile profile = db.get(nfInstanceId);
                    log.info("21 profile={}", profile);
                }

                {
                    Set<NFProfile> profiles = db.get(NFType.CHF, List.of(p -> p.getNfSetIdList().contains("Set-1")));
                    log.info("22 profiles={}", profiles);
                }

                {
                    Set<NFProfile> profiles = db.get(NFType.CHF,
                                                     List.of(p -> p.getChfInfo()
                                                                   .getSupiRangeList()
                                                                   .stream()
                                                                   .anyMatch(r -> r.getStart().equals("460001357924600")),
                                                             p -> p.getNfSetIdList().contains("Set-1")));
                    log.info("23 profiles={}", profiles);
                }
            }
        }
    }

    private static final long REGISTRY_TIMEOUT_SECS = 60;

    private static final Logger log = LoggerFactory.getLogger(NnrfNfProfileDb.class);

    private static void splitRanges(final NFProfile p)
    {
//        log.info("before={}", p);

        // Split GPSI ranges:

        if (p.getChfInfo() != null && p.getChfInfo().getGpsiRangeList() != null)
            p.getChfInfo().setGpsiRangeList(splitRangesGpsi(p.getChfInfo().getGpsiRangeList()));

        if (p.getNefInfo() != null && p.getNefInfo().getGpsiRanges() != null)
            p.getNefInfo().setGpsiRanges(splitRangesGpsi(p.getNefInfo().getGpsiRanges()));

        if (p.getPcfInfo() != null && p.getPcfInfo().getGpsiRanges() != null)
            p.getPcfInfo().setGpsiRanges(splitRangesGpsi(p.getPcfInfo().getGpsiRanges()));

        if (p.getUdmInfo() != null && p.getUdmInfo().getGpsiRanges() != null)
            p.getUdmInfo().setGpsiRanges(splitRangesGpsi(p.getUdmInfo().getGpsiRanges()));

        if (p.getUdrInfo() != null && p.getUdrInfo().getGpsiRanges() != null)
            p.getUdrInfo().setGpsiRanges(splitRangesGpsi(p.getUdrInfo().getGpsiRanges()));

        // Split SUPI ranges:

        if (p.getAusfInfo() != null && p.getAusfInfo().getSupiRanges() != null)
            p.getAusfInfo().setSupiRanges(splitRangesSupi(p.getAusfInfo().getSupiRanges()));

        if (p.getChfInfo() != null && p.getChfInfo().getSupiRangeList() != null)
            p.getChfInfo().setSupiRangeList(splitRangesSupi(p.getChfInfo().getSupiRangeList()));

        if (p.getPcfInfo() != null && p.getPcfInfo().getSupiRanges() != null)
            p.getPcfInfo().setSupiRanges(splitRangesSupi(p.getPcfInfo().getSupiRanges()));

        if (p.getUdmInfo() != null && p.getUdmInfo().getSupiRanges() != null)
            p.getUdmInfo().setSupiRanges(splitRangesSupi(p.getUdmInfo().getSupiRanges()));

        if (p.getUdrInfo() != null && p.getUdrInfo().getSupiRanges() != null)
            p.getUdrInfo().setSupiRanges(splitRangesSupi(p.getUdrInfo().getSupiRanges()));

        if (p.getUdsfInfo() != null && p.getUdsfInfo().getSupiRanges() != null)
            p.getUdsfInfo().setSupiRanges(splitRangesSupi(p.getUdsfInfo().getSupiRanges()));

//        log.info("after={}", p);
    }

    private static List<IdentityRange> splitRangesGpsi(final List<IdentityRange> ranges)
    {
        return ranges.stream()
                     .map(range -> Trie.Range.of(range.getStart(), range.getEnd(), range.getPattern()))
                     .map(range -> range.split(0)
                                        .stream()
                                        .map(r -> new IdentityRange().start(r.start).end(r.end).pattern(r.pattern))
                                        .collect(Collectors.toList()))
                     .reduce(new ArrayList<>(),
                             (result,
                              element) ->
                             {
                                 result.addAll(element);
                                 return result;
                             });
    }

    private static List<SupiRange> splitRangesSupi(final List<SupiRange> ranges)
    {
        return ranges.stream()
                     .map(range -> Trie.Range.of(range.getStart(), range.getEnd(), range.getPattern()))
                     .map(range -> range.split(0).stream().map(r -> new SupiRange().start(r.start).end(r.end).pattern(r.pattern)).collect(Collectors.toList()))
                     .reduce(new ArrayList<>(),
                             (result,
                              element) ->
                             {
                                 result.addAll(element);
                                 return result;
                             });
    }

    private final Registry<UUID, NFProfile> nfProfilesById;

    private final Map<String, Db> nfProfilesByNfType;

    public NnrfNfProfileDb(final List<Consumer<NFProfile>> removeHandlers)
    {
        this.nfProfilesByNfType = new ConcurrentHashMap<>();

        final List<Consumer<NFProfile>> removeHandlersCopy = new ArrayList<>(removeHandlers);

        // This only removes the NF profile from the DB but never removes an empty DB.
        // This is acceptable as there are only a few NF types.
        removeHandlersCopy.add(p -> this.nfProfilesByNfType.computeIfAbsent(p.getNfType(), v -> new Db()).remove(p));

        final List<Consumer<NFProfile>> putHandlers = List.of(p -> this.nfProfilesByNfType.computeIfAbsent(p.getNfType(), v -> new Db()).add(p));

        this.nfProfilesById = new Registry<>(REGISTRY_TIMEOUT_SECS * 1000l, putHandlers, removeHandlersCopy);
    }

    public void clear()
    {
        this.nfProfilesById.clear();
        this.nfProfilesByNfType.clear();
    }

    public Set<NFProfile> get(final String nfType,
                              final List<Predicate<NFProfile>> filters)
    {

        final Stream<NFProfile> nfProfiles = (nfType == null ? this.nfProfilesById.entrySet().stream().map(Entry::getValue)
                                                             : this.nfProfilesByNfType.computeIfAbsent(nfType, v -> new Db()).getAll().stream());

        if (filters == null || filters.isEmpty())
            return nfProfiles.collect(Collectors.toSet());

        Set<NFProfile> result = nfProfiles.filter(filters.get(0)).collect(Collectors.toSet());

        for (int i = 1; !result.isEmpty() && i < filters.size(); ++i)
            result = result.stream().filter(filters.get(i)).collect(Collectors.toSet());

        return result;
    }

    public NFProfile get(final UUID nfInstanceId)
    {
        return this.nfProfilesById.get(nfInstanceId);
    }

    public Set<NFProfile> getByGpsi(final String nfType,
                                    final String gpsi,
                                    final List<Predicate<NFProfile>> filters)
    {
        final Db nfProfiles = this.nfProfilesByNfType.computeIfAbsent(nfType, v -> new Db());
        final Predicate<NFProfile> filter = (filters == null || filters.isEmpty()) ? null : filters.get(0);

        Set<NFProfile> result = nfProfiles.getByGpsiPattern().get(gpsi, Trie.Strategy.BEST_MATCH, filter);

        if (result.isEmpty())
            result = nfProfiles.getByGpsiRange()
                               .get(IntervalTreeRnd.Interval.of(gpsi.substring(gpsi.indexOf('-') + 1)), IntervalTreeRnd.Strategy.FIRST_MATCH, filter);

        for (int i = 1; filter != null && !result.isEmpty() && i < filters.size(); ++i)
            result = result.stream().filter(filters.get(i)).collect(Collectors.toSet());

        return result;
    }

    public Set<NFProfile> getByNfSetId(final String nfType,
                                       final String nfSetId,
                                       final List<Predicate<NFProfile>> filters)
    {
        final Stream<NFProfile> nfProfiles = this.nfProfilesByNfType.computeIfAbsent(nfType, v -> new Db())
                                                                    .getByNfSetId()
                                                                    .computeIfAbsent(nfSetId, v -> new ConcurrentSkipListSet<>())
                                                                    .stream();

        if (filters == null || filters.isEmpty())
            return nfProfiles.collect(Collectors.toSet());

        Set<NFProfile> result = nfProfiles.filter(filters.get(0)).collect(Collectors.toSet());

        for (int i = 1; !result.isEmpty() && i < filters.size(); ++i)
            result = result.stream().filter(filters.get(i)).collect(Collectors.toSet());

        return result;
    }

    public Set<NFProfile> getBySupi(final String nfType,
                                    final String supi,
                                    final List<Predicate<NFProfile>> filters)
    {
        final Db nfProfiles = this.nfProfilesByNfType.computeIfAbsent(nfType, v -> new Db());
        final Predicate<NFProfile> filter = (filters == null || filters.isEmpty()) ? null : filters.get(0);

        Set<NFProfile> result = nfProfiles.getBySupiPattern().get(supi, Trie.Strategy.BEST_MATCH, filter);

        if (result.isEmpty())
            result = nfProfiles.getBySupiRange()
                               .get(IntervalTreeRnd.Interval.of(supi.substring(supi.indexOf('-') + 1)), IntervalTreeRnd.Strategy.FIRST_MATCH, filter);

        for (int i = 1; filter != null && !result.isEmpty() && i < filters.size(); ++i)
            result = result.stream().filter(filters.get(i)).collect(Collectors.toSet());

        return result;
    }

    public NFProfile put(final UUID nfInstanceId,
                         final NFProfile nfProfile)
    {
        splitRanges(nfProfile); // Adapt ranges (SUPI, GPSI) to Trie requirements.

        return this.nfProfilesById.put(nfInstanceId, nfProfile);
    }

    public NFProfile remove(final UUID nfInstanceId)
    {
        return this.nfProfilesById.remove(nfInstanceId);
    }

    public void setUpdateDelayInMillis(long delayInMillis)
    {
        this.nfProfilesById.setUpdateDelayInMillis(delayInMillis);
    }

    public CompletableSource start()
    {
        return this.nfProfilesById.start();
    }

    public Completable stop()
    {
        return this.nfProfilesById.stop();
    }

    @Override
    public String toString()
    {
        return this.nfProfilesByNfType.toString();
    }
}
