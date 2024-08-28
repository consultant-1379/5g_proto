/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 12, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines a traffic set of requests.
 */
@JsonDeserialize(builder = TrafficSetConfiguration.Builder.class)
public class TrafficSetConfiguration
{
    private final UUID id;
    private final IPrange ipRange;
    private final String name;
    private final Integer order;
    private final Long timeout;
    private final Long tps;
    private final String trafficSetRef;
    private final LoadType type;
    private Long numRequests;
    private final String pcfId;
    private final String recoveryTimeStr;
    private final String authAccessToken;
    private final boolean sbiNfPeerInfoEnabled;
    private final String targetHost;

    public enum LoadType
    {
        DEREGISTER,
        DISCOVERY,
        REGISTER
    }

    private TrafficSetConfiguration(Builder builder)
    {
        /*
         * Clients should set this to null, so that BsfLoad can assign a random unique
         * id. Setting the id is still allowed for future use.
         */
        this.id = (builder.id == null) ? UUID.randomUUID() : builder.id;
        this.ipRange = builder.ipRange;
        this.name = builder.name;
        this.numRequests = builder.numRequests;
        this.order = builder.order;
        this.timeout = builder.timeout;
        this.tps = builder.tps;
        this.trafficSetRef = builder.trafficSetRef;
        this.type = builder.type;
        this.pcfId = builder.pcfId;
        this.recoveryTimeStr = builder.recoveryTimeStr;
        this.authAccessToken = builder.authAccessToken;
        this.sbiNfPeerInfoEnabled = builder.sbiNfPeerInfoEnabled;
        this.targetHost = builder.targetHost;
    }

    public List<InvalidParameter> validate()
    {
        final var cv = new ConfigurationValidator();

        // It is only allowed to have null numRequests in a deregister traffic set with
        // non null trafficSetRef.
        final var validNumRequests = (this.numRequests != null && this.numRequests > 0)
                                     || (this.numRequests == null && this.type != null && this.type.equals(LoadType.DEREGISTER) && this.trafficSetRef != null);

        // It is only allowed to have null ipRange in a deregister traffic set with non
        // null trafficSetRef.
        final var validIpRange = this.ipRange != null || (this.type != null && this.type.equals(LoadType.DEREGISTER) && this.trafficSetRef != null);

        // It is allowed to have null timeout, but when it is set it should be a
        // positive number.
        final var validTimeout = timeout != null && this.timeout >= 0 || timeout == null;

        var setName = name == null ? "'unknown'" : "'" + name + "'";
        var message = "Parameter 'name' is mandatory for a set";
        cv.checkNonNull(name, "name", message);

        message = "Parameter 'type' is required in set " + setName;
        cv.checkNonNull(type, "type", message);

        message = "A positive number value is required for 'tps' in set " + setName;
        cv.checkNonNullPositive(tps, "tps", message);

        message = "A positive number value is required for 'order' in set " + setName;
        cv.checkNonNullPositive(order, "order", message);

        message = "A positive number value is required for 'num-requests' greater than 0 in set " + setName
                  + ". The value can be null when the set is of 'type' DEREGISTER and has a 'traffic-set-ref' value";
        cv.check(validNumRequests, "num-requests", message);

        message = "A positive number or a null value is required for 'timeout' in set " + setName;
        cv.check(validTimeout, "timeout", message);

        message = "Parameter 'ip-range' is required in set " + setName
                  + ". The value can be null when the set is of 'type' DEREGISTER and has a 'traffic-set-ref' value";
        cv.check(validIpRange, "ip-range", message);

        if (ipRange != null)
        {
            cv.addInvalidParameters(ipRange.validate(setName));
        }
        if (this.recoveryTimeStr != null)
        {
            message = "Parameter 'recovery-time' must be a valid recovery time";
            cv.checkRecoveryTime(this.recoveryTimeStr, "recovery-time", message);
        }

        message = "Parameter 'targetHost' is required in set " + setName;
        cv.checkNonNull(targetHost, "targetHost", message);

        return cv.getInvalidParam();
    }

    /**
     * @return the id
     */
    public UUID getId()
    {
        return id;
    }

    /**
     * Get the IP range of the requests.
     * 
     * @return the ipRange
     */
    @JsonGetter("ip-range")
    public IPrange getIpRange()
    {
        return ipRange;
    }

    /**
     * Get the name of the traffic set.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the number of requests for this traffic set.
     * 
     * @return the numRequests
     */
    @JsonGetter("num-requests")
    public Long getNumRequests()
    {
        return numRequests;
    }

    /**
     * Get the order that is used to prioritize the traffic set execution in a
     * traffic mix
     * 
     * @return the order
     */
    public Integer getOrder()
    {
        return order;
    }

    /**
     * Get the timeout period before a request fails in milliseconds.
     * 
     * @return the timeout
     */
    public Long getTimeout()
    {
        return timeout;
    }

    /**
     * Get the number of requests per second (TPS).
     * 
     * @return the TPS
     */
    public Long getTps()
    {
        return tps;
    }

    /**
     * Get the referenced set.
     * 
     * @return the trafficSetRef
     */
    @JsonGetter("traffic-set-ref")
    public String getTrafficSetRef()
    {
        return trafficSetRef;
    }

    /**
     * Get the type of requests.
     * 
     * @return the type
     */
    public LoadType getType()
    {
        return type;
    }

    /**
     * @param numRequests the numRequests to set
     */
    public void setNumRequests(long numRequests)
    {
        this.numRequests = numRequests;
    }

    /**
     * Get the pcfId of the bindings to be registered.
     * 
     * @return the pcfId
     */
    @JsonGetter("pcf-id")
    public String getPcfId()
    {
        return this.pcfId;
    }

    /**
     * Get the recoveryTime String of the bindings to be registered.
     * 
     * @return the recoveryTime
     */
    @JsonGetter("recovery-time")
    public String getRecoveryTimeStr()
    {
        return this.recoveryTimeStr;
    }

    /**
     * Get the recoveryTime object.
     * 
     * @return the recoveryTime
     */
    @JsonIgnore
    public Optional<RecoveryTime> getRecoveryTime()
    {
        final var recoveryTime = (this.recoveryTimeStr != null) ? new RecoveryTime(this.recoveryTimeStr) : null;

        return Optional.ofNullable(recoveryTime);
    }

    /**
     * Get the JWT that is used in the authorization header for OAuth 2.0
     * 
     * @return the authAccessToken
     */
    @JsonGetter("auth-access-token")
    public String getAuthAccessToken()
    {
        return this.authAccessToken;
    }

    /**
     * Get the flag for the sbi-nf-peer-info feature.
     * 
     * @return the sbiNfPeerInfoEnabled
     */
    @JsonGetter("sbi-nf-peer-info-enabled")
    public boolean getSbiNfPeerInfoEnabled()
    {
        return this.sbiNfPeerInfoEnabled;
    }

    /**
     * Get target-host
     * 
     * @return targetHost
     */
    @JsonGetter("target-host")
    public String getTargetHost()
    {
        return this.targetHost;
    }

    /**
     * @return True if this traffic set deregisters a set of known bindingsIds
     */
    public boolean deregisterKnownIds()
    {
        return this.getType().equals(LoadType.DEREGISTER) //
               && this.getTrafficSetRef() != null //
               && !this.getTrafficSetRef().isEmpty();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ipRange == null) ? 0 : ipRange.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((numRequests == null) ? 0 : numRequests.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        result = prime * result + ((tps == null) ? 0 : tps.hashCode());
        result = prime * result + ((trafficSetRef == null) ? 0 : trafficSetRef.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((pcfId == null) ? 0 : pcfId.hashCode());
        result = prime * result + ((authAccessToken == null) ? 0 : authAccessToken.hashCode());
        result = prime * result + ((targetHost == null) ? 0 : targetHost.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrafficSetConfiguration other = (TrafficSetConfiguration) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (ipRange == null)
        {
            if (other.ipRange != null)
                return false;
        }
        else if (!ipRange.equals(other.ipRange))
            return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (numRequests == null)
        {
            if (other.numRequests != null)
                return false;
        }
        else if (!numRequests.equals(other.numRequests))
            return false;
        if (order == null)
        {
            if (other.order != null)
                return false;
        }
        else if (!order.equals(other.order))
            return false;
        if (timeout == null)
        {
            if (other.timeout != null)
                return false;
        }
        else if (!timeout.equals(other.timeout))
            return false;
        if (tps == null)
        {
            if (other.tps != null)
                return false;
        }
        else if (!tps.equals(other.tps))
            return false;
        if (trafficSetRef == null)
        {
            if (other.trafficSetRef != null)
                return false;
        }
        else if (!trafficSetRef.equals(other.trafficSetRef))
            return false;
        if (type != other.type)
            return false;
        if (pcfId == null)
        {
            if (other.pcfId != null)
                return false;
        }
        else if (!pcfId.equals(other.pcfId))
            return false;
        if (authAccessToken == null)
        {
            if (other.authAccessToken != null)
                return false;
        }
        else if (!authAccessToken.equals(other.authAccessToken))
            return false;
        if (targetHost == null)
        {
            if (other.targetHost != null)
                return false;
        }
        else if (!targetHost.equals(other.targetHost))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "TrafficSetConfiguration [id=" + id + ", ipRange=" + ipRange + ", name=" + name + ", order=" + order + ", timeout=" + timeout + ", tps=" + tps
               + ", trafficSetRef=" + trafficSetRef + ", type=" + type + ", numRequests=" + numRequests + ", pcfId=" + pcfId + ", recoveryTimeStr="
               + recoveryTimeStr + ", sbiNfPeerInfoEnabled=" + sbiNfPeerInfoEnabled + ", targetHost=" + targetHost + "]"; // For security reasons,
                                                                                                                          // authAccessToken should not be
                                                                                                                          // included.
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder
    {
        private UUID id;
        @JsonProperty("ip-range")
        private IPrange ipRange;
        private String name;
        @JsonProperty("num-requests")
        private Long numRequests;
        private Integer order;
        private Long timeout;
        private Long tps;
        @JsonProperty("traffic-set-ref")
        private String trafficSetRef;
        private LoadType type;
        @JsonProperty("pcf-id")
        private String pcfId;
        @JsonProperty("recovery-time")
        private String recoveryTimeStr;
        @JsonProperty("auth-access-token")
        private String authAccessToken;
        @JsonProperty("sbi-nf-peer-info-enabled")
        private boolean sbiNfPeerInfoEnabled;
        @JsonProperty("target-host")
        private String targetHost;

        /**
         * This should be omitted or set to null by the client. BsfLoad tool is
         * responsible to provide a random UUID id value for each traffic set.
         * 
         * @param id Unique identification of the traffic set.
         * @return Builder The builder.
         */
        public Builder id(UUID id)
        {
            this.id = id;
            return this;
        }

        /**
         * Set the IP range of the requests. If the required number of requests is
         * greater than the IP range, then the generated requests start over from the
         * starting IP of the IP range.
         * 
         * @param ipRange The IP range.
         * @return Builder The builder.
         */
        public Builder ipRange(IPrange ipRange)
        {
            this.ipRange = ipRange;
            return this;
        }

        /**
         * Set the name that uniquely identifies this specific traffic set. This is used
         * to cross-reference between the traffic sets, allowing to refer to register
         * traffic sets from deregister traffic sets.
         * 
         * @param name The name.
         * @return Builder The builder.
         */
        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        /**
         * Set the number of requests for this traffic set. The traffic set is executed
         * until the number of requests is reached.
         * 
         * @param numRequests The number of requests.
         * @return Builder The builder.
         */
        public Builder numRequests(Long numRequests)
        {
            this.numRequests = numRequests;
            return this;
        }

        /**
         * Set the order that is used to prioritize the traffic set execution in a
         * traffic mix. The order must be unique inside a group of traffic sets.
         * 
         * @param order The order.
         * @return Builder The builder.
         */
        public Builder order(int order)
        {
            this.order = order;
            return this;
        }

        /**
         * Set the timeout period in milliseconds before a request fails. If this is set
         * to 0, then no timeout is used for the requests. This takes precedence over
         * the global timeout configuration option. When set to null, the global timeout
         * value is applied.
         * 
         * @param timeout The timeout.
         * @return Builder The builder.
         */
        public Builder timeout(Long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the number of requests per second (TPS)
         * 
         * @param tps The TPS.
         * @return Builder The builder.
         */
        public Builder tps(long tps)
        {
            this.tps = tps;
            return this;
        }

        /**
         * Set the reference to the name of a register traffic set. The referenced set
         * must belong in the same group of traffic sets, either setup or traffic mix.
         * 
         * @param trafficSetRef The trafficSetRef.
         * @return Builder The builder.
         */
        public Builder trafficSetRef(String trafficSetRef)
        {
            this.trafficSetRef = trafficSetRef;
            return this;
        }

        /**
         * Set the type of requests, which can be register, deregister or discovery.
         * 
         * @param type The type of requests.
         * @return Builder The builder.
         */
        public Builder type(LoadType type)
        {
            this.type = type;
            return this;
        }

        /**
         * Set the pcfId of the bindings to be registered.
         * 
         * @param pcfId The pcfId.
         * @return Builder The builder.
         */
        public Builder pcfId(String pcfId)
        {
            this.pcfId = pcfId;
            return this;
        }

        /**
         * Set the recoveryTime String of the bindings to be registered.
         * 
         * @param recoveryTime The recoveryTime
         * @return Builder The builder
         */
        public Builder recoveryTimeStr(String recoveryTimeStr)
        {
            this.recoveryTimeStr = recoveryTimeStr;
            return this;
        }

        /**
         * Set the host address of the target BSF.
         * 
         * @param targetHost The targetHost
         * @return Builder The builder
         */
        public Builder targetHost(String targetHost)
        {
            this.targetHost = targetHost;
            return this;
        }

        /**
         * Set the sbi-nf-peer-info feature flag.
         * 
         * @param sbiNfPeerInfoEnabled The sbiNfPeerInfoEnabled
         * @return Builder The builder
         */
        public Builder sbiNfPeerInfoEnabled(boolean sbiNfPeerInfoEnabled)
        {
            this.sbiNfPeerInfoEnabled = sbiNfPeerInfoEnabled;
            return this;
        }

        /**
         * Set the targetHost.
         * 
         * @param authAccessToken The authAccessToken
         * @return Builder The builder
         */
        public Builder authAccessToken(String authAccessToken)
        {
            this.authAccessToken = authAccessToken;
            return this;
        }

        /**
         * Create the TrafficSetConfiguration object.
         * 
         * @return TrafficSetConfiguration The configuration options for the traffic
         *         set.
         */
        public TrafficSetConfiguration build()
        {
            return new TrafficSetConfiguration(this);
        }
    }
}
