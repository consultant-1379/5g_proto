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

import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;

/**
 * 
 */
public interface IfHttpFilter
{

    public enum Priorities
    {
        // HTTP Filter Go Here. The sequence in which the filters are configured
        // in the listener is important since they are invoked in that order
        CDN_LOOP,
        LOCAL_RATE_LIMIT,
        GLOBAL_RATE_LIMIT,
        HEADER_TO_METADATA,
        ROUTING_SCREENING_FILTER,
        DYNAMIC_FORWARD_PROXY,
        HTTP_ROUTER, // always has to be last
        // HTTP Filter end Here.
        // DND-31297
        // the following are not HTTP Filters, however they implement the same
        // interface,
        // so a "dummy" priority needs to be defined
        RDS,
        LOCAL_REPLY_CONFIG,
        ACCESS_LOG,
        // the following are not used but are kept due to legacy code (csa)
        LUA_FILTER,
        JWT_AUTH
    }

    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder);

    public Priorities getPriority();

}
