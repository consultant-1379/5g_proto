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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFService;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.AusfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NefInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.PcfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdmInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdrInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.UdsfInfo;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.IntervalTreeRnd;
import com.ericsson.utilities.common.Trie;
import com.ericsson.utilities.common.Trie.Range;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "statistics" })
public class NnrfDiscSearchResultDb2
{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "nrfGroup", "valid", "data" })
    public static class Item
    {
        @JsonIgnore
        private static final Random random = new Random();

        private static boolean matchGpsi(final String gpsi,
                                         final ChfInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRangeList()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final List<IdentityRange> ranges)
        {
            boolean matches = true;

            if (ranges != null)
            {
                for (final IdentityRange range : ranges)
                {
                    matches = range.getPattern() != null ? gpsi.matches(range.getPattern())
                                                         : Range.includes(range.getStart(), range.getEnd(), gpsi.substring(gpsi.indexOf('-') + 1));

                    if (matches)
                        return true;
                }
            }

            return matches;
        }

        private static boolean matchGpsi(final String gpsi,
                                         final NefInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final PcfInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final UdmInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchGpsi(final String gpsi,
                                         final UdrInfo info)
        {
            return (gpsi != null && info != null) ? matchGpsi(gpsi, info.getGpsiRanges()) : (gpsi == null);
        }

        private static boolean matchServiceNames(final List<String> serviceNames,
                                                 final NFProfile nfProfile)
        {
            boolean matches = true;

            if (serviceNames != null)
            {
                // Property service-list takes precedence over deprecated property services.
                final List<NFService> nfServices = nfProfile.getNfServiceList() != null
                                                   && !nfProfile.getNfServiceList().isEmpty() ? List.copyOf(nfProfile.getNfServiceList().values())
                                                                                              : nfProfile.getNfServices();

                if (nfServices != null && !nfServices.isEmpty())
                {
                    matches = false;

                    for (String serviceName : serviceNames)
                    {
                        for (NFService nfService : nfServices)
                        {
                            if (nfService.getServiceName().equals(serviceName))
                                return true;
                        }
                    }
                }
            }

            return matches;
        }

        private static boolean matchSupi(final String supi,
                                         final AusfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final ChfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRangeList()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final PcfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdmInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdrInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final UdsfInfo info)
        {
            return (supi != null && info != null) ? matchSupi(supi, info.getSupiRanges()) : (supi == null);
        }

        private static boolean matchSupi(final String supi,
                                         final List<SupiRange> ranges)
        {
            boolean matches = true;

            if (ranges != null)
            {
                for (final SupiRange range : ranges)
                {
                    matches = range.getPattern() != null ? supi.matches(range.getPattern())
                                                         : Range.includes(range.getStart(), range.getEnd(), supi.substring(supi.indexOf('-') + 1));

                    if (matches)
                        return true;
                }
            }

            return matches;
        }

        @JsonProperty("nrfGroup")
        private final String nrfGroup;

        @JsonIgnore
        private final String queryParamNames;

        @JsonIgnore
        private final SearchResult data;

        @JsonIgnore
        private final long expirationTimeMillis;

        public Item(final String nrfGroup,
                    final String queryParamNames,
                    final SearchResult data)
        {
            this.nrfGroup = nrfGroup;
            this.queryParamNames = queryParamNames;
            this.data = data;

            // From the expirationTimeMillis a random jitter is subtracted in order to avoid
            // traffic peaks at timeout (the jitter will cause the traffic to be distributed
            // over time). The jitter is subtracted in order to make sure that the real
            // expiration time is not exceeded.
            // 0 <= jitter < 1% of the validityPeriod (but max one hour)
            final long jitterMillis = random.nextInt(Math.min(10 * data.getValidityPeriod(), 3600000)); // Limit jitter to max 1 hour.
            this.expirationTimeMillis = System.currentTimeMillis() + 1000l * data.getValidityPeriod() - jitterMillis;
        }

        @JsonIgnore
        @Override
        public boolean equals(Object other)
        {
            if (other == this)
                return true;

            if (!(other instanceof Item))
                return false;

            Item rhs = ((Item) other);
            return (this.nrfGroup == rhs.nrfGroup || this.nrfGroup != null && this.nrfGroup.equals(rhs.nrfGroup))
                   && (this.queryParamNames == rhs.queryParamNames || this.queryParamNames != null && this.queryParamNames.equals(rhs.queryParamNames))
                   && (this.data == rhs.data || this.data != null && this.data.shallowEquals(rhs.data));
        }

        @JsonProperty("data")
        public SearchResult getData()
        {
            return this.data;
        }

        @JsonIgnore
        public Item getNewer(final Item rhs)
        {
            return rhs == null || this.expirationTimeMillis > rhs.expirationTimeMillis ? this : rhs;
        }

        @Override
        public int hashCode()
        {
            int result = 1;
            result = ((result * 31) + ((this.nrfGroup == null) ? 0 : this.nrfGroup.hashCode()));
            result = ((result * 31) + ((this.queryParamNames == null) ? 0 : this.queryParamNames.hashCode()));
            result = ((result * 31) + ((this.data == null) ? 0 : this.data.shallowHashCode()));
            return result;
        }

        @JsonProperty("valid")
        public boolean isValid()
        {
            return System.currentTimeMillis() < this.expirationTimeMillis;
        }

        @JsonIgnore
        public boolean matches(final String nrfGroup,
                               final Query query)
        {
            if (!this.nrfGroup.equals(nrfGroup) || !this.queryParamNames.equals(query.getParamNames()))
                return false;

            final String targetNfType = query.getParamAsString("target-nf-type");
            @SuppressWarnings("unchecked")
            final List<String> serviceNames = (List<String>) query.getParam("service-names");
            final String gpsi = query.getParamAsString("gpsi");
            final String supi = query.getParamAsString("supi");

//            log.debug("targetNfType={}, gpsi={}, supi={}", targetNfType, gpsi, supi);

            boolean matches = false;

            for (final NFProfile nfProfile : this.data.getNfInstances())
            {
                matches = nfProfile.getNfType().equalsIgnoreCase(targetNfType) && matchServiceNames(serviceNames, nfProfile);

                if (matches)
                {
                    switch (targetNfType.toUpperCase())
                    {
                        case NFType.AUSF:
                            matches = matchSupi(supi, nfProfile.getAusfInfo());
                            break;

                        case NFType.CHF:
                            matches = matchGpsi(gpsi, nfProfile.getChfInfo()) && matchSupi(supi, nfProfile.getChfInfo());
                            break;

                        case NFType.NEF:
                            matches = matchGpsi(gpsi, nfProfile.getNefInfo());
                            break;

                        case NFType.PCF:
                            matches = matchGpsi(gpsi, nfProfile.getPcfInfo()) && matchSupi(supi, nfProfile.getPcfInfo());
                            break;

                        case NFType.UDM:
                            matches = matchGpsi(gpsi, nfProfile.getUdmInfo()) && matchSupi(supi, nfProfile.getUdmInfo());
                            break;

                        case NFType.UDR:
                            matches = matchGpsi(gpsi, nfProfile.getUdrInfo()) && matchSupi(supi, nfProfile.getUdrInfo());
                            break;

                        case NFType.UDSF:
                            matches = matchSupi(supi, nfProfile.getUdsfInfo());
                            break;

                        default:
                            break;
                    }
                }

                if (!matches)
                    return false;
            }

            return matches;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "size", "gets", "adds", "rems", "hits" })
    public static class Statistics
    {
        @JsonProperty("gets")
        public Count numberOfGets;
        @JsonProperty("adds")
        public Count numberOfAdds;
        @JsonProperty("rems")
        public Count numberOfRems;
        @JsonProperty("size")
        public AtomicLong size;

        @JsonIgnore
        private final String id;

        public Statistics(final String id)
        {
            this.numberOfGets = new Count();
            this.numberOfAdds = new Count();
            this.numberOfRems = new Count();
            this.size = new AtomicLong(0l);
            this.id = id;
        }

        @JsonIgnore
        public synchronized void clear()
        {
            this.numberOfGets.clear();
            this.numberOfAdds.clear();
            this.numberOfRems.clear();
            this.size.set(0l);
        }

        @JsonIgnore
        public String getId()
        {
            return this.id;
        }

        @JsonIgnore
        public Statistics setSize(final long size)
        {
            this.size.set(size);
            return this;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    private static class Db
    {
        private final Map<Item, Item> all = new ConcurrentHashMap<>();
        private final Trie<Item> byGpsiPattern = new Trie<>();
        private final Trie<Item> bySupiPattern = new Trie<>();
        private final IntervalTreeRnd<Item> byGpsiRange = new IntervalTreeRnd<>();
        private final IntervalTreeRnd<Item> bySupiRange = new IntervalTreeRnd<>();

        public void add(final Item i)
        {
            final Item replacedItem = this.all.put(i, i);

            // Remove old item as searchResults.add(item) does not overwrite identical
            // items.
            if (replacedItem != null)
                this.remove(replacedItem);

            // Normalize ranges to store the item only once per range:

            final Set<IdentityRange> gpsiRanges = new HashSet<>();
            final Set<SupiRange> supiRanges = new HashSet<>();

            i.getData().getNfInstances().forEach(p ->
            {
                // Collect GPSI ranges:

                if (p.getChfInfo() != null && p.getChfInfo().getGpsiRangeList() != null)
                    gpsiRanges.addAll(p.getChfInfo().getGpsiRangeList());

                if (p.getNefInfo() != null && p.getNefInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getNefInfo().getGpsiRanges());

                if (p.getPcfInfo() != null && p.getPcfInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getPcfInfo().getGpsiRanges());

                if (p.getUdmInfo() != null && p.getUdmInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getUdmInfo().getGpsiRanges());

                if (p.getUdrInfo() != null && p.getUdrInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getUdrInfo().getGpsiRanges());

                // Collect SUPI ranges:

                if (p.getAusfInfo() != null && p.getAusfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getAusfInfo().getSupiRanges());

                if (p.getChfInfo() != null && p.getChfInfo().getSupiRangeList() != null)
                    supiRanges.addAll(p.getChfInfo().getSupiRangeList());

                if (p.getPcfInfo() != null && p.getPcfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getPcfInfo().getSupiRanges());

                if (p.getUdmInfo() != null && p.getUdmInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdmInfo().getSupiRanges());

                if (p.getUdrInfo() != null && p.getUdrInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdrInfo().getSupiRanges());

                if (p.getUdsfInfo() != null && p.getUdsfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdsfInfo().getSupiRanges());
            });

            // Now store the item per range:

            gpsiRanges.forEach(r -> this.addByGpsi(r, i));
            supiRanges.forEach(r -> this.addBySupi(r, i));
        }

        public Set<Item> getAll()
        {
            return this.all.keySet();
        }

        public Trie<Item> getByGpsiPattern()
        {
            return this.byGpsiPattern;
        }

        public IntervalTreeRnd<Item> getByGpsiRange()
        {
            return this.byGpsiRange;
        }

        public Trie<Item> getBySupiPattern()
        {
            return this.bySupiPattern;
        }

        public IntervalTreeRnd<Item> getBySupiRange()
        {
            return this.bySupiRange;
        }

        public long getSize()
        {
            return this.all.size();
        }

        public void remove(final Item i)
        {
            this.all.remove(i);

            // Normalize ranges to remove the item only once per range:

            final Set<IdentityRange> gpsiRanges = new HashSet<>();
            final Set<SupiRange> supiRanges = new HashSet<>();

            i.getData().getNfInstances().forEach(p ->
            {
                // Collect GPSI ranges:

                if (p.getChfInfo() != null && p.getChfInfo().getGpsiRangeList() != null)
                    gpsiRanges.addAll(p.getChfInfo().getGpsiRangeList());

                if (p.getNefInfo() != null && p.getNefInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getNefInfo().getGpsiRanges());

                if (p.getPcfInfo() != null && p.getPcfInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getPcfInfo().getGpsiRanges());

                if (p.getUdmInfo() != null && p.getUdmInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getUdmInfo().getGpsiRanges());

                if (p.getUdrInfo() != null && p.getUdrInfo().getGpsiRanges() != null)
                    gpsiRanges.addAll(p.getUdrInfo().getGpsiRanges());

                // Collect SUPI ranges:

                if (p.getAusfInfo() != null && p.getAusfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getAusfInfo().getSupiRanges());

                if (p.getChfInfo() != null && p.getChfInfo().getSupiRangeList() != null)
                    supiRanges.addAll(p.getChfInfo().getSupiRangeList());

                if (p.getPcfInfo() != null && p.getPcfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getPcfInfo().getSupiRanges());

                if (p.getUdmInfo() != null && p.getUdmInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdmInfo().getSupiRanges());

                if (p.getUdrInfo() != null && p.getUdrInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdrInfo().getSupiRanges());

                if (p.getUdsfInfo() != null && p.getUdsfInfo().getSupiRanges() != null)
                    supiRanges.addAll(p.getUdsfInfo().getSupiRanges());
            });

            // Now remove the item per range:

            gpsiRanges.forEach(r -> this.removeByGpsi(r, i));
            supiRanges.forEach(r -> this.removeBySupi(r, i));
        }

        @Override
        public String toString()
        {
            return this.all.toString();
        }

        private void addByGpsi(final IdentityRange range,
                               final Item item)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot add invalid GSPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.byGpsiPattern.add(trieKey, item);
            else if (range.getStart() != null && range.getEnd() != null)
                this.byGpsiRange.add(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), item);
        }

        private void addBySupi(final SupiRange range,
                               final Item item)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot add invalid SUPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.bySupiPattern.add(trieKey, item);
            else if (range.getStart() != null && range.getEnd() != null)
                this.bySupiRange.add(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), item);
        }

        private void removeByGpsi(final IdentityRange range,
                                  final Item item)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot remove invalid GPSI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.byGpsiPattern.remove(trieKey, item);
            else if (range.getStart() != null && range.getEnd() != null)
                this.byGpsiRange.remove(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), item);
        }

        private void removeBySupi(final SupiRange range,
                                  final Item item)
        {
            final String trieKey = Trie.Range.toTrieKey(range.getStart(), range.getEnd(), range.getPattern());

            if (trieKey == null)
            {
                log.error("Cannot remove invalid SUPI range: start={}, end={}, pattern={}.", range.getStart(), range.getEnd(), range.getPattern());
                return;
            }

            if (range.getPattern() != null)
                this.bySupiPattern.remove(trieKey, item);
            else if (range.getStart() != null && range.getEnd() != null)
                this.bySupiRange.remove(IntervalTreeRnd.Interval.of(range.getStart(), range.getEnd()), item);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(NnrfDiscSearchResultDb2.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse

    @JsonIgnore
    private final Map<String, Db> itemsByNfType;

    @JsonIgnore
    private final Statistics counts;

    public NnrfDiscSearchResultDb2()
    {
        this.itemsByNfType = new ConcurrentHashMap<>();
        this.counts = new Statistics("db");
    }

    public void add(final Item item)
    {
        this.counts.numberOfAdds.inc();
        this.itemsByNfType.computeIfAbsent(item.getData().getNfInstances().get(0).getNfType(), v -> new Db()).add(item);
    }

    public void clear()
    {
        this.itemsByNfType.clear();
    }

    public Set<Item> get(final String nrfGroup,
                         final Query query)
    {
        this.counts.numberOfGets.inc();

        final String targetNfType = query.getParamAsString("target-nf-type");
        final String gpsi = query.getParamAsString("gpsi");
        final String supi = query.getParamAsString("supi");

//        log.debug("targetNfType={}, gpsi={}, supi={}", targetNfType, gpsi, supi);

        final Predicate<Item> filter = v -> v.matches(nrfGroup, query);

        if (gpsi != null)
            return this.getByGpsi(targetNfType, gpsi, filter);

        if (supi != null)
            return this.getBySupi(targetNfType, supi, filter);

        return Set.of();
    }

    public Set<Item> get(final String nfType,
                         final Predicate<Item> filter)
    {
        return this.itemsByNfType.computeIfAbsent(nfType, v -> new Db())
                                 .getAll()
                                 .stream()
                                 .filter(Optional.ofNullable(filter).orElse(p -> true))
                                 .collect(Collectors.toSet());
    }

    public Set<Item> getByGpsi(final String nfType,
                               final String gpsi,
                               final Predicate<Item> filter)
    {
        final Db nfProfiles = this.itemsByNfType.computeIfAbsent(nfType, v -> new Db());

        Set<Item> result = nfProfiles.getByGpsiPattern().get(gpsi, Trie.Strategy.BEST_MATCH, filter);

        if (result.isEmpty())
            result = nfProfiles.getByGpsiRange()
                               .get(IntervalTreeRnd.Interval.of(gpsi.substring(gpsi.indexOf('-') + 1)), IntervalTreeRnd.Strategy.FIRST_MATCH, filter);

        return result;
    }

    public Set<Item> getBySupi(final String nfType,
                               final String supi,
                               final Predicate<Item> filter)
    {
        final Db nfProfiles = this.itemsByNfType.computeIfAbsent(nfType, v -> new Db());
        Set<Item> result = nfProfiles.getBySupiPattern().get(supi, Trie.Strategy.BEST_MATCH, filter);

        if (result.isEmpty())
            result = nfProfiles.getBySupiRange()
                               .get(IntervalTreeRnd.Interval.of(supi.substring(supi.indexOf('-') + 1)), IntervalTreeRnd.Strategy.FIRST_MATCH, filter);

        return result;
    }

    @JsonProperty("statistics")
    public Statistics getStatistics()
    {
        return this.counts.setSize(this.itemsByNfType.values().stream().collect(Collectors.summingLong(Db::getSize)));
    }

    public void remove(final Item item)
    {
        this.counts.numberOfRems.inc();
        this.itemsByNfType.computeIfAbsent(item.getData().getNfInstances().get(0).getNfType(), v -> new Db()).remove(item);
    }

    @Override
    public String toString()
    {
        return this.itemsByNfType.toString();
    }
}
