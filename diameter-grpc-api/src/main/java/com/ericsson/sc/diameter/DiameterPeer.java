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

import java.util.Objects;

public class DiameterPeer
{
    private final String peerId;
    private final String hostIdentity;
    private final String realm;

    public String getPeerId()
    {
        return this.peerId;
    }

    public String getHostIdentity()
    {
        return hostIdentity;
    }

    public String getRealm()
    {
        return realm;
    }

    public DiameterPeer(String peerId,
                        String hostIdentity,
                        String realm)
    {
        this.peerId = peerId;
        this.hostIdentity = hostIdentity;
        this.realm = realm;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hostIdentity, peerId, realm);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DiameterPeer other = (DiameterPeer) obj;
        return Objects.equals(hostIdentity, other.hostIdentity) && Objects.equals(peerId, other.peerId) && Objects.equals(realm, other.realm);
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("DiameterPeer [peerId=");
        builder.append(peerId);
        builder.append(", hostIdentity=");
        builder.append(hostIdentity);
        builder.append(", realm=");
        builder.append(realm);
        builder.append("]");
        return builder.toString();
    }

}
