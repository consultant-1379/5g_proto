
package com.ericsson.esc.bsf.services.cm.adp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Schema
 * <p>
 * Configuration schema update
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "jsonSchema"
})
public class SchemaUpdate {

    /**
     * The title of the schema
     * (Required)
     *
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the schema")
    private String title;
    /**
     * The JSON schema definition
     *
     */
    @JsonProperty("jsonSchema")
    @JsonPropertyDescription("The JSON schema definition")
    private JsonSchema jsonSchema;

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

    public SchemaUpdate withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * The JSON schema definition
     *
     */
    @JsonProperty("jsonSchema")
    public JsonSchema getJsonSchema() {
        return jsonSchema;
    }

    /**
     * The JSON schema definition
     *
     */
    @JsonProperty("jsonSchema")
    public void setJsonSchema(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public SchemaUpdate withJsonSchema(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

}
