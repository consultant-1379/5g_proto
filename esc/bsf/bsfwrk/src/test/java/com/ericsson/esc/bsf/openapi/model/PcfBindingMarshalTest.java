/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 24, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.openapi.model;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test class for the serialization of PcfBinding. The included cases ensure the
 * correctness of the marshalling/unmarshalling procedure using the Jackson
 * Parser. Additionally, the behavior of the PcfBinding class is examined
 * against correct and invalid combinations of constructor arguments according
 * to 3GPP TS-29.521.
 * 
 * @author zmelpan
 */
public class PcfBindingMarshalTest
{

    private final ObjectMapper mapper = Jackson.om();

    /**
     * Creates a valid PcfBinding Object and then serializes it into a JSON Object.
     * The values of the serialized Object are checked. against the values of the
     * initial PcfBinding. All assertions are expected to pass.
     */
    @Test
    public void successfulBindingMarshalling() throws Exception
    {
        String supi = "imsi-310150123456789";
        String gpsi = "msisdn-918369110173";
        String ipv4Addr = "144.241.174.78";
        String dnn = "valid.ericsson.se";
        String pcfFqdn = "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org";
        Snssai snssai = Snssai.create(64, "E8F44A");
        PcfBinding bindingAsObject = PcfBinding.createJson(supi,
                                                           gpsi,
                                                           ipv4Addr,
                                                           null,
                                                           null,
                                                           null,
                                                           dnn,
                                                           pcfFqdn,
                                                           null,
                                                           null,
                                                           null,
                                                           snssai,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null);

        String bindingAsJsonStr = null;
        JsonNode bindingAsJson = null;

        bindingAsJsonStr = mapper.writeValueAsString(bindingAsObject);
        bindingAsJson = mapper.readTree(bindingAsJsonStr);

        assertTrue(bindingAsJson.get("supi").asText().equals(bindingAsObject.getSupi()));
        assertTrue(bindingAsJson.get("gpsi").asText().equals(bindingAsObject.getGpsi()));
        assertTrue(bindingAsJson.get("ipv4Addr").asText().equals(bindingAsObject.getIpv4Addr().getHostAddress()));
        assertTrue(bindingAsJson.get("dnn").asText().equals(bindingAsObject.getDnn()));
        assertTrue(bindingAsJson.get("pcfFqdn").asText().equals(bindingAsObject.getPcfFqdn()));
        assertTrue(bindingAsJson.get("snssai").get("sst").asInt() == bindingAsObject.getSnssai().getSst());
        assertTrue(bindingAsJson.get("snssai").get("sd").asText().equals(bindingAsObject.getSnssai().getSd()));
    }

    @Test
    /**
     * Creates a valid JSON Object representation for PcfBinding and then unmarshals
     * it into an actual PcfBinding Object. The assertion is expected to pass.
     */
    public void successfulBindingUnmarshalling()
    {

        // Create a binding as JSON to parse.
        ObjectNode bindingAsJson = mapper.createObjectNode();
        ObjectNode snssaiAsJson = mapper.createObjectNode();

        snssaiAsJson.put("sst", 64);
        snssaiAsJson.put("sd", "E8F44A");

        bindingAsJson.put("supi", "imsi-310150123456789");
        bindingAsJson.put("gpsi", "msisdn-918369110173");
        bindingAsJson.put("ipv4Addr", "144.241.174.78");
        bindingAsJson.put("dnn", "valid.ericsson.se");
        bindingAsJson.put("pcfFqdn", "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org");
        bindingAsJson.putPOJO("snssai", snssaiAsJson);

        PcfBinding bindingAsObject = null;
        try
        {
            // Parse binding from its JSON representation.
            bindingAsObject = mapper.readValue(bindingAsJson.toString(), PcfBinding.class);
        }
        catch (JsonParseException | JsonMappingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Create an assertion binding.
        String supi = "imsi-310150123456789";
        String gpsi = "msisdn-918369110173";
        String ipv4Addr = "144.241.174.78";
        String dnn = "valid.ericsson.se";
        String pcfFqdn = "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org";
        Snssai snssai = Snssai.create(64, "E8F44A");
        PcfBinding assertionBinding = PcfBinding.createJson(supi,
                                                            gpsi,
                                                            ipv4Addr,
                                                            null,
                                                            null,
                                                            null,
                                                            dnn,
                                                            pcfFqdn,
                                                            null,
                                                            null,
                                                            null,
                                                            snssai,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);

        assertTrue(bindingAsObject.equals(assertionBinding), "The deserialized PcfBinding is not equal to the assertion PcfBinding");
    }

    @Test(expectedExceptions = { JsonParseException.class, JsonMappingException.class })
    /**
     * Parses an invalid JSON Object representation for PcfBinding and then
     * unmarshals it. The test is expected to throw JsonParseException and
     * JsonMappingException.
     * 
     * @throws JsonParseException, JsonMappingException
     */
    public void invalidJsonBindingUnmarshalling() throws JsonParseException, JsonMappingException, IOException
    {

        File invalidJson = null;

        // Load invalid JSON file.
        ClassLoader classLoader = getClass().getClassLoader();
        invalidJson = new File(classLoader.getResource("invalidJsonBinding.json").getFile());
        // Parse binding from its JSON representation.
        mapper.readValue(invalidJson, PcfBinding.class);
    }

    /**
     * Parses a JSON Object representation for PcfBinding with invalid arguments (no
     * UE address) and then unmarshals it. The test is expected to throw
     * JsonParseException and JsonMappingException, which is caused by
     * IllegalArgumentException.
     * 
     * @throws JsonParseException, JsonMappingException
     */
    @Test(expectedExceptions = { JsonParseException.class, JsonMappingException.class })
    public void invalidArgumentsBindingUnmarshallingNoUeAddress() throws JsonParseException, JsonMappingException, IOException
    {

        // Create a binding as JSON to parse with invalid arguments (no UE address).
        ObjectNode bindingAsJson = mapper.createObjectNode();
        ObjectNode snssaiAsJson = mapper.createObjectNode();

        snssaiAsJson.put("sst", 64);
        snssaiAsJson.put("sd", "E8F44A");

        bindingAsJson.put("supi", "imsi-310150123456789");
        bindingAsJson.put("gpsi", "msisdn-918369110173");
        bindingAsJson.put("dnn", "valid.ericsson.se");
        bindingAsJson.put("pcfFqdn", "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org");
        bindingAsJson.putPOJO("snssai", snssaiAsJson);

        // Parse binding from its JSON representation.
        mapper.readValue(bindingAsJson.toString(), PcfBinding.class);
    }

    /**
     * Parses a JSON Object representation for PcfBinding with invalid arguments (no
     * dnn, no snssai) and then unmarshals it. The test is expected to throw
     * JsonParseException and JsonMappingException, which is caused by
     * IllegalArgumentException.
     * 
     * @throws JsonParseException, JsonMappingException
     */
    @Test(enabled = true, expectedExceptions = { JsonParseException.class, JsonMappingException.class })
    public void invalidArgumentsBindingUnmarshallingNoDnnNoSnssai() throws JsonParseException, JsonMappingException, IOException
    {

        // Create a binding as JSON to parse with invalid arguments (no dnn, no snssai).
        ObjectNode bindingAsJson = mapper.createObjectNode();
        ObjectNode snssaiAsJson = mapper.createObjectNode();

        snssaiAsJson.put("sst", 64);
        snssaiAsJson.put("sd", "E8F44A");
        bindingAsJson.put("ipv4Addr", "144.241.174.78");
        bindingAsJson.put("supi", "imsi-310150123456789");
        bindingAsJson.put("gpsi", "msisdn-918369110173");
        bindingAsJson.put("pcfFqdn", "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org");

        // Parse binding from its JSON representation.
        mapper.readValue(bindingAsJson.toString(), PcfBinding.class);
    }
}
