package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataMap;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.RouteMatch.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher.PathSegment;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.envoyproxy.envoy.type.matcher.v3.ValueMatcher;

/**
 * Simple container class. Holds data for a traffic route in Envoy.
 * 
 */
public class ProxyRoute
{
    public enum RouteTypes
    {
        ROUND_ROBIN,
        DIRECT_ROUTE,
        DIRECT_RESPONSE,
    }

    // Relative route priorities. This is used for route sorting inside a vhost
    // Route order is important as they are evaluated in order. Sorting is done
    // according to enum order. The relative order of routes with the same enum
    // value is undefined.
    public enum RoutePriorities
    {
        /** CSA **/
        PRI_CC_DIRECT_ROUTE,
        PRI_CC_DIRECT_ROUTE_UNKNOWN,
        PRI_CC_ROUND_ROBIN,
        PRI_SLC_ROUND_ROBIN,
        PRI_SLC_NOTIFY,
        PRI_CC_NOTIFY,
        PRI_RCC_NO_FAILOVER_OWN_REGION,
        PRI_RCC_ROUND_ROBIN_OWN_REGION,
        PRI_RCC_NO_FAILOVER,
        PRI_RCC_ROUND_ROBIN,
        PRI_RCC_NOTIFY,
        PRI_SPR,

        /** SCP and SEPP **/
        PRI_SEPP_N32C,
        PRI_SEPP_CATCH_ALL,
        PRI_SEPP_LOOP_PREVENTION,
        PRI_SEPP_FAILOVER,
        PRI_SEPP_NO_FAILOVER,
        PRI_SEPP_NOTIFY,
        PRI_SEPP_NOT_FOUND
    }

    private static final String HOST_KEY = "x-host";
    String routeName;
    ProxyRouteMatch match;
    Double requestTimeoutSeconds; // TODO: (eedala) Change type to Duration
    ProxyRetryPolicy retryPolicy;
    String cluster;
    String clusterHeader;
    RouteTypes routeType;
    RoutePriorities routePriority;
    RoutingBehaviour routingBehaviour;
    Integer directResponseStatus;
    String directResponseBody;
    Boolean autoHostRewrite = true;
    String prefixRewrite;

    ProxyMetadataMap metadataMap;

    Set<String> requestHeadersToRemove = new HashSet<>();
    String hostRewriteLiteral;
    String cause;
    List<ProxyEndpoint> lbEndpoints = new ArrayList<ProxyEndpoint>();

    public ProxyRoute(String routeName,
                      ProxyRouteMatch match,
                      ProxyRetryPolicy retryPolicy,
                      String cluster,
                      String clusterHeader,
                      RouteTypes routeType,
                      RoutePriorities routePriority,
                      RoutingBehaviour routingBehaviour,
                      Double requestTimeoutSeconds)
    {
        if ((cluster == null && clusterHeader == null) || (cluster != null && clusterHeader != null))
            throw new BadConfigurationException("Internal error. Exactly one of 'cluster' or 'clusterHeader' must be specified");
        this.routeName = routeName;
        this.match = match;
        this.retryPolicy = retryPolicy;
        this.cluster = cluster;
        this.clusterHeader = clusterHeader;
        this.routeType = routeType;
        this.routePriority = routePriority;
        this.routingBehaviour = routingBehaviour;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        this.directResponseBody = null;
        this.directResponseStatus = null;
        this.cause = null;
    }

    public ProxyRoute(String routeName,
                      ProxyRouteMatch match,
                      Integer directResponseStatus,
                      String directResponseBody,
                      RoutePriorities routePriority,
                      RoutingBehaviour routingBehaviour)
    {
        this.routeName = routeName;
        this.match = match;
        this.routeType = RouteTypes.DIRECT_RESPONSE;
        this.routePriority = routePriority;
        this.routingBehaviour = routingBehaviour;
        this.directResponseStatus = directResponseStatus;
        this.directResponseBody = directResponseBody;
        this.retryPolicy = null;
        this.cluster = null;
        this.clusterHeader = null;
        this.cause = null;
    }

    public ProxyRoute(String routeName,
                      ProxyRouteMatch match,
                      Integer directResponseStatus,
                      String directResponseBody,
                      RoutePriorities routePriority,
                      RoutingBehaviour routingBehaviour,
                      String cause)
    {
        this.routeName = routeName;
        this.match = match;
        this.routeType = RouteTypes.DIRECT_RESPONSE;
        this.routePriority = routePriority;
        this.routingBehaviour = routingBehaviour;
        this.directResponseStatus = directResponseStatus;
        this.directResponseBody = directResponseBody;
        this.retryPolicy = null;
        this.cluster = null;
        this.clusterHeader = null;
        this.cause = cause;
    }

    public ProxyRoute(ProxyRoute anotherProxyRoute)
    {
        this.routeName = anotherProxyRoute.routeName;
        this.match = anotherProxyRoute.match == null ? null : new ProxyRouteMatch(anotherProxyRoute.match);
        this.requestTimeoutSeconds = anotherProxyRoute.requestTimeoutSeconds;
        this.retryPolicy = anotherProxyRoute.retryPolicy == null ? null : new ProxyRetryPolicy(anotherProxyRoute.retryPolicy);
        this.cluster = anotherProxyRoute.cluster;
        this.clusterHeader = anotherProxyRoute.clusterHeader;
        this.routeType = anotherProxyRoute.routeType;
        this.routePriority = anotherProxyRoute.routePriority;
        this.routingBehaviour = anotherProxyRoute.routingBehaviour;
        this.directResponseStatus = anotherProxyRoute.directResponseStatus;
        this.directResponseBody = anotherProxyRoute.directResponseBody;
        this.metadataMap = anotherProxyRoute.metadataMap;
        this.cause = anotherProxyRoute.cause;

    }

    /**
     * @return the routingBehaviour
     */
    public RoutingBehaviour getRoutingBehaviour()
    {
        return routingBehaviour;
    }

    /**
     * @param routingBehaviour the routingBehaviour to set
     */
    public void setRoutingBehaviour(RoutingBehaviour routingBehaviour)
    {
        this.routingBehaviour = routingBehaviour;
    }

    public String getRouteName()
    {
        return routeName;
    }

    public void setvHost(String routeName)
    {
        this.routeName = routeName;
    }

    public ProxyRouteMatch getMatch()
    {
        return match;
    }

    public void setMatch(ProxyRouteMatch match)
    {
        this.match = match;
    }

    // Helper function for ProxyListener::addDynamicNotifyRoute() to allow sorting
    // on regexp-length
//    public int getRegexpLength()
//    {
//        return this.match.regexp.length();
//    }

    public ProxyRetryPolicy getRetryPolicy()
    {
        return retryPolicy;
    }

    /**
     * @return the routePriority
     */
    public RoutePriorities getRoutePriority()
    {
        return routePriority;
    }

    public void setRetryPolicy(ProxyRetryPolicy retryPolicy)
    {
        this.retryPolicy = retryPolicy;
    }

    public Double getRequestTimeoutSeconds()
    {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(Double requestTimeoutSeconds)
    {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public String getCluster()
    {
        return cluster;
    }

    public void setCluster(String cluster)
    {
        this.cluster = cluster;
    }

    public String getClusterHeader()
    {
        return clusterHeader;
    }

    public void setClusterHeader(String clusterHeader)
    {
        this.clusterHeader = clusterHeader;
    }

    public RouteTypes getRouteType()
    {
        return this.routeType;
    }

    public void setRouteType(RouteTypes routeType)
    {
        this.routeType = routeType;
    }

    public Integer getDirectResponseStatus()
    {
        return directResponseStatus;
    }

    public void setDirectResponseStatus(Integer directResponseStatus)
    {
        this.directResponseStatus = directResponseStatus;
    }

    public String getDirectResponseBody()
    {
        return directResponseBody;
    }

    public void setDirectResponseBody(String directResponseBody)
    {
        this.directResponseBody = directResponseBody;
    }

    public Boolean getAutoHostRewrite()
    {
        return autoHostRewrite;
    }

    public void setHostRewriteLiteral(String hostRewriteLiteral)
    {
        this.hostRewriteLiteral = hostRewriteLiteral;
    }

    public String getHostRewriteLiteral()
    {
        return hostRewriteLiteral;
    }

    public void setAutoHostRewrite(boolean rewrite)
    {
        this.autoHostRewrite = rewrite;
    }

    public String getPrefixRewrite()
    {
        return prefixRewrite;
    }

    public void setPrefixRewrite(String prefix)
    {
        this.prefixRewrite = prefix;
    }

    public ProxyMetadataMap getRouteMetadata()
    {
        return metadataMap;
    }

    public void setRouteMetadata(ProxyMetadataMap metadataMap)
    {
        this.metadataMap = metadataMap;
    }

    public void addRequestHeaderToRemove(String header)
    {
        this.requestHeadersToRemove.add(header);
    }

    public void addRequestHeadersToRemove(List<String> headers)
    {
        for (var header : headers)
        {
            this.requestHeadersToRemove.add(header);
        }
    }

    public Set<String> getRequestHeadersToRemove()
    {
        return this.requestHeadersToRemove;
    }

    public void setCause(String cause)
    {
        this.cause = cause;
    }

    public String getCause()
    {
        return cause;
    }

    public String setErrorBody()
    {
        return "status: 400" + " title:" + directResponseBody + " cause:" + cause;
    }

    public List<ProxyEndpoint> getLbEndpoints()
    {
        return lbEndpoints;
    }

    public void setLbEndpoints(List<ProxyEndpoint> lbEndpoints)
    {
        this.lbEndpoints = lbEndpoints;
    }

    public Builder initBuilder()
    {
        // Collect & generate all match conditions for this route (can be more than
        // one, but only one of (path, prefix, regexp)):
        var routeMatchBuilder = RouteMatch.newBuilder();
        this.getMatch().getPath().ifPresent(p -> routeMatchBuilder.setPath(p));

        this.getMatch().getPrefix().ifPresent(p -> routeMatchBuilder.setPrefix(p));

        this.getMatch()
            .getRegexp()
            .ifPresent(r -> routeMatchBuilder.setSafeRegex(RegexMatcher.newBuilder() //
                                                                       .setRegex(r)
                                                                       .build()));

        // append all headers that have present match or not
        this.getMatch()
            .getPresentValueHeaders() //
            .map(m -> m.entrySet().stream())
            .ifPresent(s -> s.forEach(e ->
            {
                var header = HeaderMatcher.newBuilder() //
                                          .setName(e.getKey())
                                          .setPresentMatch(true);

                // if we want to match against a header that's not present
                // then we need to invert the present_match result
                if (Boolean.FALSE.equals(e.getValue()))
                {
                    header.setInvertMatch(true);
                }

                routeMatchBuilder.addHeaders(header.build());
            }));

        // check exact match headers that have both fqdn and IP in endpoints, remove
        // exact match and add regex match instead
        this.getMatch().getExactMatchValueHeaders().ifPresent(headers ->
        {
            var hostHeader = headers.get(HOST_KEY);

            if (hostHeader != null)
            {
                this.lbEndpoints.stream()
                                .filter(ep -> ep.getHostname().isPresent() && ep.getIpAddress().isPresent()
                                              && (hostHeader.contains(ep.getHostname().get().toLowerCase()) || hostHeader.contains(ep.getIpAddress().get())))
                                .findFirst()
                                .ifPresent(ep ->
                                {
                                    if (ep.getHostname().isPresent() && ep.getIpAddress().isPresent())
                                    {
                                        headers.remove(HOST_KEY);

                                        Optional<Integer> epPort = Optional.ofNullable(ep.getPort());
                                        int port = !epPort.isEmpty() ? epPort.get() : ep.getMatchTLS() ? 443 : 80;
                                        this.getMatch()
                                            .addRegexValueHeader(HOST_KEY,
                                                                 "^((" + ep.getHostname().get().toLowerCase().replace(".", "\\.") + ")|("
                                                                           + formatIpv4Ipv6AddressForRegex(ep.getIpAddress().get()) + ")):" + port + "$");
                                    }
                                });
            }
        });

        // append all headers that have exact match
        this.getMatch()
            .getExactMatchValueHeaders()
            .map(m -> m.entrySet().stream())
            .ifPresent(s -> s.forEach(e -> routeMatchBuilder.addHeaders(HeaderMatcher.newBuilder() //
                                                                                     .setName(e.getKey())
                                                                                     .setStringMatch(StringMatcher.newBuilder().setExact(e.getValue()))
                                                                                     .build())));

        if (this.getRoutingBehaviour() != null)
        {
            var mdMatcher = MetadataMatcher.newBuilder()
                                           .setFilter("eric_proxy")
                                           .addPath(PathSegment.newBuilder().setKey("routing-behaviour"))
                                           .setValue(ValueMatcher.newBuilder()
                                                                 .setStringMatch(StringMatcher.newBuilder().setExact(this.getRoutingBehaviour().toString())))
                                           .build();

            routeMatchBuilder.addDynamicMetadata(mdMatcher);
        }

        // append all headers that have regex match
        this.getMatch()
            .getRegexValueHeaders()
            .map(m -> m.entrySet().stream())
            .ifPresent(s -> s.forEach(e -> routeMatchBuilder.addHeaders(HeaderMatcher.newBuilder() //
                                                                                     .setName(e.getKey())
                                                                                     .setStringMatch(StringMatcher.newBuilder()
                                                                                                                  .setSafeRegex(RegexMatcher.newBuilder()
                                                                                                                                            .setRegex(e.getValue())))
                                                                                     .build())));
        return routeMatchBuilder;
    }

    public static String formatIpv4Ipv6AddressForRegex(String addr)
    {
        String fAddr;
        if (addr.contains("["))
        {
            fAddr = addr;
        }
        else if (addr.contains(":"))
        {
            fAddr = "[" + addr + "]";
        }
        else
        {
            fAddr = addr;
        }

        return fAddr.replace(".", "\\.").replace("[", "\\[").replace("]", "\\]");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyRoute [routeName=" + routeName + ", match=" + match + ", requestTimeoutSeconds=" + requestTimeoutSeconds + ", retryPolicy=" + retryPolicy
               + ", cluster=" + cluster + ", clusterHeader=" + clusterHeader + ", routeType=" + routeType + ", routePriority=" + routePriority
               + ", routingBehaviour=" + routingBehaviour + ", directResponseStatus=" + directResponseStatus + ", directResponseBody=" + directResponseBody
               + ", autoHostRewrite=" + autoHostRewrite + ", prefixRewrite=" + prefixRewrite + ", requestHeadersToRemove=" + requestHeadersToRemove
               + ", hostRewriteLiteral=" + hostRewriteLiteral + ", cause=" + cause + ", metadata=" + metadataMap + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(autoHostRewrite,
                            cause,
                            cluster,
                            clusterHeader,
                            directResponseBody,
                            directResponseStatus,
                            hostRewriteLiteral,
                            match,
                            prefixRewrite,
                            requestHeadersToRemove,
                            requestTimeoutSeconds,
                            retryPolicy,
                            routeName,
                            routePriority,
                            routingBehaviour,
                            routeType,
                            metadataMap);
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
        ProxyRoute other = (ProxyRoute) obj;
        return Objects.equals(autoHostRewrite, other.autoHostRewrite) && Objects.equals(cause, other.cause) && Objects.equals(cluster, other.cluster)
               && Objects.equals(clusterHeader, other.clusterHeader) && Objects.equals(directResponseBody, other.directResponseBody)
               && Objects.equals(directResponseStatus, other.directResponseStatus) && Objects.equals(hostRewriteLiteral, other.hostRewriteLiteral)
               && Objects.equals(metadataMap, other.metadataMap) && Objects.equals(match, other.match) && Objects.equals(prefixRewrite, other.prefixRewrite)
               && Objects.equals(requestHeadersToRemove, other.requestHeadersToRemove) && Objects.equals(requestTimeoutSeconds, other.requestTimeoutSeconds)
               && Objects.equals(retryPolicy, other.retryPolicy) && Objects.equals(routeName, other.routeName) && routePriority == other.routePriority
               && routingBehaviour == other.routingBehaviour && routeType == other.routeType;
    }

}
