
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Jump to another response screening case
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "response-screening-case-ref" })
public class ActionGoTo__1
{

    /**
     * The name of the response-screening-case where the execution continues
     * 
     */
    @JsonProperty("response-screening-case-ref")
    @JsonPropertyDescription("The name of the response-screening-case where the execution continues")
    private String responseScreeningCaseRef;

    /**
     * The name of the response-screening-case where the execution continues
     * 
     */
    @JsonProperty("response-screening-case-ref")
    public String getResponseScreeningCaseRef()
    {
        return responseScreeningCaseRef;
    }

    /**
     * The name of the response-screening-case where the execution continues
     * 
     */
    @JsonProperty("response-screening-case-ref")
    public void setResponseScreeningCaseRef(String responseScreeningCaseRef)
    {
        this.responseScreeningCaseRef = responseScreeningCaseRef;
    }

    public ActionGoTo__1 withResponseScreeningCaseRef(String responseScreeningCaseRef)
    {
        this.responseScreeningCaseRef = responseScreeningCaseRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionGoTo__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("responseScreeningCaseRef");
        sb.append('=');
        sb.append(((this.responseScreeningCaseRef == null) ? "<null>" : this.responseScreeningCaseRef));
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
        result = ((result * 31) + ((this.responseScreeningCaseRef == null) ? 0 : this.responseScreeningCaseRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionGoTo__1) == false)
        {
            return false;
        }
        ActionGoTo__1 rhs = ((ActionGoTo__1) other);
        return ((this.responseScreeningCaseRef == rhs.responseScreeningCaseRef)
                || ((this.responseScreeningCaseRef != null) && this.responseScreeningCaseRef.equals(rhs.responseScreeningCaseRef)));
    }

}
