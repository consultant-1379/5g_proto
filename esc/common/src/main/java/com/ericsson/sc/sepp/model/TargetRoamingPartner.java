
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route via the referenced roaming-partner
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "roaming-partner-ref" })
public class TargetRoamingPartner
{

    /**
     * Name of the roaming-partner
     * 
     */
    @JsonProperty("roaming-partner-ref")
    @JsonPropertyDescription("Name of the roaming-partner")
    private String roamingPartnerRef;

    /**
     * Name of the roaming-partner
     * 
     */
    @JsonProperty("roaming-partner-ref")
    public String getRoamingPartnerRef()
    {
        return roamingPartnerRef;
    }

    /**
     * Name of the roaming-partner
     * 
     */
    @JsonProperty("roaming-partner-ref")
    public void setRoamingPartnerRef(String roamingPartnerRef)
    {
        this.roamingPartnerRef = roamingPartnerRef;
    }

    public TargetRoamingPartner withRoamingPartnerRef(String roamingPartnerRef)
    {
        this.roamingPartnerRef = roamingPartnerRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TargetRoamingPartner.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("roamingPartnerRef");
        sb.append('=');
        sb.append(((this.roamingPartnerRef == null) ? "<null>" : this.roamingPartnerRef));
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
        result = ((result * 31) + ((this.roamingPartnerRef == null) ? 0 : this.roamingPartnerRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TargetRoamingPartner) == false)
        {
            return false;
        }
        TargetRoamingPartner rhs = ((TargetRoamingPartner) other);
        return ((this.roamingPartnerRef == rhs.roamingPartnerRef)
                || ((this.roamingPartnerRef != null) && this.roamingPartnerRef.equals(rhs.roamingPartnerRef)));
    }

}
