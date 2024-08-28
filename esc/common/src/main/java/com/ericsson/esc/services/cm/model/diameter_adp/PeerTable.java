
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A peer-table system created singleton instance is to be used as a container
 * for static-peer and dynamic-peer-acceptor instances that are used to
 * scope/filter the Diameter Peer Nodes which should be considered and stored by
 * the Diameter Service in its internal Peer Table.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "static-peer", "dynamic-peer-acceptor" })
public class PeerTable
{

    /**
     * A static-peer instance is used to describe in an explicit (static) way a Peer
     * Diameter Node. Explicit specification of a Peer Diameter Node is mandated
     * when the own Diameter Node (represented by a node instance) is expected to
     * initiate transport connection setups towards it. Explicit specification of a
     * Peer Diameter Node is optional when the Peer Diameter Node is expected to
     * initiate transport connection setups towards the own Diameter Node. In such a
     * case a Peer Diameter Node or a matching rule set for Peer Diameter Nodes can
     * be specified by using the more generic dynamic-peer-acceptor. Changes on
     * static-peer instance level might influence the transport connections already
     * established between the Own Diameter Node and Peer Diameter Node.
     * 
     */
    @JsonProperty("static-peer")
    @JsonPropertyDescription("A static-peer instance is used to describe in an explicit (static) way a Peer Diameter Node. Explicit specification of a Peer Diameter Node is mandated when the own Diameter Node (represented by a node instance) is expected to initiate transport connection setups towards it. Explicit specification of a Peer Diameter Node is optional when the Peer Diameter Node is expected to initiate transport connection setups towards the own Diameter Node. In such a case a Peer Diameter Node or a matching rule set for Peer Diameter Nodes can be specified by using the more generic dynamic-peer-acceptor. Changes on static-peer instance level might influence the transport connections already established between the Own Diameter Node and Peer Diameter Node.")
    private List<StaticPeer> staticPeer = new ArrayList<StaticPeer>();
    /**
     * A dynamic-peer-acceptor instance is used to describe the conditions based on
     * which peer connection setups initiated by one or a collection of Diameter
     * Peer Nodes are to be accepted or rejected by Own Diameter Node. The Diameter
     * Peer Node identifiers (like peer host IP address, origin host or origin
     * realm) are matched against the related value matching patterns expressed in a
     * dynamic-peer-acceptor instance. If matching is found towards all the
     * expressed patterns the initiated peer connection is accepted by diameter.
     * Otherwise, the remote peer connection initiation is rejected. Changes on
     * dynamic-peer-acceptor instance level might influence all the peer connections
     * already established as result of matching towards the patterns expressed in
     * related peer connection acceptor instance.
     * 
     */
    @JsonProperty("dynamic-peer-acceptor")
    @JsonPropertyDescription("A dynamic-peer-acceptor instance is used to describe the conditions based on which peer connection setups initiated by one or a collection of Diameter Peer Nodes are to be accepted or rejected by Own Diameter Node. The Diameter Peer Node identifiers (like peer host IP address, origin host or origin realm) are matched against the related value matching patterns expressed in a dynamic-peer-acceptor instance. If matching is found towards all the expressed patterns the initiated peer connection is accepted by diameter. Otherwise, the remote peer connection initiation is rejected. Changes on dynamic-peer-acceptor instance level might influence all the peer connections already established as result of matching towards the patterns expressed in related peer connection acceptor instance.")
    private List<DynamicPeerAcceptor> dynamicPeerAcceptor = new ArrayList<DynamicPeerAcceptor>();

    /**
     * A static-peer instance is used to describe in an explicit (static) way a Peer
     * Diameter Node. Explicit specification of a Peer Diameter Node is mandated
     * when the own Diameter Node (represented by a node instance) is expected to
     * initiate transport connection setups towards it. Explicit specification of a
     * Peer Diameter Node is optional when the Peer Diameter Node is expected to
     * initiate transport connection setups towards the own Diameter Node. In such a
     * case a Peer Diameter Node or a matching rule set for Peer Diameter Nodes can
     * be specified by using the more generic dynamic-peer-acceptor. Changes on
     * static-peer instance level might influence the transport connections already
     * established between the Own Diameter Node and Peer Diameter Node.
     * 
     */
    @JsonProperty("static-peer")
    public List<StaticPeer> getStaticPeer()
    {
        return staticPeer;
    }

    /**
     * A static-peer instance is used to describe in an explicit (static) way a Peer
     * Diameter Node. Explicit specification of a Peer Diameter Node is mandated
     * when the own Diameter Node (represented by a node instance) is expected to
     * initiate transport connection setups towards it. Explicit specification of a
     * Peer Diameter Node is optional when the Peer Diameter Node is expected to
     * initiate transport connection setups towards the own Diameter Node. In such a
     * case a Peer Diameter Node or a matching rule set for Peer Diameter Nodes can
     * be specified by using the more generic dynamic-peer-acceptor. Changes on
     * static-peer instance level might influence the transport connections already
     * established between the Own Diameter Node and Peer Diameter Node.
     * 
     */
    @JsonProperty("static-peer")
    public void setStaticPeer(List<StaticPeer> staticPeer)
    {
        this.staticPeer = staticPeer;
    }

    public PeerTable withStaticPeer(List<StaticPeer> staticPeer)
    {
        this.staticPeer = staticPeer;
        return this;
    }

    /**
     * A dynamic-peer-acceptor instance is used to describe the conditions based on
     * which peer connection setups initiated by one or a collection of Diameter
     * Peer Nodes are to be accepted or rejected by Own Diameter Node. The Diameter
     * Peer Node identifiers (like peer host IP address, origin host or origin
     * realm) are matched against the related value matching patterns expressed in a
     * dynamic-peer-acceptor instance. If matching is found towards all the
     * expressed patterns the initiated peer connection is accepted by diameter.
     * Otherwise, the remote peer connection initiation is rejected. Changes on
     * dynamic-peer-acceptor instance level might influence all the peer connections
     * already established as result of matching towards the patterns expressed in
     * related peer connection acceptor instance.
     * 
     */
    @JsonProperty("dynamic-peer-acceptor")
    public List<DynamicPeerAcceptor> getDynamicPeerAcceptor()
    {
        return dynamicPeerAcceptor;
    }

    /**
     * A dynamic-peer-acceptor instance is used to describe the conditions based on
     * which peer connection setups initiated by one or a collection of Diameter
     * Peer Nodes are to be accepted or rejected by Own Diameter Node. The Diameter
     * Peer Node identifiers (like peer host IP address, origin host or origin
     * realm) are matched against the related value matching patterns expressed in a
     * dynamic-peer-acceptor instance. If matching is found towards all the
     * expressed patterns the initiated peer connection is accepted by diameter.
     * Otherwise, the remote peer connection initiation is rejected. Changes on
     * dynamic-peer-acceptor instance level might influence all the peer connections
     * already established as result of matching towards the patterns expressed in
     * related peer connection acceptor instance.
     * 
     */
    @JsonProperty("dynamic-peer-acceptor")
    public void setDynamicPeerAcceptor(List<DynamicPeerAcceptor> dynamicPeerAcceptor)
    {
        this.dynamicPeerAcceptor = dynamicPeerAcceptor;
    }

    public PeerTable withDynamicPeerAcceptor(List<DynamicPeerAcceptor> dynamicPeerAcceptor)
    {
        this.dynamicPeerAcceptor = dynamicPeerAcceptor;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PeerTable.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("staticPeer");
        sb.append('=');
        sb.append(((this.staticPeer == null) ? "<null>" : this.staticPeer));
        sb.append(',');
        sb.append("dynamicPeerAcceptor");
        sb.append('=');
        sb.append(((this.dynamicPeerAcceptor == null) ? "<null>" : this.dynamicPeerAcceptor));
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
        result = ((result * 31) + ((this.staticPeer == null) ? 0 : this.staticPeer.hashCode()));
        result = ((result * 31) + ((this.dynamicPeerAcceptor == null) ? 0 : this.dynamicPeerAcceptor.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PeerTable) == false)
        {
            return false;
        }
        PeerTable rhs = ((PeerTable) other);
        return (((this.staticPeer == rhs.staticPeer) || ((this.staticPeer != null) && this.staticPeer.equals(rhs.staticPeer)))
                && ((this.dynamicPeerAcceptor == rhs.dynamicPeerAcceptor)
                    || ((this.dynamicPeerAcceptor != null) && this.dynamicPeerAcceptor.equals(rhs.dynamicPeerAcceptor))));
    }

}
