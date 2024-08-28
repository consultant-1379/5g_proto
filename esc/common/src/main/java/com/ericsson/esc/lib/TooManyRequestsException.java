/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Okt 30, 2020
 *     Author: estoioa
 */
package com.ericsson.esc.lib;

public class TooManyRequestsException extends BadRequestException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TooManyRequestsException(String message)
    {
        super(message);
    }

    public TooManyRequestsException(String message,
                                    Throwable cause)
    {
        super(message, cause);
    }

}
