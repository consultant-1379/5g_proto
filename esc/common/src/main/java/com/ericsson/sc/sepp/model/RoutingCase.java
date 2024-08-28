
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfRoutingCase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "message-data-ref", "routing-rule" })
public class RoutingCase implements IfRoutingCase
{

    /**
     * Name identifying the routing-case (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the routing-case")
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
     * routing rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    @JsonPropertyDescription("Reference to defined message-data which has extracted the data to be used in routing rule conditions")
    private List<String> messageDataRef = new ArrayList<String>();
    /**
     * Routing rules executed in sequence (Required)
     * 
     */
    @JsonProperty("routing-rule")
    @JsonPropertyDescription("Routing rules executed in sequence")
    private List<RoutingRule> routingRule = new ArrayList<RoutingRule>();

    /**
     * Name identifying the routing-case (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the routing-case (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RoutingCase withName(String name)
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

    public RoutingCase withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Reference to defined message-data which has extracted the data to be used in
     * routing rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    public List<String> getMessageDataRef()
    {
        return messageDataRef;
    }

    /**
     * Reference to defined message-data which has extracted the data to be used in
     * routing rule conditions
     * 
     */
    @JsonProperty("message-data-ref")
    public void setMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
    }

    public RoutingCase withMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
        return this;
    }

    /**
     * Routing rules executed in sequence (Required)
     * 
     */
    @JsonProperty("routing-rule")
    public List<RoutingRule> getRoutingRule()
    {
        return routingRule;
    }

    /**
     * Routing rules executed in sequence (Required)
     * 
     */
    @JsonProperty("routing-rule")
    public void setRoutingRule(List<RoutingRule> routingRule)
    {
        this.routingRule = routingRule;
    }

    public RoutingCase withRoutingRule(List<RoutingRule> routingRule)
    {
        this.routingRule = routingRule;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoutingCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("routingRule");
        sb.append('=');
        sb.append(((this.routingRule == null) ? "<null>" : this.routingRule));
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
        result = ((result * 31) + ((this.routingRule == null) ? 0 : this.routingRule.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RoutingCase) == false)
        {
            return false;
        }
        RoutingCase rhs = ((RoutingCase) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.messageDataRef == rhs.messageDataRef) || ((this.messageDataRef != null) && this.messageDataRef.equals(rhs.messageDataRef))))
                && ((this.routingRule == rhs.routingRule) || ((this.routingRule != null) && this.routingRule.equals(rhs.routingRule))));
    }

}
