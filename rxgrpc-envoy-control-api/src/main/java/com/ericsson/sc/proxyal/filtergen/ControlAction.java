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
 * Created on: May 20, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.filtergen;

import java.util.List;

/**
 * 
 */
public class ControlAction extends Action
{

    private ControlAction()
    {
    }

    static ControlAction set(Variable var)
    {
        var action = new ControlAction();
        action.setLuaCode(new StringBuilder().append("local ").append(var.getName()).append(" = ").toString());
        return action;
    }

    static ControlAction set(Variable var,
                             String value)
    {
        var action = new ControlAction();
        action.setLuaCode(new StringBuilder().append("local ").append(var.getName()).append(" = ").append("\"").append(value).append("\"").toString());
        return action;
    }

    static ControlAction set(List<Variable> vars,
                             StringAction strAction)
    {
        var action = new ControlAction();
        action.vars = vars;
        action.setLuaCode(new StringBuilder().append("local ").append(action.getVarsAsCsv()).append(" = ").append(strAction.getLuaCode()).toString());
        return action;
    }

    static ControlAction set(Variable var,
                             HttpHeaderAction headerAction)
    {
        var action = new ControlAction();
        action.setLuaCode(new StringBuilder().append("local ").append(var.getName()).append(" = ").append(headerAction.getLuaCode()).toString());
        return action;
    }

    static ControlAction elseDo()
    {
        var action = new ControlAction();
        decIndent();
        action.setLuaCode("else");
        incIndent();
        return action;
    }

    static ControlAction then()
    {
        var action = new ControlAction();
        action.setLuaCode("then");
        return action;
    }

    static ControlAction end()
    {
        var action = new ControlAction();
        decIndent();
        action.setLuaCode("end");
        return action;
    }

}
