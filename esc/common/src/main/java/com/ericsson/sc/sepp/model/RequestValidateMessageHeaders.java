
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfValidationChecks;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Validation against allowed or denied headers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "report-event",
                     "allowed",
                     "denied",
                     "action-reject-message",
                     "action-drop-message",
                     "action-forward-unmodified-message",
                     "action-forward-modified-message" })
public class RequestValidateMessageHeaders implements IfValidationChecks
{

    /**
     * Report event if a message fails the validation check.
     * 
     */
    @JsonProperty("report-event")
    @JsonPropertyDescription("Report event if a message fails the validation check.")
    private Boolean reportEvent = true;
    /**
     * Allowed headers. If left empty, only the default allowed headers will be
     * permitted. Default allowed headers are defined by the system and described in
     * the CPI.
     * 
     */
    @JsonProperty("allowed")
    @JsonPropertyDescription("Allowed headers. If left empty, only the default allowed headers will be permitted. Default allowed headers are defined by the system and described in the CPI.")
    private Allowed allowed;
    /**
     * Denied headers.
     * 
     */
    @JsonProperty("denied")
    @JsonPropertyDescription("Denied headers.")
    private Denied denied;
    /**
     * Reject the message and send back a response with a configured status code and
     * title with detailed explanation.
     * 
     */
    @JsonProperty("action-reject-message")
    @JsonPropertyDescription("Reject the message and send back a response with a configured status code and title with detailed explanation.")
    private FirewallProfileActionRejectMessage actionRejectMessage;
    /**
     * Drop the message and reset the stream gracefully.
     * 
     */
    @JsonProperty("action-drop-message")
    @JsonPropertyDescription("Drop the message and reset the stream gracefully.")
    private FirewallProfileActionDropMessage actionDropMessage;
    /**
     * Forward the message unmodified.
     * 
     */
    @JsonProperty("action-forward-unmodified-message")
    @JsonPropertyDescription("Forward the message unmodified.")
    private FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage;
    /**
     * Remove not allowed or denied headers and forward the message.
     * 
     */
    @JsonProperty("action-forward-modified-message")
    @JsonPropertyDescription("Remove not allowed or denied headers and forward the message.")
    private FirewallProfileActionForwardModifiedMessage actionForwardModifiedMessage;

    /**
     * Report event if a message fails the validation check.
     * 
     */
    @JsonProperty("report-event")
    public Boolean getReportEvent()
    {
        return reportEvent;
    }

    /**
     * Report event if a message fails the validation check.
     * 
     */
    @JsonProperty("report-event")
    public void setReportEvent(Boolean reportEvent)
    {
        this.reportEvent = reportEvent;
    }

    public RequestValidateMessageHeaders withReportEvent(Boolean reportEvent)
    {
        this.reportEvent = reportEvent;
        return this;
    }

    /**
     * Allowed headers. If left empty, only the default allowed headers will be
     * permitted. Default allowed headers are defined by the system and described in
     * the CPI.
     * 
     */
    @JsonProperty("allowed")
    public Allowed getAllowed()
    {
        return allowed;
    }

    /**
     * Allowed headers. If left empty, only the default allowed headers will be
     * permitted. Default allowed headers are defined by the system and described in
     * the CPI.
     * 
     */
    @JsonProperty("allowed")
    public void setAllowed(Allowed allowed)
    {
        this.allowed = allowed;
    }

    public RequestValidateMessageHeaders withAllowed(Allowed allowed)
    {
        this.allowed = allowed;
        return this;
    }

    /**
     * Denied headers.
     * 
     */
    @JsonProperty("denied")
    public Denied getDenied()
    {
        return denied;
    }

    /**
     * Denied headers.
     * 
     */
    @JsonProperty("denied")
    public void setDenied(Denied denied)
    {
        this.denied = denied;
    }

    public RequestValidateMessageHeaders withDenied(Denied denied)
    {
        this.denied = denied;
        return this;
    }

    /**
     * Reject the message and send back a response with a configured status code and
     * title with detailed explanation.
     * 
     */
    @JsonProperty("action-reject-message")
    public FirewallProfileActionRejectMessage getActionRejectMessage()
    {
        return actionRejectMessage;
    }

    /**
     * Reject the message and send back a response with a configured status code and
     * title with detailed explanation.
     * 
     */
    @JsonProperty("action-reject-message")
    public void setActionRejectMessage(FirewallProfileActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
    }

    public RequestValidateMessageHeaders withActionRejectMessage(FirewallProfileActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
        return this;
    }

    /**
     * Drop the message and reset the stream gracefully.
     * 
     */
    @JsonProperty("action-drop-message")
    public FirewallProfileActionDropMessage getActionDropMessage()
    {
        return actionDropMessage;
    }

    /**
     * Drop the message and reset the stream gracefully.
     * 
     */
    @JsonProperty("action-drop-message")
    public void setActionDropMessage(FirewallProfileActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
    }

    public RequestValidateMessageHeaders withActionDropMessage(FirewallProfileActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
        return this;
    }

    /**
     * Forward the message unmodified.
     * 
     */
    @JsonProperty("action-forward-unmodified-message")
    public FirewallProfileActionForwardUnmodifiedMessage getActionForwardUnmodifiedMessage()
    {
        return actionForwardUnmodifiedMessage;
    }

    /**
     * Forward the message unmodified.
     * 
     */
    @JsonProperty("action-forward-unmodified-message")
    public void setActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
    {
        this.actionForwardUnmodifiedMessage = actionForwardUnmodifiedMessage;
    }

    public RequestValidateMessageHeaders withActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
    {
        this.actionForwardUnmodifiedMessage = actionForwardUnmodifiedMessage;
        return this;
    }

    /**
     * Remove not allowed or denied headers and forward the message.
     * 
     */
    @JsonProperty("action-forward-modified-message")
    public FirewallProfileActionForwardModifiedMessage getActionForwardModifiedMessage()
    {
        return actionForwardModifiedMessage;
    }

    /**
     * Remove not allowed or denied headers and forward the message.
     * 
     */
    @JsonProperty("action-forward-modified-message")
    public void setActionForwardModifiedMessage(FirewallProfileActionForwardModifiedMessage actionForwardModifiedMessage)
    {
        this.actionForwardModifiedMessage = actionForwardModifiedMessage;
    }

    public RequestValidateMessageHeaders withActionForwardModifiedMessage(FirewallProfileActionForwardModifiedMessage actionForwardModifiedMessage)
    {
        this.actionForwardModifiedMessage = actionForwardModifiedMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RequestValidateMessageHeaders.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("reportEvent");
        sb.append('=');
        sb.append(((this.reportEvent == null) ? "<null>" : this.reportEvent));
        sb.append(',');
        sb.append("allowed");
        sb.append('=');
        sb.append(((this.allowed == null) ? "<null>" : this.allowed));
        sb.append(',');
        sb.append("denied");
        sb.append('=');
        sb.append(((this.denied == null) ? "<null>" : this.denied));
        sb.append(',');
        sb.append("actionRejectMessage");
        sb.append('=');
        sb.append(((this.actionRejectMessage == null) ? "<null>" : this.actionRejectMessage));
        sb.append(',');
        sb.append("actionDropMessage");
        sb.append('=');
        sb.append(((this.actionDropMessage == null) ? "<null>" : this.actionDropMessage));
        sb.append(',');
        sb.append("actionForwardUnmodifiedMessage");
        sb.append('=');
        sb.append(((this.actionForwardUnmodifiedMessage == null) ? "<null>" : this.actionForwardUnmodifiedMessage));
        sb.append(',');
        sb.append("actionForwardModifiedMessage");
        sb.append('=');
        sb.append(((this.actionForwardModifiedMessage == null) ? "<null>" : this.actionForwardModifiedMessage));
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
        result = ((result * 31) + ((this.actionRejectMessage == null) ? 0 : this.actionRejectMessage.hashCode()));
        result = ((result * 31) + ((this.actionForwardUnmodifiedMessage == null) ? 0 : this.actionForwardUnmodifiedMessage.hashCode()));
        result = ((result * 31) + ((this.actionForwardModifiedMessage == null) ? 0 : this.actionForwardModifiedMessage.hashCode()));
        result = ((result * 31) + ((this.reportEvent == null) ? 0 : this.reportEvent.hashCode()));
        result = ((result * 31) + ((this.allowed == null) ? 0 : this.allowed.hashCode()));
        result = ((result * 31) + ((this.actionDropMessage == null) ? 0 : this.actionDropMessage.hashCode()));
        result = ((result * 31) + ((this.denied == null) ? 0 : this.denied.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RequestValidateMessageHeaders) == false)
        {
            return false;
        }
        RequestValidateMessageHeaders rhs = ((RequestValidateMessageHeaders) other);
        return ((((((((this.actionRejectMessage == rhs.actionRejectMessage)
                      || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage)))
                     && ((this.actionForwardUnmodifiedMessage == rhs.actionForwardUnmodifiedMessage)
                         || ((this.actionForwardUnmodifiedMessage != null) && this.actionForwardUnmodifiedMessage.equals(rhs.actionForwardUnmodifiedMessage))))
                    && ((this.actionForwardModifiedMessage == rhs.actionForwardModifiedMessage)
                        || ((this.actionForwardModifiedMessage != null) && this.actionForwardModifiedMessage.equals(rhs.actionForwardModifiedMessage))))
                   && ((this.reportEvent == rhs.reportEvent) || ((this.reportEvent != null) && this.reportEvent.equals(rhs.reportEvent))))
                  && ((this.allowed == rhs.allowed) || ((this.allowed != null) && this.allowed.equals(rhs.allowed))))
                 && ((this.actionDropMessage == rhs.actionDropMessage)
                     || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))))
                && ((this.denied == rhs.denied) || ((this.denied != null) && this.denied.equals(rhs.denied))));
    }

}
