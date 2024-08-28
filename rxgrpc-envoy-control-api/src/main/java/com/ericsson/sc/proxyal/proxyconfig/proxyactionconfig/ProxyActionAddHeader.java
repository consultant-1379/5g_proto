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
 * Created on: Jul 5, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig;

import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.AddHeaderAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IfExists;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarHeaderConstValue;

/**
 * 
 */
public class ProxyActionAddHeader implements ProxyAction
{
    private final String headerName;
    private final IfExists ifExistsValue;
    private final String headerValue;

    /**
     * @param name
     * @param key
     * @param tableLookup
     */
    public ProxyActionAddHeader(String name,
                                IfExists ifexists,
                                String value)
    {
        this.headerName = name;
        this.ifExistsValue = ifexists;
        this.headerValue = value;
    }

    public ProxyActionAddHeader(ProxyActionAddHeader ah)
    {
        this.headerName = ah.getHeaderName();
        this.ifExistsValue = ah.getIfExistsValue();
        this.headerValue = ah.getHeaderValue();
    }

    public String getHeaderName()
    {
        return headerName;
    }

    public IfExists getIfExistsValue()
    {
        return ifExistsValue;
    }

    public String getHeaderValue()
    {
        return headerValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionAddHeader [headerName=" + headerName + ", ifExistsValue=" + ifExistsValue + ", headerValue=" + headerValue + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(headerName, headerValue, ifExistsValue);
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
        ProxyActionAddHeader other = (ProxyActionAddHeader) obj;
        return Objects.equals(headerName, other.headerName) && Objects.equals(headerValue, other.headerValue) && ifExistsValue == other.ifExistsValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.proxyal.proxyconfig.ProxyAction#buildAction()
     */
    @Override
    public Action buildAction()
    {
        return Action.newBuilder()
                     .setActionAddHeader(AddHeaderAction.newBuilder()
                                                        .setName(this.getHeaderName())
                                                        .setValue(VarHeaderConstValue.newBuilder().setTermString(this.getHeaderValue()))
                                                        .setIfExists(this.getIfExistsValue()))
                     .build();
    }

}
