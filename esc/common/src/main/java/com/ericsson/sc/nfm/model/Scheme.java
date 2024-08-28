
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Scheme
{

    HTTP("http"),
    HTTPS("https");

    private final String value;
    private final static Map<String, Scheme> CONSTANTS = new HashMap<String, Scheme>();

    static
    {
        for (Scheme c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Scheme(String value)
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
    public static Scheme fromValue(String value)
    {
        Scheme constant = CONSTANTS.get(value);
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
