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
 * Created on: Dec 10, 2020
 *     Author: eavapsr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;

import io.envoyproxy.envoy.extensions.common.dynamic_forward_proxy.v3.DnsCacheConfig;
import io.envoyproxy.envoy.extensions.filters.http.dynamic_forward_proxy.v3.FilterConfig;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxyDynamicForwardProxy implements IfHttpFilter
{
    // For the dynamic notify cluster/routing
    // this filter will only be created if dnslookupfamily has been set on
    // the listener: V4 for cc/slc, v6 for Rcc subnet notifies
    // in other cases filter will remain disabled
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        var dynFilterConfig = FilterConfig.newBuilder() //
                                          .setDnsCacheConfig(DnsCacheConfig.newBuilder() //
                                                                           .setName("dynamic_forward_proxy_cache_config") //
                                                                           .setDnsLookupFamily(proxyListener.getDnsLookupFamily()) //
                                                                           // DND-18723 FIXME: TODO: As a workaround, we set the TTL of the
                                                                           // hosts in the dynamic-forward-cluster to one year to prevent
                                                                           // coredumps in Envoy. Remove this once Envoy has a fix for this
                                                                           // problem:
                                                                           .setHostTtl(Duration.newBuilder()
                                                                                               .setSeconds(ProxyCfg.DYN_CLUSTER_TTL_SECONDS)
                                                                                               .build()) //
                                                                           .build()) //
                                          .build();
        var dynFilterConfigAny = Any.pack(dynFilterConfig);
        builder.addHttpFilters(HttpFilter.newBuilder() //
                                         .setName("envoy.filters.http.dynamic_forward_proxy") //
                                         .setTypedConfig(dynFilterConfigAny) //
                                         .build());

    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.DYNAMIC_FORWARD_PROXY;
    }
}
