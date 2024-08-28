
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType
{

    SERVICE_REQUEST("service-request"),
    CALLBACK("callback"),
    ANY("any");

    private final String value;
    private final static Map<String, MessageType> CONSTANTS = new HashMap<String, MessageType>();

    static
    {
        for (MessageType c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private MessageType(String value)
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
    public static MessageType fromValue(String value)
    {
        MessageType constant = CONSTANTS.get(value);
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
