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
 * Created on: Jan 4, 2022
 *     Author: eaoknkr
 */

package com.ericsson.supreme.exceptions;

/**
 * 
 */
public class NetconfClientException extends Exception
{
    /**
     * Using default version uid
     */
    private static final long serialVersionUID = 1L;

    public NetconfClientException(String msg)
    {
        super(msg);
    }

    public NetconfClientException(String msg,
                                  Throwable e)
    {
        super(msg, e);
    }
}
