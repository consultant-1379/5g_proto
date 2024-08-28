/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: March 15, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyConManagerBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyDownstreamTransportSocketBuilder;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;

public class LdsHelper
{
    private static final Logger log = LoggerFactory.getLogger(LdsHelper.class);
    public static final String TYPE_URL = "type.googleapis.com/envoy.config.listener.v3.Listener";

    private LdsHelper()
    {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Given the proxy configuration, build and return the Envoy Listener
     * configuration (all listeners). The configuration is for Envoy API v2. It is
     * returned in ProtoBuf format.
     * 
     * @param config holding the proxy configuration
     * @return listenerList The Listener configuration packed into a list of "Any",
     *         ready to be sent to Envoy
     */
    public static List<Any> buildResources(ProxyCfg config)
    {
        var listenerList = new LinkedList<Any>();
        // Iterate over all listeners that are configured: (usually only one)
        for (var proxyListener : config.getListeners())
        {
            var httpConMgrBuilder = new ProxyConManagerBuilder(config.getNfType(), proxyListener).initBuilder();

            var httpConMgr = Any.pack(httpConMgrBuilder.build());
            var pxConMgrFilter = Filter.newBuilder().setName("envoy.http_connection_manager").setTypedConfig(httpConMgr).build();
            var filterChainBuilder = FilterChain.newBuilder().addFilters(pxConMgrFilter);

            var listenerBuilder = proxyListener.initBuilder();

            var proxytransportSocketBuilder = new ProxyDownstreamTransportSocketBuilder(proxyListener, listenerBuilder, filterChainBuilder);
            proxytransportSocketBuilder.initBuilder();
            listenerList.add(Any.pack(listenerBuilder.build()));
        }

        log.debug("Returning listeners: {}", listenerList);
        return listenerList;
    }
}
