/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 11, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsCacheClient;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;

import io.reactivex.Flowable;

/**
 * Singleton that uses the DnsCache singleton for the resolution of NRF FQDNs.
 */
public class NrfDnsCache implements IfDnsCacheClient
{
    private static final Logger log = LoggerFactory.getLogger(NrfDnsCache.class);
    private static final NrfDnsCache singleton = new NrfDnsCache();

    public static NrfDnsCache singleton()
    {
        return singleton;
    }

    private final IfDnsCacheClient client = DnsCache.getInstance().createClient();

    private NrfDnsCache()
    {
    }

    @Override
    public Flowable<Map<String, IfDnsLookupContext>> getResolvedHosts()
    {
        return this.client.getResolvedHosts();
    }

    @Override
    public void publishHostsToResolve(final Set<IfDnsLookupContext> unresolvedFqdns)
    {
        this.client.publishHostsToResolve(unresolvedFqdns);
    }

    @Override
    public Optional<String> toHost(final String ip)
    {
        return this.client.toHost(ip);
    }

    @Override
    public Map<IpFamily, Optional<String>> toIp(final String host)
    {
        return this.client.toIp(host);
    }
}
