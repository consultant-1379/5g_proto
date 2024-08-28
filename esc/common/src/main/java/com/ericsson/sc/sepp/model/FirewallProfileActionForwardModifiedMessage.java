
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Remove not allowed or denied headers and forward the message.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class FirewallProfileActionForwardModifiedMessage
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FirewallProfileActionForwardModifiedMessage.class.getName())
          .append('@')
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append('[');
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
        if ((other instanceof FirewallProfileActionForwardModifiedMessage) == false)
        {
            return false;
        }
        FirewallProfileActionForwardModifiedMessage rhs = ((FirewallProfileActionForwardModifiedMessage) other);
        return true;
    }

}
