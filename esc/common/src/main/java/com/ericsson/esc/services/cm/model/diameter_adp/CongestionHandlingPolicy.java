
package com.ericsson.esc.services.cm.model.diameter_adp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "enabled", "congestion-threshold-raise-level", "congestion-threshold-cease-level", "user-label" })
public class CongestionHandlingPolicy
{

    /**
     * Key of the congestion handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Key of the congestion handling policy.")
    private String id;
    /**
     * Used to enable or disable a congestion-handling-policy. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the congestion-handling-policy configuration for scoped Peer Diameter Nodes.
     * When disabled, the Diameter Service will apply the default behavior for
     * diameter peer connection congestion handling.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable a congestion-handling-policy. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter Service will apply the congestion-handling-policy configuration for scoped Peer Diameter Nodes. When disabled, the Diameter Service will apply the default behavior for diameter peer connection congestion handling.")
    private Boolean enabled = true;
    /**
     * Used to specify the arm threshold the congestion handling policy is to be
     * applied over the congested diameter peer connections of scoped Peer Diameter
     * Nodes. The congested peer connection will be temporarily removed from the
     * possible routes determined based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-raise-level")
    @JsonPropertyDescription("Used to specify the arm threshold the congestion handling policy is to be applied over the congested diameter peer connections of scoped Peer Diameter Nodes. The congested peer connection will be temporarily removed from the possible routes determined based on loaded routing table. The value, in percentage representation, represents the utilization level of the internal queue of the Local Endpoint used to send egress messages towards the scoped Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the Diameter Service will apply the new threshold value.")
    private Long congestionThresholdRaiseLevel = 85L;
    /**
     * Used to specify the disarm threshold the congestion handling policy is to be
     * ceased over the diameter peer connections, of scoped Peer Diameter Nodes,
     * getting out of congestion. The peer connection will be considered again as
     * possible route to be used based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-cease-level")
    @JsonPropertyDescription("Used to specify the disarm threshold the congestion handling policy is to be ceased over the diameter peer connections, of scoped Peer Diameter Nodes, getting out of congestion. The peer connection will be considered again as possible route to be used based on loaded routing table. The value, in percentage representation, represents the utilization level of the internal queue of the Local Endpoint used to send egress messages towards the scoped Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the Diameter Service will apply the new threshold value.")
    private Long congestionThresholdCeaseLevel = 75L;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Key of the congestion handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Key of the congestion handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public CongestionHandlingPolicy withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to enable or disable a congestion-handling-policy. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the congestion-handling-policy configuration for scoped Peer Diameter Nodes.
     * When disabled, the Diameter Service will apply the default behavior for
     * diameter peer connection congestion handling.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable a congestion-handling-policy. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the congestion-handling-policy configuration for scoped Peer Diameter Nodes.
     * When disabled, the Diameter Service will apply the default behavior for
     * diameter peer connection congestion handling.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public CongestionHandlingPolicy withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Used to specify the arm threshold the congestion handling policy is to be
     * applied over the congested diameter peer connections of scoped Peer Diameter
     * Nodes. The congested peer connection will be temporarily removed from the
     * possible routes determined based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-raise-level")
    public Long getCongestionThresholdRaiseLevel()
    {
        return congestionThresholdRaiseLevel;
    }

    /**
     * Used to specify the arm threshold the congestion handling policy is to be
     * applied over the congested diameter peer connections of scoped Peer Diameter
     * Nodes. The congested peer connection will be temporarily removed from the
     * possible routes determined based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-raise-level")
    public void setCongestionThresholdRaiseLevel(Long congestionThresholdRaiseLevel)
    {
        this.congestionThresholdRaiseLevel = congestionThresholdRaiseLevel;
    }

    public CongestionHandlingPolicy withCongestionThresholdRaiseLevel(Long congestionThresholdRaiseLevel)
    {
        this.congestionThresholdRaiseLevel = congestionThresholdRaiseLevel;
        return this;
    }

    /**
     * Used to specify the disarm threshold the congestion handling policy is to be
     * ceased over the diameter peer connections, of scoped Peer Diameter Nodes,
     * getting out of congestion. The peer connection will be considered again as
     * possible route to be used based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-cease-level")
    public Long getCongestionThresholdCeaseLevel()
    {
        return congestionThresholdCeaseLevel;
    }

    /**
     * Used to specify the disarm threshold the congestion handling policy is to be
     * ceased over the diameter peer connections, of scoped Peer Diameter Nodes,
     * getting out of congestion. The peer connection will be considered again as
     * possible route to be used based on loaded routing table. The value, in
     * percentage representation, represents the utilization level of the internal
     * queue of the Local Endpoint used to send egress messages towards the scoped
     * Peer Diameter Node. Update Apply: Immediate Update Effect: When set, the
     * Diameter Service will apply the new threshold value.
     * 
     */
    @JsonProperty("congestion-threshold-cease-level")
    public void setCongestionThresholdCeaseLevel(Long congestionThresholdCeaseLevel)
    {
        this.congestionThresholdCeaseLevel = congestionThresholdCeaseLevel;
    }

    public CongestionHandlingPolicy withCongestionThresholdCeaseLevel(Long congestionThresholdCeaseLevel)
    {
        this.congestionThresholdCeaseLevel = congestionThresholdCeaseLevel;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public CongestionHandlingPolicy withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(CongestionHandlingPolicy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("congestionThresholdRaiseLevel");
        sb.append('=');
        sb.append(((this.congestionThresholdRaiseLevel == null) ? "<null>" : this.congestionThresholdRaiseLevel));
        sb.append(',');
        sb.append("congestionThresholdCeaseLevel");
        sb.append('=');
        sb.append(((this.congestionThresholdCeaseLevel == null) ? "<null>" : this.congestionThresholdCeaseLevel));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.congestionThresholdCeaseLevel == null) ? 0 : this.congestionThresholdCeaseLevel.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.congestionThresholdRaiseLevel == null) ? 0 : this.congestionThresholdRaiseLevel.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof CongestionHandlingPolicy) == false)
        {
            return false;
        }
        CongestionHandlingPolicy rhs = ((CongestionHandlingPolicy) other);
        return ((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                   && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                  && ((this.congestionThresholdCeaseLevel == rhs.congestionThresholdCeaseLevel)
                      || ((this.congestionThresholdCeaseLevel != null) && this.congestionThresholdCeaseLevel.equals(rhs.congestionThresholdCeaseLevel))))
                 && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                && ((this.congestionThresholdRaiseLevel == rhs.congestionThresholdRaiseLevel)
                    || ((this.congestionThresholdRaiseLevel != null) && this.congestionThresholdRaiseLevel.equals(rhs.congestionThresholdRaiseLevel))));
    }

}
