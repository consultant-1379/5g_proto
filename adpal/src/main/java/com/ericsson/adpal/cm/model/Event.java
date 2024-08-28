
package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event
{

    CONFIG_CREATED("configCreated"),
    CONFIG_UPDATED("configUpdated"),
    CONFIG_DELETED("configDeleted");

    private final String value;
    private final static Map<String, Event> CONSTANTS = new HashMap<String, Event>();

    static
    {
        for (Event c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private Event(String value)
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
    public static Event fromValue(String value)
    {
        Event constant = CONSTANTS.get(value);
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
