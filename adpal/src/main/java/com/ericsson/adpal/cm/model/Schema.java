
package com.ericsson.adpal.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Schema
 * <p>
 * Definition of a configuration schema
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "title", "jsonSchema" })
public class Schema
{

    /**
     * The name of the schema (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the schema")
    private String name;
    /**
     * The title of the schema (Required)
     *
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the schema")
    private String title;
    /**
     * The JSON schema definition (Required)
     *
     */
    @JsonProperty("jsonSchema")
    @JsonPropertyDescription("The JSON schema definition")
    private JsonSchema jsonSchema;

    /**
     * The name of the schema (Required)
     *
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The name of the schema (Required)
     *
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Schema withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The title of the schema (Required)
     *
     */
    @JsonProperty("title")
    public String getTitle()
    {
        return title;
    }

    /**
     * The title of the schema (Required)
     *
     */
    @JsonProperty("title")
    public void setTitle(String title)
    {
        this.title = title;
    }

    public Schema withTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * The JSON schema definition (Required)
     *
     */
    @JsonProperty("jsonSchema")
    public JsonSchema getJsonSchema()
    {
        return jsonSchema;
    }

    /**
     * The JSON schema definition (Required)
     *
     */
    @JsonProperty("jsonSchema")
    public void setJsonSchema(JsonSchema jsonSchema)
    {
        this.jsonSchema = jsonSchema;
    }

    public Schema withJsonSchema(JsonSchema jsonSchema)
    {
        this.jsonSchema = jsonSchema;
        return this;
    }

}
