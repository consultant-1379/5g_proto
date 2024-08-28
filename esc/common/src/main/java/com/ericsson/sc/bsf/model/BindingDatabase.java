
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Database related properties and actions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "binding-timeout" })
public class BindingDatabase
{

    /**
     * Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    @JsonPropertyDescription("Session binding expiration timer in hours")
    private Integer bindingTimeout = 720;

    /**
     * Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    public Integer getBindingTimeout()
    {
        return bindingTimeout;
    }

    /**
     * Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    public void setBindingTimeout(Integer bindingTimeout)
    {
        this.bindingTimeout = bindingTimeout;
    }

    public BindingDatabase withBindingTimeout(Integer bindingTimeout)
    {
        this.bindingTimeout = bindingTimeout;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BindingDatabase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bindingTimeout");
        sb.append('=');
        sb.append(((this.bindingTimeout == null) ? "<null>" : this.bindingTimeout));
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
        result = ((result * 31) + ((this.bindingTimeout == null) ? 0 : this.bindingTimeout.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof BindingDatabase) == false)
        {
            return false;
        }
        BindingDatabase rhs = ((BindingDatabase) other);
        return ((this.bindingTimeout == rhs.bindingTimeout) || ((this.bindingTimeout != null) && this.bindingTimeout.equals(rhs.bindingTimeout)));
    }

}
