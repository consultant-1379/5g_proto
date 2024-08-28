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

package com.ericsson.sc.proxyal.expressionparser;

/**
 * 
 */
public enum Operator
{
    EQUAL("=="),
    CASEINSENSITIVEQUAL("="),
    EXISTS("exists"),
    ISEMPTY("isempty"),
    AND("and"),
    OR("or"),
    ISINSUBNET("isinsubnet"),
    NOT("not"),
    ISVALIDJSON("isvalidjson");

    String sign;

    Operator(String sign)
    {
        this.sign = sign;
    }

    public static Operator fromString(String input)
    {
        for (var op : Operator.values())
        {
            if (op.sign.equalsIgnoreCase(input))
            {
                return op;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }
}
