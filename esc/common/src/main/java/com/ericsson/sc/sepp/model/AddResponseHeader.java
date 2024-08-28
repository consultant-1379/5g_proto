
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfAddResponseHeader;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "value" })
public class AddResponseHeader implements IfAddResponseHeader
{

    /**
     * Name of the header (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the header")
    private String name;
    /**
     * Value of the header. In case the header is already present, the value is
     * appended. (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Value of the header. In case the header is already present, the value is appended.")
    private String value;

    /**
     * Name of the header (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the header (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public AddResponseHeader withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Value of the header. In case the header is already present, the value is
     * appended. (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * Value of the header. In case the header is already present, the value is
     * appended. (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public AddResponseHeader withValue(String value)
    {
        this.value = value;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AddResponseHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
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
        if ((other instanceof AddResponseHeader) == false)
        {
            return false;
        }
        AddResponseHeader rhs = ((AddResponseHeader) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
    }

}
