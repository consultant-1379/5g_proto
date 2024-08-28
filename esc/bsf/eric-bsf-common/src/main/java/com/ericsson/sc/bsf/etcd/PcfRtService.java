package com.ericsson.sc.bsf.etcd;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.esc.bsf.db.BindingRtInfo;
import com.ericsson.esc.bsf.worker.StalePcfBinding;
import com.ericsson.esc.bsf.worker.StalePcfBinding.Reason;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface PcfRtService
{
    Single<RecoveryTimeStatus> reportRecoveryTime(PcfRt pcfRt);

    Single<Optional<PcfRt>> get(UUID pcfId);

    public enum RecoveryTimeStatus
    {
        UPDATED,
        STALE,
        ALREADY_EXISTS
    }

    public enum Source
    {
        RESOLVE_UPON_LOOKUP,
        BINDING_DATABASE_SCAN
    }

    Completable run();

    Completable init();

    Completable stop();

    Single<List<PcfRt>> getAll();

    /**
     * Checks whether a binding (represented by its binding rt info subset) is
     * stale. If in the retrieved binding there is a recoveryTime, we compare the
     * retrieved recoveryTime with the latest RT for the corresponding PCF and if is
     * older then it is stale. If there is no recoveryTime in the retrieved binding
     * then we compare the writeTime of this binding with the latest RT for the
     * corresponding PCF and if is older then it is stale.
     * 
     * @param bindingRtInfo The required parameters for checking the staleness of
     *                      the binding.
     * @param source        The source from which we check for staleness of the
     *                      binding, can be either resolve_upon_lookup or
     *                      binding_database_scan.
     * @return A Single of Optional of StalePcfBinding object. The optional is
     *         present in case the binding is stale and empty otherwise.
     */
    default Single<Optional<StalePcfBinding>> checkStaleBinding(final BindingRtInfo bindingRtInfo)
    {
        final var pcfRtFromBinding = bindingRtInfo.getPcfRt();
        final var pcfId = bindingRtInfo.getPcfId();

        return pcfRtFromBinding.isPresent() ? this.reportRecoveryTime(pcfRtFromBinding.get()) // report the RT in etcd
                                                  // stale section logic based to rt from the binding
                                                  .flatMap(status -> status.equals(PcfRtService.RecoveryTimeStatus.STALE) ? this.get(pcfRtFromBinding.get()
                                                                                                                                                     .getId())
                                                                                                                                .map(optPcfRt -> createStaleBinding(optPcfRt,
                                                                                                                                                                    bindingRtInfo))

                                                                                                                          : Single.just(Optional.empty()))
                                            // stale section logic based to writeTime from the binding and etcd's latest Rt
                                            : pcfId.isPresent() ? this.get(pcfId.get())
                                                                      .map(optPcfRt -> optPcfRt.flatMap(pcfrt -> bindingRtInfo.getWriteTime()
                                                                                                                              .isBefore(pcfrt.getRtInstant()) ? Optional.of(new StalePcfBinding(bindingRtInfo.getBindingId(), pcfrt.getId(), Reason.PCF_RECOVERY_TIME)) : Optional.empty()))
                                                                : Single.just(Optional.empty());

    }

    /*
     * @param optPcfRt The corresponding PcfRT that found stale
     * 
     * @param bindingRtInfoThe required parameters for checking the staleness of the
     * binding.
     * 
     * @return An Optional of StalePcfBinding object.The optional is present in case
     * the binding is stale and empty otherwise.
     */

    private Optional<StalePcfBinding> createStaleBinding(final Optional<PcfRt> optPcfRt,
                                                         final BindingRtInfo bindingRtInfo)
    {
        return optPcfRt.flatMap(pcfrt -> Optional.of(new StalePcfBinding(bindingRtInfo.getBindingId(), pcfrt.getId(), Reason.PCF_RECOVERY_TIME)));
    }
}
