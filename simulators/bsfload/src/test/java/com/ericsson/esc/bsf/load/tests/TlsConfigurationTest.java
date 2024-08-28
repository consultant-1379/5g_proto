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

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.ericsson.esc.bsf.load.configuration.TlsConfiguration;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class TlsConfigurationTest
{

    private static final JsonMapper jm = JsonMapper.builder().build();

    @Test(groups = "functest")
    public void validTlsConfigurationDefaults()
    {
        var tlsConfig = new TlsConfiguration.Builder().build();

        assertTrue(tlsConfig.validate().isEmpty(), "Expected valid default Tls configuration");

    }

    @Test(groups = "functest")
    public void validTlsConfiguration()
    {
        var tlsConfig = new TlsConfiguration.Builder().enabled(true)
                                                      .certPath("/opt/bsf-load/certificates/certificate.pem")
                                                      .keyPath("/opt/bsf-load/certificates/keyPath.pem")
                                                      .verifyHost(true)
                                                      .build();

        assertTrue(tlsConfig.validate().isEmpty(), "Expected valid Tls configuration");

    }

    @Test(groups = "functest")
    public void invalidTlsConfiguration()
    {
        List<String> invalidParams = Arrays.asList("enabled", "verify-host", "key-path", "cert-path");
        var tlsConfig = new TlsConfiguration.Builder().enabled(null).certPath(null).keyPath(null).verifyHost(null).build();

        assertFalse(tlsConfig.validate().isEmpty(), "Expected invalid Tls configuration");
        assertEquals(tlsConfig.validate().size(), invalidParams.size(), "Expected 4 invalid values for tls configuration");

        int listCounter = 0;
        for (var item : tlsConfig.validate())
        {
            assertEquals(item.getParam(), invalidParams.get(listCounter), "Expected invalid value of " + invalidParams.get(listCounter));
            listCounter++;
        }
    }

    @Test(groups = "functest", dataProvider = "data-provider-invalidFormatException", expectedExceptions = { InvalidFormatException.class })
    public void invalidFormatException(String invalidFormatJson) throws JsonMappingException, JsonProcessingException
    {

        jm.readValue(invalidFormatJson, TlsConfiguration.class);

    }

    @DataProvider(name = "data-provider-invalidFormatException")
    public Object[][] invalidFormatExceptionTlsConf()
    {
        String invalidFormatEnabled = "{\"enabled\" : \"long\", \"verify-host\" : \"false\","
                                      + "\"cert-path\": \"./certificates/cert.pem\" , \"key-path\": \"./certificates/key.pem\"}";

        String invalidFormatVerifyHost = "{\"enabled\" : \"true\", \"verify-host\" : \"3\","
                                         + "\"cert-path\": \"./certificates/cert.pem\" , \"key-path\": \"./certificates/key.pem\"}";

        return new Object[][] { { invalidFormatEnabled }, { invalidFormatVerifyHost } };
    }

    @Test(groups = "functest", expectedExceptions = JsonParseException.class)
    public void parseInvalidJsonFail() throws JsonMappingException, JsonProcessingException
    {
        var invalidJsonString = "{\"[\"}";

        jm.readValue(invalidJsonString, TlsConfiguration.class);
    }

    @Test(groups = "functest", dataProvider = "data-provider-unknownProperty-json", expectedExceptions = { UnrecognizedPropertyException.class })
    public void invalidUnknownJsonPropertyField(String jsonString) throws JsonMappingException, JsonProcessingException
    {
        jm.readValue(jsonString, TlsConfiguration.class);
    }

    @DataProvider(name = "data-provider-unknownProperty-json")
    public Object[][] invalidUnknownJsonPropertyValues()
    {
        final String invalidJsonEntryEnabledfield = "{\"disabled\" : \"true\", \"verify-host\" : \"false\", "
                                                    + "\"cert-path\": \"./certificates/cert.pem\" , \"key-path\": \"./certificates/key.pem\"}";
        final String invalidJsonEntryVerifyHostfield = "{\"enabled\" : \"true\", \"unverify-host\" : \"false\", "
                                                       + "\"cert-path\": \"./certificates/cert.pem\" , \"key-path\": \"./certificates/key.pem\"}";

        final String invalidJsonEntryCertPathField = "{\"enabled\" : \"true\", \"verify-host\" : \"false\", "
                                                     + "\"set-path\": \"./certificates/cert.pem\" , \"key-path\": \"./certificates/key.pem\"}";

        final String invalidJsonEntryKeyPathField = "{\"enabled\" : \"true\", \"verify-host\" : \"false\", "
                                                    + "\"cert-path\": \"./certificates/cert.pem\" , \"value-path\": \"./certificates/key.pem\"}";
        return new Object[][] { { invalidJsonEntryEnabledfield },
                                { invalidJsonEntryVerifyHostfield },
                                { invalidJsonEntryCertPathField },
                                { invalidJsonEntryKeyPathField } };
    }

    @Test(groups = "functest")
    public void validEquals()
    {
        var tlsConf1 = new TlsConfiguration.Builder().build();
        var tlsConf2 = tlsConf1;

        assertTrue(tlsConf1.equals(tlsConf2), "Expected equal objects");
    }

    @Test(groups = "functest")
    public void unequalTlsConfigurationNull()
    {
        var tlsConf = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf.equals(null), "Expected unequal objects due to null");
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test(groups = "functest")
    public void unequalGetClass()
    {
        var tlsConf = new TlsConfiguration.Builder().build();
        Integer number = 5;
        assertFalse(tlsConf.equals(number), "Expected unequal objects due to diff Classes");
    }

    @Test(groups = "functest")
    public void invalidEqualsEnabledNull()
    {
        var tlsConf1 = new TlsConfiguration.Builder().enabled(null).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Last object expected non null value");

    }

    @Test(groups = "functest")
    public void unequalEnabledValues()
    {
        var tlsConf1 = new TlsConfiguration.Builder().enabled(true).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Expected unequal enabled values");
    }

    @Test(groups = "functest")
    public void unequalVerifyHostNull()
    {
        var tlsConf1 = new TlsConfiguration.Builder().verifyHost(null).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Last object expected non null value");

    }

    @Test(groups = "functest")
    public void unequalVerifyHost()
    {
        var tlsConf1 = new TlsConfiguration.Builder().verifyHost(true).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Expected non equals verify_host values");

    }

    @Test(groups = "functest")
    public void unequalCertPathNull()
    {
        var tlsConf1 = new TlsConfiguration.Builder().certPath(null).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Last object expected non null value");
    }

    @Test(groups = "functest")
    public void unequalCertPath()
    {
        var tlsConf1 = new TlsConfiguration.Builder().build();
        var tlsConf2 = new TlsConfiguration.Builder().certPath("invalid_string").build();

        assertFalse(tlsConf1.equals(tlsConf2), "Expected non equals cert_path values");
    }

    @Test(groups = "functest")
    public void unequalKeyPathNull()
    {
        var tlsConf1 = new TlsConfiguration.Builder().keyPath(null).build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertFalse(tlsConf1.equals(tlsConf2), "Last object expected non null value");
    }

    @Test(groups = "functest")
    public void unequalKeyPath()
    {
        var tlsConf1 = new TlsConfiguration.Builder().build();
        var tlsConf2 = new TlsConfiguration.Builder().keyPath("invalid_string").build();

        assertFalse(tlsConf1.equals(tlsConf2), "Expected non equals key_path values");
    }

    @Test(groups = "functest")
    public void equalsAllValues()
    {
        var tlsConf1 = new TlsConfiguration.Builder().build();
        var tlsConf2 = new TlsConfiguration.Builder().build();

        assertTrue(tlsConf1.equals(tlsConf2), "Expected equal values");
    }
}