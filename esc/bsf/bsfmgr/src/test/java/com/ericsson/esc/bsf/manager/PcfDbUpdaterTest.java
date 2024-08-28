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
 * Created on: Apr 26, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.sc.bsf.etcd.PcfDbEtcd;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class PcfDbUpdaterTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfDbUpdaterTest.class);

    private static RxEtcd rxEtcd;
    private static PcfDbEtcd pcfDbEtcd;
    private static EtcdTestBed etcdTestBed;
    private static PcfDbUpdater pcfDbUpdater;
    private static Disposable pcfDbUpdaterDisp;
    private static BehaviorSubject<List<PcfNf>> subject;
    private static Flowable<List<PcfNf>> discoveredPcfs;

    private static final Boolean TLS_ENABLED = false;
    private static final Long TTL = 2592000L;

    @Test(groups = "functest")
    public void checkUpdateDbNewPcfNfs()
    {
        // Create PcfNfs.
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        var pcfNf1 = createPcfNf(id1, NFStatus.REGISTERED, List.of("pcfSet1"), 1);
        var pcfNf2 = createPcfNf(id2, NFStatus.SUSPENDED, List.of("pcfSet1"), 2);
        var pcfNf3 = createPcfNf(id3, NFStatus.REGISTERED, List.of("pcfSet2"), 3);

        // Emit discovered PcfNfs.
        subject.onNext(List.of(pcfNf1, pcfNf2, pcfNf3));
        // Wait for the PcfNfs to be written in the DB.
        Observable.timer(1, TimeUnit.SECONDS).blockingFirst();

        // Check results from ETCD DB.
        var pcfEntries = pcfDbEtcd.getPcfs().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfNf1));
        assertTrue(pcfList.contains(pcfNf2));
        assertTrue(pcfList.contains(pcfNf3));
    }

    @Test(groups = "functest")
    public void checkUpdateDbExpiredPcfNfsRemoved()
    {
        // Create PcfNfs.
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        var pcfNf1 = createPcfNf(id1, NFStatus.REGISTERED, List.of("pcfSet1"), 1);
        var pcfNf2 = createPcfNf(id2, NFStatus.SUSPENDED, List.of("pcfSet1"), 2);
        var pcfNf3 = createPcfNf(id3, NFStatus.REGISTERED, List.of("pcfSet2"), 3);

        // Emit discovered PcfNfs.
        subject.onNext(List.of(pcfNf1, pcfNf2, pcfNf3));
        // Wait for the PcfNfs to be written in the DB.
        Observable.timer(1, TimeUnit.SECONDS).blockingFirst();

        // Check results from ETCD DB.
        var pcfEntries = pcfDbEtcd.getPcfs().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfNf1));
        assertTrue(pcfList.contains(pcfNf2));
        assertTrue(pcfList.contains(pcfNf3));

        // Emit discovered PcfNfs. PcfNf2 is not discovered this time.
        subject.onNext(List.of(pcfNf1, pcfNf3));
        // Wait for the PcfNfs to be written in the DB.
        Observable.timer(1, TimeUnit.SECONDS).blockingFirst();

        // Check results from ETCD DB.
        pcfEntries = pcfDbEtcd.getPcfs().blockingGet();
        pcfList = pcfEntries.getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfNf1));
        assertTrue(!pcfList.contains(pcfNf2));
        assertTrue(pcfList.contains(pcfNf3));
    }

    @Test(groups = "functest")
    public void checkUpdateDbPcfNfsUpdated()
    {
        // Create PcfNfs.
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        var pcfNf1 = createPcfNf(id1, NFStatus.REGISTERED, List.of("pcfSet1"), 1);
        var pcfNf2 = createPcfNf(id2, NFStatus.SUSPENDED, List.of("pcfSet1"), 2);

        // Emit discovered PcfNfs.
        subject.onNext(List.of(pcfNf1, pcfNf2));
        // Wait for the PcfNfs to be written in the DB.
        Observable.timer(1, TimeUnit.SECONDS).blockingFirst();

        // Check results from ETCD DB.
        var pcfEntries = pcfDbEtcd.getPcfs().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfNf1));
        assertTrue(pcfList.contains(pcfNf2));

        // Update PcfNf2.
        var pcfNf2New = createPcfNf(id2, NFStatus.REGISTERED, List.of("pcfSet2"), 3);

        // Emit discovered PcfNfs.
        subject.onNext(List.of(pcfNf1, pcfNf2New));
        // Wait for the PcfNfs to be written in the DB.
        Observable.timer(1, TimeUnit.SECONDS).blockingFirst();

        // Check results from ETCD DB.
        pcfEntries = pcfDbEtcd.getPcfs().blockingGet();
        pcfList = pcfEntries.getEntries().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfNf1));
        assertTrue(pcfList.contains(pcfNf2New));
    }

    private static PcfNf createPcfNf(UUID nfInstanceId,
                                     String nfStatus,
                                     List<String> nfSetIdList,
                                     int diamId)
    {
        return PcfNf.newBuilder()
                    .withNfInstanceId(nfInstanceId)
                    .withNfStatus(nfStatus)
                    .withNfSetIdList(nfSetIdList)
                    .withRxDiamHost("rx.diam.host" + diamId + ".com")
                    .withRxDiamRealm("rx.diam.realm" + diamId + ".com")
                    .build();
    }

    public static String getEndpoint(boolean tlsEnabled) throws InterruptedException
    {

        String scheme = tlsEnabled ? "https://" : "http://";

        String host = etcdTestBed.getHost();
        String ip = etcdTestBed.getEtcdIp();
        int port = etcdTestBed.getEtcdPort();
        log.info("host: {}, ip:{}, port:{}", host, ip, port);

        log.info("endpoint: {} ", scheme + ip + ":" + port);
        // TimeUnit.SECONDS.sleep(20);

        return scheme + ip + ":" + port;
    }

    @AfterMethod
    private static void cleanUpTest()
    {
        // Remove all PCFs from database.
        pcfDbEtcd.truncate().blockingGet();
    }

    @BeforeClass
    private static void setUpTestEnvironment() throws InterruptedException, SSLException
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed("nnrf-keyspace", TLS_ENABLED);
        etcdTestBed.start();
        rxEtcd = etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        pcfDbEtcd = new PcfDbEtcd(rxEtcd, true, TTL);

        // Preparations.
        subject = BehaviorSubject.<List<PcfNf>>create();
        discoveredPcfs = subject.toFlowable(BackpressureStrategy.BUFFER);
        pcfDbUpdater = new PcfDbUpdater(rxEtcd, discoveredPcfs, TTL);
        pcfDbUpdaterDisp = pcfDbUpdater.run().subscribe();
    }

    @AfterClass
    private static void cleanUpTestEnvironment()
    {
        log.info("After class.");

        pcfDbUpdaterDisp.dispose();
        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }
}
