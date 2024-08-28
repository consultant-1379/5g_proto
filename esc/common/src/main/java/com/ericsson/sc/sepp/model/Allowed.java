
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Allowed headers. If left empty, only the default allowed headers will be
 * permitted. Default allowed headers are defined by the system and described in
 * the CPI.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "additional-allowed-header" })
public class Allowed
{

    /**
     * List of additional permitted headers in the message.
     * 
     */
    @JsonProperty("additional-allowed-header")
    @JsonPropertyDescription("List of additional permitted headers in the message.")
    private List<String> additionalAllowedHeader = new ArrayList<String>();

    /**
     * List of additional permitted headers in the message.
     * 
     */
    @JsonProperty("additional-allowed-header")
    public List<String> getAdditionalAllowedHeader()
    {
        return additionalAllowedHeader;
    }

    /**
     * List of additional permitted headers in the message.
     * 
     */
    @JsonProperty("additional-allowed-header")
    public void setAdditionalAllowedHeader(List<String> additionalAllowedHeader)
    {
        this.additionalAllowedHeader = additionalAllowedHeader;
    }

    public Allowed withAdditionalAllowedHeader(List<String> additionalAllowedHeader)
    {
        this.additionalAllowedHeader = additionalAllowedHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Allowed.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("additionalAllowedHeader");
        sb.append('=');
        sb.append(((this.additionalAllowedHeader == null) ? "<null>" : this.additionalAllowedHeader));
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
        result = ((result * 31) + ((this.additionalAllowedHeader == null) ? 0 : this.additionalAllowedHeader.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Allowed) == false)
        {
            return false;
        }
        Allowed rhs = ((Allowed) other);
        return ((this.additionalAllowedHeader == rhs.additionalAllowedHeader)
                || ((this.additionalAllowedHeader != null) && this.additionalAllowedHeader.equals(rhs.additionalAllowedHeader)));
    }

}
