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

import io.envoyproxy.envoy.extensions.filters.http.lua.v3.Lua;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxyLua implements IfHttpFilter
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyConnManagerConfig#appendConfig(com.
     * ericsson.sc.proxyal.proxyconfig.ProxyListener,
     * com.google.protobuf.GeneratedMessageV3.Builder)
     */

    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        var luaConfig = Lua.newBuilder() //
                           .setInlineCode(proxyListener.getLuaFilter()) //
                           .build();
        var luaConfigAny = Any.pack(luaConfig);

        builder.addHttpFilters(HttpFilter.newBuilder() //
                                         .setName("envoy.filters.http.lua") //
                                         .setTypedConfig(luaConfigAny)
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
        return IfHttpFilter.Priorities.LUA_FILTER;
    }

}
