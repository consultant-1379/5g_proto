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
package com.ericsson.sc.tapcol;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.metrics.PmMetrics;
import com.ericsson.sc.metrics.PrometheusClient;
import com.ericsson.sc.probebroker.PvtbPacketSink;
import com.ericsson.sc.probebroker.PvtbPacketSink.PvtbStreamHeader;
import com.ericsson.sc.tapcol.pcap.ConnectionTransformer;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.logger.LogLevelChanger;

import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TapCol
{
    private static final Logger log = LoggerFactory.getLogger(TapCol.class);
    private static final PmMetrics metrics = PmMetrics.factory();
    private final Mono<Void> run;
    private final Optional<TapcollectorServiceWatcher> serviceWatcher;

    private static final String PACKET_DROPPED = "tapcollector_out_tap_frames_dropped";
    private static final String BUFFER_SIZE = "tapcollector_buffer_size";
    private static final String ENV_PVTB_DOMAIN = "PVTB_DOMAIN";
    private static final String NF = "nf";
    private static final String APP = "app";
    private static final String ENV_SERVICE_ID = "SERVICE_ID";
    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_PM_CLIENT_HOST = "PM_CLIENT_HOST";
    private static final String ENV_PM_CLIENT_PORT = "PM_CLIENT_PORT";
    private static final String ENV_PM_PATH = "PM_PATH";
    private static final String ENV_DIVISION_METHOD = "DIVISION_METHOD";
    private static final String ENV_CHUNK_SIZE_LIMIT = "CHUNK_SIZE_LIMIT";

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/worker/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    public enum DivisionMethod
    {
        SPLIT("split"),
        TRUNCATE("truncate");

        private final String value;

        DivisionMethod(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public static DivisionMethod fromValue(String value)
        {
            for (DivisionMethod mode : DivisionMethod.values())
            {
                if (mode.getValue().equalsIgnoreCase(value))
                {
                    return mode;
                }
            }
            return TRUNCATE; // Default value if the provided value doesn't match any enum value
        }
    }

    TapCol(Mono<Void> onShutdown,
           Config cfg)
    {
        this.serviceWatcher = cfg.getConnectionTransformerConfig() //
                                 .map(ctCfg -> new TapcollectorServiceWatcher(ctCfg.namespace, ctCfg.svcLabelSelector, ctCfg.pollingPeriod));
        metrics.addDefaultTag(NF, System.getenv(ENV_PVTB_DOMAIN).split("-")[1]);
        metrics.addDefaultTag(APP, System.getenv(ENV_SERVICE_ID));
        metrics.addAccept(PACKET_DROPPED);
        metrics.createGauge(BUFFER_SIZE, 0, BUFFER_SIZE);
        metrics.createCounter(PACKET_DROPPED, PACKET_DROPPED);
        final var size = new BufferWatcher();
        final var pvtbSink = new PvtbPacketSink(cfg.getPvtbHost(), cfg.getPvtbPort(), cfg.getHeader());

        final var backpressuredPvtbSink = cfg.getStreamBufferSize().map(i -> pvtbSink.modifyInput(rf -> rf.onBackpressureBuffer(i, pk ->
        {
            try
            {
                metrics.increaseCounter(PACKET_DROPPED);
            }
            catch (Exception e)
            {
                log.debug("Failed to increase counter " + PACKET_DROPPED, e);
            }
        }, BufferOverflowStrategy.DROP_LATEST).doOnSubscribe(sub -> size.setScannable(Scannable.from(sub)))));

        final var receiver = new TapCollectorReceiverTcp(cfg.getListeningPort(),
                                                         cfg.getListeningAddress(),
                                                         backpressuredPvtbSink.orElse(pvtbSink),
                                                         cfg.isTraceInput(),
                                                         this.serviceWatcher.map(TapcollectorServiceWatcher::transFlux)
                                                                            .orElse(Flux.just(ConnectionTransformer.builder().build())),
                                                         cfg.divisionMethod,
                                                         cfg.chunkSizeOfPackets);
        final var shutdown = Mono.empty() //
                                 .then(receiver.stop().doOnSubscribe(s -> log.info("Stopping tap server"))) //
                                 .then(pvtbSink.stop().doOnSubscribe(s -> log.info("Stopping pvtb sink")))
                                 .doOnSuccess(suc -> this.serviceWatcher.ifPresent(TapcollectorServiceWatcher::dispose))
                                 .then(Mono.<Void>empty().doOnSubscribe(s -> log.info("Graceful shutdown complete")))
                                 .doOnSubscribe(s -> log.info("Starting graceful shutdown"));

        this.run = this.serviceWatcher.map(TapcollectorServiceWatcher::onStarted)
                                      .orElse(Mono.empty())
                                      .then(pvtbSink.start()
                                                    .mergeWith(receiver.start().then())
                                                    .takeUntilOther(onShutdown) //
                                                    .then(onShutdown)
                                                    .then(shutdown));
    }

    public static void main(String[] args)
    {
        log.info("Starting TAP collector");
        final var cfg = Config.fromEnv();
        log.info("Configuration: {}", cfg);
        var exitStatus = 0;

        try (final var hook = new ReactiveShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {

            // Mono.just("").delayElement(Duration.ofSeconds(10)).then()
            final var manager = new TapCol(hook.get(), cfg);
            String pmClientPort = System.getenv(ENV_PM_CLIENT_PORT);
            String pmPath = System.getenv(ENV_PM_PATH);
            if (pmClientPort == null || pmClientPort.equals(""))
                pmClientPort = "9091";
            File ipv6 = new File("/proc/net/if_inet6");
            String pmServerHost = (ipv6.exists() && ipv6.length() > 0) ? "::" : "0.0.0.0";
            if (pmPath == null || pmPath.equals(""))
                pmPath = "/stats/prometheus";
            new PrometheusClient(pmServerHost, Integer.parseInt(pmClientPort), metrics.getRegistry(), pmPath).run();
            manager.run.block();
        }
        catch (Exception e)
        {
            log.error("Terminated with unexpected error", e);
            exitStatus = 1;
        }

        log.info("Stopped");
        System.exit(exitStatus);
    }

    private static class BufferWatcher
    {
        final AtomicLong size = new AtomicLong();
        private Scannable scan;
        final Flux<Long> dropFlux = Flux.interval(Duration.ofSeconds(5)).map(tick ->
        {
            if (scan != null)
            {
                try
                {
                    if (scan.scan(Scannable.Attr.BUFFERED) != null)
                        metrics.setGaugeValue(BUFFER_SIZE, scan.scan(Scannable.Attr.BUFFERED));
                    else
                        return 0l;
                }
                catch (Exception e)
                {
                    log.debug("Failed to increase counter " + BUFFER_SIZE, e);
                }
                return new AtomicLong(scan.scan(Scannable.Attr.BUFFERED)).get();
            }
            return 0L;
        }).filter(p -> !p.equals(0L)).distinctUntilChanged();
        final Disposable disp;

        public BufferWatcher()
        {
            disp = dropFlux.subscribe(numDropped -> log.debug("Buffer size {} packets", size), err -> log.error("Unexpected error", err));
        }

        public void setScannable(Scannable scan)
        {
            this.scan = scan;
        }
    }

    private static class Config
    {
        public static final Integer DEFAULT_MAX_BUFFERED_PACKETS_PER_CONNECTION = 20000;
        public static final boolean DEFAULT_TRACE_INPUT = false;
        private static final String ENV_PVTB_PROTOCOL = "PVTB_PROTOCOL";
        private static final String ENV_PVTB_PORT = "PVTB_PORT";
        private static final String ENV_PVTB_HOST = "PVTB_HOST";
        private PvtbStreamHeader header;
        private String pvtbHost;
        private int pvtbPort;
        private String listeningAddress;
        private int listeningPort;
        private Optional<Integer> streamBufferSize;
        private boolean traceInput;
        private Optional<ConnectionTransformerConfig> connectionTransformerConfig;
        private DivisionMethod divisionMethod;
        private Integer chunkSizeOfPackets;

        private static String decideListeningIP() throws SocketException
        {
            List<InetAddress> address = new ArrayList<>();

            NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(i -> i.getInetAddresses().asIterator().forEachRemaining(address::add));

            return address.stream() //
                          .filter(Inet6Address.class::isInstance)
                          .findAny()
                          .map(p -> "::") // Detected an IPv6 interface, use ipv6 any address
                          .orElse("0.0.0.0") // No IPv6 support, use ipv4 any address
            ;
        }

        public Optional<ConnectionTransformerConfig> getConnectionTransformerConfig()
        {
            return connectionTransformerConfig;
        }

        public PvtbStreamHeader getHeader()
        {
            return header;
        }

        public Optional<Integer> getStreamBufferSize()
        {
            return streamBufferSize;
        }

        public String getPvtbHost()
        {
            return pvtbHost;
        }

        public int getPvtbPort()
        {
            return pvtbPort;
        }

        public String getListeningAddress()
        {
            return listeningAddress;
        }

        public int getListeningPort()
        {
            return listeningPort;
        }

        public boolean isTraceInput()
        {
            return this.traceInput;
        }

        static Config fromEnv()
        {
            final var cfg = new Config();
            final var env = System.getenv();
            final var domain = env.get(ENV_PVTB_DOMAIN);
            final var protocol = env.get(ENV_PVTB_PROTOCOL);
            final var podName = env.get(ENV_HOSTNAME);
            cfg.header = new PvtbStreamHeader(domain, protocol, podName);

            cfg.pvtbHost = env.get(ENV_PVTB_HOST);
            try
            {
                cfg.pvtbPort = Integer.parseInt(env.get(ENV_PVTB_PORT));
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid PVTB_HOST");
            }
            try
            {
                cfg.listeningPort = Integer.parseInt(env.get("LISTENING_PORT"));
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid LISTENING_PORT");
            }
            try
            {
                cfg.listeningAddress = decideListeningIP();

            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid IP_VERSION");
            }
            try
            {
                cfg.streamBufferSize = Optional.of(Optional //
                                                           .ofNullable(env.get("MAX_BUFFERED_PACKETS_PER_CONNECTION"))
                                                           .map(Integer::parseInt)
                                                           .orElse(DEFAULT_MAX_BUFFERED_PACKETS_PER_CONNECTION))
                                               .filter(val -> val > 0);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("MAX_BUFFERED_PACKETS_PER_CONNECTION");
            }
            try
            {
                cfg.chunkSizeOfPackets = Integer.parseInt(env.get(ENV_CHUNK_SIZE_LIMIT));
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid SIZE_TAPPED_DATA");
            }
            try
            {
                cfg.divisionMethod = DivisionMethod.fromValue(env.get(ENV_DIVISION_METHOD));
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid DIVISION_METHOD");
            }
            cfg.traceInput = Optional //
                                     .ofNullable(env.get("TRACE_INPUT"))
                                     .map(Boolean::parseBoolean)
                                     .orElse(DEFAULT_TRACE_INPUT);

            cfg.traceInput = Optional //
                                     .ofNullable(env.get("TRACE_INPUT"))
                                     .map(Boolean::parseBoolean)
                                     .orElse(DEFAULT_TRACE_INPUT);

            cfg.connectionTransformerConfig = ConnectionTransformerConfig.fromEnv();

            return cfg;
        }

        @Override
        public String toString()
        {
            return String.format("Config [header=%s, pvtbHost=%s, pvtbPort=%s, listeningAddress=%s, listeningPort=%s, streamBufferSize=%s, traceInput=%s, connectionTransformerConfig=%s]",
                                 header,
                                 pvtbHost,
                                 pvtbPort,
                                 listeningAddress,
                                 listeningPort,
                                 streamBufferSize,
                                 traceInput,
                                 connectionTransformerConfig);
        }
    }

    private static class ConnectionTransformerConfig
    {

        public final Duration pollingPeriod;
        public final String namespace;
        public final String svcLabelSelector;

        public ConnectionTransformerConfig(String namespace,
                                           String svcLabelSelector,
                                           Duration pollingPeriod)
        {
            Objects.requireNonNull(namespace);
            Objects.requireNonNull(svcLabelSelector);

            this.namespace = namespace;
            this.svcLabelSelector = svcLabelSelector;
            this.pollingPeriod = pollingPeriod;
        }

        static Optional<ConnectionTransformerConfig> fromEnv()
        {
            final var env = System.getenv();

            final var namespace = env.get("NAMESPACE");
            final var lblKey = env.get("REPLACE_LOCAL_SOCKET_ADDRESS_SERVICE_LABEL_KEY");
            final var lblValue = env.get("REPLACE_LOCAL_SOCKET_ADDRESS_SERVICE_LABEL_VALUE");
            Objects.requireNonNull(lblKey);
            Objects.requireNonNull(lblValue);
            final var labelSelector = String.format("%s=%s", lblKey, lblValue);

            final var defaultPollingPeriod = Duration.ofSeconds(10);
            return Optional.of(Boolean.parseBoolean(env.get("REPLACE_LOCAL_SOCKET_ADDRESS"))) //
                           .filter(Boolean::booleanValue)
                           .map(tru -> labelSelector)
                           .map(selector -> new ConnectionTransformerConfig(namespace, selector, defaultPollingPeriod));
        }

        @Override
        public String toString()
        {
            return String.format("ConnectionTransformerConfig [pollingDuration=%s, namespace=%s, svcLabelSelector=%s]",
                                 pollingPeriod,
                                 namespace,
                                 svcLabelSelector);
        }
    }
}
