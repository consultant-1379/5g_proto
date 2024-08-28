
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Transport
{

    TCP("tcp");

    private final String value;
    private final static Map<String, Transport> CONSTANTS = new HashMap<String, Transport>();

    static
    {
        for (Transport c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Transport(String value)
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
    public static Transport fromValue(String value)
    {
        Transport constant = CONSTANTS.get(value);
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
