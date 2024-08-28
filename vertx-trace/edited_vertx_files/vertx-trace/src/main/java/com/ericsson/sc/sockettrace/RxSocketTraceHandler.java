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
package com.ericsson.sc.sockettrace;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.vertx.trace.VertxSocketTracer;
import com.ericsson.sc.vertx.trace.VertxSocketTracer.EventConsumer;
import com.ericsson.sc.vertx.trace.VertxSocketTracer.NettySocketTraceEvent;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.core.v3.SocketAddress.Protocol;
import io.envoyproxy.envoy.data.tap.v3.Body;
import io.envoyproxy.envoy.data.tap.v3.Connection;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent.Closed;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent.Read;
import io.envoyproxy.envoy.data.tap.v3.SocketEvent.Write;
import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import io.netty.buffer.ByteBuf;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.UnicastProcessor;

public class RxSocketTraceHandler implements VertxSocketTracer.TraceListener
{
    private static final Logger log = LoggerFactory.getLogger(RxSocketTraceHandler.class);

    private final TraceSink traceSink;
    private final long bufferSize;
    private Consumer<Long> onOverflow;
    private final AtomicLong traceIdGenerator = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();

    public RxSocketTraceHandler(TraceSink traceSink,
                                long bufferSize,
                                Consumer<Long> onOverflow)
    {
        Objects.requireNonNull(traceSink);
        this.bufferSize = bufferSize;
        this.onOverflow = onOverflow;
        this.traceSink = traceSink;
    }

    public RxSocketTraceHandler(TraceSink traceSink)
    {
        this(traceSink, 1000, null);
    }

    public long getTotalPacketsDropped()
    {
        return this.dropped.get();
    }

    @Override
    public EventConsumer onNewTrace()
    {
        final var unicastProcessor = UnicastProcessor.<SocketStreamedTraceSegment>create();
        final var traceId = this.traceIdGenerator.getAndIncrement();
        log.info("Started new trace, traceId: {}", traceId);
        final var traceEventConsumer = new EventConsumer()
        {
            @Override
            public void onEvent(NettySocketTraceEvent data)
            {
                SocketStreamedTraceSegment ssts = null;
                try
                {
                    ssts = createSsts(data, traceId);
                }
                catch (Exception e)
                {
                    unicastProcessor.onError(e);
                }
                if (ssts != null)
                {
                    unicastProcessor.onNext(ssts);
                }
            }

            @Override
            public void onCompleted()
            {
                unicastProcessor.onComplete();
            }
        };
        try
        {
            this.traceSink.consumeTrace(unicastProcessor.doOnSubscribe(s -> log.info("Started consuming trace, traceId: {}", traceId))
                                                        .onBackpressureBuffer(bufferSize, this::onDropped, BackpressureOverflowStrategy.DROP_LATEST))
                          .subscribe(() ->
                          {
                          }, err -> log.error("trace stream stopped due to error", err));
        }
        catch (Exception e)
        {
            log.error("Failed to consume trace stream", e);
        }

        return traceEventConsumer;
    }

    private static ByteString toByteString(ByteBuf buf)
    {
        final var bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return ByteString.copyFrom(bytes); // todo use nio buffers?
    }

    private static SocketStreamedTraceSegment createSsts(NettySocketTraceEvent se,
                                                         long traceId)
    {

        final var ssts = SocketStreamedTraceSegment.newBuilder() //
                                                   .setTraceId(traceId);
        final var ts = Timestamp.newBuilder() //
                                .setNanos(se.getTimestamp().getNano())
                                .setSeconds(se.getTimestamp().getEpochSecond());
        switch (se.getType())
        {
            case CONNECT:
                // TODO check if addresses are resolved so that IP is not null
                final var local = se.getConnection().getLocalAddress();
                final var remote = se.getConnection().getRemoteAddress();
                ssts.setConnection(Connection.newBuilder()
                                             .setLocalAddress(Address.newBuilder()
                                                                     .setSocketAddress(SocketAddress.newBuilder()
                                                                                                    .setProtocol(Protocol.TCP)
                                                                                                    .setAddress(local.getAddress().getHostAddress())
                                                                                                    .setPortValue(local.getPort())))
                                             .setRemoteAddress(Address.newBuilder()
                                                                      .setSocketAddress(SocketAddress.newBuilder()
                                                                                                     .setProtocol(Protocol.TCP)
                                                                                                     .setAddress(remote.getAddress().getHostAddress())
                                                                                                     .setPortValue(remote.getPort()))));
                break;
            case CLOSED:
                ssts.setEvent(SocketEvent.newBuilder() //
                                         .setTimestamp(ts)
                                         .setClosed(Closed.getDefaultInstance()));
                break;
            case READ:
                ssts.setEvent(SocketEvent.newBuilder() //
                                         .setTimestamp(ts)
                                         .setRead(Read.newBuilder() //
                                                      .setData(Body.newBuilder() //
                                                                   .setAsBytes(toByteString(se.getData())))));
                break;
            case WRITE:
                ssts.setEvent(SocketEvent.newBuilder() //
                                         .setTimestamp(ts)
                                         .setWrite(Write.newBuilder() //
                                                        .setData(Body.newBuilder() //
                                                                     .setAsBytes(toByteString(se.getData())))));
                break;
            default:
                throw new IllegalArgumentException();
        }

        return ssts.build();
    }

    private void onDropped()
    {
        final var totalDropped = dropped.incrementAndGet();
        if (onOverflow != null)
        {
            try
            {
                onOverflow.accept(totalDropped);
            }
            catch (Exception e)
            {
                log.error("Unexpected error", e);
            }
        }
    }

}
