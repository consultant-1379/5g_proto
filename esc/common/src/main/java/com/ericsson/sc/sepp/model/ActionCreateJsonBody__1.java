
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Creates a valid JSON body from scratch
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "json-body" })
public class ActionCreateJsonBody__1
{

    /**
     * Specifies the entire JSON body that will be created (Required)
     * 
     */
    @JsonProperty("json-body")
    @JsonPropertyDescription("Specifies the entire JSON body that will be created")
    private String jsonBody;

    /**
     * Specifies the entire JSON body that will be created (Required)
     * 
     */
    @JsonProperty("json-body")
    public String getJsonBody()
    {
        return jsonBody;
    }

    /**
     * Specifies the entire JSON body that will be created (Required)
     * 
     */
    @JsonProperty("json-body")
    public void setJsonBody(String jsonBody)
    {
        this.jsonBody = jsonBody;
    }

    public ActionCreateJsonBody__1 withJsonBody(String jsonBody)
    {
        this.jsonBody = jsonBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionCreateJsonBody__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jsonBody");
        sb.append('=');
        sb.append(((this.jsonBody == null) ? "<null>" : this.jsonBody));
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
        result = ((result * 31) + ((this.jsonBody == null) ? 0 : this.jsonBody.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionCreateJsonBody__1) == false)
        {
            return false;
        }
        ActionCreateJsonBody__1 rhs = ((ActionCreateJsonBody__1) other);
        return ((this.jsonBody == rhs.jsonBody) || ((this.jsonBody != null) && this.jsonBody.equals(rhs.jsonBody)));
    }

}
