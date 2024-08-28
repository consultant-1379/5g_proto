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

package com.ericsson.sc.sepp.config;

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
import com.ericsson.sc.configutil.CommonConfigUtils.SeppDatum;
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
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoutingAction;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
//TODO: decide if this should be a separate enum in the proxyconfig packages or importing this
// is ok
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

/**
 * Responsible to create proxyClusters and proxyRoutes to them. Not all routes
 * are created here, some are created in the RoutingContext.
 */
public class Egress implements IfEgress
{
    public static final String HEADER_POST_LUA = "x-lua";
    public static final String HEADER_FORWARDED_PROTO = "x-forwarded-proto";
    public static final String HEADER_ERIC_PROXY = "x-eric-proxy";
    public static final String HEADER_HOST = "x-host";
    public static final String HEADER_CLUSTER = "x-cluster";
    public static final String HEADER_EXPECTED_RQ_TIMEOUT = "x-envoy-expected-rq-timeout";
    public static final String HEADER_FAILOVER_PROFILE = "x-eric-fop";

    public static final String RLF_SERVICE_HOSTNAME = "eric-sc-rlf";
    public static final int RLF_CONTAINER_PORT = 8081;
    public static final String RLF_SERVICE_CLUSTER_NAME = "global_rate_limit";

    private static final Logger log = LoggerFactory.getLogger(Egress.class);

    private final NfInstance seppInst;

    private final Set<ProxyCluster> clusters = new HashSet<>();
    private final Map<String, Set<ProxyRoute>> routesPerVHostName = new HashMap<>();
    private final Optional<Map<String, Triplet<String, String, Boolean>>> seppData;

    /**
     * @param seppInst
     */
    public Egress(NfInstance seppInst,
                  Optional<Map<String, Triplet<String, String, Boolean>>> seppData)
    {
        this.seppInst = seppInst;
        this.seppData = seppData;
    }

    @Override
    public Collection<ProxyCluster> getClusters()
    {
        return clusters;
    }

    @Override
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

        for (var routingCase : this.seppInst.getRoutingCase())
        {
            routingCase.getRoutingRule().forEach(rr ->
            {
                rr.getRoutingAction().forEach(ra ->
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
                    else if (CommonConfigUtils.isOtherRoutingRule(ra))
                    {
                        // do nothing
                    }
                    else
                    {
                        throw new BadConfigurationException("Unknown type of Routing rule");
                    }
                });

            });
        }
        if (ConfigUtils.isRateLimitConfigured(seppInst))
            this.rlfRouting();

        // Create N32c relevant configuration if it's enabled on n32-c local
        // configuration
        if (seppInst.getN32C() != null)
        {
            this.n32cRouting();
        }

        // For all routes created, attach a list of headers to remove. These headers are
        // the worker-internal headers that should not leave the worker/Envoy
        configureRemoveRequestHeaders();

    }

    private void rlfRouting()
    {
        this.clusters.add(createRlfCluster());
    }

    private void preferredRouting(final RoutingAction ra)
    {
        var actionPreferred = ra.getActionRoutePreferred();

        var vHostList = ConfigUtils.collectVHosts();

        if (actionPreferred.getTargetNfPool() != null)
        {
            var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionPreferred.getLastResortNfPoolRef(), ra, this.seppInst);

            // pool-ref for destination pool
            if (actionPreferred.getTargetNfPool().getNfPoolRef() != null)
            {
                var pool = Utils.getByName(this.seppInst.getNfPool(), actionPreferred.getTargetNfPool().getNfPoolRef());
                if (pool == null)
                {
                    throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                        actionPreferred.getTargetNfPool().getNfPoolRef(),
                                                        ra.getName());
                }

                this.constructPreferredPool(pool, lastResortPool, vHostList, Optional.ofNullable(actionPreferred.getFailoverProfileRef()));
            }
            else if (actionPreferred.getTargetNfPool().getVarName() != null)
            {
                // var-name for destination pool, construct all clusters for all pools
                this.seppInst.getNfPool()
                             .stream()
                             .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName()))) //
                             .forEach(p -> this.constructPreferredPool(p,
                                                                       lastResortPool,
                                                                       vHostList,
                                                                       Optional.ofNullable(actionPreferred.getFailoverProfileRef())));
            }
            else
            {
                throw new BadConfigurationException("No valid destination found for routing-action {}", ra.getName());
            }
        }
        else
        {
            throw new BadConfigurationException("No valid destination found for routing-action {}", ra.getName());
        }
    }

    private void roundRobin(final RoutingAction ra)
    {
        var actionRoundRobin = ra.getActionRouteRoundRobin();
        var vHostList = ConfigUtils.collectVHosts();
        var lastResortPool = CommonConfigUtils.tryGetLastResortPool(actionRoundRobin.getLastResortNfPoolRef(), ra, this.seppInst);
        var failoverProfileRef = actionRoundRobin.getFailoverProfileRef();
        List<NfPool> pools;
        Optional<RoamingPartner> rp;
        if (actionRoundRobin.getTargetNfPool() != null)
        {
            rp = Optional.empty();
            // pool-ref for destination pool
            if (actionRoundRobin.getTargetNfPool().getNfPoolRef() != null)
            {
                var referencedPool = Utils.getByName(this.seppInst.getNfPool(), actionRoundRobin.getTargetNfPool().getNfPoolRef());
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
                pools = this.seppInst.getNfPool()
                                     .stream()
                                     .filter(p -> !(lastResortPool.map(IfNfPool::getName)).equals(Optional.of(p.getName())))
                                     .collect(Collectors.toList());
            }
        }
        else if (actionRoundRobin.getTargetRoamingPartner() != null)
        {
            // roaming-partner for destination pool
            var roamingPartner = Utils.getByName(this.seppInst.getExternalNetwork()
                                                              .stream()
                                                              .flatMap(enw -> enw.getRoamingPartner().stream())
                                                              .collect(Collectors.toList()),
                                                 actionRoundRobin.getTargetRoamingPartner().getRoamingPartnerRef());
            if (roamingPartner == null)
            {
                throw new BadConfigurationException("Roaming partner {} could not be found for routing-action {}",
                                                    actionRoundRobin.getTargetRoamingPartner().getRoamingPartnerRef(),
                                                    ra.getName());
            }
            rp = Optional.of(roamingPartner);
            var pool = ConfigUtils.fetchNfPoolFromRoamingPartner(roamingPartner.getName(), this.seppInst.getNfPool());
            if (pool.isPresent())
            {
                pools = List.of(pool.get());
            }
            else
            {
                throw new BadConfigurationException("Nf-pool could not be found for the roaming partner {} in routing-action {}",
                                                    roamingPartner.getName(),
                                                    ra.getName());
            }
        }
        else
        {
            throw new BadConfigurationException("No valid destination found for routing-action {}", ra.getName());
        }
        pools.forEach((pool ->
        {
            this.constructRoundRobinPool(pool, lastResortPool, rp, vHostList, Optional.ofNullable(failoverProfileRef));
        }));

    }

    private void strictRouting(final RoutingAction ra)
    {
        var actionStrict = ra.getActionRouteStrict();
        var vHostList = ConfigUtils.collectVHosts();
        List<NfPool> pools;
        if (actionStrict.getTargetNfPool() == null)
        {
            throw new BadConfigurationException("No valid destination for routing rule  {}", ra.getName());
        }
        if (actionStrict.getTargetNfPool().getNfPoolRef() != null)
        {
            var referencedPool = Utils.getByName(this.seppInst.getNfPool(), actionStrict.getTargetNfPool().getNfPoolRef());
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
            pools = this.seppInst.getNfPool();
        }

        // If no discovery query or static nf-instances are defined for the pool, the
        // SEPP manager only configures a dynamic forwarding cluster.
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
                this.constructStrictRoutingPool(pool, vHostList, Optional.ofNullable(actionStrict.getFailoverProfileRef()));
            }
        });
    }

    /**
     * @param pool
     */
    private void constructRoundRobinPool(NfPool pool,
                                         Optional<IfNfPool> lastResortPool,
                                         Optional<RoamingPartner> rp,
                                         List<String> vHostNames,
                                         Optional<String> failoverProfileName)
    {
        log.debug("Round-Robin pool");
        RoundRobinEndpointCollector rrCollector;

        if (rp.isPresent())
        {
            rrCollector = new RoundRobinEndpointCollector(pool, rp, seppInst, failoverProfileName);

        }
        else
        {
            rrCollector = new RoundRobinEndpointCollector(seppInst, pool, false, failoverProfileName);
        }

        // If n32c is enabled for rp, then create endpoints only for active sepps under
        // rp. The target-roaming-partner-ref only exists in round-robin routing
        // rule
        if (rp.isPresent() && rp.get().getN32C() != null && rp.get().getN32C().getEnabled() && this.seppData.isPresent())
        {
            log.info("N32C is configured in RP:{}", rp.get().getName());
            // Keep the data only for the roaming partner of interest
            Map<String, SeppDatum> seppDataPerRP = new HashMap<>();

            this.seppData.get().entrySet().forEach(entry ->
            {
                if (entry.getValue().getFirst().equals(rp.get().getName()))
                    seppDataPerRP.put(entry.getKey(), new SeppDatum(entry.getValue().getSecond(), entry.getValue().getThird()));
            });
            log.debug("Data for rp {} are {}", rp.get().getName(), seppDataPerRP);
            rrCollector.setN32cParameters(true, seppDataPerRP);
        }
        else
        {
            rrCollector.setN32cParameters(false, null);
        }

        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(seppInst, lastResortPool.get(), false, failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), seppInst, rrCollector, lrCollector);
        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, seppInst, rrCollector);

        }
        clusterCreator.createCluster();

        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), seppInst, clusterCreator, RoutingBehaviour.ROUND_ROBIN);

        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);

        // Add the route to all roaming-partners and the internal service, but not to
        // the internal-forwarding service (universal cluster). TODO: only add to those
        // VHosts where it is really needed (find out via routing cases)
        for (final var vHostName : vHostNames)
        {
            CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
        }
    }

    /*
     * When strict routing after DNS resultion applies, the dynamic forward pool is
     * constructed and the clusters for tls and non-tls are created based on
     * configuration of service address.
     *
     * Routes are created, for both for tls and for non-tls case. For SEPP only
     * 3gpp-Sbi-target-apiRoot header is supported.
     *
     * The routes are added under vHost (*)
     *
     * @param pool
     *
     * @param vHostName
     *
     * @param failoverProfileName
     *
     * @return
     */
    private void constructStrictRoutingPoolDynFwd(NfPool pool,
                                                  String vHostName,
                                                  Optional<String> failoverProfileName)
    {
        log.debug("Strict routing for dynamic forwarding pool");

        List<ProxyRoute> routesForVHost = new ArrayList<>();

        for (boolean wantTls : getDynFwdTlsEnabledOptions(seppInst))
        {
            log.debug("wantTls: {}", wantTls);

            var clusterCreator = new DfwClusterCreator(pool, wantTls, seppInst);
            clusterCreator.createCluster();

            var routeCreator = new DfwRouteCreator(failoverProfileName.orElse(null), wantTls, seppInst, clusterCreator, false);
            routeCreator.createRoutes();

            var dynFwdCluster = clusterCreator.getCluster();
            var dynFqdRoutes = routeCreator.getRoutes();
            this.clusters.add(dynFwdCluster);

            routesForVHost.addAll(dynFqdRoutes);
        }

        CommonConfigUtils.addRoutesUnderVHost(vHostName, routesForVHost, this.routesPerVHostName);
    }

    private void constructStrictRoutingPool(NfPool pool,
                                            List<String> vHostNames,
                                            Optional<String> failoverProfileName)
    {
        log.debug("Strict routing pool");

        RoundRobinEndpointCollector rrCollector;
        rrCollector = new RoundRobinEndpointCollector(seppInst, pool, false, failoverProfileName);

        var clusterCreator = new BaseClusterCreator(pool, seppInst, rrCollector);
        clusterCreator.createCluster();

        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), seppInst, clusterCreator, RoutingBehaviour.STRICT);

        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);

        for (final var vHostName : vHostNames)
        {
            CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
        }
    }

    /**
     * @param pool
     */
    private void constructPreferredPool(NfPool pool,
                                        Optional<IfNfPool> lastResortPool,
                                        List<String> vHostNames,
                                        Optional<String> failoverProfileName)
    {
        log.debug("Aggregate/Preferred Cluster");

        RoundRobinEndpointCollector rrCollector = new RoundRobinEndpointCollector(seppInst, pool, false, failoverProfileName);
        ClusterCreator clusterCreator;

        if (lastResortPool.isPresent())
        {
            RoundRobinEndpointCollector lrCollector = new RoundRobinEndpointCollector(seppInst, lastResortPool.get(), false, failoverProfileName);
            clusterCreator = new AggregateClusterCreator(pool, lastResortPool.get(), seppInst, rrCollector, lrCollector);

        }
        else
        {
            clusterCreator = new BaseClusterCreator(pool, seppInst, rrCollector);
        }
        clusterCreator.createCluster();

        var routeCreator = new BaseRouteCreator(failoverProfileName.orElse(null), seppInst, clusterCreator, RoutingBehaviour.PREFERRED);
        routeCreator.createRoutes();

        clusterCreator.appendClusters(clusters);
        // Add the route to all roaming-partners and the internal service, but not to
        // the internal-forwarding service (universal cluster). TODO: only add to those
        // VHosts where it is really needed (find out via routing cases)
        for (final var vHostName : vHostNames)
        {
            CommonConfigUtils.addRoutesUnderVHost(vHostName, routeCreator.getRoutes(), this.routesPerVHostName);
        }
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
                                                        HEADER_EXPECTED_RQ_TIMEOUT));
            }
        }
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

        rlfCluster.addDnsEndpoint(new ProxyDnsEndpoint(RLF_SERVICE_HOSTNAME, RLF_CONTAINER_PORT));

        // rlfCluster.setEjectionPolicy(new ProxyEjectionPolicy(5, 5, 5));
        // rlfCluster.setLbPolicy("RANDOM");
        var tls = new ProxyTls("internal_ca", EnvVars.get("RLF_CERT_NAME"));
        rlfCluster.setTls(tls);
        return rlfCluster;
    }

    /**
     * Handles configuration relevant to N32C.
     *
     * @return
     */
    private void n32cRouting()
    {
        var n32cConfigCreator = new N32cEgress(seppInst);
        n32cConfigCreator.createN32cClusters();
        n32cConfigCreator.createN32cRoutes();
        this.clusters.addAll(n32cConfigCreator.getClusters());
        CommonConfigUtils.addRoutesUnderVHost(ServiceConfig.INT_N32C_SERVICE,
                                              n32cConfigCreator.getRoutesForVHostName(ServiceConfig.INT_N32C_SERVICE),
                                              this.routesPerVHostName);
        CommonConfigUtils.addRoutesUnderVHost(ServiceConfig.INT_SERVICE,
                                              n32cConfigCreator.getRoutesForVHostName(ServiceConfig.INT_SERVICE),
                                              this.routesPerVHostName);
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

    public static List<Boolean> getDynFwdTlsEnabledOptions(NfInstance seppInst)
    {
        List<Boolean> options = new ArrayList<>();

        if ((seppInst.getRoutingCase() == null) || (seppInst.getRoutingCase().isEmpty()))
            return options;

        for (var rc : seppInst.getRoutingCase())
        {
            rc.getRoutingRule().forEach(rr -> rr.getRoutingAction().forEach(ra ->
            {
                if (CommonConfigUtils.isStrictRoutingRule(ra))
                {
                    seppInst.getOwnNetwork()
                            .stream()
                            .filter(on -> on.getRoutingCaseRef().equals(rc.getName()))
                            .map(on -> Utils.getByName(seppInst.getServiceAddress(), on.getServiceAddressRef()))
                            .filter(Objects::nonNull)
                            .forEach(sa ->
                            {
                                // If there is a non-TLS port, then a non-TLS Dynamic-Forwarding-cluster is
                                // required:
                                if ((sa.getPort() != null) && (!options.contains(false)))
                                    options.add(false);
                                // If there is a TLS port, then a TLS Dynamic-Forwarding-cluster is needed:
                                if ((sa.getTlsPort() != null) && (!options.contains(true)))
                                    options.add(true);

                            });

                    seppInst.getExternalNetwork()
                            .stream()
                            .filter(on -> on.getRoutingCaseRef().equals(rc.getName()))
                            .map(on -> Utils.getByName(seppInst.getServiceAddress(), on.getServiceAddressRef()))
                            .filter(Objects::nonNull)
                            .forEach(sa ->
                            {
                                // If there is a non-TLS port, then a non-TLS Dynamic-Forwarding-cluster is
                                // required:
                                if ((sa.getPort() != null) && (!options.contains(false)))
                                    options.add(false);
                                // If there is a TLS port, then a TLS Dynamic-Forwarding-cluster is needed:
                                if ((sa.getTlsPort() != null) && (!options.contains(true)))
                                    options.add(true);

                            });

                }

            }));
        }

        return options;
    }

}