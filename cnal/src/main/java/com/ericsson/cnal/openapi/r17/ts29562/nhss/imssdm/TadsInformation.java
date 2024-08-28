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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
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
 * T-ADS Information
 */
@ApiModel(description = "T-ADS Information")
@JsonPropertyOrder({ TadsInformation.JSON_PROPERTY_VOICE_OVER_PS_SESSION_SUPPORT,
                     TadsInformation.JSON_PROPERTY_ACCESS_TYPE,
                     TadsInformation.JSON_PROPERTY_RAT_TYPE,
                     TadsInformation.JSON_PROPERTY_LAST_UE_ACTIVITY_TIME })
public class TadsInformation
{
    public static final String JSON_PROPERTY_VOICE_OVER_PS_SESSION_SUPPORT = "voiceOverPsSessionSupport";
    private String voiceOverPsSessionSupport;

    public static final String JSON_PROPERTY_ACCESS_TYPE = "accessType";
    private String accessType;

    public static final String JSON_PROPERTY_RAT_TYPE = "ratType";
    private String ratType;

    public static final String JSON_PROPERTY_LAST_UE_ACTIVITY_TIME = "lastUeActivityTime";
    private OffsetDateTime lastUeActivityTime;

    public TadsInformation()
    {
    }

    public TadsInformation voiceOverPsSessionSupport(String voiceOverPsSessionSupport)
    {

        this.voiceOverPsSessionSupport = voiceOverPsSessionSupport;
        return this;
    }

    /**
     * Represents the support for Voice-over-PS of the UE
     * 
     * @return voiceOverPsSessionSupport
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Represents the support for Voice-over-PS of the UE")
    @JsonProperty(JSON_PROPERTY_VOICE_OVER_PS_SESSION_SUPPORT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getVoiceOverPsSessionSupport()
    {
        return voiceOverPsSessionSupport;
    }

    @JsonProperty(JSON_PROPERTY_VOICE_OVER_PS_SESSION_SUPPORT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setVoiceOverPsSessionSupport(String voiceOverPsSessionSupport)
    {
        this.voiceOverPsSessionSupport = voiceOverPsSessionSupport;
    }

    public TadsInformation accessType(String accessType)
    {

        this.accessType = accessType;
        return this;
    }

    /**
     * Get accessType
     * 
     * @return accessType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAccessType()
    {
        return accessType;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccessType(String accessType)
    {
        this.accessType = accessType;
    }

    public TadsInformation ratType(String ratType)
    {

        this.ratType = ratType;
        return this;
    }

    /**
     * Indicates the radio access used.
     * 
     * @return ratType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the radio access used.")
    @JsonProperty(JSON_PROPERTY_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRatType()
    {
        return ratType;
    }

    @JsonProperty(JSON_PROPERTY_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatType(String ratType)
    {
        this.ratType = ratType;
    }

    public TadsInformation lastUeActivityTime(OffsetDateTime lastUeActivityTime)
    {

        this.lastUeActivityTime = lastUeActivityTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return lastUeActivityTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_LAST_UE_ACTIVITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getLastUeActivityTime()
    {
        return lastUeActivityTime;
    }

    @JsonProperty(JSON_PROPERTY_LAST_UE_ACTIVITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUeActivityTime(OffsetDateTime lastUeActivityTime)
    {
        this.lastUeActivityTime = lastUeActivityTime;
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
        TadsInformation tadsInformation = (TadsInformation) o;
        return Objects.equals(this.voiceOverPsSessionSupport, tadsInformation.voiceOverPsSessionSupport)
               && Objects.equals(this.accessType, tadsInformation.accessType) && Objects.equals(this.ratType, tadsInformation.ratType)
               && Objects.equals(this.lastUeActivityTime, tadsInformation.lastUeActivityTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(voiceOverPsSessionSupport, accessType, ratType, lastUeActivityTime);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TadsInformation {\n");
        sb.append("    voiceOverPsSessionSupport: ").append(toIndentedString(voiceOverPsSessionSupport)).append("\n");
        sb.append("    accessType: ").append(toIndentedString(accessType)).append("\n");
        sb.append("    ratType: ").append(toIndentedString(ratType)).append("\n");
        sb.append("    lastUeActivityTime: ").append(toIndentedString(lastUeActivityTime)).append("\n");
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
