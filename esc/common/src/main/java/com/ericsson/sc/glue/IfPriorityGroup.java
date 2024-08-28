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
 * Created on: Mar 30, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfPriorityGroup extends IfNamedListItem
{
    Integer getPriority();

    List<String> getStaticScpInstanceDataRef();

    List<String> getStaticSeppInstanceDataRef();

    String getNfMatchCondition();

    String getScpMatchCondition();

    String getSeppMatchCondition();

}
