package com.ericsson.sc.expressionparser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.ericsson.utilities.exceptions.ParseException;

public class ConditionParserError extends BaseErrorListener
{
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {

        throw new ParseException(line, charPositionInLine, "Syntax error : " + msg);
    }
}
