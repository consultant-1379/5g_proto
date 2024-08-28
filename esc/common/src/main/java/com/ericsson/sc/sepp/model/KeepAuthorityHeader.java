
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfKeepAuthorityHeader;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * If present, the authority header of request remains unchanged
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class KeepAuthorityHeader implements IfKeepAuthorityHeader
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(KeepAuthorityHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof KeepAuthorityHeader) == false)
        {
            return false;
        }
        KeepAuthorityHeader rhs = ((KeepAuthorityHeader) other);
        return true;
    }

}
