
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "message-data-ref", "screening-rule" })
public class RequestScreeningCase implements IfNamedListItem
{

    /**
     * Name identifying the request-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the request-screening-case")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Reference to defined message-data which has extracted the data to be used in
     * screening rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    @JsonPropertyDescription("Reference to defined message-data which has extracted the data to be used in screening rule conditions")
    private List<String> messageDataRef = new ArrayList<String>();
    /**
     * The screening rules are evaluated in sequence once the request-screening-case
     * is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    @JsonPropertyDescription("The screening rules are evaluated in sequence once the request-screening-case is referenced")
    private List<ScreeningRule> screeningRule = new ArrayList<ScreeningRule>();

    /**
     * Name identifying the request-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the request-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RequestScreeningCase withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public RequestScreeningCase withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Reference to defined message-data which has extracted the data to be used in
     * screening rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    public List<String> getMessageDataRef()
    {
        return messageDataRef;
    }

    /**
     * Reference to defined message-data which has extracted the data to be used in
     * screening rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    public void setMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
    }

    public RequestScreeningCase withMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
        return this;
    }

    /**
     * The screening rules are evaluated in sequence once the request-screening-case
     * is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    public List<ScreeningRule> getScreeningRule()
    {
        return screeningRule;
    }

    /**
     * The screening rules are evaluated in sequence once the request-screening-case
     * is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    public void setScreeningRule(List<ScreeningRule> screeningRule)
    {
        this.screeningRule = screeningRule;
    }

    public RequestScreeningCase withScreeningRule(List<ScreeningRule> screeningRule)
    {
        this.screeningRule = screeningRule;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RequestScreeningCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("messageDataRef");
        sb.append('=');
        sb.append(((this.messageDataRef == null) ? "<null>" : this.messageDataRef));
        sb.append(',');
        sb.append("screeningRule");
        sb.append('=');
        sb.append(((this.screeningRule == null) ? "<null>" : this.screeningRule));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.messageDataRef == null) ? 0 : this.messageDataRef.hashCode()));
        result = ((result * 31) + ((this.screeningRule == null) ? 0 : this.screeningRule.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RequestScreeningCase) == false)
        {
            return false;
        }
        RequestScreeningCase rhs = ((RequestScreeningCase) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.messageDataRef == rhs.messageDataRef) || ((this.messageDataRef != null) && this.messageDataRef.equals(rhs.messageDataRef))))
                && ((this.screeningRule == rhs.screeningRule) || ((this.screeningRule != null) && this.screeningRule.equals(rhs.screeningRule))));
    }

}
