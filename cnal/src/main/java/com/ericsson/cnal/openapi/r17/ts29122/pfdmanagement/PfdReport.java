/*
 * 3gpp-pfd-management
 * API for PFD management.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.pfdmanagement;

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
 * Represents a PFD report indicating the external application identifier(s)
 * which PFD(s) are not added or modified successfully and the corresponding
 * failure cause(s).
 */
@ApiModel(description = "Represents a PFD report indicating the external application identifier(s) which PFD(s) are not added or modified successfully and the corresponding failure cause(s).")
@JsonPropertyOrder({ PfdReport.JSON_PROPERTY_EXTERNAL_APP_IDS,
                     PfdReport.JSON_PROPERTY_FAILURE_CODE,
                     PfdReport.JSON_PROPERTY_CACHING_TIME,
                     PfdReport.JSON_PROPERTY_LOCATION_AREA })
public class PfdReport
{
    public static final String JSON_PROPERTY_EXTERNAL_APP_IDS = "externalAppIds";
    private List<String> externalAppIds = new ArrayList<>();

    public static final String JSON_PROPERTY_FAILURE_CODE = "failureCode";
    private String failureCode;

    public static final String JSON_PROPERTY_CACHING_TIME = "cachingTime";
    private Integer cachingTime;

    public static final String JSON_PROPERTY_LOCATION_AREA = "locationArea";
    private UserPlaneLocationArea locationArea;

    public PfdReport()
    {
    }

    public PfdReport externalAppIds(List<String> externalAppIds)
    {

        this.externalAppIds = externalAppIds;
        return this;
    }

    public PfdReport addExternalAppIdsItem(String externalAppIdsItem)
    {
        this.externalAppIds.add(externalAppIdsItem);
        return this;
    }

    /**
     * Identifies the external application identifier(s) which PFD(s) are not added
     * or modified successfully
     * 
     * @return externalAppIds
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Identifies the external application identifier(s) which PFD(s) are not added or modified successfully")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getExternalAppIds()
    {
        return externalAppIds;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setExternalAppIds(List<String> externalAppIds)
    {
        this.externalAppIds = externalAppIds;
    }

    public PfdReport failureCode(String failureCode)
    {

        this.failureCode = failureCode;
        return this;
    }

    /**
     * Possible values are - MALFUNCTION: This value indicates that something
     * functions wrongly in PFD provisioning or the PFD provisioning does not
     * function at all. - RESOURCE_LIMITATION: This value indicates there is
     * resource limitation for PFD storage. - SHORT_DELAY: This value indicates that
     * the allowed delay is too short and PFD(s) are not stored. -
     * APP_ID_DUPLICATED: The received external application identifier(s) are
     * already provisioned. - PARTIAL_FAILURE: The PFD(s) are not provisioned to all
     * PCEFs/TDFs/SMFs. - OTHER_REASON: Other reason unspecified.
     * 
     * @return failureCode
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Possible values are - MALFUNCTION: This value indicates that something functions wrongly in PFD provisioning or the PFD provisioning does not function at all. - RESOURCE_LIMITATION: This value indicates there is resource limitation for PFD storage. - SHORT_DELAY: This value indicates that the allowed delay is too short and PFD(s) are not stored. - APP_ID_DUPLICATED: The received external application identifier(s) are already provisioned. - PARTIAL_FAILURE: The PFD(s) are not provisioned to all PCEFs/TDFs/SMFs. - OTHER_REASON: Other reason unspecified. ")
    @JsonProperty(JSON_PROPERTY_FAILURE_CODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getFailureCode()
    {
        return failureCode;
    }

    @JsonProperty(JSON_PROPERTY_FAILURE_CODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFailureCode(String failureCode)
    {
        this.failureCode = failureCode;
    }

    public PfdReport cachingTime(Integer cachingTime)
    {

        this.cachingTime = cachingTime;
        return this;
    }

    /**
     * Unsigned integer identifying a period of time in units of seconds. minimum: 0
     * 
     * @return cachingTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a period of time in units of seconds.")
    @JsonProperty(JSON_PROPERTY_CACHING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getCachingTime()
    {
        return cachingTime;
    }

    @JsonProperty(JSON_PROPERTY_CACHING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCachingTime(Integer cachingTime)
    {
        this.cachingTime = cachingTime;
    }

    public PfdReport locationArea(UserPlaneLocationArea locationArea)
    {

        this.locationArea = locationArea;
        return this;
    }

    /**
     * Get locationArea
     * 
     * @return locationArea
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserPlaneLocationArea getLocationArea()
    {
        return locationArea;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationArea(UserPlaneLocationArea locationArea)
    {
        this.locationArea = locationArea;
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
        PfdReport pfdReport = (PfdReport) o;
        return Objects.equals(this.externalAppIds, pfdReport.externalAppIds) && Objects.equals(this.failureCode, pfdReport.failureCode)
               && Objects.equals(this.cachingTime, pfdReport.cachingTime) && Objects.equals(this.locationArea, pfdReport.locationArea);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(externalAppIds, failureCode, cachingTime, locationArea);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PfdReport {\n");
        sb.append("    externalAppIds: ").append(toIndentedString(externalAppIds)).append("\n");
        sb.append("    failureCode: ").append(toIndentedString(failureCode)).append("\n");
        sb.append("    cachingTime: ").append(toIndentedString(cachingTime)).append("\n");
        sb.append("    locationArea: ").append(toIndentedString(locationArea)).append("\n");
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
