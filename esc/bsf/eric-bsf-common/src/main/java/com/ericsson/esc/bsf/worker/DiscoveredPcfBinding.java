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
 * Created on: May 10, 2022
 *     Author: echfari
 */
package com.ericsson.esc.bsf.worker;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.SupportedFeatures;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class DiscoveredPcfBinding extends PcfBinding
{

    private final UUID bindingId;
    private final Instant writeTime;

    public DiscoveredPcfBinding(PcfBinding b,
                                UUID bindingId,
                                Instant writeTime)
    {
        super(b.getUeAddressType(),
              b.getSupi(),
              b.getGpsi(),
              b.getIpv4Addr(),
              b.getIpv6Prefix(),
              b.getIpDomain(),
              b.getMacAddr48(),
              b.getDnn(),
              b.getPcfFqdn(),
              b.getPcfIpEndPoints(),
              b.getPcfDiamHost(),
              b.getPcfDiamRealm(),
              b.getSnssai(),
              b.getPcfId(),
              b.getRecoveryTime(),
              b.getSuppFeat(),
              b.getAddIpv6Prefixes(),
              b.getAddMacAddrs(),
              b.getPcfSetId(),
              b.getBindLevel());

        Objects.requireNonNull(bindingId);
        Objects.requireNonNull(writeTime);

        this.bindingId = bindingId;
        this.writeTime = writeTime;
    }

    public DiscoveredPcfBinding(DiscoveredPcfBinding b,
                                SupportedFeatures commonSuppFeat)
    {
        this(PcfBinding.create(b, commonSuppFeat), b.bindingId, b.writeTime);
    }

    @JsonIgnore
    public UUID getBindingId()
    {
        return bindingId;
    }

    @JsonIgnore
    public Instant getWriteTime()
    {
        return writeTime;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

}
