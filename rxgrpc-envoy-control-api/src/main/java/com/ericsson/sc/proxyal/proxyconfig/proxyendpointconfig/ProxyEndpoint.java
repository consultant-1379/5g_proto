/**
 * COPYRIGHT ERICSSON GMBH 2019
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Apr 18, 2019
 * Author: eedrak
 */
package com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataMap;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint.Builder;

/**
 * Simple container class. Holds all the data needed for an endpoint. Some
 * values are hard-coded here, some come from the configuration uploaded to the
 * CM Mediator.
 */
public class ProxyEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(ProxyEndpoint.class);

    private final String address;
    private final int port;
    private final int priority;
    private Optional<String> hostname = Optional.empty();
    private int loadBalancingWeight;
    private Optional<String> ipAddress = Optional.empty();

    private ProxyMetadataMap metadataMap = new ProxyMetadataMap();

    private boolean schemeHttps = false;
    private boolean endpointVtapFlag = false;
    private boolean matchTLS = false;

    public ProxyEndpoint(String address,
                         int port,
                         int priority)
    {
        this.address = address;
        this.port = port;
        this.priority = priority;
        this.loadBalancingWeight = 100;
    }

    public ProxyEndpoint(String address,
                         int port,
                         int priority,
                         Optional<String> hostname,
                         boolean schemeHttps)
    {
        this.address = address;
        this.port = port;
        this.priority = priority;
        this.loadBalancingWeight = 100;
        this.hostname = hostname;
        this.schemeHttps = schemeHttps;
    }

    public ProxyEndpoint(String address,
                         int port)
    {
        this.address = address;
        this.port = port;
        this.priority = 0;
        this.loadBalancingWeight = 100;
    }

    public ProxyEndpoint(ProxyEndpoint anotherEndpoint)
    {
        this.address = anotherEndpoint.address;
        this.port = anotherEndpoint.port;
        this.priority = anotherEndpoint.priority;
        this.hostname = anotherEndpoint.getHostname().map(v -> v);
        this.loadBalancingWeight = anotherEndpoint.loadBalancingWeight;
        this.matchTLS = anotherEndpoint.matchTLS;
        this.ipAddress = anotherEndpoint.getIpAddress().map(v -> v);
        this.schemeHttps = anotherEndpoint.schemeHttps;
        this.endpointVtapFlag = anotherEndpoint.endpointVtapFlag;
        this.metadataMap = anotherEndpoint.getEndpointMetadata();
    }

    public boolean isHttps()
    {
        return this.schemeHttps;
    }

    public Optional<String> getHostname()
    {
        return this.hostname;
    }

    public String getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    public int getPriority()
    {
        return priority;
    }

    public int getLoadBalancingWeight()
    {
        return loadBalancingWeight;
    }

    public void setLoadBalancingWeight(int loadBalancingWeight)
    {
        this.loadBalancingWeight = loadBalancingWeight;
    }

    /**
     * @return the endpointVtapFlag
     */
    public boolean getEndpointVtapFlag()
    {
        return endpointVtapFlag;
    }

    /**
     * @param endpointVtapFlag the endpointVtapFlag to set
     */
    public void setEndpointVtapFlag(boolean endpointVtapFlag)
    {
        this.endpointVtapFlag = endpointVtapFlag;
    }

    public void setIpAddress(Optional<String> ipAddress)
    {
        this.ipAddress = ipAddress;

    }

    /**
     * Simple check if an IP address is an IPv6-address. The check is positive if
     * the address contains at least on colon (":").
     *
     * @return true if it is an IPv6-address, false otherwise.
     */
    public boolean isIpV6Address()
    {
        return (this.ipAddress.isPresent() && this.ipAddress.get().contains(":"));
    }

    public Optional<String> getIpAddress()
    {
        return this.ipAddress;
    }

    public ProxyMetadataMap getEndpointMetadata()
    {
        return metadataMap;
    }

    public void setEndpointMetadata(ProxyMetadataMap metadataMap)
    {
        this.metadataMap = metadataMap;
    }

    public boolean getMatchTLS()
    {
        return matchTLS;
    }

    public void setMatchTLS(boolean matchTLS)
    {
        this.matchTLS = matchTLS;
    }

    public Builder initBuilder()
    {
        var epAddress = this.address;

        // Hostname purely for auto_rewrite_host to automatically set the authority
        // header in EDS-type clusters:
        var hostnameForAuthorityHeader = this.hostname.orElse(isIpV6Address() ? "[" + epAddress + "]" : epAddress);

        epAddress = this.ipAddress.get();
        log.debug("Adding endpoint {} with priority {}", epAddress, this.priority);
        var lbEpBuilder = LbEndpoint.newBuilder();

        lbEpBuilder.setEndpoint(Endpoint.newBuilder()
                                        .setAddress(Address.newBuilder()
                                                           .setSocketAddress(SocketAddress.newBuilder().setAddress(epAddress).setPortValue(this.port).build())
                                                           .build()) //
                                        .setHostname(hostnameForAuthorityHeader + ":" + this.port)
                                        .build())
                   .setLoadBalancingWeight(UInt32Value.of(this.loadBalancingWeight));
        return lbEpBuilder;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyEndpoint [address=" + address + ", port=" + port + ", priority=" + priority + ", hostname=" + hostname + ", loadBalancingWeight="
               + loadBalancingWeight + ", ipAddress=" + ipAddress + ", metaData=" + metadataMap + ", schemeHttps=" + schemeHttps + ", vtapFlag="
               + endpointVtapFlag + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(address, hostname, ipAddress, metadataMap, matchTLS, loadBalancingWeight, port, priority, schemeHttps, endpointVtapFlag);
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
        ProxyEndpoint other = (ProxyEndpoint) obj;
        return Objects.equals(address, other.address) && Objects.equals(hostname, other.hostname) && Objects.equals(ipAddress, other.ipAddress)
               && Objects.equals(metadataMap, other.metadataMap) && Objects.equals(matchTLS, other.matchTLS) && loadBalancingWeight == other.loadBalancingWeight
               && port == other.port && priority == other.priority && schemeHttps == other.schemeHttps
               && Objects.equals(endpointVtapFlag, other.endpointVtapFlag);
    }

}
