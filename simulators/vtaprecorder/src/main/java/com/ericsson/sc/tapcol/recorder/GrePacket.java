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
 * Created on: Jul 10, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol.recorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

/**
 * Represents a PVTB GRE packet. Warning: Users are responsible for manually
 * releasing the payload buffer, otherwise memory leak shall occur
 */
public class GrePacket
{
    private static final int PAYLOAD_CONTENT_TYPE_MASK = 0B1111111; // last 7 bits
    private static final int UTF_ID_MASK = ~PAYLOAD_CONTENT_TYPE_MASK; // first 25 bits

    public enum PayloadType
    {
        PCAPNG_HEADERS,
        JSON,
        PCAPNG_DATA,
        UNKNOWN
    }

    public enum Flags
    {
        CHECKSUM,
        ROUTING,
        KEY,
        SEQUENCE_NUMBER;

        private int shift;

        private Flags()
        {
            this.shift = 15 - this.ordinal();
        }

        boolean matches(short flagsAndVersion)
        {
            return (((flagsAndVersion >>> shift) & 0x1) == 1);
        }

        static EnumSet<Flags> parseFlagsAndVersion(short flagsAndVersion)
        {
            EnumSet<Flags> result = EnumSet.noneOf(Flags.class);
            for (final var flag : Flags.values())
            {
                if (flag.matches(flagsAndVersion))
                    result.add(flag);
            }
            return result;
        }
    }

    private final short flagsAndVersion;
    private final short protocolVersion;
    private final Short checksum; // Not supported
    private final Short reserved; // Not supported
    private final int key;
    private final int sequenceNumber;
    private final ByteBuf payload;
    // parsed data
    private final int uniqueTrafficId;
    private final PayloadType payloadType;
    private final EnumSet<Flags> flags;

    /**
     * Parse UDP payload and construct a GrePacket.
     * 
     * @param udpPayload Buffer containing the payload of a GRE-in-UDP packet
     * 
     */
    public GrePacket(ByteBuf udpPayload)
    {
        final var buf = udpPayload.slice();
        this.flagsAndVersion = buf.readShort();
        this.flags = Flags.parseFlagsAndVersion(flagsAndVersion);
        if (flags.contains(Flags.ROUTING))
            throw new IllegalArgumentException("GRE packet should not have routing bit on"); // unsupported
        if (!flags.contains(Flags.KEY))
            throw new IllegalArgumentException("GRE key bit is mandatory");
        if (!flags.contains(Flags.SEQUENCE_NUMBER))
            throw new IllegalArgumentException("GRE sequence number bit is mandatory");
        this.protocolVersion = buf.readShort();
        this.checksum = flags.contains(Flags.CHECKSUM) ? buf.readShort() : null;
        this.reserved = flags.contains(Flags.CHECKSUM) ? buf.readShort() : null;
        this.key = buf.readInt();
        this.sequenceNumber = buf.readInt();

        this.uniqueTrafficId = key & UTF_ID_MASK;
        final var pcType = key & PAYLOAD_CONTENT_TYPE_MASK;
        switch (pcType)
        {
            case 0:
                payloadType = PayloadType.JSON;
                break;
            case 0x2:
                payloadType = PayloadType.PCAPNG_HEADERS;
                break;
            case 0x11:
            case 0x0F:
                payloadType = PayloadType.PCAPNG_DATA;
                break;
            default:
                payloadType = PayloadType.UNKNOWN;
        }

        this.payload = buf.slice();
        // Note that this might cause a memory leak. Payload should be released
        this.payload.retain();
    }

    /**
     * Parse UDP payload and construct a GrePacket
     * 
     * @param udp
     * @return
     */
    public static GrePacket fromUdp(DatagramPacket udp)
    {
        return new GrePacket(udp.content());
    }

    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    /**
     * Get payload. Warning: Users are responsible for releasing the returned
     * buffer, otherwise memory leak will occur
     * 
     * @return The GRE payload
     */
    public ByteBuf getPayload()
    {
        return payload;
    }

    public int getUniqueTrafficId()
    {
        return uniqueTrafficId;
    }

    public PayloadType getPayloadType()
    {
        return payloadType;
    }

    @Override
    public String toString()
    {
        return String.format("GrePacket [flagsAndVersion=0x%x(%s), protocolVersion=0x%x, checksum=0x%x, reserved=0x%x, key=0x%x, sequenceNumber=0x%x, uniqueTrafficId=0x%x, payloadType=%s]",
                             flagsAndVersion,
                             flags,
                             protocolVersion,
                             checksum,
                             reserved,
                             key,
                             sequenceNumber,
                             uniqueTrafficId,
                             payloadType);
    }

    /**
     * Parse GRE packet from binary file
     * 
     * @param file Binary file to get the data from
     * @return The parsed GRE packet
     */
    public static GrePacket fromFile(File file)
    {
        try (final var aFile = new RandomAccessFile(file, "r"))
        {
            final var inChannel = aFile.getChannel();
            final var fileSize = inChannel.size();

            // Create buffer of the file size
            final var buffer = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buffer);
            buffer.flip();
            final var wrappedBuffer = Unpooled.wrappedBuffer(buffer);
            try
            {
                return new GrePacket(wrappedBuffer);
            }
            finally
            {
                wrappedBuffer.release();
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
