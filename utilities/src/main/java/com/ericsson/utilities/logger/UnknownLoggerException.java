/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 29, 2022
 *     Author: ekoteva
 */

package com.ericsson.utilities.logger;

/**
 * Exception to indicate that there was a failure to extract logger with
 * specific name
 */
public class UnknownLoggerException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public UnknownLoggerException(String message)
    {
        super(message);
    }

    public UnknownLoggerException(String message,
                                  Throwable cause)
    {
        super(message, cause);
    }

    public UnknownLoggerException(Throwable cause)
    {
        super(cause);
    }
}
