/*
 * N32 Handshake API
 * N32-c Handshake Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29573.n32.handshake;

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
 * The protection policy to be negotiated between the SEPPs
 */
@ApiModel(description = "The protection policy to be negotiated between the SEPPs")
@JsonPropertyOrder({ ProtectionPolicy.JSON_PROPERTY_API_IE_MAPPING_LIST, ProtectionPolicy.JSON_PROPERTY_DATA_TYPE_ENC_POLICY })
public class ProtectionPolicy
{
    public static final String JSON_PROPERTY_API_IE_MAPPING_LIST = "apiIeMappingList";
    private List<ApiIeMapping> apiIeMappingList = new ArrayList<>();

    public static final String JSON_PROPERTY_DATA_TYPE_ENC_POLICY = "dataTypeEncPolicy";
    private List<String> dataTypeEncPolicy = null;

    public ProtectionPolicy()
    {
    }

    public ProtectionPolicy apiIeMappingList(List<ApiIeMapping> apiIeMappingList)
    {

        this.apiIeMappingList = apiIeMappingList;
        return this;
    }

    public ProtectionPolicy addApiIeMappingListItem(ApiIeMapping apiIeMappingListItem)
    {
        this.apiIeMappingList.add(apiIeMappingListItem);
        return this;
    }

    /**
     * Get apiIeMappingList
     * 
     * @return apiIeMappingList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_API_IE_MAPPING_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<ApiIeMapping> getApiIeMappingList()
    {
        return apiIeMappingList;
    }

    @JsonProperty(JSON_PROPERTY_API_IE_MAPPING_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setApiIeMappingList(List<ApiIeMapping> apiIeMappingList)
    {
        this.apiIeMappingList = apiIeMappingList;
    }

    public ProtectionPolicy dataTypeEncPolicy(List<String> dataTypeEncPolicy)
    {

        this.dataTypeEncPolicy = dataTypeEncPolicy;
        return this;
    }

    public ProtectionPolicy addDataTypeEncPolicyItem(String dataTypeEncPolicyItem)
    {
        if (this.dataTypeEncPolicy == null)
        {
            this.dataTypeEncPolicy = new ArrayList<>();
        }
        this.dataTypeEncPolicy.add(dataTypeEncPolicyItem);
        return this;
    }

    /**
     * Get dataTypeEncPolicy
     * 
     * @return dataTypeEncPolicy
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DATA_TYPE_ENC_POLICY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDataTypeEncPolicy()
    {
        return dataTypeEncPolicy;
    }

    @JsonProperty(JSON_PROPERTY_DATA_TYPE_ENC_POLICY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDataTypeEncPolicy(List<String> dataTypeEncPolicy)
    {
        this.dataTypeEncPolicy = dataTypeEncPolicy;
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
        ProtectionPolicy protectionPolicy = (ProtectionPolicy) o;
        return Objects.equals(this.apiIeMappingList, protectionPolicy.apiIeMappingList)
               && Objects.equals(this.dataTypeEncPolicy, protectionPolicy.dataTypeEncPolicy);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(apiIeMappingList, dataTypeEncPolicy);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProtectionPolicy {\n");
        sb.append("    apiIeMappingList: ").append(toIndentedString(apiIeMappingList)).append("\n");
        sb.append("    dataTypeEncPolicy: ").append(toIndentedString(dataTypeEncPolicy)).append("\n");
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
