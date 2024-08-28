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
 * Created on: Oct 27, 2022
 *     Author: echaias
 */

package com.ericsson.sc.sepp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.glue.IfEgress;
import com.ericsson.sc.proxy.clusters.N32ClusterCreator;
import com.ericsson.sc.proxy.endpoints.N32EndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyDnsEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handles route and cluster generation for the two n32c scenarios
 * 
 * Initializing SEPP: Creates an internal cluster for each RP with n32c enabled
 * as well as a route with no retry policy, leveraging the x-cluster header to
 * indicate target cluster. Route belongs to the dedicated n32c listener
 * 
 * Responding SEPP: Creates an internal cluster containing the manager's
 * endpoint serving the n32c requests coming from other sepps. Also creates a
 * route plugged in the traffic listener servicing RPs
 * 
 */
public class N32cEgress implements IfEgress
{

    private static final Logger log = LoggerFactory.getLogger(N32cEgress.class);

    /*
     * The cluster name for the responding sepp scenario is important as it's used
     * from the eric proxy filter to determine a request is n32c related and routing
     * rule processing is to be skipped. ** Do not Change the name **
     */
    private static final String N32C_SERVICE_CLUSTER_NAME = "internal_n32c_server";
    private static final String N32C_SERVICE_HOSTNAME = "eric-sepp-manager";
    private static final String N32C_MATCH_PREFIX = "/n32c-handshake/v1/exchange-capability";
    private static final int N32C_SERVICE_PORT = 8083;
    // responding sepp scenario tls enable indicator
    // This only concerns the internal part of the communication (worker->manager)
    // disabled if global tls is disabled
    // optionally may be disabled if global tls is enabled
    private static final Boolean RESP_SEPP_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_RESP_TLS_ENABLED", true));

    private final NfInstance seppInst;

    private final List<ProxyCluster> clusters = new ArrayList<>();
    private Map<String, List<ProxyRoute>> routesPerVhostName = new HashMap<>();

    N32cEgress(NfInstance seppInst)
    {
        this.seppInst = seppInst;
    }

    public void createN32cClusters()
    {
        log.info("Create N32-C ProxyCluster configuration");

        // initializing sepp scenario
        ConfigUtils.getAllRoamingPartnersWithN32C(seppInst.getExternalNetwork())
                   .forEach(rp -> Optional.ofNullable(Utils.getByName(seppInst.getNfPool(), rp.getN32C().getNfPoolRef())).ifPresent(rpSeppPool ->
                   {
                       var endpointCollector = new N32EndpointCollector(seppInst, rpSeppPool);
                       var clusterCreator = new N32ClusterCreator(rpSeppPool, seppInst, endpointCollector, rp.getName());

                       endpointCollector.createEndpoints();
                       clusterCreator.createCluster();

                       // find egress-connection-profile of pool. If not exists, find global profile.
                       // If exists, assign dscp value to cluster.
                       var epc = Utils.getByName(seppInst.getEgressConnectionProfile(), rpSeppPool.getEgressConnectionProfileRef());
                       if (epc == null)
                       {
                           epc = Utils.getByName(seppInst.getEgressConnectionProfile(), seppInst.getEgressConnectionProfileRef());
                       }
                       if (epc != null)
                       {
                           clusterCreator.getCluster().setDscpMarking(epc.getDscpMarking());
                       }

                       this.clusters.add(clusterCreator.getCluster());
                   }));

        // internal cluster for the responding sepp scenario, containg the manager's
        // endpoint
        var n32cReqCluster = new ProxyCluster(N32C_SERVICE_CLUSTER_NAME);
        n32cReqCluster.makeInternalCluster();
        n32cReqCluster.setDnsType("STRICT_DNS");
        // it was decided that if internal ip family is DS, then this cluster will be
        // ipv4.
        n32cReqCluster.setDnsLookupFamily(RuntimeEnvironment.getDeployedIpVersion().equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                            : DnsLookupFamily.V4_ONLY);
        n32cReqCluster.addDnsEndpoint(new ProxyDnsEndpoint(N32C_SERVICE_HOSTNAME, N32C_SERVICE_PORT));
        if (RESP_SEPP_TLS_ENABLED.booleanValue())
        {
            n32cReqCluster.setTls(new ProxyTls("internal_n32c_server_ca", "n32c_client_cert"));
        }
        this.clusters.add(n32cReqCluster);
    }

    public void createN32cRoutes()
    {
        log.info("Create N32-C ProxyRoute configuration");
        // Only one route is needed for the n32c initializing sepp scenario. The chosen
        // cluster is retrieved
        // from the clusterHeader. Retries are not needed
        final var routeInit = new ProxyRoute("n32c_init_route",
                                             new ProxyRouteMatch().setPrefix("/"),
                                             null,
                                             null,
                                             "x-cluster",
                                             ProxyRoute.RouteTypes.DIRECT_ROUTE,
                                             ProxyRoute.RoutePriorities.PRI_SEPP_CATCH_ALL,
                                             null,
                                             2D);

        final var routeN32cLis = new ProxyRoute("n32c_listener_route",
                                                new ProxyRouteMatch().setPrefix("/n32c-listener-connection"),
                                                HttpResponseStatus.OK.code(),
                                                HttpResponseStatus.OK.toString(),
                                                ProxyRoute.RoutePriorities.PRI_SEPP_N32C,
                                                null);

        routeInit.setAutoHostRewrite(false);
        routeN32cLis.setAutoHostRewrite(false);
        routesPerVhostName.put(ServiceConfig.INT_N32C_SERVICE, List.of(routeN32cLis, routeInit));
        log.debug("N32-C Init sepp Route: {}", routeInit);

        final var routeMatch = new ProxyRouteMatch().setPrefix(N32C_MATCH_PREFIX);
        final var routeResp = new ProxyRoute("n32c_resp_route",
                                             routeMatch,
                                             null,
                                             N32C_SERVICE_CLUSTER_NAME,
                                             null,
                                             ProxyRoute.RouteTypes.ROUND_ROBIN,
                                             ProxyRoute.RoutePriorities.PRI_SEPP_N32C,
                                             null,
                                             2D); // request timeout 2 seconds

        routesPerVhostName.put(ServiceConfig.INT_SERVICE, List.of(routeResp));

    }

    public List<ProxyCluster> getClusters()
    {
        return this.clusters;
    }

    public List<ProxyRoute> getRoutesForVHostName(String vHostName)
    {
        return routesPerVhostName.get(vHostName);
    }

    public void convertConfig()
    {
        // just for interface compatibility purposes

    }

}
