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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProxyMessageSelector
{
    private List<String> serviceName = new ArrayList<>();
    private List<String> serviceVersion = new ArrayList<>();
    private List<String> resource = new ArrayList<>();
    private boolean isNotification;
    private List<String> httpMethod;

    /**
     * 
     */
    public ProxyMessageSelector(List<String> serviceName,
                                List<String> serviceVersion,
                                List<String> resource,
                                boolean isNotification,
                                List<String> httpMethod)
    {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.resource = resource;
        this.isNotification = isNotification;
        this.httpMethod = httpMethod;

    }

    /**
     * @return the serviceName
     */
    public List<String> getServiceName()
    {
        return serviceName;
    }

    /**
     * @return the serviceVersion
     */
    public List<String> getServiceVersion()
    {
        return serviceVersion;
    }

    /**
     * @return the resource
     */
    public List<String> getResource()
    {
        return resource;
    }

    /**
     * @return the isNotification
     */
    public boolean getIsNotification()
    {
        return isNotification;
    }

    /**
     * @return the httpMethod
     */
    public List<String> getHttpMethod()
    {
        return httpMethod;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(httpMethod, isNotification, resource, serviceName, serviceVersion);
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
        ProxyMessageSelector other = (ProxyMessageSelector) obj;
        return Objects.equals(httpMethod, other.httpMethod) && isNotification == other.isNotification && Objects.equals(resource, other.resource)
               && Objects.equals(serviceName, other.serviceName) && Objects.equals(serviceVersion, other.serviceVersion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyMessageSelector [serviceName=" + serviceName + ", serviceVersion=" + serviceVersion + ", resource=" + resource + ", isNotification="
               + isNotification + ", httpMethod=" + httpMethod + "]";
    }

}