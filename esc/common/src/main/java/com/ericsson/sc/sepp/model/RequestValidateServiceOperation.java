
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.utilities.common.IfValidationChecks;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Validation against allowed operations. If no operations are added or removed
 * from the default list of allowed operations, only the default allowed
 * operations will be permitted. Default allowed operations are defined by the
 * system and described in the CPI.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "additional-allowed-operations",
                     "removed-default-operations",
                     "report-event",
                     "action-reject-message",
                     "action-drop-message",
                     "action-forward-unmodified-message" })
public class RequestValidateServiceOperation implements IfValidationChecks
{

    /**
     * List of operations allowed in addition to the default allowed operations.
     * 
     */
    @JsonProperty("additional-allowed-operations")
    @JsonPropertyDescription("List of operations allowed in addition to the default allowed operations.")
    private List<AdditionalAllowedOperation> additionalAllowedOperations = new ArrayList<AdditionalAllowedOperation>();
    /**
     * List of operations that will be removed from default allowed operations.
     * 
     */
    @JsonProperty("removed-default-operations")
    @JsonPropertyDescription("List of operations that will be removed from default allowed operations.")
    private List<RemovedDefaultOperation> removedDefaultOperations = new ArrayList<RemovedDefaultOperation>();
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
     * List of operations allowed in addition to the default allowed operations.
     * 
     */
    @JsonProperty("additional-allowed-operations")
    public List<AdditionalAllowedOperation> getAdditionalAllowedOperations()
    {
        return additionalAllowedOperations;
    }

    /**
     * List of operations allowed in addition to the default allowed operations.
     * 
     */
    @JsonProperty("additional-allowed-operations")
    public void setAdditionalAllowedOperations(List<AdditionalAllowedOperation> additionalAllowedOperations)
    {
        this.additionalAllowedOperations = additionalAllowedOperations;
    }

    public RequestValidateServiceOperation withAdditionalAllowedOperations(List<AdditionalAllowedOperation> additionalAllowedOperations)
    {
        this.additionalAllowedOperations = additionalAllowedOperations;
        return this;
    }

    /**
     * List of operations that will be removed from default allowed operations.
     * 
     */
    @JsonProperty("removed-default-operations")
    public List<RemovedDefaultOperation> getRemovedDefaultOperations()
    {
        return removedDefaultOperations;
    }

    /**
     * List of operations that will be removed from default allowed operations.
     * 
     */
    @JsonProperty("removed-default-operations")
    public void setRemovedDefaultOperations(List<RemovedDefaultOperation> removedDefaultOperations)
    {
        this.removedDefaultOperations = removedDefaultOperations;
    }

    public RequestValidateServiceOperation withRemovedDefaultOperations(List<RemovedDefaultOperation> removedDefaultOperations)
    {
        this.removedDefaultOperations = removedDefaultOperations;
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

    public RequestValidateServiceOperation withReportEvent(Boolean reportEvent)
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

    public RequestValidateServiceOperation withActionRejectMessage(FirewallProfileActionRejectMessage actionRejectMessage)
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

    public RequestValidateServiceOperation withActionDropMessage(FirewallProfileActionDropMessage actionDropMessage)
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

    public RequestValidateServiceOperation withActionForwardUnmodifiedMessage(FirewallProfileActionForwardUnmodifiedMessage actionForwardUnmodifiedMessage)
    {
        this.actionForwardUnmodifiedMessage = actionForwardUnmodifiedMessage;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RequestValidateServiceOperation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("additionalAllowedOperations");
        sb.append('=');
        sb.append(((this.additionalAllowedOperations == null) ? "<null>" : this.additionalAllowedOperations));
        sb.append(',');
        sb.append("removedDefaultOperations");
        sb.append('=');
        sb.append(((this.removedDefaultOperations == null) ? "<null>" : this.removedDefaultOperations));
        sb.append(',');
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
        result = ((result * 31) + ((this.removedDefaultOperations == null) ? 0 : this.removedDefaultOperations.hashCode()));
        result = ((result * 31) + ((this.reportEvent == null) ? 0 : this.reportEvent.hashCode()));
        result = ((result * 31) + ((this.actionDropMessage == null) ? 0 : this.actionDropMessage.hashCode()));
        result = ((result * 31) + ((this.additionalAllowedOperations == null) ? 0 : this.additionalAllowedOperations.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RequestValidateServiceOperation) == false)
        {
            return false;
        }
        RequestValidateServiceOperation rhs = ((RequestValidateServiceOperation) other);
        return (((((((this.actionRejectMessage == rhs.actionRejectMessage)
                     || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage)))
                    && ((this.actionForwardUnmodifiedMessage == rhs.actionForwardUnmodifiedMessage)
                        || ((this.actionForwardUnmodifiedMessage != null) && this.actionForwardUnmodifiedMessage.equals(rhs.actionForwardUnmodifiedMessage))))
                   && ((this.removedDefaultOperations == rhs.removedDefaultOperations)
                       || ((this.removedDefaultOperations != null) && this.removedDefaultOperations.equals(rhs.removedDefaultOperations))))
                  && ((this.reportEvent == rhs.reportEvent) || ((this.reportEvent != null) && this.reportEvent.equals(rhs.reportEvent))))
                 && ((this.actionDropMessage == rhs.actionDropMessage)
                     || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))))
                && ((this.additionalAllowedOperations == rhs.additionalAllowedOperations)
                    || ((this.additionalAllowedOperations != null) && this.additionalAllowedOperations.equals(rhs.additionalAllowedOperations))));
    }

}
