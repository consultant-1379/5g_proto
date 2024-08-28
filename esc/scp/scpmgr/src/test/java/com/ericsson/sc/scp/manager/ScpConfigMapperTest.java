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
 * Created on: Aug 25, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.scp.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.service.FilterFactory;
import com.ericsson.sc.scp.model.ActionRoutePreferred;
import com.ericsson.sc.scp.model.ActionRouteStrict;
import com.ericsson.sc.scp.model.Address;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.IngressConnectionProfile;
import com.ericsson.sc.scp.model.MessageDatum;
import com.ericsson.sc.scp.model.MultipleIpEndpoint;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.OwnNetwork;
import com.ericsson.sc.scp.model.Path;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.NfPoolDiscovery;
import com.ericsson.sc.scp.model.PriorityGroup;
import com.ericsson.sc.scp.model.RoutingAction;
import com.ericsson.sc.scp.model.RoutingCase;
import com.ericsson.sc.scp.model.RoutingRule;
import com.ericsson.sc.scp.model.StaticNfInstance;
import com.ericsson.sc.scp.model.StaticNfInstanceDatum;
import com.ericsson.sc.scp.model.StaticNfService;
import com.ericsson.sc.scp.model.PriorityGroup;
import com.ericsson.sc.scp.model.TargetNfPool;
import com.ericsson.sc.scp.model.ServiceAddress;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;

@ExtendWith(MockitoExtension.class)
class ScpConfigMapperTest
{

    private static final Logger log = LoggerFactory.getLogger(ScpConfigMapperTest.class);

    @Mock
    EricssonScp scpCfgMock;

    @Mock
    EricssonScpScpFunction scpfunctionMock;
    @Mock
    NfInstance scpInstanceMock;

    @Mock
    ServiceAddress serviceAddressMock;

    @DisplayName("map EricssonScp  to ProxyConfig with CreateCluster")
    @Test
    @Tag("integration")
    void testToProxyCfg_with_basic_ingress_listener_and_clusters()
    {
        List<V1Service> svcList = new ArrayList<>();

        final BehaviorSubject<Optional<EricssonScp>> configFlow = BehaviorSubject.createDefault(Optional.<EricssonScp>empty());

        Flowable<Optional<ProxyCfg>> proxyCfgFlow = ScpCfgMapper.toProxyCfg(configFlow.toFlowable(BackpressureStrategy.LATEST), svcList);

        TestSubscriber<Optional<ProxyCfg>> proxyCfgTester = proxyCfgFlow.test();

        log.info("NEW STATIC CONFIGURATION");
        // configFlow.onNext(Optional.of(buildCScpReferenceConfig()));
        configFlow.onNext(Optional.of(buildEst2Config()));
        proxyCfgTester.awaitCount(1);

        // We expect:
        // 1 Listener
        // proxyCfgTester.assertValueAt(0, e -> e.get().getClusters().size() == 1 + 3 +
        // 1)

        proxyCfgTester.assertValueAt(1, e -> e.get().getListeners().size() == 1);
        proxyCfgTester.assertValueAt(1, e -> e.get().getClusters().size() == 6);

        proxyCfgTester.assertNotComplete();
        proxyCfgTester.assertNoErrors();

    }

    @DisplayName("map EricssonScp  to ProxyConfig print lua code")
    @Test
    @Tag("integration")
    void testToProxyCfg_with_basic_print_lua()
    {
        List<V1Service> svcList = new ArrayList<>();

        final BehaviorSubject<Optional<EricssonScp>> configFlow = BehaviorSubject.createDefault(Optional.<EricssonScp>empty());

        Flowable<Optional<ProxyCfg>> proxyCfgFlow = ScpCfgMapper.toProxyCfg(configFlow.toFlowable(BackpressureStrategy.LATEST), svcList);

        TestSubscriber<Optional<ProxyCfg>> proxyCfgTester = proxyCfgFlow.test();

        log.info("NEW EST1 STATIC CONFIGURATION");
        configFlow.onNext(Optional.of(buildEst2Config()));

        proxyCfgFlow.filter(Optional::isPresent).subscribe(cfg ->
        {
            var proxyCfg = cfg.get();
            for (var listener : proxyCfg.getListeners())
            {
                var luaCode = FilterFactory.getCsaLuaFilterString(listener.getRoutingContext());
                System.out.println("LUA code:");
                System.out.print(luaCode);
            }
        });

        proxyCfgTester.awaitCount(1);
        // We expect:
        // 1 Listener
        // proxyCfgTester.assertValueAt(0, e -> e.get().getClusters().size() == 1 + 3 +
        // 1)

        proxyCfgTester.assertValueAt(1, e -> e.get().getListeners().size() == 1);
        // TODO: fix expected #of cluster
        // proxyCfgTester.assertValueAt(1, e -> e.get().getClusters().size() == 3);

        proxyCfgTester.assertNotComplete();
        proxyCfgTester.assertNoErrors();

    }

    private EricssonScp buildCScpReferenceConfig()
    {
        EricssonScp refCfg = new EricssonScp();
        EricssonScpScpFunction refScpFnct = new EricssonScpScpFunction();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        List<ServiceAddress> serviceAddresses = new ArrayList<>();
        NfInstance cScp = new NfInstance();
        cScp.setName("cScp");

        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80).withFqdn("own_fqdn");
        serviceAddresses.add(aSvcAddr);
        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ownNetworks.add(aNetwork);
        cScp.withOwnNetwork(ownNetworks);
        cScp.withServiceAddress(serviceAddresses);

        NfPool rp1_scp_pool = new NfPool().withName("rp_1_scp_pool");

        NfPoolDiscovery rp1_pool_discovery = new NfPoolDiscovery().withName("rp_1_pd")
                                                                  .withStaticNfInstanceDataRef(Arrays.asList(("rp_1_scp_static_nf_instance_data_ref")));

        rp1_scp_pool.setNfPoolDiscovery(Arrays.asList(rp1_pool_discovery));

        cScp.withNfPool(Arrays.asList(rp1_scp_pool));

        StaticNfInstance rp1Pscp1 = createStaticNfInstance("rp1_pScp1", "10.10.10.1", 80);
        StaticNfInstanceDatum rp1_pScpData = new StaticNfInstanceDatum().withName("rp_1_scp_static_nf_instance_data_ref")
                                                                        .withStaticNfInstance(Arrays.asList(rp1Pscp1));

        cScp.setStaticNfInstanceData(Arrays.asList(rp1_pScpData));

        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(cScp);

        refScpFnct.setNfInstance(nfInstances);

        refCfg.withEricssonScpScpFunction(refScpFnct);

        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

    private StaticNfInstance createStaticNfInstance(String name,
                                                    String ipv4,
                                                    int port)
    {
        return createStaticNfInstanceWithFqdn(name, null, ipv4, port);
    }

    private StaticNfInstance createStaticNfInstanceWithFqdn(String name,
                                                            String fqdn,
                                                            String ipv4,
                                                            int port)
    {
        // List<StaticNfService> nfServ =
        StaticNfService nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value())
                                                      .withAddress(new Address().withFqdn(fqdn)
                                                                                .withScheme(Scheme.HTTP)
                                                                                .withMultipleIpEndpoint(Arrays.asList(new MultipleIpEndpoint().withPort(port)
                                                                                                                                              .withIpv4Address(Arrays.asList(ipv4)))));
        return new StaticNfInstance().withName(name).withStaticNfService(List.of(nfServ));

    }

    /**
     * EST1 - Envoy Standalone 1 Configuration (sample_est1_config.netconf)
     * 
     * @return reference configuration
     */
    private EricssonScp buildEst1Config()
    {
        EricssonScp refCfg = new EricssonScp();
        EricssonScpScpFunction refScpFnct = new EricssonScpScpFunction();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        List<ServiceAddress> serviceAddresses = new ArrayList<>();
        // SCP general attributes
        NfInstance scp = new NfInstance();
        scp.setName("instance_1");

        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address("10.10.10.1").withName("vpn_own").withPort(80).withFqdn("scp.ericsson.se");
        serviceAddresses.add(aSvcAddr);
        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName()).withRoutingCaseRef("default_routing");
        ownNetworks.add(aNetwork);
        scp.withOwnNetwork(ownNetworks);
        scp.withServiceAddress(serviceAddresses);

        // Pools and -discovery
        // 1. Pool with statically discovered nodes (scp)
        StaticNfInstance rpAScp1 = createStaticNfInstanceWithFqdn("nfp1-chfsim1", "nfp1.mnc.123.mcc.123.com", "10.98.47.205", 80);

        StaticNfInstance rpAScp2 = createStaticNfInstanceWithFqdn("nfp2-chfsim2", "nfp2.mnc.123.mcc.123.com", "10.104.193.139", 80);
        StaticNfInstanceDatum rpAStaticScpData = new StaticNfInstanceDatum().withName("static_scp").withStaticNfInstance(Arrays.asList(rpAScp1, rpAScp2));

        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("scp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_scp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpAScpPool = new NfPool().withName("scp");
        rpAScpPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> scpSubpools = new ArrayList<>();
        scpSubpools.add(new PriorityGroup().withName("scp_subpool").withPriority(1));
        rpAScpPool.withPriorityGroup(scpSubpools);
        pools.add(rpAScpPool);

        // 2. Pool with statically discovered nodes (int)
        StaticNfInstance int1 = createStaticNfInstanceWithFqdn("nfp3-chfsim3", "nfp3.mnc.456.mcc.456.com", "110.99.75.200", 80);
        StaticNfInstance int2 = createStaticNfInstanceWithFqdn("nfp4-chfsim4", "nfp4.mnc.456.mcc.456.com", "10.99.75.201", 80);

        StaticNfInstanceDatum staticIntData = new StaticNfInstanceDatum().withName("static_int").withStaticNfInstance(Arrays.asList(int1, int2));

        NfPoolDiscovery intPoolDiscovery = new NfPoolDiscovery().withName("int_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_int")));

        NfPool intScpPool = new NfPool().withName("scp");
        intScpPool.setNfPoolDiscovery(Arrays.asList(intPoolDiscovery));

        List<PriorityGroup> intSubpools = new ArrayList<>();
        intSubpools.add(new PriorityGroup().withName("scp_subpool").withPriority(1));
        intScpPool.withPriorityGroup(intSubpools);
        pools.add(intScpPool);

        // Add static node data and pools to scp config:
        scp.setStaticNfInstanceData(Arrays.asList(rpAStaticScpData, staticIntData));
        scp.withNfPool(pools);

        // Routing-Data
        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?P<nfp>.+?)\\..+?\\.(?P<mnc>.+?)\\..+?\\.(?P<mcc>.+?)\\."); // Each backslash doubled from
                                                                                                                                // Netconf

        messageData.add(rd1);
        scp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing").withMessageDataRef(Arrays.asList("apiRoot_data"));

        ActionRoutePreferred actionRouteToPool = new ActionRoutePreferred().withTargetNfPool(new TargetNfPool().withNfPoolRef("int"));
        RoutingAction routeToPool = new RoutingAction().withActionRoutePreferred(actionRouteToPool);

        List<RoutingAction> routingActions = new ArrayList<>();
        routingActions.add(routeToPool);

        List<RoutingRule> routingRules = new ArrayList<>();
        routingRules.add(new RoutingRule().withName("pscp_to_ownPLMN").withCondition("var.mnc=='456' and var.mcc=='456'").withRoutingAction(routingActions));
// TODO       routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
//                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
//                                          .withActionRoamingPartner("scp"));
        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        scp.withRoutingCase(routingCases);

        // SCP general
        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(scp);

        refScpFnct.setNfInstance(nfInstances);

        refCfg.withEricssonScpScpFunction(refScpFnct);

        // Finish
        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

    /**
     * EST2 - Envoy Standalone 2 Configuration (sample_est2_config.netconf)
     * 
     * @return reference configuration
     */
    private EricssonScp buildEst2Config()
    {
        EricssonScp refCfg = new EricssonScp();
        EricssonScpScpFunction refScpFnct = new EricssonScpScpFunction();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        List<ServiceAddress> serviceAddresses = new ArrayList<>();
        // SCP general attributes
        NfInstance scp = new NfInstance();
        scp.setName("instance_1");
        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address("10.10.10.1").withName("vpn_own").withPort(80).withFqdn("scp.ericsson.se");
        serviceAddresses.add(aSvcAddr);
        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName()).withRoutingCaseRef("default_routing");
        ownNetworks.add(aNetwork);
        scp.withOwnNetwork(ownNetworks);
        scp.withServiceAddress(serviceAddresses);

        // Pools and -discovery
        // 1. Pool with statically discovered nodes (scp)
        StaticNfInstance rpAScp1 = createStaticNfInstanceWithFqdn("nfp1-chfsim1", "nfp1.mnc.123.mcc.123.com", "10.98.47.205", 80);
        StaticNfInstance rpAScp2 = createStaticNfInstanceWithFqdn("nfp1-chfsim2", "nfp2.mnc.123.mcc.123.com", "10.104.193.139", 80);

        StaticNfInstanceDatum rpAStaticScpData = new StaticNfInstanceDatum().withName("static_scp").withStaticNfInstance(Arrays.asList(rpAScp1, rpAScp2));
        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("scp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_scp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpAScpPool = new NfPool().withName("scp");
        rpAScpPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> scpSubpools = new ArrayList<>();
        scpSubpools.add(new PriorityGroup().withName("scp_subpool").withPriority(1));
        rpAScpPool.withPriorityGroup(scpSubpools);
        pools.add(rpAScpPool);

        // 2. Pool with statically discovered nodes (int)
        StaticNfInstance int1 = createStaticNfInstanceWithFqdn("nfp3-chfsim3", "nfp3.mnc.456.mcc.456.com", "110.99.75.200", 80);
        StaticNfInstance int2 = createStaticNfInstanceWithFqdn("nfp4-chfsim4", "nfp4.mnc.456.mcc.456.com", "10.99.75.201", 80);

        StaticNfInstanceDatum staticIntData = new StaticNfInstanceDatum().withName("static_int").withStaticNfInstance(Arrays.asList(int1, int2));

        NfPoolDiscovery intPoolDiscovery = new NfPoolDiscovery().withName("int_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_int")));

        NfPool intScpPool = new NfPool().withName("scp");
        intScpPool.setNfPoolDiscovery(Arrays.asList(intPoolDiscovery));

        List<PriorityGroup> intSubpools = new ArrayList<>();
        intSubpools.add(new PriorityGroup().withName("scp_subpool").withPriority(1));
        intScpPool.withPriorityGroup(intSubpools);
        pools.add(intScpPool);

        // Add static node data and pools to scp config:
        scp.setStaticNfInstanceData(Arrays.asList(rpAStaticScpData, staticIntData));
        scp.withNfPool(pools);

        // Routing-Data
        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?<nfp>.+?)\\..+?\\.(?<mnc>.+?)\\..+?\\.(?<mcc>.+?)\\."); // Each backslash doubled from
                                                                                                                             // Netconf
        messageData.add(rd1);
        MessageDatum rd2 = new MessageDatum().withName("apiRoot_var").withHeader("3gpp-Sbi-target-apiRoot").withVariableName("apiroot_copy");
        messageData.add(rd2);
        MessageDatum rd3 = new MessageDatum().withName("path_var").withPath(new Path()).withVariableName("path_copy");
        messageData.add(rd3);
        MessageDatum rd4 = new MessageDatum().withName("path_data").withPath(new Path()).withExtractorRegex("nchf(?<proto>.+?)/"); // Each backslash
                                                                                                                                   // doubled
        scp.withMessageData(messageData);
        messageData.add(rd4);

        scp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing")
                                           .withMessageDataRef(Arrays.asList("apiRoot_data", "apiRoot_var", "path_var", "path_data"));

        ActionRoutePreferred actionRouteToPool = new ActionRoutePreferred().withTargetNfPool(new TargetNfPool().withNfPoolRef("int"));
        RoutingAction routeToPool = new RoutingAction().withActionRoutePreferred(actionRouteToPool);

        List<RoutingAction> routingActions = new ArrayList<>();
        routingActions.add(routeToPool);

        List<RoutingRule> routingRules = new ArrayList<>();
        routingRules.add(new RoutingRule().withName("pscp_to_ownPLMN").withCondition("var.mnc=='456' and var.mcc=='456'").withRoutingAction(routingActions));
// TODO       routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
//                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
//                                          .withActionRoamingPartner("scp"));
        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        scp.withRoutingCase(routingCases);

        // SCP general
        List<NfInstance> nfInstances = new ArrayList<>();

        List<IngressConnectionProfile> ingressConnectionProfiles = new ArrayList<>();
        IngressConnectionProfile aConnProf = new IngressConnectionProfile().withName("aIngressConn");
        ingressConnectionProfiles.add(aConnProf);
        scp.withIngressConnectionProfile(ingressConnectionProfiles);
        scp.withIngressConnectionProfileRef("aIngressConn");

        nfInstances.add(scp);

        refScpFnct.setNfInstance(nfInstances);

        refCfg.withEricssonScpScpFunction(refScpFnct);

        // Finish
        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

    /**
     * Combined C-SCP and P-SCP configuration matching the sample_poc_config.netconf
     * 
     * @return reference configuration
     */
    private EricssonScp buildPocSampleReferenceConfig()
    {
        EricssonScp refCfg = new EricssonScp();
        EricssonScpScpFunction refScpFnct = new EricssonScpScpFunction();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        List<ServiceAddress> serviceAddresses = new ArrayList<>();
        // SCP general attributes
        NfInstance scp = new NfInstance();
        scp.setName("instance_1");
        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address("10.10.10.1").withName("vpn_own").withPort(80).withFqdn("scp.ericsson.se");
        serviceAddresses.add(aSvcAddr);
        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName()).withRoutingCaseRef("default_routing");
        ownNetworks.add(aNetwork);
        scp.withOwnNetwork(ownNetworks);
        scp.withServiceAddress(serviceAddresses);

        // Pools and -discovery
        // 1. Pool with statically discovered nodes
        StaticNfInstance rpAScp1 = createStaticNfInstanceWithFqdn("scp1", "scp-1.rp-1.com", "10.10.11.1", 80);
        StaticNfInstanceDatum rpAStaticScpData = new StaticNfInstanceDatum().withName("static_scp").withStaticNfInstance(Arrays.asList(rpAScp1));
        scp.setStaticNfInstanceData(Arrays.asList(rpAStaticScpData));

        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("scp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_scp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpAScpPool = new NfPool().withName("scp");
        rpAScpPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> subpools = new ArrayList<>();
        subpools.add(new PriorityGroup().withName("scp_subpool").withPriority(1));
        rpAScpPool.withPriorityGroup(subpools);
        pools.add(rpAScpPool);

        // 2. Pool with dynamic-forwarding proxy (i.e. no discovered or configured
        // nodes):
        NfPool rpAUniversalPool = new NfPool().withName("universal_pool");
        pools.add(rpAUniversalPool);

        // add pools to scp config:
        scp.withNfPool(pools);

        // Routing-Data
        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?<nfp>.+?)\\..+?\\.(?<mnc>.+?)\\..+?\\.(?<mcc>.+?)\\."); // Each backslash doubled from
                                                                                                                             // Netconf

        messageData.add(rd1);
        scp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing").withMessageDataRef(Arrays.asList("apiRoot_data"));

        ActionRouteStrict actionRouteToPool = new ActionRouteStrict().withTargetNfPool(new TargetNfPool().withNfPoolRef("universal_pool"));
        RoutingAction routeToPool = new RoutingAction().withActionRouteStrict(actionRouteToPool);

        List<RoutingAction> routingActions = new ArrayList<>();
        routingActions.add(routeToPool);

        List<RoutingRule> routingRules = new ArrayList<>();
        routingRules.add(new RoutingRule().withName("pscp_to_ownPLMN").withCondition("var.mnc=='456' and var.mcc=='456'").withRoutingAction(routingActions));
// TODO       routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
//                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
//                                          .withActionRoamingPartner("rp_A"));
        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        scp.withRoutingCase(routingCases);

        // SCP general
        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(scp);

        refScpFnct.setNfInstance(nfInstances);

        refCfg.withEricssonScpScpFunction(refScpFnct);

        // Finish
        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

}
