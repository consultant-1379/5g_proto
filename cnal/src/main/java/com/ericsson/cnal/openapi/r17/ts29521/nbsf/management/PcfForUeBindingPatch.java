/*
 * Nbsf_Management
 * Binding Support Management Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.3.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29521.nbsf.management;

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
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Identifies the updates of an Individual PCF for a UE binding.
 */
@ApiModel(description = "Identifies the updates of an Individual PCF for a UE binding.")
@JsonPropertyOrder({ PcfForUeBindingPatch.JSON_PROPERTY_PCF_FOR_UE_FQDN,
                     PcfForUeBindingPatch.JSON_PROPERTY_PCF_FOR_UE_IP_END_POINTS,
                     PcfForUeBindingPatch.JSON_PROPERTY_PCF_ID })
public class PcfForUeBindingPatch
{
    public static final String JSON_PROPERTY_PCF_FOR_UE_FQDN = "pcfForUeFqdn";
    private String pcfForUeFqdn;

    public static final String JSON_PROPERTY_PCF_FOR_UE_IP_END_POINTS = "pcfForUeIpEndPoints";
    private List<IpEndPoint> pcfForUeIpEndPoints = null;

    public static final String JSON_PROPERTY_PCF_ID = "pcfId";
    private UUID pcfId;

    public PcfForUeBindingPatch()
    {
    }

    public PcfForUeBindingPatch pcfForUeFqdn(String pcfForUeFqdn)
    {

        this.pcfForUeFqdn = pcfForUeFqdn;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pcfForUeFqdn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PCF_FOR_UE_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfForUeFqdn()
    {
        return pcfForUeFqdn;
    }

    @JsonProperty(JSON_PROPERTY_PCF_FOR_UE_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfForUeFqdn(String pcfForUeFqdn)
    {
        this.pcfForUeFqdn = pcfForUeFqdn;
    }

    public PcfForUeBindingPatch pcfForUeIpEndPoints(List<IpEndPoint> pcfForUeIpEndPoints)
    {

        this.pcfForUeIpEndPoints = pcfForUeIpEndPoints;
        return this;
    }

    public PcfForUeBindingPatch addPcfForUeIpEndPointsItem(IpEndPoint pcfForUeIpEndPointsItem)
    {
        if (this.pcfForUeIpEndPoints == null)
        {
            this.pcfForUeIpEndPoints = new ArrayList<>();
        }
        this.pcfForUeIpEndPoints.add(pcfForUeIpEndPointsItem);
        return this;
    }

    /**
     * IP end points of the PCF hosting the Npcf_AmPolicyAuthorization service.
     * 
     * @return pcfForUeIpEndPoints
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "IP end points of the PCF hosting the Npcf_AmPolicyAuthorization service.")
    @JsonProperty(JSON_PROPERTY_PCF_FOR_UE_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IpEndPoint> getPcfForUeIpEndPoints()
    {
        return pcfForUeIpEndPoints;
    }

    @JsonProperty(JSON_PROPERTY_PCF_FOR_UE_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfForUeIpEndPoints(List<IpEndPoint> pcfForUeIpEndPoints)
    {
        this.pcfForUeIpEndPoints = pcfForUeIpEndPoints;
    }

    public PcfForUeBindingPatch pcfId(UUID pcfId)
    {

        this.pcfId = pcfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return pcfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_PCF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPcfId()
    {
        return pcfId;
    }

    @JsonProperty(JSON_PROPERTY_PCF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfId(UUID pcfId)
    {
        this.pcfId = pcfId;
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
        PcfForUeBindingPatch pcfForUeBindingPatch = (PcfForUeBindingPatch) o;
        return Objects.equals(this.pcfForUeFqdn, pcfForUeBindingPatch.pcfForUeFqdn)
               && Objects.equals(this.pcfForUeIpEndPoints, pcfForUeBindingPatch.pcfForUeIpEndPoints) && Objects.equals(this.pcfId, pcfForUeBindingPatch.pcfId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pcfForUeFqdn, pcfForUeIpEndPoints, pcfId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PcfForUeBindingPatch {\n");
        sb.append("    pcfForUeFqdn: ").append(toIndentedString(pcfForUeFqdn)).append("\n");
        sb.append("    pcfForUeIpEndPoints: ").append(toIndentedString(pcfForUeIpEndPoints)).append("\n");
        sb.append("    pcfId: ").append(toIndentedString(pcfId)).append("\n");
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
