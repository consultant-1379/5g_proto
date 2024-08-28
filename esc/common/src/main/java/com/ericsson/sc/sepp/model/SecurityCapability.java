
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SecurityCapability
{

    TLS("TLS"),
    PRINS("PRINS");

    private final String value;
    private final static Map<String, SecurityCapability> CONSTANTS = new HashMap<String, SecurityCapability>();

    static
    {
        for (SecurityCapability c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private SecurityCapability(String value)
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
    public static SecurityCapability fromValue(String value)
    {
        SecurityCapability constant = CONSTANTS.get(value);
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
