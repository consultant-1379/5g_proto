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
 * Created on: Feb 18, 2022
 *     Author: ecaoyuk
 */

package com.ericsson.sc.proxyal.proxyconfig;

/**
 * Simple container class. Holds all the TcpKeepalive data. It is attached to
 * the ProxyCluster and ProxyListener object.
 */
public class ProxyTcpKeepalive
{
    private Integer probes;
    private Integer time;
    private Integer interval;

    public ProxyTcpKeepalive()
    {
        this.probes = 6;
        this.time = 5;
        this.interval = 5;
    }

    public ProxyTcpKeepalive(Integer probes,
                             Integer time,
                             Integer interval)
    {
        this.probes = probes;
        this.time = time;
        this.interval = interval;

    }

    public Integer getTime()
    {
        return time;
    }

    public void setTime(Integer time)
    {
        this.time = time;
    }

    public Integer getProbes()
    {
        return probes;
    }

    public void setProbes(Integer probes)
    {
        this.probes = probes;
    }

    public Integer getInterval()
    {
        return interval;
    }

    public void setInterval(Integer interval)
    {
        this.interval = interval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
        result = prime * result + ((probes == null) ? 0 : probes.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
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
        ProxyTcpKeepalive other = (ProxyTcpKeepalive) obj;
        if (interval == null)
        {
            if (other.interval != null)
                return false;
        }
        else if (!interval.equals(other.interval))
            return false;
        if (probes == null)
        {
            if (other.probes != null)
                return false;
        }
        else if (!probes.equals(other.probes))
            return false;
        if (time == null)
        {
            if (other.time != null)
                return false;
        }
        else if (!time.equals(other.time))
            return false;
        return true;
    }

}
