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
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KvtLookup;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyVariableAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarHeaderConstValue;

/**
 * 
 */
public class ProxyActionModifyVariable implements ProxyAction
{
    private final String name;
    private final String key;
    private final String tableName;

    /**
     * @param name
     * @param key
     * @param tableLookup
     */
    public ProxyActionModifyVariable(String name,
                                     String tableName,
                                     String key)
    {
        this.name = name;
        this.key = key;
        this.tableName = tableName;
    }

    public ProxyActionModifyVariable(ProxyActionModifyVariable mv)
    {
        this.name = mv.getName();
        this.key = mv.getKey();
        this.tableName = mv.getTableName();
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return the tableLookup
     */
    public String getTableName()
    {
        return tableName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionModifyVariable [name=" + name + ", key=" + key + ", tableName=" + tableName + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(key, name, tableName);
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
        ProxyActionModifyVariable other = (ProxyActionModifyVariable) obj;
        return Objects.equals(key, other.key) && Objects.equals(name, other.name) && Objects.equals(tableName, other.tableName);
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
                     .setActionModifyVariable(ModifyVariableAction.newBuilder()
                                                                  .setName(this.getName())
                                                                  .setTableLookup(KvtLookup.newBuilder()
                                                                                           .setTableName(this.getTableName())
                                                                                           .setKey(VarHeaderConstValue.newBuilder().setTermVar(this.getKey()))))
                     .build();
    }

}
