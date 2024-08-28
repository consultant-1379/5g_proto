
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The discovery parameters of the received request to preserve in case of
 * indirect routing. By default no parameters are preserved
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "preserve-selected", "preserve-all" })
public class PreserveDiscParamIfIndirect
{

    /**
     * List of specific discovery parameters to preserve in the request before
     * forwarding it to the next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-selected")
    @JsonPropertyDescription("List of specific discovery parameters to preserve in the request before forwarding it to the next-hop SCP or SEPP")
    private PreserveSelected preserveSelected;
    /**
     * Preserve all discovery parameters in the request before forwarding it to the
     * next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-all")
    @JsonPropertyDescription("Preserve all discovery parameters in the request before forwarding it to the next-hop SCP or SEPP")
    private PreserveAll preserveAll;

    /**
     * List of specific discovery parameters to preserve in the request before
     * forwarding it to the next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-selected")
    public PreserveSelected getPreserveSelected()
    {
        return preserveSelected;
    }

    /**
     * List of specific discovery parameters to preserve in the request before
     * forwarding it to the next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-selected")
    public void setPreserveSelected(PreserveSelected preserveSelected)
    {
        this.preserveSelected = preserveSelected;
    }

    public PreserveDiscParamIfIndirect withPreserveSelected(PreserveSelected preserveSelected)
    {
        this.preserveSelected = preserveSelected;
        return this;
    }

    /**
     * Preserve all discovery parameters in the request before forwarding it to the
     * next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-all")
    public PreserveAll getPreserveAll()
    {
        return preserveAll;
    }

    /**
     * Preserve all discovery parameters in the request before forwarding it to the
     * next-hop SCP or SEPP
     * 
     */
    @JsonProperty("preserve-all")
    public void setPreserveAll(PreserveAll preserveAll)
    {
        this.preserveAll = preserveAll;
    }

    public PreserveDiscParamIfIndirect withPreserveAll(PreserveAll preserveAll)
    {
        this.preserveAll = preserveAll;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PreserveDiscParamIfIndirect.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("preserveSelected");
        sb.append('=');
        sb.append(((this.preserveSelected == null) ? "<null>" : this.preserveSelected));
        sb.append(',');
        sb.append("preserveAll");
        sb.append('=');
        sb.append(((this.preserveAll == null) ? "<null>" : this.preserveAll));
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
        result = ((result * 31) + ((this.preserveAll == null) ? 0 : this.preserveAll.hashCode()));
        result = ((result * 31) + ((this.preserveSelected == null) ? 0 : this.preserveSelected.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PreserveDiscParamIfIndirect) == false)
        {
            return false;
        }
        PreserveDiscParamIfIndirect rhs = ((PreserveDiscParamIfIndirect) other);
        return (((this.preserveAll == rhs.preserveAll) || ((this.preserveAll != null) && this.preserveAll.equals(rhs.preserveAll)))
                && ((this.preserveSelected == rhs.preserveSelected)
                    || ((this.preserveSelected != null) && this.preserveSelected.equals(rhs.preserveSelected))));
    }

}
