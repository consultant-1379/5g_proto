package com.ericsson.sc.pm;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PmbrConfigBuilder
{
    private final ObjectMapper om = Jackson.om();
    List<JsonNode> jobArray = new ArrayList<>();
    List<JsonNode> groupArray = new ArrayList<>();

    public PmbrConfigBuilder()
    {
    }

    public PmbrConfigBuilder addJobs(List<JsonNode> jobs)
    {
        this.jobArray.addAll(jobs);
        return this;
    }

    public PmbrConfigBuilder addGroups(List<JsonNode> groups)
    {
        this.groupArray.addAll(groups);
        return this;
    }

    public JsonNode build()
    {
        Jackson.om().createArrayNode().addAll(jobArray);
        final var ericssonPm = om.createObjectNode();
        ericssonPm.set("job", om.createArrayNode().addAll(jobArray));
        ericssonPm.set("group", om.createArrayNode().addAll(groupArray));

        return om.createObjectNode().set("ericsson-pm:pm", ericssonPm);
    }
}
