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
 * Created on: Dec 2, 2020
 *     Author: eavapsr
 */

package com.ericsson.sc.expressionparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ConditionParserTest
{

    @Tag("integration")
    @Test
    void testEqualityBoolean()
    {
        String input = "true == false";
        String expectedOutput = "op_equal(term_boolean(true),term_boolean(false))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testEqualityReqPath()
    {
        String input = "req.path == 'path'";
        String expectedOutput = "op_equal(term_reqheader(':path'),term_string(path))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testEqualityReqHeader()
    {
        String input = "req.header['name'] == var.name";
        String expectedOutput = "op_equal(term_reqheader(name),term_var(name))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testEqualityReqMethod()
    {
        String input = "req.method == 'GET'";
        String expectedOutput = "op_equal(term_reqheader(':method'),term_string(GET))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testEqualityVar()
    {
        String input = "var.name == false";
        String expectedOutput = "op_equal(term_var(name),term_boolean(false))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testExistsReqHeader()
    {
        String input = "req.header['name'] exists";
        String expectedOutput = "op_exists(term_reqheader(name))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testExistsReqMethod()
    {
        String input = "req.method exists";
        String expectedOutput = "op_exists(term_reqheader(':method'))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testExistsReqPath()
    {
        String input = "req.path exists";
        String expectedOutput = "op_exists(term_reqheader(':path'))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testExistsVar()
    {
        String input = "var.name exists";
        String expectedOutput = "op_exists(term_var(name))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testIsEmptyReqHeader()
    {
        String input = "req.header['name'] isempty";
        String expectedOutput = "op_isempty(term_reqheader(name))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testIsEmptyReqMethod()
    {
        String input = "req.method isempty";
        String expectedOutput = "op_isempty(term_reqheader(':method'))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testIsEmptyReqPath()
    {
        String input = "req.path isempty";
        String expectedOutput = "op_isempty(term_reqheader(':path'))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testIsEmptyVar()
    {
        String input = "var.name_a isempty";
        String expectedOutput = "op_isempty(term_var(name_a))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testAnd()
    {
        String input = "true and false";
        String expectedOutput = "op_and(term_boolean(true),term_boolean(false))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testOr()
    {
        String input = "true or true";
        String expectedOutput = "op_or(term_boolean(true),term_boolean(true))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testIsInSubnet()
    {
        String input = "req.header['authority'] isinsubnet '10.10.10.0/24'";
        String expectedOutput = "op_isinsubnet(term_reqheader(authority),term_string(10.10.10.0/24))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testNot()
    {
        String input = "not false";
        String expectedOutput = "op_not(term_boolean(false))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testStrAndBoolPrecedence() // 22
    {
        String input = "req.method == 'GET' and not var.name isempty";
        String expectedOutput = "op_and(op_equal(term_reqheader(':method'),term_string(GET)),op_not(op_isempty(term_var(name))))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testBooleanPrecedence() // 23
    {
        String input = "true or not false and true or var.d exists";
        String expectedOutput = "op_or(op_or(term_boolean(true),op_and(op_not(term_boolean(false)),term_boolean(true))),op_exists(term_var(d)))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testStrinOpPrecedence() // 24
    {
        String input = "var.a == var.b or req.path isempty and req.method exists";
        String expectedOutput = "op_or(op_equal(term_var(a),term_var(b)),op_and(op_isempty(term_reqheader(':path')),op_exists(term_reqheader(':method'))))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testParentheticBooleanInvertedPrecedence() // 25
    {
        String input = "(true or false) and not (true or false)";
        String expectedOutput = "op_and(op_or(term_boolean(true),term_boolean(false)),op_not(op_or(term_boolean(true),term_boolean(false))))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testMultipleOperations()
    {
        String input = "req.method == 'POST' and not req.header['t/e-st'] isempty and (var.mcc == '123' or var.mcc == '456' or req.path exists)";
        String expectedOutput = "op_and(op_and(op_equal(term_reqheader(':method'),term_string(POST)),op_not(op_isempty(term_reqheader(t/e-st)))),op_or(op_or(op_equal(term_var(mcc),term_string(123)),op_equal(term_var(mcc),term_string(456))),op_exists(term_reqheader(':path'))))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

    @Tag("integration")
    @Test
    void testVarReservedKeyword()
    {
        String input = "var.method isempty";
        String expectedOutput = "op_isempty(term_var(method))";
        assertEquals(expectedOutput, ConditionParser.parse(input).toString());
    }

}
