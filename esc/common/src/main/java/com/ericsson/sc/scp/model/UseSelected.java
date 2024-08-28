
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Explicitly selected discovery parameters from the received request that are
 * used in the delegated discovery
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name" })
public class UseSelected
{

    /**
     * The selected discovery parameter to use in the delegated discovery
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The selected discovery parameter to use in the delegated discovery")
    private List<String> name = new ArrayList<String>();

    /**
     * The selected discovery parameter to use in the delegated discovery
     * 
     */
    @JsonProperty("name")
    public List<String> getName()
    {
        return name;
    }

    /**
     * The selected discovery parameter to use in the delegated discovery
     * 
     */
    @JsonProperty("name")
    public void setName(List<String> name)
    {
        this.name = name;
    }

    public UseSelected withName(List<String> name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(UseSelected.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
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
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof UseSelected) == false)
        {
            return false;
        }
        UseSelected rhs = ((UseSelected) other);
        return ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)));
    }

}
