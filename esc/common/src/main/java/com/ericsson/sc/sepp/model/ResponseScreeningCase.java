
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "message-data-ref", "screening-rule" })
public class ResponseScreeningCase implements IfNamedListItem
{

    /**
     * Name identifying the response-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the response-screening-case")
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
     * The screening rules are evaluated in sequence once the
     * response-screening-case is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    @JsonPropertyDescription("The screening rules are evaluated in sequence once the response-screening-case is referenced")
    private List<ScreeningRule__1> screeningRule = new ArrayList<ScreeningRule__1>();

    /**
     * Name identifying the response-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the response-screening-case (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ResponseScreeningCase withName(String name)
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

    public ResponseScreeningCase withUserLabel(String userLabel)
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

    public ResponseScreeningCase withMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
        return this;
    }

    /**
     * The screening rules are evaluated in sequence once the
     * response-screening-case is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    public List<ScreeningRule__1> getScreeningRule()
    {
        return screeningRule;
    }

    /**
     * The screening rules are evaluated in sequence once the
     * response-screening-case is referenced (Required)
     * 
     */
    @JsonProperty("screening-rule")
    public void setScreeningRule(List<ScreeningRule__1> screeningRule)
    {
        this.screeningRule = screeningRule;
    }

    public ResponseScreeningCase withScreeningRule(List<ScreeningRule__1> screeningRule)
    {
        this.screeningRule = screeningRule;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ResponseScreeningCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        result = ((result * 31) + ((this.messageDataRef == null) ? 0 : this.messageDataRef.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
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
        if ((other instanceof ResponseScreeningCase) == false)
        {
            return false;
        }
        ResponseScreeningCase rhs = ((ResponseScreeningCase) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.messageDataRef == rhs.messageDataRef) || ((this.messageDataRef != null) && this.messageDataRef.equals(rhs.messageDataRef))))
                && ((this.screeningRule == rhs.screeningRule) || ((this.screeningRule != null) && this.screeningRule.equals(rhs.screeningRule))));
    }

}
