
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "action-add-header",
                     "action-modify-header",
                     "action-remove-header",
                     "action-exit-screening-case",
                     "action-modify-status-code",
                     "action-go-to",
                     "action-log",
                     "action-modify-json-body",
                     "action-create-json-body" })
public class ScreeningAction__1
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
    private ActionAddHeader__1 actionAddHeader;
    /**
     * Either replaces the value of an existing HTTP header with a new one or
     * prepends/appends a value to the existing value of a header
     * 
     */
    @JsonProperty("action-modify-header")
    @JsonPropertyDescription("Either replaces the value of an existing HTTP header with a new one or prepends/appends a value to the existing value of a header")
    private ActionModifyHeader__1 actionModifyHeader;
    /**
     * Remove header from the response
     * 
     */
    @JsonProperty("action-remove-header")
    @JsonPropertyDescription("Remove header from the response")
    private ActionRemoveHeader__1 actionRemoveHeader;
    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    @JsonPropertyDescription("Exits from a screening-case and terminates message screening processing")
    private ActionExitScreeningCase__1 actionExitScreeningCase;
    /**
     * Modify the response status-code
     * 
     */
    @JsonProperty("action-modify-status-code")
    @JsonPropertyDescription("Modify the response status-code")
    private ActionModifyStatusCode actionModifyStatusCode;
    /**
     * Jump to another response screening case
     * 
     */
    @JsonProperty("action-go-to")
    @JsonPropertyDescription("Jump to another response screening case")
    private ActionGoTo__1 actionGoTo;
    /**
     * Define log level for troubleshooting
     * 
     */
    @JsonProperty("action-log")
    @JsonPropertyDescription("Define log level for troubleshooting")
    private ActionLog__1 actionLog;
    /**
     * Performs modifications on JSON body elements by either adding new elements,
     * replacing the value of existing elements or removing the elements from the
     * JSON body
     * 
     */
    @JsonProperty("action-modify-json-body")
    @JsonPropertyDescription("Performs modifications on JSON body elements by either adding new elements, replacing the value of existing elements or removing the elements from the JSON body")
    private ActionModifyJsonBody__1 actionModifyJsonBody;
    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    @JsonPropertyDescription("Creates a valid JSON body from scratch")
    private ActionCreateJsonBody__1 actionCreateJsonBody;

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

    public ScreeningAction__1 withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Adds an HTTP header to a message
     * 
     */
    @JsonProperty("action-add-header")
    public ActionAddHeader__1 getActionAddHeader()
    {
        return actionAddHeader;
    }

    /**
     * Adds an HTTP header to a message
     * 
     */
    @JsonProperty("action-add-header")
    public void setActionAddHeader(ActionAddHeader__1 actionAddHeader)
    {
        this.actionAddHeader = actionAddHeader;
    }

    public ScreeningAction__1 withActionAddHeader(ActionAddHeader__1 actionAddHeader)
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
    public ActionModifyHeader__1 getActionModifyHeader()
    {
        return actionModifyHeader;
    }

    /**
     * Either replaces the value of an existing HTTP header with a new one or
     * prepends/appends a value to the existing value of a header
     * 
     */
    @JsonProperty("action-modify-header")
    public void setActionModifyHeader(ActionModifyHeader__1 actionModifyHeader)
    {
        this.actionModifyHeader = actionModifyHeader;
    }

    public ScreeningAction__1 withActionModifyHeader(ActionModifyHeader__1 actionModifyHeader)
    {
        this.actionModifyHeader = actionModifyHeader;
        return this;
    }

    /**
     * Remove header from the response
     * 
     */
    @JsonProperty("action-remove-header")
    public ActionRemoveHeader__1 getActionRemoveHeader()
    {
        return actionRemoveHeader;
    }

    /**
     * Remove header from the response
     * 
     */
    @JsonProperty("action-remove-header")
    public void setActionRemoveHeader(ActionRemoveHeader__1 actionRemoveHeader)
    {
        this.actionRemoveHeader = actionRemoveHeader;
    }

    public ScreeningAction__1 withActionRemoveHeader(ActionRemoveHeader__1 actionRemoveHeader)
    {
        this.actionRemoveHeader = actionRemoveHeader;
        return this;
    }

    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    public ActionExitScreeningCase__1 getActionExitScreeningCase()
    {
        return actionExitScreeningCase;
    }

    /**
     * Exits from a screening-case and terminates message screening processing
     * 
     */
    @JsonProperty("action-exit-screening-case")
    public void setActionExitScreeningCase(ActionExitScreeningCase__1 actionExitScreeningCase)
    {
        this.actionExitScreeningCase = actionExitScreeningCase;
    }

    public ScreeningAction__1 withActionExitScreeningCase(ActionExitScreeningCase__1 actionExitScreeningCase)
    {
        this.actionExitScreeningCase = actionExitScreeningCase;
        return this;
    }

    /**
     * Modify the response status-code
     * 
     */
    @JsonProperty("action-modify-status-code")
    public ActionModifyStatusCode getActionModifyStatusCode()
    {
        return actionModifyStatusCode;
    }

    /**
     * Modify the response status-code
     * 
     */
    @JsonProperty("action-modify-status-code")
    public void setActionModifyStatusCode(ActionModifyStatusCode actionModifyStatusCode)
    {
        this.actionModifyStatusCode = actionModifyStatusCode;
    }

    public ScreeningAction__1 withActionModifyStatusCode(ActionModifyStatusCode actionModifyStatusCode)
    {
        this.actionModifyStatusCode = actionModifyStatusCode;
        return this;
    }

    /**
     * Jump to another response screening case
     * 
     */
    @JsonProperty("action-go-to")
    public ActionGoTo__1 getActionGoTo()
    {
        return actionGoTo;
    }

    /**
     * Jump to another response screening case
     * 
     */
    @JsonProperty("action-go-to")
    public void setActionGoTo(ActionGoTo__1 actionGoTo)
    {
        this.actionGoTo = actionGoTo;
    }

    public ScreeningAction__1 withActionGoTo(ActionGoTo__1 actionGoTo)
    {
        this.actionGoTo = actionGoTo;
        return this;
    }

    /**
     * Define log level for troubleshooting
     * 
     */
    @JsonProperty("action-log")
    public ActionLog__1 getActionLog()
    {
        return actionLog;
    }

    /**
     * Define log level for troubleshooting
     * 
     */
    @JsonProperty("action-log")
    public void setActionLog(ActionLog__1 actionLog)
    {
        this.actionLog = actionLog;
    }

    public ScreeningAction__1 withActionLog(ActionLog__1 actionLog)
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
    public ActionModifyJsonBody__1 getActionModifyJsonBody()
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
    public void setActionModifyJsonBody(ActionModifyJsonBody__1 actionModifyJsonBody)
    {
        this.actionModifyJsonBody = actionModifyJsonBody;
    }

    public ScreeningAction__1 withActionModifyJsonBody(ActionModifyJsonBody__1 actionModifyJsonBody)
    {
        this.actionModifyJsonBody = actionModifyJsonBody;
        return this;
    }

    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    public ActionCreateJsonBody__1 getActionCreateJsonBody()
    {
        return actionCreateJsonBody;
    }

    /**
     * Creates a valid JSON body from scratch
     * 
     */
    @JsonProperty("action-create-json-body")
    public void setActionCreateJsonBody(ActionCreateJsonBody__1 actionCreateJsonBody)
    {
        this.actionCreateJsonBody = actionCreateJsonBody;
    }

    public ScreeningAction__1 withActionCreateJsonBody(ActionCreateJsonBody__1 actionCreateJsonBody)
    {
        this.actionCreateJsonBody = actionCreateJsonBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScreeningAction__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("actionExitScreeningCase");
        sb.append('=');
        sb.append(((this.actionExitScreeningCase == null) ? "<null>" : this.actionExitScreeningCase));
        sb.append(',');
        sb.append("actionModifyStatusCode");
        sb.append('=');
        sb.append(((this.actionModifyStatusCode == null) ? "<null>" : this.actionModifyStatusCode));
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
        result = ((result * 31) + ((this.actionModifyStatusCode == null) ? 0 : this.actionModifyStatusCode.hashCode()));
        result = ((result * 31) + ((this.actionAddHeader == null) ? 0 : this.actionAddHeader.hashCode()));
        result = ((result * 31) + ((this.actionGoTo == null) ? 0 : this.actionGoTo.hashCode()));
        result = ((result * 31) + ((this.actionCreateJsonBody == null) ? 0 : this.actionCreateJsonBody.hashCode()));
        result = ((result * 31) + ((this.actionRemoveHeader == null) ? 0 : this.actionRemoveHeader.hashCode()));
        result = ((result * 31) + ((this.actionExitScreeningCase == null) ? 0 : this.actionExitScreeningCase.hashCode()));
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
        if ((other instanceof ScreeningAction__1) == false)
        {
            return false;
        }
        ScreeningAction__1 rhs = ((ScreeningAction__1) other);
        return (((((((((((this.actionModifyStatusCode == rhs.actionModifyStatusCode)
                         || ((this.actionModifyStatusCode != null) && this.actionModifyStatusCode.equals(rhs.actionModifyStatusCode)))
                        && ((this.actionAddHeader == rhs.actionAddHeader)
                            || ((this.actionAddHeader != null) && this.actionAddHeader.equals(rhs.actionAddHeader))))
                       && ((this.actionGoTo == rhs.actionGoTo) || ((this.actionGoTo != null) && this.actionGoTo.equals(rhs.actionGoTo))))
                      && ((this.actionCreateJsonBody == rhs.actionCreateJsonBody)
                          || ((this.actionCreateJsonBody != null) && this.actionCreateJsonBody.equals(rhs.actionCreateJsonBody))))
                     && ((this.actionRemoveHeader == rhs.actionRemoveHeader)
                         || ((this.actionRemoveHeader != null) && this.actionRemoveHeader.equals(rhs.actionRemoveHeader))))
                    && ((this.actionExitScreeningCase == rhs.actionExitScreeningCase)
                        || ((this.actionExitScreeningCase != null) && this.actionExitScreeningCase.equals(rhs.actionExitScreeningCase))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.actionModifyJsonBody == rhs.actionModifyJsonBody)
                      || ((this.actionModifyJsonBody != null) && this.actionModifyJsonBody.equals(rhs.actionModifyJsonBody))))
                 && ((this.actionModifyHeader == rhs.actionModifyHeader)
                     || ((this.actionModifyHeader != null) && this.actionModifyHeader.equals(rhs.actionModifyHeader))))
                && ((this.actionLog == rhs.actionLog) || ((this.actionLog != null) && this.actionLog.equals(rhs.actionLog))));
    }

}
