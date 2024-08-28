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

import static org.junit.jupiter.api.Assertions.fail;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualHost;
import com.ericsson.sc.scp.model.IngressConnectionProfile;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.ServiceAddress;
import com.ericsson.sc.scp.model.OwnNetwork;
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

    private static final String OWN_FQDN = "scp.ericsson.com";
    private static final String OWN_VPN = "own_vpn";
    private static final String IPV4_ADDRESS = "10.10.10.1";

    @DisplayName("map NfInst (Scp)  to ProxyListeners (Ingress)")
    @Test
    @Tag("integration")
    void test_mapping_of_vHosts_ingress_data()
    {
        NfInstance refScpInst = buildReferenceScpInst();

        List<V1Service> svcList = new ArrayList<>();
        Ingress ingress = new Ingress(refScpInst, svcList);

        ingress.convertConfig();

        for (var listener : ingress.getListeners())
        {
            // log.info("listener:{}.", listener);
            System.out.println("listener:  " + listener);
            if (listener.getName().equals(OWN_VPN))
            {
                var vHosts = listener.getVirtualHosts();

                Map<String, List<ProxyEndpoint>> expectedVhostNames = new HashMap<>();
                expectedVhostNames.put("int_service", Arrays.asList(new ProxyEndpoint(OWN_FQDN, 80), new ProxyEndpoint(IPV4_ADDRESS, 80)));
                expectedVhostNames.put("int_fwd_service", new ArrayList<ProxyEndpoint>());

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

    private NfInstance buildReferenceScpInst()
    {

        List<ServiceAddress> svcAddresses = new ArrayList<>();
        List<OwnNetwork> ownNetworks = new ArrayList<>();
        NfInstance scpInst = new NfInstance();
        scpInst.setName("cScp");

        ServiceAddress aSvcAddr = new ServiceAddress();
        aSvcAddr.withIpv4Address(IPV4_ADDRESS).withName(OWN_VPN).withPort(80).withFqdn(OWN_FQDN);
        svcAddresses.add(aSvcAddr);

        OwnNetwork aNetwork = new OwnNetwork().withName("aNetwork").withServiceAddressRef(aSvcAddr.getName());
        ownNetworks.add(aNetwork);
        scpInst.withOwnNetwork(ownNetworks);
        scpInst.withServiceAddress(svcAddresses);

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
