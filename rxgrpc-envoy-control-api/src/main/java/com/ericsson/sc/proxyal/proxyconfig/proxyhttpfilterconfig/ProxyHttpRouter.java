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

import java.util.concurrent.TimeUnit;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;

import io.envoyproxy.envoy.config.core.v3.HttpProtocolOptions;
import io.envoyproxy.envoy.extensions.filters.http.router.v3.Router;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxyHttpRouter implements IfHttpFilter
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
        int connectTimeoutSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(proxyListener.getMaxStreamDuration());
        int connectTimeoutNanos = (int) TimeUnit.MILLISECONDS.toNanos(proxyListener.getMaxStreamDuration() - TimeUnit.SECONDS.toMillis(connectTimeoutSeconds));
        var httpProtocolBuilder = HttpProtocolOptions.newBuilder() //
                                                     .setMaxStreamDuration(Duration.newBuilder()
                                                                                   .setSeconds(connectTimeoutSeconds)
                                                                                   .setNanos(connectTimeoutNanos)
                                                                                   .build())
                                                     .setIdleTimeout(Duration.newBuilder() //
                                                                             .setSeconds(proxyListener.getIdleTimeout())
                                                                             .clearNanos()
                                                                             .build());
        if (proxyListener.getMaxConnectionDuration() != null && (!proxyListener.getMaxConnectionDuration().equals(0)))
        {
            httpProtocolBuilder.setMaxConnectionDuration(Duration.newBuilder()
                                                                 .setSeconds(proxyListener.getMaxConnectionDuration().longValue())
                                                                 .clearNanos()
                                                                 .build());
        }

        builder.addHttpFilters(HttpFilter.newBuilder() //
                                         .setName("envoy.filters.http.router")
                                         .setTypedConfig(Any.pack(Router.newBuilder().build()))//
                                         .build())
               .setCommonHttpProtocolOptions(httpProtocolBuilder.build());

    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.HTTP_ROUTER;
    }

}
