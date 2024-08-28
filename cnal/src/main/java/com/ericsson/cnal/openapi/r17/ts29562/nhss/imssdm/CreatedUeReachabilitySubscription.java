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
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the response data returned by HSS after the subscription to
 * notifications of UE reachability for IP was created
 */
@ApiModel(description = "Contains the response data returned by HSS after the subscription to  notifications of UE reachability for IP was created ")
@JsonPropertyOrder({ CreatedUeReachabilitySubscription.JSON_PROPERTY_EXPIRY })
public class CreatedUeReachabilitySubscription
{
    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private OffsetDateTime expiry;

    public CreatedUeReachabilitySubscription()
    {
    }

    public CreatedUeReachabilitySubscription expiry(OffsetDateTime expiry)
    {

        this.expiry = expiry;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return expiry
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getExpiry()
    {
        return expiry;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setExpiry(OffsetDateTime expiry)
    {
        this.expiry = expiry;
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
        CreatedUeReachabilitySubscription createdUeReachabilitySubscription = (CreatedUeReachabilitySubscription) o;
        return Objects.equals(this.expiry, createdUeReachabilitySubscription.expiry);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(expiry);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreatedUeReachabilitySubscription {\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
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
