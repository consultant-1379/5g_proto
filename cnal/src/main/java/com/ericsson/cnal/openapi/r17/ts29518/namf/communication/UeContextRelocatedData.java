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
 * Data within a Relocate UE Context response
 */
@ApiModel(description = "Data within a Relocate UE Context response")
@JsonPropertyOrder({ UeContextRelocatedData.JSON_PROPERTY_UE_CONTEXT })
public class UeContextRelocatedData
{
    public static final String JSON_PROPERTY_UE_CONTEXT = "ueContext";
    private UeContext ueContext;

    public UeContextRelocatedData()
    {
    }

    public UeContextRelocatedData ueContext(UeContext ueContext)
    {

        this.ueContext = ueContext;
        return this;
    }

    /**
     * Get ueContext
     * 
     * @return ueContext
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_UE_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UeContext getUeContext()
    {
        return ueContext;
    }

    @JsonProperty(JSON_PROPERTY_UE_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setUeContext(UeContext ueContext)
    {
        this.ueContext = ueContext;
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
        UeContextRelocatedData ueContextRelocatedData = (UeContextRelocatedData) o;
        return Objects.equals(this.ueContext, ueContextRelocatedData.ueContext);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ueContext);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeContextRelocatedData {\n");
        sb.append("    ueContext: ").append(toIndentedString(ueContext)).append("\n");
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
