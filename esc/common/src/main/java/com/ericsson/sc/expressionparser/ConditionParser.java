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
 * Created on: Nov 30, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.expressionparser;

import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.ericsson.sc.proxyal.expressionparser.BinaryExpression;
import com.ericsson.sc.proxyal.expressionparser.Expression;
import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.proxyal.expressionparser.Request;
import com.ericsson.sc.proxyal.expressionparser.Response;
import com.ericsson.sc.proxyal.expressionparser.SimpleTerm;
import com.ericsson.sc.proxyal.expressionparser.UnaryExpression;
import com.ericsson.sc.proxyal.expressionparser.Variable;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.sc.sepp.manager.Condition;
import com.ericsson.sc.sepp.manager.ConditionBaseVisitor;

/**
 * 
 */
public class ConditionParser extends ConditionBaseVisitor<Expression>
{
    @Override
    public Expression visitRoot(Condition.RootContext ctx)
    {
        return visit(ctx.expression());
    }

    @Override
    public Expression visitReq(Condition.ReqContext ctx)
    {
        var reqType = Request.Type.valueOf(ctx.property.getText().toUpperCase());
        return new Request(reqType,
                           Optional.ofNullable(ctx.key_val()) //
                                   .map(keyVal -> keyVal.key.getText()//
                                                            .replace("'", "")));
    }

    @Override
    public Expression visitResp(Condition.RespContext ctx)
    {
        var respType = Response.Type.valueOf(ctx.property.getText().toUpperCase());
        return new Response(respType,
                            Optional.ofNullable(ctx.key_val()) //
                                    .map(keyVal -> keyVal.key.getText()//
                                                             .replace("'", "")));
    }

    @Override
    public Expression visitVar(Condition.VarContext ctx)
    {
        return new Variable(ctx.name.getText());
    }

//    @Override
//    public Expression visitTable(Condition.TableContext ctx)
//    {
//        if (!ctx.key_val().isEmpty())
//        {
//            return new Table<String>(ctx.name.getText(), ctx.key_val().STRING().getText());
//        }
//        else
//        {
//            return new Table<Integer>(ctx.name.getText(), Integer.parseInt(ctx.index().INTEGER().getText()));
//        }
//    }

//    @Override
//    public Expression visitLiteralInt(Condition.LiteralIntContext ctx)
//    {
//        return new SimpleTerm<Integer>(Integer.parseInt(ctx.INTEGER().getText()));
//    }

    @Override
    public Expression visitLiteralJsonNumber(Condition.LiteralJsonNumberContext ctx)
    {
        return new SimpleTerm<Double>(Double.parseDouble(ctx.NUMBER().getText()));
    }

    @Override
    public Expression visitLiteralStr(Condition.LiteralStrContext ctx)
    {
        return new SimpleTerm<String>(ctx.STRING().getText().replace("'", ""));
    }

    @Override
    public Expression visitLiteralBool(Condition.LiteralBoolContext ctx)
    {
        return new SimpleTerm<Boolean>(Boolean.valueOf(ctx.BOOLEAN().getText().toLowerCase()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression visitBinaryExpr(Condition.BinaryExprContext ctx)
    {
        var operator = Operator.fromString(ctx.op.getText());
        var rightExpression = this.visit(ctx.right);
        var boolValue = rightExpression.toString().contains("term_boolean");

        if (boolValue && (operator.equals(Operator.EQUAL) || operator.equals(Operator.CASEINSENSITIVEQUAL)))
        {
            rightExpression = new SimpleTerm<Boolean>((SimpleTerm<Boolean>) rightExpression, true);
        }
        return new BinaryExpression(this.visit(ctx.left), rightExpression, operator);
    }

    @Override
    public Expression visitParentheticExpr(Condition.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public Expression visitUnaryExpr(Condition.UnaryExprContext ctx)
    {
        var operator = Operator.valueOf(ctx.op.getText().toUpperCase());
        return new UnaryExpression(this.visit(ctx.arg), operator);
    }

    public static Expression parse(String text)
    {
        if (text.isBlank())
        {
            return new SimpleTerm<Boolean>(true);
        }
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new Condition(tokens);

        return new ConditionParser().visit(parser.root());
    }
}
