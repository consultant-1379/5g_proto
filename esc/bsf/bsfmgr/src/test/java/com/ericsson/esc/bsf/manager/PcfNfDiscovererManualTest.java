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
 * Created on: Apr 14, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunction;
import com.ericsson.sc.bsf.model.NfDiscovery;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.NrfGroup;
import com.ericsson.sc.bsf.model.NrfService;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.bsf.model.glue.NfFunction;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.nfm.model.IpEndpoint;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.utilities.common.Rdn;

import io.reactivex.Flowable;

/**
 * Manual test for PcfNfDiscoverer.
 * 
 * Steps: a. Deploy NrfSim in the cluster. b. Port forward the NrfSim service.
 * c. Update the NrfSim end-points in the TC. d. Execute the manual test.
 * 
 * Any PCF registrations,updates or deletions done at the target NrfSim via curl
 * are visible on the output logs of the TC.
 */
public class PcfNfDiscovererManualTest
{
    private static final Logger log = LoggerFactory.getLogger(PcfNfDiscovererManualTest.class);

    @Test(groups = "functest")
    public void manualTest()
    {
        // Here define the end-points of the NRF that should be used for the discovery
        // of other NFs.
        var nrfIp = "127.0.0.1";
        var nrfPort = 30000;

        // Preparing the bsfFunction.
        var bsfConfig = createBsfConfig(nrfIp, nrfPort);
        var bsfFunction = new NfFunction(Alarm.Context.of(null, "Bsf", "erisson-bsf"), null, null, new Rdn("nf", "bsf-function"));
        bsfFunction.update(bsfConfig);

        // Create a new pcfDiscoverer.
        var pcfDiscoverer = new PcfDiscoverer(bsfFunction, Flowable.just(Optional.of(new EricssonBsf().withEricssonBsfBsfFunction(bsfConfig))));

        // Subscribe to receive notifications for the discover PcfNfs.
        var outputChain = pcfDiscoverer.getDiscoveredPcfNfs() //
                                       .doOnNext(list -> log.debug("Received a PcfNf list: {}", list));

        outputChain.ignoreElements().ambWith(pcfDiscoverer.start()).blockingGet();
    }

    /**
     * Utility method to create a BSF configuration.
     * 
     * @param nrfIp   The IP of the target NRF.
     * @param nrfPort The port of the target NRF.
     * @return A BSF configuration.
     */
    private static EricssonBsfBsfFunction createBsfConfig(String nrfIp,
                                                          int nrfPort)
    {
        // NfProfile.
        ServiceAddress serviceAddress = new ServiceAddress();
        serviceAddress.withName("serviceAddress1");
        serviceAddress.withFqdn("bsf.ericsson.com");
        serviceAddress.withIpv4Address("192.168.1.1");
        serviceAddress.withPort(8080);

        // NfProfile.
        NfProfile nfProfile = new NfProfile();
        nfProfile.withName("nfProfile1");
        String nfType = NFType.BSF;
        nfProfile.withNfType(nfType);
        nfProfile.withServiceAddressRef("serviceAddress1");

        // NrfService.
        NfDiscovery nfDiscovery = new NfDiscovery().withNrfGroupRef("nrfDiscoveryGroup");
        NrfService nrfService = new NrfService().withNfDiscovery(nfDiscovery);

        // Nrf and NrfGroup.
        com.ericsson.sc.bsf.model.Nrf nrf = new com.ericsson.sc.bsf.model.Nrf();
        nrf.withName("nrf1");
        nrf.withScheme(Scheme.HTTP);
        nrf.withPriority(1);
        var nrfEndPoint = new IpEndpoint().withName("nrf1IPEP1") //
                                          .withIpv4Address(nrfIp)
                                          .withPort(nrfPort);
        nrf.withIpEndpoint(List.of(nrfEndPoint));
        nrf.withNfProfileRef("nfProfile1");

        NrfGroup nrfGroup = new NrfGroup();
        nrfGroup.withName("nrfDiscoveryGroup");
        nrfGroup.withNrf(List.of(nrf));

        // NfInstance.
        NfInstance nfInstance = new NfInstance();
        nfInstance.withName("bsf-function");
        nfInstance.withNrfGroup(List.of(nrfGroup));
        nfInstance.withNrfService(nrfService);
        nfInstance.withNfProfile(List.of(nfProfile));
        nfInstance.withServiceAddress(List.of(serviceAddress));

        return new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
    }

}
