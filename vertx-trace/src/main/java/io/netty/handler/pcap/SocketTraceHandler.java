package io.netty.handler.pcap;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public final class SocketTraceHandler extends ChannelDuplexHandler
{

    /**
     * Logger for logging events
     */
    private final InternalLogger logger = InternalLoggerFactory.getInstance(SocketTraceHandler.class);

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
     * Whether this handler is initialized (headers written, channel type inferred)
     */
    private boolean initialized;

    private ByteBuf consolidatedTcpBuf;

    private int consolidatedBytes;

    private SocketTraceHandler()
    {
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

        initializeIfNecessary(ctx);

        if (channelType == ChannelType.TCP)
        {
            handleTCP(ctx, msg, false);
        }
        else
        {
            // DISCARD logDiscard();
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx,
                      Object msg,
                      ChannelPromise promise) throws Exception
    {
        initializeIfNecessary(ctx);

        if (channelType == ChannelType.TCP)
        {
            handleTCP(ctx, msg, true);
        }
        else
        {
            // DISCARD logDiscard();
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception
    {
        final ByteBufAllocator byteBufAllocator = ctx.alloc();
        final ByteBuf packet = this.consolidatedTcpBuf;
        final ByteBuf tcpBuf = byteBufAllocator.buffer();
        final int bytes = this.consolidatedBytes;

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

                    // TODO
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

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {

        this.consolidatedTcpBuf.release();
        super.handlerRemoved(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) throws Exception
    {

        // TODO
        super.exceptionCaught(ctx, cause);
    }

    private enum ChannelType
    {
        TCP
        // UDP ?
    }

}
