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
 * Created on: Jan 10, 2022
 *     Author: eaoknkr
 */

package com.ericsson.supreme.exceptions;

/**
 * 
 */
public class KubernetesClientException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public KubernetesClientException(String msg)
    {
        super(msg);
    }

    public KubernetesClientException(String msg,
                                     Throwable e)
    {
        super(msg, e);
    }
}
