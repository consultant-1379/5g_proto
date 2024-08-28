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
 * Created on: Jan 20, 2021
 *     Author: enocakh
 */

package com.ericsson.sc.expressionparser;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.sepp.manager.NfMatchConditionParser;
import com.ericsson.sc.sepp.manager.NfMatchConditionParserBaseVisitor;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.utilities.exceptions.ParseException;

/**
 * 
 */
public class NfConditionParserValidator extends NfMatchConditionParserBaseVisitor<OperandType>
{

    @Override
    public OperandType visitRoot(NfMatchConditionParser.RootContext ctx)
    {
        var type = visit(ctx.expression());
        if (type != OperandType.BOOL)
        {
            throw new ParseException(0,
                                     0,
                                     String.format("NF match condition should always return %s. Instead it returned %s",
                                                   OperandType.BOOL.toString(),
                                                   type.toString()));
        }

        return OperandType.BOOL;
    }

    @Override
    public OperandType visitNfDataKnown(NfMatchConditionParser.NfDataKnownContext ctx)
    {
        return OperandType.NFDATA;
    }

    @Override
    public OperandType visitNfDataUnknown(NfMatchConditionParser.NfDataUnknownContext ctx)
    {
        return OperandType.NFDATA;
    }

    @Override
    public OperandType visitNfDataNf(NfMatchConditionParser.NfDataNfContext ctx)
    {
        return OperandType.NFDATA;
    }

    @Override
    public OperandType visitNfDataService(NfMatchConditionParser.NfDataServiceContext ctx)
    {
        return OperandType.NFDATA;
    }

    @Override
    public OperandType visitNfDataIpEndpoint(NfMatchConditionParser.NfDataIpEndpointContext ctx)
    {
        return OperandType.NFDATA;
    }

    @Override
    public OperandType visitVar(NfMatchConditionParser.VarContext ctx)
    {
        return OperandType.DATA;
    }

    @Override
    public OperandType visitLiteralStr(NfMatchConditionParser.LiteralStrContext ctx)
    {
        return OperandType.STRING;
    }

    @Override
    public OperandType visitLiteralBool(NfMatchConditionParser.LiteralBoolContext ctx)
    {
        return OperandType.BOOL;
    }

    @Override
    public OperandType visitBinaryExpr(NfMatchConditionParser.BinaryExprContext ctx)
    {

        /*
         * For NF match conditions, an EQUALITY binary expression must either contain
         * NFDATA and LITERAL (static constraint) OR NFATA and DATA (dynamic constraint)
         */
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
                // Check if the left operand is nfdata or string or bool
                if ((leftType != OperandType.NFDATA) && (leftType != OperandType.STRING) && (leftType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.NFDATA.toString(), OperandType.BOOL.toString(), OperandType.STRING.toString()));
                }
                // Check if the right operand is nfdata or string or bool
                if ((rightType != OperandType.NFDATA) && (rightType != OperandType.STRING) && (rightType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.NFDATA.toString(), OperandType.BOOL.toString(), OperandType.STRING.toString()));
                }
                if ((leftType == OperandType.BOOL) && (rightType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                if ((rightType == OperandType.BOOL) && (leftType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                if ((leftType == OperandType.NFDATA) && (rightType != OperandType.STRING))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.STRING.toString()));
                }
                if ((rightType == OperandType.NFDATA) && (leftType != OperandType.STRING))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.STRING.toString()));
                }
                if ((leftType == OperandType.STRING) && (rightType != OperandType.NFDATA))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.NFDATA.toString()));
                }
                if ((rightType == OperandType.STRING) && (leftType != OperandType.NFDATA))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.NFDATA.toString()));
                }

                break;
            case CASEINSENSITIVEQUAL:
                // Check if the left operand is nfdata or string or bool
                if ((leftType != OperandType.NFDATA) && (leftType != OperandType.STRING) && (leftType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.NFDATA.toString(), OperandType.BOOL.toString(), OperandType.STRING.toString()));
                }
                // Check if the right operand is nfdata or string or bool
                if ((rightType != OperandType.NFDATA) && (rightType != OperandType.STRING) && (rightType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.NFDATA.toString(), OperandType.BOOL.toString(), OperandType.STRING.toString()));
                }
                if ((leftType == OperandType.BOOL) && (rightType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                if ((rightType == OperandType.BOOL) && (leftType != OperandType.BOOL))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.BOOL.toString()));
                }
                if ((leftType == OperandType.NFDATA) && (rightType != OperandType.STRING))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.STRING.toString()));
                }
                if ((rightType == OperandType.NFDATA) && (leftType != OperandType.STRING))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.STRING.toString()));
                }
                if ((leftType == OperandType.STRING) && (rightType != OperandType.NFDATA))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             rightType.toString(),
                                             List.of(OperandType.NFDATA.toString()));
                }
                if ((rightType == OperandType.STRING) && (leftType != OperandType.NFDATA))
                {
                    throw new ParseException(ctx.right.start.getLine(),
                                             ctx.right.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             leftType.toString(),
                                             List.of(OperandType.NFDATA.toString()));
                }

                break;
            default:
                break;
        }
        return OperandType.BOOL;
    }

    @Override
    public OperandType visitParentheticExpr(NfMatchConditionParser.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public OperandType visitUnaryExpr(NfMatchConditionParser.UnaryExprContext ctx)
    {
        var operator = Operator.valueOf(ctx.op.getText().toUpperCase());
        var argType = visit(ctx.arg);
        switch (operator)
        {
            case EXISTS:
                if (argType != OperandType.NFDATA)
                {
                    throw new ParseException(ctx.arg.start.getLine(),
                                             ctx.arg.start.getCharPositionInLine(),
                                             ctx.op.getText(),
                                             argType.toString(),
                                             List.of(OperandType.NFDATA.toString()));
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
        var parser = new NfMatchConditionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConditionParserError());
        new NfConditionParserValidator().visit(parser.root());

    }
}
