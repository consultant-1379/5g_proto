/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents the mapping between a S-NSSAI in serving PLMN to a S-NSSAI in home
 * PLMN
 */
@ApiModel(description = "Represents the mapping between a S-NSSAI in serving PLMN to a S-NSSAI in home PLMN")
@JsonPropertyOrder({ NssaiMapping.JSON_PROPERTY_MAPPED_SNSSAI, NssaiMapping.JSON_PROPERTY_H_SNSSAI })
public class NssaiMapping
{
    public static final String JSON_PROPERTY_MAPPED_SNSSAI = "mappedSnssai";
    private Snssai mappedSnssai;

    public static final String JSON_PROPERTY_H_SNSSAI = "hSnssai";
    private Snssai hSnssai;

    public NssaiMapping()
    {
    }

    public NssaiMapping mappedSnssai(Snssai mappedSnssai)
    {

        this.mappedSnssai = mappedSnssai;
        return this;
    }

    /**
     * Get mappedSnssai
     * 
     * @return mappedSnssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MAPPED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getMappedSnssai()
    {
        return mappedSnssai;
    }

    @JsonProperty(JSON_PROPERTY_MAPPED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMappedSnssai(Snssai mappedSnssai)
    {
        this.mappedSnssai = mappedSnssai;
    }

    public NssaiMapping hSnssai(Snssai hSnssai)
    {

        this.hSnssai = hSnssai;
        return this;
    }

    /**
     * Get hSnssai
     * 
     * @return hSnssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_H_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai gethSnssai()
    {
        return hSnssai;
    }

    @JsonProperty(JSON_PROPERTY_H_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void sethSnssai(Snssai hSnssai)
    {
        this.hSnssai = hSnssai;
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
        NssaiMapping nssaiMapping = (NssaiMapping) o;
        return Objects.equals(this.mappedSnssai, nssaiMapping.mappedSnssai) && Objects.equals(this.hSnssai, nssaiMapping.hSnssai);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mappedSnssai, hSnssai);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NssaiMapping {\n");
        sb.append("    mappedSnssai: ").append(toIndentedString(mappedSnssai)).append("\n");
        sb.append("    hSnssai: ").append(toIndentedString(hSnssai)).append("\n");
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
