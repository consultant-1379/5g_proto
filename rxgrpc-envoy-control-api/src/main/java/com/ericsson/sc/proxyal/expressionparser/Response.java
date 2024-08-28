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

import java.util.Optional;

import com.google.protobuf.GeneratedMessageV3.Builder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op1SourceArg;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Value;
import io.envoyproxy.envoy.extensions.filters.http.lua.v3.Lua;

public class Response implements Expression
{
    public enum Type implements Expression
    {
        HEADER("header",
               Lua.newBuilder()),
        BODY("body",
             Lua.newBuilder());

        String property;
        Builder<?> builder;

        private Type(String property,
                     Builder<?> builder)
        {
            this.property = property;
            this.builder = builder;
        }

        @Override
        public Builder<?> construct()
        {
            return null;
        }

        @Override
        public String toString()
        {
            return this.property;
        }

    }

    private final Type type;
    private final Optional<String> key;

    public Response(Type type,
                    Optional<String> key)
    {
        this.type = type;
        this.key = key;
    }

    public Builder<?> construct()
    {
        // Different Builder based on Optional
        if (key.isPresent())
            return Value.newBuilder().setTermRespheader(key.get());

        if (type.equals(Type.BODY))
            return Op1SourceArg.newBuilder().setResponseBody(true);

        return Value.newBuilder();
    }

    @Override
    public String toString()
    {
        if (key.isPresent())
            return String.format("term_respheader(%s)", this.key.get());

        if (type.equals(Type.BODY))
            return String.format("response_body:%s", true);

        return "";
    }
}
