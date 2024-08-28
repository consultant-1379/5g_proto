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
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.EpsInterworkingInfo;
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
 * UeContextInAmfData
 */
@JsonPropertyOrder({ UeContextInAmfData.JSON_PROPERTY_EPS_INTERWORKING_INFO, UeContextInAmfData.JSON_PROPERTY_AMF_INFO })
public class UeContextInAmfData
{
    public static final String JSON_PROPERTY_EPS_INTERWORKING_INFO = "epsInterworkingInfo";
    private EpsInterworkingInfo epsInterworkingInfo;

    public static final String JSON_PROPERTY_AMF_INFO = "amfInfo";
    private List<AmfInfo> amfInfo = null;

    public UeContextInAmfData()
    {
    }

    public UeContextInAmfData epsInterworkingInfo(EpsInterworkingInfo epsInterworkingInfo)
    {

        this.epsInterworkingInfo = epsInterworkingInfo;
        return this;
    }

    /**
     * Get epsInterworkingInfo
     * 
     * @return epsInterworkingInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EPS_INTERWORKING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public EpsInterworkingInfo getEpsInterworkingInfo()
    {
        return epsInterworkingInfo;
    }

    @JsonProperty(JSON_PROPERTY_EPS_INTERWORKING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpsInterworkingInfo(EpsInterworkingInfo epsInterworkingInfo)
    {
        this.epsInterworkingInfo = epsInterworkingInfo;
    }

    public UeContextInAmfData amfInfo(List<AmfInfo> amfInfo)
    {

        this.amfInfo = amfInfo;
        return this;
    }

    public UeContextInAmfData addAmfInfoItem(AmfInfo amfInfoItem)
    {
        if (this.amfInfo == null)
        {
            this.amfInfo = new ArrayList<>();
        }
        this.amfInfo.add(amfInfoItem);
        return this;
    }

    /**
     * AMF information
     * 
     * @return amfInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "AMF information")
    @JsonProperty(JSON_PROPERTY_AMF_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AmfInfo> getAmfInfo()
    {
        return amfInfo;
    }

    @JsonProperty(JSON_PROPERTY_AMF_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfInfo(List<AmfInfo> amfInfo)
    {
        this.amfInfo = amfInfo;
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
        UeContextInAmfData ueContextInAmfData = (UeContextInAmfData) o;
        return Objects.equals(this.epsInterworkingInfo, ueContextInAmfData.epsInterworkingInfo) && Objects.equals(this.amfInfo, ueContextInAmfData.amfInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(epsInterworkingInfo, amfInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeContextInAmfData {\n");
        sb.append("    epsInterworkingInfo: ").append(toIndentedString(epsInterworkingInfo)).append("\n");
        sb.append("    amfInfo: ").append(toIndentedString(amfInfo)).append("\n");
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
