package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import io.envoyproxy.envoy.config.route.v3.DirectResponseAction;

import java.util.Objects;

import io.envoyproxy.envoy.config.core.v3.DataSource;
import io.envoyproxy.envoy.config.core.v3.HeaderValue;
import io.envoyproxy.envoy.config.core.v3.HeaderValueOption;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.Route.Builder;

public class ProxyDirectResponseBuilder
{

    private ProxyRoute proxyRoute;
    private io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder;

    public ProxyDirectResponseBuilder(ProxyRoute proxyRoute,
                                      io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder)
    {
        this.proxyRoute = proxyRoute;
        this.routeMatchBuilder = routeMatchBuilder;
    }

    public ProxyDirectResponseBuilder(ProxyDirectResponseBuilder that)
    {
        this(that.getProxyRoute(), that.getRouteMatchBuilder());
    }

    public ProxyRoute getProxyRoute()
    {
        return proxyRoute;
    }

    public void setProxyRoute(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
    }

    public ProxyDirectResponseBuilder withProxyRoute(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
        return this;
    }

    public io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder getRouteMatchBuilder()
    {
        return routeMatchBuilder;
    }

    public void setRouteMatchBuilder(io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder)
    {
        this.routeMatchBuilder = routeMatchBuilder;
    }

    public ProxyDirectResponseBuilder withRouteMatchBuilder(io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder routeMatchBuilder)
    {
        this.routeMatchBuilder = routeMatchBuilder;
        return this;
    }

    public Builder initBuilder()
    {
        var route = Route.newBuilder();
        var directResponseAction = DirectResponseAction.newBuilder() //
                                                       .setStatus(proxyRoute.getDirectResponseStatus());
        if (proxyRoute.getDirectResponseBody() != null)
        {

            directResponseAction.setBody(DataSource.newBuilder() //
                                                   .setInlineString(proxyRoute.getDirectResponseBody()) //
                                                   .build());
        }
        route.setMatch(routeMatchBuilder.build()) //
             .setDirectResponse(directResponseAction.build()) //
             .addResponseHeadersToRemove("content-type") // Envoy removes headers first, then adds headers
             .addResponseHeadersToAdd(HeaderValueOption.newBuilder() // see router/header_parser.cc
                                                       // evaluateHeaders()
                                                       .setHeader(HeaderValue.newBuilder() //
                                                                             .setKey("content-type") //
                                                                             .setValue("application/problem+json") //
                                                                             .build()) //
                                                       .build());
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
        return "ProxyDirectResponseBuilder [proxyRoute=" + proxyRoute + ", routeMatchBuilder=" + routeMatchBuilder + "]";
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
        ProxyDirectResponseBuilder other = (ProxyDirectResponseBuilder) obj;
        return Objects.equals(proxyRoute, other.proxyRoute) && Objects.equals(routeMatchBuilder, other.routeMatchBuilder);
    }

}
