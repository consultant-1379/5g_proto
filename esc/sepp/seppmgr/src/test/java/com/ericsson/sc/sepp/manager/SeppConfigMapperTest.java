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

package com.ericsson.sc.sepp.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.service.FilterFactory;
import com.ericsson.sc.sepp.model.ActionRoutePreferred;
import com.ericsson.sc.sepp.model.ActionRouteRoundRobin;
import com.ericsson.sc.sepp.model.Address;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.EricssonSeppSeppFunction;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.IngressConnectionProfile;
import com.ericsson.sc.sepp.model.MessageDatum;
import com.ericsson.sc.sepp.model.MultipleIpEndpoint;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.NfPoolDiscovery;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.Path;
import com.ericsson.sc.sepp.model.PriorityGroup;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoutingAction;
import com.ericsson.sc.sepp.model.RoutingCase;
import com.ericsson.sc.sepp.model.RoutingRule;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.sepp.model.StaticNfInstance;
import com.ericsson.sc.sepp.model.StaticNfInstanceDatum;
import com.ericsson.sc.sepp.model.StaticNfService;
import com.ericsson.sc.sepp.model.TargetNfPool;
import com.ericsson.sc.sepp.model.TargetRoamingPartner;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;

@ExtendWith(MockitoExtension.class)
class SeppConfigMapperTest
{

    private static final Logger log = LoggerFactory.getLogger(SeppConfigMapperTest.class);

    @Mock
    EricssonSepp seppCfgMock;

    @Mock
    EricssonSeppSeppFunction seppfunctionMock;
    @Mock
    NfInstance seppInstanceMock;

    @Mock
    ServiceAddress serviceAddressMock;

    @Mock
    CmmPatch cmPatch;

    @DisplayName("map EricssonSepp  to ProxyConfig with CreateCluster")
    @Test
    @Tag("integration")
    void testToProxyCfg_with_basic_ingress_listener_and_clusters()
    {

        List<V1Service> svcList = new ArrayList<>();

        final BehaviorSubject<Optional<EricssonSepp>> configFlow = BehaviorSubject.createDefault(Optional.<EricssonSepp>empty());
        var tmp = configFlow.toFlowable(BackpressureStrategy.LATEST);
        Flowable<Optional<Map<String, Triplet<String, String, Boolean>>>> tmp2 = Flowable.empty();
        Flowable<Optional<ProxyCfg>> proxyCfgFlow = SeppCfgMapper.toProxyCfg(Flowable.combineLatest(tmp, tmp2, Pair::of), cmPatch, svcList);

        TestSubscriber<Optional<ProxyCfg>> proxyCfgTester = proxyCfgFlow.test();

        log.info("NEW STATIC CONFIGURATION");
        // configFlow.onNext(Optional.of(buildCSeppReferenceConfig()));
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

    @DisplayName("map EricssonSepp  to ProxyConfig print lua code")
    @Test
    @Tag("integration")
    void testToProxyCfg_with_basic_print_lua()
    {
        List<V1Service> svcList = new ArrayList<>();

        final BehaviorSubject<Optional<EricssonSepp>> configFlow = BehaviorSubject.createDefault(Optional.<EricssonSepp>empty());

        var tmp = configFlow.toFlowable(BackpressureStrategy.LATEST);
        Flowable<Optional<Map<String, Triplet<String, String, Boolean>>>> tmp2 = Flowable.empty();
        Flowable<Optional<ProxyCfg>> proxyCfgFlow = SeppCfgMapper.toProxyCfg(Flowable.combineLatest(tmp, tmp2, Pair::of), cmPatch, svcList);

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

    private EricssonSepp buildCSeppReferenceConfig()
    {
        EricssonSepp refCfg = new EricssonSepp();
        EricssonSeppSeppFunction refSeppFnct = new EricssonSeppSeppFunction();

        NfInstance cSepp = new NfInstance();
        cSepp.setName("cSepp");

        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(80).withFqdn("sepp.ericsson.se");

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("UT default RC");

        cSepp.withOwnNetwork(List.of(intNetwork));
        cSepp.withExternalNetwork(List.of(extNetwork));
        cSepp.withServiceAddress(List.of(intSvcAddr, extSvcAddr));

        RoamingPartner rp1 = new RoamingPartner().withName("rp_1");
        NfPool rp1_sepp_pool = new NfPool().withName("rp_1_sepp_pool").withRoamingPartnerRef("rp_1");

        NfPoolDiscovery rp1_pool_discovery = new NfPoolDiscovery().withName("rp_1_pd")
                                                                  .withStaticNfInstanceDataRef(Arrays.asList(("rp_1_sepp_static_nf_instance_data_ref")));

        rp1_sepp_pool.setNfPoolDiscovery(Arrays.asList(rp1_pool_discovery));

        cSepp.withNfPool(Arrays.asList(rp1_sepp_pool));

        StaticNfInstance rp1Psepp1 = createStaticNfInstance("rp1_pSepp1", "10.10.10.1", 80);
        StaticNfInstanceDatum rp1_pSeppData = new StaticNfInstanceDatum().withName("rp_1_sepp_static_nf_instance_data_ref")
                                                                         .withStaticNfInstance(Arrays.asList(rp1Psepp1));

        cSepp.getExternalNetwork().get(0).setRoamingPartner(List.of(rp1));
        cSepp.setStaticNfInstanceData(Arrays.asList(rp1_pSeppData));

        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(cSepp);

        refSeppFnct.setNfInstance(nfInstances);

        refCfg.withEricssonSeppSeppFunction(refSeppFnct);

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
    private EricssonSepp buildEst1Config()
    {
        EricssonSepp refCfg = new EricssonSepp();
        EricssonSeppSeppFunction refSeppFnct = new EricssonSeppSeppFunction();

        // SEPP general attributes
        NfInstance sepp = new NfInstance();
        sepp.setName("instance_1");

        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(80).withFqdn("sepp.ericsson.se");

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("UT default RC");

        sepp.withOwnNetwork(List.of(intNetwork));
        sepp.withExternalNetwork(List.of(extNetwork));
        sepp.withServiceAddress(List.of(intSvcAddr, extSvcAddr));

        // Roaming-Partners
        List<RoamingPartner> roamingPartners = new ArrayList<>();
        RoamingPartner rpA = new RoamingPartner().withName("rp_A").withComment("Trusted roaming partner in PLMN A");
        roamingPartners.add(rpA);

        sepp.getExternalNetwork().get(0).setRoamingPartner(roamingPartners);

        // Pools and -discovery
        // 1. Pool with statically discovered nodes (sepp)
        StaticNfInstance rpASepp1 = createStaticNfInstanceWithFqdn("nfp1-chfsim1", "nfp1.mnc.123.mcc.123.com", "10.98.47.205", 80);

        StaticNfInstance rpASepp2 = createStaticNfInstanceWithFqdn("nfp2-chfsim2", "nfp2.mnc.123.mcc.123.com", "10.104.193.139", 80);
        StaticNfInstanceDatum rpAStaticSeppData = new StaticNfInstanceDatum().withName("static_sepp").withStaticNfInstance(Arrays.asList(rpASepp1, rpASepp2));

        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("sepp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_sepp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpASeppPool = new NfPool().withName("sepp").withRoamingPartnerRef("rp_A");
        rpASeppPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> seppSubpools = new ArrayList<>();
        seppSubpools.add(new PriorityGroup().withName("sepp_subpool").withPriority(1));
        rpASeppPool.withPriorityGroup(seppSubpools);
        pools.add(rpASeppPool);

        // 2. Pool with statically discovered nodes (int)
        StaticNfInstance int1 = createStaticNfInstanceWithFqdn("nfp3-chfsim3", "nfp3.mnc.456.mcc.456.com", "110.99.75.200", 80);
        StaticNfInstance int2 = createStaticNfInstanceWithFqdn("nfp4-chfsim4", "nfp4.mnc.456.mcc.456.com", "10.99.75.201", 80);

        StaticNfInstanceDatum staticIntData = new StaticNfInstanceDatum().withName("static_int").withStaticNfInstance(Arrays.asList(int1, int2));

        NfPoolDiscovery intPoolDiscovery = new NfPoolDiscovery().withName("int_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_int")));

        NfPool intSeppPool = new NfPool().withName("sepp");
        intSeppPool.setNfPoolDiscovery(Arrays.asList(intPoolDiscovery));

        List<PriorityGroup> intSubpools = new ArrayList<>();
        intSubpools.add(new PriorityGroup().withName("sepp_subpool").withPriority(1));
        intSeppPool.withPriorityGroup(intSubpools);
        pools.add(intSeppPool);

        // Add static node data and pools to sepp config:
        sepp.setStaticNfInstanceData(Arrays.asList(rpAStaticSeppData, staticIntData));
        sepp.withNfPool(pools);

        // Message-Data
        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?P<nfp>.+?)\\..+?\\.(?P<mnc>.+?)\\..+?\\.(?P<mcc>.+?)\\."); // Each backslash doubled from
                                                                                                                                // Netconf

        messageData.add(rd1);
        sepp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing").withMessageDataRef(Arrays.asList("apiRoot_data"));
        List<RoutingRule> routingRules = new ArrayList<>();

        ActionRoutePreferred actionRouteToPool = new ActionRoutePreferred().withTargetNfPool(new TargetNfPool().withNfPoolRef("int"));

        routingRules.add(new RoutingRule().withName("psepp_to_ownPLMN")
                                          .withCondition("var.mnc=='456' and var.mcc=='456'")
                                          .withRoutingAction(List.of(new RoutingAction().withName("action-route-preferred")
                                                                                        .withActionRoutePreferred(actionRouteToPool))));

        ActionRouteRoundRobin actionRouteToRoamingPartner = new ActionRouteRoundRobin().withTargetRoamingPartner(new TargetRoamingPartner().withRoamingPartnerRef("sepp"));

        routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
                                          .withRoutingAction(List.of(new RoutingAction().withName("action-route-round-robin")
                                                                                        .withActionRouteRoundRobin(actionRouteToRoamingPartner))));
        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        sepp.withRoutingCase(routingCases);

        // SEPP general
        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(sepp);

        refSeppFnct.setNfInstance(nfInstances);

        refCfg.withEricssonSeppSeppFunction(refSeppFnct);

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
    private EricssonSepp buildEst2Config()
    {
        EricssonSepp refCfg = new EricssonSepp();
        EricssonSeppSeppFunction refSeppFnct = new EricssonSeppSeppFunction();

        // SEPP general attributes
        NfInstance sepp = new NfInstance();
        sepp.setName("instance_1");
        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(80).withFqdn("sepp.ericsson.se");

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("default_routing");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("default_routing");

        sepp.withOwnNetwork(List.of(intNetwork));
        sepp.withExternalNetwork(List.of(extNetwork));
        sepp.withServiceAddress(List.of(intSvcAddr, extSvcAddr));

        List<IngressConnectionProfile> ingressConnectionProfiles = new ArrayList<>();
        IngressConnectionProfile aConnProf = new IngressConnectionProfile().withName("aIgressConn");
        ingressConnectionProfiles.add(aConnProf);
        sepp.withIngressConnectionProfile(ingressConnectionProfiles);

        sepp.withIngressConnectionProfileRef(aConnProf.getName());

        // Roaming-Partners
        RoamingPartner rpA = new RoamingPartner().withName("rp_A").withComment("Trusted roaming partner in PLMN A");
        sepp.getExternalNetwork().get(0).setRoamingPartner(List.of(rpA));

        // Pools and -discovery
        // 1. Pool with statically discovered nodes (sepp)
        StaticNfInstance rpASepp1 = createStaticNfInstanceWithFqdn("nfp1-chfsim1", "nfp1.mnc.123.mcc.123.com", "10.98.47.205", 80);
        StaticNfInstance rpASepp2 = createStaticNfInstanceWithFqdn("nfp1-chfsim2", "nfp2.mnc.123.mcc.123.com", "10.104.193.139", 80);

        StaticNfInstanceDatum rpAStaticSeppData = new StaticNfInstanceDatum().withName("static_sepp").withStaticNfInstance(Arrays.asList(rpASepp1, rpASepp2));
        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("sepp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_sepp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpASeppPool = new NfPool().withName("sepp").withRoamingPartnerRef("rp_A");
        rpASeppPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> seppSubpools = new ArrayList<>();
        seppSubpools.add(new PriorityGroup().withName("sepp_subpool").withPriority(1));
        rpASeppPool.withPriorityGroup(seppSubpools);
        pools.add(rpASeppPool);

        // 2. Pool with statically discovered nodes (int)
        StaticNfInstance int1 = createStaticNfInstanceWithFqdn("nfp3-chfsim3", "nfp3.mnc.456.mcc.456.com", "110.99.75.200", 80);
        StaticNfInstance int2 = createStaticNfInstanceWithFqdn("nfp4-chfsim4", "nfp4.mnc.456.mcc.456.com", "10.99.75.201", 80);

        StaticNfInstanceDatum staticIntData = new StaticNfInstanceDatum().withName("static_int").withStaticNfInstance(Arrays.asList(int1, int2));

        NfPoolDiscovery intPoolDiscovery = new NfPoolDiscovery().withName("int_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_int")));

        NfPool intSeppPool = new NfPool().withName("sepp");
        intSeppPool.setNfPoolDiscovery(Arrays.asList(intPoolDiscovery));

        List<PriorityGroup> intSubpools = new ArrayList<>();
        intSubpools.add(new PriorityGroup().withName("sepp_subpool").withPriority(1));
        intSeppPool.withPriorityGroup(intSubpools);
        pools.add(intSeppPool);

        // Add static node data and pools to sepp config:
        sepp.setStaticNfInstanceData(Arrays.asList(rpAStaticSeppData, staticIntData));
        sepp.withNfPool(pools);

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
        messageData.add(rd4);

        sepp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing")
                                           .withMessageDataRef(Arrays.asList("apiRoot_data", "apiRoot_var", "path_var", "path_data"));
        List<RoutingRule> routingRules = new ArrayList<>();

        ActionRoutePreferred actionRouteToPool = new ActionRoutePreferred().withTargetNfPool(new TargetNfPool().withNfPoolRef("int"));
        RoutingAction actionRoutePreferred = new RoutingAction().withName("action-route-preferred").withActionRoutePreferred(actionRouteToPool);

        routingRules.add(new RoutingRule().withName("psepp_to_ownPLMN")
                                          .withCondition("var.mnc=='456' and var.mcc=='456'")
                                          .withRoutingAction(List.of(actionRoutePreferred)));

        ActionRouteRoundRobin actionRouteToRoamingPartner = new ActionRouteRoundRobin().withTargetRoamingPartner(new TargetRoamingPartner().withRoamingPartnerRef("sepp"));
        RoutingAction actionRouteRoundRobin = new RoutingAction().withName("action-route-to-roaming-partner")
                                                                 .withActionRouteRoundRobin(actionRouteToRoamingPartner);

        routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
                                          .withRoutingAction(List.of(actionRouteRoundRobin)));

        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        sepp.withRoutingCase(routingCases);

        // SEPP general
        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(sepp);

        refSeppFnct.setNfInstance(nfInstances);

        refCfg.withEricssonSeppSeppFunction(refSeppFnct);

        // Finish
        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

    /**
     * Combined C-SEPP and P-SEPP configuration matching the
     * sample_poc_config.netconf
     * 
     * @return reference configuration
     */
    private EricssonSepp buildPocSampleReferenceConfig()
    {
        EricssonSepp refCfg = new EricssonSepp();
        EricssonSeppSeppFunction refSeppFnct = new EricssonSeppSeppFunction();
        // SCP general attributes

        // SEPP general attributes
        NfInstance sepp = new NfInstance();
        sepp.setName("instance_1");

        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(80).withFqdn("sepp.ericsson.se");

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("UT default RC");

        sepp.withOwnNetwork(List.of(intNetwork));
        sepp.withExternalNetwork(List.of(extNetwork));
        sepp.withServiceAddress(List.of(intSvcAddr, extSvcAddr));

        // Roaming-Partners
        List<RoamingPartner> roamingPartners = new ArrayList<>();
        RoamingPartner rpA = new RoamingPartner().withName("rp_A").withComment("Trusted roaming partner in PLMN A");

        sepp.getExternalNetwork().get(0).setRoamingPartner(List.of(rpA));

        // Pools and -discovery
        // 1. Pool with statically discovered nodes
        StaticNfInstance rpASepp1 = createStaticNfInstanceWithFqdn("sepp1", "sepp-1.rp-1.com", "10.10.11.1", 80);
        StaticNfInstanceDatum rpAStaticSeppData = new StaticNfInstanceDatum().withName("static_sepp").withStaticNfInstance(Arrays.asList(rpASepp1));
        sepp.setStaticNfInstanceData(Arrays.asList(rpAStaticSeppData));

        NfPoolDiscovery rpAPoolDiscovery = new NfPoolDiscovery().withName("sepp_pool").withStaticNfInstanceDataRef(Arrays.asList(("static_sepp")));

        List<NfPool> pools = new ArrayList<>();
        NfPool rpASeppPool = new NfPool().withName("sepp").withRoamingPartnerRef("rp_A");
        rpASeppPool.setNfPoolDiscovery(Arrays.asList(rpAPoolDiscovery));

        List<PriorityGroup> subpools = new ArrayList<>();
        subpools.add(new PriorityGroup().withName("sepp_subpool").withPriority(1));
        rpASeppPool.withPriorityGroup(subpools);
        pools.add(rpASeppPool);

        // 2. Pool with dynamic-forwarding proxy (i.e. no discovered or configured
        // nodes):
        NfPool rpAUniversalPool = new NfPool().withName("universal_pool");
        pools.add(rpAUniversalPool);

        // add pools to sepp config:
        sepp.withNfPool(pools);

        // Routing-Data
        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?<nfp>.+?)\\..+?\\.(?<mnc>.+?)\\..+?\\.(?<mcc>.+?)\\."); // Each backslash doubled from
                                                                                                                             // Netconf

        messageData.add(rd1);
        sepp.withMessageData(messageData);

        // Routing-Cases
        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing").withMessageDataRef(Arrays.asList("apiRoot_data"));
        List<RoutingRule> routingRules = new ArrayList<>();

        ActionRoutePreferred actionRouteToPool = new ActionRoutePreferred().withTargetNfPool(new TargetNfPool().withNfPoolRef("universal_pool"));
        RoutingAction actionRoutePreferred = new RoutingAction().withName("action-route-preferred").withActionRoutePreferred(actionRouteToPool);

        ActionRouteRoundRobin actionRouteToRoamingPartner = new ActionRouteRoundRobin().withTargetRoamingPartner(new TargetRoamingPartner().withRoamingPartnerRef("rp_A"));
        RoutingAction actionRouteRoundRobin = new RoutingAction().withName("action-route-to-roaming-partner")
                                                                 .withActionRouteRoundRobin(actionRouteToRoamingPartner);

        routingRules.add(new RoutingRule().withName("psepp_to_ownPLMN")
                                          .withCondition("var.mnc=='456' and var.mcc=='456'")
                                          .withRoutingAction(List.of(actionRoutePreferred)));
        routingRules.add(new RoutingRule().withName("csapp_to_PLMN_A")
                                          .withCondition("req.method=='POST' and var.mnc=='123' and var.mcc=='123'")
                                          .withRoutingAction(List.of(actionRouteRoundRobin)));

        rc1.withRoutingRule(routingRules);
        routingCases.add(rc1);
        sepp.withRoutingCase(routingCases);

        // SEPP general
        List<NfInstance> nfInstances = new ArrayList<>();

        nfInstances.add(sepp);

        refSeppFnct.setNfInstance(nfInstances);

        refCfg.withEricssonSeppSeppFunction(refSeppFnct);

        // Finish
        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(refCfg, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return refCfg;
    }

}