
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "last-update", "sepp-name", "sepp-name-ref", "operational-state", "received-plmn-id", "security-capability", "supports-target-apiroot" })
public class SecurityNegotiationDatum
{

    /**
     * Date and time of the last N32-C event
     * 
     */
    @JsonProperty("last-update")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonPropertyDescription("Date and time of the last N32-C event")
    private Date lastUpdate;
    @JsonProperty("sepp-name")
    private String seppName;

    @JsonIgnore
    @JsonProperty("sepp-name-ref")
    private SeppNameRef seppNameRef;
    /**
     * Operational state of the N32-c handshake procedure in the responding SEPP.
     * 
     */
    @JsonProperty("operational-state")
    @JsonPropertyDescription("Operational state of the N32-c handshake procedure in the responding SEPP.")
    private OperationalState operationalState;
    /**
     * A list of received PLMN Ids of a single PLMN associated with the responding
     * SEPP.
     * 
     */
    @JsonProperty("received-plmn-id")
    @JsonPropertyDescription("A list of received PLMN Ids of a single PLMN associated with the responding SEPP.")
    private List<ReceivedPlmnId> receivedPlmnId = new ArrayList<ReceivedPlmnId>();
    /**
     * Agreed security capability, i.e. PRINS and/or TLS.
     * 
     */
    @JsonProperty("security-capability")
    @JsonPropertyDescription("Agreed security capability, i.e. PRINS and/or TLS.")
    private SecurityNegotiationDatum.SecurityCapability securityCapability;
    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * agreed for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    @JsonPropertyDescription("When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is agreed for N32f message forwarding.")
    private Boolean supportsTargetApiroot;

    /**
     * Date and time of the last N32-C event
     * 
     */
    @JsonProperty("last-update")
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * Date and time of the last N32-C event
     * 
     */
    @JsonProperty("last-update")
    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public SecurityNegotiationDatum withLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
        return this;
    }

    @JsonProperty("sepp-name")
    public String getSeppName()
    {
        return seppName;
    }

    @JsonProperty("sepp-name")
    public void setSeppName(String seppName)
    {
        this.seppName = seppName;
    }

    public SecurityNegotiationDatum withSeppName(String seppName)
    {
        this.seppName = seppName;
        return this;
    }

    @JsonIgnore
    @JsonProperty("sepp-name-ref")
    public SeppNameRef getSeppNameRef()
    {
        return seppNameRef;
    }

    @JsonIgnore
    @JsonProperty("sepp-name-ref")
    public void setSeppNameRef(SeppNameRef seppNameRef)
    {
        this.seppNameRef = seppNameRef;
    }

    @JsonIgnore
    public SecurityNegotiationDatum withSeppNameRef(SeppNameRef seppNameRef)
    {
        this.seppNameRef = seppNameRef;
        return this;
    }

    /**
     * Operational state of the N32-c handshake procedure in the responding SEPP.
     * 
     */
    @JsonProperty("operational-state")
    public OperationalState getOperationalState()
    {
        return operationalState;
    }

    /**
     * Operational state of the N32-c handshake procedure in the responding SEPP.
     * 
     */
    @JsonProperty("operational-state")
    public void setOperationalState(OperationalState operationalState)
    {
        this.operationalState = operationalState;
    }

    public SecurityNegotiationDatum withOperationalState(OperationalState operationalState)
    {
        this.operationalState = operationalState;
        return this;
    }

    /**
     * A list of received PLMN Ids of a single PLMN associated with the responding
     * SEPP.
     * 
     */
    @JsonProperty("received-plmn-id")
    public List<ReceivedPlmnId> getReceivedPlmnId()
    {
        return receivedPlmnId;
    }

    /**
     * A list of received PLMN Ids of a single PLMN associated with the responding
     * SEPP.
     * 
     */
    @JsonProperty("received-plmn-id")
    public void setReceivedPlmnId(List<ReceivedPlmnId> receivedPlmnId)
    {
        this.receivedPlmnId = receivedPlmnId;
    }

    public SecurityNegotiationDatum withReceivedPlmnId(List<ReceivedPlmnId> receivedPlmnId)
    {
        this.receivedPlmnId = receivedPlmnId;
        return this;
    }

    /**
     * Agreed security capability, i.e. PRINS and/or TLS.
     * 
     */
    @JsonProperty("security-capability")
    public SecurityNegotiationDatum.SecurityCapability getSecurityCapability()
    {
        return securityCapability;
    }

    /**
     * Agreed security capability, i.e. PRINS and/or TLS.
     * 
     */
    @JsonProperty("security-capability")
    public void setSecurityCapability(SecurityNegotiationDatum.SecurityCapability securityCapability)
    {
        this.securityCapability = securityCapability;
    }

    public SecurityNegotiationDatum withSecurityCapability(SecurityNegotiationDatum.SecurityCapability securityCapability)
    {
        this.securityCapability = securityCapability;
        return this;
    }

    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * agreed for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public Boolean getSupportsTargetApiroot()
    {
        return supportsTargetApiroot;
    }

    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * agreed for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public void setSupportsTargetApiroot(Boolean supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
    }

    public SecurityNegotiationDatum withSupportsTargetApiroot(Boolean supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SecurityNegotiationDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lastUpdate");
        sb.append('=');
        sb.append(((this.lastUpdate == null) ? "<null>" : this.lastUpdate));
        sb.append(',');
        sb.append("seppName");
        sb.append('=');
        sb.append(((this.seppName == null) ? "<null>" : this.seppName));
        sb.append(',');
        sb.append("seppNameRef");
        sb.append('=');
        sb.append(((this.seppNameRef == null) ? "<null>" : this.seppNameRef));
        sb.append(',');
        sb.append("operationalState");
        sb.append('=');
        sb.append(((this.operationalState == null) ? "<null>" : this.operationalState));
        sb.append(',');
        sb.append("receivedPlmnId");
        sb.append('=');
        sb.append(((this.receivedPlmnId == null) ? "<null>" : this.receivedPlmnId));
        sb.append(',');
        sb.append("securityCapability");
        sb.append('=');
        sb.append(((this.securityCapability == null) ? "<null>" : this.securityCapability));
        sb.append(',');
        sb.append("supportsTargetApiroot");
        sb.append('=');
        sb.append(((this.supportsTargetApiroot == null) ? "<null>" : this.supportsTargetApiroot));
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
        result = ((result * 31) + ((this.seppNameRef == null) ? 0 : this.seppNameRef.hashCode()));
        result = ((result * 31) + ((this.receivedPlmnId == null) ? 0 : this.receivedPlmnId.hashCode()));
        result = ((result * 31) + ((this.lastUpdate == null) ? 0 : this.lastUpdate.hashCode()));
        result = ((result * 31) + ((this.seppName == null) ? 0 : this.seppName.hashCode()));
        result = ((result * 31) + ((this.supportsTargetApiroot == null) ? 0 : this.supportsTargetApiroot.hashCode()));
        result = ((result * 31) + ((this.securityCapability == null) ? 0 : this.securityCapability.hashCode()));
        result = ((result * 31) + ((this.operationalState == null) ? 0 : this.operationalState.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SecurityNegotiationDatum) == false)
        {
            return false;
        }
        SecurityNegotiationDatum rhs = ((SecurityNegotiationDatum) other);
        return ((((((((this.seppNameRef == rhs.seppNameRef) || ((this.seppNameRef != null) && this.seppNameRef.equals(rhs.seppNameRef)))
                     && ((this.receivedPlmnId == rhs.receivedPlmnId) || ((this.receivedPlmnId != null) && this.receivedPlmnId.equals(rhs.receivedPlmnId))))
                    && ((this.lastUpdate == rhs.lastUpdate) || ((this.lastUpdate != null) && this.lastUpdate.equals(rhs.lastUpdate))))
                   && ((this.seppName == rhs.seppName) || ((this.seppName != null) && this.seppName.equals(rhs.seppName))))
                  && ((this.supportsTargetApiroot == rhs.supportsTargetApiroot)
                      || ((this.supportsTargetApiroot != null) && this.supportsTargetApiroot.equals(rhs.supportsTargetApiroot))))
                 && ((this.securityCapability == rhs.securityCapability)
                     || ((this.securityCapability != null) && this.securityCapability.equals(rhs.securityCapability))))
                && ((this.operationalState == rhs.operationalState)
                    || ((this.operationalState != null) && this.operationalState.equals(rhs.operationalState))));
    }

    public enum SecurityCapability
    {

        TLS("TLS"),
        PRINS("PRINS");

        private final String value;
        private final static Map<String, SecurityNegotiationDatum.SecurityCapability> CONSTANTS = new HashMap<String, SecurityNegotiationDatum.SecurityCapability>();

        static
        {
            for (SecurityNegotiationDatum.SecurityCapability c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private SecurityCapability(String value)
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
        public static SecurityNegotiationDatum.SecurityCapability fromValue(String value)
        {
            SecurityNegotiationDatum.SecurityCapability constant = CONSTANTS.get(value);
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
