/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 28, 2020
 *     Author: eaoknkr
 */

package com.ericsson.esc.bsf.manager;

import java.util.Objects;

import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.utilities.common.EnvVars;

/**
 * BSF Manager configuration parameters.
 */
public class BsfManagerParameters
{
    private final String cmMediatorHost;
    private final String hostname;
    private final Service service;
    private final DbConfiguration dbConfiguration;
    private final boolean enableLeaderElection;
    private final boolean checkRT;
    private final long pcfRtTTLsec;
    private final String ipFamily;
    private final boolean bsfDiameterEnabled;

    public BsfManagerParameters(String cmMediatorHost,
                                String hostname,
                                Service service,
                                DbConfiguration dbConfiguration,
                                boolean enableLeaderElection,
                                boolean checkRT,
                                long pcfRtTTLsec,
                                String ipFamily,
                                boolean bsfDiameterEnabled)
    {
        Objects.requireNonNull(cmMediatorHost);
        Objects.requireNonNull(hostname);
        Objects.requireNonNull(service);
        Objects.requireNonNull(dbConfiguration);

        this.cmMediatorHost = cmMediatorHost;
        this.hostname = hostname;
        this.service = service;
        this.dbConfiguration = dbConfiguration;
        this.enableLeaderElection = enableLeaderElection;
        this.checkRT = checkRT;
        this.pcfRtTTLsec = pcfRtTTLsec;
        this.ipFamily = ipFamily;
        this.bsfDiameterEnabled = bsfDiameterEnabled;
    }

    public String getCmMediatorHost()
    {
        return this.cmMediatorHost;
    }

    public String getHostname()
    {
        return this.hostname;
    }

    public DbConfiguration getDatabaseConfiguration()
    {
        return this.dbConfiguration;
    }

    public boolean isEnableLeaderElection()
    {
        return this.enableLeaderElection;
    }

    /**
     * @return the checkRT
     */
    public boolean getCheckRT()
    {
        return checkRT;
    }

    /**
     * @return the pcfRtTTLsec
     */
    public long getPcfRtTTLsec()
    {
        return pcfRtTTLsec;
    }

    public String getIpFamily()
    {
        return ipFamily;
    }

    /**
     * @return the bsfDiameterEnabled
     */
    public boolean isBsfDiameterEnabled()
    {
        return this.bsfDiameterEnabled;
    }

    public static BsfManagerParameters fromEnvironment()
    {
        final var cassandraData = DbConfiguration.fromEnvironment();
        cassandraData.setConsistency(EnvVars.get("CASSANDRA_CONSISTENCY"));
        cassandraData.setGcGrace(Integer.parseInt(EnvVars.get("CASSANDRA_GC_GRACE")));
        cassandraData.setMemTableFlushPeriod(Integer.parseInt(EnvVars.get("CASSANDRA_MEMTABLE_FLUSH_PERIOD")));
        cassandraData.setThrottlerClass(EnvVars.get("CASSANDRA_THROTTLER_CLASS"));
        cassandraData.setThrottlerMaxQueueSize(Integer.parseInt(EnvVars.get("CASSANDRA_THROTTLER_MAX_QUEUE_SIZE")));
        cassandraData.setThrottlerMaxConcurrentRequests(Integer.parseInt(EnvVars.get("CASSANDRA_THROTTLER_MAX_CONCURRENT_REQUESTS")));
        cassandraData.setAdminCredentials(EnvVars.get("CASSANDRA_ADMIN"), EnvVars.get("CASSANDRA_ADMIN_PASSWORD"));
        cassandraData.setStorageMaxPercentage(Integer.parseInt(EnvVars.get("CASSANDRA_STORAGE_MAX_PERCENTAGE")));

        final var pcfRtTTLsec = Long.parseLong(EnvVars.get("PCFRT_TTL_SEC", 2592000L));
        final var compactionBucketHigh = Double.parseDouble(EnvVars.get("CASSANDRA_COMPACTION_BUCKET_HIGH"));
        final var compactionBucketLow = Double.parseDouble(EnvVars.get("CASSANDRA_COMPACTION_BUCKET_LOW"));
        final var compactionMaxThreshold = Integer.parseInt(EnvVars.get("CASSANDRA_COMPACTION_MAX_THRESHOLD"));
        final var compactionMinThreshold = Integer.parseInt(EnvVars.get("CASSANDRA_COMPACTION_MIN_THRESHOLD"));
        final var compactionStrategy = new SizeTieredCompactionStrategy(compactionBucketHigh,
                                                                        compactionBucketLow,
                                                                        compactionMaxThreshold,
                                                                        compactionMinThreshold);
        cassandraData.setCompactionStrategy(compactionStrategy);

        return new BsfManagerParameters(EnvVars.get("CM_MEDIATOR", "eric-cm-mediator"),
                                        EnvVars.get("HOSTNAME"),
                                        new Service(EnvVars.get("SERVICE_HOST", "eric-bsf-manager"),
                                                    Integer.parseInt(EnvVars.get("SERVICE_PORT", 80)),
                                                    Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT", 80))),
                                        cassandraData,
                                        Boolean.valueOf(EnvVars.get("LEADER_ELECTION_ENABLED")),
                                        Boolean.valueOf(EnvVars.get("CHECK_RECOVERY_TIME", "true")),
                                        pcfRtTTLsec,
                                        EnvVars.get("IP_FAMILY"),
                                        Boolean.parseBoolean(EnvVars.get("BSF_DIAMETER_ENABLED", false)));
    }

    public Service getService()
    {
        return service;
    }

    /**
     * Represents the configuration for a kubernetes service
     */
    static final class Service
    {
        final String host;
        final int port;
        final int targetPort;

        public Service(String host,
                       int port,
                       int targetPort)
        {
            this.host = host;
            this.port = port;
            this.targetPort = targetPort;
        }

        /**
         * @return The service FQDN
         */
        public String getHost()
        {
            return host;
        }

        /**
         * 
         * @return The service port
         */
        public int getPort()
        {
            return port;
        }

        /**
         * 
         * @return The service port, as seen from within POD
         */
        public int getTargetPort()
        {
            return targetPort;
        }

        @Override
        public String toString()
        {
            var builder = new StringBuilder();
            builder.append("Service [host=");
            builder.append(host);
            builder.append(", port=");
            builder.append(port);
            builder.append(", targetPort=");
            builder.append(targetPort);
            builder.append("]");
            return builder.toString();
        }

    }
}
