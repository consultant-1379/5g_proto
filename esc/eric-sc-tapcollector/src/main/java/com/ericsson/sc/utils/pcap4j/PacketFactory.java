package com.ericsson.sc.utils.pcap4j;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Random;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Rfc1349Tos;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.util.MacAddress;

public class PacketFactory
{
    static
    {
        System.setProperty("org.pcap4j.packet.tcpV4.calcChecksumAtBuild", "true");
        System.setProperty("org.pcap4j.packet.ipV4.calcChecksumAtBuild,", "true");
    }
    static final byte TTL = 64;
    static final short WINDOW = (short) (64 * 1024 - 1);

    public static Packet buildIpV4(MacAddress srcAddressMac,
                                   MacAddress dstAddrressMac,
                                   IpV4Packet.Builder builderIpv4)
    {

        Packet p = null;

        var etherBuilder = new EthernetPacket.Builder();
        etherBuilder.dstAddr(dstAddrressMac).srcAddr(srcAddressMac).type(EtherType.IPV4).payloadBuilder(builderIpv4).paddingAtBuild(true);

        p = etherBuilder.build();

        return p;
    }

    public static Packet buildIpV6(MacAddress srcAddressMac,
                                   MacAddress dstAddrressMac,
                                   IpV6Packet.Builder builderIpv6)
    {

        Packet p = null;

        var etherBuilder = new EthernetPacket.Builder();
        etherBuilder.dstAddr(dstAddrressMac).srcAddr(srcAddressMac).type(EtherType.IPV6).payloadBuilder(builderIpv6).paddingAtBuild(true);

        p = etherBuilder.build();

        return p;
    }

    public static Packet createTcpPacket(MacAddress dstAddrressMac,
                                         MacAddress srcAddressMac,
                                         InetAddress srcAddress,
                                         short srcPort,
                                         InetAddress dstAddress,
                                         short dstPort,
                                         int sequence,
                                         byte[] data,
                                         short ident)
    {
        Packet p = null;

        var tcpPktBuilder = new TcpPacket.Builder();
        tcpPktBuilder.payloadBuilder(new UnknownPacket.Builder().rawData(data));
        tcpPktBuilder.correctChecksumAtBuild(true)
                     .correctLengthAtBuild(true)
                     .paddingAtBuild(true)
                     .srcAddr(srcAddress)
                     .srcPort(new TcpPort(srcPort, ""))
                     .dstAddr(dstAddress)
                     .dstPort(new TcpPort(dstPort, ""))
                     .sequenceNumber(sequence)
                     .window(WINDOW);

        if (srcAddress instanceof Inet4Address && dstAddress instanceof Inet4Address)
        {
            var builderIpv4 = new IpV4Packet.Builder();
            builderIpv4.correctChecksumAtBuild(true);
            builderIpv4.correctLengthAtBuild(true);
            builderIpv4.paddingAtBuild(true);
            builderIpv4.dstAddr((Inet4Address) dstAddress);
            builderIpv4.identification(ident);
            builderIpv4.ihl((byte) 5);

            builderIpv4.protocol(IpNumber.TCP);
            builderIpv4.srcAddr((Inet4Address) srcAddress);
            builderIpv4.tos(IpV4Rfc1349Tos.newInstance((byte) 0));
            builderIpv4.ttl(TTL);
            builderIpv4.version(IpVersion.IPV4);
            builderIpv4.payloadBuilder(tcpPktBuilder);

            p = buildIpV4(srcAddressMac, dstAddrressMac, builderIpv4);
        }

        if (srcAddress instanceof Inet6Address && dstAddress instanceof Inet6Address)
        {
            var builderIpv6 = new IpV6Packet.Builder();
            builderIpv6.correctLengthAtBuild(true);
            builderIpv6.dstAddr((Inet6Address) dstAddress);

            builderIpv6.srcAddr((Inet6Address) srcAddress);
            builderIpv6.version(IpVersion.IPV6);

            builderIpv6.flowLabel((IpV6Packet.IpV6FlowLabel) () -> 0);
            builderIpv6.nextHeader(IpNumber.TCP);
            builderIpv6.trafficClass((IpV6Packet.IpV6TrafficClass) () -> (byte) 0);

            builderIpv6.payloadBuilder(tcpPktBuilder);

            p = buildIpV6(srcAddressMac, dstAddrressMac, builderIpv6);
        }

        return p;
    }

    static final Random random = new Random();
    static final byte[] dummyRemoteMacArr = { (byte) 0x02, (byte) 0x42, (byte) 0x5B, (byte) 0x55, (byte) 0xED, (byte) 0x00 };
    static final byte[] dummyLocalMacArr = { (byte) 0x02, (byte) 0x42, (byte) 0x5B, (byte) 0x55, (byte) 0xED, (byte) 0xFF };
    public static final MacAddress dummyRemoteMac = MacAddress.getByAddress(dummyRemoteMacArr);
    public static final MacAddress dummyLocalMac = MacAddress.getByAddress(dummyLocalMacArr);

    public static Packet createTcpPacket(MacAddress srcMac,
                                         MacAddress dstMac,
                                         InetAddress srcAddress,
                                         short srcPort,
                                         InetAddress dstAddress,
                                         short dstPort,
                                         int sequenceNumber,
                                         byte[] data)
    {
        return createTcpPacket(srcMac,
                               dstMac,
                               srcAddress,
                               srcPort,
                               dstAddress,
                               dstPort,
                               sequenceNumber,
                               data,
                               (short) Math.abs(random.nextInt(Short.MAX_VALUE)));
    }

    private PacketFactory()
    {

    }
}
