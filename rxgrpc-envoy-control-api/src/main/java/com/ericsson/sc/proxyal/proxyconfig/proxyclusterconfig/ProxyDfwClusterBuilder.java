package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Map;
import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.google.protobuf.Duration;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CustomClusterType;
import io.envoyproxy.envoy.extensions.clusters.dynamic_forward_proxy.v3.ClusterConfig;
import io.envoyproxy.envoy.extensions.common.dynamic_forward_proxy.v3.DnsCacheConfig;

public class ProxyDfwClusterBuilder
{

    private ProxyCluster proxyCluster;
    private Builder clusterBuilder;

    static final Map<String, Cluster.DnsLookupFamily> DNS_LOOKUP_FAMILY = Map.of( //
                                                                                 "AUTO",
                                                                                 Cluster.DnsLookupFamily.AUTO, //
                                                                                 "V4_ONLY",
                                                                                 Cluster.DnsLookupFamily.V4_ONLY, //
                                                                                 "V6_ONLY",
                                                                                 Cluster.DnsLookupFamily.V6_ONLY //
    );

    public ProxyDfwClusterBuilder(ProxyCluster proxyCluster,
                                  Builder clusterBuilder)
    {
        this.proxyCluster = proxyCluster;
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyDfwClusterBuilder(ProxyDfwClusterBuilder that)
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

    public ProxyDfwClusterBuilder withProxyCluster(ProxyCluster proxyCluster)
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

    public ProxyDfwClusterBuilder withClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyDfwClusterBuilder [proxyCluster=" + proxyCluster + ", clusterBuilder=" + clusterBuilder + "]";
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
        ProxyDfwClusterBuilder other = (ProxyDfwClusterBuilder) obj;
        return Objects.equals(clusterBuilder, other.clusterBuilder) && Objects.equals(proxyCluster, other.proxyCluster);
    }

    public Builder initBuilder()
    {
        var dfwClusterConfig = ClusterConfig.newBuilder()
                                            .setAllowInsecureClusterOptions(true)// TO be corrected via bug DND-28958
                                            .setDnsCacheConfig(DnsCacheConfig.newBuilder()
                                                                             .setName("dynamic_forward_proxy_cache_config")
                                                                             .setDnsLookupFamily(this.proxyCluster.getDnsLookupFamily().get())
                                                                             // DND-18723 FIXME: TODO: As a workaround, we set the TTL of the
                                                                             // hosts in the dynamic-forward-cluster to one year to prevent
                                                                             // coredumps in Envoy. Remove this once Envoy has a fix for this
                                                                             // problem:
                                                                             .setHostTtl(Duration.newBuilder()
                                                                                                 .setSeconds(ProxyCfg.DYN_CLUSTER_TTL_SECONDS)
                                                                                                 .build()) //
                                                                             .build())
                                            .build();
        return clusterBuilder.setClusterType(CustomClusterType.newBuilder() //
                                                              .setName("envoy.clusters.dynamic_forward_proxy") //
                                                              .setTypedConfig(Any.pack(dfwClusterConfig)) //
                                                              .build());

    }
}
