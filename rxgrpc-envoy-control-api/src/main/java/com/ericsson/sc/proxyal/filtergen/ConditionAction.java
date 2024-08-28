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

enum Condition
{
    EQUALS(" == "),
    NOT_EQUALS(" ~= "),
    GREATER_THAN(" > "),
    GREATER_OR_EQUAL_THAN(" >= "),
    SMALLER_THAN(" < "),
    SMALLER_OR_EQUAL_THAN(" <= ");

    private final String luaCode;

    private Condition(String luaOperator)
    {
        this.luaCode = luaOperator;
    }

    @Override
    public String toString()
    {
        return this.luaCode;
    }

}

enum Logical
{
    AND(" and "),
    OR(" or ");

    private final String luaCode;

    private Logical(String luaOperator)
    {
        this.luaCode = luaOperator;
    }

    @Override
    public String toString()
    {
        return this.luaCode;
    }

}

/**
 * 
 */
public class ConditionAction extends Action
{

    private ConditionAction()
    {
    }

    private static class CondData
    {

        private Condition condition;
        private String cmpString;

        private CondData(Condition conditon,
                         String cmpString)
        {
            this.condition = conditon;
            this.cmpString = cmpString;
        }
    }

    static ConditionAction when(Variable var)
    {
        var action = new ConditionAction();
        action.setLuaCode("if(" + var.getName() + ")");
        incIndent();
        return action;
    }

    static ConditionAction when(Variable var,
                                CondData condData)
    {
        var action = new ConditionAction();
        action.setLuaCode("if(" + var.getName() + condData.condition + "\"" + condData.cmpString + "\"" + ")");
        incIndent();
        return action;
    }

    static ConditionAction when(Variable var,
                                Condition operator,
                                String cmpString)
    {
        var action = new ConditionAction();
        action.setLuaCode("if(" + var.getName() + operator + "\"" + cmpString + "\"" + ")");
        incIndent();
        return action;
    }

    static ConditionAction when(StringAction straction)
    {
        var action = new ConditionAction();
        action.setLuaCode("if(" + straction.getLuaCode() + ")");
        incIndent();
        return action;
    }

    /*
     * 
     * Methods for chaining various conditional expressions
     * 
     * 
     */

    public ConditionAction and(Variable var,
                               Condition condition,
                               String string)
    {
        appendLogicalExpressionToLua(Logical.AND, var, condition, string);
        return this;
    }

    public ConditionAction and(Variable var,
                               CondData condData)
    {
        appendLogicalExpressionToLua(Logical.AND, var, condData.condition, condData.cmpString);
        return this;
    }

    public ConditionAction or(Variable var,
                              Condition condition,
                              String string)
    {
        appendLogicalExpressionToLua(Logical.OR, var, condition, string);
        return this;
    }

    public ConditionAction or(Variable var,
                              CondData condData)
    {
        appendLogicalExpressionToLua(Logical.OR, var, condData.condition, condData.cmpString);
        return this;
    }

    /**
     * 
     * Helper mehod append the luaCode for a condition
     * 
     * @param logicalCond
     * @param varLeft
     * @param condition
     * @param stringRight
     */
    private void appendLogicalExpressionToLua(Logical logicalCond,
                                              Variable varLeft,
                                              Condition condition,
                                              String stringRight)
    {
        setLuaCode(new StringBuilder().append(getLuaCode())
                                      .append(logicalCond)
                                      .append("(")
                                      .append(varLeft.getName())
                                      .append(condition)
                                      .append("\"")
                                      .append(stringRight)
                                      .append("\"")
                                      .append(")")
                                      .toString());
    }

    public static CondData isEqualTo(String cmpStr)
    {
        return new CondData(Condition.EQUALS, cmpStr);
    }

    public static CondData isNotEqualTo(String cmpStr)
    {
        return new CondData(Condition.NOT_EQUALS, cmpStr);
    }

}
