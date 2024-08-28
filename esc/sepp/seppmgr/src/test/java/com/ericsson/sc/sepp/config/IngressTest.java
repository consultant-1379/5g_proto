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

import static org.junit.jupiter.api.Assertions.fail;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualHost;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.IngressConnectionProfile;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.models.V1Service;

/**
 * 
 */
class IngressTest
{
    private static final Logger log = LoggerFactory.getLogger(IngressTest.class);

    private static final String OWN_FQDN = "sepp.ericsson.com";
    private static final String OWN_VPN = "own_vpn";

    @Mock
    CmmPatch cmPatch;

    @DisplayName("map NfInst (Sepp)  to ProxyListeners (Ingress)")
    @Test
    @Tag("integration")
    void test_mapping_of_vHosts_ingress_data()
    {
        NfInstance refSeppInst = buildReferenceSeppInst();
        List<V1Service> svcList = new ArrayList<>();

        Ingress ingress = new Ingress(refSeppInst, cmPatch, svcList);
        ingress.convertConfig();

        for (var listener : ingress.getListeners())
        {
            log.info("listener:{}.", listener);
            if (listener.getName().equals(OWN_VPN))
            {
                var vHosts = listener.getVirtualHosts();

                Map<String, List<ProxyEndpoint>> expectedVhostNames = new HashMap<>();
                expectedVhostNames.put("int_service", Arrays.asList(new ProxyEndpoint(OWN_FQDN, 80)));
                expectedVhostNames.put("int_fwd_service", new ArrayList<ProxyEndpoint>());
                expectedVhostNames.put("rp_rp_A_service", Arrays.asList(new ProxyEndpoint("rp_A." + OWN_FQDN, 80)));
                expectedVhostNames.put("rp_rp_B_service", Arrays.asList(new ProxyEndpoint("rp_B." + OWN_FQDN, 80)));

                Map<String, List<ProxyEndpoint>> actualVhostNames = new HashMap<>();

                for (ProxyVirtualHost vHost : vHosts)
                {
                    log.info("vhost:{}.", vHost);
                    List<ProxyEndpoint> domains = new ArrayList<>();
                    for (ProxyEndpoint ep : vHost.getEndpoints())
                        domains.add(ep);

                    actualVhostNames.put(vHost.getvHostName(), domains);
                    vHost.getEndpoints();
                }
                assertEquals(actualVhostNames, expectedVhostNames);

                if (vHosts.isEmpty())
                    fail("no  vHosts found");
            }
        }

        if (ingress.getListeners().isEmpty())
            fail("no listener found");

    }

    private NfInstance buildReferenceSeppInst()
    {

//        List<Vpn> vpns = new ArrayList<>();
//
//        seppInst.withOwnFqdn(OWN_FQDN);
//
//        Vpn ownVpn = new Vpn();
//        ownVpn.withIpv4Address("10.10.10.1").withName(OWN_VPN).withPort(80);
//
//        vpns.add(ownVpn);
//
//        seppInst.withOwnVpnRef("own_vpn");
//        seppInst.withVpn(vpns);

        NfInstance seppInst = new NfInstance();
        seppInst.setName("cSepp");

        ServiceAddress intSvcAddr = new ServiceAddress();
        intSvcAddr.withIpv4Address("10.10.10.1").withName(OWN_VPN).withPort(80).withFqdn(OWN_FQDN);

        ServiceAddress extSvcAddr = new ServiceAddress();
        extSvcAddr.withIpv4Address("9.9.9.1").withName("external_vpn").withTlsPort(443).withFqdn(OWN_FQDN);

        OwnNetwork intNetwork = new OwnNetwork().withName("intNetwork").withServiceAddressRef(intSvcAddr.getName()).withRoutingCaseRef("UT default RC");
        ExternalNetwork extNetwork = new ExternalNetwork().withName("extNetwork")
                                                          .withServiceAddressRef(extSvcAddr.getName())
                                                          .withRoutingCaseRef("UT default RC");

        seppInst.withOwnNetwork(List.of(intNetwork));
        seppInst.withExternalNetwork(List.of(extNetwork));
        seppInst.withServiceAddress(List.of(intSvcAddr, extSvcAddr));

        RoamingPartner rpA = new RoamingPartner().withName("rp_A");
        RoamingPartner rpB = new RoamingPartner().withName("rp_B");

        seppInst.getExternalNetwork().get(0).setRoamingPartner(List.of(rpA, rpB));

        List<IngressConnectionProfile> ingressConnectionProfiles = new ArrayList<>();
        IngressConnectionProfile aConnProf = new IngressConnectionProfile().withName("aIgressConn");
        ingressConnectionProfiles.add(aConnProf);
        seppInst.withIngressConnectionProfile(ingressConnectionProfiles);

        seppInst.withIngressConnectionProfileRef(aConnProf.getName());

        ObjectMapper mapper = Jackson.om();
        JsonNode jsonNode = mapper.convertValue(seppInst, JsonNode.class);
        System.out.println("******************************TEST*********************************");
        System.out.println(jsonNode.toString());

        return seppInst;
    }

}
