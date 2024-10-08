/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.pcap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;

/**
 * <p>
 * {@link PcapWriteHandler} captures {@link ByteBuf} from {@link SocketChannel}
 * / {@link ServerChannel} or {@link DatagramPacket} and writes it into Pcap
 * {@link OutputStream}.
 * </p>
 *
 * <p>
 * Things to keep in mind when using {@link PcapWriteHandler} with TCP:
 *
 * <ul>
 * <li>Whenever
 * {@link ChannelInboundHandlerAdapter#channelActive(ChannelHandlerContext)} is
 * called, a fake TCP 3-way handshake (SYN, SYN+ACK, ACK) is simulated as new
 * connection in Pcap.</li>
 *
 * <li>Whenever
 * {@link ChannelInboundHandlerAdapter#handlerRemoved(ChannelHandlerContext)} is
 * called, a fake TCP 3-way handshake (FIN+ACK, FIN+ACK, ACK) is simulated as
 * connection shutdown in Pcap.</li>
 *
 * <li>Whenever
 * {@link ChannelInboundHandlerAdapter#exceptionCaught(ChannelHandlerContext, Throwable)}
 * is called, a fake TCP RST is sent to simulate connection Reset in Pcap.</li>
 *
 * <li>ACK is sent each time data is send / received.</li>
 *
 * <li>Zero Length Data Packets can cause TCP Double ACK error in Wireshark. To
 * tackle this, set {@code captureZeroByte} to {@code false}.</li>
 * </ul>
 * </p>
 */
public final class PcapWriteHandler extends ChannelDuplexHandler implements Closeable
{

    /**
     * Logger for logging events
     */
    private final InternalLogger logger = InternalLoggerFactory.getInstance(PcapWriteHandler.class);

    /**
     * {@link PcapWriter} Instance
     */
    private PcapWriter pCapWriter;

    /**
     * {@link OutputStream} where we'll write Pcap data.
     */
    private final OutputStream outputStream;

    /**
     * {@code true} if we want to capture packets with zero bytes else
     * {@code false}.
     */
    private final boolean captureZeroByte;

    /**
     * {@code true} if we want to write Pcap Global Header on initialization of
     * {@link PcapWriter} else {@code false}.
     */
    private final boolean writePcapGlobalHeader;

    /**
     * TCP Sender Segment Number. It'll start with 1 and keep incrementing with
     * number of bytes read/sent.
     */
    private int sendSegmentNumber = 1;

    /**
     * TCP Receiver Segment Number. It'll start with 1 and keep incrementing with
     * number of bytes read/sent.
     */
    private int receiveSegmentNumber = 1;

    /**
     * Type of the channel this handler is registered on
     */
    private ChannelType channelType;

    /**
     * Address of the initiator of the connection
     */
    private InetSocketAddress initiatiorAddr;

    /**
     * Address of the receiver of the connection
     */
    private InetSocketAddress handlerAddr;

    private boolean isServerPipeline;

    /**
     * Set to {@code true} if {@link #close()} is called and we should stop writing
     * Pcap.
     */
    private boolean isClosed;

    /**
     * Whether this handler is initialized (headers written, channel type inferred)
     */
    private boolean initialized;

    private ByteBuf consolidatedTcpBuf;

    private int consolidatedBytes;

    /**
     * Create new {@link PcapWriteHandler} Instance. {@code captureZeroByte} is set
     * to {@code false} and {@code writePcapGlobalHeader} is set to {@code true}.
     *
     * @param outputStream OutputStream where Pcap data will be written. Call
     *                     {@link #close()} to close this OutputStream.
     * @throws NullPointerException If {@link OutputStream} is {@code null} then
     *                              we'll throw an {@link NullPointerException}
     * @deprecated Use {@link #builder() builder} instead.
     */
    @Deprecated
    public PcapWriteHandler(OutputStream outputStream)
    {
        this(outputStream, false, true);
    }

    /**
     * Create new {@link PcapWriteHandler} Instance
     *
     * @param outputStream          OutputStream where Pcap data will be written.
     *                              Call {@link #close()} to close this
     *                              OutputStream.
     * @param captureZeroByte       Set to {@code true} to enable capturing packets
     *                              with empty (0 bytes) payload. Otherwise, if set
     *                              to {@code false}, empty packets will be filtered
     *                              out.
     * @param writePcapGlobalHeader Set to {@code true} to write Pcap Global Header
     *                              on initialization. Otherwise, if set to
     *                              {@code false}, Pcap Global Header will not be
     *                              written on initialization. This could when
     *                              writing Pcap data on a existing file where Pcap
     *                              Global Header is already present.
     * @throws NullPointerException If {@link OutputStream} is {@code null} then
     *                              we'll throw an {@link NullPointerException}
     * @deprecated Use {@link #builder() builder} instead.
     */
    @Deprecated
    public PcapWriteHandler(OutputStream outputStream,
                            boolean captureZeroByte,
                            boolean writePcapGlobalHeader)
    {
        this.outputStream = ObjectUtil.checkNotNull(outputStream, "OutputStream");
        this.captureZeroByte = captureZeroByte;
        this.writePcapGlobalHeader = writePcapGlobalHeader;
    }

    private PcapWriteHandler(Builder builder,
                             OutputStream outputStream)
    {
        this.outputStream = outputStream;
        this.captureZeroByte = builder.captureZeroByte;
        this.writePcapGlobalHeader = builder.writePcapGlobalHeader;
        this.channelType = builder.channelType;
        this.handlerAddr = builder.handlerAddr;
        this.initiatiorAddr = builder.initiatiorAddr;
        this.isServerPipeline = builder.isServerPipeline;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    private void initializeIfNecessary(ChannelHandlerContext ctx)
    {
        if (initialized)
        {
            return;
        }

        ByteBufAllocator byteBufAllocator = ctx.alloc();
        this.consolidatedTcpBuf = byteBufAllocator.buffer();
        this.consolidatedBytes = 0;

        /*
         * If `writePcapGlobalHeader` is `true`, we'll write Pcap Global Header.
         */
        if (writePcapGlobalHeader)
        {

            ByteBuf byteBuf = byteBufAllocator.buffer();
            try
            {
                this.pCapWriter = new PcapWriter(this.outputStream, byteBuf);
            }
            catch (IOException ex)
            {
                ctx.channel().close();
                ctx.fireExceptionCaught(ex);
                logger.error("Caught Exception While Initializing PcapWriter, Closing Channel.", ex);
            }
            finally
            {
                byteBuf.release();
            }
        }
        else
        {
            this.pCapWriter = new PcapWriter(this.outputStream);
        }

        if (channelType == null)
        {
            // infer channel type
            if (ctx.channel() instanceof SocketChannel)
            {
                channelType = ChannelType.TCP;

                // If Channel belongs to `SocketChannel` then we're handling TCP.
                // Capture correct `localAddress` and `remoteAddress`
                if (ctx.channel().parent() instanceof ServerSocketChannel)
                {
                    isServerPipeline = true;
                    initiatiorAddr = (InetSocketAddress) ctx.channel().remoteAddress();
                    handlerAddr = (InetSocketAddress) ctx.channel().localAddress();
                }
                else
                {
                    isServerPipeline = false;
                    initiatiorAddr = (InetSocketAddress) ctx.channel().localAddress();
                    handlerAddr = (InetSocketAddress) ctx.channel().remoteAddress();
                }
            }
            else if (ctx.channel() instanceof DatagramChannel)
            {
                channelType = ChannelType.UDP;

                DatagramChannel datagramChannel = (DatagramChannel) ctx.channel();

                // If `DatagramChannel` is connected then we can get
                // `localAddress` and `remoteAddress` from Channel.
                if (datagramChannel.isConnected())
                {
                    initiatiorAddr = (InetSocketAddress) ctx.channel().localAddress();
                    handlerAddr = (InetSocketAddress) ctx.channel().remoteAddress();
                }
            }
        }

        if (channelType == ChannelType.TCP)
        {
            logger.debug("Initiating Fake TCP 3-Way Handshake");

            ByteBuf tcpBuf = byteBufAllocator.buffer();

            try
            {
                // Write SYN with Normal Source and Destination Address
                TCPPacket.writePacket(tcpBuf, null, 0, 0, initiatiorAddr.getPort(), handlerAddr.getPort(), TCPPacket.TCPFlag.SYN);
                completeTCPWrite(initiatiorAddr, handlerAddr, tcpBuf, byteBufAllocator, ctx);

                // Write SYN+ACK with Reversed Source and Destination Address
                TCPPacket.writePacket(tcpBuf, null, 0, 1, handlerAddr.getPort(), initiatiorAddr.getPort(), TCPPacket.TCPFlag.SYN, TCPPacket.TCPFlag.ACK);
                completeTCPWrite(handlerAddr, initiatiorAddr, tcpBuf, byteBufAllocator, ctx);

                // Write ACK with Normal Source and Destination Address
                TCPPacket.writePacket(tcpBuf, null, 1, 1, initiatiorAddr.getPort(), handlerAddr.getPort(), TCPPacket.TCPFlag.ACK);
                completeTCPWrite(initiatiorAddr, handlerAddr, tcpBuf, byteBufAllocator, ctx);
            }
            finally
            {
                tcpBuf.release();
            }

            logger.debug("Finished Fake TCP 3-Way Handshake");
        }

        initialized = true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        initializeIfNecessary(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,
                            Object msg) throws Exception
    {
        if (!isClosed)
        {
            initializeIfNecessary(ctx);

            if (channelType == ChannelType.TCP)
            {
                handleTCP(ctx, msg, false);
            }
            else if (channelType == ChannelType.UDP)
            {
                handleUDP(ctx, msg);
            }
            else
            {
                logDiscard();
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx,
                      Object msg,
                      ChannelPromise promise) throws Exception
    {
        if (!isClosed)
        {
            initializeIfNecessary(ctx);

            if (channelType == ChannelType.TCP)
            {
                handleTCP(ctx, msg, true);
            }
            else if (channelType == ChannelType.UDP)
            {
                handleUDP(ctx, msg);
            }
            else
            {
                logDiscard();
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception
    {
        ByteBufAllocator byteBufAllocator = ctx.alloc();
        ByteBuf packet = this.consolidatedTcpBuf;
        ByteBuf tcpBuf = byteBufAllocator.buffer();
        int bytes = this.consolidatedBytes;

        try
        {
            final InetSocketAddress srcAddr;
            final InetSocketAddress dstAddr;
            if (isServerPipeline)
            {
                srcAddr = handlerAddr;
                dstAddr = initiatiorAddr;
            }
            else
            {
                srcAddr = initiatiorAddr;
                dstAddr = handlerAddr;
            }

            TCPPacket.writePacket(tcpBuf, packet, sendSegmentNumber, receiveSegmentNumber, srcAddr.getPort(), dstAddr.getPort(), TCPPacket.TCPFlag.ACK);
            completeTCPWrite(srcAddr, dstAddr, tcpBuf, byteBufAllocator, ctx);
            logTCP(true, bytes, sendSegmentNumber, receiveSegmentNumber, srcAddr, dstAddr, false);

            sendSegmentNumber += bytes;

            TCPPacket.writePacket(tcpBuf, null, receiveSegmentNumber, sendSegmentNumber, dstAddr.getPort(), srcAddr.getPort(), TCPPacket.TCPFlag.ACK);
            completeTCPWrite(dstAddr, srcAddr, tcpBuf, byteBufAllocator, ctx);
            logTCP(true, bytes, sendSegmentNumber, receiveSegmentNumber, dstAddr, srcAddr, true);
        }
        catch (Exception e)
        {
            logger.error("Failed to write ", e);
        }
        finally
        {
            tcpBuf.release();
            packet.clear();
            this.consolidatedBytes = 0;
        }
        ctx.flush();
    }

    private void logDiscard()
    {
        logger.warn("Discarding pcap write because channel type is unknown. The channel this handler is registered "
                    + "on is not a SocketChannel or DatagramChannel, so the inference does not work. Please call "
                    + "forceTcpChannel or forceUdpChannel before registering the handler.");
    }

    /**
     * Handle TCP L4
     *
     * @param ctx              {@link ChannelHandlerContext} for {@link ByteBuf}
     *                         allocation and {@code fireExceptionCaught}
     * @param msg              {@link Object} must be {@link ByteBuf} else it'll be
     *                         discarded
     * @param isWriteOperation Set {@code true} if we have to process packet when
     *                         packets are being sent out else set {@code false}
     */
    private void handleTCP(ChannelHandlerContext ctx,
                           Object msg,
                           boolean isWriteOperation)
    {
        if (msg instanceof ByteBuf)
        {

            // If bytes are 0 and `captureZeroByte` is false, we won't capture this.
            if (((ByteBuf) msg).readableBytes() == 0 && !captureZeroByte)
            {
                logger.debug("Discarding Zero Byte TCP Packet. isWriteOperation {}", isWriteOperation);
                return;
            }

            ByteBufAllocator byteBufAllocator = ctx.alloc();
            ByteBuf packet = ((ByteBuf) msg).duplicate();
            ByteBuf tcpBuf = byteBufAllocator.buffer();
            int bytes = packet.readableBytes();

            try
            {
                if (isWriteOperation)
                {
                    this.consolidatedTcpBuf.writeBytes(packet);
                    this.consolidatedBytes += bytes;
                }
                else
                {
                    final InetSocketAddress srcAddr;
                    final InetSocketAddress dstAddr;
                    if (isServerPipeline)
                    {
                        srcAddr = initiatiorAddr;
                        dstAddr = handlerAddr;
                    }
                    else
                    {
                        srcAddr = handlerAddr;
                        dstAddr = initiatiorAddr;
                    }

                    TCPPacket.writePacket(tcpBuf, packet, receiveSegmentNumber, sendSegmentNumber, srcAddr.getPort(), dstAddr.getPort(), TCPPacket.TCPFlag.ACK);
                    completeTCPWrite(srcAddr, dstAddr, tcpBuf, byteBufAllocator, ctx);
                    logTCP(false, bytes, receiveSegmentNumber, sendSegmentNumber, srcAddr, dstAddr, false);

                    receiveSegmentNumber += bytes;

                    TCPPacket.writePacket(tcpBuf, null, sendSegmentNumber, receiveSegmentNumber, dstAddr.getPort(), srcAddr.getPort(), TCPPacket.TCPFlag.ACK);
                    completeTCPWrite(dstAddr, srcAddr, tcpBuf, byteBufAllocator, ctx);
                    logTCP(false, bytes, sendSegmentNumber, receiveSegmentNumber, dstAddr, srcAddr, true);
                }
            }
            finally
            {
                tcpBuf.release();
            }
        }
        else
        {
            logger.debug("Discarding Pcap Write for TCP Object: {}", msg);
        }
    }

    /**
     * Write TCP/IP L3 and L2 here.
     *
     * @param srcAddr          {@link InetSocketAddress} Source Address of this
     *                         Packet
     * @param dstAddr          {@link InetSocketAddress} Destination Address of this
     *                         Packet
     * @param tcpBuf           {@link ByteBuf} containing TCP L4 Data
     * @param byteBufAllocator {@link ByteBufAllocator} for allocating bytes for
     *                         TCP/IP L3 and L2 data.
     * @param ctx              {@link ChannelHandlerContext} for
     *                         {@code fireExceptionCaught}
     */
    private void completeTCPWrite(InetSocketAddress srcAddr,
                                  InetSocketAddress dstAddr,
                                  ByteBuf tcpBuf,
                                  ByteBufAllocator byteBufAllocator,
                                  ChannelHandlerContext ctx)
    {

        ByteBuf ipBuf = byteBufAllocator.buffer();
        ByteBuf ethernetBuf = byteBufAllocator.buffer();
        ByteBuf pcap = byteBufAllocator.buffer();

        try
        {
            if (srcAddr.getAddress() instanceof Inet4Address && dstAddr.getAddress() instanceof Inet4Address)
            {
                IPPacket.writeTCPv4(ipBuf,
                                    tcpBuf,
                                    NetUtil.ipv4AddressToInt((Inet4Address) srcAddr.getAddress()),
                                    NetUtil.ipv4AddressToInt((Inet4Address) dstAddr.getAddress()));

                EthernetPacket.writeIPv4(ethernetBuf, ipBuf);
            }
            else if (srcAddr.getAddress() instanceof Inet6Address && dstAddr.getAddress() instanceof Inet6Address)
            {
                IPPacket.writeTCPv6(ipBuf, tcpBuf, srcAddr.getAddress().getAddress(), dstAddr.getAddress().getAddress());

                EthernetPacket.writeIPv6(ethernetBuf, ipBuf);
            }
            else
            {
                logger.error("Source and Destination IP Address versions are not same. Source Address: {}, " + "Destination Address: {}",
                             srcAddr.getAddress(),
                             dstAddr.getAddress());
                return;
            }

            // Write Packet into Pcap
            pCapWriter.writePacket(pcap, ethernetBuf);
        }
        catch (IOException ex)
        {
            logger.error("Caught Exception While Writing Packet into Pcap", ex);
            ctx.fireExceptionCaught(ex);
        }
        finally
        {
            ipBuf.release();
            ethernetBuf.release();
            pcap.release();
        }
    }

    /**
     * Logger for TCP
     */
    private void logTCP(boolean isWriteOperation,
                        int bytes,
                        int sendSegmentNumber,
                        int receiveSegmentNumber,
                        InetSocketAddress srcAddr,
                        InetSocketAddress dstAddr,
                        boolean ackOnly)
    {
        // If `ackOnly` is `true` when we don't need to write any data so we'll not
        // log number of bytes being written and mark the operation as "TCP ACK".
        if (logger.isDebugEnabled())
        {
            if (ackOnly)
            {
                logger.debug("Writing TCP ACK, isWriteOperation {}, Segment Number {}, Ack Number {}, Src Addr {}, " + "Dst Addr {}",
                             isWriteOperation,
                             sendSegmentNumber,
                             receiveSegmentNumber,
                             dstAddr,
                             srcAddr);
            }
            else
            {
                logger.debug("Writing TCP Data of {} Bytes, isWriteOperation {}, Segment Number {}, Ack Number {}, " + "Src Addr {}, Dst Addr {}",
                             bytes,
                             isWriteOperation,
                             sendSegmentNumber,
                             receiveSegmentNumber,
                             srcAddr,
                             dstAddr);
            }
        }
    }

    /**
     * Handle UDP l4
     *
     * @param ctx {@link ChannelHandlerContext} for {@code localAddress} /
     *            {@code remoteAddress}, {@link ByteBuf} allocation and
     *            {@code fireExceptionCaught}
     * @param msg {@link DatagramPacket} or {@link ByteBuf}
     */
    private void handleUDP(ChannelHandlerContext ctx,
                           Object msg)
    {
        ByteBuf udpBuf = ctx.alloc().buffer();

        try
        {
            if (msg instanceof DatagramPacket)
            {

                // If bytes are 0 and `captureZeroByte` is false, we won't capture this.
                if (((DatagramPacket) msg).content().readableBytes() == 0 && !captureZeroByte)
                {
                    logger.debug("Discarding Zero Byte UDP Packet");
                    return;
                }

                DatagramPacket datagramPacket = ((DatagramPacket) msg).duplicate();
                InetSocketAddress srcAddr = datagramPacket.sender();
                InetSocketAddress dstAddr = datagramPacket.recipient();

                // If `datagramPacket.sender()` is `null` then DatagramPacket is initialized
                // `sender` (local) address. In this case, we'll get source address from
                // Channel.
                if (srcAddr == null)
                {
                    srcAddr = (InetSocketAddress) ctx.channel().localAddress();
                }

                logger.debug("Writing UDP Data of {} Bytes, Src Addr {}, Dst Addr {}", datagramPacket.content().readableBytes(), srcAddr, dstAddr);

                UDPPacket.writePacket(udpBuf, datagramPacket.content(), srcAddr.getPort(), dstAddr.getPort());
                completeUDPWrite(srcAddr, dstAddr, udpBuf, ctx.alloc(), ctx);
            }
            else if (msg instanceof ByteBuf && (!(ctx.channel() instanceof DatagramChannel) || ((DatagramChannel) ctx.channel()).isConnected()))
            {

                // If bytes are 0 and `captureZeroByte` is false, we won't capture this.
                if (((ByteBuf) msg).readableBytes() == 0 && !captureZeroByte)
                {
                    logger.debug("Discarding Zero Byte UDP Packet");
                    return;
                }

                ByteBuf byteBuf = ((ByteBuf) msg).duplicate();

                logger.debug("Writing UDP Data of {} Bytes, Src Addr {}, Dst Addr {}", byteBuf.readableBytes(), initiatiorAddr, handlerAddr);

                UDPPacket.writePacket(udpBuf, byteBuf, initiatiorAddr.getPort(), handlerAddr.getPort());
                completeUDPWrite(initiatiorAddr, handlerAddr, udpBuf, ctx.alloc(), ctx);
            }
            else
            {
                logger.debug("Discarding Pcap Write for UDP Object: {}", msg);
            }
        }
        finally
        {
            udpBuf.release();
        }
    }

    /**
     * Write UDP/IP L3 and L2 here.
     *
     * @param srcAddr          {@link InetSocketAddress} Source Address of this
     *                         Packet
     * @param dstAddr          {@link InetSocketAddress} Destination Address of this
     *                         Packet
     * @param udpBuf           {@link ByteBuf} containing UDP L4 Data
     * @param byteBufAllocator {@link ByteBufAllocator} for allocating bytes for
     *                         UDP/IP L3 and L2 data.
     * @param ctx              {@link ChannelHandlerContext} for
     *                         {@code fireExceptionCaught}
     */
    private void completeUDPWrite(InetSocketAddress srcAddr,
                                  InetSocketAddress dstAddr,
                                  ByteBuf udpBuf,
                                  ByteBufAllocator byteBufAllocator,
                                  ChannelHandlerContext ctx)
    {

        ByteBuf ipBuf = byteBufAllocator.buffer();
        ByteBuf ethernetBuf = byteBufAllocator.buffer();
        ByteBuf pcap = byteBufAllocator.buffer();

        try
        {
            if (srcAddr.getAddress() instanceof Inet4Address && dstAddr.getAddress() instanceof Inet4Address)
            {
                IPPacket.writeUDPv4(ipBuf,
                                    udpBuf,
                                    NetUtil.ipv4AddressToInt((Inet4Address) srcAddr.getAddress()),
                                    NetUtil.ipv4AddressToInt((Inet4Address) dstAddr.getAddress()));

                EthernetPacket.writeIPv4(ethernetBuf, ipBuf);
            }
            else if (srcAddr.getAddress() instanceof Inet6Address && dstAddr.getAddress() instanceof Inet6Address)
            {
                IPPacket.writeUDPv6(ipBuf, udpBuf, srcAddr.getAddress().getAddress(), dstAddr.getAddress().getAddress());

                EthernetPacket.writeIPv6(ethernetBuf, ipBuf);
            }
            else
            {
                logger.error("Source and Destination IP Address versions are not same. Source Address: {}, " + "Destination Address: {}",
                             srcAddr.getAddress(),
                             dstAddr.getAddress());
                return;
            }

            // Write Packet into Pcap
            pCapWriter.writePacket(pcap, ethernetBuf);
        }
        catch (IOException ex)
        {
            logger.error("Caught Exception While Writing Packet into Pcap", ex);
            ctx.fireExceptionCaught(ex);
        }
        finally
        {
            ipBuf.release();
            ethernetBuf.release();
            pcap.release();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {

        // If `isTCP` is true, then we'll simulate a `FIN` flow.
        try
        {
            if (channelType == ChannelType.TCP)
            {
                logger.debug("Starting Fake TCP FIN+ACK Flow to close connection");

                ByteBufAllocator byteBufAllocator = ctx.alloc();
                ByteBuf tcpBuf = byteBufAllocator.buffer();

                try
                {
                    // Write FIN+ACK with Normal Source and Destination Address
                    TCPPacket.writePacket(tcpBuf,
                                          null,
                                          sendSegmentNumber,
                                          receiveSegmentNumber,
                                          initiatiorAddr.getPort(),
                                          handlerAddr.getPort(),
                                          TCPPacket.TCPFlag.FIN,
                                          TCPPacket.TCPFlag.ACK);
                    completeTCPWrite(initiatiorAddr, handlerAddr, tcpBuf, byteBufAllocator, ctx);

                    // Write FIN+ACK with Reversed Source and Destination Address
                    TCPPacket.writePacket(tcpBuf,
                                          null,
                                          receiveSegmentNumber,
                                          sendSegmentNumber,
                                          handlerAddr.getPort(),
                                          initiatiorAddr.getPort(),
                                          TCPPacket.TCPFlag.FIN,
                                          TCPPacket.TCPFlag.ACK);
                    completeTCPWrite(handlerAddr, initiatiorAddr, tcpBuf, byteBufAllocator, ctx);

                    // Write ACK with Normal Source and Destination Address
                    TCPPacket.writePacket(tcpBuf,
                                          null,
                                          sendSegmentNumber + 1,
                                          receiveSegmentNumber + 1,
                                          initiatiorAddr.getPort(),
                                          handlerAddr.getPort(),
                                          TCPPacket.TCPFlag.ACK);
                    completeTCPWrite(initiatiorAddr, handlerAddr, tcpBuf, byteBufAllocator, ctx);
                }
                finally
                {
                    tcpBuf.release();
                    this.consolidatedTcpBuf.release();
                }

                logger.debug("Finished Fake TCP FIN+ACK Flow to close connection");
            }

            close();
        }
        catch (Exception e)
        {
            logger.error("handlerRemoved failed", e);
        }
        super.handlerRemoved(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) throws Exception
    {

        if (channelType == ChannelType.TCP)
        {
            ByteBuf tcpBuf = ctx.alloc().buffer();

            try
            {
                // Write RST with Normal Source and Destination Address
                TCPPacket.writePacket(tcpBuf,
                                      null,
                                      sendSegmentNumber,
                                      receiveSegmentNumber,
                                      initiatiorAddr.getPort(),
                                      handlerAddr.getPort(),
                                      TCPPacket.TCPFlag.RST,
                                      TCPPacket.TCPFlag.ACK);
                completeTCPWrite(initiatiorAddr, handlerAddr, tcpBuf, ctx.alloc(), ctx);
            }
            finally
            {
                tcpBuf.release();
            }

            logger.debug("Sent Fake TCP RST to close connection");
        }

        close();
        ctx.fireExceptionCaught(cause);
    }

    /**
     * <p>
     * Close {@code PcapWriter} and {@link OutputStream}.
     * </p>
     * <p>
     * Note: Calling this method does not close {@link PcapWriteHandler}. Only Pcap
     * Writes are closed.
     * </p>
     *
     * @throws IOException If {@link OutputStream#close()} throws an exception
     */
    @Override
    public void close() throws IOException
    {
        if (isClosed)
        {
            logger.debug("PcapWriterHandler is already closed");
        }
        else
        {
            isClosed = true;
            pCapWriter.close();
            logger.debug("PcapWriterHandler is now closed");
        }
    }

    private enum ChannelType
    {
        TCP,
        UDP
    }

    /**
     * Builder for {@link PcapWriteHandler}.
     */
    public static final class Builder
    {
        private boolean captureZeroByte;
        private boolean writePcapGlobalHeader = true;

        private ChannelType channelType;
        private InetSocketAddress initiatiorAddr;
        private InetSocketAddress handlerAddr;
        private boolean isServerPipeline;

        private Builder()
        {
        }

        /**
         * Set to {@code true} to enable capturing packets with empty (0 bytes) payload.
         * Otherwise, if set to {@code false}, empty packets will be filtered out.
         *
         * @param captureZeroByte Whether to filter out empty packets.
         * @return this builder
         */
        public Builder captureZeroByte(boolean captureZeroByte)
        {
            this.captureZeroByte = captureZeroByte;
            return this;
        }

        /**
         * Set to {@code true} to write Pcap Global Header on initialization. Otherwise,
         * if set to {@code false}, Pcap Global Header will not be written on
         * initialization. This could when writing Pcap data on a existing file where
         * Pcap Global Header is already present.
         *
         * @param writePcapGlobalHeader Whether to write the pcap global header.
         * @return this builder
         */
        public Builder writePcapGlobalHeader(boolean writePcapGlobalHeader)
        {
            this.writePcapGlobalHeader = writePcapGlobalHeader;
            return this;
        }

        /**
         * Force this handler to write data as if they were TCP packets, with the given
         * connection metadata. If this method isn't called, we determine the metadata
         * from the channel.
         *
         * @param serverAddress The address of the TCP server (handler)
         * @param clientAddress The address of the TCP client (initiator)
         * @param localIsServer Whether the handler is part of the server channel
         * @return this builder
         */
        public Builder forceTcpChannel(InetSocketAddress serverAddress,
                                       InetSocketAddress clientAddress,
                                       boolean localIsServer)
        {
            channelType = ChannelType.TCP;
            handlerAddr = ObjectUtil.checkNotNull(serverAddress, "serverAddress");
            initiatiorAddr = ObjectUtil.checkNotNull(clientAddress, "clientAddress");
            isServerPipeline = localIsServer;
            return this;
        }

        /**
         * Force this handler to write data as if they were UDP packets, with the given
         * connection metadata. If this method isn't called, we determine the metadata
         * from the channel. <br>
         * Note that even if this method is called, the address information on
         * {@link DatagramPacket} takes precedence if it is present.
         *
         * @param localAddress  The address of the UDP local
         * @param remoteAddress The address of the UDP remote
         * @return this builder
         */
        public Builder forceUdpChannel(InetSocketAddress localAddress,
                                       InetSocketAddress remoteAddress)
        {
            channelType = ChannelType.UDP;
            handlerAddr = ObjectUtil.checkNotNull(remoteAddress, "remoteAddress");
            initiatiorAddr = ObjectUtil.checkNotNull(localAddress, "localAddress");
            return this;
        }

        /**
         * Build the {@link PcapWriteHandler}.
         *
         * @param outputStream The output stream to write the pcap data to.
         * @return The handler.
         */
        public PcapWriteHandler build(OutputStream outputStream)
        {
            ObjectUtil.checkNotNull(outputStream, "outputStream");
            return new PcapWriteHandler(this, outputStream);
        }
    }
}
