/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Sep 3, 2021 Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

public final class Rate
{
    long tps;
    Stats stats;

    public Rate()
    {
        tps = 0L;
        stats = null;
    }

    public Rate(Rate oldRate,
                Stats newStats)
    {
        this.tps = oldRate.stats == null ? newStats.getTotal() : (newStats.getTotal() - oldRate.stats.getTotal());
        this.stats = newStats;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("TPS=");
        builder.append(tps);
        builder.append(" ");
        builder.append(stats);
        return builder.toString();
    }

}
