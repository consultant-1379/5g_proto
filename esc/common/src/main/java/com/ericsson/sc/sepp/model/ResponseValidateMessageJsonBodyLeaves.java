
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfValidationChecks;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Validation of configured maximum integer of JSON body leaves in the message.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "max-leaves", "report-event", "action-respond-with-error", "action-forward-unmodified-message" })
public class ResponseValidateMessageJsonBodyLeaves implements IfValidationChecks
{

    /**
     * Maximum integer of leaves in the JSON body. Must be equal or lower than
     * message-body-limits max-leaves. (Required)
     * 
     */
    @JsonProperty("max-leaves")
    @JsonPropertyDescription("Maximum integer of leaves in the JSON body. Must be equal or lower than message-body-limits max-leaves.")
    private Integer maxLeaves;
    /**
     * Report event if a message fails the validation check.
     * 
     */
    @JsonProperty("report-event")
    @JsonPropertyDescription("Report event if a message fails the validation check.")
    private Boolean reportEvent = true;
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
     * Maximum integer of leaves in the JSON body. Must be equal or lower than
     * message-body-limits max-leaves. (Required)
     * 
     */
    @JsonProperty("max-leaves")
    public Integer getMaxLeaves()
    {
        return maxLeaves;
    }

    /**
     * Maximum integer of leaves in the JSON body. Must be equal or lower than
     * message-body-limits max-leaves. (Required)
     * 
     */
    @JsonProperty("max-leaves")
    public void setMaxLeaves(Integer maxLeaves)
    {
        this.maxLeaves = maxLeaves;
    }

    public ResponseValidateMessageJsonBodyLeaves withMaxLeaves(Integer maxLeaves)
    {
        this.maxLeaves = maxLeaves;
        return this;
    }

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

    public ResponseValidateMessageJsonBodyLeaves withReportEvent(Boolean reportEvent)
    {
        this.reportEvent = reportEvent;
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

    public ResponseValidateMessageJsonBodyLeaves withActionRespondWithError(FirewallProfileActionRespondWithError actionRespondWithError)
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

    public ResponseValidateMessageJsonBodyLeaves withActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
    {
        this.actionForwardUnmodifiedMessage = actionForwardUnmodifiedMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ResponseValidateMessageJsonBodyLeaves.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("maxLeaves");
        sb.append('=');
        sb.append(((this.maxLeaves == null) ? "<null>" : this.maxLeaves));
        sb.append(',');
        sb.append("reportEvent");
        sb.append('=');
        sb.append(((this.reportEvent == null) ? "<null>" : this.reportEvent));
        sb.append(',');
        sb.append("actionRespondWithError");
        sb.append('=');
        sb.append(((this.actionRespondWithError == null) ? "<null>" : this.actionRespondWithError));
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
        result = ((result * 31) + ((this.maxLeaves == null) ? 0 : this.maxLeaves.hashCode()));
        result = ((result * 31) + ((this.reportEvent == null) ? 0 : this.reportEvent.hashCode()));
        result = ((result * 31) + ((this.actionForwardUnmodifiedMessage == null) ? 0 : this.actionForwardUnmodifiedMessage.hashCode()));
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
        if ((other instanceof ResponseValidateMessageJsonBodyLeaves) == false)
        {
            return false;
        }
        ResponseValidateMessageJsonBodyLeaves rhs = ((ResponseValidateMessageJsonBodyLeaves) other);
        return (((((this.actionForwardUnmodifiedMessage == rhs.actionForwardUnmodifiedMessage)
                   || ((this.actionForwardUnmodifiedMessage != null) && this.actionForwardUnmodifiedMessage.equals(rhs.actionForwardUnmodifiedMessage)))
                  && ((this.maxLeaves == rhs.maxLeaves) || ((this.maxLeaves != null) && this.maxLeaves.equals(rhs.maxLeaves))))
                 && ((this.reportEvent == rhs.reportEvent) || ((this.reportEvent != null) && this.reportEvent.equals(rhs.reportEvent))))
                && ((this.actionRespondWithError == rhs.actionRespondWithError)
                    || ((this.actionRespondWithError != null) && this.actionRespondWithError.equals(rhs.actionRespondWithError))));
    }

}
