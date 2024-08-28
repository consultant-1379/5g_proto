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
package com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ericsson.sc.proxyal.proxyconfig.ProxyLocalReplyContext;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTcpKeepalive;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.IfHttpFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualHost;
import com.ericsson.sc.proxyal.service.FilterFactory;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.core.v3.SocketOption;
import io.envoyproxy.envoy.config.core.v3.SocketOptionsOverride;
import io.envoyproxy.envoy.config.listener.v3.AdditionalAddress;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.listener.v3.Listener.Builder;
import io.envoyproxy.envoy.config.listener.v3.Listener.ConnectionBalanceConfig;
import io.envoyproxy.envoy.config.listener.v3.Listener.ConnectionBalanceConfig.ExactBalance;

/**
 * Simple container class. Holds all the data we need to create a Listener
 * configuration in Envoy. Some values are hard-coded here, some come from the
 * configuration uploaded to the CM Mediator.
 */
public class ProxyListener
{
    // name of the ProxyListener
    private final String name;
    // the final name with which this ProxyListener is going to be mapped in Envoy
    private final String envoyListenerName;
    private Optional<ProxyTls> tls = Optional.empty();
    private List<ProxyVirtualHost> vHosts = new ArrayList<>();
    private Map<String, Map<String, String>> contextData = new HashMap<>();
    private Map<String, Object> routingContext = new HashMap<>();
    private List<ProxyLocalReplyContext> localReplyMappings = new ArrayList<>();
    private String scpInstanceName;
    private String scpServiceAddrName;
    private List<String> scpServiceAddress;
    // DND-23357
    private boolean sprRouteConfigured = false;
    private boolean accessLogEnabled = false;
    private boolean tcpKeepaliveEnabled = false;
    private ProxyTcpKeepalive tcpKeepalive = new ProxyTcpKeepalive();
    private long idleTimeoutSeconds = 3600;
    private Integer maxConnectionDuration = null;
    private Integer maxConcurrentStreams = null;
    private Optional<Integer> hpackTableSize = Optional.empty();
    private Optional<ProxyVtapSettings> vtapSettings = Optional.empty();
    private Integer dscpMarking = null;

    private long maxStreamDuration = 0;
    // specify dnslookup family: v4 for cc slc dyn notifies
    // v6 for rcc notify subnets
    private DnsLookupFamily dnsLookupFamily = null;
    private IP_VERSION ipVersion = null;

    private final Integer targetPort;
    private SortedSet<IfHttpFilter> httpFilterSet = new TreeSet<>((proxy1,
                                                                   proxy2) -> proxy1.getPriority().compareTo(proxy2.getPriority()));

    private static final int SOCKET_OPTIONS_IPPROTO_IP = 0;
    private static final int SOCKET_OPTIONS_IP_TOS = 1;
    private static final String SOCKET_OPTIONS_IP_TOS_DESCRIPTION = "IP_TOS";
    private static final int SOCKET_OPTIONS_IPPROTO_IPV6 = 41;
    private static final int SOCKET_OPTIONS_IPV6_TCLASS = 67;
    private static final String SOCKET_OPTIONS_IPV6_TCLASS_DESCRIPTION = "IPV6_TCLASS";

    /**
     * Creates a ProxyListener to be used for CSA
     *
     * @param name
     * @param scpInstName
     * @param scpServiceAddrName
     */
    public ProxyListener(String name,
                         String scpInstName,
                         String scpServiceAddrName)
    {
        this.name = name;
        // For CSA, envoyListenerName = name
        this.envoyListenerName = name;
        this.scpInstanceName = scpInstName;
        this.scpServiceAddrName = scpServiceAddrName;
        // not used in csa
        this.targetPort = null;
    }

    /**
     * Creates a ProxyListener to be used for SCP and SEPP
     *
     * @param name               Name of the ProxyListener
     * @param envoyListenerName  The final name with which this ProxyListener is
     *                           going to be mapped in Envoy
     * @param targetPort         The port the resulting envoy listener listens to
     * @param ipVersion          Defined the localhost interface the resulting envoy
     *                           listener listens to (0.0.0.0 / ::)
     * @param scpInstName
     * @param scpServiceAddrName
     */
    public ProxyListener(String name,
                         String envoyListenerName,
                         Integer targetPort,
                         IP_VERSION ipVersion,
                         String scpInstName,
                         String scpServiceAddrName)
    {
        this.name = name;
        this.envoyListenerName = envoyListenerName;
        this.targetPort = targetPort;
        this.ipVersion = ipVersion;
        this.scpInstanceName = scpInstName;
        this.scpServiceAddrName = scpServiceAddrName;
    }

    public ProxyListener(ProxyListener anotherPxListener)
    {
        this.name = anotherPxListener.name;
        this.envoyListenerName = anotherPxListener.envoyListenerName;
        this.tls = anotherPxListener.getTls().map(v -> v);
        // deep copy
        this.vHosts = anotherPxListener.vHosts.stream().collect(Collectors.toList());
        this.contextData = new HashMap<>(anotherPxListener.contextData);
        this.routingContext = new HashMap<>(anotherPxListener.routingContext);
        this.localReplyMappings = anotherPxListener.localReplyMappings.stream().collect(Collectors.toList());
        this.scpInstanceName = anotherPxListener.getScpInstanceName();
        this.scpServiceAddrName = anotherPxListener.scpServiceAddrName;
        this.scpServiceAddress = anotherPxListener.scpServiceAddress;
        this.sprRouteConfigured = anotherPxListener.sprRouteConfigured;
        this.accessLogEnabled = anotherPxListener.accessLogEnabled;
        this.tcpKeepaliveEnabled = anotherPxListener.tcpKeepaliveEnabled;
        this.tcpKeepalive = anotherPxListener.tcpKeepalive;
        this.idleTimeoutSeconds = anotherPxListener.idleTimeoutSeconds;
        this.maxConnectionDuration = anotherPxListener.maxConnectionDuration;
        this.maxConcurrentStreams = anotherPxListener.maxConcurrentStreams;
        this.dnsLookupFamily = anotherPxListener.dnsLookupFamily;
        this.httpFilterSet = new TreeSet<>(anotherPxListener.httpFilterSet);
        this.targetPort = anotherPxListener.targetPort;
        this.ipVersion = anotherPxListener.ipVersion;
        this.maxStreamDuration = anotherPxListener.maxStreamDuration;
        this.hpackTableSize = anotherPxListener.getHpackTableSize().map(v -> v);
        this.vtapSettings = anotherPxListener.getVtapSettings().map(vs -> (new ProxyVtapSettings(vs)));
        this.dscpMarking = anotherPxListener.getDscpMarking();
    }

    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the name of the Listener as it is going to be configured in envoy,
     * form this ProxyListener.
     *
     * @return
     */
    public String getEnvoyListenerName()
    {

        return this.envoyListenerName;
    }

    public void addVirtualHost(ProxyVirtualHost vh)
    {
        this.vHosts.add(vh);
    }

    /**
     * Return all VHosts. The order is not important since Envoy has rules how to
     * evaluate all domains:<br>
     * https://www.envoyproxy.io/docs/envoy/v1.15.0/api-v2/api/v2/route/route_components.proto<br>
     *
     * Domain search order:
     * <ol>
     * <li>Exact domain names: www.foo.com.
     * <li>Suffix domain wildcards: *.foo.com or *-bar.foo.com.
     * <li>Prefix domain wildcards: foo.* or foo-*.
     * <li>Special wildcard * matching any domain.
     * </ol>
     *
     * @return
     */
    public List<ProxyVirtualHost> getVirtualHosts()
    {
        return this.vHosts;
    }

    public void addContextData(Map<String, Map<String, String>> contextData)
    {
        this.contextData.putAll(contextData);
    }

    public void setRoutingContext(Map<String, Object> routingContext)
    {
        this.routingContext = routingContext;
    }

    public Map<String, Object> getRoutingContext()
    {
        return this.routingContext;
    }

    public List<ProxyLocalReplyContext> getLocalReplyMappings()
    {
        return this.localReplyMappings;
    }

    public void setLocalReplyMappings(List<ProxyLocalReplyContext> alist)
    {
        this.localReplyMappings = alist;
    }

    public String getLuaFilter()
    {
        if (!this.contextData.isEmpty())
            return FilterFactory.getCsaLuaFilter(this.contextData);
        else if (!this.routingContext.isEmpty())
            return FilterFactory.getCsaLuaFilterString(this.routingContext);
        return "";
    }

    public void setTls(Optional<ProxyTls> tls)
    {
        this.tls = tls;
    }

    public Optional<ProxyTls> getTls()
    {
        return this.tls;
    }

    /**
     * @return the targetPort
     */
    public Integer getTargetPort()
    {
        return targetPort;
    }

    public String getRoutesName()
    {
        return this.name + "_routes";
    }

    /**
     * @return the scpInstance
     */
    public String getScpInstanceName()
    {
        return scpInstanceName;
    }

    /**
     * @param scpInstance the scpInstance to set
     */
    public void setScpInstanceName(String scpInstance)
    {
        this.scpInstanceName = scpInstance;
    }

    public String getScpServiceAddrName()
    {
        return scpServiceAddrName;
    }

    public void setScpServiceAddrName(String scpServiceAddrName)
    {
        this.scpServiceAddrName = scpServiceAddrName;
    }

    public List<String> getScpServiceAddress()
    {
        return scpServiceAddress;
    }

    public void setScpServiceAddress(List<String> scpServiceAddress)
    {
        this.scpServiceAddress = List.copyOf(scpServiceAddress);
    }

    /**
     * @return the sprRouteConfigured
     */
    public boolean isSprRouteConfigured()
    {
        return sprRouteConfigured;
    }

    /**
     * @param sprRouteConfigured the sprRouteConfigured to set
     */
    public void setSprRouteConfigured(boolean sprRouteConfigured)
    {
        this.sprRouteConfigured = sprRouteConfigured;
    }

    public boolean isAccessLogEnabled()
    {
        return accessLogEnabled;
    }

    public void setAccessLogEnabled(boolean accessLogEnabled)
    {
        this.accessLogEnabled = accessLogEnabled;
    }

    public boolean isTcpKeepaliveEnabled()
    {
        return tcpKeepaliveEnabled;
    }

    public void setTcpKeepaliveEnabled(boolean tcpKeepaliveEnabled)
    {
        this.tcpKeepaliveEnabled = tcpKeepaliveEnabled;
    }

    public ProxyTcpKeepalive getTcpKeepalive()
    {
        return tcpKeepalive;
    }

    public void setTcpKeepalive(ProxyTcpKeepalive tcpKeepalive)
    {
        this.tcpKeepalive = tcpKeepalive;
    }

    public long getIdleTimeout()
    {
        return this.idleTimeoutSeconds;
    }

    public void setIdleTimeout(long timeout)
    {
        this.idleTimeoutSeconds = timeout;
    }

    public Integer getMaxConnectionDuration()
    {
        return maxConnectionDuration;
    }

    public void setMaxConnectionDuration(Integer maxConnectionDuration)
    {
        this.maxConnectionDuration = maxConnectionDuration;
    }

    public Integer getMaxConcurrentStreams()
    {
        return maxConcurrentStreams;
    }

    public void setMaxConcurrentStreams(Integer maxConcurrentStreams)
    {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public DnsLookupFamily getDnsLookupFamily()
    {
        return dnsLookupFamily;
    }

    public void setDnsLookupFamily(DnsLookupFamily val)
    {
        this.dnsLookupFamily = val;
    }

    public void setMaxStreamDuration(long duration)
    {
        this.maxStreamDuration = duration;
    }

    public long getMaxStreamDuration()
    {
        return this.maxStreamDuration;
    }

    public Optional<Integer> getHpackTableSize()
    {
        return hpackTableSize;
    }

    public void setHpackTableSize(Optional<Integer> hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
    }

    /**
     * @return the ipVersion
     */
    public IP_VERSION getIpVersion()
    {
        return ipVersion;
    }

    /**
     * @param ipVersion the ipVersion to set
     */
    public void setIpVersion(IP_VERSION ipVersion)
    {
        this.ipVersion = ipVersion;
    }

    /**
     * @return the vtapSettings
     */
    public Optional<ProxyVtapSettings> getVtapSettings()
    {
        return vtapSettings;
    }

    /**
     * @param vtapSettings the vtapSettings to set
     */
    public void setVtapSettings(Optional<ProxyVtapSettings> vtapSettings)
    {
        this.vtapSettings = vtapSettings;
    }

    public Integer getDscpMarking()
    {
        return dscpMarking;
    }

    public void setDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
    }

    public Builder initBuilder()
    {
        var listenerBuilder = initiateListenerBuilder();
        buildSocketOptions(listenerBuilder);
        return listenerBuilder;
    }

    public Builder initiateListenerBuilder()
    {
        var builder = Listener.newBuilder()
                              .setName(this.getEnvoyListenerName())
                              .setAddress(Address.newBuilder()
                                                 .setSocketAddress(SocketAddress.newBuilder()
                                                                                .setAddress(this.getListenerAddress())
                                                                                .setPortValue(this.getListenerTargetPort().intValue())
                                                                                .build())
                                                 .build())
                              .setConnectionBalanceConfig(ConnectionBalanceConfig.newBuilder().setExactBalance(ExactBalance.newBuilder().build()));

        // if listener is DS, then the main address is ipv6 and the additional address
        // is ipv4. The additional address also has the specific socket options for
        // ipv4.
        if (this.isIpVersionDualstack())
        {
            List<SocketOption> socketOptions = getCommonSocketOptions();

            if (this.dscpMarking != null)
            {
                socketOptions.add(SocketOption.newBuilder()
                                              .setDescription(SOCKET_OPTIONS_IP_TOS_DESCRIPTION)
                                              .setLevel(SOCKET_OPTIONS_IPPROTO_IP)
                                              .setName(SOCKET_OPTIONS_IP_TOS)
                                              .setIntValue(this.dscpMarking * 4L)
                                              .build());
            }

            builder.addAdditionalAddresses(AdditionalAddress.newBuilder()
                                                            .setAddress(Address.newBuilder()
                                                                               .setSocketAddress(SocketAddress.newBuilder()
                                                                                                              .setAddress("0.0.0.0")
                                                                                                              .setPortValue(this.getListenerTargetPort()
                                                                                                                                .intValue())
                                                                                                              .build())
                                                                               .build())
                                                            .setSocketOptions(SocketOptionsOverride.newBuilder().addAllSocketOptions(socketOptions).build()));
        }

        return builder;
    }

    public void buildSocketOptions(Builder listenerBuilder)
    {
        List<SocketOption> socketOptions = getCommonSocketOptions();

        if (this.dscpMarking != null)
        {
            // for IPv6 and Dualstack, the main address is the ipv6, so in this part the
            // ipv6-specific options are used.
            // For DS, the socket options for additionalAddress (ipv4) are created in
            // initiateListenerBuilder().
            var level = this.isVersionIpv4() ? SOCKET_OPTIONS_IPPROTO_IP : SOCKET_OPTIONS_IPPROTO_IPV6;
            var name = this.isVersionIpv4() ? SOCKET_OPTIONS_IP_TOS : SOCKET_OPTIONS_IPV6_TCLASS;
            var description = this.isVersionIpv4() ? SOCKET_OPTIONS_IP_TOS_DESCRIPTION : SOCKET_OPTIONS_IPV6_TCLASS_DESCRIPTION;

            socketOptions.add(SocketOption.newBuilder()
                                          .setDescription(description)
                                          .setLevel(level)
                                          .setName(name)
                                          .setIntValue(this.dscpMarking * 4L) // Multiply value by 4 due to bit shift that happens in DSCP
                                                                              // marking parsing
                                          .build());
        }

        listenerBuilder.addAllSocketOptions(socketOptions);
    }

    private List<SocketOption> getCommonSocketOptions()
    {
        List<SocketOption> options = new ArrayList<>();

        // DND-22504 due to backforward compatibility, by default tcpkeepalive is
        // disabled in ingress(nothing configured in ingress connection profile). When
        // operator configures it, it's enabled with configured values.
        if (this.isTcpKeepaliveEnabled())
        {
            long userTimeout = 1000l * (this.getTcpKeepalive().getTime() + this.getTcpKeepalive().getProbes() * this.getTcpKeepalive().getInterval());
            options.add(SocketOption.newBuilder()
                                    .setDescription("SO_KEEPALIVE") // # SOL_SOCKET = 1, SO_KEEPALIVE = 9, int_value =1 to
                                    // enable KA feature in socket.
                                    .setLevel(1)
                                    .setName(9)
                                    .setIntValue(1)
                                    .build());
            options.add(SocketOption.newBuilder()
                                    .setDescription("TCP_KEEPIDLE") // level = 6 IPPROTO_TCP, name = 4 TCP_KEEPIDLE(time)
                                    .setLevel(6)
                                    .setName(4)
                                    .setIntValue((long) (this.getTcpKeepalive().getTime()))
                                    .build());
            options.add(SocketOption.newBuilder()
                                    .setDescription("TCP_KEEPINTVL") // level = 6 IPPROTO_TCP, name = 5 TCP_KEEPINTVL
                                    .setLevel(6)
                                    .setName(5)
                                    .setIntValue((long) (this.getTcpKeepalive().getInterval()))
                                    .build());
            options.add(SocketOption.newBuilder()
                                    .setDescription("TCP_KEEPCNT") // level = 6 IPPROTO_TCP, name = 6
                                    // TCP_KEEPCNT(keepalive_probes)
                                    .setLevel(6)
                                    .setName(6)
                                    .setIntValue((long) (this.getTcpKeepalive().getProbes()))
                                    .build());
            options.add(SocketOption.newBuilder()
                                    .setDescription("TCP_USER_TIMEOUT")
                                    .setLevel(6) // IPPROT_TCP, see /usr/include/linux/in.h
                                    .setName(18) // TCP_USER_TIMEOUT, see
                                                 // /usr/include/linux/tcp.h
                                    .setIntValue(userTimeout)
                                    .build());
        }

        return options;
    }

    public Integer getListenerTargetPort()
    {
        if (this.getTargetPort() != null)
        {
            return this.getTargetPort();
        }
        // These are hardcoded because the helm-chart depends on it
        // useTls is nonNull (set to false during construction)
        if (this.getTls().isPresent())
        {
            return 8443;
        }
        else
        {
            return 8080;
        }
    }

    /**
     * If the listener is single stack, returns the respective address. If the
     * listener is dualstack, returns the ipv6 address.
     * 
     * @return The main address of this listener
     */
    public String getListenerAddress()
    {
        return this.isVersionIpv4() ? "0.0.0.0" : "::";
    }

    public boolean isVersionIpv4()
    {
        return this.getIpVersion() != null ? this.getIpVersion().equals(IP_VERSION.IPV4) : RuntimeEnvironment.getDeployedIpVersion().equals(IP_VERSION.IPV4);
    }

    public boolean isIpVersionDualstack()
    {
        IP_VERSION currentIpVersion = this.ipVersion != null ? this.ipVersion : RuntimeEnvironment.getDeployedIpVersion();
        return currentIpVersion.equals(IP_VERSION.IPV4_IPV6);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyListener [name=" + name + ", envoyListenerName=" + envoyListenerName + ", tls=" + tls + ", vHosts=" + vHosts + ", contextData="
               + contextData + ", routingContext=" + routingContext + ", localReplyMappings=" + localReplyMappings + ", scpInstanceName=" + scpInstanceName
               + ", scpServiceAddrName=" + scpServiceAddrName + ", scpServiceAddress=" + scpServiceAddress + ", sprRouteConfigured=" + sprRouteConfigured
               + ", accessLogEnabled=" + accessLogEnabled + ", tcpKeepaliveEnabled=" + tcpKeepaliveEnabled + ", tcpKeepalive=" + tcpKeepalive
               + ", idleTimeoutSeconds=" + idleTimeoutSeconds + ", maxConnectionDuration=" + maxConnectionDuration + ", maxConcurrentStreams="
               + maxConcurrentStreams + ", hpackTableSize=" + hpackTableSize + ", vtapSettings=" + vtapSettings + ", maxStreamDuration=" + maxStreamDuration
               + ", dnsLookupFamily=" + dnsLookupFamily + ", ipVersion=" + ipVersion + ", targetPort=" + targetPort + ", httpFilterSet=" + httpFilterSet + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(accessLogEnabled,
                            contextData,
                            dnsLookupFamily,
                            envoyListenerName,
                            hpackTableSize,
                            httpFilterSet,
                            idleTimeoutSeconds,
                            ipVersion,
                            localReplyMappings,
                            maxConcurrentStreams,
                            maxConnectionDuration,
                            maxStreamDuration,
                            name,
                            routingContext,
                            scpInstanceName,
                            scpServiceAddrName,
                            scpServiceAddress,
                            sprRouteConfigured,
                            targetPort,
                            tcpKeepalive,
                            tcpKeepaliveEnabled,
                            tls,
                            vHosts,
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
        ProxyListener other = (ProxyListener) obj;
        return accessLogEnabled == other.accessLogEnabled && Objects.equals(contextData, other.contextData) && dnsLookupFamily == other.dnsLookupFamily
               && Objects.equals(envoyListenerName, other.envoyListenerName) && Objects.equals(hpackTableSize, other.hpackTableSize)
               && Objects.equals(httpFilterSet, other.httpFilterSet) && idleTimeoutSeconds == other.idleTimeoutSeconds && ipVersion == other.ipVersion
               && Objects.equals(localReplyMappings, other.localReplyMappings) && Objects.equals(maxConcurrentStreams, other.maxConcurrentStreams)
               && Objects.equals(maxConnectionDuration, other.maxConnectionDuration) && maxStreamDuration == other.maxStreamDuration
               && Objects.equals(name, other.name) && Objects.equals(routingContext, other.routingContext)
               && Objects.equals(scpInstanceName, other.scpInstanceName) && Objects.equals(scpServiceAddrName, other.scpServiceAddrName)
               && Objects.equals(scpServiceAddress, other.scpServiceAddress) && sprRouteConfigured == other.sprRouteConfigured
               && Objects.equals(targetPort, other.targetPort) && Objects.equals(tcpKeepalive, other.tcpKeepalive)
               && tcpKeepaliveEnabled == other.tcpKeepaliveEnabled && Objects.equals(tls, other.tls) && Objects.equals(vHosts, other.vHosts)
               && Objects.equals(vtapSettings, other.vtapSettings);
    }

    /**
     * @return the connManagerConfigSet
     */
    public SortedSet<IfHttpFilter> getHttpFilterSet()
    {
        return httpFilterSet;
    }

    /**
     * @param connManagerConfigSet the connManagerConfigSet to set
     */
    public void setHttpFilterSet(SortedSet<IfHttpFilter> httpFilterSet)
    {
        this.httpFilterSet = httpFilterSet;
    }

    public void addHttpFilter(IfHttpFilter httpFilter)
    {
        this.httpFilterSet.add(httpFilter);
    }

}
