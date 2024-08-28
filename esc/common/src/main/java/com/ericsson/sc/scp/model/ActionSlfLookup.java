
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Perform SLF interrogation
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "slf-lookup-profile-ref" })
public class ActionSlfLookup
{

    /**
     * Specifies the slf-lookup-profile used for the SLF interrogation
     * 
     */
    @JsonProperty("slf-lookup-profile-ref")
    @JsonPropertyDescription("Specifies the slf-lookup-profile used for the SLF interrogation")
    private String slfLookupProfileRef;

    /**
     * Specifies the slf-lookup-profile used for the SLF interrogation
     * 
     */
    @JsonProperty("slf-lookup-profile-ref")
    public String getSlfLookupProfileRef()
    {
        return slfLookupProfileRef;
    }

    /**
     * Specifies the slf-lookup-profile used for the SLF interrogation
     * 
     */
    @JsonProperty("slf-lookup-profile-ref")
    public void setSlfLookupProfileRef(String slfLookupProfileRef)
    {
        this.slfLookupProfileRef = slfLookupProfileRef;
    }

    public ActionSlfLookup withSlfLookupProfileRef(String slfLookupProfileRef)
    {
        this.slfLookupProfileRef = slfLookupProfileRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionSlfLookup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("slfLookupProfileRef");
        sb.append('=');
        sb.append(((this.slfLookupProfileRef == null) ? "<null>" : this.slfLookupProfileRef));
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
        result = ((result * 31) + ((this.slfLookupProfileRef == null) ? 0 : this.slfLookupProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionSlfLookup) == false)
        {
            return false;
        }
        ActionSlfLookup rhs = ((ActionSlfLookup) other);
        return ((this.slfLookupProfileRef == rhs.slfLookupProfileRef)
                || ((this.slfLookupProfileRef != null) && this.slfLookupProfileRef.equals(rhs.slfLookupProfileRef)));
    }

}
