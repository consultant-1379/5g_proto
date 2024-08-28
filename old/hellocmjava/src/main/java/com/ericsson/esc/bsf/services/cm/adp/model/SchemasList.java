
package com.ericsson.esc.bsf.services.cm.adp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "title"
})
public class SchemasList {

    /**
     * The name of the schema
     * (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the schema")
    private String name;
    /**
     * The title of the schema
     * (Required)
     *
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the schema")
    private String title;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The name of the schema
     * (Required)
     *
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The name of the schema
     * (Required)
     *
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public SchemasList withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The title of the schema
     * (Required)
     *
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The title of the schema
     * (Required)
     *
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    public SchemasList withTitle(String title) {
        this.title = title;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SchemasList withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
