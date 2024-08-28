
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "tph-profile-ref", "admin-state", "scrambling-key" })
public class TopologyHidingWithAdminState
{

    /**
     * Reference to a defined topology hiding profile for FQDN Mapping or FQDN
     * Scrambling, applied to all roaming-partners of this network. In order to
     * configure FQDN Scrambling, an NRF FQDN mapping profile must be configured as
     * well (Required)
     * 
     */
    @JsonProperty("tph-profile-ref")
    @JsonPropertyDescription("Reference to a defined topology hiding profile for FQDN Mapping or FQDN Scrambling, applied to all roaming-partners of this network. In order to configure FQDN Scrambling, an NRF FQDN mapping profile must be configured as well")
    private String tphProfileRef;
    /**
     * The administrative state of the FQDN Mapping/FQDN Scrambling functionality
     * 
     */
    @JsonProperty("admin-state")
    @JsonPropertyDescription("The administrative state of the FQDN Mapping/FQDN Scrambling functionality")
    private TopologyHidingWithAdminState.AdminState adminState = TopologyHidingWithAdminState.AdminState.fromValue("active");
    /**
     * The list of the scrambling encryption identifiers (referencing the scrambling
     * keys and the relevant initial vectors) and the corresponding activation dates
     * 
     */
    @JsonProperty("scrambling-key")
    @JsonPropertyDescription("The list of the scrambling encryption identifiers (referencing the scrambling keys and the relevant initial vectors) and the corresponding activation dates")
    private List<ScramblingKey> scramblingKey = new ArrayList<ScramblingKey>();

    /**
     * Reference to a defined topology hiding profile for FQDN Mapping or FQDN
     * Scrambling, applied to all roaming-partners of this network. In order to
     * configure FQDN Scrambling, an NRF FQDN mapping profile must be configured as
     * well (Required)
     * 
     */
    @JsonProperty("tph-profile-ref")
    public String getTphProfileRef()
    {
        return tphProfileRef;
    }

    /**
     * Reference to a defined topology hiding profile for FQDN Mapping or FQDN
     * Scrambling, applied to all roaming-partners of this network. In order to
     * configure FQDN Scrambling, an NRF FQDN mapping profile must be configured as
     * well (Required)
     * 
     */
    @JsonProperty("tph-profile-ref")
    public void setTphProfileRef(String tphProfileRef)
    {
        this.tphProfileRef = tphProfileRef;
    }

    public TopologyHidingWithAdminState withTphProfileRef(String tphProfileRef)
    {
        this.tphProfileRef = tphProfileRef;
        return this;
    }

    /**
     * The administrative state of the FQDN Mapping/FQDN Scrambling functionality
     * 
     */
    @JsonProperty("admin-state")
    public TopologyHidingWithAdminState.AdminState getAdminState()
    {
        return adminState;
    }

    /**
     * The administrative state of the FQDN Mapping/FQDN Scrambling functionality
     * 
     */
    @JsonProperty("admin-state")
    public void setAdminState(TopologyHidingWithAdminState.AdminState adminState)
    {
        this.adminState = adminState;
    }

    public TopologyHidingWithAdminState withAdminState(TopologyHidingWithAdminState.AdminState adminState)
    {
        this.adminState = adminState;
        return this;
    }

    /**
     * The list of the scrambling encryption identifiers (referencing the scrambling
     * keys and the relevant initial vectors) and the corresponding activation dates
     * 
     */
    @JsonProperty("scrambling-key")
    public List<ScramblingKey> getScramblingKey()
    {
        return scramblingKey;
    }

    /**
     * The list of the scrambling encryption identifiers (referencing the scrambling
     * keys and the relevant initial vectors) and the corresponding activation dates
     * 
     */
    @JsonProperty("scrambling-key")
    public void setScramblingKey(List<ScramblingKey> scramblingKey)
    {
        this.scramblingKey = scramblingKey;
    }

    public TopologyHidingWithAdminState withScramblingKey(List<ScramblingKey> scramblingKey)
    {
        this.scramblingKey = scramblingKey;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TopologyHidingWithAdminState.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("tphProfileRef");
        sb.append('=');
        sb.append(((this.tphProfileRef == null) ? "<null>" : this.tphProfileRef));
        sb.append(',');
        sb.append("adminState");
        sb.append('=');
        sb.append(((this.adminState == null) ? "<null>" : this.adminState));
        sb.append(',');
        sb.append("scramblingKey");
        sb.append('=');
        sb.append(((this.scramblingKey == null) ? "<null>" : this.scramblingKey));
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
        result = ((result * 31) + ((this.adminState == null) ? 0 : this.adminState.hashCode()));
        result = ((result * 31) + ((this.scramblingKey == null) ? 0 : this.scramblingKey.hashCode()));
        result = ((result * 31) + ((this.tphProfileRef == null) ? 0 : this.tphProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TopologyHidingWithAdminState) == false)
        {
            return false;
        }
        TopologyHidingWithAdminState rhs = ((TopologyHidingWithAdminState) other);
        return ((((this.adminState == rhs.adminState) || ((this.adminState != null) && this.adminState.equals(rhs.adminState)))
                 && ((this.scramblingKey == rhs.scramblingKey) || ((this.scramblingKey != null) && this.scramblingKey.equals(rhs.scramblingKey))))
                && ((this.tphProfileRef == rhs.tphProfileRef) || ((this.tphProfileRef != null) && this.tphProfileRef.equals(rhs.tphProfileRef))));
    }

    public enum AdminState
    {

        ACTIVE("active"),
        GRACEFUL_ACTIVATION("graceful-activation"),
        GRACEFUL_DEACTIVATION("graceful-deactivation");

        private final String value;
        private final static Map<String, TopologyHidingWithAdminState.AdminState> CONSTANTS = new HashMap<String, TopologyHidingWithAdminState.AdminState>();

        static
        {
            for (TopologyHidingWithAdminState.AdminState c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private AdminState(String value)
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
        public static TopologyHidingWithAdminState.AdminState fromValue(String value)
        {
            TopologyHidingWithAdminState.AdminState constant = CONSTANTS.get(value);
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
