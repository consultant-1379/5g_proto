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

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.sepp.manager.Condition;
import com.ericsson.sc.sepp.manager.ConditionBaseVisitor;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.utilities.exceptions.ParseException;

/**
 * 
 */
public class ConditionParserValidator extends ConditionBaseVisitor<OperandType>
{

    @Override
    public OperandType visitRoot(Condition.RootContext ctx)
    {
        var type = visit(ctx.expression());
        if (type != OperandType.BOOL)
        {
            throw new ParseException(0,
                                     0,
                                     String.format("Condition should always return %s. Instead it returned %s", OperandType.BOOL.toString(), type.toString()));
        }

        return OperandType.BOOL;
    }

    @Override
    public OperandType visitReq(Condition.ReqContext ctx)
    {
        return OperandType.DATA;
    }

    @Override
    public OperandType visitResp(Condition.RespContext ctx)
    {
        return OperandType.DATA;
    }

    @Override
    public OperandType visitVar(Condition.VarContext ctx)
    {
        return OperandType.DATA;
    }

//    @Override
//    public Type visitTable(Condition.TableContext ctx)
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
//    public Type visitLiteralInt(Condition.LiteralIntContext ctx)
//    {
//        return Type.INTEGER;
//    }

    @Override
    public OperandType visitLiteralJsonNumber(Condition.LiteralJsonNumberContext ctx)
    {
        var jsonNumber = Double.parseDouble(ctx.NUMBER().getText());

        if (jsonNumber == Double.POSITIVE_INFINITY || jsonNumber == Double.NEGATIVE_INFINITY || jsonNumber == Double.NaN)
        {
            throw new ParseException(0,
                                     0,
                                     String.format("Condition should always have numbers that can be stored with double precision. Numbers must be within [%s %s]",
                                                   -Double.MAX_VALUE,
                                                   Double.MAX_VALUE));
        }

        return OperandType.NUMBER;
    }

    @Override
    public OperandType visitLiteralStr(Condition.LiteralStrContext ctx)
    {
        return OperandType.STRING;
    }

    @Override
    public OperandType visitLiteralBool(Condition.LiteralBoolContext ctx)
    {
        return OperandType.BOOL;
    }

    @Override
    public OperandType visitBinaryExpr(Condition.BinaryExprContext ctx)
    {
        var operator = Operator.fromString(ctx.op.getText());
        var leftType = visit(ctx.left);
        var rightType = visit(ctx.right);
        switch (operator)
        {
            case OR:
            case AND:
                if (leftType != OperandType.BOOL)
                {
                    throw new ParseException(ctx.left.start.getLine(),
                                             ctx.left.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                if (rightType != OperandType.BOOL)
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                break;
            case EQUAL:
                break;
            case CASEINSENSITIVEQUAL:
                break;
            case ISINSUBNET:
                if (leftType != OperandType.DATA)
                {
                    throw new ParseException(ctx.left.start.getLine(),
                                             ctx.left.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.DATA.toString()));
                }
                if (rightType != OperandType.STRING)
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.STRING.toString()));
                }
                break;
            default:
                break;
        }
        return OperandType.BOOL;
    }

    @Override
    public OperandType visitParentheticExpr(Condition.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public OperandType visitUnaryExpr(Condition.UnaryExprContext ctx)
    {
        var operator = Operator.valueOf(ctx.op.getText().toUpperCase());
        var argType = visit(ctx.arg);
        switch (operator)
        {
            case EXISTS:
                if (argType != OperandType.DATA)
                {
                    throw new ParseException(ctx.arg.start.getLine(),
                                             ctx.arg.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             argType.toString(),
                                             List.of(OperandType.DATA.toString()));
                }
                break;
            case ISEMPTY:
                if (argType != OperandType.DATA)
                {
                    throw new ParseException(ctx.arg.start.getLine(),
                                             ctx.arg.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             argType.toString(),
                                             List.of(OperandType.DATA.toString()));
                }
                break;
            case NOT:
                if (argType != OperandType.BOOL)
                {
                    throw new ParseException(ctx.arg.start.getLine(),
                                             ctx.arg.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             argType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                break;
            case ISVALIDJSON:
                if (argType != OperandType.DATA)
                {
                    throw new ParseException(ctx.arg.start.getLine(),
                                             ctx.arg.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             argType.toString(),
                                             List.of(OperandType.DATA.toString()));
                }
                break;
            default:
                break;
        }
        return OperandType.BOOL;
    }

    public static void validate(String text)
    {
        if (text.isBlank())
        {
            return;
        }
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new Condition(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConditionParserError());
        new ConditionParserValidator().visit(parser.root());

    }
}
