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
 * Created on: Nov 3, 2020
 *     Author: epaxale
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

/**
 * 
 */
public class ProxyJwtRule
{

    private final String regexp;
    private final String providerName;

    public ProxyJwtRule(String regexp,
                        String providerName)
    {
        this.regexp = regexp;
        this.providerName = providerName;
    }

    public ProxyJwtRule(ProxyJwtRule jwtRule)
    {
        this.regexp = jwtRule.getRegexp();
        this.providerName = jwtRule.getProviderName();
    }

    /**
     * @return the providerName
     */
    public String getProviderName()
    {
        return providerName;
    }

    /**
     * @return the prefix
     */
    public String getRegexp()
    {
        return regexp;
    }

    @Override
    public String toString()
    {
        return "\nProvider Name: " + providerName + "\nRegexp: " + regexp;
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((regexp == null) ? 0 : regexp.hashCode());
        result = prime * result + ((providerName == null) ? 0 : providerName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof ProxyJwtRule))
        {
            return false;
        }

        var other = (ProxyJwtRule) o;

        var prefixEquals = (this.regexp == null && other.regexp == null) || (this.regexp != null && this.regexp.equals(other.regexp));
        var providerEquals = (this.providerName == null && other.providerName == null)
                             || (this.providerName != null && this.providerName.equals(other.providerName));

        return prefixEquals && providerEquals;
    }

}
