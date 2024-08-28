
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Combination
{

    IPV_4_ADDR("IPV4_ADDR"),
    IPV_6_PREFIX("IPV6_PREFIX"),
    MAC_ADDR_48("MAC_ADDR48"),
    IP_DOMAIN("IP_DOMAIN"),
    DNN("DNN"),
    SNSSAI("SNSSAI"),
    GPSI("GPSI"),
    SUPI("SUPI");

    private final String value;
    private final static Map<String, Combination> CONSTANTS = new HashMap<String, Combination>();

    static
    {
        for (Combination c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Combination(String value)
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
    public static Combination fromValue(String value)
    {
        Combination constant = CONSTANTS.get(value);
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
