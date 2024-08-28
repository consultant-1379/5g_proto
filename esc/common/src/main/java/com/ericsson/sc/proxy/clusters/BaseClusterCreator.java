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

import java.util.Optional;
import java.util.Set;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.endpoints.EndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;

public class BaseClusterCreator extends ClusterCreator
{

    /**
     * Constructs a baseClusterCreator for all the routing scenarios apart from
     * direct forwarding
     * 
     * @param pool              the nf-pool for which the ProxyCluster will be made
     * @param configInst
     * @param endpointCollector The @see EndpointCollector containing the endpoints
     *                          for this cluster
     */
    public BaseClusterCreator(IfNfPool pool,
                              IfNfInstance configInst,
                              EndpointCollector endpointCollector)
    {
        super(pool, configInst, endpointCollector);
        // Re-calculate suffix and alt stat since they depend on clusterType.
        this.suffix = generateSuffix();
        this.altStatPoolName = generateAltStatName();
    }

    @Override
    public void createCluster()
    {
        ProxyCluster rrCluster = new ProxyCluster(pool.getName() + suffix, super.getClusterIpFamilies());
        endpointCollector.createEndpoints();
        rrCluster.setStatName(altStatPoolName);

        var clusterMd = endpointCollector.getClusterMetadata();
        if (clusterMd != null)
            rrCluster.setClusterMetaData(clusterMd);
        endpointCollector.getEndpoints().forEach(rrCluster::addEndpoint);
        addTlsConfiguration(rrCluster);
        addEgressConnectionProfile(rrCluster);
        CommonConfigUtils.setTempBlockingForCluster(pool, rrCluster);
        CommonConfigUtils.setActiveHealthCheckForCluster(configInst, pool, rrCluster);
        setClusterVtapSettings(rrCluster);
        this.cluster = rrCluster;
    }

    @Override
    public String generateAltStatName()
    {
        return ConfigHelper.getClusterAltStatName(configInst.getName(), pool.getName());

    }

    @Override
    public String generateSuffix()
    {
        return CommonConfigUtils.buildClusterNameSuffix(Optional.empty());

    }

    @Override
    public void appendClusters(Set<ProxyCluster> clusters)
    {
        clusters.add(cluster);
    }

}
