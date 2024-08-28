
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "out-message-handling" })
public class NfPeerInfo
{

    /**
     * A switch to define the header handling in a global level
     * 
     */
    @JsonProperty("out-message-handling")
    @JsonPropertyDescription("A switch to define the header handling in a global level")
    private NfPeerInfo.OutMessageHandling outMessageHandling = NfPeerInfo.OutMessageHandling.fromValue("off");

    /**
     * A switch to define the header handling in a global level
     * 
     */
    @JsonProperty("out-message-handling")
    public NfPeerInfo.OutMessageHandling getOutMessageHandling()
    {
        return outMessageHandling;
    }

    /**
     * A switch to define the header handling in a global level
     * 
     */
    @JsonProperty("out-message-handling")
    public void setOutMessageHandling(NfPeerInfo.OutMessageHandling outMessageHandling)
    {
        this.outMessageHandling = outMessageHandling;
    }

    public NfPeerInfo withOutMessageHandling(NfPeerInfo.OutMessageHandling outMessageHandling)
    {
        this.outMessageHandling = outMessageHandling;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfPeerInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("outMessageHandling");
        sb.append('=');
        sb.append(((this.outMessageHandling == null) ? "<null>" : this.outMessageHandling));
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
        result = ((result * 31) + ((this.outMessageHandling == null) ? 0 : this.outMessageHandling.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfPeerInfo) == false)
        {
            return false;
        }
        NfPeerInfo rhs = ((NfPeerInfo) other);
        return ((this.outMessageHandling == rhs.outMessageHandling)
                || ((this.outMessageHandling != null) && this.outMessageHandling.equals(rhs.outMessageHandling)));
    }

    public enum OutMessageHandling
    {

        ON("on"),
        OFF("off");

        private final String value;
        private final static Map<String, NfPeerInfo.OutMessageHandling> CONSTANTS = new HashMap<String, NfPeerInfo.OutMessageHandling>();

        static
        {
            for (NfPeerInfo.OutMessageHandling c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private OutMessageHandling(String value)
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
        public static NfPeerInfo.OutMessageHandling fromValue(String value)
        {
            NfPeerInfo.OutMessageHandling constant = CONSTANTS.get(value);
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
