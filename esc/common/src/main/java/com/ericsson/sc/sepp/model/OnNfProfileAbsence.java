
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * IP addresses of the configured range to delete from a notification request,
 * in case no full NF profile data is included. These IP addresses shall be
 * assigned to NFs of the specified NF type.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "remove-ipv4-address-range", "remove-ipv6-address-range" })
public class OnNfProfileAbsence
{

    /**
     * The IPv4 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv4-address-range")
    @JsonPropertyDescription("The IPv4 addresses to delete from a notification request.")
    private List<String> removeIpv4AddressRange = new ArrayList<String>();
    /**
     * The IPv6 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv6-address-range")
    @JsonPropertyDescription("The IPv6 addresses to delete from a notification request.")
    private List<String> removeIpv6AddressRange = new ArrayList<String>();

    /**
     * The IPv4 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv4-address-range")
    public List<String> getRemoveIpv4AddressRange()
    {
        return removeIpv4AddressRange;
    }

    /**
     * The IPv4 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv4-address-range")
    public void setRemoveIpv4AddressRange(List<String> removeIpv4AddressRange)
    {
        this.removeIpv4AddressRange = removeIpv4AddressRange;
    }

    public OnNfProfileAbsence withRemoveIpv4AddressRange(List<String> removeIpv4AddressRange)
    {
        this.removeIpv4AddressRange = removeIpv4AddressRange;
        return this;
    }

    /**
     * The IPv6 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv6-address-range")
    public List<String> getRemoveIpv6AddressRange()
    {
        return removeIpv6AddressRange;
    }

    /**
     * The IPv6 addresses to delete from a notification request.
     * 
     */
    @JsonProperty("remove-ipv6-address-range")
    public void setRemoveIpv6AddressRange(List<String> removeIpv6AddressRange)
    {
        this.removeIpv6AddressRange = removeIpv6AddressRange;
    }

    public OnNfProfileAbsence withRemoveIpv6AddressRange(List<String> removeIpv6AddressRange)
    {
        this.removeIpv6AddressRange = removeIpv6AddressRange;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OnNfProfileAbsence.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("removeIpv4AddressRange");
        sb.append('=');
        sb.append(((this.removeIpv4AddressRange == null) ? "<null>" : this.removeIpv4AddressRange));
        sb.append(',');
        sb.append("removeIpv6AddressRange");
        sb.append('=');
        sb.append(((this.removeIpv6AddressRange == null) ? "<null>" : this.removeIpv6AddressRange));
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
        result = ((result * 31) + ((this.removeIpv4AddressRange == null) ? 0 : this.removeIpv4AddressRange.hashCode()));
        result = ((result * 31) + ((this.removeIpv6AddressRange == null) ? 0 : this.removeIpv6AddressRange.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OnNfProfileAbsence) == false)
        {
            return false;
        }
        OnNfProfileAbsence rhs = ((OnNfProfileAbsence) other);
        return (((this.removeIpv4AddressRange == rhs.removeIpv4AddressRange)
                 || ((this.removeIpv4AddressRange != null) && this.removeIpv4AddressRange.equals(rhs.removeIpv4AddressRange)))
                && ((this.removeIpv6AddressRange == rhs.removeIpv6AddressRange)
                    || ((this.removeIpv6AddressRange != null) && this.removeIpv6AddressRange.equals(rhs.removeIpv6AddressRange))));
    }

}
