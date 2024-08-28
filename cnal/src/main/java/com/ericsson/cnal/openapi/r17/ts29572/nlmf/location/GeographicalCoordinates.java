/*
 * LMF Location
 * LMF Location Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29572.nlmf.location;

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
 * Geographical coordinates.
 */
@ApiModel(description = "Geographical coordinates.")
@JsonPropertyOrder({ GeographicalCoordinates.JSON_PROPERTY_LON, GeographicalCoordinates.JSON_PROPERTY_LAT })
public class GeographicalCoordinates
{
    public static final String JSON_PROPERTY_LON = "lon";
    private Double lon;

    public static final String JSON_PROPERTY_LAT = "lat";
    private Double lat;

    public GeographicalCoordinates()
    {
    }

    public GeographicalCoordinates lon(Double lon)
    {

        this.lon = lon;
        return this;
    }

    /**
     * Get lon minimum: -180 maximum: 180
     * 
     * @return lon
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LON)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Double getLon()
    {
        return lon;
    }

    @JsonProperty(JSON_PROPERTY_LON)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLon(Double lon)
    {
        this.lon = lon;
    }

    public GeographicalCoordinates lat(Double lat)
    {

        this.lat = lat;
        return this;
    }

    /**
     * Get lat minimum: -90 maximum: 90
     * 
     * @return lat
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LAT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Double getLat()
    {
        return lat;
    }

    @JsonProperty(JSON_PROPERTY_LAT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLat(Double lat)
    {
        this.lat = lat;
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
        GeographicalCoordinates geographicalCoordinates = (GeographicalCoordinates) o;
        return Objects.equals(this.lon, geographicalCoordinates.lon) && Objects.equals(this.lat, geographicalCoordinates.lat);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lon, lat);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class GeographicalCoordinates {\n");
        sb.append("    lon: ").append(toIndentedString(lon)).append("\n");
        sb.append("    lat: ").append(toIndentedString(lat)).append("\n");
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
