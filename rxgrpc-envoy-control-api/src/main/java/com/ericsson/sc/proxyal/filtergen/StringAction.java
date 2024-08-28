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
 * Created on: May 22, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.filtergen;

import java.util.List;

/**
 * 
 */
public class StringAction extends Action
{

    private StringAction()
    {
    }

    static StringAction stringFormat(String fmtString,
                                     List<Variable> vars)
    {
        var action = new StringAction();

        action.vars = vars;
        action.setLuaCode(new StringBuilder().append("string.format(\"")
                                             .append(fmtString)
                                             .append("\"), ")
                                             .append(action.getVarsAsCsv())
                                             .append(")")
                                             .toString());
        return action;
    }

    static StringAction stringFind(Variable var,
                                   String searchString)
    {
        var action = new StringAction();
        action.setLuaCode(new StringBuilder().append("string.find(")
                                             .append(var.getName())
                                             .append(", ")
                                             .append("\'")
                                             .append(searchString)
                                             .append("\'")
                                             .append(")")
                                             .toString());
        return action;
    }

    static StringAction fromMatch(Variable var,
                                  String searchString)
    {
        var action = new StringAction();
        action.setLuaCode(new StringBuilder().append("string.match(")
                                             .append(var.getName())
                                             .append(", ")
                                             .append("\'")
                                             .append(searchString)
                                             .append("\'")
                                             .append(")")
                                             .toString());
        return action;
    }

}
