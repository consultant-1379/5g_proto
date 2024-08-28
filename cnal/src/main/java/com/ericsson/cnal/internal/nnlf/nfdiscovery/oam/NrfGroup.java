/*
 * NLF O&M Service
 * NLF Operation & Maintenance Service.
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.internal.nnlf.nfdiscovery.oam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Group of NRFs for redundancy.
 */
@ApiModel(description = "Group of NRFs for redundancy.")
@JsonPropertyOrder({ NrfGroup.JSON_PROPERTY_NAME, NrfGroup.JSON_PROPERTY_SOURCE, NrfGroup.JSON_PROPERTY_PATH, NrfGroup.JSON_PROPERTY_NRF })
@JsonTypeName("NrfGroup")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-07-26T08:47:10.100164+02:00[Europe/Berlin]")
public class NrfGroup
{
    public static final String JSON_PROPERTY_NAME = "name";
    private String name;

    public static final String JSON_PROPERTY_SOURCE = "source";
    private String source;

    public static final String JSON_PROPERTY_PATH = "path";
    private String path;

    public static final String JSON_PROPERTY_NRF = "nrf";
    private List<Nrf> nrf = null;

    public NrfGroup()
    {
    }

    public NrfGroup name(String name)
    {

        this.name = name;
        return this;
    }

    /**
     * Name identifying the NRF group.
     * 
     * @return name
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Name identifying the NRF group.")
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getName()
    {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setName(String name)
    {
        this.name = name;
    }

    public NrfGroup source(String source)
    {

        this.source = source;
        return this;
    }

    /**
     * Name of the source configuration. Example: 'ericsson-scp'.
     * 
     * @return source
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Name of the source configuration. Example: 'ericsson-scp'.")
    @JsonProperty(JSON_PROPERTY_SOURCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSource()
    {
        return source;
    }

    @JsonProperty(JSON_PROPERTY_SOURCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSource(String source)
    {
        this.source = source;
    }

    public NrfGroup path(String path)
    {

        this.path = path;
        return this;
    }

    /**
     * Distinguished name (parent first) to the NRF group in the source
     * configuration. Example: 'nf=scp-function,nf-instance=instance-1'.
     * 
     * @return path
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Distinguished name (parent first) to the NRF group in the source configuration. Example: 'nf=scp-function,nf-instance=instance-1'.")
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getPath()
    {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPath(String path)
    {
        this.path = path;
    }

    public NrfGroup nrf(List<Nrf> nrf)
    {

        this.nrf = nrf;
        return this;
    }

    public NrfGroup addNrfItem(Nrf nrfItem)
    {
        if (this.nrf == null)
        {
            this.nrf = new ArrayList<>();
        }
        this.nrf.add(nrfItem);
        return this;
    }

    /**
     * List of NRFs amongst which failover can take place.
     * 
     * @return nrf
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "List of NRFs amongst which failover can take place.")
    @JsonProperty(JSON_PROPERTY_NRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Nrf> getNrf()
    {
        return nrf;
    }

    @JsonProperty(JSON_PROPERTY_NRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNrf(List<Nrf> nrf)
    {
        this.nrf = nrf;
    }

    @JsonIgnore
    public int getId()
    {
        // Consider both path and name for the ID generation. name alone is not enough.
        return Objects.hash(this.getPath(), this.getName());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        NrfGroup nrfGroup = (NrfGroup) o;
        return Objects.equals(this.name, nrfGroup.name) && Objects.equals(this.source, nrfGroup.source) && Objects.equals(this.path, nrfGroup.path)
               && Objects.equals(this.nrf, nrfGroup.nrf);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, source, path, nrf);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NrfGroup {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    nrf: ").append(toIndentedString(nrf)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o)
    {
        if (o == null)
        {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
