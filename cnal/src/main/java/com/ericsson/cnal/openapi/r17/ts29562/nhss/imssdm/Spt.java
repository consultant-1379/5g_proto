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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the data of a Service Point Trigger
 */
@ApiModel(description = "Contains the data of a Service Point Trigger")
@JsonPropertyOrder({ Spt.JSON_PROPERTY_CONDITION_NEGATED,
                     Spt.JSON_PROPERTY_SPT_GROUP,
                     Spt.JSON_PROPERTY_REG_TYPE,
                     Spt.JSON_PROPERTY_REQUEST_URI,
                     Spt.JSON_PROPERTY_SIP_METHOD,
                     Spt.JSON_PROPERTY_SIP_HEADER,
                     Spt.JSON_PROPERTY_SESSION_CASE,
                     Spt.JSON_PROPERTY_SESSION_DESCRIPTION })
public class Spt
{
    public static final String JSON_PROPERTY_CONDITION_NEGATED = "conditionNegated";
    private Boolean conditionNegated;

    public static final String JSON_PROPERTY_SPT_GROUP = "sptGroup";
    private List<Integer> sptGroup = new ArrayList<>();

    public static final String JSON_PROPERTY_REG_TYPE = "regType";
    private List<String> regType = null;

    public static final String JSON_PROPERTY_REQUEST_URI = "requestUri";
    private String requestUri;

    public static final String JSON_PROPERTY_SIP_METHOD = "sipMethod";
    private String sipMethod;

    public static final String JSON_PROPERTY_SIP_HEADER = "sipHeader";
    private HeaderSipRequest sipHeader;

    public static final String JSON_PROPERTY_SESSION_CASE = "sessionCase";
    private String sessionCase;

    public static final String JSON_PROPERTY_SESSION_DESCRIPTION = "sessionDescription";
    private SdpDescription sessionDescription;

    public Spt()
    {
    }

    public Spt conditionNegated(Boolean conditionNegated)
    {

        this.conditionNegated = conditionNegated;
        return this;
    }

    /**
     * Get conditionNegated
     * 
     * @return conditionNegated
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONDITION_NEGATED)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getConditionNegated()
    {
        return conditionNegated;
    }

    @JsonProperty(JSON_PROPERTY_CONDITION_NEGATED)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setConditionNegated(Boolean conditionNegated)
    {
        this.conditionNegated = conditionNegated;
    }

    public Spt sptGroup(List<Integer> sptGroup)
    {

        this.sptGroup = sptGroup;
        return this;
    }

    public Spt addSptGroupItem(Integer sptGroupItem)
    {
        this.sptGroup.add(sptGroupItem);
        return this;
    }

    /**
     * Get sptGroup
     * 
     * @return sptGroup
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SPT_GROUP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Integer> getSptGroup()
    {
        return sptGroup;
    }

    @JsonProperty(JSON_PROPERTY_SPT_GROUP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSptGroup(List<Integer> sptGroup)
    {
        this.sptGroup = sptGroup;
    }

    public Spt regType(List<String> regType)
    {

        this.regType = regType;
        return this;
    }

    public Spt addRegTypeItem(String regTypeItem)
    {
        if (this.regType == null)
        {
            this.regType = new ArrayList<>();
        }
        this.regType.add(regTypeItem);
        return this;
    }

    /**
     * Get regType
     * 
     * @return regType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REG_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getRegType()
    {
        return regType;
    }

    @JsonProperty(JSON_PROPERTY_REG_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRegType(List<String> regType)
    {
        this.regType = regType;
    }

    public Spt requestUri(String requestUri)
    {

        this.requestUri = requestUri;
        return this;
    }

    /**
     * Get requestUri
     * 
     * @return requestUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQUEST_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRequestUri()
    {
        return requestUri;
    }

    @JsonProperty(JSON_PROPERTY_REQUEST_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestUri(String requestUri)
    {
        this.requestUri = requestUri;
    }

    public Spt sipMethod(String sipMethod)
    {

        this.sipMethod = sipMethod;
        return this;
    }

    /**
     * Get sipMethod
     * 
     * @return sipMethod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SIP_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSipMethod()
    {
        return sipMethod;
    }

    @JsonProperty(JSON_PROPERTY_SIP_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSipMethod(String sipMethod)
    {
        this.sipMethod = sipMethod;
    }

    public Spt sipHeader(HeaderSipRequest sipHeader)
    {

        this.sipHeader = sipHeader;
        return this;
    }

    /**
     * Get sipHeader
     * 
     * @return sipHeader
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public HeaderSipRequest getSipHeader()
    {
        return sipHeader;
    }

    @JsonProperty(JSON_PROPERTY_SIP_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSipHeader(HeaderSipRequest sipHeader)
    {
        this.sipHeader = sipHeader;
    }

    public Spt sessionCase(String sessionCase)
    {

        this.sessionCase = sessionCase;
        return this;
    }

    /**
     * Represents the direction of the request in combination with the registration
     * status of the user as evaluated in the S-CSCF
     * 
     * @return sessionCase
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the direction of the request in combination with the registration status of the user as evaluated in the S-CSCF ")
    @JsonProperty(JSON_PROPERTY_SESSION_CASE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSessionCase()
    {
        return sessionCase;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_CASE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionCase(String sessionCase)
    {
        this.sessionCase = sessionCase;
    }

    public Spt sessionDescription(SdpDescription sessionDescription)
    {

        this.sessionDescription = sessionDescription;
        return this;
    }

    /**
     * Get sessionDescription
     * 
     * @return sessionDescription
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SESSION_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SdpDescription getSessionDescription()
    {
        return sessionDescription;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionDescription(SdpDescription sessionDescription)
    {
        this.sessionDescription = sessionDescription;
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
        Spt spt = (Spt) o;
        return Objects.equals(this.conditionNegated, spt.conditionNegated) && Objects.equals(this.sptGroup, spt.sptGroup)
               && Objects.equals(this.regType, spt.regType) && Objects.equals(this.requestUri, spt.requestUri) && Objects.equals(this.sipMethod, spt.sipMethod)
               && Objects.equals(this.sipHeader, spt.sipHeader) && Objects.equals(this.sessionCase, spt.sessionCase)
               && Objects.equals(this.sessionDescription, spt.sessionDescription);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(conditionNegated, sptGroup, regType, requestUri, sipMethod, sipHeader, sessionCase, sessionDescription);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Spt {\n");
        sb.append("    conditionNegated: ").append(toIndentedString(conditionNegated)).append("\n");
        sb.append("    sptGroup: ").append(toIndentedString(sptGroup)).append("\n");
        sb.append("    regType: ").append(toIndentedString(regType)).append("\n");
        sb.append("    requestUri: ").append(toIndentedString(requestUri)).append("\n");
        sb.append("    sipMethod: ").append(toIndentedString(sipMethod)).append("\n");
        sb.append("    sipHeader: ").append(toIndentedString(sipHeader)).append("\n");
        sb.append("    sessionCase: ").append(toIndentedString(sessionCase)).append("\n");
        sb.append("    sessionDescription: ").append(toIndentedString(sessionDescription)).append("\n");
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
