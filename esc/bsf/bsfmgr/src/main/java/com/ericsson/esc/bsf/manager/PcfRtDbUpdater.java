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
 * Created on: Apr 12, 2022
 *     Author: estoioa
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.etcd.PcfRtService;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Maps the discovered PcfNfs to PcfRts and updates etcd.
 */
public class PcfRtDbUpdater
{
    private static final Logger log = LoggerFactory.getLogger(PcfRtDbUpdater.class);

    private final PcfRtService pcfRtService;
    private final Flowable<List<PcfRt>> discoveredPcfs;

    public PcfRtDbUpdater(PcfRtService pcfRtService,
                          Flowable<List<PcfRt>> discoveredPcfs)
    {
        this.pcfRtService = pcfRtService;
        this.discoveredPcfs = discoveredPcfs;
    }

    public Completable run()
    {
        return discoveredPcfs.doOnNext(this::updateDb).ignoreElements();
    }

    public Completable init()
    {
        return this.pcfRtService.init();
    }

    public Completable stop()
    {
        return this.pcfRtService.stop();
    }

    /**
     * 
     * @param pcfRt
     * @return
     */
    private void updateDb(List<PcfRt> pcfs)
    {
        log.info("Updating PCF recovery times received from NRF: {}", pcfs);
        pcfs.forEach(pcf -> this.pcfRtService.reportRecoveryTime(pcf)
                                             .doOnSuccess(result -> log.info("Reported PCF recovery time, PCF:{} status:{}", pcf, result))
                                             .doOnError(err -> log.error("Unexpected error while updating database with PCF recovery time, PCF: {}", pcf, err))
                                             .ignoreElement()
                                             .onErrorComplete() // Log and ignore errors, there is no way to recover
                                             .subscribe());
    }
}
