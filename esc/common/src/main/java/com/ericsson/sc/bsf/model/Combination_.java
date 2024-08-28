
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Combination_
{

    IPV_4("IPV4"),
    IPV_6("IPV6"),
    IP_DOMAIN("IP_DOMAIN");

    private final String value;
    private final static Map<String, Combination_> CONSTANTS = new HashMap<String, Combination_>();

    static
    {
        for (Combination_ c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Combination_(String value)
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
    public static Combination_ fromValue(String value)
    {
        Combination_ constant = CONSTANTS.get(value);
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
