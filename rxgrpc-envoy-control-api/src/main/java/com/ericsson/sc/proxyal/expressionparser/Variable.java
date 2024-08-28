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

import com.google.protobuf.GeneratedMessageV3.Builder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Value;

public class Variable implements Expression
{
    private String name;

    public Variable(String name)
    {
        this.name = name;
    }

    public Builder<?> construct()
    {
        return Value.newBuilder().setTermVar(name);
    }

    @Override
    public String toString()
    {
        return String.format("term_var(%s)", this.name);
    }
}
