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

package com.ericsson.sc.scp.config;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.scp.model.IngressConnectionProfile;
import com.ericsson.sc.scp.model.MessageDatum;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.OwnNetwork;
import com.ericsson.sc.scp.model.RoutingCase;
import com.ericsson.sc.scp.model.ServiceAddress;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 */
class RoutingContextTest
{
    private static final Logger log = LoggerFactory.getLogger(RoutingContextTest.class);

    private static final String OWN_FQDN = "scp.ericsson.com";
    private static final String OWN_VPN = "own_vpn";

    @DisplayName("map NfInst (Scp) to ProxyRoutes (RoutingContext)")
    @Test
    @Tag("integration")
    void test_mapping_of_scpinst_to_service_routes()
    {
        NfInstance refScpInst = buildReferenceScpInst();
        RoutingContext routingCtxt = new RoutingContext(refScpInst);
        routingCtxt.convertConfig();

        assertEquals(routingCtxt.getServiceRoutesForListener(refScpInst.getServiceAddress().get(0).getName(), "int_service").get(0).getRouteName(),
                     "not_found");
        assertEquals(routingCtxt.getServiceRoutesForListener(refScpInst.getServiceAddress().get(0).getName(), "int_service").get(1).getRouteName(),
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

        log.debug("int_service route:{}.", routingCtxt.getServiceRoutesForListener(refScpInst.getServiceAddress().get(0).getName(), "int_service"));

    }

    private NfInstance buildReferenceScpInst()
    {

        List<ServiceAddress> svcAddresses = new ArrayList<>();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        NfInstance scpInst = new NfInstance();
        scpInst.setName("cScp");

        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address("10.10.10.1").withName(OWN_VPN).withPort(80).withFqdn(OWN_FQDN);
        svcAddresses.add(aSvcAddr);

        List<MessageDatum> routingData = new ArrayList<>();
        MessageDatum rd1 = new MessageDatum().withName("apoiRoot_data")
                                             .withHeader("3gpp-Sbi-target-apiRoot")
                                             .withExtractorRegex("^(?<nfp>.+?)\\..+?\\.(?<mnc>.+?)\\..+?\\.(?<mcc>.+?)\\.");
        routingData.add(rd1);
        scpInst.withMessageData(routingData);

        List<RoutingCase> routingCases = new ArrayList<>();
        RoutingCase rc1 = new RoutingCase().withName("default_routing").withMessageDataRef(Arrays.asList(rd1.getName()));
        routingCases.add(rc1);

        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName()).withRoutingCaseRef(rc1.getName());
        ownNetworks.add(aNetwork);

        scpInst.withOwnNetwork(ownNetworks).withServiceAddress(svcAddresses).withRoutingCase(routingCases);

        List<IngressConnectionProfile> ingressConnectionProfiles = new ArrayList<>();
        IngressConnectionProfile aConnProf = new IngressConnectionProfile().withName("aIngressConn");
        ingressConnectionProfiles.add(aConnProf);
        scpInst.withIngressConnectionProfile(ingressConnectionProfiles);
        scpInst.withIngressConnectionProfileRef("aIngressConn");

        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(scpInst, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return scpInst;
    }

}
