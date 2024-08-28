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
 * Created on: Jun 02, 2020
 *     Author: ekoteva
 */

package com.ericsson.esc.bsf.db;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.internal.querybuilder.schema.compaction.DefaultSizeTieredCompactionStrategy;
import com.ericsson.utilities.common.EnvVars;

public class DbConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(DbConfiguration.class);
    private String cL;
    private int gccGrace;
    private int memTableFlushPeriod;
    private SizeTieredCompactionStrategy compactionStrategy;
    private String throttlerClass;
    private int throttlerMaxQueueSize;
    private int throttlerMaxConcurrentRequests;
    private final List<String> contactPoint;
    private final String keyspace;
    private final String localDataCenter;
    private int storageMaxPercentage;
    private boolean useWriteTime;

    private String user;
    private String password;
    private String adminUser;
    private String adminPassword;
    private Optional<TlsConfig> tls = Optional.empty();

    // TLS configuration

    public static class TlsConfig
    {
        public final boolean hostnameVerification;

        public TlsConfig(boolean hostnameVerification)
        {
            this.hostnameVerification = hostnameVerification;
        }
    }

    /**
     * Size tiered compaction strategy configuration.
     */
    public static class SizeTieredCompactionStrategy
    {
        private final double bucketHigh;
        private final double bucketLow;
        private final int maxThreshold;
        private final int minThreshold;
        private final DefaultSizeTieredCompactionStrategy strategy;

        public SizeTieredCompactionStrategy(double bucketHigh,
                                            double bucketLow,
                                            int maxThreshold,
                                            int minThreshold)
        {
            this.bucketHigh = bucketHigh;
            this.bucketLow = bucketLow;
            this.maxThreshold = maxThreshold;
            this.minThreshold = minThreshold;
            this.strategy = new DefaultSizeTieredCompactionStrategy().withBucketHigh(bucketHigh)
                                                                     .withBucketLow(bucketLow)
                                                                     .withMaxThreshold(maxThreshold)
                                                                     .withMinThreshold(minThreshold);
        }

        /**
         * @return the bucketHigh
         */
        public double getBucketHigh()
        {
            return bucketHigh;
        }

        /**
         * @return the bucketLow
         */
        public double getBucketLow()
        {
            return bucketLow;
        }

        /**
         * @return the maxThreshold
         */
        public int getMaxThreshold()
        {
            return maxThreshold;
        }

        /**
         * @return the minThreshold
         */
        public int getMinThreshold()
        {
            return minThreshold;
        }

        /**
         * @return the strategy
         */
        public DefaultSizeTieredCompactionStrategy getStrategy()
        {
            return strategy;
        }
    }

    public static DbConfiguration fromEnvironment()
    {
        final var basicConfig = new DbConfiguration(EnvVars.get("CASSANDRA_KEYSPACE"), //
                                                    scaleContactPoint(EnvVars.get("CASSANDRA_CONTACT_POINT"),
                                                                      Integer.parseInt(EnvVars.get("CASSANDRA_CONTACT_POINT_REPLICAS"))),
                                                    EnvVars.get("CASSANDRA_LOCAL_DATACENTER"));
        basicConfig.setUserCredentials(EnvVars.get("CASSANDRA_USER"), EnvVars.get("CASSANDRA_USER_PASSWORD"));
        basicConfig.setUseWriteTime(Boolean.parseBoolean(EnvVars.get("USE_WRITE_TIME", true)));
        basicConfig.tls = Optional.ofNullable(EnvVars.get("CASSANDRA_TLS_ENABLED"))
                                  .map(Boolean::parseBoolean)
                                  .filter(Boolean::booleanValue)
                                  .map(val -> new TlsConfig(Boolean.valueOf(EnvVars.get("CASSANDRA_TLS_HOSTNAME_VERIFICATION"))));

        return basicConfig;
    }

    public DbConfiguration(final String keyspace,
                           final List<String> contactPoint,
                           final String localDataCenter)
    {
        this.keyspace = keyspace;
        this.contactPoint = contactPoint;
        this.localDataCenter = localDataCenter;
    }

    static List<String> scaleContactPoint(String contactPoint,
                                          int count)
    {
        if (count == 0)
            return List.of(contactPoint);
        final var parts = contactPoint.split(":");
        final var hostname = parts[0];
        final var port = Integer.parseInt(parts[1]);
        return IntStream //
                        .range(0, count)
                        .mapToObj(i -> String.format("%s-%d.%s:%d", hostname, i, hostname, port))
                        .collect(Collectors.toUnmodifiableList());
    }

    public void setTls(Optional<TlsConfig> tls)
    {
        this.tls = tls;
    }

    public void setUserCredentials(String user,
                                   String password)
    {
        this.user = user;
        this.password = password;
    }

    public void setAdminCredentials(String adminUser,
                                    String adminPassword)
    {
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
    }

    public void setConsistency(final String consistency)
    {
        this.cL = consistency;
    }

    public void setGcGrace(final int gccGrace)
    {
        this.gccGrace = gccGrace;
    }

    public void setMemTableFlushPeriod(final int memTableFlushPeriod)
    {
        this.memTableFlushPeriod = memTableFlushPeriod;
    }

    public void setCompactionStrategy(final SizeTieredCompactionStrategy compactionStrategy)
    {
        this.compactionStrategy = compactionStrategy;
    }

    public void setThrottlerClass(final String throttlerClass)
    {
        this.throttlerClass = throttlerClass;
    }

    public void setThrottlerMaxQueueSize(final int throttlerMaxQueueSize)
    {
        this.throttlerMaxQueueSize = throttlerMaxQueueSize;
    }

    public void setThrottlerMaxConcurrentRequests(final int throttlerMaxConcurrentRequests)
    {
        this.throttlerMaxConcurrentRequests = throttlerMaxConcurrentRequests;
    }

    public void setStorageMaxPercentage(final int storageMaxPercentage)
    {
        this.storageMaxPercentage = storageMaxPercentage;
    }

    public void setUseWriteTime(final boolean useWriteTime)
    {
        this.useWriteTime = useWriteTime;
    }

    public static void main(String[] args)
    {
        log.info("Challengers ROCK!!");
    }

    public String getKeyspace()
    {
        return this.keyspace;
    }

    public List<String> getContactPoint()
    {
        return this.contactPoint;
    }

    public String getLocalDataCenter()
    {
        return this.localDataCenter;
    }

    public Optional<TlsConfig> getTls()
    {
        return this.tls;
    }

    public int getGcGrace()
    {
        return this.gccGrace;
    }

    public String getThrottlerClass()
    {
        return this.throttlerClass;
    }

    public int getThrottlerMaxQueueSize()
    {
        return this.throttlerMaxQueueSize;
    }

    public int getThrottlerMaxConcurrentRequests()
    {
        return this.throttlerMaxConcurrentRequests;
    }

    public int getStorageMaxPercentage()
    {
        return this.storageMaxPercentage;
    }

    public boolean getUseWriteTime()
    {
        return this.useWriteTime;
    }

    public int getMemTableFlushPeriod()
    {
        return this.memTableFlushPeriod;
    }

    public SizeTieredCompactionStrategy getCompactionStrategy()
    {
        return this.compactionStrategy;
    }

    public String getCL()
    {
        return this.cL;
    }

    public String getUser()
    {
        return this.user;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getAdminUser()
    {
        return this.adminUser;
    }

    public String getAdminPassword()
    {
        return this.adminPassword;
    }
}