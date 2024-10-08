/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains load level information applicable for one or several slices.
 */
@ApiModel(description = "Contains load level information applicable for one or several slices.")
@JsonPropertyOrder({ SliceLoadLevelInformation.JSON_PROPERTY_LOAD_LEVEL_INFORMATION, SliceLoadLevelInformation.JSON_PROPERTY_SNSSAIS })
public class SliceLoadLevelInformation
{
    public static final String JSON_PROPERTY_LOAD_LEVEL_INFORMATION = "loadLevelInformation";
    private Integer loadLevelInformation;

    public static final String JSON_PROPERTY_SNSSAIS = "snssais";
    private List<Snssai> snssais = new ArrayList<>();

    public SliceLoadLevelInformation()
    {
    }

    public SliceLoadLevelInformation loadLevelInformation(Integer loadLevelInformation)
    {

        this.loadLevelInformation = loadLevelInformation;
        return this;
    }

    /**
     * Load level information of the network slice and the optionally associated
     * network slice instance.
     * 
     * @return loadLevelInformation
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Load level information of the network slice and the optionally associated network slice  instance. ")
    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getLoadLevelInformation()
    {
        return loadLevelInformation;
    }

    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLoadLevelInformation(Integer loadLevelInformation)
    {
        this.loadLevelInformation = loadLevelInformation;
    }

    public SliceLoadLevelInformation snssais(List<Snssai> snssais)
    {

        this.snssais = snssais;
        return this;
    }

    public SliceLoadLevelInformation addSnssaisItem(Snssai snssaisItem)
    {
        this.snssais.add(snssaisItem);
        return this;
    }

    /**
     * Identification(s) of network slice to which the subscription applies.
     * 
     * @return snssais
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Identification(s) of network slice to which the subscription applies.")
    @JsonProperty(JSON_PROPERTY_SNSSAIS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Snssai> getSnssais()
    {
        return snssais;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAIS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSnssais(List<Snssai> snssais)
    {
        this.snssais = snssais;
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
        SliceLoadLevelInformation sliceLoadLevelInformation = (SliceLoadLevelInformation) o;
        return Objects.equals(this.loadLevelInformation, sliceLoadLevelInformation.loadLevelInformation)
               && Objects.equals(this.snssais, sliceLoadLevelInformation.snssais);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(loadLevelInformation, snssais);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SliceLoadLevelInformation {\n");
        sb.append("    loadLevelInformation: ").append(toIndentedString(loadLevelInformation)).append("\n");
        sb.append("    snssais: ").append(toIndentedString(snssais)).append("\n");
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
