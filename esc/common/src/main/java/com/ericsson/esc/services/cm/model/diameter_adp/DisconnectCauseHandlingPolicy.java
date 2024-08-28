
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "enabled", "disconnect-cause", "user-label" })
public class DisconnectCauseHandlingPolicy
{

    /**
     * Key of the disconnect cause handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Key of the disconnect cause handling policy.")
    private String id;
    /**
     * Can be used to enable or disable the actual DisconnectCause handling policy.
     * When disabled, the following alarm is raised: ADP Diameter, Managed Object
     * Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter
     * Service will apply the disconnect-cause-handling-policy. When disabled, the
     * Diameter Service will apply the default behavior for DPR message content
     * construction.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Can be used to enable or disable the actual DisconnectCause handling policy. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter Service will apply the disconnect-cause-handling-policy. When disabled, the Diameter Service will apply the default behavior for DPR message content construction.")
    private Boolean enabled = true;
    /**
     * Defines the content of the Disconnect-Cause AVP to be sent in DPRs. It is
     * used in the Disconnect-Peer-Request message to inform the peer about the
     * reason of the disconnection. Update Apply: Immediate Update Effect: When set,
     * the Diameter Service will apply the new configuration over scoped Local
     * Endpoints. That is, upon diameter peer connection disconnection the
     * configured error code will be applied.
     * 
     */
    @JsonProperty("disconnect-cause")
    @JsonPropertyDescription("Defines the content of the Disconnect-Cause AVP to be sent in DPRs. It is used in the Disconnect-Peer-Request message to inform the peer about the reason of the disconnection. Update Apply: Immediate Update Effect: When set, the Diameter Service will apply the new configuration over scoped Local Endpoints. That is, upon diameter peer connection disconnection the configured error code will be applied.")
    private DisconnectCauseHandlingPolicy.DisconnectCause disconnectCause = DisconnectCauseHandlingPolicy.DisconnectCause.fromValue("rebooting");
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Key of the disconnect cause handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Key of the disconnect cause handling policy. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public DisconnectCauseHandlingPolicy withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Can be used to enable or disable the actual DisconnectCause handling policy.
     * When disabled, the following alarm is raised: ADP Diameter, Managed Object
     * Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter
     * Service will apply the disconnect-cause-handling-policy. When disabled, the
     * Diameter Service will apply the default behavior for DPR message content
     * construction.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Can be used to enable or disable the actual DisconnectCause handling policy.
     * When disabled, the following alarm is raised: ADP Diameter, Managed Object
     * Disabled Update Apply: Immediate Update Effect: When enabled, the Diameter
     * Service will apply the disconnect-cause-handling-policy. When disabled, the
     * Diameter Service will apply the default behavior for DPR message content
     * construction.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public DisconnectCauseHandlingPolicy withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Defines the content of the Disconnect-Cause AVP to be sent in DPRs. It is
     * used in the Disconnect-Peer-Request message to inform the peer about the
     * reason of the disconnection. Update Apply: Immediate Update Effect: When set,
     * the Diameter Service will apply the new configuration over scoped Local
     * Endpoints. That is, upon diameter peer connection disconnection the
     * configured error code will be applied.
     * 
     */
    @JsonProperty("disconnect-cause")
    public DisconnectCauseHandlingPolicy.DisconnectCause getDisconnectCause()
    {
        return disconnectCause;
    }

    /**
     * Defines the content of the Disconnect-Cause AVP to be sent in DPRs. It is
     * used in the Disconnect-Peer-Request message to inform the peer about the
     * reason of the disconnection. Update Apply: Immediate Update Effect: When set,
     * the Diameter Service will apply the new configuration over scoped Local
     * Endpoints. That is, upon diameter peer connection disconnection the
     * configured error code will be applied.
     * 
     */
    @JsonProperty("disconnect-cause")
    public void setDisconnectCause(DisconnectCauseHandlingPolicy.DisconnectCause disconnectCause)
    {
        this.disconnectCause = disconnectCause;
    }

    public DisconnectCauseHandlingPolicy withDisconnectCause(DisconnectCauseHandlingPolicy.DisconnectCause disconnectCause)
    {
        this.disconnectCause = disconnectCause;
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

    public DisconnectCauseHandlingPolicy withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DisconnectCauseHandlingPolicy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("disconnectCause");
        sb.append('=');
        sb.append(((this.disconnectCause == null) ? "<null>" : this.disconnectCause));
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
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.disconnectCause == null) ? 0 : this.disconnectCause.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DisconnectCauseHandlingPolicy) == false)
        {
            return false;
        }
        DisconnectCauseHandlingPolicy rhs = ((DisconnectCauseHandlingPolicy) other);
        return (((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                && ((this.disconnectCause == rhs.disconnectCause) || ((this.disconnectCause != null) && this.disconnectCause.equals(rhs.disconnectCause))));
    }

    public enum DisconnectCause
    {

        REBOOTING("rebooting"),
        BUSY("busy"),
        DO_NOT_WANT_TO_TALK_TO_YOU("do_not_want_to_talk_to_you");

        private final String value;
        private final static Map<String, DisconnectCauseHandlingPolicy.DisconnectCause> CONSTANTS = new HashMap<String, DisconnectCauseHandlingPolicy.DisconnectCause>();

        static
        {
            for (DisconnectCauseHandlingPolicy.DisconnectCause c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private DisconnectCause(String value)
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
        public static DisconnectCauseHandlingPolicy.DisconnectCause fromValue(String value)
        {
            DisconnectCauseHandlingPolicy.DisconnectCause constant = CONSTANTS.get(value);
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
