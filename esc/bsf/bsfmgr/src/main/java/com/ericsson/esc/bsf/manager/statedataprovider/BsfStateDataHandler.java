/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 1, 2024
 *     Author: znpvaap
 */

package com.ericsson.esc.bsf.manager.statedataprovider;

import com.ericsson.adpal.cm.state.StateDataHandler;

/**
 * The BSF instance of StateDataHandler with the approriate first part of the
 * path. Every state data handler that will be used for State data in BSF shall
 * implement this Handler
 */
public interface BsfStateDataHandler extends StateDataHandler
{
    public default String handlerPath()
    {
        return "/ericsson-bsf:bsf-function/nf-instance/";
    }
}
