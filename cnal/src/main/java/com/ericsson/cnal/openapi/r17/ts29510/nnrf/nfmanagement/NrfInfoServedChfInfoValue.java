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
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.PlmnRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
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
 * NrfInfoServedChfInfoValue
 */
@JsonPropertyOrder({ NrfInfoServedChfInfoValue.JSON_PROPERTY_SUPI_RANGE_LIST,
                     NrfInfoServedChfInfoValue.JSON_PROPERTY_GPSI_RANGE_LIST,
                     NrfInfoServedChfInfoValue.JSON_PROPERTY_PLMN_RANGE_LIST,
                     NrfInfoServedChfInfoValue.JSON_PROPERTY_GROUP_ID,
                     NrfInfoServedChfInfoValue.JSON_PROPERTY_PRIMARY_CHF_INSTANCE,
                     NrfInfoServedChfInfoValue.JSON_PROPERTY_SECONDARY_CHF_INSTANCE })
@JsonTypeName("NrfInfo_servedChfInfo_value")
public class NrfInfoServedChfInfoValue
{
    public static final String JSON_PROPERTY_SUPI_RANGE_LIST = "supiRangeList";
    private List<SupiRange> supiRangeList = null;

    public static final String JSON_PROPERTY_GPSI_RANGE_LIST = "gpsiRangeList";
    private List<IdentityRange> gpsiRangeList = null;

    public static final String JSON_PROPERTY_PLMN_RANGE_LIST = "plmnRangeList";
    private List<PlmnRange> plmnRangeList = null;

    public static final String JSON_PROPERTY_GROUP_ID = "groupId";
    private String groupId;

    public static final String JSON_PROPERTY_PRIMARY_CHF_INSTANCE = "primaryChfInstance";
    private UUID primaryChfInstance;

    public static final String JSON_PROPERTY_SECONDARY_CHF_INSTANCE = "secondaryChfInstance";
    private UUID secondaryChfInstance;

    public NrfInfoServedChfInfoValue()
    {
    }

    public NrfInfoServedChfInfoValue supiRangeList(List<SupiRange> supiRangeList)
    {

        this.supiRangeList = supiRangeList;
        return this;
    }

    public NrfInfoServedChfInfoValue addSupiRangeListItem(SupiRange supiRangeListItem)
    {
        if (this.supiRangeList == null)
        {
            this.supiRangeList = new ArrayList<>();
        }
        this.supiRangeList.add(supiRangeListItem);
        return this;
    }

    /**
     * Get supiRangeList
     * 
     * @return supiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SupiRange> getSupiRangeList()
    {
        return supiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_SUPI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupiRangeList(List<SupiRange> supiRangeList)
    {
        this.supiRangeList = supiRangeList;
    }

    public NrfInfoServedChfInfoValue gpsiRangeList(List<IdentityRange> gpsiRangeList)
    {

        this.gpsiRangeList = gpsiRangeList;
        return this;
    }

    public NrfInfoServedChfInfoValue addGpsiRangeListItem(IdentityRange gpsiRangeListItem)
    {
        if (this.gpsiRangeList == null)
        {
            this.gpsiRangeList = new ArrayList<>();
        }
        this.gpsiRangeList.add(gpsiRangeListItem);
        return this;
    }

    /**
     * Get gpsiRangeList
     * 
     * @return gpsiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GPSI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IdentityRange> getGpsiRangeList()
    {
        return gpsiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_GPSI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsiRangeList(List<IdentityRange> gpsiRangeList)
    {
        this.gpsiRangeList = gpsiRangeList;
    }

    public NrfInfoServedChfInfoValue plmnRangeList(List<PlmnRange> plmnRangeList)
    {

        this.plmnRangeList = plmnRangeList;
        return this;
    }

    public NrfInfoServedChfInfoValue addPlmnRangeListItem(PlmnRange plmnRangeListItem)
    {
        if (this.plmnRangeList == null)
        {
            this.plmnRangeList = new ArrayList<>();
        }
        this.plmnRangeList.add(plmnRangeListItem);
        return this;
    }

    /**
     * Get plmnRangeList
     * 
     * @return plmnRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnRange> getPlmnRangeList()
    {
        return plmnRangeList;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnRangeList(List<PlmnRange> plmnRangeList)
    {
        this.plmnRangeList = plmnRangeList;
    }

    public NrfInfoServedChfInfoValue groupId(String groupId)
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

    public NrfInfoServedChfInfoValue primaryChfInstance(UUID primaryChfInstance)
    {

        this.primaryChfInstance = primaryChfInstance;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return primaryChfInstance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_PRIMARY_CHF_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPrimaryChfInstance()
    {
        return primaryChfInstance;
    }

    @JsonProperty(JSON_PROPERTY_PRIMARY_CHF_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryChfInstance(UUID primaryChfInstance)
    {
        this.primaryChfInstance = primaryChfInstance;
    }

    public NrfInfoServedChfInfoValue secondaryChfInstance(UUID secondaryChfInstance)
    {

        this.secondaryChfInstance = secondaryChfInstance;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return secondaryChfInstance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_SECONDARY_CHF_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getSecondaryChfInstance()
    {
        return secondaryChfInstance;
    }

    @JsonProperty(JSON_PROPERTY_SECONDARY_CHF_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecondaryChfInstance(UUID secondaryChfInstance)
    {
        this.secondaryChfInstance = secondaryChfInstance;
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
        NrfInfoServedChfInfoValue nrfInfoServedChfInfoValue = (NrfInfoServedChfInfoValue) o;
        return Objects.equals(this.supiRangeList, nrfInfoServedChfInfoValue.supiRangeList)
               && Objects.equals(this.gpsiRangeList, nrfInfoServedChfInfoValue.gpsiRangeList)
               && Objects.equals(this.plmnRangeList, nrfInfoServedChfInfoValue.plmnRangeList) && Objects.equals(this.groupId, nrfInfoServedChfInfoValue.groupId)
               && Objects.equals(this.primaryChfInstance, nrfInfoServedChfInfoValue.primaryChfInstance)
               && Objects.equals(this.secondaryChfInstance, nrfInfoServedChfInfoValue.secondaryChfInstance);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supiRangeList, gpsiRangeList, plmnRangeList, groupId, primaryChfInstance, secondaryChfInstance);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NrfInfoServedChfInfoValue {\n");
        sb.append("    supiRangeList: ").append(toIndentedString(supiRangeList)).append("\n");
        sb.append("    gpsiRangeList: ").append(toIndentedString(gpsiRangeList)).append("\n");
        sb.append("    plmnRangeList: ").append(toIndentedString(plmnRangeList)).append("\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    primaryChfInstance: ").append(toIndentedString(primaryChfInstance)).append("\n");
        sb.append("    secondaryChfInstance: ").append(toIndentedString(secondaryChfInstance)).append("\n");
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
