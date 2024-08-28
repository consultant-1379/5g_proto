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

import java.util.Arrays;
import java.util.Set;

import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxy.endpoints.EndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubset;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubsetSelector;

public class SubsetClusterCreator extends ClusterCreator
{

    public SubsetClusterCreator(IfNfPool pool,
                                IfNfInstance configInst,
                                EndpointCollector endpointCollector)
    {
        super(pool, configInst, endpointCollector);

        this.suffix = generateSuffix();
        this.altStatPoolName = generateAltStatName();
    }

    @Override
    public void createCluster()
    {
        var subsetCluster = new ProxyCluster(pool.getName() + suffix);

        subsetCluster.setLbSubset(createLbSubsetConfig());
        subsetCluster.setStatName(altStatPoolName);

        for (var ep : endpointCollector.getEndpoints())
        {
            subsetCluster.addEndpoint(ep);
        }
        addEgressConnectionProfile(subsetCluster);
        addTlsConfiguration(subsetCluster);
        setClusterVtapSettings(subsetCluster);

        this.cluster = subsetCluster;
    }

    @Override
    public String generateAltStatName()
    {
        var name = pool.getName();
        return ConfigHelper.getClusterAltStatName(configInst.getName(), name);
    }

    @Override
    public String generateSuffix()
    {
        return "";
    }

    protected ProxyLbSubset createLbSubsetConfig()
    {
        ProxyLbSubset lbSubset;
        lbSubset = new ProxyLbSubset(Arrays.asList(new ProxyLbSubsetSelector(Arrays.asList(METADATA.HOST))));
        lbSubset.setFbPolicy(getDefaultFallbackPolicy());
        lbSubset.getSubsetSelectorKeys().forEach(lb -> lb.setSingleHostPerSubset(true));
        return lbSubset;
    }

    protected ProxyLbSubset.FallbackPolicy getDefaultFallbackPolicy()
    {
        return ProxyLbSubset.FallbackPolicy.ANY_ENDPOINT;
    }

    @Override
    public void appendClusters(Set<ProxyCluster> clusters)
    {
        clusters.add(cluster);
    }
}
