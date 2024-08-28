
package com.ericsson.adpal.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Schema
 * <p>
 * Definition of configuration schema get
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "title", "jsonSchema", "yangArchiveMd5" })
public class SchemaGet
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
     * The YANG archive md5 checksum in hexadecimal format
     *
     */
    @JsonProperty("yangArchiveMd5")
    @JsonPropertyDescription("The YANG archive md5 checksum in hexadecimal format")
    private String yangArchiveMd5;

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

    public SchemaGet withName(String name)
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

    public SchemaGet withTitle(String title)
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

    public SchemaGet withJsonSchema(JsonSchema jsonSchema)
    {
        this.jsonSchema = jsonSchema;
        return this;
    }

    /**
     * The YANG archive md5 checksum in hexadecimal format
     *
     */
    @JsonProperty("yangArchiveMd5")
    public String getYangArchiveMd5()
    {
        return yangArchiveMd5;
    }

    /**
     * The YANG archive md5 checksum in hexadecimal format
     *
     */
    @JsonProperty("yangArchiveMd5")
    public void setYangArchiveMd5(String yangArchiveMd5)
    {
        this.yangArchiveMd5 = yangArchiveMd5;
    }

    public SchemaGet withYangArchiveMd5(String yangArchiveMd5)
    {
        this.yangArchiveMd5 = yangArchiveMd5;
        return this;
    }

}
