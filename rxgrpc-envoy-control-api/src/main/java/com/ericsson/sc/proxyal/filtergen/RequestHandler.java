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
 * Created on: May 19, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.filtergen;

import static com.ericsson.sc.proxyal.filtergen.StringAction.stringFind;
import static com.ericsson.sc.proxyal.filtergen.Variable.var;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.proxyal.filtergen.Action.Handle;
import com.ericsson.sc.proxyal.filtergen.LogAction.LogLevel;

public class RequestHandler
{

    private String name;
    private List<? super Action> actions = new ArrayList<>();

    public RequestHandler()
    {
        Action.setHandle(Handle.Request);
    }

    public void name(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public RequestHandler add(Action action)
    {
        this.actions.add(action);
        return this;
    }

    public List<? super Action> getActions()
    {
        return actions;
    }

    public RequestHandler log(LogLevel logLevel,
                              final String msg)
    {
        this.actions.add(LogAction.log(logLevel, msg));
        return this;
    }

    public RequestHandler log(LogLevel logLevel,
                              StringAction action)
    {
        this.actions.add(LogAction.log(logLevel, action));
        return this;
    }

    public RequestHandler then()
    {
        this.actions.add(ControlAction.then());
        return this;
    }

    public RequestHandler end()
    {
        this.actions.add(ControlAction.end());
        return this;
    }

//    public RequestHandler when(Variable var)
//    {
//       this.actions.add(ConditionAction.when(var));
//       return this;                                     
//    } 

    /**
     * add a condition
     */
    public RequestHandler cond(Action act)
    {
        this.actions.add(act);
        return this;
    }

    public RequestHandler set(List<Variable> vars,
                              StringAction act)
    {
        this.actions.add(ControlAction.set(vars, act));
        return this;
    }

    public RequestHandler set(Variable var,
                              HttpHeaderAction act)
    {
        this.actions.add(ControlAction.set(var, act));
        return this;
    }

    public RequestHandler set(Variable var)
    {
        this.actions.add(ControlAction.set(var));
        return this;
    }

    public RequestHandler set(ControlAction act)
    {
        this.actions.add(act);
        return this;
    }

    public RequestHandler ifPathContains(String pattern)
    {
        this.actions.add(ConditionAction.when(stringFind(var("path"), pattern)));
        this.actions.add(ControlAction.then());
        return this;

    }

    public RequestHandler addHttpHeader(final String header,
                                        final String headerValue)
    {
        this.actions.add(HttpHeaderAction.addHttpHeader(header, headerValue));
        return this;
    }

    public RequestHandler removeHttpHeader(final String header,
                                           final String headerValue)
    {
        this.actions.add(HttpHeaderAction.removeHttpHeader(header));
        return this;
    }

}
