/*
 * TS 29.122 Common Data Types
 * Data types applicable to several APIs.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.commondata;

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
 * Represents a usage threshold.
 */
@ApiModel(description = "Represents a usage threshold.")
@JsonPropertyOrder({ UsageThreshold.JSON_PROPERTY_DURATION,
                     UsageThreshold.JSON_PROPERTY_TOTAL_VOLUME,
                     UsageThreshold.JSON_PROPERTY_DOWNLINK_VOLUME,
                     UsageThreshold.JSON_PROPERTY_UPLINK_VOLUME })
public class UsageThreshold
{
    public static final String JSON_PROPERTY_DURATION = "duration";
    private Integer duration;

    public static final String JSON_PROPERTY_TOTAL_VOLUME = "totalVolume";
    private Long totalVolume;

    public static final String JSON_PROPERTY_DOWNLINK_VOLUME = "downlinkVolume";
    private Long downlinkVolume;

    public static final String JSON_PROPERTY_UPLINK_VOLUME = "uplinkVolume";
    private Long uplinkVolume;

    public UsageThreshold()
    {
    }

    public UsageThreshold duration(Integer duration)
    {

        this.duration = duration;
        return this;
    }

    /**
     * Unsigned integer identifying a period of time in units of seconds. minimum: 0
     * 
     * @return duration
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a period of time in units of seconds.")
    @JsonProperty(JSON_PROPERTY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDuration()
    {
        return duration;
    }

    @JsonProperty(JSON_PROPERTY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public UsageThreshold totalVolume(Long totalVolume)
    {

        this.totalVolume = totalVolume;
        return this;
    }

    /**
     * Unsigned integer identifying a volume in units of bytes. minimum: 0
     * 
     * @return totalVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a volume in units of bytes.")
    @JsonProperty(JSON_PROPERTY_TOTAL_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getTotalVolume()
    {
        return totalVolume;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalVolume(Long totalVolume)
    {
        this.totalVolume = totalVolume;
    }

    public UsageThreshold downlinkVolume(Long downlinkVolume)
    {

        this.downlinkVolume = downlinkVolume;
        return this;
    }

    /**
     * Unsigned integer identifying a volume in units of bytes. minimum: 0
     * 
     * @return downlinkVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a volume in units of bytes.")
    @JsonProperty(JSON_PROPERTY_DOWNLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getDownlinkVolume()
    {
        return downlinkVolume;
    }

    @JsonProperty(JSON_PROPERTY_DOWNLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownlinkVolume(Long downlinkVolume)
    {
        this.downlinkVolume = downlinkVolume;
    }

    public UsageThreshold uplinkVolume(Long uplinkVolume)
    {

        this.uplinkVolume = uplinkVolume;
        return this;
    }

    /**
     * Unsigned integer identifying a volume in units of bytes. minimum: 0
     * 
     * @return uplinkVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a volume in units of bytes.")
    @JsonProperty(JSON_PROPERTY_UPLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getUplinkVolume()
    {
        return uplinkVolume;
    }

    @JsonProperty(JSON_PROPERTY_UPLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUplinkVolume(Long uplinkVolume)
    {
        this.uplinkVolume = uplinkVolume;
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
        UsageThreshold usageThreshold = (UsageThreshold) o;
        return Objects.equals(this.duration, usageThreshold.duration) && Objects.equals(this.totalVolume, usageThreshold.totalVolume)
               && Objects.equals(this.downlinkVolume, usageThreshold.downlinkVolume) && Objects.equals(this.uplinkVolume, usageThreshold.uplinkVolume);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(duration, totalVolume, downlinkVolume, uplinkVolume);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UsageThreshold {\n");
        sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
        sb.append("    totalVolume: ").append(toIndentedString(totalVolume)).append("\n");
        sb.append("    downlinkVolume: ").append(toIndentedString(downlinkVolume)).append("\n");
        sb.append("    uplinkVolume: ").append(toIndentedString(uplinkVolume)).append("\n");
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
