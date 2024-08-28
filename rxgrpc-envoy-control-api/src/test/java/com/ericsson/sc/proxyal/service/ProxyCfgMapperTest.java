package com.ericsson.sc.proxyal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.fm.Alarm;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;

@ExtendWith(MockitoExtension.class)
class ProxyCfgMapperTest
{
    private static final Logger log = LoggerFactory.getLogger(ProxyCfgMapperTest.class);

    private ProxyCfgMapper pxCfgMapper;

    final BehaviorSubject<Optional<ProxyCfg>> pxCfgFlowIn = BehaviorSubject.create();

    final Set<IpFamily> defaultIpFamilies = Set.of(IpFamily.IPV4, IpFamily.IPV6);

    @Mock
    IfDnsCache dnsCacheMock;
    final BehaviorSubject<Map<String, IfDnsLookupContext>> dnsCacheUpdates = BehaviorSubject.create();

    @Mock
    Alarm alarmMock;

    @BeforeEach
    void setUp() throws Exception
    {
        lenient().when(dnsCacheMock.getResolvedHosts()).thenReturn(dnsCacheUpdates.toFlowable(BackpressureStrategy.LATEST));
        pxCfgMapper = new ProxyCfgMapper(dnsCacheMock, pxCfgFlowIn.toFlowable(BackpressureStrategy.LATEST));
        pxCfgMapper.start().subscribe();
        dnsCacheUpdates.onNext(new HashMap<>());
    }

    @AfterEach
    void tearDown() throws Exception
    {
        pxCfgMapper.stop().subscribe();
    }

    /**
     * 
     * 
     * Tests based on the new API
     * 
     * @throws JsonProcessingException
     * 
     * 
     * 
     */

    @Tag("integration")
    @Test
    void test_mapHostNamesToIps_emptyPxCfg_emptyDnsUpdate() throws JsonProcessingException
    {
        // test the initial scenario, new (empty) PxCfg., emptyDnsCache

        ProxyCfg inCfg = new ProxyCfg("testNf");

        pxCfgFlowIn.onNext(Optional.of(inCfg));

        Flowable<Optional<ProxyCfg>> pxCfgFlowOut = pxCfgMapper.getMappedProxyConfigs();
        TestSubscriber<Optional<ProxyCfg>> tester = pxCfgFlowOut.test();

        tester.awaitCount(1);
        tester.assertValueAt(0, e -> e.get().equals(inCfg));

        tester.assertValueCount(1);

        tester.assertNotComplete();
        tester.assertNoErrors();
        tester.assertNoTimeout();

        verify(alarmMock, times(0)).raiseMajor();
        verify(alarmMock, times(0)).cease();
    }

    @Tag("integration")
    @Test
    void test_mapHostNamesToIps_PxCfg_with_hostname_mapped_to_ip() throws JsonProcessingException
    {
        // the input cfg contains one endpoint with hostname as address
        // the dnsCache publishes an update containing the ipadress for this hostname
        // check the ipadress in the mapped ProxyCfg

        final String exp_ep_ip_address = "10.20.30.40";

        ProxyCfg inCfg = new ProxyCfg("testNf");
        ProxyCluster inCluster = new ProxyCluster("testCluster");

        inCluster.addEndpoint(new ProxyEndpoint("ep_hostname", 80));
        inCfg.addCluster(inCluster);

        Map<String, IfDnsLookupContext> testCache = new HashMap<>();
        dnsCacheUpdates.onNext(testCache);

        pxCfgFlowIn.onNext(Optional.of(inCfg));
        Flowable<Optional<ProxyCfg>> pxCfgFlowOut = pxCfgMapper.getMappedProxyConfigs();

        TestSubscriber<Optional<ProxyCfg>> tester = pxCfgFlowOut.test();

        testCache = new HashMap<>();
        testCache.put("ep_hostname", DnsCache.LookupContext.of(exp_ep_ip_address, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);
        log.info("tester.values(): {}", tester.values());
        log.info("tester.values().get(0): {}", tester.values().get(0));

        String actual_ep_ip_address = tester.values().get(0).get().getClusters().get(0).getEndpoints().get(0).getIpAddress().get();
        String actual_ep_address = tester.values().get(0).get().getClusters().get(0).getEndpoints().get(0).getAddress();

        assertEquals("ep_hostname", actual_ep_address);
        assertEquals(exp_ep_ip_address, actual_ep_ip_address);

        tester.assertValueCount(1);

        tester.assertNotComplete();
        tester.assertNoErrors();
        tester.assertNoTimeout();

        verify(alarmMock, times(0)).raiseMajor();
        verify(alarmMock, times(0)).cease();

    }

    @Tag("integration")
    @Test
    void test_mapHostNamesToIps_PxCfg_with_hostname_wait_for_cache_update() throws JsonProcessingException
    {
        // the input cfg contains one endpoint with hostname as address
        // the dnsCache publishes an update containing the ipadress for this hostname
        // after the proxy cfg is pushed
        // check the ipadress in the mapped ProxyCfg after the dns is resolved

        final String exp_ep_ip_address = "ep_ip_add";

        ProxyCfg inCfg = new ProxyCfg("testNf");
        ProxyCluster inCluster = new ProxyCluster("testCluster");

        inCluster.addEndpoint(new ProxyEndpoint("ep_hostname", 80));
        inCfg.addCluster(inCluster);

        Flowable<Optional<ProxyCfg>> pxCfgFlowOut = pxCfgMapper.getMappedProxyConfigs();

        TestSubscriber<Optional<ProxyCfg>> tester = pxCfgFlowOut.test();

        pxCfgFlowIn.onNext(Optional.of(inCfg));

        Map<String, IfDnsLookupContext> testCache = new HashMap<>();
        testCache.put("unother_hostname", DnsCache.LookupContext.of(exp_ep_ip_address, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);

        String actual_ep_address = tester.values().get(0).get().getClusters().get(0).getEndpoints().get(0).getAddress();

        assertEquals("ep_hostname", actual_ep_address);
        assertEquals(tester.values().get(0).get().getClusters().get(0).getEndpoints().get(0).getIpAddress(), Optional.empty());

        // update cache and verify correct mapping
        testCache = new HashMap<>();
        testCache.put("ep_hostname", DnsCache.LookupContext.of(exp_ep_ip_address, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);

        String actual_ep_ip_address = tester.values().get(1).get().getClusters().get(0).getEndpoints().get(0).getIpAddress().get();
        actual_ep_address = tester.values().get(1).get().getClusters().get(0).getEndpoints().get(0).getAddress();

        assertEquals("ep_hostname", actual_ep_address);
        assertEquals(exp_ep_ip_address, actual_ep_ip_address);

        tester.assertValueCount(2);

        tester.assertNotComplete();
        tester.assertNoErrors();
        tester.assertNoTimeout();

        verify(alarmMock, times(1)).raiseMajor("unresolved hosts:[ep_hostname].");
        verify(alarmMock, times(1)).cease();

    }

    @Tag("integration")
    @Test
    void test_mapHostNamesToIps_PxCfg_with_2hostnames_wait_for_cache_update() throws JsonProcessingException
    {
        // the input cfg contains 2 clusters with one endpoint each with hostname as
        // address
        // the dnsCache first publishes an update containing the ipadress first cluster
        // the dnsCache then publishes an update containing the ipadress second cluster

        ProxyCfg inCfg1 = new ProxyCfg("testNf");

        String cl1_name = "cl1";
        String cl1_ep1_host = "cl1_ep1_hostname";
        String cl1_ep1_ip = "cl1_ep1_ip";

        ProxyCluster inCluster1 = new ProxyCluster(cl1_name);
        inCluster1.addEndpoint(new ProxyEndpoint(cl1_ep1_host, 80));
        inCfg1.addCluster(inCluster1);

        String cl2_name = "cl2";
        String cl2_ep1_host = "cl2_ep1_hostname";
        String cl2_ep1_ip = "cl2_ep1_ip";
        ProxyCluster inCluster2 = new ProxyCluster(cl2_name);
        inCluster2.addEndpoint(new ProxyEndpoint(cl2_ep1_host, 80));
        inCfg1.addCluster(inCluster2);

        pxCfgFlowIn.onNext(Optional.of(inCfg1));

        Map<String, IfDnsLookupContext> testCache = new HashMap<>();

        Flowable<Optional<ProxyCfg>> pxCfgFlowOut = pxCfgMapper.getMappedProxyConfigs();

        TestSubscriber<Optional<ProxyCfg>> tester = pxCfgFlowOut.test();

        // 1nd update cache and verify correct mapping

        testCache.put(cl1_ep1_host, DnsCache.LookupContext.of(cl1_ep1_ip, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);
        String actual_ep_ip_address = tester.values().get(0).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getIpAddress().get();
        String actual_ep_address = tester.values().get(0).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl1_ep1_host, actual_ep_address);
        assertEquals(cl1_ep1_ip, actual_ep_ip_address);

        actual_ep_address = tester.values().get(0).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl2_ep1_host, actual_ep_address);
        assertEquals(Optional.empty(), tester.values().get(0).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getIpAddress());

        // Update Cache
        testCache = new HashMap<>();
        testCache.put(cl1_ep1_host, DnsCache.LookupContext.of(cl1_ep1_ip, this.defaultIpFamilies));
        testCache.put(cl2_ep1_host, DnsCache.LookupContext.of(cl2_ep1_ip, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);
        actual_ep_ip_address = tester.values().get(1).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getIpAddress().get();
        actual_ep_address = tester.values().get(1).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl1_ep1_host, actual_ep_address);
        assertEquals(cl1_ep1_ip, actual_ep_ip_address);

        actual_ep_ip_address = tester.values().get(1).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getIpAddress().get();

        actual_ep_address = tester.values().get(1).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl2_ep1_host, actual_ep_address);
        assertEquals(cl2_ep1_ip, actual_ep_ip_address);

        tester.assertValueCount(2);

        tester.assertNotComplete();
        tester.assertNoErrors();
        tester.assertNoTimeout();

        verify(alarmMock, times(1)).raiseMajor("unresolved hosts:[" + cl2_ep1_host + "].");
        verify(alarmMock, times(1)).cease();

    }

    @Tag("integration")
    @Test
    void test_mapHostNamesToIps_2PxCfgs_with_2hostnames_wait_for_cache_update()
    {
        // the input cfg 1 contains 1 clusters with one endpoint with hostname as
        // address
        // the dnsCache first publishes an update containing the ipadress first cluster

        // the input cfg 2 contains 1 clusters with one endpoint with hostname as
        // address
        // the dnsCache then publishes an update containing the ipadress second cluster

        ProxyCfg inCfg1 = new ProxyCfg("testNf");

        String cl1_name = "cl1";
        String cl1_ep1_host = "cl1_ep1_hostname";
        String cl1_ep1_ip = "cl1_ep1_ip";

        ProxyCluster inCluster1 = new ProxyCluster(cl1_name);
        inCluster1.addEndpoint(new ProxyEndpoint(cl1_ep1_host, 80));
        inCfg1.addCluster(inCluster1);
        // inCfg1.addListener(new ProxyListener("li1", "scpInstName", "scpServiceName",
        // "scpServiceAddrName"));

        String cl2_name = "cl2";
        String cl2_ep1_host = "cl2_ep1_hostname";
        String cl2_ep1_ip = "cl2_ep1_ip";
        ProxyCluster inCluster2 = new ProxyCluster(cl2_name);
        inCluster2.addEndpoint(new ProxyEndpoint(cl2_ep1_host, 80));

        Map<String, IfDnsLookupContext> testCache = new HashMap<>();
        dnsCacheUpdates.onNext(testCache);

        // Flowable<Optional<ProxyCfg>> pxCfgFlowOut =
        // pxCfgMapper.mapHostNamesToIps(pxCfgFlowIn.toFlowable(BackpressureStrategy.LATEST));
        Flowable<Optional<ProxyCfg>> pxCfgFlowOut = pxCfgMapper.getMappedProxyConfigs();

        TestSubscriber<Optional<ProxyCfg>> tester = pxCfgFlowOut.test();

        pxCfgFlowIn.onNext(Optional.of(inCfg1));

        // 1nd update cache and verify correct mapping
        testCache = new HashMap<>();
        testCache.put(cl1_ep1_host, DnsCache.LookupContext.of(cl1_ep1_ip, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);

        // Update Config
        inCfg1.addCluster(inCluster2);
        pxCfgFlowIn.onNext(Optional.of(inCfg1));

        // Update Cache
        testCache = new HashMap<>();
        ;
        testCache.put(cl1_ep1_host, DnsCache.LookupContext.of(cl1_ep1_ip, this.defaultIpFamilies));
        testCache.put(cl2_ep1_host, DnsCache.LookupContext.of(cl2_ep1_ip, this.defaultIpFamilies));
        dnsCacheUpdates.onNext(testCache);

        tester.awaitCount(1);

        String actual_ep_ip_address = tester.values().get(1).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getIpAddress().get();
        String actual_ep_address = tester.values().get(1).get().getClusterWithName(cl1_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl1_ep1_host, actual_ep_address);
        assertEquals(cl1_ep1_ip, actual_ep_ip_address);

        actual_ep_ip_address = tester.values().get(1).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getIpAddress().get();

        actual_ep_address = tester.values().get(1).get().getClusterWithName(cl2_name).get().getEndpoints().get(0).getAddress();

        assertEquals(cl2_ep1_host, actual_ep_address);
        assertEquals(cl2_ep1_ip, actual_ep_ip_address);

        tester.assertValueCount(2);

        tester.assertNotComplete();
        tester.assertNoErrors();
        tester.assertNoTimeout();

        HashSet<IfDnsLookupContext> requestedHostsExpected = new HashSet<>();
        requestedHostsExpected.add(DnsCache.LookupContext.of(cl1_ep1_host, this.defaultIpFamilies));
        verify(dnsCacheMock, times(1)).publishHostsToResolve(requestedHostsExpected);

        requestedHostsExpected.add(DnsCache.LookupContext.of(cl2_ep1_host, this.defaultIpFamilies));
        verify(dnsCacheMock, times(1)).publishHostsToResolve(requestedHostsExpected);

        // verify(alarmMock, times(1)).raiseMajor("unresolved hosts:[ep_hostname].");
        // verify(alarmMock, times(2)).cease();

    }

}
