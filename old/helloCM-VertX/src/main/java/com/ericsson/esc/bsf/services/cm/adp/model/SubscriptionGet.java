
package com.ericsson.esc.bsf.services.cm.adp.model;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * Subscription
 * <p>
 * Subscription data for GET method
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "configName",
    "event",
    "updateNotificationFormat",
    "callback",
    "leaseSeconds"
})
public class SubscriptionGet {

    /**
     * The id of the subscription
     * (Required)
     *
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The id of the subscription")
    private String id;
    /**
     * The configuration name
     * (Required)
     *
     */
    @JsonProperty("configName")
    @JsonPropertyDescription("The configuration name")
    private String configName;
    /**
     * The subscribed events
     * (Required)
     *
     */
    @JsonProperty("event")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The subscribed events")
    private Set<Event> event = new LinkedHashSet<Event>();
    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     * (Required)
     *
     */
    @JsonProperty("updateNotificationFormat")
    @JsonPropertyDescription("The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.")
    private SubscriptionGet.UpdateNotificationFormat updateNotificationFormat;
    /**
     * The callback URI for receiving notifications
     * (Required)
     *
     */
    @JsonProperty("callback")
    @JsonPropertyDescription("The callback URI for receiving notifications")
    private URI callback;
    /**
     * The number of seconds the subscription is valid
     * (Required)
     *
     */
    @JsonProperty("leaseSeconds")
    @JsonPropertyDescription("The number of seconds the subscription is valid")
    private Integer leaseSeconds;

    /**
     * The id of the subscription
     * (Required)
     *
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * The id of the subscription
     * (Required)
     *
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public SubscriptionGet withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * The configuration name
     * (Required)
     *
     */
    @JsonProperty("configName")
    public String getConfigName() {
        return configName;
    }

    /**
     * The configuration name
     * (Required)
     *
     */
    @JsonProperty("configName")
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public SubscriptionGet withConfigName(String configName) {
        this.configName = configName;
        return this;
    }

    /**
     * The subscribed events
     * (Required)
     *
     */
    @JsonProperty("event")
    public Set<Event> getEvent() {
        return event;
    }

    /**
     * The subscribed events
     * (Required)
     *
     */
    @JsonProperty("event")
    public void setEvent(Set<Event> event) {
        this.event = event;
    }

    public SubscriptionGet withEvent(Set<Event> event) {
        this.event = event;
        return this;
    }

    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     * (Required)
     *
     */
    @JsonProperty("updateNotificationFormat")
    public SubscriptionGet.UpdateNotificationFormat getUpdateNotificationFormat() {
        return updateNotificationFormat;
    }

    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     * (Required)
     *
     */
    @JsonProperty("updateNotificationFormat")
    public void setUpdateNotificationFormat(SubscriptionGet.UpdateNotificationFormat updateNotificationFormat) {
        this.updateNotificationFormat = updateNotificationFormat;
    }

    public SubscriptionGet withUpdateNotificationFormat(SubscriptionGet.UpdateNotificationFormat updateNotificationFormat) {
        this.updateNotificationFormat = updateNotificationFormat;
        return this;
    }

    /**
     * The callback URI for receiving notifications
     * (Required)
     *
     */
    @JsonProperty("callback")
    public URI getCallback() {
        return callback;
    }

    /**
     * The callback URI for receiving notifications
     * (Required)
     *
     */
    @JsonProperty("callback")
    public void setCallback(URI callback) {
        this.callback = callback;
    }

    public SubscriptionGet withCallback(URI callback) {
        this.callback = callback;
        return this;
    }

    /**
     * The number of seconds the subscription is valid
     * (Required)
     *
     */
    @JsonProperty("leaseSeconds")
    public Integer getLeaseSeconds() {
        return leaseSeconds;
    }

    /**
     * The number of seconds the subscription is valid
     * (Required)
     *
     */
    @JsonProperty("leaseSeconds")
    public void setLeaseSeconds(Integer leaseSeconds) {
        this.leaseSeconds = leaseSeconds;
    }

    public SubscriptionGet withLeaseSeconds(Integer leaseSeconds) {
        this.leaseSeconds = leaseSeconds;
        return this;
    }

    public enum UpdateNotificationFormat {

        NONE("none"),
        FULL("full"),
        PATCH("patch");
        private final String value;
        private final static Map<String, SubscriptionGet.UpdateNotificationFormat> CONSTANTS = new HashMap<String, SubscriptionGet.UpdateNotificationFormat>();

        static {
            for (SubscriptionGet.UpdateNotificationFormat c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private UpdateNotificationFormat(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static SubscriptionGet.UpdateNotificationFormat fromValue(String value) {
            SubscriptionGet.UpdateNotificationFormat constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
