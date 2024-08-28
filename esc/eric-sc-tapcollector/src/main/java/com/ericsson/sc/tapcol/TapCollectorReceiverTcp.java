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

package com.ericsson.sc.tapcol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.tapcol.TapCol.DivisionMethod;
import com.ericsson.sc.tapcol.pcap.ConnectionTransformer;
import com.ericsson.sc.tapcol.pcap.SstsProcessor;

import io.envoyproxy.envoy.data.tap.v3.TraceWrapper;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.FutureMono;
import reactor.netty.tcp.TcpServer;

/**
 * Receives captured packets via envoy's TCP interface.
 * 
 *
 */
public class TapCollectorReceiverTcp
{
    private static final Logger log = LoggerFactory.getLogger(TapCollectorReceiverTcp.class);
    private static final String IN_FRAMES = "tapcollector_in_tap_frames_total";
    private static final String IN_SERVER_ERROR_TOTAL = "tapcollector_in_server_error_total";
    private static final String SERVER_ERRORS = "reactor.netty.tcp.server.errors";
    private static final String SERVER_CONNECTIONS = "reactor.netty.tcp.server.connections.total";
    private static final String IN_SERVER_CONNECTIONS = "tapcollector_in_active_connections";
    private static final String IN_BYTES = "tapcollector_in_bytes";

    private final int port;
    private final SstsProcessor handler;
    private final TcpServer tcpServer;
    private final Mono<? extends DisposableServer> boundServer;

    private DefaultChannelGroup channelGroup;

    private Mono<Void> channelGroupMono;

    public TapCollectorReceiverTcp(int port,
                                   String host,
                                   PacketSink consumer,
                                   boolean traceInput,
                                   Flux<ConnectionTransformer> ctFlux,
                                   DivisionMethod divisionMethod,
                                   Integer chunkSize)
    {
        this.port = port;
        this.handler = new SstsProcessor(ctFlux);

        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.channelGroupMono = FutureMono.deferFuture(() -> this.channelGroup.newCloseFuture());

        // Define server counters
        PmMetrics metrics = PmMetrics.factory();
        metrics.addAccept(IN_FRAMES);
        metrics.addAccept(IN_SERVER_ERROR_TOTAL);
        metrics.addAccept(IN_SERVER_CONNECTIONS);
        metrics.addAccept(IN_BYTES);

        metrics.createCounter(IN_FRAMES, IN_FRAMES);
        metrics.createCounter(IN_BYTES, IN_BYTES);
        metrics.replaceWith(SERVER_ERRORS, IN_SERVER_ERROR_TOTAL);
        metrics.replaceWith(SERVER_CONNECTIONS, IN_SERVER_CONNECTIONS);

        this.tcpServer = TcpServer.create()
                                  .channelGroup(channelGroup) // Does not work
                                  .host(host) // listen on specific host
                                  .port(this.port)
                                  .metrics(true)
                                  .doOnBound(server -> log.info("Listening for incoming tap connections on {}:{}", server.host(), server.port()))
                                  .doOnChannelInit((observer,
                                                    channel,
                                                    remoteAddress) ->
                                  {
                                      channel.pipeline() //
                                             .addFirst(new ProtobufVarint32FrameDecoder(), new ProtobufDecoder(TraceWrapper.getDefaultInstance()));
                                      if (traceInput)
                                      {
                                          channel.pipeline() //
                                                 .addFirst(new LoggingHandler(LogLevel.INFO));
                                      }
                                  })
                                  .option(ChannelOption.AUTO_READ, false)
                                  .doOnConnection(connection ->
                                  {
                                      // Following line probably not needed because connections are added
                                      // automatically to channelgroup
                                      log.info("Connection created");
                                      channelGroup.add(connection.channel()); // Add new connection to ChannelGroup so that it can stop gracefully

                                      final Mono<Void> onRemotePeerClosedConnection = connection.onTerminate() //
                                                                                                .or(connection.onDispose())
                                                                                                .or(FutureMono.from(connection.channel()
                                                                                                                              .parent()
                                                                                                                              .closeFuture()))
                                                                                                .doOnEach(ev -> log.debug("onEachTerminateDispose: {}", ev));
                                      final var packetFlow = connection.inbound()
                                                                       .receiveObject()
                                                                       .ofType(TraceWrapper.class) // Ignore other types TODO: log error
                                                                       .filter(TraceWrapper::hasSocketStreamedTraceSegment)
                                                                       .map(TraceWrapper::getSocketStreamedTraceSegment)
                                                                       .map(segment ->
                                                                       {
                                                                           if (segment.hasEvent() && !segment.getEvent().hasClosed())
                                                                           {
                                                                               try
                                                                               {
                                                                                   metrics.increaseCounter(IN_BYTES, segment.getEvent().toByteArray().length);
                                                                                   metrics.increaseCounter(IN_FRAMES);
                                                                               }
                                                                               catch (Exception e)
                                                                               {
                                                                                   log.debug("Failed to increase counter", e);
                                                                               }
                                                                           }
                                                                           return segment;
                                                                       })
                                                                       .transform(traceFlux -> handler.processSsts(traceFlux, divisionMethod, chunkSize));

                                      final var packetHandlerResponse = consumer.consumePacketStream(packetFlow.takeUntilOther(onRemotePeerClosedConnection))
                                                                                .doOnError(err ->
                                                                                {
                                                                                    log.warn("Unexpected error while handling client {}", connection, err);
                                                                                });

                                      packetHandlerResponse.subscribe(connection.disposeSubscriber());
                                  })
                                  .metrics(true);
        this.boundServer = this.tcpServer.bind().cache();
    }

    public TcpServer getTcpServer()
    {
        return this.tcpServer;
    }

    public Mono<DisposableServer> start()
    {
        return this.boundServer.cast(DisposableServer.class);
    }

    public Mono<Void> stop()
    {
        return this.boundServer.doOnNext(DisposableServer::dispose)
                               .flatMap(DisposableServer::onDispose)
                               .then(this.channelGroupMono.doOnSubscribe(s -> log.info("Draining {} tap connections", this.channelGroup.size())));
    }
}
