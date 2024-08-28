/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information about known Diameter Peers. Information is obtained from
 * proxy gRPC component
 */
public final class PeerTable
{
    private static final Logger log = LoggerFactory.getLogger(PeerTable.class);
    /**
     * Peers by peerId
     */
    final HashMap<String, DiameterPeer> peers;

    /**
     * Construct an empty peer table
     */
    public PeerTable()
    {
        this.peers = new HashMap<>();
    }

    /**
     * Add or replace a diameter peer. Replacement is unexpected and generates
     * warning
     * 
     * @param peer The Diameter peer to add
     * @return the new PeerTable after addition
     */
    public PeerTable addPeer(DiameterPeer peer)
    {
        final var copy = new PeerTable(this);
        copy.add(peer);
        return copy;
    }

    /**
     * Remove a peer from the peer table.
     * 
     * @param peerId The unique key of the peer, as defined by proxy gRPC
     * @return The new PeerTable after deletion
     */
    public PeerTable removePeer(String peerId)
    {
        final var copy = new PeerTable(this);
        copy.remove(peerId);
        return copy;
    }

    private PeerTable(PeerTable oldTable)
    {
        this.peers = oldTable.peers;
    }

    private void add(DiameterPeer peer)
    {
        final var res = this.peers.put(peer.getPeerId(), peer);
        if (res != null && !res.equals(peer))
        {
            log.warn("New peer replaced old with same peerId: {} old: {}", peer, res);
        }
    }

    private void remove(String peerId)
    {
        final var found = this.peers.remove(peerId);
        if (found == null)
        {
            log.warn("Not removing non-existent Diameter peer with peerId: {}", peerId);
        }
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("PeerTable [peers=");
        builder.append(peers.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toUnmodifiableList()));
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(peers);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PeerTable other = (PeerTable) obj;
        return Objects.equals(peers, other.peers);
    }

}
