
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfMultipleIpEndpoint;
import com.ericsson.sc.nfm.model.Transport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "transport", "port", "ipv4-address", "ipv6-address" })
public class MultipleIpEndpoint implements IfMultipleIpEndpoint
{

    /**
     * Name identifying the ip-endpoint
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the ip-endpoint")
    private String name;
    /**
     * Transport protocol
     * 
     */
    @JsonProperty("transport")
    @JsonPropertyDescription("Transport protocol")
    private Transport transport;
    /**
     * Port integer
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("Port integer")
    private Integer port;
    /**
     * IPv4 address
     * 
     */
    @JsonProperty("ipv4-address")
    @JsonPropertyDescription("IPv4 address")
    private List<String> ipv4Address = new ArrayList<String>();
    /**
     * IPv6 address
     * 
     */
    @JsonProperty("ipv6-address")
    @JsonPropertyDescription("IPv6 address")
    private List<String> ipv6Address = new ArrayList<String>();

    /**
     * Name identifying the ip-endpoint
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the ip-endpoint
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public MultipleIpEndpoint withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Transport protocol
     * 
     */
    @JsonProperty("transport")
    public Transport getTransport()
    {
        return transport;
    }

    /**
     * Transport protocol
     * 
     */
    @JsonProperty("transport")
    public void setTransport(Transport transport)
    {
        this.transport = transport;
    }

    public MultipleIpEndpoint withTransport(Transport transport)
    {
        this.transport = transport;
        return this;
    }

    /**
     * Port integer
     * 
     */
    @JsonProperty("port")
    public Integer getPort()
    {
        return port;
    }

    /**
     * Port integer
     * 
     */
    @JsonProperty("port")
    public void setPort(Integer port)
    {
        this.port = port;
    }

    public MultipleIpEndpoint withPort(Integer port)
    {
        this.port = port;
        return this;
    }

    /**
     * IPv4 address
     * 
     */
    @JsonProperty("ipv4-address")
    public List<String> getIpv4Address()
    {
        return ipv4Address;
    }

    /**
     * IPv4 address
     * 
     */
    @JsonProperty("ipv4-address")
    public void setIpv4Address(List<String> ipv4Address)
    {
        this.ipv4Address = ipv4Address;
    }

    public MultipleIpEndpoint withIpv4Address(List<String> ipv4Address)
    {
        this.ipv4Address = ipv4Address;
        return this;
    }

    /**
     * IPv6 address
     * 
     */
    @JsonProperty("ipv6-address")
    public List<String> getIpv6Address()
    {
        return ipv6Address;
    }

    /**
     * IPv6 address
     * 
     */
    @JsonProperty("ipv6-address")
    public void setIpv6Address(List<String> ipv6Address)
    {
        this.ipv6Address = ipv6Address;
    }

    public MultipleIpEndpoint withIpv6Address(List<String> ipv6Address)
    {
        this.ipv6Address = ipv6Address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MultipleIpEndpoint.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("transport");
        sb.append('=');
        sb.append(((this.transport == null) ? "<null>" : this.transport));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
        sb.append(',');
        sb.append("ipv4Address");
        sb.append('=');
        sb.append(((this.ipv4Address == null) ? "<null>" : this.ipv4Address));
        sb.append(',');
        sb.append("ipv6Address");
        sb.append('=');
        sb.append(((this.ipv6Address == null) ? "<null>" : this.ipv6Address));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.ipv4Address == null) ? 0 : this.ipv4Address.hashCode()));
        result = ((result * 31) + ((this.transport == null) ? 0 : this.transport.hashCode()));
        result = ((result * 31) + ((this.ipv6Address == null) ? 0 : this.ipv6Address.hashCode()));
        result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof MultipleIpEndpoint) == false)
        {
            return false;
        }
        MultipleIpEndpoint rhs = ((MultipleIpEndpoint) other);
        return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                   && ((this.ipv4Address == rhs.ipv4Address) || ((this.ipv4Address != null) && this.ipv4Address.equals(rhs.ipv4Address))))
                  && ((this.transport == rhs.transport) || ((this.transport != null) && this.transport.equals(rhs.transport))))
                 && ((this.ipv6Address == rhs.ipv6Address) || ((this.ipv6Address != null) && this.ipv6Address.equals(rhs.ipv6Address))))
                && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))));
    }
}
