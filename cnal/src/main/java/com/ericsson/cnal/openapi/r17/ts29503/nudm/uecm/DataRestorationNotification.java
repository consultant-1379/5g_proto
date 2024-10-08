/*
 * Nudm_UECM
 * Nudm Context Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains identities representing those UEs potentially affected by a
 * data-loss event at the UDR
 */
@ApiModel(description = "Contains identities representing those UEs potentially affected by a data-loss event at the UDR")
@JsonPropertyOrder({ DataRestorationNotification.JSON_PROPERTY_LAST_REPLICATION_TIME,
                     DataRestorationNotification.JSON_PROPERTY_RECOVERY_TIME,
                     DataRestorationNotification.JSON_PROPERTY_PLMN_ID,
                     DataRestorationNotification.JSON_PROPERTY_SUPI_RANGES,
                     DataRestorationNotification.JSON_PROPERTY_GPSI_RANGES,
                     DataRestorationNotification.JSON_PROPERTY_RESET_IDS,
                     DataRestorationNotification.JSON_PROPERTY_S_NSSAI_LIST,
                     DataRestorationNotification.JSON_PROPERTY_DNN_LIST,
                     DataRestorationNotification.JSON_PROPERTY_UDM_GROUP_ID })
public class DataRestorationNotification
{
    public static final String JSON_PROPERTY_LAST_REPLICATION_TIME = "lastReplicationTime";
    private OffsetDateTime lastReplicationTime;

    public static final String JSON_PROPERTY_RECOVERY_TIME = "recoveryTime";
    private OffsetDateTime recoveryTime;

    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public static final String JSON_PROPERTY_SUPI_RANGES = "supiRanges";
    private List<SupiRange> supiRanges = null;

    public static final String JSON_PROPERTY_GPSI_RANGES = "gpsiRanges";
    private List<IdentityRange> gpsiRanges = null;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public static final String JSON_PROPERTY_S_NSSAI_LIST = "sNssaiList";
    private List<Snssai> sNssaiList = null;

    public static final String JSON_PROPERTY_DNN_LIST = "dnnList";
    private List<String> dnnList = null;

    public static final String JSON_PROPERTY_UDM_GROUP_ID = "udmGroupId";
    private String udmGroupId;

    public DataRestorationNotification()
    {
    }

    public DataRestorationNotification lastReplicationTime(OffsetDateTime lastReplicationTime)
    {

        this.lastReplicationTime = lastReplicationTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return lastReplicationTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_LAST_REPLICATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getLastReplicationTime()
    {
        return lastReplicationTime;
    }

    @JsonProperty(JSON_PROPERTY_LAST_REPLICATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastReplicationTime(OffsetDateTime lastReplicationTime)
    {
        this.lastReplicationTime = lastReplicationTime;
    }

    public DataRestorationNotification recoveryTime(OffsetDateTime recoveryTime)
    {

        this.recoveryTime = recoveryTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return recoveryTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_RECOVERY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRecoveryTime()
    {
        return recoveryTime;
    }

    @JsonProperty(JSON_PROPERTY_RECOVERY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecoveryTime(OffsetDateTime recoveryTime)
    {
        this.recoveryTime = recoveryTime;
    }

    public DataRestorationNotification plmnId(PlmnId plmnId)
    {

        this.plmnId = plmnId;
        return this;
    }

    /**
     * Get plmnId
     * 
     * @return plmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnId(PlmnId plmnId)
    {
        this.plmnId = plmnId;
    }

    public DataRestorationNotification supiRanges(List<SupiRange> supiRanges)
    {

        this.supiRanges = supiRanges;
        return this;
    }

    public DataRestorationNotification addSupiRangesItem(SupiRange supiRangesItem)
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

    public DataRestorationNotification gpsiRanges(List<IdentityRange> gpsiRanges)
    {

        this.gpsiRanges = gpsiRanges;
        return this;
    }

    public DataRestorationNotification addGpsiRangesItem(IdentityRange gpsiRangesItem)
    {
        if (this.gpsiRanges == null)
        {
            this.gpsiRanges = new ArrayList<>();
        }
        this.gpsiRanges.add(gpsiRangesItem);
        return this;
    }

    /**
     * Get gpsiRanges
     * 
     * @return gpsiRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GPSI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IdentityRange> getGpsiRanges()
    {
        return gpsiRanges;
    }

    @JsonProperty(JSON_PROPERTY_GPSI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsiRanges(List<IdentityRange> gpsiRanges)
    {
        this.gpsiRanges = gpsiRanges;
    }

    public DataRestorationNotification resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public DataRestorationNotification addResetIdsItem(String resetIdsItem)
    {
        if (this.resetIds == null)
        {
            this.resetIds = new ArrayList<>();
        }
        this.resetIds.add(resetIdsItem);
        return this;
    }

    /**
     * Get resetIds
     * 
     * @return resetIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getResetIds()
    {
        return resetIds;
    }

    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetIds(List<String> resetIds)
    {
        this.resetIds = resetIds;
    }

    public DataRestorationNotification sNssaiList(List<Snssai> sNssaiList)
    {

        this.sNssaiList = sNssaiList;
        return this;
    }

    public DataRestorationNotification addSNssaiListItem(Snssai sNssaiListItem)
    {
        if (this.sNssaiList == null)
        {
            this.sNssaiList = new ArrayList<>();
        }
        this.sNssaiList.add(sNssaiListItem);
        return this;
    }

    /**
     * Get sNssaiList
     * 
     * @return sNssaiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_NSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getsNssaiList()
    {
        return sNssaiList;
    }

    @JsonProperty(JSON_PROPERTY_S_NSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsNssaiList(List<Snssai> sNssaiList)
    {
        this.sNssaiList = sNssaiList;
    }

    public DataRestorationNotification dnnList(List<String> dnnList)
    {

        this.dnnList = dnnList;
        return this;
    }

    public DataRestorationNotification addDnnListItem(String dnnListItem)
    {
        if (this.dnnList == null)
        {
            this.dnnList = new ArrayList<>();
        }
        this.dnnList.add(dnnListItem);
        return this;
    }

    /**
     * Get dnnList
     * 
     * @return dnnList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DNN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDnnList()
    {
        return dnnList;
    }

    @JsonProperty(JSON_PROPERTY_DNN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnnList(List<String> dnnList)
    {
        this.dnnList = dnnList;
    }

    public DataRestorationNotification udmGroupId(String udmGroupId)
    {

        this.udmGroupId = udmGroupId;
        return this;
    }

    /**
     * Identifier of a group of NFs.
     * 
     * @return udmGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifier of a group of NFs.")
    @JsonProperty(JSON_PROPERTY_UDM_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUdmGroupId()
    {
        return udmGroupId;
    }

    @JsonProperty(JSON_PROPERTY_UDM_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUdmGroupId(String udmGroupId)
    {
        this.udmGroupId = udmGroupId;
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
        DataRestorationNotification dataRestorationNotification = (DataRestorationNotification) o;
        return Objects.equals(this.lastReplicationTime, dataRestorationNotification.lastReplicationTime)
               && Objects.equals(this.recoveryTime, dataRestorationNotification.recoveryTime) && Objects.equals(this.plmnId, dataRestorationNotification.plmnId)
               && Objects.equals(this.supiRanges, dataRestorationNotification.supiRanges)
               && Objects.equals(this.gpsiRanges, dataRestorationNotification.gpsiRanges) && Objects.equals(this.resetIds, dataRestorationNotification.resetIds)
               && Objects.equals(this.sNssaiList, dataRestorationNotification.sNssaiList) && Objects.equals(this.dnnList, dataRestorationNotification.dnnList)
               && Objects.equals(this.udmGroupId, dataRestorationNotification.udmGroupId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lastReplicationTime, recoveryTime, plmnId, supiRanges, gpsiRanges, resetIds, sNssaiList, dnnList, udmGroupId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DataRestorationNotification {\n");
        sb.append("    lastReplicationTime: ").append(toIndentedString(lastReplicationTime)).append("\n");
        sb.append("    recoveryTime: ").append(toIndentedString(recoveryTime)).append("\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    supiRanges: ").append(toIndentedString(supiRanges)).append("\n");
        sb.append("    gpsiRanges: ").append(toIndentedString(gpsiRanges)).append("\n");
        sb.append("    resetIds: ").append(toIndentedString(resetIds)).append("\n");
        sb.append("    sNssaiList: ").append(toIndentedString(sNssaiList)).append("\n");
        sb.append("    dnnList: ").append(toIndentedString(dnnList)).append("\n");
        sb.append("    udmGroupId: ").append(toIndentedString(udmGroupId)).append("\n");
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
