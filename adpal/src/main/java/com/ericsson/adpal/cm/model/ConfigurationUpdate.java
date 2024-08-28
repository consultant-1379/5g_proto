
package com.ericsson.adpal.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration Update
 * <p>
 * Configuration data update
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "title", "data", "baseETag" })
public class ConfigurationUpdate
{

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
    @JsonProperty("data")
    @JsonPropertyDescription("The configuration data")
    private Data data;
    /**
     * The ETag value of the configuration this change is based upon
     *
     */
    @JsonProperty("baseETag")
    @JsonPropertyDescription("The ETag value of the configuration this change is based upon")
    private String baseETag;

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

    public ConfigurationUpdate withTitle(String title)
    {
        this.title = title;
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

    public ConfigurationUpdate withData(Data data)
    {
        this.data = data;
        return this;
    }

    /**
     * The ETag value of the configuration this change is based upon
     *
     */
    @JsonProperty("baseETag")
    public String getBaseETag()
    {
        return baseETag;
    }

    /**
     * The ETag value of the configuration this change is based upon
     *
     */
    @JsonProperty("baseETag")
    public void setBaseETag(String baseETag)
    {
        this.baseETag = baseETag;
    }

    public ConfigurationUpdate withBaseETag(String baseETag)
    {
        this.baseETag = baseETag;
        return this;
    }

}
