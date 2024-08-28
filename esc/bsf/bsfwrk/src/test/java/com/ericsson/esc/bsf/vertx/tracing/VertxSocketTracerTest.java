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
package com.ericsson.esc.bsf.vertx.tracing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.sockettrace.RxSocketTraceHandler;
import com.ericsson.sc.sockettrace.RxSocketTraceHandler.SegmentType;
import com.ericsson.sc.sockettrace.TapcolTraceSink;
import com.ericsson.sc.tapcol.PacketSink;
import com.ericsson.sc.tapcol.TapCollectorReceiverTcp;
import com.ericsson.sc.tapcol.TapCol.DivisionMethod;
import com.ericsson.sc.tapcol.pcap.TapPacket;
import com.ericsson.sc.vertx.trace.VertxSocketTracer;
import com.ericsson.sc.vertx.trace.VertxSocketTracer.NettySocketTraceEvent.EventType;
import com.ericsson.utilities.http.HelperHttp;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxBuilder;
import com.ericsson.utilities.test.PacketAnalyzer;
import com.google.protobuf.ByteString;
import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

public class VertxSocketTracerTest
{
    private static final Logger log = LoggerFactory.getLogger(VertxSocketTracerTest.class);
    private static final String LOCAL_HOST = "127.0.0.50";
    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();
    private final int localPort = 1111;

    private TapCollectorReceiverTcp tapcolReceiver;
    private RxSocketTraceHandler socketTraceHandler;
    private TapcolTraceSink traceSink;

    private static final String IN_FRAMES = "tapcollector_in_tap_frames_total";
    private static final String IN_BYTES = "tapcollector_in_bytes";

    private final PacketAnalyzer packetAnalyzer = new PacketAnalyzer(false);
    private Flowable<String> nfInstanceFlow = Flowable.just("bsf");

    @AfterMethod
    private void afterMethod()
    {
        this.tapcolReceiver.stop().block();
    }

    @AfterClass
    private void cleanup() throws InterruptedException, IOException
    {
        log.info("Cleaning up {}, closing vertx", VertxSocketTracerTest.class);
        vertx.close();
        this.deleteTmpPcaps();
        log.info("Vertx closed, cleanups completed");
    }

    @Test(enabled = true)
    private void httpServerTraceOff() throws IOException, InterruptedException
    {
        this.setup("127.0.0.2", 9998);

        final var numOfPackets = 10;
        final var server = WebServer.builder() //
                                    .withHost(LOCAL_HOST)
                                    .withPort(localPort)
                                    .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(100))
                                    .build(vertx, 1);

        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end("Non-TLS HTTP11 response");
        })));
        server.startListener().blockingAwait();
        assertServerUp();

        sendPackets(numOfPackets, 1).blockLast();

        server.stopListener().blockingAwait();
        try
        {
            assertions(numOfPackets, 1);

        }
        catch (IllegalArgumentException e)
        {
            assertEquals(e.getMessage(), "No packets sent");
        }

        this.validateCounters(0, 0);
    }

    @Test(enabled = true)
    private void httpServerTraceOn() throws InterruptedException, IOException
    {
        this.setup("127.0.0.1", 9999);

        final var numOfPackets = 10;

        final var server = WebServer.builder() //
                                    .withHost(LOCAL_HOST)
                                    .withPort(localPort)
                                    .withGlobalTracing(true)
                                    .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(100))
                                    .build(vertx, 1);

        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end("Non-TLS HTTP11 response");
        })));
        server.startListener().blockingAwait();
        assertServerUp();

        sendPackets(numOfPackets, 1).blockLast();

        TimeUnit.SECONDS.sleep(5);

        server.stopListener().blockingAwait();

        assertions(numOfPackets, 1);

        this.validateCounters(numOfPackets, 1);
    }

    @Test(enabled = true, groups = "functest")
    private void httpStressTraceOn() throws IOException, InterruptedException
    {
        final var bsfNfInstance = String.format("bsf%s", new Random().nextInt());

        this.nfInstanceFlow = Flowable.just(bsfNfInstance);

        final var bytesCounter = new AtomicInteger(0);
        final var frames = new AtomicInteger(0);

        final var fps = new PacketSink()
        {
            @Override
            public Mono<Void> consumePacketStream(Flux<List<TapPacket>> inFlow)
            {
                return inFlow.doOnNext(tappacket ->
                {
                    bytesCounter.addAndGet(tappacket.get(0).getPacket().getRawData().length);
                    frames.addAndGet(1);
                })

                             .then();
            }
        };

        final var tapcolReceiverStress = new TapCollectorReceiverTcp(9997, //
                                                                     "127.0.0.3",
                                                                     fps, //
                                                                     false,
                                                                     Flux.empty(),
                                                                     DivisionMethod.TRUNCATE,
                                                                     61440);

        tapcolReceiverStress.start().block();

        this.traceSink = new TapcolTraceSink(vertx, "127.0.0.3", 9997, this.nfInstanceFlow);
        this.traceSink.init().blockingAwait();
        this.socketTraceHandler = new RxSocketTraceHandler(this.traceSink, 1000, 1000, this.nfInstanceFlow);
        VertxSocketTracer.setGlobalTracer(this.socketTraceHandler);

        // edit these values to stress
        final var numOfPackets = 50000;
        final var numOfConnections = 10;

        final var server = WebServer.builder() //
                                    .withHost(LOCAL_HOST)
                                    .withPort(localPort)
                                    .withGlobalTracing(true)
                                    .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(100))
                                    .build(vertx, 1);

        server.childRouters().forEach(routerCfg -> routerCfg.configureRouter(router -> router.get().handler(rc ->
        {
            rc.response().end("Non-TLS HTTP11 response");
        })));
        server.startListener().blockingAwait();
        assertServerUp();

        sendPackets(numOfPackets, numOfConnections).blockLast();

        TimeUnit.SECONDS.sleep(2);

        server.stopListener().blockingAwait();
        this.validateCounters(numOfPackets, numOfConnections);

        tapcolReceiverStress.stop().block();
    }

    private void setup(final String tapcolIpAddress,
                       final int tapcolPort)
    {
        final var bytesCounter = new AtomicInteger(0);
        final var frames = new AtomicInteger(0);

        final var fps = new PacketSink()
        {
            @Override
            public Mono<Void> consumePacketStream(Flux<List<TapPacket>> inFlow)
            {
                return inFlow.doOnNext(tappacket ->
                {
                    bytesCounter.addAndGet(tappacket.get(0).getPacket().getRawData().length);
                    frames.addAndGet(1);
                })
                             .doOnNext(tappacket -> log.info("Got packet: \n------------->\n{}\n<--------------",
                                                             ByteString.copyFrom(tappacket.get(0)
                                                                                          .getPacket()
                                                                                          .getPayload()
                                                                                          .getPayload()
                                                                                          .getPayload()
                                                                                          .getRawData())
                                                                       .toStringUtf8()))
                             .doOnNext(tappacket -> log.info("Details: \n- Header: \n {}- Payload: \n {}",
                                                             tappacket.get(0).getPacket().getHeader(),
                                                             tappacket.get(0).getPacket().getPayload()))
                             .doOnNext(tappacket -> packetAnalyzer.put(tappacket.get(0).getPacket()))
                             .then();
            }
        };

        this.tapcolReceiver = new TapCollectorReceiverTcp(tapcolPort, //
                                                          tapcolIpAddress,
                                                          fps, //
                                                          false,
                                                          Flux.empty(),
                                                          DivisionMethod.TRUNCATE,
                                                          61440);

        tapcolReceiver.start().block();

        final var bsfNfInstance = String.format("bsf%s", new Random().nextInt());

        this.nfInstanceFlow = Flowable.just(bsfNfInstance);

        this.traceSink = new TapcolTraceSink(vertx, tapcolIpAddress, tapcolPort, this.nfInstanceFlow);
        this.traceSink.init().blockingAwait();
        this.socketTraceHandler = new RxSocketTraceHandler(this.traceSink, 1000, 61440, this.nfInstanceFlow);
        VertxSocketTracer.setGlobalTracer(this.socketTraceHandler);
    }

    private void validateCounters(final int numOfPackets,
                                  final int numOfConnections)
    {
        log.info("Validating counters");

        assertEquals(this.socketTraceHandler.getCounters().getCcVtapSegmentsDropped(SegmentType.CONNECTION, this.traceSink.getNfInstance()).get(), 0);
        assertEquals(this.socketTraceHandler.getCounters().getCcVtapSegmentsDropped(SegmentType.EVENT, this.traceSink.getNfInstance()).get(), 0);

        assertEquals(this.socketTraceHandler.getCounters().getCcVtapSegmentsDroppedSizeBig(SegmentType.CONNECTION, this.traceSink.getNfInstance()).get(), 0);
        assertEquals(this.socketTraceHandler.getCounters().getCcVtapSegmentsDroppedSizeBig(SegmentType.EVENT, this.traceSink.getNfInstance()).get(), 0);

        assertEquals(traceSink.getCounters().getCcVtapSegmentsTapped(SegmentType.CONNECTION, this.traceSink.getNfInstance()).get(), numOfConnections);
        assertEquals(traceSink.getCounters().getCcVtapSegmentsTapped(SegmentType.EVENT, this.traceSink.getNfInstance()).get(),
                     numOfConnections * (2 * numOfPackets + 1));

        assertEquals(traceSink.getCounters().getCcVtapEventsTapped(EventType.WRITE, this.traceSink.getNfInstance()).get(), numOfPackets * numOfConnections);
        assertEquals(traceSink.getCounters().getCcVtapEventsTapped(EventType.READ, this.traceSink.getNfInstance()).get(), numOfPackets * numOfConnections);
        assertEquals(traceSink.getCounters().getCcVtapEventsTapped(EventType.CLOSED, this.traceSink.getNfInstance()).get(), numOfConnections);
        assertEquals(traceSink.getCounters().getGaugeVtapOpenTraceSessions(this.traceSink.getNfInstance()).get(), 0);
    }

    private void assertServerUp()
    {
        assertFalse(HelperHttp.isPortAvailable(localPort, LOCAL_HOST));
    }

    private void deleteTmpPcaps() throws IOException
    {
        Files.list(Path.of("/tmp")).filter(file -> file.getFileName().toString().contains("tapcol")).forEach(f ->
        {
            try
            {
                Files.deleteIfExists(f);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e.toString());
            }
        });
    }

    private void assertions(final int numOfPacketsPerStream,
                            final int numOfStreams) throws InterruptedException
    {
        final var baseUri = "/test/";

        final PmMetrics metrics = PmMetrics.factory();
        final PrometheusMeterRegistry registry = metrics.getRegistry();
        Counter inBytes = null;
        Counter inFrames = null;

        packetAnalyzer.analyze();

        try
        {
            inFrames = (Counter) registry.find(IN_FRAMES).counter();
            if (inFrames == null)
            {
                throw new RuntimeException(IN_FRAMES + " counter is missing");
            }
            inBytes = (Counter) registry.find(IN_BYTES).counter();
            if (inBytes == null)
            {
                throw new RuntimeException(IN_BYTES + " counter is missing");
            }

        }
        catch (Exception e)
        {
            log.error("Failed to get counter.", e);
            registry.forEachMeter(meter -> log.error(meter.getId().getName()));
            assertTrue(false);
        }
        finally
        {
            registry.clear();
        }

        assertTrue(packetAnalyzer.validateIpAddresses("127.0.0.1", LOCAL_HOST));
        assertTrue(packetAnalyzer.validateSequenceNumbers());
        final var packetUris = this.getUris(numOfPacketsPerStream);
        log.info("Validate URIS {} {}", baseUri, packetUris);
        assertTrue(packetAnalyzer.validateUris(baseUri, packetUris));

        assertEquals(packetAnalyzer.getNumOfConnections(), numOfStreams);
        assertEquals(packetAnalyzer.getNumOfPacketsPerConnection(), numOfPacketsPerStream);
    }

    private List<String> getUris(int count)
    {
        final var baseUri = "/test/";
        Integer tick = 0;
        List<String> urisList = new ArrayList<>();

        while (tick < count)
        {
            urisList.add(baseUri.concat(tick.toString()));
            tick++;
        }

        return urisList;
    }

    private Flux<HttpClientResponse> sendPackets(int num,
                                                 int parallel)
    {
        return Flux.merge(IntStream.range(0, parallel).mapToObj(i -> sendPacket(num)).collect(Collectors.toUnmodifiableList()));
    }

    private Flux<HttpClientResponse> sendPacket(int count)
    {

        final var baseUri = "/test/";
        final var cp = ConnectionProvider.create("cp", 1);
        final var client = HttpClient.create(cp)
                                     .protocol(HttpProtocol.HTTP11)
                                     .disableRetry(true)
                                     .keepAlive(true)
                                     .headers(h -> h.add("user-agent", "reactor"))
                                     .port(localPort)
                                     .host(LOCAL_HOST)
                                     .baseUrl("/")
                                     .get();

        return Flux.range(0, count)
                   .concatMap(tick -> client.uri(baseUri + tick)
                                            .responseConnection((r,
                                                                 c) -> Mono.just(r))
                                            .doOnNext(response -> log.info("" + response.status() + "for " + response.uri())))
                   .doOnError(e -> log.error("Error response received", e))
                   .doFinally(ss -> cp.dispose());
    }

}
