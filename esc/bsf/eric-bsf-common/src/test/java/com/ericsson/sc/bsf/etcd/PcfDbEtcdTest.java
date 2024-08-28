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

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.observers.BaseTestConsumer.TestWaitStrategy;

public class PcfDbEtcdTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfDbEtcdTest.class);

    private static final String PREFIX = PcfDbSerializer.BSF_PREFIX;
    private static final Long TTL = 2592000L;

    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;
    private PcfDbEtcd db;

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(PREFIX, true);
        etcdTestBed.start();
        rxEtcd = etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        rxEtcd.ready().blockingAwait(); // Ensure DB is up
        db = new PcfDbEtcd(rxEtcd, true, TTL);

    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }

    /**
     * Test the watcher start() and stop() methods
     */
    @Test(groups = "functest")
    public void watchStartTest()
    {
        final var watcher = db.newWatcher();

        final var pcfStream = watcher.pcfNfBySetId();
        final var pcfStreamTest = pcfStream.test();

        watcher.initialize().blockingAwait();
        pcfStreamTest.assertValue(Map.of());
        watcher.terminate().blockingAwait();

    }

    @Test(groups = "functest")
    public void watcherFailureRecoverTest() throws JsonProcessingException
    {

        final var pcfCount = 10;
        final var pcfs = createTestData(pcfCount);
        final var watcher = db.newWatcher();
        final var pcfStream = watcher.pcfNfBySetId().doOnNext(next -> log.debug("next {}", next));
        final var pcfStreamTest = pcfStream.test();
        db.createOrUpdate(pcfs).blockingAwait();

        watcher.initialize().blockingAwait();
        log.info("Started database watcher");

        log.info("Waiting watcher");
        pcfStreamTest.awaitCount(1);
        pcfStreamTest.assertNotTerminated();

        log.info("Watcher received all expected events");
        final var watchResult = pcfStreamTest.values().get(0);

        assertEquals(watchResult.keySet().size(), pcfCount);

        for (int i = 0; i < pcfCount; i++)
            assertEquals(watchResult.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()), Set.copyOf(pcfs.subList(i, pcfCount)));

        log.info("Restarting etcd database");
        this.etcdTestBed.restartEtcdServer(0);

        log.info("Waiting watcher");
        pcfStreamTest.awaitCount(1);
        pcfStreamTest.assertNotTerminated();

        final var watchResult2 = pcfStreamTest.values().get(0);
        for (int i = 0; i < pcfCount; i++)
            assertEquals(watchResult2.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()), Set.copyOf(pcfs.subList(i, pcfCount)));

        final var deleted = this.db.delete(List.of(pcfs.get(0).getNfInstanceId())).blockingGet();
        assertEquals(deleted, List.of(1l));
        log.info("Waiting watcher");
        pcfStreamTest.awaitCount(1);
        final var watchResult3 = pcfStreamTest.values().get(0);
        for (int i = 1; i < pcfCount; i++)
            assertEquals(watchResult3.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()), Set.copyOf(pcfs.subList(i, pcfCount)));

        pcfStreamTest.dispose();
        watcher.terminate().blockingAwait();
    }

    @Test(groups = "functest")
    public void dbWatchTest()
    {
        int awaitCount = 0;
        final var watcher = db.newWatcher();
        final var pcfStream = watcher.pcfNfBySetId().doOnNext(st -> log.info("New state {}", st));
        final var pcfStreamTest = pcfStream.test();
        watcher.initialize().blockingAwait();
        log.info("Started database watcher");

        try
        {
            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount);
            assertEquals(pcfStreamTest.values().get(awaitCount - 1).size(), 0);

            final var pcfCount = 30;
            final var pcfs = createTestData(pcfCount);
            db.createOrUpdate(pcfs).doOnSubscribe(dsp -> log.info("updating db")).blockingAwait();
            log.info("All entries written to database");

            log.info("retrieving all stored keys from database");
            final var storedKeys = db.getPcfKeys().blockingGet();
            log.info("retrieived all stored keys from database");
            assertEquals(storedKeys.collect(Collectors.toSet()), pcfs.stream().map(PcfNf::getNfInstanceId).collect(Collectors.toSet()));

            log.info("retrieving all stored objects from database");
            final var retrievedPcfs = db.getPcfs().blockingGet().getEntries().stream().map(EtcdEntry::getValue).collect(Collectors.toSet());

            log.info("Retrieved all stored objects from database");
            assertEquals(retrievedPcfs, Set.copyOf(pcfs));

            awaitCount += pcfCount;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, pcfCount * 2 * 1000);
            log.info("Watcher received all expected events");

            final var watchResult = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchResult.keySet().size(), pcfCount);
            for (int i = 0; i < pcfCount; i++)
                assertEquals(watchResult.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()),
                             Set.copyOf(pcfs.subList(i, pcfCount)));

            // Delete a single PCF
            final var deletedId = pcfs.get(0).getNfInstanceId();
            log.info("Deleting single pcf {}", deletedId);
            final var deleteResult = db.delete(List.of(deletedId)).blockingGet();
            assertEquals(deleteResult, List.of(1l));

            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, pcfCount * 2 * 1000);
            log.info("Watcher received all expected events");

            log.info("Valuecount: {}", pcfStreamTest.valueCount());
            final var watchDeleteResult = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchDeleteResult.get("pcfSetId" + 0).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()),
                         Set.copyOf(pcfs.subList(1, pcfCount)));

            for (int i = 1; i < pcfCount; i++)
                assertEquals(watchResult.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()),
                             Set.copyOf(pcfs.subList(i, pcfCount)));

            // todo delete all
        }
        finally
        {
            pcfStreamTest.dispose();
            watcher.terminate().blockingAwait();
            log.info("Watcher terminating");
        }
    }

    @Test(groups = "functest")
    public void watchOverwriteTest()
    {
        final var pcfCount = 20;

        int awaitCount = 0;
        final var watcher = db.newWatcher();
        final var pcfStream = watcher.pcfNfBySetId().doOnNext(st -> log.info("New state {}", st));
        final var pcfStreamTest = pcfStream.test();
        watcher.initialize().blockingAwait();
        log.info("Started database watcher");
        try
        {
            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount);
            assertEquals(pcfStreamTest.values().get(awaitCount - 1).size(), 0);

            final var pcfs = createTestData(pcfCount);
            db.createOrUpdate(pcfs).doOnSubscribe(dsp -> log.info("updating db")).blockingAwait();
            log.info("All entries written to database");

            log.info("retrieving all stored keys from database");
            final var storedKeys = db.getPcfKeys().blockingGet();
            log.info("retrieived all stored keys from database");
            assertEquals(storedKeys.collect(Collectors.toSet()), pcfs.stream().map(pcf -> pcf.getNfInstanceId()).collect(Collectors.toSet()));

            log.info("retrieving all stored objects from database");
            final var retrievedPcfs = db.getPcfs().blockingGet().getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toSet());
            log.info("retrieived all stored objects from database");
            assertEquals(retrievedPcfs, Set.copyOf(pcfs));

            awaitCount += pcfCount;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, pcfCount * 2 * 1000);
            log.info("Watcher received all expected events");

            log.info("event count {}", pcfStreamTest.values().size());
            final var watchResult = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchResult.keySet().size(), pcfCount);
            for (int i = 0; i < pcfCount; i++)
                assertEquals(watchResult.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()),
                             Set.copyOf(pcfs.subList(i, pcfCount)));

            // Modfy all previously inserted PcfNf objects
            final var modifiedPcfs = this.changePcfData("xxxxx", pcfs);
            this.db.createOrUpdate(modifiedPcfs).blockingAwait();

            awaitCount += pcfCount;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, pcfCount * 2 * 1000);
            log.info("Watcher received all expected events");

            final var watchResult2 = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchResult2.keySet().size(), pcfCount);
            for (int i = 0; i < pcfCount; i++)
                assertEquals(watchResult2.get("pcfSetId" + i).stream().map(PcfNfRecord::pcfNf).collect(Collectors.toSet()),
                             Set.copyOf(modifiedPcfs.subList(i, pcfCount)));

        }
        finally
        {
            pcfStreamTest.dispose();
            watcher.terminate().blockingAwait();
            log.info("Watcher terminating");
        }
    }

    @Test(groups = "functest")
    public void dbDuplicatesTest()
    {
        int awaitCount = 0;
        final var watcher = db.newWatcher();
        final var pcfStream = watcher.bsfDbView();
        final var pcfStreamTest = pcfStream.test();
        watcher.initialize().blockingAwait();
        log.info("Started database watcher");

        try
        {
            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount);
            assertEquals(pcfStreamTest.values().get(awaitCount - 1).getPcfsBySetId("pcfSetId1").size(), 0);

            final var pcfSetIdCommonBuilder = PcfNf.newBuilder()
                                                   .withNfInstanceId(UUID.randomUUID())
                                                   .withNfStatus(NFStatus.REGISTERED)
                                                   .withRxDiamHost("diamHost0")
                                                   .withRxDiamRealm("diamRealm0")
                                                   .withNfSetIdList(List.of("pcfSetId1"));

            final var pcf1 = pcfSetIdCommonBuilder.withNfInstanceId(UUID.randomUUID()).build();

            final var pcf2 = pcfSetIdCommonBuilder.withNfInstanceId(UUID.randomUUID()).build(); // Duplicate with pcf1 - same PCF set

            final var pcfsInPcfSet1 = new ArrayList<PcfNf>(List.of(pcf1, pcf2));

            db.createOrUpdate(pcfsInPcfSet1).doOnSubscribe(dsp -> log.info("updating db")).blockingAwait();

            final var pcf3 = createTestData(1); // Duplicate with pcf1/pcf2 - different PCF set
            db.createOrUpdate(pcf3).doOnSubscribe(dsp -> log.info("updating db")).blockingAwait();

            log.info("All entries written to database");

            log.info("Retrieving all stored keys from database");
            final var storedKeys = db.getPcfKeys().blockingGet();
            log.info("Retrieived all stored keys from database");

            final var allPcfs = pcfsInPcfSet1;
            allPcfs.addAll(pcf3); // list containing all PCFs

            assertEquals(storedKeys.collect(Collectors.toSet()), allPcfs.stream().map(PcfNf::getNfInstanceId).collect(Collectors.toSet()));

            log.info("Retrieving all stored objects from database");
            final var retrievedPcfs = db.getPcfs().blockingGet().getEntries().stream().map(EtcdEntry::getValue).collect(Collectors.toSet());

            log.info("Retrieved all stored objects from database");
            assertEquals(retrievedPcfs, Set.copyOf(allPcfs));

            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, 2 * 1000);
            log.info("Watcher received all expected events");

            final var watchResult = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchResult.getPcfsBySetId("pcfSetId1").size(), 1);

            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, 2 * 1000);
            log.info("Watcher received all expected events");

            final var watchResult2 = pcfStreamTest.values().get(awaitCount - 1);
            assertEquals(watchResult2.getPcfsBySetId("pcfSetId1").size(), 1);

            final var retrievedPcfSamePcfSet = watchResult2.getPcfsBySetId("pcfSetId1").stream().findFirst().get();
            assertEquals(retrievedPcfSamePcfSet, pcf2);

            awaitCount += 1;
            log.info("Waiting for {} events", awaitCount);
            pcfStreamTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, 2 * 1000);
            log.info("Watcher received all expected events");

            final var watchResult3 = pcfStreamTest.values().get(awaitCount - 1);

            assertEquals(watchResult3.getPcfsBySetId("pcfSetId0").size(), 1);
            assertEquals(watchResult3.getPcfsBySetId("pcfSetId1").size(), 0);

            final var retrievedPcfDifferentPcfSets = watchResult3.findPcfsInSet("diamHost0", "diamRealm0", "pcfSetId1").stream().findFirst().get();

            assertEquals(retrievedPcfDifferentPcfSets, pcf3.get(0));

            final var retrievedPcfDifferentPcfNoPcfSet = watchResult3.findPcfsInSet("diamHost0", "diamRealm0", null).stream().findFirst().get();

            assertEquals(retrievedPcfDifferentPcfNoPcfSet, pcf3.get(0));

            // Delete all PCFs
            final var deletedIds = allPcfs.stream().map(PcfNf::getNfInstanceId).toList();
            log.info("Deleting PCFs {}", deletedIds);

            final var deleteResult = db.delete(deletedIds).blockingGet();
            assertEquals(deleteResult, List.of(1l, 1l, 1l));
        }
        finally
        {
            pcfStreamTest.dispose();
            watcher.terminate().blockingAwait();
            log.info("Watcher terminating");
        }
    }

    @AfterMethod
    public void cleanupDb()
    {
        this.etcdTestBed.clearKeyspace();
    }

    private List<PcfNf> changePcfData(String prefix,
                                      Collection<PcfNf> pcfData)
    {
        return pcfData.stream().map(pcf -> PcfNf.newBuilder(pcf).withRxDiamHost(prefix + "." + pcf.getRxDiamHost()).build()).collect(Collectors.toList());
    }

    private List<PcfNf> createTestData(int count)
    {
        return IntStream.range(0, count)
                        .mapToObj(i -> PcfNf.newBuilder()
                                            .withNfInstanceId(UUID.randomUUID())
                                            .withNfStatus(NFStatus.REGISTERED)
                                            .withRxDiamHost("diamHost" + i)
                                            .withRxDiamRealm("diamRealm" + i)
                                            .withNfSetIdList(IntStream.range(0, i + 1).mapToObj(j -> "pcfSetId" + j).collect(Collectors.toList()))
                                            .build())
                        .collect(Collectors.toList());
    }
}
