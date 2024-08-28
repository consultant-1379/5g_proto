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
 * Created on: Feb 19, 2020
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

/**
 * Test class for RxLeaderElection.
 */
public class RxLeaderElectionTest
{
    private static final Logger log = LoggerFactory.getLogger(RxLeaderElectionTest.class);

    private static final String LEADER_KEY = "leader-id";
    private static final int LEADER_TTL = 10;
    private static final int RENEW_INTERVAL = 8;
    private static final int CLAIM_INTERVAL = 2;
    private static final int RECOVERY_DELAY = 12;
    private static final float REQUEST_LATENCY = 0.2f;

    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;

    private final Action onComplete = () -> log.info("Election completed.");
    private final Consumer<Throwable> onError = e -> log.error(e.getMessage());
    private final BiConsumer<String, LeaderStatus> logStatus = (id,
                                                                status) -> log.info("[ID: {}] leaderStatus: {}", id, status);

    /**
     * Examines if a contender can successfully assume leadership.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains the correct UUID.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyAssumeLeadership_oneContender()
    {
        var cleanUp = new CompositeDisposable();

        try
        {
            // Create election node.
            var ownId = UUID.randomUUID().toString();
            var election = createRxLeaderElection(ownId).blockingGet();

            // Subscribe to leaderStatusUpdates and then start the election.
            var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
            cleanUp.add(leaderStatus);
            var disp = election.run().subscribe(onComplete, onError);
            cleanUp.add(disp);

            // Expect only one leader key from ETCD notifications.
            var leaderUpdates = leaderKeyUpdates().take(1).toList().blockingGet();
            var currentLeader = leaderUpdates.get(0).get();

            // Assert leaderKey.
            assertTrue(currentLeader.equals(ownId), "The uuid of leader does not match the leaderKey stored in ETCD.");

            // Assert leader status. Status: CONTENDER, LEADER.
            leaderStatus.awaitCount(2) //
                        .assertValueCount(2)
                        .assertValueAt(0, LeaderStatus.CONTENDER)
                        .assertValueAt(1, LeaderStatus.LEADER);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    /**
     * Examines if a contender can successfully renew leadership.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains the correct UUID.</li>
     * <li>LeaderKey remains the same.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyRenewLeadership_oneContender()
    {
        var cleanUp = new CompositeDisposable();

        try
        {
            // Create election node.
            var ownId = UUID.randomUUID().toString();
            var election = createRxLeaderElection(ownId).blockingGet();

            // Subscribe to leaderStatusUpdates and then start the election.
            var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
            cleanUp.add(leaderStatus);
            var disp = election.run().subscribe(onComplete, onError);
            cleanUp.add(disp);

            // Expect two leader key notifications from ETCD.
            var leaderUpdates = leaderKeyUpdates().take(2).toList().blockingGet();
            var leader1 = leaderUpdates.get(0).get();
            var leader2 = leaderUpdates.get(1).get();

            // Assert leaderKey updates.
            assertTrue(leader1.equals(ownId) && leader2.equals(ownId), "The uuid of leader does not match the leaderKey stored in ETCD.");

            // Assert leader status. Status: CONTENDER, LEADER.
            leaderStatus.awaitCount(2) //
                        .assertValueCount(2)
                        .assertValueAt(0, LeaderStatus.CONTENDER)
                        .assertValueAt(1, LeaderStatus.LEADER);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    /**
     * Examines if a contender can successfully keep claiming leadership and become
     * leader after multiple claims.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains otherUUID and then the own UUID.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyAssumeLeadershipAfterMultipleClaims_oneContender()
    {
        var cleanUp = new CompositeDisposable();

        try
        {
            var otherUUID = "otherUUID";
            // Set the leaderKey to another value.
            rxEtcd.put(EtcdTestBed.btSqn(LEADER_KEY), EtcdTestBed.btSqn(otherUUID)).blockingGet();
            // Confirm leaderKey is set.
            var response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            var value = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(value.equals(otherUUID));

            // Create election node.
            var ownId = UUID.randomUUID().toString();
            var election = createRxLeaderElection(ownId).blockingGet();

            // Subscribe to leaderStatusUpdates and then start the election.
            var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
            cleanUp.add(leaderStatus);
            var disp = election.run().subscribe(onComplete, onError);
            cleanUp.add(disp);

            // Allow for 3 extra claims before deleting the current leaderKey.
            Observable.timer(CLAIM_INTERVAL * 3, TimeUnit.SECONDS) //
                      .ignoreElements()
                      .andThen(rxEtcd.delete(EtcdTestBed.btSqn(LEADER_KEY)))
                      .subscribe();

            // Expect two leaderKey notifications from ETCD (delete and put).
            var leaderUpdates = leaderKeyUpdates().take(2).toList().blockingGet();
            var leader = leaderUpdates.get(1).get();

            // Assert leaderKey updates.
            assertTrue(leader.equals(ownId), "The uuid of leader does not match the leaderKey stored in ETCD.");

            // Assert leader status. Status: CONTENDER, LEADER.
            leaderStatus.awaitCount(2) //
                        .assertValueCount(2)
                        .assertValueAt(0, LeaderStatus.CONTENDER)
                        .assertValueAt(1, LeaderStatus.LEADER);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    /**
     * Examines if a contender that became a leader, recovers properly after losing
     * leadership and starts claiming leadership again. Leadership is taken over due
     * to malicious override of the leaderKey.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains own UUID and then otherUUID.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER, CONTENDER, CONTENDER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyRecoverAfterLosingLeadership_oneContender()
    {
        var cleanUp = new CompositeDisposable();

        try
        {
            // Create election node.
            var ownId = UUID.randomUUID().toString();
            var election = createRxLeaderElection(ownId).blockingGet();

            // Subscribe to leaderStatusUpdates and then start the election.
            var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
            cleanUp.add(leaderStatus);
            var disp = election.run().subscribe(onComplete, onError);
            cleanUp.add(disp);

            // Expect two leaderKey notifications from ETCD.
            var leaderUpdates = leaderKeyUpdates().test();

            // Allow election node to assume leadership before overriding the leaderKey.
            var otherUUID = "otherUUID";
            Observable.timer(CLAIM_INTERVAL, TimeUnit.SECONDS) //
                      .ignoreElements()
                      .andThen(rxEtcd.put(EtcdTestBed.btSqn(LEADER_KEY), EtcdTestBed.btSqn(otherUUID)))
                      .blockingGet();

            // Confirm leaderKey is set.
            var response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            var value = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(value.equals(otherUUID));

            // Wait for the expiration of the leadership and the recovery of the election.
            Observable.timer(LEADER_TTL + RECOVERY_DELAY, TimeUnit.SECONDS).blockingFirst();

            // Assert leaderKey updates.
            leaderUpdates.awaitCount(2) //
                         .assertValueCount(2)
                         .assertValueAt(0, Optional.of(ownId))
                         .assertValueAt(1, Optional.of(otherUUID));

            // Assert leader status. Status: CONTENDER, LEADER, CONTENDER (instantly when
            // leadership is lost), CONTENDER (when starting to claim leadership again).
            leaderStatus.awaitCount(4) //
                        .assertValueCount(4)
                        .assertValueAt(0, LeaderStatus.CONTENDER)
                        .assertValueAt(1, LeaderStatus.LEADER)
                        .assertValueAt(2, LeaderStatus.CONTENDER)
                        .assertValueAt(3, LeaderStatus.CONTENDER);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    /**
     * Examines if a contender can successfully assume leadership between multiple
     * contenders.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains a UUID from the contenders.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER.</li>
     * <li>Contenders statusUpdates: CONTENDER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyAssumeLeadership_multipleContenders()
    {
        var cleanUp = new CompositeDisposable();
        var contenders = new ConcurrentHashMap<String, ElectionEntry>();

        try
        {
            // Create multiple election nodes, subscribe to leaderStatusUpdates. Store them
            // in the contenders structure.
            var numContenders = 40;
            IntStream.range(0, numContenders).parallel().forEach(i ->
            {
                var ownId = UUID.randomUUID().toString();
                var election = createRxLeaderElection(ownId).blockingGet();
                var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
                contenders.put(ownId, new ElectionEntry(ownId, election, leaderStatus));
                cleanUp.add(leaderStatus);
            });

            // Start all elections concurrently.
            var elections = contenders.values().stream().map(entry -> entry.getElection().run()).collect(Collectors.toList());
            var mergeDisp = Completable.merge(elections).subscribe(onComplete, onError);
            cleanUp.add(mergeDisp);

            // Expect only one leader key from ETCD notifications.
            var leaderUpdates = leaderKeyUpdates().take(1).toList().blockingGet();
            var currentLeader = leaderUpdates.get(0).get();

            // Assert leaderKey.
            assertTrue(contenders.containsKey(currentLeader), "The uuid of leader does not match the leaderKey stored in ETCD.");

            // Assertions for the leader. Status: CONTENDER -> LEADER.
            contenders.get(currentLeader) //
                      .getLeaderStatus()
                      .awaitCount(2)
                      .assertValueCount(2)
                      .assertValueAt(0, LeaderStatus.CONTENDER)
                      .assertValueAt(1, LeaderStatus.LEADER);

            // Assertions for the contenders, Status: CONTENDER.
            contenders.values() //
                      .stream()
                      .filter(entry -> !entry.getIdentity().equals(currentLeader))
                      .forEach(entry ->
                      {
                          entry.getLeaderStatus() //
                               .awaitCount(1)
                               .assertValueCount(1)
                               .assertValueAt(0, LeaderStatus.CONTENDER);
                      });
        }
        finally
        {
            cleanUp.dispose();
            contenders.clear();
        }
    }

    /**
     * Examines if a contender can successfully renew leadership between multiple
     * contenders.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains a UUID from the contenders.</li>
     * <li>LeaderKey remains the same.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER.</li>
     * <li>Contenders statusUpdates: CONTENDER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void successfullyRenewLeadership_multipleContenders()
    {
        var cleanUp = new CompositeDisposable();
        var contenders = new ConcurrentHashMap<String, ElectionEntry>();

        try
        {
            // Create multiple election nodes, subscribe to leaderStatusUpdates. Store them
            // in the contenders structure.
            var numContenders = 40;
            IntStream.range(0, numContenders).parallel().forEach(i ->
            {
                var ownId = UUID.randomUUID().toString();
                var election = createRxLeaderElection(ownId).blockingGet();
                var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
                contenders.put(ownId, new ElectionEntry(ownId, election, leaderStatus));
                cleanUp.add(leaderStatus);
            });

            // Start all elections concurrently.
            var elections = contenders.values().stream().map(entry -> entry.getElection().run()).collect(Collectors.toList());
            var mergeDisp = Completable.merge(elections).subscribe(onComplete, onError);
            cleanUp.add(mergeDisp);

            // Expect only one leader key from ETCD notifications.
            var leaderUpdates = leaderKeyUpdates().take(2).toList().blockingGet();
            var leader1 = leaderUpdates.get(0).get();
            var leader2 = leaderUpdates.get(1).get();

            // Assert leaderKey updates.
            assertTrue(contenders.containsKey(leader1), "The uuid of leader does not match the leaderKey stored in ETCD.");
            assertTrue(leader1.equals(leader2), "The uuid of the leader changed, so the leader couldn't renew successfully.");

            // Assertions for the leader. Status: CONTENDER -> LEADER.
            contenders.get(leader1) //
                      .getLeaderStatus()
                      .awaitCount(2)
                      .assertValueCount(2)
                      .assertValueAt(0, LeaderStatus.CONTENDER)
                      .assertValueAt(1, LeaderStatus.LEADER);

            // Assertions for the contenders, Status: CONTENDER.
            contenders.values() //
                      .stream()
                      .filter(entry -> !entry.getIdentity().equals(leader1))
                      .forEach(entry ->
                      {
                          entry.getLeaderStatus() //
                               .awaitCount(1)
                               .assertValueCount(1)
                               .assertValueAt(0, LeaderStatus.CONTENDER);
                      });
        }
        finally
        {
            cleanUp.dispose();
            contenders.clear();
        }
    }

    /**
     * Examines if a contender that became leader, recovers properly after ETCD
     * restart. Contender should start claiming leadership again and manage to take
     * the leadership.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey in ETCD contains own UUID.</li>
     * <li>Leader statusUpdates: CONTENDER, LEADER, CONTENDER, CONTENDER,
     * LEADER.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void etcdRestartRecovery_oneContender()
    {
        var cleanUp = new CompositeDisposable();

        try
        {
            // Create election node.
            var ownId = UUID.randomUUID().toString();
            var election = createRxLeaderElection(ownId).blockingGet();

            // Subscribe to leaderStatusUpdates and then start the election.
            var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
            cleanUp.add(leaderStatus);
            var disp = election.run().subscribe(onComplete, onError);
            cleanUp.add(disp);

            // Wait for the node to become leader.
            Observable.timer(CLAIM_INTERVAL + 1, TimeUnit.SECONDS).blockingFirst();
            // Confirm leaderKey is equal to ownID.
            var response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            var value = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(value.equals(ownId));

            // Restart ETCD Server after a period equal to renew interval.
            etcdTestBed.restartEtcdServer(RENEW_INTERVAL - 1);

            // Wait for the node to become leader again.
            Observable.timer(LEADER_TTL + CLAIM_INTERVAL + 4, TimeUnit.SECONDS).blockingFirst();

            // Confirm leaderKey is equal to ownID.
            response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            value = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(value.equals(ownId));

            // Assert leader status. Status: CONTENDER, LEADER, CONTENDER (instantly when
            // leadership is lost), CONTENDER (when starting to claim leadership again),
            // LEADER.
            leaderStatus.awaitCount(5) //
                        .assertValueCount(5)
                        .assertValueAt(0, LeaderStatus.CONTENDER)
                        .assertValueAt(1, LeaderStatus.LEADER)
                        .assertValueAt(2, LeaderStatus.CONTENDER)
                        .assertValueAt(3, LeaderStatus.CONTENDER)
                        .assertValueAt(4, LeaderStatus.LEADER);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    /**
     * Examines if after restarting ETCD, a new leader is elected and the rest of
     * the contenders keep claiming leadership.
     * 
     * Expect:
     * <ul>
     * <li>LeaderKey value in ETCD database:</li>
     * <li>Before ETCD restart, leaderKey contains a UUID from the contenders.</li>
     * <li>After ETCD restart, leaderKey contains a UUID from the contenders.</li>
     * 
     * <li>Same leader after ETCD restart:</li>
     * <li>Contender statusUpdates: {CONTENDER, CONTENDER, CONTENDER}</li>
     * <li>Leader statusUpdates: {CONTENDER, LEADER, CONTENDER, CONTENDER,
     * LEADER}.</li>
     * 
     * <li>Different leader after ETCD restart:</li>
     * <li>Contender statusUpdates: {CONTENDER, CONTENDER, CONTENDER}</li>
     * <li>First leader statusUpdates: {CONTENDER, LEADER, CONTENDER,
     * CONTENDER}.</li>
     * <li>Second leader statusUpdates: {CONTENDER, CONTENDER, CONTENDER,
     * LEADER}.</li>
     * </ul>
     */
    @Test(groups = "functest")
    public void etcdRestartRecovery_MultipleContenders()
    {
        var cleanUp = new CompositeDisposable();
        var contenders = new ConcurrentHashMap<String, ElectionEntry>();

        try
        {
            // Create multiple election nodes, subscribe to leaderStatusUpdates. Store them
            // in the contenders structure.
            var numContenders = 20;
            IntStream.range(0, numContenders).parallel().forEach(i ->
            {
                var ownId = UUID.randomUUID().toString();
                var election = createRxLeaderElection(ownId).blockingGet();
                var leaderStatus = election.leaderStatusUpdates().doOnNext(status -> logStatus.accept(ownId, status)).test();
                contenders.put(ownId, new ElectionEntry(ownId, election, leaderStatus));
                cleanUp.add(leaderStatus);
            });

            // Start all elections concurrently.
            var elections = contenders.values().stream().map(entry -> entry.getElection().run()).collect(Collectors.toList());
            var mergeDisp = Completable.merge(elections).subscribe(onComplete, onError);
            cleanUp.add(mergeDisp);

            // Wait for a node to become leader.
            Observable.timer(CLAIM_INTERVAL + 1, TimeUnit.SECONDS).blockingFirst();
            // Confirm leaderKey belongs to one of the contenders.
            var response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            var firstLeader = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(contenders.containsKey(firstLeader));

            // Restart ETCD Server after a period equal to renew interval.
            etcdTestBed.restartEtcdServer(RENEW_INTERVAL - 1);

            // Wait for a node to become leader again.
            Observable.timer(LEADER_TTL + CLAIM_INTERVAL + 4, TimeUnit.SECONDS).blockingFirst();
            // Confirm leaderKey belongs to one of the contenders.
            response = rxEtcd.get(EtcdTestBed.btSqn(LEADER_KEY)).blockingGet();
            var secondLeader = new String(response.getKvs().get(0).getValue().getBytes());
            assertTrue(contenders.containsKey(secondLeader));

            // ASSERTIONS.

            // Case: Same leader before and after ETCD restart.
            if (firstLeader.equals(secondLeader))
            {
                // Assertions for the leader. Status: CONTENDER, LEADER,
                // CONTENDER (instantly when leadership is lost), CONTENDER (when starting to
                // claim leadership again), LEADER.
                contenders.get(firstLeader) //
                          .getLeaderStatus()
                          .awaitCount(5)
                          .assertValueCount(5)
                          .assertValueAt(0, LeaderStatus.CONTENDER)
                          .assertValueAt(1, LeaderStatus.LEADER)
                          .assertValueAt(2, LeaderStatus.CONTENDER)
                          .assertValueAt(3, LeaderStatus.CONTENDER)
                          .assertValueAt(4, LeaderStatus.LEADER);
            }
            // Case: Different leader before and after ETCD restart.
            else
            {
                // Assertions for the leader before ETCD restart. Status: CONTENDER, LEADER,
                // CONTENDER (instantly when leadership is lost), CONTENDER (when starting to
                // claim leadership again).
                contenders.get(firstLeader) //
                          .getLeaderStatus()
                          .awaitCount(4)
                          .assertValueCount(4)
                          .assertValueAt(0, LeaderStatus.CONTENDER)
                          .assertValueAt(1, LeaderStatus.LEADER)
                          .assertValueAt(2, LeaderStatus.CONTENDER)
                          .assertValueAt(3, LeaderStatus.CONTENDER);

                // Assertions for the leader after ETCD restart. Status: CONTENDER, CONTENDER
                // (instantly when leadership is lost), CONTENDER (when starting to claim
                // leadership again), LEADER.
                contenders.get(secondLeader) //
                          .getLeaderStatus()
                          .awaitCount(4)
                          .assertValueCount(4)
                          .assertValueAt(0, LeaderStatus.CONTENDER)
                          .assertValueAt(1, LeaderStatus.CONTENDER)
                          .assertValueAt(2, LeaderStatus.CONTENDER)
                          .assertValueAt(3, LeaderStatus.LEADER);
            }

            // Assertions for the contenders that never became leaders, Status: CONTENDER,
            // CONTENDER (instantly when leadership is lost), CONTENDER (when starting to
            // claim leadership again).
            contenders.values() //
                      .stream()
                      .filter(entry -> !entry.getIdentity().equals(firstLeader) && !entry.getIdentity().equals(secondLeader))
                      .forEach(entry ->
                      {
                          entry.getLeaderStatus() //
                               .awaitCount(3)
                               .assertValueCount(3)
                               .assertValueAt(0, LeaderStatus.CONTENDER)
                               .assertValueAt(1, LeaderStatus.CONTENDER)
                               .assertValueAt(2, LeaderStatus.CONTENDER);
                      });
        }
        finally
        {
            cleanUp.dispose();
            contenders.clear();
        }
    }

    /**
     * Creates a leader election for a given id.
     * 
     * @param ownId The id of the contender.
     * @return Single<RxLeaderElection>
     */
    private Single<RxLeaderElection> createRxLeaderElection(String ownId)
    {
        return new RxLeaderElection.Builder(rxEtcd, ownId, LEADER_KEY).leaderInterval(LEADER_TTL)
                                                                      .renewInterval(RENEW_INTERVAL)
                                                                      .claimInterval(CLAIM_INTERVAL)
                                                                      .recoveryDelay(RECOVERY_DELAY)
                                                                      .requestLatency(REQUEST_LATENCY)
                                                                      .build();
    }

    /**
     * Create a watcher for changes to the leaderKey in ETCD database.
     * 
     * @return Flowable<Optional<String>> Returns the value updates of the
     *         leaderKey.
     */
    private Flowable<Optional<String>> leaderKeyUpdates()
    {
        return rxEtcd.watch(EtcdTestBed.btSqn(LEADER_KEY)).map(notification ->
        {
            var event = notification.getEvents().get(0);
            var eventType = event.getEventType();

            switch (eventType)
            {
                case PUT:
                    return Optional.of(new String(event.getKeyValue().getValue().getBytes()));
                case DELETE:
                    return Optional.of("noLeader");
                default:
                    return Optional.empty();
            }
        });
    }

    @BeforeClass
    private void setUpTestEnvironment() throws InterruptedException, SSLException
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(LEADER_KEY, true);
        etcdTestBed.start();

        var endpoint = etcdTestBed.getEndpoint();

        rxEtcd = etcdTestBed.createEtcdClient(3, (CLAIM_INTERVAL * 1000) - 50, TimeUnit.MILLISECONDS, endpoint);
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }

    @BeforeMethod
    private void prepareTest(Method method)
    {
        log.info("Before method.");

        if (!etcdTestBed.isKeyspaceClean())
            etcdTestBed.clearKeyspace();

        log.info("Executing: {}", method.getName());
    }

    @AfterMethod
    private void cleanupTest()
    {
        log.info("After method.");

        etcdTestBed.clearKeyspace();
    }

    /**
     * Utility inner class.
     */
    private class ElectionEntry
    {
        private final String identity;
        private final RxLeaderElection election;
        private final TestObserver<LeaderStatus> leaderStatus;

        public ElectionEntry(String identity,
                             RxLeaderElection election,
                             TestObserver<LeaderStatus> leaderStatus)
        {
            this.identity = identity;
            this.election = election;
            this.leaderStatus = leaderStatus;
        }

        public String getIdentity()
        {
            return identity;
        }

        public RxLeaderElection getElection()
        {
            return election;
        }

        public TestObserver<LeaderStatus> getLeaderStatus()
        {
            return leaderStatus;
        }
    }
}
