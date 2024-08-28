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
 * Created on: Jan 22, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.worker;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.ericsson.esc.bsf.worker.BsfSchema.pcf_bindings;
import com.ericsson.esc.bsf.worker.BsfSchema.ue_dnn_snssai_mv;
import com.ericsson.esc.bsf.worker.BsfSchema.ue_gpsi_dnn_snssai_mv;
import com.ericsson.esc.bsf.worker.BsfSchema.ue_supi_dnn_snssai_mv;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration class with the available queries for deploying the PCF bindings
 * schema and supporting the Nbsf_Management_Register and
 * Nbsf_Management_Discovery interfaces.
 */
public enum BsfQuery
{

    UEADDR((keyspace,
            useWriteTime) ->
    {
        var builder = QueryBuilder.selectFrom(keyspace, ue_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_DNN((keyspace,
                useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_DNN_SNSSAI((keyspace,
                       useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_dnn_snssai_mv.snssai.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_SUPI((keyspace,
                 useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_supi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_supi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.supi.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_SUPI_DNN((keyspace,
                     useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_supi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_supi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.supi.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_SUPI_DNN_SNSSAI((keyspace,
                            useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_supi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_supi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.supi.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_supi_dnn_snssai_mv.snssai.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_GPSI((keyspace,
                 useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_gpsi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.gpsi.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_GPSI_DNN((keyspace,
                     useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_gpsi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.gpsi.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    UEADDR_GPSI_DNN_SNSSAI((keyspace,
                            useWriteTime) ->
    {
        var builder = QueryBuilder //
                                  .selectFrom(keyspace, ue_gpsi_dnn_snssai_mv.viewName())
                                  .column(pcf_bindings.matching_ue_address.column())
                                  .column(pcf_bindings.gpsi.column())
                                  .column(pcf_bindings.dnn.column())
                                  .column(pcf_bindings.snssai.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .column(pcf_bindings.pcf_diam_host.column())
                                  .column(pcf_bindings.pcf_diam_realm.column())
                                  .column(pcf_bindings.pcf_fqdn.column())
                                  .column(pcf_bindings.pcf_id.column())
                                  .column(pcf_bindings.pcf_ip_end_points.column())
                                  .column(pcf_bindings.recovery_time.column())
                                  .column(pcf_bindings.supi.column())
                                  .column(pcf_bindings.ue_address.column())
                                  .column(pcf_bindings.add_ipv6_prefixes.column())
                                  .column(pcf_bindings.add_mac_addrs.column())
                                  .column(pcf_bindings.pcf_set_id.column())
                                  .column(pcf_bindings.bind_level.column())
                                  .column(pcf_bindings.binding_id.column())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.matching_ue_address.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.gpsi.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.dnn.key())
                                  .isEqualTo(bindMarker())
                                  .whereColumn(ue_gpsi_dnn_snssai_mv.snssai.key())
                                  .isEqualTo(bindMarker());

        return useWriteTime ? builder.writeTime(pcf_bindings.ue_address.column()).limit(BsfQuery.LIMIT_WITH_WRITE_TIME).build() : builder.limit(2).build();
    }),

    FULLTABLESCAN((keyspace,
                   useWriteTime) -> QueryBuilder.selectFrom(keyspace, pcf_bindings.tableName())
                                                .column(pcf_bindings.binding_id.column())
                                                .column(pcf_bindings.pcf_id.column())
                                                .column(pcf_bindings.recovery_time.column())
                                                .writeTime(pcf_bindings.ue_address.column())
                                                .perPartitionLimit(1)
                                                .build()),

    DEREGISTER((keyspace,
                useWriteTime) -> QueryBuilder.deleteFrom(keyspace, pcf_bindings.tableName())
                                             .whereColumn(pcf_bindings.binding_id.column())
                                             .isEqualTo(bindMarker())
                                             .build()),

    REGISTER((keyspace,
              useWriteTime) -> QueryBuilder.insertInto(keyspace, pcf_bindings.tableName())
                                           .values(Stream.of(BsfSchema.pcf_bindings.values())
                                                         .collect(Collectors.toMap(BsfSchema.pcf_bindings::column, val -> bindMarker(val.column()))))
                                           .usingTtl(bindMarker("ttl"))
                                           .build());

    private final StatementProvider query;
    public static final int LIMIT_WITH_WRITE_TIME = 50;
    public static final String WRITE_TIME_COL = String.format("writetime(%s)", BsfSchema.pcf_bindings.ue_address);

    private BsfQuery(StatementProvider query)
    {
        this.query = query;
    }

    private interface StatementProvider
    {
        /**
         * 
         * @param keyspace     The keyspace to operate on.
         * @param useWriteTime if multiple bindings are found, use write time to resolve
         *                     conflicts.
         * @return A cassandra {@link SimpleStatement} for the given keyspace
         */
        SimpleStatement createStatement(String keyspace,
                                        boolean useWriteTime);
    }

    /**
     * Create prepared statements for the BSF database schema
     * 
     * @param keyspace     The keyspace to operate on.
     * @param useWriteTime if multiple bindings are found, use write time to resolve
     *                     conflicts.
     * @return The created statements
     */
    public static Map<BsfQuery, SimpleStatement> createStatements(String keyspace,
                                                                  boolean useWriteTime)
    {
        final EnumMap<BsfQuery, SimpleStatement> queries = new EnumMap<>(BsfQuery.class);
        EnumSet.allOf(BsfQuery.class).stream().forEach(value -> queries.put(value, value.query.createStatement(keyspace, useWriteTime)));

        return queries;
    }

}
