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
 * Created on: Apr 27, 2022
 *     Author: echfari
 */
package com.ericsson.sc.probebroker;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.tapcol.PacketSink;
import com.ericsson.sc.tapcol.pcap.TapPacket;
import com.ericsson.sc.utils.pcapng.PcapNgBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableChannel;
import reactor.netty.channel.MicrometerChannelMetricsRecorder;
import reactor.netty.tcp.TcpClient;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

public class PvtbPacketSink implements PacketSink
{
    private static final Logger log = LoggerFactory.getLogger(PvtbPacketSink.class);
    private static final JsonMapper jm = JsonMapper.builder().build();
    private static final String OUT_CONNECTIONS_OPENED = "tapcollector_out_connections_opened";
    private static final String OUT_CONNECTIONS_CLOSED = "tapcollector_out_connections_closed";
    private static final String OUT_SEND_ERROR_TOTAL = "tapcollector_out_tap_frames_sent_errors_total";
    private static final String OUT_SEND_SUCCESS_TOTAL = "tapcollector_out_tap_frames_success_total";
    private static final String CLIENT_ERRORS = "Client.errors";
    private static final String OUT_CLIENT_ERRORS = "tapcollector_out_send_error_total";
    private static final String OUT_PVTB_INIT_FAILURES = "tapcollector_out_pvtb_init_failures_total";
    private static final String OUT_PVTB_INIT_SUCCESS = "tapcollector_out_pvtb_init_success_total";
    private final byte[] pvtbStreamHeader;
    private final TcpClient tcpClient;
    private final Mono<? extends Connection> connection;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final RetryBackoffSpec sendRetryPolicy = Retry //
                                                          .backoff(Long.MAX_VALUE, Duration.ofMillis(100))
                                                          .jitter(0.5)
                                                          .maxBackoff(Duration.ofSeconds(2))
                                                          .doBeforeRetry(r -> log.warn("Failed to send,{}", r.failure().getMessage()));

    public PvtbPacketSink(String host,
                          int port,
                          PvtbStreamHeader header)
    {
        Objects.requireNonNull(host);
        Objects.requireNonNull(header);

        this.pvtbStreamHeader = createPvtbHeader(header);

        // Define the counters
        MicrometerChannelMetricsRecorder mrClient = new MicrometerChannelMetricsRecorder("Client", "TCP");
        PmMetrics metrics = PmMetrics.factory();

        metrics.replaceWith(CLIENT_ERRORS, OUT_CLIENT_ERRORS);

        metrics.addAccept(OUT_CONNECTIONS_OPENED);
        metrics.addAccept(OUT_SEND_ERROR_TOTAL);
        metrics.addAccept(OUT_SEND_SUCCESS_TOTAL);
        metrics.addAccept(OUT_CLIENT_ERRORS);
        metrics.addAccept(OUT_PVTB_INIT_FAILURES);
        metrics.addAccept(OUT_PVTB_INIT_SUCCESS);

        metrics.createCounter(OUT_CONNECTIONS_OPENED, OUT_CONNECTIONS_OPENED);
        metrics.createCounter(OUT_CONNECTIONS_CLOSED, OUT_CONNECTIONS_CLOSED);
        metrics.createCounter(OUT_SEND_ERROR_TOTAL, OUT_SEND_ERROR_TOTAL);
        metrics.createCounter(OUT_PVTB_INIT_FAILURES, OUT_PVTB_INIT_FAILURES);
        metrics.createCounter(OUT_PVTB_INIT_SUCCESS, OUT_PVTB_INIT_SUCCESS);
        metrics.createCounter(OUT_SEND_SUCCESS_TOTAL, OUT_SEND_SUCCESS_TOTAL);

        final var connectionRetryPolicy = Retry //
                                               .backoff(Long.MAX_VALUE, Duration.ofMillis(100))
                                               .maxBackoff(Duration.ofSeconds(10));

        this.tcpClient = TcpClient //
                                  .newConnection()
                                  .option(ChannelOption.SO_KEEPALIVE, true) // Enable TCP keepalive to detect stale PVTB connections
                                  .option(EpollChannelOption.TCP_KEEPIDLE, 10)
                                  .option(EpollChannelOption.TCP_KEEPINTVL, 5)
                                  .option(EpollChannelOption.TCP_KEEPCNT, 3)
                                  .doOnConnected(conn ->
                                  {
                                      conn.addHandlerFirst(new WriteTimeoutHandler(5, TimeUnit.SECONDS));
                                      safeIncreaseCounter(OUT_CONNECTIONS_OPENED);

                                  })
                                  .metrics(true, () -> mrClient)
                                  .host(host)
                                  .port(port);

        this.connection = tcpClient.connect() //
                                   .doOnSubscribe(s -> log.info("Creating PVTB connection {}:{}", host, port))
                                   .flatMap(conn -> //
                                   {
                                       final var brokerStreamInitMsg = createInitMessage(conn.channel().alloc());
                                       return conn.outbound() //
                                                  .send(Mono.just(brokerStreamInitMsg))
                                                  .then()
                                                  .doOnError(error -> safeIncreaseCounter(OUT_PVTB_INIT_FAILURES))
                                                  .then(Mono.<Connection>just(conn));
                                   })
                                   .doOnNext(c ->
                                   {
                                       log.info("Established PVTB connection {}", c.channel());
                                       safeIncreaseCounter(OUT_PVTB_INIT_SUCCESS);
                                   })
                                   .doOnError(err -> log.warn("Failed to initialize PVTB connection: {}", err.getMessage()))
                                   .retryWhen(connectionRetryPolicy)
                                   .cacheInvalidateWhen(conn -> conn.onDispose()
                                                                    .doOnSuccess(vd -> log.info("PVTB connection terminated {}", conn))
                                                                    .then(Mono.defer(() -> this.stopped.get() ? Mono.never() : Mono.empty())));
    }

    public Mono<Void> start()
    {
        return this.connection.then();
    }

    public Mono<Void> stop()
    {
        return this.connection.doOnSubscribe(c -> stopped.set(true)) //
                              .doOnNext(DisposableChannel::dispose)
                              .flatMap(DisposableChannel::onDispose);
    }

    public ByteBuf createInitMessage(ByteBufAllocator allocator)
    {
        log.debug("Creating PVTB init message, streamHeader length: {}", this.pvtbStreamHeader.length);
        final var buf = allocator.buffer();
        buf.writeBytes(pvtbStreamHeader);
        PcapNgBuilder.writeSHB(buf);
        PcapNgBuilder.writeIDB(buf);
        return buf;
    }

    @Override
    public Mono<Void> consumePacketStream(Flux<List<TapPacket>> inFlow)
    {
        return inFlow.map(list -> Flux.fromIterable(list)).concatMap(rp -> rp.concatMap(packet -> send(Mono.just(packet))).retryWhen(sendRetryPolicy)).next();

    }

    public Mono<Void> send(Mono<TapPacket> inFlow)
    {
        return connection.flatMap(c -> c.outbound() //
                                        .send(inFlow.map(rp -> toByteBuf(rp, c.channel().alloc())))
                                        .then()
                                        .doOnError(error -> safeIncreaseCounter(OUT_SEND_ERROR_TOTAL))
                                        .doOnSuccess(send -> safeIncreaseCounter(OUT_SEND_SUCCESS_TOTAL))
                                        .onErrorResume(err -> c.onDispose() // Close connection upon error before retrying
                                                               .doOnSubscribe(s ->
                                                               {
                                                                   log.warn("Closing PVTB connection after send error");
                                                                   c.dispose();
                                                               })
                                                               .then(Mono.error(err))));
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

    private static ByteBuf toByteBuf(TapPacket input,
                                     ByteBufAllocator allocator)
    {
        final var buf = allocator.buffer();
        PcapNgBuilder.writeEPB(buf, input.getPacket(), input.getTimeStamp());
        return buf;
    }

    @JsonPropertyOrder({ "version", "domain", "podName", "protocol", "contentType" })
    public static class PvtbStreamHeader
    {

        @JsonProperty("version")
        private String version = "1.0";
        @JsonProperty("domain")
        private String domain;
        @JsonProperty("podName")
        private String podName;
        @JsonProperty("protocol")
        private String protocol;
        @JsonProperty("contentType")
        private String contentType = "application/vnd.tcpdump.pcapng";

        public PvtbStreamHeader(String domain,
                                String protocol,
                                String podName)
        {
            Objects.requireNonNull(domain);
            Objects.requireNonNull(protocol);
            Objects.requireNonNull(podName);

            this.domain = domain;
            this.protocol = protocol;
            this.podName = podName;
        }

        @Override
        public String toString()
        {
            return String.format("PvtbStreamHeader [version=%s, domain=%s, podName=%s, protocol=%s, contentType=%s]",
                                 version,
                                 domain,
                                 podName,
                                 protocol,
                                 contentType);
        }
    }

    private static class PvtbStreamHeaderContainer
    {
        @JsonProperty("stream")
        PvtbStreamHeader stream;

        public PvtbStreamHeaderContainer(PvtbStreamHeader h)
        {
            this.stream = h;
        }
    }

    private static byte[] createPvtbHeader(PvtbStreamHeader header)
    {
        Objects.requireNonNull(header);

        try
        {
            final var jsonHeader = jm.writeValueAsBytes(new PvtbStreamHeaderContainer(header));
            final var baos = new ByteArrayOutputStream();

            baos.write(0x1E); // Record separator character
            baos.writeBytes(jsonHeader); // JSON header
            baos.write(0xA); // LF

            return baos.toByteArray();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to create PVTB stream header", e);
        }
    }
}
