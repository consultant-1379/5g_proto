
package com.ericsson.esc.bsf.services.cm.adp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "leaseSeconds"
})
public class SubscriptionsList {

    /**
     * The id of the subscription
     * (Required)
     *
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The id of the subscription")
    private String id;
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

    public SubscriptionsList withId(String id) {
        this.id = id;
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

    public SubscriptionsList withLeaseSeconds(Integer leaseSeconds) {
        this.leaseSeconds = leaseSeconds;
        return this;
    }

}
