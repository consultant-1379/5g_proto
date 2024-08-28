/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: March 15, 2019
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyAggregateClusterBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyDfwClusterBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubsetConfigBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyNormalClusterBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyThresholdBuilder;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyUpstreamTransportSocketBuilder;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.cluster.v3.CircuitBreakers;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CommonLbConfig;
import io.envoyproxy.envoy.config.core.v3.RoutingPriority;
import io.envoyproxy.envoy.type.v3.Percent;

public class CdsHelper
{
    private static final Logger log = LoggerFactory.getLogger(CdsHelper.class);

    public static final String TYPE_URL = "type.googleapis.com/envoy.config.cluster.v3.Cluster";

    private CdsHelper()
    {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Given the proxy configuration, build and return the Envoy Cluster
     * configuration (all clusters). The configuration is for Envoy API v2. It is
     * returned in ProtoBuf format.
     * 
     * @param config holding the proxy configuration
     * @return clusterList The Cluster configuration packed into a list of "Any",
     *         ready to be sent to Envoy
     */
    public static List<Any> buildResources(ProxyCfg config)
    {
        var clusterList = new LinkedList<Any>();

        // Iterate over all clusters to be generated:
        for (var proxyCluster : config.getClusters())
        {
            log.debug("Creating cluster: {}", proxyCluster.getName());

            var pxThresholdBuilderDef = new ProxyThresholdBuilder(proxyCluster, RoutingPriority.DEFAULT).initBuilder();
            var pxThresholdBuilderHigh = new ProxyThresholdBuilder(proxyCluster, RoutingPriority.HIGH).initBuilder();

            var clusterBuilder = proxyCluster.initBuilder();

            // DND-25617
            // Set retry budget parameters in Circuit Breakers in order to
            // override the max_retries parameter which by default is set to
            // 3. This means that only 3 retries in parallel are possible
            // for one cluster, which breaks the whole failover policy configured
            // for a pool.
            clusterBuilder.setCircuitBreakers(CircuitBreakers.newBuilder()
                                                             .addAllThresholds(List.of(pxThresholdBuilderDef.build(), pxThresholdBuilderHigh.build()))
                                                             .build());

            var proxyTransportSocketBuilder = new ProxyUpstreamTransportSocketBuilder(proxyCluster, clusterBuilder);
            proxyTransportSocketBuilder.initBuilder();

            // Special options for Dynamic clusters:
            if (Boolean.TRUE.equals(proxyCluster.isDynamicCluster()))
            {
                var pxDfwClusterBuilder = new ProxyDfwClusterBuilder(proxyCluster, clusterBuilder);
                pxDfwClusterBuilder.initBuilder();
            }
            else if (Boolean.TRUE.equals(proxyCluster.isAggregateCluster()))
            {
                var pxAggrClusterBuilder = new ProxyAggregateClusterBuilder(proxyCluster, clusterBuilder);
                pxAggrClusterBuilder.initBuilder();
            }
            else // proxyCluster is a "normal" cluster
            {
                var pxClusterBuilder = new ProxyNormalClusterBuilder(proxyCluster, clusterBuilder);
                pxClusterBuilder.initBuilder();
            }

            // Special options when ejection/hibernation is enabled:
            var ejectionPolicy = proxyCluster.getEjectionPolicy();
            if (ejectionPolicy.isPresent())
            {
                var outlierDetect = ejectionPolicy.get().toOutlierDetection();
                clusterBuilder.setOutlierDetection(outlierDetect); // DND-16739
                clusterBuilder.setCommonLbConfig(CommonLbConfig.newBuilder().setHealthyPanicThreshold(Percent.newBuilder().setValue(0).build()));
            }

            var activeHealthCheck = proxyCluster.getActiveHealthCheck();
            if (activeHealthCheck.isPresent())
            {
                var activeHealthCheckBuilder = activeHealthCheck.get().initBuilder();
                clusterBuilder.addHealthChecks(activeHealthCheckBuilder);
                clusterBuilder.setCommonLbConfig(CommonLbConfig.newBuilder().setHealthyPanicThreshold(Percent.newBuilder().setValue(0).build()));
            }

            if (proxyCluster.getLbSubset() != null)
            {
                var pxLbSubSetCfgBuilder = new ProxyLbSubsetConfigBuilder(proxyCluster, clusterBuilder);
                pxLbSubSetCfgBuilder.initBuilder();
            }

            if (proxyCluster.getClusterMetadata() != null)
            {
                var proxyMdBuilder = new ProxyMetadataBuilder(proxyCluster.getClusterMetadata());
                clusterBuilder.setMetadata(proxyMdBuilder.initMdBuilder());
            }
            clusterList.add(Any.pack(clusterBuilder.build()));
        }
        log.debug("Returning clusters: {}", clusterList);
        return clusterList;
    }
}
