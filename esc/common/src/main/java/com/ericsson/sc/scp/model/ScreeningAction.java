
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "action-add-header",
                     "action-modify-header",
                     "action-remove-header",
                     "action-drop-message",
                     "action-exit-screening-case",
                     "action-reject-message",
                     "action-go-to",
                     "action-log",
                     "action-modify-json-body",
                     "action-create-json-body" })
public class ScreeningAction
{

    /**
     * Name identifying the screening-action (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the screening-action")
    private String name;
    /**
     * Adds an HTTP header to a message
     * 
     */
    @JsonProperty("action-add-header")
    @JsonPropertyDescription("Adds an HTTP header to a message")
    private ActionAddHeader actionAddHeader;
    /**
     * Either replaces the value of an existing HTTP header with a new one or
     * prepends/appends a value to the existing value of a header
     * 
     */
    @JsonProperty("action-modify-header")
    @JsonPropertyDescription("Either replaces the value of an existing HTTP header with a new one or prepends/appends a value to the existing value of a header")
    private ActionModifyHeader actionModifyHeader;
    /**
     * Removes an HTTP header from a message
     * 
     */
    @JsonProperty("action-remove-header")
    @JsonPropertyDescription("Removes an HTTP header from a message")
    private ActionRemoveHeader actionRemoveHeader;
    /**
     * Drops an http request message and the HTTP/2 stream is reset gracefully
     * 
     */
    @JsonProperty("action-drop-message")
    @JsonPropertyDescription("Drops an http request message and the HTTP/2 stream is reset gracefully")
    private ActionDropMessage actionDropMessage;
    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    @JsonPropertyDescription("Exits from a screening-case and terminates message screening processing")
    private ActionExitScreeningCase actionExitScreeningCase;
    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    @JsonPropertyDescription("Rejects an http request and sends back a response with an operator defined status code and title with detailed explanation")
    private ActionRejectMessage actionRejectMessage;
    /**
     * Jump to another request-screening-case
     * 
     */
    @JsonProperty("action-go-to")
    @JsonPropertyDescription("Jump to another request-screening-case")
    private ActionGoTo actionGoTo;
    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    @JsonPropertyDescription("Logs a user-defined message with the configured log-level")
    private ActionLog actionLog;
    /**
     * Performs modifications on JSON body elements by either adding new elements,
     * replacing the value of existing elements or removing the elements from the
     * JSON body
     * 
     */
    @JsonProperty("action-modify-json-body")
    @JsonPropertyDescription("Performs modifications on JSON body elements by either adding new elements, replacing the value of existing elements or removing the elements from the JSON body")
    private ActionModifyJsonBody actionModifyJsonBody;
    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    @JsonPropertyDescription("Creates a valid JSON body from scratch")
    private ActionCreateJsonBody actionCreateJsonBody;

    /**
     * Name identifying the screening-action (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the screening-action (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ScreeningAction withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Adds an HTTP header to a message
     * 
     */
    @JsonProperty("action-add-header")
    public ActionAddHeader getActionAddHeader()
    {
        return actionAddHeader;
    }

    /**
     * Adds an HTTP header to a message
     * 
     */
    @JsonProperty("action-add-header")
    public void setActionAddHeader(ActionAddHeader actionAddHeader)
    {
        this.actionAddHeader = actionAddHeader;
    }

    public ScreeningAction withActionAddHeader(ActionAddHeader actionAddHeader)
    {
        this.actionAddHeader = actionAddHeader;
        return this;
    }

    /**
     * Either replaces the value of an existing HTTP header with a new one or
     * prepends/appends a value to the existing value of a header
     * 
     */
    @JsonProperty("action-modify-header")
    public ActionModifyHeader getActionModifyHeader()
    {
        return actionModifyHeader;
    }

    /**
     * Either replaces the value of an existing HTTP header with a new one or
     * prepends/appends a value to the existing value of a header
     * 
     */
    @JsonProperty("action-modify-header")
    public void setActionModifyHeader(ActionModifyHeader actionModifyHeader)
    {
        this.actionModifyHeader = actionModifyHeader;
    }

    public ScreeningAction withActionModifyHeader(ActionModifyHeader actionModifyHeader)
    {
        this.actionModifyHeader = actionModifyHeader;
        return this;
    }

    /**
     * Removes an HTTP header from a message
     * 
     */
    @JsonProperty("action-remove-header")
    public ActionRemoveHeader getActionRemoveHeader()
    {
        return actionRemoveHeader;
    }

    /**
     * Removes an HTTP header from a message
     * 
     */
    @JsonProperty("action-remove-header")
    public void setActionRemoveHeader(ActionRemoveHeader actionRemoveHeader)
    {
        this.actionRemoveHeader = actionRemoveHeader;
    }

    public ScreeningAction withActionRemoveHeader(ActionRemoveHeader actionRemoveHeader)
    {
        this.actionRemoveHeader = actionRemoveHeader;
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

    public ScreeningAction withActionDropMessage(ActionDropMessage actionDropMessage)
    {
        this.actionDropMessage = actionDropMessage;
        return this;
    }

    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    public ActionExitScreeningCase getActionExitScreeningCase()
    {
        return actionExitScreeningCase;
    }

    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    public void setActionExitScreeningCase(ActionExitScreeningCase actionExitScreeningCase)
    {
        this.actionExitScreeningCase = actionExitScreeningCase;
    }

    public ScreeningAction withActionExitScreeningCase(ActionExitScreeningCase actionExitScreeningCase)
    {
        this.actionExitScreeningCase = actionExitScreeningCase;
        return this;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public ActionRejectMessage getActionRejectMessage()
    {
        return actionRejectMessage;
    }

    /**
     * Rejects an http request and sends back a response with an operator defined
     * status code and title with detailed explanation
     * 
     */
    @JsonProperty("action-reject-message")
    public void setActionRejectMessage(ActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
    }

    public ScreeningAction withActionRejectMessage(ActionRejectMessage actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
        return this;
    }

    /**
     * Jump to another request-screening-case
     * 
     */
    @JsonProperty("action-go-to")
    public ActionGoTo getActionGoTo()
    {
        return actionGoTo;
    }

    /**
     * Jump to another request-screening-case
     * 
     */
    @JsonProperty("action-go-to")
    public void setActionGoTo(ActionGoTo actionGoTo)
    {
        this.actionGoTo = actionGoTo;
    }

    public ScreeningAction withActionGoTo(ActionGoTo actionGoTo)
    {
        this.actionGoTo = actionGoTo;
        return this;
    }

    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    public ActionLog getActionLog()
    {
        return actionLog;
    }

    /**
     * Logs a user-defined message with the configured log-level
     * 
     */
    @JsonProperty("action-log")
    public void setActionLog(ActionLog actionLog)
    {
        this.actionLog = actionLog;
    }

    public ScreeningAction withActionLog(ActionLog actionLog)
    {
        this.actionLog = actionLog;
        return this;
    }

    /**
     * Performs modifications on JSON body elements by either adding new elements,
     * replacing the value of existing elements or removing the elements from the
     * JSON body
     * 
     */
    @JsonProperty("action-modify-json-body")
    public ActionModifyJsonBody getActionModifyJsonBody()
    {
        return actionModifyJsonBody;
    }

    /**
     * Performs modifications on JSON body elements by either adding new elements,
     * replacing the value of existing elements or removing the elements from the
     * JSON body
     * 
     */
    @JsonProperty("action-modify-json-body")
    public void setActionModifyJsonBody(ActionModifyJsonBody actionModifyJsonBody)
    {
        this.actionModifyJsonBody = actionModifyJsonBody;
    }

    public ScreeningAction withActionModifyJsonBody(ActionModifyJsonBody actionModifyJsonBody)
    {
        this.actionModifyJsonBody = actionModifyJsonBody;
        return this;
    }

    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    public ActionCreateJsonBody getActionCreateJsonBody()
    {
        return actionCreateJsonBody;
    }

    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    public void setActionCreateJsonBody(ActionCreateJsonBody actionCreateJsonBody)
    {
        this.actionCreateJsonBody = actionCreateJsonBody;
    }

    public ScreeningAction withActionCreateJsonBody(ActionCreateJsonBody actionCreateJsonBody)
    {
        this.actionCreateJsonBody = actionCreateJsonBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScreeningAction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("actionAddHeader");
        sb.append('=');
        sb.append(((this.actionAddHeader == null) ? "<null>" : this.actionAddHeader));
        sb.append(',');
        sb.append("actionModifyHeader");
        sb.append('=');
        sb.append(((this.actionModifyHeader == null) ? "<null>" : this.actionModifyHeader));
        sb.append(',');
        sb.append("actionRemoveHeader");
        sb.append('=');
        sb.append(((this.actionRemoveHeader == null) ? "<null>" : this.actionRemoveHeader));
        sb.append(',');
        sb.append("actionDropMessage");
        sb.append('=');
        sb.append(((this.actionDropMessage == null) ? "<null>" : this.actionDropMessage));
        sb.append(',');
        sb.append("actionExitScreeningCase");
        sb.append('=');
        sb.append(((this.actionExitScreeningCase == null) ? "<null>" : this.actionExitScreeningCase));
        sb.append(',');
        sb.append("actionRejectMessage");
        sb.append('=');
        sb.append(((this.actionRejectMessage == null) ? "<null>" : this.actionRejectMessage));
        sb.append(',');
        sb.append("actionGoTo");
        sb.append('=');
        sb.append(((this.actionGoTo == null) ? "<null>" : this.actionGoTo));
        sb.append(',');
        sb.append("actionLog");
        sb.append('=');
        sb.append(((this.actionLog == null) ? "<null>" : this.actionLog));
        sb.append(',');
        sb.append("actionModifyJsonBody");
        sb.append('=');
        sb.append(((this.actionModifyJsonBody == null) ? "<null>" : this.actionModifyJsonBody));
        sb.append(',');
        sb.append("actionCreateJsonBody");
        sb.append('=');
        sb.append(((this.actionCreateJsonBody == null) ? "<null>" : this.actionCreateJsonBody));
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
        result = ((result * 31) + ((this.actionAddHeader == null) ? 0 : this.actionAddHeader.hashCode()));
        result = ((result * 31) + ((this.actionGoTo == null) ? 0 : this.actionGoTo.hashCode()));
        result = ((result * 31) + ((this.actionCreateJsonBody == null) ? 0 : this.actionCreateJsonBody.hashCode()));
        result = ((result * 31) + ((this.actionRemoveHeader == null) ? 0 : this.actionRemoveHeader.hashCode()));
        result = ((result * 31) + ((this.actionExitScreeningCase == null) ? 0 : this.actionExitScreeningCase.hashCode()));
        result = ((result * 31) + ((this.actionDropMessage == null) ? 0 : this.actionDropMessage.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.actionModifyJsonBody == null) ? 0 : this.actionModifyJsonBody.hashCode()));
        result = ((result * 31) + ((this.actionModifyHeader == null) ? 0 : this.actionModifyHeader.hashCode()));
        result = ((result * 31) + ((this.actionLog == null) ? 0 : this.actionLog.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScreeningAction) == false)
        {
            return false;
        }
        ScreeningAction rhs = ((ScreeningAction) other);
        return ((((((((((((this.actionRejectMessage == rhs.actionRejectMessage)
                          || ((this.actionRejectMessage != null) && this.actionRejectMessage.equals(rhs.actionRejectMessage)))
                         && ((this.actionAddHeader == rhs.actionAddHeader)
                             || ((this.actionAddHeader != null) && this.actionAddHeader.equals(rhs.actionAddHeader))))
                        && ((this.actionGoTo == rhs.actionGoTo) || ((this.actionGoTo != null) && this.actionGoTo.equals(rhs.actionGoTo))))
                       && ((this.actionCreateJsonBody == rhs.actionCreateJsonBody)
                           || ((this.actionCreateJsonBody != null) && this.actionCreateJsonBody.equals(rhs.actionCreateJsonBody))))
                      && ((this.actionRemoveHeader == rhs.actionRemoveHeader)
                          || ((this.actionRemoveHeader != null) && this.actionRemoveHeader.equals(rhs.actionRemoveHeader))))
                     && ((this.actionExitScreeningCase == rhs.actionExitScreeningCase)
                         || ((this.actionExitScreeningCase != null) && this.actionExitScreeningCase.equals(rhs.actionExitScreeningCase))))
                    && ((this.actionDropMessage == rhs.actionDropMessage)
                        || ((this.actionDropMessage != null) && this.actionDropMessage.equals(rhs.actionDropMessage))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.actionModifyJsonBody == rhs.actionModifyJsonBody)
                      || ((this.actionModifyJsonBody != null) && this.actionModifyJsonBody.equals(rhs.actionModifyJsonBody))))
                 && ((this.actionModifyHeader == rhs.actionModifyHeader)
                     || ((this.actionModifyHeader != null) && this.actionModifyHeader.equals(rhs.actionModifyHeader))))
                && ((this.actionLog == rhs.actionLog) || ((this.actionLog != null) && this.actionLog.equals(rhs.actionLog))));
    }

}
