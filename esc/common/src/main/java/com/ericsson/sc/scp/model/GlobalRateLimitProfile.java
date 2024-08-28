
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfGlobalRateLimitProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "sustainable-rate", "max-burst-size", "action-reject-message", "action-drop-message" })
public class GlobalRateLimitProfile implements IfGlobalRateLimitProfile
{

    /**
     * Name of the rate limit profile. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the rate limit profile.")
    private String name;
    /**
     * The integer of incoming requests that can be sustainably received from a peer
     * within an one second interval (Required)
     * 
     */
    @JsonProperty("sustainable-rate")
    @JsonPropertyDescription("The integer of incoming requests that can be sustainably received from a peer within an one second interval")
    private Integer sustainableRate;
    /**
     * The maximum integer of the incoming requests, beyond the sustainable rate,
     * that can be received from a peer in a burst (Required)
     * 
     */
    @JsonProperty("max-burst-size")
    @JsonPropertyDescription("The maximum integer of the incoming requests, beyond the sustainable rate, that can be received from a peer in a burst")
    private Integer maxBurstSize;
    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    @JsonPropertyDescription("Rejects an http request and sends back a response with an operator defined status code and title with detailed explanation")
    private RateLimitingActionRejectMessage actionRejectMessage;
    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    @JsonPropertyDescription("Drops an http request message and the HTTP/2 stream is reset gracefully")
    private ActionDropMessage actionDropMessage;

    /**
     * Name of the rate limit profile. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the rate limit profile. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public GlobalRateLimitProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The integer of incoming requests that can be sustainably received from a peer
     * within an one second interval (Required)
     * 
     */
    @JsonProperty("sustainable-rate")
    public Integer getSustainableRate()
    {
        return sustainableRate;
    }

    /**
     * The integer of incoming requests that can be sustainably received from a peer
     * within an one second interval (Required)
     * 
     */
    @JsonProperty("sustainable-rate")
    public void setSustainableRate(Integer sustainableRate)
    {
        this.sustainableRate = sustainableRate;
    }

    public GlobalRateLimitProfile withSustainableRate(Integer sustainableRate)
    {
        this.sustainableRate = sustainableRate;
        return this;
    }

    /**
     * The maximum integer of the incoming requests, beyond the sustainable rate,
     * that can be received from a peer in a burst (Required)
     * 
     */
    @JsonProperty("max-burst-size")
    public Integer getMaxBurstSize()
    {
        return maxBurstSize;
    }

    /**
     * The maximum integer of the incoming requests, beyond the sustainable rate,
     * that can be received from a peer in a burst (Required)
     * 
     */
    @JsonProperty("max-burst-size")
    public void setMaxBurstSize(Integer maxBurstSize)
    {
        this.maxBurstSize = maxBurstSize;
    }

    public GlobalRateLimitProfile withMaxBurstSize(Integer maxBurstSize)
    {
        this.maxBurstSize = maxBurstSize;
        return this;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public RateLimitingActionRejectMessage getActionRejectMessage()
    {
        return actionRejectMessage;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public void setActionRejectMessage(RateLimitingActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
    }

    public GlobalRateLimitProfile withActionRejectMessage(RateLimitingActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
        return this;
    }

    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    public ActionDropMessage getActionDropMessage()
    {
        return actionDropMessage;
    }

    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    public void setActionDropMessage(ActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
    }

    public GlobalRateLimitProfile withActionDropMessage(ActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(GlobalRateLimitProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("sustainableRate");
        sb.append('=');
        sb.append(((this.sustainableRate == null) ? "<null>" : this.sustainableRate));
        sb.append(',');
        sb.append("maxBurstSize");
        sb.append('=');
        sb.append(((this.maxBurstSize == null) ? "<null>" : this.maxBurstSize));
        sb.append(',');
        sb.append("actionRejectMessage");
        sb.append('=');
        sb.append(((this.actionRejectMessage == null) ? "<null>" : this.actionRejectMessage));
        sb.append(',');
        sb.append("actionDropMessage");
        sb.append('=');
        sb.append(((this.actionDropMessage == null) ? "<null>" : this.actionDropMessage));
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
        result = ((result * 31) + ((this.sustainableRate == null) ? 0 : this.sustainableRate.hashCode()));
        result = ((result * 31) + ((this.actionRejectMessage == null) ? 0 : this.actionRejectMessage.hashCode()));
        result = ((result * 31) + ((this.maxBurstSize == null) ? 0 : this.maxBurstSize.hashCode()));
        result = ((result * 31) + ((this.actionDropMessage == null) ? 0 : this.actionDropMessage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof GlobalRateLimitProfile) == false)
        {
            return false;
        }
        GlobalRateLimitProfile rhs = ((GlobalRateLimitProfile) other);
        return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                   && ((this.sustainableRate == rhs.sustainableRate) || ((this.sustainableRate != null) && this.sustainableRate.equals(rhs.sustainableRate))))
                  && ((this.actionRejectMessage == rhs.actionRejectMessage)
                      || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage))))
                 && ((this.maxBurstSize == rhs.maxBurstSize) || ((this.maxBurstSize != null) && this.maxBurstSize.equals(rhs.maxBurstSize))))
                && ((this.actionDropMessage == rhs.actionDropMessage)
                    || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))));
    }

}
