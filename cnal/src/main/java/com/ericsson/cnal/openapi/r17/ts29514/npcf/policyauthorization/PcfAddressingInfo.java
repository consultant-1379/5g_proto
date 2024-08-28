/*
 * Npcf_PolicyAuthorization Service API
 * PCF Policy Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29514.npcf.policyauthorization;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpEndPoint;
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
 * Contains PCF address information.
 */
@ApiModel(description = "Contains PCF address information.")
@JsonPropertyOrder({ PcfAddressingInfo.JSON_PROPERTY_PCF_FQDN,
                     PcfAddressingInfo.JSON_PROPERTY_PCF_IP_END_POINTS,
                     PcfAddressingInfo.JSON_PROPERTY_BINDING_INFO })
public class PcfAddressingInfo
{
    public static final String JSON_PROPERTY_PCF_FQDN = "pcfFqdn";
    private String pcfFqdn;

    public static final String JSON_PROPERTY_PCF_IP_END_POINTS = "pcfIpEndPoints";
    private List<IpEndPoint> pcfIpEndPoints = null;

    public static final String JSON_PROPERTY_BINDING_INFO = "bindingInfo";
    private String bindingInfo;

    public PcfAddressingInfo()
    {
    }

    public PcfAddressingInfo pcfFqdn(String pcfFqdn)
    {

        this.pcfFqdn = pcfFqdn;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pcfFqdn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PCF_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfFqdn()
    {
        return pcfFqdn;
    }

    @JsonProperty(JSON_PROPERTY_PCF_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfFqdn(String pcfFqdn)
    {
        this.pcfFqdn = pcfFqdn;
    }

    public PcfAddressingInfo pcfIpEndPoints(List<IpEndPoint> pcfIpEndPoints)
    {

        this.pcfIpEndPoints = pcfIpEndPoints;
        return this;
    }

    public PcfAddressingInfo addPcfIpEndPointsItem(IpEndPoint pcfIpEndPointsItem)
    {
        if (this.pcfIpEndPoints == null)
        {
            this.pcfIpEndPoints = new ArrayList<>();
        }
        this.pcfIpEndPoints.add(pcfIpEndPointsItem);
        return this;
    }

    /**
     * IP end points of the PCF hosting the Npcf_PolicyAuthorization service.
     * 
     * @return pcfIpEndPoints
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "IP end points of the PCF hosting the Npcf_PolicyAuthorization service.")
    @JsonProperty(JSON_PROPERTY_PCF_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IpEndPoint> getPcfIpEndPoints()
    {
        return pcfIpEndPoints;
    }

    @JsonProperty(JSON_PROPERTY_PCF_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfIpEndPoints(List<IpEndPoint> pcfIpEndPoints)
    {
        this.pcfIpEndPoints = pcfIpEndPoints;
    }

    public PcfAddressingInfo bindingInfo(String bindingInfo)
    {

        this.bindingInfo = bindingInfo;
        return this;
    }

    /**
     * contains the binding indications of the PCF.
     * 
     * @return bindingInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "contains the binding indications of the PCF.")
    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBindingInfo()
    {
        return bindingInfo;
    }

    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBindingInfo(String bindingInfo)
    {
        this.bindingInfo = bindingInfo;
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
        PcfAddressingInfo pcfAddressingInfo = (PcfAddressingInfo) o;
        return Objects.equals(this.pcfFqdn, pcfAddressingInfo.pcfFqdn) && Objects.equals(this.pcfIpEndPoints, pcfAddressingInfo.pcfIpEndPoints)
               && Objects.equals(this.bindingInfo, pcfAddressingInfo.bindingInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pcfFqdn, pcfIpEndPoints, bindingInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PcfAddressingInfo {\n");
        sb.append("    pcfFqdn: ").append(toIndentedString(pcfFqdn)).append("\n");
        sb.append("    pcfIpEndPoints: ").append(toIndentedString(pcfIpEndPoints)).append("\n");
        sb.append("    bindingInfo: ").append(toIndentedString(bindingInfo)).append("\n");
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
