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
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.esc.lib;

public class MalformedMessageException extends BadRequestException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MalformedMessageException(String message)
    {
        super(message);
    }

    public MalformedMessageException(String message,
                                     Throwable cause)
    {
        super(message, cause);
    }

}
