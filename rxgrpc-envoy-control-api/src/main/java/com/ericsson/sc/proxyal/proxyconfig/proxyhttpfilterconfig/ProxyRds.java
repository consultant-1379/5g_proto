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

import io.envoyproxy.envoy.config.core.v3.AggregatedConfigSource;
import io.envoyproxy.envoy.config.core.v3.ConfigSource;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;

/**
 * 
 */
public class ProxyRds implements IfHttpFilter
{

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
        builder.setRds(Rds.newBuilder() //
                          .setConfigSource(ConfigSource.newBuilder()
                                                       .setResourceApiVersion(io.envoyproxy.envoy.config.core.v3.ApiVersion.V3) //
                                                       .setAds(AggregatedConfigSource.newBuilder().build())
                                                       .build()) //
                          .setRouteConfigName(proxyListener.getRoutesName()) //
                          .build());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#getPriority()
     */
    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.RDS;
    }

}
