package com.ericsson.sc.bsf.etcd;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Watches for Pcf recovery time database changes and accordingly updates
 * PcfRtCache
 */
public class PcfRtDbWatcher
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtDbWatcher.class);

    private final PcfRtDbEtcd db;
    private Completable mainchain;
    private final PcfRtCache cache;
    private RetryFunction watcherRetryFunction;
    private CompletableSubject started = CompletableSubject.create();
    private CompletableSubject stopRequest = CompletableSubject.create();

    private final boolean cacheNotification;
    private Subject<PcfRtCache> cacheUpdates = PublishSubject.<PcfRtCache>create().toSerialized();

    PcfRtDbWatcher(PcfRtDbEtcd db,
                   PcfRtCache cache)
    {
        this(db, cache, false);
    }

    /**
     * 
     * @param db                The PCF recovery time etcd database
     * @param cache             PcfRtDbcache for storing etcd database changes
     * @param cacheNotification Boolean value for emmiting cache updates
     */
    PcfRtDbWatcher(PcfRtDbEtcd db,
                   PcfRtCache cache,
                   boolean cacheNotification)
    {
        this.db = db;
        this.cache = cache;
        this.cacheNotification = cacheNotification;

        final var etcd = db.getEtcd();
        final var prefix = db.serializer.pcfRt().getPrefixBytes();

        // default etcd watcher retry strategy. Keep trying for 2 minutes, then fail
        this.watcherRetryFunction = new RetryFunction().withRetries(-1) // infinite retries
                                                       .withDelay(5 * 1000L) // 5 second delay between retries
                                                       .withJitter(30) //
                                                       .withRetryAction((ex,
                                                                         cnt) -> log.warn("Etcd watch failed, retry: {}", cnt, ex));

        mainchain = db.getAll()
                      .doOnSuccess(this::resetCache)
                      .doOnSuccess(initialEntries -> this.started.onComplete())
                      .flatMapPublisher(entries -> etcd.watch(prefix,
                                                              WatchOption.newBuilder() //
                                                                         .withRevision(entries.getRevision() + 1L)
                                                                         .isPrefix(true)
                                                                         .build())
                                                       .doOnNext(this::updateCache)
                                                       .doOnSubscribe(disp -> log.info("Watching etcd for changes, revision: {}, prefix: {}",
                                                                                       entries.getRevision(),
                                                                                       prefix.toString(StandardCharsets.UTF_8))))
                      .ignoreElements()
                      .takeUntil(this.stopRequest) // Terminate if requested
                      .retryWhen(watcherRetryFunction.create())
                      .doOnError(err -> log.warn("Watcher terminated due to unexpected error", err))
                      .doAfterTerminate(() -> log.info("Watch terminated"))
                      .cache();
    }

    private synchronized void updateCache(WatchResponse resp)
    {
        resp.getEvents().forEach(event ->
        {
            switch (event.getEventType())
            {
                case PUT:
                    final var entry = new EtcdEntry<>(db.serializer.pcfRt(), event.getKeyValue());
                    final var pcfRt = new PcfRt(entry.getKey(), entry.getValue());
                    log.debug("Adding PcfRt to cache {}", pcfRt);
                    cache.update(pcfRt);
                    break;
                case DELETE:
                    final var pcfRtId = db.serializer.pcfRt().key(event.getKeyValue().getKey());
                    log.debug("Removing PcfRt from cache {}", pcfRtId);
                    cache.remove(pcfRtId);
                    break;
                default:
                    log.warn("Ignoring unrecognized etcd Watch event type {}", event.getEventType());
            }
            if (cacheNotification)
                cacheUpdates.onNext(cache);
        });

    }

    private synchronized void resetCache(EtcdEntries<UUID, Long> entries)
    {

        this.cache.reset(entries.getEntries() //
                                .stream()
                                .map(entry -> new PcfRt(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList()));
        if (cacheNotification)
            cacheUpdates.onNext(cache);
    }

    public Completable run()
    {
        return mainchain;
    }

    /**
     * 
     * @return A Completable that completes as soon as initial data have been
     *         fetched from etcd
     */
    public Completable init()
    {
        return this.mainchain.ambWith(this.started);
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            this.stopRequest.onComplete();
            return this.mainchain;
        });
    }

    protected Flowable<PcfRtCache> cacheUpdatesWatcher()
    {
        return cacheUpdates.toFlowable(BackpressureStrategy.BUFFER);
    }
}
