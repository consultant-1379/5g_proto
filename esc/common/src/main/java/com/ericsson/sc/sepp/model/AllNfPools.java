
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfAllNfPools;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * If present , it applies egress vtap configuration to all nf-pools
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class AllNfPools implements IfAllNfPools
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AllNfPools.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof AllNfPools) == false)
        {
            return false;
        }
        AllNfPools rhs = ((AllNfPools) other);
        return true;
    }

}
