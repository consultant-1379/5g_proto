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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.ericsson.sc.sepp.manager.TphExpressionLexer;
import com.ericsson.sc.sepp.manager.TphExpressionParser;
import com.ericsson.sc.sepp.manager.TphExpressionParserBaseVisitor;

/**
 * 
 */
public class TphParserValidator extends TphExpressionParserBaseVisitor<OperandType>
{

    public static void validate(String text)
    {
        /*
         * if (text.isBlank()) { return; }
         */
        var lexer = new TphExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new TphExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConditionParserError());
        new TphParserValidator().visit(parser.root());
    }
}
