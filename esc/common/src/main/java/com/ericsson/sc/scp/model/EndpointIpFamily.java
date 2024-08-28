
package com.ericsson.sc.scp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EndpointIpFamily
{

    IPV_4("ipv4"),
    IPV_6("ipv6");

    private final String value;
    private final static Map<String, EndpointIpFamily> CONSTANTS = new HashMap<String, EndpointIpFamily>();

    static
    {
        for (EndpointIpFamily c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private EndpointIpFamily(String value)
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
    public static EndpointIpFamily fromValue(String value)
    {
        EndpointIpFamily constant = CONSTANTS.get(value);
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
