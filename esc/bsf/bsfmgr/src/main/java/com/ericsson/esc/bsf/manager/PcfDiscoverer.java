/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 6, 2023
 *     Author: eedstl
 */

package com.ericsson.esc.bsf.manager;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.esc.bsf.worker.CmConfigurationUtil;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.glue.NfFunction;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext.PollingQuery;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext.TargetedQuery;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.ericsson.sc.rxetcd.RxEtcd;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;

/**
 * Encapsulates the discovery of all NF data needed by BSF. <br>
 * There must be only one discoverer that is retrieving the NF data from the
 * NRF. At the moment only one query is sent for discovery, for PCF NFs. In case
 * data from other NFs need to be retrieved, the respective queries need just to
 * be added to the SearchingContext passed to the NnrfNfDiscoverer.
 */
public class PcfDiscoverer
{
    private static final Logger log = LoggerFactory.getLogger(PcfDiscoverer.class);

    private static final int PCF_POLLING_INTERVAL_SECS = 60;
    private static final String PCF_GROUP = "pcfGroup";

    private final NnrfNfDiscoverer pcfNfDiscoverer;
    private AtomicReference<Set<TargetedQuery>> targetedPcfQueries = new AtomicReference<>(Set.of());

    public PcfDiscoverer(final NfFunction nfFunction,
                         final Flowable<Optional<EricssonBsf>> configFlow)
    {
        this.pcfNfDiscoverer = new NnrfNfDiscoverer(null,
                                                    nrfGroup -> Optional.ofNullable(nfFunction.getNfInstance(0)).map(i -> i.getNrfGroups().get(nrfGroup)),
                                                    configFlow.map(this::mapConfigToInput),
                                                    SearchingContext.OutputStrategy.ALL_SUCCESS);
    }

    /**
     * Creates a PcfNf object from a NFProfile object. It throws
     * NullPointerException when a required field for PcfNf is missing from the
     * NFProfile object.
     * 
     * @param nfProfile The discovered NFProfile of a PCF.
     * @return A PcfNF object, which can be stored in PcfDb.
     */
    static PcfNf toPcfNf(NFProfile nfProfile)
    {
        return PcfNf.newBuilder()
                    .withNfInstanceId(nfProfile.getNfInstanceId())
                    .withNfStatus(nfProfile.getNfStatus())
                    .withNfSetIdList(nfProfile.getNfSetIdList())
                    .withRxDiamHost(nfProfile.getPcfInfo().getRxDiamHost())
                    .withRxDiamRealm(nfProfile.getPcfInfo().getRxDiamRealm())
                    .build();
    }

    /**
     * Creates a list of PcfNf objects from a list of NFProfile objects. Any
     * NFProfile that does not have the required fields for PcfNf is skipped.
     * 
     * @param nfProfiles A list of discovered NFProfiles from NRF.
     * @return A list of PcfNf objects, which can be stored in PcfDb.
     */
    static List<PcfNf> toPcfNfList(List<NFProfile> nfProfiles)
    {
        return nfProfiles.stream()
                         // Ensure PCF NFType, although all profiles should already refer to PCFs.
                         .filter(profile -> profile.getNfType().equalsIgnoreCase(NFType.PCF))
                         .map(profile ->
                         {
                             try
                             {
                                 return Optional.<PcfNf>of(toPcfNf(profile));
                             }
                             catch (NullPointerException e)
                             {
                                 // In case, required fields are missing from NFProfile and one
                                 // of RxDiamHost or RxDiamRealm is available log the PCF profile,
                                 // otherwise skip it silently.
                                 var pcfInfo = profile.getPcfInfo();
                                 if (pcfInfo != null && (pcfInfo.getRxDiamHost() != null || pcfInfo.getRxDiamRealm() != null))
                                 {
                                     log.warn("Skipped PCF due to required fields missing: {}", profile);
                                 }

                                 return Optional.<PcfNf>empty();
                             }
                         })
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .toList();
    }

    /**
     * Transforms each emitted SearchingContext object to a list of PcfNf objects.
     *
     * @param targetPollGroup The name of the target group of polling queries.
     * @param targetQueries   The target queries.
     * @return A Flowable transformer from SearchingContext to list of PcfNfs.
     */
    static FlowableTransformer<SearchingContext, List<PcfNf>> toPcfNfTransformer(String targetPollGroup,
                                                                                 AtomicReference<Set<TargetedQuery>> targetQueries)
    {
        return context -> context.map(SearchingContext::getData) //
                                 .filter(Optional::isPresent)
                                 .map(Optional::get)
                                 .map(data -> data.entrySet()
                                                  .stream()
                                                  .filter(entry -> entry.getKey().equals(targetPollGroup))
                                                  .map(Entry::getValue)
                                                  .map(map -> map.entrySet()
                                                                 .stream()
                                                                 .filter(e -> targetQueries.get().contains(e.getKey()))
                                                                 .map(Entry::getValue)
                                                                 .map(SearchResult::getNfInstances)
                                                                 .map(PcfDiscoverer::toPcfNfList)
                                                                 .reduce(new ArrayList<>(),
                                                                         (result,
                                                                          element) ->
                                                                         {
                                                                             result.addAll(element);
                                                                             return result;
                                                                         }))
                                                  .findAny()
                                                  .orElse(List.of()));
    }

    /**
     * Creates a PcfRt object from a NFProfile object if required fields are
     * present.
     * 
     * @param nfProfile The discovered NFProfile of a PCF.
     * @return Optional of PcfRt, which can be stored in PcfRtDb.
     */
    static Optional<PcfRt> toPcfRt(NFProfile nfProfile)
    {
        try
        {
            return Optional //
                           .ofNullable(nfProfile.getRecoveryTime())
                           .map(OffsetDateTime::toInstant)
                           .map(instant -> new PcfRt(nfProfile.getNfInstanceId(), instant));
        }
        catch (Exception e)
        {
            // This should never happen
            log.warn("Ignored invalid PcfRt: {}", nfProfile, e);
            return Optional.empty();
        }
    }

    /**
     * Creates a list of PcfRt objects from a list of NFProfile objects. Any
     * NFProfile that does not have the required fields for PcfRt is skipped.
     * 
     * @param nfProfiles A list of discovered NFProfiles from NRF.
     * @return A list of PcfRt objects, which can be stored in PcfRtDb.
     */
    static List<PcfRt> toPcfRtList(List<NFProfile> nfProfiles)
    {
        return nfProfiles.stream()
                         // Ensure PCF NFType, although all profiles should already refer to PCFs.
                         .filter(profile -> profile.getNfType().equalsIgnoreCase(NFType.PCF))
                         .map(PcfDiscoverer::toPcfRt)
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .toList();
    }

    /**
     * Transforms each emitted SearchingContext object to a list of PcfRt objects.
     *
     * @param targetPollGroup The name of the target group of polling queries.
     * @param targetQueries   The target queries.
     * @return A Flowable transformer from SearchingContext to list of PcfRts.
     */
    static FlowableTransformer<SearchingContext, List<PcfRt>> toPcfRtTransformer(String targetPollGroup,
                                                                                 AtomicReference<Set<TargetedQuery>> targetQueries)
    {
        return context -> context.map(SearchingContext::getData) //
                                 .filter(Optional::isPresent)
                                 .map(Optional::get)
                                 .map(data -> data.entrySet()
                                                  .stream()
                                                  .filter(entry -> entry.getKey().equals(targetPollGroup))
                                                  .map(Entry::getValue)
                                                  .map(map -> map.entrySet()
                                                                 .stream()
                                                                 .filter(e -> targetQueries.get().contains(e.getKey()))
                                                                 .map(Entry::getValue)
                                                                 .map(SearchResult::getNfInstances)
                                                                 .map(PcfDiscoverer::toPcfRtList)
                                                                 .reduce(new ArrayList<>(),
                                                                         (result,
                                                                          element) ->
                                                                         {
                                                                             result.addAll(element);
                                                                             return result;
                                                                         }))
                                                  .findAny()
                                                  .orElse(List.of()));
    }

    private static List<String> getNrfGroupsForDiscovery(final EricssonBsf config)
    {
        final List<String> nrfGroups = new ArrayList<>();

        if (config.getEricssonBsfBsfFunction() != null && config.getEricssonBsfBsfFunction().getNfInstance() != null
            && !config.getEricssonBsfBsfFunction().getNfInstance().isEmpty()
            && config.getEricssonBsfBsfFunction().getNfInstance().get(0).getNrfService() != null
            && config.getEricssonBsfBsfFunction().getNfInstance().get(0).getNrfService().getNfDiscovery() != null)
        {
            nrfGroups.add(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getNrfService().getNfDiscovery().getNrfGroupRef());
        }

        return nrfGroups;
    }

    public SearchingContext mapConfigToInput(final Optional<EricssonBsf> bsfNfDiscoveryConfig)
    {
        return bsfNfDiscoveryConfig.map(config ->
        {
            final Query pcfQuery = new Query.Builder().add("requester-nf-type", NFType.BSF).add("target-nf-type", NFType.PCF).build();

            this.targetedPcfQueries.set(getNrfGroupsForDiscovery(config).stream().map(group -> TargetedQuery.of(pcfQuery, group)).collect(Collectors.toSet()));

            final var pollingInterval = CmConfigurationUtil.getNfPoolDiscovery(bsfNfDiscoveryConfig)
                                                           .map(nfPoolDiscovery -> nfPoolDiscovery.getUpdateInterval().seconds())
                                                           .orElse(PCF_POLLING_INTERVAL_SECS);

            final var queriesPerPool = Map.of(PCF_GROUP,
                                              this.targetedPcfQueries.get()
                                                                     .stream()
                                                                     .map(query -> PollingQuery.of(query, pollingInterval))
                                                                     .collect(Collectors.toSet()));

            log.debug("queriesPerPool={}", queriesPerPool);

            return SearchingContext.of(Optional.of(queriesPerPool));
        }).orElse(SearchingContext.empty());
    }

    /**
     * Emits a list of all PCF network functions that are discovered when polling
     * the configured NrfGroup for discovery.
     * 
     * @return A Flowable that emits a list of all discovered PcfNfs.
     */
    public Flowable<List<PcfNf>> getDiscoveredPcfNfs()
    {
        return this.pcfNfDiscoverer.getOutput() //
                                   .toFlowable(BackpressureStrategy.BUFFER)
                                   .compose(toPcfNfTransformer(PCF_GROUP, this.targetedPcfQueries));
    }

    /**
     * Fetch the timestamp of the last Nf-discovery and write is to etcd
     * 
     * @return A {@code Completable} that completes as soon as etcd writes the
     *         last-update's data in its database or with an error if write did not
     *         succeed
     */
    public Completable lastUpdateTimestampToEtcd(final JsonValueSerializer<String, String> etcdSerializer,
                                                 final RxEtcd rxEtcd,
                                                 final String etcdKey)
    {
        final var key = etcdSerializer.keyBytes(etcdKey);
        log.debug("The key for the last-update leaf value for the etcd is {}", key);

        return this.getTimestamp()
                   .doOnNext(timestamp -> log.debug("The timestamp of the NF discovery is {}", timestamp))
                   .flatMapSingle(timestamp -> rxEtcd.put(key, etcdSerializer.valueBytes(timestamp)))
                   .doOnError(t -> log.error("Failed to write the new timestamp of NF discovery to etcd", t))
                   .ignoreElements();
    }

    Flowable<String> getTimestamp()
    {
        return this.pcfNfDiscoverer.getOutput()
                                   .toFlowable(BackpressureStrategy.BUFFER)
                                   .map(searchingContext -> OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    /**
     * Emits a list of all PCF network functions that are discovered when polling
     * the configured NrfGroup for discovery.
     * 
     * @return A Flowable that emits a list of all discovered PcfRts.
     */
    public Flowable<List<PcfRt>> getDiscoveredPcfRts()
    {
        return this.pcfNfDiscoverer.getOutput() //
                                   .toFlowable(BackpressureStrategy.BUFFER)
                                   .compose(toPcfRtTransformer(PCF_GROUP, this.targetedPcfQueries));
    }

    public Completable start()
    {
        return this.pcfNfDiscoverer.start();
    }

    public Completable stop()
    {
        return this.pcfNfDiscoverer.stop();
    }
}
