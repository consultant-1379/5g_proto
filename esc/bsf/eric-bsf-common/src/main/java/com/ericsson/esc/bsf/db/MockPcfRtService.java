package com.ericsson.esc.bsf.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.etcd.PcfRtService;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class MockPcfRtService implements PcfRtService
{
    private final AtomicInteger reportCount = new AtomicInteger(0);
    private final AtomicReference<RtStatus> rtStatus = new AtomicReference<>(RtStatus.UPDATED);
    private final AtomicBoolean dbPresence = new AtomicBoolean(true);

    // Maybe we should keep this data structure against just PcfRt field
    private ConcurrentHashMap<UUID, PcfRt> pcfIdtoPcfRt;

    private PublishSubject<Boolean> fetchedResult = PublishSubject.create();

    public enum RtStatus
    {
        UPDATED,
        NO_UPDATED,
        ALREADY_EXISTS,
        ERROR
    }

    public MockPcfRtService()
    {
        pcfIdtoPcfRt = new ConcurrentHashMap<>();
    }

    public void setRtStatus(RtStatus status)
    {
        this.rtStatus.set(status);
    }

    public void setDbPresence(boolean dbPresence)
    {
        this.dbPresence.set(dbPresence);
    }

    public int getReportCount()
    {
        return this.reportCount.get();
    }

    public Flowable<Boolean> getFetchedResult()
    {
        return fetchedResult.toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Single<RecoveryTimeStatus> reportRecoveryTime(PcfRt pcfRt)
    {
        this.reportCount.getAndIncrement();
        switch (rtStatus.get())
        {
            case UPDATED:
                createOrUpdate(pcfRt).doOnSuccess(res -> fetchedResult.onNext(res)).doOnError(err -> fetchedResult.onError(err)).subscribe();
                return Single.just(PcfRtService.RecoveryTimeStatus.UPDATED);
            case NO_UPDATED:
                return Single.just(PcfRtService.RecoveryTimeStatus.STALE);
            case ALREADY_EXISTS:
                return Single.just(PcfRtService.RecoveryTimeStatus.ALREADY_EXISTS);
            case ERROR:
                return Single.error(new IllegalArgumentException("unexpected transaction error"));
            default:
                break;
        }

        return null;
    }

    @Override
    public Completable run()
    {
        return Completable.never();
    }

    @Override
    public Completable init()
    {
        return Completable.complete();
    }

    @Override
    public Single<Optional<PcfRt>> get(UUID pcfId)
    {
        return Single.fromCallable(() -> this.pcfIdtoPcfRt.containsKey(pcfId) ? Optional.ofNullable(this.pcfIdtoPcfRt.get(pcfId)) : Optional.<PcfRt>empty());
    }

    public Single<Boolean> createOrUpdate(PcfRt pcfRt)
    {
        return Single.just(true);
    }

    public void setPcfRt(UUID pcfId,
                         PcfRt pcfRt)
    {
        pcfIdtoPcfRt.put(pcfId, pcfRt);
    }

    @Override
    public Completable stop()
    {
        return Completable.complete();
    }

    @Override
    public Single<List<PcfRt>> getAll()
    {
        return Single.fromCallable(() -> new ArrayList<>(this.pcfIdtoPcfRt.values()));
    }
}
