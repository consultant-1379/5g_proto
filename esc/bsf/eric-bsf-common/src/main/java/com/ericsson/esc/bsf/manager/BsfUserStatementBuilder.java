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

package com.ericsson.esc.bsf.manager;

import static com.datastax.oss.driver.api.core.cql.SimpleStatement.newInstance;
import static java.lang.String.format;

import java.util.List;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;

/**
 * Builds CQL statements that create a new user and grant appropriate
 * permissions
 */
public final class BsfUserStatementBuilder
{
    private static final String CREATE_USER = "CREATE ROLE IF NOT EXISTS '%s' WITH PASSWORD = '%s' AND LOGIN=true;";
    private static final String GRANT_CREATE_ROLE = "GRANT CREATE ON KEYSPACE %s to '%s';";
    private static final String GRANT_MODIFY_ROLE = "GRANT MODIFY ON KEYSPACE %s to '%s';";
    private static final String GRANT_SELECT_ROLE = "GRANT SELECT ON KEYSPACE %s to '%s';";
    private static final String GRANT_AUTHORIZE_ROLE = "GRANT AUTHORIZE ON KEYSPACE %s to '%s';";
    private static final String GRANT_SYS_SELECT_ROLE = "GRANT SELECT ON system_auth.role_permissions to '%s';";

    /**
     * Disable object creation
     */
    private BsfUserStatementBuilder()
    {
    }

    /**
     * Create CQL statement for BSF user creation
     * 
     * @param keyspace The keyspace
     * @param bsfUser  The BSF user name to create
     * @param password The BSF user password to create
     * @return
     */
    public static List<SimpleStatement> build(String keyspace,
                                              String bsfUser,
                                              String password)
    {
        return List.of(newInstance(format(CREATE_USER, bsfUser, password)),
                       newInstance(format(GRANT_CREATE_ROLE, keyspace, bsfUser)),
                       newInstance(format(GRANT_MODIFY_ROLE, keyspace, bsfUser)),
                       newInstance(format(GRANT_SELECT_ROLE, keyspace, bsfUser)),
                       newInstance(format(GRANT_AUTHORIZE_ROLE, keyspace, bsfUser)),
                       newInstance(format(GRANT_SYS_SELECT_ROLE, bsfUser)));
    }
}
