package com.ericsson.sc.proxyal.filtergen;

import static com.ericsson.sc.proxyal.filtergen.ConditionAction.*;
import static com.ericsson.sc.proxyal.filtergen.ControlAction.*;
import static com.ericsson.sc.proxyal.filtergen.HttpHeaderAction.*;
import static com.ericsson.sc.proxyal.filtergen.StringAction.fromMatch;
import static com.ericsson.sc.proxyal.filtergen.StringAction.stringFind;
import static com.ericsson.sc.proxyal.filtergen.Variable.var;
import static com.ericsson.sc.proxyal.filtergen.Variable.vars;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ericsson.sc.proxyal.filtergen.Action;
import com.ericsson.sc.proxyal.filtergen.Condition;

class ControlActionsTest
{

    @Test
    void testIfStringFound()
    {
        Action ctrAction;
        String testVar = "testVar";

        ctrAction = when(var(testVar));
        assertEquals("if(testVar)", ctrAction.getLuaCode());

        ctrAction = when(stringFind(var(testVar), "pattern"));
        assertEquals("if(string.find(testVar, 'pattern'))", ctrAction.getLuaCode());
    }

    @Test
    void testSetVarFromMatch()
    {
        Action ctrAction;

        String inputVar = "path";
        String pattern = "pattern";
        String varsToBeSet = "testVar1, testvar2";

        ctrAction = set(vars(varsToBeSet), fromMatch(var(inputVar), pattern));
        assertEquals("local testVar1, testvar2 = string.match(path, 'pattern')", ctrAction.getLuaCode());
    }

    @Test
    void testSetVarFromHttpHeader()
    {
        Action ctrAction;
        ctrAction = set(var("var"), fromHttpHeader("x-notify-uri"));
        assertEquals("local var = request_handle:headers():get(\"x-notify-uri\")", ctrAction.getLuaCode());
    }

    @Test
    void testSetVar()
    {
        Action ctrAction;
        ctrAction = set(var("varname"), "testVal");
        assertEquals("local varname = \"testVal\"", ctrAction.getLuaCode());
    }

    @Test
    void testIfVarEquals()
    {
        Action ctrAction;
        ctrAction = when(var("varname"), Condition.EQUALS, "testVal");
        assertEquals("if(varname == \"testVal\")", ctrAction.getLuaCode());
    }

    @Test
    void testIfVarEqualsAndOtherCondition()
    {
        Action ctrAction;
        ctrAction = when(var("varname"), Condition.EQUALS, "testVal").and(var("varname1"), Condition.EQUALS, "testVal1");
        assertEquals("if(varname == \"testVal\") and (varname1 == \"testVal1\")", ctrAction.getLuaCode());
    }

}
