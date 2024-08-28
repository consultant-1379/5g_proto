/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 12, 2022
 *     Author: zpavcha
 */

package com.ericsson.utilities.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The set of {source IP address, source port, destination IP address,
 * destination port} of a packet.
 */
public class FourTuple
{
    private static final Logger log = LoggerFactory.getLogger(FourTuple.class);
    private final String srcIpAddress;
    private final String dstIpAddress;
    private final Integer srcPort;
    private final Integer dstPort;

    private FourTuple(Builder builder)
    {
        this.srcIpAddress = builder.srcIpAddress;
        this.dstIpAddress = builder.dstIpAddress;
        this.srcPort = builder.srcPort;
        this.dstPort = builder.dstPort;
    }

    // Getters

    /**
     * Get the source IP address.
     *
     * @return The IP address.
     */
    public String getSrcIpAddress()
    {
        return this.srcIpAddress;
    }

    /**
     * Get the destination IP address.
     *
     * @return The IP address.
     */
    public String getDstIpAddress()
    {
        return this.dstIpAddress;
    }

    /**
     * Get the source port.
     *
     * @return The port.
     */
    public int getSrcPort()
    {
        return this.srcPort;
    }

    /**
     * Get the destination port.
     *
     * @return The port.
     */
    public int getDstPort()
    {
        return this.dstPort;
    }

    public static class Builder
    {
        private String srcIpAddress;
        private String dstIpAddress;
        private Integer srcPort;
        private Integer dstPort;

        public Builder withSrcIpAddress(String srcIpAddress)
        {
            this.srcIpAddress = srcIpAddress;
            return this;
        }

        public Builder withDstIpAddress(String dstIpAddress)
        {
            this.dstIpAddress = dstIpAddress;
            return this;
        }

        public Builder withSrcPort(int srcPort)
        {
            this.srcPort = srcPort;
            return this;
        }

        public Builder withDstPort(int dstPort)
        {
            this.dstPort = dstPort;
            return this;
        }

        public FourTuple build()
        {
            return new FourTuple(this);
        }

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

        FourTuple other = (FourTuple) obj;

        if (srcIpAddress == null)
        {
            if (other.srcIpAddress != null)
                return false;
        }
        else if (!srcIpAddress.equals(other.srcIpAddress))
            return false;

        if (dstIpAddress == null)
        {
            if (other.dstIpAddress != null)
                return false;
        }
        else if (!dstIpAddress.equals(other.dstIpAddress))
            return false;

        if (srcPort == null)
        {
            if (other.srcPort != null)
                return false;
        }
        else if (!srcPort.equals(other.srcPort))
            return false;

        if (dstPort == null)
        {
            if (other.dstPort != null)
                return false;
        }
        else if (!dstPort.equals(other.dstPort))
            return false;

        return true;
    }

    /**
     * Validates if the provided packet has the same IP addresses with this. In case
     * the source and destination IP addresses don't match with each other, it
     * checks in the opposite direction.
     *
     * @param obj The packet to compare with.
     * @return True, if the two packets have the same two IP addresses (source and
     *         destination) and false, otherwise.
     */
    public boolean hasSameIpAddressesWith(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        FourTuple other = (FourTuple) obj;

        if (srcIpAddress == null)
        {
            if (other.srcIpAddress != null)
                return false;
        }
        else if (!compareIpAddresses(srcIpAddress, other.srcIpAddress) && !compareIpAddresses(srcIpAddress, other.dstIpAddress))
            return false;

        if (dstIpAddress == null)
        {
            if (other.dstIpAddress != null)
                return false;
        }
        else if (!compareIpAddresses(dstIpAddress, other.srcIpAddress) && !compareIpAddresses(dstIpAddress, other.dstIpAddress))
            return false;

        return true;
    }

    private boolean compareIpAddresses(String addr1,
                                       String addr2)
    {
        try
        {
            var inetAddr1 = InetAddress.getByName(addr1);
            var inetAddr2 = InetAddress.getByName(addr2);
            log.debug("Comparing {} {}", inetAddr1, inetAddr2);
            return inetAddr2.equals(inetAddr1);
        }
        catch (UnknownHostException e)
        {
            log.error("Unable to parse ip address", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
    {
        return String.format("Source: %s:%s, Destination: %s:%s", this.srcIpAddress, this.srcPort, this.dstIpAddress, this.dstPort);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.srcIpAddress, this.srcPort, this.dstIpAddress, this.dstPort);
    }

}