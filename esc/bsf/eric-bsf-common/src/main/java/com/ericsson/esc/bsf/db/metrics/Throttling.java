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
 * Created on: Aug 22, 2020
 *     Author: eavapsr
 */

/**
 * 
 */
package com.ericsson.esc.bsf.db.metrics;

import java.time.Duration;

public class Throttling
{
    private Duration highestLatency;
    private int significantDigits;
    private Duration refreshInterval;

    public Throttling(ThrottlingBuilder builder)
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

    public static class ThrottlingBuilder
    {
        private Duration highestLatency;
        private int significantDigits;
        private Duration refreshInterval;

        public ThrottlingBuilder()
        {
            // empty constructor
        }

        public ThrottlingBuilder withHighestLatency(Duration highestLatency)
        {
            this.highestLatency = highestLatency;
            return this;
        }

        public ThrottlingBuilder withSignificantDigits(int digits)
        {
            this.significantDigits = digits;
            return this;
        }

        public ThrottlingBuilder withRefreshInterval(Duration refreshInterval)
        {
            this.refreshInterval = refreshInterval;
            return this;
        }

        public Throttling build()
        {
            return new Throttling(this);
        }

    }
}
