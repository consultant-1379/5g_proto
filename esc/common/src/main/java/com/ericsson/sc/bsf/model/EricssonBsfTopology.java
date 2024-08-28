
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Schema topology related properties
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "datacenter" })
public class EricssonBsfTopology
{

    /**
     * Replication strategy related properties
     * 
     */
    @JsonProperty("datacenter")
    @JsonPropertyDescription("Replication strategy related properties")
    private List<Datacenter> datacenter = new ArrayList<Datacenter>();

    /**
     * Replication strategy related properties
     * 
     */
    @JsonProperty("datacenter")
    public List<Datacenter> getDatacenter()
    {
        return datacenter;
    }

    /**
     * Replication strategy related properties
     * 
     */
    @JsonProperty("datacenter")
    public void setDatacenter(List<Datacenter> datacenter)
    {
        this.datacenter = datacenter;
    }

    public EricssonBsfTopology withDatacenter(List<Datacenter> datacenter)
    {
        this.datacenter = datacenter;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfTopology.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("datacenter");
        sb.append('=');
        sb.append(((this.datacenter == null) ? "<null>" : this.datacenter));
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
        result = ((result * 31) + ((this.datacenter == null) ? 0 : this.datacenter.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfTopology) == false)
        {
            return false;
        }
        EricssonBsfTopology rhs = ((EricssonBsfTopology) other);
        return ((this.datacenter == rhs.datacenter) || ((this.datacenter != null) && this.datacenter.equals(rhs.datacenter)));
    }

}
