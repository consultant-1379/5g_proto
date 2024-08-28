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

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckMessageBytes;

/**
 * 
 */
public class ProxyCheckMessageBytes
{
    private final Optional<Integer> maxMessageBytes;
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param maxMessageBytes
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckMessageBytes(Optional<Integer> maxMessageBytes,
                                  boolean reportEvent,
                                  ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.maxMessageBytes = maxMessageBytes;
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyMessageBytes
     */
    public ProxyCheckMessageBytes(ProxyCheckMessageBytes proxyCheckMessageBytes)
    {
        super();
        this.maxMessageBytes = proxyCheckMessageBytes.getMaxMessageBytes();
        this.reportEvent = proxyCheckMessageBytes.isReportEvent();
        this.actionOnFailure = proxyCheckMessageBytes.getActionOnFailure();
    }

    /**
     * @return the maxMessageBytes
     */
    public Optional<Integer> getMaxMessageBytes()
    {
        return maxMessageBytes;
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
        return Objects.hash(actionOnFailure, maxMessageBytes, reportEvent);
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
        ProxyCheckMessageBytes other = (ProxyCheckMessageBytes) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && Objects.equals(maxMessageBytes, other.maxMessageBytes)
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
        return "ProxyCheckMessageBytes [maxMessageBytes=" + maxMessageBytes + ", reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure + "]";
    }

    public CheckMessageBytes build()
    {
        var messageBytesBuilder = CheckMessageBytes.newBuilder();
        this.getMaxMessageBytes().ifPresent(maxBytes -> messageBytesBuilder.setMaxMessageBytes(UInt32Value.newBuilder().setValue(maxBytes)));

        return messageBytesBuilder.setReportEvent(this.reportEvent).setActionOnFailure(this.actionOnFailure.buildActionOnFailure()).build();
    }

}
