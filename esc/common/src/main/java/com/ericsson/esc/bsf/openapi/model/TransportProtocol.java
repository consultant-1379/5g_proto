package com.ericsson.esc.bsf.openapi.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransportProtocol
{

    TCP("TCP");

    private final String value;

    TransportProtocol(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    public static TransportProtocol fromValue(String value)
    {
        for (TransportProtocol b : TransportProtocol.values())
        {
            if (b.value.equals(value))
            {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
