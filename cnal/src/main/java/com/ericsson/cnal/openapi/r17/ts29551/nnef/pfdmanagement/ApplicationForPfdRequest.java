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
 * Contains the application identifier(s) for the PFD(s) request.
 */
@ApiModel(description = "Contains the application identifier(s) for the PFD(s) request.")
@JsonPropertyOrder({ ApplicationForPfdRequest.JSON_PROPERTY_APPLICATION_ID, ApplicationForPfdRequest.JSON_PROPERTY_PFD_TIMESTAMP })
public class ApplicationForPfdRequest
{
    public static final String JSON_PROPERTY_APPLICATION_ID = "applicationId";
    private String applicationId;

    public static final String JSON_PROPERTY_PFD_TIMESTAMP = "pfdTimestamp";
    private OffsetDateTime pfdTimestamp;

    public ApplicationForPfdRequest()
    {
    }

    public ApplicationForPfdRequest applicationId(String applicationId)
    {

        this.applicationId = applicationId;
        return this;
    }

    /**
     * String providing an application identifier.
     * 
     * @return applicationId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an application identifier.")
    @JsonProperty(JSON_PROPERTY_APPLICATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getApplicationId()
    {
        return applicationId;
    }

    @JsonProperty(JSON_PROPERTY_APPLICATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setApplicationId(String applicationId)
    {
        this.applicationId = applicationId;
    }

    public ApplicationForPfdRequest pfdTimestamp(OffsetDateTime pfdTimestamp)
    {

        this.pfdTimestamp = pfdTimestamp;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return pfdTimestamp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_PFD_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getPfdTimestamp()
    {
        return pfdTimestamp;
    }

    @JsonProperty(JSON_PROPERTY_PFD_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPfdTimestamp(OffsetDateTime pfdTimestamp)
    {
        this.pfdTimestamp = pfdTimestamp;
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
        ApplicationForPfdRequest applicationForPfdRequest = (ApplicationForPfdRequest) o;
        return Objects.equals(this.applicationId, applicationForPfdRequest.applicationId)
               && Objects.equals(this.pfdTimestamp, applicationForPfdRequest.pfdTimestamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(applicationId, pfdTimestamp);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationForPfdRequest {\n");
        sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
        sb.append("    pfdTimestamp: ").append(toIndentedString(pfdTimestamp)).append("\n");
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
