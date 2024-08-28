
package com.ericsson.esc.services.cm.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "ipv4-address", "ipv6-address", "fqdn", "port", "tls-port" })
public class ServiceAddress implements IfNamedListItem
{

    /**
     * Name uniquely identifying the service address.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the service address.")
    private String name;
    /**
     * IPv4 address of the SCP service
     * 
     */
    @JsonProperty("ipv4-address")
    @JsonPropertyDescription("IPv4 address of the SCP service")
    private String ipv4Address;
    /**
     * IPv6 address of the SCP service
     * 
     */
    @JsonProperty("ipv6-address")
    @JsonPropertyDescription("IPv6 address of the SCP service")
    private String ipv6Address;
    /**
     * FQDN of the nf-service, for example chf.op.com
     * 
     */
    @JsonProperty("fqdn")
    @JsonPropertyDescription("FQDN of the nf-service, for example chf.op.com")
    private String fqdn;
    /**
     * This port is used to receive non-TLS traffic.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("This port is used to receive non-TLS traffic.")
    private Integer port;
    /**
     * This port is used to receive TLS traffic.
     * 
     */
    @JsonProperty("tls-port")
    @JsonPropertyDescription("This port is used to receive TLS traffic.")
    private Integer tlsPort;

    /**
     * Name uniquely identifying the service address.
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the service address.
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ServiceAddress withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * IPv4 address of the SCP service
     * 
     */
    @JsonProperty("ipv4-address")
    public String getIpv4Address()
    {
        return ipv4Address;
    }

    /**
     * IPv4 address of the SCP service
     * 
     */
    @JsonProperty("ipv4-address")
    public void setIpv4Address(String ipv4Address)
    {
        this.ipv4Address = ipv4Address;
    }

    public ServiceAddress withIpv4Address(String ipv4Address)
    {
        this.ipv4Address = ipv4Address;
        return this;
    }

    /**
     * IPv6 address of the SCP service
     * 
     */
    @JsonProperty("ipv6-address")
    public String getIpv6Address()
    {
        return ipv6Address;
    }

    /**
     * IPv6 address of the SCP service
     * 
     */
    @JsonProperty("ipv6-address")
    public void setIpv6Address(String ipv6Address)
    {
        this.ipv6Address = ipv6Address;
    }

    public ServiceAddress withIpv6Address(String ipv6Address)
    {
        this.ipv6Address = ipv6Address;
        return this;
    }

    /**
     * FQDN of the nf-service, for example chf.op.com
     * 
     */
    @JsonProperty("fqdn")
    public String getFqdn()
    {
        return fqdn;
    }

    /**
     * FQDN of the nf-service, for example chf.op.com
     * 
     */
    @JsonProperty("fqdn")
    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public ServiceAddress withFqdn(String fqdn)
    {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * This port is used to receive non-TLS traffic.
     * 
     */
    @JsonProperty("port")
    public Integer getPort()
    {
        return port;
    }

    /**
     * This port is used to receive non-TLS traffic.
     * 
     */
    @JsonProperty("port")
    public void setPort(Integer port)
    {
        this.port = port;
    }

    public ServiceAddress withPort(Integer port)
    {
        this.port = port;
        return this;
    }

    /**
     * This port is used to receive TLS traffic.
     * 
     */
    @JsonProperty("tls-port")
    public Integer getTlsPort()
    {
        return tlsPort;
    }

    /**
     * This port is used to receive TLS traffic.
     * 
     */
    @JsonProperty("tls-port")
    public void setTlsPort(Integer tlsPort)
    {
        this.tlsPort = tlsPort;
    }

    public ServiceAddress withTlsPort(Integer tlsPort)
    {
        this.tlsPort = tlsPort;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ServiceAddress.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("ipv4Address");
        sb.append('=');
        sb.append(((this.ipv4Address == null) ? "<null>" : this.ipv4Address));
        sb.append(',');
        sb.append("ipv6Address");
        sb.append('=');
        sb.append(((this.ipv6Address == null) ? "<null>" : this.ipv6Address));
        sb.append(',');
        sb.append("fqdn");
        sb.append('=');
        sb.append(((this.fqdn == null) ? "<null>" : this.fqdn));
        sb.append(',');
        sb.append("port");
        sb.append('=');
        sb.append(((this.port == null) ? "<null>" : this.port));
        sb.append(',');
        sb.append("tlsPort");
        sb.append('=');
        sb.append(((this.tlsPort == null) ? "<null>" : this.tlsPort));
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
        result = ((result * 31) + ((this.ipv6Address == null) ? 0 : this.ipv6Address.hashCode()));
        result = ((result * 31) + ((this.tlsPort == null) ? 0 : this.tlsPort.hashCode()));
        result = ((result * 31) + ((this.fqdn == null) ? 0 : this.fqdn.hashCode()));
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
        if ((other instanceof ServiceAddress) == false)
        {
            return false;
        }
        ServiceAddress rhs = ((ServiceAddress) other);
        return (((((((this.ipv6Address == rhs.ipv6Address) || ((this.ipv6Address != null) && this.ipv6Address.equals(rhs.ipv6Address)))
                    && ((this.tlsPort == rhs.tlsPort) || ((this.tlsPort != null) && this.tlsPort.equals(rhs.tlsPort))))
                   && ((this.fqdn == rhs.fqdn) || ((this.fqdn != null) && this.fqdn.equals(rhs.fqdn))))
                  && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))))
                 && ((this.ipv4Address == rhs.ipv4Address) || ((this.ipv4Address != null) && this.ipv4Address.equals(rhs.ipv4Address))))
                && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))));
    }

}
