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
 * Created on: Jan 28, 2022
 *     Author: eedsvs, echfari
 */
package com.ericsson.sc.tapcol.pcap;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.tapcol.TapCol.DivisionMethod;

import io.envoyproxy.envoy.data.tap.v3.Connection;
import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

public class SstsProcessor
{
    private static final Logger log = LoggerFactory.getLogger(SstsProcessor.class);

    private final ConcurrentHashMap<Long, TapStream> streamForTraceID;
    private final AtomicReference<ConnectionTransformer> transformer = new AtomicReference<>();

    public SstsProcessor()
    {
        this.streamForTraceID = new ConcurrentHashMap<>(128);
    }

    public SstsProcessor(Flux<ConnectionTransformer> ctFlux)
    {
        this.streamForTraceID = new ConcurrentHashMap<>(128);
        ctFlux.doOnError(err -> log.error("Stopped receiving local socket address replacement configuration, will retry")) //
              .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(10)))
              .subscribe(this.transformer::set, err -> log.error("Fatal error in socket address replacement process", err));
    }

    public Flux<List<TapPacket>> processSsts(Flux<SocketStreamedTraceSegment> traceFlux,
                                             DivisionMethod divisionMethod,
                                             Integer chunkSize)
    {

        return traceFlux.handle((traceSegment,
                                 sink) ->
        {
            if (traceSegment.hasConnection())
            {
                handleNewConnection(traceSegment.getTraceId(), traceSegment.getConnection());
            }
            else if (traceSegment.hasEvent())
            {
                final var event = traceSegment.getEvent();
                final var traceId = traceSegment.getTraceId();
                final var tapStream = streamForTraceID.computeIfAbsent(traceId, key ->
                {
                    // we missed the initial connection message from Envoy (create dummy tapStream)
                    log.warn("Unknown traceId {} tap decoding for this stream will be dummy decoded", traceId);
                    return new TapStream(key);
                });

                final var packets = tapStream.synthesizePacket(event, divisionMethod, chunkSize);
                if (packets != null && !packets.isEmpty())
                {
                    // TODO handle exception
                    sink.next(packets);
                }

                // Envoy ends tap stream
                if (event.hasClosed())
                {
                    final var previous = streamForTraceID.remove(traceId);
                    if (previous != null)
                    {
                        log.debug("Closed tap session for traceId {}", traceId);
                    }
                    else
                    {
                        log.warn("Cannot close session for unknown traceId {}", traceId);
                    }
                }
            }
            else
            {
                log.error("Received trace segment lacking connection and event content:\n>>>\n{}<<<", traceSegment);
            }
        });
    }

    private void handleNewConnection(final Long traceId,
                                     final Connection realConnection)
    {
        final var tr = this.transformer.get();
        final var transformed = tr != null ? // A transformed is available, use it to process connection
                                           tr.transform(realConnection).orElse(null)  // indicate that transformation is not applicable
                                           : null;
        final var connection = transformed != null ? transformed : realConnection;
        final var newTapStream = new TapStream(traceId, connection);

        final var previous = streamForTraceID.put(traceId, newTapStream);
        if (previous != null)
        {
            log.warn("Updated existing session for traceId {}", traceId);
        }
        else
        {
            log.debug("Creating tap session for traceId: {}", traceId);
        }
    }
}
