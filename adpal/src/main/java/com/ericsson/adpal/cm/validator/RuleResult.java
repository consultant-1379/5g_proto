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
 * Created on: Oct 7, 2020
 *     Author: evouioa
 */

package com.ericsson.adpal.cm.validator;

/**
 * Holds the result of the validation process of an applied {@code Rule}
 */
public class RuleResult
{
    private final boolean result;
    private String errorMessage;

    public RuleResult(boolean result,
                      String errorMessage)
    {
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public boolean getResult()
    {
        return this.result;
    }

    public String getErrorMessage()
    {
        return this.errorMessage;
    }

}
