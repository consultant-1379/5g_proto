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

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.extensions.filters.http.cdn_loop.v3.CdnLoopConfig;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxyCdnLoop implements IfHttpFilter
{

    private String cdnId;

    public ProxyCdnLoop(String cdnId)
    {
        this.cdnId = cdnId;
    }

    public String getCdnId()
    {
        return cdnId;
    }

    public void setCdnId(String cdnId)
    {
        this.cdnId = cdnId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#appendConfig(
     * com.ericsson.sc.proxyal.proxyconfig.ProxyListener,
     * io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.
     * HttpConnectionManager.Builder)
     */
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        // This is a modified cnd_loop filter that uses VIA header,
        // setMaxAllowedOccurrences default value is 0.

        var cdnLoopFilterConfig = CdnLoopConfig.newBuilder().setCdnId(cdnId).setMaxAllowedOccurrences(0).build();
        var cdnLoopFilterConfigAny = Any.pack(cdnLoopFilterConfig);

        builder.addHttpFilters(HttpFilter.newBuilder() //
                                         .setName("envoy.filters.http.cdn_loop") //
                                         .setTypedConfig(cdnLoopFilterConfigAny) //
                                         .build());
    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.CDN_LOOP;
    }

}
