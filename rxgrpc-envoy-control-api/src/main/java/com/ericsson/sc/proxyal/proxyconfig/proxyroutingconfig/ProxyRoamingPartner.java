/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 18, 2020
 *     Author: eavapsr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyMessageValidation;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PlmnIdInfo;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoamingPartner;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoamingPartner.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding;

/**
 * 
 */
public class ProxyRoamingPartner
{
    private final String name;
    private Optional<String> poolName = Optional.empty();
    private Optional<String> inRequestCase = Optional.empty();
    private Optional<String> outRequestCase = Optional.empty();
    private Optional<String> inResponseCase = Optional.empty();
    private Optional<String> outResponseCase = Optional.empty();
    private Optional<List<IfProxyTopologyHiding>> topologyHiding = Optional.empty();
    private Optional<ProxyMessageValidation> requestMessageValidation = Optional.empty();
    private Optional<ProxyMessageValidation> responseMessageValidation = Optional.empty();
    private Optional<String> ownNetworkFqdn = Optional.empty();
    private Optional<PlmnIdInfo> plmnIdInfo = Optional.empty();

    public ProxyRoamingPartner(String name)
    {
        this.name = name;
    }

    public ProxyRoamingPartner(String name,
                               String poolName)
    {
        this(name);
        this.poolName = Optional.of(poolName);
    }

    public ProxyRoamingPartner(ProxyRoamingPartner pxRp)
    {
        this.name = pxRp.getName();
        this.poolName = pxRp.getPoolName();
        pxRp.getInRequestCase().ifPresent(this::setInRequestCase);
        pxRp.getInResponseCase().ifPresent(this::setInResponseCase);
        pxRp.getOutRequestCase().ifPresent(this::setOutRequestCase);
        pxRp.getOutResponseCase().ifPresent(this::setOutResponseCase);
        pxRp.getTopologyHiding().ifPresent(thl ->
        {
            this.topologyHiding = Optional.of(new ArrayList<IfProxyTopologyHiding>());
            thl.forEach(th -> this.topologyHiding.get().add(th.clone()));
        });
        pxRp.getRequestMessageValidation().ifPresent(this::setRequestMessageValidation);
        pxRp.getResponseMessageValidation().ifPresent(this::setResponseMessageValidation);
        pxRp.getOwnNetworkFqdn().ifPresent(this::setOwnNetworkFqdn);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the requestMessageValidation
     */
    public Optional<ProxyMessageValidation> getRequestMessageValidation()
    {
        return requestMessageValidation;
    }

    /**
     * @param requestMessageValidation the requestMessageValidation to set
     */
    public void setRequestMessageValidation(ProxyMessageValidation requestMessageValidation)
    {
        this.requestMessageValidation = Optional.ofNullable(requestMessageValidation);
    }

    /**
     * @return the responseMessageValidation
     */
    public Optional<ProxyMessageValidation> getResponseMessageValidation()
    {
        return responseMessageValidation;
    }

    /**
     * @param responseMessageValidation the responseMessageValidation to set
     */
    public void setResponseMessageValidation(ProxyMessageValidation responseMessageValidation)
    {
        this.responseMessageValidation = Optional.ofNullable(responseMessageValidation);
    }

    /**
     * @return the poolName
     */
    public Optional<String> getPoolName()
    {
        return poolName;
    }

    /**
     * This method is used to get the roaming partner plmnd ids that are configured
     * under the N32C container.
     *
     * @return the plmnIdInfo
     */
    public Optional<PlmnIdInfo> getPlmnIdInfo()
    {
        return plmnIdInfo;
    }

    /**
     * This method is used to set the roaming partner plmnd ids that are configured
     * under the N32C container.
     *
     * @param plmnIdInfo the plmnIdInfo to set
     */
    public void setPlmnIdInfo(PlmnIdInfo plmnIdInfo)
    {
        this.plmnIdInfo = Optional.ofNullable(plmnIdInfo);
    }

    /**
     * @return the inRequestCase
     */
    public Optional<String> getInRequestCase()
    {
        return inRequestCase;
    }

    /**
     * @param inRequestCase the inRequestCase to set
     */
    public void setInRequestCase(String inRequestCase)
    {
        this.inRequestCase = Optional.ofNullable(inRequestCase);
    }

    /**
     * @return the outRequestCase
     */
    public Optional<String> getOutRequestCase()
    {
        return outRequestCase;
    }

    /**
     * @param outRequestCase the outRequestCase to set
     */
    public void setOutRequestCase(String outRequestCase)
    {
        this.outRequestCase = Optional.ofNullable(outRequestCase);
    }

    /**
     * @return the inResponseCase
     */
    public Optional<String> getInResponseCase()
    {
        return inResponseCase;
    }

    /**
     * @param inResponseCase the inResponseCase to set
     */
    public void setInResponseCase(String inResponseCase)
    {
        this.inResponseCase = Optional.ofNullable(inResponseCase);
    }

    /**
     * @return the outResponseCase
     */
    public Optional<String> getOutResponseCase()
    {
        return outResponseCase;
    }

    /**
     * @param outResponseCase the outResponseCase to set
     */
    public void setOutResponseCase(String outResponseCase)
    {
        this.outResponseCase = Optional.ofNullable(outResponseCase);
    }

    /**
     * @param topologyHiding the topologyHiding to set
     */
    public void setTopologyHiding(Optional<List<IfProxyTopologyHiding>> topologyHiding)
    {
        this.topologyHiding = topologyHiding;
    }

    /**
     * @return the topologyHidingList
     */
    public Optional<List<IfProxyTopologyHiding>> getTopologyHiding()
    {
        return topologyHiding;
    }

    /**
     * @return the ownNetworkFqdn
     */
    public Optional<String> getOwnNetworkFqdn()
    {
        return ownNetworkFqdn;
    }

    /**
     * @param ownNetworkFqdn the ownNetworkFqdn to set
     */
    public void setOwnNetworkFqdn(String ownNetworkFqdn)
    {
        this.ownNetworkFqdn = Optional.ofNullable(ownNetworkFqdn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(inRequestCase,
                            inResponseCase,
                            name,
                            outRequestCase,
                            outResponseCase,
                            ownNetworkFqdn,
                            poolName,
                            plmnIdInfo,
                            requestMessageValidation,
                            responseMessageValidation,
                            topologyHiding);
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
        ProxyRoamingPartner other = (ProxyRoamingPartner) obj;
        return Objects.equals(inRequestCase, other.inRequestCase) && Objects.equals(inResponseCase, other.inResponseCase) && Objects.equals(name, other.name)
               && Objects.equals(outRequestCase, other.outRequestCase) && Objects.equals(outResponseCase, other.outResponseCase)
               && Objects.equals(ownNetworkFqdn, other.ownNetworkFqdn) && Objects.equals(poolName, other.poolName)
               && Objects.equals(requestMessageValidation, other.requestMessageValidation)
               && Objects.equals(responseMessageValidation, other.responseMessageValidation) && Objects.equals(topologyHiding, other.topologyHiding)
               && Objects.equals(plmnIdInfo, other.plmnIdInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyRoamingPartner [name=" + name + ", poolName=" + poolName + ", plmnIdInfo=" + plmnIdInfo + ", inRequestCase=" + inRequestCase
               + ", outRequestCase=" + outRequestCase + ", inResponseCase=" + inResponseCase + ", outResponseCase=" + outResponseCase + ", ownNetworkFqdn="
               + ownNetworkFqdn + ", topologyHiding=" + topologyHiding + ", requestMessageValidation=" + requestMessageValidation
               + ", responseMessageValidation=" + responseMessageValidation + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */
    public Builder initBuilder()
    {
        var rpBuilder = RoamingPartner.newBuilder().setName(this.name);
        this.poolName.ifPresent(rpBuilder::setPoolName);
        this.plmnIdInfo.ifPresent(rpBuilder::setPlmnIds);

        this.topologyHiding.ifPresent(th ->
        {
            TopologyHiding.Builder thBuilder = TopologyHiding.newBuilder();
            th.forEach(t -> t.initBuilder(thBuilder));
            rpBuilder.setTopologyHiding(thBuilder);
        });
        this.requestMessageValidation.ifPresent(reqMV -> rpBuilder.setRequestValidation(reqMV.build()));
        this.responseMessageValidation.ifPresent(resMV -> rpBuilder.setResponseValidation(resMV.build()));
        this.ownNetworkFqdn.ifPresent(rpBuilder::setOwnNetworkFqdn);

        return rpBuilder;
    }

}
