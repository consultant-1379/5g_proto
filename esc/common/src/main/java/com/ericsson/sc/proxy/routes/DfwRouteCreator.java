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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.FailoverWizardBase;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.proxy.ProxyConstants.HEADERS;
import com.ericsson.sc.proxy.clusters.ClusterCreator;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

public class DfwRouteCreator extends RouteCreator
{
    private static final Logger log = LoggerFactory.getLogger(DfwRouteCreator.class);
    private final boolean wantTls;
    // Flag used to differentiate between SCP and SEPP.
    private boolean isScp;

    public DfwRouteCreator(String failoverProfileName,
                           boolean wantTls,
                           IfNfInstance configInst,
                           ClusterCreator targetCluster,
                           boolean isScp)
    {
        super(targetCluster, failoverProfileName, configInst, RoutingBehaviour.STRICT_DFP);
        this.wantTls = wantTls;
        this.isScp = isScp;
    }

    @Override
    public void createRoutes()
    {
        var gandalf = new FailoverWizardBase<>(getConfiguredOrDefaultFailoverProfile(Optional.ofNullable(failoverProfileName),
                                                                                     this.configInst.getFailoverProfile(),
                                                                                     false));
        final var proxyRetryPolicy = gandalf.transform(Optional.ofNullable(RoutingBehaviour.STRICT_DFP));

        final var routeMatch = new ProxyRouteMatch().setPrefix("/").addRegexValueHeader(HEADERS.TARGET_API_ROOT, (wantTls ? "^https:(.*)" : "^http:(.*)"));
        proxyRetryPolicy.getName().ifPresent(name -> routeMatch.addExactMatchValueHeader(HEADERS.FAILOVER_PROFILE, name));

        // 3gpp-sbi-target-apiroot header-based routes applicable for both SEPP and SCP.
        var route = new ProxyRoute(getRouteName() + "_tar",
                                   routeMatch,
                                   proxyRetryPolicy,
                                   targetCluster.getCluster().getName(),
                                   null,
                                   ProxyRoute.RouteTypes.DIRECT_ROUTE,
                                   ProxyRoute.RoutePriorities.PRI_SEPP_NOTIFY,
                                   RoutingBehaviour.STRICT_DFP,
                                   proxyRetryPolicy.getRequestTimeoutSeconds());

        log.debug("Route: {}", route);
        route.setAutoHostRewrite(false);

        // x-notify-uri header-based routes applicable for SCP only
        if (this.isScp)
        {
            final var proxyRetryPolicy2 = gandalf.transform(Optional.ofNullable(RoutingBehaviour.STRICT_DFP));
            final var routeMatch2 = new ProxyRouteMatch().setPrefix("/").addRegexValueHeader(HEADERS.X_NOTIFY_URI, (wantTls ? "^https:(.*)" : "^http:(.*)"));
            proxyRetryPolicy2.getName().ifPresent(name -> routeMatch2.addExactMatchValueHeader(HEADERS.FAILOVER_PROFILE, name));

            var route2 = new ProxyRoute(getRouteName() + "_x_notify",
                                        routeMatch2,
                                        proxyRetryPolicy2,
                                        targetCluster.getCluster().getName(),
                                        null,
                                        ProxyRoute.RouteTypes.DIRECT_ROUTE,
                                        ProxyRoute.RoutePriorities.PRI_SEPP_NOTIFY,
                                        RoutingBehaviour.STRICT_DFP,
                                        proxyRetryPolicy2.getRequestTimeoutSeconds());

            route2.setAutoHostRewrite(false);
            log.debug("Route: {}", route2);

            this.routes = List.of(route, route2);
        }
        else
        {
            this.routes = List.of(route);

        }

    }

    @Override
    protected String getRouteName()
    {
        StringBuilder sb = new StringBuilder(targetCluster.getCluster().getName());
        sb.insert(0, "to_");
        sb.append("_DFP");
        return sb.toString();

    }
}