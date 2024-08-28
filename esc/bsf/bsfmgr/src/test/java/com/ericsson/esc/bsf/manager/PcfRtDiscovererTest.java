package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertTrue;

import java.time.OffsetDateTime;
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
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer.SearchingContext;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.BehaviorSubject;

public class PcfRtDiscovererTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtDiscovererTest.class);

    @Test
    public void checkToPcfRtSuccess()
    {
        // Create NFProfile that is valid for PcfRt.
        var nfInstanceId = UUID.randomUUID();
        var nfStatus = NFStatus.REGISTERED;
        NFProfile nfProfile = new NFProfile();
        var date = "1985-04-13T23:50:50.52Z";
        var recoveryTime = OffsetDateTime.parse(date);
        nfProfile.nfInstanceId(nfInstanceId).nfStatus(nfStatus).recoveryTime(recoveryTime);
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfRt. Expecting successful creation of the PcfRt.
        PcfRt pcfRt = PcfDiscoverer.toPcfRt(nfProfile).get();
        assertTrue(pcfRt.getId().equals(nfInstanceId), "PcfRt has wrong NfInstanceId");
        log.debug("Output PcfRt: {}", pcfRt);
    }

    @Test
    public void checkToPcfRtEmptyNFProfile()
    {
        // Create empty NFProfile.
        NFProfile nfProfile = new NFProfile();
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfRt. Expecting empty Optional
        var pcfRt = PcfDiscoverer.toPcfRt(nfProfile);
        assertTrue(pcfRt.isEmpty());
    }

    @Test
    public void checkToPcfRtNFProfileWithNoRt()
    {
        // Create NFProfile with no recoveryTime.
        NFProfile nfProfile = new NFProfile().nfInstanceId(UUID.randomUUID());
        log.debug("Input NFProfile: {}", nfProfile);

        // Transform to PcfRt. Expecting empty Optional
        var pcfRt = PcfDiscoverer.toPcfRt(nfProfile);
        assertTrue(pcfRt.isEmpty());
    }

    @Test
    public void checkToPcfRtListSuccess()
    {
        // Create NFProfile list that contains only profiles that are valid for
        // transformation to PcfRt.
        List<UUID> uuids = IntStream.range(0, 4) //
                                    .mapToObj(i -> UUID.randomUUID())
                                    .collect(Collectors.toList());
        var nfProfileList = uuids.stream() //
                                 .map(uuid -> baseNfProfile(uuid))
                                 .collect(Collectors.toList());
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfRt list.
        var pcfRtList = PcfDiscoverer.toPcfRtList(nfProfileList);
        log.debug("Output PcfRt list: {}", pcfRtList);

        // Assertions. Expecting all NFProfiles to be kept as PcfRts.
        assertTrue(pcfRtList.size() == 4, "The list contains wrong number of PcfRts");
        for (PcfRt pcfRt : pcfRtList)
        {
            assertTrue(uuids.contains(pcfRt.getId()), "Unexpected nfInstanceId for a PcfRt");
        }
    }

    @Test
    public void checkToPcfRtListNFProfileListWithWrongFields()
    {
        // Create NfProfile list with three profiles that are valid for transformation
        // to PcfRt and two profiles that are not.
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
                                                .map(uuid -> baseNfProfile(uuid).recoveryTime(null))
                                                .collect(Collectors.toList());
        var nfProfileList = nfProfilesToBeKept;
        nfProfileList.addAll(nfProfilesToBeDropped);
        log.debug("Input NFProfile list: {}", nfProfileList);

        // Transform to PcfRt list.
        var pcfRtList = PcfDiscoverer.toPcfRtList(nfProfileList);
        log.debug("Output PcfRt list: {}", pcfRtList);

        // Assertions. Expecting only three NFProfiles to be kept.
        assertTrue(pcfRtList.size() == 3, "The list contains wrong number of PcfRts");
        for (PcfRt pcfRt : pcfRtList)
        {
            assertTrue(uuidsKept.contains(pcfRt.getId()), "Unexpected nfInstanceId for a PcfRt");
        }
    }

    @Test
    public void checkToPcfRtTransformerEmptyOutput()
    {
        SearchingContext mockSearchingContext = Mockito.mock(SearchingContext.class);
        var pcfPollGroup = "pcfPollGroup";
        var pcfQuery = new Query.Builder().build();
        var targetedPcfQuery = SearchingContext.TargetedQuery.of(pcfQuery, null);

        // The input of the transformation chain, which emits SearchingContext objects.
        BehaviorSubject<SearchingContext> input = BehaviorSubject.createDefault(SearchingContext.empty());

        // The output of the transformation chain, which emits list of PcfRts.
        var output = input.toFlowable(BackpressureStrategy.BUFFER)
                          .compose(PcfDiscoverer.toPcfRtTransformer(pcfPollGroup, new AtomicReference<>(Set.of(targetedPcfQuery))));

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

    @Test
    public void checkToPcfRtTransformerPollingGroups()
    {
        SearchingContext mockSearchingContext = Mockito.mock(SearchingContext.class);
        var pcfPollGroup = "pcfPollGroup";
        var otherPollGroup = "otherPollGroup";
        var pcfQuery = new Query.Builder().build();
        var targetedPcfQuery = SearchingContext.TargetedQuery.of(pcfQuery, null);

        // The input of the transformation chain, which emits SearchingContext objects.
        BehaviorSubject<SearchingContext> input = BehaviorSubject.createDefault(SearchingContext.empty());

        // The output of the transformation chain, which emits list of PcfRts.
        var output = input.toFlowable(BackpressureStrategy.BUFFER)
                          .compose(PcfDiscoverer.toPcfRtTransformer(pcfPollGroup, new AtomicReference<>(Set.of(targetedPcfQuery))));

        // Get the TestObserver.
        var testObs = output.doOnNext(x -> log.debug("Output item: {}", x)).test();

        // Create an outputItem that contains only the other polling Group, which should
        // be ignored.
        var ignoredNfInstanceIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var ignoredNfProfileList = List.of(baseNfProfile(ignoredNfInstanceIds.get(0)), baseNfProfile(ignoredNfInstanceIds.get(1)));
        var ignoredResultMapInput = Map.of(targetedPcfQuery, ignoredNfProfileList);
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
        // 1: Expecting a list with only two PcfRts from the target PollingGroup. The
        // other PollingGroup must be ignored. The PcfRts must match the kept
        // nfInstanceIds and not match the ignored nfInstanceIds.
        // 2: Expecting empty list since the target PollingGroup has empty SearchResult.
        testObs.awaitCount(3) //
               .assertValueAt(0, list -> list.isEmpty())
               .assertValueAt(1, list -> list.size() == 2)
               .assertValueAt(1, list -> list.stream().noneMatch(profile -> ignoredNfInstanceIds.contains(profile.getId())))
               .assertValueAt(1, list -> list.stream().allMatch(profile -> keptNfInstanceIds.contains(profile.getId())))
               .assertValueAt(2, list -> list.isEmpty());

        // Dispose the chain.
        testObs.dispose();
    }

    private static NFProfile baseNfProfile(UUID nfInstanceId)
    {
        return new NFProfile().nfInstanceId(nfInstanceId)
                              .nfType(NFType.PCF)
                              .nfStatus(NFStatus.REGISTERED)
                              .recoveryTime(OffsetDateTime.parse("2005-04-13T23:50:50.52Z"));
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
