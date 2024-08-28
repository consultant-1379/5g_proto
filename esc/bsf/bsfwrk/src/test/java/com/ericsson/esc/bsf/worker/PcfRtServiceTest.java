package com.ericsson.esc.bsf.worker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.ericsson.esc.bsf.db.BindingRtInfo;
import com.ericsson.esc.bsf.openapi.model.DiameterIdentity;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.etcd.PcfRtCachedServiceImpl;
import com.ericsson.sc.bsf.etcd.PcfRtDbEtcd;
import com.ericsson.sc.bsf.etcd.PcfRtService;
import com.ericsson.sc.bsf.etcd.PcfRtService.Source;
import com.ericsson.sc.bsf.etcd.PcfRtServiceImpl;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PcfRtServiceTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtServiceTest.class);

    private static EtcdTestBed etcdTestBed;
    private static RxEtcd rxEtcd;
    private static PcfRtDbEtcd pcfRtDbEtcd;
    private static long ttl = 10000;

    @BeforeClass
    private void startServer() throws InterruptedException, SSLException
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed("nnrf-keyspace", true);
        etcdTestBed.start();
        rxEtcd = etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        pcfRtDbEtcd = new PcfRtDbEtcd(rxEtcd, ttl);

    }

    @AfterClass
    private void stopServer()
    {
        log.info("After class.");

//        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }

    @DataProvider(name = "cachMode")
    public Boolean[] createData()
    {
        return new Boolean[] { Boolean.FALSE };
    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void getPcfRtTest(boolean cached) throws InterruptedException
    {

        PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        pcfSvc.init().blockingAwait();

        UUID pcfId1 = UUID.randomUUID();
        RecoveryTime recTime1 = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBinding = generateBinding(pcfId1, recTime1);
        var pcfRt = extractPcfRt(pcfBinding).get();

        pcfSvc.reportRecoveryTime(pcfRt).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfRtFromGet = pcfSvc.get(pcfId1).blockingGet().get();

        assertEquals(pcfRtFromGet, pcfRt);
    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void updateWhenNotExist(boolean cached) throws InterruptedException
    {

        PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        UUID pcfId1 = UUID.randomUUID();
        RecoveryTime recTime1 = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBinding = generateBinding(pcfId1, recTime1);
        var pcfRt = extractPcfRt(pcfBinding);

        pcfSvc.reportRecoveryTime(pcfRt.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfEntries = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfRt.get()));

    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void updateWhenNewer(boolean cached) throws InterruptedException
    {

        PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        UUID pcfId = UUID.randomUUID();
        RecoveryTime recTime = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBinding = generateBinding(pcfId, recTime);
        var pcfRt = extractPcfRt(pcfBinding);

        pcfSvc.reportRecoveryTime(pcfRt.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfEntries = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfRt.get()));

        RecoveryTime recTimeNew = new RecoveryTime("2022-05-12T11:56:12.5Z");

        var pcfBindingNew = generateBinding(pcfId, recTimeNew);
        var pcfRtNew = extractPcfRt(pcfBindingNew);

        pcfSvc.reportRecoveryTime(pcfRtNew.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfEntries2 = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList2 = pcfEntries2.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList2.contains(pcfRtNew.get()));

    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void noUpdateWhenOlder(boolean cached) throws InterruptedException
    {

        PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        UUID pcfId = UUID.randomUUID();
        RecoveryTime recTime = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBinding = generateBinding(pcfId, recTime);
        var pcfRt = extractPcfRt(pcfBinding);

        pcfSvc.reportRecoveryTime(pcfRt.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfEntries = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfRt.get()));

        RecoveryTime recTimeNew = new RecoveryTime("2022-05-01T11:56:12.5Z");

        var pcfBindingNew = generateBinding(pcfId, recTimeNew);
        var pcfRtNew = extractPcfRt(pcfBindingNew);

        pcfSvc.reportRecoveryTime(pcfRtNew.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.STALE);
        Thread.sleep(1000);

        var pcfEntries2 = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList2 = pcfEntries2.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList2.contains(pcfRt.get()));
        assertFalse(pcfList2.contains(pcfRtNew.get()));

    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void noUpdateWhenEquals(boolean cached) throws InterruptedException
    {

        PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        UUID pcfId = UUID.randomUUID();
        RecoveryTime recTime = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBinding = generateBinding(pcfId, recTime);
        var pcfRt = extractPcfRt(pcfBinding);

        pcfSvc.reportRecoveryTime(pcfRt.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        Thread.sleep(1000);

        var pcfEntries = pcfRtDbEtcd.getAll().blockingGet();
        var pcfList = pcfEntries.getEntries().stream().map(entry -> new PcfRt(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        assertTrue(pcfList.contains(pcfRt.get()));

        RecoveryTime recTimeNew = new RecoveryTime("2022-05-04T11:56:12.5Z");

        var pcfBindingNew = generateBinding(pcfId, recTimeNew);
        var pcfRtNew = extractPcfRt(pcfBindingNew);

        pcfSvc.reportRecoveryTime(pcfRtNew.get()).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.ALREADY_EXISTS);

    }

    @Test(enabled = true, groups = "functest", dataProvider = "cachMode")
    private void checkStaleBindingTest(boolean cached) throws InterruptedException
    {

        final PcfRtService pcfSvc;
        if (cached)
            pcfSvc = new PcfRtCachedServiceImpl(rxEtcd, ttl);
        else
            pcfSvc = new PcfRtServiceImpl(rxEtcd, ttl);

        logSvcType(pcfSvc);

        pcfSvc.init().blockingAwait();

        final var pcfId = UUID.randomUUID();
        final var recTime1 = new RecoveryTime("2022-05-04T11:56:12.5Z");

        final var pcfBinding = generateBinding(pcfId, recTime1);

        final var bindingRtInfo = new BindingRtInfo(UUID.randomUUID(),
                                                    Instant.parse("2022-06-04T11:56:12.5Z"),
                                                    Optional.of(pcfBinding.getPcfId()),
                                                    pcfBinding.getRecoveryTime() != null ? Optional.of(pcfBinding.getRecoveryTime()) : Optional.empty());

        final var pcfRt = extractPcfRt(pcfBinding).get();

        pcfSvc.reportRecoveryTime(pcfRt).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        TimeUnit.SECONDS.sleep(1);

        assertTrue(pcfSvc.checkStaleBinding(bindingRtInfo).blockingGet().isEmpty());

        final var recTime2 = new RecoveryTime("2032-05-04T11:56:12.5Z");

        final var pcfRt2 = new PcfRt(pcfId, Instant.from(recTime2.parse()));

        pcfSvc.reportRecoveryTime(pcfRt2).test().awaitCount(1).assertValues(PcfRtService.RecoveryTimeStatus.UPDATED);
        TimeUnit.SECONDS.sleep(1);

        assertTrue(pcfSvc.checkStaleBinding(bindingRtInfo).blockingGet().isPresent());
    }

    private void logSvcType(PcfRtService pcfSvc)
    {

        if (pcfSvc instanceof PcfRtServiceImpl)
        {
            log.info("Running simple svc");
        }
        else if (pcfSvc instanceof PcfRtCachedServiceImpl)
        {
            log.info("Running cached svc");
        }
    }

    private PcfBinding generateBinding(UUID id,
                                       RecoveryTime recoveryTime)
    {

        List<IpEndPoint> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(IpEndPoint.createJson("10.0.0.1", null, "TCP", 1024));

        return PcfBinding.create("imsi-12345",
                                 "msisdn-306972909290",
                                 null,
                                 new Ipv6Prefix("2001:db8:abcd:0012::0/64"),
                                 null,
                                 null,
                                 "testDnn",
                                 null, // fqdn
                                 pcfIpEndPointsJson,
                                 new DiameterIdentity("pcf-diamhost.gr"),
                                 new DiameterIdentity("pcf-diamrealm.gr"),
                                 Snssai.create(2, "DEADF0"),
                                 id,
                                 recoveryTime,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);

    }

    private Optional<PcfRt> extractPcfRt(PcfBinding binding)
    {
        try
        {
            return Optional.of(new PcfRt(binding.getPcfId(), Instant.parse(binding.getRecoveryTimeString())));
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

}
