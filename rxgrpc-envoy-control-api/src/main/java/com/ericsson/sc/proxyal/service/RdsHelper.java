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
 * Created on: Jan 28, 2020
 *     Author: eedala
 */

package com.ericsson.sc.proxyal.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyDirectResponseBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteToClusterBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxySubnet;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualHost;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.Route.Builder;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.VirtualCluster;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

/**
 * 
 */
public class RdsHelper
{
    private static final Logger log = LoggerFactory.getLogger(RdsHelper.class);

    public static final String TYPE_URL = "type.googleapis.com/envoy.config.route.v3.RouteConfiguration";

    private RdsHelper()
    {
        throw new IllegalStateException("Utility class");
    }

    public static Route buildRoute(ProxyRoute proxyRoute,
                                   List<ProxyEndpoint> pxEndpoints)
    {
        proxyRoute.setLbEndpoints(pxEndpoints);
        var routeMatchBuilder = proxyRoute.initBuilder();

        Builder route;

        // Route type: direct_response -> reply directly with status code and body
        // direct_route -> route to specified destination with failover
        // round_robin -> route to a pool of equal destinations
        if (proxyRoute.getRouteType() == ProxyRoute.RouteTypes.DIRECT_RESPONSE)
        {
            route = new ProxyDirectResponseBuilder(proxyRoute, routeMatchBuilder).initBuilder();
        }
        else // "normal routes", i.e. to clusters
        {
            route = new ProxyRouteToClusterBuilder(proxyRoute, routeMatchBuilder).initBuilder();
        }

        // Some internal headers should not leave Envoy, remove them:
        for (var header : proxyRoute.getRequestHeadersToRemove())
        {
            route.addRequestHeadersToRemove(header);
        }

        return route.build();
    }

    /**
     * Helper function to create a virtual cluster
     *
     * @param vc ProxyVirtualCluster object
     * @return a VirtualCluster object
     */
    public static VirtualCluster buildVirtualCluster(ProxyVirtualCluster vc)
    {

        String name = vc.getName();
        String regex = vc.getRegex();
        String method = vc.getMethod();

        var vcb = VirtualCluster.newBuilder() //
                                .setName(name) //
                                .addHeaders(HeaderMatcher.newBuilder() //
                                                         .setName(":path")
                                                         .setStringMatch(StringMatcher.newBuilder()
                                                                                      .setSafeRegex(RegexMatcher.newBuilder().setRegex(regex).build())
                                                                                      .build())
                                                         .build());
        if (method != null)
        {
            vcb.addHeaders(HeaderMatcher.newBuilder() //
                                        .setName(":method")
                                        .setStringMatch(StringMatcher.newBuilder().setExact(method))
                                        .build());
        }
        return vcb.build();
    }

    public static VirtualHost buildVirtualHost(ProxyVirtualHost proxyVhost,
                                               List<ProxyEndpoint> pxEndpoints)
    {
        var virtualHostBuilder = VirtualHost.newBuilder().setName(proxyVhost.getvHostName());

        if (proxyVhost.getSubnets() != null && !proxyVhost.getSubnets().isEmpty())
        {
            // ipv6 subnets are added to vhosts on RCC notify routes

            for (ProxySubnet subnet : proxyVhost.getSubnets())
            {
                virtualHostBuilder.addDomains(subnet.addressForDomainMatching());
            }
        }
        else if (proxyVhost.getEndpoints() == null || proxyVhost.getEndpoints().isEmpty())
        {
            var allDomains = "*";
            virtualHostBuilder.addDomains(allDomains);
        }
        else
        {
            // First add all endpoints, then, if the port is 80 or 443, it is optional per
            // RFC7230
            // to add the port to the Host/authority header. Unfortunately, Envoy just
            // matches the
            // Host/authority header against the virtualHost settings. So if the port is 80
            // or 443,
            // we add both "host:80" and "host" to the domains.
            for (ProxyEndpoint endpoint : proxyVhost.getEndpoints())
            {
                var adr = endpoint.getAddress();
                adr = adr.contains(":") ? ("[" + adr + "]") : adr;
                virtualHostBuilder.addDomains(adr + ":" + endpoint.getPort());
                if (endpoint.getPort() == 80 || endpoint.getPort() == 443)
                {
                    virtualHostBuilder.addDomains(adr);
                }
            }
        }

        for (ProxyRoute route : proxyVhost.getRoutes())
        {
            virtualHostBuilder.addRoutes(buildRoute(route, pxEndpoints));
        }

        // sort virtual clusters based on regexp length: Longer should be tried first
        // (reversed ordering).
        // In case the length is the same (".*" for example) VCs with a method should
        // be tried first.
        proxyVhost.getVirtualClusters()
                  .sort((vc1,
                         vc2) ->
                  {
                      int vcl2 = vc2.getRegex() == null ? 0 : vc2.getRegex().length();
                      int vcl1 = vc1.getRegex() == null ? 0 : vc1.getRegex().length();
                      if (vcl1 == vcl2)
                      {
                          if ((vc1.getMethod() == null) && (vc2.getMethod() != null))
                              return 1;
                          else if ((vc2.getMethod() == null) && (vc1.getMethod() != null))
                              return -1;
                          else
                              return 0;
                      }
                      else
                          return vcl2 - vcl1;
                  });
        for (ProxyVirtualCluster vCluster : proxyVhost.getVirtualClusters())
        {
            virtualHostBuilder.addVirtualClusters(buildVirtualCluster(vCluster));
        }

        return virtualHostBuilder.build();
    }

    public static List<Any> buildRouteConfiguration(ProxyListener listener,
                                                    List<ProxyEndpoint> pxEndpoints)
    {
        List<Any> routeConfigList = new LinkedList<>();

        var routeConfigurationBuilder = RouteConfiguration.newBuilder().setName(listener.getRoutesName());

        for (var proxyVhost : listener.getVirtualHosts())
        {
            routeConfigurationBuilder.addVirtualHosts(buildVirtualHost(proxyVhost, pxEndpoints));
        }

        var routeConfig = routeConfigurationBuilder.build();
        routeConfigList.add(Any.pack(routeConfig));

        return routeConfigList;
    }

    public static List<Any> buildRoutes(ProxyCfg pxCfg)
    {
        List<Any> configList = new LinkedList<>();

        pxCfg.getListeners().forEach(listener ->
        {
            log.debug("building vHost config for Listener route {}", listener.getRoutesName());
            buildRouteConfiguration(listener, pxCfg.getProxyEndpoints()).forEach(configList::add);
        });

        log.debug("RdsHelper: returning {}", configList);
        return configList;

    }
}
