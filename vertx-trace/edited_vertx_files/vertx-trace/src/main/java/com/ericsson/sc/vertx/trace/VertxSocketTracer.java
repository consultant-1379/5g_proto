/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 21, 2022
 *     Author: echfari
 */

package com.ericsson.sc.vertx.trace;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class VertxSocketTracer
{
    static final AtomicReference<VertxSocketTracer> globalTracer = new AtomicReference<>();

    public static void setGlobalTracer(TraceListener observer)
    {
        Objects.requireNonNull(observer);
        globalTracer.set(new VertxSocketTracer(observer));
    }

    public static void disableGlobalTracer()
    {
        globalTracer.set(null);
    }

    public static VertxSocketTracer getGlobalTracer()
    {
        return globalTracer.get();
    }

    private final TraceListener connectionObserver;

    public VertxSocketTracer(TraceListener connectionObserver)
    {
        Objects.requireNonNull(connectionObserver);
        this.connectionObserver = connectionObserver;
    }

    public void addTraceHandler(Channel ch)
    {
        EventConsumer sev = connectionObserver.onNewTrace();
        ch.pipeline() //
          .addLast(VertxSocketTracer.class.getName(), new TracingChannelHandler(sev));
    }

    public interface TraceListener
    {
        EventConsumer onNewTrace();
    }

    public interface EventConsumer
    {
        void onEvent(NettySocketTraceEvent data);

        void onCompleted();
    }

    public static final class Connection
    {
        public boolean isServer()
        {
            return isServer;
        }

        public InetSocketAddress getLocalAddress()
        {
            return localAddress;
        }

        public InetSocketAddress getRemoteAddress()
        {
            return remoteAddress;
        }

        public Connection(boolean isServer,
                          InetSocketAddress localAddress,
                          InetSocketAddress remoteAddress)
        {
            this.isServer = isServer;
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
        }

        final boolean isServer;
        final InetSocketAddress localAddress;
        final InetSocketAddress remoteAddress;

    }

    public static final class NettySocketTraceEvent
    {
        public EventType getType()
        {
            return operation;
        }

        public ByteBuf getData()
        {
            return data;
        }

        public Connection getConnection()
        {
            return this.connection;
        }

        public Instant getTimestamp()
        {
            return this.timestamp;
        }

        public enum EventType
        {
            READ,
            WRITE,
            CONNECT,
            CLOSED
        }

        private final EventType operation;
        private final ByteBuf data;
        private final Connection connection;
        private final Instant timestamp;

        public NettySocketTraceEvent(EventType operation,
                                     Connection connection,
                                     Instant timestamp,
                                     ByteBuf data)
        {
            this.data = data;
            this.operation = operation;
            this.timestamp = timestamp;
            this.connection = connection;
        }
    }

    private static final class TracingChannelHandler extends ChannelDuplexHandler
    {
        /**
         * Logger for logging events
         */
        private final InternalLogger logger = InternalLoggerFactory.getInstance(TracingChannelHandler.class);

        /**
         * Type of the channel this handler is registered on
         */
        private boolean isSocketChannel;

        private Connection connection;

        /**
         * Whether this handler is initialized (headers written, channel type
         * inferred).No need to make it thread-safe because channel is single-threaded
         */
        private boolean initialized;

        private ByteBuf consolidatedTcpBuf;

        private final EventConsumer eventConsumer;

        TracingChannelHandler(EventConsumer eventConsumer)
        {
            this.eventConsumer = eventConsumer;
        }

        private void initializeIfNecessary(ChannelHandlerContext ctx)
        {
            if (initialized)
            {
                return;
            }

            ByteBufAllocator byteBufAllocator = ctx.alloc();
            this.consolidatedTcpBuf = byteBufAllocator.buffer();

            // infer channel type
            if (ctx.channel() instanceof SocketChannel)
            {
                isSocketChannel = true;

                final boolean isServerSocket = (ctx.channel().parent() instanceof ServerSocketChannel);
                this.connection = (new Connection(isServerSocket,
                                                  (InetSocketAddress) ctx.channel().localAddress(),
                                                  (InetSocketAddress) ctx.channel().remoteAddress()));
                final NettySocketTraceEvent connectEvent = new NettySocketTraceEvent(NettySocketTraceEvent.EventType.CONNECT, connection, Instant.now(), null);
                this.eventConsumer.onEvent(connectEvent);
            }

            initialized = true;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception
        {
            try
            {
                initializeIfNecessary(ctx);
            }
            catch (Exception e)
            {
                logger.debug("Unexpected error in tracer handler, ignored", e);
            }
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception
        {
            logger.info("CHANNEL IS INACTIVE");
            try
            {
                this.eventConsumer.onEvent(new NettySocketTraceEvent(NettySocketTraceEvent.EventType.CLOSED, connection, Instant.now(), null));
                this.eventConsumer.onCompleted();
            }
            catch (Exception e)
            {
                logger.debug("Unexpected error in tracer handler, ignored", e);
            }
            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx,
                                Object msg) throws Exception
        {
            try
            {
                initializeIfNecessary(ctx);
                handleTCP(msg, false);
            }
            catch (Exception e)
            {
                logger.debug("Unexpected error in tracer handler, ignored", e);
            }
            super.channelRead(ctx, msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx,
                          Object msg,
                          ChannelPromise promise) throws Exception
        {
            try
            {
                initializeIfNecessary(ctx);
                handleTCP(msg, true);
            }
            catch (Exception e)
            {
                logger.debug("Unexpected error in tracer handler, ignored", e);
            }
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception
        {
            final ByteBuf packet = this.consolidatedTcpBuf;
            try
            {
                if (packet.readableBytes() > 0) // suppress empty packets
                {
                    eventConsumer.onEvent(new NettySocketTraceEvent(NettySocketTraceEvent.EventType.WRITE, this.connection, Instant.now(), packet.duplicate()));
                }
            }
            catch (Exception e)
            {
                logger.error("Unexpected error in tracing handler, ignored", e);
            }
            packet.clear();
            ctx.flush();
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
        {
            try
            {
                this.eventConsumer.onEvent(new NettySocketTraceEvent(NettySocketTraceEvent.EventType.CLOSED, connection, Instant.now(), null));
                this.eventConsumer.onCompleted();
            }
            catch (Exception e)
            {
                logger.error("Unexpected exception while removing tracing handler, ignored", e);
            }

            this.consolidatedTcpBuf.release();
            super.handlerRemoved(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx,
                                    Throwable cause) throws Exception
        {
            try
            {
                // TODO convey error instead of simulating connection close
                this.eventConsumer.onEvent(new NettySocketTraceEvent(NettySocketTraceEvent.EventType.CLOSED, connection, Instant.now(), null));
                this.eventConsumer.onCompleted();
            }
            catch (Exception e)
            {
                logger.error("Unexpected error in tracer handler, ignored", e);
            }
            super.exceptionCaught(ctx, cause);
        }

        private void handleTCP(Object msg,
                               boolean isWriteOperation)
        {
            if (this.isSocketChannel && msg instanceof ByteBuf)
            {
                ByteBuf packet = ((ByteBuf) msg).duplicate().asReadOnly();
                if (isWriteOperation)
                {
                    // Handle socket write op
                    this.consolidatedTcpBuf.writeBytes(packet);
                }
                else
                {
                    // handle socket read op
                    if (packet.readableBytes() > 0) // suppress empty packets
                    {
                        this.eventConsumer.onEvent(new NettySocketTraceEvent(NettySocketTraceEvent.EventType.READ, this.connection, Instant.now(), packet));
                    }
                }
            }
            else
            {
                logger.debug("Discarded tracing for Object: {}", msg);
            }
        }
    }
}
