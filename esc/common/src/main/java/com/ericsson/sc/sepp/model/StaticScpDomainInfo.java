
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfTypedScpDomainInfo;
import com.ericsson.sc.glue.NfServiceParams;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "domain", "capacity", "priority", "address" })
public class StaticScpDomainInfo extends NfServiceParams implements IfTypedScpDomainInfo
{

    /**
     * Name identifying the static scp domain (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the static scp domain")
    private String name;
    /**
     * Domain of the static scp
     * 
     */
    @JsonProperty("domain")
    @JsonPropertyDescription("Domain of the static scp")
    private String domain = "default";
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

    /**
     * Name identifying the static scp domain (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the static scp domain (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticScpDomainInfo withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Domain of the static scp
     * 
     */
    @JsonProperty("domain")
    public String getDomain()
    {
        return domain;
    }

    /**
     * Domain of the static scp
     * 
     */
    @JsonProperty("domain")
    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public StaticScpDomainInfo withDomain(String domain)
    {
        this.domain = domain;
        return this;
    }

    /**
     * Specifies the capacity of this service
     * 
     */
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

    public StaticScpDomainInfo withCapacity(Integer capacity)
    {
        this.capacity = capacity;
        return this;
    }

    /**
     * The priority of the service. Lower values indicate a higher priority
     * 
     */
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

    public StaticScpDomainInfo withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given
     * 
     */
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

    public StaticScpDomainInfo withAddress(Address address)
    {
        this.address = address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticScpDomainInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof StaticScpDomainInfo) == false)
        {
            return false;
        }
        StaticScpDomainInfo rhs = ((StaticScpDomainInfo) other);
        return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                   && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                  && ((this.priority == rhs.priority) || ((this.priority != null) && this.priority.equals(rhs.priority))))
                 && ((this.domain == rhs.domain) || ((this.domain != null) && this.domain.equals(rhs.domain))))
                && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))));
    }

}
