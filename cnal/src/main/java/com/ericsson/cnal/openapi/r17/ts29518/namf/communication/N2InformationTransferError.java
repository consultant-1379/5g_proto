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
 * Data within a failure response for a non-UE related N2 Information Transfer
 */
@ApiModel(description = "Data within a failure response for a non-UE related N2 Information Transfer")
@JsonPropertyOrder({ N2InformationTransferError.JSON_PROPERTY_ERROR, N2InformationTransferError.JSON_PROPERTY_PWS_ERROR_INFO })
public class N2InformationTransferError
{
    public static final String JSON_PROPERTY_ERROR = "error";
    private ProblemDetails error;

    public static final String JSON_PROPERTY_PWS_ERROR_INFO = "pwsErrorInfo";
    private PWSErrorData pwsErrorInfo;

    public N2InformationTransferError()
    {
    }

    public N2InformationTransferError error(ProblemDetails error)
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

    public N2InformationTransferError pwsErrorInfo(PWSErrorData pwsErrorInfo)
    {

        this.pwsErrorInfo = pwsErrorInfo;
        return this;
    }

    /**
     * Get pwsErrorInfo
     * 
     * @return pwsErrorInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PWS_ERROR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PWSErrorData getPwsErrorInfo()
    {
        return pwsErrorInfo;
    }

    @JsonProperty(JSON_PROPERTY_PWS_ERROR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPwsErrorInfo(PWSErrorData pwsErrorInfo)
    {
        this.pwsErrorInfo = pwsErrorInfo;
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
        N2InformationTransferError n2InformationTransferError = (N2InformationTransferError) o;
        return Objects.equals(this.error, n2InformationTransferError.error) && Objects.equals(this.pwsErrorInfo, n2InformationTransferError.pwsErrorInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(error, pwsErrorInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class N2InformationTransferError {\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    pwsErrorInfo: ").append(toIndentedString(pwsErrorInfo)).append("\n");
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
