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
 * Created on: Mar 2, 2021
 *     Author: cardinulls
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterCase;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterCase.Builder;

/**
 * 
 */
public class ProxyScreeningCase
{
    private final String name;
    private List<ProxyFilterData> messageData = new ArrayList<>();
    private List<ProxyScreeningRule> screeningRules = new ArrayList<>();

    public ProxyScreeningCase(String name)
    {
        this.name = name;
    }

    public ProxyScreeningCase(ProxyScreeningCase proxyScreeningCase)
    {
        this.name = proxyScreeningCase.getName();
        proxyScreeningCase.getMessageData().forEach(datum -> this.messageData.add(datum));
        proxyScreeningCase.getScreeningRules().forEach(rule -> this.screeningRules.add(rule));
    }

    public void addMessageData(ProxyFilterData rd)
    {
        this.messageData.add(rd);
    }

    public void addScreeningRule(ProxyScreeningRule rr)
    {
        this.screeningRules.add(rr);
    }

    public String getName()
    {
        return this.name;
    }

    public List<ProxyFilterData> getMessageData()
    {
        return this.messageData;
    }

    public List<ProxyScreeningRule> getScreeningRules()
    {
        return this.screeningRules;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messageData == null) ? 0 : messageData.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((screeningRules == null) ? 0 : screeningRules.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyScreeningCase other = (ProxyScreeningCase) obj;
        if (messageData == null)
        {
            if (other.messageData != null)
                return false;
        }
        else if (!messageData.equals(other.messageData))
            return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (screeningRules == null)
        {
            if (other.screeningRules != null)
                return false;
        }
        else if (!screeningRules.equals(other.screeningRules))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */

    public Builder initBuilder()
    {
        var fcBuilder = FilterCase.newBuilder().setName(this.name);

        for (var fd : this.messageData)
        {
            var fdBuilder = fd.initBuilder();
            fcBuilder.addFilterData(fdBuilder.build());
        }

        for (var fr : this.screeningRules)
        {
            var frBuilder = fr.initBuilder();
            fcBuilder.addFilterRules(frBuilder.build());
        }

        return fcBuilder;
    }

}
