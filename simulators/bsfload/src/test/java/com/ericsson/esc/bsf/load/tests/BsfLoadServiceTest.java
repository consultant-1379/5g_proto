/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 4, 2022
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.tests;

import static org.testng.Assert.assertTrue;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.metrics.MetricsHandler;
import com.ericsson.esc.bsf.load.server.BsfLoadParameters;
import com.ericsson.esc.bsf.load.server.BsfLoadService;
import com.ericsson.esc.bsf.load.server.BsfLoadService.ResultStatus;
import com.ericsson.esc.bsf.load.server.BsfLoadService.WorkLoadState;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.reactivex.core.Vertx;

/**
 * BsfLoadService Operations Tests.
 */
public class BsfLoadServiceTest
{
    // Common Properties.
    private static final BsfLoadConfiguration INVALID_CONF = new BsfLoadConfiguration.Builder().build();
    private static final BsfLoadConfiguration VALID_CONF = new BsfLoadConfiguration.Builder().targetPort(80).build();
    private static final BsfLoadParameters PARAMS = new BsfLoadParameters(30080, false, false, "/metrics", 30081);
    private static final MetricsHandler METRICS_HANDLER = new MetricsHandler("127.0.0.1", PARAMS);
    private static final Vertx VERTX = VertxBuilder.newInstance().build();

    // Assertion Messages.
    private static final String INCORRECT_INVALID_PARAMS = "Incorrect number of invalid parameters.";
    private static final String INCORRECT_NUMBER_OF_ENTRIES = "Incorrect number of workLoad entries.";
    private static final String INCORRECT_RESULT_STATUS = "Incorrect result status.";
    private static final String INCORRECT_RUN_ID = "Incorrect runId.";
    private static final String INCORRECT_WORKLOAD_STATE = "Incorrect workload state.";
    private static final String UNEXPECTED_INVALID_PARAM = "Unexpected invalid parameter.";

    @Test(groups = "functest")
    public void executeWorkLoadSuccess()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);
        final var result = service.executeWorkLoad(VALID_CONF) //
                                  .timeout(500, TimeUnit.MILLISECONDS)
                                  .blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.WORKLOAD_CREATED, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void executeWorkLoadServerBusy()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);
        final var result = service.executeWorkLoad(VALID_CONF) //
                                  .ignoreElement()
                                  .andThen(service.executeWorkLoad(VALID_CONF))
                                  .blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.SERVICE_BUSY, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void executeWorkLoadInvalidConfiguration()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);
        final var result = service.executeWorkLoad(INVALID_CONF) //
                                  .timeout(1000, TimeUnit.MILLISECONDS)
                                  .blockingGet();

        assertTrue(result.getInvalidParams().size() == 1, INCORRECT_INVALID_PARAMS);
        assertTrue(result.getInvalidParams().get(0).getParam().equals("target-port"), UNEXPECTED_INVALID_PARAM);
        assertTrue(result.getStatus() == ResultStatus.INVALID_CONFIGURATION, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void getWorkLoads()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        Completable.complete()
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .blockingAwait();

        final var result = service.getWorkLoads().blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().size() == 3, INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void getWorkLoadsEmpty()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);
        final var result = service.getWorkLoads().blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void getWorkLoadInCompletedState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        // Add timer to wait for workload to complete.
        final var getResult = Completable.timer(200, TimeUnit.MILLISECONDS) //
                                         .andThen(service.getWorkLoad(runId))
                                         .blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
        assertTrue(getResult.getWorkLoadEntries().get(0).getRunId().equals(runId), INCORRECT_RUN_ID);
        assertTrue(getResult.getWorkLoadEntries().get(0).getState() == WorkLoadState.COMPLETED, INCORRECT_WORKLOAD_STATE);
    }

    @Test(groups = "functest")
    public void getWorkLoadInRunningState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        final var getResult = service.getWorkLoad(runId).blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
        assertTrue(getResult.getWorkLoadEntries().get(0).getRunId().equals(runId), INCORRECT_RUN_ID);
        assertTrue(getResult.getWorkLoadEntries().get(0).getState() == WorkLoadState.RUNNING, INCORRECT_WORKLOAD_STATE);
    }

    @Test(groups = "functest")
    public void getWorkLoadNotFound()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var result = service.getWorkLoad(UUID.randomUUID()).blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.NOT_FOUND, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void deleteWorkLoadsAllCompleted()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        // Add 3 workloads and let them complete before deleting.
        Completable.complete()
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .blockingAwait();

        final var result = service.deleteWorkLoads().blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.WORKLOAD_DELETED, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);

        // Verify all workloads are deleted.
        final var getResult = service.getWorkLoads().blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void deleteWorkLoadsOneRunning()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        // Add 3 workloads and let the last one in running state.
        Completable.complete()
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .andThen(Observable.timer(200, TimeUnit.MILLISECONDS).ignoreElements())
                   .andThen(service.executeWorkLoad(VALID_CONF).ignoreElement())
                   .blockingAwait();

        final var result = service.deleteWorkLoads().blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.WORKLOAD_DELETED, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);

        // Verify that one workload remained.
        final var getResult = service.getWorkLoads().blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void deleteWorkLoadInCompletedState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        // Add timer to wait for workload to complete.
        final var deleteResult = Completable.timer(200, TimeUnit.MILLISECONDS) //
                                            .andThen(service.deleteWorkLoad(runId))
                                            .blockingGet();

        assertTrue(deleteResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(deleteResult.getStatus() == ResultStatus.WORKLOAD_DELETED, INCORRECT_RESULT_STATUS);
        assertTrue(deleteResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);

        final var getResult = service.getWorkLoad(runId).blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.NOT_FOUND, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void deleteWorkLoadInRunningState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        // Do not wait for workload to complete.
        final var deleteResult = service.deleteWorkLoad(runId).blockingGet();

        assertTrue(deleteResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(deleteResult.getStatus() == ResultStatus.INVALID_OPERATION, INCORRECT_RESULT_STATUS);
        assertTrue(deleteResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);

        final var getResult = service.getWorkLoad(runId).blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
        assertTrue(getResult.getWorkLoadEntries().get(0).getRunId().equals(runId), INCORRECT_RUN_ID);
    }

    @Test(groups = "functest")
    public void deleteWorkLoadNotFound()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var result = service.deleteWorkLoad(UUID.randomUUID()).blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.NOT_FOUND, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void terminateWorkLoadInCompletedState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        // Add timer to wait for workload to complete.
        final var terminateResult = Completable.timer(200, TimeUnit.MILLISECONDS) //
                                               .andThen(service.terminateWorkLoad(runId))
                                               .blockingGet();

        assertTrue(terminateResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(terminateResult.getStatus() == ResultStatus.INVALID_OPERATION, INCORRECT_RESULT_STATUS);
        assertTrue(terminateResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }

    @Test(groups = "functest")
    public void terminateWorkLoadInRunningState()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var executeResult = service.executeWorkLoad(VALID_CONF).blockingGet();
        final var runId = executeResult.getWorkLoadEntries().get(0).getRunId();

        // Do not wait for workload to complete.
        final var terminateResult = service.terminateWorkLoad(runId).blockingGet();

        assertTrue(terminateResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(terminateResult.getStatus() == ResultStatus.WORKLOAD_TERMINATED, INCORRECT_RESULT_STATUS);
        assertTrue(terminateResult.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);

        final var getResult = service.getWorkLoad(runId).blockingGet();

        assertTrue(getResult.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(getResult.getStatus() == ResultStatus.WORKLOAD_FETCHED, INCORRECT_RESULT_STATUS);
        assertTrue(getResult.getWorkLoadEntries().size() == 1, INCORRECT_NUMBER_OF_ENTRIES);
        assertTrue(getResult.getWorkLoadEntries().get(0).getRunId().equals(runId), INCORRECT_RUN_ID);
        assertTrue(getResult.getWorkLoadEntries().get(0).getState() == WorkLoadState.TERMINATED, INCORRECT_WORKLOAD_STATE);
    }

    @Test(groups = "functest")
    public void terminateWorkLoadNotFound()
    {
        final var service = new BsfLoadService(METRICS_HANDLER, VERTX);

        final var result = service.terminateWorkLoad(UUID.randomUUID()).blockingGet();

        assertTrue(result.getInvalidParams().isEmpty(), INCORRECT_INVALID_PARAMS);
        assertTrue(result.getStatus() == ResultStatus.NOT_FOUND, INCORRECT_RESULT_STATUS);
        assertTrue(result.getWorkLoadEntries().isEmpty(), INCORRECT_NUMBER_OF_ENTRIES);
    }
}
