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
 * Created on: Nov 17, 2020
 *     Author: echfari
 */
package com.ericsson.adpal.cm.actions;

/**
 * A YANG action specification
 */
public class ActionSpec
{

    private final ActionHandler handler;
    private final String actionId;

    public ActionSpec(ActionHandler handler,
                      String actionId)
    {
        this.handler = handler;
        this.actionId = actionId;
    }

    public ActionHandler getActionHandler()
    {
        return handler;
    }

    public String getActionId()
    {
        return actionId;
    }
}
