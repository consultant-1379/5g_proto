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
 * Created on: Aug 1, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.exceptions;

public class BadConfigurationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private static String expand(final String msg,
                                 final Object... args)
    {
        final String PATTERN = "{}";
        final StringBuilder b = new StringBuilder();

        for (int i = 0, start = 0;;)
        {
            int stop = msg.indexOf(PATTERN, start);

            if (stop >= 0)
            {
                b.append(msg.substring(start, stop));

                if (i < args.length)
                    b.append(args[i++]);
                else
                    b.append(PATTERN);

                start = stop + PATTERN.length();
            }
            else
            {
                b.append(msg.substring(start));
                break;
            }
        }

        return b.toString();
    }

    /**
     * Create a new BadConfigurationException.
     * 
     * @param errorMsg     The message for the exception. Format is like for the
     *                     logger.
     * @param errorMsgArgs The arguments for the error-message.
     */
    public BadConfigurationException(final String errorMsg,
                                     final Object... errorMsgArgs)
    {
        super(expand(errorMsg, errorMsgArgs));
    }

    /**
     * Create a new BadConfigurationException.
     * 
     * @param cause        The original cause of the error.
     * @param errorMsg     The message for the exception. Format is like for the
     *                     logger.
     * @param errorMsgArgs The arguments for the error-message.
     */
    public BadConfigurationException(final Throwable cause,
                                     final String errorMsg,
                                     final Object... errorMsgArgs)
    {
        super(expand(errorMsg, errorMsgArgs), cause);
    }
}
