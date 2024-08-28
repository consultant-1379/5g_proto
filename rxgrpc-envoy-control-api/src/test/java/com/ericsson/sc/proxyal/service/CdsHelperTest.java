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
 * Created on: Sep 15, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubset;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubsetSelector;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 
 */
class CdsHelperTest
{

    private static final Logger log = LoggerFactory.getLogger(RdsHelperTest.class);

    @Test
    void testAggregateClusters()
    {
        var aggregatedClusters = new ArrayList<ProxyCluster>();
        aggregatedClusters.add(new ProxyCluster("Cl1"));
        aggregatedClusters.add(new ProxyCluster("Cl2"));
        aggregatedClusters.add(new ProxyCluster("Cl3"));

        var aggregateCluster = ProxyCluster.createAggregateCluster("AggregateCluster", aggregatedClusters);

        var pxConfig = new ProxyCfg("testNf");
        pxConfig.addCluster(aggregateCluster);
        aggregatedClusters.forEach(cl -> pxConfig.addCluster(cl));

        var clusterRessources = CdsHelper.buildResources(pxConfig);

        assertEquals(4, clusterRessources.size(), "wrong number of  cluster ressouces, 3 clusters + 1 agg. cluster expected");

        logClusterRessources(clusterRessources);

    }

    @Test
    void testClusterWithLbSubset()
    {

        var testCluster = new ProxyCluster("UT test cluster");
        List<ProxyLbSubsetSelector> subsetKeys = new ArrayList<>();

        subsetKeys.add(new ProxyLbSubsetSelector(Arrays.asList("host", "other_key")));
        subsetKeys.add(new ProxyLbSubsetSelector(Arrays.asList("host1", "other_key1")));

        var lbSubset = new ProxyLbSubset(subsetKeys);

        testCluster.setLbSubset(lbSubset);

        var pxConfig = new ProxyCfg("testNf");
        pxConfig.addCluster(testCluster);

        var clusterRessources = CdsHelper.buildResources(pxConfig);

        assertEquals(1, clusterRessources.size(), "wrong number of  cluster ressouces, 1 cluster expected");

        logClusterRessources(clusterRessources);

    }

    private void logClusterRessources(List<Any> clusterRessources)
    {
        clusterRessources.forEach(cl ->
        {
            try
            {
                log.debug(cl.unpack(io.envoyproxy.envoy.config.cluster.v3.Cluster.class).toString());
            }
            catch (InvalidProtocolBufferException e)
            {
                e.printStackTrace();
            }
        });
    }

}
