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
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.expressionparser;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3.Builder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Condition;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op2AnyArgs;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op2ConditionArgs;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.OpValueStringArgs;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Value;

/**
 * 
 */
public class BinaryExpression implements Expression
{
    private final Expression left;
    private final Expression right;
    private final Operator operator;

    public BinaryExpression(Expression left,
                            Expression right,
                            Operator operator)
    {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public Builder<?> construct()
    {

        switch (operator)
        {
            case OR:
                return Condition.newBuilder()
                                .setOpOr(Op2ConditionArgs.newBuilder()
                                                         .setArg1((Condition.Builder) left.construct())
                                                         .setArg2((Condition.Builder) right.construct()));
            case AND:
                return Condition.newBuilder()
                                .setOpAnd(Op2ConditionArgs.newBuilder()
                                                          .setArg1((Condition.Builder) left.construct())
                                                          .setArg2((Condition.Builder) right.construct()));
            case EQUAL:
                return Condition.newBuilder()
                                .setOpEquals(Op2AnyArgs.newBuilder()
                                                       .setTypedConfig1(Any.pack(left.construct().build()))
                                                       .setTypedConfig2(Any.pack(right.construct().build())));
            case CASEINSENSITIVEQUAL:
                return Condition.newBuilder()
                                .setOpEqualsCaseInsensitive(Op2AnyArgs.newBuilder()
                                                                      .setTypedConfig1(Any.pack(left.construct().build()))
                                                                      .setTypedConfig2(Any.pack(right.construct().build())));
            case ISINSUBNET:
                return Condition.newBuilder()
                                .setOpIsinsubnet(OpValueStringArgs.newBuilder()
                                                                  .setArg1((Value.Builder) left.construct())
                                                                  .setArg2(((Value.Builder) right.construct()).build().getTermString()));
            default:
                return Condition.newBuilder();

        }
    }

    public String toString()
    {
        return String.format("op_%s(%s,%s)", this.operator.toString(), this.left.toString(), this.right.toString());
    }
}
