package com.ericsson.esc.bsf.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.esc.bsf.db.MockNbsfManagementService;
import com.ericsson.esc.bsf.db.MockPcfRtService;
import com.ericsson.esc.bsf.openapi.model.BindingLevel;
import com.ericsson.esc.bsf.openapi.model.DiameterIdentity;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.esc.bsf.openapi.model.UeAddress;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DeregisterResult;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DiscoveryResult;
import com.ericsson.esc.bsf.worker.NBsfManagementServiceRt;
import com.ericsson.esc.bsf.worker.RecoveryTimeConfig;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.google.common.net.InetAddresses;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

public class NbsfManagementRtTest
{

    private static final Logger log = LoggerFactory.getLogger(NbsfManagementRtTest.class);
    private final Flowable<RecoveryTimeConfig> rtResolution = Flowable.just(new RecoveryTimeConfig(true, true));

    @Test(enabled = false) // Todo proper error handling using reportRecoveryTime
    public void discoverReportErrorTest()
    {
//        These 3 services can't be global. Each TC manipulates them to verify functionality.
//        In each TC we need clean objects of these services
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date = "1985-04-13T23:50:50.52Z";
        final RecoveryTime recoveryTime = new RecoveryTime(date);
        final UUID pcfId = UUID.randomUUID();

        final var regBinding = generateTestBinding(pcfId, recoveryTime);

        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();

//      reportRecoveryTime returns Error. writeTime > pcfRt.recTime (in etcd)

        var recTimeEtcd = Instant.parse("1992-04-13T23:50:50.52Z");

        pcfRtSvc.setPcfRt(pcfId, new PcfRt(pcfId, recTimeEtcd));
        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.ERROR);

        var addr = (Inet4Address) InetAddresses.forString("10.11.12.13");
        var ueadr = new UeAddress(addr, Optional.empty());
        var query = new DiscoveryQuery.UeAddr(ueadr);

        var res = dbRt.discovery(query).blockingGet();

        assertEquals(regBinding, res.getPcfBinding().get());
        assertEquals(res.getResult(), DiscoveryResult.Status.OK);

//      reportRecoveryTime returns STALE. writeTime < pcfRt.recTime (in etcd)

        recTimeEtcd = Instant.parse("2077-04-13T23:50:50.52Z");
        pcfRtSvc.setPcfRt(pcfId, new PcfRt(pcfId, recTimeEtcd));

        res = dbRt.discovery(query).blockingGet();
        assertEquals(res.getResult(), DiscoveryResult.Status.NOT_FOUND);
    }

    @Test(enabled = true)
//    RtStatus.NO_UPDATED -> PcfRtService.RecoveryTimeStatus.STALE
//    RecoveryTimeStatus.STALE triggers staleHandler there are two possible paths
//    1. writeTime is after RecoveryTime -> discovery result is returned
//    2. writeTime is before RecoveryTime -> considered stale
    public void discoverStaleInEtcdTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        PublishSubject<RecoveryTimeConfig> rtResolutionLocal = PublishSubject.create();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolutionLocal.toFlowable(BackpressureStrategy.LATEST));

        final String date = "1985-04-13T23:50:50.52Z";
        final RecoveryTime recoveryTime = new RecoveryTime(date);
        final UUID pcfId = UUID.randomUUID();

        final var regBinding = generateTestBinding(pcfId, recoveryTime);

//      check discovery with disabled RT. Old behavior of discovery is expected
        rtResolutionLocal.onNext(new RecoveryTimeConfig(false, true));
        log.debug("Rt: false");
        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();
        this.testDiscover(pcfRtSvc, dbRt, regBinding, false);

//      check discovery with enabled RT. NOT_FOUND reply should be returned on stale binding discovery.
        rtResolutionLocal.onNext(new RecoveryTimeConfig(true, true));
        log.debug("Rt: true");
        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();
        this.testDiscover(pcfRtSvc, dbRt, regBinding, true);

    }

    @Test(enabled = true)
    public void discoverNoRecoveryTimeTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);
        final UUID pcfId = UUID.randomUUID();

        final var regBinding = generateTestBinding(pcfId, null);

        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();

//      reportRecoveryTime returns STALE. writeTime > pcfRt.recTime (in etcd)

        var recTimeEtcd = Instant.parse("1992-04-13T23:50:50.52Z");

        pcfRtSvc.setPcfRt(pcfId, new PcfRt(pcfId, recTimeEtcd));
        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.NO_UPDATED);

        var addr = (Inet4Address) InetAddresses.forString("10.11.12.13");
        var ueadr = new UeAddress(addr, Optional.empty());
        var query = new DiscoveryQuery.UeAddr(ueadr);

        var res = dbRt.discovery(query).blockingGet();
        var registeredBindingId = res.getPcfBinding().get().getBindingId();

        assertEquals(regBinding, res.getPcfBinding().get());
        assertEquals(res.getResult(), DiscoveryResult.Status.OK);

//      reportRecoveryTime returns STALE. writeTime < pcfRt.recTime (in etcd)

        recTimeEtcd = Instant.parse("2077-04-13T23:50:50.52Z");
        pcfRtSvc.setPcfRt(pcfId, new PcfRt(pcfId, recTimeEtcd));

        res = dbRt.discovery(query).blockingGet();
        assertEquals(res.getResult(), DiscoveryResult.Status.NOT_FOUND);
        assertEquals(res.getStaleBindings().get(0).getBindingId(), registeredBindingId);
        assertEquals(res.getStaleBindings().get(0).getPcfId(), pcfId);
    }

    @Test(enabled = true)
//    checks both RtStatus.UPDATED and RtStatus.ALREADY_EXISTS behavior
//    the behavior should be the same (thats why both of them are sharing the same TC)
    public void discoverReportNonStaleTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date = "2022-04-13T23:50:50.52Z";
        final RecoveryTime recoveryTime = new RecoveryTime(date);
        final UUID pcfId = UUID.randomUUID();

        final var regBinding = generateTestBinding(pcfId, recoveryTime);

        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();

//      reportRecoveryTime returns UPDATED. writeTime > recTime 

        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.UPDATED);

        var addr = (Inet4Address) InetAddresses.forString("10.11.12.13");
        var ueadr = new UeAddress(addr, Optional.empty());
        var query = new DiscoveryQuery.UeAddr(ueadr);

        var res = dbRt.discovery(query).blockingGet();

        assertEquals(regBinding, res.getPcfBinding().get());
        assertEquals(res.getResult(), DiscoveryResult.Status.OK);

//      reportRecoveryTime returns ALREADY_EXISTS. writeTime > recTime 

        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.ALREADY_EXISTS);

        res = dbRt.discovery(query).blockingGet();

        assertEquals(regBinding, res.getPcfBinding().get());
        assertEquals(res.getResult(), DiscoveryResult.Status.OK);

//      reportRecoveryTime returns ALREADY_EXISTS. writeTime < pcfRt.recTime (in etcd)
//      Change binding in order to meet writeTime < pcfRt.recTime criterium

        final String date2 = "2077-04-13T23:50:50.52Z";
        final RecoveryTime recoveryTime2 = new RecoveryTime(date2);
        final var regBinding2 = generateTestBinding(pcfId, recoveryTime2);

//      Deletes the first binding and registers the new one
        dbRt.deregister(res.getPcfBinding().get().getBindingId())
            .filter(derRes -> DeregisterResult.OK.equals(derRes))
            .ignoreElement()
            .andThen(dbRt.register(regBinding2, 0)//
                         .doOnError(err -> log.error("Registration failed: ", err)))
            .blockingGet();

        res = dbRt.discovery(query).blockingGet();

        assertEquals(res.getResult(), DiscoveryResult.Status.OK);
        assertEquals(res.getStaleBindings(), Collections.emptyList());

//      reportRecoveryTime returns ALREADY_EXISTS. writeTime < pcfRt.recTime (in etcd)

        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.UPDATED);

        dbRt.register(regBinding2, 0).blockingGet();
        res = dbRt.discovery(query).blockingGet();

        assertEquals(res.getResult(), DiscoveryResult.Status.OK);
        assertEquals(res.getStaleBindings(), Collections.emptyList());

    }

    @Test(enabled = true)
    public void discoverBindingWithoutPcfIdTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);
        final var regBinding = generateTestBinding(null, null);

        dbRt.register(regBinding, 0).doOnError(err -> log.error("Registration failed: ", err)).blockingGet();

        var addr = (Inet4Address) InetAddresses.forString("10.11.12.13");
        var ueadr = new UeAddress(addr, Optional.empty());
        var query = new DiscoveryQuery.UeAddr(ueadr);

        var res = dbRt.discovery(query).blockingGet();

        assertEquals(regBinding, res.getPcfBinding().get());
        assertEquals(res.getResult(), DiscoveryResult.Status.OK);
    }

    @Test(enabled = true)
    public void registerUpdateTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date1 = "1985-04-13T23:20:50.52Z";
        final RecoveryTime recoveryTime1 = new RecoveryTime(date1);
        final String date2 = "1985-04-13T23:50:50.52Z";
        final RecoveryTime recoveryTime2 = new RecoveryTime(date2);

        final UUID pcfId1 = UUID.randomUUID();
        final UUID pcfId2 = UUID.randomUUID();

        final var bindings = List.of(generateTestBinding(pcfId1, recoveryTime1), generateTestBinding(pcfId2, recoveryTime2), generateTestBinding(null, null));
        final var registeredBindings = bindings.stream().map(binding -> dbRt.register(binding, 0).blockingGet().getPcfBinding()).collect(Collectors.toList());

        assertEquals(Set.copyOf(bindings), Set.copyOf(registeredBindings));
        assertTrue(pcfRtSvc.getReportCount() == bindings.size() - 1);
    }

    @Test(enabled = true)
    public void registerNoUpdateTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date1 = "1985-04-13T23:20:50.52Z";
        final RecoveryTime recoveryTime1 = new RecoveryTime(date1);
        final UUID pcfId = UUID.randomUUID();

        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.NO_UPDATED);

        final var binding = generateTestBinding(pcfId, recoveryTime1);
        final var res = dbRt.register(binding, 0).blockingGet();

        assertEquals(res.getPcfBinding(), binding);
        assertTrue(pcfRtSvc.getReportCount() == 1);
    }

    @Test(enabled = true)
    public void registerReportErrorTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date1 = "1985-04-13T23:20:50.52Z";
        final RecoveryTime recoveryTime1 = new RecoveryTime(date1);
        final UUID pcfId1 = UUID.randomUUID();

        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.ERROR);

        final var binding = generateTestBinding(pcfId1, recoveryTime1);
        final var res = dbRt.register(binding, 0).blockingGet();

        assertEquals(res.getPcfBinding(), binding);
        assertTrue(pcfRtSvc.getReportCount() == 1);
    }

    @Test(enabled = true)
    public void registerErrorTest()
    {
        final MockPcfRtService pcfRtSvc = new MockPcfRtService();
        final MockNbsfManagementService db = new MockNbsfManagementService();
        final NBsfManagementServiceRt dbRt = new NBsfManagementServiceRt(db, pcfRtSvc, rtResolution);

        final String date1 = "1985-04-13T23:20:50.52Z";
        final RecoveryTime recoveryTime1 = new RecoveryTime(date1);
        final UUID pcfId1 = UUID.randomUUID();

        db.setState(MockNbsfManagementService.State.ERROR);

        final var binding = generateTestBinding(pcfId1, recoveryTime1);

        try
        {
            dbRt.register(binding, 0).blockingGet();
        }
        catch (Exception e)
        {
            // log.warn("Catched {}", e);
        }

        assertTrue(pcfRtSvc.getReportCount() == 0);
    }

    private void testDiscover(MockPcfRtService pcfRtSvc,
                              NBsfManagementServiceRt dbRt,
                              PcfBinding regBinding,
                              boolean checkRt)
    {

        log.info("Running with Rt: {}", checkRt);
        var pcfId = regBinding.getPcfId();

//      reportRecoveryTime returns STALE. writeTime is after pcfRt.recTime (in etcd)
//      discovery result is returned 

        var recTimeEtcd = Instant.parse("1992-04-13T23:50:50.52Z");

        pcfRtSvc.setPcfRt(pcfId, new PcfRt(pcfId, recTimeEtcd));
        pcfRtSvc.setRtStatus(MockPcfRtService.RtStatus.NO_UPDATED);

        var addr = (Inet4Address) InetAddresses.forString("10.11.12.13");
        var ueadr = new UeAddress(addr, Optional.empty());
        var query = new DiscoveryQuery.UeAddr(ueadr);

        var res = dbRt.discovery(query).blockingGet();

//      checkRt is true: In that case the binding is stale and NOT_FOUND is returned as reply 
        if (checkRt)
        {

            assertEquals(res.getPcfBinding(), Optional.empty());

            assertEquals(res.getResult(), DiscoveryResult.Status.NOT_FOUND);

        }
//      checkRt is false - old behavior 
        else
        {
            var registeredBindingId = res.getPcfBinding().get().getBindingId();

            assertEquals(regBinding, res.getPcfBinding().get());
            assertEquals(res.getResult(), DiscoveryResult.Status.OK);

            dbRt.deregister(registeredBindingId).blockingGet();
        }

    }

    private static PcfBinding generateTestBinding(UUID pcfId,
                                                  RecoveryTime recoveryTime)
    {

        return PcfBinding.create("testSupi",
                                 "testGpsi",
                                 (Inet4Address) InetAddresses.forString("10.11.12.13"),
                                 null,
                                 null,
                                 null,
                                 "dnn",
                                 "testPcfFQDN",
                                 List.of(IpEndPoint.createJson("10.11.12.13", null, "TCP", 3868)),
                                 new DiameterIdentity("pcfDhost.gr"),
                                 new DiameterIdentity("testPcfDiamRealm.gr"),
                                 Snssai.create(6, "AF0456"),
                                 pcfId,
                                 recoveryTime,
                                 null,
                                 null,
                                 null,
                                 "set12.pcfset.5gc.mnc012.mcc345",
                                 new BindingLevel("NF_INSTANCE"));
    }
}
