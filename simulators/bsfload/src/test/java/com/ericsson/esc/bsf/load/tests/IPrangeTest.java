/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 20, 2022
 *     Author: znpvaap
 */
package com.ericsson.esc.bsf.load.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.ericsson.esc.bsf.load.configuration.IPrange;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class IPrangeTest
{
    private static final JsonMapper jm = JsonMapper.builder().build();

    @Test(groups = "functest", dataProvider = "invalid-start-ip-provider")
    public void invalidStartIP(String invalidIP)

    {
        var ipRange = new IPrange();
        ipRange.setStartIP(invalidIP);
        ipRange.setRange(3);

        var resultedList = ipRange.validate("set1");

        assertEquals(resultedList.size(), 1, "Expected one item in the list");

        assertEquals(resultedList.get(0).getParam(), "startIP", "Expected invalid startIP value");
    }

    @DataProvider(name = "invalid-start-ip-provider")
    public Object[][] invalidStartIPValues()
    {
        final String[] invalidIP = { "256.22.33.1", "null" };

        return new Object[][] { { invalidIP[0] }, { invalidIP[1] } };
    }

    @Test(groups = "functest", dataProvider = "invalid-range-provider")
    public void invalidRange(Long invalidRange)

    {
        var ipRange = new IPrange();
        ipRange.setStartIP("1.1.1.1");
        ipRange.setRange(invalidRange);

        var resultedList = ipRange.validate("set1");

        assertEquals(resultedList.size(), 1, "Expected one item in the list");

        assertEquals(resultedList.get(0).getParam(), "range", "Expected invalid range value");

    }

    @DataProvider(name = "invalid-range-provider")
    public Object[][] invalidRangeValues()
    {
        final Long[] invalidRange = { -1L, 0L };

        return new Object[][] { { invalidRange[0] }, { invalidRange[1] } };
    }

    @Test(groups = "functest", dataProvider = "data-provider-json", expectedExceptions = { InvalidFormatException.class })
    public void invalidRangeType(String jsonString) throws JsonMappingException, JsonProcessingException
    {

        jm.readValue(jsonString, IPrange.class);
    }

    @DataProvider(name = "data-provider-json")
    public Object[][] invalidIPRangeTypesJsonValues()
    {
        final String invalidStringAlphanumericRangeValue = "{\"start-ip\" : \"10.0.2.3\", \"range\" : \"10a\"}";
        final String invalidStringFloatRangeValue = "{\"start-ip\" : \"10.0.2.3\", \"range\" : \"1.3\"}";

        return new Object[][] { { invalidStringAlphanumericRangeValue }, { invalidStringFloatRangeValue } };
    }

    @Test(groups = "functest", dataProvider = "data-provider-unknownProperty-json", expectedExceptions = { UnrecognizedPropertyException.class })
    public void invalidUnknownJsonField(String jsonString) throws JsonMappingException, JsonProcessingException
    {
        jm.readValue(jsonString, IPrange.class);
    }

    @DataProvider(name = "data-provider-unknownProperty-json")
    public Object[][] invalidIPRangeUnknownJsonValues()
    {
        final String invalidJsonEntryIPfield = "{\"begin-ip\" : \"1.1.1.1\", \"range\" : 1}";
        final String invalidJsonEntryRangefield = "{\"start-ip\" : \"10.0.2.3\", \"step\" : 10}";

        return new Object[][] { { invalidJsonEntryIPfield }, { invalidJsonEntryRangefield } };
    }

    @Test(groups = "functest")
    public void validIPrange()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("192.168.3.2");
        ipRange.setRange(2);

        assertTrue(ipRange.validate("set1").isEmpty(), "Expected valid IPrange");
    }

    @Test(groups = "functest", expectedExceptions = JsonParseException.class)
    public void parseInvalidJsonFail() throws JsonMappingException, JsonProcessingException
    {
        var invalidJsonString = "{\"[\"}";

        jm.readValue(invalidJsonString, IPrange.class);
    }

    @Test(groups = "functest")
    public void validEquals()
    {
        var ipRange1 = new IPrange();
        var ipRange2 = ipRange1;

        assertTrue(ipRange1.equals(ipRange2), "Expected equal objects");
    }

    @Test(groups = "functest")
    public void unequalIPrangeNull()
    {
        var ipRange = new IPrange();

        assertFalse(ipRange.equals(null), "Expected unequal objects");
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test(groups = "functest")
    public void unequalGetClass()
    {
        var ipRange = new IPrange();
        Integer number = 5;
        assertFalse(ipRange.equals(number), "Expected unequal objects due to diff Classes");
    }

    @Test(groups = "functest")
    public void unequalRangeValues()
    {
        var ipRange1 = new IPrange();
        var ipRange2 = new IPrange();
        ipRange1.setRange(1);
        ipRange2.setRange(3);

        assertFalse(ipRange1.equals(ipRange2), "Expected unequal range values");
    }

    @Test(groups = "functest")
    public void unequalIPNull()
    {
        var ipRange1 = new IPrange();
        var ipRange2 = new IPrange();
        ipRange1.setStartIP(null);
        ipRange2.setStartIP("1.1.1.1");

        assertFalse(ipRange1.equals(ipRange2), "Last object expected non null value");

    }

    @Test(groups = "functest")
    public void unequalStartIP()
    {
        var ipRange1 = new IPrange();
        var ipRange2 = new IPrange();
        ipRange1.setStartIP("2.1.3.4");
        ipRange2.setStartIP("1.1.1.1");

        assertFalse(ipRange1.equals(ipRange2), "Expected non equals IPs");

    }

    @Test(groups = "functest")
    public void equalsRangeAndIPValues()
    {
        var ipRange1 = new IPrange();
        var ipRange2 = new IPrange();
        ipRange1.setStartIP("1.1.1.1");
        ipRange2.setStartIP("1.1.1.1");

        ipRange1.setRange(1);
        ipRange2.setRange(1);

        assertTrue(ipRange1.equals(ipRange2), "Expected equal values");
    }

}