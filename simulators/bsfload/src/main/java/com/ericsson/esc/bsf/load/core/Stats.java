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
 * Created on: Sep 3, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

public final class Stats
{
    private long fail;
    private long success;
    private long startTime;

    Stats()
    {
        this.success = 0L;
        this.fail = 0L;
        this.startTime = System.nanoTime();
    }

    Stats(Stats old,
          Response latest)
    {
        this.success = old.success;
        this.fail = old.fail;
        this.startTime = old.startTime;

        if (latest.success)
        {
            this.success += 1;
        }
        else
        {
            this.fail += 1;
        }
    }

    public long getTotal()
    {
        return this.success + this.fail;
    }

    public double getSR()
    {
        return (double) Math.round((success * 100d / getTotal()) * 100) / 100;
    }

    public long getStartTime()
    {
        return startTime;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SR=");
        builder.append(this.getSR());
        builder.append("% [num-requests=");
        builder.append(getTotal());
        builder.append(", num-fails=");
        builder.append(fail);
        builder.append("]");
        return builder.toString();
    }
}
