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
 * Created on: Apr 18, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataMap;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTcpKeepalive;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
import io.envoyproxy.envoy.config.cluster.v3.TrackClusterStats;
import io.envoyproxy.envoy.config.cluster.v3.UpstreamConnectionOptions;
import io.envoyproxy.envoy.config.core.v3.BindConfig;
import io.envoyproxy.envoy.config.core.v3.ExtraSourceAddress;
import io.envoyproxy.envoy.config.core.v3.Http2ProtocolOptions;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.core.v3.SocketOption;
import io.envoyproxy.envoy.config.core.v3.SocketOptionsOverride;
import io.envoyproxy.envoy.config.core.v3.TcpKeepalive;
import io.envoyproxy.envoy.config.core.v3.UpstreamHttpProtocolOptions;
import io.envoyproxy.envoy.extensions.upstreams.http.v3.HttpProtocolOptions;
import io.envoyproxy.envoy.extensions.upstreams.http.v3.HttpProtocolOptions.ExplicitHttpConfig;

/**
 * Simple container class. Holds all the data we need to create a Cluster
 * configuration in Envoy. Some values are hard-coded here, some come from the
 * configuration uploaded to the CM Mediator.
 * 
 */
public class ProxyCluster
{

    private String name = ""; // set in constructor
    private String statName = ""; // for PM counters
    private Double connectTimeout = 2.0; // seconds -> 2000 milliseconds

    private String dnsType = "EDS"; // "type" in the Envoy config
    private Optional<DnsLookupFamily> dnsLookupFamily = Optional.empty();

    private String lbPolicy = "ROUND_ROBIN";
    private ProxyCircuitBreaker circuitBreaker = new ProxyCircuitBreaker();
    private ProxyLbSubset lbSubset = null;
    private List<ProxyEndpoint> endpoints = new ArrayList<>();
    private List<ProxyDnsEndpoint> dnsEndpoints = new ArrayList<>();
    private Optional<ProxyEjectionPolicy> ejectionPolicy = Optional.empty();
    private Optional<ProxyActiveHealthCheck> activeHealthCheck = Optional.empty();

    private Boolean isDynamicCluster = false;
    private Optional<ProxyTls> tls = Optional.empty();
    private long idleTimeoutSeconds = 3600;
    private ProxyTcpKeepalive tcpKeepalive = new ProxyTcpKeepalive();
    private Integer maxConnectionDuration = null;
    private Optional<Integer> hpackTableSize = Optional.empty();
    private Integer maxConcurrentStreams = 2147483647;
    private final Set<IpFamily> clusterIpFamilies = new HashSet<>();

    private Collection<ProxyCluster> aggregatedClusters = new ArrayList<>();
    private Boolean isAggregateCluster = false;
    private Boolean isInternal = false; // makeInternalCLuster method sets it to true, false value is set by default

    private Optional<ProxyVtapSettings> vtapSettings = Optional.empty();
    private boolean trackClusterStats = false;
    private Integer dscpMarking = null;
    private ProxyMetadataMap metaDataMap = new ProxyMetadataMap();

    static final Map<String, Cluster.LbPolicy> LB_POLICY = Map.of( //
                                                                  "ROUND_ROBIN",
                                                                  Cluster.LbPolicy.ROUND_ROBIN, //
                                                                  "LEAST_REQUEST",
                                                                  Cluster.LbPolicy.LEAST_REQUEST, //
                                                                  "RING_HASH",
                                                                  Cluster.LbPolicy.RING_HASH, //
                                                                  "RANDOM",
                                                                  Cluster.LbPolicy.RANDOM, //
                                                                  "MAGLEV",
                                                                  Cluster.LbPolicy.MAGLEV, //
                                                                  "CLUSTER_PROVIDED",
                                                                  Cluster.LbPolicy.CLUSTER_PROVIDED);

    private static final int SOCKET_OPTIONS_IPPROTO_IP = 0;
    private static final int SOCKET_OPTIONS_IP_TOS = 1;
    private static final String SOCKET_OPTIONS_IP_TOS_DESCRIPTION = "IP_TOS";
    private static final int SOCKET_OPTIONS_IPPROTO_IPV6 = 41;
    private static final int SOCKET_OPTIONS_IPV6_TCLASS = 67;
    private static final String SOCKET_OPTIONS_IPV6_TCLASS_DESCRIPTION = "IPV6_TCLASS";

    /**
     * 
     * @param name The name of the cluster
     */

    public ProxyCluster(String name)
    {
        this.name = name;
    }

    public ProxyCluster(final String name,
                        final Set<IpFamily> clusterIpFamilies)
    {
        this.name = name;
        this.clusterIpFamilies.addAll(clusterIpFamilies);
    }

    public ProxyCluster(final String name,
                        final String stat_name,
                        final Set<IpFamily> clusterIpFamilies)
    {
        this.name = name;
        this.statName = stat_name;
        this.clusterIpFamilies.addAll(clusterIpFamilies);
    }

    public ProxyCluster(ProxyCluster anotherPxCluster)
    {
        this.name = anotherPxCluster.name;
        this.statName = anotherPxCluster.statName;
        this.connectTimeout = anotherPxCluster.getConnectTimeout();
        this.dnsType = anotherPxCluster.dnsType;
        this.dnsLookupFamily = anotherPxCluster.dnsLookupFamily;
        this.lbPolicy = anotherPxCluster.lbPolicy;
        anotherPxCluster.getEndpoints().forEach(ep -> this.addEndpoint(new ProxyEndpoint(ep)));
        anotherPxCluster.getDnsEndpoints().forEach(ep -> this.addDnsEndpoint(new ProxyDnsEndpoint(ep)));
        this.ejectionPolicy = anotherPxCluster.ejectionPolicy;
        this.activeHealthCheck = anotherPxCluster.activeHealthCheck;
        this.isDynamicCluster = anotherPxCluster.isDynamicCluster;
        this.isInternal = anotherPxCluster.isInternal;

        this.isAggregateCluster = anotherPxCluster.isAggregateCluster;
        anotherPxCluster.aggregatedClusters.forEach(cluster -> this.aggregatedClusters.add((new ProxyCluster(cluster))));

        this.lbSubset = anotherPxCluster.lbSubset;
        this.circuitBreaker = anotherPxCluster.circuitBreaker;
        this.tcpKeepalive = anotherPxCluster.tcpKeepalive;
        this.idleTimeoutSeconds = anotherPxCluster.idleTimeoutSeconds;
        this.maxConnectionDuration = anotherPxCluster.maxConnectionDuration;
        this.hpackTableSize = anotherPxCluster.hpackTableSize;
        this.maxConcurrentStreams = anotherPxCluster.maxConcurrentStreams;
        this.tls = anotherPxCluster.getTls().map(ProxyTls::new);
        this.vtapSettings = anotherPxCluster.getVtapSettings().map(ProxyVtapSettings::new);
        this.trackClusterStats = anotherPxCluster.getTrackClusterStats();
        this.dscpMarking = anotherPxCluster.getDscpMarking();
        this.clusterIpFamilies.addAll(anotherPxCluster.getClusterIpFamilies());
        this.metaDataMap = anotherPxCluster.getClusterMetadata();
    }

    public static ProxyCluster createAggregateCluster(String name,
                                                      Collection<ProxyCluster> aggregatedClusters)
    {
        var aggregateCluster = new ProxyCluster(name);
        aggregateCluster.aggregatedClusters = aggregatedClusters;
        aggregateCluster.isAggregateCluster = true;
        aggregateCluster.lbPolicy = "CLUSTER_PROVIDED";
        return aggregateCluster;
    }

    public String getName()
    {
        return this.name;
    }

    public void setStatName(String name)
    {
        this.statName = name;
    }

    public String getStatName()
    {
        return this.statName;
    }

    public void addEndpoint(ProxyEndpoint ep)
    {
        this.endpoints.add(ep);
    }

    public List<ProxyEndpoint> getEndpoints()
    {
        return this.endpoints;
    }

    public boolean hasEndpoints()
    {
        return !this.endpoints.isEmpty();
    }

    public void addDnsEndpoint(ProxyDnsEndpoint ep)
    {
        this.dnsEndpoints.add(ep);
    }

    public List<ProxyDnsEndpoint> getDnsEndpoints()
    {
        return this.dnsEndpoints;
    }

    public boolean hasDnsEndpoints()
    {
        return !this.dnsEndpoints.isEmpty();
    }

    public Optional<ProxyVtapSettings> getVtapSettings()
    {
        return vtapSettings;
    }

    public void setVtapSettings(Optional<ProxyVtapSettings> vtapSettings)
    {
        this.vtapSettings = vtapSettings;
    }

    /**
     * Return a map of endpoints in this cluster
     * <p>
     * Key is the priority, value is the list of endpoints having this priority. The
     * map is sorted by priorities in ascending order. No guarantees that the keys
     * start a zero and are consecutive (as Envoy needs it).
     * 
     * @return EndpointsByPriority Map of <prio, EndpointList>.
     */
    public Map<Integer, List<ProxyEndpoint>> getEndpointsByPriorities()
    {
        return this.endpoints.stream().collect(Collectors.groupingBy(ProxyEndpoint::getPriority, TreeMap::new, Collectors.toList()));
    }

    /**
     * Return the number of nodes on the highest priority level in this cluster.
     * 
     * @return how many endpoints are on highest prio level. Zero if there are no
     *         nodes.
     */
    public int getNumberOfEndpointsOnHighestPriority()
    {
        var poolByPriorities = this.getEndpointsByPriorities();
        var prioLevels = poolByPriorities.keySet().toArray();
        var highestPrioLevel = prioLevels.length > 0 ? prioLevels[0] : 0;
        var nodesOnHighestPriorityLevel = poolByPriorities.get(highestPrioLevel);
        return nodesOnHighestPriorityLevel != null ? nodesOnHighestPriorityLevel.size() : 0;
    }

    public Double getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(Double connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public String getDnsType()
    {
        return dnsType;
    }

    public void setDnsType(String dnsType)
    {
        this.dnsType = dnsType;
    }

    public Optional<DnsLookupFamily> getDnsLookupFamily()
    {
        return dnsLookupFamily;
    }

    public void setDnsLookupFamily(DnsLookupFamily dnsLookupFamily)
    {
        this.dnsLookupFamily = Optional.ofNullable(dnsLookupFamily);
    }

    /**
     * @return the activeHealthCheck
     */
    public Optional<ProxyActiveHealthCheck> getActiveHealthCheck()
    {
        return activeHealthCheck;
    }

    /**
     * @param activeHealthCheck the activeHealthCheck to set
     */
    public void setActiveHealthCheck(ProxyActiveHealthCheck activeHealthCheck)
    {
        this.activeHealthCheck = Optional.of(activeHealthCheck);
    }

    public void setEjectionPolicy(ProxyEjectionPolicy ejectionPolicy)
    {
        this.ejectionPolicy = Optional.of(ejectionPolicy);
    }

    public Optional<ProxyEjectionPolicy> getEjectionPolicy()
    {
        return ejectionPolicy;
    }

    public String getLbPolicy()
    {
        return lbPolicy;
    }

    public void setLbPolicy(String lbPolicy)
    {
        this.lbPolicy = lbPolicy;
    }

    public void setLbSubset(ProxyLbSubset lbSubset)
    {
        this.lbSubset = lbSubset;
    }

    public ProxyLbSubset getLbSubset()
    {
        return lbSubset;
    }

    public ProxyCircuitBreaker getCircuitBreaker()
    {

        return circuitBreaker;
    }

    public void setCircuitBreaker(ProxyCircuitBreaker circuitBreaker)
    {

        this.circuitBreaker = circuitBreaker;
    }

    /*
     * @deprecated please use makeDynamicCluster
     */
    @Deprecated
    public void makeNotifyCluster()
    {
        this.isDynamicCluster = true;
    }

    public void makeDynamicCluster()
    {
        this.isDynamicCluster = true;
    }

    public void makeInternalCluster()
    {
        this.isInternal = true;
    }

    /*
     * @deprecated please use isDynamicCluster
     */
    @Deprecated
    public Boolean isNotifyCluster()
    {
        return this.isDynamicCluster;
    }

    public Boolean isDynamicCluster()
    {
        return this.isDynamicCluster;
    }

    public Boolean isAggregateCluster()
    {
        return this.isAggregateCluster;
    }

    public Boolean isInternalCluster()
    {
        return this.isInternal;
    }

    public Collection<ProxyCluster> getAggregateClusters()
    {
        return this.aggregatedClusters;
    }

    public Optional<ProxyTls> getTls()
    {
        return this.tls;
    }

    public void setTls(ProxyTls tls)
    {
        this.tls = Optional.ofNullable(tls);
    }

    public long getIdleTimeout()
    {
        return this.idleTimeoutSeconds;
    }

    public void setIdleTimeout(long timeout)
    {
        this.idleTimeoutSeconds = timeout;
    }

    public ProxyTcpKeepalive getTcpKeepalive()
    {
        return tcpKeepalive;
    }

    public void setTcpKeepalive(ProxyTcpKeepalive tcpKeepalive)
    {
        this.tcpKeepalive = tcpKeepalive;
    }

    public Integer getMaxConnectionDuration()
    {
        return maxConnectionDuration;
    }

    public void setMaxConnectionDuration(Integer maxConnectionDuration)
    {
        this.maxConnectionDuration = maxConnectionDuration;
    }

    public Optional<Integer> getHpackTableSize()
    {
        return hpackTableSize;
    }

    public void setHpackTableSize(Optional<Integer> hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
    }

    public Integer getMaxConcurrentStreams()
    {
        return maxConcurrentStreams;
    }

    public void setMaxConcurrentStreams(Integer maxConcurrentStreams)
    {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public boolean getTrackClusterStats()
    {
        return trackClusterStats;
    }

    public void setTrackClusterStats(boolean trackClusterStats)
    {
        this.trackClusterStats = trackClusterStats;
    }

    public Integer getDscpMarking()
    {
        return dscpMarking;
    }

    public Set<IpFamily> getClusterIpFamilies()
    {
        return clusterIpFamilies;
    }

    public void setDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
    }

    public ProxyMetadataMap getClusterMetadata()
    {
        return metaDataMap;
    }

    public void setClusterMetaData(ProxyMetadataMap metaDataMap)
    {
        this.metaDataMap = metaDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(activeHealthCheck,
                            aggregatedClusters,
                            circuitBreaker,
                            clusterIpFamilies,
                            connectTimeout,
                            dnsEndpoints,
                            dnsLookupFamily,
                            dnsType,
                            dscpMarking,
                            ejectionPolicy,
                            endpoints,
                            hpackTableSize,
                            idleTimeoutSeconds,
                            isAggregateCluster,
                            isDynamicCluster,
                            isInternal,
                            lbPolicy,
                            lbSubset,
                            maxConcurrentStreams,
                            maxConnectionDuration,
                            metaDataMap,
                            name,
                            statName,
                            tcpKeepalive,
                            tls,
                            trackClusterStats,
                            vtapSettings);
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
        ProxyCluster other = (ProxyCluster) obj;
        return Objects.equals(activeHealthCheck, other.activeHealthCheck) && Objects.equals(aggregatedClusters, other.aggregatedClusters)
               && Objects.equals(circuitBreaker, other.circuitBreaker) && Objects.equals(clusterIpFamilies, other.clusterIpFamilies)
               && Objects.equals(connectTimeout, other.connectTimeout) && Objects.equals(dnsEndpoints, other.dnsEndpoints)
               && Objects.equals(dnsLookupFamily, other.dnsLookupFamily) && Objects.equals(dnsType, other.dnsType)
               && Objects.equals(dscpMarking, other.dscpMarking) && Objects.equals(ejectionPolicy, other.ejectionPolicy)
               && Objects.equals(endpoints, other.endpoints) && Objects.equals(hpackTableSize, other.hpackTableSize)
               && idleTimeoutSeconds == other.idleTimeoutSeconds && Objects.equals(isAggregateCluster, other.isAggregateCluster)
               && Objects.equals(isDynamicCluster, other.isDynamicCluster) && Objects.equals(isInternal, other.isInternal)
               && Objects.equals(lbPolicy, other.lbPolicy) && Objects.equals(lbSubset, other.lbSubset)
               && Objects.equals(maxConcurrentStreams, other.maxConcurrentStreams) && Objects.equals(maxConnectionDuration, other.maxConnectionDuration)
               && Objects.equals(metaDataMap, other.metaDataMap) && Objects.equals(name, other.name) && Objects.equals(statName, other.statName)
               && Objects.equals(tcpKeepalive, other.tcpKeepalive) && Objects.equals(tls, other.tls) && trackClusterStats == other.trackClusterStats
               && Objects.equals(vtapSettings, other.vtapSettings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCluster [name=" + name + ", statName=" + statName + ", connectTimeout=" + connectTimeout + ", dnsType=" + dnsType + ", dnsLookupFamily="
               + dnsLookupFamily + ", lbPolicy=" + lbPolicy + ", circuitBreaker=" + circuitBreaker + ", lbSubset=" + lbSubset + ", endpoints=" + endpoints
               + ", dnsEndpoints=" + dnsEndpoints + ", ejectionPolicy=" + ejectionPolicy + ", activeHealthCheck=" + activeHealthCheck + ", isDynamicCluster="
               + isDynamicCluster + ", tls=" + tls + ", idleTimeoutSeconds=" + idleTimeoutSeconds + ", tcpKeepalive=" + tcpKeepalive
               + ", maxConnectionDuration=" + maxConnectionDuration + ", hpackTableSize=" + hpackTableSize + ", maxConcurrentStreams=" + maxConcurrentStreams
               + ", clusterIpFamilies=" + clusterIpFamilies + ", aggregatedClusters=" + aggregatedClusters + ", isAggregateCluster=" + isAggregateCluster
               + ", isInternal=" + isInternal + ", vtapSettings=" + vtapSettings + ", trackClusterStats=" + trackClusterStats + ", dscpMarking=" + dscpMarking
               + ", metaDataMap=" + metaDataMap + "]";
    }

    public Builder initBuilder()
    {
        // Envoy needs the timeout in whole seconds and nanoseconds:
        int connectTimeoutSeconds = (int) Math.floor(this.getConnectTimeout());
        int connectTimeoutNanos = (int) (1E9 * (this.getConnectTimeout() - connectTimeoutSeconds));

        // Timeout value for TCP_USER_TIMEOUT, used to time-box retransmissions. This
        // works close cooperation with TCP_KEEPALIVE, used to detect dead idle links.
        // Unit is milliseconds (see "man tcp"), whereas the TCP KEEPALIVE Envoy
        // configuration option uses seconds.
        // Both together are used to quickly detect that a peer has gone down instead
        // of waiting 15 minutes.
        long userTimeout = 1000l * (this.getTcpKeepalive().getTime() + this.getTcpKeepalive().getProbes() * this.getTcpKeepalive().getInterval());

        // for dual stack, the main address will be ipv6.
        var ipVersion = getClusterIpVersion();
        String ownAnyAddress = ipVersion.equals(IP_VERSION.IPV4) ? "0.0.0.0" : "::";

        var upstreamBindConfigBuilder = BindConfig.newBuilder() //
                                                  .setSourceAddress(SocketAddress.newBuilder() //
                                                                                 .setAddress(ownAnyAddress)
                                                                                 .setPortValue(0)
                                                                                 .build())
                                                  .addSocketOptions(SocketOption.newBuilder() //
                                                                                .setDescription("TCP_USER_TIMEOUT")
                                                                                .setLevel(6) // IPPROT_TCP,
                                                                                             // see
                                                                                             // /usr/include/linux/in.h
                                                                                .setName(18) // TCP_USER_TIMEOUT,
                                                                                             // see
                                                                                             // /usr/include/linux/tcp.h
                                                                                .setIntValue(userTimeout)
                                                                                .build());

        if (this.dscpMarking != null)
        {
            // IPV4 values
            // IPPROTO_IP = 0
            // IP_TOS = 1
            var level = SOCKET_OPTIONS_IPPROTO_IP;
            var name = SOCKET_OPTIONS_IP_TOS;
            var description = SOCKET_OPTIONS_IP_TOS_DESCRIPTION;

            // for ipv6 and dualstack, the main address is the ipv6, so the same happens for
            // the socket options
            if (!ipVersion.equals(IP_VERSION.IPV4))
            {
                // IPPROTO_IPV6 = 41
                // IPV6_TCLASS = 67
                level = SOCKET_OPTIONS_IPPROTO_IPV6;
                name = SOCKET_OPTIONS_IPV6_TCLASS;
                description = SOCKET_OPTIONS_IPV6_TCLASS_DESCRIPTION;
            }

            upstreamBindConfigBuilder.addSocketOptions(SocketOption.newBuilder()
                                                                   .setDescription(description)
                                                                   .setLevel(level)
                                                                   .setName(name)
                                                                   .setIntValue(this.dscpMarking * 4L) // Multiply value by 4 due to bit shift that happens in
                                                                                                       // DSCP marking parsing
                                                                   .build());
        }

        // set the extraSourceAddress for Dualstack
        if (ipVersion.equals(IP_VERSION.IPV4_IPV6))
        {
            var socketOptions = SocketOptionsOverride.newBuilder()
                                                     .addSocketOptions(SocketOption.newBuilder() //
                                                                                   .setDescription("TCP_USER_TIMEOUT")
                                                                                   .setLevel(6)
                                                                                   .setName(18)
                                                                                   .setIntValue(userTimeout)
                                                                                   .build());
            if (this.dscpMarking != null)
            {
                socketOptions.addSocketOptions(SocketOption.newBuilder()
                                                           .setDescription(SOCKET_OPTIONS_IP_TOS_DESCRIPTION)
                                                           .setLevel(SOCKET_OPTIONS_IPPROTO_IP)
                                                           .setName(SOCKET_OPTIONS_IP_TOS)
                                                           .setIntValue(this.dscpMarking * 4L)
                                                           .build());
            }

            upstreamBindConfigBuilder.addExtraSourceAddresses(ExtraSourceAddress.newBuilder()
                                                                                .setAddress(SocketAddress.newBuilder() //
                                                                                                         .setAddress("0.0.0.0")
                                                                                                         .setPortValue(0)
                                                                                                         .build())
                                                                                .setSocketOptions(socketOptions.build())
                                                                                .build());

        }

        var clusterBuilder = Cluster.newBuilder() //
                                    .setName(this.getName()) //
                                    .setAltStatName(this.getStatName()) //
                                    .setConnectTimeout(Duration.newBuilder() //
                                                               .setSeconds(connectTimeoutSeconds)
                                                               .setNanos(connectTimeoutNanos)
                                                               .build()) //
                                    .putTypedExtensionProtocolOptions("envoy.extensions.upstreams.http.v3.HttpProtocolOptions", getHttpProtocolOptions())
                                    .setLbPolicy(LB_POLICY.get(this.getLbPolicy())) //
                                    .setUpstreamConnectionOptions(UpstreamConnectionOptions.newBuilder() //
                                                                                           .setTcpKeepalive(TcpKeepalive.newBuilder() //
                                                                                                                        .setKeepaliveProbes(UInt32Value.of(this.getTcpKeepalive()
                                                                                                                                                               .getProbes()))
                                                                                                                        .setKeepaliveTime(UInt32Value.of(this.getTcpKeepalive()
                                                                                                                                                             .getTime()))
                                                                                                                        .setKeepaliveInterval(UInt32Value.of(this.getTcpKeepalive()
                                                                                                                                                                 .getInterval()))
                                                                                                                        .build())
                                                                                           .build()) //
                                    .setUpstreamBindConfig(upstreamBindConfigBuilder.build());
        clusterBuilder.setTrackClusterStats(TrackClusterStats.newBuilder().setRequestResponseSizes(this.trackClusterStats));

        return clusterBuilder;

    }

    private IP_VERSION getClusterIpVersion()
    {
        boolean foundIpv4 = false;
        boolean foundIpv6 = false;

        if (this.clusterIpFamilies.size() == 2) // if dns resolution is DS, then the whole cluster is DS.
        {
            return IP_VERSION.IPV4_IPV6;
        }

        if (this.clusterIpFamilies.contains(IpFamily.IPV4))
        {
            foundIpv4 = true;
        }
        else if (this.clusterIpFamilies.contains(IpFamily.IPV6))
        {
            foundIpv6 = true;
        }

        for (ProxyEndpoint ep : this.endpoints)
        {
            if (isAddressIpv6(ep.getIpAddress().orElse("")) || isAddressIpv6(ep.getAddress()))
            {
                foundIpv6 = true;
            }
            else if (isAddressIpv4(ep.getIpAddress().orElse("")) || isAddressIpv4(ep.getAddress()))
            {
                foundIpv4 = true;
            }

            if (foundIpv4 && foundIpv6) // if both ip versions have been found, no need to search further
            {
                return IP_VERSION.IPV4_IPV6;
            }
        }

        // if nothing is found, use the runtime environment
        if (!foundIpv4 && !foundIpv6)
        {
            if (this.dnsLookupFamily.isPresent())
            {
                if (this.dnsLookupFamily.get().equals(DnsLookupFamily.V4_ONLY))
                {
                    return IP_VERSION.IPV4;
                }
                else if (this.dnsLookupFamily.get().equals(DnsLookupFamily.V6_ONLY))
                {
                    return IP_VERSION.IPV6;
                }
                else if (this.dnsLookupFamily.get().equals(DnsLookupFamily.ALL))
                {
                    return IP_VERSION.IPV4_IPV6;
                }
            }

            return RuntimeEnvironment.getDeployedIpVersion();
        }
        return foundIpv4 ? IP_VERSION.IPV4 : IP_VERSION.IPV6;
    }

    private boolean isAddressIpv6(String ipAddress)
    {
        if (ipAddress.isEmpty())
        {
            return false;
        }
        try
        {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return (inetAddress instanceof Inet6Address);
        }
        catch (UnknownHostException ex)
        {
            return false;
        }
    }

    private boolean isAddressIpv4(String ipAddress)
    {
        if (ipAddress.isEmpty())
        {
            return false;
        }

        try
        {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return (inetAddress instanceof Inet4Address);
        }
        catch (UnknownHostException ex)
        {
            return false;
        }
    }

    private Any getHttpProtocolOptions()
    {
        // This MUST be set to true to allow h2 or h2c connection
        var http2Connection = Http2ProtocolOptions.newBuilder().setAllowConnect(true);
        this.getHpackTableSize().ifPresent(size -> http2Connection.setHpackTableSize(UInt32Value.of(size)));
        http2Connection.setMaxConcurrentStreams(UInt32Value.of(this.getMaxConcurrentStreams()));
        var commonHttpOptions = io.envoyproxy.envoy.config.core.v3.HttpProtocolOptions.newBuilder();

        var builder = HttpProtocolOptions.newBuilder();

        var clusterTls = this.getTls();

        if (Boolean.TRUE.equals(this.isDynamicCluster()) && clusterTls.isPresent())
        {
            builder.setUpstreamHttpProtocolOptions(UpstreamHttpProtocolOptions.newBuilder().setAutoSni(true).setAutoSanValidation(true).build());
        }
        else if (clusterTls.isPresent())
        {
            builder.setUpstreamHttpProtocolOptions(UpstreamHttpProtocolOptions.newBuilder().setEnableSniFromHost(true).setAutoSanValidation(false).build());
        }

        if (this.getMaxConnectionDuration() != null && (!this.getMaxConnectionDuration().equals(0)))
        {
            commonHttpOptions.setMaxConnectionDuration(Duration.newBuilder().setSeconds(this.getMaxConnectionDuration().longValue()).clearNanos().build());
        }

        return Any.pack(builder.setCommonHttpProtocolOptions(commonHttpOptions.setIdleTimeout(Duration.newBuilder()
                                                                                                      .setSeconds(this.getIdleTimeout())
                                                                                                      .clearNanos()
                                                                                                      .build())
                                                                              .build())
                               .setExplicitHttpConfig(ExplicitHttpConfig.newBuilder().setHttp2ProtocolOptions(http2Connection.build()).build())
                               .build());
    }

}
