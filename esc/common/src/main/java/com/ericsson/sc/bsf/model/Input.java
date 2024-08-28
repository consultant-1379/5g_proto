
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Contains the action input parameters from Yang model.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-bsf:datacenter" })
public class Input
{

    /**
     * Replication strategy related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:datacenter")
    @JsonPropertyDescription("Replication strategy related properties")
    private List<EricssonBsfDatacenter> ericssonBsfDatacenter = new ArrayList<EricssonBsfDatacenter>();

    /**
     * Replication strategy related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:datacenter")
    public List<EricssonBsfDatacenter> getEricssonBsfDatacenter()
    {
        return ericssonBsfDatacenter;
    }

    /**
     * Replication strategy related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:datacenter")
    public void setEricssonBsfDatacenter(List<EricssonBsfDatacenter> ericssonBsfDatacenter)
    {
        this.ericssonBsfDatacenter = ericssonBsfDatacenter;
    }

    public Input withEricssonBsfDatacenter(List<EricssonBsfDatacenter> ericssonBsfDatacenter)
    {
        this.ericssonBsfDatacenter = ericssonBsfDatacenter;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Input.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ericssonBsfDatacenter");
        sb.append('=');
        sb.append(((this.ericssonBsfDatacenter == null) ? "<null>" : this.ericssonBsfDatacenter));
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
        result = ((result * 31) + ((this.ericssonBsfDatacenter == null) ? 0 : this.ericssonBsfDatacenter.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Input) == false)
        {
            return false;
        }
        Input rhs = ((Input) other);
        return ((this.ericssonBsfDatacenter == rhs.ericssonBsfDatacenter)
                || ((this.ericssonBsfDatacenter != null) && this.ericssonBsfDatacenter.equals(rhs.ericssonBsfDatacenter)));
    }

}
