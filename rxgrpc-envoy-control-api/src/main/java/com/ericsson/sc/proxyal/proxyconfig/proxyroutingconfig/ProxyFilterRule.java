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
 * Created on: Dec 16, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.sc.proxyal.expressionparser.Expression;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Condition;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterRule;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterRule.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

/**
 * 
 */
public class ProxyFilterRule
{
    private final String name;
    private final Optional<Expression> condition;
    private final RoutingBehaviour routingBehaviour;
    private List<ProxyRoutingAction> routingActions = new ArrayList<>();

    public ProxyFilterRule(String name,
                           Expression condition,
                           RoutingBehaviour routingBehaviour,
                           List<ProxyRoutingAction> routingActions)
    {
        this.name = name;
        this.condition = Optional.ofNullable(condition);
        this.routingBehaviour = routingBehaviour;
        this.routingActions = routingActions;
    }

    // COPY constructor
    public ProxyFilterRule(ProxyFilterRule anotherProxyFilterRule)
    {
        this.name = anotherProxyFilterRule.name;
        this.condition = anotherProxyFilterRule.condition;
        this.routingBehaviour = anotherProxyFilterRule.routingBehaviour;
        this.routingActions = anotherProxyFilterRule.routingActions.stream().map(ProxyRoutingAction::new).collect(Collectors.toList());
    }

    /**
     * @return the routingActions
     */
    public List<ProxyRoutingAction> getRoutingActions()
    {
        return routingActions;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the condition
     */
    public Optional<Expression> getCondition()
    {
        return condition;
    }

    /**
     * @return the routingBehaviour
     */
    public RoutingBehaviour getRoutingBehaviour()
    {
        return routingBehaviour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyFilterRule [name=" + name + ", condition=" + condition + ", routingBehaviour=" + routingBehaviour + ", routingActions=" + routingActions
               + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(condition, name, routingActions, routingBehaviour);
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
        ProxyFilterRule other = (ProxyFilterRule) obj;
        return Objects.equals(condition, other.condition) && Objects.equals(name, other.name) && Objects.equals(routingActions, other.routingActions)
               && routingBehaviour == other.routingBehaviour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */
    public Builder initBuilder()
    {
        var frBuilder = FilterRule.newBuilder().setName(this.getName());

        this.getCondition().ifPresent(v -> frBuilder.setCondition((Condition.Builder) v.construct()));

        for (var ra : this.getRoutingActions())
        {
            var actionSlfLookup = ra.getSlfLookup();
            var actionModifyVar = ra.getActionModifyVariable();
            var actionReject = ra.getActionRejectMessage();
            var actionAddHeader = ra.getActionAddHeader();
            var actionLog = ra.getActionLog();
            var actionDropMessage = ra.getActionDropMessage();
            var actionNfDiscovery = ra.getActionNfDiscovery();

            if (actionSlfLookup.isPresent() || actionModifyVar.isPresent())
            {
                actionSlfLookup.ifPresent(slfLookup -> frBuilder.addActions(slfLookup.buildAction()));
                actionModifyVar.ifPresent(modVar -> frBuilder.addActions(modVar.buildAction()));
            }
            else if (actionReject.isPresent())
            {
                frBuilder.addActions(actionReject.get().buildAction());
            }
            else if (actionAddHeader.isPresent())
            {
                frBuilder.addActions(actionAddHeader.get().buildAction());
            }
            else if (actionLog.isPresent())
            {
                frBuilder.addActions(actionLog.get().buildAction());
            }
            else if (actionDropMessage.isPresent())
            {
                var actionBuilder = Action.newBuilder().setActionDropMessage(actionDropMessage.get()).build();
                frBuilder.addActions(actionBuilder);
            }
            else if (actionNfDiscovery.isPresent())
            {
                frBuilder.addActions(actionNfDiscovery.get().buildAction());
            }
            else
            {
                ra.setRoutingBehaviour(this.routingBehaviour);
                var action = ra.initBuilder();
                frBuilder.addActions(action);
            }
        }
        return frBuilder;
    }

}
