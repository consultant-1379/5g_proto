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
 * Created on: Oct 27, 2021
 *     Author: echaias
 */

package com.ericsson.sc.glue;

/**
 * 
 */
public interface IfTokenBucket
{
    Integer getMaxTokens();

    Integer getTokensPerFill();

    Integer getFillInterval();
}
