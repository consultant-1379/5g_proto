package com.ericsson.sc.expressionparser;

import static org.testng.Assert.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ericsson.sc.expressionparser.NfConditionParserValidator;

class ConstraintParserValidatorTest
{

    @Tag("integration")
    @Test
    void testEmptyExpression()
    {
        String input = "";
        NfConditionParserValidator.validate(input);

    }

    @Tag("integration")
    @Test
    void testExistsNfdata()
    {
        String input = "nfdata.fqdn exists";
        NfConditionParserValidator.validate(input);

    }

    @Tag("integration")
    @Test
    void testEqNfdataString()
    {
        String input = "nfdata.scp-domain == 'eric'";
        NfConditionParserValidator.validate(input);
    }

    @Tag("integration")
    @Test
    void testEqUnknwonNfdata()
    {
        String input = "nfdata['unknown'] == 'uk'";
        NfConditionParserValidator.validate(input);
    }

    @Tag("integration")
    @Test
    void testAndEq()
    {
        String input = "nfdata['set'] == 'A' and 'eric2' == nfdata.fqdn";
        NfConditionParserValidator.validate(input);
    }

    @Tag("integration")
    @Test
    void testOrExistsEq()
    {
        String input = "nfdata['flag'] exists or nfdata.ipv4-address == '1.1'";
        NfConditionParserValidator.validate(input);
    }

    @Tag("integration")
    @Test
    void testNotAndEqEx()
    {
        String input = "not('targ' == nfdata['target'] and nfdata['set'] exists)";
        NfConditionParserValidator.validate(input);
    }

//   
    // Negative Cases
    @Tag("integration")
    @Test
    void testEqVarStr()
    {
        String input = "var.data == 'str'";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testEqStrStr()
    {
        String input = "'str1' == 'str'";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testEqVarVar()
    {
        String input = "var.locality == var.set";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testEqNfdataNfdata()
    {
        String input = "nfdata.locality == nfdata.fqdn";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testExStr()
    {
        String input = "'str' exists";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testExVar()
    {
        String input = "var.locality exists";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testExNfdataWrongOrder()
    {
        String input = "exists nfdata.set";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

    @Tag("integration")
    @Test
    void testUnknownNfdataAsIdentifier()
    {
        String input = "nfdata.notwellknown exists";
        assertThrows(() -> NfConditionParserValidator.validate(input));
    }

}
