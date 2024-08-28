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
 * Created on: Feb 18, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.Objects;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonPropertyOrder({ Pair.PROPERTY_FIRST, Pair.PROPERTY_SECOND })
public class Pair<F, S> implements Comparable<Pair<F, S>>
{
    static final String PROPERTY_FIRST = "first";
    static final String PROPERTY_SECOND = "second";

    private static final ObjectMapper mapper = Jackson.om();

    public static <F, S> Pair<F, S> of(final F first,
                                       final S second)
    {
        return new Pair<>(first, second);
    }

    @JsonProperty(PROPERTY_FIRST)
    private F first;

    @JsonProperty(PROPERTY_SECOND)
    private S second;

    protected Pair()
    {
        this.first = null;
        this.second = null;
    }

    protected Pair(final F first,
                   final S second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair<F, S> o)
    {
        return this.equals(o) ? 0 : this.hashCode() - o.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
            return true;

        if (!(other instanceof Pair))
            return false;

        final Pair<?, ?> that = ((Pair<?, ?>) other);

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second);
    }

    public F getFirst()
    {
        return first;
    }

    public S getSecond()
    {
        return second;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.first, this.second);
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
