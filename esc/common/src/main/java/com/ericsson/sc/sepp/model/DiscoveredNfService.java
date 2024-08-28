
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.glue.NfServiceParams;
import com.ericsson.sc.nfm.model.NfStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-service-id", "name", "api-prefix", "status", "capacity", "priority", "set-id", "address" })
public class DiscoveredNfService extends NfServiceParams implements IfTypedNfService, Comparable<DiscoveredNfService>
{

    /**
     * The NF service identity
     * 
     */
    @JsonProperty("nf-service-id")
    @JsonPropertyDescription("The NF service identity")
    private String nfServiceId;
    /**
     * Name identifying the service supported
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the service supported")
    private String name;
    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    @JsonPropertyDescription("Optional path used to construct the API URI for this service")
    private String apiPrefix;
    /**
     * The status of the service
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("The status of the service")
    private NfStatus status;
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
     * The set identity of the service
     * 
     */
    @JsonProperty("set-id")
    @JsonPropertyDescription("The set identity of the service")
    private List<String> setId = new ArrayList<String>();
    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given
     * 
     */
    @JsonProperty("address")
    @JsonPropertyDescription("Address of the service, at least one of FQDN or IPv4 or IPv6 address must be given")
    private Address address;

    /**
     * The NF service identity
     * 
     */
    @JsonProperty("nf-service-id")
    public String getNfServiceId()
    {
        return nfServiceId;
    }

    /**
     * The NF service identity
     * 
     */
    @JsonProperty("nf-service-id")
    public void setNfServiceId(String nfServiceId)
    {
        this.nfServiceId = nfServiceId;
    }

    public DiscoveredNfService withNfServiceId(String nfServiceId)
    {
        this.nfServiceId = nfServiceId;
        return this;
    }

    /**
     * Name identifying the service supported
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the service supported
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public DiscoveredNfService withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    public String getApiPrefix()
    {
        return apiPrefix;
    }

    /**
     * Optional path used to construct the API URI for this service
     * 
     */
    @JsonProperty("api-prefix")
    public void setApiPrefix(String apiPrefix)
    {
        this.apiPrefix = apiPrefix;
    }

    public DiscoveredNfService withApiPrefix(String apiPrefix)
    {
        this.apiPrefix = apiPrefix;
        return this;
    }

    /**
     * The status of the service
     * 
     */
    @JsonProperty("status")
    public NfStatus getStatus()
    {
        return status;
    }

    /**
     * The status of the service
     * 
     */
    @JsonProperty("status")
    public void setStatus(NfStatus status)
    {
        this.status = status;
    }

    public DiscoveredNfService withStatus(NfStatus status)
    {
        this.status = status;
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

    public DiscoveredNfService withCapacity(Integer capacity)
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

    public DiscoveredNfService withPriority(Integer priority)
    {
        this.priority = priority;
        return this;
    }

    /**
     * The set identity of the service
     * 
     */
    @JsonProperty("set-id")
    public List<String> getSetId()
    {
        return setId;
    }

    /**
     * The set identity of the service
     * 
     */
    @JsonProperty("set-id")
    public void setSetId(List<String> setId)
    {
        this.setId = setId;
    }

    public DiscoveredNfService withSetId(List<String> setId)
    {
        this.setId = setId;
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

    public DiscoveredNfService withAddress(Address address)
    {
        this.address = address;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DiscoveredNfService.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        int baseLength = sb.length();
        String superString = super.toString();
        if (superString != null)
        {
            int contentStart = superString.indexOf('[');
            int contentEnd = superString.lastIndexOf(']');
            if ((contentStart >= 0) && (contentEnd > contentStart))
            {
                sb.append(superString, (contentStart + 1), contentEnd);
            }
            else
            {
                sb.append(superString);
            }
        }
        if (sb.length() > baseLength)
        {
            sb.append(',');
        }
        sb.append("nfServiceId");
        sb.append('=');
        sb.append(((this.nfServiceId == null) ? "<null>" : this.nfServiceId));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("apiPrefix");
        sb.append('=');
        sb.append(((this.apiPrefix == null) ? "<null>" : this.apiPrefix));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("capacity");
        sb.append('=');
        sb.append(((this.capacity == null) ? "<null>" : this.capacity));
        sb.append(',');
        sb.append("priority");
        sb.append('=');
        sb.append(((this.priority == null) ? "<null>" : this.priority));
        sb.append(',');
        sb.append("setId");
        sb.append('=');
        sb.append(((this.setId == null) ? "<null>" : this.setId));
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
    public int compareTo(DiscoveredNfService o)
    {
        return this.getNfServiceId().compareTo(o.getNfServiceId());
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.address == null) ? 0 : this.address.hashCode()));
        result = ((result * 31) + ((this.apiPrefix == null) ? 0 : this.apiPrefix.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.setId == null) ? 0 : this.setId.hashCode()));
        result = ((result * 31) + ((this.priority == null) ? 0 : this.priority.hashCode()));
        result = ((result * 31) + ((this.nfServiceId == null) ? 0 : this.nfServiceId.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        result = ((result * 31) + ((this.capacity == null) ? 0 : this.capacity.hashCode()));
        result = ((result * 31) + super.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DiscoveredNfService) == false)
        {
            return false;
        }
        DiscoveredNfService rhs = ((DiscoveredNfService) other);
        return super.equals(rhs) && (this.address == rhs.address || this.address != null && this.address.equals(rhs.address))
               && (this.name == rhs.name || this.name != null && this.name.equals(rhs.name))
               && (this.apiPrefix == rhs.apiPrefix || this.apiPrefix != null && this.apiPrefix.equals(rhs.apiPrefix))
               && (this.setId == rhs.setId || this.setId != null && this.setId.equals(rhs.setId))
               && (this.priority == rhs.priority || this.priority != null && this.priority.equals(rhs.priority))
               && (this.nfServiceId == rhs.nfServiceId || this.nfServiceId != null && this.nfServiceId.equals(rhs.nfServiceId))
               && (this.capacity == rhs.capacity || this.capacity != null && this.capacity.equals(rhs.capacity))
               && (this.status == rhs.status || this.status != null && this.status.equals(rhs.status));
    }

}
