
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines data to be used for the N32 handshake procedure between the SEPPs in
 * two PLMNs.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "enabled", "nf-pool-ref", "allow-plmn", "security-negotiation-data" })
public class N32C__1
{

    /**
     * A switch that allows the operator to enable or disable N32-c support for the
     * roaming partner
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("A switch that allows the operator to enable or disable N32-c support for the roaming partner")
    private Boolean enabled = false;
    /**
     * SEPP NF Pool that belongs to the Roaming Partner. (Required)
     * 
     */
    @JsonProperty("nf-pool-ref")
    @JsonPropertyDescription("SEPP NF Pool that belongs to the Roaming Partner.")
    private String nfPoolRef;
    /**
     * Definition of the primary and additional PLMN Ids of the roaming partner's
     * PLMN. (Required)
     * 
     */
    @JsonProperty("allow-plmn")
    @JsonPropertyDescription("Definition of the primary and additional PLMN Ids of the roaming partner's PLMN.")
    private AllowPlmn allowPlmn;
    /**
     * Table containing the state data of N32-C handshake procedure per responding
     * SEPP.
     * 
     */
    @JsonProperty("security-negotiation-data")
    @JsonPropertyDescription("Table containing the state data of N32-C handshake procedure per responding SEPP.")
    private List<SecurityNegotiationDatum> securityNegotiationData = new ArrayList<SecurityNegotiationDatum>();

    /**
     * A switch that allows the operator to enable or disable N32-c support for the
     * roaming partner
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * A switch that allows the operator to enable or disable N32-c support for the
     * roaming partner
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public N32C__1 withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * SEPP NF Pool that belongs to the Roaming Partner. (Required)
     * 
     */
    @JsonProperty("nf-pool-ref")
    public String getNfPoolRef()
    {
        return nfPoolRef;
    }

    /**
     * SEPP NF Pool that belongs to the Roaming Partner. (Required)
     * 
     */
    @JsonProperty("nf-pool-ref")
    public void setNfPoolRef(String nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
    }

    public N32C__1 withNfPoolRef(String nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
        return this;
    }

    /**
     * Definition of the primary and additional PLMN Ids of the roaming partner's
     * PLMN. (Required)
     * 
     */
    @JsonProperty("allow-plmn")
    public AllowPlmn getAllowPlmn()
    {
        return allowPlmn;
    }

    /**
     * Definition of the primary and additional PLMN Ids of the roaming partner's
     * PLMN. (Required)
     * 
     */
    @JsonProperty("allow-plmn")
    public void setAllowPlmn(AllowPlmn allowPlmn)
    {
        this.allowPlmn = allowPlmn;
    }

    public N32C__1 withAllowPlmn(AllowPlmn allowPlmn)
    {
        this.allowPlmn = allowPlmn;
        return this;
    }

    /**
     * Table containing the state data of N32-C handshake procedure per responding
     * SEPP.
     * 
     */
    @JsonProperty("security-negotiation-data")
    public List<SecurityNegotiationDatum> getSecurityNegotiationData()
    {
        return securityNegotiationData;
    }

    /**
     * Table containing the state data of N32-C handshake procedure per responding
     * SEPP.
     * 
     */
    @JsonProperty("security-negotiation-data")
    public void setSecurityNegotiationData(List<SecurityNegotiationDatum> securityNegotiationData)
    {
        this.securityNegotiationData = securityNegotiationData;
    }

    public N32C__1 withSecurityNegotiationData(List<SecurityNegotiationDatum> securityNegotiationData)
    {
        this.securityNegotiationData = securityNegotiationData;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(N32C__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("nfPoolRef");
        sb.append('=');
        sb.append(((this.nfPoolRef == null) ? "<null>" : this.nfPoolRef));
        sb.append(',');
        sb.append("allowPlmn");
        sb.append('=');
        sb.append(((this.allowPlmn == null) ? "<null>" : this.allowPlmn));
        sb.append(',');
        sb.append("securityNegotiationData");
        sb.append('=');
        sb.append(((this.securityNegotiationData == null) ? "<null>" : this.securityNegotiationData));
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
        result = ((result * 31) + ((this.allowPlmn == null) ? 0 : this.allowPlmn.hashCode()));
        result = ((result * 31) + ((this.nfPoolRef == null) ? 0 : this.nfPoolRef.hashCode()));
        result = ((result * 31) + ((this.securityNegotiationData == null) ? 0 : this.securityNegotiationData.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof N32C__1) == false)
        {
            return false;
        }
        N32C__1 rhs = ((N32C__1) other);
        return (((((this.allowPlmn == rhs.allowPlmn) || ((this.allowPlmn != null) && this.allowPlmn.equals(rhs.allowPlmn)))
                  && ((this.nfPoolRef == rhs.nfPoolRef) || ((this.nfPoolRef != null) && this.nfPoolRef.equals(rhs.nfPoolRef))))
                 && ((this.securityNegotiationData == rhs.securityNegotiationData)
                     || ((this.securityNegotiationData != null) && this.securityNegotiationData.equals(rhs.securityNegotiationData))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
