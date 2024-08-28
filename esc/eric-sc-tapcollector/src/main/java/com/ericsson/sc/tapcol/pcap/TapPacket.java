/**
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 27, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol.pcap;

import java.sql.Timestamp;

import org.pcap4j.packet.Packet;

/**
 * A captured packet
 *
 */
public class TapPacket
{
    private final Packet packet;
    private final Timestamp timeStamp;

    public TapPacket(Packet packet,
                     Timestamp timeStamp)
    {
        this.packet = packet;
        this.timeStamp = timeStamp;
    }

    /**
     * 
     * @return The parsed packet contents
     */
    public Packet getPacket()
    {
        return packet;
    }

    /**
     * 
     * @return The capture time stamp
     */
    public Timestamp getTimeStamp()
    {
        return timeStamp;
    }

    @Override
    public String toString()
    {
        return "TapPacket [packet=" + packet + ", timeStamp=" + timeStamp + "]";
    }

}