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
 * Created on: Mar 1, 2022
 *     Author: eodnouk
 */

package com.ericsson.sc.glue;

import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfGlobalRateLimitProfile extends IfNamedListItem
{
    String getName();

    Integer getSustainableRate();

    Integer getMaxBurstSize();

}
