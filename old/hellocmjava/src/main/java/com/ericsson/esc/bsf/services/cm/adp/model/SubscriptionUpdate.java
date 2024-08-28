
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
 * Subscription data for put method
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configName",
    "event",
    "updateNotificationFormat",
    "leaseSeconds",
    "callback"
})
public class SubscriptionUpdate {

    /**
     * The configuration name, use '*' to subscribe on all configurations
     * (Required)
     *
     */
    @JsonProperty("configName")
    @JsonPropertyDescription("The configuration name, use '*' to subscribe on all configurations")
    private String configName;
    /**
     * The events to subscribe to
     * (Required)
     *
     */
    @JsonProperty("event")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The events to subscribe to")
    private Set<Event> event = new LinkedHashSet<Event>();
    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     *
     */
    @JsonProperty("updateNotificationFormat")
    @JsonPropertyDescription("The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.")
    private SubscriptionUpdate.UpdateNotificationFormat updateNotificationFormat = SubscriptionUpdate.UpdateNotificationFormat.fromValue("none");
    /**
     * The number of seconds the subscription should be valid.
     *
     */
    @JsonProperty("leaseSeconds")
    @JsonPropertyDescription("The number of seconds the subscription should be valid.")
    private Integer leaseSeconds = 3600;
    /**
     * The callback URI for receiving notifications. HTTP Notifications will be POSTed to the callback URI and follow the Notification schema. A Kafka URI is of the format: kafka:<topic name>[:<message key>]. The Notification will be delivered to specified topic and using the optional message key for the Notification. The message key should be used if distribution over several partitions are needed.
     * (Required)
     *
     */
    @JsonProperty("callback")
    @JsonPropertyDescription("The callback URI for receiving notifications. HTTP Notifications will be POSTed to the callback URI and follow the Notification schema. A Kafka URI is of the format: kafka:<topic name>[:<message key>]. The Notification will be delivered to specified topic and using the optional message key for the Notification. The message key should be used if distribution over several partitions are needed.")
    private URI callback;

    /**
     * The configuration name, use '*' to subscribe on all configurations
     * (Required)
     *
     */
    @JsonProperty("configName")
    public String getConfigName() {
        return configName;
    }

    /**
     * The configuration name, use '*' to subscribe on all configurations
     * (Required)
     *
     */
    @JsonProperty("configName")
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public SubscriptionUpdate withConfigName(String configName) {
        this.configName = configName;
        return this;
    }

    /**
     * The events to subscribe to
     * (Required)
     *
     */
    @JsonProperty("event")
    public Set<Event> getEvent() {
        return event;
    }

    /**
     * The events to subscribe to
     * (Required)
     *
     */
    @JsonProperty("event")
    public void setEvent(Set<Event> event) {
        this.event = event;
    }

    public SubscriptionUpdate withEvent(Set<Event> event) {
        this.event = event;
        return this;
    }

    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     *
     */
    @JsonProperty("updateNotificationFormat")
    public SubscriptionUpdate.UpdateNotificationFormat getUpdateNotificationFormat() {
        return updateNotificationFormat;
    }

    /**
     * The configuration update notification format. This is only relevant if a configUpdated event is specified. 'none' means no configuration data is sent in the notification, 'full' means the complete configuration data is sent, 'patch' means a JSON Patch notification is sent with the changes.
     *
     */
    @JsonProperty("updateNotificationFormat")
    public void setUpdateNotificationFormat(SubscriptionUpdate.UpdateNotificationFormat updateNotificationFormat) {
        this.updateNotificationFormat = updateNotificationFormat;
    }

    public SubscriptionUpdate withUpdateNotificationFormat(SubscriptionUpdate.UpdateNotificationFormat updateNotificationFormat) {
        this.updateNotificationFormat = updateNotificationFormat;
        return this;
    }

    /**
     * The number of seconds the subscription should be valid.
     *
     */
    @JsonProperty("leaseSeconds")
    public Integer getLeaseSeconds() {
        return leaseSeconds;
    }

    /**
     * The number of seconds the subscription should be valid.
     *
     */
    @JsonProperty("leaseSeconds")
    public void setLeaseSeconds(Integer leaseSeconds) {
        this.leaseSeconds = leaseSeconds;
    }

    public SubscriptionUpdate withLeaseSeconds(Integer leaseSeconds) {
        this.leaseSeconds = leaseSeconds;
        return this;
    }

    /**
     * The callback URI for receiving notifications. HTTP Notifications will be POSTed to the callback URI and follow the Notification schema. A Kafka URI is of the format: kafka:<topic name>[:<message key>]. The Notification will be delivered to specified topic and using the optional message key for the Notification. The message key should be used if distribution over several partitions are needed.
     * (Required)
     *
     */
    @JsonProperty("callback")
    public URI getCallback() {
        return callback;
    }

    /**
     * The callback URI for receiving notifications. HTTP Notifications will be POSTed to the callback URI and follow the Notification schema. A Kafka URI is of the format: kafka:<topic name>[:<message key>]. The Notification will be delivered to specified topic and using the optional message key for the Notification. The message key should be used if distribution over several partitions are needed.
     * (Required)
     *
     */
    @JsonProperty("callback")
    public void setCallback(URI callback) {
        this.callback = callback;
    }

    public SubscriptionUpdate withCallback(URI callback) {
        this.callback = callback;
        return this;
    }

    public enum UpdateNotificationFormat {

        NONE("none"),
        FULL("full"),
        PATCH("patch");
        private final String value;
        private final static Map<String, SubscriptionUpdate.UpdateNotificationFormat> CONSTANTS = new HashMap<String, SubscriptionUpdate.UpdateNotificationFormat>();

        static {
            for (SubscriptionUpdate.UpdateNotificationFormat c: values()) {
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
        public static SubscriptionUpdate.UpdateNotificationFormat fromValue(String value) {
            SubscriptionUpdate.UpdateNotificationFormat constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
