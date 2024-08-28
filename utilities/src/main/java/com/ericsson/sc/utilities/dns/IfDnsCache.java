package com.ericsson.sc.utilities.dns;

import java.util.Map;

import io.reactivex.Flowable;

public interface IfDnsCache extends IfDnsCacheClient
{

    IfDnsCacheClient createClient();

    Flowable<Map<String, IfDnsLookupContext>> getUnresolvedHosts();

}
