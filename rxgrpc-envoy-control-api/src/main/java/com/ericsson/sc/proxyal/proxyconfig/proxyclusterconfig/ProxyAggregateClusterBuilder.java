package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CustomClusterType;

public class ProxyAggregateClusterBuilder
{

    private ProxyCluster proxyCluster;
    private Builder clusterBuilder;

    public ProxyAggregateClusterBuilder(ProxyCluster proxyCluster,
                                        Builder clusterBuilder)
    {
        this.proxyCluster = proxyCluster;
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyAggregateClusterBuilder(ProxyAggregateClusterBuilder that)
    {
        this(that.getProxyCluster(), that.getClusterBuilder());
    }

    public ProxyCluster getProxyCluster()
    {
        return proxyCluster;
    }

    public void setProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
    }

    public ProxyAggregateClusterBuilder withProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
        return this;
    }

    public Builder getClusterBuilder()
    {
        return clusterBuilder;
    }

    public void setClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyAggregateClusterBuilder withClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
        return null;
    }

    public Builder initBuilder()
    {

        var clusterTypeConfigBuilder = io.envoyproxy.envoy.extensions.clusters.aggregate.v3.ClusterConfig.newBuilder();
        proxyCluster.getAggregateClusters().forEach(aggPxCluster -> clusterTypeConfigBuilder.addClusters(aggPxCluster.getName()));

        return clusterBuilder.setClusterType(CustomClusterType.newBuilder()
                                                              .setName("envoy.clusters.aggregate")
                                                              .setTypedConfig(Any.pack(clusterTypeConfigBuilder.build())) //
                                                              .build());
    }
}
