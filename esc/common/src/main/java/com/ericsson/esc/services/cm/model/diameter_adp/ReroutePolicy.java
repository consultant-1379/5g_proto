
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "enabled", "reroute-error-id", "user-label" })
public class ReroutePolicy
{

    /**
     * Key of the routing policy. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Key of the routing policy.")
    private String id;
    /**
     * Can be used to enable or disable the actual reroute policy. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the reroute-policy configuration. When disabled, the Diameter Service will
     * apply the default behavior for egress request message rerouting.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Can be used to enable or disable the actual reroute policy. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter Service will apply the reroute-policy configuration. When disabled, the Diameter Service will apply the default behavior for egress request message rerouting.")
    private Boolean enabled = true;
    /**
     * Used to specify the Diameter Result Codes which should trigger an egress
     * request message re-routing to alternate peer based on the configured routing
     * table. Error codes are received in the Result-Code data field of a
     * Result-Code AVP. The following error categories exists: 1xxx (Informational)
     * 3xxx (Protocol Errors) 4xxx (Transient Failures) 5xxx (Permanent Failure)
     * Update Apply: Immediate Update Effect: When set, the Diameter Service will
     * start to behave according to the configured reroute-policy. It will
     * automatically reroute messages upon reception of error answers holding the
     * configured error codes. (Required)
     * 
     */
    @JsonProperty("reroute-error-id")
    @JsonPropertyDescription("Used to specify the Diameter Result Codes which should trigger an egress request message re-routing to alternate peer based on the configured routing table. Error codes are received in the Result-Code data field of a Result-Code AVP. The following error categories exists: 1xxx (Informational) 3xxx (Protocol Errors) 4xxx (Transient Failures) 5xxx (Permanent Failure) Update Apply: Immediate Update Effect: When set, the Diameter Service will start to behave according to the configured reroute-policy. It will automatically reroute messages upon reception of error answers holding the configured error codes.")
    private List<Long> rerouteErrorId = new ArrayList<Long>();
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Key of the routing policy. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Key of the routing policy. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public ReroutePolicy withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Can be used to enable or disable the actual reroute policy. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the reroute-policy configuration. When disabled, the Diameter Service will
     * apply the default behavior for egress request message rerouting.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Can be used to enable or disable the actual reroute policy. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate Update Effect: When enabled, the Diameter Service will apply
     * the reroute-policy configuration. When disabled, the Diameter Service will
     * apply the default behavior for egress request message rerouting.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public ReroutePolicy withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Used to specify the Diameter Result Codes which should trigger an egress
     * request message re-routing to alternate peer based on the configured routing
     * table. Error codes are received in the Result-Code data field of a
     * Result-Code AVP. The following error categories exists: 1xxx (Informational)
     * 3xxx (Protocol Errors) 4xxx (Transient Failures) 5xxx (Permanent Failure)
     * Update Apply: Immediate Update Effect: When set, the Diameter Service will
     * start to behave according to the configured reroute-policy. It will
     * automatically reroute messages upon reception of error answers holding the
     * configured error codes. (Required)
     * 
     */
    @JsonProperty("reroute-error-id")
    public List<Long> getRerouteErrorId()
    {
        return rerouteErrorId;
    }

    /**
     * Used to specify the Diameter Result Codes which should trigger an egress
     * request message re-routing to alternate peer based on the configured routing
     * table. Error codes are received in the Result-Code data field of a
     * Result-Code AVP. The following error categories exists: 1xxx (Informational)
     * 3xxx (Protocol Errors) 4xxx (Transient Failures) 5xxx (Permanent Failure)
     * Update Apply: Immediate Update Effect: When set, the Diameter Service will
     * start to behave according to the configured reroute-policy. It will
     * automatically reroute messages upon reception of error answers holding the
     * configured error codes. (Required)
     * 
     */
    @JsonProperty("reroute-error-id")
    public void setRerouteErrorId(List<Long> rerouteErrorId)
    {
        this.rerouteErrorId = rerouteErrorId;
    }

    public ReroutePolicy withRerouteErrorId(List<Long> rerouteErrorId)
    {
        this.rerouteErrorId = rerouteErrorId;
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

    public ReroutePolicy withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ReroutePolicy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("rerouteErrorId");
        sb.append('=');
        sb.append(((this.rerouteErrorId == null) ? "<null>" : this.rerouteErrorId));
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
        result = ((result * 31) + ((this.rerouteErrorId == null) ? 0 : this.rerouteErrorId.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ReroutePolicy) == false)
        {
            return false;
        }
        ReroutePolicy rhs = ((ReroutePolicy) other);
        return (((((this.rerouteErrorId == rhs.rerouteErrorId) || ((this.rerouteErrorId != null) && this.rerouteErrorId.equals(rhs.rerouteErrorId)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
