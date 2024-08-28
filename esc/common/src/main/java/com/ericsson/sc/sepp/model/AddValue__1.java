
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Adds a new element to the path specified by the json-pointer attribute. If
 * the element already exists in the JSON body, it provides the possibility to
 * either replace the existing value or not. If the path where the element is to
 * be added does not already exist in the JSON body, it provides the possibility
 * to either create the path and add the element or do nothing and not add the
 * element to the JSON body
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "value", "if-path-not-exists", "if-element-exists" })
public class AddValue__1
{

    /**
     * Specifies the value of the element to be added (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Specifies the value of the element to be added")
    private String value;
    /**
     * Specifies whether the non-existing path, specified by the json-pointer
     * attribute, will be created and the element will be added or nothing will be
     * done and neither the path nor the element will be added to the JSON body
     * 
     */
    @JsonProperty("if-path-not-exists")
    @JsonPropertyDescription("Specifies whether the non-existing path, specified by the json-pointer attribute, will be created and the element will be added or nothing will be done and neither the path nor the element will be added to the JSON body")
    private AddValue__1.IfPathNotExists ifPathNotExists = AddValue__1.IfPathNotExists.fromValue("create");
    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    @JsonPropertyDescription("Specifies whether the value of an existing element will be replaced or not")
    private AddValue__1.IfElementExists ifElementExists = AddValue__1.IfElementExists.fromValue("no-action");

    /**
     * Specifies the value of the element to be added (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * Specifies the value of the element to be added (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public AddValue__1 withValue(String value)
    {
        this.value = value;
        return this;
    }

    /**
     * Specifies whether the non-existing path, specified by the json-pointer
     * attribute, will be created and the element will be added or nothing will be
     * done and neither the path nor the element will be added to the JSON body
     * 
     */
    @JsonProperty("if-path-not-exists")
    public AddValue__1.IfPathNotExists getIfPathNotExists()
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
    public void setIfPathNotExists(AddValue__1.IfPathNotExists ifPathNotExists)
    {
        this.ifPathNotExists = ifPathNotExists;
    }

    public AddValue__1 withIfPathNotExists(AddValue__1.IfPathNotExists ifPathNotExists)
    {
        this.ifPathNotExists = ifPathNotExists;
        return this;
    }

    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    public AddValue__1.IfElementExists getIfElementExists()
    {
        return ifElementExists;
    }

    /**
     * Specifies whether the value of an existing element will be replaced or not
     * 
     */
    @JsonProperty("if-element-exists")
    public void setIfElementExists(AddValue__1.IfElementExists ifElementExists)
    {
        this.ifElementExists = ifElementExists;
    }

    public AddValue__1 withIfElementExists(AddValue__1.IfElementExists ifElementExists)
    {
        this.ifElementExists = ifElementExists;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AddValue__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
        sb.append(',');
        sb.append("ifPathNotExists");
        sb.append('=');
        sb.append(((this.ifPathNotExists == null) ? "<null>" : this.ifPathNotExists));
        sb.append(',');
        sb.append("ifElementExists");
        sb.append('=');
        sb.append(((this.ifElementExists == null) ? "<null>" : this.ifElementExists));
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
        result = ((result * 31) + ((this.ifPathNotExists == null) ? 0 : this.ifPathNotExists.hashCode()));
        result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
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
        if ((other instanceof AddValue__1) == false)
        {
            return false;
        }
        AddValue__1 rhs = ((AddValue__1) other);
        return ((((this.ifPathNotExists == rhs.ifPathNotExists) || ((this.ifPathNotExists != null) && this.ifPathNotExists.equals(rhs.ifPathNotExists)))
                 && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))))
                && ((this.ifElementExists == rhs.ifElementExists) || ((this.ifElementExists != null) && this.ifElementExists.equals(rhs.ifElementExists))));
    }

    public enum IfElementExists
    {

        REPLACE("replace"),
        NO_ACTION("no-action");

        private final String value;
        private final static Map<String, AddValue__1.IfElementExists> CONSTANTS = new HashMap<String, AddValue__1.IfElementExists>();

        static
        {
            for (AddValue__1.IfElementExists c : values())
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
        public static AddValue__1.IfElementExists fromValue(String value)
        {
            AddValue__1.IfElementExists constant = CONSTANTS.get(value);
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
        private final static Map<String, AddValue__1.IfPathNotExists> CONSTANTS = new HashMap<String, AddValue__1.IfPathNotExists>();

        static
        {
            for (AddValue__1.IfPathNotExists c : values())
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
        public static AddValue__1.IfPathNotExists fromValue(String value)
        {
            AddValue__1.IfPathNotExists constant = CONSTANTS.get(value);
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
