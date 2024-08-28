package com.ericsson.sc.utils.pcapng;

import java.sql.Timestamp;

import org.pcap4j.packet.Packet;

import com.ericsson.sc.utils.ConvUtils;

import io.netty.buffer.ByteBuf;

/**
 * Helper functions to generate PCAPNG format for given pcap4j Packets (e.g. an
 * Ethernet packet)
 *
 * Supported PCAPNG blocks to support Ericsson's ProbeBroker, Probe Virtual Tap
 * Broker (PVTB):
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | SHB | IDB | EPB | EPB | ... |
 * EPB | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 *
 * For a speedy processing all constants are brought into NW byte order before
 * start
 */
public class PcapNgBuilder
{
    //
    /**
     *
     * Section Header Block (SHB)
     *
     * 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 0 | Block
     * Type = 0x0A0D0D0A |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 4 | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 8 |
     * Byte-Order Magic |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 12 | Major
     * Version | Minor Version |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 16 | | |
     * Section Length | | |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 24 / / /
     * Options (variable) / / /
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     */
    // block type Section Header Block (SHB)
    private static final byte[] SHB = ConvUtils.toByteArrayLe(0x0A0D0D0A);

    // SHB block total length (no options)
    private static final byte[] shbBlockTotalLengthNoOpt = ConvUtils.toByteArrayLe(28);

    // SHB byte order magic
    private static final byte[] shbByteOrderMagic = ConvUtils.toByteArrayLe(0x1A2B3C4D);

    // SHB PCAP version
    private static final byte[] shbMajorVersion = ConvUtils.toByteArrayLe((short) 0x0001);
    private static final byte[] shbMinorVersion = ConvUtils.toByteArrayLe((short) 0x0000);

    // SHB Section length (unspecified)
    private static final byte[] shbSectionLengthUnspec = ConvUtils.toByteArrayLe((long) -1);

    public static void writeSHB(ByteBuf out)
    {
        out.writeBytes(SHB);
        out.writeBytes(shbBlockTotalLengthNoOpt);
        out.writeBytes(shbByteOrderMagic);
        out.writeBytes(shbMajorVersion);
        out.writeBytes(shbMinorVersion);
        out.writeBytes(shbSectionLengthUnspec);
        out.writeBytes(shbBlockTotalLengthNoOpt);
    }

    /**
     *
     * Interface Description Block (IDB)
     *
     * 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 0 | Block
     * Type = 0x00000001 |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 4 | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 8 |
     * LinkType | Reserved |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 12 |
     * SnapLen | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * 16 / / / Options (variable) / / /
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    // block type Interface Description Block (IDB)
    private static final byte[] IDB = ConvUtils.toByteArrayLe(0x00000001);

    // IDB block total length (empty i/f description = no options)
    private static final byte[] idbBlockTotalLengthNoOpt = ConvUtils.toByteArrayLe(32);

    // IDB LinkType (Ethernet)
    private static final byte[] LINKTYPE_ETHERNET = ConvUtils.toByteArrayLe((short) 1);

    // IDB 16 bits for reserved field set to zero
    private static final byte[] zero16 = ConvUtils.toByteArrayLe((short) 0);

    // IDB 32 bits for SnapLen field set to zero
    private static final byte[] zero32 = ConvUtils.toByteArrayLe(0);

    // IDB 16 bits for "if_tsresol" option code
    private static final byte[] idbIfTsresol = ConvUtils.toByteArrayLe((short) 9);

    // IDB 16 bits for "if_tsresol" option length
    private static final byte[] idbIfTsresolLen = ConvUtils.toByteArrayLe((short) 1);

    // IDB 32 bits for "if_tsresol" value
    private static final byte[] idbIfTsresolVal = ConvUtils.toByteArrayLe(9);

    // IDB 32 bits for "opt_endofopt" option code (16 bits) and option length (16
    // bits)
    private static final byte[] idbEndOfOptAndLen = ConvUtils.toByteArrayLe(0);

    public static void writeIDB(ByteBuf out)
    {
        out.writeBytes(IDB);
        out.writeBytes(idbBlockTotalLengthNoOpt);
        out.writeBytes(LINKTYPE_ETHERNET);
        out.writeBytes(zero16); // Reserved
        out.writeBytes(zero32); // SnapLen
        out.writeBytes(idbIfTsresol);
        out.writeBytes(idbIfTsresolLen);
        out.writeBytes(idbIfTsresolVal);
        out.writeBytes(idbEndOfOptAndLen);
        out.writeBytes(idbBlockTotalLengthNoOpt);
    }

    /**
     *
     * Enhanced Packet Block (EPB)
     *
     * 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 0 | Block
     * Type = 0x00000006 |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 4 | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 8 |
     * Interface ID |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 12 |
     * Timestamp (High) |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 16 |
     * Timestamp (Low) |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 20 |
     * Captured Packet Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 24 |
     * Original Packet Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 28 / / /
     * Packet Data / / variable length, padded to 32 bits / / /
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ / / /
     * Options (variable) / / /
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | Block
     * Total Length |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     */
    // block type Enhanced Packet Block (EPB)
    private static final byte[] EPB = ConvUtils.toByteArrayLe(0x00000006);

    // only one i/f per section: i/f ID = 0
    private static final byte[] epbInterfaceId0 = ConvUtils.toByteArrayLe(0);

    public static void writeEPB(ByteBuf out,
                                Packet packet,
                                Timestamp timestamp)
    {
        final long nanos = timestamp.getTime() * 1000l * 1000l + timestamp.getNanos();
        final int packetLength = packet.getRawData().length;
        final byte[] packetLengthArr = ConvUtils.toByteArrayLe(packetLength);
        final int packetLengthPadded = ConvUtils.padTwo32(packetLength);
        final byte[] blockTotalLength = ConvUtils.toByteArrayLe(packetLengthPadded + 32);

        out.writeBytes(EPB);
        out.writeBytes(blockTotalLength);
        out.writeBytes(epbInterfaceId0);
        out.writeBytes(ConvUtils.pcapNgTimestampLe(nanos)); // Timestamp, in pcapng encoding
        out.writeBytes(packetLengthArr);
        out.writeBytes(packetLengthArr); // = Original Packet Length (no SnapLen set in IDB)
        out.writeBytes(packet.getRawData());

        for (var i = 0; i < packetLengthPadded - packetLength; i++)
            out.writeByte((byte) 0);

        out.writeBytes(blockTotalLength);
    }

    private PcapNgBuilder()
    {
    }

}
