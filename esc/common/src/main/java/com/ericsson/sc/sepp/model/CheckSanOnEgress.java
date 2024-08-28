
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * If present, SEPP acting as client checks RP's server certificate matching the
 * SANs presented in it with the configured fqdns of that specific RP and only
 * if they are matched , SEPP will send requests towards RP
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class CheckSanOnEgress
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(CheckSanOnEgress.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof CheckSanOnEgress) == false)
        {
            return false;
        }
        CheckSanOnEgress rhs = ((CheckSanOnEgress) other);
        return true;
    }

}
