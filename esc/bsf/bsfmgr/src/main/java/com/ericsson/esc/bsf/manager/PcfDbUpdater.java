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
 * Created on: Apr 23, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.bsf.etcd.PcfDbEtcd;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.rxetcd.RxEtcd;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Stores the discovered PcfNfs in the ETCD database.
 */
public class PcfDbUpdater
{
    private static final Logger log = LoggerFactory.getLogger(PcfDbUpdater.class);

    private final PcfDbEtcd pcfDb;
    private final Flowable<List<PcfNf>> discoveredPcfs;

    public PcfDbUpdater(final RxEtcd rxEtcd,
                        final Flowable<List<PcfNf>> discoveredPcfs,
                        final long ttl)
    {
        this.pcfDb = new PcfDbEtcd(rxEtcd, true, ttl);
        this.discoveredPcfs = discoveredPcfs;
    }

    public Completable run()
    {
        return discoveredPcfs.flatMapCompletable(this::updateDb);
    }

    /**
     * 
     * @param pcfNfs
     * @return
     */
    private Completable updateDb(final List<PcfNf> pcfNfs)
    {
        log.info("Storing pcfNfs in etcd database: {}", pcfNfs);
        return this.pcfDb.createOrUpdate(pcfNfs);
    }
}
