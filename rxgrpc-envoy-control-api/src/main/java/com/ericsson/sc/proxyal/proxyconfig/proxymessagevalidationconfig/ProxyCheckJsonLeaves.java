/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 17, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig;

import java.util.Objects;
import java.util.Optional;

import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckJsonLeaves;

/**
 * 
 */
public class ProxyCheckJsonLeaves
{
    private final Optional<Integer> maxMessageLeaves;
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param maxMessageLeaves
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckJsonLeaves(Optional<Integer> maxMessageLeaves,
                                boolean reportEvent,
                                ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.maxMessageLeaves = maxMessageLeaves;
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyCheckJsonLeaves
     */
    public ProxyCheckJsonLeaves(ProxyCheckJsonLeaves proxyCheckJsonLeaves)
    {
        super();
        this.maxMessageLeaves = proxyCheckJsonLeaves.getMaxMessageLeaves();
        this.reportEvent = proxyCheckJsonLeaves.isReportEvent();
        this.actionOnFailure = proxyCheckJsonLeaves.getActionOnFailure();
    }

    /**
     * @return the maxMessageLeaves
     */
    public Optional<Integer> getMaxMessageLeaves()
    {
        return maxMessageLeaves;
    }

    /**
     * @return the reportEvent
     */
    public boolean isReportEvent()
    {
        return reportEvent;
    }

    /**
     * @return the actionOnFailure
     */
    public ProxyActionOnFailure getActionOnFailure()
    {
        return actionOnFailure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(actionOnFailure, maxMessageLeaves, reportEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyCheckJsonLeaves other = (ProxyCheckJsonLeaves) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && Objects.equals(maxMessageLeaves, other.maxMessageLeaves)
               && reportEvent == other.reportEvent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCheckJsonLeaves [maxMessageLeaves=" + maxMessageLeaves + ", reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure + "]";
    }

    public CheckJsonLeaves build()
    {
        var jsonLeavesBuilder = CheckJsonLeaves.newBuilder();
        this.getMaxMessageLeaves().ifPresent(maxLeaves -> jsonLeavesBuilder.setMaxMessageLeaves(UInt32Value.newBuilder().setValue(maxLeaves)));

        return jsonLeavesBuilder.setReportEvent(this.reportEvent).setActionOnFailure(this.actionOnFailure.buildActionOnFailure()).build();
    }
}
