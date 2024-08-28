package com.ericsson.sc.tapcol.pcap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.namednumber.DataLinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.UnknownFieldSet;

import io.envoyproxy.envoy.data.tap.v3.SocketEvent;
import io.envoyproxy.envoy.data.tap.v3.TraceWrapper;
import io.envoyproxy.envoy.service.tap.v3.StreamTapsRequest;

/**
 * PCAP generator for SocketBufferedTrace
 */
public class PcapGeneratorSBT
{
    private static final Logger log = LoggerFactory.getLogger(PcapGeneratorSBT.class);

    private static String indent(int indentation)
    {
        var stringWriter = new StringWriter();

        for (var i = 0; i < indentation; i++)
            stringWriter.append(' ');

        return stringWriter.toString();
    }

    private static void writeUnknownFields(UnknownFieldSet unknownFields,
                                           int indentBy)
    {
        final var allUnknownFieldsStr = indent(indentBy) + "ALL UNKNOWN FIELDS: >>> \"" + unknownFields + "\" <<<";
        log.debug(allUnknownFieldsStr);

        final var unknownFieldsStr = indent(indentBy) + "UNKNOWN FIELDS:";
        log.debug(unknownFieldsStr);

        var curField = 1;
        while (unknownFields.hasField(curField))
        {
            final var outputStr = indent(indentBy + 2) + curField + ": >>>\"" + unknownFields.getField(curField) + "\" <<<";
            log.debug(outputStr);
            curField++;
        }

    }

    public static boolean didWriteTracWrapperToPcapOutput(PcapDumper pcapDumper,
                                                          TraceWrapper traceWrapper,
                                                          int indentBy)
    {
        var didWrite = false;
        Socket localAdr = null;
        Socket remoteAdr = null;

        final var traceWrapperOutputStr = indent(indentBy) + "TRACE WRAPPER";
        log.debug(traceWrapperOutputStr);

        if (traceWrapper.hasSocketBufferedTrace())
        {
            final var socketBufferedTrace = traceWrapper.getSocketBufferedTrace();

            final var socketBufferedTraceOutputStr = indent(indentBy + 2) + "SOCKET BUFFERED TRACE";
            log.debug(socketBufferedTraceOutputStr);

            // Socket connection
            if (socketBufferedTrace.hasConnection())
            {
                final var connectionOutputStr = indent(indentBy + 4) + "CONNECTION";
                log.debug(connectionOutputStr);

                var connection = socketBufferedTrace.getConnection();

                if (connection.hasLocalAddress())
                {
                    localAdr = Socket.getSocketByAddress(connection.getLocalAddress());

                    final var localAddressOutputStr = indent(indentBy + 6) + connection.getLocalAddress() + " = " + localAdr;
                    log.debug(localAddressOutputStr);
                }

                if (connection.hasRemoteAddress())
                {
                    remoteAdr = Socket.getSocketByAddress(connection.getRemoteAddress());

                    final var remoteAddressOutputStr = indent(indentBy + 6) + connection.getRemoteAddress() + " = " + remoteAdr;
                    log.debug(remoteAddressOutputStr);
                }
            }
            else
            {
                final var noConnectionFoundOutputStr = indent(indentBy + 4) + "NO CONNECTION FOUND";
                log.debug(noConnectionFoundOutputStr);
            }
            // Socket events
            final var eventsOutputStr = indent(indentBy + 4) + "EVENTS:";
            log.debug(eventsOutputStr);
            for (SocketEvent socketEvent : socketBufferedTrace.getEventsList())
                PcapGeneratorCommon.dumpSocketEvent(pcapDumper, socketEvent, localAdr, remoteAdr);

            writeUnknownFields(socketBufferedTrace.getUnknownFields(), indentBy + 6);

            final var readyOutputStr = indent(indentBy + 4) + "READY.";
            log.debug(readyOutputStr);
        }
        else
        {
            log.error("ERROR: Not a buffered socket trace.");
        }

        didWrite = true;
        return didWrite;
    }

    private static boolean didWriteTapRequestToPcapOutput(PcapDumper pcapDumper,
                                                          StreamTapsRequest tapReq)
    {
        var indentBy = 0;
        var didWrite = false;

        final var tapRequestOutputStr = indent(indentBy) + "TAP REQUEST [" + tapReq.getClass() + "] (Trace ID " + tapReq.getTraceId() + ")";
        log.debug(tapRequestOutputStr);

        if (tapReq.hasTrace())
        {
            final var traceWrapper = tapReq.getTrace();
            didWrite = didWriteTracWrapperToPcapOutput(pcapDumper, traceWrapper, indentBy + 2);
        }
        else
        {
            log.error("ERROR: No trace wrapper found.");
        }

        return didWrite;
    }

    // Snapshot length, which is the number of bytes captured for each packet.
    private static final int SNAP_LEN = 1 << 14; // 16 kBytes

    public static boolean didConvertEnvoyTapFile(String filename)
    {
        var tapFileCreated = false;
        final String pcapFilename = filename + ".pcap";

        try (var input = new FileInputStream(filename))
        {
            tapFileCreated = didCreatePcapFileFrom(input, filename, pcapFilename);
        }
        catch (FileNotFoundException e)
        {
            log.error("ERROR: Cannot open " + filename, e);
        }
        catch (IOException e)
        {
            log.error("ERROR: I/O error occurred while processing " + filename, e);
        }

        return tapFileCreated;
    }

    public static boolean didCreatePcapFileFrom(TraceWrapper traceWrapper,
                                                String pcapFilename)
    {
        var pcapCreated = false;
        PcapDumper pcapDumper = null;

        try (var pcapOut = Pcaps.openDead(DataLinkType.EN10MB, SNAP_LEN, PcapHandle.TimestampPrecision.NANO))
        {
            // Open PCAP output
            pcapDumper = pcapOut.dumpOpen(pcapFilename);

            if (pcapDumper != null && didWriteTracWrapperToPcapOutput(pcapDumper, traceWrapper, 0))
            {
                log.info("CREATED {}", pcapFilename);
                pcapDumper.close();
                pcapDumper = null;

                pcapCreated = true;
            }
        }
        catch (PcapNativeException | NotOpenException e)
        {
            log.error("ERROR: While outputting " + pcapFilename, e);
        }
        finally
        {
            if (pcapDumper != null)
                pcapDumper.close();
        }

        return pcapCreated;
    }

    private static boolean didCreatePcapFileFrom(InputStream input,
                                                 String inputID,
                                                 String pcapFilename)
    {
        var pcapCreated = false;

        try
        {
            var traceWrapper = TraceWrapper.parseFrom(input);

            if (traceWrapper != null)
            {
                pcapCreated = didCreatePcapFileFrom(traceWrapper, pcapFilename);
            }
            else
            {
                final var errorMsg = "Could not parse tap request from " + inputID;
                log.error(errorMsg);
            }

            input.close();
        }
        catch (IOException e)
        {
            log.error("ERROR: I/O error occurred while processing " + inputID, e);
            System.exit(1);
        }

        return pcapCreated;
    }
}
