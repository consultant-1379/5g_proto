
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "condition", "screening-action" })
public class ScreeningRule
{

    /**
     * Name identifying the screening-rule (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the screening-rule")
    private String name;
    /**
     * A logical expression which is evaluated using the collected data from
     * incoming requests as stored in message-data, and determines whether the
     * screening-rule shall be executed. If no condition is defined, the
     * screening-rule is always executed (Required)
     * 
     */
    @JsonProperty("condition")
    @JsonPropertyDescription("A logical expression which is evaluated using the collected data from incoming requests as stored in message-data, and determines whether the screening-rule shall be executed. If no condition is defined, the screening-rule is always executed")
    private String condition;
    /**
     * The screening-actions are executed in sequence once the condition of the
     * screening-rule evaluates to true (Required)
     * 
     */
    @JsonProperty("screening-action")
    @JsonPropertyDescription("The screening-actions are executed in sequence once the condition of the screening-rule evaluates to true")
    private List<ScreeningAction> screeningAction = new ArrayList<ScreeningAction>();

    /**
     * Name identifying the screening-rule (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the screening-rule (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ScreeningRule withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * A logical expression which is evaluated using the collected data from
     * incoming requests as stored in message-data, and determines whether the
     * screening-rule shall be executed. If no condition is defined, the
     * screening-rule is always executed (Required)
     * 
     */
    @JsonProperty("condition")
    public String getCondition()
    {
        return condition;
    }

    /**
     * A logical expression which is evaluated using the collected data from
     * incoming requests as stored in message-data, and determines whether the
     * screening-rule shall be executed. If no condition is defined, the
     * screening-rule is always executed (Required)
     * 
     */
    @JsonProperty("condition")
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public ScreeningRule withCondition(String condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     * The screening-actions are executed in sequence once the condition of the
     * screening-rule evaluates to true (Required)
     * 
     */
    @JsonProperty("screening-action")
    public List<ScreeningAction> getScreeningAction()
    {
        return screeningAction;
    }

    /**
     * The screening-actions are executed in sequence once the condition of the
     * screening-rule evaluates to true (Required)
     * 
     */
    @JsonProperty("screening-action")
    public void setScreeningAction(List<ScreeningAction> screeningAction)
    {
        this.screeningAction = screeningAction;
    }

    public ScreeningRule withScreeningAction(List<ScreeningAction> screeningAction)
    {
        this.screeningAction = screeningAction;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScreeningRule.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("condition");
        sb.append('=');
        sb.append(((this.condition == null) ? "<null>" : this.condition));
        sb.append(',');
        sb.append("screeningAction");
        sb.append('=');
        sb.append(((this.screeningAction == null) ? "<null>" : this.screeningAction));
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
        result = ((result * 31) + ((this.condition == null) ? 0 : this.condition.hashCode()));
        result = ((result * 31) + ((this.screeningAction == null) ? 0 : this.screeningAction.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScreeningRule) == false)
        {
            return false;
        }
        ScreeningRule rhs = ((ScreeningRule) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.condition == rhs.condition) || ((this.condition != null) && this.condition.equals(rhs.condition))))
                && ((this.screeningAction == rhs.screeningAction) || ((this.screeningAction != null) && this.screeningAction.equals(rhs.screeningAction))));
    }

}
