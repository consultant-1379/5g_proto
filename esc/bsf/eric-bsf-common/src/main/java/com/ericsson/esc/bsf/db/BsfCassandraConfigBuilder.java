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
 * Created on: Dec 22, 2020
 *     Author: echfari
 */
package com.ericsson.esc.bsf.db;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.connection.ConstantReconnectionPolicy;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.cassandra.EnhancedDriverOption;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.file.SipTlsCertWatch;

/**
 * Creates Cassandra driver configuration for BSF
 */
public class BsfCassandraConfigBuilder
{
    private static final long MAX_SESSION_ID = 1000;

    private static final Logger log = LoggerFactory.getLogger(BsfCassandraConfigBuilder.class);
    private final boolean adminConfig;
    private final DbConfiguration cfg;
    private final MetricsConfigurator metricsCfg;
    private long connectionTimeout = RxSession.DEFAULT_INIT_TIMEOUT_MILLIS;
    private long retries = RxSession.DEFAULT_INIT_RETRIES;
    private final String sessionName;
    private final DynamicTlsCertManager tlsMgr;

    public static BsfCassandraConfigBuilder adminConfig(DbConfiguration cfg,
                                                        String cassandraCertPath,
                                                        String cassandraCaPath)
    {
        final var builder = new BsfCassandraConfigBuilder(true, cfg, null, cassandraCertPath, cassandraCaPath);
        return builder.withConnectionTimeout(10 * 60 * 1000L, -1) // 10 minutes, infinite retries
        ;
    }

    public static BsfCassandraConfigBuilder trafficConfig(DbConfiguration cfg,
                                                          MetricsConfigurator metricsCfg,
                                                          String cassandraCertPath,
                                                          String cassandraCaPath)
    {
        final var builder = new BsfCassandraConfigBuilder(false, cfg, metricsCfg, cassandraCertPath, cassandraCaPath);
        builder.withConnectionTimeout(5 * 60 * 1000L, 1); // 5 minutes, single retry, then fail(POD restart)
        return builder;
    }

    public static BsfCassandraConfigBuilder trafficConfigProbe(DbConfiguration cfg,
                                                               MetricsConfigurator metricsCfg,
                                                               String cassandraCertPath,
                                                               String cassandraCaPath)
    {
        return trafficConfig(cfg, metricsCfg, cassandraCertPath, cassandraCaPath).withConnectionTimeout(10 * 60 * 1000L, -1); // 10 minutes, infinite retries
    }

    private BsfCassandraConfigBuilder(boolean adminConfig,
                                      DbConfiguration cfg,
                                      MetricsConfigurator metricsCfg,
                                      String cassandraCertPath,
                                      String cassandraCaPath)
    {
        Objects.requireNonNull(cfg);

        this.adminConfig = adminConfig;
        this.cfg = cfg;
        this.metricsCfg = metricsCfg;
        this.sessionName = metricsCfg == null ? "s0" : metricsCfg.getSessionName();
        this.tlsMgr = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(cassandraCertPath), SipTlsCertWatch.trustedCert(cassandraCaPath));
    }

    public BsfCassandraConfigBuilder withConnectionTimeout(long timeoutMillis,
                                                           long retries)
    {
        this.connectionTimeout = timeoutMillis;
        this.retries = retries;
        return this;
    }

    public ProgrammaticDriverConfigLoaderBuilder buildConfig()
    {
        final var cnt = new AtomicLong();

        final var config = adminConfig ? createAdminCfg() : createTrafficCfg();

        return config.withString(DefaultDriverOption.SESSION_NAME,
                                 metricsCfg != null ? this.sessionName
                                                    : String.format("%s-%s", this.sessionName, Math.abs(cnt.getAndIncrement()) % MAX_SESSION_ID));
    }

    public RxSession buildSession()
    {
        final var config = cfg.getTls() //
                              .map(tlsConfig -> this.buildConfig()
                                                    .withBoolean(EnhancedDriverOption.TLS_ENABLED, true)
                                                    .withBoolean(EnhancedDriverOption.VERIFY_HOST, tlsConfig.hostnameVerification) // disable
                                                                                                                                   // hostname
                                                                                                                                   // verification
                                                    .build())
                              .orElseGet(() -> this.buildConfig().build() //
                              );

        final var builder = RxSession //
                                     .builder()
                                     .withConfig(config)
                                     .withInitTimeoutMillis(connectionTimeout)
                                     .withInitRetries(retries);
        cfg.getTls().ifPresent(tls -> builder.withDynamicTls(tlsMgr));

        return builder.build();
    }

    private ProgrammaticDriverConfigLoaderBuilder programmaticBuilder()
    {
        final var ccl = Thread.currentThread().getContextClassLoader();

        if (ccl == null)
        {
            // This is a workaround, for an unexplained bug.

            // Context class loader appears may be null for some Vertx worker threads
            log.info("Current thread:{}, contextClassLoader: {}", Thread.currentThread(), ccl);
        }
        return ccl == null ? DriverConfigLoader.programmaticBuilder(DriverConfigLoader.class.getClassLoader()) : DriverConfigLoader.programmaticBuilder();
    }

    /**
     * 
     * @param cfg
     * @param metricsCfg
     * @return A Cassandra configuration appropriate for traffics
     */
    private ProgrammaticDriverConfigLoaderBuilder createTrafficCfg()
    {
        return programmaticBuilder() //
                                     // Basic settings
                                    .withDuration(DefaultDriverOption.CONFIG_RELOAD_INTERVAL, Duration.ZERO) // Disable configuration reloading, DND-28090
                                    .withStringList(DefaultDriverOption.CONTACT_POINTS, cfg.getContactPoint())
                                    .withBoolean(DefaultDriverOption.RESOLVE_CONTACT_POINTS, false)
                                    .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, cfg.getLocalDataCenter())
                                    // Authentication settings
                                    .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                    .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, cfg.getUser())
                                    .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, cfg.getPassword())
                                    // Reconnection settings
                                    .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                    .withClass(DefaultDriverOption.RECONNECTION_POLICY_CLASS, ConstantReconnectionPolicy.class)
                                    .withDuration(DefaultDriverOption.RECONNECTION_BASE_DELAY, Duration.ofSeconds(1))
                                    // Query consistency settings
                                    .withString(DefaultDriverOption.REQUEST_CONSISTENCY, cfg.getCL())
                                    // Performance settings
                                    .withInt(DefaultDriverOption.CONNECTION_MAX_REQUESTS, 32768)
                                    // Throttling settings
                                    .withString(DefaultDriverOption.REQUEST_THROTTLER_CLASS, cfg.getThrottlerClass())
                                    .withInt(DefaultDriverOption.REQUEST_THROTTLER_MAX_CONCURRENT_REQUESTS, cfg.getThrottlerMaxConcurrentRequests())
                                    .withInt(DefaultDriverOption.REQUEST_THROTTLER_MAX_QUEUE_SIZE, cfg.getThrottlerMaxQueueSize())
                                    // Metrics settings
                                    .withStringList(DefaultDriverOption.METRICS_SESSION_ENABLED, metricsCfg.getSessionMetrics())
                                    .withDuration(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_HIGHEST, metricsCfg.getCqlRequests().getHighestLatency())
                                    .withInt(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_DIGITS, metricsCfg.getCqlRequests().getSignificantDigits())
                                    .withDuration(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_INTERVAL, metricsCfg.getCqlRequests().getRefreshInterval())
                                    .withDuration(DefaultDriverOption.METRICS_SESSION_THROTTLING_HIGHEST, metricsCfg.getThrottling().getHighestLatency())
                                    .withInt(DefaultDriverOption.METRICS_SESSION_THROTTLING_DIGITS, metricsCfg.getThrottling().getSignificantDigits())
                                    .withDuration(DefaultDriverOption.METRICS_SESSION_THROTTLING_INTERVAL, metricsCfg.getThrottling().getRefreshInterval())
                                    .withStringList(DefaultDriverOption.METRICS_NODE_ENABLED, metricsCfg.getNodeMetrics())
                                    .withDuration(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_HIGHEST, metricsCfg.getCqlMessages().getHighestLatency())
                                    .withInt(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_DIGITS, metricsCfg.getCqlMessages().getSignificantDigits())
                                    .withDuration(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_INTERVAL, metricsCfg.getCqlMessages().getRefreshInterval());
    }

    /**
     * 
     * @param cfg
     * @return A Cassandra configuration appropriate for database administration
     */
    private ProgrammaticDriverConfigLoaderBuilder createAdminCfg()
    {
        return programmaticBuilder() //
                                     // Basic settings
                                    .withDuration(DefaultDriverOption.CONFIG_RELOAD_INTERVAL, Duration.ZERO) // Disable configuration reloading, DND-28090
                                    .withStringList(DefaultDriverOption.CONTACT_POINTS, cfg.getContactPoint())
                                    .withBoolean(DefaultDriverOption.RESOLVE_CONTACT_POINTS, false)
                                    .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, cfg.getLocalDataCenter())
                                    // Authentication settings
                                    .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                    .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, cfg.getAdminUser())
                                    .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, cfg.getAdminPassword())
                                    // Reconnection settings
                                    .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                    .withClass(DefaultDriverOption.RECONNECTION_POLICY_CLASS, ConstantReconnectionPolicy.class)
                                    .withDuration(DefaultDriverOption.RECONNECTION_BASE_DELAY, Duration.ofSeconds(1))
                                    // Why do we need this?
                                    .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(10));
    }
}
