/*
 * Nudm_SDM
 * Nudm Subscriber Data Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm;

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
 * Lpi
 */
@JsonPropertyOrder({ Lpi.JSON_PROPERTY_LOCATION_PRIVACY_IND, Lpi.JSON_PROPERTY_VALID_TIME_PERIOD })
public class Lpi
{
    public static final String JSON_PROPERTY_LOCATION_PRIVACY_IND = "locationPrivacyInd";
    private String locationPrivacyInd;

    public static final String JSON_PROPERTY_VALID_TIME_PERIOD = "validTimePeriod";
    private ValidTimePeriod validTimePeriod;

    public Lpi()
    {
    }

    public Lpi locationPrivacyInd(String locationPrivacyInd)
    {

        this.locationPrivacyInd = locationPrivacyInd;
        return this;
    }

    /**
     * Get locationPrivacyInd
     * 
     * @return locationPrivacyInd
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION_PRIVACY_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getLocationPrivacyInd()
    {
        return locationPrivacyInd;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_PRIVACY_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLocationPrivacyInd(String locationPrivacyInd)
    {
        this.locationPrivacyInd = locationPrivacyInd;
    }

    public Lpi validTimePeriod(ValidTimePeriod validTimePeriod)
    {

        this.validTimePeriod = validTimePeriod;
        return this;
    }

    /**
     * Get validTimePeriod
     * 
     * @return validTimePeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_VALID_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ValidTimePeriod getValidTimePeriod()
    {
        return validTimePeriod;
    }

    @JsonProperty(JSON_PROPERTY_VALID_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidTimePeriod(ValidTimePeriod validTimePeriod)
    {
        this.validTimePeriod = validTimePeriod;
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
        Lpi lpi = (Lpi) o;
        return Objects.equals(this.locationPrivacyInd, lpi.locationPrivacyInd) && Objects.equals(this.validTimePeriod, lpi.validTimePeriod);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(locationPrivacyInd, validTimePeriod);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Lpi {\n");
        sb.append("    locationPrivacyInd: ").append(toIndentedString(locationPrivacyInd)).append("\n");
        sb.append("    validTimePeriod: ").append(toIndentedString(validTimePeriod)).append("\n");
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
