
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
 * Adds a new element to the path specified by the json-pointer attribute. The
 * value of the new element is stored on a variable. If the element already
 * exists in the JSON body, it provides the possibility to either replace the
 * existing value or not. If the path where the element is to be added does not
 * already exist in the JSON body it provides the possibility to either create
 * the path and add the element or do nothing and not add the element to the
 * JSON body
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "if-path-not-exists", "if-element-exists", "variable" })
public class AddFromVarName
{

    /**
     * Specifies whether the non-existing path, specified by the json-pointer
     * attribute, will be created and the element will be added or nothing will be
     * done and neither the path nor the element will be added to the JSON body
     * 
     */
    @JsonProperty("if-path-not-exists")
    @JsonPropertyDescription("Specifies whether the non-existing path, specified by the json-pointer attribute, will be created and the element will be added or nothing will be done and neither the path nor the element will be added to the JSON body")
    private AddFromVarName.IfPathNotExists ifPathNotExists = AddFromVarName.IfPathNotExists.fromValue("create");
    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    @JsonPropertyDescription("Specifies whether the value of an existing element will be replaced or not")
    private AddFromVarName.IfElementExists ifElementExists = AddFromVarName.IfElementExists.fromValue("no-action");
    /**
     * Specifies the name of the variable from which to fetch the value to add to
     * the path specified by the json-pointer attribute (Required)
     * 
     */
    @JsonProperty("variable")
    @JsonPropertyDescription("Specifies the name of the variable from which to fetch the value to add to the path specified by the json-pointer attribute")
    private String variable;

    /**
     * Specifies whether the non-existing path, specified by the json-pointer
     * attribute, will be created and the element will be added or nothing will be
     * done and neither the path nor the element will be added to the JSON body
     * 
     */
    @JsonProperty("if-path-not-exists")
    public AddFromVarName.IfPathNotExists getIfPathNotExists()
    {
        return ifPathNotExists;
    }

    /**
     * Specifies whether the non-existing path, specified by the json-pointer
     * attribute, will be created and the element will be added or nothing will be
     * done and neither the path nor the element will be added to the JSON body
     * 
     */
    @JsonProperty("if-path-not-exists")
    public void setIfPathNotExists(AddFromVarName.IfPathNotExists ifPathNotExists)
    {
        this.ifPathNotExists = ifPathNotExists;
    }

    public AddFromVarName withIfPathNotExists(AddFromVarName.IfPathNotExists ifPathNotExists)
    {
        this.ifPathNotExists = ifPathNotExists;
        return this;
    }

    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    public AddFromVarName.IfElementExists getIfElementExists()
    {
        return ifElementExists;
    }

    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    public void setIfElementExists(AddFromVarName.IfElementExists ifElementExists)
    {
        this.ifElementExists = ifElementExists;
    }

    public AddFromVarName withIfElementExists(AddFromVarName.IfElementExists ifElementExists)
    {
        this.ifElementExists = ifElementExists;
        return this;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to add to
     * the path specified by the json-pointer attribute (Required)
     * 
     */
    @JsonProperty("variable")
    public String getVariable()
    {
        return variable;
    }

    /**
     * Specifies the name of the variable from which to fetch the value to add to
     * the path specified by the json-pointer attribute (Required)
     * 
     */
    @JsonProperty("variable")
    public void setVariable(String variable)
    {
        this.variable = variable;
    }

    public AddFromVarName withVariable(String variable)
    {
        this.variable = variable;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AddFromVarName.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ifPathNotExists");
        sb.append('=');
        sb.append(((this.ifPathNotExists == null) ? "<null>" : this.ifPathNotExists));
        sb.append(',');
        sb.append("ifElementExists");
        sb.append('=');
        sb.append(((this.ifElementExists == null) ? "<null>" : this.ifElementExists));
        sb.append(',');
        sb.append("variable");
        sb.append('=');
        sb.append(((this.variable == null) ? "<null>" : this.variable));
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
        result = ((result * 31) + ((this.variable == null) ? 0 : this.variable.hashCode()));
        result = ((result * 31) + ((this.ifPathNotExists == null) ? 0 : this.ifPathNotExists.hashCode()));
        result = ((result * 31) + ((this.ifElementExists == null) ? 0 : this.ifElementExists.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AddFromVarName) == false)
        {
            return false;
        }
        AddFromVarName rhs = ((AddFromVarName) other);
        return ((((this.variable == rhs.variable) || ((this.variable != null) && this.variable.equals(rhs.variable)))
                 && ((this.ifPathNotExists == rhs.ifPathNotExists) || ((this.ifPathNotExists != null) && this.ifPathNotExists.equals(rhs.ifPathNotExists))))
                && ((this.ifElementExists == rhs.ifElementExists) || ((this.ifElementExists != null) && this.ifElementExists.equals(rhs.ifElementExists))));
    }

    public enum IfElementExists
    {

        REPLACE("replace"),
        NO_ACTION("no-action");

        private final String value;
        private final static Map<String, AddFromVarName.IfElementExists> CONSTANTS = new HashMap<String, AddFromVarName.IfElementExists>();

        static
        {
            for (AddFromVarName.IfElementExists c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private IfElementExists(String value)
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
        public static AddFromVarName.IfElementExists fromValue(String value)
        {
            AddFromVarName.IfElementExists constant = CONSTANTS.get(value);
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

    public enum IfPathNotExists
    {

        CREATE("create"),
        NO_ACTION("no-action");

        private final String value;
        private final static Map<String, AddFromVarName.IfPathNotExists> CONSTANTS = new HashMap<String, AddFromVarName.IfPathNotExists>();

        static
        {
            for (AddFromVarName.IfPathNotExists c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private IfPathNotExists(String value)
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
        public static AddFromVarName.IfPathNotExists fromValue(String value)
        {
            AddFromVarName.IfPathNotExists constant = CONSTANTS.get(value);
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
