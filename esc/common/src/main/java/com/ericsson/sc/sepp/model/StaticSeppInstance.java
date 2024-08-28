
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfStaticSeppInstance;
import com.ericsson.sc.glue.NfServiceParams;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "nf-instance-id", "capacity", "priority", "address", "nf-type", "locality", "nf-set-id", "scp-domain" })
public class StaticSeppInstance extends NfServiceParams implements IfStaticSeppInstance
{

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("The NF instance identity")
    private String nfInstanceId;
    /**
     * Specifies the capacity of this static sepp instance
     * 
     */
    @JsonProperty("capacity")
    @JsonPropertyDescription("Specifies the capacity of this static sepp instance")
    private Integer capacity;
    /**
     * The priority of the static sepp instance. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("priority")
    @JsonPropertyDescription("The priority of the static sepp instance. Lower values indicate a higher priority")
    private Integer priority;
    /**
     * Address of the static sepp instance, at least one of FQDN or IPv4 or IPv6
     * address must be given
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Address of the static sepp instance, at least one of FQDN or IPv4 or IPv6 address must be given")
    private Address address;
    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the profile")
    private String name;
    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The type of the NF (according to TS 29.510)")
    private String nfType;
    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    @JsonPropertyDescription("The geographic locality of the NF")
    private String locality;
    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    @JsonPropertyDescription("The set identity of the NF")
    private List<String> nfSetId = new ArrayList<String>();
    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    @JsonPropertyDescription("The SCP domains this NF is associated with")
    private List<String> scpDomain = new ArrayList<String>();

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public StaticSeppInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * Specifies the capacity of this static sepp instance
     * 
     */
    @JsonProperty("capacity")
    public Integer getCapacity()
    {
        return capacity;
    }

    /**
     * Specifies the capacity of this static sepp instance
     * 
     */
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public StaticSeppInstance withCapacity(Integer capacity)
    {
        this.capacity = capacity;
        return this;
    }

    /**
     * The priority of the static sepp instance. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * The priority of the static sepp instance. Lower values indicate a higher
     * priority
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public StaticSeppInstance withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /**
     * Address of the static sepp instance, at least one of FQDN or IPv4 or IPv6
     * address must be given
     * 
     */
    @JsonProperty("address")
    public Address getAddress()
    {
        return address;
    }

    /**
     * Address of the static sepp instance, at least one of FQDN or IPv4 or IPv6
     * address must be given
     * 
     */
    @JsonProperty("address")
    public void setAddress(Address address)
    {
        this.address = address;
    }

    public StaticSeppInstance withAddress(Address address)
    {
        this.address = address;
        return this;
    }

    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticSeppInstance withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public StaticSeppInstance withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    public String getLocality()
    {
        return locality;
    }

    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    public StaticSeppInstance withLocality(String locality)
    {
        this.locality = locality;
        return this;
    }

    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    public List<String> getNfSetId()
    {
        return nfSetId;
    }

    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    public void setNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
    }

    public StaticSeppInstance withNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
        return this;
    }

    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    public List<String> getScpDomain()
    {
        return scpDomain;
    }

    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    public void setScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
    }

    public StaticSeppInstance withScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticSeppInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
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
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("locality");
        sb.append('=');
        sb.append(((this.locality == null) ? "<null>" : this.locality));
        sb.append(',');
        sb.append("nfSetId");
        sb.append('=');
        sb.append(((this.nfSetId == null) ? "<null>" : this.nfSetId));
        sb.append(',');
        sb.append("scpDomain");
        sb.append('=');
        sb.append(((this.scpDomain == null) ? "<null>" : this.scpDomain));
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
        result = ((result * 31) + ((this.scpDomain == null) ? 0 : this.scpDomain.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.locality == null) ? 0 : this.locality.hashCode()));
        result = ((result * 31) + ((this.nfSetId == null) ? 0 : this.nfSetId.hashCode()));
        result = ((result * 31) + ((this.priority == null) ? 0 : this.priority.hashCode()));
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
        if ((other instanceof StaticSeppInstance) == false)
        {
            return false;
        }
        StaticSeppInstance rhs = ((StaticSeppInstance) other);
        return ((((((((((this.scpDomain == rhs.scpDomain) || ((this.scpDomain != null) && this.scpDomain.equals(rhs.scpDomain)))
                       && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                      && ((this.address == rhs.address) || ((this.address != null) && this.address.equals(rhs.address))))
                     && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                    && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                   && ((this.locality == rhs.locality) || ((this.locality != null) && this.locality.equals(rhs.locality))))
                  && ((this.nfSetId == rhs.nfSetId) || ((this.nfSetId != null) && this.nfSetId.equals(rhs.nfSetId))))
                 && ((this.priority == rhs.priority) || ((this.priority != null) && this.priority.equals(rhs.priority))))
                && ((this.capacity == rhs.capacity) || ((this.capacity != null) && this.capacity.equals(rhs.capacity))));
    }

}
