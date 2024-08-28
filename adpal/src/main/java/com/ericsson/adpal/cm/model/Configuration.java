package com.ericsson.adpal.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration
 * <p>
 * Configuration data
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "title", "baseETag", "data" })
public class Configuration
{

    /**
     * The name of the configuration (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the configuration")
    private String name;
    /**
     * The title of the configuration (Required)
     *
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the configuration")
    private String title;
    /**
     * The configuration data (Required)
     *
     */
    @JsonProperty("baseETag")
    @JsonPropertyDescription("The base ETag attribute")
    private String baseETag;

    @JsonProperty("data")
    @JsonPropertyDescription("The configuration data")
    private Data data;

    /**
     * The name of the configuration (Required)
     *
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The name of the configuration (Required)
     *
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Configuration withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The title of the configuration (Required)
     *
     */
    @JsonProperty("title")
    public String getTitle()
    {
        return title;
    }

    /**
     * The title of the configuration (Required)
     *
     */
    @JsonProperty("title")
    public void setTitle(String title)
    {
        this.title = title;
    }

    public Configuration withTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * 
     * The baseETag attribute (Optional)
     */
    @JsonProperty("baseETag")
    public String getBaseETag()
    {
        return this.baseETag;
    }

    /**
     * 
     * The baseETag attribute (Optional)
     */
    @JsonProperty("baseETag")
    public void setBaseETag(String baseETag)
    {
        this.baseETag = baseETag;
    }

    public Configuration withBaseETag(String baseETag)
    {
        this.baseETag = baseETag;
        return this;
    }

    /**
     * The configuration data (Required)
     *
     */
    @JsonProperty("data")
    public Data getData()
    {
        return data;
    }

    /**
     * The configuration data (Required)
     *
     */
    @JsonProperty("data")
    public void setData(Data data)
    {
        this.data = data;
    }

    public Configuration withData(Data data)
    {
        this.data = data;
        return this;
    }

}
