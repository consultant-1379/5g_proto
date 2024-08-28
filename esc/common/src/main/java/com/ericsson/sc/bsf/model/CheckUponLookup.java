
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration regarding pcf recovery time during traffic.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "enabled", "deletion-upon-lookup" })
public class CheckUponLookup
{

    /**
     * Enable or disable pcf recovery-time.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Enable or disable pcf recovery-time.")
    private Boolean enabled = true;
    /**
     * Enable or disable deletion-upon-lookup based on recovery-time.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    @JsonPropertyDescription("Enable or disable deletion-upon-lookup based on recovery-time.")
    private Boolean deletionUponLookup = true;

    /**
     * Enable or disable pcf recovery-time.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Enable or disable pcf recovery-time.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public CheckUponLookup withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Enable or disable deletion-upon-lookup based on recovery-time.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public Boolean getDeletionUponLookup()
    {
        return deletionUponLookup;
    }

    /**
     * Enable or disable deletion-upon-lookup based on recovery-time.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public void setDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
    }

    public CheckUponLookup withDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(CheckUponLookup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("deletionUponLookup");
        sb.append('=');
        sb.append(((this.deletionUponLookup == null) ? "<null>" : this.deletionUponLookup));
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
        result = ((result * 31) + ((this.deletionUponLookup == null) ? 0 : this.deletionUponLookup.hashCode()));
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
        if ((other instanceof CheckUponLookup) == false)
        {
            return false;
        }
        CheckUponLookup rhs = ((CheckUponLookup) other);
        return (((this.deletionUponLookup == rhs.deletionUponLookup)
                 || ((this.deletionUponLookup != null) && this.deletionUponLookup.equals(rhs.deletionUponLookup)))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
