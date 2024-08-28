/*
 * N32 Handshake API
 * N32-c Handshake Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29573.n32.handshake;

import java.util.Objects;
import java.util.Arrays;

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
 * N32-f error information
 */
@ApiModel(description = "N32-f error information")
@JsonPropertyOrder({ N32fErrorInfo.JSON_PROPERTY_N32F_MESSAGE_ID,
                     N32fErrorInfo.JSON_PROPERTY_N32F_ERROR_TYPE,
                     N32fErrorInfo.JSON_PROPERTY_N32F_CONTEXT_ID,
                     N32fErrorInfo.JSON_PROPERTY_FAILED_MODIFICATION_LIST,
                     N32fErrorInfo.JSON_PROPERTY_ERROR_DETAILS_LIST })
public class N32fErrorInfo
{
    public static final String JSON_PROPERTY_N32F_MESSAGE_ID = "n32fMessageId";
    private String n32fMessageId;

    public static final String JSON_PROPERTY_N32F_ERROR_TYPE = "n32fErrorType";
    private String n32fErrorType;

    public static final String JSON_PROPERTY_N32F_CONTEXT_ID = "n32fContextId";
    private String n32fContextId;

    public static final String JSON_PROPERTY_FAILED_MODIFICATION_LIST = "failedModificationList";
    private List<FailedModificationInfo> failedModificationList = null;

    public static final String JSON_PROPERTY_ERROR_DETAILS_LIST = "errorDetailsList";
    private List<N32fErrorDetail> errorDetailsList = null;

    public N32fErrorInfo()
    {
    }

    public N32fErrorInfo n32fMessageId(String n32fMessageId)
    {

        this.n32fMessageId = n32fMessageId;
        return this;
    }

    /**
     * Get n32fMessageId
     * 
     * @return n32fMessageId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_N32F_MESSAGE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN32fMessageId()
    {
        return n32fMessageId;
    }

    @JsonProperty(JSON_PROPERTY_N32F_MESSAGE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN32fMessageId(String n32fMessageId)
    {
        this.n32fMessageId = n32fMessageId;
    }

    public N32fErrorInfo n32fErrorType(String n32fErrorType)
    {

        this.n32fErrorType = n32fErrorType;
        return this;
    }

    /**
     * Type of error while processing N32-f message
     * 
     * @return n32fErrorType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Type of error while processing N32-f message")
    @JsonProperty(JSON_PROPERTY_N32F_ERROR_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN32fErrorType()
    {
        return n32fErrorType;
    }

    @JsonProperty(JSON_PROPERTY_N32F_ERROR_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN32fErrorType(String n32fErrorType)
    {
        this.n32fErrorType = n32fErrorType;
    }

    public N32fErrorInfo n32fContextId(String n32fContextId)
    {

        this.n32fContextId = n32fContextId;
        return this;
    }

    /**
     * Get n32fContextId
     * 
     * @return n32fContextId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N32F_CONTEXT_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getN32fContextId()
    {
        return n32fContextId;
    }

    @JsonProperty(JSON_PROPERTY_N32F_CONTEXT_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN32fContextId(String n32fContextId)
    {
        this.n32fContextId = n32fContextId;
    }

    public N32fErrorInfo failedModificationList(List<FailedModificationInfo> failedModificationList)
    {

        this.failedModificationList = failedModificationList;
        return this;
    }

    public N32fErrorInfo addFailedModificationListItem(FailedModificationInfo failedModificationListItem)
    {
        if (this.failedModificationList == null)
        {
            this.failedModificationList = new ArrayList<>();
        }
        this.failedModificationList.add(failedModificationListItem);
        return this;
    }

    /**
     * Get failedModificationList
     * 
     * @return failedModificationList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_FAILED_MODIFICATION_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<FailedModificationInfo> getFailedModificationList()
    {
        return failedModificationList;
    }

    @JsonProperty(JSON_PROPERTY_FAILED_MODIFICATION_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFailedModificationList(List<FailedModificationInfo> failedModificationList)
    {
        this.failedModificationList = failedModificationList;
    }

    public N32fErrorInfo errorDetailsList(List<N32fErrorDetail> errorDetailsList)
    {

        this.errorDetailsList = errorDetailsList;
        return this;
    }

    public N32fErrorInfo addErrorDetailsListItem(N32fErrorDetail errorDetailsListItem)
    {
        if (this.errorDetailsList == null)
        {
            this.errorDetailsList = new ArrayList<>();
        }
        this.errorDetailsList.add(errorDetailsListItem);
        return this;
    }

    /**
     * Get errorDetailsList
     * 
     * @return errorDetailsList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ERROR_DETAILS_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<N32fErrorDetail> getErrorDetailsList()
    {
        return errorDetailsList;
    }

    @JsonProperty(JSON_PROPERTY_ERROR_DETAILS_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setErrorDetailsList(List<N32fErrorDetail> errorDetailsList)
    {
        this.errorDetailsList = errorDetailsList;
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
        N32fErrorInfo n32fErrorInfo = (N32fErrorInfo) o;
        return Objects.equals(this.n32fMessageId, n32fErrorInfo.n32fMessageId) && Objects.equals(this.n32fErrorType, n32fErrorInfo.n32fErrorType)
               && Objects.equals(this.n32fContextId, n32fErrorInfo.n32fContextId)
               && Objects.equals(this.failedModificationList, n32fErrorInfo.failedModificationList)
               && Objects.equals(this.errorDetailsList, n32fErrorInfo.errorDetailsList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(n32fMessageId, n32fErrorType, n32fContextId, failedModificationList, errorDetailsList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class N32fErrorInfo {\n");
        sb.append("    n32fMessageId: ").append(toIndentedString(n32fMessageId)).append("\n");
        sb.append("    n32fErrorType: ").append(toIndentedString(n32fErrorType)).append("\n");
        sb.append("    n32fContextId: ").append(toIndentedString(n32fContextId)).append("\n");
        sb.append("    failedModificationList: ").append(toIndentedString(failedModificationList)).append("\n");
        sb.append("    errorDetailsList: ").append(toIndentedString(errorDetailsList)).append("\n");
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
