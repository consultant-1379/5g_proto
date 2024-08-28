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

import java.util.List;
import java.util.Optional;

import com.ericsson.sc.proxyal.expressionparser.Expression;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Condition;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterRule;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterRule.Builder;

/**
 * 
 */
public class ProxyScreeningRule
{
    private final String name;
    private final Optional<Expression> condition;

    private List<ProxyScreeningAction> screeningActions;

    public ProxyScreeningRule(String name,
                              Expression condition,
                              List<ProxyScreeningAction> actions)
    {
        this.name = name;
        this.condition = Optional.ofNullable(condition);
        this.screeningActions = actions;
    }

    public ProxyScreeningRule(ProxyScreeningRule proxyScreeningRule)
    {
        this.name = proxyScreeningRule.getName();
        this.condition = Optional.ofNullable(proxyScreeningRule.getCondition().orElse(null));
        proxyScreeningRule.getScreeningActions().forEach(action -> this.screeningActions.add(action));
    }

    /**
     * @return the condition
     */
    public Optional<Expression> getCondition()
    {
        return condition;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the screeningActions
     */
    public List<ProxyScreeningAction> getScreeningActions()
    {
        return screeningActions;
    }

    /**
     * @param screeningActions the screeningActions to set
     */
    public void setScreeningActions(List<ProxyScreeningAction> screeningActions)
    {
        this.screeningActions = screeningActions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + condition.hashCode();
        result = prime * result + ((screeningActions == null) ? 0 : screeningActions.hashCode());
        return result;
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

        ProxyScreeningRule other = (ProxyScreeningRule) obj;

        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;

        if (!condition.equals(other.condition))
            return false;

        if (screeningActions == null)
        {
            if (other.screeningActions != null)
                return false;
        }
        else if (!screeningActions.equals(other.screeningActions))
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
        var frBuilder = FilterRule.newBuilder().setName(this.getName());

        this.getCondition().ifPresent(v -> frBuilder.setCondition((Condition.Builder) v.construct()));

        this.getScreeningActions().forEach(sa ->
        {
            var action = sa.initBuilder();
            if (!action.getAllFields().isEmpty())
            {
                frBuilder.addActions(action);
            }
        });
        return frBuilder;
    }

}
