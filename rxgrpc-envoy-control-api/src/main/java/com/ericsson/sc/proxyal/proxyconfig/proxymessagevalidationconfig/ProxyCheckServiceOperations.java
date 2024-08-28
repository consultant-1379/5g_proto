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
 * Created on: Apr 17, 2024
 *     Author: xvonmar
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig;

import java.util.List;
import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CheckServiceOperations;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageSelector;

/**
 * 
 */
public class ProxyCheckServiceOperations
{
    private final List<ProxyMessageSelector> additionalAllowedOperations;
    private final List<ProxyMessageSelector> removedDefaultOperations;
    private final boolean reportEvent;
    private final ProxyActionOnFailure actionOnFailure;

    /**
     * @param additionalAllowedOperations
     * @param removedDefaultOperations
     * @param reportEvent
     * @param actionOnFailure
     */
    public ProxyCheckServiceOperations(List<ProxyMessageSelector> additionalAllowedOperations,
                                       List<ProxyMessageSelector> removedDefaultOperations,
                                       boolean reportEvent,
                                       ProxyActionOnFailure actionOnFailure)
    {
        super();
        this.additionalAllowedOperations = additionalAllowedOperations;
        this.removedDefaultOperations = removedDefaultOperations;
        this.reportEvent = reportEvent;
        this.actionOnFailure = actionOnFailure;
    }

    /**
     * @param proxyCheckHeaders
     */
    public ProxyCheckServiceOperations(ProxyCheckServiceOperations proxyCheckServiceOperations)
    {
        super();
        this.additionalAllowedOperations = proxyCheckServiceOperations.getAdditionalAllowedOperations();
        this.removedDefaultOperations = proxyCheckServiceOperations.getRemovedDefaultOperations();
        this.reportEvent = proxyCheckServiceOperations.isReportEvent();
        this.actionOnFailure = proxyCheckServiceOperations.getActionOnFailure();
    }

    /**
     * @return the additionalAllowedOperations
     */
    public List<ProxyMessageSelector> getAdditionalAllowedOperations()
    {
        return additionalAllowedOperations;
    }

    /**
     * @return the removedDefaultOperations
     */
    public List<ProxyMessageSelector> getRemovedDefaultOperations()
    {
        return removedDefaultOperations;
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
        return Objects.hash(actionOnFailure, additionalAllowedOperations, removedDefaultOperations, reportEvent);
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
        ProxyCheckServiceOperations other = (ProxyCheckServiceOperations) obj;
        return Objects.equals(actionOnFailure, other.actionOnFailure) && Objects.equals(additionalAllowedOperations, other.additionalAllowedOperations)
               && Objects.equals(removedDefaultOperations, other.removedDefaultOperations) && reportEvent == other.reportEvent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCheckServiceOperations [additionalAllowedOperations=" + additionalAllowedOperations + ", removedDefaultOperations="
               + removedDefaultOperations + ", reportEvent=" + reportEvent + ", actionOnFailure=" + actionOnFailure + "]";
    }

    public CheckServiceOperations build()
    {
        var customAllowedOps = this.getAdditionalAllowedOperations().stream().map(op ->
        {
            MessageSelector.Builder msBuilder = MessageSelector.newBuilder().setIsNotification(op.getIsNotification());

            if (op.getServiceName() != null && !op.getServiceName().isEmpty())
                msBuilder.addAllApiNames(op.getServiceName());

            if (op.getServiceVersion() != null && !op.getServiceVersion().isEmpty())
                msBuilder.addAllApiVersions(op.getServiceVersion());

            if (op.getResource() != null && !op.getResource().isEmpty())
                msBuilder.addAllResourceMatchers(op.getResource());

            if (op.getHttpMethod() != null && !op.getHttpMethod().isEmpty())
                msBuilder.addAllHttpMethods(op.getHttpMethod());

            return msBuilder.build();
        }).toList();

        var defDeniedOps = this.getRemovedDefaultOperations().stream().map(op ->
        {
            MessageSelector.Builder msBuilder = MessageSelector.newBuilder().setIsNotification(op.getIsNotification());

            if (op.getServiceName() != null && !op.getServiceName().isEmpty())
                msBuilder.addAllApiNames(op.getServiceName());

            if (op.getServiceVersion() != null && !op.getServiceVersion().isEmpty())
                msBuilder.addAllApiVersions(op.getServiceVersion());

            if (op.getResource() != null && !op.getResource().isEmpty())
                msBuilder.addAllResourceMatchers(op.getResource());

            if (op.getHttpMethod() != null && !op.getHttpMethod().isEmpty())
                msBuilder.addAllHttpMethods(op.getHttpMethod());

            return msBuilder.build();
        }).toList();

        var checkServiceOperationsBuilder = CheckServiceOperations.newBuilder()
                                                                  .addAllCustomAllowedServiceOperations(customAllowedOps)
                                                                  .addAllCustomDeniedServiceOperations(defDeniedOps)
                                                                  .setReportEvent(this.reportEvent)
                                                                  .setActionOnFailure(this.actionOnFailure.buildActionOnFailure());
        return checkServiceOperationsBuilder.build();

    }

}