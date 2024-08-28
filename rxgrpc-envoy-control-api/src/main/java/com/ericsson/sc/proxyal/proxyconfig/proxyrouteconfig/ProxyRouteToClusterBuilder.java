package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Duration;

import io.envoyproxy.envoy.config.route.v3.RouteAction;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.Route.Builder;
import io.envoyproxy.envoy.config.route.v3.Route;

public class ProxyRouteToClusterBuilder
{

    private ProxyRoute proxyRoute;
    private io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder;

    public ProxyRouteToClusterBuilder(ProxyRoute proxyRoute,
                                      io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder)
    {
        this.proxyRoute = proxyRoute;
        this.routeMatchBuilder = routeMatchBuilder;
    }

    public ProxyRouteToClusterBuilder(ProxyRouteToClusterBuilder that)
    {
        this.proxyRoute = new ProxyRoute(that.getProxyRoute());
        this.routeMatchBuilder = RouteMatch.newBuilder(that.getRouteMatchBuilder().build());
    }

    /**
     * @return the proxyRoute
     */
    public ProxyRoute getProxyRoute()
    {
        return proxyRoute;
    }

    /**
     * @param proxyRoute the proxyRoute to set
     */
    public void setProxyRoute(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
    }

    /**
     * @return the routeMatchBuilder
     */
    public io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder getRouteMatchBuilder()
    {
        return routeMatchBuilder;
    }

    /**
     * @param routeMatchBuilder the routeMatchBuilder to set
     */
    public void setRouteMatchBuilder(io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder)
    {
        this.routeMatchBuilder = routeMatchBuilder;
    }

    public Builder initBuilder()
    {
        var route = Route.newBuilder();

        int requestTimeoutSeconds = (int) Math.floor((proxyRoute.getRequestTimeoutSeconds()));
        int requestTimeoutNanos = (int) (1E9 * (proxyRoute.getRequestTimeoutSeconds() - requestTimeoutSeconds));

        // Even though the Envoy documentation states that auto_host_rewrite only works
        // for strict_dns and logical_dns -type clusters, since Envoy 1.14 it is also
        // possible to use it with EDS-type clusters when the hostname is set in the
        // Endpoint (which we do nowadays).

        var routeActionBuilder = RouteAction.newBuilder();

        if (proxyRoute.getRetryPolicy() != null)
        {
            var proxyRetryPolicyBuilder = new ProxyRetryPolicyBuilder(proxyRoute).initBuilder();
            routeActionBuilder.setRetryPolicy(proxyRetryPolicyBuilder.build());
        }

        routeActionBuilder.setAutoHostRewrite(BoolValue.of(proxyRoute.getAutoHostRewrite())) //
                          .setTimeout(Duration.newBuilder() //
                                              .setSeconds(requestTimeoutSeconds) //
                                              .setNanos(requestTimeoutNanos) //
                                              .build());

        if ((proxyRoute.getPrefixRewrite() != null) && (!proxyRoute.getPrefixRewrite().isEmpty()))
        {
            routeActionBuilder.setPrefixRewrite(proxyRoute.getPrefixRewrite());
        }
        // We can route either to a cluster (= we specify the cluster name in the
        // config), or we tell Envoy to look at a header and take the cluster name from
        // the header ("clusterHeader"). This header is set by LUA code.
        if (proxyRoute.getCluster() != null)
        {
            routeActionBuilder.setCluster(proxyRoute.getCluster());
        }
        else
        {
            routeActionBuilder.setClusterHeader(proxyRoute.getClusterHeader());
        }

        var proxyMdBuilder = new ProxyMetadataBuilder(proxyRoute.getRouteMetadata());

        if (proxyRoute.getRouteMetadata() != null)
        {
            var metaDataBuilder = proxyMdBuilder.initMdBuilder();
            route.setMetadata(metaDataBuilder.build());
        }

        // Handling for route that specifies the "internal-spr" cluster
        if (proxyRoute.getHostRewriteLiteral() != null)
        {
            routeActionBuilder.setHostRewriteLiteral(proxyRoute.getHostRewriteLiteral());
        }

        route.setMatch(routeMatchBuilder.build()) //
             .setRoute(routeActionBuilder.build());
        return route;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyRouteToClusterBuilder [proxyRoute=" + proxyRoute + ", routeMatchBuilder=" + routeMatchBuilder + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(proxyRoute, routeMatchBuilder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyRouteToClusterBuilder other = (ProxyRouteToClusterBuilder) obj;
        return Objects.equals(proxyRoute, other.proxyRoute) && Objects.equals(routeMatchBuilder, other.routeMatchBuilder);
    }
}
