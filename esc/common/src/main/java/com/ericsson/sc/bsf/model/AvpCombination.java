
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "combination" })
public class AvpCombination
{

    /**
     * Name uniquely identifying the avp-combination.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the avp-combination.")
    private String name;
    @JsonProperty("combination")
    private List<Combination_> combination = new ArrayList<Combination_>();

    /**
     * Name uniquely identifying the avp-combination.
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the avp-combination.
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public AvpCombination withName(String name)
    {
        this.name = name;
        return this;
    }

    @JsonProperty("combination")
    public List<Combination_> getCombination()
    {
        return combination;
    }

    @JsonProperty("combination")
    public void setCombination(List<Combination_> combination)
    {
        this.combination = combination;
    }

    public AvpCombination withCombination(List<Combination_> combination)
    {
        this.combination = combination;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AvpCombination.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("combination");
        sb.append('=');
        sb.append(((this.combination == null) ? "<null>" : this.combination));
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
        result = ((result * 31) + ((this.combination == null) ? 0 : this.combination.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AvpCombination) == false)
        {
            return false;
        }
        AvpCombination rhs = ((AvpCombination) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.combination == rhs.combination) || ((this.combination != null) && this.combination.equals(rhs.combination))));
    }

}
