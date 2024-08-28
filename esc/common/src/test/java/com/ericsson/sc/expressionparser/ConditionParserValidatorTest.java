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
 * Created on: Dec 4, 2020
 *     Author: enocakh
 */

package com.ericsson.sc.expressionparser;

import static org.testng.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionParserValidatorTest
{
    private static Stream<String> generateValidConditions()
    {
        var inputList = new ArrayList<String>();
        inputList.add("true == false");
        inputList.add("var.header == false");
        inputList.add("req.path == 'path'");
        inputList.add("req.header['name'] == var.name_a");
        inputList.add("req.method == 'GET'");
        inputList.add("var.name == false");
        inputList.add("req.header['name'] exists");
        inputList.add("req.method exists");
        inputList.add("req.path exists");
        inputList.add("var.name exists");
        inputList.add("req.header['name'] isempty");
        inputList.add("req.method isempty");
        inputList.add("req.path isempty");
        inputList.add("var.name isempty");
        inputList.add("true and false");
        inputList.add("true or true");
        inputList.add("not false");
        inputList.add("req.method == 'GET' and not var.name isempty");
        inputList.add("true or not false and true or var.d exists");
        inputList.add("var.a == var.b or req.path isempty and req.method exists");
        inputList.add("(true or false) and not (true or false)");
        inputList.add("req.method == 'POST' and not req.header['t/e-st'] isempty and (var.mcc == '123' or var.mcc == '456' or req.path exists)");
        inputList.add("var.fqdn == 'abc' and not ( var.req isempty ) or var.nfdata == var.method");
        inputList.add("var.sth == 'a b c'");
        // inputList.add("");
        return inputList.stream();
    }

    @Tag("integration")
    @ParameterizedTest
    @MethodSource("generateValidConditions")
    void testPositiveInput(String input)
    {
        ConditionParserValidator.validate(input);
    }

    // ============ Negative TCs ============

    private static Stream<String> generateInvalidConditions()
    {
        var inputList = new ArrayList<String>();
        inputList.add("var.name ==");
        inputList.add("== var.name");
        inputList.add("exists var.name");
        inputList.add("'string' exists");
        inputList.add("(var.name isEmpty) exists");
        inputList.add("isempty var.a");
        inputList.add("isempty 'string'");
        inputList.add("isempty bool");
        inputList.add("'string' and (var.a exists)");
        inputList.add("var.b and (var.a exists)");
        inputList.add("and var.a");
        inputList.add("var.b or (var.a exists)");
        inputList.add("true or var.a");
        inputList.add("true not");
        inputList.add("not req.path");
        inputList.add("req.method");
        inputList.add("var.sth == \"a b c\"");
        // Construct multiple inputs with reserved keywords
        var reservedKeywords = List.of("true", "false", "and", "or", "not", "exists", "isinsubnet");
        reservedKeywords.forEach(kw ->
        {
            String input = "var." + kw + " == 'a'";
            inputList.add(input);
        });
        // inputList.add("");
        return inputList.stream();
    }

    @Tag("integration")
    @ParameterizedTest
    @MethodSource("generateInvalidConditions")
    void testNegativeInput(String input)
    {
        assertThrows(() -> ConditionParserValidator.validate(input));
    }

}
