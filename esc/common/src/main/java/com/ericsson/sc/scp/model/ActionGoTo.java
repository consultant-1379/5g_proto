
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Jump to another request-screening-case
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "request-screening-case-ref" })
public class ActionGoTo
{

    /**
     * The name of the request-screening-case where the execution continues
     * 
     */
    @JsonProperty("request-screening-case-ref")
    @JsonPropertyDescription("The name of the request-screening-case where the execution continues")
    private String requestScreeningCaseRef;

    /**
     * The name of the request-screening-case where the execution continues
     * 
     */
    @JsonProperty("request-screening-case-ref")
    public String getRequestScreeningCaseRef()
    {
        return requestScreeningCaseRef;
    }

    /**
     * The name of the request-screening-case where the execution continues
     * 
     */
    @JsonProperty("request-screening-case-ref")
    public void setRequestScreeningCaseRef(String requestScreeningCaseRef)
    {
        this.requestScreeningCaseRef = requestScreeningCaseRef;
    }

    public ActionGoTo withRequestScreeningCaseRef(String requestScreeningCaseRef)
    {
        this.requestScreeningCaseRef = requestScreeningCaseRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionGoTo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("requestScreeningCaseRef");
        sb.append('=');
        sb.append(((this.requestScreeningCaseRef == null) ? "<null>" : this.requestScreeningCaseRef));
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
        result = ((result * 31) + ((this.requestScreeningCaseRef == null) ? 0 : this.requestScreeningCaseRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionGoTo) == false)
        {
            return false;
        }
        ActionGoTo rhs = ((ActionGoTo) other);
        return ((this.requestScreeningCaseRef == rhs.requestScreeningCaseRef)
                || ((this.requestScreeningCaseRef != null) && this.requestScreeningCaseRef.equals(rhs.requestScreeningCaseRef)));
    }

}
