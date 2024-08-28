
package com.ericsson.sc.bsf.model;

import com.ericsson.sc.nfm.model.Transport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "transport", "ipv4-address", "port", "ipv6-address" })
public class Endpoint
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("transport")
    private Transport transport;
    @JsonProperty("ipv4-address")
    private String ipv4Address;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("ipv6-address")
    private String ipv6Address;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId()
    {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id)
    {
        this.id = id;
    }

    public Endpoint withId(Integer id)
    {
        this.id = id;
        return this;
    }

    @JsonProperty("transport")
    public Transport getTransport()
    {
        return transport;
    }

    @JsonProperty("transport")
    public void setTransport(Transport transport)
    {
        this.transport = transport;
    }

    public Endpoint withTransport(Transport transport)
    {
        this.transport = transport;
        return this;
    }

    @JsonProperty("ipv4-address")
    public String getIpv4Address()
    {
        return ipv4Address;
    }

    @JsonProperty("ipv4-address")
    public void setIpv4Address(String ipv4Address)
    {
        this.ipv4Address = ipv4Address;
    }

    public Endpoint withIpv4Address(String ipv4Address)
    {
        this.ipv4Address = ipv4Address;
        return this;
    }

    @JsonProperty("port")
    public Integer getPort()
    {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port)
    {
        this.port = port;
    }

    public Endpoint withPort(Integer port)
    {
        this.port = port;
        return this;
    }

    @JsonProperty("ipv6-address")
    public String getIpv6Address()
    {
        return ipv6Address;
    }

    @JsonProperty("ipv6-address")
    public void setIpv6Address(String ipv6Address)
    {
        this.ipv6Address = ipv6Address;
    }

    public Endpoint withIpv6Address(String ipv6Address)
    {
        this.ipv6Address = ipv6Address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Endpoint.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("transport");
        sb.append('=');
        sb.append(((this.transport == null) ? "<null>" : this.transport));
        sb.append(',');
        sb.append("ipv4Address");
        sb.append('=');
        sb.append(((this.ipv4Address == null) ? "<null>" : this.ipv4Address));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
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
        result = ((result * 31) + ((this.ipv4Address == null) ? 0 : this.ipv4Address.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
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
        if ((other instanceof Endpoint) == false)
        {
            return false;
        }
        Endpoint rhs = ((Endpoint) other);
        return ((((((this.ipv4Address == rhs.ipv4Address) || ((this.ipv4Address != null) && this.ipv4Address.equals(rhs.ipv4Address)))
                   && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                  && ((this.transport == rhs.transport) || ((this.transport != null) && this.transport.equals(rhs.transport))))
                 && ((this.ipv6Address == rhs.ipv6Address) || ((this.ipv6Address != null) && this.ipv6Address.equals(rhs.ipv6Address))))
                && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))));
    }

}
