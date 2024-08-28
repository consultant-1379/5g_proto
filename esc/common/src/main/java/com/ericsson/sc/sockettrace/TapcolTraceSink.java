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

import com.ericsson.sc.sockettrace.RxSocketTraceHandler.SegmentType;
import com.ericsson.sc.vertx.trace.VertxSocketTracer.NettySocketTraceEvent.EventType;
import com.ericsson.utilities.reactivex.RetryFunction;
import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import io.envoyproxy.envoy.data.tap.v3.TraceWrapper;
import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.prometheus.client.Gauge;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.NetClient;
import io.vertx.reactivex.core.net.NetSocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TapcolTraceSink implements TraceSink
{
    private static final Logger log = LoggerFactory.getLogger(TapcolTraceSink.class);

    private static final long RETRY_DELAY_MILLIS = 1000;
    private static final long NUM_OF_RETRIES = 100;
    private final Counters counters = new Counters();

    enum State

    {
        IDLE,
        CONNECTING,
        CONNECTED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
    private final AtomicReference<Single<NetSocket>> tcpSocket = new AtomicReference<>();
    private final AtomicReference<Optional<String>> nfInstance = new AtomicReference<>();
    private final AtomicInteger activeTraceSessions = new AtomicInteger();

    private final NetClient tcpClient;
    private final int port;
    private final String host;

    public TapcolTraceSink(final Vertx vertx,
                           final String host,
                           final int port,
                           final Flowable<String> nfInstance)
    {
        this.port = port;
        this.host = host;
        tcpClient = vertx.createNetClient();
        nfInstance.subscribe(this::setNfInstance, err -> log.error("Stopped updating nfInstance on socket trace handler with error: ", err));
    }

    @Override
    public Completable consumeTrace(final Flowable<SocketStreamedTraceSegment> traceFlow)
    {
        return traceFlow.concatMapCompletable(ssts -> send(ssts) //
                                                                .doOnError(err -> log.warn("Send error, reconnecting", err))
                                                                .doOnError(err -> reconnect()))
                        .doOnError(err -> log.error("Stop sending traces", err));
    }

    private Single<NetSocket> createNewConnection()
    {
        final var newConnection = this.tcpClient //
                                                .rxConnect(port, host)
                                                .doOnSuccess(s ->
                                                {
                                                    this.state.set(State.CONNECTED);
                                                    log.info("Connection has been established");
                                                })
                                                .retryWhen(new RetryFunction().withRetries(NUM_OF_RETRIES)
                                                                              .withDelay(RETRY_DELAY_MILLIS)
                                                                              .withRetryAction((err,
                                                                                                retry) -> log.error("Could not connect to tapcollector, retrying {}",
                                                                                                                    retry))
                                                                              .create())
                                                .cache(); // caches only the success value due to retry

        newConnection.subscribe(c -> log.debug("Establishing new connection to the tapcollector"),
                                err -> log.error("Error while establishing new connection to the tapcollector", err));
        return newConnection;
    }

    public Completable init()
    {
        return Completable.defer(() ->
        {
            if (this.state.compareAndSet(State.IDLE, State.CONNECTING))
            {
                log.debug("Connection set from idle to connecting");

                final var newConnection = this.createNewConnection();
                this.tcpSocket.getAndSet(newConnection);
                return newConnection.ignoreElement();
            }
            else
            {
                throw new IllegalStateException("Could not connect to tapcollector due to existing connection"); // this should never happen
            }
        });
    }

    void reconnect()
    {
        this.state.set(State.CONNECTING);

        final var newConnection = this.createNewConnection();
        final var previous = Optional.ofNullable(this.tcpSocket.getAndSet(newConnection));

        previous.ifPresent(pr -> pr.flatMapCompletable(NetSocket::rxClose)
                                   .subscribe(() -> log.debug("Closing previous connection to tapcollector"),
                                              err -> log.error("Error while closing previous connection to tapcollector", err)));
    }

    private Completable send(final SocketStreamedTraceSegment ssts)
    {
        return this.tcpSocket.get().flatMapCompletable(socket -> socket.rxWrite(this.toBuffer(ssts)).doOnComplete(() ->
        {
            log.debug("Successfully tapped SSTS: {}", ssts);
            this.extractSstsInfo(ssts);
        }));
    }

    private final Buffer toBuffer(final SocketStreamedTraceSegment ssts) throws IOException
    {
        final var baos = new ByteArrayOutputStream();
        TraceWrapper.newBuilder().setSocketStreamedTraceSegment(ssts).build().writeDelimitedTo(baos);
        return Buffer.buffer(baos.toByteArray());
    }

    /**
     * Extracts the type of the current SSTS and steps the relevant counters
     * accordingly. The SSTS can be a connection or an event. In case it is an
     * event, the type can be read, write and close.
     * 
     * @param ssts The SocketStreamedTraceSegment.
     */
    private void extractSstsInfo(final SocketStreamedTraceSegment ssts)
    {
        if (ssts.hasConnection())
        {
            this.stepVtapSegmentsTappedCounter(SegmentType.CONNECTION, this.getNfInstance());

            this.updateVtapOpenTraceSessionsGauge(this.activeTraceSessions.incrementAndGet(), this.getNfInstance());
        }
        else
        {
            this.stepVtapSegmentsTappedCounter(SegmentType.EVENT, this.getNfInstance());

            final var event = ssts.getEvent();

            if (event.hasRead())
            {
                this.stepVtapEventsTappedCounter(EventType.READ, this.getNfInstance());
            }
            else if (event.hasWrite())
            {
                this.stepVtapEventsTappedCounter(EventType.WRITE, this.getNfInstance());
            }
            else if (event.hasClosed())
            {
                this.stepVtapEventsTappedCounter(EventType.CLOSED, this.getNfInstance());
                this.updateVtapOpenTraceSessionsGauge(this.activeTraceSessions.decrementAndGet(), this.getNfInstance());
            }
            else
            {
                throw new IllegalArgumentException("Invalid SSTS event type"); // this should never happen
            }

        }
    }

    /**
     * Get the connection state.
     * 
     * @return the state.
     */
    State getState()
    {
        return this.state.get();
    }

    /**
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

    private void stepVtapSegmentsTappedCounter(final SegmentType segmentType,
                                               final String nfInstance)
    {
        log.debug("Stepping segments tapped counter, eventType: {}, nfInstance: {}", segmentType, nfInstance);

        this.counters.getCcVtapSegmentsTapped(segmentType, nfInstance).inc();
    }

    private void stepVtapEventsTappedCounter(final EventType segmentType,
                                             final String nfInstance)
    {
        log.debug("Stepping events tapped counter, eventType: {}, nfInstance: {}", segmentType, nfInstance);

        this.counters.getCcVtapEventsTapped(segmentType, nfInstance).inc();
    }

    private void updateVtapOpenTraceSessionsGauge(final int value,
                                                  final String nfInstance)
    {
        log.debug("Updating open trace sessions counter with value {}, nfInstance: {}", value, nfInstance);

        this.counters.getGaugeVtapOpenTraceSessions(nfInstance).set(value);
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
        private static final String EVENT_TYPE_LBL = "event_type";

        /**
         * Prometheus CC counter for total segments tapped.
         */
        private static final Counter ccVtapSegmentsTapped = Counter.build()
                                                                   .namespace(BSF_NF_NAME)
                                                                   .name("vtap_segments_tapped_total")
                                                                   .labelNames(SEGMENT_TYPE_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                   .help("Number of BSF segments successfully tapped")
                                                                   .register();

        public Child getCcVtapSegmentsTapped(final SegmentType segmentType,
                                             final String nfInstance)
        {
            return ccVtapSegmentsTapped.labels(segmentType.toString().toLowerCase(), // segment_type
                                               nfInstance, // nf_instance
                                               BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for total events tapped.
         */
        private static final Counter ccVtapEventsTapped = Counter.build()
                                                                 .namespace(BSF_NF_NAME)
                                                                 .name("vtap_events_tapped_total")
                                                                 .labelNames(EVENT_TYPE_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                 .help("Number of BSF events successfully tapped")
                                                                 .register();

        public Child getCcVtapEventsTapped(final EventType eventType,
                                           final String nfInstance)
        {
            return ccVtapEventsTapped.labels(eventType.toString().toLowerCase(), // event_type
                                             nfInstance, // nf_instance
                                             BSF_NF_NAME); // nf
        }

        // Gauge counter

        /**
         * Prometheus gauge counter for open trace sessions.
         */
        private static final Gauge gaugeVtapOpenTraceSessions = Gauge.build()
                                                                     .namespace(BSF_NF_NAME)
                                                                     .name("vtap_open_trace_sessions")
                                                                     .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                     .help("Number of currently open BSF trace sessions")
                                                                     .register();

        public Gauge.Child getGaugeVtapOpenTraceSessions(final String nfInstance)
        {
            return gaugeVtapOpenTraceSessions.labels(nfInstance, // nf_instance
                                                     BSF_NF_NAME); // nf
        }

    }

}
