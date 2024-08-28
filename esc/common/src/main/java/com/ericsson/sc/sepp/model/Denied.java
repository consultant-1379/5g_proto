
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Denied headers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "denied-header" })
public class Denied
{

    /**
     * List of headers prohibited in the message. (Required)
     * 
     */
    @JsonProperty("denied-header")
    @JsonPropertyDescription("List of headers prohibited in the message.")
    private List<String> deniedHeader = new ArrayList<String>();

    /**
     * List of headers prohibited in the message. (Required)
     * 
     */
    @JsonProperty("denied-header")
    public List<String> getDeniedHeader()
    {
        return deniedHeader;
    }

    /**
     * List of headers prohibited in the message. (Required)
     * 
     */
    @JsonProperty("denied-header")
    public void setDeniedHeader(List<String> deniedHeader)
    {
        this.deniedHeader = deniedHeader;
    }

    public Denied withDeniedHeader(List<String> deniedHeader)
    {
        this.deniedHeader = deniedHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Denied.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("deniedHeader");
        sb.append('=');
        sb.append(((this.deniedHeader == null) ? "<null>" : this.deniedHeader));
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
        result = ((result * 31) + ((this.deniedHeader == null) ? 0 : this.deniedHeader.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Denied) == false)
        {
            return false;
        }
        Denied rhs = ((Denied) other);
        return ((this.deniedHeader == rhs.deniedHeader) || ((this.deniedHeader != null) && this.deniedHeader.equals(rhs.deniedHeader)));
    }

}
