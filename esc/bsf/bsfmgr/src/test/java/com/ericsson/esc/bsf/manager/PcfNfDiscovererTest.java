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
 * Created on: Apr 15, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.PcfInfo;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.BehaviorSubject;

public class PcfNfDiscovererTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfNfDiscovererTest.class);

    @Test
    public void checkToPcfNfSuccess()
    {
        // Create NFProfile that is valid for PcfNf.
        var nfInstanceId = UUID.randomUUID();
        var nfStatus = NFStatus.REGISTERED;
        var nfSetIdList = List.of("pcfSet1");
        var rxDiamHost = "rxDiamHost";
        var rxDiamRealm = "rxDiamRealm";
        var pcfInfo = new PcfInfo().rxDiamHost(rxDiamHost).rxDiamRealm(rxDiamRealm);
        NFProfile nfProfile = new NFProfile();
        nfProfile.nfInstanceId(nfInstanceId).nfStatus(nfStatus).nfSetIdList(nfSetIdList).pcfInfo(pcfInfo);
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfNf. Expecting successful creation of the PcfNf.
        PcfNf pcfNf = PcfDiscoverer.toPcfNf(nfProfile);
        assertTrue(pcfNf.getNfInstanceId().equals(nfInstanceId), "PcfNf has wrong NfInstanceId");
        assertTrue(pcfNf.getNfStatus().equals(nfStatus), "PcfNf has wrong NfStatus");
        assertTrue(pcfNf.getNfSetIdList().equals(nfSetIdList), "PcfNf has wrong NfSetIdList");
        assertTrue(pcfNf.getRxDiamHost().equals(rxDiamHost), "PcfNf has wrong RxDiamHost");
        assertTrue(pcfNf.getRxDiamRealm().equals(rxDiamRealm), "PcfNf has wrong RxDiamRealm");
        log.debug("Output PcfNf: {}", pcfNf);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void checkToPcfNfEmptyNFProfile()
    {
        // Create empty NFProfile.
        NFProfile nfProfile = new NFProfile();
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfNf. Expecting exception due to nfInstanceID and nfStatus
        // missing.
        PcfDiscoverer.toPcfNf(nfProfile);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void checkToPcfNfNFProfileWithNoRxDiam()
    {
        // Create NFProfile with no PcfInfo.
        NFProfile nfProfile = new NFProfile();
        nfProfile.nfInstanceId(UUID.randomUUID()).nfStatus(NFStatus.REGISTERED).pcfInfo(new PcfInfo());
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfNf. Expecting exception due to rxDiamHost and rxDiamRealm
        // missing.
        PcfDiscoverer.toPcfNf(nfProfile);
    }

    @Test
    public void checkToPcfNfListSuccess()
    {
        // Create NFProfile list that contains only profiles that are valid for
        // transformation to PcfNf.
        List<UUID> uuids = IntStream.range(0, 4) //
                                    .mapToObj(i -> UUID.randomUUID())
                                    .collect(Collectors.toList());
        var nfProfileList = uuids.stream() //
                                 .map(uuid -> baseNfProfile(uuid))
                                 .collect(Collectors.toList());
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfNf list.
        var pcfNfList = PcfDiscoverer.toPcfNfList(nfProfileList);
        log.debug("Output PcfNf list: {}", pcfNfList);

        // Assertions. Expecting all NFProfiles to be kept as PcfNfs.
        assertTrue(pcfNfList.size() == 4, "The list contains wrong number of PcfNfs");
        for (PcfNf pcfNf : pcfNfList)
        {
            assertTrue(uuids.contains(pcfNf.getNfInstanceId()), "Unexpected nfInstanceId for a PcfNF");
        }
    }

    @Test
    public void checkToPcfNfListEmptyNFProfileList()
    {
        // Create empty NFProfile list.
        var nfProfileList = List.<NFProfile>of();
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfNf list.
        var pcfNfList = PcfDiscoverer.toPcfNfList(nfProfileList);
        log.debug("Output PcfNf list: {}", pcfNfList);

        // Assertions. Expecting empty PcfNf list.
        assertTrue(pcfNfList.isEmpty(), "The PcfNf list is not empty");
    }

    @Test
    public void checkToPcfNfListNFProfileListWithWrongNFType()
    {
        // Create NFProfile list with PCF and AMF.
        var amfNfInstanceId = UUID.randomUUID();
        var pcfNfInstanceId = UUID.randomUUID();
        var amfNfProfile = baseNfProfile(amfNfInstanceId).nfType(NFType.AMF);
        var pcfNfProfile = baseNfProfile(pcfNfInstanceId);
        var nfProfileList = List.of(amfNfProfile, pcfNfProfile);
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfNf list.
        var pcfNfList = PcfDiscoverer.toPcfNfList(nfProfileList);
        log.debug("Output PcfNf list: {}", pcfNfList);

        // Assertions. Expecting that only the PCF NFProfile will be kept.
        assertTrue(pcfNfList.size() == 1, "The PcfNf list does not contain only one profile");
        assertTrue(pcfNfList.get(0).getNfInstanceId().equals(pcfNfInstanceId), "Wrong nfInstanceID in the NFProfile");
    }

    @Test
    public void checkToPcfNfListNFProfileListWithWrongFields()
    {
        // Create NfProfile list with three profiles that are valid for transformation
        // to PcfNf and two profiles that are not.
        List<UUID> uuidsKept = IntStream.range(0, 3) //
                                        .mapToObj(i -> UUID.randomUUID())
                                        .collect(Collectors.toList());
        List<UUID> uuidsDropped = IntStream.range(0, 2) //
                                           .mapToObj(i -> UUID.randomUUID())
                                           .collect(Collectors.toList());
        var nfProfilesToBeKept = uuidsKept.stream() //
                                          .map(uuid -> baseNfProfile(uuid))
                                          .collect(Collectors.toList());
        var nfProfilesToBeDropped = uuidsDropped.stream() //
                                                .map(uuid -> baseNfProfile(uuid).pcfInfo(new PcfInfo()))
                                                .collect(Collectors.toList());
        var nfProfileList = nfProfilesToBeKept;
        nfProfileList.addAll(nfProfilesToBeDropped);
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfNf list.
        var pcfNfList = PcfDiscoverer.toPcfNfList(nfProfileList);
        log.debug("Output PcfNf list: {}", pcfNfList);

        // Assertions. Expecting only three NFProfiles to be kept.
        assertTrue(pcfNfList.size() == 3, "The list contains wrong number of PcfNfs");
        for (PcfNf pcfNf : pcfNfList)
        {
            assertTrue(uuidsKept.contains(pcfNf.getNfInstanceId()), "Unexpected nfInstanceId for a PcfNF");
        }
    }

    /**
     * Checks that empty Searching context output data is ignored.
     */
    @Test
    public void checkToPcfNfTransformerEmptyOutput()
    {
        SearchingContext mockSearchingContext = Mockito.mock(SearchingContext.class);
        var pcfPollGroup = "pcfPollGroup";
        var pcfQuery = new Query.Builder().build();
        var targetedPcfQuery = SearchingContext.TargetedQuery.of(pcfQuery, null);

        // The input of the transformation chain, which emits SearchingContext objects.
        BehaviorSubject<SearchingContext> input = BehaviorSubject.createDefault(SearchingContext.empty());

        // The output of the transformation chain, which emits list of PcfNfs.
        var output = input.toFlowable(BackpressureStrategy.BUFFER)
                          .compose(PcfDiscoverer.toPcfNfTransformer(pcfPollGroup, new AtomicReference<>(Set.of(targetedPcfQuery))));

        // Get the TestObserver.
        var testObs = output.doOnNext(x -> log.debug("Output item: {}", x)).test();

        log.debug("SearchingContext output item to be emitted:\n{}", Optional.empty());

        // Manipulate the output of the SearchingContext via mocking. Return Optional
        // empty.
        Mockito.when(mockSearchingContext.getData()).thenReturn(Optional.empty());
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Assertions.
        testObs.assertNoValues();

        // Dispose the chain.
        testObs.dispose();
    }

    /**
     * Checks that the other PollingGroups are ignored, that the pcfNfs of the
     * target PollingGroup are returned, and that an empty target PollingGroup
     * returns an empty PcfNf list.
     */
    @Test
    public void checkToPcfNfTransformerPollingGroups()
    {
        SearchingContext mockSearchingContext = Mockito.mock(SearchingContext.class);
        var pcfPollGroup = "pcfPollGroup";
        var otherPollGroup = "otherPollGroup";
        var pcfQuery = new Query.Builder().build();
        var targetedPcfQuery = SearchingContext.TargetedQuery.of(pcfQuery, null);

        // The input of the transformation chain, which emits SearchingContext objects.
        BehaviorSubject<SearchingContext> input = BehaviorSubject.createDefault(SearchingContext.empty());

        // The output of the transformation chain, which emits list of PcfNfs.
        var output = input.toFlowable(BackpressureStrategy.BUFFER)
                          .compose(PcfDiscoverer.toPcfNfTransformer(pcfPollGroup, new AtomicReference<>(Set.of(targetedPcfQuery))));

        // Get the TestObserver.
        var testObs = output.doOnNext(x -> log.debug("Output item: {}", x)).test();

        // Create an outputItem that contains only the other polling Group, which should
        // be ignored.
        var ignoredNfInstanceIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var ignoredNfProfileList = List.of(baseNfProfile(ignoredNfInstanceIds.get(0)), baseNfProfile(ignoredNfInstanceIds.get(1)));
        var ignoredResultMapInput = Map.of(SearchingContext.TargetedQuery.of(pcfQuery, null), ignoredNfProfileList);
        var ignoredResultMap = createResultMap(ignoredResultMapInput);
        var outputItem = Optional.of(Map.of(otherPollGroup, ignoredResultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Create an outputItem that contains the target polling Group and another one,
        // which should be ignored.
        var keptNfInstanceIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var keptNfProfileList = List.of(baseNfProfile(keptNfInstanceIds.get(0)), baseNfProfile(keptNfInstanceIds.get(1)));
        var resultMapInput = Map.of(targetedPcfQuery, keptNfProfileList);
        var resultMap = createResultMap(resultMapInput);
        outputItem = Optional.of(Map.of(otherPollGroup, ignoredResultMap, pcfPollGroup, resultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Create an outputItem that contains the target polling Group with empty
        // results.
        resultMapInput = Map.of();
        resultMap = createResultMap(resultMapInput);
        outputItem = Optional.of(Map.of(pcfPollGroup, resultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Assertions.
        // 0: Expecting empty list since only the other PollingGroup exists in the
        // output item.
        // 1: Expecting a list with only two PcfNfs from the target PollingGroup. The
        // other PollingGroup must be ignored. The PcfNfs must match the kept
        // nfInstanceIds and not match the ignored nfInstanceIds.
        // 2: Expecting empty list since the target PollingGroup has empty SearchResult.
        testObs.awaitCount(3) //
               .assertValueAt(0, list -> list.isEmpty())
               .assertValueAt(1, list -> list.size() == 2)
               .assertValueAt(1, list -> list.stream().noneMatch(profile -> ignoredNfInstanceIds.contains(profile.getNfInstanceId())))
               .assertValueAt(1, list -> list.stream().allMatch(profile -> keptNfInstanceIds.contains(profile.getNfInstanceId())))
               .assertValueAt(2, list -> list.isEmpty());

        // Dispose the chain.
        testObs.dispose();
    }

    /**
     * Checks that the other PollingQueries are ignored, that the pcfNfs of the
     * target PollingQueries are returned, and that an empty target PollingQuery
     * returns an empty PcfNf list.
     */
    @Test
    public void checkToPcfNfTransformerPollingQueries()
    {
        SearchingContext mockSearchingContext = Mockito.mock(SearchingContext.class);
        var pcfPollGroup = "pcfPollGroup";
        var pcfQuery = new Query.Builder().add("target-nf-type", NFType.PCF).build();
        var otherQuery = new Query.Builder().add("target-nf-type", NFType.AMF).build();
        var targetedPcfQuery = SearchingContext.TargetedQuery.of(pcfQuery, null);
        var targetedOtherQuery = SearchingContext.TargetedQuery.of(otherQuery, null);

        // The input of the transformation chain, which emits SearchingContext objects.
        BehaviorSubject<SearchingContext> input = BehaviorSubject.createDefault(SearchingContext.empty());

        // The output of the transformation chain, which emits list of PcfNfs.
        var output = input.toFlowable(BackpressureStrategy.BUFFER)
                          .compose(PcfDiscoverer.toPcfNfTransformer(pcfPollGroup, new AtomicReference<>(Set.of(targetedPcfQuery))));

        // Get the TestObserver.
        var testObs = output.doOnNext(x -> log.debug("Output item: {}", x)).test();

        // Create an outputItem that contains only the other polling query, which should
        // be ignored.
        var ignoredNfInstanceIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var ignoredNfProfileList = List.of(baseNfProfile(ignoredNfInstanceIds.get(0)), baseNfProfile(ignoredNfInstanceIds.get(1)));
        var ignoredResultMapInput = Map.of(targetedOtherQuery, ignoredNfProfileList);
        var resultMap = createResultMap(ignoredResultMapInput);
        var outputItem = Optional.of(Map.of(pcfPollGroup, resultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Create an outputItem that contains the target polling query and another one,
        // which should be ignored.
        var keptNfInstanceIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var keptNfProfileList = List.of(baseNfProfile(keptNfInstanceIds.get(0)), baseNfProfile(keptNfInstanceIds.get(1)));
        var resultMapInput = Map.of(targetedPcfQuery, keptNfProfileList, targetedOtherQuery, ignoredNfProfileList);
        resultMap = createResultMap(resultMapInput);
        outputItem = Optional.of(Map.of(pcfPollGroup, resultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Create an outputItem that contains the target polling query with empty
        // results.
        resultMapInput = Map.of(targetedPcfQuery, List.of());
        resultMap = createResultMap(resultMapInput);
        outputItem = Optional.of(Map.of(pcfPollGroup, resultMap));
        log.debug("SearchingContext output item to be emitted:\n{}", outputItem);

        // Manipulate the output of the SearchingContext via mocking.
        Mockito.when(mockSearchingContext.getData()).thenReturn(outputItem);
        // Emit the item.
        input.onNext(mockSearchingContext);

        // Assertions.
        // 0: Expecting empty list since only the other PollingQuery exists in the
        // output item.
        // 1: Expecting a list with only two PcfNfs from the target PollingQuery. The
        // other PollingQuery must be ignored. The PcfNfs must match the kept
        // nfInstanceIds and not match the ignored nfInstanceIds.
        // 2: Expecting empty list since the target PollingQuery has empty SearchResult.
        testObs.awaitCount(2) //
               .assertValueAt(0, list -> list.isEmpty())
               .assertValueAt(1, list -> list.size() == 2)
               .assertValueAt(1, list -> list.stream().noneMatch(profile -> ignoredNfInstanceIds.contains(profile.getNfInstanceId())))
               .assertValueAt(1, list -> list.stream().allMatch(profile -> keptNfInstanceIds.contains(profile.getNfInstanceId())))
               .assertValueAt(2, list -> list.isEmpty());

        // Dispose the chain.
        testObs.dispose();
    }

    /**
     * Returns a base NFProfile with the provided nfInstanceId, nfStatus registered,
     * nfType PCF, one NfSetId and pcfInfo with rxDiamHost and rxDiamRealm. String
     * fields contain the first 3 characters of the nfInstanceId for uniqueness.
     * 
     * @param nfInstanceId A UUID.
     * @return A base NFProfile.
     */
    private static NFProfile baseNfProfile(UUID nfInstanceId)
    {
        String uniqueStr = nfInstanceId.toString().substring(0, 3);
        return new NFProfile().nfInstanceId(nfInstanceId)
                              .nfType(NFType.PCF)
                              .nfStatus(NFStatus.REGISTERED)
                              .nfSetIdList(List.of("pcfSet" + uniqueStr))
                              .pcfInfo(new PcfInfo().rxDiamHost("rxDiamHost" + uniqueStr) //
                                                    .rxDiamRealm("rxDiamRealm" + uniqueStr));
    }

    /**
     * Utility method to create a Map of PollingQueries and SearchResults from a Map
     * of PollingQueries and list of NFProfiles.
     * 
     * @param resultMapInput Entries of Queries and lists of NFProfiles.
     * @return A map of PollingQueries and SearchResults.
     */
    private static Map<SearchingContext.TargetedQuery, SearchResult> createResultMap(Map<SearchingContext.TargetedQuery, List<NFProfile>> resultMapInput)
    {
        return resultMapInput.entrySet() //
                             .stream()
                             .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                             {
                                 var searchResult = new SearchResult();
                                 // FIXME copy immutable list, workaround for setNfINstaces() modifying its
                                 // arugments
                                 searchResult.setNfInstances(new ArrayList<>(entry.getValue()));
                                 return searchResult;
                             }));
    }
}
