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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.MetadataListValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataStringValue;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;

/**
 * 
 */
public class EdsHelper
{
    private static final Logger log = LoggerFactory.getLogger(EdsHelper.class);

    public static final String TYPE_URL = "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment";

    private EdsHelper()
    {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Given the proxy configuration endpoints for the Envoy Cluster configuration.
     * The configuration is for Envoy API v3. It is returned in ProtoBuf format.
     * 
     * @param config holding the proxy configuration
     * @return endpoints a protobuf object with the route configuration for the
     *         given listener
     */
    public static List<Any> buildEndpoints(ProxyCfg config)
    {
        log.info("Building Endpoints for all clusters");

        List<Any> buildClusters = new LinkedList<>();

        for (var clusterName : config.getClusterNames())
        {
            var proxyClusterOpt = config.getClusterWithName(clusterName);
            if (proxyClusterOpt.isEmpty())
            {
                log.error("Cluster {} not found", clusterName);
                continue; // try next cluster
            }

            var proxyCluster = proxyClusterOpt.get();

            var loadAssignmentBuilder = ClusterLoadAssignment.newBuilder() //
                                                             .setClusterName(clusterName);
            // Inside the cluster, we may have groups with endpoints of the same
            // priority, for example: main and fallback. Group and generate
            // them by priority:
            var endpointsByPriorities = proxyCluster.getEndpointsByPriorities();
            endpointsByPriorities.forEach((prio,
                                           lbEndpoints) ->
            {
                var endpointsBuilder = LocalityLbEndpoints.newBuilder() //
                                                          .setPriority(prio);

                for (var lbEndpoint : lbEndpoints)
                {
                    if (lbEndpoint.getIpAddress().isEmpty())
                    {
                        log.warn("endpoint {} could not be resolved.", lbEndpoint.getAddress());
                    }
                    else
                    {
                        var mdMap = lbEndpoint.getEndpointMetadata().getMetadataMap().get(MetaDataType.LB);
                        if (mdMap != null && mdMap.containsKey("host"))
                        {
                            @SuppressWarnings("unchecked")
                            var initialHostMetadata = (MetadataListValue<MetadataStringValue>) mdMap.get("host");

                            var ip = lbEndpoint.getIpAddress().get();

                            if (initialHostMetadata != null)
                            {
                                var hostMetadata = new ArrayList<MetadataStringValue>(initialHostMetadata.getListValue());

                                Optional<Integer> epPort = Optional.ofNullable(lbEndpoint.getPort());
                                int port = !epPort.isEmpty() ? epPort.get() : lbEndpoint.getMatchTLS() ? 443 : 80;
                                String formedIP = formatIpv4Ipv6Address(ip) + ":" + port;

                                var hostExists = hostMetadata.stream().map(h -> h.getVal()).filter(h -> h.equals(formedIP) || h.equals("no_match")).findAny();

                                if (hostExists.isEmpty())
                                {
                                    hostMetadata.add(new MetadataStringValue(formedIP));
                                    lbEndpoint.getEndpointMetadata().addMetadata(MetaDataType.LB, "host", new MetadataListValue<>(hostMetadata));
                                }
                            }
                        }

                        var proxyMdBuilder = new ProxyMetadataBuilder(lbEndpoint.getEndpointMetadata()).initMdBuilder();

                        var lbEpBuilder = lbEndpoint.initBuilder();
                        lbEpBuilder.setMetadata(proxyMdBuilder.build());
                        endpointsBuilder.addLbEndpoints(lbEpBuilder.build());
                    }
                }
                loadAssignmentBuilder.addEndpoints(endpointsBuilder.build());
            });
            var loadAssignment = loadAssignmentBuilder.build();
            log.debug("Adding configuration for cluster {}:\n{}", clusterName, loadAssignment);
            log.info("Adding configuration for cluster {}", clusterName);

            buildClusters.add(Any.pack(loadAssignment));
        }

        return buildClusters;
    }

    public static String formatIpv4Ipv6Address(String addr)
    {
        if (addr.contains("["))
        {
            return addr;
        }
        if (addr.contains(":"))
        {
            return "[" + addr + "]";
        }
        return addr;
    }

}
