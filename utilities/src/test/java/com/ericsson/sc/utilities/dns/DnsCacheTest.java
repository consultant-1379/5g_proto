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
 * Created on: Feb 5, 2020
 *     Author: eedala
 */

package com.ericsson.sc.utilities.dns;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.utilities.dns.DnsCache.LookupContext;

public class DnsCacheTest
{
    private static final Logger log = LoggerFactory.getLogger(DnsCacheTest.class);

    @Tag("integration")
    @Test
    void testLookupDnsCache() throws IOException
    {
        var defaultIpFamilies = Set.of(IpFamily.IPV4, IpFamily.IPV6);
        var cache = DnsCache.getInstance();
        var hosts = new HashSet<>(Arrays.asList(LookupContext.of("localhost.localdomain", defaultIpFamilies),
                                                LookupContext.of("www.ericsson.com", defaultIpFamilies),
                                                LookupContext.of("www.ard.de", defaultIpFamilies),
                                                LookupContext.of("asdfasdfasd", Set.of(IpFamily.IPV6))));
        cache.publishHostsToResolve(hosts);
        sleep(4000);
        assertTrue(cache.toIp("localhost.localdomain").values().stream().anyMatch(Optional::isPresent));
        assertTrue(cache.toIp("www.ericsson.com").values().stream().anyMatch(Optional::isPresent));
        assertTrue(cache.toIp("www.ard.de").values().stream().anyMatch(Optional::isPresent));
        assertFalse(cache.toIp("www.xyz.com").values().stream().anyMatch(Optional::isPresent));
        log.info("Cache: {}", cache);
        hosts = new HashSet<>(Arrays.asList(LookupContext.of("www.zdf.de", defaultIpFamilies), LookupContext.of("www.sportschau.de", defaultIpFamilies)));
        cache.publishHostsToResolve(hosts);
        sleep(4000);
        assertTrue(cache.toIp("www.zdf.de").values().stream().anyMatch(Optional::isPresent));
        assertTrue(cache.toIp("www.sportschau.de").values().stream().anyMatch(Optional::isPresent));
        assertFalse(cache.toIp("www.klm.com").values().stream().anyMatch(Optional::isPresent));
        log.info("Cache: {}", cache);
        sleep(60 * 1000);
        log.info("Cache: {}", cache);
    }

    @Test
    void testReadEtcResolvDotConf() throws IOException
    {
        var data = DnsCache.parseEtcResolverSearchDomains();
        log.info("Search Domains: {}", data);
        var ndots = DnsCache.parseEtcResolverFirstNdots();
        log.info("ndots: {}", ndots);
        var nameserver = DnsCache.parseEtcResolverNameserver();
        log.info("nameserver: {}, port {}", nameserver.nameserver, nameserver.port);

    }

    // ------------------------------------------------------------------------------------------------

    public void sleep(long millis)
    {
        log.info("Sleeping for {}s", millis / 1000);
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        log.info("--> sleep is over");
    }
}
