
package com.ericsson.sc.scp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PreserveIfIndirectRouting
{

    TARGET_API_ROOT_HEADER("target-api-root-header"),
    ABSOLUTE_URI_PATH("absolute-uri-path");

    private final String value;
    private final static Map<String, PreserveIfIndirectRouting> CONSTANTS = new HashMap<String, PreserveIfIndirectRouting>();

    static
    {
        for (PreserveIfIndirectRouting c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private PreserveIfIndirectRouting(String value)
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
    public static PreserveIfIndirectRouting fromValue(String value)
    {
        PreserveIfIndirectRouting constant = CONSTANTS.get(value);
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
