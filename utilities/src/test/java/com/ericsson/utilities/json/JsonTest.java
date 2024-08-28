package com.ericsson.utilities.json;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.utilities.json.Json.Patch;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTest
{
    private static final Logger log = LoggerFactory.getLogger(JsonTest.class);

    private static final ObjectMapper mapper = Jackson.newOm(); // create once, reuse

    @Test(enabled = true)
    public void shouldReturnEmptyMapForEqualDocument() throws IOException
    {
        var from = toJson("{\"foo\":1}");
        var xxto = toJson("{\"foo\":1}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);
        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectRemovedKeys() throws IOException
    {
        var from = toJson("{\"foo\":123,\"bar\":1}");
        var xxto = toJson("{\"foo\":123}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectReplacementOfPrimitives() throws IOException
    {
        var from = toJson("{\"foo\":1}");
        var xxto = toJson("{\"foo\":\"bar\"}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectReplacementOfObjectWithPrimitive() throws IOException
    {
        var from = toJson("{\"foo\":1,\"bar\":{\"a\":2,\"b\":3}}");
        var xxto = toJson("{\"foo\":1,\"bar\":2}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectReplacementInNestedObject() throws IOException
    {
        var from = toJson("{\"foo\":{\"bar\":{\"a\":1,\"b\":\"b\"},\"baz\":12}}");
        var xxto = toJson("{\"foo\":{\"bar\":{\"a\":2,\"c\":\"c\"},\"baz\":12}}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectReplacementInPrimitiveArray() throws IOException
    {
        var from = toJson("{\"foo\":[0,1,2]}");
        var xxto = toJson("{\"foo\":[0,3,2]}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectExtensionOfPrimitiveArray() throws IOException
    {
        var from = toJson("{\"foo\":[0,1,2]}");
        var xxto = toJson("{\"foo\":[0,1,2,3,4]}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectShrinkOfPrimitiveArray() throws IOException
    {
        var from = toJson("{\"foo\":[0,1,2,3,4]}");
        var xxto = toJson("{\"foo\":[0,2]}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectInsertionOfANewObjectToArray() throws IOException
    {
        var from = toJson("{\"foo\":[]}");
        var xxto = toJson("{\"foo\":[{\"a\":{\"value\":1}}]}");
        List<Patch> patches = Json.diff(from, xxto);
        log.info("patches={}", patches);

        assertEquals(xxto, Json.patch(from, patches, LinkedHashMap.class));
    }

    @Test(enabled = true)
    public void shouldDetectChangesInArrayElement() throws IOException
    {
        String from = "{\"foo\":[{\"a\":1,\"b\":2},{\"c\":3}]}";
        String xxto = "{\"foo\":[{\"a\":1,\"b\":{\"value\":2}},{\"c\":3}]}";
        List<Patch> patches = Json.diff(from, xxto);
        // System.out.println(patches.toString());
        assertEquals(xxto, Json.patch(from, patches, String.class));
        assertEquals(xxto, Json.patch(from, mapper.writeValueAsString(patches)));
        assertEquals(toJson(xxto), Json.patch(from, patches, LinkedHashMap.class));
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> toJson(String json)
    {
        try
        {
            return mapper.readValue(json, LinkedHashMap.class);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
