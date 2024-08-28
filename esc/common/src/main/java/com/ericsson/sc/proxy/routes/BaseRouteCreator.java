/**
 * COPYRIGHT ERICSSON GMBH 2023
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 9, 2023
 * Author: zdoukon
 */
package com.ericsson.sc.proxy.routes;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.FailoverWizardBase;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.ProxyConstants.HEADERS;
import com.ericsson.sc.proxy.clusters.AggregateClusterCreator;
import com.ericsson.sc.proxy.clusters.ClusterCreator;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRetryPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

public class BaseRouteCreator extends RouteCreator
{
    private static final Logger log = LoggerFactory.getLogger(BaseRouteCreator.class);

    public BaseRouteCreator(String failoverProfileName,
                            IfNfInstance configInst,
                            ClusterCreator targetClusterCreator,
                            RoutingBehaviour routingBehaviour)
    {
        super(targetClusterCreator, failoverProfileName, configInst, routingBehaviour);
    }

    @Override
    public void createRoutes()
    {
        log.debug("Create routes");

        Optional<IfNfPool> lrp = Optional.empty();
        if (targetCluster instanceof AggregateClusterCreator)
        {
            lrp = Optional.ofNullable(((AggregateClusterCreator) targetCluster).getLastResortPool());
        }

        var failoverProfile = getConfiguredOrDefaultFailoverProfile(Optional.ofNullable(failoverProfileName),
                                                                    this.configInst.getFailoverProfile(),
                                                                    lrp.isPresent());

        var gandalf = new FailoverWizardBase<>(failoverProfile);
        final var proxyRetryPolicy = gandalf.transform(Optional.ofNullable(routingBehaviour));

        if (Boolean.TRUE.equals(targetCluster.getCluster().isAggregateCluster()))
        {
            proxyRetryPolicy.setSupportTemporaryBlocking(targetCluster.getCluster()
                                                                      .getAggregateClusters()
                                                                      .stream()
                                                                      .anyMatch(cluster -> cluster.getEjectionPolicy().isPresent()
                                                                                           || cluster.getActiveHealthCheck().isPresent()));
        }
        else
        {
            proxyRetryPolicy.setSupportTemporaryBlocking(targetCluster.getCluster().getEjectionPolicy().isPresent()
                                                         || targetCluster.getCluster().getActiveHealthCheck().isPresent());
        }

        proxyRetryPolicy.setSupportLoopPrevention((ConfigHelper.hasScpInPool(Optional.of(targetCluster.getPool())) || ConfigHelper.hasScpInPool(lrp)));

        var route = List.of(createRoute(targetCluster.getCluster().getName(), proxyRetryPolicy));

        this.routes = route;
    }

    private ProxyRoute createRoute(String clusterName,
                                   ProxyRetryPolicy proxyRetryPolicy)
    {
        final var routeMatch = new ProxyRouteMatch().setPrefix("/").addExactMatchValueHeader(HEADERS.CLUSTER, clusterName);
        final var priority = new AtomicReference<ProxyRoute.RoutePriorities>();

        proxyRetryPolicy.getName()//
                        .ifPresentOrElse(name ->
                        {
                            routeMatch.addExactMatchValueHeader(HEADERS.FAILOVER_PROFILE, name);
                            priority.set(ProxyRoute.RoutePriorities.PRI_SEPP_FAILOVER);
                        }, () -> priority.set(ProxyRoute.RoutePriorities.PRI_SEPP_NO_FAILOVER));

        final var route = new ProxyRoute(getRouteName(),
                                         routeMatch,
                                         proxyRetryPolicy,
                                         clusterName,
                                         null,
                                         ProxyRoute.RouteTypes.ROUND_ROBIN,
                                         priority.get(),
                                         routingBehaviour,
                                         proxyRetryPolicy.getRequestTimeoutSeconds());

        route.setAutoHostRewrite(false);
        log.debug("Route: {}", route);

        return route;
    }

    @Override
    protected String getRouteName()
    {
        StringBuilder sb = new StringBuilder(targetCluster.getCluster().getName());
        sb.insert(0, "to_");
        switch (routingBehaviour)
        {
            case PREFERRED:
                return sb.append("_PR").toString();
            case STRICT:
                return sb.append("_SR").toString();
            case ROUND_ROBIN:
                return sb.append("_RR").toString();
            case REMOTE_PREFERRED:
                return sb.append("_RPR").toString();
            case REMOTE_ROUND_ROBIN:
                return sb.append("_RRR").toString();
            default:
                return sb.toString();
        }
    }
}
