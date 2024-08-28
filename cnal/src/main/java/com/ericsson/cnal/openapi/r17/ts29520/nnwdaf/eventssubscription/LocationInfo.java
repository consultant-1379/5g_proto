/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents UE location information.
 */
@ApiModel(description = "Represents UE location information.")
@JsonPropertyOrder({ LocationInfo.JSON_PROPERTY_LOC, LocationInfo.JSON_PROPERTY_RATIO, LocationInfo.JSON_PROPERTY_CONFIDENCE })
public class LocationInfo
{
    public static final String JSON_PROPERTY_LOC = "loc";
    private UserLocation loc;

    public static final String JSON_PROPERTY_RATIO = "ratio";
    private Integer ratio;

    public static final String JSON_PROPERTY_CONFIDENCE = "confidence";
    private Integer confidence;

    public LocationInfo()
    {
    }

    public LocationInfo loc(UserLocation loc)
    {

        this.loc = loc;
        return this;
    }

    /**
     * Get loc
     * 
     * @return loc
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LOC)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UserLocation getLoc()
    {
        return loc;
    }

    @JsonProperty(JSON_PROPERTY_LOC)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLoc(UserLocation loc)
    {
        this.loc = loc;
    }

    public LocationInfo ratio(Integer ratio)
    {

        this.ratio = ratio;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return ratio
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getRatio()
    {
        return ratio;
    }

    @JsonProperty(JSON_PROPERTY_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatio(Integer ratio)
    {
        this.ratio = ratio;
    }

    public LocationInfo confidence(Integer confidence)
    {

        this.confidence = confidence;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return confidence
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_CONFIDENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getConfidence()
    {
        return confidence;
    }

    @JsonProperty(JSON_PROPERTY_CONFIDENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConfidence(Integer confidence)
    {
        this.confidence = confidence;
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
        LocationInfo locationInfo = (LocationInfo) o;
        return Objects.equals(this.loc, locationInfo.loc) && Objects.equals(this.ratio, locationInfo.ratio)
               && Objects.equals(this.confidence, locationInfo.confidence);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(loc, ratio, confidence);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class LocationInfo {\n");
        sb.append("    loc: ").append(toIndentedString(loc)).append("\n");
        sb.append("    ratio: ").append(toIndentedString(ratio)).append("\n");
        sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
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
