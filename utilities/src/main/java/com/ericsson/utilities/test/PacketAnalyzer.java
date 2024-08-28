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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.util.ByteArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives packets (one by one), categorizes them and performs validations. The
 * categorization is such that all packets that belong to the same connection
 * and have the same direction (request - reply), match.
 */

public class PacketAnalyzer
{
    private static final String IP_TUPLE_MISMATCH = "Error in IP validation for tuple [{}]\n";
    private static final String IP_MISMATCH = "IP addresses do not match with the given ones in any direction";
    private static final String SQN_NUMBER_ERROR_TUPLE = "Error in packet sequence numbers validation for tuple:\n{}";
    private static final String SQN_NUMBER_INFO_TUPLE = "Sequence number of packet {} in tuple [{}] -> {}";
    private static final String ERROR_URI = "Error in validating URI";

    private static final Logger log = LoggerFactory.getLogger(PacketAnalyzer.class);

    private final List<Packet> packets = new ArrayList<>();
    private final Set<FourTuple> keySet = new LinkedHashSet<>();
    private final Map<FourTuple, List<Packet>> categorizedPackets = new HashMap<FourTuple, List<Packet>>(); // Categorize per connection AND direction

    private int numOfConnections;
    private int numPacketsPerConnection;
    private boolean ipv6Enabled;

    public PacketAnalyzer(boolean ipv6Enabled)
    {
        this.ipv6Enabled = ipv6Enabled;
    }

    public synchronized void put(Packet packet)
    {
        packets.add(packet);
    }

    public int getNumOfConnections()
    {
        return this.numOfConnections;
    }

    public int getNumOfPacketsPerConnection()
    {
        return this.numPacketsPerConnection;
    }

    /**
     * Categorizes the incoming packets with respect to connection and direction.
     * The result is a Map where the key is a unique set of {source IP address,
     * source port, destination IP address, destination port} which its called a
     * four tuple. For each key the value is a list of all the packets that have the
     * same four tuple.
     */
    public void analyze()
    {
        if (this.packets.isEmpty())
            throw new IllegalArgumentException("No packets sent");

        for (Packet packet : this.packets)
        {
            final var packetTuple = this.getTuple(packet);

            this.keySet.add(packetTuple);
        }

        for (FourTuple fourTuple : keySet)
        {
            List<Packet> packetsPerConnectionAndDirection = new ArrayList<>();

            for (Packet packet : this.packets)
            {
                final var packetTuple = this.getTuple(packet);

                if (fourTuple.equals(packetTuple))
                {
                    packetsPerConnectionAndDirection.add(packet);
                }

            }
            this.categorizedPackets.put(fourTuple, packetsPerConnectionAndDirection);
            this.numPacketsPerConnection = packetsPerConnectionAndDirection.size();
        }

        this.numOfConnections = keySet.size() / 2; // Because we have 2 directions
    }

    /**
     * Validates the IP addresses of both client and server. The two IP addresses
     * are interchangeable.
     * 
     * @param clientIpAddress. The client IP address.
     * @param serverIpAddress. The server IP address.
     * @return True if all packets have these two IP addresses, false otherwise.
     */
    public boolean validateIpAddresses(String clientIpAddress,
                                       String serverIpAddress)
    {
        var result = true;

        final var testTuple = new FourTuple.Builder().withSrcIpAddress(clientIpAddress).withDstIpAddress(serverIpAddress).build();

        outerloop: for (FourTuple fourTuple : this.keySet)
        {
            if (testTuple.hasSameIpAddressesWith(fourTuple))
            {
                for (Packet packet : this.categorizedPackets.get(fourTuple))
                {
                    final var packetTuple = getTuple(packet);
                    result = result && testTuple.hasSameIpAddressesWith(packetTuple);

                    if (!result)
                    {
                        log.error(IP_TUPLE_MISMATCH, fourTuple.toString());
                        break outerloop;
                    }
                }
            }
            else
            {
                log.error(IP_MISMATCH);
                result = false;
                break;
            }

        }

        return result;
    }

    /**
     * Validates whether the sequence numbers are generated successfully. The
     * sequence numbers are incremented per direction and per connection.
     * 
     * @return True if the validation was successful, false otherwise.
     */
    public boolean validateSequenceNumbers()
    {
        var result = true;
        outerloop: for (FourTuple fourTuple : this.keySet)
        {

            int sequenceNumber = 0;
            int count = 0;

            for (Packet packet : this.categorizedPackets.get(fourTuple))
            {
                result = result && sequenceNumber == packet.get(TcpPacket.class).getHeader().getSequenceNumber();

                if (!result)
                {
                    log.error(SQN_NUMBER_ERROR_TUPLE, fourTuple.toString());
                    break outerloop;
                }

                log.debug(SQN_NUMBER_INFO_TUPLE, count, fourTuple.toString(), sequenceNumber);

                sequenceNumber += packet.get(TcpPacket.class).getPayload().getRawData().length;
                count++;
            }
        }

        return result;
    }

    /**
     * Validates whether all request packets that have a particular tuple contain
     * the specified URI. The URIs are incremented for packets belonging to the same
     * connection.
     * 
     * @param baseUri
     * @param uris
     * @return
     */
    public boolean validateUris(String baseUri,
                                final List<String> uris)
    {
        List<Boolean> result = new ArrayList<>();

        final List<String> uriHexStrings = uris.stream().map(uri -> ByteArrays.toHexString(uri.getBytes(), " ")).collect(Collectors.toList());

        outerloop: for (FourTuple fourTuple : this.keySet)
        {
            var count = 0;

            for (Packet packet : this.categorizedPackets.get(fourTuple)) // Check base URI first
            {
                final var payLoad = ByteArrays.toHexString(packet.get(TcpPacket.class).getPayload().getRawData(), " ");

                if (payLoad.contains(ByteArrays.toHexString(baseUri.getBytes(), " ")))
                {
                    final var packetResult = payLoad.contains(uriHexStrings.get(count));

                    if (packetResult)
                    {
                        result.add(count, packetResult);
                        count++;
                    }
                    else
                    {
                        log.error(ERROR_URI);
                        break outerloop;
                    }
                }
            }
        }

        return !result.contains(false);
    }

    /**
     * Extracts a four tuple from a packet.
     * 
     * @param packet The packet.
     * @return A four tuple object.
     */
    private FourTuple getTuple(Packet packet)
    {
        if (ipv6Enabled)
        {
            final var ipHeader = packet.get(IpV6Packet.class).getHeader();
            final var tcpHeader = packet.get(TcpPacket.class).getHeader();

            return new FourTuple.Builder().withSrcIpAddress(ipHeader.getSrcAddr().toString().replace("/", ""))
                                          .withSrcPort(tcpHeader.getSrcPort().valueAsInt())
                                          .withDstIpAddress(ipHeader.getDstAddr().toString().replace("/", ""))
                                          .withDstPort(tcpHeader.getDstPort().valueAsInt())
                                          .build();
        }
        else
        {
            final var ipHeader = packet.get(IpV4Packet.class).getHeader();
            final var tcpHeader = packet.get(TcpPacket.class).getHeader();

            return new FourTuple.Builder().withSrcIpAddress(ipHeader.getSrcAddr().toString().replace("/", ""))
                                          .withSrcPort(tcpHeader.getSrcPort().valueAsInt())
                                          .withDstIpAddress(ipHeader.getDstAddr().toString().replace("/", ""))
                                          .withDstPort(tcpHeader.getDstPort().valueAsInt())
                                          .build();
        }
    }

}