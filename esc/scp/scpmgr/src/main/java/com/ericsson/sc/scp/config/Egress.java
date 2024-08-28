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
 * Created on: Sep 1, 2020
 * Author: eedrak
 */

package com.ericsson.sc.scp.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.glue.IfEgress;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.clusters.AggregateClusterCreator;
import com.ericsson.sc.proxy.clusters.ClusterCreator;
import com.ericsson.sc.proxy.clusters.DfwClusterCreator;
import com.ericsson.sc.proxy.clusters.BaseClusterCreator;
import com.ericsson.sc.proxy.endpoints.RoundRobinEndpointCollector;
import com.ericsson.sc.proxy.routes.BaseRouteCreator;
import com.ericsson.sc.proxy.routes.DfwRouteCreator;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyDnsEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyEjectionPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.RoutingAction;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

/**
 * Responsible to create proxyClusters and proxyRoutes to them. Not all routes
 * are created here, some are created in the RoutingContext.
 */
public class Egress implements IfEgress
{
    // Constants that have to match between and inside Java and LUA. They are
    // exported to LUA/Velocity in ServiceConfig.java via the LUA-context.
    public static final String HEADER_POST_LUA = "x-lua";
    public static final String HEADER_FORWARDED_PROTO = "x-forwarded-proto";
    public static final String HEADER_X_NOTIFY_URI = "x-notify-uri";
    public static final String HEADER_ERIC_PROXY = "x-eric-proxy";
    public static final String HEADER_HOST = "x-host";
    public static final String HEADER_CLUSTER = "x-cluster";
    public static final String HEADER_EXPECTED_RQ_TIMEOUT = "x-envoy-expected-rq-timeout";
    public static final String HEADER_FAILOVER_PROFILE = "x-eric-fop";
    public static final String METADATA_MATE_SCP = "mate_scp";  // used to exclude mate scp for loop prevention.

    private static final String SLF_SERVICE_HOSTNAME = "eric-sc-slf-v2";
    private static final int SLF_SERVICE_PORT_TLS = 8443;
    private static final String SLF_SERVICE_CLUSTER_NAME = "internal-slf";

    public static final String RLF_SERVICE_HOSTNAME = "eric-sc-rlf";
    public static final int RLF_SERVICE_PORT = 8081;
    public static final String RLF_SERVICE_CLUSTER_NAME = "global_rate_limit";

    private static final String NLF_SERVICE_CLUSTER_NAME = "internal-nlf";
    public static final String NLF_SERVICE_HOSTNAME = "eric-sc-nlf";
    private static final int NLF_SERVICE_PORT = 8083;

    private static final Logger log = LoggerFactory.getLogger(Egress.class);

    private final NfInstance scpInst;
    private final Set<ProxyCluster> clusters = new HashSet<>();
    private final Map<String, Set<ProxyRoute>> routesPerVHostName = new HashMap<>();

    /**
     * @param scpInst
     */
    public Egress(NfInstance scpInst)
    {
        this.scpInst = scpInst;
    }

    @Override
    public Collection<ProxyCluster> getClusters()
    {
        return clusters;
    }

    public Collection<ProxyRoute> getRoutesForVHostName(String vHostName)
    {
        return routesPerVHostName.get(vHostName);
    }

    /**
     * From the configuration, create all proxyClusters and the proxyRoutes to them.
     */
    @Override
    public void convertConfig()
    {
        log.debug("create clusters from Routing Actions.");

        for (var routingCase : this.scpInst.getRoutingCase())
        {
            routingCase.getRoutingRule().forEach(rr -> rr.getRoutingAction().forEach(ra ->
            {
                if (CommonConfigUtils.isPreferredRoutingRule(ra))
                {
                    this.preferredRouting(ra);
                }
                else if (CommonConfigUtils.isRoundRobinRule(ra))
                {
                    this.roundRobin(ra);
                }
                else if (CommonConfigUtils.isStrictRoutingRule(ra))
                {
                    this.strictRouting(ra);
                }
                else if (ConfigUtils.isSlfRoutingRule(ra))
                {
                    this.slfRouting();
                }
                else if (ConfigUtils.isRemoteRoundRobinAction(ra))
                {
                    this.remoteRoundRobin(ra);
                }
                else if (ConfigUtils.isRemotePreferredAction(ra))
                {
                    this.remotePreferred(ra);
                }
                else if (ConfigUtils.isNfDiscoveryAction(ra))
                {
                    this.nlfRouting();
                }
                else if (CommonConfigUtils.isOtherRoutingRule(ra))
                {
                    // do nothing
                }
                else
                {
                    throw new BadConfigurationException("Unknown type of Routing rule");
                }
            }));
        }
        if (ConfigUtils.isRateLimitConfigured(scpInst))
            this.rlfRouting();
        // For all routes created, attach a list of headers to remove. These headers are
        // the worker-internal headers that should not leave the worker/Envoy
        configureRemoveRequestHeaders();
    }

    private void nlfRouting()
    {
        this.clusters.add(createNlfCluster());
    }

    private void slfRouting()
    {
        this.clusters.add(createSlfCluster());
    }

    private void rlfRouting()
    {
        this.clusters.add(createRlfCluster());
    }

    private void preferredRouting(final RoutingAction ra)
    {
        var actionPreferred = ra.getActionRoutePreferred();
        var vHost = actionPreferred.getKeepAuthorityHeader() != null ? ServiceConfig.INT_FORWARD_SERVICE : ServiceConfig.INT_SERVICE;
        var keepOriginalAuthorityHeader = actionPreferred.getKeepAuthorityHeader() != null;
        var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionPreferred.getLastResortNfPoolRef(), ra, this.scpInst);
        var failoverProfileRef = actionPreferred.getFailoverProfileRef();
        List<NfPool> pools;
        if (actionPreferred.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionPreferred.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionPreferred.getTargetNfPool().getNfPoolRef());
            if (referencedPool == null)
            {
                throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                    actionPreferred.getTargetNfPool().getNfPoolRef(),
                                                    ra.getName());
            }
            pools = List.of(referencedPool);
        }
        else // actionPreferred.getTargetNfPool().getVarName() != null
        {
            pools = this.scpInst.getNfPool()
                                .stream()
                                .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName())))
                                .collect(Collectors.toList());
        }

        pools.forEach((pool -> this.constructPreferredPool(pool, lastResortPool, vHost, Optional.ofNullable(failoverProfileRef), keepOriginalAuthorityHeader)));
    }

    private void roundRobin(final RoutingAction ra)
    {
        var actionRoundRobin = ra.getActionRouteRoundRobin();
        var vHost = actionRoundRobin.getKeepAuthorityHeader() != null ? ServiceConfig.INT_FORWARD_SERVICE : ServiceConfig.INT_SERVICE;
        var keepOriginalAuthorityHeader = actionRoundRobin.getKeepAuthorityHeader() != null;
        var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionRoundRobin.getLastResortNfPoolRef(), ra, this.scpInst);
        var failoverProfileRef = actionRoundRobin.getFailoverProfileRef();
        List<NfPool> pools;
        if (actionRoundRobin.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionRoundRobin.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionRoundRobin.getTargetNfPool().getNfPoolRef());
            if (referencedPool == null)
            {
                throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                    actionRoundRobin.getTargetNfPool().getNfPoolRef(),
                                                    ra.getName());
            }
            pools = List.of(referencedPool);
        }
        else // actionPreferred.getTargetNfPool().getVarName() != null
        {
            pools = this.scpInst.getNfPool()
                                .stream()
                                .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName())))
                                .collect(Collectors.toList());
        }

        pools.forEach((pool ->
        {
            this.constructRoundRobinPool(pool, lastResortPool, vHost, Optional.ofNullable(failoverProfileRef), keepOriginalAuthorityHeader);
        }));
    }

    private void strictRouting(final RoutingAction ra)
    {
        var actionStrict = ra.getActionRouteStrict();
        var vHost = actionStrict.getKeepAuthorityHeader() != null ? ServiceConfig.INT_FORWARD_SERVICE : ServiceConfig.INT_SERVICE;
        var keepOriginalAuthorityHeader = actionStrict.getKeepAuthorityHeader() != null;
        List<NfPool> pools;
        if (actionStrict.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionStrict.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionStrict.getTargetNfPool().getNfPoolRef());
            if (referencedPool == null)
            {
                throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                    actionStrict.getTargetNfPool().getNfPoolRef(),
                                                    ra.getName());
            }
            pools = List.of(referencedPool);
        }
        else // actionPreferred.getTargetNfPool().getVarName() != null
        {
            pools = this.scpInst.getNfPool();
        }

        pools.forEach(pool ->
        {
            if ((pool.getStaticScpInstanceDataRef() == null || pool.getStaticScpInstanceDataRef().isEmpty())
                && (pool.getStaticSeppInstanceDataRef() == null || pool.getStaticSeppInstanceDataRef().isEmpty())
                && (pool.getNfPoolDiscovery() == null || pool.getNfPoolDiscovery().isEmpty()))
            {
                var vHostAll = ServiceConfig.INT_FORWARD_SERVICE;
                log.debug("vHost:{}", vHostAll);
                this.constructStrictRoutingPoolDynFwd(pool, vHostAll, Optional.ofNullable(actionStrict.getFailoverProfileRef()));
            }
            else
            {
                this.constructStrictRoutingPool(pool, vHost, Optional.ofNullable(actionStrict.getFailoverProfileRef()), keepOriginalAuthorityHeader);
            }
        });
    }

    private void remoteRoundRobin(final RoutingAction ra)
    {
        var actionRemoteRoundRobin = ra.getActionRouteRemoteRoundRobin();
        var vHost = ServiceConfig.INT_SERVICE;
        var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionRemoteRoundRobin.getLastResortNfPoolRef(), ra, this.scpInst);
        var failoverProfileRef = actionRemoteRoundRobin.getFailoverProfileRef();
        List<NfPool> pools;
        if (actionRemoteRoundRobin.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionRemoteRoundRobin.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionRemoteRoundRobin.getTargetNfPool().getNfPoolRef());
            if (referencedPool == null)
            {
                throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                    actionRemoteRoundRobin.getTargetNfPool().getNfPoolRef(),
                                                    ra.getName());
            }
            pools = List.of(referencedPool);
        }
        else
        {
            pools = this.scpInst.getNfPool()
                                .stream()
                                .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName())))
                                .collect(Collectors.toList());
        }

        pools.forEach((pool -> this.constructRemoteRoundRobinPool(pool, lastResortPool, vHost, Optional.ofNullable(failoverProfileRef))));
    }

    private void remotePreferred(final RoutingAction ra)
    {
        var actionRouteRemotePreferred = ra.getActionRouteRemotePreferred();
        var vHost = actionRouteRemotePreferred.getFromTargetApiRootHeader() == null ? ServiceConfig.INT_FORWARD_SERVICE : ServiceConfig.INT_SERVICE;
        var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionRouteRemotePreferred.getLastResortNfPoolRef(), ra, this.scpInst);
        var failoverProfileRef = actionRouteRemotePreferred.getFailoverProfileRef();
        List<NfPool> pools;
        if (actionRouteRemotePreferred.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionRouteRemotePreferred.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionRouteRemotePreferred.getTargetNfPool().getNfPoolRef());
            if (referencedPool == null)
            {
                throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                    actionRouteRemotePreferred.getTargetNfPool().getNfPoolRef(),
                                                    ra.getName());
            }
            pools = List.of(referencedPool);
        }
        else
        {
            pools = this.scpInst.getNfPool()
                                .stream()
                                .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName())))
                                .collect(Collectors.toList());
        }

        /*
         * Action Remote Preferred is used for preferred inter-PLMN routing. Though the
         * routing is done based on the TaR, this consists a specific case of indirect
         * routing where the next hop is the own SEPP. Thus, in the configuration the
         * pool configured is supposed to be the SEPP pool. The SCP is supposed to use
         * (or reselect) the TaR but then forward the request indirectly to the own SEPP
         * in a round robin manner
         */
        pools.forEach((pool -> this.constructRemotePreferredPool(pool, lastResortPool, vHost, Optional.ofNullable(failoverProfileRef))));

    }

    /**
     * @param pool
     * @param failoverProfileName
     */
    private void constructRemotePreferredPool(NfPool pool,
                                              Optional<IfNfPool> lastResortPool,
                                              String vHostName,
                                              Optional<String> failoverProfileName)
    {
        log.debug("Remote Preferred pool");

        var rrCollector = new RoundRobinEndpointCollector(scpInst, pool, false, failoverProfileName);

        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(scpInst, lastResortPool.get(), false, failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), scpInst, rrCollector, lrCollector);
        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, scpInst, rrCollector);
        }
        clusterCreator.createCluster();
        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), scpInst, clusterCreator, RoutingBehaviour.REMOTE_PREFERRED);
        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);

        CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
    }

    /**
     * @param pool
     * @param failoverProfileName
     */
    private void constructRoundRobinPool(NfPool pool,
                                         Optional<IfNfPool> lastResortPool,
                                         String vHostName,
                                         Optional<String> failoverProfileName,
                                         boolean keepAuthorityHeaderUnchanged)
    {
        log.debug("Round-Robin pool");

        var rrCollector = new RoundRobinEndpointCollector(scpInst, pool, keepAuthorityHeaderUnchanged, failoverProfileName);

        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(scpInst,
                                                                                      lastResortPool.get(),
                                                                                      keepAuthorityHeaderUnchanged,
                                                                                      failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), scpInst, rrCollector, lrCollector);
        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, scpInst, rrCollector);
        }
        clusterCreator.createCluster();
        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), scpInst, clusterCreator, RoutingBehaviour.ROUND_ROBIN);
        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);

        // Add the route to all roaming-partners and the internal service, but not to
        // the internal-forwarding service (universal cluster). TODO: only add to those
        // VHosts where it is really needed (find out via routing cases)
        CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
    }

    /**
     * @param pool
     * @param failoverProfileName
     */
    private void constructRemoteRoundRobinPool(NfPool pool,
                                               Optional<IfNfPool> lastResortPool,
                                               String vHostName,
                                               Optional<String> failoverProfileName)
    {
        log.debug("Remote-Round-Robin pool");

        var rrCollector = new RoundRobinEndpointCollector(scpInst, pool, false, failoverProfileName);

        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(scpInst, lastResortPool.get(), false, failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), scpInst, rrCollector, lrCollector);
        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, scpInst, rrCollector);
        }
        clusterCreator.createCluster();
        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), scpInst, clusterCreator, RoutingBehaviour.REMOTE_ROUND_ROBIN);
        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);

        // Add the route to all roaming-partners and the internal service, but not to
        // the internal-forwarding service (universal cluster). TODO: only add to those
        // VHosts where it is really needed (find out via routing cases)
        CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
    }

    private void constructStrictRoutingPool(NfPool pool,
                                            String vHostName,
                                            Optional<String> failoverProfileName,
                                            boolean keepAuthorityHeaderUnchanged)
    {
        log.debug("Strict routing pool");
        RoundRobinEndpointCollector rrCollector = new RoundRobinEndpointCollector(scpInst, pool, keepAuthorityHeaderUnchanged, failoverProfileName);
        ClusterCreator clusterCreator = new BaseClusterCreator(pool, scpInst, rrCollector);
        clusterCreator.createCluster();
        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), scpInst, clusterCreator, RoutingBehaviour.STRICT);
        routeCreator.createRoutes();
        clusterCreator.appendClusters(clusters);
        CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
    }

    /*
     * When strict routing applies for dynamic forward pool create the clusters for
     * tls and non-tls based on configuration of service address.
     *
     * Two routes are added, one for tls and one for non-tls case, for both CC and
     * SLC services. TMO uses this functionality.
     *
     * @param pool
     *
     * @param vHostName
     *
     * @param proxyRetryPolicy
     *
     * @param keepAuthorityHeaderUnchanged
     *
     * @return
     */
    private void constructStrictRoutingPoolDynFwd(NfPool pool,
                                                  String vHostName,
                                                  Optional<String> failoverProfileName)
    {
        log.debug("Strict routing for dynamic forwarding pool");

        // From the routing case identify the own network service address
        // and create routes for dynamic forwarding functionality for
        // tls and non tls configuration.
        // Create the corresponding routes for vHost (*)

        List<ProxyRoute> routesForVHost = new ArrayList<>();

        for (boolean wantTls : getDynFwdTlsEnabledOptions(scpInst))
        {
            log.debug("wantTls: {}", wantTls);

            var clusterCreator = new DfwClusterCreator(pool, wantTls, scpInst);
            clusterCreator.createCluster();

            var routeCreator = new DfwRouteCreator(failoverProfileName.orElse(null), wantTls, scpInst, clusterCreator, true);
            routeCreator.createRoutes();

            var dynFwdCluster = clusterCreator.getCluster();
            var dynFqdRoutes = routeCreator.getRoutes();
            this.clusters.add(dynFwdCluster);

            routesForVHost.addAll(dynFqdRoutes);
        }

        CommonConfigUtils.addRoutesUnderVHost(vHostName, routesForVHost, this.routesPerVHostName);
    }

    /**
     * @param pool
     * @param vHost
     */

    private void constructPreferredPool(NfPool pool,
                                        Optional<IfNfPool> lastResortPool,
                                        String vHost,
                                        Optional<String> failoverProfileName,
                                        boolean keepAuthorityHeader)
    {
        log.debug("Aggregate/Preferred Cluster");

        var rrCollector = new RoundRobinEndpointCollector(scpInst, pool, keepAuthorityHeader, failoverProfileName);
        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(scpInst, lastResortPool.get(), keepAuthorityHeader, failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), scpInst, rrCollector, lrCollector);

        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, scpInst, rrCollector);

        }
        clusterCreator.createCluster();
        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), scpInst, clusterCreator, RoutingBehaviour.PREFERRED);
        routeCreator.createRoutes();
        clusterCreator.appendClusters(clusters);
        CommonConfigUtils.addRoutesUnderVHost(vHost, routeCreator.getRoutes(), this.routesPerVHostName);
    }

    /**
     * Remove the worker-internal headers that we need for routing (because we
     * cannot use metadata in route matchers in Envoy) Do this for all routes.<br>
     * Also remove two Envoy-generated headers that are unnecessary.
     */
    private void configureRemoveRequestHeaders()
    {
        for (var vhostRoutes : this.routesPerVHostName.entrySet())
        {
            for (var route : vhostRoutes.getValue())
            {
                route.addRequestHeadersToRemove(List.of(HEADER_POST_LUA,
                                                        //
                                                        HEADER_HOST,
                                                        HEADER_CLUSTER,
                                                        HEADER_FAILOVER_PROFILE,
                                                        HEADER_FORWARDED_PROTO,
                                                        HEADER_ERIC_PROXY,
                                                        HEADER_EXPECTED_RQ_TIMEOUT,
                                                        HEADER_X_NOTIFY_URI));

            }
        }
    }

    /**
     * Creates the SLF cluster
     *
     * @return
     */
    private ProxyCluster createSlfCluster()
    {
        final var slfCluster = new ProxyCluster(SLF_SERVICE_CLUSTER_NAME);
        slfCluster.makeInternalCluster();
        slfCluster.setDnsType("STRICT_DNS");
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        slfCluster.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                                : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                            : DnsLookupFamily.ALL);
        slfCluster.addDnsEndpoint(new ProxyDnsEndpoint(SLF_SERVICE_HOSTNAME, SLF_SERVICE_PORT_TLS));
        slfCluster.setEjectionPolicy(new ProxyEjectionPolicy(5, 5, 5));
        slfCluster.getCircuitBreaker().setBudgetPercent(100);
        var tls = new ProxyTls("internal_ca", EnvVars.get("SLF_CERT_NAME"));
        slfCluster.setTls(tls);
        return slfCluster;
    }

    /**
     * Creates the RLF cluster
     *
     * @return
     */
    private ProxyCluster createRlfCluster()
    {
        final var rlfCluster = new ProxyCluster(RLF_SERVICE_CLUSTER_NAME);
        rlfCluster.makeInternalCluster();
        rlfCluster.setDnsType("STRICT_DNS");
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        rlfCluster.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                                : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                            : DnsLookupFamily.ALL);
        rlfCluster.addDnsEndpoint(new ProxyDnsEndpoint(RLF_SERVICE_HOSTNAME, RLF_SERVICE_PORT));
        // rlfCluster.setEjectionPolicy(new ProxyEjectionPolicy(5, 5, 5));
        // rlfCluster.setLbPolicy("RANDOM");
        var tls = new ProxyTls("internal_ca", EnvVars.get("RLF_CERT_NAME"));
        rlfCluster.setTls(tls);
        return rlfCluster;
    }

    /**
     * Creates the NLF cluster
     *
     * @return
     */
    private ProxyCluster createNlfCluster()
    {
        final var nlfCluster = new ProxyCluster(NLF_SERVICE_CLUSTER_NAME);
        nlfCluster.makeInternalCluster();
        nlfCluster.setDnsType("STRICT_DNS");
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        nlfCluster.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                                : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                            : DnsLookupFamily.ALL);
        nlfCluster.addDnsEndpoint(new ProxyDnsEndpoint(NLF_SERVICE_HOSTNAME, NLF_SERVICE_PORT));
        nlfCluster.setEjectionPolicy(new ProxyEjectionPolicy(5, 5, 5));
        nlfCluster.getCircuitBreaker().setBudgetPercent(100);
        var tls = new ProxyTls("internal_ca", EnvVars.get("NLF_CERT_NAME"));
        nlfCluster.setTls(tls);
        return nlfCluster;
    }

    /**
     * Return a list of boolean values indicating if for the given service the
     * Dynamic Forwarding cluster(s) for notify/SLC Terminate shall have TLS enabled
     * or not. Since a ServiceAddress in the CM configuration can have both
     * Dynamic-Forwarding-cluster with TLS and without. That is why this function
     * returns a list.
     *
     * @param scpInst
     * @return
     */

    public static List<Boolean> getDynFwdTlsEnabledOptions(NfInstance scpInst)
    {
        List<Boolean> options = new ArrayList<>();

        if ((scpInst.getRoutingCase() == null) || (scpInst.getRoutingCase().isEmpty()))
            return options;

        for (var rc : scpInst.getRoutingCase())
        {
            rc.getRoutingRule().forEach(rr -> rr.getRoutingAction().forEach(ra ->
            {
                if (CommonConfigUtils.isStrictRoutingRule(ra))
                {
                    scpInst.getOwnNetwork()
                           .stream()
                           .filter(on -> on.getRoutingCaseRef().equals(rc.getName()))
                           .map(on -> Utils.getByName(scpInst.getServiceAddress(), on.getServiceAddressRef()))
                           .filter(Objects::nonNull)
                           .forEach(sa ->
                           {
                               // If there is a non-TLS port, then a non-TLS Notify cluster is required:
                               if ((sa.getPort() != null) && (!options.contains(false)))
                                   options.add(false);
                               // If there is a TLS port, then a TLS Notify cluster is needed:
                               if ((sa.getTlsPort() != null) && (!options.contains(true)))
                                   options.add(true);

                           });

                }

            }));
        }

        return options;
    }

}
