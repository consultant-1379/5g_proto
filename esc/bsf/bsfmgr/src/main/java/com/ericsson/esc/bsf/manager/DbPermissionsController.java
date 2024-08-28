/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: March 10, 2021
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data.Result;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.utilities.cassandra.RxSession;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * The DbPermissionsController checks (and revokes if needed) the database
 * permissions based on the current storage utilization and the max specified
 * threshold that is allowed
 */
public class DbPermissionsController
{
    private static final Logger log = LoggerFactory.getLogger(DbPermissionsController.class);

    private final Observable<List<PmAdapter.Query.Response.Data>> dbStorageUtilizationData;
    private final BsfUserHandler dbUserHandler;
    private final int dbStorageMaxPercentage;

    /**
     * Creates a DbPermissionsController
     * 
     * @param dbStorageUtilizationData The data received with polling mechanism
     *                                 including the current database storage
     *                                 utilization
     * @param cassandraDbSession       The RxJava cassandra driver instance.
     * @param dbParams                 The parameters of the database configuration.
     */
    public DbPermissionsController(Observable<List<PmAdapter.Query.Response.Data>> dbStorageUtilizationData,
                                   RxSession cassandraDbSession,
                                   DbConfiguration dbParams)
    {

        this.dbStorageUtilizationData = dbStorageUtilizationData;
        this.dbUserHandler = new BsfUserHandler(cassandraDbSession, dbParams.getKeyspace(), dbParams.getUser(), dbParams.getPassword());
        this.dbStorageMaxPercentage = dbParams.getStorageMaxPercentage();
    }

    /**
     * Controls database MODIFY permissions on BSF keyspace based on current disk
     * usage and specified threshold
     * 
     * @return A Completable that never completes unless there is an expected
     *         termination
     */
    public Completable controlDbModifyPermissions()
    {
        return dbStorageUtilizationData.flatMapSingle(data ->
        {
            double dbStorageUtilization = getDbStorageUtilization(data);

            if (dbStorageUtilization > dbStorageMaxPercentage)
            {
                log.warn("DB storage utilization is higher than the configured threshold. "
                         + "MODIFY permissions on BSF keyspace will be revoked (if they still exist).");

                return dbUserHandler.verifyBsfUserModifyPermissions();
            }
            else
            {
                return Single.just(false);
            }
        })
                                       .flatMapSingle(blockPermissionsNeeded -> Boolean.TRUE.equals(blockPermissionsNeeded) ? dbUserHandler.revokeBsfUserModifyPermissions()
                                                                                                                            : Single.just(true))
                                       .doOnError(e -> log.error("DB permissions controller terminated unexpectedly.", e))
                                       .ignoreElements();
    }

    /**
     * Extracts the current database storage utilization based on the Query Response
     * Data
     * 
     * @param data The data received as response to the defined PM Query:
     *             job:cassandra_storage_utilization_total_100.
     * @return The extracted actual database storage utilization value (in
     *         percentage).
     */
    private double getDbStorageUtilization(List<PmAdapter.Query.Response.Data> data)
    {
        double dbStorageUtilization = 0.0;

        if (data == null)
        {
            log.debug("NULL data received.");
            return dbStorageUtilization;
        }

        log.debug("data.size={}", data.size());

        if (!data.isEmpty())
        {
            final List<Result> result = data.get(0).getResult();
            log.debug("result={}", result);

            if (!result.isEmpty())
            {
                dbStorageUtilization = result.get(0).getValue().get(1);
                log.debug("dbStorageUtilization={}", dbStorageUtilization);
            }
        }

        return dbStorageUtilization;
    }

}
