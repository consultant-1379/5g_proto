
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Exits from a screening-case and terminates message screening processing
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class ActionExitScreeningCase__1
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionExitScreeningCase__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionExitScreeningCase__1) == false)
        {
            return false;
        }
        ActionExitScreeningCase__1 rhs = ((ActionExitScreeningCase__1) other);
        return true;
    }

}
