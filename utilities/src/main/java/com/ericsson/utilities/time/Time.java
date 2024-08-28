/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 22, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.time;

import java.time.Instant;

/**
 * Time related functions
 */
public class Time
{
    public static final String nowInMillis()
    {
        return Instant.now().toString();
    }

    private Time()
    {
    }
}
