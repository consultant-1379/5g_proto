
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfTypedScpDomainInfo;
import com.ericsson.sc.glue.NfServiceParams;
import com.ericsson.sc.sepp.model.DiscoveredScpDomainInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "domain", "capacity", "priority", "address" })
public class DiscoveredScpDomainInfo extends NfServiceParams implements IfTypedScpDomainInfo, Comparable<DiscoveredScpDomainInfo>
{

    /**
     * Name identifying the discovered scp domain
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the discovered scp domain")
    private String name;
    /**
     * Domain of the discovered scp
     * 
     */
    @JsonProperty("domain")
    @JsonPropertyDescription("Domain of the discovered scp")
    private String domain;
    /**
     * Specifies the capacity of this service
     * 
     */
    @JsonProperty("capacity")
    @JsonPropertyDescription("Specifies the capacity of this service")
    private Integer capacity;
    /**
     * The priority of the service. Lower values indicate a higher priority
     * 
     */
    @JsonProperty("priority")
    @JsonPropertyDescription("The priority of the service. Lower values indicate a higher priority")
    private Integer priority;
    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Address of the service, at least one of FQDN or IPv4 or IPv6 address must be given")
    private Address address;

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.scp.model.IfDiscoveredScpDomain#getName()
     */
    @Override
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the discovered scp domain
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public DiscoveredScpDomainInfo withName(String name)
    {
        this.name = name;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.scp.model.IfDiscoveredScpDomain#getDomain()
     */
    @Override
    @JsonProperty("domain")
    public String getDomain()
    {
        return domain;
    }

    /**
     * Domain of the discovered scp
     * 
     */
    @JsonProperty("domain")
    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public DiscoveredScpDomainInfo withDomain(String domain)
    {
        this.domain = domain;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.scp.model.IfDiscoveredScpDomain#getCapacity()
     */
    @Override
    @JsonProperty("capacity")
    public Integer getCapacity()
    {
        return capacity;
    }

    /**
     * Specifies the capacity of this service
     * 
     */
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public DiscoveredScpDomainInfo withCapacity(Integer capacity)
    {
        this.capacity = capacity;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.scp.model.IfDiscoveredScpDomain#getPriority()
     */
    @Override
    @JsonProperty("priority")
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * The priority of the service. Lower values indicate a higher priority
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public DiscoveredScpDomainInfo withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.scp.model.IfDiscoveredScpDomain#getAddress()
     */
    @Override
    @JsonProperty("address")
    public Address getAddress()
    {
        return address;
    }

    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given
     * 
     */
    @JsonProperty("address")
    public void setAddress(Address address)
    {
        this.address = address;
    }

    public DiscoveredScpDomainInfo withAddress(Address address)
    {
        this.address = address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DiscoveredScpDomainInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("domain");
        sb.append('=');
        sb.append(((this.domain == null) ? "<null>" : this.domain));
        sb.append(',');
        sb.append("capacity");
        sb.append('=');
        sb.append(((this.capacity == null) ? "<null>" : this.capacity));
        sb.append(',');
        sb.append("priority");
        sb.append('=');
        sb.append(((this.priority == null) ? "<null>" : this.priority));
        sb.append(',');
        sb.append("address");
        sb.append('=');
        sb.append(((this.address == null) ? "<null>" : this.address));
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

    @JsonIgnore
    @Override
    public int compareTo(DiscoveredScpDomainInfo o)
    {
        int result;

        result = this.getName().compareTo(o.getName());

        if (result != 0)
            return result;

        return this.hashCode() - o.hashCode();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.priority == null) ? 0 : this.priority.hashCode()));
        result = ((result * 31) + ((this.domain == null) ? 0 : this.domain.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DiscoveredScpDomainInfo) == false)
        {
            return false;
        }
        DiscoveredScpDomainInfo rhs = ((DiscoveredScpDomainInfo) other);
        return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                   && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                  && ((this.priority == rhs.priority) || ((this.priority != null) && this.priority.equals(rhs.priority))))
                 && ((this.domain == rhs.domain) || ((this.domain != null) && this.domain.equals(rhs.domain))))
                && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))));
    }
}
