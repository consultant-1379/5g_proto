
package com.ericsson.esc.services.cm.model.diameter_adp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "enabled", "expression", "action", "user-label" })
public class RoutingEntry
{

    /**
     * Used to specify the key of the routing-entry instance. This attribute takes
     * as value the integer of the related routing rule/entry. This is how user can
     * control the order of evaluation of a routing rule/entry in the related
     * routing table. It is recommended to increment this value in steps of 100 or
     * 1000. In this way it is easy to inject further routing entries in an existing
     * routing table between 2 existing routing entries. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the routing-entry instance. This attribute takes as value the integer of the related routing rule/entry. This is how user can control the order of evaluation of a routing rule/entry in the related routing table. It is recommended to increment this value in steps of 100 or 1000. In this way it is easy to inject further routing entries in an existing routing table between 2 existing routing entries.")
    private Long id;
    /**
     * Used to enable or disable a routing rule/entry. When routing rule is disabled
     * the routing rule is inactive, therefore, skipped by the diameter routing
     * mechanism. When disabled, the following alarm is raised: ADP Diameter,
     * Managed Object Disabled Update Apply: Immediate. Update Effect: No effect on
     * already established peer connections but on routing information. Introduced
     * change will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable a routing rule/entry. When routing rule is disabled the routing rule is inactive, therefore, skipped by the diameter routing mechanism. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private Boolean enabled = true;
    /**
     * Used to specify the expression part of a routing rule. The expression is that
     * part of the routing rule the request message is matched against and evaluates
     * to either TRUE or FALSE. If it evaluates to TRUE the action of the routing
     * rule is executed. If it evaluates to FALSE the action of the routing rule is
     * skipped and the routing mechanism continues with matching the request message
     * against the expression of the next routing rule in the routing table. The
     * routing language elements are described by Diameter Managed Object Model User
     * Guide. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("expression")
    @JsonPropertyDescription("Used to specify the expression part of a routing rule. The expression is that part of the routing rule the request message is matched against and evaluates to either TRUE or FALSE. If it evaluates to TRUE the action of the routing rule is executed. If it evaluates to FALSE the action of the routing rule is skipped and the routing mechanism continues with matching the request message against the expression of the next routing rule in the routing table. The routing language elements are described by Diameter Managed Object Model User Guide. Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private String expression;
    /**
     * Used to specify the action part of a routing rule. The action is that part of
     * the routing rule which is executed if the expression part of the routing rule
     * evaluates to TRUE (that is, when the expression fires). The routing language
     * elements are described by Diameter Managed Object Model User Guide. Update
     * Apply: Immediate. Update Effect: No effect on already established peer
     * connections but on routing information. Introduced change will be applied
     * next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("action")
    @JsonPropertyDescription("Used to specify the action part of a routing rule. The action is that part of the routing rule which is executed if the expression part of the routing rule evaluates to TRUE (that is, when the expression fires). The routing language elements are described by Diameter Managed Object Model User Guide. Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private String action;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the routing-entry instance. This attribute takes
     * as value the integer of the related routing rule/entry. This is how user can
     * control the order of evaluation of a routing rule/entry in the related
     * routing table. It is recommended to increment this value in steps of 100 or
     * 1000. In this way it is easy to inject further routing entries in an existing
     * routing table between 2 existing routing entries. (Required)
     * 
     */
    @JsonProperty("id")
    public Long getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the routing-entry instance. This attribute takes
     * as value the integer of the related routing rule/entry. This is how user can
     * control the order of evaluation of a routing rule/entry in the related
     * routing table. It is recommended to increment this value in steps of 100 or
     * 1000. In this way it is easy to inject further routing entries in an existing
     * routing table between 2 existing routing entries. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Long id)
    {
        this.id = id;
    }

    public RoutingEntry withId(Long id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to enable or disable a routing rule/entry. When routing rule is disabled
     * the routing rule is inactive, therefore, skipped by the diameter routing
     * mechanism. When disabled, the following alarm is raised: ADP Diameter,
     * Managed Object Disabled Update Apply: Immediate. Update Effect: No effect on
     * already established peer connections but on routing information. Introduced
     * change will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable a routing rule/entry. When routing rule is disabled
     * the routing rule is inactive, therefore, skipped by the diameter routing
     * mechanism. When disabled, the following alarm is raised: ADP Diameter,
     * Managed Object Disabled Update Apply: Immediate. Update Effect: No effect on
     * already established peer connections but on routing information. Introduced
     * change will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public RoutingEntry withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Used to specify the expression part of a routing rule. The expression is that
     * part of the routing rule the request message is matched against and evaluates
     * to either TRUE or FALSE. If it evaluates to TRUE the action of the routing
     * rule is executed. If it evaluates to FALSE the action of the routing rule is
     * skipped and the routing mechanism continues with matching the request message
     * against the expression of the next routing rule in the routing table. The
     * routing language elements are described by Diameter Managed Object Model User
     * Guide. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("expression")
    public String getExpression()
    {
        return expression;
    }

    /**
     * Used to specify the expression part of a routing rule. The expression is that
     * part of the routing rule the request message is matched against and evaluates
     * to either TRUE or FALSE. If it evaluates to TRUE the action of the routing
     * rule is executed. If it evaluates to FALSE the action of the routing rule is
     * skipped and the routing mechanism continues with matching the request message
     * against the expression of the next routing rule in the routing table. The
     * routing language elements are described by Diameter Managed Object Model User
     * Guide. Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("expression")
    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public RoutingEntry withExpression(String expression)
    {
        this.expression = expression;
        return this;
    }

    /**
     * Used to specify the action part of a routing rule. The action is that part of
     * the routing rule which is executed if the expression part of the routing rule
     * evaluates to TRUE (that is, when the expression fires). The routing language
     * elements are described by Diameter Managed Object Model User Guide. Update
     * Apply: Immediate. Update Effect: No effect on already established peer
     * connections but on routing information. Introduced change will be applied
     * next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("action")
    public String getAction()
    {
        return action;
    }

    /**
     * Used to specify the action part of a routing rule. The action is that part of
     * the routing rule which is executed if the expression part of the routing rule
     * evaluates to TRUE (that is, when the expression fires). The routing language
     * elements are described by Diameter Managed Object Model User Guide. Update
     * Apply: Immediate. Update Effect: No effect on already established peer
     * connections but on routing information. Introduced change will be applied
     * next time a routing entry is evaluated. (Required)
     * 
     */
    @JsonProperty("action")
    public void setAction(String action)
    {
        this.action = action;
    }

    public RoutingEntry withAction(String action)
    {
        this.action = action;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public RoutingEntry withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RoutingEntry.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("expression");
        sb.append('=');
        sb.append(((this.expression == null) ? "<null>" : this.expression));
        sb.append(',');
        sb.append("action");
        sb.append('=');
        sb.append(((this.action == null) ? "<null>" : this.action));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.action == null) ? 0 : this.action.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.expression == null) ? 0 : this.expression.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RoutingEntry) == false)
        {
            return false;
        }
        RoutingEntry rhs = ((RoutingEntry) other);
        return ((((((this.action == rhs.action) || ((this.action != null) && this.action.equals(rhs.action)))
                   && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.expression == rhs.expression) || ((this.expression != null) && this.expression.equals(rhs.expression))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
