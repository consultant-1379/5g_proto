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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Distinct or wildcarded public identity and its associated priority, trace and
 * barring information
 */
@ApiModel(description = "Distinct or wildcarded public identity and its associated priority, trace and barring information ")
@JsonPropertyOrder({ PublicIdentifier.JSON_PROPERTY_PUBLIC_IDENTITY,
                     PublicIdentifier.JSON_PROPERTY_DISPLAY_NAME,
                     PublicIdentifier.JSON_PROPERTY_IMS_SERVICE_PRIORITY,
                     PublicIdentifier.JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO,
                     PublicIdentifier.JSON_PROPERTY_BARRING_INDICATOR,
                     PublicIdentifier.JSON_PROPERTY_WILDCARDED_IMPU })
public class PublicIdentifier
{
    public static final String JSON_PROPERTY_PUBLIC_IDENTITY = "publicIdentity";
    private PublicIdentity publicIdentity;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "displayName";
    private String displayName;

    public static final String JSON_PROPERTY_IMS_SERVICE_PRIORITY = "imsServicePriority";
    private PriorityLevels imsServicePriority;

    public static final String JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO = "serviceLevelTraceInfo";
    private ServiceLevelTraceInformation serviceLevelTraceInfo;

    public static final String JSON_PROPERTY_BARRING_INDICATOR = "barringIndicator";
    private Boolean barringIndicator;

    public static final String JSON_PROPERTY_WILDCARDED_IMPU = "wildcardedImpu";
    private String wildcardedImpu;

    public PublicIdentifier()
    {
    }

    public PublicIdentifier publicIdentity(PublicIdentity publicIdentity)
    {

        this.publicIdentity = publicIdentity;
        return this;
    }

    /**
     * Get publicIdentity
     * 
     * @return publicIdentity
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PUBLIC_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public PublicIdentity getPublicIdentity()
    {
        return publicIdentity;
    }

    @JsonProperty(JSON_PROPERTY_PUBLIC_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPublicIdentity(PublicIdentity publicIdentity)
    {
        this.publicIdentity = publicIdentity;
    }

    public PublicIdentifier displayName(String displayName)
    {

        this.displayName = displayName;
        return this;
    }

    /**
     * Get displayName
     * 
     * @return displayName
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DISPLAY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDisplayName()
    {
        return displayName;
    }

    @JsonProperty(JSON_PROPERTY_DISPLAY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public PublicIdentifier imsServicePriority(PriorityLevels imsServicePriority)
    {

        this.imsServicePriority = imsServicePriority;
        return this;
    }

    /**
     * Get imsServicePriority
     * 
     * @return imsServicePriority
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IMS_SERVICE_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PriorityLevels getImsServicePriority()
    {
        return imsServicePriority;
    }

    @JsonProperty(JSON_PROPERTY_IMS_SERVICE_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImsServicePriority(PriorityLevels imsServicePriority)
    {
        this.imsServicePriority = imsServicePriority;
    }

    public PublicIdentifier serviceLevelTraceInfo(ServiceLevelTraceInformation serviceLevelTraceInfo)
    {

        this.serviceLevelTraceInfo = serviceLevelTraceInfo;
        return this;
    }

    /**
     * Get serviceLevelTraceInfo
     * 
     * @return serviceLevelTraceInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ServiceLevelTraceInformation getServiceLevelTraceInfo()
    {
        return serviceLevelTraceInfo;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_LEVEL_TRACE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceLevelTraceInfo(ServiceLevelTraceInformation serviceLevelTraceInfo)
    {
        this.serviceLevelTraceInfo = serviceLevelTraceInfo;
    }

    public PublicIdentifier barringIndicator(Boolean barringIndicator)
    {

        this.barringIndicator = barringIndicator;
        return this;
    }

    /**
     * Get barringIndicator
     * 
     * @return barringIndicator
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BARRING_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getBarringIndicator()
    {
        return barringIndicator;
    }

    @JsonProperty(JSON_PROPERTY_BARRING_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBarringIndicator(Boolean barringIndicator)
    {
        this.barringIndicator = barringIndicator;
    }

    public PublicIdentifier wildcardedImpu(String wildcardedImpu)
    {

        this.wildcardedImpu = wildcardedImpu;
        return this;
    }

    /**
     * Get wildcardedImpu
     * 
     * @return wildcardedImpu
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_WILDCARDED_IMPU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getWildcardedImpu()
    {
        return wildcardedImpu;
    }

    @JsonProperty(JSON_PROPERTY_WILDCARDED_IMPU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWildcardedImpu(String wildcardedImpu)
    {
        this.wildcardedImpu = wildcardedImpu;
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
        PublicIdentifier publicIdentifier = (PublicIdentifier) o;
        return Objects.equals(this.publicIdentity, publicIdentifier.publicIdentity) && Objects.equals(this.displayName, publicIdentifier.displayName)
               && Objects.equals(this.imsServicePriority, publicIdentifier.imsServicePriority)
               && Objects.equals(this.serviceLevelTraceInfo, publicIdentifier.serviceLevelTraceInfo)
               && Objects.equals(this.barringIndicator, publicIdentifier.barringIndicator)
               && Objects.equals(this.wildcardedImpu, publicIdentifier.wildcardedImpu);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(publicIdentity, displayName, imsServicePriority, serviceLevelTraceInfo, barringIndicator, wildcardedImpu);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PublicIdentifier {\n");
        sb.append("    publicIdentity: ").append(toIndentedString(publicIdentity)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    imsServicePriority: ").append(toIndentedString(imsServicePriority)).append("\n");
        sb.append("    serviceLevelTraceInfo: ").append(toIndentedString(serviceLevelTraceInfo)).append("\n");
        sb.append("    barringIndicator: ").append(toIndentedString(barringIndicator)).append("\n");
        sb.append("    wildcardedImpu: ").append(toIndentedString(wildcardedImpu)).append("\n");
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
