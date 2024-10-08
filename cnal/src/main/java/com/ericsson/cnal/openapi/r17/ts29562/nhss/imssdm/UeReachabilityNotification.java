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
 * Represents the contents of a notification of UE reachability for IP sent by
 * the HSS
 */
@ApiModel(description = "Represents the contents of a notification of UE reachability for IP sent by the HSS ")
@JsonPropertyOrder({ UeReachabilityNotification.JSON_PROPERTY_REACHABILITY_INDICATOR,
                     UeReachabilityNotification.JSON_PROPERTY_DETECTING_NODE,
                     UeReachabilityNotification.JSON_PROPERTY_ACCESS_TYPE })
public class UeReachabilityNotification
{
    public static final String JSON_PROPERTY_REACHABILITY_INDICATOR = "reachabilityIndicator";
    private Boolean reachabilityIndicator;

    public static final String JSON_PROPERTY_DETECTING_NODE = "detectingNode";
    private String detectingNode;

    public static final String JSON_PROPERTY_ACCESS_TYPE = "accessType";
    private String accessType;

    public UeReachabilityNotification()
    {
    }

    public UeReachabilityNotification reachabilityIndicator(Boolean reachabilityIndicator)
    {

        this.reachabilityIndicator = reachabilityIndicator;
        return this;
    }

    /**
     * Get reachabilityIndicator
     * 
     * @return reachabilityIndicator
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REACHABILITY_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getReachabilityIndicator()
    {
        return reachabilityIndicator;
    }

    @JsonProperty(JSON_PROPERTY_REACHABILITY_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReachabilityIndicator(Boolean reachabilityIndicator)
    {
        this.reachabilityIndicator = reachabilityIndicator;
    }

    public UeReachabilityNotification detectingNode(String detectingNode)
    {

        this.detectingNode = detectingNode;
        return this;
    }

    /**
     * Represents the type of serving node that detected the reachability of the UE
     * 
     * @return detectingNode
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Represents the type of serving node that detected the reachability of the UE")
    @JsonProperty(JSON_PROPERTY_DETECTING_NODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDetectingNode()
    {
        return detectingNode;
    }

    @JsonProperty(JSON_PROPERTY_DETECTING_NODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDetectingNode(String detectingNode)
    {
        this.detectingNode = detectingNode;
    }

    public UeReachabilityNotification accessType(String accessType)
    {

        this.accessType = accessType;
        return this;
    }

    /**
     * Represents the type of access (3GPP or non-3GPP)
     * 
     * @return accessType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Represents the type of access (3GPP or non-3GPP)")
    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAccessType()
    {
        return accessType;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAccessType(String accessType)
    {
        this.accessType = accessType;
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
        UeReachabilityNotification ueReachabilityNotification = (UeReachabilityNotification) o;
        return Objects.equals(this.reachabilityIndicator, ueReachabilityNotification.reachabilityIndicator)
               && Objects.equals(this.detectingNode, ueReachabilityNotification.detectingNode)
               && Objects.equals(this.accessType, ueReachabilityNotification.accessType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(reachabilityIndicator, detectingNode, accessType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeReachabilityNotification {\n");
        sb.append("    reachabilityIndicator: ").append(toIndentedString(reachabilityIndicator)).append("\n");
        sb.append("    detectingNode: ").append(toIndentedString(detectingNode)).append("\n");
        sb.append("    accessType: ").append(toIndentedString(accessType)).append("\n");
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
