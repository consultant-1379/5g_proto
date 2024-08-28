
package com.ericsson.sc.scp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Adds an HTTP header to a message
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "if-exists", "value" })
public class ActionAddHeader
{

    /**
     * Specifies the header to be added to the request (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Specifies the header to be added to the request")
    private String name;
    /**
     * Defines the behavior when the header exists
     * 
     */
    @JsonProperty("if-exists")
    @JsonPropertyDescription("Defines the behavior when the header exists")
    private ActionAddHeader.IfExists ifExists = ActionAddHeader.IfExists.fromValue("no-action");
    /**
     * Specifies the value of the header to be added to the request (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Specifies the value of the header to be added to the request")
    private String value;

    /**
     * Specifies the header to be added to the request (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Specifies the header to be added to the request (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public ActionAddHeader withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Defines the behavior when the header exists
     * 
     */
    @JsonProperty("if-exists")
    public ActionAddHeader.IfExists getIfExists()
    {
        return ifExists;
    }

    /**
     * Defines the behavior when the header exists
     * 
     */
    @JsonProperty("if-exists")
    public void setIfExists(ActionAddHeader.IfExists ifExists)
    {
        this.ifExists = ifExists;
    }

    public ActionAddHeader withIfExists(ActionAddHeader.IfExists ifExists)
    {
        this.ifExists = ifExists;
        return this;
    }

    /**
     * Specifies the value of the header to be added to the request (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * Specifies the value of the header to be added to the request (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public ActionAddHeader withValue(String value)
    {
        this.value = value;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionAddHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("ifExists");
        sb.append('=');
        sb.append(((this.ifExists == null) ? "<null>" : this.ifExists));
        sb.append(',');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
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
        result = ((result * 31) + ((this.ifExists == null) ? 0 : this.ifExists.hashCode()));
        result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionAddHeader) == false)
        {
            return false;
        }
        ActionAddHeader rhs = ((ActionAddHeader) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.ifExists == rhs.ifExists) || ((this.ifExists != null) && this.ifExists.equals(rhs.ifExists))))
                && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
    }

    public enum IfExists
    {

        REPLACE("replace"),
        ADD("add"),
        NO_ACTION("no-action");

        private final String value;
        private final static Map<String, ActionAddHeader.IfExists> CONSTANTS = new HashMap<String, ActionAddHeader.IfExists>();

        static
        {
            for (ActionAddHeader.IfExists c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private IfExists(String value)
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
        public static ActionAddHeader.IfExists fromValue(String value)
        {
            ActionAddHeader.IfExists constant = CONSTANTS.get(value);
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
