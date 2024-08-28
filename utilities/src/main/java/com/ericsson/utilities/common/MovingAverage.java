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
 * Created on: Jun 18, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Calculates the moving average of a moving window of N values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "average", "unit", "window" })
public class MovingAverage
{
    private static final ObjectMapper json = Jackson.om();

    @JsonProperty("window")
    private final int size;
    @JsonProperty("unit")
    private final String unit;
    @JsonIgnore
    private final double init;
    @JsonIgnore
    private final double[] data;
    @JsonIgnore
    private int first;
    @JsonIgnore
    private int last;
    @JsonIgnore
    private double sum;

    public MovingAverage(int windowSize)
    {
        this(windowSize, "1", 0d);
    }

    public MovingAverage(int windowSize,
                         final String unit)
    {
        this(windowSize, unit, 0d);
    }

    public MovingAverage(final int windowSize,
                         final String unit,
                         final double init)
    {

        this.size = windowSize <= 0 ? 1 : windowSize;
        this.unit = unit;
        this.init = init;
        this.data = new double[this.size];

        for (int i = 0; i < this.size; ++i)
            this.data[i] = init;

        this.first = 0;
        this.last = this.size - 1;
        this.sum = init * this.size;
    }

    /**
     * Adds datum to the window and returns the newly calculated average of all data
     * in the window.
     * 
     * @param datum The datum to be added to the window.
     * @return The newly calculated average of all data in the window.
     */
    public synchronized double add(final double datum)
    {
        final double first = this.data[this.first];

        if (++this.first == this.size)
            this.first = 0;

        if (++this.last == this.size)
            this.last = 0;

        this.data[last] = datum;

        this.sum += (datum - first);

        return this.sum / this.size;
    }

    /**
     * Resets the average to it initial value but returns the average of all data in
     * the window prior to that.
     * 
     * @return The average of all data in the window prior to resetting.
     */
    public synchronized double clear()
    {
        final double avg = this.get();

        for (int i = 0; i < this.size; ++i)
            this.data[i] = init;

        return avg;
    }

    /**
     * Calculates and returns the average of all data in the window.
     * 
     * @return The average of all data in the window
     */
    @JsonProperty("average")
    public synchronized double get()
    {
        return this.sum / this.size;
    }

    @Override
    public String toString()
    {
        try
        {
            return json.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }
}
