
package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "name", "module-name", "access-operations", "action", "comment", "rpc-name", "notification-name", "path" })
public class Rule__1
{

    /**
     * Arbitrary name assigned to the rule. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Arbitrary name assigned to the rule.")
    private String name;
    /**
     * Name of the module associated with this rule. This leaf matches if it has the
     * value '*' or if the object being accessed is defined in the module with the
     * specified module name.
     * 
     */
    @JsonProperty("module-name")
    @JsonPropertyDescription("Name of the module associated with this rule. This leaf matches if it has the value '*' or if the object being accessed is defined in the module with the specified module name.")
    private String moduleName = "*";
    /**
     * Access operations associated with this rule. This leaf matches if it has the
     * value '*' or if the bit corresponding to the requested operation is set.
     * 
     */
    @JsonProperty("access-operations")
    @JsonPropertyDescription("Access operations associated with this rule. This leaf matches if it has the value '*' or if the bit corresponding to the requested operation is set.")
    private String accessOperations = "*";
    /**
     * The access control action associated with the rule. If a rule is determined
     * to match a particular request, then this object is used to determine whether
     * to permit or deny the request. (Required)
     * 
     */
    @JsonProperty("action")
    @JsonPropertyDescription("The access control action associated with the rule. If a rule is determined to match a particular request, then this object is used to determine whether to permit or deny the request.")
    private Rule__1.Action action;
    /**
     * A textual description of the access rule.
     * 
     */
    @JsonProperty("comment")
    @JsonPropertyDescription("A textual description of the access rule.")
    private String comment;
    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested protocol operation name.
     * 
     */
    @JsonProperty("rpc-name")
    @JsonPropertyDescription("This leaf matches if it has the value '*' or if its value equals the requested protocol operation name.")
    private String rpcName;
    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested notification name.
     * 
     */
    @JsonProperty("notification-name")
    @JsonPropertyDescription("This leaf matches if it has the value '*' or if its value equals the requested notification name.")
    private String notificationName;
    /**
     * Data Node Instance Identifier associated with the data node controlled by
     * this rule. Configuration data or state data instance identifiers start with a
     * top-level data node. A complete instance identifier is required for this type
     * of path value. The special value '/' refers to all possible datastore
     * contents. (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("Data Node Instance Identifier associated with the data node controlled by this rule. Configuration data or state data instance identifiers start with a top-level data node. A complete instance identifier is required for this type of path value. The special value '/' refers to all possible datastore contents.")
    private String path;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Rule__1()
    {
    }

    /**
     * 
     * @param accessOperations
     * @param path
     * @param name
     * @param moduleName
     * @param action
     * @param comment
     * @param rpcName
     * @param notificationName
     */
    public Rule__1(String name,
                   String moduleName,
                   String accessOperations,
                   Rule__1.Action action,
                   String comment,
                   String rpcName,
                   String notificationName,
                   String path)
    {
        super();
        this.name = name;
        this.moduleName = moduleName;
        this.accessOperations = accessOperations;
        this.action = action;
        this.comment = comment;
        this.rpcName = rpcName;
        this.notificationName = notificationName;
        this.path = path;
    }

    /**
     * Arbitrary name assigned to the rule. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Arbitrary name assigned to the rule. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Rule__1 withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Name of the module associated with this rule. This leaf matches if it has the
     * value '*' or if the object being accessed is defined in the module with the
     * specified module name.
     * 
     */
    @JsonProperty("module-name")
    public Optional<String> getModuleName()
    {
        return Optional.ofNullable(moduleName);
    }

    /**
     * Name of the module associated with this rule. This leaf matches if it has the
     * value '*' or if the object being accessed is defined in the module with the
     * specified module name.
     * 
     */
    @JsonProperty("module-name")
    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public Rule__1 withModuleName(String moduleName)
    {
        this.moduleName = moduleName;
        return this;
    }

    /**
     * Access operations associated with this rule. This leaf matches if it has the
     * value '*' or if the bit corresponding to the requested operation is set.
     * 
     */
    @JsonProperty("access-operations")
    public Optional<String> getAccessOperations()
    {
        return Optional.ofNullable(accessOperations);
    }

    /**
     * Access operations associated with this rule. This leaf matches if it has the
     * value '*' or if the bit corresponding to the requested operation is set.
     * 
     */
    @JsonProperty("access-operations")
    public void setAccessOperations(String accessOperations)
    {
        this.accessOperations = accessOperations;
    }

    public Rule__1 withAccessOperations(String accessOperations)
    {
        this.accessOperations = accessOperations;
        return this;
    }

    /**
     * The access control action associated with the rule. If a rule is determined
     * to match a particular request, then this object is used to determine whether
     * to permit or deny the request. (Required)
     * 
     */
    @JsonProperty("action")
    public Rule__1.Action getAction()
    {
        return action;
    }

    /**
     * The access control action associated with the rule. If a rule is determined
     * to match a particular request, then this object is used to determine whether
     * to permit or deny the request. (Required)
     * 
     */
    @JsonProperty("action")
    public void setAction(Rule__1.Action action)
    {
        this.action = action;
    }

    public Rule__1 withAction(Rule__1.Action action)
    {
        this.action = action;
        return this;
    }

    /**
     * A textual description of the access rule.
     * 
     */
    @JsonProperty("comment")
    public Optional<String> getComment()
    {
        return Optional.ofNullable(comment);
    }

    /**
     * A textual description of the access rule.
     * 
     */
    @JsonProperty("comment")
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Rule__1 withComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested protocol operation name.
     * 
     */
    @JsonProperty("rpc-name")
    public Optional<String> getRpcName()
    {
        return Optional.ofNullable(rpcName);
    }

    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested protocol operation name.
     * 
     */
    @JsonProperty("rpc-name")
    public void setRpcName(String rpcName)
    {
        this.rpcName = rpcName;
    }

    public Rule__1 withRpcName(String rpcName)
    {
        this.rpcName = rpcName;
        return this;
    }

    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested notification name.
     * 
     */
    @JsonProperty("notification-name")
    public Optional<String> getNotificationName()
    {
        return Optional.ofNullable(notificationName);
    }

    /**
     * This leaf matches if it has the value '*' or if its value equals the
     * requested notification name.
     * 
     */
    @JsonProperty("notification-name")
    public void setNotificationName(String notificationName)
    {
        this.notificationName = notificationName;
    }

    public Rule__1 withNotificationName(String notificationName)
    {
        this.notificationName = notificationName;
        return this;
    }

    /**
     * Data Node Instance Identifier associated with the data node controlled by
     * this rule. Configuration data or state data instance identifiers start with a
     * top-level data node. A complete instance identifier is required for this type
     * of path value. The special value '/' refers to all possible datastore
     * contents. (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath()
    {
        return path;
    }

    /**
     * Data Node Instance Identifier associated with the data node controlled by
     * this rule. Configuration data or state data instance identifiers start with a
     * top-level data node. A complete instance identifier is required for this type
     * of path value. The special value '/' refers to all possible datastore
     * contents. (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path)
    {
        this.path = path;
    }

    public Rule__1 withPath(String path)
    {
        this.path = path;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Rule__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("moduleName");
        sb.append('=');
        sb.append(((this.moduleName == null) ? "<null>" : this.moduleName));
        sb.append(',');
        sb.append("accessOperations");
        sb.append('=');
        sb.append(((this.accessOperations == null) ? "<null>" : this.accessOperations));
        sb.append(',');
        sb.append("action");
        sb.append('=');
        sb.append(((this.action == null) ? "<null>" : this.action));
        sb.append(',');
        sb.append("comment");
        sb.append('=');
        sb.append(((this.comment == null) ? "<null>" : this.comment));
        sb.append(',');
        sb.append("rpcName");
        sb.append('=');
        sb.append(((this.rpcName == null) ? "<null>" : this.rpcName));
        sb.append(',');
        sb.append("notificationName");
        sb.append('=');
        sb.append(((this.notificationName == null) ? "<null>" : this.notificationName));
        sb.append(',');
        sb.append("path");
        sb.append('=');
        sb.append(((this.path == null) ? "<null>" : this.path));
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
        result = ((result * 31) + ((this.accessOperations == null) ? 0 : this.accessOperations.hashCode()));
        result = ((result * 31) + ((this.path == null) ? 0 : this.path.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.moduleName == null) ? 0 : this.moduleName.hashCode()));
        result = ((result * 31) + ((this.action == null) ? 0 : this.action.hashCode()));
        result = ((result * 31) + ((this.comment == null) ? 0 : this.comment.hashCode()));
        result = ((result * 31) + ((this.rpcName == null) ? 0 : this.rpcName.hashCode()));
        result = ((result * 31) + ((this.notificationName == null) ? 0 : this.notificationName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Rule__1) == false)
        {
            return false;
        }
        Rule__1 rhs = ((Rule__1) other);
        return (((((((((this.accessOperations == rhs.accessOperations)
                       || ((this.accessOperations != null) && this.accessOperations.equals(rhs.accessOperations)))
                      && ((this.path == rhs.path) || ((this.path != null) && this.path.equals(rhs.path))))
                     && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                    && ((this.moduleName == rhs.moduleName) || ((this.moduleName != null) && this.moduleName.equals(rhs.moduleName))))
                   && ((this.action == rhs.action) || ((this.action != null) && this.action.equals(rhs.action))))
                  && ((this.comment == rhs.comment) || ((this.comment != null) && this.comment.equals(rhs.comment))))
                 && ((this.rpcName == rhs.rpcName) || ((this.rpcName != null) && this.rpcName.equals(rhs.rpcName))))
                && ((this.notificationName == rhs.notificationName)
                    || ((this.notificationName != null) && this.notificationName.equals(rhs.notificationName))));
    }

    /**
     * The access control action associated with the rule. If a rule is determined
     * to match a particular request, then this object is used to determine whether
     * to permit or deny the request.
     * 
     */
    public enum Action
    {

        PERMIT("permit"),
        DENY("deny");

        private final String value;
        private final static Map<String, Rule__1.Action> CONSTANTS = new HashMap<String, Rule__1.Action>();

        static
        {
            for (Rule__1.Action c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Action(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Rule__1.Action fromValue(String value)
        {
            Rule__1.Action constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
