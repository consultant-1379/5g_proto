
package com.ericsson.esc.bsf.services.cm.adp.model;

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
@JsonPropertyOrder({
    "name",
    "title",
    "data"
})
public class Configuration {

    /**
     * The name of the configuration
     * (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the configuration")
    private String name;
    /**
     * The title of the configuration
     * (Required)
     *
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the configuration")
    private String title;
    /**
     * The configuration data
     * (Required)
     *
     */
    @JsonProperty("data")
    @JsonPropertyDescription("The configuration data")
    private Data data;

    /**
     * The name of the configuration
     * (Required)
     *
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The name of the configuration
     * (Required)
     *
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Configuration withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The title of the configuration
     * (Required)
     *
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The title of the configuration
     * (Required)
     *
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    public Configuration withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * The configuration data
     * (Required)
     *
     */
    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    /**
     * The configuration data
     * (Required)
     *
     */
    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }

    public Configuration withData(Data data) {
        this.data = data;
        return this;
    }

}
