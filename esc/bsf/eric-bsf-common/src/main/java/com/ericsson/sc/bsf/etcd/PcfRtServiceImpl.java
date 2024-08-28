package com.ericsson.sc.bsf.etcd;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

import io.reactivex.Completable;
import io.reactivex.Single;

public class PcfRtServiceImpl implements PcfRtService
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtServiceImpl.class);

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        UNEXPECTED_ERROR,
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    private final PcfRtDbEtcd db;

    public PcfRtServiceImpl(RxEtcd rxEtcd,
                            long ttl)
    {
        this.db = new PcfRtDbEtcd(rxEtcd, ttl);
    }

    @Override
    public Completable init()
    {
        return this.db.getEtcd().ready();
    }

    @Override
    public Completable run()
    {
        return Completable.never();
    }

    @Override
    public Completable stop()
    {
        return Completable.complete();
    }

    @Override
    public Single<RecoveryTimeStatus> reportRecoveryTime(PcfRt pcfRt)
    {
        return db.get(pcfRt.getId()) //
                 .map(dbPcfRt ->
                 {
                     final var lookupStatus = dbLookup(dbPcfRt, pcfRt);
                     switch (lookupStatus)
                     {
                         case ABSENT_NEWER:
                             db.createOrUpdate(pcfRt).subscribe(res ->
                             {
                             }, e -> safeLog.log(Lbl.UNEXPECTED_ERROR, l -> l.warn("Faild to update PcfRt database {}", pcfRt, e)));
                             return PcfRtService.RecoveryTimeStatus.UPDATED;
                         case EQUAL:
                             return PcfRtService.RecoveryTimeStatus.ALREADY_EXISTS;
                         case OLDER:
                             return PcfRtService.RecoveryTimeStatus.STALE;

                         default:
                             throw new IllegalArgumentException("Invalid RecoveryTime status");

                     }
                 });

    }

    @Override
    public Single<Optional<PcfRt>> get(UUID pcfId)
    {
        return db.get(pcfId);
    }

    @Override
    public Single<List<PcfRt>> getAll()
    {
        return this.db.getAll() //
                      .map(all -> all.getEntries() //
                                     .stream()
                                     .map(entry -> new PcfRt(entry.getKey(), entry.getValue()))
                                     .collect(Collectors.toList()));
    }

    private LookupStatus dbLookup(Optional<PcfRt> fetched,
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

    public PcfRtDbEtcd getEtcd()
    {
        return this.db;
    }

}
