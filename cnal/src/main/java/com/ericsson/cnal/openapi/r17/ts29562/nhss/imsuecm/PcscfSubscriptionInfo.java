/*
 * Nhss_imsUECM
 * Nhss UE Context Management Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imsuecm;

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
 * Subscription information of the P-CSCF for the SIP Registration State event
 */
@ApiModel(description = "Subscription information of the P-CSCF for the SIP Registration State event")
@JsonPropertyOrder({ PcscfSubscriptionInfo.JSON_PROPERTY_CALL_ID_SIP_HEADER,
                     PcscfSubscriptionInfo.JSON_PROPERTY_FROM_SIP_HEADER,
                     PcscfSubscriptionInfo.JSON_PROPERTY_TO_SIP_HEADER,
                     PcscfSubscriptionInfo.JSON_PROPERTY_CONTACT })
public class PcscfSubscriptionInfo
{
    public static final String JSON_PROPERTY_CALL_ID_SIP_HEADER = "callIdSipHeader";
    private String callIdSipHeader;

    public static final String JSON_PROPERTY_FROM_SIP_HEADER = "fromSipHeader";
    private String fromSipHeader;

    public static final String JSON_PROPERTY_TO_SIP_HEADER = "toSipHeader";
    private String toSipHeader;

    public static final String JSON_PROPERTY_CONTACT = "contact";
    private String contact;

    public PcscfSubscriptionInfo()
    {
    }

    public PcscfSubscriptionInfo callIdSipHeader(String callIdSipHeader)
    {

        this.callIdSipHeader = callIdSipHeader;
        return this;
    }

    /**
     * Get callIdSipHeader
     * 
     * @return callIdSipHeader
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CALL_ID_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getCallIdSipHeader()
    {
        return callIdSipHeader;
    }

    @JsonProperty(JSON_PROPERTY_CALL_ID_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCallIdSipHeader(String callIdSipHeader)
    {
        this.callIdSipHeader = callIdSipHeader;
    }

    public PcscfSubscriptionInfo fromSipHeader(String fromSipHeader)
    {

        this.fromSipHeader = fromSipHeader;
        return this;
    }

    /**
     * Get fromSipHeader
     * 
     * @return fromSipHeader
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_FROM_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getFromSipHeader()
    {
        return fromSipHeader;
    }

    @JsonProperty(JSON_PROPERTY_FROM_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFromSipHeader(String fromSipHeader)
    {
        this.fromSipHeader = fromSipHeader;
    }

    public PcscfSubscriptionInfo toSipHeader(String toSipHeader)
    {

        this.toSipHeader = toSipHeader;
        return this;
    }

    /**
     * Get toSipHeader
     * 
     * @return toSipHeader
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TO_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getToSipHeader()
    {
        return toSipHeader;
    }

    @JsonProperty(JSON_PROPERTY_TO_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setToSipHeader(String toSipHeader)
    {
        this.toSipHeader = toSipHeader;
    }

    public PcscfSubscriptionInfo contact(String contact)
    {

        this.contact = contact;
        return this;
    }

    /**
     * Get contact
     * 
     * @return contact
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONTACT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getContact()
    {
        return contact;
    }

    @JsonProperty(JSON_PROPERTY_CONTACT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setContact(String contact)
    {
        this.contact = contact;
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
        PcscfSubscriptionInfo pcscfSubscriptionInfo = (PcscfSubscriptionInfo) o;
        return Objects.equals(this.callIdSipHeader, pcscfSubscriptionInfo.callIdSipHeader)
               && Objects.equals(this.fromSipHeader, pcscfSubscriptionInfo.fromSipHeader) && Objects.equals(this.toSipHeader, pcscfSubscriptionInfo.toSipHeader)
               && Objects.equals(this.contact, pcscfSubscriptionInfo.contact);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(callIdSipHeader, fromSipHeader, toSipHeader, contact);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PcscfSubscriptionInfo {\n");
        sb.append("    callIdSipHeader: ").append(toIndentedString(callIdSipHeader)).append("\n");
        sb.append("    fromSipHeader: ").append(toIndentedString(fromSipHeader)).append("\n");
        sb.append("    toSipHeader: ").append(toIndentedString(toSipHeader)).append("\n");
        sb.append("    contact: ").append(toIndentedString(contact)).append("\n");
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
