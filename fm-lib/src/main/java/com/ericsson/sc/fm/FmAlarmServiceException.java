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
 * Created on: Jul 22, 2022
 *     Author: ekoteva
 */

package com.ericsson.sc.fm;

/**
 * Exception to indicate that there was a failure to update fault after request
 * to Alarm Handler Service
 */
public class FmAlarmServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public FmAlarmServiceException(String message)
    {
        super(message);
    }

    public FmAlarmServiceException(String message,
                                   Throwable cause)
    {
        super(message, cause);
    }

    public FmAlarmServiceException(Throwable cause)
    {
        super(cause);
    }

}
