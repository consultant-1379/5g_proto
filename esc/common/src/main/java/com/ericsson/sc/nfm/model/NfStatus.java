
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NfStatus
{

    REGISTERED("registered"),
    SUSPENDED("suspended"),
    UNDISCOVERABLE("undiscoverable");

    private final String value;
    private final static Map<String, NfStatus> CONSTANTS = new HashMap<String, NfStatus>();

    static
    {
        for (NfStatus c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private NfStatus(String value)
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
    public static NfStatus fromValue(String value)
    {
        NfStatus constant = CONSTANTS.get(value);
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
