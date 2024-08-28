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
 * Created on: Dec 1, 2020
 *     Author: enocakh
 */

package com.ericsson.sc.proxyal.expressionparser;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessageV3.Builder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Condition;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Value;

public class SimpleTerm<T> implements Expression
{
    private T value;
    private boolean boolValue = false;

    public SimpleTerm(T value)
    {
        this.value = value;
    }

    public SimpleTerm(SimpleTerm<T> simpleTerm,
                      boolean boolValue)
    {
        this.value = simpleTerm.value;
        this.boolValue = boolValue;
    }

    @Override
    public Builder<?> construct()
    {
        if (value instanceof Boolean && !boolValue)
        {
            return Condition.newBuilder().setTermBoolean((Boolean) value);
        }
        else if (value instanceof Boolean && boolValue)
        {
            return Value.newBuilder().setTermBoolean((Boolean) value);
        }
        else if (value instanceof String)
        {

            return Value.newBuilder().setTermString((String) value);
        }
        else if (value instanceof Double)
        {
            return Value.newBuilder().setTermNumber((Double) value);
        }
        return Value.newBuilder();
    }

    @Override
    public String toString()
    {
        if (value instanceof String)
        {
            return String.format("term_string(%s)", this.value);
        }
        else if (value instanceof Boolean)
        {
            return String.format("term_boolean(%s)", this.value);
        }
        else if (value instanceof Double)
        {
            return String.format("term_number(%s)", this.value);
        }

        return StringUtils.EMPTY;
    }
}
