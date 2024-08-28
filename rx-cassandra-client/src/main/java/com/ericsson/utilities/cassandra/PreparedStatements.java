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
 * Created on: Feb 17, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.cassandra;

import java.util.Map;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

/**
 * A set of prepared statements that have been built from an enumeration of
 * {@link SimpleStatement}
 * 
 * @param <K> The enumeration that holds {@link SimpleStatement} keys
 */
public class PreparedStatements<K extends Enum<K>>
{
    private final Map<K, PreparedStatement> prepared;

    PreparedStatements(Map<K, PreparedStatement> preparedStatements)
    {
        this.prepared = preparedStatements;
    }

    /**
     * Get built prepared statement
     * 
     * @param key The statement key
     * @return The built prepared statement, corresponding to the given key
     */
    public PreparedStatement get(K key)
    {
        return this.prepared.get(key);
    }
}
