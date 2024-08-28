package com.ericsson.esc.bsf.worker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.internal.querybuilder.schema.compaction.DefaultSizeTieredCompactionStrategy;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;

/**
 * Describes the BSF cassandra schema
 * 
 * WARNING: The enumeration names SHOULD NOT be changed. They are used as
 * identifiers for the cassandra schema
 */
public class BsfSchema // NOSONAR
{

    private BsfSchema()
    {

    }

    /**
     * The ipv6_prefix user defined type
     */
    public enum ipv6_prefix implements UdtField
    {
        prefix_address(DataTypes.INET), // NOSONAR
        prefix_length(DataTypes.SMALLINT) // NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace)
        {
            return createUdtType(keyspace, values(), typeName());
        }

        private DataType dataType;

        ipv6_prefix(DataType dataType)
        {
            this.dataType = dataType;
        }

        @Override
        public String field()
        {
            return this.name();
        }

        @Override
        public DataType fieldType()
        {
            return this.dataType;
        }

        public static String typeName()
        {
            return ipv6_prefix.class.getSimpleName();
        }

    }

    /**
     * The ue_address user defined type
     */
    public enum ue_address implements UdtField
    {
        ipv4_addr(DataTypes.INET), // NOSONAR
        ipv6_prefix(SchemaBuilder.udt(BsfSchema.ipv6_prefix.typeName(), true)), // NOSONAR
        mac_addr48(DataTypes.TEXT), // NOSONAR
        ip_domain(DataTypes.TEXT) // NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace)
        {
            return createUdtType(keyspace, values(), typeName());
        }

        private DataType dataType;

        ue_address(DataType dataType)
        {
            this.dataType = dataType;
        }

        @Override
        public String field()
        {
            return this.name();
        }

        @Override
        public DataType fieldType()
        {
            return this.dataType;
        }

        public static String typeName()
        {
            return ue_address.class.getSimpleName();
        }

    }

    /**
     * The snssai user defined type
     */
    public enum snssai implements UdtField
    {
        sst(DataTypes.INT), // NOSONAR
        sd(DataTypes.TEXT)// NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace)
        {
            return createUdtType(keyspace, values(), typeName());
        }

        private DataType dataType;

        snssai(DataType dataType)
        {
            this.dataType = dataType;
        }

        @Override
        public String field()
        {
            return this.name();
        }

        @Override
        public DataType fieldType()
        {
            return this.dataType;
        }

        static String typeName()
        {
            return snssai.class.getSimpleName();
        }

    }

    /**
     * 
     * The ip_end_point user defined type
     *
     */
    public enum ip_end_point implements UdtField
    {
        ipv4_address(DataTypes.INET), // NOSONAR
        ipv6_address(DataTypes.INET), // NOSONAR
        transport(DataTypes.TEXT), // NOSONAR
        port(DataTypes.INT) // NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace)
        {
            return createUdtType(keyspace, values(), typeName());
        }

        private DataType dataType;

        ip_end_point(DataType dataType)
        {
            this.dataType = dataType;
        }

        @Override
        public String field()
        {
            return this.name();
        }

        @Override
        public DataType fieldType()
        {
            return this.dataType;
        }

        static String typeName()
        {
            return ip_end_point.class.getSimpleName();
        }

    }

    /**
     * The pcf_bindings table
     */
    public enum pcf_bindings implements TableColumn
    {
        binding_id, // NOSONAR
        matching_ue_address, // NOSONAR
        ue_address, // NOSONAR
        supi, // NOSONAR
        gpsi, // NOSONAR
        dnn, // NOSONAR
        pcf_fqdn, // NOSONAR
        pcf_ip_end_points, // NOSONAR
        pcf_diam_host, // NOSONAR
        pcf_diam_realm, // NOSONAR
        snssai, // NOSONAR
        pcf_id, // NOSONAR
        recovery_time, // NOSONAR
        add_ipv6_prefixes, // NOSONAR
        add_mac_addrs, // NOSONAR
        pcf_set_id, // NOSONAR
        bind_level // NOSONAR
        ;

        public static String tableName()
        {
            return pcf_bindings.class.getSimpleName();
        }

        public String column()
        {
            return this.name();
        }

        static SimpleStatement createStatement(String namespace,
                                               int gcGraceSeconds,
                                               int memtableFlushPeriodInMs,
                                               SizeTieredCompactionStrategy compactionStrategy)
        {
            return SchemaBuilder.createTable(namespace, tableName())
                                .ifNotExists()
                                .withPartitionKey(pcf_bindings.binding_id.column(), DataTypes.UUID)
                                .withClusteringColumn(pcf_bindings.matching_ue_address.column(), SchemaBuilder.udt(BsfSchema.ue_address.typeName(), true))
                                .withClusteringColumn(pcf_bindings.dnn.column(), DataTypes.TEXT)
                                .withClusteringColumn(pcf_bindings.snssai.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.ue_address.column(), SchemaBuilder.udt(BsfSchema.ue_address.typeName(), true))
                                .withColumn(pcf_bindings.supi.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.gpsi.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.pcf_fqdn.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.pcf_ip_end_points.column(),
                                            DataTypes.listOf(SchemaBuilder.udt(BsfSchema.ip_end_point.typeName(), true)))
                                .withColumn(pcf_bindings.pcf_diam_host.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.pcf_diam_realm.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.snssai.column(), SchemaBuilder.udt(BsfSchema.snssai.typeName(), true))
                                .withColumn(pcf_bindings.pcf_id.column(), DataTypes.UUID)
                                .withColumn(pcf_bindings.recovery_time.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.add_ipv6_prefixes.column(),
                                            DataTypes.listOf(SchemaBuilder.udt(BsfSchema.ipv6_prefix.typeName(), true)))
                                .withColumn(pcf_bindings.add_mac_addrs.column(), DataTypes.listOf(DataTypes.TEXT))
                                .withColumn(pcf_bindings.pcf_set_id.column(), DataTypes.TEXT)
                                .withColumn(pcf_bindings.bind_level.column(), DataTypes.TEXT)
                                .withGcGraceSeconds(gcGraceSeconds)
                                .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                                .withCompaction(compactionStrategy.getStrategy())
                                .build();
        }

        static SimpleStatement alterStatement(String namespace,
                                              int gcGraceSeconds,
                                              int memtableFlushPeriodInMs,
                                              SizeTieredCompactionStrategy compactionStrategy)
        {
            return SchemaBuilder.alterTable(namespace, tableName())
                                .withGcGraceSeconds(gcGraceSeconds)
                                .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                                .withCompaction(compactionStrategy.getStrategy())
                                .build();
        }
    }

    /**
     * The ue_dnn_snssai_mv materialized view
     */
    public enum ue_dnn_snssai_mv implements MaterializedViewKey
    {
        matching_ue_address, // NOSONAR
        dnn, // NOSONAR
        snssai, // NOSONAR
        binding_id // NOSONAR
        ;

        public static String viewName()
        {
            return ue_dnn_snssai_mv.class.getSimpleName();
        }

        public String key()
        {
            return this.name();
        }

        static SimpleStatement createStatement(String keyspace,
                                               int gcGraceSeconds,
                                               int memtableFlushPeriodInMs,
                                               SizeTieredCompactionStrategy compactionStrategy)
        {
            return createPcfBindingView(keyspace, values(), viewName(), gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy);
        }

        static SimpleStatement alterStatement(String namespace,
                                              int gcGraceSeconds,
                                              int memtableFlushPeriodInMs,
                                              SizeTieredCompactionStrategy compactionStrategy)
        {
            return SchemaBuilder.alterMaterializedView(namespace, viewName())
                                .withGcGraceSeconds(gcGraceSeconds)
                                .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                                .withCompaction(compactionStrategy.getStrategy())
                                .build();
        }
    }

    /**
     * The ue_supi_dnn_snssai_mv materialized view
     */
    public enum ue_supi_dnn_snssai_mv implements MaterializedViewKey
    {
        matching_ue_address, // NOSONAR
        supi, // NOSONAR
        dnn, // NOSONAR
        snssai, // NOSONAR
        binding_id // NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace,
                                               int gcGraceSeconds,
                                               int memtableFlushPeriodInMs,
                                               SizeTieredCompactionStrategy compactionStrategy)
        {
            return createPcfBindingView(keyspace, values(), viewName(), gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy);
        }

        static SimpleStatement alterStatement(String namespace,
                                              int gcGraceSeconds,
                                              int memtableFlushPeriodInMs,
                                              SizeTieredCompactionStrategy compactionStrategy)
        {
            return SchemaBuilder.alterMaterializedView(namespace, viewName())
                                .withGcGraceSeconds(gcGraceSeconds)
                                .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                                .withCompaction(compactionStrategy.getStrategy())
                                .build();
        }

        public static String viewName()
        {
            return ue_supi_dnn_snssai_mv.class.getSimpleName();
        }

        public String key()
        {
            return this.name();
        }
    }

    /**
     * The ue_gpsi_dnn_snssai_mv materialized view
     */
    public enum ue_gpsi_dnn_snssai_mv implements MaterializedViewKey
    {
        matching_ue_address, // NOSONAR
        gpsi, // NOSONAR
        dnn, // NOSONAR
        snssai, // NOSONAR
        binding_id // NOSONAR
        ;

        static SimpleStatement createStatement(String keyspace,
                                               int gcGraceSeconds,
                                               int memtableFlushPeriodInMs,
                                               SizeTieredCompactionStrategy compactionStrategy)
        {
            return createPcfBindingView(keyspace, values(), viewName(), gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy);
        }

        static SimpleStatement alterStatement(String namespace,
                                              int gcGraceSeconds,
                                              int memtableFlushPeriodInMs,
                                              SizeTieredCompactionStrategy compactionStrategy)
        {
            return SchemaBuilder.alterMaterializedView(namespace, viewName())
                                .withGcGraceSeconds(gcGraceSeconds)
                                .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                                .withCompaction(compactionStrategy.getStrategy())
                                .build();
        }

        public static String viewName()
        {
            return ue_gpsi_dnn_snssai_mv.class.getSimpleName();
        }

        public String key()
        {
            return this.name();
        }
    }

    /**
     * Represents a cassandra User Defined Type field
     */
    public interface UdtField
    {
        String field();

        DataType fieldType();
    }

    /**
     * Represents a materialized view key column
     */
    public interface MaterializedViewKey
    {
        String key();
    }

    /**
     * Represents a table column
     */
    public interface TableColumn
    {
        String column();
    }

    /**
     * Generate BSF keyspace creation statement
     * 
     * @param keyspace            The keyspace
     * @param replicationSettings The replication settings for the BSF keyspace
     * @return A SimpleStatement that shall create the keyspace upon execution
     */
    public static SimpleStatement createKeyspace(String keyspace,
                                                 Map<String, Object> replicationSettings)
    {

        return SchemaBuilder.createKeyspace(keyspace)//
                            .ifNotExists()
                            .withReplicationOptions(replicationSettings)
                            .build();
    }

    /**
     * 
     * @return The table names of BSF schema. Views are not included
     * @see #views()
     */
    public static List<String> tables()
    {
        return List.of(pcf_bindings.tableName());
    }

    /**
     * 
     * @return The materialized views of BSF schema
     * @see #tables()
     */
    public static List<String> views()
    {
        return List.of(ue_dnn_snssai_mv.viewName(), ue_supi_dnn_snssai_mv.viewName(), ue_gpsi_dnn_snssai_mv.viewName());
    }

    /**
     * Create complete BSF schema as a CQL string
     * 
     * @see #createSchema(String, Map, int, int)
     * @return CQL commands that can be used to construct BSF schema
     */
    public static String createSchemaCql(String keyspace,
                                         Map<String, Object> replicationSettings,
                                         int gcGraceSeconds,
                                         int memtableFlushPeriodInMs,
                                         SizeTieredCompactionStrategy compactionStrategy)
    {
        final var sb = new StringBuilder();
        createSchema(keyspace, replicationSettings, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy).forEach(ss -> sb.append(ss.getQuery())
                                                                                                                                 .append(";\n"));
        return sb.toString();
    }

    /**
     * Produce CQL statements that can create the complete BSF schema
     * 
     * @param keyspace                The wanted keyspace
     * @param replicationSettings     The replication settings
     * @param gcGraceSeconds          The garbage collector grace period in seconds
     * @param memtableFlushPeriodInMs The memtable flush period in ms
     * @return A Collection of CQL statements
     */
    public static List<SimpleStatement> createSchema(String keyspace,
                                                     Map<String, Object> replicationSettings,
                                                     int gcGraceSeconds,
                                                     int memtableFlushPeriodInMs,
                                                     SizeTieredCompactionStrategy compactionStrategy)
    {
        return List.of(createKeyspace(keyspace, replicationSettings),
                       ipv6_prefix.createStatement(keyspace),
                       ue_address.createStatement(keyspace),
                       snssai.createStatement(keyspace),
                       ip_end_point.createStatement(keyspace),
                       pcf_bindings.createStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_gpsi_dnn_snssai_mv.createStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_supi_dnn_snssai_mv.createStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_dnn_snssai_mv.createStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy));
    }

    public static List<SimpleStatement> alterSchema(String keyspace,
                                                    int gcGraceSeconds,
                                                    int memtableFlushPeriodInMs,
                                                    SizeTieredCompactionStrategy compactionStrategy)
    {
        return List.of(pcf_bindings.alterStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_gpsi_dnn_snssai_mv.alterStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_supi_dnn_snssai_mv.alterStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy),
                       ue_dnn_snssai_mv.alterStatement(keyspace, gcGraceSeconds, memtableFlushPeriodInMs, compactionStrategy));
    }

    /**
     * Construct a simple UDT type definition
     * 
     * @param keyspace    The kespace
     * @param fields      The field-name\field type pairs describing UDT contents
     * @param udtTypeName The name of the UDT
     * @return The constructed UDT
     */
    private static SimpleStatement createUdtType(String keyspace,
                                                 UdtField[] fields,
                                                 String udtTypeName)
    {
        final var begin = SchemaBuilder.createType(keyspace, udtTypeName).ifNotExists();
        final var it = List.of(fields).iterator();
        var udtField = it.next();
        var b = begin.withField(udtField.field(), udtField.fieldType());
        while (it.hasNext())
        {
            udtField = it.next();
            b = b.withField(udtField.field(), udtField.fieldType());
        }
        return b.build();
    }

    /**
     * Construct a materialized view of pcf_binding table from a subset of its
     * columns. The view contains all pcf_bindings columns except binding_id
     * 
     * @param keyspace                The keyspace
     * @param fields                  The pcf_bindings fields to use as keys
     * @param viewName                The name of the materialized view
     * @param gcGraceSeconds
     * @param memtableFlushPeriodInMs
     * @return The constructed view
     */
    private static SimpleStatement createPcfBindingView(String keyspace,
                                                        MaterializedViewKey[] fields,
                                                        String viewName,
                                                        int gcGraceSeconds,
                                                        int memtableFlushPeriodInMs,
                                                        SizeTieredCompactionStrategy compactionStrategy)
    {
        final var relations = Stream.of(fields).map(field -> Relation.column(field.key()).isNotNull()).collect(Collectors.toList());

        final var queryBegin = SchemaBuilder //
                                            .createMaterializedView(keyspace, viewName)
                                            .ifNotExists()
                                            .asSelectFrom(pcf_bindings.tableName())
                                            .all()
                                            .where(relations);

        final var fieldsIt = List.of(fields).iterator();
        var queryPart = queryBegin.withPartitionKey(fieldsIt.next().key());
        while (fieldsIt.hasNext())
        {
            queryPart = queryPart.withClusteringColumn(fieldsIt.next().key());
        }

        return queryPart //
                        .withGcGraceSeconds(gcGraceSeconds)
                        .withMemtableFlushPeriodInMs(memtableFlushPeriodInMs)
                        .withCompaction(compactionStrategy.getStrategy())
                        .build();

    }

    /**
     * Test method, outputs a sample BSF schema generated by this class
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        final var replicationSettings = Map.<String, Object>of("class", "NetworkTopologyStrategy", "datacenter1", 2);
        final var compactionStrategy = new SizeTieredCompactionStrategy(2, 0.5, 32, 3);
        final var schema = createSchemaCql("nbsf_management_keyspace", replicationSettings, 10800, 300000, compactionStrategy);
        System.out.println(schema); // NOSONAR
        final var sb = new StringBuilder();
        alterSchema("nbsf_management_keyspace", 10800, 300000, compactionStrategy).forEach(ss -> sb.append(ss.getQuery()).append(";\n"));
        System.out.println(sb.toString()); // NOSONAR

    }
}
