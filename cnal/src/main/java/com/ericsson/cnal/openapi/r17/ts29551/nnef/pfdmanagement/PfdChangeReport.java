/*
 * Nnef_PFDmanagement Service API
 * Packet Flow Description Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29551.nnef.pfdmanagement;

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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents an error report on PFD change.
 */
@ApiModel(description = "Represents an error report on PFD change.")
@JsonPropertyOrder({ PfdChangeReport.JSON_PROPERTY_PFD_ERROR, PfdChangeReport.JSON_PROPERTY_APPLICATION_ID })
public class PfdChangeReport
{
    public static final String JSON_PROPERTY_PFD_ERROR = "pfdError";
    private ProblemDetails pfdError;

    public static final String JSON_PROPERTY_APPLICATION_ID = "applicationId";
    private List<String> applicationId = new ArrayList<>();

    public PfdChangeReport()
    {
    }

    public PfdChangeReport pfdError(ProblemDetails pfdError)
    {

        this.pfdError = pfdError;
        return this;
    }

    /**
     * Get pfdError
     * 
     * @return pfdError
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PFD_ERROR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public ProblemDetails getPfdError()
    {
        return pfdError;
    }

    @JsonProperty(JSON_PROPERTY_PFD_ERROR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPfdError(ProblemDetails pfdError)
    {
        this.pfdError = pfdError;
    }

    public PfdChangeReport applicationId(List<String> applicationId)
    {

        this.applicationId = applicationId;
        return this;
    }

    public PfdChangeReport addApplicationIdItem(String applicationIdItem)
    {
        this.applicationId.add(applicationIdItem);
        return this;
    }

    /**
     * Get applicationId
     * 
     * @return applicationId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_APPLICATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getApplicationId()
    {
        return applicationId;
    }

    @JsonProperty(JSON_PROPERTY_APPLICATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setApplicationId(List<String> applicationId)
    {
        this.applicationId = applicationId;
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
        PfdChangeReport pfdChangeReport = (PfdChangeReport) o;
        return Objects.equals(this.pfdError, pfdChangeReport.pfdError) && Objects.equals(this.applicationId, pfdChangeReport.applicationId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pfdError, applicationId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PfdChangeReport {\n");
        sb.append("    pfdError: ").append(toIndentedString(pfdError)).append("\n");
        sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
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
