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

import java.util.Objects;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint.Builder;

/**
 * Simple container class. Holds all the data needed for a DNS endpoint. This
 * should only be used for clusters that use STRICT_DNS. It is for hosts that
 * are DNS resolved by envoy.
 */
public class ProxyDnsEndpoint
{
    private final String hostname;
    private final int port;

    public ProxyDnsEndpoint(String hostname,
                            int port)
    {
        this.hostname = hostname;
        this.port = port;
    }

    public ProxyDnsEndpoint(ProxyDnsEndpoint ep)
    {
        this.hostname = ep.getHostname();
        this.port = ep.getPort();
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyDnsEndpoint [hostname=" + hostname + ", port=" + port + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(hostname, port);
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
        ProxyDnsEndpoint other = (ProxyDnsEndpoint) obj;
        return Objects.equals(hostname, other.hostname) && port == other.port;
    }

    public Builder initBuilder()
    {
        var addressBuilder = Address.newBuilder().setSocketAddress(SocketAddress.newBuilder().setAddress(hostname).setPortValue(port).build());
        var epBuilder = Endpoint.newBuilder().setAddress(addressBuilder.build());

        return LbEndpoint.newBuilder().setEndpoint(epBuilder.build());
    }
}
