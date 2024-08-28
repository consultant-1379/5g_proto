
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Container holding various policy objects.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "reroute-policy", "disconnect-cause-handling-policy", "congestion-handling-policy" })
public class Policies
{

    /**
     * Specifies the answer message error codes for automatic message re-routing.
     * The reroute-policy instance can be used to specify the answer message error
     * codes triggering automatic egress request message rerouting using configured
     * routing rules. The context of this policy can be scoped on AAA Service level.
     * 
     */
    @JsonProperty("reroute-policy")
    @JsonPropertyDescription("Specifies the answer message error codes for automatic message re-routing. The reroute-policy instance can be used to specify the answer message error codes triggering automatic egress request message rerouting using configured routing rules. The context of this policy can be scoped on AAA Service level.")
    private List<ReroutePolicy> reroutePolicy = new ArrayList<ReroutePolicy>();
    /**
     * Defines the content of the Disconnect-Cause AVP sent in the DPR.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    @JsonPropertyDescription("Defines the content of the Disconnect-Cause AVP sent in the DPR.")
    private List<DisconnectCauseHandlingPolicy> disconnectCauseHandlingPolicy = new ArrayList<DisconnectCauseHandlingPolicy>();
    /**
     * The congestion-handling-policy can be used to define the conditions
     * (thresholds) which will trigger a traffic congestion handling towards
     * relevant Peer Diameter Nodes. As result of congestion handling, congested
     * diameter peer connection will be excluded temporarily from the list of
     * potential routes and alternate routes (determined based on loaded routing
     * table) will be used to send egress request messages towards relevant target
     * Diameter Nodes. The context of this policy can be scoped on Peer Diameter
     * Node level
     * 
     */
    @JsonProperty("congestion-handling-policy")
    @JsonPropertyDescription("The congestion-handling-policy can be used to define the conditions (thresholds) which will trigger a traffic congestion handling towards relevant Peer Diameter Nodes. As result of congestion handling, congested diameter peer connection will be excluded temporarily from the list of potential routes and alternate routes (determined based on loaded routing table) will be used to send egress request messages towards relevant target Diameter Nodes. The context of this policy can be scoped on Peer Diameter Node level")
    private List<CongestionHandlingPolicy> congestionHandlingPolicy = new ArrayList<CongestionHandlingPolicy>();

    /**
     * Specifies the answer message error codes for automatic message re-routing.
     * The reroute-policy instance can be used to specify the answer message error
     * codes triggering automatic egress request message rerouting using configured
     * routing rules. The context of this policy can be scoped on AAA Service level.
     * 
     */
    @JsonProperty("reroute-policy")
    public List<ReroutePolicy> getReroutePolicy()
    {
        return reroutePolicy;
    }

    /**
     * Specifies the answer message error codes for automatic message re-routing.
     * The reroute-policy instance can be used to specify the answer message error
     * codes triggering automatic egress request message rerouting using configured
     * routing rules. The context of this policy can be scoped on AAA Service level.
     * 
     */
    @JsonProperty("reroute-policy")
    public void setReroutePolicy(List<ReroutePolicy> reroutePolicy)
    {
        this.reroutePolicy = reroutePolicy;
    }

    public Policies withReroutePolicy(List<ReroutePolicy> reroutePolicy)
    {
        this.reroutePolicy = reroutePolicy;
        return this;
    }

    /**
     * Defines the content of the Disconnect-Cause AVP sent in the DPR.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    public List<DisconnectCauseHandlingPolicy> getDisconnectCauseHandlingPolicy()
    {
        return disconnectCauseHandlingPolicy;
    }

    /**
     * Defines the content of the Disconnect-Cause AVP sent in the DPR.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    public void setDisconnectCauseHandlingPolicy(List<DisconnectCauseHandlingPolicy> disconnectCauseHandlingPolicy)
    {
        this.disconnectCauseHandlingPolicy = disconnectCauseHandlingPolicy;
    }

    public Policies withDisconnectCauseHandlingPolicy(List<DisconnectCauseHandlingPolicy> disconnectCauseHandlingPolicy)
    {
        this.disconnectCauseHandlingPolicy = disconnectCauseHandlingPolicy;
        return this;
    }

    /**
     * The congestion-handling-policy can be used to define the conditions
     * (thresholds) which will trigger a traffic congestion handling towards
     * relevant Peer Diameter Nodes. As result of congestion handling, congested
     * diameter peer connection will be excluded temporarily from the list of
     * potential routes and alternate routes (determined based on loaded routing
     * table) will be used to send egress request messages towards relevant target
     * Diameter Nodes. The context of this policy can be scoped on Peer Diameter
     * Node level
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public List<CongestionHandlingPolicy> getCongestionHandlingPolicy()
    {
        return congestionHandlingPolicy;
    }

    /**
     * The congestion-handling-policy can be used to define the conditions
     * (thresholds) which will trigger a traffic congestion handling towards
     * relevant Peer Diameter Nodes. As result of congestion handling, congested
     * diameter peer connection will be excluded temporarily from the list of
     * potential routes and alternate routes (determined based on loaded routing
     * table) will be used to send egress request messages towards relevant target
     * Diameter Nodes. The context of this policy can be scoped on Peer Diameter
     * Node level
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public void setCongestionHandlingPolicy(List<CongestionHandlingPolicy> congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
    }

    public Policies withCongestionHandlingPolicy(List<CongestionHandlingPolicy> congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Policies.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("reroutePolicy");
        sb.append('=');
        sb.append(((this.reroutePolicy == null) ? "<null>" : this.reroutePolicy));
        sb.append(',');
        sb.append("disconnectCauseHandlingPolicy");
        sb.append('=');
        sb.append(((this.disconnectCauseHandlingPolicy == null) ? "<null>" : this.disconnectCauseHandlingPolicy));
        sb.append(',');
        sb.append("congestionHandlingPolicy");
        sb.append('=');
        sb.append(((this.congestionHandlingPolicy == null) ? "<null>" : this.congestionHandlingPolicy));
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
        result = ((result * 31) + ((this.disconnectCauseHandlingPolicy == null) ? 0 : this.disconnectCauseHandlingPolicy.hashCode()));
        result = ((result * 31) + ((this.reroutePolicy == null) ? 0 : this.reroutePolicy.hashCode()));
        result = ((result * 31) + ((this.congestionHandlingPolicy == null) ? 0 : this.congestionHandlingPolicy.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Policies) == false)
        {
            return false;
        }
        Policies rhs = ((Policies) other);
        return ((((this.disconnectCauseHandlingPolicy == rhs.disconnectCauseHandlingPolicy)
                  || ((this.disconnectCauseHandlingPolicy != null) && this.disconnectCauseHandlingPolicy.equals(rhs.disconnectCauseHandlingPolicy)))
                 && ((this.reroutePolicy == rhs.reroutePolicy) || ((this.reroutePolicy != null) && this.reroutePolicy.equals(rhs.reroutePolicy))))
                && ((this.congestionHandlingPolicy == rhs.congestionHandlingPolicy)
                    || ((this.congestionHandlingPolicy != null) && this.congestionHandlingPolicy.equals(rhs.congestionHandlingPolicy))));
    }

}
