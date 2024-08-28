/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: March 10, 2021
 *     Author: eevagal
 */
package com.ericsson.esc.lib;

public class DbMissingPermissionsException extends BadRequestException
{

    private static final long serialVersionUID = 1L;

    public DbMissingPermissionsException(String message)
    {
        super(message);
    }

    public DbMissingPermissionsException(String message,
                                         Throwable cause)
    {
        super(message, cause);
    }

}
