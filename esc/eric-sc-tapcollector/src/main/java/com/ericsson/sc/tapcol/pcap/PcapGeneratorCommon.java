package com.ericsson.sc.tapcol.pcap;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.utils.pcap4j.PacketFactory;

import io.envoyproxy.envoy.data.tap.v3.Body;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent;

// TODO: cont. refactoring towards TapStream
public class PcapGeneratorCommon
{
    private static final Logger log = LoggerFactory.getLogger(PcapGeneratorCommon.class);

    public static void dumpSocketEvent(PcapDumper pcapDumper,
                                       SocketEvent socketEvent,
                                       Socket localAdr,
                                       Socket remoteAdr)
    {
        MacAddress srcMac = null;
        MacAddress dstMac = null;
        Socket srcAdr = null;
        Socket dstAdr = null;
        Body body = null;

        if (pcapDumper == null)
            return; // silently ignore this case (failed PCAP dump creation reported outside)

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
                    body = read.getData();
            }
            else if (socketEvent.hasWrite())
            {
                srcMac = PacketFactory.dummyLocalMac;
                dstMac = PacketFactory.dummyRemoteMac;
                srcAdr = localAdr;
                dstAdr = remoteAdr;
                var write = socketEvent.getWrite();

                if (write.hasData())
                    body = write.getData();
            }
            else
            {
                log.error("Received read/write event with empty body:\n>>>{}<<<", socketEvent);
            }
        }
        else if (socketEvent.hasClosed())
        {
            log.debug("--> CLOSED");
        }
        else
        {
            log.error("ERROR: Only read/write/close events implemented.");
        }

        if (body != null)
        {

            PacketFactory.createTcpPacket(srcMac, dstMac, srcAdr.ip, srcAdr.port.value(), dstAdr.ip, dstAdr.port.value(), 0, body.getAsBytes().toByteArray())
                         .forEach(packet ->
                         {
                             try
                             {
                                 pcapDumper.dump(packet, getTimeStampFor(socketEvent));
                             }
                             catch (NotOpenException e)
                             {
                                 log.error("ERROR: writing to PCAP output.", e);
                             }
                         });

        }
    }

    private static final java.sql.Timestamp NIL_TIMESTAMP = new java.sql.Timestamp(0);

    private static java.sql.Timestamp getTimeStampFor(SocketEvent socketEvent)
    {
        if (socketEvent.hasTimestamp())
        {
            var timestamp = socketEvent.getTimestamp();
            var timestampSQL = new java.sql.Timestamp(timestamp.getSeconds() * 1000);
            timestampSQL.setNanos(timestamp.getNanos());

            return timestampSQL;
        }
        else
            return NIL_TIMESTAMP;
    }
}
