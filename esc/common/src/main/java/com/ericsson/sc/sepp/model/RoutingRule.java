
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfRoutingRule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "condition", "routing-action" })
public class RoutingRule implements IfRoutingRule
{

    /**
     * Name identifying the routing-rule (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the routing-rule")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * The evaluation of the condition determines, using the collected data from
     * incoming requests, whether the actions in this routing rule shall be
     * executed. If empty, the condition always evaluates to true (Required)
     * 
     */
    @JsonProperty("condition")
    @JsonPropertyDescription("The evaluation of the condition determines, using the collected data from incoming requests, whether the actions in this routing rule shall be executed. If empty, the condition always evaluates to true")
    private String condition;
    /**
     * Routing actions that are executed in sequence once the routing rule is used
     * if the condition is true (Required)
     * 
     */
    @JsonProperty("routing-action")
    @JsonPropertyDescription("Routing actions that are executed in sequence once the routing rule is used if the condition is true")
    private List<RoutingAction> routingAction = new ArrayList<RoutingAction>();

    /**
     * Name identifying the routing-rule (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the routing-rule (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RoutingRule withName(String name)
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

    public RoutingRule withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * The evaluation of the condition determines, using the collected data from
     * incoming requests, whether the actions in this routing rule shall be
     * executed. If empty, the condition always evaluates to true (Required)
     * 
     */
    @JsonProperty("condition")
    public String getCondition()
    {
        return condition;
    }

    /**
     * The evaluation of the condition determines, using the collected data from
     * incoming requests, whether the actions in this routing rule shall be
     * executed. If empty, the condition always evaluates to true (Required)
     * 
     */
    @JsonProperty("condition")
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public RoutingRule withCondition(String condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     * Routing actions that are executed in sequence once the routing rule is used
     * if the condition is true (Required)
     * 
     */
    @JsonProperty("routing-action")
    public List<RoutingAction> getRoutingAction()
    {
        return routingAction;
    }

    /**
     * Routing actions that are executed in sequence once the routing rule is used
     * if the condition is true (Required)
     * 
     */
    @JsonProperty("routing-action")
    public void setRoutingAction(List<RoutingAction> routingAction)
    {
        this.routingAction = routingAction;
    }

    public RoutingRule withRoutingAction(List<RoutingAction> routingAction)
    {
        this.routingAction = routingAction;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoutingRule.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("condition");
        sb.append('=');
        sb.append(((this.condition == null) ? "<null>" : this.condition));
        sb.append(',');
        sb.append("routingAction");
        sb.append('=');
        sb.append(((this.routingAction == null) ? "<null>" : this.routingAction));
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
        result = ((result * 31) + ((this.routingAction == null) ? 0 : this.routingAction.hashCode()));
        result = ((result * 31) + ((this.condition == null) ? 0 : this.condition.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RoutingRule) == false)
        {
            return false;
        }
        RoutingRule rhs = ((RoutingRule) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.routingAction == rhs.routingAction) || ((this.routingAction != null) && this.routingAction.equals(rhs.routingAction))))
                && ((this.condition == rhs.condition) || ((this.condition != null) && this.condition.equals(rhs.condition))));
    }

}
