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

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckJsonSyntax;

/**
 * 
 */
public class ProxyCheckJsonSyntax
{
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckJsonSyntax(boolean reportEvent,
                                ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyCheckJsonSyntax
     */
    public ProxyCheckJsonSyntax(ProxyCheckJsonSyntax proxyCheckJsonSyntax)
    {
        super();
        this.reportEvent = proxyCheckJsonSyntax.isReportEvent();
        this.actionOnFailure = proxyCheckJsonSyntax.getActionOnFailure();
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
        return Objects.hash(actionOnFailure, reportEvent);
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
        ProxyCheckJsonSyntax other = (ProxyCheckJsonSyntax) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && reportEvent == other.reportEvent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCheckMessageBytes [reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure + "]";
    }

    public CheckJsonSyntax build()
    {
        return CheckJsonSyntax.newBuilder().setReportEvent(this.reportEvent).setActionOnFailure(this.actionOnFailure.buildActionOnFailure()).build();
    }
}
