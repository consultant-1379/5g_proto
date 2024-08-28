package com.ericsson.sc.bsf.diameter;

import java.time.Duration;

import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.metrics.CqlMessages;
import com.ericsson.esc.bsf.db.metrics.CqlRequests;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.esc.bsf.db.metrics.Throttling;
import com.ericsson.utilities.common.EnvVars;

public class BsfDiameterParams
{
    private final String hostname;
    private final String cmSchemaName;
    private final String cmMediatorHost;
    private final int cmMediatorPort;
    private final DbConfiguration dbConfiguration;
    private final MetricsConfigurator metricsConfigurator;
    private final EtcdParams etcdParams;
    private final boolean checkPcfRt;
    private final long pcfRtTTLsec;
    private final String ipFamily;

    BsfDiameterParams(String hostname,
                      DbConfiguration dbConfiguration,
                      MetricsConfigurator metricsConfigurator,
                      EtcdParams etcdParams,
                      boolean checkRT,
                      long pcfRtTTLsec,
                      String ipFamily)
    {
        this.cmMediatorPort = 5003;
        this.cmMediatorHost = "eric-cm-mediator";
        this.cmSchemaName = "ericsson-bsf";

        this.hostname = hostname;
        this.dbConfiguration = dbConfiguration;
        this.metricsConfigurator = metricsConfigurator;
        this.etcdParams = etcdParams;
        this.checkPcfRt = checkRT;
        this.pcfRtTTLsec = pcfRtTTLsec;
        this.ipFamily = ipFamily;
    }

    public String getHostname()
    {
        return hostname;
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

    public DbConfiguration getDatabaseConfiguration()
    {
        return this.dbConfiguration;
    }

    public MetricsConfigurator getMetricsConfigurator()
    {
        return this.metricsConfigurator;
    }

    public EtcdParams getEtcdParams()
    {
        return etcdParams;
    }

    /**
     * @return true if recovery time feature is enabled
     */
    public boolean getCheckPcfRt()
    {
        return checkPcfRt;
    }

    public long getPcfRtTTLsec()
    {
        return pcfRtTTLsec;
    }

    public String getIpFamily()
    {
        return ipFamily;
    }

    public static BsfDiameterParams fromEnvironment()
    {

        final var cassandraData = DbConfiguration.fromEnvironment();
        cassandraData.setConsistency(EnvVars.get("CASSANDRA_CONSISTENCY"));
        cassandraData.setThrottlerClass(EnvVars.get("CASSANDRA_THROTTLER_CLASS"));
        cassandraData.setThrottlerMaxQueueSize(Integer.parseInt(EnvVars.get("CASSANDRA_THROTTLER_MAX_QUEUE_SIZE")));
        cassandraData.setThrottlerMaxConcurrentRequests(Integer.parseInt(EnvVars.get("CASSANDRA_THROTTLER_MAX_CONCURRENT_REQUESTS")));

        final CqlRequests cqlRequests = new CqlRequests.CqlRequestsBuilder().withHighestLatency(Duration.parse(EnvVars.get("METRICS_SESSION_CQL_REQUESTS_HIGHEST")))
                                                                            .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_SESSION_CQL_REQUESTS_DIGITS")))
                                                                            .withRefreshInterval(Duration.parse(EnvVars.get("METRICS_SESSION_CQL_REQUESTS_INTERVAL")))
                                                                            .build();

        final CqlMessages cqlMessages = new CqlMessages.CqlMessagesBuilder().withHighestLatency(Duration.parse((EnvVars.get("METRICS_NODE_CQL_MESSAGES_HIGHEST"))))
                                                                            .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_NODE_CQL_MESSAGES_DIGITS")))
                                                                            .withRefreshInterval(Duration.parse((EnvVars.get("METRICS_NODE_CQL_MESSAGES_INTERVAL"))))
                                                                            .build();

        final Throttling throttling = new Throttling.ThrottlingBuilder().withHighestLatency(Duration.parse(EnvVars.get("METRICS_SESSION_THROTTLING_HIGHEST")))
                                                                        .withSignificantDigits(Integer.parseInt(EnvVars.get("METRICS_SESSION_THROTTLING_DIGITS")))
                                                                        .withRefreshInterval(Duration.parse(EnvVars.get("METRICS_SESSION_THROTTLING_INTERVAL")))
                                                                        .build();

        final MetricsConfigurator metricsData = new MetricsConfigurator.MetricsConfiguratorBuilder(EnvVars.get("METRICS_JMX_EXPORTER_SESSION"),
                                                                                                   EnvVars.get("METRICS_JMX_EXPORTER_DOMAIN")).withCqlRequests(cqlRequests)
                                                                                                                                              .withCqlMessages(cqlMessages)
                                                                                                                                              .withThrottling(throttling)
                                                                                                                                              .build();

        final var checkRT = Boolean.parseBoolean(EnvVars.get("CHECK_RECOVERY_TIME"));
        final var pcfRtTTLsec = Long.parseLong(EnvVars.get("PCFRT_TTL_SEC"));

        return new BsfDiameterParams(EnvVars.get("HOSTNAME"), //
                                     cassandraData,
                                     metricsData,
                                     EtcdParams.fromEnvironment(),
                                     checkRT,
                                     pcfRtTTLsec,
                                     EnvVars.get("IP_FAMILY"));
    }
}
