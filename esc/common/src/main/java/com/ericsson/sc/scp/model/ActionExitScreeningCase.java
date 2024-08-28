
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Exits from a screening-case and terminates message screening processing
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class ActionExitScreeningCase
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionExitScreeningCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof ActionExitScreeningCase) == false)
        {
            return false;
        }
        ActionExitScreeningCase rhs = ((ActionExitScreeningCase) other);
        return true;
    }

}
