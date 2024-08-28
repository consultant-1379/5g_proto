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
 * Data related to PWS error included in a N2 Information Transfer failure
 * response
 */
@ApiModel(description = "Data related to PWS error included in a N2 Information Transfer failure response")
@JsonPropertyOrder({ PWSErrorData.JSON_PROPERTY_NAMF_CAUSE })
public class PWSErrorData
{
    public static final String JSON_PROPERTY_NAMF_CAUSE = "namfCause";
    private Integer namfCause;

    public PWSErrorData()
    {
    }

    public PWSErrorData namfCause(Integer namfCause)
    {

        this.namfCause = namfCause;
        return this;
    }

    /**
     * Get namfCause
     * 
     * @return namfCause
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NAMF_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getNamfCause()
    {
        return namfCause;
    }

    @JsonProperty(JSON_PROPERTY_NAMF_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNamfCause(Integer namfCause)
    {
        this.namfCause = namfCause;
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
        PWSErrorData pwSErrorData = (PWSErrorData) o;
        return Objects.equals(this.namfCause, pwSErrorData.namfCause);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(namfCause);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PWSErrorData {\n");
        sb.append("    namfCause: ").append(toIndentedString(namfCause)).append("\n");
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
