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
 * Created on: Aug 21, 2020
 *     Author: eavapsr
 */

/**
 * 
 */
package com.ericsson.esc.bsf.db.metrics;

import java.time.Duration;

public class CqlRequests
{
    private Duration highestLatency;
    private int significantDigits;
    private Duration refreshInterval;

    public CqlRequests(CqlRequestsBuilder builder)
    {
        this.highestLatency = builder.highestLatency;
        this.significantDigits = builder.significantDigits;
        this.refreshInterval = builder.refreshInterval;
    }

    public Duration getHighestLatency()
    {
        return highestLatency;
    }

    public int getSignificantDigits()
    {
        return significantDigits;
    }

    public Duration getRefreshInterval()
    {
        return refreshInterval;
    }

    @Override
    public String toString()
    {
        return "HighestLatency: " + this.highestLatency + " SignificantDigits: " + this.significantDigits + " RefreshInterval: " + this.refreshInterval;
    }

    public static class CqlRequestsBuilder
    {
        private Duration highestLatency;
        private int significantDigits;
        private Duration refreshInterval;

        public CqlRequestsBuilder()
        {
            // empty constructor
        }

        public CqlRequestsBuilder withHighestLatency(Duration highestLatency)
        {
            this.highestLatency = highestLatency;
            return this;
        }

        public CqlRequestsBuilder withSignificantDigits(int digits)
        {
            this.significantDigits = digits;
            return this;
        }

        public CqlRequestsBuilder withRefreshInterval(Duration refreshInterval)
        {
            this.refreshInterval = refreshInterval;
            return this;
        }

        public CqlRequests build()
        {
            return new CqlRequests(this);
        }

    }

}
