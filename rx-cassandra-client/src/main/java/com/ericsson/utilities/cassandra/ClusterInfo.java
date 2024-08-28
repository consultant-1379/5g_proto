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
 * Created on: May 21, 2020
 *     Author: ekoteva
 */

package com.ericsson.utilities.cassandra;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.RelationMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.ViewMetadata;

public class ClusterInfo
{

    private static final Logger log = LoggerFactory.getLogger(ClusterInfo.class);

    private final Metadata clusterMetadata;
    private final Map<CqlIdentifier, KeyspaceMetadata> keyspacesMetadata;
    private final CqlSession session;

    public ClusterInfo(CqlSession session)
    {
        this.session = session;
        this.clusterMetadata = session.getMetadata();
        log.debug("Extracting metadata for cluster {}.", this.clusterMetadata.getClusterName());

        this.keyspacesMetadata = this.clusterMetadata.getKeyspaces();
        log.debug("Cluster {} contains {} keyspaces.", this.clusterMetadata.getClusterName(), this.keyspacesMetadata.size());
    }

    /*
     * Checks whether hosts that are currently up agree on the schema definition.
     * 
     * @return boolean true if all hosts agree on schema definition false if all
     * host don't agree, or if the check could not be performed
     */
    public boolean checkSchemaAgreement()
    {
        return session.checkSchemaAgreement();
    }

    /*
     * Check existence of keyspace in current cluster
     * 
     * @param keyspace This is the name of keyspace to be checked
     * 
     * @return boolean true if keyspace exists, false if keyspace is not a known
     * keyspace
     */
    public boolean keyspaceExists(String keyspace)
    {
        return this.clusterMetadata.getKeyspace(keyspace).isPresent();
    }

    /*
     * Check existence of table in current cluster keyspace
     * 
     * @param keyspace This is the name of keyspace that the table expected to be
     * 
     * @param table This is the name of table to be checked
     * 
     * @return boolean true if the table exists in keyspace, false if the keyspace
     * is not a known keyspace or if the table does not exist in given keyspace
     */
    public boolean tableExists(String keyspace,
                               String table)
    {
        return this.clusterMetadata. //
                                   getKeyspace(keyspace) //
                                   .flatMap(ks -> ks.getTable(table))
                                   .isPresent();
    }

    public boolean viewExists(String keyspace,
                              String view)
    {
        return this.clusterMetadata //
                                   .getKeyspace(keyspace)
                                   .flatMap(ks -> ks.getView(view))
                                   .isPresent();
    }

    public boolean checkMaterializedViewColumns(String keyspace,
                                                String mViewTable,
                                                List<String> columns,
                                                List<String> primaryKeys,
                                                List<String> partitionKeys,
                                                List<String> clusteringColumns)
    {
        ViewMetadata mView = null;
        Optional<KeyspaceMetadata> keyspaceMetadata = this.clusterMetadata.getKeyspace(keyspace);
        if (keyspaceMetadata.isPresent())
        {
            Optional<ViewMetadata> viewMetadata = keyspaceMetadata.get().getView(mViewTable);
            if (viewMetadata.isPresent())
            {
                mView = viewMetadata.get();
            }
        }

        // check that mview exists
        if (mView == null)
        {
            log.debug("Materialized view {} does not exist", mViewTable);
            return false;
        }

        return checkTableColumns(mView, columns, primaryKeys, partitionKeys, clusteringColumns);
    }

    public boolean checkTableColumns(String keyspace,
                                     String table,
                                     List<String> columns,
                                     List<String> primaryKeys,
                                     List<String> partitionKeys,
                                     List<String> clusteringColumns)
    {
        Optional<KeyspaceMetadata> keyspaceMetadata = this.clusterMetadata.getKeyspace(keyspace);
        if (keyspaceMetadata.isPresent())
        {
            Optional<TableMetadata> tableMetadata = keyspaceMetadata.get().getTable(table);
            if (tableMetadata.isPresent())
            {
                return checkTableColumns(tableMetadata.get(), columns, primaryKeys, partitionKeys, clusteringColumns);
            }
        }
        return false;

    }

    private boolean checkTableColumns(RelationMetadata tableMetadata,
                                      List<String> columns,
                                      List<String> primaryKeys,
                                      List<String> partitionKeys,
                                      List<String> clusteringColumns)
    {
        // check allColumns
        final var actualColumns = tableMetadata.getColumns().values();
        log.debug("Current columns {}", actualColumns.stream().map(ColumnMetadata::getName).collect(Collectors.toList()));
        log.debug("Expected columns {}", columns);
        if (!checkColumnData(actualColumns, columns))
        {
            log.error("Column mismatch in table {}", tableMetadata.getName());
            return false;
        }

        // check primaryKey
        final var primaryKeyColumns = tableMetadata.getPrimaryKey();
        log.debug("Current primary keys {}", primaryKeyColumns.stream().map(ColumnMetadata::getName).collect(Collectors.toList()));
        log.debug("Expected primary keys {}", primaryKeys);
        if (!checkColumnData(primaryKeyColumns, primaryKeys))
        {
            log.error("Primary key column mismatch for table {}", tableMetadata.getName());
            return false;
        }

        // check partitionKey
        var partitionKeyColumns = tableMetadata.getPartitionKey();
        log.debug("Current partition keys {}", partitionKeyColumns.stream().map(ColumnMetadata::getName).collect(Collectors.toList()));
        log.debug("Expected partition keys {}", partitionKeys);
        if (!checkColumnData(partitionKeyColumns, partitionKeys))
        {
            log.error("Partition key column mismatch for table {}", tableMetadata.getName());
            return false;
        }

        // check clusteringColumns
        var clusteringKeys = tableMetadata.getClusteringColumns().keySet();
        log.debug("Current primary key columns {}", clusteringKeys.stream().map(ColumnMetadata::getName).collect(Collectors.toList()));
        log.debug("Expected primary key columns {}", columns);
        if (!checkColumnData(clusteringKeys, clusteringColumns))
        {
            log.error("Clustering key column mismatch for table {}", tableMetadata.getName());
            return false;
        }

        log.info("Table {} verified successfully.", tableMetadata.getName());
        return true;
    }

    /*
     * Check if current cassandra column key name match with the expected input
     * columns names
     * 
     * @param columnMetadata The collection of the column metadata to be checked
     * 
     * @param columns The expected input columns to be checked
     * 
     * @return boolean true if all columns exist in expected column list false
     * otherwise
     */
    public boolean checkColumnData(Collection<ColumnMetadata> columnMetadata,
                                   List<String> columns)
    {
        final var expectedColumnNames = new TreeSet<>(columns);
        final var currentColumnNames = columnMetadata.stream()
                                                     .map(ColumnMetadata::getName)
                                                     .map(CqlIdentifier::toString)
                                                     .map(String::toLowerCase)
                                                     .collect(Collectors.toCollection(TreeSet::new));

        return expectedColumnNames.equals(currentColumnNames);
    }

    /**
     * Get existing replication factor settings of the specific keyspace, ignoring
     * replication strategy class (NetworkTopologyStrategy/SimpleStrategy)
     * 
     * @param keyspace
     * @return
     */
    public Map<String, String> getReplication(String keyspace)
    {
        Optional<KeyspaceMetadata> keyspaceMetadata = this.clusterMetadata.getKeyspace(keyspace);
        Map<String, String> replication = null;

        if (keyspaceMetadata.isPresent())
        {
            replication = keyspaceMetadata.get().getReplication();
            replication.remove("class");
        }

        return replication;
    }

}
