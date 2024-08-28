package com.ericsson.sc.tapcol.pcap;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.Arrays;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.tapcol.TapCol.DivisionMethod;
import com.ericsson.sc.utils.pcap4j.PacketFactory;

import io.envoyproxy.envoy.data.tap.v3.Body;
import io.envoyproxy.envoy.data.tap.v3.Connection;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent;

public class TapStream
{
    private static final Logger log = LoggerFactory.getLogger(TapStream.class);
    private static final int RAW_PACKET_LENGTH_LIMIT = 64 * 1024 * 1024;
    private Socket localAdr = null;
    private Socket remoteAdr = null;
    private final Long traceId;
    private final AtomicInteger tcpSeqNoRead = new AtomicInteger();
    private final AtomicInteger tcpSeqNoWrite = new AtomicInteger();
    private static final String ENV_MODE_DATA = "MODE_TAPPED_DATA";
    private static final String ENV_SIZE_DATA = "SIZE_TAPPED_DATA";
    private static final String OUT_TAP_FRAMES_TRUNCATE_TOTAL = "tapcollector_out_tap_frames_truncated_total";
    private static final String OUT_TAP_FRAMES_SPLIT_TOTAL = "tapcollector_out_tap_frame_splits_total";

    TapStream(Long traceId)
    {
        this.traceId = traceId;
        this.localAdr = Socket.NIL;
        this.remoteAdr = Socket.NIL;
    }

    TapStream(Long traceId,
              Connection newConnection)
    {
        this.traceId = traceId;
        if (newConnection.hasLocalAddress())
        {
            this.localAdr = Socket.getSocketByAddress(newConnection.getLocalAddress());
        }
        else
        {
            log.error("Received connection lacking local address information\n>>>{}<<<", newConnection);
        }

        if (newConnection.hasRemoteAddress())
        {
            this.remoteAdr = Socket.getSocketByAddress(newConnection.getRemoteAddress());
        }
        else
        {
            log.error("Received connection lacking remote address information\n>>>{}<<<", newConnection);
        }

        if (this.localAdr == null)
        {
            if (this.remoteAdr != null && this.remoteAdr.ip != null && this.remoteAdr.ip.toString().contains(":"))
                this.localAdr = Socket.NIL6;
            else
                this.localAdr = Socket.NIL;
        }
        if (this.remoteAdr == null)
        {
            if (this.localAdr != null && this.localAdr.ip != null && this.localAdr.ip.toString().contains(":"))
                this.remoteAdr = Socket.NIL6;
            else
                this.remoteAdr = Socket.NIL;
        }
    }

    private List<TapPacket> truncateDivisionMethod(byte[] rawPacketData,
                                                   MacAddress srcMac,
                                                   MacAddress dstMac,
                                                   Socket srcAdr,
                                                   Socket dstAdr,
                                                   Integer chunkSize,
                                                   SocketEvent socketEvent)
    {

        log.debug("Truncate packet for source (Socket Addr): {}, and destination (Socket Addr): {} with chunkSize: {} and SocketEvent: {}",
                  srcAdr,
                  dstAdr,
                  chunkSize,
                  socketEvent);

        int seqNo;
        Packet newPacket = null;
        List<TapPacket> tapPackets = new ArrayList<>();
        var currentChunkSize = Math.min(chunkSize, rawPacketData.length);
        seqNo = socketEvent.hasRead() ? this.tcpSeqNoRead.getAndAdd(currentChunkSize) : this.tcpSeqNoWrite.getAndAdd(currentChunkSize);
        newPacket = PacketFactory.createTcpPacket(srcMac,
                                                  dstMac,
                                                  srcAdr.ip,
                                                  srcAdr.port.value(),
                                                  dstAdr.ip,
                                                  dstAdr.port.value(),
                                                  seqNo,
                                                  Arrays.copyOfRange(rawPacketData, 0, currentChunkSize));
        log.debug("tcpSeqNoRead={}, tcpSeqNoWrite={}, rawPacketData.length={}", tcpSeqNoRead, tcpSeqNoWrite, currentChunkSize);
        if (chunkSize < rawPacketData.length)
        {
            PmMetrics metrics = PmMetrics.factory();
            metrics.addAccept(OUT_TAP_FRAMES_TRUNCATE_TOTAL);
            metrics.createCounter(OUT_TAP_FRAMES_TRUNCATE_TOTAL, OUT_TAP_FRAMES_TRUNCATE_TOTAL);
            log.debug("Increase truncate counter");
            safeIncreaseCounter(OUT_TAP_FRAMES_TRUNCATE_TOTAL);
        }
        tapPackets.add(new TapPacket(newPacket, getTimeStampFor(socketEvent)));
        return tapPackets;
    }

    private List<TapPacket> splitDivisionMethod(byte[] rawPacketData,
                                                MacAddress srcMac,
                                                MacAddress dstMac,
                                                Socket srcAdr,
                                                Socket dstAdr,
                                                Integer chunkSize,
                                                SocketEvent socketEvent)
    {

        log.debug("Split packet for source (Socket Addr): {}, and destination (Socket Addr): {} with chunkSize: {} and SocketEvent: {}",
                  srcAdr,
                  dstAdr,
                  chunkSize,
                  socketEvent);
        int totalSize = 0;
        int seqNo;
        Packet newPacket = null;
        List<TapPacket> tapPackets = new ArrayList<>();
        if (chunkSize < rawPacketData.length)
        {
            PmMetrics metrics = PmMetrics.factory();
            metrics.addAccept(OUT_TAP_FRAMES_SPLIT_TOTAL);
            metrics.createCounter(OUT_TAP_FRAMES_SPLIT_TOTAL, OUT_TAP_FRAMES_SPLIT_TOTAL);
        }
        while (totalSize < rawPacketData.length)
        {
            var currentChunkSize = Math.min(chunkSize, rawPacketData.length - totalSize);
            seqNo = socketEvent.hasRead() ? this.tcpSeqNoRead.getAndAdd(currentChunkSize) : this.tcpSeqNoWrite.getAndAdd(currentChunkSize);

            newPacket = PacketFactory.createTcpPacket(srcMac,
                                                      dstMac,
                                                      srcAdr.ip,
                                                      srcAdr.port.value(),
                                                      dstAdr.ip,
                                                      dstAdr.port.value(),
                                                      seqNo,
                                                      Arrays.copyOfRange(rawPacketData, totalSize, totalSize + currentChunkSize));
            log.debug("tcpSeqNoRead={}, tcpSeqNoWrite={}, rawPacketData.length={}", tcpSeqNoRead, tcpSeqNoWrite, currentChunkSize);
            tapPackets.add(new TapPacket(newPacket, getTimeStampFor(socketEvent)));
            totalSize += currentChunkSize;

            if (totalSize < rawPacketData.length)
            {
                log.debug("Increase split counter");
                safeIncreaseCounter(OUT_TAP_FRAMES_SPLIT_TOTAL);
            }
        }

        return tapPackets;
    }

    public List<TapPacket> synthesizePacket(SocketEvent socketEvent,
                                            DivisionMethod divisionMethod,
                                            Integer chunkSize)
    {
        MacAddress srcMac = null;
        MacAddress dstMac = null;
        Socket srcAdr = null;
        Socket dstAdr = null;
        Body body = null;

        if (socketEvent.hasRead() || socketEvent.hasWrite())
        {
            if (socketEvent.hasRead())
            {
                srcMac = PacketFactory.dummyRemoteMac;
                dstMac = PacketFactory.dummyLocalMac;
                srcAdr = remoteAdr;
                dstAdr = localAdr;
                var read = socketEvent.getRead();

                if (read.hasData())
                {
                    body = read.getData();
                }
            }
            else if (socketEvent.hasWrite())
            {
                srcMac = PacketFactory.dummyLocalMac;
                dstMac = PacketFactory.dummyRemoteMac;
                srcAdr = localAdr;
                dstAdr = remoteAdr;
                var write = socketEvent.getWrite();

                if (write.hasData())
                {
                    body = write.getData();
                }
            }
            else
            {
                log.error("Received read/write event with empty body:\n>>>{}<<<", socketEvent);
            }
        }
        else if (socketEvent.hasClosed())
        {
            // Nothing to do
        }
        else
        {
            log.error("Unknown socket event:\n>>>{}<<<", socketEvent);
        }

        if (body != null)
        {
            final var rawPacketData = body.getAsBytes().toByteArray();
            if (rawPacketData.length > RAW_PACKET_LENGTH_LIMIT)
            {
                log.warn("Packet size exceeds limit");
            }

            if (divisionMethod == DivisionMethod.SPLIT)
            {
                return splitDivisionMethod(rawPacketData, srcMac, dstMac, srcAdr, dstAdr, chunkSize, socketEvent);
            }
            else if (divisionMethod == DivisionMethod.TRUNCATE)
            {
                return truncateDivisionMethod(rawPacketData, srcMac, dstMac, srcAdr, dstAdr, chunkSize, socketEvent);
            }
            else
            {
                log.error("Unknown division method:\n>>>{}<<<", divisionMethod);
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    // TODO re-introduce when factored out: private final java.sql.Timestamp
    // NIL_TIMESTAMP = new java.sql.Timestamp(0);

    private static Timestamp getTimeStampFor(SocketEvent socketEvent)
    {
        if (socketEvent.hasTimestamp())
        {
            final var ts = socketEvent.getTimestamp();
            final var timestamp = new Timestamp(ts.getSeconds() * 1000);
            timestamp.setNanos(ts.getNanos());

            return timestamp;
        }
        else
        {
            return new Timestamp(0); // TODO should rather be a constant: NIL_TIMESTAMP;
        }
    }

    private void safeIncreaseCounter(String mapEntryName)
    {
        PmMetrics metrics = PmMetrics.factory();
        try
        {
            metrics.increaseCounter(mapEntryName);
        }
        catch (Exception e)
        {
            log.debug("Failed to increase counter {}", mapEntryName, e);
        }
    }
}
