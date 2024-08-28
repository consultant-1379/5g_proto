package com.ericsson.esc.bsf.worker;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.db.BindingRtInfo;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.etcd.PcfRtService;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class NBsfManagementServiceRt implements NBsfManagementService
{
    private static final Logger log = LoggerFactory.getLogger(NBsfManagementServiceRt.class);

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        UNEXPECTED_ERROR,
        UNEXPECTED_ERROR_DISCOVERY,
        REGISTER_RT_ENABLED,
        DISCOVERY_RT_ENABLED
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);
    private final NBsfManagementService parent;
    private final PcfRtService pcfRtSvc;
    private final AtomicReference<RecoveryTimeConfig> rtResolver = new AtomicReference<>();
    private final CompositeDisposable checkRtWatcher = new CompositeDisposable();

    public NBsfManagementServiceRt(NBsfManagementService parent,
                                   PcfRtService pcfRtSvc,
                                   Flowable<RecoveryTimeConfig> rtResolver)
    {
        super();
        this.parent = parent;
        this.pcfRtSvc = pcfRtSvc;
        var disposableWatcher = rtResolver.subscribe(this::setRtResolver, err -> log.error("Stoped updating checkRt flag: ", err));
        this.checkRtWatcher.add(disposableWatcher);
    }

    @Override
    public Single<RegisterResult> register(PcfBinding binding,
                                           int ttlConfig)
    {
        var enableEtcdUpdate = this.getRtResolver().getRtResolution() || this.getRtResolver().getScanConfig() != BindingDatabaseScan.Configuration.DISABLED;
        log.debug("Update pcfRt etcd during registration is: {}, checkUpLookup: {} ScanConfig: {}",
                  enableEtcdUpdate,
                  this.getRtResolver().getRtResolution(),
                  this.getRtResolver().getScanConfig());

        return enableEtcdUpdate ? this.registerRt(binding, ttlConfig) : this.parent.register(binding, ttlConfig);
    }

    private void setRtResolver(RecoveryTimeConfig rtResolver)
    {
        this.rtResolver.set(rtResolver);
    }

    private RecoveryTimeConfig getRtResolver()
    {
        return this.rtResolver.get();
    }

    private Single<RegisterResult> registerRt(PcfBinding binding,
                                              int ttlConfig)
    {
        safeLog.log(Lbl.REGISTER_RT_ENABLED, logger -> logger.debug("RegisterRt is enabled."));

        try
        {
            final var pcfRt = extractPcfRt(binding);

            return this.parent.register(binding, ttlConfig).doOnSuccess(registerRes ->
            {
                if (pcfRt.isPresent())
                    this.pcfRtSvc.reportRecoveryTime(pcfRt.get()) //
                                 .subscribe(res ->
                                 {
                                 }, err -> safeLog.log(Lbl.UNEXPECTED_ERROR, l -> l.warn("Failed to report recovery time {}", pcfRt)));
            });
        }
        catch (Exception e)
        {
            return Single.error(e);
        }
    }

    @Override
    public Single<DeregisterResult> deregister(UUID bindingId)
    {
        return this.parent.deregister(bindingId);
    }

    @Override
    public Single<DiscoveryResult> discovery(DiscoveryQuery query)
    {
        return this.getRtResolver().getRtResolution() ? this.parent.discovery(query).flatMap(this::processResult) : this.parent.discovery(query);
    }

    @Override
    public Completable init()
    {
        return parent.init() //
                     .mergeWith(pcfRtSvc.init()); // report ready when both encapsulated service are ready
    }

    @Override
    public Completable run()
    {
        return parent.run().mergeWith(pcfRtSvc.run());
    }

    @Override
    public Completable stop()
    {
        return this.parent.stop() //
                          .mergeWith(this.pcfRtSvc.stop())
                          .doFinally(this.checkRtWatcher::dispose);
    }

    private Single<DiscoveryResult> processResult(DiscoveryResult result)
    {

        safeLog.log(Lbl.DISCOVERY_RT_ENABLED, logger -> logger.debug("Discovery using recovery time is enabled."));

        final var deleteUponLookup = this.getRtResolver().getDeletionUponLookup();

        return result.getPcfBinding().map(binding ->
        {
            final var bindingRtInfo = new BindingRtInfo(binding.getBindingId(),
                                                        binding.getWriteTime(),
                                                        Optional.ofNullable(binding.getPcfId()),
                                                        Optional.ofNullable(binding.getRecoveryTime()));
            return this.pcfRtSvc.checkStaleBinding(bindingRtInfo)
                                .map(optionalstale -> optionalstale.map(stalepcfbinding -> deleteUponLookup ? DiscoveryResult.notFound(List.of(stalepcfbinding))
                                                                                                            : DiscoveryResult.notFound())
                                                                   .orElse(result));

        }).orElseGet(() -> Single.<DiscoveryResult>just(result));

    }

    private Optional<PcfRt> extractPcfRt(PcfBinding binding)
    {
        try
        {
            if (binding.getPcfId() != null && binding.getRecoveryTime() != null)
            {
                return Optional.of(new PcfRt(binding.getPcfId(), Instant.from(binding.getRecoveryTime().parse())));
            }
            else
            {
                return Optional.empty();
            }
        }
        catch (DateTimeParseException e)
        {
            throw new IllegalArgumentException("Invalid recovery time: " + binding.getRecoveryTimeString(), e);
        }

    }
}