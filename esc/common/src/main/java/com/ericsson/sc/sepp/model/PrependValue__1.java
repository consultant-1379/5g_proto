
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Prepends a string to an element's old value
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "value" })
public class PrependValue__1
{

    /**
     * Specifies the string value to be prepended to the elements's old value
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Specifies the string value to be prepended to the elements's old value")
    private String value;

    /**
     * Specifies the string value to be prepended to the elements's old value
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * Specifies the string value to be prepended to the elements's old value
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public PrependValue__1 withValue(String value)
    {
        this.value = value;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PrependValue__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
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
        result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PrependValue__1) == false)
        {
            return false;
        }
        PrependValue__1 rhs = ((PrependValue__1) other);
        return ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value)));
    }

}
