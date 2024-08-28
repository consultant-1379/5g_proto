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
 * Created on: Sep 7, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.sepp.config;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.IngressConnectionProfile;
import com.ericsson.sc.sepp.model.MessageDatum;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoutingCase;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 */
class RoutingContextTest
{
    private static final Logger log = LoggerFactory.getLogger(RoutingContextTest.class);

    private static final String OWN_FQDN = "sepp.ericsson.com";
    private static final String OWN_VPN = "own_vpn";

    @DisplayName("map NfInst (Sepp) to ProxyRoutes (RoutingContext)")
    @Test
    @Tag("integration")
    void test_mapping_of_seppinst_to_service_routes()
    {
        NfInstance refSeppInst = buildReferenceSeppInst();
        RoutingContext routingCtxt = new RoutingContext(refSeppInst, Optional.empty(), 3);
        routingCtxt.convertConfig();

        // assertEquals(routingCtxt.getServiceRoutesForListener("int_service").get(0).getRouteName(),
        // "catch_all");
        assertEquals(routingCtxt.getServiceRoutesForListener(refSeppInst.getServiceAddress().get(0).getName(), "int_service").get(0).getRouteName(),
                     "not_found");
        assertEquals(routingCtxt.getServiceRoutesForListener(refSeppInst.getServiceAddress().get(0).getName(), "int_service").get(1).getRouteName(),
                     "catch_all");
        // assertEquals(routingCtxt.getServiceRoutesForListener("rp_rp_A_service").get(0).getRouteName(),
        // "catch_all");
        assertEquals(routingCtxt.getServiceRoutesForListener(refSeppInst.getServiceAddress().get(0).getName(), "rp_rp_A_service").get(1).getRouteName(),
                     "catch_all");
        // assertEquals(routingCtxt.getServiceRoutesForListener("rp_rp_B_service").get(0).getRouteName(),
        // "catch_all");
        assertEquals(routingCtxt.getServiceRoutesForListener(refSeppInst.getServiceAddress().get(0).getName(), "rp_rp_B_service").get(1).getRouteName(),
                     "catch_all");

        // assertEquals(routingCtxt.getServiceRoutes("int_service").get(0).getRouteName(),
        // "int_service");
        // assertEquals(routingCtxt.getServiceRoutes("int_service").get(1).getRouteName(),
        // "catch_all");

        // assertEquals(routingCtxt.getServiceRoutes("rp_rp_A_service").get(0).getRouteName(),
        // "rp_rp_A_service");
        // assertEquals(routingCtxt.getServiceRoutes("rp_rp_A_service").get(1).getRouteName(),
        // "catch_all");

        // assertEquals(routingCtxt.getServiceRoutes("rp_rp_B_service").get(0).getRouteName(),
        // "rp_rp_B_service");
        // assertEquals(routingCtxt.getServiceRoutes("rp_rp_B_service").get(1).getRouteName(),
        // "catch_all");

        log.debug("int_service route:{}.", routingCtxt.getServiceRoutesForListener(refSeppInst.getServiceAddress().get(0).getName(), "int_service"));

    }

    private NfInstance buildReferenceSeppInst()
    {

//        List<Vpn> vpns = new ArrayList<>();
//        NfInstance seppInst = new NfInstance();
//
//        seppInst.setName("cSepp");
//
//        seppInst.withOwnFqdn(OWN_FQDN);
//
//        Vpn ownVpn = new Vpn();
//        ownVpn.withIpv4Address("10.10.10.1").withName(OWN_VPN).withPort(80);
//        vpns.add(ownVpn);
//        seppInst.withOwnVpnRef("own_vpn");
//        seppInst.withVpn(vpns);

        List<ServiceAddress> svcAddresses = new ArrayList<>();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        NfInstance seppInst = new NfInstance();
        seppInst.setName("cSepp");

        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName("own_vpn").withPort(80);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(80).withFqdn("sepp.ericsson.se");

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("UT default RC");

        RoamingPartner rpA = new RoamingPartner().withName("rp_A");
        RoamingPartner rpB = new RoamingPartner().withName("rp_B");

        seppInst.getExternalNetwork().get(0).setRoamingPartner(List.of(rpA, rpB));

        List<MessageDatum> messageData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apoiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?<nfp>.+?)\\..+?\\.(?<mnc>.+?)\\..+?\\.(?<mcc>.+?)\\.");
        messageData.add(rd1);
        seppInst.withMessageData(messageData);

        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing");

        List<IngressConnectionProfile> ingressConnectionProfiles = new ArrayList<>();
        IngressConnectionProfile aConnProf = new IngressConnectionProfile().withName("aIngressConn");
        ingressConnectionProfiles.add(aConnProf);
        seppInst.withIngressConnectionProfile(ingressConnectionProfiles);
        seppInst.withIngressConnectionProfileRef("aIngressConn");

        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(seppInst, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return seppInst;
    }

}
