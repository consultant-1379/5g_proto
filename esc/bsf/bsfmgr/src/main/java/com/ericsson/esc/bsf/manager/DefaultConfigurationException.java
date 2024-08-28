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
 * Created on: May 28, 2020
 *     Author: eaoknkr
 */

package com.ericsson.esc.bsf.manager;

// TODO: move this to common lib
/**
 * 
 */
public class DefaultConfigurationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public DefaultConfigurationException(String message)
    {
        super(message);
    }

    public DefaultConfigurationException(String message,
                                         Throwable cause)
    {
        super(message, cause);
    }
}
