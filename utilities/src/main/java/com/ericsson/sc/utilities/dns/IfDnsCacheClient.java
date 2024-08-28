package com.ericsson.sc.utilities.dns;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.reactivex.Flowable;

public interface IfDnsCacheClient
{
    Flowable<Map<String, IfDnsLookupContext>> getResolvedHosts();

    void publishHostsToResolve(final Set<IfDnsLookupContext> hostsToResolve);

    Optional<String> toHost(final String ip);

    Map<IpFamily, Optional<String>> toIp(final String host);
}
