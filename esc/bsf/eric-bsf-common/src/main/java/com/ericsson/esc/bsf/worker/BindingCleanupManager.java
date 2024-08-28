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
 * Created on: May 10, 2012
 *     Author: echfari
 */
package com.ericsson.esc.bsf.worker;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.ericsson.sc.bsf.etcd.PcfRtService.Source;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.cassandra.RxSession.SessionHolder;
import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.UnicastProcessor;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages deletion of stale/redundant bindings Deletion takes place
 * asynchronously. Deletion requests are queued in a bounded sized queue. If
 * queue becomes full deletion requests are ignored
 *
 */
public class BindingCleanupManager
{
    private static final Logger log = LoggerFactory.getLogger(BindingCleanupManager.class);

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        BUFFER_FULL,
        CLEANUP_BINDING,
        UNEXPECTED_ERROR
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log, 1000);

    private static final long DEFAULT_BUFFER_SIZE = 100000;
    private static final int MAX_PENDING_DELETIONS = 50;
    private final FlowableProcessor<StalePcfBinding> up = UnicastProcessor.<StalePcfBinding>create().toSerialized();
    private final Completable deleteChain;
    private final AtomicReference<String> nfInstanceId = new AtomicReference<>();
    private final Counters counters = new Counters();
    private final RxSession session;
    private final String keyspace;
    private final Single<PreparedStatement> cachedPreparedStatement;
    private Disposable nfInstanceWatcher;

    /**
     * 
     * @param svc The service to use for actually deleting bindings
     */
    public BindingCleanupManager(final RxSession session,
                                 final String keyspace,
                                 final Flowable<String> nfInstance)
    {
        this.session = session;
        this.keyspace = keyspace;

        this.nfInstanceWatcher = nfInstance.subscribe(this::setNfInstanceId,
                                                      err -> log.error("Stoped updating nfInstance Id on cleanup Manager with error: ", err));

        this.deleteChain = up.onBackpressureBuffer(DEFAULT_BUFFER_SIZE,
                                                   () -> safeLog.log(Lbl.BUFFER_FULL, l -> log.warn("Cannot cleanup bindings, buffer is full")),
                                                   BackpressureOverflowStrategy.DROP_LATEST)
                             .flatMapCompletable(sbinding -> session.sessionHolder()
                                                                    .flatMapCompletable(sh -> deleteBinding(sh, sbinding, Source.RESOLVE_UPON_LOOKUP)),
                                                 false,
                                                 MAX_PENDING_DELETIONS)
                             .doOnError(err -> safeLog.log(Lbl.UNEXPECTED_ERROR, l -> l.error("Unexpected error while trying to cleanup binding", err)))
                             .onErrorComplete(); // log and ignore any errors, so that chain is not stopped

        this.deleteChain.subscribe(() ->
        {
        }, err -> log.error("Unexpected fatal error, binding cleanup has stopped", err));

        final var deregisterStatement = BsfQuery.createStatements(this.keyspace, true).get(BsfQuery.DEREGISTER);

        this.cachedPreparedStatement = session.sessionHolder().flatMap(sh -> sh.prepare(deregisterStatement)).cache();
    }

    /**
     * Stop the manager. Note that the manager cannot be restarted
     * 
     * @return A Completable that completes as soon as all queued deletion requests
     *         have completed
     */
    public Completable stop()
    {
        up.onComplete();
        this.nfInstanceWatcher.dispose();
        return deleteChain;
    }

    /**
     * @param nfInstanceId the nfInstanceId to set
     */
    private void setNfInstanceId(final String nfInstanceId)
    {
        this.nfInstanceId.set(nfInstanceId);
    }

    /**
     * @return the nfInstanceId
     */
    public String getNfInstanceId()
    {
        var res = nfInstanceId.get();
        return res == null ? "unknown" : res;
    }

    /**
     * 
     * @return the counters
     */
    public Counters getCounters()
    {
        return this.counters;
    }

    /**
     * 
     * @return the rxSession
     */
    public RxSession getSession()
    {
        return this.session;
    }

    /**
     * 
     * @return the keyspace
     */
    public String getKeyspace()
    {
        return this.keyspace;
    }

    /**
     * Request asynchronous binding deletion
     * 
     * @param staleBinding The binding to be deleted contains bindingId and
     *                     optionally pcfId in case of stale binding deletion (from
     *                     pcfRt functionality)
     */
    public void deleteBindingAsync(final StalePcfBinding staleBinding)
    {
        try
        {
            Objects.requireNonNull(staleBinding);
            safeLog.log(Lbl.CLEANUP_BINDING, l -> l.debug("Cleaning up bindings, bindingId: {}", staleBinding));
            up.onNext(staleBinding);
        }
        catch (Exception e)
        {
            // this should never happen
            safeLog.log(Lbl.UNEXPECTED_ERROR, l -> l.error("Unexpected error while trying to cleanup binding {}", staleBinding, e));
        }
    }

    /**
     * Delete stale binding
     * 
     * @param sessionHolder The current session.
     * @param staleBinding  The binding to be deleted contains bindingId and
     *                      optionally pcfId in case of stale binding deletion (from
     *                      pcfRt functionality)
     * @return A Completable that completes if the deletion is successful.
     */
    public Completable deleteBinding(final SessionHolder sessionHolder,
                                     final StalePcfBinding staleBinding,
                                     final Source source)
    {
        return this.cachedPreparedStatement.flatMap(ps -> sessionHolder.executeReactive(ps.bind(staleBinding.getBindingId()))
                                                                       .toList()
                                                                       .doOnSuccess(success -> this.stepStaleBindingsDeletedCounter(source.toString(),
                                                                                                                                    staleBinding.getReason()
                                                                                                                                                .toString(),
                                                                                                                                    staleBinding.getPcfId(),
                                                                                                                                    this.getNfInstanceId()))
                                                                       .doOnError(err -> log.error("Error in deleting stale binding", err)))
                                           .ignoreElement();

    }

    private void stepStaleBindingsDeletedCounter(final String source,
                                                 final String reason,
                                                 final UUID pcfId,
                                                 final String nfInstanceId)
    {
        log.debug("Stepping stale bindings deleted counter, source: {}, reason: {}, pcfId: {}, nfInstanceId: {}", source, reason, pcfId, nfInstanceId);

        counters.getCcStaleBindingsDeleted(source, reason, pcfId, nfInstanceId).inc();
    }

    public static class Counters
    {
        /**
         * Prometheus counter for multiple bindings resolution
         */
        private static final Counter ccStaleBindingsDeleted = io.prometheus.client.Counter.build()
                                                                                          .namespace("bsf")
                                                                                          .name("bindings_stale_deleted_total")
                                                                                          .labelNames("source", "reason", "pcf_id", "nf_instance", "nf")
                                                                                          .help("Number of bindings that were deleted either due to Binding Lookup Resolution Based on PCF Recovery Time or due to Multiple Binding Resolution")
                                                                                          .register();

        public Child getCcStaleBindingsDeleted(final String source,
                                               final String reason,
                                               final UUID pcfId,
                                               final String nfInstance)
        {
            return ccStaleBindingsDeleted.labels(source.toLowerCase(), // source
                                                 reason.toLowerCase(),    // reason
                                                 pcfId == null ? "unknown" : pcfId.toString(), // PcfId
                                                 nfInstance,    // nf_instance
                                                 "bsf");        // nf
        }
    }
}
