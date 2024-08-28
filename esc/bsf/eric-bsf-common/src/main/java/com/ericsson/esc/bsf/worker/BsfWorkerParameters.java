/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 16, 2018
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import org.javatuples.Pair;

import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.metrics.CqlMessages;
import com.ericsson.esc.bsf.db.metrics.CqlRequests;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.esc.bsf.db.metrics.Throttling;
import com.ericsson.utilities.common.EnvVars;

public class BsfWorkerParameters
{
    private final String hostname;
    private final int workerPort;
    private final String cmSchemaName;
    private final String cmMediatorHost;
    private final int cmMediatorPort;
    private final int webServerPoolSize;
    private final DbConfiguration dbConfiguration;
    private final MetricsConfigurator metricsConfigurator;
    private final boolean useTls;
    private final int tlsPort;
    private final long maxConcurrentStreams;
    private final int dscpMarking;
    private final int defaultZeroBindingTimeout;
    private final boolean checkPcfRt;
    private final long pcfRtTTLsec;
    private final String ipFamily;
    private final Pair<Optional<String>, Optional<String>> listeningAddresses;
    private final String certificatesPath;
    private final String trustCAPath;

    public BsfWorkerParameters(String cmSchemaName,
                               String hostname,
                               int workerPort,
                               String cmMediatorHost,
                               int webServerPoolSize,
                               DbConfiguration dbConfiguration,
                               MetricsConfigurator metricsConfigurator,
                               boolean useTls,
                               int tlsPort,
                               long maxConcurrentStreams,
                               int dscpMarking,
                               int defaultZeroBindingTimeout,
                               boolean checkRT,
                               long pcfRtTTLsec,
                               String ipFamily,
                               Pair<Optional<String>, Optional<String>> listeningAddresses,
                               String certificatesPath,
                               String trustCAPath)
    {
        cmMediatorPort = 5003;
        this.hostname = hostname;
        this.workerPort = workerPort;
        this.cmSchemaName = cmSchemaName;
        this.cmMediatorHost = cmMediatorHost;
        this.webServerPoolSize = webServerPoolSize;
        this.dbConfiguration = dbConfiguration;
        this.metricsConfigurator = metricsConfigurator;
        this.useTls = useTls;
        this.tlsPort = tlsPort;
        this.maxConcurrentStreams = maxConcurrentStreams;
        this.dscpMarking = dscpMarking;
        this.defaultZeroBindingTimeout = defaultZeroBindingTimeout;
        this.checkPcfRt = checkRT;
        this.pcfRtTTLsec = pcfRtTTLsec;
        this.ipFamily = ipFamily;
        this.listeningAddresses = listeningAddresses;
        this.certificatesPath = certificatesPath;
        this.trustCAPath = trustCAPath;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getWorkerPort()
    {
        return workerPort;
    }

    public String getCmSchemaName()
    {
        return this.cmSchemaName;
    }

    public String getCmMediatorHost()
    {
        return this.cmMediatorHost;
    }

    public int getCmMediatorPort()
    {
        return this.cmMediatorPort;
    }

    public int getWebServerPoolSize()
    {
        return this.webServerPoolSize;
    }

    public DbConfiguration getDatabaseConfiguration()
    {
        return this.dbConfiguration;
    }

    public MetricsConfigurator getMetricsConfigurator()
    {
        return this.metricsConfigurator;
    }

    public long getMaxConcurrentStreams()
    {
        return this.maxConcurrentStreams;
    }

    public int getDscpMarking()
    {
        return this.dscpMarking;
    }

    public int getDefaultZeroBindingTimeout()
    {
        return this.defaultZeroBindingTimeout;
    }

    public String getIpFamily()
    {
        return this.ipFamily;
    }

    public Optional<String> getIpv4ListeningAddress()
    {
        return this.listeningAddresses.getValue0();
    }

    public Optional<String> getIpv6ListeningAddress()
    {
        return this.listeningAddresses.getValue1();
    }

    /**
     * @return the checkRT
     */
    public boolean getCheckPcfRt()
    {
        return checkPcfRt;
    }

    public long getPcfRtTTLsec()
    {
        return pcfRtTTLsec;
    }

    public String getCertificatesPath()
    {
        return certificatesPath;
    }

    public String getTrustCAPath()
    {
        return trustCAPath;
    }

    @Override
    public String toString()
    {
        return "BsfWorkerParameters [hostname=" + hostname + ", workerPort=" + workerPort + ", cmSchemaName=" + cmSchemaName + ", cmMediatorHost="
               + cmMediatorHost + ", cmMediatorPort=" + cmMediatorPort + ", webServerPoolSize=" + webServerPoolSize + ", dbConfiguration=" + dbConfiguration
               + ", metricsConfigurator=" + metricsConfigurator + ", useTls=" + useTls + ", tlsPort=" + tlsPort + ", maxConcurrentStreams="
               + maxConcurrentStreams + ", dscpMarking=" + dscpMarking + ", defaultZeroBindingTimeout=" + defaultZeroBindingTimeout + ", checkPcfRt="
               + checkPcfRt + ", pcfRtTTLsec=" + pcfRtTTLsec + ", ipFamily=" + ipFamily + ", listeningAddresses=" + listeningAddresses + ", certificatesPath="
               + certificatesPath + ", trustCAPath=" + trustCAPath + "]";
    }

    public static BsfWorkerParameters fromEnvironment()
    {
        final var cmSchemaName = "ericsson-bsf";
        final var webServerPoolSize = Integer.parseInt(EnvVars.get("WEBSERVER_POOL_SIZE", 0));
        final var dscpMarking = Integer.parseInt(EnvVars.get("DSCP_MARKING", 0));

        final var cassandraData = DbConfiguration.fromEnvironment();

        cassandraData.setConsistency(EnvVars.get("CASSANDRA_CONSISTENCY"));

        final var cassandraThrottlerClass = EnvVars.get("CASSANDRA_THROTTLER_CLASS");
        if (cassandraThrottlerClass != null && !cassandraThrottlerClass.isEmpty())
        {
            cassandraData.setThrottlerClass(cassandraThrottlerClass);
        }

        final var cassandraThrottlerMaxQueSize = EnvVars.get("CASSANDRA_THROTTLER_MAX_QUEUE_SIZE");
        if (cassandraThrottlerMaxQueSize != null && !cassandraThrottlerMaxQueSize.isEmpty())
        {
            cassandraData.setThrottlerMaxQueueSize(Integer.parseInt(cassandraThrottlerMaxQueSize));
        }

        final var cassandraThrottlerMaxConcurrentRequests = EnvVars.get("CASSANDRA_THROTTLER_MAX_CONCURRENT_REQUESTS");
        if (cassandraThrottlerMaxConcurrentRequests != null && !cassandraThrottlerMaxConcurrentRequests.isEmpty())
        {
            cassandraData.setThrottlerMaxConcurrentRequests(Integer.parseInt(cassandraThrottlerMaxConcurrentRequests));
        }

        final var cqlRequests = new CqlRequests.CqlRequestsBuilder().withHighestLatency(Duration.parse(EnvVars.get("METRICS_SESSION_CQL_REQUESTS_HIGHEST")))
                                                                    .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_SESSION_CQL_REQUESTS_DIGITS")))
                                                                    .withRefreshInterval(Duration.parse((EnvVars.get("METRICS_SESSION_CQL_REQUESTS_INTERVAL"))))
                                                                    .build();

        final var cqlMessages = new CqlMessages.CqlMessagesBuilder().withHighestLatency(Duration.parse(EnvVars.get("METRICS_NODE_CQL_MESSAGES_HIGHEST")))
                                                                    .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_NODE_CQL_MESSAGES_DIGITS")))
                                                                    .withRefreshInterval(Duration.parse(EnvVars.get("METRICS_NODE_CQL_MESSAGES_INTERVAL")))
                                                                    .build();

        final var throttling = new Throttling.ThrottlingBuilder().withHighestLatency(Duration.parse(EnvVars.get("METRICS_SESSION_THROTTLING_HIGHEST")))
                                                                 .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_SESSION_THROTTLING_DIGITS")))
                                                                 .withRefreshInterval(Duration.parse(EnvVars.get("METRICS_SESSION_THROTTLING_INTERVAL")))
                                                                 .build();

        final var metricsData = new MetricsConfigurator.MetricsConfiguratorBuilder(EnvVars.get("METRICS_JMX_EXPORTER_SESSION"),
                                                                                   EnvVars.get("METRICS_JMX_EXPORTER_DOMAIN")).withCqlRequests(cqlRequests)
                                                                                                                              .withCqlMessages(cqlMessages)
                                                                                                                              .withThrottling(throttling)
                                                                                                                              .build();
        final var useTls = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));
        final var tlsPort = Integer.parseInt(EnvVars.get("TLS_PORT", 443));
        final var nonTlsPort = Integer.parseInt(EnvVars.get("NON_TLS_PORT", 80));

        final var maxConcurrentStreams = Long.parseLong(EnvVars.get("MAX_CONCURRENT_STREAMS", 100));

        final var defaultZeroBindingTimeout = Integer.parseInt(EnvVars.get("DEFAULT_ZERO_BINDING_TIMEOUT"));

        final var checkRT = Boolean.parseBoolean(EnvVars.get("CHECK_RECOVERY_TIME"));
        final var pcfRtTTLsec = Long.parseLong(EnvVars.get("PCFRT_TTL_SEC"));

        final var podIps = Optional.ofNullable(EnvVars.get("POD_IPS"));
        final var podIp = EnvVars.get("POD_IP");
        final Optional<String> ipv4ListeningAddress;
        final Optional<String> ipv6ListeningAddress;
        if (podIps.isPresent())
        {
            ipv4ListeningAddress = Stream.of(podIps.get().split(",")).filter(ip -> ip.contains(".")).findFirst();
            ipv6ListeningAddress = Stream.of(podIps.get().split(",")).filter(ip -> ip.contains(":")).findFirst();
        }
        else
        {
            ipv4ListeningAddress = podIp.contains(".") ? Optional.of(podIp) : Optional.empty();
            ipv6ListeningAddress = podIp.contains(":") ? Optional.of(podIp) : Optional.empty();
        }
        final var listeningAddresses = new Pair<>(ipv4ListeningAddress, ipv6ListeningAddress);
        final var certificatesPath = EnvVars.get("WORKER_TRAFFIC_CERTIFICATE", "/run/secrets/bsfworker/certificates");
        final var trustCAPath = EnvVars.get("WORKER_TRAFFIC_ROOT_CA_PATH", "/run/secrets/bsfworker/certificates/trustCA");

        return new BsfWorkerParameters(cmSchemaName,
                                       EnvVars.get("HOSTNAME"),
                                       nonTlsPort,
                                       EnvVars.get("CM_MEDIATOR"),
                                       webServerPoolSize,
                                       cassandraData,
                                       metricsData,
                                       useTls,
                                       tlsPort,
                                       maxConcurrentStreams,
                                       dscpMarking,
                                       defaultZeroBindingTimeout,
                                       checkRT,
                                       pcfRtTTLsec,
                                       EnvVars.get("IP_FAMILY"),
                                       listeningAddresses,
                                       certificatesPath,
                                       trustCAPath);
    }

    // to be removed
    public boolean isUseTls()
    {
        return useTls;
    }

    public int getTlsPort()
    {
        return tlsPort;
    }
}
