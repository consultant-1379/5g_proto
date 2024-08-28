package com.ericsson.utilities.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.jmx.JmxReporter;

import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * Exposes Cassandra driver metrics to JMX interface
 */
public class CassandraMetricsExporter
{
    private static final Logger log = LoggerFactory.getLogger(CassandraMetricsExporter.class);
    private Maybe<JmxReporter> chain;

    public CassandraMetricsExporter(RxSession rxSession,
                                    String domain)
    {
        this.chain = rxSession.sessionHolder() //
                              .map(RxSession.SessionHolder::getCqlSession)
                              .doOnSuccess(s ->
                              {
                                  if (s.getMetrics().isEmpty())
                                      log.warn("Cassandra driver metrics are disabled, cannot export to JMX");
                              })
                              .filter(s -> s.getMetrics().isPresent())
                              .map(s ->
                              {
                                  final var metrics = s.getMetrics().get();

                                  final var reporter = JmxReporter //
                                                                  .forRegistry(metrics.getRegistry())
                                                                  .inDomain(domain)
                                                                  .build();
                                  log.info("Starting JMX exporter for Cassandra session {}", s.getName());
                                  reporter.start();
                                  return reporter;
                              })
                              .cache();
    }

    /**
     * Stop the exporter
     * 
     * @return A Completable that performs the Operation upon subscription
     */
    public Completable stop()
    {
        return this.chain.doOnSuccess(JmxReporter::close) //
                         .doOnError(err -> log.error("Failed to close JMX reporter", err))
                         .onErrorComplete()
                         .ignoreElement()
                         .cache();
    }

    /**
     * Start the exporter
     * 
     * @return A Completable that performs the opertation upon subscription
     */
    public Completable start()
    {
        return this.chain.ignoreElement();
    }
}
