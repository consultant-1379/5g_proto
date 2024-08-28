
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "scp-domain-ref", "scp-prefix", "service-address-ref" })
public class ScpDomainInfo
{

    /**
     * Reference to a defined SCP domain
     * 
     */
    @JsonProperty("scp-domain-ref")
    @JsonPropertyDescription("Reference to a defined SCP domain")
    private String scpDomainRef;
    /**
     * Optional string used to construct the apiRoot of the next hop SCP
     * 
     */
    @JsonProperty("scp-prefix")
    @JsonPropertyDescription("Optional string used to construct the apiRoot of the next hop SCP")
    private String scpPrefix;
    /**
     * Reference to a defined service-address
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Reference to a defined service-address")
    private List<String> serviceAddressRef = new ArrayList<String>();

    /**
     * Reference to a defined SCP domain
     * 
     */
    @JsonProperty("scp-domain-ref")
    public String getScpDomainRef()
    {
        return scpDomainRef;
    }

    /**
     * Reference to a defined SCP domain
     * 
     */
    @JsonProperty("scp-domain-ref")
    public void setScpDomainRef(String scpDomainRef)
    {
        this.scpDomainRef = scpDomainRef;
    }

    public ScpDomainInfo withScpDomainRef(String scpDomainRef)
    {
        this.scpDomainRef = scpDomainRef;
        return this;
    }

    /**
     * Optional string used to construct the apiRoot of the next hop SCP
     * 
     */
    @JsonProperty("scp-prefix")
    public String getScpPrefix()
    {
        return scpPrefix;
    }

    /**
     * Optional string used to construct the apiRoot of the next hop SCP
     * 
     */
    @JsonProperty("scp-prefix")
    public void setScpPrefix(String scpPrefix)
    {
        this.scpPrefix = scpPrefix;
    }

    public ScpDomainInfo withScpPrefix(String scpPrefix)
    {
        this.scpPrefix = scpPrefix;
        return this;
    }

    /**
     * Reference to a defined service-address
     * 
     */
    @JsonProperty("service-address-ref")
    public List<String> getServiceAddressRef()
    {
        return serviceAddressRef;
    }

    /**
     * Reference to a defined service-address
     * 
     */
    @JsonProperty("service-address-ref")
    public void setServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
    }

    public ScpDomainInfo withServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScpDomainInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("scpDomainRef");
        sb.append('=');
        sb.append(((this.scpDomainRef == null) ? "<null>" : this.scpDomainRef));
        sb.append(',');
        sb.append("scpPrefix");
        sb.append('=');
        sb.append(((this.scpPrefix == null) ? "<null>" : this.scpPrefix));
        sb.append(',');
        sb.append("serviceAddressRef");
        sb.append('=');
        sb.append(((this.serviceAddressRef == null) ? "<null>" : this.serviceAddressRef));
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
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        result = ((result * 31) + ((this.scpDomainRef == null) ? 0 : this.scpDomainRef.hashCode()));
        result = ((result * 31) + ((this.scpPrefix == null) ? 0 : this.scpPrefix.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScpDomainInfo) == false)
        {
            return false;
        }
        ScpDomainInfo rhs = ((ScpDomainInfo) other);
        return ((((this.serviceAddressRef == rhs.serviceAddressRef)
                  || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef)))
                 && ((this.scpDomainRef == rhs.scpDomainRef) || ((this.scpDomainRef != null) && this.scpDomainRef.equals(rhs.scpDomainRef))))
                && ((this.scpPrefix == rhs.scpPrefix) || ((this.scpPrefix != null) && this.scpPrefix.equals(rhs.scpPrefix))));
    }

}
