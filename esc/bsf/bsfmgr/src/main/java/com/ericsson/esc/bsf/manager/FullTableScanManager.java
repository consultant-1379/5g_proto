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
 * Created on: Jul 21, 2022
 *     Author: zpavcha
 */

package com.ericsson.esc.bsf.manager;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.servererrors.ReadFailureException;
import com.ericsson.esc.bsf.db.BindingRtInfo;
import com.ericsson.esc.bsf.db.FullTableScanParameters;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.BsfQuery;
import com.ericsson.esc.bsf.worker.BsfSchema;
import com.ericsson.esc.bsf.worker.MalformedDbContentException;
import com.ericsson.sc.bsf.etcd.PcfRtService;
import com.ericsson.sc.bsf.etcd.PcfRtService.Source;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.EricssonBsfCurrentScan;
import com.ericsson.sc.bsf.model.EricssonBsfLastScan;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.cassandra.RxSession.SessionHolder;
import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.prometheus.client.Gauge;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the full table scan execution.
 */
public class FullTableScanManager
{
    public enum FailureReason
    {
        READ_FAILURE,
        BINDING_DATABASE_UNREACHABLE,
        BSF_SCHEMA_INVALID,
        OTHER
    }

    /**
     * The Scan state
     *
     */
    public enum State
    {
        /**
         * No ongoing scanning
         */
        IDLE,
        /**
         * Database scanning ongoing
         */
        SCANNING
    }

    private static final Logger log = LoggerFactory.getLogger(FullTableScanManager.class);
    private static final AtomicLong sessionId = new AtomicLong();

    // General
    private static final String START = "Binding database scan is starting, page size = {} bindings, throttling = {} ms";
    private static final String COMPLETED = "Binding database scan completed, duration = {} seconds, number of fetched pages = {}, number of scanned bindings = {}, number of stale bindings = {}, number of deleted bindings = {}";

    // Errors
    private static final String INTERR_EXC_RECV = "Interrupted exception received";

    private final RxSession session;
    private final PcfRtService pcfRtService;
    private final FullTableScanParameters fullTableScanParameters;
    private final Counters counters = new Counters();
    private final SimpleStatement selectStatement;
    private final BindingCleanupManager bindingCleanupManager;

    private final Subject<Boolean> cmdSubject = PublishSubject.<Boolean>create().toSerialized();
    private final AtomicReference<ScanSession> currentSession = new AtomicReference<>();
    private final ConnectableObservable<State> scanTriggerChain;

    private final Flowable<Boolean> scanTriggers;

    // Params related to check scan status action
    private final AtomicReference<EricssonBsfLastScan> lastScanState = new AtomicReference<>();

    private final AtomicReference<Instant> currentScanStarted = new AtomicReference<>();
    private final AtomicBoolean dbReady = new AtomicBoolean();

    /**
     * Create a new manager that can be controlled via a flow of configuration
     * objects. Scans are triggered automatically according to current
     * configuration. Additionally, scans can be controlled manually via
     * {@link #triggerScan()} and {@link #cancelScan()} methods.
     * 
     * @param pcfRtService
     * @param fullTableScanParameters
     * @param dbConfiguration
     * @param bindingCleanupManager
     * @param scanConfig
     */
    public FullTableScanManager(final PcfRtService pcfRtService,
                                final FullTableScanParameters fullTableScanParameters,
                                final BindingCleanupManager bindingCleanupManager,
                                final Flowable<BindingDatabaseScan> scanConfig)
    {
        Objects.requireNonNull(pcfRtService);
        Objects.requireNonNull(fullTableScanParameters);
        Objects.requireNonNull(bindingCleanupManager);
        Objects.requireNonNull(scanConfig);

        this.pcfRtService = pcfRtService;
        this.fullTableScanParameters = fullTableScanParameters;
        this.bindingCleanupManager = bindingCleanupManager;
        this.session = this.bindingCleanupManager.getSession();
        this.scanTriggers = DbScanConfigMapper.generateScanCommands(scanConfig.distinctUntilChanged());

        final var keyspace = this.bindingCleanupManager.getKeyspace();
        this.selectStatement = BsfQuery.createStatements(keyspace, true).get(BsfQuery.FULLTABLESCAN);

        this.scanTriggerChain = cmdSubject //
                                          .concatMapSingle(cmd -> cmd.booleanValue() ? this.checkDbInitialized(keyspace) : Single.just(false)) //
                                          .<State>concatMapSingle(cmd ->
                                          {
                                              if (Boolean.TRUE.equals(cmd))
                                              {
                                                  final var newSession = new ScanSession();
                                                  if (this.currentSession.compareAndSet(null, newSession))
                                                  {
                                                      newSession //
                                                                .onFinished()
                                                                .subscribe(() -> this.currentSession.compareAndSet(newSession, null), //
                                                                           err -> log.error("Failed to initiate scan session", err));
                                                  }
                                                  else
                                                  {
                                                      log.info("Scan session ongoing, not starting new");
                                                  }

                                                  return Single.just(State.SCANNING);
                                              }
                                              else
                                              {
                                                  final var sess = this.currentSession.get();
                                                  if (sess != null)
                                                  {
                                                      return sess //
                                                                 .stop()
                                                                 .doFinally(() -> this.currentSession.compareAndSet(sess, null))
                                                                 .toSingleDefault(State.IDLE);
                                                  }
                                                  else
                                                  {
                                                      log.debug("Scan session already stopped");
                                                  }
                                              }
                                              return Single.just(State.IDLE);
                                          }) //
                                          .distinctUntilChanged()
                                          .replay(1);
        this.scanTriggerChain.connect(); // Subscribe to chain immediately
    }

    /**
     * Checks whether the binding database is initialized until the BSF schema is
     * validated to be in place.
     * 
     * @param keyspace The database keyspace to check.
     * @return A {@link Single} that emits true if the binding database is
     *         initialized, false otherwise.
     */
    private Single<Boolean> checkDbInitialized(final String keyspace)
    {
        final var doCheck = BsfSchemaHandler.verifyBsfDb(this.session, keyspace, 0, 0)
                                            .toSingleDefault(true)
                                            .onErrorReturnItem(false) //
                                            .doOnSuccess(res ->
                                            {
                                                if (res.booleanValue())
                                                {
                                                    dbReady.set(true);
                                                }
                                            });

        return Single.fromCallable(dbReady::get) //
                     .flatMap(res -> res.booleanValue() ? Single.just(true) : doCheck)
                     .doOnSuccess(dbready ->
                     {
                         if (!dbready.booleanValue())
                         {
                             log.info("Binding database scan not triggered because BSF database is not yet initialized");
                         }
                     });
    }

    /**
     * Trigger a new db scan. Method returns immediately. If scan is ongoing, no new
     * scan will be initiated
     */
    public void triggerScan()
    {
        this.cmdSubject.onNext(true);
    }

    /**
     * Cancel a possibly ongoing db scan. If not scan is ongoing the request is
     * ignored. The method returns immediately.
     */
    public void cancelScan()
    {
        this.cmdSubject.onNext(false);
    }

    /**
     * Starts processing and applying scan configuration
     * 
     * @return A {@link Completable} that never completes.
     */
    public Completable run()
    {
        return this.scanTriggers //
                                .doOnNext(this::applyScanCmd)
                                .ignoreElements()
                                .mergeWith(this.scanTriggerChain.ignoreElements());
    }

    /**
     * Create a manager that does not accept commands. Starting/stopping can be
     * triggered only via {@link #triggerScan()} and {@link #cancelScan()} methods
     * 
     * @param pcfRtService
     * @param fullTableScanParameters
     * @param dbConfiguration
     * @param bindingCleanupManager
     */
    FullTableScanManager(final PcfRtService pcfRtService,
                         final FullTableScanParameters fullTableScanParameters,
                         final BindingCleanupManager bindingCleanupManager)
    {
        this(pcfRtService, fullTableScanParameters, bindingCleanupManager, Flowable.never());
    }

    /**
     * 
     * @param sessionHolder
     * @return A Completable that will initiate a scan upon subscription. The scan
     *         cannot be cancelled.
     */
    Completable fullTableScan(final SessionHolder sessionHolder)
    {
        return fullTableScan(sessionHolder, new AtomicBoolean(false));
    }

    /**
     * A full table scan process. It traverses all Cassandra rows and for each row
     * it deletes stale bindings.
     * 
     * @param sessionHolder The sessionHolder that corresponds to a Cassandra
     *                      session.
     * @param canceled      A boolean that can be used to cancel an ongoing scan
     * @return A {@link Completable} that performs the scan operation upon
     *         subscription
     */
    Completable fullTableScan(final SessionHolder sessionHolder,
                              final AtomicBoolean cancel)
    {
        return Completable.fromAction(() ->
        {
            EricssonBsfLastScan.Status lastScanStatus = null;
            final var lastScanStarted = Instant.now();

            try
            {
                log.info(START, this.fullTableScanParameters.getPageSize(), this.fullTableScanParameters.getPageThrottlingMillis());

                final var startTime = Instant.now();
                this.currentScanStarted.set(startTime);

                // state
                final var pageSize = this.fullTableScanParameters.getPageSize();
                final var pageThrottlingMillis = this.fullTableScanParameters.getPageThrottlingMillis();
                final var deleteThrottlingMillis = this.fullTableScanParameters.getDeleteThrottlingMillis();
                final var cqlSession = sessionHolder.getCqlSession();
                final var currentPage = new AtomicInteger();

                final var numberOfScannedBindings = new AtomicInteger();
                final var numberOfStaleBindings = new AtomicInteger();
                final var numberOfDeletedBindings = new AtomicInteger();

                final var pagedSelectStatement = this.selectStatement.setPageSize(pageSize);

                this.stepScansStartedTotalCounter(this.bindingCleanupManager.getNfInstanceId());

                final var resultSet = cqlSession.execute(pagedSelectStatement);

                for (final var row : resultSet)
                {
                    if (cancel.get())
                    {
                        lastScanStatus = EricssonBsfLastScan.Status.ABORTED;
                        log.info("DB scan cancelled");
                        return;
                    }
                    final var bindingScanInfo = this.createBindingScanInfoFromRow(row);

                    this.stepBindingsScannedTotalCounter(bindingScanInfo.getPcfId(), this.bindingCleanupManager.getNfInstanceId());

                    this.updateBindingsScannedCurrentCounter(this.bindingCleanupManager.getNfInstanceId(), numberOfScannedBindings.incrementAndGet());

                    this.pcfRtService.checkStaleBinding(bindingScanInfo).blockingGet().ifPresent(stalePcfBinding ->
                    {
                        this.stepBindingsStaleTotalCounter(bindingScanInfo.getPcfId().get(), this.bindingCleanupManager.getNfInstanceId());
                        this.updateBindingsStaleCurrentCounter(this.bindingCleanupManager.getNfInstanceId(), numberOfStaleBindings.incrementAndGet());

                        this.bindingCleanupManager.deleteBinding(sessionHolder, stalePcfBinding, Source.BINDING_DATABASE_SCAN)
                                                  .doOnComplete(() -> this.updateBindingsDeletedCurrentCounter(this.bindingCleanupManager.getNfInstanceId(),
                                                                                                               numberOfDeletedBindings.incrementAndGet()))
                                                  .onErrorComplete()
                                                  .blockingAwait();

                        this.throttle(deleteThrottlingMillis);
                    });

                    this.sleepAfterPageChanged(resultSet, currentPage, pageThrottlingMillis);
                }

                final var endTime = Instant.now();
                final var duration = Duration.between(startTime, endTime).toSeconds();

                log.info(COMPLETED, duration, currentPage.get(), numberOfScannedBindings.get(), numberOfStaleBindings.get(), numberOfDeletedBindings.get());

                lastScanStatus = EricssonBsfLastScan.Status.COMPLETED;
                this.stepScansCompletedTotalCounter(this.bindingCleanupManager.getNfInstanceId());
                this.updateBindingDatabaseScanDurationSeconds(this.bindingCleanupManager.getNfInstanceId(), duration);
            }
            catch (ReadFailureException rfe)
            {
                lastScanStatus = EricssonBsfLastScan.Status.FAILED;
                throw new BindingDatabaseScanException(FailureReason.READ_FAILURE, rfe);
            }
            catch (AllNodesFailedException anfe)
            {
                lastScanStatus = EricssonBsfLastScan.Status.FAILED;
                throw new BindingDatabaseScanException(FailureReason.BINDING_DATABASE_UNREACHABLE, anfe);
            }
            catch (InvalidQueryException iqe)
            {
                lastScanStatus = EricssonBsfLastScan.Status.FAILED;
                throw new BindingDatabaseScanException(FailureReason.BSF_SCHEMA_INVALID, iqe);
            }
            catch (Exception ex)
            {
                lastScanStatus = EricssonBsfLastScan.Status.FAILED;
                throw new BindingDatabaseScanException(FailureReason.OTHER, ex);
            }
            finally
            {
                this.currentScanStarted.set(null);
                final var lastScanStopped = Instant.now();
                this.lastScanState.set(new EricssonBsfLastScan().withStatus(lastScanStatus)
                                                                .withScannedBindings(Math.round(this.counters.getGaugeBindingsScannedCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                                             .get()))
                                                                .withDeletedBindings(Math.round(this.counters.getGaugeBindingsDeletedCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                                             .get()))
                                                                .withStaleBindings(Math.round(this.counters.getGaugeBindingsStaleCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                                           .get()))
                                                                .withStarted(lastScanStarted.toString())
                                                                .withStopped(lastScanStopped.toString()));
                // Gauge counters reset
                this.updateBindingsScannedCurrentCounter(this.bindingCleanupManager.getNfInstanceId(), 0);
                this.updateBindingsStaleCurrentCounter(this.bindingCleanupManager.getNfInstanceId(), 0);
                this.updateBindingsDeletedCurrentCounter(this.bindingCleanupManager.getNfInstanceId(), 0);
            }

        })
                          .doOnError(err -> this.stepScansFailedTotalCounter(this.getFailureReason(err), this.bindingCleanupManager.getNfInstanceId()))
                          .subscribeOn(Schedulers.newThread());
    }

    /**
     * Map each error thrown from the binding database scan process into a failure
     * reason to be used in the corresponding label of the failed scans counter.
     * 
     * @param error The error thrown.
     * @return The failure reason.
     */
    private String getFailureReason(final Throwable error)
    {
        if (!(error instanceof BindingDatabaseScanException))
        {
            return FailureReason.OTHER.toString(); // this should never happen
        }

        final var bindingDatabaseError = (BindingDatabaseScanException) error;

        return bindingDatabaseError.getReason().equals(FailureReason.BSF_SCHEMA_INVALID) ? FailureReason.OTHER.toString()
                                                                                         : bindingDatabaseError.getReason().toString();
    }

    /**
     * Trigger or cancel a DB scan
     * 
     * @param cmd If true scan is triggered otherwise scan is cancelled
     */
    private void applyScanCmd(boolean cmd)
    {
        if (cmd)
        {
            triggerScan();
        }
        else
        {
            cancelScan();
        }
    }

    /**
     * Causes the thread to sleep after a page has changed.
     * 
     * @param resultSet            The resultSet.
     * @param currentPage          The serial number that corresponds to the current
     *                             page being fetched.
     * @param pageThrottlingMillis The sleep time in milliseconds.
     */
    private void sleepAfterPageChanged(final ResultSet resultSet,
                                       final AtomicInteger currentPage,
                                       final long pageThrottlingMillis)
    {
        if (this.pageChanged(resultSet, currentPage))
        {
            this.throttle(pageThrottlingMillis);
        }
    }

    /**
     * Provides a validation of whether the page is changed in a resultSet.
     * 
     * @param resultSet   The resultSet.
     * @param currentPage The serial number that corresponds to the current page
     *                    being fetched.
     * @return True is the page is changed, false otherwise.
     */
    private boolean pageChanged(final ResultSet resultSet,
                                final AtomicInteger currentPage)
    {
        if (resultSet.getAvailableWithoutFetching() == 0 && !resultSet.isFullyFetched())
        {
            currentPage.incrementAndGet();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Causes the thread to sleep.
     * 
     * @param pageThrottlingMillis The sleep time in milliseconds.
     */
    private void throttle(final long pageThrottlingMillis)
    {
        try
        {
            Thread.sleep(pageThrottlingMillis);
        }
        catch (final InterruptedException ex)
        {
            log.error(INTERR_EXC_RECV, ex);
            Thread.currentThread().interrupt();
        }
    }

    private BindingRtInfo createBindingScanInfoFromRow(final Row row)
    {
        try
        {
            final var bindingId = row.getUuid(BsfSchema.pcf_bindings.binding_id.column());
            final var writeTime = Instant.EPOCH.plus(row.getLong(BsfQuery.WRITE_TIME_COL), ChronoUnit.MICROS);
            final var pcfId = Optional.ofNullable(row.getUuid(BsfSchema.pcf_bindings.pcf_id.column()));
            final var recoveryTime = Optional.ofNullable(row.getString(BsfSchema.pcf_bindings.recovery_time.column()))
                                             .flatMap(pcfid -> Optional.of(new RecoveryTime(pcfid)));

            return new BindingRtInfo(bindingId, writeTime, pcfId, recoveryTime);
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + row.getFormattedContents(), e); // This should never happen
        }
    }

    private void stepBindingsScannedTotalCounter(final Optional<UUID> pcfId,
                                                 final String nfInstance)
    {

        log.debug("Stepping bindings scanned total counter, pcfId: {}, nfInstance: {}", pcfId, nfInstance);

        this.counters.getCcBindingsScannedTotal(pcfId, nfInstance).inc();
    }

    private void stepBindingsStaleTotalCounter(final UUID pcfId,
                                               final String nfInstance)
    {

        log.debug("Stepping stale bindings total counter, pcfId: {}, nfInstance: {}", pcfId, nfInstance);

        this.counters.getCcBindingsStaleTotal(pcfId, nfInstance).inc();
    }

    private void stepScansStartedTotalCounter(final String nfInstance)
    {

        log.debug("Stepping started scans total counter, nfInstance: {}", nfInstance);

        this.counters.getCcScansStartedTotal(nfInstance).inc();
    }

    private void stepScansFailedTotalCounter(final String reason,
                                             final String nfInstance)
    {

        log.debug("Stepping failed scans total counter, nfInstance: {}", nfInstance);

        this.counters.getCcScansFailedTotal(reason, nfInstance).inc();
    }

    private void stepScansCompletedTotalCounter(final String nfInstance)
    {

        log.debug("Stepping completed scans total counter, nfInstance: {}", nfInstance);

        this.counters.getCcScansCompletedTotal(nfInstance).inc();
    }

    private void updateBindingsScannedCurrentCounter(final String nfInstance,
                                                     final double value)
    {
        log.debug("Updating current bindings scanned counter, nfInstance: {}", nfInstance);

        this.counters.getGaugeBindingsScannedCurrent(nfInstance).set(value);
    }

    private void updateBindingsStaleCurrentCounter(final String nfInstance,
                                                   final double value)
    {
        log.debug("Updating current bindings stale counter, nfInstance: {}", nfInstance);

        this.counters.getGaugeBindingsStaleCurrent(nfInstance).set(value);
    }

    private void updateBindingsDeletedCurrentCounter(final String nfInstance,
                                                     final double value)
    {
        log.debug("Updating current bindings deleted counter, nfInstance: {}", nfInstance);

        this.counters.getGaugeBindingsDeletedCurrent(nfInstance).set(value);
    }

    private void updateBindingDatabaseScanDurationSeconds(final String nfInstance,
                                                          final double value)
    {
        log.debug("Updating binding database scan duration counter, nfInstance: {}", nfInstance);

        this.counters.getGaugeBindingDatabaseScanDurationSeconds(nfInstance).set(value);
    }

    private class ScanSession
    {
        final Completable scanProcess;
        final AtomicBoolean canceled = new AtomicBoolean();
        final long id;

        ScanSession()
        {
            this.id = FullTableScanManager.sessionId.incrementAndGet();
            scanProcess = scan().cache();
        }

        private void cancel()
        {
            canceled.set(true);
        }

        Completable stop()
        {
            return Completable.defer(() ->
            {
                log.info("Cancelling scan session {}", this);
                cancel();
                return scanProcess.onErrorComplete()
                                  .timeout(20, TimeUnit.SECONDS) // Timeout after 20 seconds if stop is hanging
                                  .doOnError(err -> log.warn("Unexpected error while stopping scan process", err))
                                  .onErrorComplete()
                                  .doFinally(() -> log.info("Canceled scan session {}", this));
            });

        }

        Completable onFinished()
        {
            return this.scanProcess.onErrorComplete();
        }

        private Completable scan()
        {
            return session.sessionHolder()
                          .flatMapCompletable(sh -> fullTableScan(sh, canceled) //
                                                                               .doOnSubscribe(sub -> log.info("Triggered scan session {}", this)))
                          .doFinally(() -> log.info("Finished scan session {}", this))
                          .doFinally(() -> canceled.set(true)); // safety measure, not actually needed
        }

        @Override
        public String toString()
        {
            return "Scan session " + id;
        }

    }

    /**
     * Get the statistics of the last table scan execution
     * 
     * @return lastStats
     */
    public Optional<EricssonBsfLastScan> getLastScanState()
    {
        final var lastStats = lastScanState.get();
        return Optional.ofNullable(lastStats);
    }

    /**
     * Get the statistics of the current-runtime table scan execution
     * 
     * @return currentStats
     */
    public EricssonBsfCurrentScan getCurrentScanState()
    {
        final var scanStarted = this.currentScanStarted.get();
        final var currentStatus = scanStarted == null ? EricssonBsfCurrentScan.CurrentStatus.NOT_RUNNING : EricssonBsfCurrentScan.CurrentStatus.RUNNING;
        return new EricssonBsfCurrentScan().withCurrentStatus(currentStatus)
                                           .withStarted(scanStarted == null ? null : scanStarted.toString())
                                           .withScannedBindings(Math.round(this.counters.getGaugeBindingsScannedCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                        .get()))
                                           .withDeletedBindings(Math.round(this.counters.getGaugeBindingsDeletedCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                        .get()))
                                           .withStaleBindings(Math.round(this.counters.getGaugeBindingsStaleCurrent(this.bindingCleanupManager.getNfInstanceId())
                                                                                      .get()));
    }

    public Counters getCounters()
    {
        return this.counters;
    }

    static class Counters
    {
        private static final String NF_LBL = "nf";
        private static final String NF_INSTANCE_LBL = "nf_instance";
        private static final String PCF_ID_LBL = "pcf_id";
        private static final String BSF_NF_NAME = "bsf";
        private static final String PCF_UNKNOWN = "unknown";

        // CC counters

        /**
         * Prometheus CC counter for total scanned bindings.
         */
        private static final Counter ccBindingsScannedTotal = Counter.build()
                                                                     .namespace(BSF_NF_NAME)
                                                                     .name("bindings_scanned_total")
                                                                     .labelNames(PCF_ID_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                     .help("Number of bindings scanned from the Binding Database Scan operation")
                                                                     .register();

        public Child getCcBindingsScannedTotal(final Optional<UUID> pcfId,
                                               final String nfInstance)
        {
            return ccBindingsScannedTotal.labels(pcfId.isPresent() ? pcfId.get().toString() : PCF_UNKNOWN, // pcfId
                                                 nfInstance, // nf_instance
                                                 BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for total stale bindings.
         */
        private static final Counter ccBindingsStaleTotal = Counter.build()
                                                                   .namespace(BSF_NF_NAME)
                                                                   .name("bindings_stale_total")
                                                                   .labelNames(PCF_ID_LBL, NF_INSTANCE_LBL, NF_LBL)
                                                                   .help("Number of bindings that were identified as stale in terms of PCF recovery time from the Binding Database Scan operation")
                                                                   .register();

        public Child getCcBindingsStaleTotal(final UUID pcfId,
                                             final String nfInstance)
        {
            return ccBindingsStaleTotal.labels(pcfId.toString(), // pcfId
                                               nfInstance, // nf_instance
                                               BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for started full table scans.
         */
        private static final Counter ccScansStartedTotal = Counter.build()
                                                                  .namespace(BSF_NF_NAME)
                                                                  .name("binding_database_scans_started_total")
                                                                  .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                  .help("Number of started Binding Database Scans")
                                                                  .register();

        public Child getCcScansStartedTotal(final String nfInstance)
        {
            return ccScansStartedTotal.labels(nfInstance, // nf_instance
                                              BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for failed full table scans.
         */
        private static final Counter ccScansFailedTotal = Counter.build()
                                                                 .namespace(BSF_NF_NAME)
                                                                 .name("binding_database_scans_failed_total")
                                                                 .labelNames("reason", NF_INSTANCE_LBL, NF_LBL)
                                                                 .help("Number of failed Binding Database Scans")
                                                                 .register();

        public Child getCcScansFailedTotal(final String reason,
                                           final String nfInstance)
        {
            return ccScansFailedTotal.labels(reason.toLowerCase(),
                                             nfInstance, // nf_instance
                                             BSF_NF_NAME); // nf
        }

        /**
         * Prometheus CC counter for completed full table scans.
         */
        private static final Counter ccScansCompletedTotal = Counter.build()
                                                                    .namespace(BSF_NF_NAME)
                                                                    .name("binding_database_scans_completed_total")
                                                                    .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                    .help("Number of successfully completed Binding Database Scans")
                                                                    .register();

        public Child getCcScansCompletedTotal(final String nfInstance)
        {
            return ccScansCompletedTotal.labels(nfInstance, // nf_instance
                                                BSF_NF_NAME); // nf
        }

        // Gauge counters

        /**
         * Prometheus gauge counter binding database scan duration.
         */
        private static final Gauge gaugeBindingDatabaseScanDurationSeconds = Gauge.build()
                                                                                  .namespace(BSF_NF_NAME)
                                                                                  .name("binding_database_scan_duration_seconds")
                                                                                  .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                                  .help("Duration of the Binding Database Scan operation in seconds")
                                                                                  .register();

        public Gauge.Child getGaugeBindingDatabaseScanDurationSeconds(final String nfInstance)
        {
            return gaugeBindingDatabaseScanDurationSeconds.labels(nfInstance, // nf_instance
                                                                  BSF_NF_NAME); // nf
        }

        /**
         * Prometheus gauge counter current scanned bindings.
         */
        private static final Gauge gaugeBindingsScannedCurrent = Gauge.build()
                                                                      .namespace(BSF_NF_NAME)
                                                                      .name("bindings_scanned_current")
                                                                      .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                      .help("Number of bindings scanned by the current Binding Database Scan operation")
                                                                      .register();

        public Gauge.Child getGaugeBindingsScannedCurrent(final String nfInstance)
        {
            return gaugeBindingsScannedCurrent.labels(nfInstance, // nf_instance
                                                      BSF_NF_NAME); // nf
        }

        /**
         * Prometheus gauge counter current stale bindings.
         */
        private static final Gauge gaugeBindingsStaleCurrent = Gauge.build()
                                                                    .namespace(BSF_NF_NAME)
                                                                    .name("bindings_stale_current")
                                                                    .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                    .help("Number of bindings that were identified as stale in terms of PCF recovery time from the current Binding Database Scan operation")
                                                                    .register();

        public Gauge.Child getGaugeBindingsStaleCurrent(final String nfInstance)
        {
            return gaugeBindingsStaleCurrent.labels(nfInstance, // nf_instance
                                                    BSF_NF_NAME); // nf
        }

        /**
         * Prometheus gauge counter current deleted bindings.
         */
        private static final Gauge gaugeBindingsDeletedCurrent = Gauge.build()
                                                                      .namespace(BSF_NF_NAME)
                                                                      .name("bindings_stale_deleted_current")
                                                                      .labelNames(NF_INSTANCE_LBL, NF_LBL)
                                                                      .help("Number of bindings deleted by the current Binding Database Scan operation")
                                                                      .register();

        public Gauge.Child getGaugeBindingsDeletedCurrent(final String nfInstance)
        {
            return gaugeBindingsDeletedCurrent.labels(nfInstance, // nf_instance
                                                      BSF_NF_NAME); // nf
        }

    }

    class BindingDatabaseScanException extends RuntimeException
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private final FailureReason reason;

        private BindingDatabaseScanException(final FailureReason reason,
                                             final Throwable cause)
        {
            super(getErrorMessageFromFailureReason(reason), cause);
            this.reason = reason;
        }

        public FailureReason getReason()
        {
            return this.reason;
        }
    }

    protected String getErrorMessageFromFailureReason(final FailureReason reason)
    {
        switch (reason)
        {
            case READ_FAILURE:
                return "High number of tombstones might be found in the database, binding database scan is aborted";
            case BINDING_DATABASE_UNREACHABLE:
                return "The binding database is unreachable, binding database scan is aborted";
            case BSF_SCHEMA_INVALID:
                return "Could not execute binding database scan because BSF schema may not be properly set";
            default:
                return "Binding database scan failed due to unspecified reason";
        }
    }

}
