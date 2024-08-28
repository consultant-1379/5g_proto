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
 * Created on: Mar 8, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.shaded.guava.common.base.Charsets;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchResponse;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;

/**
 * Watches for PCF database changes
 */
public final class PcfDbWatcher
{
    private final PcfDbEtcd db;
    private final CompositeDisposable cleanup = new CompositeDisposable();
    private final ConnectableFlowable<PcfNfCollection> mainChain;
    private static final Logger log = LoggerFactory.getLogger(PcfDbWatcher.class);

    /**
     * 
     * @param db            The PCF etcd database
     * @param retryFunction The retry strategy to apply upon etcd watch failure
     */
    PcfDbWatcher(PcfDbEtcd db,
                 RetryFunction retryFunction)
    {
        this.db = db;
        final var etcd = db.getEtcd();
        final var prefix = db.serializer.pcfNf().getPrefixBytes();

        mainChain = db.getPcfs().flatMapPublisher(pcfs -> //
        etcd.watch(prefix, //
                   WatchOption.builder() //
                              .isPrefix(true)
                              .build()) //
            .doOnSubscribe(disp -> log.info("Watching etcd for changes, revision: {}, prefix: {}", pcfs.getRevision(), prefix.toString(StandardCharsets.UTF_8)))
            .scan(processPcfs(pcfs.getEntries() //
                                  .stream()
                                  .map(entry -> new PcfNfRecord(entry.getValue(), entry.getModRevision()))),

                  this::processWatchResponse) //
        )
                      .retryWhen(retryFunction.create())
                      .doOnError(err -> log.warn("Watcher terminated due to unexpected error", err))
                      .doAfterTerminate(() -> log.info("Watch terminated"))
                      .replay(1);
    }

    /**
     * Get all PcfNfRecord objects grouped by their NF set id. Note that objects are
     * emitted only after {@link #initialize()} is complete. The Emitted values are
     * Maps with key the PCF set ID and values the corresponding PcfNfRecord
     * objects. Each map is a snapshot of the database state.
     * 
     * @return A Flowable that emits snapshots of the database state
     */
    public Flowable<Map<String, Set<PcfNfRecord>>> pcfNfBySetId()
    {
        return this.mainChain.map(PcfNfCollection::getBySetId);
    }

    /**
     * @return The complete BSF database view
     */
    public Flowable<PcfDbView> bsfDbView()
    {
        return pcfNfBySetId().map(PcfDbView::new);
    }

    /**
     * Start watching the PCF database. Note that the watcher does not support
     * restarts, subsequent subscriptions to returned Completable have no effect
     * 
     * @return A Completable that peforms the operation upon subscription and
     *         completes as soon as the first database snapshot has been received
     */
    public Completable initialize()
    {
        return db.getEtcd().ready().andThen(Completable.defer(() ->
        {
            final var disp = mainChain.connect();
            cleanup.add(disp);
            return isStarted();
        })).cache();
    }

    /**
     * Stop watching for changes and dispose all resources. Note that the watcher
     * cannot be restarted.
     * 
     * @return A Completable that stops watcher upon subscription
     */
    public Completable terminate()
    {
        return Completable.fromAction(cleanup::dispose);
    }

    /**
     * 
     * @return A Completable that completes as soon as the watcher is started
     */
    private Completable isStarted()
    {
        return this.mainChain.firstElement().ignoreElement();
    }

    private PcfNfCollection processWatchResponse(final PcfNfCollection previousState,
                                                 final WatchResponse response)
    {
        final var newState = new PcfNfCollection(previousState);
        for (final var event : response.getEvents())
        {
            try
            {
                switch (event.getEventType())
                {
                    case PUT:
                        final var entry = new EtcdEntry<>(db.serializer.pcfNf(), event.getKeyValue());
                        final var pcfWithRev = new PcfNfRecord(entry.getValue(), entry.getModRevision());
                        log.debug("Adding PCF in etcd: {}, with modRevision: {}", pcfWithRev.pcfNf(), pcfWithRev.modRevision());
                        newState.add(pcfWithRev);
                        break;
                    case DELETE:
                        final var pcfNfId = db.serializer.pcfNf().key(event.getKeyValue().getKey());
                        log.debug("Removing PCF from etcd: {}", pcfNfId);
                        newState.remove(pcfNfId);
                        break;
                    default:
                        log.warn("Ignoring unrecognized etcd Watch event type {}", event.getEventType());
                }
            }
            catch (final Exception e)
            {
                log.error("Failed to process PcfNf entry key={} value={}",
                          event.getKeyValue().getKey().toString(Charsets.UTF_8),
                          event.getKeyValue().getValue().toString(Charsets.UTF_8),
                          e);
                // TODO skip error?
            }
        }

        return newState;
    }

    private static PcfNfCollection processPcfs(final Stream<PcfNfRecord> pcfNfs)
    {
        return PcfNfCollection.create(pcfNfs);
    }

}
