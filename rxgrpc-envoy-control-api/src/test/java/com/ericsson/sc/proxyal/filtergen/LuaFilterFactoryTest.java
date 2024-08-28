package com.ericsson.sc.proxyal.filtergen;

import static com.ericsson.sc.proxyal.filtergen.ConditionAction.*;
import static com.ericsson.sc.proxyal.filtergen.HttpHeaderAction.*;
import static com.ericsson.sc.proxyal.filtergen.StringAction.*;
import static com.ericsson.sc.proxyal.filtergen.Variable.var;
import static com.ericsson.sc.proxyal.filtergen.Variable.vars;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.filtergen.Condition;
import com.ericsson.sc.proxyal.filtergen.LuaFilterFactory;
import com.ericsson.sc.proxyal.filtergen.RequestHandler;
import com.ericsson.sc.proxyal.filtergen.LogAction.LogLevel;
import com.ericsson.sc.proxyal.service.FilterFactory;

class LuaFilterFactoryTest
{
    private static final Logger log = LoggerFactory.getLogger(LuaFilterFactoryTest.class);

    @Disabled
    @Test
    void testGetCsaLuaFilter()
    {
        Map<String, Map<String, String>> contextData = new HashMap<String, Map<String, String>>();
        contextData.put("common",
                        Map.of("ownIp",
                               "ALEXcsa.ericsson.se", //
                               "ownPort",
                               "ALEX80",
                               "ownTlsPort",
                               "TEST443"));
        contextData.put("convergedCharging",
                        Map.of("apiRoot",
                               "ALEX",
                               "apiName",
                               "ALEXnchf%-convergedcharging",
                               "apiVersion",
                               "ALEXv1",
                               "apiSRUP",
                               "ALEXchargingdata",
                               "labelSelector",
                               "regex([^-]+)"));
        contextData.put("spendingLimitControl",
                        Map.of("apiRoot", "ANDER", "apiName", "ANDERnchf%-spendinglimitcontrol", "apiVersion", "ANDERv1", "apiSRUP", "ANDERsubscriptions"));

        log.info(FilterFactory.getCsaLuaFilter(contextData));
    }

    @Test
    void testGetCsaLuaFilterMod()
    {
        RequestHandler ccReqHandler = new RequestHandler();
        ccReqHandler.name("convergedCharging");
        ccReqHandler.log(LogLevel.DEBUG, "logMessage 1 by " + ccReqHandler.getName())
                    .log(LogLevel.INFO, "logMessage 2 by " + ccReqHandler.getName())
                    // we do this because ...
                    .cond(when(var("chargingRef")))
                    .log(LogLevel.INFO, "logMessage 3 by " + ccReqHandler.getName())
                    .set(var("var"), fromHttpHeader("x-notify-uri"))
                    .end()
                    .cond(when(var("varname"), isEqualTo("testVal")).and(var("varname1"), Condition.EQUALS, "testVal1")
                                                                    .or(var("varname3"), Condition.EQUALS, "testVal3")
                                                                    .and(var("varname4"), isNotEqualTo("testVal4")))
                    .then()
                    .log(LogLevel.DEBUG, "after long cond")
                    .end()
                    .ifPathContains("nchf%-convergedcharging/v1/chargingdata")
                    .log(LogLevel.WARN, "Converged Charging")
                    .end()
                    .set(vars("a, b"), fromMatch(var("path"), "nchf%-convergedcharging/v1/chargingdata/([^-]+)-[^/]+/(.+)$"))
                    .addHttpHeader("path:", "somevalue");

        RequestHandler regCcHandler = new RequestHandler();
        regCcHandler.name("regionalConvergedCharging");

        RequestHandler slcReqHandler = new RequestHandler();
        slcReqHandler.name("spendingLimit");

        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(ccReqHandler);
        // handlers.add(regCcHandler);
        // handlers.add(slcReqHandler);

        log.info(LuaFilterFactory.getCsaLuaFilter(handlers));
    }

    @Test
    void testGetCsaLuaFilterMod_LogActions_simple()
    {
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.name("UT  Handler");
        reqHandler.log(LogLevel.INFO, "UTlogMessage")
                  .log(LogLevel.DEBUG, "UTlogMessage")
                  .log(LogLevel.TRACE, "UTlogMessage")
                  .log(LogLevel.WARN, "UTlogMessage")
                  .log(LogLevel.ERROR, "UTlogMessage");

        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(reqHandler);

        String lua = LuaFilterFactory.getCsaLuaFilter(handlers);
        String luaForHandler = extractLuaCodeForHandler(lua, reqHandler);

        var actualLines = extractLuaLines(luaForHandler);
        var expectedLines = new ArrayList<String>();

        expectedLines.add("request_handle:logInfo(\"UTlogMessage\")");
        expectedLines.add("request_handle:logDebug(\"UTlogMessage\")");
        expectedLines.add("request_handle:logTrace(\"UTlogMessage\")");
        expectedLines.add("request_handle:logWarn(\"UTlogMessage\")");
        expectedLines.add("request_handle:logError(\"UTlogMessage\")");

        assertLinesMatch(expectedLines, actualLines);

    }

    @Test
    void testGetCsaLuaFilterMod_LogActions_stringFormat()
    {
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.name("UT  Handler");
        reqHandler.log(LogLevel.TRACE, stringFormat("UTlogMessage", vars("utVar1, utVar2")));
        ;

        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(reqHandler);

        String lua = LuaFilterFactory.getCsaLuaFilter(handlers);
        String luaForHandler = extractLuaCodeForHandler(lua, reqHandler);

        var actualLines = extractLuaLines(luaForHandler);
        var expectedLines = new ArrayList<String>();
        // response_handle:logTrace(string.format("Create Repl: IP: %s", ip))

        expectedLines.add("request_handle:logTrace(string.format(\"UTlogMessage\"), utVar1, utVar2))");

        assertLinesMatch(expectedLines, actualLines);

    }

    @Test
    void testGetCsaLuaFilterMod_ControlAction_simple()
    {
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.name("UT  Handler");
        reqHandler.cond(when(var("chargingDataRef"))).then().end();

        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(reqHandler);

        String lua = LuaFilterFactory.getCsaLuaFilter(handlers);
        String luaForHandler = extractLuaCodeForHandler(lua, reqHandler);

        var actualLines = extractLuaLines(luaForHandler);
        var expectedLines = new ArrayList<String>();

        expectedLines.add("if(chargingDataRef)");
        expectedLines.add("then");
        expectedLines.add("end");

        assertLinesMatch(expectedLines, actualLines);

    }

    @Test
    void testGetCsaLuaFilterMod_HeaderAction_simple()
    {
        RequestHandler reqHandler = new RequestHandler();
        reqHandler.name("UT  Handler");
        reqHandler.addHttpHeader("x-chf-dest", "someheadervalue");
        reqHandler.removeHttpHeader("x-chf-dest", "someheadervalue");

        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(reqHandler);

        String lua = LuaFilterFactory.getCsaLuaFilter(handlers);
        String luaForHandler = extractLuaCodeForHandler(lua, reqHandler);

        var actualLines = extractLuaLines(luaForHandler);
        var expectedLines = new ArrayList<String>();

        expectedLines.add("request_handle:headers():add(\"x-chf-dest\", \"someheadervalue\")");
        expectedLines.add("request_handle:headers():remove(\"x-chf-dest\")");

        assertLinesMatch(expectedLines, actualLines);

    }

    /*
     * 
     * Utility Methods
     * 
     */

    /**
     * Extracts the Lua code for a specific handler
     * 
     * @param lua
     * @return
     */
    private String extractLuaCodeForHandler(String lua,
                                            RequestHandler handler)
    {
        String startStr = "# start request_handle for " + handler.getName();
        String endStr = "# end request_handle for " + handler.getName();
        return lua.substring(lua.indexOf(startStr) + startStr.length(), lua.indexOf(endStr));
    }

    /**
     * Extracts the complete Lua code string to list of trimmed strings
     * 
     * @param lua
     * @return
     */
    List<String> extractLuaLines(String lua)
    {
        var lualLines = Arrays.asList(lua.split(System.lineSeparator()));
        return lualLines.stream().filter(s -> !s.isBlank()).map(String::trim).collect(Collectors.toList());
    }

}
