
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Removes an HTTP header from a message
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name" })
public class ActionRemoveHeader
{

    /**
     * Specifies the header to be removed from the request (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Specifies the header to be removed from the request")
    private String name;

    /**
     * Specifies the header to be removed from the request (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Specifies the header to be removed from the request (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ActionRemoveHeader withName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionRemoveHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof ActionRemoveHeader) == false)
        {
            return false;
        }
        ActionRemoveHeader rhs = ((ActionRemoveHeader) other);
        return ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)));
    }

}
