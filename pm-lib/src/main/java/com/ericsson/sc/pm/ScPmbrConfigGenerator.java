package com.ericsson.sc.pm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.core.json.JsonObject;

public class ScPmbrConfigGenerator
{

    public enum CfgType
    {
        BSF,
        CSA,
        SCP,
        SEPP
    }

    private enum CfgFileType
    {
        GROUP,
        JOB
    }

    private static final Logger log = LoggerFactory.getLogger(ScPmbrConfigGenerator.class);
    private final PmbrConfigBuilder builder = new PmbrConfigBuilder();
    private final ObjectMapper om = Jackson.om();
    private final JsonNode generatedConfig;
    private List<PatchItem> jobItems;
    private List<PatchItem> groupItems;

    public ScPmbrConfigGenerator()

    {
        try
        {
            log.info("Initialized PM bulk reporter configuration manager");
            this.generatedConfig = generateConfig();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Failed to generate PM Bulk reporter configuration", e);
        }
    }

    public JsonNode getConfig()
    {
        return this.generatedConfig;
    }

    private JsonNode generateConfig() throws IOException
    {
        addConfigGroups();
        addConfigJobs();
        return builder.build();
    }

    private void addConfigGroups() throws IOException
    {
        final var mergedGroups = this.mergeConfigFilesIfNeeded(CfgFileType.GROUP);

        builder.addGroups(this.updateWcdbNameIfNeeded(mergedGroups));

        this.groupItems = generateGroupPatches(builder.groupArray);
    }

    private void addConfigJobs() throws IOException
    {
        builder.addJobs(this.mergeConfigFilesIfNeeded(CfgFileType.JOB));

        this.jobItems = generateJobPatches(builder.jobArray);
    }

    private final synchronized List<JsonNode> mergeConfigFilesIfNeeded(final CfgFileType fileType) throws IOException
    {
        final var configJson = om.readValue(this.getClass().getResourceAsStream(String.format("/pmbr/configs/%s.json", fileType.toString().toLowerCase())),
                                            new TypeReference<List<JsonNode>>()
                                            {
                                            });

        if (Boolean.parseBoolean(EnvVars.get("BSF_DIAMETER_ENABLED", false))) // defaults to false for SCP/SEPP
        {
            log.info("Merging PMBR {} JSON files since diameter is deployed", fileType);

            final var configDiameterJson = om.readValue(this.getClass()
                                                            .getResourceAsStream(String.format("/pmbr/configs/%s-diameter.json",
                                                                                               fileType.toString().toLowerCase())),
                                                        new TypeReference<List<JsonNode>>()
                                                        {
                                                        });

            final var configMap = new HashMap<String, JsonNode>();
            final var configDiameterMap = new HashMap<String, JsonNode>();

            configJson.forEach(node -> configMap.put(node.get("name").asText(), node.deepCopy()));

            configDiameterJson.forEach(diameternode -> configDiameterMap.put(diameternode.get("name").asText(), diameternode.deepCopy()));

            configMap.forEach((fieldName,
                               fieldValues) ->
            {
                if (configDiameterMap.containsKey(fieldName)) // common fields, e.g., common measurement groups
                {
                    final var subFieldName = String.format("measurement-%s", fileType.equals(CfgFileType.GROUP) ? "type" : "reader");

                    final var arrayPatch = (ArrayNode) fieldValues.get(subFieldName);

                    configDiameterMap.get(fieldName).get(subFieldName).forEach(arrayPatch::add);

                    log.debug("Patch for {} field of {} : {}", subFieldName, fieldName, arrayPatch);

                    fieldValues = ((ObjectNode) fieldValues).set(subFieldName, arrayPatch);
                }
            });

            configDiameterMap.forEach((diameterFieldName,
                                       diameterFieldValues) ->
            {
                if (!configMap.containsKey(diameterFieldName)) // only group-diameter or job-diameter fields
                {
                    configMap.put(diameterFieldName, diameterFieldValues);
                }
            });

            return List.copyOf(configMap.values());
        }
        else
        {
            return configJson;
        }
    }

    /**
     * Parses the group.json file (JSON array of measurement type items) and
     * replaces the WCDBCD name template with the name of the deployed WCDBCD
     * service.
     * 
     * @param groupJson The input group.json file as a JSON array.
     * @return A list of JsonNode items (final group.json file).
     */
    private final synchronized List<JsonNode> updateWcdbNameIfNeeded(final List<JsonNode> groupJson)
    {
        final var wcdbHostname = Optional.ofNullable(EnvVars.get("CASSANDRA_HOSTNAME"));

        return wcdbHostname.map(hostname -> groupJson.stream().map(measType ->
        {
            try
            {
                final var measTypeStr = this.om.writeValueAsString(measType).replace("<wcdbcd-deployment-name>", hostname);

                return this.om.readValue(measTypeStr, new TypeReference<JsonNode>()
                {
                });
            }
            catch (final JsonProcessingException ex)
            {
                throw new IllegalArgumentException("Unable to process group.json file", ex);
            }
        }).toList()).orElse(groupJson);

    }

    private List<PatchItem> generateGroupPatches(List<JsonNode> jArray)
    {
        List<PatchItem> patches = new ArrayList<>();

        for (JsonNode jNode : jArray)
        {
            var groupItem = new JsonObject(jNode.toString());
            patches.add(new PatchItem(PatchOperation.ADD, "/ericsson-pm:pm/group/-", "", groupItem));
        }

        return patches;
    }

    private List<PatchItem> generateJobPatches(List<JsonNode> jArray)
    {
        List<PatchItem> patches = new ArrayList<>();

        for (JsonNode jNode : jArray)
        {
            var jobItem = new JsonObject(jNode.toString());
            patches.add(new PatchItem(PatchOperation.ADD, "/ericsson-pm:pm/job/-", "", jobItem));
        }

        return patches;
    }

    public List<PatchItem> getPmbrJobPatches()
    {
        return this.jobItems;
    }

    public List<PatchItem> getPmbrGroupPatches()
    {
        return this.groupItems;
    }

    public static void main(String[] args) throws IOException
    {
        var gen = new ScPmbrConfigGenerator();
        final var result = gen.generateConfig().toPrettyString();
        log.info("Generated configuration\n {}", result);
    }
}
