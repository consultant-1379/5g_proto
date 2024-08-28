package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Map;
import java.util.Objects;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.EdsClusterConfig;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.envoyproxy.envoy.config.core.v3.ConfigSource;
import io.envoyproxy.envoy.config.core.v3.AggregatedConfigSource;

public class ProxyNormalClusterBuilder
{

    private Builder clusterBuilder;
    private ProxyCluster proxyCluster;

    static final Map<String, Cluster.DiscoveryType> DISCOVERY_TYPE = Map.of( //
                                                                            "STATIC",
                                                                            Cluster.DiscoveryType.STATIC, //
                                                                            "STRICT_DNS",
                                                                            Cluster.DiscoveryType.STRICT_DNS, //
                                                                            "LOGICAL_DNS",
                                                                            Cluster.DiscoveryType.LOGICAL_DNS, //
                                                                            "EDS",
                                                                            Cluster.DiscoveryType.EDS, //
                                                                            "ORIGINAL_DST",
                                                                            Cluster.DiscoveryType.ORIGINAL_DST //
    );
    static final Map<String, Cluster.DnsLookupFamily> DNS_LOOKUP_FAMILY = Map.of( //
                                                                                 "AUTO",
                                                                                 Cluster.DnsLookupFamily.AUTO, //
                                                                                 "V4_ONLY",
                                                                                 Cluster.DnsLookupFamily.V4_ONLY, //
                                                                                 "V6_ONLY",
                                                                                 Cluster.DnsLookupFamily.V6_ONLY, //
                                                                                 "ALL",
                                                                                 Cluster.DnsLookupFamily.ALL);

    public ProxyNormalClusterBuilder(ProxyCluster proxyCluster,
                                     Builder clusterBuilder)
    {
        this.proxyCluster = proxyCluster;
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyNormalClusterBuilder(ProxyNormalClusterBuilder that)
    {
        this(that.getProxyCluster(), that.getClusterBuilder());
    }

    public Builder getClusterBuilder()
    {
        return clusterBuilder;
    }

    public void setClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyNormalClusterBuilder withClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
        return this;
    }

    public ProxyCluster getProxyCluster()
    {
        return proxyCluster;
    }

    public void setProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
    }

    public ProxyNormalClusterBuilder withProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
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
        return "ProxyNormalClusterBuilder [clusterBuilder=" + clusterBuilder + ", proxyCluster=" + proxyCluster + "]";
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
        ProxyNormalClusterBuilder other = (ProxyNormalClusterBuilder) obj;
        return Objects.equals(clusterBuilder, other.clusterBuilder) && Objects.equals(proxyCluster, other.proxyCluster);
    }

    public Builder initBuilder()
    {
        if (proxyCluster.getDnsLookupFamily().isPresent())
        {
            clusterBuilder.setDnsLookupFamily(proxyCluster.getDnsLookupFamily().get()); //
        }

        clusterBuilder.setType(DISCOVERY_TYPE.get(proxyCluster.getDnsType()));

        if (proxyCluster.hasDnsEndpoints())
        {
            var localityLbEndpointsBuilder = LocalityLbEndpoints.newBuilder();
            for (var dnsEp : proxyCluster.getDnsEndpoints())
            {
                localityLbEndpointsBuilder.addLbEndpoints(dnsEp.initBuilder().build());
            }
            return clusterBuilder.setLoadAssignment(ClusterLoadAssignment.newBuilder()
                                                                         .setClusterName(proxyCluster.getName())
                                                                         .addEndpoints(localityLbEndpointsBuilder.build())
                                                                         .build());
        }
        else
        {
            return clusterBuilder.setEdsClusterConfig(EdsClusterConfig.newBuilder() //
                                                                      .setEdsConfig(ConfigSource.newBuilder()
                                                                                                .setResourceApiVersion(io.envoyproxy.envoy.config.core.v3.ApiVersion.V3) //
                                                                                                .setAds(AggregatedConfigSource.newBuilder().build())
                                                                                                .build()) //
                                                                      .setServiceName(proxyCluster.getName())
                                                                      .build());
        }
    }

}
