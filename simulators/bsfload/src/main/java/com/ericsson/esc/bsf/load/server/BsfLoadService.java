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
 * Created on: Nov 18, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.core.WorkLoad;
import com.ericsson.esc.bsf.load.metrics.MetricsHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.vertx.reactivex.core.Vertx;

/**
 * Provides a management interface to control the life cycle of WorkLoads.
 */
public class BsfLoadService
{
    private static final Logger log = LoggerFactory.getLogger(BsfLoadService.class);

    private AtomicBoolean busy;
    private AtomicReference<Disposable> workloadDisp;
    private final ConcurrentHashMap<UUID, WorkLoadEntry> workloads;
    private final MetricsHandler metricsHandler;
    private final Vertx vertx;

    /**
     * The result status after executing a service operation.
     */
    public enum ResultStatus
    {
        INVALID_CONFIGURATION,
        INVALID_OPERATION,
        NOT_FOUND,
        SERVICE_BUSY,
        WORKLOAD_CREATED,
        WORKLOAD_DELETED,
        WORKLOAD_FETCHED,
        WORKLOAD_TERMINATED
    }

    /**
     * The state of a WorkLoad.
     */
    public enum WorkLoadState
    {
        COMPLETED,
        ERROR,
        RUNNING,
        TERMINATED
    }

    public BsfLoadService(MetricsHandler metricsHandler,
                          Vertx vertx)
    {
        this.busy = new AtomicBoolean(false);
        this.metricsHandler = metricsHandler;
        this.workloads = new ConcurrentHashMap<>();
        this.workloadDisp = new AtomicReference<>(null);
        this.vertx = vertx;
    }

    /**
     * Create and execute a workload.
     * <ul>
     * <li>If configuration is invalid, return INVALID_CONFIGURATION.</li>
     * <li>If service is not idle, return SERVICE_BUSY.</li>
     * <li>If configuration is valid and service is idle, return
     * WORKLOAD_CREATED.</li>
     * </ul>
     * 
     * @param configuration The BsfLoadConfiguration.
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> executeWorkLoad(BsfLoadConfiguration configuration)
    {
        return Single.defer(() ->
        {
            final var invalidParams = configuration.validate();

            if (invalidParams.isEmpty())
            {
                if (this.busy.compareAndSet(false, true))
                {
                    return this.startWorkLoad(configuration)
                               .doOnError(e -> setBusy(false))
                               .map(entry -> ServiceResult.withWorkLoadEntries(ResultStatus.WORKLOAD_CREATED, List.of(entry)));
                }
                else
                {
                    return Single.just(ServiceResult.withStatus(ResultStatus.SERVICE_BUSY));
                }
            }
            else
            {
                log.info("Received invalid configuration with the following invalid parameters {}", invalidParams);
                return Single.just(ServiceResult.withInvalidParams(ResultStatus.INVALID_CONFIGURATION, invalidParams));
            }
        });
    }

    /**
     * Fetch the stored workloads.
     * 
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> getWorkLoads()
    {
        return Single.defer(() ->
        {
            final var entries = workloads.values();
            return Single.just(ServiceResult.withWorkLoadEntries(ResultStatus.WORKLOAD_FETCHED, List.copyOf(entries)));
        });
    }

    /**
     * Fetch the workload information.
     * <ul>
     * <li>If workload does not exist, return NOT_FOUND.</li>
     * <li>If workload exists, return WORKLOAD_FETCHED.</li>
     * </ul>
     * 
     * @param runId The identifier of the workload.
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> getWorkLoad(UUID runId)
    {
        return Single.defer(() ->
        {
            final var entry = Optional.ofNullable(workloads.get(runId));
            return entry.isEmpty() ? Single.just(ServiceResult.withStatus(ResultStatus.NOT_FOUND))
                                   : Single.just(ServiceResult.withWorkLoadEntries(ResultStatus.WORKLOAD_FETCHED, List.of(entry.get())));
        });
    }

    /**
     * Delete all workloads that are not in RUNNING state.
     * 
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> deleteWorkLoads()
    {
        return Single.defer(() ->
        {
            final var keys = Collections.list(workloads.keys());

            keys.stream().forEach(key ->
            {
                // Remove all workload entries that are not in RUNNING.
                if (!workloads.get(key).getState().equals(WorkLoadState.RUNNING))
                {
                    workloads.remove(key);
                }
            });

            return Single.just(ServiceResult.withStatus(ResultStatus.WORKLOAD_DELETED));
        });

    }

    /**
     * Delete a workload.
     * <ul>
     * <li>If workload does not exist, return NOT_FOUND.</li>
     * <li>If workload is running, return INVALID_OPERATION.</li>
     * <li>If workload exists and not running, return WORKLOAD_DELETED.</li>
     * </ul>
     * 
     * @param runId The identifier of the workload.
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> deleteWorkLoad(UUID runId)
    {
        return Single.defer(() ->
        {
            final var workload = Optional.ofNullable(workloads.get(runId));
            if (workload.isEmpty())
            {
                return Single.just(ServiceResult.withStatus(ResultStatus.NOT_FOUND));
            }
            else
            {
                if (workload.get().getState().equals(WorkLoadState.RUNNING))
                {
                    return Single.just(ServiceResult.withStatus(ResultStatus.INVALID_OPERATION));
                }
                else
                {
                    // Workload not in RUNNING state. Not possible to return to RUNNING.
                    workloads.remove(runId);
                    return Single.just(ServiceResult.withStatus(ResultStatus.WORKLOAD_DELETED));
                }
            }
        });
    }

    /**
     * Terminate a workload.
     * <ul>
     * <li>If workload does not exist, return NOT_FOUND.</li>
     * <li>If workload is not running, return INVALID_OPERATION.</li>
     * <li>If workload exists and running, return WORKLOAD_TERMINATED.</li>
     * </ul>
     * 
     * @param runId The identifier of the workload.
     * @return A Single with the result of the operation.
     */
    public Single<ServiceResult> terminateWorkLoad(UUID runId)
    {
        return Single.defer(() ->
        {
            final var entry = Optional.ofNullable(workloads.get(runId));
            if (entry.isEmpty())
            {
                return Single.just(ServiceResult.withStatus(ResultStatus.NOT_FOUND));
            }
            else
            {
                if (entry.get().getState().equals(WorkLoadState.RUNNING))
                {
                    return this.stopWorkLoad(entry.get())
                               .doOnError(e -> setBusy(false))
                               .andThen(Single.just(ServiceResult.withStatus(ResultStatus.WORKLOAD_TERMINATED)));
                }
                else
                {
                    return Single.just(ServiceResult.withStatus(ResultStatus.INVALID_OPERATION));
                }
            }
        });
    }

    /**
     * Toggle busy state to the desired value.
     * 
     * Throws: IllegaleStateException - Not expected busy state found.
     * 
     * @param val The desired busy state.
     */
    private void setBusy(boolean val)
    {
        final var expected = this.busy.compareAndSet(!val, val);
        if (!expected)
        {
            throw new IllegalStateException("BsfLoadService in illegal busy state. This should never happen.");
        }
    }

    /**
     * Dispose a WorkLoad properly and reset the disposable variable.
     */
    private void disposeWorkLoad()
    {
        final var disposable = this.workloadDisp.get();
        if (disposable != null && !disposable.isDisposed())
        {
            disposable.dispose();
            this.workloadDisp.compareAndSet(disposable, null);
        }
    }

    /**
     * Start a new WorkLoad:
     * <ul>
     * <li>Create a new WorkLoad entry and store it.</li>
     * <li>Start the WorkLoad execution.</li>
     * <li>Define completion and error handling.</li>
     * </ul>
     * 
     * @param configuration The BsfLoadConfiguration of the WorkLoad.
     * @return A Single with the new stored WorkLoad entry.
     */
    private Single<WorkLoadEntry> startWorkLoad(BsfLoadConfiguration configuration)
    {
        return Single.defer(() ->
        {
            final var runId = UUID.randomUUID();
            final var workload = new WorkLoad(configuration, runId, this.metricsHandler, this.vertx);
            final var entry = new WorkLoadEntry(runId, workload, WorkLoadState.RUNNING, configuration);
            this.workloads.put(entry.getRunId(), entry);

            final Action doOnComplete = () ->
            {
                this.updateWorkLoadStatus(entry, WorkLoadState.COMPLETED);
                this.disposeWorkLoad();
            };

            final Consumer<Throwable> doOnError = e ->
            {
                final var newEntry = this.updateWorkLoadStatus(entry, WorkLoadState.ERROR);
                this.disposeWorkLoad();

                log.error("The execution of WorkLoad {} was interrupted due to fatal error {}", newEntry, e);
            };

            this.workloadDisp.set(entry.getWorkLoad().run().subscribe(doOnComplete, doOnError));

            return Single.just(entry);
        });
    }

    /**
     * Stop a running WorkLoad and update its WorkLoadEntry status.
     * 
     * @param entry The WorkLoadEntry to be updated.
     * @return Completable that completes when the WorkLoad is stopped and disposed.
     */
    private Completable stopWorkLoad(WorkLoadEntry entry)
    {
        final var newEntry = updateWorkLoadStatus(entry, WorkLoadState.TERMINATED);
        return newEntry.getWorkLoad().stop().andThen(Completable.fromAction(this::disposeWorkLoad));
    }

    /**
     * Used after the WorkLoad has concluded. Resets the busy state to false and
     * updates the WorkLoad status according to the WorkLoad termination cause.
     * 
     * @param entry The WorkLoadEntry to be updated.
     * @param state The WorkLoadState representing the reason why the WorkLoad
     *              concluded.
     * @return The updated WorkLoadEntry.
     */
    private WorkLoadEntry updateWorkLoadStatus(WorkLoadEntry entry,
                                               WorkLoadState state)
    {
        this.setBusy(false);
        final var newEntry = new WorkLoadEntry(entry, state);
        this.workloads.put(newEntry.getRunId(), newEntry);
        return newEntry;
    }

    /**
     * Structure to store WorkLoad related information.
     */
    public static class WorkLoadEntry
    {
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());

        private final UUID runId;
        private final WorkLoadState state;
        private final String createdAt;
        private final BsfLoadConfiguration configuration;

        @JsonIgnore
        private final WorkLoad workLoad;

        WorkLoadEntry(UUID runId,
                      WorkLoad workLoad,
                      WorkLoadState state,
                      BsfLoadConfiguration configuration)
        {
            this.runId = runId;
            this.workLoad = workLoad;
            this.state = state;
            this.createdAt = ZonedDateTime.now().format(FORMAT);
            this.configuration = configuration;
        }

        WorkLoadEntry(WorkLoadEntry entry,
                      WorkLoadState state)
        {
            this.state = state;
            this.workLoad = entry.getWorkLoad();
            this.runId = entry.getRunId();
            this.createdAt = entry.getCreatedAt();
            this.configuration = entry.getConfiguration();
        }

        /**
         * @return the runId
         */
        public UUID getRunId()
        {
            return this.runId;
        }

        /**
         * @return the workLoad
         */
        public WorkLoad getWorkLoad()
        {
            return this.workLoad;
        }

        /**
         * @return the state
         */
        public WorkLoadState getState()
        {
            return this.state;
        }

        /**
         * @return the createdAt
         */
        public String getCreatedAt()
        {
            return createdAt;
        }

        /**
         * @return the configuration
         */
        public BsfLoadConfiguration getConfiguration()
        {
            return this.configuration;
        }

        @Override
        public String toString()
        {
            return "WorkLoadEntry [runId=" + runId + ", state=" + state + ", createdAt=" + createdAt + ", configuration=" + configuration + ", workLoad="
                   + workLoad + "]";
        }
    }
}
