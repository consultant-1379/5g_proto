
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "status-code-match-condition" })
public class PcfReselectionCase
{

    /**
     * Name of the pcf-reselection. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the pcf-reselection.")
    private String name;
    /**
     * The configurable diameter status codes that triggers the reroute (Required)
     * 
     */
    @JsonProperty("status-code-match-condition")
    @JsonPropertyDescription("The configurable diameter status codes that triggers the reroute")
    private List<Integer> statusCodeMatchCondition = new ArrayList<Integer>();

    /**
     * Name of the pcf-reselection. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the pcf-reselection. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public PcfReselectionCase withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The configurable diameter status codes that triggers the reroute (Required)
     * 
     */
    @JsonProperty("status-code-match-condition")
    public List<Integer> getStatusCodeMatchCondition()
    {
        return statusCodeMatchCondition;
    }

    /**
     * The configurable diameter status codes that triggers the reroute (Required)
     * 
     */
    @JsonProperty("status-code-match-condition")
    public void setStatusCodeMatchCondition(List<Integer> statusCodeMatchCondition)
    {
        this.statusCodeMatchCondition = statusCodeMatchCondition;
    }

    public PcfReselectionCase withStatusCodeMatchCondition(List<Integer> statusCodeMatchCondition)
    {
        this.statusCodeMatchCondition = statusCodeMatchCondition;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PcfReselectionCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("statusCodeMatchCondition");
        sb.append('=');
        sb.append(((this.statusCodeMatchCondition == null) ? "<null>" : this.statusCodeMatchCondition));
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.statusCodeMatchCondition == null) ? 0 : this.statusCodeMatchCondition.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PcfReselectionCase) == false)
        {
            return false;
        }
        PcfReselectionCase rhs = ((PcfReselectionCase) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.statusCodeMatchCondition == rhs.statusCodeMatchCondition)
                    || ((this.statusCodeMatchCondition != null) && this.statusCodeMatchCondition.equals(rhs.statusCodeMatchCondition))));
    }

}
