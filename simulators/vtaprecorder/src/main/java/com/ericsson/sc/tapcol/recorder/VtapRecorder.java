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
 * Created on: Jul 10, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol.recorder;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.tapcol.recorder.GrePacket.PayloadType;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.udp.UdpServer;

/**
 * PVTB NBI simulator
 *
 */
public class VtapRecorder
{
//    static
//    {
//        // Force IPv4
//        System.setProperty("java.net.preferIPv4Stack", "true");
//    }
    private static final Logger log = LoggerFactory.getLogger(VtapRecorder.class);

    private final Flux<DatagramPacket> udpFlux;
    private final HttpServer controlServer;
    private final Composite compDisp = Disposables.composite();
    private final Config config;
    private AtomicReference<GrePacketWriter> packetWriter = new AtomicReference<>();
    private AtomicReference<GrePacketWriter> previousPacketWriter = new AtomicReference<>();

    private final GrePacketFlux recorderData;

    VtapRecorder(Config config)
    {
        this.config = config;
        this.udpFlux = receiveUdp(config.listenHost, config.sinkPort) //
                                                                     .share();

        this.recorderData = new GrePacketFluxImpl(udpFlux);

        this.controlServer = createControlServer(config.listenHost, config.controlPort);
    }

    private void run()
    {
        compDisp.add(this.udpFlux.subscribe());
        this.controlServer.bindUntilJavaShutdown(Duration.ofSeconds(5), null);
        compDisp.dispose();
    }

    public static void main(String[] args)
    {

        final var cfg = Config.fromEnv();
        log.info("Read configuration from enviromental variables: {}", cfg);

        final var pvtbRecorder = new VtapRecorder(cfg);
        pvtbRecorder.run();
        log.info("VTAP recorder terminated");
    }

    private HttpServer createControlServer(String host,
                                           int port)
    {
        return HttpServer.create()
                         .host(host)
                         .port(port)
                         .route(routes -> routes.get("/status",
                                                     (request,
                                                      response) ->
                                                     {
                                                         try
                                                         {
                                                             return response//
                                                                            .sendString(Mono.just(Jackson.om().writeValueAsString(getStatus())));
                                                         }
                                                         catch (Exception err)
                                                         {
                                                             log.warn("Error while processing request", err);
                                                             return response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                                                                            .sendString(Mono.just(err.toString()));
                                                         }
                                                     })
                                                .post("/start",
                                                      (request,
                                                       response) -> response.sendString(startCapture().map(String::valueOf)
                                                                                                      .doOnError(err -> log.error("/start command failed", err))
                                                                                                      .doOnError(err -> response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR))
                                                                                                      .onErrorResume(err -> Mono.just(err.toString()))))
                                                .post("/stop",
                                                      (request,
                                                       response) -> response.sendString(stopCapture().map(String::valueOf)
                                                                                                     .doOnError(err -> log.error("/stop commaind failed", err))
                                                                                                     .doOnError(err -> response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR))
                                                                                                     .onErrorResume(err -> Mono.just(err.toString()))))
                                                .get("/pcapng",
                                                     (request,
                                                      response) -> response.sendFile(Path.of(config.getPcapNgFilename()))) //
                         );
    }

    private Mono<Boolean> startCapture()
    {
        return Mono.defer(() ->
        {
            final var pw = this.packetWriter.get();
            if (pw == null)
            {
                log.info("Start dumping packets to file: {}", config.getPcapNgFilename());
                final var grePacketFlux = this.recorderData.getGrePacketFlux();
                final var newPw = new GrePacketWriter(grePacketFlux, this.config.getPcapNgFilename(), recorderData.getLatestPcapngHeader());
                final var raceCondition = !this.packetWriter.compareAndSet(null, newPw);
                if (raceCondition)
                {
                    // This should never happen due to synchronized keyword
                    throw new IllegalStateException("Race condition during start()");
                }
                newPw.start();
                return Mono.just(true);
            }
            return Mono.just(false); // Already started, NOOP
        });
    }

    private Mono<Boolean> stopCapture()
    {
        return Mono.defer(() ->
        {
            final var pw = this.packetWriter.get();
            if (pw != null)
            {
                log.info("Stop dumping packets");
                pw.stop(); // request stop, might throw

                return pw.onFinished() //
                         .doOnSuccess(empty ->
                         {
                             log.debug("Packet writter fully stopped");
                             final var raceCondition = !this.packetWriter.compareAndSet(pw, null); // set current packet writer to null
                             if (raceCondition)
                             {
                                 // This should never happen due to synchronized keyword
                                 throw new IllegalStateException("Race condition during stop()");
                             }
                             this.previousPacketWriter.set(pw);
                         })
                         .then(Mono.just(true));
            }
            else
            {
                return Mono.just(false); // already stopped, NOOP
            }
        });
    }

    private Status getStatus()
    {
        var writer = this.packetWriter.get();
        final var isRunning = writer != null;
        final var previousWriter = this.previousPacketWriter.get();
        writer = isRunning ? writer : previousWriter;
        final var greStats = new Status.GreStats(recorderData.getUdpPacketCount(),
                                                 recorderData.getDroppedUdpPackets(),
                                                 recorderData.getPcapNgHeaderCount(),
                                                 recorderData.getJsonHeaderCount());
        return writer != null ? new Status(isRunning,
                                           greStats,
                                           new Status.FileStats(writer.getpcapNgHeadersWritten(), writer.getDataPacketWritten(), writer.getBytesWritten()))
                              : new Status(greStats);
    }

    private static class GrePacketFluxImpl implements GrePacketFlux
    {
        private final AtomicReference<byte[]> latestPcapngHeader = new AtomicReference<>();
        private final AtomicLong receivedUdpPackets = new AtomicLong();
        private final AtomicLong droppedUdpPackets = new AtomicLong();
        private final AtomicLong pcapNgHeaderCount = new AtomicLong();
        private final AtomicLong jsonHeaderCount = new AtomicLong();

        private final Flux<GrePacket> packetFlux;

        GrePacketFluxImpl(Flux<DatagramPacket> udpFlux)
        {
            this.packetFlux = udpFlux //
                                     .map(udp ->
                                     {
                                         try
                                         {
                                             return Optional.of(GrePacket.fromUdp(udp));
                                         }
                                         catch (Exception e)
                                         {
                                             log.warn("Failed to decode UDP packet", e);
                                             return Optional.<GrePacket>empty();
                                         }
                                     })
                                     .filter(Optional::isPresent) //
                                     .map(Optional::get);
            udpFlux //
                   .doOnNext(nxt -> receivedUdpPackets.incrementAndGet())
                   .map(udp ->
                   {
                       try
                       {
                           return Optional.of(GrePacket.fromUdp(udp));
                       }
                       catch (Exception e)
                       {
                           droppedUdpPackets.incrementAndGet();
                           return Optional.<GrePacket>empty();
                       }
                   })
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .doOnNext(gre ->
                   {
                       if (gre.getPayloadType() == PayloadType.PCAPNG_HEADERS)
                       {
                           pcapNgHeaderCount.incrementAndGet();

                           final byte[] bytes = new byte[gre.getPayload().readableBytes()];
                           gre.getPayload().slice().getBytes(0, bytes);
                           latestPcapngHeader.set(bytes);
                       }
                       else if (gre.getPayloadType() == PayloadType.JSON)
                       {
                           this.jsonHeaderCount.incrementAndGet();
                       }
                       gre.getPayload().release();
                   })
                   .ignoreElements()
                   .subscribe(x ->
                   {
                   }, err -> log.error("Unexpected error, pcapng header update process stopped", err));
        }

        @Override
        public long getUdpPacketCount()
        {
            return receivedUdpPackets.get();
        }

        @Override
        public long getDroppedUdpPackets()
        {
            return droppedUdpPackets.get();
        }

        @Override
        public Flux<GrePacket> getGrePacketFlux()
        {
            return packetFlux;
        }

        @Override
        public byte[] getLatestPcapngHeader()
        {
            return this.latestPcapngHeader.get();
        }

        @Override
        public long getPcapNgHeaderCount()
        {
            return pcapNgHeaderCount.get();
        }

        @Override
        public long getJsonHeaderCount()
        {
            return jsonHeaderCount.get();
        }
    }

    static interface GrePacketFlux
    {
        long getUdpPacketCount();

        long getJsonHeaderCount();

        long getPcapNgHeaderCount();

        byte[] getLatestPcapngHeader();

        long getDroppedUdpPackets();

        Flux<GrePacket> getGrePacketFlux();
    }

    @JsonPropertyOrder({ "capturing", "synced", "greStats", "fileStats" })
    public static class Status
    {
        private final boolean capturing;
        private final boolean synced;
        private final GreStats greStats;
        private final FileStats fileStats;

        public Status(boolean capturing,
                      GreStats greStats,
                      FileStats fileStats)
        {
            Objects.requireNonNull(greStats);
            this.capturing = capturing;
            this.greStats = greStats;
            this.fileStats = fileStats;
            this.synced = greStats.getPcapNgHeaders() > 0;
        }

        public Status(GreStats greStats)
        {
            this(false, greStats, null);
        }

        public boolean isCapturing()
        {
            return capturing;
        }

        public boolean isSynced()
        {
            return synced;
        }

        public GreStats getGreStats()
        {
            return this.greStats;
        }

        public FileStats getFileStats()
        {
            return this.fileStats;
        }

        @JsonPropertyOrder({ "udpPackets", "droppedUdpPackets", "pcapNgHeaders", "jsonHeaders" })
        public static class GreStats
        {
            private long udpPackets;
            private long droppedUdpPackets;
            private long pcapNgHeaders;
            private long jsonHeaders;

            public GreStats(long udpPackets,
                            long droppedUdpPackets,
                            long pcapNgHeaders,
                            long jsonHeaders)
            {
                this.udpPackets = udpPackets;
                this.droppedUdpPackets = droppedUdpPackets;
                this.pcapNgHeaders = pcapNgHeaders;
                this.jsonHeaders = jsonHeaders;
            }

            public long getUdpPackets()
            {
                return udpPackets;
            }

            public long getDroppedUdpPackets()
            {
                return droppedUdpPackets;
            }

            public long getPcapNgHeaders()
            {
                return pcapNgHeaders;
            }

            public long getJsonHeaders()
            {
                return this.jsonHeaders;
            }

        }

        @JsonPropertyOrder({ "dataPackets", "pcapNgHeaders", "bytes" })
        public static class FileStats
        {
            private long pcapNgHeaders;
            private long dataPackets;
            private long bytes;

            public FileStats(long pcapNgHeaders,
                             long dataPackets,
                             long bytes)
            {
                this.pcapNgHeaders = pcapNgHeaders;
                this.dataPackets = dataPackets;
                this.bytes = bytes;
            }

            public long getPcapNgHeaders()
            {
                return pcapNgHeaders;
            }

            public long getDataPackets()
            {
                return dataPackets;
            }

            public long getBytes()
            {
                return bytes;
            }
        }
    }

    private static Flux<DatagramPacket> receiveUdp(String host,
                                                   int port)
    {
        return UdpServer //
                        .create()
                        .host(host)
                        .port(port)
                        .bind()
                        .doOnNext(n -> log.info("Created UDP listening socket: {}", n.channel().localAddress()))
                        .<DatagramPacket>flatMapMany(c -> c//
                                                           .inbound()
                                                           .receiveObject()
                                                           .ofType(DatagramPacket.class)
                                                           .doOnNext(dp -> log.debug("Got DatagramPacket: {}", dp))
                                                           .doFinally(f -> c.dispose()));
    }

    static class Config
    {
        final int sinkPort;
        final int controlPort;
        private final String listenHost;
        private String pcapNgFilename;

        public Config(final String sinkHost,
                      final int sinkPort,
                      final int controlPort,
                      final String pcapNgFilename

        )
        {
            Objects.requireNonNull(sinkHost);
            Objects.requireNonNull(pcapNgFilename);
            if (controlPort < 0 || controlPort > 65535)
                throw new IllegalArgumentException("Invalid controlPort: " + controlPort);
            if (sinkPort < 0 || controlPort > 65535)
                throw new IllegalArgumentException("Invalid sinkPort: " + sinkPort);

            this.listenHost = sinkHost;
            this.sinkPort = sinkPort;
            this.controlPort = controlPort;
            this.pcapNgFilename = pcapNgFilename;
        }

        public String getListenHost()
        {
            return listenHost;
        }

        public String getPcapNgFilename()
        {
            return this.pcapNgFilename;
        }

        public int getSinkPort()
        {
            return sinkPort;
        }

        public int getControlPort()
        {
            return controlPort;
        }

        public static Config fromEnv()
        {
            final var env = System.getenv();
            final var sinkPort = Integer.parseInt(env.get("SINK_PORT"));
            final var controlPort = Integer.parseInt(env.get("CONTROL_PORT"));
            final var sinkHost = env.get("IP_VERSION").strip().equals("4") ? "0.0.0.0" : "::";

            final var pcapNgFilename = env.get("PCAPNG_FILENAME");

            return new Config(sinkHost, sinkPort, controlPort, pcapNgFilename);
        }

        @Override
        public String toString()
        {
            return String.format("Config [sinkPort=%s, controlPort=%s, listenHost=%s, pcapNgFilename=%s]", sinkPort, controlPort, listenHost, pcapNgFilename);
        }
    }
}
