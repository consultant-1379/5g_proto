
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfFromAuthorityHeader;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * If present, route the request based on the authority-header's value
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class FromAuthorityHeader implements IfFromAuthorityHeader
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FromAuthorityHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof FromAuthorityHeader) == false)
        {
            return false;
        }
        FromAuthorityHeader rhs = ((FromAuthorityHeader) other);
        return true;
    }

}
