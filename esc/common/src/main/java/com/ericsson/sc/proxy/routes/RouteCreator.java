/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy.routes;

import com.ericsson.sc.glue.*;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.clusters.ClusterCreator;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class RouteCreator
{
//    protected static final String HEADER_X_NOTIFY_URI = "x-notify-uri";

    protected List<ProxyRoute> routes = new ArrayList<>();
    protected final ClusterCreator targetCluster;
    protected final IfNfInstance configInst;
    protected final String failoverProfileName;
    protected final RoutingBehaviour routingBehaviour;

    protected RouteCreator(ClusterCreator targetCluster,
                           String failoverProfileName,
                           IfNfInstance configInst,
                           RoutingBehaviour routingBehaviour)
    {
        this.targetCluster = targetCluster;
        this.configInst = configInst;
        this.failoverProfileName = failoverProfileName;
        this.routingBehaviour = routingBehaviour;
    }

    public List<ProxyRoute> getRoutes()
    {
        return routes;
    }

    public abstract void createRoutes();

    protected abstract String getRouteName();

    /**
     * Return the values of the referenced failover-profile, given that it is
     * configured properly. If a failover-profile is not configured, return the
     * default values (same as in the Yang Model) in case a failover-profile is not
     * configured by the user Note: Timeouts are in milliseconds
     *
     */
    public IfFailoverProfile getConfiguredOrDefaultFailoverProfile(Optional<String> failoverProfName,
                                                                   final List<IfFailoverProfile> failoverProfiles,
                                                                   boolean lastResortPoolPresent)
    {
        if (this.configInst instanceof com.ericsson.sc.scp.model.NfInstance)
        {
            var scpFailoverProfiles = failoverProfiles.stream().map(f -> (com.ericsson.sc.scp.model.FailoverProfile) f).collect(Collectors.toList());
            return ConfigHelper.getScpFailoverProfileOrDefault(failoverProfName, scpFailoverProfiles, lastResortPoolPresent);
        }
        else
        {
            var seppFailoverProfiles = failoverProfiles.stream().map(f -> (com.ericsson.sc.sepp.model.FailoverProfile) f).collect(Collectors.toList());
            return ConfigHelper.getSeppFailoverProfileOrDefault(failoverProfName, seppFailoverProfiles, lastResortPoolPresent);
        }
    }

}
