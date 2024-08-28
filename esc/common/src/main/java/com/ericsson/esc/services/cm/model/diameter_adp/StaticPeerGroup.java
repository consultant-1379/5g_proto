
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "rank", "user-label", "peer-reference" })
public class StaticPeerGroup
{

    /**
     * Used to specify the key of the static-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the static-peer-group instance.")
    private String id;
    /**
     * Used to assign a rank to the group of Diameter Peer Nodes represented by the
     * collection of static-peer instances referred by child peer-reference
     * instances. The lower the rank value the higher the configured rank of the
     * static-peer-group. The default value is 0 (highest rank). Update Apply:
     * Immediate. Update Effect: No effect on already established peer connections
     * but on routing information. Introduced change will be applied next time a
     * routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("Used to assign a rank to the group of Diameter Peer Nodes represented by the collection of static-peer instances referred by child peer-reference instances. The lower the rank value the higher the configured rank of the static-peer-group. The default value is 0 (highest rank). Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private Long rank = 0L;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * A peer-reference instance is used to assign a rank to an explicit
     * representation of a Diameter Peer Node (represented through referred
     * static-peer instance). Any change on peer-reference instance values will be
     * applied immediately impacting in this way the related routing entries
     * evaluated by the routing logic of diameter. (Required)
     * 
     */
    @JsonProperty("peer-reference")
    @JsonPropertyDescription("A peer-reference instance is used to assign a rank to an explicit representation of a Diameter Peer Node (represented through referred static-peer instance). Any change on peer-reference instance values will be applied immediately impacting in this way the related routing entries evaluated by the routing logic of diameter.")
    private List<PeerReference> peerReference = new ArrayList<PeerReference>();

    /**
     * Used to specify the key of the static-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the static-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public StaticPeerGroup withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to assign a rank to the group of Diameter Peer Nodes represented by the
     * collection of static-peer instances referred by child peer-reference
     * instances. The lower the rank value the higher the configured rank of the
     * static-peer-group. The default value is 0 (highest rank). Update Apply:
     * Immediate. Update Effect: No effect on already established peer connections
     * but on routing information. Introduced change will be applied next time a
     * routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    public Long getRank()
    {
        return rank;
    }

    /**
     * Used to assign a rank to the group of Diameter Peer Nodes represented by the
     * collection of static-peer instances referred by child peer-reference
     * instances. The lower the rank value the higher the configured rank of the
     * static-peer-group. The default value is 0 (highest rank). Update Apply:
     * Immediate. Update Effect: No effect on already established peer connections
     * but on routing information. Introduced change will be applied next time a
     * routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Long rank)
    {
        this.rank = rank;
    }

    public StaticPeerGroup withRank(Long rank)
    {
        this.rank = rank;
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

    public StaticPeerGroup withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A peer-reference instance is used to assign a rank to an explicit
     * representation of a Diameter Peer Node (represented through referred
     * static-peer instance). Any change on peer-reference instance values will be
     * applied immediately impacting in this way the related routing entries
     * evaluated by the routing logic of diameter. (Required)
     * 
     */
    @JsonProperty("peer-reference")
    public List<PeerReference> getPeerReference()
    {
        return peerReference;
    }

    /**
     * A peer-reference instance is used to assign a rank to an explicit
     * representation of a Diameter Peer Node (represented through referred
     * static-peer instance). Any change on peer-reference instance values will be
     * applied immediately impacting in this way the related routing entries
     * evaluated by the routing logic of diameter. (Required)
     * 
     */
    @JsonProperty("peer-reference")
    public void setPeerReference(List<PeerReference> peerReference)
    {
        this.peerReference = peerReference;
    }

    public StaticPeerGroup withPeerReference(List<PeerReference> peerReference)
    {
        this.peerReference = peerReference;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticPeerGroup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null) ? "<null>" : this.rank));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("peerReference");
        sb.append('=');
        sb.append(((this.peerReference == null) ? "<null>" : this.peerReference));
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
        result = ((result * 31) + ((this.rank == null) ? 0 : this.rank.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.peerReference == null) ? 0 : this.peerReference.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof StaticPeerGroup) == false)
        {
            return false;
        }
        StaticPeerGroup rhs = ((StaticPeerGroup) other);
        return (((((this.rank == rhs.rank) || ((this.rank != null) && this.rank.equals(rhs.rank)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.peerReference == rhs.peerReference) || ((this.peerReference != null) && this.peerReference.equals(rhs.peerReference))));
    }

}
