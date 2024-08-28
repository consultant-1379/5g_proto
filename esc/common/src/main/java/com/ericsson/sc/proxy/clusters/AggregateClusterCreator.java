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
package com.ericsson.sc.proxy.clusters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.endpoints.RoundRobinEndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;

public class AggregateClusterCreator extends ClusterCreator
{
    private static final Logger log = LoggerFactory.getLogger(AggregateClusterCreator.class);

    private final IfNfPool lastResortPool;
    List<ProxyCluster> subClusters = new ArrayList<>();
    private final RoundRobinEndpointCollector rrEndpointCollector;
    private final RoundRobinEndpointCollector lrEndpointCollector;

    /**
     * Constructs an AggregateClusterCreator for when a routing-action specifies a
     * lastResort pool. This clusterCreator will create two clusters, one for the
     * primary and one for the last resort pool, as well as an aggregate cluster
     * containing the two as cluster members.
     * 
     * @param pool                the primary(targetted) nf-pool
     * @param lastResortPool      the last resort pool
     * @param configInst
     * @param rrEndpointCollector The @see EndpointCollector containing the
     *                            endpoints of the primary pool
     * @param lrEndpointCollector The @see EndpointCollector containing the
     *                            endpoints of the last resort pool
     * 
     */
    public AggregateClusterCreator(IfNfPool pool,
                                   IfNfPool lastResortPool,
                                   IfNfInstance configInst,
                                   RoundRobinEndpointCollector rrEndpointCollector,
                                   RoundRobinEndpointCollector lrEndpointCollector)
    {

        super(pool, configInst);
        this.lastResortPool = lastResortPool;
        this.suffix = generateSuffix();
        this.altStatPoolName = generateAltStatName();
        this.rrEndpointCollector = rrEndpointCollector;
        this.lrEndpointCollector = lrEndpointCollector;

    }

    @Override
    public void createCluster()
    {
        ClusterCreator primaryClusterCreator = new BaseClusterCreator(pool, configInst, this.rrEndpointCollector);
        primaryClusterCreator.createCluster();

        ClusterCreator lrClusterCreator = new BaseClusterCreator(lastResortPool, configInst, this.lrEndpointCollector);
        lrClusterCreator.createCluster();

        // The order of subClusters is important, first primary then last-resort
        subClusters = List.of(primaryClusterCreator.getCluster(), lrClusterCreator.getCluster());
        if (log.isDebugEnabled())
        {

            log.debug("Clusters created {}", subClusters);
        }

        var cluster = ProxyCluster.createAggregateCluster(pool.getName() + suffix, subClusters);
        cluster.setStatName(altStatPoolName);
        addEgressConnectionProfile(cluster);

        this.cluster = cluster;
    }

    @Override
    public String generateAltStatName()
    {
        var name = pool.getName() + "_" + this.lastResortPool.getName();
        return ConfigHelper.getClusterAltStatName(configInst.getName(), name);
    }

    @Override
    public String generateSuffix()
    {
        return CommonConfigUtils.buildClusterNameSuffix(Optional.of(this.lastResortPool.getName()));
    }

    @Override
    public void appendClusters(Set<ProxyCluster> clusters)
    {
        clusters.add(cluster);
        clusters.addAll(subClusters);
    }

    public IfNfPool getLastResortPool()
    {
        return lastResortPool;
    }
}
