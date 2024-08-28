
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
 * Operational state of the N32-c handshake procedure in the responding SEPP.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "value", "reason" })
public class OperationalState
{

    @JsonProperty("value")
    private OperationalState.Value value = OperationalState.Value.fromValue("inactive");
    /**
     * Reason of failure. This in filled-in only in case of faulty operational
     * state.
     * 
     */
    @JsonProperty("reason")
    @JsonPropertyDescription("Reason of failure. This in filled-in only in case of faulty operational state.")
    private String reason;

    @JsonProperty("value")
    public OperationalState.Value getValue()
    {
        return value;
    }

    @JsonProperty("value")
    public void setValue(OperationalState.Value value)
    {
        this.value = value;
    }

    public OperationalState withValue(OperationalState.Value value)
    {
        this.value = value;
        return this;
    }

    /**
     * Reason of failure. This in filled-in only in case of faulty operational
     * state.
     * 
     */
    @JsonProperty("reason")
    public String getReason()
    {
        return reason;
    }

    /**
     * Reason of failure. This in filled-in only in case of faulty operational
     * state.
     * 
     */
    @JsonProperty("reason")
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public OperationalState withReason(String reason)
    {
        this.reason = reason;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OperationalState.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null) ? "<null>" : this.reason));
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
        result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
        result = ((result * 31) + ((this.reason == null) ? 0 : this.reason.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OperationalState) == false)
        {
            return false;
        }
        OperationalState rhs = ((OperationalState) other);
        return (((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value)))
                && ((this.reason == rhs.reason) || ((this.reason != null) && this.reason.equals(rhs.reason))));
    }

    public enum Value
    {

        INACTIVE("inactive"),
        ACTIVE("active"),
        FAULTY("faulty");

        private final String value;
        private final static Map<String, OperationalState.Value> CONSTANTS = new HashMap<String, OperationalState.Value>();

        static
        {
            for (OperationalState.Value c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Value(String value)
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
        public static OperationalState.Value fromValue(String value)
        {
            OperationalState.Value constant = CONSTANTS.get(value);
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
