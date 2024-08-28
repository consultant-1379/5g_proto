
package com.ericsson.sc.bsf.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Configuration for full table scan feature.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "configuration", "schedule" })
public class BindingDatabaseScan
{

    /**
     * Configuration for table scan triggering.
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("Configuration for table scan triggering.")
    private BindingDatabaseScan.Configuration configuration = BindingDatabaseScan.Configuration.fromValue("auto");
    /**
     * Cron format used to specify schedule time only when configuration is set to
     * scheduled.
     * 
     */
    @JsonProperty("schedule")
    @JsonPropertyDescription("Cron format used to specify schedule time only when configuration is set to scheduled.")
    private String schedule;

    /**
     * Configuration for table scan triggering.
     * 
     */
    @JsonProperty("configuration")
    public BindingDatabaseScan.Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Configuration for table scan triggering.
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(BindingDatabaseScan.Configuration configuration)
    {
        this.configuration = configuration;
    }

    public BindingDatabaseScan withConfiguration(BindingDatabaseScan.Configuration configuration)
    {
        this.configuration = configuration;
        return this;
    }

    /**
     * Cron format used to specify schedule time only when configuration is set to
     * scheduled.
     * 
     */
    @JsonProperty("schedule")
    public String getSchedule()
    {
        return schedule;
    }

    /**
     * Cron format used to specify schedule time only when configuration is set to
     * scheduled.
     * 
     */
    @JsonProperty("schedule")
    public void setSchedule(String schedule)
    {
        this.schedule = schedule;
    }

    public BindingDatabaseScan withSchedule(String schedule)
    {
        this.schedule = schedule;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BindingDatabaseScan.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("configuration");
        sb.append('=');
        sb.append(((this.configuration == null) ? "<null>" : this.configuration));
        sb.append(',');
        sb.append("schedule");
        sb.append('=');
        sb.append(((this.schedule == null) ? "<null>" : this.schedule));
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
        result = ((result * 31) + ((this.configuration == null) ? 0 : this.configuration.hashCode()));
        result = ((result * 31) + ((this.schedule == null) ? 0 : this.schedule.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof BindingDatabaseScan) == false)
        {
            return false;
        }
        BindingDatabaseScan rhs = ((BindingDatabaseScan) other);
        return (((this.configuration == rhs.configuration) || ((this.configuration != null) && this.configuration.equals(rhs.configuration)))
                && ((this.schedule == rhs.schedule) || ((this.schedule != null) && this.schedule.equals(rhs.schedule))));
    }

    public enum Configuration
    {

        AUTO("auto"),
        SCHEDULED("scheduled"),
        DISABLED("disabled");

        private final String value;
        private final static Map<String, BindingDatabaseScan.Configuration> CONSTANTS = new HashMap<String, BindingDatabaseScan.Configuration>();

        static
        {
            for (BindingDatabaseScan.Configuration c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Configuration(String value)
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
        public static BindingDatabaseScan.Configuration fromValue(String value)
        {
            BindingDatabaseScan.Configuration constant = CONSTANTS.get(value);
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
