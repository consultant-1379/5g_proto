
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HttpMethod
{

    GET("get"),
    PUT("put"),
    DELETE("delete"),
    POST("post"),
    HEAD("head"),
    CONNECT("connect"),
    OPTIONS("options"),
    PATCH("patch"),
    TRACE("trace");

    private final String value;
    private final static Map<String, HttpMethod> CONSTANTS = new HashMap<String, HttpMethod>();

    static
    {
        for (HttpMethod c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private HttpMethod(String value)
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
    public static HttpMethod fromValue(String value)
    {
        HttpMethod constant = CONSTANTS.get(value);
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
