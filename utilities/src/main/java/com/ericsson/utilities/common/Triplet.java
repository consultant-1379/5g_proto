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
 * Created on: Aug 24, 2021
 *     Author: echaias (copy of the Pair class)
 */

package com.ericsson.utilities.common;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonPropertyOrder({ Triplet.PROPERTY_FIRST, Triplet.PROPERTY_SECOND })
public class Triplet<F, S, T> implements Comparable<Triplet<F, S, T>>
{
    static final String PROPERTY_FIRST = "first";
    static final String PROPERTY_SECOND = "second";
    static final String PROPERTY_THIRD = "third";

    private static final ObjectMapper mapper = Jackson.om();

    public static <F, S, T> Triplet<F, S, T> of(final F first,
                                                final S second,
                                                final T third)
    {
        return new Triplet<>(first, second, third);
    }

    @JsonProperty(PROPERTY_FIRST)
    private F first;

    @JsonProperty(PROPERTY_SECOND)
    private S second;

    @JsonProperty(PROPERTY_SECOND)
    private T third;

    protected Triplet()
    {
        this.first = null;
        this.second = null;
        this.third = null;
    }

    protected Triplet(final F first,
                      final S second,
                      final T third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public int compareTo(Triplet<F, S, T> o)
    {
        return this.equals(o) ? 0 : this.hashCode() - o.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
            return true;

        if (!(other instanceof Triplet))
            return false;

        final Triplet<?, ?, ?> rhs = ((Triplet<?, ?, ?>) other);

        return (this.first == rhs.first || ((this.first != null) && this.first.equals(rhs.first)))
               && (this.second == rhs.second || ((this.second != null) && this.second.equals(rhs.second)))
               && (this.third == rhs.third || ((this.third != null) && this.third.equals(rhs.third)));
    }

    public F getFirst()
    {
        return first;
    }

    public S getSecond()
    {
        return second;
    }

    public T getThird()
    {
        return third;
    }

    @Override
    public int hashCode()
    {
        var result = 1;
        result = ((result * 31) + ((this.first == null) ? 0 : this.first.hashCode()));
        result = ((result * 31) + ((this.second == null) ? 0 : this.second.hashCode()));
        result = ((result * 31) + ((this.third == null) ? 0 : this.third.hashCode()));

        return result;
    }

    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return "";
        }
    }
}
