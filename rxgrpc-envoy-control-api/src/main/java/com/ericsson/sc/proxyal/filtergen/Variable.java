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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class Variable
{

    private String name;

    private Variable(String name)
    {
        this.name = name;
    }

    static Variable var(String varName)
    {
        return new Variable(varName);
    }

    static List<Variable> vars(String varNames)
    {

        List<Variable> vars = new ArrayList<>();
        for (String name : Arrays.asList(varNames.split(",")))
            vars.add(new Variable(name.trim()));
        return vars;
    }

    public String getName()
    {
        return this.name;
    }

}
