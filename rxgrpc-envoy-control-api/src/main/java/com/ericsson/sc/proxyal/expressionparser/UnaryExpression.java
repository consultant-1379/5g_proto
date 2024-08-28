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

import com.google.protobuf.GeneratedMessageV3.Builder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op1ConditionArg;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op1SourceArg;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Op1ValueArg;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Condition;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Value;

/**
 * 
 */
public class UnaryExpression implements Expression
{
    final Expression operand;
    final Operator operator;

    public UnaryExpression(Expression operand,
                           Operator operator)
    {
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public Builder<?> construct()
    {
        switch (operator)
        {
            case EXISTS:
                return Condition.newBuilder().setOpExists(Op1ValueArg.newBuilder().setArg1((Value.Builder) operand.construct()));
            case ISEMPTY:
                return Condition.newBuilder().setOpIsempty(Op1ValueArg.newBuilder().setArg1((Value.Builder) operand.construct()));
            case NOT:
                return Condition.newBuilder().setOpNot(Op1ConditionArg.newBuilder().setArg1((Condition.Builder) operand.construct()));
            case ISVALIDJSON:
                return Condition.newBuilder().setOpIsvalidjson((Op1SourceArg.Builder) operand.construct());
            default:
                return Condition.newBuilder();

        }
    }

    @Override
    public String toString()
    {
        return String.format("op_%s(%s)", this.operator.toString(), this.operand.toString());
    }

}
