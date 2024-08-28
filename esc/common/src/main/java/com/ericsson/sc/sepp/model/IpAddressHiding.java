
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Enable/Disable IP Address Hiding functionality based on the targeted nf-type
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "on-fqdn-absence", "on-nf-profile-absence" })
public class IpAddressHiding
{

    /**
     * Action to perform in case IP Address Hiding cannot be applied, given that the
     * FQDN is missing from both the NF profile and all of its services. If no
     * action is configured, then IP Address Hiding shall be performed despite
     * missing FQDN.
     * 
     */
    @JsonProperty("on-fqdn-absence")
    @JsonPropertyDescription("Action to perform in case IP Address Hiding cannot be applied, given that the FQDN is missing from both the NF profile and all of its services. If no action is configured, then IP Address Hiding shall be performed despite missing FQDN.")
    private OnFqdnAbsence onFqdnAbsence;
    /**
     * IP addresses of the configured range to delete from a notification request,
     * in case no full NF profile data is included. These IP addresses shall be
     * assigned to NFs of the specified NF type.
     * 
     */
    @JsonProperty("on-nf-profile-absence")
    @JsonPropertyDescription("IP addresses of the configured range to delete from a notification request, in case no full NF profile data is included. These IP addresses shall be assigned to NFs of the specified NF type.")
    private OnNfProfileAbsence onNfProfileAbsence;

    /**
     * Action to perform in case IP Address Hiding cannot be applied, given that the
     * FQDN is missing from both the NF profile and all of its services. If no
     * action is configured, then IP Address Hiding shall be performed despite
     * missing FQDN.
     * 
     */
    @JsonProperty("on-fqdn-absence")
    public OnFqdnAbsence getOnFqdnAbsence()
    {
        return onFqdnAbsence;
    }

    /**
     * Action to perform in case IP Address Hiding cannot be applied, given that the
     * FQDN is missing from both the NF profile and all of its services. If no
     * action is configured, then IP Address Hiding shall be performed despite
     * missing FQDN.
     * 
     */
    @JsonProperty("on-fqdn-absence")
    public void setOnFqdnAbsence(OnFqdnAbsence onFqdnAbsence)
    {
        this.onFqdnAbsence = onFqdnAbsence;
    }

    public IpAddressHiding withOnFqdnAbsence(OnFqdnAbsence onFqdnAbsence)
    {
        this.onFqdnAbsence = onFqdnAbsence;
        return this;
    }

    /**
     * IP addresses of the configured range to delete from a notification request,
     * in case no full NF profile data is included. These IP addresses shall be
     * assigned to NFs of the specified NF type.
     * 
     */
    @JsonProperty("on-nf-profile-absence")
    public OnNfProfileAbsence getOnNfProfileAbsence()
    {
        return onNfProfileAbsence;
    }

    /**
     * IP addresses of the configured range to delete from a notification request,
     * in case no full NF profile data is included. These IP addresses shall be
     * assigned to NFs of the specified NF type.
     * 
     */
    @JsonProperty("on-nf-profile-absence")
    public void setOnNfProfileAbsence(OnNfProfileAbsence onNfProfileAbsence)
    {
        this.onNfProfileAbsence = onNfProfileAbsence;
    }

    public IpAddressHiding withOnNfProfileAbsence(OnNfProfileAbsence onNfProfileAbsence)
    {
        this.onNfProfileAbsence = onNfProfileAbsence;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IpAddressHiding.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("onFqdnAbsence");
        sb.append('=');
        sb.append(((this.onFqdnAbsence == null) ? "<null>" : this.onFqdnAbsence));
        sb.append(',');
        sb.append("onNfProfileAbsence");
        sb.append('=');
        sb.append(((this.onNfProfileAbsence == null) ? "<null>" : this.onNfProfileAbsence));
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
        result = ((result * 31) + ((this.onFqdnAbsence == null) ? 0 : this.onFqdnAbsence.hashCode()));
        result = ((result * 31) + ((this.onNfProfileAbsence == null) ? 0 : this.onNfProfileAbsence.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IpAddressHiding) == false)
        {
            return false;
        }
        IpAddressHiding rhs = ((IpAddressHiding) other);
        return (((this.onFqdnAbsence == rhs.onFqdnAbsence) || ((this.onFqdnAbsence != null) && this.onFqdnAbsence.equals(rhs.onFqdnAbsence)))
                && ((this.onNfProfileAbsence == rhs.onNfProfileAbsence)
                    || ((this.onNfProfileAbsence != null) && this.onNfProfileAbsence.equals(rhs.onNfProfileAbsence))));
    }

}
