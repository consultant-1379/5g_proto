
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "rank", "peer", "user-label" })
public class DynamicPeerGroup
{

    /**
     * Used to specify the key of the dynamic-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the dynamic-peer-group instance.")
    private String id;
    /**
     * Used to assign a rank to the set of Diameter Peer Nodes matching with related
     * identity patterns expressed through dynamic-peer-acceptor instances referred
     * by the peer values. The lower the rank attribute value the higher the
     * configured rank of the referred Peer Diameter Nodes. The default value is 0
     * (highest rank). Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("Used to assign a rank to the set of Diameter Peer Nodes matching with related identity patterns expressed through dynamic-peer-acceptor instances referred by the peer values. The lower the rank attribute value the higher the configured rank of the referred Peer Diameter Nodes. The default value is 0 (highest rank). Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private Long rank = 0L;
    /**
     * Used to refer to a set of dynamic-peer-acceptor instance accepted Diameter
     * Peer Nodes ought to be assigned with same rank. Update Apply: Immediate.
     * Update Effect: No effect on already established peer connections but on
     * routing information. Introduced change will be applied next time a routing
     * entry is evaluated. (Required)
     * 
     */
    @JsonProperty("peer")
    @JsonPropertyDescription("Used to refer to a set of dynamic-peer-acceptor instance accepted Diameter Peer Nodes ought to be assigned with same rank. Update Apply: Immediate. Update Effect: No effect on already established peer connections but on routing information. Introduced change will be applied next time a routing entry is evaluated.")
    private List<String> peer = new ArrayList<String>();
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the dynamic-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the dynamic-peer-group instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public DynamicPeerGroup withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to assign a rank to the set of Diameter Peer Nodes matching with related
     * identity patterns expressed through dynamic-peer-acceptor instances referred
     * by the peer values. The lower the rank attribute value the higher the
     * configured rank of the referred Peer Diameter Nodes. The default value is 0
     * (highest rank). Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    public Long getRank()
    {
        return rank;
    }

    /**
     * Used to assign a rank to the set of Diameter Peer Nodes matching with related
     * identity patterns expressed through dynamic-peer-acceptor instances referred
     * by the peer values. The lower the rank attribute value the higher the
     * configured rank of the referred Peer Diameter Nodes. The default value is 0
     * (highest rank). Update Apply: Immediate. Update Effect: No effect on already
     * established peer connections but on routing information. Introduced change
     * will be applied next time a routing entry is evaluated.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Long rank)
    {
        this.rank = rank;
    }

    public DynamicPeerGroup withRank(Long rank)
    {
        this.rank = rank;
        return this;
    }

    /**
     * Used to refer to a set of dynamic-peer-acceptor instance accepted Diameter
     * Peer Nodes ought to be assigned with same rank. Update Apply: Immediate.
     * Update Effect: No effect on already established peer connections but on
     * routing information. Introduced change will be applied next time a routing
     * entry is evaluated. (Required)
     * 
     */
    @JsonProperty("peer")
    public List<String> getPeer()
    {
        return peer;
    }

    /**
     * Used to refer to a set of dynamic-peer-acceptor instance accepted Diameter
     * Peer Nodes ought to be assigned with same rank. Update Apply: Immediate.
     * Update Effect: No effect on already established peer connections but on
     * routing information. Introduced change will be applied next time a routing
     * entry is evaluated. (Required)
     * 
     */
    @JsonProperty("peer")
    public void setPeer(List<String> peer)
    {
        this.peer = peer;
    }

    public DynamicPeerGroup withPeer(List<String> peer)
    {
        this.peer = peer;
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

    public DynamicPeerGroup withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DynamicPeerGroup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null) ? "<null>" : this.rank));
        sb.append(',');
        sb.append("peer");
        sb.append('=');
        sb.append(((this.peer == null) ? "<null>" : this.peer));
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
        result = ((result * 31) + ((this.rank == null) ? 0 : this.rank.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.peer == null) ? 0 : this.peer.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DynamicPeerGroup) == false)
        {
            return false;
        }
        DynamicPeerGroup rhs = ((DynamicPeerGroup) other);
        return (((((this.rank == rhs.rank) || ((this.rank != null) && this.rank.equals(rhs.rank)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.peer == rhs.peer) || ((this.peer != null) && this.peer.equals(rhs.peer))));
    }

}
