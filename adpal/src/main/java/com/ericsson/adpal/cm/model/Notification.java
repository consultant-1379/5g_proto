/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 5, 2018
 *     Author: eedstl
 */

package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Notification
 * <p>
 * Notification data for schema and configuration events
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "configName", "schemaName", "event", "configETag", "baseETag", "data", "patch" })
public class Notification
{
    /**
     * The configuration name, only available for configuration events
     */
    @JsonProperty("configName")
    @JsonPropertyDescription("The configuration name, only available for configuration events")
    private String configName;

    /**
     * The schema name, only available for schema events
     */
    @JsonProperty("schemaName")
    @JsonPropertyDescription("The schema name, only available for schema events")
    private String schemaName;

    /**
     * The event triggered (Required)
     */
    @JsonProperty("event")
    @JsonPropertyDescription("The event triggered")
    private Notification.Event event;

    /**
     * The ETag value of the configuration, only available for configCreated and
     * configUpdated events
     */
    @JsonProperty("configETag")
    @JsonPropertyDescription("The ETag value of the configuration, only available for configCreated and configUpdated events")
    private String configETag;

    /**
     * The ETag value of the configuration this change is based upon, only available
     * for configUpdated events
     */
    @JsonProperty("baseETag")
    @JsonPropertyDescription("The ETag value of the configuration this change is based upon, only available for configUpdated events")
    private String baseETag;

    /**
     * Configuration data in case of configUpdated event and
     * updateNotificationFormat is 'full'
     */
    @JsonProperty("data")
    @JsonPropertyDescription("Configuration data in case of configUpdated event and updateNotificationFormat is 'full'")
    private Data data;

    /**
     * A JSON Patch of configuration data changes in case of configUpdated event and
     * updateNotificationFormat is 'patch'
     */
    @JsonProperty("patch")
    @JsonPropertyDescription("A JSON Patch of configuration data changes in case of configUpdated event and updateNotificationFormat is 'patch'")
    private JsonPatch patch;

    /**
     * No args constructor for use in serialization
     */
    public Notification()
    {
    }

    /**
     * @param configETag
     * @param configName
     * @param baseETag
     * @param schemaName
     * @param event
     * @param data
     * @param patch
     */
    public Notification(String configName,
                        String schemaName,
                        Notification.Event event,
                        String configETag,
                        String baseETag,
                        Data data,
                        JsonPatch patch)
    {
        super();
        this.configName = configName;
        this.schemaName = schemaName;
        this.event = event;
        this.configETag = configETag;
        this.baseETag = baseETag;
        this.data = data;
        this.patch = patch;
    }

    /**
     * The configuration name, only available for configuration events
     */
    @JsonProperty("configName")
    public String getConfigName()
    {
        return configName;
    }

    /**
     * The configuration name, only available for configuration events
     */
    @JsonProperty("configName")
    public void setConfigName(String configName)
    {
        this.configName = configName;
    }

    /**
     * The schema name, only available for schema events
     */
    @JsonProperty("schemaName")
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * The schema name, only available for schema events
     */
    @JsonProperty("schemaName")
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * The event triggered (Required)
     */
    @JsonProperty("event")
    public Notification.Event getEvent()
    {
        return event;
    }

    /**
     * The event triggered (Required)
     */
    @JsonProperty("event")
    public void setEvent(Notification.Event event)
    {
        this.event = event;
    }

    /**
     * The ETag value of the configuration, only available for configCreated and
     * configUpdated events
     */
    @JsonProperty("configETag")
    public String getConfigETag()
    {
        return configETag;
    }

    /**
     * The ETag value of the configuration, only available for configCreated and
     * configUpdated events
     */
    @JsonProperty("configETag")
    public void setConfigETag(String configETag)
    {
        this.configETag = configETag;
    }

    /**
     * The ETag value of the configuration this change is based upon, only available
     * for configUpdated events
     */
    @JsonProperty("baseETag")
    public String getBaseETag()
    {
        return baseETag;
    }

    /**
     * The ETag value of the configuration this change is based upon, only available
     * for configUpdated events
     */
    @JsonProperty("baseETag")
    public void setBaseETag(String baseETag)
    {
        this.baseETag = baseETag;
    }

    /**
     * Configuration data in case of configUpdated event and
     * updateNotificationFormat is 'full'
     */
    @JsonProperty("data")
    public Data getData()
    {
        return data;
    }

    /**
     * Configuration data in case of configUpdated event and
     * updateNotificationFormat is 'full'
     */
    @JsonProperty("data")
    public void setData(Data data)
    {
        this.data = data;
    }

    /**
     * A JSON Patch of configuration data changes in case of configUpdated event and
     * updateNotificationFormat is 'patch'
     */
    @JsonProperty("patch")
    public JsonPatch getPatch()
    {
        return patch;
    }

    /**
     * A JSON Patch of configuration data changes in case of configUpdated event and
     * updateNotificationFormat is 'patch'
     */
    @JsonProperty("patch")
    public void setPatch(JsonPatch patch)
    {
        this.patch = patch;
    }

    public enum Event
    {
        CONFIG_CREATED("configCreated"),
        CONFIG_UPDATED("configUpdated"),
        CONFIG_DELETED("configDeleted"),
        SCHEMA_CREATED("schemaCreated"),
        SCHEMA_UPDATED("schemaUpdated"),
        SCHEMA_DELETED("schemaDeleted");

        private final String value;
        private static final Map<String, Notification.Event> CONSTANTS = new HashMap<>();

        static
        {
            for (Notification.Event c : values())
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
        public static Notification.Event fromValue(String value)
        {
            Notification.Event constant = CONSTANTS.get(value);
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
