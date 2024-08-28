/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Information related to UDSF
 */
@ApiModel(description = "Information related to UDSF")
@JsonPropertyOrder({ UdsfInfo.JSON_PROPERTY_GROUP_ID, UdsfInfo.JSON_PROPERTY_SUPI_RANGES, UdsfInfo.JSON_PROPERTY_STORAGE_ID_RANGES })
public class UdsfInfo
{
    public static final String JSON_PROPERTY_GROUP_ID = "groupId";
    private String groupId;

    public static final String JSON_PROPERTY_SUPI_RANGES = "supiRanges";
    private List<SupiRange> supiRanges = null;

    public static final String JSON_PROPERTY_STORAGE_ID_RANGES = "storageIdRanges";
    private Map<String, List<IdentityRange>> storageIdRanges = null;

    public UdsfInfo()
    {
    }

    public UdsfInfo groupId(String groupId)
    {

        this.groupId = groupId;
        return this;
    }

    /**
     * Identifier of a group of NFs.
     * 
     * @return groupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifier of a group of NFs.")
    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGroupId()
    {
        return groupId;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public UdsfInfo supiRanges(List<SupiRange> supiRanges)
    {

        this.supiRanges = supiRanges;
        return this;
    }

    public UdsfInfo addSupiRangesItem(SupiRange supiRangesItem)
    {
        if (this.supiRanges == null)
        {
            this.supiRanges = new ArrayList<>();
        }
        this.supiRanges.add(supiRangesItem);
        return this;
    }

    /**
     * Get supiRanges
     * 
     * @return supiRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SupiRange> getSupiRanges()
    {
        return supiRanges;
    }

    @JsonProperty(JSON_PROPERTY_SUPI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupiRanges(List<SupiRange> supiRanges)
    {
        this.supiRanges = supiRanges;
    }

    public UdsfInfo storageIdRanges(Map<String, List<IdentityRange>> storageIdRanges)
    {

        this.storageIdRanges = storageIdRanges;
        return this;
    }

    public UdsfInfo putStorageIdRangesItem(String key,
                                           List<IdentityRange> storageIdRangesItem)
    {
        if (this.storageIdRanges == null)
        {
            this.storageIdRanges = new HashMap<>();
        }
        this.storageIdRanges.put(key, storageIdRangesItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where realmId serves as key and each value in
     * the map is an array of IdentityRanges. Each IdentityRange is a range of
     * storageIds.
     * 
     * @return storageIdRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs) where realmId serves as key and each value in the map is an array of IdentityRanges. Each IdentityRange is a range of storageIds. ")
    @JsonProperty(JSON_PROPERTY_STORAGE_ID_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, List<IdentityRange>> getStorageIdRanges()
    {
        return storageIdRanges;
    }

    @JsonProperty(JSON_PROPERTY_STORAGE_ID_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStorageIdRanges(Map<String, List<IdentityRange>> storageIdRanges)
    {
        this.storageIdRanges = storageIdRanges;
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
        UdsfInfo udsfInfo = (UdsfInfo) o;
        return Objects.equals(this.groupId, udsfInfo.groupId) && Objects.equals(this.supiRanges, udsfInfo.supiRanges)
               && Objects.equals(this.storageIdRanges, udsfInfo.storageIdRanges);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(groupId, supiRanges, storageIdRanges);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UdsfInfo {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    supiRanges: ").append(toIndentedString(supiRanges)).append("\n");
        sb.append("    storageIdRanges: ").append(toIndentedString(storageIdRanges)).append("\n");
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
