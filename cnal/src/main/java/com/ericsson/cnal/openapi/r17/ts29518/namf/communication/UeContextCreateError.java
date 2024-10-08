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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.NgApCause;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
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
 * Data within a failure response for creating a UE context
 */
@ApiModel(description = "Data within a failure response for creating a UE context")
@JsonPropertyOrder({ UeContextCreateError.JSON_PROPERTY_ERROR,
                     UeContextCreateError.JSON_PROPERTY_NGAP_CAUSE,
                     UeContextCreateError.JSON_PROPERTY_TARGET_TO_SOURCE_FAILURE_DATA })
public class UeContextCreateError
{
    public static final String JSON_PROPERTY_ERROR = "error";
    private ProblemDetails error;

    public static final String JSON_PROPERTY_NGAP_CAUSE = "ngapCause";
    private NgApCause ngapCause;

    public static final String JSON_PROPERTY_TARGET_TO_SOURCE_FAILURE_DATA = "targetToSourceFailureData";
    private N2InfoContent targetToSourceFailureData;

    public UeContextCreateError()
    {
    }

    public UeContextCreateError error(ProblemDetails error)
    {

        this.error = error;
        return this;
    }

    /**
     * Get error
     * 
     * @return error
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ERROR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public ProblemDetails getError()
    {
        return error;
    }

    @JsonProperty(JSON_PROPERTY_ERROR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setError(ProblemDetails error)
    {
        this.error = error;
    }

    public UeContextCreateError ngapCause(NgApCause ngapCause)
    {

        this.ngapCause = ngapCause;
        return this;
    }

    /**
     * Get ngapCause
     * 
     * @return ngapCause
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NGAP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NgApCause getNgapCause()
    {
        return ngapCause;
    }

    @JsonProperty(JSON_PROPERTY_NGAP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNgapCause(NgApCause ngapCause)
    {
        this.ngapCause = ngapCause;
    }

    public UeContextCreateError targetToSourceFailureData(N2InfoContent targetToSourceFailureData)
    {

        this.targetToSourceFailureData = targetToSourceFailureData;
        return this;
    }

    /**
     * Get targetToSourceFailureData
     * 
     * @return targetToSourceFailureData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TARGET_TO_SOURCE_FAILURE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N2InfoContent getTargetToSourceFailureData()
    {
        return targetToSourceFailureData;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_TO_SOURCE_FAILURE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetToSourceFailureData(N2InfoContent targetToSourceFailureData)
    {
        this.targetToSourceFailureData = targetToSourceFailureData;
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
        UeContextCreateError ueContextCreateError = (UeContextCreateError) o;
        return Objects.equals(this.error, ueContextCreateError.error) && Objects.equals(this.ngapCause, ueContextCreateError.ngapCause)
               && Objects.equals(this.targetToSourceFailureData, ueContextCreateError.targetToSourceFailureData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(error, ngapCause, targetToSourceFailureData);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeContextCreateError {\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    ngapCause: ").append(toIndentedString(ngapCause)).append("\n");
        sb.append("    targetToSourceFailureData: ").append(toIndentedString(targetToSourceFailureData)).append("\n");
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
