
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "request", "response" })
public class FirewallProfile implements IfNamedListItem
{

    /**
     * Name uniquely identifying the firewall profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the firewall profile")
    private String name;
    /**
     * Firewall rules for request messages. Order of applied rules is defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("request")
    @JsonPropertyDescription("Firewall rules for request messages. Order of applied rules is defined by the system and described in the CPI.")
    private Request request;
    /**
     * Firewall rules for response messages. Order of applied rules is defined by
     * the system and described in the CPI.
     * 
     */
    @JsonProperty("response")
    @JsonPropertyDescription("Firewall rules for response messages. Order of applied rules is defined by the system and described in the CPI.")
    private Response response;

    /**
     * Name uniquely identifying the firewall profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the firewall profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public FirewallProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Firewall rules for request messages. Order of applied rules is defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("request")
    public Request getRequest()
    {
        return request;
    }

    /**
     * Firewall rules for request messages. Order of applied rules is defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("request")
    public void setRequest(Request request)
    {
        this.request = request;
    }

    public FirewallProfile withRequest(Request request)
    {
        this.request = request;
        return this;
    }

    /**
     * Firewall rules for response messages. Order of applied rules is defined by
     * the system and described in the CPI.
     * 
     */
    @JsonProperty("response")
    public Response getResponse()
    {
        return response;
    }

    /**
     * Firewall rules for response messages. Order of applied rules is defined by
     * the system and described in the CPI.
     * 
     */
    @JsonProperty("response")
    public void setResponse(Response response)
    {
        this.response = response;
    }

    public FirewallProfile withResponse(Response response)
    {
        this.response = response;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FirewallProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("request");
        sb.append('=');
        sb.append(((this.request == null) ? "<null>" : this.request));
        sb.append(',');
        sb.append("response");
        sb.append('=');
        sb.append(((this.response == null) ? "<null>" : this.response));
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.request == null) ? 0 : this.request.hashCode()));
        result = ((result * 31) + ((this.response == null) ? 0 : this.response.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof FirewallProfile) == false)
        {
            return false;
        }
        FirewallProfile rhs = ((FirewallProfile) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.request == rhs.request) || ((this.request != null) && this.request.equals(rhs.request))))
                && ((this.response == rhs.response) || ((this.response != null) && this.response.equals(rhs.response))));
    }

}
