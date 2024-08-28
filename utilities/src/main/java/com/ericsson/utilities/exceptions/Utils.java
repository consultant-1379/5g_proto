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
 * Created on: Jul 31, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.exceptions;

/**
 * Utilities to ease dealing with exceptions.
 */
public final class Utils
{
    /**
     * Converts a Throwable to String. <br>
     * Alias for calling
     * <code>toString(t, verbose ? null : 4, verbose, "com.ericsson")</code>.
     * 
     * @param t       The Throwable to be converted to String.
     * @param verbose If true, output has all stack-frames, otherwise only one.
     * @return The String representation of the exception.
     */
    public static String toString(final Throwable t,
                                  final boolean verbose)
    {
        return toString(t, verbose ? null : 4, verbose, "com.ericsson");
    }

    /**
     * Converts a Throwable to String.
     * 
     * @param t      The Throwable to be converted to String.
     * @param depth  Maximum number of stack-frames to be considered. If null, all
     *               stack-frames are considered.
     * @param pretty If true, stack-frame separator is <code>"\n\tat "</code>,
     *               otherwise <code>" <- "</code>.
     * @param prefix If != null, stack-frames are considered until and including the
     *               first frame starting with <code>prefix</code>, but at least
     *               <code>depth</code> stack-frames.
     * @return The String representation of the exception.
     */
    public static String toString(final Throwable t,
                                  final Integer depth,
                                  final boolean pretty,
                                  final String prefix)
    {
        final StringBuilder b = new StringBuilder();
        final StackTraceElement[] stackTrace = t.getStackTrace();

        b.append(t.toString());

        final String separator = pretty ? "\n\tat " : " <- ";

        if (prefix != null)
        {
            boolean found = false;

            for (int i = 0; (depth == null || i < depth || !found) && i < stackTrace.length; i++)
            {
                final StackTraceElement element = stackTrace[i];

                if (!found)
                    found = element.getClassName().startsWith(prefix);

                b.append(separator).append(element.toString());
            }
        }
        else
        {
            for (int i = 0; (depth == null || i < depth) && i < stackTrace.length; i++)
                b.append(separator).append(stackTrace[i].toString());
        }

        return b.toString();
    }

    private Utils()
    {
    }
}
