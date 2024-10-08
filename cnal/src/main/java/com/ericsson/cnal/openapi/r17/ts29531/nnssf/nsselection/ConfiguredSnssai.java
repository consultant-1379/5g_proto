/*
 * NSSF NS Selection
 * NSSF Network Slice Selection Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29531.nnssf.nsselection;

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
 * Contains the configured S-NSSAI(s) authorized by the NSSF in the serving PLMN
 * and optional mapped home S-NSSAI
 */
@ApiModel(description = "Contains the configured S-NSSAI(s) authorized by the NSSF in the serving PLMN and optional mapped home S-NSSAI")
@JsonPropertyOrder({ ConfiguredSnssai.JSON_PROPERTY_CONFIGURED_SNSSAI, ConfiguredSnssai.JSON_PROPERTY_MAPPED_HOME_SNSSAI })
public class ConfiguredSnssai
{
    public static final String JSON_PROPERTY_CONFIGURED_SNSSAI = "configuredSnssai";
    private Snssai configuredSnssai;

    public static final String JSON_PROPERTY_MAPPED_HOME_SNSSAI = "mappedHomeSnssai";
    private Snssai mappedHomeSnssai;

    public ConfiguredSnssai()
    {
    }

    public ConfiguredSnssai configuredSnssai(Snssai configuredSnssai)
    {

        this.configuredSnssai = configuredSnssai;
        return this;
    }

    /**
     * Get configuredSnssai
     * 
     * @return configuredSnssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONFIGURED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getConfiguredSnssai()
    {
        return configuredSnssai;
    }

    @JsonProperty(JSON_PROPERTY_CONFIGURED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setConfiguredSnssai(Snssai configuredSnssai)
    {
        this.configuredSnssai = configuredSnssai;
    }

    public ConfiguredSnssai mappedHomeSnssai(Snssai mappedHomeSnssai)
    {

        this.mappedHomeSnssai = mappedHomeSnssai;
        return this;
    }

    /**
     * Get mappedHomeSnssai
     * 
     * @return mappedHomeSnssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MAPPED_HOME_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getMappedHomeSnssai()
    {
        return mappedHomeSnssai;
    }

    @JsonProperty(JSON_PROPERTY_MAPPED_HOME_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMappedHomeSnssai(Snssai mappedHomeSnssai)
    {
        this.mappedHomeSnssai = mappedHomeSnssai;
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
        ConfiguredSnssai configuredSnssai = (ConfiguredSnssai) o;
        return Objects.equals(this.configuredSnssai, configuredSnssai.configuredSnssai)
               && Objects.equals(this.mappedHomeSnssai, configuredSnssai.mappedHomeSnssai);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(configuredSnssai, mappedHomeSnssai);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConfiguredSnssai {\n");
        sb.append("    configuredSnssai: ").append(toIndentedString(configuredSnssai)).append("\n");
        sb.append("    mappedHomeSnssai: ").append(toIndentedString(mappedHomeSnssai)).append("\n");
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
