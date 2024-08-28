
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfVtap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the required data for traffic tapping
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "enabled", "vtap-configuration" })
public class Vtap implements IfVtap
{

    /**
     * A switch that allows the operator to enable or disable traffic tapping in a
     * global level
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("A switch that allows the operator to enable or disable traffic tapping in a global level")
    private Boolean enabled = true;
    /**
     * Defines the required settings for traffic tapping
     * 
     */
    @JsonProperty("vtap-configuration")
    @JsonPropertyDescription("Defines the required settings for traffic tapping")
    private VtapConfiguration vtapConfiguration;

    /**
     * A switch that allows the operator to enable or disable traffic tapping in a
     * global level
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * A switch that allows the operator to enable or disable traffic tapping in a
     * global level
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Vtap withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Defines the required settings for traffic tapping
     * 
     */
    @JsonProperty("vtap-configuration")
    public VtapConfiguration getVtapConfiguration()
    {
        return vtapConfiguration;
    }

    /**
     * Defines the required settings for traffic tapping
     * 
     */
    @JsonProperty("vtap-configuration")
    public void setVtapConfiguration(VtapConfiguration vtapConfiguration)
    {
        this.vtapConfiguration = vtapConfiguration;
    }

    public Vtap withVtapConfiguration(VtapConfiguration vtapConfiguration)
    {
        this.vtapConfiguration = vtapConfiguration;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Vtap.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("vtapConfiguration");
        sb.append('=');
        sb.append(((this.vtapConfiguration == null) ? "<null>" : this.vtapConfiguration));
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
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.vtapConfiguration == null) ? 0 : this.vtapConfiguration.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Vtap) == false)
        {
            return false;
        }
        Vtap rhs = ((Vtap) other);
        return (((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled)))
                && ((this.vtapConfiguration == rhs.vtapConfiguration)
                    || ((this.vtapConfiguration != null) && this.vtapConfiguration.equals(rhs.vtapConfiguration))));
    }

}
