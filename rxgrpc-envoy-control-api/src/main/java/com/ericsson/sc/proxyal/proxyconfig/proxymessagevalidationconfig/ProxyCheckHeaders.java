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

import java.util.List;
import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckHeaders;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.StringBoolMap;

/**
 * 
 */
public class ProxyCheckHeaders
{
    private final policyType policyType;
    private final List<String> headers;
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param policyType
     * @param headers
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckHeaders(policyType policyType,
                             List<String> headers,
                             boolean reportEvent,
                             ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.policyType = policyType;
        this.headers = headers;
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyCheckHeaders
     */
    public ProxyCheckHeaders(ProxyCheckHeaders proxyCheckHeaders)
    {
        super();
        this.policyType = proxyCheckHeaders.getPolicyType();
        this.headers = proxyCheckHeaders.getHeaders();
        this.reportEvent = proxyCheckHeaders.isReportEvent();
        this.actionOnFailure = proxyCheckHeaders.getActionOnFailure();
    }

    /**
     * @return the policyType
     */
    public policyType getPolicyType()
    {
        return policyType;
    }

    /**
     * @return the headers
     */
    public List<String> getHeaders()
    {
        return headers;
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
        return Objects.hash(actionOnFailure, headers, policyType, reportEvent);
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
        ProxyCheckHeaders other = (ProxyCheckHeaders) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && Objects.equals(headers, other.headers) && policyType == other.policyType
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
        return "ProxyCheckHeaders [policyType=" + policyType + ", headers=" + headers + ", reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure
               + "]";
    }

    public CheckHeaders build()
    {
        var checkHeaderBuilder = CheckHeaders.newBuilder();
        var stringBoolBuilder = StringBoolMap.newBuilder();
        this.getHeaders().forEach(header -> stringBoolBuilder.putValues(header, true));
        if (this.getPolicyType().equals(policyType.allowed))
            return checkHeaderBuilder.setAllowedHeaders(stringBoolBuilder)
                                     .setReportEvent(this.reportEvent)
                                     .setActionOnFailure(this.actionOnFailure.buildActionOnFailure())
                                     .build();
        else
            return checkHeaderBuilder.setDeniedHeaders(stringBoolBuilder)
                                     .setReportEvent(this.reportEvent)
                                     .setActionOnFailure(this.actionOnFailure.buildActionOnFailure())
                                     .build();
    }

    public enum policyType
    {
        allowed,
        denied
    }
}
