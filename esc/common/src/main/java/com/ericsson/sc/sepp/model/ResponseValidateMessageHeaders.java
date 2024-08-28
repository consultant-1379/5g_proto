
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
@JsonPropertyOrder({ "report-event", "allowed", "denied", "action-respond-with-error", "action-forward-unmodified-message", "action-forward-modified-message" })
public class ResponseValidateMessageHeaders implements IfValidationChecks
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
     * Create an error response with a configured status code and title with
     * detailed explanation.
     * 
     */
    @JsonProperty("action-respond-with-error")
    @JsonPropertyDescription("Create an error response with a configured status code and title with detailed explanation.")
    private FirewallProfileActionRespondWithError actionRespondWithError;
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

    public ResponseValidateMessageHeaders withReportEvent(Boolean reportEvent)
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

    public ResponseValidateMessageHeaders withAllowed(Allowed allowed)
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

    public ResponseValidateMessageHeaders withDenied(Denied denied)
    {
        this.denied = denied;
        return this;
    }

    /**
     * Create an error response with a configured status code and title with
     * detailed explanation.
     * 
     */
    @JsonProperty("action-respond-with-error")
    public FirewallProfileActionRespondWithError getActionRespondWithError()
    {
        return actionRespondWithError;
    }

    /**
     * Create an error response with a configured status code and title with
     * detailed explanation.
     * 
     */
    @JsonProperty("action-respond-with-error")
    public void setActionRespondWithError(FirewallProfileActionRespondWithError actionRespondWithError)
    {
        this.actionRespondWithError = actionRespondWithError;
    }

    public ResponseValidateMessageHeaders withActionRespondWithError(FirewallProfileActionRespondWithError actionRespondWithError)
    {
        this.actionRespondWithError = actionRespondWithError;
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

    public ResponseValidateMessageHeaders withActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
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

    public ResponseValidateMessageHeaders withActionForwardModifiedMessage(FirewallProfileActionForwardModifiedMessage actionForwardModifiedMessage)
    {
        this.actionForwardModifiedMessage = actionForwardModifiedMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ResponseValidateMessageHeaders.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("actionRespondWithError");
        sb.append('=');
        sb.append(((this.actionRespondWithError == null) ? "<null>" : this.actionRespondWithError));
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
        result = ((result * 31) + ((this.actionForwardUnmodifiedMessage == null) ? 0 : this.actionForwardUnmodifiedMessage.hashCode()));
        result = ((result * 31) + ((this.actionForwardModifiedMessage == null) ? 0 : this.actionForwardModifiedMessage.hashCode()));
        result = ((result * 31) + ((this.reportEvent == null) ? 0 : this.reportEvent.hashCode()));
        result = ((result * 31) + ((this.allowed == null) ? 0 : this.allowed.hashCode()));
        result = ((result * 31) + ((this.denied == null) ? 0 : this.denied.hashCode()));
        result = ((result * 31) + ((this.actionRespondWithError == null) ? 0 : this.actionRespondWithError.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ResponseValidateMessageHeaders) == false)
        {
            return false;
        }
        ResponseValidateMessageHeaders rhs = ((ResponseValidateMessageHeaders) other);
        return (((((((this.actionForwardUnmodifiedMessage == rhs.actionForwardUnmodifiedMessage)
                     || ((this.actionForwardUnmodifiedMessage != null) && this.actionForwardUnmodifiedMessage.equals(rhs.actionForwardUnmodifiedMessage)))
                    && ((this.actionForwardModifiedMessage == rhs.actionForwardModifiedMessage)
                        || ((this.actionForwardModifiedMessage != null) && this.actionForwardModifiedMessage.equals(rhs.actionForwardModifiedMessage))))
                   && ((this.reportEvent == rhs.reportEvent) || ((this.reportEvent != null) && this.reportEvent.equals(rhs.reportEvent))))
                  && ((this.allowed == rhs.allowed) || ((this.allowed != null) && this.allowed.equals(rhs.allowed))))
                 && ((this.denied == rhs.denied) || ((this.denied != null) && this.denied.equals(rhs.denied))))
                && ((this.actionRespondWithError == rhs.actionRespondWithError)
                    || ((this.actionRespondWithError != null) && this.actionRespondWithError.equals(rhs.actionRespondWithError))));
    }

}
