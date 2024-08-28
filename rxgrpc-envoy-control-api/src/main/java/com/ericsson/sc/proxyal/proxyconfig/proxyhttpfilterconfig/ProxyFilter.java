/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 18, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

public abstract class ProxyFilter
{
    private final String filterName;

    public ProxyFilter(String name)
    {
        this.filterName = name;
    }

    public String getName()
    {
        return this.filterName;
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((filterName == null) ? 0 : filterName.hashCode());
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
        ProxyFilter other = (ProxyFilter) obj;
        if (filterName == null)
        {
            if (other.filterName != null)
                return false;
        }
        else if (!filterName.equals(other.filterName))
            return false;
        return true;
    }
}
