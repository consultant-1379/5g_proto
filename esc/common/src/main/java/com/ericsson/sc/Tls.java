
package com.ericsson.sc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Tls
{

    OFF("off"),
    MUTUAL("mutual");

    private final String value;
    private final static Map<String, Tls> CONSTANTS = new HashMap<>();

    static
    {
        for (Tls c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Tls(String value)
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
    public static Tls fromValue(String value)
    {
        Tls constant = CONSTANTS.get(value);
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
