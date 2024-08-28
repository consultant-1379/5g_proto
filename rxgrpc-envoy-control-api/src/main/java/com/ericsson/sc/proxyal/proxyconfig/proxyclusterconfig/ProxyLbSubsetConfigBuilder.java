package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubset.FallbackPolicy;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.LbSubsetConfig;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.LbSubsetConfig.LbSubsetFallbackPolicy;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.LbSubsetConfig.LbSubsetSelector;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.LbSubsetConfig.LbSubsetSelector.LbSubsetSelectorFallbackPolicy;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;

public class ProxyLbSubsetConfigBuilder
{

    private ProxyCluster proxyCluster;
    private Builder clusterBuilder;

    public ProxyLbSubsetConfigBuilder(ProxyCluster proxyCluster,
                                      Builder clusterBuilder)
    {
        this.proxyCluster = proxyCluster;
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyLbSubsetConfigBuilder(ProxyLbSubsetConfigBuilder that)
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

    public ProxyLbSubsetConfigBuilder withProxyCluster(ProxyCluster proxyCluster)
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

    public ProxyLbSubsetConfigBuilder withClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyLbSubsetConfigBuilder [proxyCluster=" + proxyCluster + ", clusterBuilder=" + clusterBuilder + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(clusterBuilder, proxyCluster);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyLbSubsetConfigBuilder other = (ProxyLbSubsetConfigBuilder) obj;
        return Objects.equals(clusterBuilder, other.clusterBuilder) && Objects.equals(proxyCluster, other.proxyCluster);
    }

    public Builder initBuilder()
    {
        var lbSubsetCfgBuilder = LbSubsetConfig.newBuilder();

        proxyCluster.getLbSubset().getSubsetSelectorKeys().forEach(subset ->
        {
            var lbSubsetSelectorBuilder = LbSubsetSelector.newBuilder();
            lbSubsetSelectorBuilder.addAllKeys(subset.getKeys());
            lbSubsetSelectorBuilder.setFallbackPolicy(LbSubsetSelectorFallbackPolicy.valueOf(subset.getFallbackPolicy().toString()));
            lbSubsetSelectorBuilder.setSingleHostPerSubset(subset.getSingleHostPerSubset());
            if (subset.getFallbackPolicy() == FallbackPolicy.KEYS_SUBSET)
            {
                lbSubsetSelectorBuilder.addAllFallbackKeysSubset(subset.getFallbackKeys());
            }
            lbSubsetCfgBuilder.addSubsetSelectors(lbSubsetSelectorBuilder);
            lbSubsetSelectorBuilder.build();
        });

        lbSubsetCfgBuilder.setFallbackPolicy(LbSubsetFallbackPolicy.valueOf(proxyCluster.getLbSubset().getFbPolicy().toString()));
        if (proxyCluster.getLbSubset().getFbPolicy() == FallbackPolicy.DEFAULT_SUBSET)
        {
            proxyCluster.getLbSubset()
                        .getDefaultSubset()
                        .forEach((k,
                                  v) -> lbSubsetCfgBuilder.setDefaultSubset(Struct.newBuilder().putFields(k, Value.newBuilder().setStringValue(v).build())));
        }
        lbSubsetCfgBuilder.setListAsAny(true);
        return clusterBuilder.setLbSubsetConfig(lbSubsetCfgBuilder.build());
    }

}
