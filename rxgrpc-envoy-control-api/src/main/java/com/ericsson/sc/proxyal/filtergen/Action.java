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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 */
public class Action
{
    enum Handle
    {
        Request,
        Response
    }

    protected List<Variable> vars;

    private String luaCode;
    private String indentedLuaCode;

    static Handle handle = Handle.Request;

    static final int INDENT_MIN = 0;
    static final int INDENT_STEP = 4;

    private static int indent = INDENT_MIN;

    static void setHandle(Handle handle)
    {
        Action.handle = handle;
    }

    protected String getLuaHandle()
    {
        if (handle == Handle.Request)
            return "request_handle:";
        else
            return "response_handle:";
    }

    protected void setLuaCode(String luaCode)
    {
        this.luaCode = luaCode;
        this.indentedLuaCode = (" ").repeat(indent * INDENT_STEP) + luaCode;
    }

    /**
     * @return the luaCode
     */
    public String getLuaCode()
    {
        return this.luaCode;
    }

    /**
     * @return the luaCode
     */
    public String getIndentedLuaCode()
    {
        return this.indentedLuaCode;
    }

    List<Variable> getVars()
    {
        return this.vars;
    }

    String getVarsAsCsv()
    {
        var varNames = this.vars.stream().map(Variable::getName).collect(Collectors.toList());
        return String.join(", ", varNames);
    }

    protected static void incIndent()
    {
        indent++;
    }

    protected static void decIndent()
    {
        if (indent > INDENT_MIN)
            indent--;
    }

}
