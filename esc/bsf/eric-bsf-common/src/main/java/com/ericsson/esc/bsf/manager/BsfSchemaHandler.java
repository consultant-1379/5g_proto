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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.worker.BsfSchema;
import com.ericsson.esc.lib.FaultyDbSchemaException;
import com.ericsson.sc.bsf.model.Datacenter;
import com.ericsson.sc.bsf.model.EricssonBsfStatus;
import com.ericsson.sc.bsf.model.EricssonBsfTopology;
import com.ericsson.utilities.cassandra.ClusterInfo;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class BsfSchemaHandler
{
    private static final Logger log = LoggerFactory.getLogger(BsfSchemaHandler.class);

    // Main table expected data
    private static final List<String> TABLES = BsfSchema.tables();
    private static final List<String> VIEWS = BsfSchema.views();

    private final RxSession rxSession;
    private final DbConfiguration dbConfiguration;

    public BsfSchemaHandler(RxSession rxSession,
                            DbConfiguration dbConfiguration)
    {
        this.rxSession = rxSession;
        this.dbConfiguration = dbConfiguration;
    }

    public Single<Boolean> createAndVerifySchema(Map<String, Object> replicationFactorSettings)
    {
        return createSchema(replicationFactorSettings) //
                                                      .filter(Boolean.TRUE::equals)
                                                      .flatMapSingleElement(res -> verifySchema(this.rxSession, this.dbConfiguration.getKeyspace()) //
                                                                                                                                                   .doOnError(err -> log.warn("Failed to verify BSF database schema",
                                                                                                                                                                              err))
                                                                                                                                                   .toSingleDefault(true)
                                                                                                                                                   .onErrorReturnItem(false))
                                                      .toSingle(false);
    }

    /**
     * Updates GC grace period and memtable flush period for an already deployed
     * schema
     * 
     * @return The result of the asynchronous schema update
     */
    public Single<Boolean> alterSchema()
    {
        final var statements = BsfSchema.alterSchema(dbConfiguration.getKeyspace(),
                                                     dbConfiguration.getGcGrace(),
                                                     dbConfiguration.getMemTableFlushPeriod(),
                                                     dbConfiguration.getCompactionStrategy());
        return Observable.fromIterable(statements)
                         .concatMapCompletable(st -> rxSession.sessionHolder()
                                                              .flatMapPublisher(sh -> sh.executeReactive(st))
                                                              .doOnSubscribe(disp -> log.info("Executing alter statement {}", st.getQuery()))
                                                              .doOnError(err -> log.error("Error while executing statement {}", st.getQuery(), err))
                                                              .toList()
                                                              .doOnSuccess(res -> log.debug("Statement execution result: {}", res))
                                                              .ignoreElement())
                         .toSingleDefault(true)
                         .onErrorReturnItem(false);
    }

    /**
     * Creates BSF schema, if not exists
     * 
     * @param replicationFactorSettings
     * @return The result of the asynchronous schema creation
     */
    public Single<Boolean> createSchema(Map<String, Object> replicationFactorSettings)
    {
        final var schemaStatements = BsfSchema.createSchema(dbConfiguration.getKeyspace(), //
                                                            replicationFactorSettings,
                                                            dbConfiguration.getGcGrace(),
                                                            dbConfiguration.getMemTableFlushPeriod(),
                                                            dbConfiguration.getCompactionStrategy());
        return Observable.fromIterable(schemaStatements)
                         .concatMapCompletable(st -> rxSession.sessionHolder()
                                                              .flatMapPublisher(sh -> sh.executeReactive(st))
                                                              .doOnSubscribe(disp -> log.info("Executing statement {}", st.getQuery()))
                                                              .doOnError(err -> log.error("Error while executing statement {}", st.getQuery(), err))
                                                              .toList()
                                                              .doOnSuccess(res -> log.debug("Statement execution result: {}", res))
                                                              .ignoreElement())
                         .toSingleDefault(true)
                         .onErrorReturnItem(false);
    }

    /**
     * Updates BSF schema replication
     * 
     * @param replicationFactorSettings
     * @return The result of the asynchronous schema update
     */
    public Single<Boolean> updateReplication(String replicationFactorSettings)
    {
        final var alterQuery = String.format("ALTER KEYSPACE %s WITH replication = {'class':'NetworkTopologyStrategy',%s};",
                                             dbConfiguration.getKeyspace(),
                                             replicationFactorSettings);

        return rxSession.sessionHolder()
                        .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance(alterQuery)) //
                                                  .doOnSubscribe(disp -> log.info("Executing statement {}", alterQuery)))
                        .toList()
                        .doOnError(err -> log.error("Error while executing keyspace/schema query {}", alterQuery, err))
                        .map(result -> true)
                        .onErrorReturnItem(false);
    }

    public static Completable verifySchema(RxSession rxSession,
                                           String keyspace)
    {
        return rxSession.sessionHolder() //
                        .map(RxSession.SessionHolder::getCqlSession)
                        .doOnSuccess(session -> verifyBsfSchema(new ClusterInfo(session), keyspace))
                        .ignoreElement();
    }

    /**
     * Verify that BSF database is ready to accept traffic. In case of error, retry
     * forever.
     * 
     * @param rxSession The cassandra session
     * @param keyspace  The keyspace
     * @return A Completable that completes as soon as BSF database is ready
     */
    public static Completable verifyBsfDb(final RxSession rxSession,
                                          final String keyspace)
    {
        return verifyBsfDb(rxSession, keyspace, 2000, -1); // retry for ever with a 2 second period
    }

    public static Completable verifyBsfDb(final RxSession rxSession,
                                          final String keyspace,
                                          final Integer delay,
                                          final Integer numberRetries)
    {
        final var retryFunction = new RetryFunction().withDelay(delay) // retry upon failure with delay in ms
                                                     .withRetries(numberRetries) // maximum retries
                                                     .withRetryAction((error,
                                                                       retries) -> log.warn("Retrying BSF database verification,keyspace {} retries {}",
                                                                                            keyspace,
                                                                                            retries,
                                                                                            error))
                                                     .create();
        return verifyBsfKeyspace(rxSession, keyspace) //
                                                     .retryWhen(retryFunction)
                                                     .doOnError(e -> log.error("BSF database verification on keyspace {} failed after maximum number of retries",
                                                                               keyspace));
    }

    private static Completable verifyBsfKeyspace(RxSession rxSession,
                                                 String keyspace)
    {
        return verifySchema(rxSession, keyspace) //
                                                .andThen(verifySchemaAgreement(rxSession));
    }

    /**
     * Verifies keyspace, existence of tables and views
     * 
     * @param metadata
     * @param keyspace
     * @throws FaultyDbSchemaException
     */
    private static void verifyBsfSchema(ClusterInfo metadata,
                                        String keyspace)
    {
        // Verify keyspace
        if (!metadata.keyspaceExists(keyspace))
            throw new FaultyDbSchemaException("Keyspace " + keyspace + " does not exist");

        TABLES.forEach(tableName -> verifyTable(metadata, keyspace, tableName));
        VIEWS.forEach(viewName -> verifyView(metadata, keyspace, viewName));
    }

    /**
     * 
     * @param metadata
     * @param keyspace
     * @param tableName
     * @throws FaultyDbSchemaException
     */
    private static void verifyTable(ClusterInfo metadata,
                                    String keyspace,
                                    String tableName)
    {
        if (!metadata.tableExists(keyspace, tableName))
            throw new FaultyDbSchemaException("TableColumn " + tableName + " missing from keyspace " + keyspace);
    }

    /**
     * 
     * @param metadata
     * @param keyspace
     * @param viewName
     * @throws FaultyDbSchemaException
     */
    private static void verifyView(ClusterInfo metadata,
                                   String keyspace,
                                   String viewName)
    {
        if (!metadata.viewExists(keyspace, viewName))
            throw new FaultyDbSchemaException("View " + viewName + " missing from keyspace " + keyspace);
    }

    static Completable verifySchemaAgreement(RxSession rxSession)
    {
        return rxSession.sessionHolder()
                        .flatMap(RxSession.SessionHolder::checkSchemaAgreement)
                        .flatMapCompletable(result -> result.booleanValue() ? Completable.complete()
                                                                            : Completable.error(new FaultyDbSchemaException("All hosts that are currently up do not agree on the schema definition.")));
    }

    public Single<EricssonBsfStatus> getSchemaStatus()
    {
        return rxSession.sessionHolder() //
                        .map(RxSession.SessionHolder::getCqlSession)
                        .flatMap(session ->
                        {
                            var schemaStatus = new EricssonBsfStatus();

                            try
                            {
                                verifyBsfSchema(new ClusterInfo(session), this.dbConfiguration.getKeyspace());
                                schemaStatus.setReady(true);
                            }
                            catch (FaultyDbSchemaException e)
                            {
                                schemaStatus.setReady(false);
                                schemaStatus.setInfo(e.getMessage());
                            }

                            return Single.just(schemaStatus);
                        });
    }

    public Single<EricssonBsfTopology> getSchemaTopology()
    {
        return rxSession.sessionHolder().map(RxSession.SessionHolder::getCqlSession).flatMap(session ->
        {
            var dbTopology = new EricssonBsfTopology();
            var clusterMetadata = new ClusterInfo(session);

            final var datacenters = new ArrayList<Datacenter>();

            final var replication = clusterMetadata.getReplication(this.dbConfiguration.getKeyspace());

            if (replication != null && !replication.isEmpty())
            {
                for (var entry : replication.entrySet())
                {
                    final var dc = new Datacenter();
                    dc.setName(entry.getKey());
                    dc.setReplicationFactor(Integer.valueOf(entry.getValue()));
                    datacenters.add(dc);
                }
            }
            dbTopology.setDatacenter(datacenters);

            return Single.just(dbTopology);
        });
    }

}
