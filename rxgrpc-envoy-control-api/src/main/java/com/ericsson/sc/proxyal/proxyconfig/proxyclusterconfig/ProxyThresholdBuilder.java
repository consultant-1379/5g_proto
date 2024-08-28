package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Objects;

import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.cluster.v3.CircuitBreakers.Thresholds;
import io.envoyproxy.envoy.config.cluster.v3.CircuitBreakers.Thresholds.Builder;
import io.envoyproxy.envoy.config.cluster.v3.CircuitBreakers.Thresholds.RetryBudget;
import io.envoyproxy.envoy.config.core.v3.RoutingPriority;

import io.envoyproxy.envoy.type.v3.Percent;

public class ProxyThresholdBuilder
{

    ProxyCluster proxyCluster;
    RoutingPriority routingPriority;

    public ProxyThresholdBuilder(ProxyCluster proxyCluster,
                                 RoutingPriority routingPriority)
    {
        this.proxyCluster = proxyCluster;
        this.routingPriority = routingPriority;
    }

    public ProxyThresholdBuilder(ProxyThresholdBuilder that)
    {
        this(that.getProxyCluster(), that.getRoutingPriority());
    }

    public ProxyCluster getProxyCluster()
    {
        return proxyCluster;
    }

    public void setProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
    }

    public ProxyThresholdBuilder withProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
        return this;
    }

    public RoutingPriority getRoutingPriority()
    {
        return routingPriority;
    }

    public void setRoutingPriority(RoutingPriority routingPriority)
    {
        this.routingPriority = routingPriority;
    }

    public ProxyThresholdBuilder withRoutingPriority(RoutingPriority routingPriority)
    {
        this.routingPriority = routingPriority;
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
        return "ProxyThresholdBuilder [proxyCluster=" + proxyCluster + ", routingPriority=" + routingPriority + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(proxyCluster, routingPriority);
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
        ProxyThresholdBuilder other = (ProxyThresholdBuilder) obj;
        return Objects.equals(proxyCluster, other.proxyCluster) && routingPriority == other.routingPriority;
    }

    public Builder initBuilder()
    {
        return Thresholds.newBuilder()
                         .setPriority(routingPriority)
                         .setRetryBudget(RetryBudget.newBuilder()
                                                    .setBudgetPercent(Percent.newBuilder()
                                                                             .setValue(proxyCluster.getCircuitBreaker().getBudgetPercent())
                                                                             .build())
                                                    .setMinRetryConcurrency(UInt32Value.of(proxyCluster.getCircuitBreaker().getMinRetriesConcurrency()))
                                                    .build())
                         .setMaxPendingRequests(UInt32Value.of(proxyCluster.getCircuitBreaker().getMaxPendingRequests()))
                         .setMaxConnections(UInt32Value.of(proxyCluster.getCircuitBreaker().getMaxConnections()))
                         .setMaxRequests(UInt32Value.of(proxyCluster.getCircuitBreaker().getMaxRequests()));
    }

}
