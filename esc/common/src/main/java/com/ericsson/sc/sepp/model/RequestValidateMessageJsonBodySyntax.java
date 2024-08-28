
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfValidationChecks;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Validation of message JSON body syntax.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "report-event", "action-reject-message", "action-drop-message", "action-forward-unmodified-message" })
public class RequestValidateMessageJsonBodySyntax implements IfValidationChecks
{

    /**
     * Report event if a message fails the validation check.
     * 
     */
    @JsonProperty("report-event")
    @JsonPropertyDescription("Report event if a message fails the validation check.")
    private Boolean reportEvent = true;
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

    public RequestValidateMessageJsonBodySyntax withReportEvent(Boolean reportEvent)
    {
        this.reportEvent = reportEvent;
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

    public RequestValidateMessageJsonBodySyntax withActionRejectMessage(FirewallProfileActionRejectMessage actionRejectMessage)
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

    public RequestValidateMessageJsonBodySyntax withActionDropMessage(FirewallProfileActionDropMessage actionDropMessage)
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

    public RequestValidateMessageJsonBodySyntax withActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
    {
        this.actionForwardUnmodifiedMessage = actionForwardUnmodifiedMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RequestValidateMessageJsonBodySyntax.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("reportEvent");
        sb.append('=');
        sb.append(((this.reportEvent == null) ? "<null>" : this.reportEvent));
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
        result = ((result * 31) + ((this.reportEvent == null) ? 0 : this.reportEvent.hashCode()));
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
        if ((other instanceof RequestValidateMessageJsonBodySyntax) == false)
        {
            return false;
        }
        RequestValidateMessageJsonBodySyntax rhs = ((RequestValidateMessageJsonBodySyntax) other);
        return (((((this.actionRejectMessage == rhs.actionRejectMessage)
                   || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage)))
                  && ((this.actionForwardUnmodifiedMessage == rhs.actionForwardUnmodifiedMessage)
                      || ((this.actionForwardUnmodifiedMessage != null) && this.actionForwardUnmodifiedMessage.equals(rhs.actionForwardUnmodifiedMessage))))
                 && ((this.reportEvent == rhs.reportEvent) || ((this.reportEvent != null) && this.reportEvent.equals(rhs.reportEvent))))
                && ((this.actionDropMessage == rhs.actionDropMessage)
                    || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))));
    }

}
