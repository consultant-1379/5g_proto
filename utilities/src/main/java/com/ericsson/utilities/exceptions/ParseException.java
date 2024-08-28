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
 * Created on: Dec 3, 2020
 *     Author: echaias
 */

package com.ericsson.utilities.exceptions;

import java.util.List;

/**
 * 
 */
public class ParseException extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    public final int charPos;
    public final int line;

    public ParseException(int line,
                          int charPos,
                          String operator,
                          String givenType,
                          List<String> expectedTypes)
    {
        super(String.format("Type missmatch error for operator %s. Expected %s, got %s", operator, expectedTypes.toString(), givenType));
        this.charPos = charPos;
        this.line = line;

    }

    public ParseException(int line,
                          int charPos,
                          String msg)
    {
        super(msg);
        this.charPos = charPos;
        this.line = line;

    }

}
