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
 * Created on: Jun 01, 2020
 *     Author: xekoteva
 */
package com.ericsson.esc.lib;

public class FaultyDbSchemaException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public FaultyDbSchemaException(String errorMessage,
                                   Throwable error)
    {
        super(errorMessage, error);
    }

    public FaultyDbSchemaException(String errorMessage)
    {
        super(errorMessage);
    }

}
