
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfActionDropMessageBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Drops an http request message and the HTTP/2 stream is reset gracefully
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class ActionDropMessage implements IfActionDropMessageBase
{

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionDropMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof ActionDropMessage) == false)
        {
            return false;
        }
        ActionDropMessage rhs = ((ActionDropMessage) other);
        return true;
    }

}
