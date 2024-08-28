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
 * Created on: Apr 1, 2021
 *     Author: epitgio
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionAddHeader;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionLog;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionModifyVariable;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionNfDiscovery;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionRejectMessage;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionSlfLookup;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PreserveDiscParamIfIndirect;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PreserveIfIndirect;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RouteToPoolAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RouteToRoamingPartnerAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.StringList;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarHeaderConstValue;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarOrString;

/**
 * 
 */
public class ProxyRoutingAction
{
    private Optional<ProxyActionSlfLookup> slfLookup = Optional.empty();
    private Optional<ProxyActionModifyVariable> actionModifyVariable = Optional.empty();
    private Optional<ProxyActionAddHeader> actionAddHeader = Optional.empty();
    private Optional<ProxyActionRejectMessage> actionRejectMessage = Optional.empty();
    private Optional<ProxyActionLog> actionLog = Optional.empty();
    private Optional<ProxyActionNfDiscovery> actionNfDiscovery = Optional.empty();
    private Optional<Boolean> actionDropMessage = Optional.empty();

    private Optional<PreserveIfIndirect> preserveType = Optional.empty();

    private Optional<Boolean> keepAuthorityHeader = Optional.empty();

    private Optional<Boolean> preserveAllDiscParams = Optional.empty();
    private Optional<List<String>> preserveSelectedDiscParams = Optional.empty();
    private Optional<Integer> numOfRemoteReselections = Optional.empty();
    private Optional<Integer> numOfRemoteRetries = Optional.empty();

    // target
    private Optional<String> fromVarName = Optional.empty();
    private Optional<String> fromHeader = Optional.empty();

    // destination
    private Optional<String> destPoolVarName = Optional.empty();
    private Optional<String> destPoolRef = Optional.empty();
    private Optional<String> destRoamingPartnerRef = Optional.empty();

    // last resort pool
    private Optional<String> lastResPoolVarName = Optional.empty();
    private Optional<String> lastResPoolRef = Optional.empty();

    private RoutingBehaviour routingBehaviour;

    public ProxyRoutingAction()
    {
    }

    /**
     * Copy constructor
     * 
     * @param proxyRoutingActionBuilder
     */
    public ProxyRoutingAction(ProxyRoutingAction pra)
    {
        this.slfLookup = pra.getSlfLookup().map(ProxyActionSlfLookup::new);
        this.actionRejectMessage = pra.getActionRejectMessage().map(ProxyActionRejectMessage::new);
        this.actionLog = pra.getActionLog().map(ProxyActionLog::new);
        this.actionNfDiscovery = pra.getActionNfDiscovery().map(ProxyActionNfDiscovery::new);
        this.actionDropMessage = pra.getActionDropMessage().map(v -> v);
        this.preserveType = pra.getPreserveIfIndirect().map(v -> v);
        this.keepAuthorityHeader = pra.getKeepAuthorityHeader().map(v -> v);
        this.preserveAllDiscParams = pra.getPreserveAllDiscParams().map(v -> v);
        this.numOfRemoteReselections = pra.getNumOfRemoteRetries().map(v -> v);
        this.numOfRemoteRetries = pra.getNumOfRemoteReselections().map(v -> v);
        this.preserveSelectedDiscParams = pra.getPreserveSelectedDiscParams().map(v -> v);
        this.fromVarName = pra.getFromVarName().map(v -> v);
        this.fromHeader = pra.getFromHeader().map(v -> v);
        this.destPoolVarName = pra.getDestinationVarName().map(v -> v);
        this.destPoolRef = pra.getDestinationPoolRef().map(v -> v);
        this.destRoamingPartnerRef = pra.getDestinationRoamingPartner().map(v -> v);
        this.lastResPoolVarName = pra.getLastResortVarName().map(v -> v);
        this.lastResPoolRef = pra.getLastResortPoolRef().map(v -> v);
        this.actionModifyVariable = pra.getActionModifyVariable().map(ProxyActionModifyVariable::new);
        this.actionAddHeader = pra.getActionAddHeader().map(ProxyActionAddHeader::new);
        this.routingBehaviour = pra.routingBehaviour;
    }

    public Optional<PreserveIfIndirect> getPreserveIfIndirect()
    {
        return this.preserveType;
    }

    public ProxyRoutingAction setPreserveIfIndirect(PreserveIfIndirect value)
    {
        this.preserveType = Optional.ofNullable(value);
        return this;
    }

    /**
     * Returns the preserveSelectedDiscParams
     * 
     * @return
     */
    public Optional<List<String>> getPreserveSelectedDiscParams()
    {
        return this.preserveSelectedDiscParams;
    }

    /**
     * Sets the preserveSelectedDiscParams.
     * 
     * @param preserveAllDiscParams
     * @return
     */
    public ProxyRoutingAction setPreserveSelectedDiscParams(List<String> preserveSelectedDiscParams)
    {
        this.preserveSelectedDiscParams = Optional.ofNullable(preserveSelectedDiscParams);
        return this;
    }

    /**
     * Returns the preserveAllDiscParams
     * 
     * @return
     */
    public Optional<Boolean> getPreserveAllDiscParams()
    {
        return this.preserveAllDiscParams;
    }

    /**
     * Sets the preserveAllDiscParams.
     * 
     * @param preserveAllDiscParams
     * @return
     */
    public ProxyRoutingAction setPreserveAllDiscParams(Boolean preserveAllDiscParams)
    {
        this.preserveAllDiscParams = Optional.ofNullable(preserveAllDiscParams);
        return this;
    }

    /**
     * Returns the numOfRemoteReselections
     * 
     * @return
     */
    public Optional<Integer> getNumOfRemoteReselections()
    {
        return this.numOfRemoteReselections;
    }

    /**
     * Sets the numOfRemoteReselections.
     * 
     * @param numOfRemoteReselections
     * @return
     */
    public ProxyRoutingAction setNumOfRemoteReselections(Integer numOfRemoteReselections)
    {
        this.numOfRemoteReselections = Optional.ofNullable(numOfRemoteReselections);
        return this;
    }

    /**
     * Returns the numOfRemoteRetries
     * 
     * @return
     */
    public Optional<Integer> getNumOfRemoteRetries()
    {
        return this.numOfRemoteRetries;
    }

    /**
     * Sets the numOfRemoteRetries.
     * 
     * @param numOfRemoteRetries
     * @return
     */
    public ProxyRoutingAction setNumOfRemoteRetries(Integer numOfRemoteRetries)
    {
        this.numOfRemoteRetries = Optional.ofNullable(numOfRemoteRetries);
        return this;
    }

    /**
     * Returns the keepAuthorityHeader
     * 
     * @return
     */
    public Optional<Boolean> getKeepAuthorityHeader()
    {
        return this.keepAuthorityHeader;
    }

    /**
     * Sets the keepAuthorityHeader.
     * 
     * @param fromVarName
     * @return
     */
    public ProxyRoutingAction setKeepAuthorityHeader(Boolean keepAuthorityHeader)
    {
        this.keepAuthorityHeader = Optional.ofNullable(keepAuthorityHeader);
        return this;
    }

    /**
     * Sets the variable name for the target and wraps it into a nullable optional.
     * 
     * @param fromVarName
     * @return
     */
    public ProxyRoutingAction setFromVarName(String fromVarName)
    {
        this.fromVarName = Optional.ofNullable(fromVarName);
        return this;
    }

    /**
     * Returns the variable name for the target
     * 
     * @return
     */
    public Optional<String> getFromVarName()
    {
        return this.fromVarName;
    }

    /**
     * Sets the target header.
     * 
     * @param value
     * @return
     */
    public ProxyRoutingAction setFromHeader(String value)
    {
        this.fromHeader = Optional.ofNullable(value);
        return this;
    }

    public Optional<String> getFromHeader()
    {
        return this.fromHeader;
    }

    /**
     * Sets the destination pool to the pool reference defined. The value is
     * internally wrapped into a nullable optional.
     * 
     * @param destPoolRef
     * @return
     */
    public ProxyRoutingAction setDestinationPoolRef(String destPoolRef)
    {
        this.destPoolRef = Optional.ofNullable(destPoolRef);
        return this;
    }

    public Optional<String> getDestinationPoolRef()
    {
        return this.destPoolRef;
    }

    /**
     * Sets the destination pool to the var name defined. The value is internally
     * wrapped into a nullable optional.
     * 
     * @param varName
     * @return
     */
    public ProxyRoutingAction setDestinationVarName(String varName)
    {
        this.destPoolVarName = Optional.ofNullable(varName);
        return this;
    }

    public Optional<String> getDestinationVarName()
    {
        return this.destPoolVarName;
    }

    /**
     * Sets the destination to a roaming partner reference. The value is internally
     * wrapped into a nullable optional.
     * 
     * @param roamingPartnerRef
     * @return
     */
    public ProxyRoutingAction setDestinationRoamingPartner(String roamingPartnerRef)
    {
        this.destRoamingPartnerRef = Optional.ofNullable(roamingPartnerRef);
        return this;
    }

    public Optional<String> getDestinationRoamingPartner()
    {
        return this.destRoamingPartnerRef;
    }

    /**
     * Sets the last resort pool to the pool reference defined. The value is
     * internally wrapped into a nullable optional.
     * 
     * @param destPoolRef
     * @return
     */
    public ProxyRoutingAction setLastResortPoolRef(String lastResortPoolRef)
    {
        this.lastResPoolRef = Optional.ofNullable(lastResortPoolRef);
        return this;
    }

    public Optional<String> getLastResortPoolRef()
    {
        return this.lastResPoolRef;
    }

    /**
     * Sets the last resort pool to the var name defined. The value is internally
     * wrapped into a nullable optional.
     * 
     * @param varName
     * @return
     */
    public ProxyRoutingAction setLastResortVarName(String varName)
    {
        this.lastResPoolVarName = Optional.ofNullable(varName);
        return this;
    }

    public Optional<String> getLastResortVarName()
    {
        return this.lastResPoolVarName;
    }

    /**
     * @return the slfLookup
     */
    public Optional<ProxyActionSlfLookup> getSlfLookup()
    {
        return slfLookup;
    }

    /**
     * @param slfLookup the slfLookup to set
     */
    public ProxyRoutingAction setSlfLookup(ProxyActionSlfLookup slfLookup)
    {
        this.slfLookup = Optional.ofNullable(slfLookup);
        return this;
    }

    public Optional<ProxyActionRejectMessage> getActionRejectMessage()
    {
        return actionRejectMessage;
    }

    public ProxyRoutingAction setActionRejectMessage(Optional<ProxyActionRejectMessage> actionRejectMessage)
    {
        this.actionRejectMessage = actionRejectMessage;
        return this;
    }

    public Optional<ProxyActionAddHeader> getActionAddHeader()
    {
        return actionAddHeader;
    }

    public void setActionAddHeader(Optional<ProxyActionAddHeader> actionAddHeader)
    {
        this.actionAddHeader = actionAddHeader;
    }

    /**
     * @return the actionModifyVariable
     */
    public Optional<ProxyActionModifyVariable> getActionModifyVariable()
    {
        return actionModifyVariable;
    }

    /**
     * @param actionModifyVariable the actionModifyVariable to set
     */
    public void setActionModifyVariable(Optional<ProxyActionModifyVariable> actionModifyVariable)
    {
        this.actionModifyVariable = actionModifyVariable;
    }

    public Optional<ProxyActionLog> getActionLog()
    {
        return actionLog;
    }

    public void setActionLog(Optional<ProxyActionLog> actionLog)
    {
        this.actionLog = actionLog;
    }

    public Optional<ProxyActionNfDiscovery> getActionNfDiscovery()
    {
        return actionNfDiscovery;
    }

    public void setActionNfDiscovery(Optional<ProxyActionNfDiscovery> actionNfDiscovery)
    {
        this.actionNfDiscovery = actionNfDiscovery;
    }

    public Optional<Boolean> getActionDropMessage()
    {
        return actionDropMessage;
    }

    public void setActionDropMessage(Optional<Boolean> drop)
    {
        this.actionDropMessage = drop;
    }

    public RoutingBehaviour getRoutingBehaviour()
    {
        return routingBehaviour;
    }

    public void setRoutingBehaviour(RoutingBehaviour routingBehaviour)
    {
        this.routingBehaviour = routingBehaviour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyRoutingAction [slfLookup=" + slfLookup + ", actionModifyVariable=" + actionModifyVariable + ", actionAddHeader=" + actionAddHeader
               + ", actionRejectMessage=" + actionRejectMessage + ", actionLog=" + actionLog + ", actionDropMessage=" + actionDropMessage
               + ", actionNfDiscovery=" + actionNfDiscovery + ", preserveType=" + preserveType + ", keepAuthorityHeader=" + keepAuthorityHeader
               + ", preserveAllDiscParams=" + preserveAllDiscParams + ", preserveSelectedDiscParams=" + preserveSelectedDiscParams + ", fromVarName="
               + fromVarName + ", fromHeader=" + fromHeader + ", destPoolVarName=" + destPoolVarName + ", destPoolRef=" + destPoolRef
               + ", destRoamingPartnerRef=" + destRoamingPartnerRef + ", lastResPoolVarName=" + lastResPoolVarName + ", lastResPoolRef=" + lastResPoolRef
               + ", routingBehaviour=" + routingBehaviour + ", numOfRemoteReselections=" + numOfRemoteReselections + ", numOfRemoteRetries="
               + numOfRemoteRetries + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(actionAddHeader,
                            actionModifyVariable,
                            actionRejectMessage,
                            actionLog,
                            actionDropMessage,
                            destPoolRef,
                            destPoolVarName,
                            destRoamingPartnerRef,
                            fromHeader,
                            fromVarName,
                            keepAuthorityHeader,
                            lastResPoolRef,
                            lastResPoolVarName,
                            preserveType,
                            routingBehaviour,
                            slfLookup,
                            actionNfDiscovery,
                            preserveAllDiscParams,
                            preserveSelectedDiscParams,
                            numOfRemoteReselections,
                            numOfRemoteRetries);
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
        ProxyRoutingAction other = (ProxyRoutingAction) obj;
        return Objects.equals(actionAddHeader, other.actionAddHeader) && Objects.equals(actionModifyVariable, other.actionModifyVariable)
               && Objects.equals(actionRejectMessage, other.actionRejectMessage) && Objects.equals(actionLog, other.actionLog)
               && Objects.equals(actionDropMessage, other.actionDropMessage) && Objects.equals(destPoolRef, other.destPoolRef)
               && Objects.equals(destPoolVarName, other.destPoolVarName) && Objects.equals(destRoamingPartnerRef, other.destRoamingPartnerRef)
               && Objects.equals(fromHeader, other.fromHeader) && Objects.equals(fromVarName, other.fromVarName)
               && Objects.equals(keepAuthorityHeader, other.keepAuthorityHeader) && Objects.equals(lastResPoolRef, other.lastResPoolRef)
               && Objects.equals(preserveAllDiscParams, other.preserveAllDiscParams)
               && Objects.equals(preserveSelectedDiscParams, other.preserveSelectedDiscParams) && Objects.equals(lastResPoolVarName, other.lastResPoolVarName)
               && Objects.equals(preserveType, other.preserveType) && Objects.equals(actionNfDiscovery, other.actionNfDiscovery)
               && Objects.equals(numOfRemoteReselections, other.numOfRemoteReselections) && Objects.equals(numOfRemoteRetries, other.numOfRemoteRetries)
               && routingBehaviour == other.routingBehaviour && Objects.equals(slfLookup, other.slfLookup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */
    public Builder initBuilder()
    {
        return this.getDestinationRoamingPartner().map(rpName ->
        {
            var routeToRoamingPartnerBuilder = buildRouteToRpBuilder(rpName);
            return Action.newBuilder().setActionRouteToRoamingPartner(routeToRoamingPartnerBuilder);
        }).orElseGet(() ->
        {
            var routeToPoolBuilder = buildRouteToPool();
            return Action.newBuilder().setActionRouteToPool(routeToPoolBuilder);
        });
    }

    public RouteToRoamingPartnerAction.Builder buildRouteToRpBuilder(String rpName)
    {
        var routeToRoamingPartnerBuilder = RouteToRoamingPartnerAction.newBuilder().setRoamingPartnerName(rpName).setRoutingBehaviour(this.routingBehaviour);
        this.getPreserveIfIndirect().ifPresent(routeToRoamingPartnerBuilder::setPreserveIfIndirect);
        this.getKeepAuthorityHeader().ifPresent(routeToRoamingPartnerBuilder::setKeepAuthorityHeader);

        return routeToRoamingPartnerBuilder;

    }

    public RouteToPoolAction.Builder buildRouteToPool()
    {
        var routeToPoolBuilder = RouteToPoolAction.newBuilder().setRoutingBehaviour(this.routingBehaviour);
        this.getDestinationPoolRef().ifPresent(poolRef -> routeToPoolBuilder.setPoolName(VarOrString.newBuilder().setTermString(poolRef)));
        this.getDestinationVarName().ifPresent(poolVar -> routeToPoolBuilder.setPoolName(VarOrString.newBuilder().setTermVar(poolVar)));

        this.getPreserveIfIndirect().ifPresent(routeToPoolBuilder::setPreserveIfIndirect);
        this.getKeepAuthorityHeader().ifPresent(routeToPoolBuilder::setKeepAuthorityHeader);

        this.getPreserveAllDiscParams()
            .ifPresent(useAll -> routeToPoolBuilder.setPreserveDiscParamsIfIndirect(PreserveDiscParamIfIndirect.newBuilder().setPreserveAll(useAll).build()));

        this.getNumOfRemoteReselections().ifPresent(num -> routeToPoolBuilder.setRemoteReselections(UInt32Value.newBuilder().setValue(num).build()));
        this.getNumOfRemoteRetries().ifPresent(num -> routeToPoolBuilder.setRemoteRetries(UInt32Value.newBuilder().setValue(num).build()));

        this.getPreserveSelectedDiscParams()
            .ifPresent(useAll -> routeToPoolBuilder.setPreserveDiscParamsIfIndirect(PreserveDiscParamIfIndirect.newBuilder()
                                                                                                               .setPreserveParams(StringList.newBuilder()
                                                                                                                                            .addAllValues(this.getPreserveSelectedDiscParams()
                                                                                                                                                              .get())
                                                                                                                                            .build())));

        this.getFromHeader().ifPresent(header -> routeToPoolBuilder.setPreferredTarget(VarHeaderConstValue.newBuilder().setTermHeader(header)));
        this.getFromVarName().ifPresent(varName -> routeToPoolBuilder.setPreferredTarget(VarHeaderConstValue.newBuilder().setTermVar(varName)));
        return routeToPoolBuilder;
    }

}