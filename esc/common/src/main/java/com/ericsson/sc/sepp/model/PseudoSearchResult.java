
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the data of the nrf-discovery response
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validity-period", "nf-profile" })
public class PseudoSearchResult
{

    /**
     * The time in seconds during which the discovery result is considered valid and
     * can be cached by the NF service consumer (Required)
     * 
     */
    @JsonProperty("validity-period")
    @JsonPropertyDescription("The time in seconds during which the discovery result is considered valid and can be cached by the NF service consumer")
    private Integer validityPeriod;
    /**
     * Profile consisting of general parameters of the network function, included in
     * the NRF discovery response (Required)
     * 
     */
    @JsonProperty("nf-profile")
    @JsonPropertyDescription("Profile consisting of general parameters of the network function, included in the NRF discovery response")
    private NfProfile nfProfile;

    /**
     * The time in seconds during which the discovery result is considered valid and
     * can be cached by the NF service consumer (Required)
     * 
     */
    @JsonProperty("validity-period")
    public Integer getValidityPeriod()
    {
        return validityPeriod;
    }

    /**
     * The time in seconds during which the discovery result is considered valid and
     * can be cached by the NF service consumer (Required)
     * 
     */
    @JsonProperty("validity-period")
    public void setValidityPeriod(Integer validityPeriod)
    {
        this.validityPeriod = validityPeriod;
    }

    public PseudoSearchResult withValidityPeriod(Integer validityPeriod)
    {
        this.validityPeriod = validityPeriod;
        return this;
    }

    /**
     * Profile consisting of general parameters of the network function, included in
     * the NRF discovery response (Required)
     * 
     */
    @JsonProperty("nf-profile")
    public NfProfile getNfProfile()
    {
        return nfProfile;
    }

    /**
     * Profile consisting of general parameters of the network function, included in
     * the NRF discovery response (Required)
     * 
     */
    @JsonProperty("nf-profile")
    public void setNfProfile(NfProfile nfProfile)
    {
        this.nfProfile = nfProfile;
    }

    public PseudoSearchResult withNfProfile(NfProfile nfProfile)
    {
        this.nfProfile = nfProfile;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PseudoSearchResult.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("validityPeriod");
        sb.append('=');
        sb.append(((this.validityPeriod == null) ? "<null>" : this.validityPeriod));
        sb.append(',');
        sb.append("nfProfile");
        sb.append('=');
        sb.append(((this.nfProfile == null) ? "<null>" : this.nfProfile));
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
        result = ((result * 31) + ((this.validityPeriod == null) ? 0 : this.validityPeriod.hashCode()));
        result = ((result * 31) + ((this.nfProfile == null) ? 0 : this.nfProfile.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PseudoSearchResult) == false)
        {
            return false;
        }
        PseudoSearchResult rhs = ((PseudoSearchResult) other);
        return (((this.validityPeriod == rhs.validityPeriod) || ((this.validityPeriod != null) && this.validityPeriod.equals(rhs.validityPeriod)))
                && ((this.nfProfile == rhs.nfProfile) || ((this.nfProfile != null) && this.nfProfile.equals(rhs.nfProfile))));
    }

}
