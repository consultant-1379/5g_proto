/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 2, 2022
 *     Author: zpavcha
 */

package com.ericsson.esc.bsf.db;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.sc.bsf.etcd.PcfRt;

/**
 * 
 */
public class BindingRtInfo
{
    private final UUID bindingId;
    private final Instant writeTime;
    private final Optional<UUID> pcfId;
    private final Optional<PcfRt> pcfRt;

    public BindingRtInfo(final UUID bindingId,
                         final Instant writeTime,
                         final Optional<UUID> pcfId,
                         final Optional<RecoveryTime> recoveryTime)
    {
        Objects.requireNonNull(bindingId);
        Objects.requireNonNull(writeTime);

        this.bindingId = bindingId;
        this.pcfId = pcfId;
        this.writeTime = writeTime;
        this.pcfRt = recoveryTime.flatMap(recoverytime -> pcfId.map(pcfid -> new PcfRt(pcfid, Instant.from(recoverytime.parse()))));
    }

    public UUID getBindingId()
    {
        return this.bindingId;
    }

    public Instant getWriteTime()
    {
        return this.writeTime;
    }

    public Optional<UUID> getPcfId()
    {
        return this.pcfId;
    }

    public Optional<PcfRt> getPcfRt()
    {
        return this.pcfRt;
    }

}