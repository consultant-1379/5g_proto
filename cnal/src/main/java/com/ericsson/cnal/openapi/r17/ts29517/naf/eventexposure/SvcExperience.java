/*
 * Naf_EventExposure
 * AF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29517.naf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
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
 * Contains a mean opinion score with the customized range.
 */
@ApiModel(description = "Contains a mean opinion score with the customized range.")
@JsonPropertyOrder({ SvcExperience.JSON_PROPERTY_MOS, SvcExperience.JSON_PROPERTY_UPPER_RANGE, SvcExperience.JSON_PROPERTY_LOWER_RANGE })
public class SvcExperience
{
    public static final String JSON_PROPERTY_MOS = "mos";
    private Float mos;

    public static final String JSON_PROPERTY_UPPER_RANGE = "upperRange";
    private Float upperRange;

    public static final String JSON_PROPERTY_LOWER_RANGE = "lowerRange";
    private Float lowerRange;

    public SvcExperience()
    {
    }

    public SvcExperience mos(Float mos)
    {

        this.mos = mos;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return mos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_MOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getMos()
    {
        return mos;
    }

    @JsonProperty(JSON_PROPERTY_MOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMos(Float mos)
    {
        this.mos = mos;
    }

    public SvcExperience upperRange(Float upperRange)
    {

        this.upperRange = upperRange;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return upperRange
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_UPPER_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getUpperRange()
    {
        return upperRange;
    }

    @JsonProperty(JSON_PROPERTY_UPPER_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpperRange(Float upperRange)
    {
        this.upperRange = upperRange;
    }

    public SvcExperience lowerRange(Float lowerRange)
    {

        this.lowerRange = lowerRange;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return lowerRange
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_LOWER_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getLowerRange()
    {
        return lowerRange;
    }

    @JsonProperty(JSON_PROPERTY_LOWER_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLowerRange(Float lowerRange)
    {
        this.lowerRange = lowerRange;
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
        SvcExperience svcExperience = (SvcExperience) o;
        return Objects.equals(this.mos, svcExperience.mos) && Objects.equals(this.upperRange, svcExperience.upperRange)
               && Objects.equals(this.lowerRange, svcExperience.lowerRange);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mos, upperRange, lowerRange);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SvcExperience {\n");
        sb.append("    mos: ").append(toIndentedString(mos)).append("\n");
        sb.append("    upperRange: ").append(toIndentedString(upperRange)).append("\n");
        sb.append("    lowerRange: ").append(toIndentedString(lowerRange)).append("\n");
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
