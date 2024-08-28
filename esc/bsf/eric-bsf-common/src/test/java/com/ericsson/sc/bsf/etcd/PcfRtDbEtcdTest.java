package com.ericsson.sc.bsf.etcd;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.bsf.etcd.PcfRtDbEtcd.State;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.etcd.jetcd.options.LeaseOption;
import io.reactivex.observers.BaseTestConsumer.TestWaitStrategy;

public class PcfRtDbEtcdTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtDbEtcdTest.class);

    private static final String PREFIX = PcfRtDbSerializer.PCFRT_PREFIX;

    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;
    private PcfRtDbEtcd db;
    private final long ttl = 10000;

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(PREFIX, true);
        etcdTestBed.start();
        rxEtcd = etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        rxEtcd.ready().blockingAwait(); // Ensure DB is up
        db = new PcfRtDbEtcd(rxEtcd, ttl);
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }

    @Test(groups = "functest")
    public void createOrUpdateTxnTest()
    {
        final int totalEntries = 10;

        final var initPcfRts = initData(totalEntries);
        final var updatePcfRts = updateData(initPcfRts);

        log.info("omggg {} {}", initPcfRts, updatePcfRts);
        final var newerCount = IntStream.range(0, initPcfRts.size())
                                        .filter(i -> initPcfRts.get(i).getRecoverytime() < updatePcfRts.get(i).getRecoverytime())
                                        .count();

        final var olderCount = IntStream.range(0, initPcfRts.size())
                                        .filter(i -> initPcfRts.get(i).getRecoverytime() > updatePcfRts.get(i).getRecoverytime())
                                        .count();

        final var equalCount = IntStream.range(0, initPcfRts.size())
                                        .filter(i -> initPcfRts.get(i).getRecoverytime() == updatePcfRts.get(i).getRecoverytime())
                                        .count();

        log.info("Storing {} PcfRt objects in etcd db", totalEntries);
        final var createResult = initPcfRts.stream()
                                           .map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet())
                                           .filter(state -> state.equals(State.OK))
                                           .collect(Collectors.toList());
        assertEquals(createResult.size(), initPcfRts.size());

        log.info("Updating stored entries");
        final var updateResult = updatePcfRts.stream().map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet()).collect(Collectors.toList());

        final var okUpdates = Collections.frequency(updateResult, State.OK);
        final var oldUpdates = Collections.frequency(updateResult, State.OLD);
        final var equalUpdates = Collections.frequency(updateResult, State.OK_EQUAL);

        assertEquals(okUpdates, newerCount);
        assertEquals(equalUpdates, equalCount);
        assertEquals(oldUpdates, olderCount);

        final List<PcfRt> retrievedPcfRts = db.getAll() //
                                              .blockingGet()
                                              .getEntries()
                                              .stream()
                                              .map(entry -> new PcfRt(entry.getKey(), entry.getValue()))
                                              .collect(Collectors.toList());

        final var maxPcfRts = maxRtData(initPcfRts, updatePcfRts);
        assertEquals(Set.copyOf(maxPcfRts), Set.copyOf(retrievedPcfRts));

    }

    @Test(groups = "functest")
    public void createOrUpdateLeaseTest() throws InterruptedException
    {
        final int totalEntries = 1;

        final var initPcfRt = initData(totalEntries).get(0);
        final var newerPcfRt = new PcfRt(initPcfRt.getId(), initPcfRt.getRecoverytime() + 100L);
        final var olderPcfRt = new PcfRt(initPcfRt.getId(), initPcfRt.getRecoverytime() - 100L);
        final var equalPcfRt = initPcfRt;

        db.createOrUpdate(initPcfRt).blockingGet();
        final var leaseId = db.getLeaseId(initPcfRt.getId()).blockingGet().get();
        log.info("created leaseId {}", leaseId);
        assertTrue(leaseId > 0);

        final var initTtl = rxEtcd.getLease(leaseId, LeaseOption.DEFAULT).blockingGet().getTTL();
        TimeUnit.SECONDS.sleep(1);

        db.createOrUpdate(equalPcfRt).blockingGet();
        final var equalTtl = rxEtcd.getLease(leaseId, LeaseOption.DEFAULT).blockingGet().getTTL();
        assertTrue(equalTtl > initTtl - 1);

        TimeUnit.SECONDS.sleep(1);
        db.createOrUpdate(newerPcfRt).blockingGet();
        final var newerTtl = rxEtcd.getLease(leaseId, LeaseOption.DEFAULT).blockingGet().getTTL();

        assertTrue(newerTtl > equalTtl - 1);

        TimeUnit.SECONDS.sleep(1);
        db.createOrUpdate(olderPcfRt).blockingGet();
        final var olderTtl = rxEtcd.getLease(leaseId, LeaseOption.DEFAULT).blockingGet().getTTL();

        assertTrue(olderTtl == newerTtl - 1);
    }

    @Test(groups = "functest")
    public void leaseExpirationTest() throws InterruptedException
    {
        final int totalEntries = 1;
        final long smallTtl = 1;
        final var smallDb = new PcfRtDbEtcd(rxEtcd, smallTtl);

        final var pcfRt = initData(totalEntries).get(0);

        smallDb.createOrUpdate(pcfRt).blockingGet();
        final var leaseId = smallDb.getLeaseId(pcfRt.getId()).blockingGet().get();
        assertTrue(leaseId > 0);
        TimeUnit.SECONDS.sleep(smallTtl * 3);

        final var fetched = smallDb.get(pcfRt.getId()).blockingGet();
        assertTrue(fetched.isEmpty());
    }

    @Test(groups = "functest")
    public void deleteTest()
    {
        final int totalEntries = 10;

        final var initPcfRts = initData(totalEntries);

        log.info("Storing {} PcfRt objects in etcd db", totalEntries);
        final var updateResult = initPcfRts.stream()
                                           .map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet())
                                           .filter(state -> state.equals(State.OK))
                                           .collect(Collectors.toList());
        assertEquals(updateResult.size(), totalEntries);

        log.info("Deleted {} from etcd db", initPcfRts.get(0).getId());
        db.delete(initPcfRts.get(0)).blockingAwait();
        initPcfRts.remove(0);

        final List<PcfRt> retrievedPcfRts = db.getAll() //
                                              .blockingGet()
                                              .getEntries()
                                              .stream()
                                              .map(entry -> new PcfRt(entry.getKey(), entry.getValue()))
                                              .collect(Collectors.toList());

        assertEquals(Set.copyOf(initPcfRts), Set.copyOf(retrievedPcfRts));

    }

    @Test(groups = "functest")
    public void dbWatchInitTest() throws InterruptedException
    {

        final int totalEntries = 13;
        final int awaitCount = 1;

        final var initPcfRts = initData(totalEntries);

        final var updateResult = initPcfRts.stream()
                                           .map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet())
                                           .filter(state -> state.equals(State.OK))
                                           .collect(Collectors.toList());
        assertEquals(updateResult.size(), totalEntries);

        final var cache = new PcfRtCache();
        final var watcher = new PcfRtDbWatcher(db, cache, true);

        final var watchDisp = watcher.run().subscribe();
        final var updatesTest = watcher.cacheUpdatesWatcher().test();

        updatesTest.awaitCount(awaitCount);
        log.info("Started database watcher");

        final var currentCache = updatesTest.values().get(awaitCount - 1);

        assertEquals(updatesTest.valueCount(), awaitCount);
        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(initPcfRts));
        log.info("Watcher initialized cache as expected");

        watchDisp.dispose();
        updatesTest.dispose();
    }

    @Test(groups = "functest")
    public void dbWatchUpdateTest()
    {

        final int totalEntries = 20;
        int awaitCount = 1;

        final var initPcfRts = initData(totalEntries);

        final var watcher = new PcfRtDbWatcher(db, new PcfRtCache(), true);
        PcfRtCache currentCache;

        final var watchDisp = watcher.run().subscribe();
        final var updatesTest = watcher.cacheUpdatesWatcher().test();

        log.info("Started database watcher");
        updatesTest.awaitCount(awaitCount);
        currentCache = updatesTest.values().get(awaitCount - 1);

        assertEquals(updatesTest.valueCount(), awaitCount);
        assertTrue(currentCache.get().isEmpty());

        log.info("Update db with init Data");
        var updateResult = initPcfRts.stream().map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet());
        awaitCount += updateResult.filter(r -> r.equals(State.OK)).count();
        log.info("Waiting for {} events", awaitCount);

        updatesTest.awaitCount(awaitCount);
        currentCache = updatesTest.values().get(awaitCount - 1);
        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(initPcfRts));
        log.info("Watcher updated cache as expected");

        final var updatedPcfRts = updateData(initPcfRts);
        log.info("Update db with updated Data");

        updateResult = updatedPcfRts.stream().map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet());
        awaitCount += updateResult.filter(r -> r.equals(State.OK) || r.equals(State.OK_EQUAL)).count();

        log.info("Waiting for {} events", awaitCount);
        updatesTest.awaitCount(awaitCount);
        currentCache = updatesTest.values().get(awaitCount - 1);

        final var maxPcfRts = maxRtData(initPcfRts, updatedPcfRts);
        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(maxPcfRts));

        watchDisp.dispose();
        updatesTest.dispose();
    }

    @Test(groups = "functest")
    public void dbWatchDeleteTest() throws InterruptedException
    {

        final int totalEntries = 5;
        final int awaitCount = 2;
        final var initPcfRts = initData(totalEntries);

        final var updateResult = initPcfRts.stream()
                                           .map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet())
                                           .filter(state -> state.equals(State.OK))
                                           .collect(Collectors.toList());
        assertEquals(updateResult.size(), totalEntries);

        final var cache = new PcfRtCache();
        final var watcher = new PcfRtDbWatcher(db, cache, true);

        final var watchDisp = watcher.run().subscribe();
        final var updatesTest = watcher.cacheUpdatesWatcher().test();
        log.info("Started database watcher");

        db.delete(initPcfRts.get(0)).blockingAwait();
        log.info("Deleted {} from etcd db", initPcfRts.get(0).getId());

        updatesTest.awaitCount(awaitCount);
        final var currentCache = updatesTest.values().get(0);
        initPcfRts.remove(0);

        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(initPcfRts));

        watchDisp.dispose();
        updatesTest.dispose();
    }

    @Test(groups = "functest", invocationCount = 1)
    public void watcherFailureCacheRecoverTest()
    {
        final int totalEntries = 10;
        int awaitCount = 1;

        final var initPcfRts = initData(totalEntries);

        final var updateResult = initPcfRts.stream()
                                           .map(pcfRt -> db.createOrUpdate(pcfRt).blockingGet())
                                           .filter(state -> state.equals(State.OK))
                                           .collect(Collectors.toList());
        assertEquals(updateResult.size(), totalEntries);

        final var cache = new PcfRtCache();
        final var watcher = new PcfRtDbWatcher(db, cache, true);

        final var watchDisp = watcher.run().subscribe();
        final var updatesTest = watcher.cacheUpdatesWatcher().test();

        updatesTest.awaitCount(awaitCount);
        log.info("Started database watcher");

        var currentCache = updatesTest.values().get(awaitCount - 1);

        assertEquals(updatesTest.valueCount(), awaitCount);
        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(initPcfRts));
        log.info("Watcher initialized cache as expected");

        cache.clear();
        log.info("cleared cache {}", cache.get());

        log.info("Restarting etcd database");
        this.etcdTestBed.restartEtcdServer(0);
        awaitCount += 1;

        log.info("Waiting watcher");
        updatesTest.awaitCount(awaitCount, TestWaitStrategy.SLEEP_10MS, 10000);
        updatesTest.assertNotTerminated();

        currentCache = updatesTest.values().get(awaitCount - 1);

        assertEquals(updatesTest.valueCount(), awaitCount);
        assertEquals(Set.copyOf(currentCache.get().values()), Set.copyOf(initPcfRts));

        watchDisp.dispose();
        updatesTest.dispose();
    }

    @AfterMethod
    public void cleanupDb()
    {
        this.etcdTestBed.clearKeyspace();
    }

    private List<PcfRt> initData(int totalEntries)
    {
        return IntStream.range(0, totalEntries) //
                        .mapToObj(i -> new PcfRt(UUID.randomUUID(), (long) (Math.random() * 10000000)))
                        .collect(Collectors.toList());
    }

    private List<PcfRt> updateData(List<PcfRt> initData)
    {
        return IntStream.range(0, initData.size()) //
                        .mapToObj(i -> new PcfRt(initData.get(i).getId(), (long) (Math.random() * 10000000)))
                        .collect(Collectors.toList());
    }

    private List<PcfRt> maxRtData(List<PcfRt> initData,
                                  List<PcfRt> updatedData)
    {
        return IntStream.range(0, initData.size()) //
                        .mapToObj(i -> Stream.of(initData.get(i), updatedData.get(i)).max(Comparator.comparing(PcfRt::getRecoverytime)).get())
                        .collect(Collectors.toList());

    }
}
