/*
 * Nhss_imsSDM
 * Nhss Subscriber Data Management Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imssdm;

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
 * IMS Service Level Trace Information
 */
@ApiModel(description = "IMS Service Level Trace Information")
@JsonPropertyOrder({ ServiceLevelTraceInformation.JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO })
public class ServiceLevelTraceInformation
{
    public static final String JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO = "serviceLevelTraceInfo";
    private String serviceLevelTraceInfo;

    public ServiceLevelTraceInformation()
    {
    }

    public ServiceLevelTraceInformation serviceLevelTraceInfo(String serviceLevelTraceInfo)
    {

        this.serviceLevelTraceInfo = serviceLevelTraceInfo;
        return this;
    }

    /**
     * Get serviceLevelTraceInfo
     * 
     * @return serviceLevelTraceInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServiceLevelTraceInfo()
    {
        return serviceLevelTraceInfo;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceLevelTraceInfo(String serviceLevelTraceInfo)
    {
        this.serviceLevelTraceInfo = serviceLevelTraceInfo;
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
        ServiceLevelTraceInformation serviceLevelTraceInformation = (ServiceLevelTraceInformation) o;
        return Objects.equals(this.serviceLevelTraceInfo, serviceLevelTraceInformation.serviceLevelTraceInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceLevelTraceInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceLevelTraceInformation {\n");
        sb.append("    serviceLevelTraceInfo: ").append(toIndentedString(serviceLevelTraceInfo)).append("\n");
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
