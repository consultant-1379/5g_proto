
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.ericsson.sc.glue.IfActionLogBase;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Logs a user-defined message with the configured log-level
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "text", "max-log-message-length", "log-level" })
public class ActionLog__2 implements IfActionLogBase
{

    /**
     * The text to be used for troubleshooting (Required)
     * 
     */
    @JsonProperty("text")
    @JsonPropertyDescription("The text to be used for troubleshooting")
    private String text;
    /**
     * Max length of action-log message; any message longer than the limit will be
     * truncated and only the beginning up until the limit will be kept.
     * 
     */
    @JsonProperty("max-log-message-length")
    @JsonPropertyDescription("Max length of action-log message; any message longer than the limit will be truncated and only the beginning up until the limit will be kept.")
    private Integer maxLogMessageLength = 1000;
    /**
     * Define the log level for troubleshooting
     * 
     */
    @JsonProperty("log-level")
    @JsonPropertyDescription("Define the log level for troubleshooting")
    private ActionLog__2.LogLevel logLevel = ActionLog__2.LogLevel.fromValue("debug");

    /**
     * The text to be used for troubleshooting (Required)
     * 
     */
    @JsonProperty("text")
    public String getText()
    {
        return text;
    }

    /**
     * The text to be used for troubleshooting (Required)
     * 
     */
    @JsonProperty("text")
    public void setText(String text)
    {
        this.text = text;
    }

    public ActionLog__2 withText(String text)
    {
        this.text = text;
        return this;
    }

    /**
     * Max length of action-log message; any message longer than the limit will be
     * truncated and only the beginning up until the limit will be kept.
     * 
     */
    @JsonProperty("max-log-message-length")
    public Integer getMaxLogMessageLength()
    {
        return maxLogMessageLength;
    }

    /**
     * Max length of action-log message; any message longer than the limit will be
     * truncated and only the beginning up until the limit will be kept.
     * 
     */
    @JsonProperty("max-log-message-length")
    public void setMaxLogMessageLength(Integer maxLogMessageLength)
    {
        this.maxLogMessageLength = maxLogMessageLength;
    }

    public ActionLog__2 withMaxLogMessageLength(Integer maxLogMessageLength)
    {
        this.maxLogMessageLength = maxLogMessageLength;
        return this;
    }

    /**
     * Define the log level for troubleshooting
     * 
     */
    @JsonProperty("log-level")
    public ActionLog__2.LogLevel getLogLevel()
    {
        return logLevel;
    }

    /**
     * Define the log level for troubleshooting
     * 
     */
    @JsonProperty("log-level")
    public void setLogLevel(ActionLog__2.LogLevel logLevel)
    {
        this.logLevel = logLevel;
    }

    public ActionLog__2 withLogLevel(ActionLog__2.LogLevel logLevel)
    {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionLog__2.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("text");
        sb.append('=');
        sb.append(((this.text == null) ? "<null>" : this.text));
        sb.append(',');
        sb.append("maxLogMessageLength");
        sb.append('=');
        sb.append(((this.maxLogMessageLength == null) ? "<null>" : this.maxLogMessageLength));
        sb.append(',');
        sb.append("logLevel");
        sb.append('=');
        sb.append(((this.logLevel == null) ? "<null>" : this.logLevel));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.text == null) ? 0 : this.text.hashCode()));
        result = ((result * 31) + ((this.logLevel == null) ? 0 : this.logLevel.hashCode()));
        result = ((result * 31) + ((this.maxLogMessageLength == null) ? 0 : this.maxLogMessageLength.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionLog__2) == false)
        {
            return false;
        }
        ActionLog__2 rhs = ((ActionLog__2) other);
        return ((((this.text == rhs.text) || ((this.text != null) && this.text.equals(rhs.text)))
                 && ((this.logLevel == rhs.logLevel) || ((this.logLevel != null) && this.logLevel.equals(rhs.logLevel))))
                && ((this.maxLogMessageLength == rhs.maxLogMessageLength)
                    || ((this.maxLogMessageLength != null) && this.maxLogMessageLength.equals(rhs.maxLogMessageLength))));
    }

    public enum LogLevel
    {

        DEBUG("debug"),
        INFO("info"),
        TRACE("trace"),
        WARN("warn"),
        ERROR("error");

        private final String value;
        private final static Map<String, ActionLog__2.LogLevel> CONSTANTS = new HashMap<String, ActionLog__2.LogLevel>();

        static
        {
            for (ActionLog__2.LogLevel c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private LogLevel(String value)
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
        public static ActionLog__2.LogLevel fromValue(String value)
        {
            ActionLog__2.LogLevel constant = CONSTANTS.get(value);
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

}
