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
 * Created on: Sep 6, 2022
 *     Author: ekilagg
 */
package com.ericsson.esc.bsf.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

import java.net.Inet4Address;
import java.util.List;
import java.util.Optional;

import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.MacAddr48;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.esc.bsf.openapi.model.UeAddress;
import com.ericsson.esc.bsf.worker.MultipleBindingResolver;
import com.ericsson.sc.bsf.model.AvpCombination;
import com.ericsson.sc.bsf.model.Combination;
import com.ericsson.sc.bsf.model.Combination_;
import com.ericsson.sc.bsf.model.DiameterLookup;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.QueryParameterCombination;
import com.google.common.net.InetAddresses;

public class MultipleBindingResolverTest
{

    // essential query parameters
    private static final Inet4Address IPV4_ADDR = (Inet4Address) InetAddresses.forString("10.0.0.1");
    private static final String IP_DOMAIN = "IP_DOMAIN";
    private static final Ipv6Prefix IPV6_PREFIX = new Ipv6Prefix("2001:db8:a::123/64");
    private static final MacAddr48 MAC_ADDR_48 = new MacAddr48("10-65-30-69-45-8D");
    private static final String SUPI = "imsi-12345";
    private static final String GPSI = "msisdn-306972909290";
    private static final String DNN = "testDnn";
    private static final Integer SNSSAI_SST = 3;
    private static final String SNSSAI_SD = "ABCDE1";

    private static final Snssai snssai = Snssai.create(SNSSAI_SST, SNSSAI_SD);

    private static final UeAddress ueAddrIpv4 = new UeAddress(IPV4_ADDR, Optional.empty());
    private static final UeAddress ueAddrIpv4IpDomain = new UeAddress(IPV4_ADDR, Optional.of(IP_DOMAIN));
    private static final UeAddress ueAddrIpv6 = new UeAddress(IPV6_PREFIX);
    private static final UeAddress ueAddrIpv4_6 = new UeAddress(IPV4_ADDR, Optional.empty(), IPV6_PREFIX);
    private static final UeAddress ueAddrMac = new UeAddress(MAC_ADDR_48);

    @Test()
    public void testUeAddrQuery()
    {

        final var httpLookup = new HttpLookup();
        var diamLookup = new DiameterLookup().withResolutionType(DiameterLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                             .withAvpCombination(List.of(new AvpCombination().withName("Ipv4IpDomain")
                                                                                             .withCombination(List.of(Combination_.IPV_4,
                                                                                                                      Combination_.IP_DOMAIN)),
                                                                         new AvpCombination().withName("Ipv6").withCombination(List.of(Combination_.IPV_6))));

        final var httpResolver = new MultipleBindingResolver(httpLookup);
        var diamResolver = new MultipleBindingResolver(diamLookup);

        var query = new DiscoveryQuery.UeAddr(ueAddrIpv4);
        assertTrue(httpResolver.isQueryApplicable(query));
        assertTrue(!diamResolver.isQueryApplicable(query));

        query = new DiscoveryQuery.UeAddr(ueAddrIpv6);
        assertTrue(httpResolver.isQueryApplicable(query));
        assertTrue(diamResolver.isQueryApplicable(query));

        query = new DiscoveryQuery.UeAddr(ueAddrIpv4IpDomain);
        assertTrue(httpResolver.isQueryApplicable(query));
        assertTrue(diamResolver.isQueryApplicable(query));

        diamLookup.setResolutionType(DiameterLookup.ResolutionType.REJECT);
        diamResolver = new MultipleBindingResolver(diamLookup);
        assertTrue(!diamResolver.isQueryApplicable(query));

        diamLookup.setResolutionType(DiameterLookup.ResolutionType.MOST_RECENT);
        diamResolver = new MultipleBindingResolver(diamLookup);
        assertTrue(diamResolver.isQueryApplicable(query));
    }

    @Test()
    public void testUeAddrDnnQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.REJECT);
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrDnn(ueAddrIpv4, DNN);
        assertTrue(!httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrDnnSnssaiQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Mac")
                                                                                                               .withCombination(List.of(Combination.MAC_ADDR_48)),
                                                                                new QueryParameterCombination().withName("Ipv6Supi")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX,
                                                                                                                                        Combination.SUPI)),
                                                                                new QueryParameterCombination().withName("Ipv4DnnSnssai")
                                                                                                               .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                        Combination.DNN,
                                                                                                                                        Combination.SNSSAI))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrDnnSnssai(ueAddrIpv4, DNN, snssai);
        assertTrue(httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrSupiQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Mac")
                                                                                                               .withCombination(List.of(Combination.MAC_ADDR_48)),
                                                                                new QueryParameterCombination().withName("Ipv4Supi")
                                                                                                               .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                        Combination.SUPI))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrSupi(ueAddrIpv6, SUPI);
        assertTrue(!httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrSupiDnnQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Mac")
                                                                                                               .withCombination(List.of(Combination.MAC_ADDR_48)),
                                                                                new QueryParameterCombination().withName("Ipv4IpDomainSupiDnnSnssai")
                                                                                                               .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                        Combination.IP_DOMAIN,
                                                                                                                                        Combination.SUPI,
                                                                                                                                        Combination.DNN,
                                                                                                                                        Combination.SNSSAI))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrSupiDnn(ueAddrIpv4IpDomain, SUPI, DNN);
        assertTrue(!httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrSupiDnnSnssaiQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX)),
                                                                                new QueryParameterCombination().withName("MacSupiDnnSnssai")
                                                                                                               .withCombination(List.of(Combination.MAC_ADDR_48,
                                                                                                                                        Combination.SUPI,
                                                                                                                                        Combination.DNN,
                                                                                                                                        Combination.SNSSAI))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrSupiDnnSnssai(ueAddrMac, SUPI, DNN, snssai);
        assertTrue(httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrGpsiQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6Gpsi")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX,
                                                                                                                                        Combination.GPSI)),
                                                                                new QueryParameterCombination().withName("Ipv4Gpsi")
                                                                                                               .withCombination(List.of(Combination.GPSI,
                                                                                                                                        Combination.IPV_4_ADDR))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrGpsi(ueAddrIpv4, GPSI);
        assertTrue(httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrGpsiDnnQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX)),
                                                                                new QueryParameterCombination().withName("Ipv4SupiDnn")
                                                                                                               .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                        Combination.SUPI,
                                                                                                                                        Combination.DNN))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrGpsiDnn(ueAddrIpv4, GPSI, DNN);
        assertTrue(!httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testUeAddrGpsiDnnSnssaiQuery()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL)
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6")
                                                                                                               .withCombination(List.of(Combination.MAC_ADDR_48)),
                                                                                new QueryParameterCombination().withName("Ipv6GpsiDnn")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX,
                                                                                                                                        Combination.SUPI)),
                                                                                new QueryParameterCombination().withName("Ipv6SupiDnnSnssai")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX,
                                                                                                                                        Combination.SUPI,
                                                                                                                                        Combination.DNN,
                                                                                                                                        Combination.SNSSAI))));
        var httpResolver = new MultipleBindingResolver(httpLookup);

        var query = new DiscoveryQuery.UeAddrGpsiDnnSnssai(ueAddrIpv6, GPSI, DNN, snssai);
        assertTrue(!httpResolver.isQueryApplicable(query));

    }

    @Test()
    public void testIpv4ContainLogic()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv4")
                                                                                                               .withCombination(List.of(Combination.IPV_4_ADDR))));
        var diamLookup = new DiameterLookup().withResolutionType(DiameterLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                             .withAvpCombination(List.of(new AvpCombination().withName("Ipv4").withCombination(List.of(Combination_.IPV_4))));

        var httpResolver = new MultipleBindingResolver(httpLookup);
        var diamResolver = new MultipleBindingResolver(diamLookup);

        var queryHttp = new DiscoveryQuery.UeAddrGpsiDnnSnssai(ueAddrIpv4IpDomain, SUPI, DNN, snssai);
        var queryDiam = new DiscoveryQuery.UeAddr(ueAddrIpv4IpDomain);
        assertTrue(httpResolver.isQueryApplicable(queryHttp));
        assertTrue(diamResolver.isQueryApplicable(queryDiam));

    }

    @Test()
    public void testIpv6ContainLogic()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX))));
        var diamLookup = new DiameterLookup().withResolutionType(DiameterLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                             .withAvpCombination(List.of(new AvpCombination().withName("Ipv6").withCombination(List.of(Combination_.IPV_6))));

        var httpResolver = new MultipleBindingResolver(httpLookup);
        var diamResolver = new MultipleBindingResolver(diamLookup);

        var queryHttp = new DiscoveryQuery.UeAddrGpsi(ueAddrIpv6, GPSI);
        var queryDiam = new DiscoveryQuery.UeAddr(ueAddrIpv6);
        assertTrue(httpResolver.isQueryApplicable(queryHttp));
        assertTrue(diamResolver.isQueryApplicable(queryDiam));

    }

    @Test()
    public void testIpv4_6ContainLogic()
    {

        var httpLookup = new HttpLookup().withResolutionType(HttpLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                         .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("Ipv6")
                                                                                                               .withCombination(List.of(Combination.IPV_6_PREFIX))));
        var diamLookup = new DiameterLookup().withResolutionType(DiameterLookup.ResolutionType.MOST_RECENT_CONDITIONAL) //
                                             .withAvpCombination(List.of(new AvpCombination().withName("Ipv4").withCombination(List.of(Combination_.IPV_4))));

        var httpResolver = new MultipleBindingResolver(httpLookup);
        var diamResolver = new MultipleBindingResolver(diamLookup);

        var queryHttp = new DiscoveryQuery.UeAddrGpsi(ueAddrIpv4_6, GPSI);
        var queryDiam = new DiscoveryQuery.UeAddr(ueAddrIpv4_6);
        assertTrue(httpResolver.isQueryApplicable(queryHttp));
        assertTrue(diamResolver.isQueryApplicable(queryDiam));

    }

}
