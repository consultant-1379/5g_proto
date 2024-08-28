package com.ericsson.sc.tapcol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.tapcol.TapCol.DivisionMethod;
import com.ericsson.sc.tapcol.pcap.TapPacket;
import com.ericsson.utilities.test.PacketAnalyzer;
import com.google.protobuf.ByteString;

import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

public class TapCollectorReceiverTest
{
    private static final String STOP_TAPCOL = "Stopping tapcol receiver";
    private static final int TAPCOL_PORT = 0; // Use ephemeral port
    private static final int SLEEP_SECONDS = 5;
    private static final String IN_FRAMES = "tapcollector_in_tap_frames_total";
    private static final String IN_BYTES = "tapcollector_in_bytes";

    private static final Logger log = LoggerFactory.getLogger(TapCollectorReceiverTest.class);

    private TapcolTestBed tapcolTestBed;
    private String baseUri;
    private String tapCollectorIpAddress;

    @BeforeClass
    public void beforeClass() throws UnknownHostException
    {
        this.tapCollectorIpAddress = InetAddress.getLocalHost().getHostAddress();
        this.baseUri = "/test/";
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider()
    {
        // Set the number of packets per stream and the number of parallel streams:

        return new Object[][] { { Integer.valueOf(1), Integer.valueOf(1) },
                                { Integer.valueOf(1), Integer.valueOf(3) },
                                { Integer.valueOf(3), Integer.valueOf(1) },
                                { Integer.valueOf(10), Integer.valueOf(3) } //
        };
    }

    @Test(enabled = true, groups = "functest", dataProvider = "data-provider")
    public void multiShotTraffic(Integer numOfPacketsPerStream,
                                 Integer numOfStreams) throws UnsupportedOperationException, IOException, InterruptedException
    {
        final var packetAnalyzer = new PacketAnalyzer(false);

        final PmMetrics metrics = PmMetrics.factory();
        final PrometheusMeterRegistry registry = metrics.getRegistry();
        Counter inBytes = null;
        Counter inFrames = null;
        final AtomicInteger bytesCounter = new AtomicInteger(0);
        final AtomicInteger frames = new AtomicInteger(0);

        final var tapcolReceiver = new TapCollectorReceiverTcp(TAPCOL_PORT, //
                                                               tapCollectorIpAddress,
                                                               new PacketSink()
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
                                                               }, //
                                                               false,
                                                               Flux.empty(),
                                                               DivisionMethod.TRUNCATE,
                                                               300);

        final var tapcolTcpServer = tapcolReceiver.start().block();

        final var tapcolListeningPort = tapcolTcpServer.port();

        this.tapcolTestBed = new TapcolTestBed(this.tapCollectorIpAddress, tapcolListeningPort, false);

        this.sendPackets(numOfPacketsPerStream, numOfStreams).blockLast();
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        ; // Ensure that envoy has sent all packets TODO: Perhaps we could detect the
          // termination of all tap streams

        log.info(STOP_TAPCOL);
        tapcolReceiver.stop().block();

        final var clientIpAddr = this.tapcolTestBed.getGatewayIpAddress();
        final var envoyContainerIpAddr = this.tapcolTestBed.getContainerIpAddress();

        this.tapcolTestBed.close();
        packetAnalyzer.analyze();

        try
        {
            inFrames = (Counter) registry.find(IN_FRAMES).counter();
            if (inFrames == null)
                throw new Exception(IN_FRAMES + " counter is missing");
            inBytes = (Counter) registry.find(IN_BYTES).counter();
            if (inBytes == null)
                throw new Exception(IN_BYTES + " counter is missing");

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

        assertTrue(packetAnalyzer.validateIpAddresses(clientIpAddr, envoyContainerIpAddr));
        assertTrue(packetAnalyzer.validateSequenceNumbers());
        var packetUris = this.getUris(numOfPacketsPerStream);
        log.info("Validate URIS {} {}", baseUri, packetUris);
        assertTrue(packetAnalyzer.validateUris(this.baseUri, packetUris));

        assertEquals(packetAnalyzer.getNumOfConnections(), numOfStreams.intValue());
        assertEquals(packetAnalyzer.getNumOfPacketsPerConnection(), numOfPacketsPerStream.intValue());
    }

    @Test(enabled = true, groups = "functest", dataProvider = "data-provider")
    public void multiShotTrafficIp6(Integer numOfPacketsPerStream,
                                    Integer numOfStreams) throws UnsupportedOperationException, IOException, InterruptedException
    {
        final var packetAnalyzer = new PacketAnalyzer(true);

        final PmMetrics metrics = PmMetrics.factory();
        final PrometheusMeterRegistry registry = metrics.getRegistry();
        Counter inBytes = null;
        Counter inFrames = null;
        final AtomicInteger bytesCounter = new AtomicInteger(0);
        final AtomicInteger frames = new AtomicInteger(0);

        final var tapcolReceiver = new TapCollectorReceiverTcp(9000, //
                                                               "::",
                                                               new PacketSink()
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
                                                               }, //
                                                               false,
                                                               Flux.empty(),
                                                               DivisionMethod.TRUNCATE,
                                                               300);

        final var tapcolTcpServer = tapcolReceiver.start().block();

        final var tapcolListeningPort = tapcolTcpServer.port();

        this.tapcolTestBed = new TapcolTestBed("[0:0:0:0]", tapcolListeningPort, true);

        this.sendPackets(numOfPacketsPerStream, numOfStreams).blockLast();
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        ; // Ensure that envoy has sent all packets TODO: Perhaps we could detect the
          // termination of all tap streams

        log.info(STOP_TAPCOL);
        tapcolReceiver.stop().block();

        final var clientIpAddr = this.tapcolTestBed.getGatewayIpAddress();
        final var envoyContainerIpAddr = this.tapcolTestBed.getContainerIpAddress();

        this.tapcolTestBed.close();
        packetAnalyzer.analyze();

        try
        {
            inFrames = (Counter) registry.find(IN_FRAMES).counter();
            if (inFrames == null)
                throw new Exception(IN_FRAMES + " counter is missing");
            inBytes = (Counter) registry.find(IN_BYTES).counter();
            if (inBytes == null)
                throw new Exception(IN_BYTES + " counter is missing");

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

        assertTrue(packetAnalyzer.validateIpAddresses(clientIpAddr, envoyContainerIpAddr));
        assertTrue(packetAnalyzer.validateSequenceNumbers());
        var packetUris = this.getUris(numOfPacketsPerStream);
        log.info("Validate URIS {} {}", baseUri, packetUris);
        assertTrue(packetAnalyzer.validateUris(this.baseUri, packetUris));

        assertEquals(packetAnalyzer.getNumOfConnections(), numOfStreams.intValue());
        assertEquals(packetAnalyzer.getNumOfPacketsPerConnection(), numOfPacketsPerStream.intValue());
    }

    private Flux<HttpClientResponse> sendPackets(int num,
                                                 int parallel)
    {
        return Flux.merge(IntStream.range(0, parallel).mapToObj(i -> sendPacket(num)).collect(Collectors.toUnmodifiableList()));
    }

    private Flux<HttpClientResponse> sendPacket(int count)
    {
        ConnectionProvider cp = ConnectionProvider.create("cp", 1);
        final var client = HttpClient.create(cp)
                                     .protocol(HttpProtocol.HTTP11)
                                     .disableRetry(true)
                                     .keepAlive(true)
                                     .headers(h -> h.add("user-agent", "reactor"))
                                     .port(this.tapcolTestBed.getTargetPort())
                                     .host(this.tapcolTestBed.getTargetIpAddress())
                                     .baseUrl("/")
                                     .get();

        return Flux.range(0, count)
                   .concatMap(tick -> client.uri(this.baseUri + tick)
                                            .responseConnection((r,
                                                                 c) -> Mono.just(r))
                                            .doOnNext(response -> log.info("" + response.status() + "for " + response.uri())))
                   .doOnError(e -> log.error("Error response received", e))
                   .doFinally(ss -> cp.dispose());
    }

    private List<String> getUris(int count)
    {
        Integer tick = 0;
        List<String> urisList = new ArrayList<>();

        while (tick < count)
        {
            urisList.add(this.baseUri.concat(tick.toString()));
            tick++;
        }

        return urisList;
    }

}