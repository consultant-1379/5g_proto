/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

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
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PSA Information
 */
@ApiModel(description = "PSA Information")
@JsonPropertyOrder({ PsaInformation.JSON_PROPERTY_PSA_IND,
                     PsaInformation.JSON_PROPERTY_DNAI_LIST,
                     PsaInformation.JSON_PROPERTY_UE_IPV6_PREFIX,
                     PsaInformation.JSON_PROPERTY_PSA_UPF_ID })
public class PsaInformation
{
    public static final String JSON_PROPERTY_PSA_IND = "psaInd";
    private String psaInd;

    public static final String JSON_PROPERTY_DNAI_LIST = "dnaiList";
    private List<String> dnaiList = null;

    public static final String JSON_PROPERTY_UE_IPV6_PREFIX = "ueIpv6Prefix";
    private String ueIpv6Prefix;

    public static final String JSON_PROPERTY_PSA_UPF_ID = "psaUpfId";
    private UUID psaUpfId;

    public PsaInformation()
    {
    }

    public PsaInformation psaInd(String psaInd)
    {

        this.psaInd = psaInd;
        return this;
    }

    /**
     * Indication of whether a PSA is inserted or removed. Possible values are -
     * PSA_INSERTED - PSA_REMOVED - PSA_INSERTED_ONLY - PSA_REMOVED_ONLY
     * 
     * @return psaInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indication of whether a PSA is inserted or removed. Possible values are   - PSA_INSERTED   - PSA_REMOVED   - PSA_INSERTED_ONLY   - PSA_REMOVED_ONLY ")
    @JsonProperty(JSON_PROPERTY_PSA_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPsaInd()
    {
        return psaInd;
    }

    @JsonProperty(JSON_PROPERTY_PSA_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPsaInd(String psaInd)
    {
        this.psaInd = psaInd;
    }

    public PsaInformation dnaiList(List<String> dnaiList)
    {

        this.dnaiList = dnaiList;
        return this;
    }

    public PsaInformation addDnaiListItem(String dnaiListItem)
    {
        if (this.dnaiList == null)
        {
            this.dnaiList = new ArrayList<>();
        }
        this.dnaiList.add(dnaiListItem);
        return this;
    }

    /**
     * Get dnaiList
     * 
     * @return dnaiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DNAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDnaiList()
    {
        return dnaiList;
    }

    @JsonProperty(JSON_PROPERTY_DNAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnaiList(List<String> dnaiList)
    {
        this.dnaiList = dnaiList;
    }

    public PsaInformation ueIpv6Prefix(String ueIpv6Prefix)
    {

        this.ueIpv6Prefix = ueIpv6Prefix;
        return this;
    }

    /**
     * Get ueIpv6Prefix
     * 
     * @return ueIpv6Prefix
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_IPV6_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeIpv6Prefix()
    {
        return ueIpv6Prefix;
    }

    @JsonProperty(JSON_PROPERTY_UE_IPV6_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpv6Prefix(String ueIpv6Prefix)
    {
        this.ueIpv6Prefix = ueIpv6Prefix;
    }

    public PsaInformation psaUpfId(UUID psaUpfId)
    {

        this.psaUpfId = psaUpfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return psaUpfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_PSA_UPF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPsaUpfId()
    {
        return psaUpfId;
    }

    @JsonProperty(JSON_PROPERTY_PSA_UPF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPsaUpfId(UUID psaUpfId)
    {
        this.psaUpfId = psaUpfId;
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
        PsaInformation psaInformation = (PsaInformation) o;
        return Objects.equals(this.psaInd, psaInformation.psaInd) && Objects.equals(this.dnaiList, psaInformation.dnaiList)
               && Objects.equals(this.ueIpv6Prefix, psaInformation.ueIpv6Prefix) && Objects.equals(this.psaUpfId, psaInformation.psaUpfId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(psaInd, dnaiList, ueIpv6Prefix, psaUpfId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PsaInformation {\n");
        sb.append("    psaInd: ").append(toIndentedString(psaInd)).append("\n");
        sb.append("    dnaiList: ").append(toIndentedString(dnaiList)).append("\n");
        sb.append("    ueIpv6Prefix: ").append(toIndentedString(ueIpv6Prefix)).append("\n");
        sb.append("    psaUpfId: ").append(toIndentedString(psaUpfId)).append("\n");
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
