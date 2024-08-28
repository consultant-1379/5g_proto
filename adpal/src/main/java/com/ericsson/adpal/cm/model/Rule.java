
package com.ericsson.adpal.cm.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "group", "rule" })
public class Rule
{

    /**
     * Arbitrary name assigned to the rule-list. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Arbitrary name assigned to the rule-list.")
    private String name;
    /**
     * List of administrative groups that will be assigned the associated access
     * rights defined by the 'rule' list. The string '*' indicates that all groups
     * apply to the entry.
     * 
     */
    @JsonProperty("group")
    @JsonPropertyDescription("List of administrative groups that will be assigned the associated access rights defined by the 'rule' list. The string '*' indicates that all groups apply to the entry.")
    private List<String> group = null;
    /**
     * One access control rule. Rules are processed in user-defined order until a
     * match is found. A rule matches if 'module-name', 'rule-type', and
     * 'access-operations' match the request. If a rule matches, the 'action' leaf
     * determines if access is granted or not.
     * 
     */
    @JsonProperty("rule")
    @JsonPropertyDescription("One access control rule. Rules are processed in user-defined order until a match is found. A rule matches if 'module-name', 'rule-type', and 'access-operations' match the request. If a rule matches, the 'action' leaf determines if access is granted or not.")
    private List<Rule__1> rule = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Rule()
    {
    }

    /**
     * 
     * @param name
     * @param rule
     * @param group
     */
    public Rule(String name,
                List<String> group,
                List<Rule__1> rule)
    {
        super();
        this.name = name;
        this.group = group;
        this.rule = rule;
    }

    /**
     * Arbitrary name assigned to the rule-list. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Arbitrary name assigned to the rule-list. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Rule withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * List of administrative groups that will be assigned the associated access
     * rights defined by the 'rule' list. The string '*' indicates that all groups
     * apply to the entry.
     * 
     */
    @JsonProperty("group")
    public Optional<List<String>> getGroup()
    {
        return Optional.ofNullable(group);
    }

    /**
     * List of administrative groups that will be assigned the associated access
     * rights defined by the 'rule' list. The string '*' indicates that all groups
     * apply to the entry.
     * 
     */
    @JsonProperty("group")
    public void setGroup(List<String> group)
    {
        this.group = group;
    }

    public Rule withGroup(List<String> group)
    {
        this.group = group;
        return this;
    }

    /**
     * One access control rule. Rules are processed in user-defined order until a
     * match is found. A rule matches if 'module-name', 'rule-type', and
     * 'access-operations' match the request. If a rule matches, the 'action' leaf
     * determines if access is granted or not.
     * 
     */
    @JsonProperty("rule")
    public Optional<List<Rule__1>> getRule()
    {
        return Optional.ofNullable(rule);
    }

    /**
     * One access control rule. Rules are processed in user-defined order until a
     * match is found. A rule matches if 'module-name', 'rule-type', and
     * 'access-operations' match the request. If a rule matches, the 'action' leaf
     * determines if access is granted or not.
     * 
     */
    @JsonProperty("rule")
    public void setRule(List<Rule__1> rule)
    {
        this.rule = rule;
    }

    public Rule withRule(List<Rule__1> rule)
    {
        this.rule = rule;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Rule.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("group");
        sb.append('=');
        sb.append(((this.group == null) ? "<null>" : this.group));
        sb.append(',');
        sb.append("rule");
        sb.append('=');
        sb.append(((this.rule == null) ? "<null>" : this.rule));
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
        result = ((result * 31) + ((this.rule == null) ? 0 : this.rule.hashCode()));
        result = ((result * 31) + ((this.group == null) ? 0 : this.group.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Rule) == false)
        {
            return false;
        }
        Rule rhs = ((Rule) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.rule == rhs.rule) || ((this.rule != null) && this.rule.equals(rhs.rule))))
                && ((this.group == rhs.group) || ((this.group != null) && this.group.equals(rhs.group))));
    }

}
