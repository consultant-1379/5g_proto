package com.ericsson.sc.sepp.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.ericsson.sc.sepp.model.OperationalState;
import com.ericsson.sc.sepp.model.ReceivedPlmnId;
import com.ericsson.sc.sepp.model.SecurityNegotiationDatum;
import com.ericsson.sc.sepp.model.SecurityNegotiationDatum.SecurityCapability;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonInclude(Include.NON_EMPTY)
@JsonDeserialize(builder = EtcdSecurityNegotiationDatum.Builder.class)
public class EtcdSecurityNegotiationDatum
{

    @JsonProperty("sepp-name")
    private final String seppName;
    @JsonProperty("operational-state")
    private final OperationalState operationState;
    @JsonProperty("received-plmn-id")
    private final List<ReceivedPlmnId> receivedPlmnId;
    @JsonProperty("last-update")
    private final Date lastUpdate;
    @JsonProperty("security-capability")
    private final SecurityCapability securityCapability;
    @JsonProperty("supports-target-apiroot")
    private final Boolean supportsTargetApiroot;
    @JsonProperty("roaming-partner-ref")
    private final String roamingPartnerRef;
    @JsonProperty("num-of-failures")
    private final int numOfFailures;

    private EtcdSecurityNegotiationDatum(Builder builder)
    {
        this.seppName = builder.seppName;
        this.roamingPartnerRef = builder.roamingPartnerRef;
        this.operationState = builder.operationState;
        this.receivedPlmnId = builder.receivedPlmnId != null ? Collections.unmodifiableList(builder.receivedPlmnId) : List.of();
        this.lastUpdate = builder.lastUpdate;
        this.securityCapability = builder.securityCapability;
        this.supportsTargetApiroot = builder.supportsTargetApiroot;
        this.numOfFailures = builder.numOfFailures;

        Objects.requireNonNull(seppName);
    }

    @JsonProperty("sepp-name")
    public String getSeppName()
    {
        return seppName;
    }

    @JsonProperty("roaming-partner-ref")
    public String getRoamingPartnerRef()
    {
        return roamingPartnerRef;
    }

    @JsonProperty("operational-state")
    public OperationalState getOperationState()
    {
        return operationState;
    }

    @JsonProperty("received-plmn-id")
    public List<ReceivedPlmnId> getReceivedPlmnId()
    {
        return receivedPlmnId;
    }

    @JsonProperty("last-update")
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    @JsonProperty("security-capability")
    public SecurityCapability getSecurityCapability()
    {
        return securityCapability;
    }

    @JsonProperty("supports-target-apiroot")
    public Boolean getSupportsTargetApiroot()
    {
        return supportsTargetApiroot;
    }

    @JsonProperty("num-of-failures")
    public int getNumOfFailures()
    {
        return numOfFailures;
    }

    /**
     * Create a new Builder for building EtcdSecurityNegotiationDatum objects
     * 
     * @return The newly created builder
     */
    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder(EtcdSecurityNegotiationDatum datum)
    {
        return new Builder(datum);
    }

    public SecurityNegotiationDatum convertToYang()
    {
        return new SecurityNegotiationDatum().withLastUpdate(this.lastUpdate)
                                             .withOperationalState(this.operationState)
                                             .withReceivedPlmnId(this.receivedPlmnId)
                                             .withSecurityCapability(this.securityCapability)
                                             .withSeppName(this.seppName)
                                             .withSupportsTargetApiroot(this.supportsTargetApiroot);
    }

    @JsonPOJOBuilder
    public static class Builder
    {
        public Builder()
        {
        }

        public Builder(EtcdSecurityNegotiationDatum datum)
        {
            this.seppName = datum.seppName;
            this.roamingPartnerRef = datum.roamingPartnerRef;
            this.operationState = datum.operationState;
            this.receivedPlmnId = datum.receivedPlmnId;
            this.lastUpdate = datum.lastUpdate;
            this.securityCapability = datum.securityCapability;
            this.supportsTargetApiroot = datum.supportsTargetApiroot;
            this.numOfFailures = datum.numOfFailures;
        }

        public Builder withSeppName(String seppName)
        {
            this.seppName = seppName;
            return this;
        }

        public Builder withRoamingPartnerRef(String roamingPartnerRef)
        {
            this.roamingPartnerRef = roamingPartnerRef;
            return this;
        }

        public Builder withOperationState(OperationalState operationState)
        {
            this.operationState = operationState;
            return this;
        }

        public Builder withReceivedPlmnId(List<ReceivedPlmnId> receivedPlmnId)
        {
            this.receivedPlmnId = receivedPlmnId;
            return this;
        }

        public Builder withLastUpdate(Date lastUpdate)
        {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Builder withLastUpdate(String lastUpdate) throws ParseException
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
            this.lastUpdate = sdf.parse(lastUpdate);
            return this;
        }

        public Builder withSecurityCapability(SecurityCapability securityCapability)
        {
            this.securityCapability = securityCapability;
            return this;
        }

        public Builder withSupportsTargetApiroot(Boolean supportsTargetApiroot)
        {
            this.supportsTargetApiroot = supportsTargetApiroot;
            return this;
        }

        public Builder withNumOfFailures(int numOfFailures)
        {
            this.numOfFailures = numOfFailures;
            return this;
        }

        public EtcdSecurityNegotiationDatum build()
        {
            return new EtcdSecurityNegotiationDatum(this);
        }

        @JsonProperty("sepp-name")
        private String seppName;
        @JsonProperty("roaming-partner-ref")
        private String roamingPartnerRef;
        @JsonProperty("operational-state")
        private OperationalState operationState;
        @JsonProperty("received-plmn-id")
        private List<ReceivedPlmnId> receivedPlmnId;
        @JsonProperty("last-update")
        private Date lastUpdate;
        @JsonProperty("security-capability")
        private SecurityCapability securityCapability;
        @JsonProperty("supports-target-apiroot")
        private Boolean supportsTargetApiroot;
        @JsonProperty("num-of-failures")
        private int numOfFailures;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lastUpdate, numOfFailures, operationState, receivedPlmnId, roamingPartnerRef, securityCapability, seppName, supportsTargetApiroot);
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
        EtcdSecurityNegotiationDatum other = (EtcdSecurityNegotiationDatum) obj;
        return Objects.equals(lastUpdate, other.lastUpdate) && numOfFailures == other.numOfFailures && Objects.equals(operationState, other.operationState)
               && Objects.equals(receivedPlmnId, other.receivedPlmnId) && Objects.equals(roamingPartnerRef, other.roamingPartnerRef)
               && securityCapability == other.securityCapability && Objects.equals(seppName, other.seppName)
               && Objects.equals(supportsTargetApiroot, other.supportsTargetApiroot);
    }

    @Override
    public String toString()
    {
        var builder2 = new StringBuilder();
        builder2.append("EtcdSecurityNegotiationDatum [seppName=");
        builder2.append(seppName);
        builder2.append(", roamingPartnerRef=");
        builder2.append(roamingPartnerRef);
        builder2.append(", operationState=");
        builder2.append(operationState);
        builder2.append(", receivedPlmnId=");
        builder2.append(receivedPlmnId);
        builder2.append(", lastUpdate=");
        builder2.append(lastUpdate);
        builder2.append(", securityCapability=");
        builder2.append(securityCapability);
        builder2.append(", supportsTargetApiroot=");
        builder2.append(supportsTargetApiroot);
        builder2.append(", numOfFailures=");
        builder2.append(numOfFailures);
        builder2.append("]");
        return builder2.toString();
    }

}
