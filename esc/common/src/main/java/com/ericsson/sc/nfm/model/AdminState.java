
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdminState
{
    ACTIVE("active"),
    UNDISCOVERABLE("undiscoverable");

    private final String value;
    private final static Map<String, AdminState> CONSTANTS = new HashMap<String, AdminState>();

    static
    {
        for (AdminState c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private AdminState(String value)
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
    public static AdminState fromValue(String value)
    {
        AdminState constant = CONSTANTS.get(value);
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
