package com.ericsson.sc.bsf.etcd;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

import io.reactivex.Completable;
import io.reactivex.Single;

public class PcfRtCachedServiceImpl implements PcfRtService
{

    private static final Logger log = LoggerFactory.getLogger(PcfRtCachedServiceImpl.class);

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        UNEXPECTED_ERROR,
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    private final PcfRtDbEtcd db;
    private final PcfRtCache rtCache;
    private final PcfRtDbWatcher watcher;

    public PcfRtCachedServiceImpl(RxEtcd rxEtcd,
                                  long ttl)
    {
        this.db = new PcfRtDbEtcd(rxEtcd, ttl);
        this.rtCache = new PcfRtCache();
        this.watcher = new PcfRtDbWatcher(db, rtCache);
    }

    public Completable init()
    {
        return this.db.getEtcd()
                      .ready() //
                      .andThen(this.watcher.init());
    }

    public Completable run()
    {
        return watcher.run();
    }

    public Completable stop()
    {
        return watcher.stop();
    }

    @Override
    public Single<RecoveryTimeStatus> reportRecoveryTime(PcfRt pcfRt)
    {
        final var fetchedPcfRt = Optional.ofNullable(rtCache.get(pcfRt.getId()));
        final var lookupStatus = cacheLookup(fetchedPcfRt, pcfRt);

        switch (lookupStatus)
        {
            case ABSENT_NEWER:
                rtCache.update(pcfRt);
                db.createOrUpdate(pcfRt).subscribe(res ->
                {
                }, e -> safeLog.log(Lbl.UNEXPECTED_ERROR, l -> l.warn("Faild to update PcfRt database {}", pcfRt, e)));
                return Single.just(PcfRtService.RecoveryTimeStatus.UPDATED);
            case EQUAL:
                return Single.just(PcfRtService.RecoveryTimeStatus.ALREADY_EXISTS);
            case OLDER:
                return Single.just(PcfRtService.RecoveryTimeStatus.STALE);
            default:
                throw new IllegalArgumentException("Invalid RecoveryTime status");
        }
    }

    private LookupStatus cacheLookup(Optional<PcfRt> fetched,
                                     PcfRt provided)
    {
        if (fetched.isEmpty() || fetched.get().getRecoverytime() < provided.getRecoverytime())
            return LookupStatus.ABSENT_NEWER;
        else if (fetched.get().equals(provided))
            return LookupStatus.EQUAL;
        else
            return LookupStatus.OLDER;
    }

    private enum LookupStatus
    {
        EQUAL,
        ABSENT_NEWER,
        OLDER
    }

    @Override
    public Single<Optional<PcfRt>> get(UUID pcfId)
    {

        return Single.just(Optional.ofNullable(rtCache.get(pcfId)));
    }

    @Override
    public Single<List<PcfRt>> getAll()
    {
        return Single.fromCallable(rtCache::getAll);
    }
}
