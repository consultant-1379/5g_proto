/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 3, 2022
 *     Author: esrhpac
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.extensions.filters.http.header_to_metadata.v3.Config;
import io.envoyproxy.envoy.extensions.filters.http.header_to_metadata.v3.Config.Rule;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.extensions.filters.http.header_to_metadata.v3.Config.KeyValuePair;

import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;

/**
 * 
 */
public class ProxyHeaderToMetadata implements IfHttpFilter
{
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {

        var key = KeyValuePair.newBuilder().setMetadataNamespace("envoy.lb").setKey("host").build();

        var rule = Rule.newBuilder();
        rule.setHeader("x-host").setOnHeaderPresent(key).setRemove(true).build();

        var filterConfig = Config.newBuilder().addRequestRules(rule).build();

        var filterConfigAny = Any.pack(filterConfig);

        builder.addHttpFilters(HttpFilter.newBuilder() //
                                         .setName("envoy.filters.http.header_to_metadata") //
                                         .setTypedConfig(filterConfigAny) //
                                         .build()); //
    }

    @Override
    public Priorities getPriority()
    {
        return IfHttpFilter.Priorities.HEADER_TO_METADATA;
    }
}
