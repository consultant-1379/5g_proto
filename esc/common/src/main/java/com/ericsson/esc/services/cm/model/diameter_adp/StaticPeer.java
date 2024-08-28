
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id",
                     "peer-origin-host",
                     "restrict-connections",
                     "enabled",
                     "max-connection-nr",
                     "required-connection-nr",
                     "congestion-handling-policy",
                     "user-label",
                     "remote-endpoint" })
public class StaticPeer
{

    /**
     * Used to specify the key of the static-peer instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the static-peer instance.")
    private String id;
    /**
     * Used to specify the origin host of the diameter Peer Node the static-peer
     * instance is meant to represent. The value provided for this attribute, if
     * any, is matched against the value received in the Origin Host AVP during peer
     * connection establishment (CER/CEA) with a Diameter Peer. If matching fails
     * (that is, the connecting or connected Diameter Peer is no the expected one),
     * the connection establishment is rejected. No value provided for this means
     * matching diameter peer with any origin host. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the peers matching
     * the static-peer expressed constraints are dropped and reestablished by need
     * in accordance with updated information. (Required)
     * 
     */
    @JsonProperty("peer-origin-host")
    @JsonPropertyDescription("Used to specify the origin host of the diameter Peer Node the static-peer instance is meant to represent. The value provided for this attribute, if any, is matched against the value received in the Origin Host AVP during peer connection establishment (CER/CEA) with a Diameter Peer. If matching fails (that is, the connecting or connected Diameter Peer is no the expected one), the connection establishment is rejected. No value provided for this means matching diameter peer with any origin host. Update Apply: Immediate. Update Effect: All established diameter peer connections towards the peers matching the static-peer expressed constraints are dropped and reestablished by need in accordance with updated information.")
    private String peerOriginHost;
    /**
     * Used to disallow more than one diameter peer connection between Own Diameter
     * Node and the Peer Diameter Node represented by current static-peer MO. The
     * Diameter Base Protocol specifies the use of single active connection between
     * Diameter Peer Nodes. However, Diameter Nodes can be implemented by using a
     * cluster of compute resources in which case the use of single peer connection
     * between such Diameter Nodes might be a bottleneck in handling required
     * traffic throughput. Typically such Diameter Node implementations provides
     * settings through which multiple diameter peer connections towards same
     * Diameter Peer can be established. The Diameter Service provides as well
     * support for such a functionality which use can be enabled or disabled towards
     * relevant Peer Diameter Node with the help of this attribute value. Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are dropped
     * and reestablished by need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    @JsonPropertyDescription("Used to disallow more than one diameter peer connection between Own Diameter Node and the Peer Diameter Node represented by current static-peer MO. The Diameter Base Protocol specifies the use of single active connection between Diameter Peer Nodes. However, Diameter Nodes can be implemented by using a cluster of compute resources in which case the use of single peer connection between such Diameter Nodes might be a bottleneck in handling required traffic throughput. Typically such Diameter Node implementations provides settings through which multiple diameter peer connections towards same Diameter Peer can be established. The Diameter Service provides as well support for such a functionality which use can be enabled or disabled towards relevant Peer Diameter Node with the help of this attribute value. Update Apply: Immediate. Update Effect: All established diameter peer connections towards the peers matching the static-peer expressed constraints are dropped and reestablished by need in accordance with updated information.")
    private Boolean restrictConnections = true;
    /**
     * Used to enable or disable establishment of peer connections with the Diameter
     * Peer Node(s) the static-peer instance is meant to represent. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are
     * disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable establishment of peer connections with the Diameter Peer Node(s) the static-peer instance is meant to represent. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: All established diameter peer connections towards the peers matching the static-peer expressed constraints are disconnected upon setting to value false.")
    private Boolean enabled = true;
    /**
     * Limits the integer of connections built from this Peer Diameter Node. This
     * attribute is considered by the Diameter Stack if the restrict-connections
     * attribute of same static-peer instance is set to false and the Peer Diameter
     * Node represented by the static-peer is playing a connection initiation role
     * towards the Own Diameter Node (that is, the static-peer is referred by a
     * local-endpoint through a terminate-connection-from-peer reference). 0 == Any
     * integer, that is the integer of connections is not limited hereby. See also
     * restrict-connections attribute.
     * 
     */
    @JsonProperty("max-connection-nr")
    @JsonPropertyDescription("Limits the integer of connections built from this Peer Diameter Node. This attribute is considered by the Diameter Stack if the restrict-connections attribute of same static-peer instance is set to false and the Peer Diameter Node represented by the static-peer is playing a connection initiation role towards the Own Diameter Node (that is, the static-peer is referred by a local-endpoint through a terminate-connection-from-peer reference). 0 == Any integer, that is the integer of connections is not limited hereby. See also restrict-connections attribute.")
    private Long maxConnectionNr = 4294967295L;
    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * the Diameter Peer. When the configured threshold is crossed, one of the
     * following alarms is raised: ADP Diameter, Peer Connection Number Below
     * Required Level ADP Diameter, Peer Unavailable Update Apply: Immediate Update
     * Effect: When set, the configured threshold will be used by the stack during
     * alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    @JsonPropertyDescription("Used to indicate a threshold for the integer of redundant connections towards the Diameter Peer. When the configured threshold is crossed, one of the following alarms is raised: ADP Diameter, Peer Connection Number Below Required Level ADP Diameter, Peer Unavailable Update Apply: Immediate Update Effect: When set, the configured threshold will be used by the stack during alarm handling.")
    private Long requiredConnectionNr = 1L;
    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    @JsonPropertyDescription("Reference to a congestion-handling policy")
    private String congestionHandlingPolicy;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * A remote-endpoint instance is used to specify a remote endpoint for a Peer
     * Diameter Node represented by the parent static-peer instance. Many remote
     * endpoints can be defined by need for a Peer Diameter Node. It is mandatory to
     * define at least a remote endpoint if the Own Diameter Node is configured to
     * initiate connections towards the related Peer Diameter Node. If the Peer
     * Diameter Node is the one initiating connections towards the Own Diameter Node
     * the specification of a remote endpoint can be omitted for related Peer
     * Diameter Node.
     * 
     */
    @JsonProperty("remote-endpoint")
    @JsonPropertyDescription("A remote-endpoint instance is used to specify a remote endpoint for a Peer Diameter Node represented by the parent static-peer instance. Many remote endpoints can be defined by need for a Peer Diameter Node. It is mandatory to define at least a remote endpoint if the Own Diameter Node is configured to initiate connections towards the related Peer Diameter Node. If the Peer Diameter Node is the one initiating connections towards the Own Diameter Node the specification of a remote endpoint can be omitted for related Peer Diameter Node.")
    private List<RemoteEndpoint> remoteEndpoint = new ArrayList<RemoteEndpoint>();

    /**
     * Used to specify the key of the static-peer instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the static-peer instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public StaticPeer withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to specify the origin host of the diameter Peer Node the static-peer
     * instance is meant to represent. The value provided for this attribute, if
     * any, is matched against the value received in the Origin Host AVP during peer
     * connection establishment (CER/CEA) with a Diameter Peer. If matching fails
     * (that is, the connecting or connected Diameter Peer is no the expected one),
     * the connection establishment is rejected. No value provided for this means
     * matching diameter peer with any origin host. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the peers matching
     * the static-peer expressed constraints are dropped and reestablished by need
     * in accordance with updated information. (Required)
     * 
     */
    @JsonProperty("peer-origin-host")
    public String getPeerOriginHost()
    {
        return peerOriginHost;
    }

    /**
     * Used to specify the origin host of the diameter Peer Node the static-peer
     * instance is meant to represent. The value provided for this attribute, if
     * any, is matched against the value received in the Origin Host AVP during peer
     * connection establishment (CER/CEA) with a Diameter Peer. If matching fails
     * (that is, the connecting or connected Diameter Peer is no the expected one),
     * the connection establishment is rejected. No value provided for this means
     * matching diameter peer with any origin host. Update Apply: Immediate. Update
     * Effect: All established diameter peer connections towards the peers matching
     * the static-peer expressed constraints are dropped and reestablished by need
     * in accordance with updated information. (Required)
     * 
     */
    @JsonProperty("peer-origin-host")
    public void setPeerOriginHost(String peerOriginHost)
    {
        this.peerOriginHost = peerOriginHost;
    }

    public StaticPeer withPeerOriginHost(String peerOriginHost)
    {
        this.peerOriginHost = peerOriginHost;
        return this;
    }

    /**
     * Used to disallow more than one diameter peer connection between Own Diameter
     * Node and the Peer Diameter Node represented by current static-peer MO. The
     * Diameter Base Protocol specifies the use of single active connection between
     * Diameter Peer Nodes. However, Diameter Nodes can be implemented by using a
     * cluster of compute resources in which case the use of single peer connection
     * between such Diameter Nodes might be a bottleneck in handling required
     * traffic throughput. Typically such Diameter Node implementations provides
     * settings through which multiple diameter peer connections towards same
     * Diameter Peer can be established. The Diameter Service provides as well
     * support for such a functionality which use can be enabled or disabled towards
     * relevant Peer Diameter Node with the help of this attribute value. Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are dropped
     * and reestablished by need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    public Boolean getRestrictConnections()
    {
        return restrictConnections;
    }

    /**
     * Used to disallow more than one diameter peer connection between Own Diameter
     * Node and the Peer Diameter Node represented by current static-peer MO. The
     * Diameter Base Protocol specifies the use of single active connection between
     * Diameter Peer Nodes. However, Diameter Nodes can be implemented by using a
     * cluster of compute resources in which case the use of single peer connection
     * between such Diameter Nodes might be a bottleneck in handling required
     * traffic throughput. Typically such Diameter Node implementations provides
     * settings through which multiple diameter peer connections towards same
     * Diameter Peer can be established. The Diameter Service provides as well
     * support for such a functionality which use can be enabled or disabled towards
     * relevant Peer Diameter Node with the help of this attribute value. Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are dropped
     * and reestablished by need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    public void setRestrictConnections(Boolean restrictConnections)
    {
        this.restrictConnections = restrictConnections;
    }

    public StaticPeer withRestrictConnections(Boolean restrictConnections)
    {
        this.restrictConnections = restrictConnections;
        return this;
    }

    /**
     * Used to enable or disable establishment of peer connections with the Diameter
     * Peer Node(s) the static-peer instance is meant to represent. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are
     * disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable establishment of peer connections with the Diameter
     * Peer Node(s) the static-peer instance is meant to represent. When disabled,
     * the following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the static-peer expressed constraints are
     * disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public StaticPeer withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Limits the integer of connections built from this Peer Diameter Node. This
     * attribute is considered by the Diameter Stack if the restrict-connections
     * attribute of same static-peer instance is set to false and the Peer Diameter
     * Node represented by the static-peer is playing a connection initiation role
     * towards the Own Diameter Node (that is, the static-peer is referred by a
     * local-endpoint through a terminate-connection-from-peer reference). 0 == Any
     * integer, that is the integer of connections is not limited hereby. See also
     * restrict-connections attribute.
     * 
     */
    @JsonProperty("max-connection-nr")
    public Long getMaxConnectionNr()
    {
        return maxConnectionNr;
    }

    /**
     * Limits the integer of connections built from this Peer Diameter Node. This
     * attribute is considered by the Diameter Stack if the restrict-connections
     * attribute of same static-peer instance is set to false and the Peer Diameter
     * Node represented by the static-peer is playing a connection initiation role
     * towards the Own Diameter Node (that is, the static-peer is referred by a
     * local-endpoint through a terminate-connection-from-peer reference). 0 == Any
     * integer, that is the integer of connections is not limited hereby. See also
     * restrict-connections attribute.
     * 
     */
    @JsonProperty("max-connection-nr")
    public void setMaxConnectionNr(Long maxConnectionNr)
    {
        this.maxConnectionNr = maxConnectionNr;
    }

    public StaticPeer withMaxConnectionNr(Long maxConnectionNr)
    {
        this.maxConnectionNr = maxConnectionNr;
        return this;
    }

    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * the Diameter Peer. When the configured threshold is crossed, one of the
     * following alarms is raised: ADP Diameter, Peer Connection Number Below
     * Required Level ADP Diameter, Peer Unavailable Update Apply: Immediate Update
     * Effect: When set, the configured threshold will be used by the stack during
     * alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    public Long getRequiredConnectionNr()
    {
        return requiredConnectionNr;
    }

    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * the Diameter Peer. When the configured threshold is crossed, one of the
     * following alarms is raised: ADP Diameter, Peer Connection Number Below
     * Required Level ADP Diameter, Peer Unavailable Update Apply: Immediate Update
     * Effect: When set, the configured threshold will be used by the stack during
     * alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    public void setRequiredConnectionNr(Long requiredConnectionNr)
    {
        this.requiredConnectionNr = requiredConnectionNr;
    }

    public StaticPeer withRequiredConnectionNr(Long requiredConnectionNr)
    {
        this.requiredConnectionNr = requiredConnectionNr;
        return this;
    }

    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public String getCongestionHandlingPolicy()
    {
        return congestionHandlingPolicy;
    }

    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public void setCongestionHandlingPolicy(String congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
    }

    public StaticPeer withCongestionHandlingPolicy(String congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
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

    public StaticPeer withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A remote-endpoint instance is used to specify a remote endpoint for a Peer
     * Diameter Node represented by the parent static-peer instance. Many remote
     * endpoints can be defined by need for a Peer Diameter Node. It is mandatory to
     * define at least a remote endpoint if the Own Diameter Node is configured to
     * initiate connections towards the related Peer Diameter Node. If the Peer
     * Diameter Node is the one initiating connections towards the Own Diameter Node
     * the specification of a remote endpoint can be omitted for related Peer
     * Diameter Node.
     * 
     */
    @JsonProperty("remote-endpoint")
    public List<RemoteEndpoint> getRemoteEndpoint()
    {
        return remoteEndpoint;
    }

    /**
     * A remote-endpoint instance is used to specify a remote endpoint for a Peer
     * Diameter Node represented by the parent static-peer instance. Many remote
     * endpoints can be defined by need for a Peer Diameter Node. It is mandatory to
     * define at least a remote endpoint if the Own Diameter Node is configured to
     * initiate connections towards the related Peer Diameter Node. If the Peer
     * Diameter Node is the one initiating connections towards the Own Diameter Node
     * the specification of a remote endpoint can be omitted for related Peer
     * Diameter Node.
     * 
     */
    @JsonProperty("remote-endpoint")
    public void setRemoteEndpoint(List<RemoteEndpoint> remoteEndpoint)
    {
        this.remoteEndpoint = remoteEndpoint;
    }

    public StaticPeer withRemoteEndpoint(List<RemoteEndpoint> remoteEndpoint)
    {
        this.remoteEndpoint = remoteEndpoint;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticPeer.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("peerOriginHost");
        sb.append('=');
        sb.append(((this.peerOriginHost == null) ? "<null>" : this.peerOriginHost));
        sb.append(',');
        sb.append("restrictConnections");
        sb.append('=');
        sb.append(((this.restrictConnections == null) ? "<null>" : this.restrictConnections));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("maxConnectionNr");
        sb.append('=');
        sb.append(((this.maxConnectionNr == null) ? "<null>" : this.maxConnectionNr));
        sb.append(',');
        sb.append("requiredConnectionNr");
        sb.append('=');
        sb.append(((this.requiredConnectionNr == null) ? "<null>" : this.requiredConnectionNr));
        sb.append(',');
        sb.append("congestionHandlingPolicy");
        sb.append('=');
        sb.append(((this.congestionHandlingPolicy == null) ? "<null>" : this.congestionHandlingPolicy));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("remoteEndpoint");
        sb.append('=');
        sb.append(((this.remoteEndpoint == null) ? "<null>" : this.remoteEndpoint));
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
        result = ((result * 31) + ((this.remoteEndpoint == null) ? 0 : this.remoteEndpoint.hashCode()));
        result = ((result * 31) + ((this.requiredConnectionNr == null) ? 0 : this.requiredConnectionNr.hashCode()));
        result = ((result * 31) + ((this.peerOriginHost == null) ? 0 : this.peerOriginHost.hashCode()));
        result = ((result * 31) + ((this.restrictConnections == null) ? 0 : this.restrictConnections.hashCode()));
        result = ((result * 31) + ((this.maxConnectionNr == null) ? 0 : this.maxConnectionNr.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
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
        if ((other instanceof StaticPeer) == false)
        {
            return false;
        }
        StaticPeer rhs = ((StaticPeer) other);
        return ((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                       && ((this.remoteEndpoint == rhs.remoteEndpoint) || ((this.remoteEndpoint != null) && this.remoteEndpoint.equals(rhs.remoteEndpoint))))
                      && ((this.requiredConnectionNr == rhs.requiredConnectionNr)
                          || ((this.requiredConnectionNr != null) && this.requiredConnectionNr.equals(rhs.requiredConnectionNr))))
                     && ((this.peerOriginHost == rhs.peerOriginHost) || ((this.peerOriginHost != null) && this.peerOriginHost.equals(rhs.peerOriginHost))))
                    && ((this.restrictConnections == rhs.restrictConnections)
                        || ((this.restrictConnections != null) && this.restrictConnections.equals(rhs.restrictConnections))))
                   && ((this.maxConnectionNr == rhs.maxConnectionNr) || ((this.maxConnectionNr != null) && this.maxConnectionNr.equals(rhs.maxConnectionNr))))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                && ((this.congestionHandlingPolicy == rhs.congestionHandlingPolicy)
                    || ((this.congestionHandlingPolicy != null) && this.congestionHandlingPolicy.equals(rhs.congestionHandlingPolicy))));
    }

}
