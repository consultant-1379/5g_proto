/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.RefToBinaryData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Error within Update Response from H-SMF, or from SMF to I-SMF
 */
@ApiModel(description = "Error within Update Response from H-SMF, or from SMF to I-SMF")
@JsonPropertyOrder({ HsmfUpdateError.JSON_PROPERTY_ERROR,
                     HsmfUpdateError.JSON_PROPERTY_PTI,
                     HsmfUpdateError.JSON_PROPERTY_N1SM_CAUSE,
                     HsmfUpdateError.JSON_PROPERTY_N1_SM_INFO_TO_UE,
                     HsmfUpdateError.JSON_PROPERTY_BACK_OFF_TIMER,
                     HsmfUpdateError.JSON_PROPERTY_RECOVERY_TIME })
public class HsmfUpdateError
{
    public static final String JSON_PROPERTY_ERROR = "error";
    private ProblemDetails error;

    public static final String JSON_PROPERTY_PTI = "pti";
    private Integer pti;

    public static final String JSON_PROPERTY_N1SM_CAUSE = "n1smCause";
    private String n1smCause;

    public static final String JSON_PROPERTY_N1_SM_INFO_TO_UE = "n1SmInfoToUe";
    private RefToBinaryData n1SmInfoToUe;

    public static final String JSON_PROPERTY_BACK_OFF_TIMER = "backOffTimer";
    private Integer backOffTimer;

    public static final String JSON_PROPERTY_RECOVERY_TIME = "recoveryTime";
    private OffsetDateTime recoveryTime;

    public HsmfUpdateError()
    {
    }

    public HsmfUpdateError error(ProblemDetails error)
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

    public HsmfUpdateError pti(Integer pti)
    {

        this.pti = pti;
        return this;
    }

    /**
     * Procedure Transaction Identifier minimum: 0 maximum: 255
     * 
     * @return pti
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Procedure Transaction Identifier")
    @JsonProperty(JSON_PROPERTY_PTI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPti()
    {
        return pti;
    }

    @JsonProperty(JSON_PROPERTY_PTI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPti(Integer pti)
    {
        this.pti = pti;
    }

    public HsmfUpdateError n1smCause(String n1smCause)
    {

        this.n1smCause = n1smCause;
        return this;
    }

    /**
     * Get n1smCause
     * 
     * @return n1smCause
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N1SM_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getN1smCause()
    {
        return n1smCause;
    }

    @JsonProperty(JSON_PROPERTY_N1SM_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN1smCause(String n1smCause)
    {
        this.n1smCause = n1smCause;
    }

    public HsmfUpdateError n1SmInfoToUe(RefToBinaryData n1SmInfoToUe)
    {

        this.n1SmInfoToUe = n1SmInfoToUe;
        return this;
    }

    /**
     * Get n1SmInfoToUe
     * 
     * @return n1SmInfoToUe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N1_SM_INFO_TO_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RefToBinaryData getN1SmInfoToUe()
    {
        return n1SmInfoToUe;
    }

    @JsonProperty(JSON_PROPERTY_N1_SM_INFO_TO_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN1SmInfoToUe(RefToBinaryData n1SmInfoToUe)
    {
        this.n1SmInfoToUe = n1SmInfoToUe;
    }

    public HsmfUpdateError backOffTimer(Integer backOffTimer)
    {

        this.backOffTimer = backOffTimer;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return backOffTimer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_BACK_OFF_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBackOffTimer()
    {
        return backOffTimer;
    }

    @JsonProperty(JSON_PROPERTY_BACK_OFF_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackOffTimer(Integer backOffTimer)
    {
        this.backOffTimer = backOffTimer;
    }

    public HsmfUpdateError recoveryTime(OffsetDateTime recoveryTime)
    {

        this.recoveryTime = recoveryTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return recoveryTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_RECOVERY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRecoveryTime()
    {
        return recoveryTime;
    }

    @JsonProperty(JSON_PROPERTY_RECOVERY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecoveryTime(OffsetDateTime recoveryTime)
    {
        this.recoveryTime = recoveryTime;
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
        HsmfUpdateError hsmfUpdateError = (HsmfUpdateError) o;
        return Objects.equals(this.error, hsmfUpdateError.error) && Objects.equals(this.pti, hsmfUpdateError.pti)
               && Objects.equals(this.n1smCause, hsmfUpdateError.n1smCause) && Objects.equals(this.n1SmInfoToUe, hsmfUpdateError.n1SmInfoToUe)
               && Objects.equals(this.backOffTimer, hsmfUpdateError.backOffTimer) && Objects.equals(this.recoveryTime, hsmfUpdateError.recoveryTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(error, pti, n1smCause, n1SmInfoToUe, backOffTimer, recoveryTime);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class HsmfUpdateError {\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    pti: ").append(toIndentedString(pti)).append("\n");
        sb.append("    n1smCause: ").append(toIndentedString(n1smCause)).append("\n");
        sb.append("    n1SmInfoToUe: ").append(toIndentedString(n1SmInfoToUe)).append("\n");
        sb.append("    backOffTimer: ").append(toIndentedString(backOffTimer)).append("\n");
        sb.append("    recoveryTime: ").append(toIndentedString(recoveryTime)).append("\n");
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
