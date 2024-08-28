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

import java.util.Map;

/**
 * 
 */
public class LogAction extends Action
{

    public enum LogLevel
    {
        INFO,
        DEBUG,
        TRACE,
        WARN,
        ERROR
    }

    private static final Map<LogLevel, String> luaLogPrefix = Map.of(LogLevel.INFO,
                                                                     "logInfo",
                                                                     LogLevel.DEBUG,
                                                                     "logDebug",
                                                                     LogLevel.TRACE,
                                                                     "logTrace",
                                                                     LogLevel.WARN,
                                                                     "logWarn",
                                                                     LogLevel.ERROR,
                                                                     "logError");

    private LogLevel logLevel;
    private String logMsg;

    private StringAction strAct;

    private LogAction()
    {
    }

    static LogAction log(LogLevel logLevel,
                         String msg)
    {
        var action = new LogAction();
        action.logMsg = msg;
        action.logLevel = logLevel;

        action.setLuaCode(new StringBuilder().append(action.getLuaHandle())
                                             .append(luaLogPrefix.get(logLevel))
                                             .append("(\"")
                                             .append(action.logMsg)
                                             .append("\")")
                                             .toString());
        return action;
    }

    /**
     * @param logLevel2
     * @param action
     * @return
     */
    public static LogAction log(LogLevel logLevel,
                                StringAction strAction)
    {
        var action = new LogAction();
        action.strAct = strAction;
        action.logLevel = logLevel;

        action.setLuaCode(new StringBuilder().append(action.getLuaHandle())
                                             .append(luaLogPrefix.get(logLevel))
                                             .append("(")
                                             .append(strAction.getLuaCode())
                                             .append(")")
                                             .toString());

        return action;
    }

}
