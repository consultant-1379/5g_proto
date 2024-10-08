/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UserLocation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * CoverageInfo
 */
@JsonPropertyOrder({ CoverageInfo.JSON_PROPERTY_COVERAGE_STATUS, CoverageInfo.JSON_PROPERTY_CHANGE_TIME, CoverageInfo.JSON_PROPERTY_LOCATION_INFO })
public class CoverageInfo
{
    public static final String JSON_PROPERTY_COVERAGE_STATUS = "coverageStatus";
    private Boolean coverageStatus;

    public static final String JSON_PROPERTY_CHANGE_TIME = "changeTime";
    private OffsetDateTime changeTime;

    public static final String JSON_PROPERTY_LOCATION_INFO = "locationInfo";
    private List<UserLocation> locationInfo = null;

    public CoverageInfo()
    {
    }

    public CoverageInfo coverageStatus(Boolean coverageStatus)
    {

        this.coverageStatus = coverageStatus;
        return this;
    }

    /**
     * Get coverageStatus
     * 
     * @return coverageStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_COVERAGE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCoverageStatus()
    {
        return coverageStatus;
    }

    @JsonProperty(JSON_PROPERTY_COVERAGE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCoverageStatus(Boolean coverageStatus)
    {
        this.coverageStatus = coverageStatus;
    }

    public CoverageInfo changeTime(OffsetDateTime changeTime)
    {

        this.changeTime = changeTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return changeTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_CHANGE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getChangeTime()
    {
        return changeTime;
    }

    @JsonProperty(JSON_PROPERTY_CHANGE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChangeTime(OffsetDateTime changeTime)
    {
        this.changeTime = changeTime;
    }

    public CoverageInfo locationInfo(List<UserLocation> locationInfo)
    {

        this.locationInfo = locationInfo;
        return this;
    }

    public CoverageInfo addLocationInfoItem(UserLocation locationInfoItem)
    {
        if (this.locationInfo == null)
        {
            this.locationInfo = new ArrayList<>();
        }
        this.locationInfo.add(locationInfoItem);
        return this;
    }

    /**
     * Get locationInfo
     * 
     * @return locationInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UserLocation> getLocationInfo()
    {
        return locationInfo;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationInfo(List<UserLocation> locationInfo)
    {
        this.locationInfo = locationInfo;
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
        CoverageInfo coverageInfo = (CoverageInfo) o;
        return Objects.equals(this.coverageStatus, coverageInfo.coverageStatus) && Objects.equals(this.changeTime, coverageInfo.changeTime)
               && Objects.equals(this.locationInfo, coverageInfo.locationInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(coverageStatus, changeTime, locationInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CoverageInfo {\n");
        sb.append("    coverageStatus: ").append(toIndentedString(coverageStatus)).append("\n");
        sb.append("    changeTime: ").append(toIndentedString(changeTime)).append("\n");
        sb.append("    locationInfo: ").append(toIndentedString(locationInfo)).append("\n");
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
