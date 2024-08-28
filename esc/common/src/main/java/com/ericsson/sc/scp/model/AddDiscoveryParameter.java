
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "value", "from-var-name" })
public class AddDiscoveryParameter
{

    /**
     * The discovery parameter name (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The discovery parameter name")
    private String name;
    /**
     * The specific value of the discovery parameter. Strings as well as valid JSON
     * data structures are allowed
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The specific value of the discovery parameter. Strings as well as valid JSON data structures are allowed")
    private String value;
    /**
     * The name of the variable that holds the discovery parameter's value
     * 
     */
    @JsonProperty("from-var-name")
    @JsonPropertyDescription("The name of the variable that holds the discovery parameter's value")
    private String fromVarName;

    /**
     * The discovery parameter name (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The discovery parameter name (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public AddDiscoveryParameter withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The specific value of the discovery parameter. Strings as well as valid JSON
     * data structures are allowed
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * The specific value of the discovery parameter. Strings as well as valid JSON
     * data structures are allowed
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public AddDiscoveryParameter withValue(String value)
    {
        this.value = value;
        return this;
    }

    /**
     * The name of the variable that holds the discovery parameter's value
     * 
     */
    @JsonProperty("from-var-name")
    public String getFromVarName()
    {
        return fromVarName;
    }

    /**
     * The name of the variable that holds the discovery parameter's value
     * 
     */
    @JsonProperty("from-var-name")
    public void setFromVarName(String fromVarName)
    {
        this.fromVarName = fromVarName;
    }

    public AddDiscoveryParameter withFromVarName(String fromVarName)
    {
        this.fromVarName = fromVarName;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AddDiscoveryParameter.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
        sb.append(',');
        sb.append("fromVarName");
        sb.append('=');
        sb.append(((this.fromVarName == null) ? "<null>" : this.fromVarName));
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
        result = ((result * 31) + ((this.fromVarName == null) ? 0 : this.fromVarName.hashCode()));
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
        if ((other instanceof AddDiscoveryParameter) == false)
        {
            return false;
        }
        AddDiscoveryParameter rhs = ((AddDiscoveryParameter) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.fromVarName == rhs.fromVarName) || ((this.fromVarName != null) && this.fromVarName.equals(rhs.fromVarName))))
                && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
    }

}
