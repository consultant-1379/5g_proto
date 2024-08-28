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

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.utilities.cassandra.RxSession;

import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;

/**
 * The DbStorageUtilizationSupervisor supervises the database storage
 * utilization using Inquisitor polling mechanism and DbPermissionsController
 * checks and decisions
 */
public class DbStorageUtilizationSupervisor
{
    private static final int REQUEST_TIMEOUT_MILLIS = 30000;
    private final PmAdapter.Inquisitor inquisitor;
    private final DbPermissionsController dbPermissionsController;

    /**
     * Creates a DbStorageUtilizationSupervisor
     * 
     * @param vertx              The vertx instance.
     * @param pmServerParams     The parameters of PM Server configuration,
     * @param cassandraDbSession The RxJava cassandra driver instance.
     * @param dbParams           The parameters of the database configuration.
     */
    public DbStorageUtilizationSupervisor(final Vertx vertx,
                                          PmServerParameters pmServerParams,
                                          RxSession cassandraDbSession,
                                          DbConfiguration dbParams)
    {

        // 1. cassandra storage utilization (in percentage) sampled every 5 minutes
        final Query.Element dbStorageUtilization = Query.max(Query.metric("job:cassandra_storage_utilization_total_100")
                                                                  .param("kubernetes_namespace", pmServerParams.getNamespace()));

        this.inquisitor = new PmAdapter.Inquisitor(new PmAdapter(vertx, pmServerParams.getPmServerSvcPort(), pmServerParams.getPmServerSvcHost()),
                                                   REQUEST_TIMEOUT_MILLIS,
                                                   dbStorageUtilization);

        this.dbPermissionsController = new DbPermissionsController(this.inquisitor.getData(), cassandraDbSession, dbParams);
    }

    /**
     * Starts the usage of inquisitor polling mechanism and executes the periodic
     * checks on database storage utilization
     * 
     * @return A Completable that never completes, unless dBPermissionsController
     *         terminated unexpectedly
     */
    public Completable run()
    {
        return this.inquisitor.start().andThen(this.dbPermissionsController.controlDbModifyPermissions());
    }

    /**
     * Stops the Inquisitor polling mechanism
     * 
     * @return A Completable
     */
    public Completable stop()
    {
        return this.inquisitor.stop();
    }

}
