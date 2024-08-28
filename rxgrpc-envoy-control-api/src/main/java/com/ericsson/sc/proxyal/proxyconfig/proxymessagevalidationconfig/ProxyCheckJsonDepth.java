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

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckJsonDepth;

/**
 * 
 */
public class ProxyCheckJsonDepth
{
    private final Optional<Integer> maxMessageNestingDepth;
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param maxMessageNestingDepth
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckJsonDepth(Optional<Integer> maxMessageNestingDepth,
                               boolean reportEvent,
                               ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.maxMessageNestingDepth = maxMessageNestingDepth;
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyCheckJsonDepth
     */
    public ProxyCheckJsonDepth(ProxyCheckJsonDepth proxyCheckJsonDepth)
    {
        super();
        this.maxMessageNestingDepth = proxyCheckJsonDepth.getMaxMessageNestingDepth();
        this.reportEvent = proxyCheckJsonDepth.isReportEvent();
        this.actionOnFailure = proxyCheckJsonDepth.getActionOnFailure();
    }

    /**
     * @return the maxMessageLeaves
     */
    public Optional<Integer> getMaxMessageNestingDepth()
    {
        return maxMessageNestingDepth;
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
        return Objects.hash(actionOnFailure, maxMessageNestingDepth, reportEvent);
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
        ProxyCheckJsonDepth other = (ProxyCheckJsonDepth) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && Objects.equals(maxMessageNestingDepth, other.maxMessageNestingDepth)
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
        return "ProxyCheckJsonLeaves [maxMessageLeaves=" + maxMessageNestingDepth + ", reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure
               + "]";
    }

    public CheckJsonDepth build()
    {
        var jsonDepthBuilder = CheckJsonDepth.newBuilder();
        this.getMaxMessageNestingDepth().ifPresent(maxDepth -> jsonDepthBuilder.setMaxMessageNestingDepth(UInt32Value.newBuilder().setValue(maxDepth)));

        return jsonDepthBuilder.setReportEvent(this.reportEvent).setActionOnFailure(this.actionOnFailure.buildActionOnFailure()).build();
    }
}
