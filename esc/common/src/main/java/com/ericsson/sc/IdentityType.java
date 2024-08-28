
package com.ericsson.sc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentityType
{

    SUPI("supi"),
    GPSI("gpsi");

    private final String value;
    private final static Map<String, IdentityType> CONSTANTS = new HashMap<>();

    static
    {
        for (IdentityType c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private IdentityType(String value)
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
    public static IdentityType fromValue(String value)
    {
        IdentityType constant = CONSTANTS.get(value);
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
