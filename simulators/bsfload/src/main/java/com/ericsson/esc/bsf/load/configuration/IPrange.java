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
 * Created on: Jul 12, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.List;

import com.datastax.oss.driver.shaded.guava.common.net.InetAddresses;
import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the IP range for the TrafficSet. The generated requests begin from
 * the startIP and stepped by one until the range is reached.
 */
public class IPrange
{
    @JsonProperty("start-ip")
    private String startIP;
    private Long range;

    /**
     * @return the startIP
     */
    public String getStartIP()
    {
        return startIP;
    }

    /**
     * @param startIP the startIP to set
     */
    public void setStartIP(String startIP)
    {
        this.startIP = startIP;
    }

    /**
     * @return the range
     */
    public Long getRange()
    {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(long range)
    {
        this.range = range;
    }

    @Override
    public String toString()
    {
        return "IPrange [startIP=" + startIP + ", range=" + range + "]";
    }

    public List<InvalidParameter> validate(String setName)
    {
        final var cv = new ConfigurationValidator();

        final var validStartIp = this.startIP != null && InetAddresses.isInetAddress(this.startIP);

        var message = "A valid IP value is required for 'start-ip' in set " + setName;
        cv.check(validStartIp, "startIP", message);

        message = "A positive number value is required for 'range' in set " + setName;
        cv.checkNonNullPositive(range, "range", message);

        return cv.getInvalidParam();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        result = prime * result + ((startIP == null) ? 0 : startIP.hashCode());
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
        IPrange other = (IPrange) obj;
        if (range == null)
        {
            if (other.range != null)
                return false;
        }
        else if (!range.equals(other.range))
            return false;
        if (startIP == null)
        {
            if (other.startIP != null)
                return false;
        }
        else if (!startIP.equals(other.startIP))
            return false;
        return true;
    }
}
