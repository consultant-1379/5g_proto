
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Appends a string stored in a variable to an element's old value
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "variable" })
public class AppendFromVarName__1
{

    /**
     * Specifies the name of the variable from which to fetch the string value to be
     * appended to the element's old value
     * 
     */
    @JsonProperty("variable")
    @JsonPropertyDescription("Specifies the name of the variable from which to fetch the string value to be appended to the element's old value")
    private String variable;

    /**
     * Specifies the name of the variable from which to fetch the string value to be
     * appended to the element's old value
     * 
     */
    @JsonProperty("variable")
    public String getVariable()
    {
        return variable;
    }

    /**
     * Specifies the name of the variable from which to fetch the string value to be
     * appended to the element's old value
     * 
     */
    @JsonProperty("variable")
    public void setVariable(String variable)
    {
        this.variable = variable;
    }

    public AppendFromVarName__1 withVariable(String variable)
    {
        this.variable = variable;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AppendFromVarName__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("variable");
        sb.append('=');
        sb.append(((this.variable == null) ? "<null>" : this.variable));
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
        result = ((result * 31) + ((this.variable == null) ? 0 : this.variable.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AppendFromVarName__1) == false)
        {
            return false;
        }
        AppendFromVarName__1 rhs = ((AppendFromVarName__1) other);
        return ((this.variable == rhs.variable) || ((this.variable != null) && this.variable.equals(rhs.variable)));
    }

}
