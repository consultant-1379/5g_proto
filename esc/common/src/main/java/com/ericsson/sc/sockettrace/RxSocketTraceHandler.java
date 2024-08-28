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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.processors.UnicastProcessor;

public final class RxSocketTraceHandler implements VertxSocketTracer.TraceListener
{
    public enum SegmentType
    {
        CONNECTION,
        EVENT
    }

    private static final Logger log = LoggerFactory.getLogger(RxSocketTraceHandler.class);

    private final TraceSink traceSink;
    private final long bufferSize;
    private final int segmentLimit;
    private final AtomicLong traceIdGenerator = new AtomicLong();
    private final AtomicReference<Optional<String>> nfInstance = new AtomicReference<>();
    private final Counters counters = new Counters();

    public RxSocketTraceHandler(final TraceSink traceSink,
                                final long bufferSize,
                                final int segmentLimit,
                                final Flowable<String> nfInstance)
    {
        Objects.requireNonNull(traceSink);
        this.bufferSize = bufferSize;
        this.traceSink = traceSink;
        this.segmentLimit = segmentLimit;

        nfInstance.subscribe(this::setNfInstance, err -> log.error("Stopped updating nfInstance on socket trace handler with error: ", err));
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
            final var currentSsts = new AtomicReference<SocketStreamedTraceSegment>();

            this.traceSink.consumeTrace(unicastProcessor //
                                                        .doOnNext(currentSsts::set)
                                                        .filter(this::checkValidSize)
                                                        .doOnSubscribe(s -> log.info("Started consuming trace, traceId: {}", traceId))
                                                        .onBackpressureBuffer(this.bufferSize,
                                                                              () -> onDropped(currentSsts.get()),
                                                                              BackpressureOverflowStrategy.DROP_LATEST))
                          .subscribe(() ->
                          {
                          }, err -> log.error("Trace stream stopped due to error", err));
        }
        catch (Exception e)
        {
            log.error("Failed to consume trace stream", e);
        }

        return traceEventConsumer;
    }

    private boolean checkValidSize(final SocketStreamedTraceSegment ssts)
    {
        log.debug("Current SSTS: {}, size: {}", ssts, ssts.getSerializedSize());

        if (ssts.getSerializedSize() <= this.segmentLimit)
        {
            return true;
        }
        else
        {
            this.onDroppedSizeBig(ssts);
            return false;
        }

    }

    private static ByteString toByteString(ByteBuf buf)
    {
        final var bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return ByteString.copyFrom(bytes); // TODO maybe use nio buffers
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
            {
                final var local = se.getConnection().getLocalAddress();
                final var remote = se.getConnection().getRemoteAddress();

                log.debug("Channel connected with LocalAddress: {}:{}, RemoteAddress: {}:{}",
                          local.getAddress(),
                          local.getPort(),
                          remote.getAddress(),
                          remote.getPort());

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
            }
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
                throw new IllegalArgumentException(String.format("Unexpected exception received, trace event type: %s", se.getType())); // this should never
                                                                                                                                        // happen
        }

        return ssts.build();
    }

    /**
     * Set the nfInstance.
     * 
     * @param nfInstance the nfInstance to set
     */
    private void setNfInstance(final String nfInstance)
    {
        this.nfInstance.set(Optional.ofNullable(nfInstance));
    }

    /**
     * Get the nfInstance.
     * 
     * @return the nfInstance
     */
    public String getNfInstance()
    {
        final var res = this.nfInstance.get();
        return res.orElse("unknown");
    }

    private void onDroppedSizeBig(final SocketStreamedTraceSegment ssts)
    {
        log.debug("Segment dropped due to big size: {}", ssts);

        try
        {
            final var segmentType = ssts.hasConnection() ? SegmentType.CONNECTION : SegmentType.EVENT;

            this.stepVtapSegmentsDroppedSizeBigCounter(segmentType, this.getNfInstance());
        }
        catch (Exception e)
        {
            log.error("Exception received while stepping segment dropped due to big size counter: ", e);
        }
    }

    private void onDropped(final SocketStreamedTraceSegment ssts)
    {
        log.debug("Segment dropped due to backpressure: {}", ssts);

        try
        {
            final var segmentType = ssts.hasConnection() ? SegmentType.CONNECTION : SegmentType.EVENT;

            this.stepVtapSegmentsDroppedCounter(segmentType, this.getNfInstance());
        }
        catch (Exception e)
        {
            log.error("Exception received while stepping segment dropped counter: ", e);
        }

    }

    private void stepVtapSegmentsDroppedCounter(final SegmentType segmentType,
                                                final String nfInstance)
    {
        log.debug("Stepping segments dropped counter, eventType: {}, nfInstance: {}", segmentType, nfInstance);

        this.counters.getCcVtapSegmentsDropped(segmentType, nfInstance).inc();
    }

    private void stepVtapSegmentsDroppedSizeBigCounter(final SegmentType segmentType,
                                                       final String nfInstance)
    {
        log.debug("Stepping segments dropped counter due to big size, eventType: {}, nfInstance: {}", segmentType, nfInstance);

        this.counters.getCcVtapSegmentsDroppedSizeBig(segmentType, nfInstance).inc();
    }

    public Counters getCounters()
    {
        return this.counters;
    }

    public static class Counters
    {
        private static final String NF_LBL = "nf";
        private static final String NF_INSTANCE_LBL = "nf_instance";
        private static final String BSF_NF_NAME = "bsf";
        private static final String SEGMENT_TYPE_LBL = "segment_type";

        // CC counters

        /**
         * Prometheus CC counter for total segments dropped.
         */
        private static final Counter ccVtapSegmentsDropped = Counter.build()
                                                                    .namespace(BSF_NF_NAME)
                                                                    .name("vtap_segments_dropped_total")
                                                                    .labelNames(SEGMENT_TYPE_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                    .help("Number of segments dropped from BSF worker")
                                                                    .register();

        public Child getCcVtapSegmentsDropped(final SegmentType segmentType,
                                              final String nfInstance)
        {
            return ccVtapSegmentsDropped.labels(segmentType.toString().toLowerCase(), // segment_type
                                                nfInstance, // nf_instance
                                                BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for total segments dropped due to big size.
         */
        private static final Counter ccVtapSegmentsDroppedSizeBig = Counter.build()
                                                                           .namespace(BSF_NF_NAME)
                                                                           .name("vtap_segments_dropped_size_too_big_total")
                                                                           .labelNames(SEGMENT_TYPE_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                           .help("Number of segments dropped due to big size from BSF worker")
                                                                           .register();

        public Child getCcVtapSegmentsDroppedSizeBig(final SegmentType segmentType,
                                                     final String nfInstance)
        {
            return ccVtapSegmentsDroppedSizeBig.labels(segmentType.toString().toLowerCase(), // segment_type
                                                       nfInstance, // nf_instance
                                                       BSF_NF_NAME); // nf
        }

    }

}
