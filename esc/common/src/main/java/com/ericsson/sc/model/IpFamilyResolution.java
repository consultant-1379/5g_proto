
package com.ericsson.sc.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IpFamilyResolution
{

    IPV4("ipv4"),
    IPV6("ipv6");

    private final String value;
    private final static Map<String, IpFamilyResolution> CONSTANTS = new HashMap<String, IpFamilyResolution>();

    static
    {
        for (IpFamilyResolution c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private IpFamilyResolution(String value)
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
    public static IpFamilyResolution fromValue(String value)
    {
        IpFamilyResolution constant = CONSTANTS.get(value);
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
