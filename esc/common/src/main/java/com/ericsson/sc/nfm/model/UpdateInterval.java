
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UpdateInterval
{
    INFINITE("infinite"),
    _24_H("24h"),
    _12_H("12h"),
    _6_H("6h"),
    _4_H("4h"),
    _3_H("3h"),
    _2_H("2h"),
    _1_H("1h"),
    _30_MIN("30min"),
    _20_MIN("20min"),
    _15_MIN("15min"),
    _10_MIN("10min"),
    _5_MIN("5min"),
    _1_MIN("1min"),
    _30_S("30s"),
    _20_S("20s"),
    _15_S("15s"),
    _10_S("10s"),
    _5_S("5s");

    private final static Map<String, UpdateInterval> CONSTANTS = new HashMap<String, UpdateInterval>();
    private final static int SECOND = 1;
    private final static int MINUTE = 60 * SECOND;
    private final static int HOUR = 60 * MINUTE;
    private final static int INF = Integer.MAX_VALUE / 1000; // Prevent integer overflow if converted to milliseconds.

    private final String value;
    private final int seconds;

    static
    {
        for (UpdateInterval c : values())
            CONSTANTS.put(c.value, c);
    }

    private UpdateInterval(String value)
    {
        this.value = value;

        int seconds = INF;

        Pattern pattern = Pattern.compile("^([0-9]+)([a-z]+)$");
        Matcher matcher = pattern.matcher(value);

        if (matcher.matches())
        {
            int unit;

            switch (matcher.group(2))
            {
                case "h":
                    unit = HOUR;
                    break;

                case "min":
                    unit = MINUTE;
                    break;

                case "s":
                default:
                    unit = SECOND;
                    break;
            }

            seconds = Integer.valueOf(matcher.group(1)) * unit;
        }

        this.seconds = seconds;
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    @JsonIgnore
    public int seconds()
    {
        return this.seconds;
    }

    @JsonValue
    public String value()
    {
        return this.value;
    }

    @JsonCreator
    public static UpdateInterval fromValue(String value)
    {
        UpdateInterval constant = CONSTANTS.get(value);
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
