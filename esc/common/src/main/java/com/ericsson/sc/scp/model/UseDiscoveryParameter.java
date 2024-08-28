
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Discovery parameters from the received request that are used in the delegated
 * discovery
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "use-selected", "use-all" })
public class UseDiscoveryParameter
{

    /**
     * Explicitly selected discovery parameters from the received request that are
     * used in the delegated discovery
     * 
     */
    @JsonProperty("use-selected")
    @JsonPropertyDescription("Explicitly selected discovery parameters from the received request that are used in the delegated discovery")
    private UseSelected useSelected;
    /**
     * Use all discovery parameters present in the received request when performing
     * delegated discovery.
     * 
     */
    @JsonProperty("use-all")
    @JsonPropertyDescription("Use all discovery parameters present in the received request when performing delegated discovery.")
    private UseAll useAll;

    /**
     * Explicitly selected discovery parameters from the received request that are
     * used in the delegated discovery
     * 
     */
    @JsonProperty("use-selected")
    public UseSelected getUseSelected()
    {
        return useSelected;
    }

    /**
     * Explicitly selected discovery parameters from the received request that are
     * used in the delegated discovery
     * 
     */
    @JsonProperty("use-selected")
    public void setUseSelected(UseSelected useSelected)
    {
        this.useSelected = useSelected;
    }

    public UseDiscoveryParameter withUseSelected(UseSelected useSelected)
    {
        this.useSelected = useSelected;
        return this;
    }

    /**
     * Use all discovery parameters present in the received request when performing
     * delegated discovery.
     * 
     */
    @JsonProperty("use-all")
    public UseAll getUseAll()
    {
        return useAll;
    }

    /**
     * Use all discovery parameters present in the received request when performing
     * delegated discovery.
     * 
     */
    @JsonProperty("use-all")
    public void setUseAll(UseAll useAll)
    {
        this.useAll = useAll;
    }

    public UseDiscoveryParameter withUseAll(UseAll useAll)
    {
        this.useAll = useAll;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(UseDiscoveryParameter.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("useSelected");
        sb.append('=');
        sb.append(((this.useSelected == null) ? "<null>" : this.useSelected));
        sb.append(',');
        sb.append("useAll");
        sb.append('=');
        sb.append(((this.useAll == null) ? "<null>" : this.useAll));
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
        result = ((result * 31) + ((this.useAll == null) ? 0 : this.useAll.hashCode()));
        result = ((result * 31) + ((this.useSelected == null) ? 0 : this.useSelected.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof UseDiscoveryParameter) == false)
        {
            return false;
        }
        UseDiscoveryParameter rhs = ((UseDiscoveryParameter) other);
        return (((this.useAll == rhs.useAll) || ((this.useAll != null) && this.useAll.equals(rhs.useAll)))
                && ((this.useSelected == rhs.useSelected) || ((this.useSelected != null) && this.useSelected.equals(rhs.useSelected))));
    }

}
