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
 * Contains the request parameters received by the HSS for a subscription to
 * notifications of UE reachability for IP
 */
@ApiModel(description = "Contains the request parameters received by the HSS for a subscription to notifications of UE reachability for IP ")
@JsonPropertyOrder({ UeReachabilitySubscription.JSON_PROPERTY_EXPIRY, UeReachabilitySubscription.JSON_PROPERTY_CALLBACK_REFERENCE })
public class UeReachabilitySubscription
{
    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private OffsetDateTime expiry;

    public static final String JSON_PROPERTY_CALLBACK_REFERENCE = "callbackReference";
    private String callbackReference;

    public UeReachabilitySubscription()
    {
    }

    public UeReachabilitySubscription expiry(OffsetDateTime expiry)
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

    public UeReachabilitySubscription callbackReference(String callbackReference)
    {

        this.callbackReference = callbackReference;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return callbackReference
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_CALLBACK_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getCallbackReference()
    {
        return callbackReference;
    }

    @JsonProperty(JSON_PROPERTY_CALLBACK_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCallbackReference(String callbackReference)
    {
        this.callbackReference = callbackReference;
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
        UeReachabilitySubscription ueReachabilitySubscription = (UeReachabilitySubscription) o;
        return Objects.equals(this.expiry, ueReachabilitySubscription.expiry)
               && Objects.equals(this.callbackReference, ueReachabilitySubscription.callbackReference);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(expiry, callbackReference);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeReachabilitySubscription {\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    callbackReference: ").append(toIndentedString(callbackReference)).append("\n");
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
