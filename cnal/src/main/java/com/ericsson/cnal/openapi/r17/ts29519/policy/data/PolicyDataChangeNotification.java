/*
 * Unified Data Repository Service API file for policy data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.policy.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29505.subscription.data.OperatorSpecificDataContainer;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
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
 * Contains changed policy data for which notification was requested.
 */
@ApiModel(description = "Contains changed policy data for which notification was requested.")
@JsonPropertyOrder({ PolicyDataChangeNotification.JSON_PROPERTY_AM_POLICY_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_UE_POLICY_SET,
                     PolicyDataChangeNotification.JSON_PROPERTY_PLMN_UE_POLICY_SET,
                     PolicyDataChangeNotification.JSON_PROPERTY_SM_POLICY_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_USAGE_MON_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_SPONSOR_CONNECTIVITY_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_BDT_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_OP_SPEC_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_OP_SPEC_DATA_MAP,
                     PolicyDataChangeNotification.JSON_PROPERTY_UE_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_SPONSOR_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_BDT_REF_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_USAGE_MON_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_PLMN_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_DEL_RESOURCES,
                     PolicyDataChangeNotification.JSON_PROPERTY_NOTIF_ID,
                     PolicyDataChangeNotification.JSON_PROPERTY_REPORTED_FRAGMENTS,
                     PolicyDataChangeNotification.JSON_PROPERTY_SLICE_POLICY_DATA,
                     PolicyDataChangeNotification.JSON_PROPERTY_SNSSAI })
public class PolicyDataChangeNotification
{
    public static final String JSON_PROPERTY_AM_POLICY_DATA = "amPolicyData";
    private AmPolicyData amPolicyData;

    public static final String JSON_PROPERTY_UE_POLICY_SET = "uePolicySet";
    private UePolicySet uePolicySet;

    public static final String JSON_PROPERTY_PLMN_UE_POLICY_SET = "plmnUePolicySet";
    private UePolicySet plmnUePolicySet;

    public static final String JSON_PROPERTY_SM_POLICY_DATA = "smPolicyData";
    private SmPolicyData smPolicyData;

    public static final String JSON_PROPERTY_USAGE_MON_DATA = "usageMonData";
    private UsageMonData usageMonData;

    public static final String JSON_PROPERTY_SPONSOR_CONNECTIVITY_DATA = "SponsorConnectivityData";
    private SponsorConnectivityData sponsorConnectivityData;

    public static final String JSON_PROPERTY_BDT_DATA = "bdtData";
    private BdtData bdtData;

    public static final String JSON_PROPERTY_OP_SPEC_DATA = "opSpecData";
    private OperatorSpecificDataContainer opSpecData;

    public static final String JSON_PROPERTY_OP_SPEC_DATA_MAP = "opSpecDataMap";
    private Map<String, OperatorSpecificDataContainer> opSpecDataMap = null;

    public static final String JSON_PROPERTY_UE_ID = "ueId";
    private String ueId;

    public static final String JSON_PROPERTY_SPONSOR_ID = "sponsorId";
    private String sponsorId;

    public static final String JSON_PROPERTY_BDT_REF_ID = "bdtRefId";
    private String bdtRefId;

    public static final String JSON_PROPERTY_USAGE_MON_ID = "usageMonId";
    private String usageMonId;

    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public static final String JSON_PROPERTY_DEL_RESOURCES = "delResources";
    private List<String> delResources = null;

    public static final String JSON_PROPERTY_NOTIF_ID = "notifId";
    private String notifId;

    public static final String JSON_PROPERTY_REPORTED_FRAGMENTS = "reportedFragments";
    private List<NotificationItem> reportedFragments = null;

    public static final String JSON_PROPERTY_SLICE_POLICY_DATA = "slicePolicyData";
    private SlicePolicyData slicePolicyData;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public PolicyDataChangeNotification()
    {
    }

    public PolicyDataChangeNotification amPolicyData(AmPolicyData amPolicyData)
    {

        this.amPolicyData = amPolicyData;
        return this;
    }

    /**
     * Get amPolicyData
     * 
     * @return amPolicyData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AM_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmPolicyData getAmPolicyData()
    {
        return amPolicyData;
    }

    @JsonProperty(JSON_PROPERTY_AM_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmPolicyData(AmPolicyData amPolicyData)
    {
        this.amPolicyData = amPolicyData;
    }

    public PolicyDataChangeNotification uePolicySet(UePolicySet uePolicySet)
    {

        this.uePolicySet = uePolicySet;
        return this;
    }

    /**
     * Get uePolicySet
     * 
     * @return uePolicySet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_POLICY_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UePolicySet getUePolicySet()
    {
        return uePolicySet;
    }

    @JsonProperty(JSON_PROPERTY_UE_POLICY_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUePolicySet(UePolicySet uePolicySet)
    {
        this.uePolicySet = uePolicySet;
    }

    public PolicyDataChangeNotification plmnUePolicySet(UePolicySet plmnUePolicySet)
    {

        this.plmnUePolicySet = plmnUePolicySet;
        return this;
    }

    /**
     * Get plmnUePolicySet
     * 
     * @return plmnUePolicySet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_UE_POLICY_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UePolicySet getPlmnUePolicySet()
    {
        return plmnUePolicySet;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_UE_POLICY_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnUePolicySet(UePolicySet plmnUePolicySet)
    {
        this.plmnUePolicySet = plmnUePolicySet;
    }

    public PolicyDataChangeNotification smPolicyData(SmPolicyData smPolicyData)
    {

        this.smPolicyData = smPolicyData;
        return this;
    }

    /**
     * Get smPolicyData
     * 
     * @return smPolicyData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SM_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmPolicyData getSmPolicyData()
    {
        return smPolicyData;
    }

    @JsonProperty(JSON_PROPERTY_SM_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmPolicyData(SmPolicyData smPolicyData)
    {
        this.smPolicyData = smPolicyData;
    }

    public PolicyDataChangeNotification usageMonData(UsageMonData usageMonData)
    {

        this.usageMonData = usageMonData;
        return this;
    }

    /**
     * Get usageMonData
     * 
     * @return usageMonData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USAGE_MON_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UsageMonData getUsageMonData()
    {
        return usageMonData;
    }

    @JsonProperty(JSON_PROPERTY_USAGE_MON_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsageMonData(UsageMonData usageMonData)
    {
        this.usageMonData = usageMonData;
    }

    public PolicyDataChangeNotification sponsorConnectivityData(SponsorConnectivityData sponsorConnectivityData)
    {

        this.sponsorConnectivityData = sponsorConnectivityData;
        return this;
    }

    /**
     * Get sponsorConnectivityData
     * 
     * @return sponsorConnectivityData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SPONSOR_CONNECTIVITY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SponsorConnectivityData getSponsorConnectivityData()
    {
        return sponsorConnectivityData;
    }

    @JsonProperty(JSON_PROPERTY_SPONSOR_CONNECTIVITY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSponsorConnectivityData(SponsorConnectivityData sponsorConnectivityData)
    {
        this.sponsorConnectivityData = sponsorConnectivityData;
    }

    public PolicyDataChangeNotification bdtData(BdtData bdtData)
    {

        this.bdtData = bdtData;
        return this;
    }

    /**
     * Get bdtData
     * 
     * @return bdtData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BDT_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BdtData getBdtData()
    {
        return bdtData;
    }

    @JsonProperty(JSON_PROPERTY_BDT_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtData(BdtData bdtData)
    {
        this.bdtData = bdtData;
    }

    public PolicyDataChangeNotification opSpecData(OperatorSpecificDataContainer opSpecData)
    {

        this.opSpecData = opSpecData;
        return this;
    }

    /**
     * Get opSpecData
     * 
     * @return opSpecData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_OP_SPEC_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OperatorSpecificDataContainer getOpSpecData()
    {
        return opSpecData;
    }

    @JsonProperty(JSON_PROPERTY_OP_SPEC_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpSpecData(OperatorSpecificDataContainer opSpecData)
    {
        this.opSpecData = opSpecData;
    }

    public PolicyDataChangeNotification opSpecDataMap(Map<String, OperatorSpecificDataContainer> opSpecDataMap)
    {

        this.opSpecDataMap = opSpecDataMap;
        return this;
    }

    public PolicyDataChangeNotification putOpSpecDataMapItem(String key,
                                                             OperatorSpecificDataContainer opSpecDataMapItem)
    {
        if (this.opSpecDataMap == null)
        {
            this.opSpecDataMap = new HashMap<>();
        }
        this.opSpecDataMap.put(key, opSpecDataMapItem);
        return this;
    }

    /**
     * Operator Specific Data resource data, if changed and notification was
     * requested. The key of the map is operator specific data element name and the
     * value is the operator specific data of the UE.
     * 
     * @return opSpecDataMap
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Operator Specific Data resource data, if changed and notification was requested. The key of the map is operator specific data element name and the value is the operator specific data of the UE. ")
    @JsonProperty(JSON_PROPERTY_OP_SPEC_DATA_MAP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, OperatorSpecificDataContainer> getOpSpecDataMap()
    {
        return opSpecDataMap;
    }

    @JsonProperty(JSON_PROPERTY_OP_SPEC_DATA_MAP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpSpecDataMap(Map<String, OperatorSpecificDataContainer> opSpecDataMap)
    {
        this.opSpecDataMap = opSpecDataMap;
    }

    public PolicyDataChangeNotification ueId(String ueId)
    {

        this.ueId = ueId;
        return this;
    }

    /**
     * String represents the SUPI or GPSI
     * 
     * @return ueId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String represents the SUPI or GPSI")
    @JsonProperty(JSON_PROPERTY_UE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeId()
    {
        return ueId;
    }

    @JsonProperty(JSON_PROPERTY_UE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeId(String ueId)
    {
        this.ueId = ueId;
    }

    public PolicyDataChangeNotification sponsorId(String sponsorId)
    {

        this.sponsorId = sponsorId;
        return this;
    }

    /**
     * Get sponsorId
     * 
     * @return sponsorId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SPONSOR_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSponsorId()
    {
        return sponsorId;
    }

    @JsonProperty(JSON_PROPERTY_SPONSOR_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSponsorId(String sponsorId)
    {
        this.sponsorId = sponsorId;
    }

    public PolicyDataChangeNotification bdtRefId(String bdtRefId)
    {

        this.bdtRefId = bdtRefId;
        return this;
    }

    /**
     * string identifying a BDT Reference ID as defined in clause 5.3.3 of 3GPP TS
     * 29.154.
     * 
     * @return bdtRefId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string identifying a BDT Reference ID as defined in clause 5.3.3 of 3GPP TS 29.154.")
    @JsonProperty(JSON_PROPERTY_BDT_REF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBdtRefId()
    {
        return bdtRefId;
    }

    @JsonProperty(JSON_PROPERTY_BDT_REF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtRefId(String bdtRefId)
    {
        this.bdtRefId = bdtRefId;
    }

    public PolicyDataChangeNotification usageMonId(String usageMonId)
    {

        this.usageMonId = usageMonId;
        return this;
    }

    /**
     * Get usageMonId
     * 
     * @return usageMonId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USAGE_MON_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUsageMonId()
    {
        return usageMonId;
    }

    @JsonProperty(JSON_PROPERTY_USAGE_MON_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsageMonId(String usageMonId)
    {
        this.usageMonId = usageMonId;
    }

    public PolicyDataChangeNotification plmnId(PlmnId plmnId)
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

    public PolicyDataChangeNotification delResources(List<String> delResources)
    {

        this.delResources = delResources;
        return this;
    }

    public PolicyDataChangeNotification addDelResourcesItem(String delResourcesItem)
    {
        if (this.delResources == null)
        {
            this.delResources = new ArrayList<>();
        }
        this.delResources.add(delResourcesItem);
        return this;
    }

    /**
     * Get delResources
     * 
     * @return delResources
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DEL_RESOURCES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDelResources()
    {
        return delResources;
    }

    @JsonProperty(JSON_PROPERTY_DEL_RESOURCES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDelResources(List<String> delResources)
    {
        this.delResources = delResources;
    }

    public PolicyDataChangeNotification notifId(String notifId)
    {

        this.notifId = notifId;
        return this;
    }

    /**
     * Get notifId
     * 
     * @return notifId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNotifId()
    {
        return notifId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifId(String notifId)
    {
        this.notifId = notifId;
    }

    public PolicyDataChangeNotification reportedFragments(List<NotificationItem> reportedFragments)
    {

        this.reportedFragments = reportedFragments;
        return this;
    }

    public PolicyDataChangeNotification addReportedFragmentsItem(NotificationItem reportedFragmentsItem)
    {
        if (this.reportedFragments == null)
        {
            this.reportedFragments = new ArrayList<>();
        }
        this.reportedFragments.add(reportedFragmentsItem);
        return this;
    }

    /**
     * Get reportedFragments
     * 
     * @return reportedFragments
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REPORTED_FRAGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NotificationItem> getReportedFragments()
    {
        return reportedFragments;
    }

    @JsonProperty(JSON_PROPERTY_REPORTED_FRAGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportedFragments(List<NotificationItem> reportedFragments)
    {
        this.reportedFragments = reportedFragments;
    }

    public PolicyDataChangeNotification slicePolicyData(SlicePolicyData slicePolicyData)
    {

        this.slicePolicyData = slicePolicyData;
        return this;
    }

    /**
     * Get slicePolicyData
     * 
     * @return slicePolicyData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SLICE_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SlicePolicyData getSlicePolicyData()
    {
        return slicePolicyData;
    }

    @JsonProperty(JSON_PROPERTY_SLICE_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSlicePolicyData(SlicePolicyData slicePolicyData)
    {
        this.slicePolicyData = slicePolicyData;
    }

    public PolicyDataChangeNotification snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
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
        PolicyDataChangeNotification policyDataChangeNotification = (PolicyDataChangeNotification) o;
        return Objects.equals(this.amPolicyData, policyDataChangeNotification.amPolicyData)
               && Objects.equals(this.uePolicySet, policyDataChangeNotification.uePolicySet)
               && Objects.equals(this.plmnUePolicySet, policyDataChangeNotification.plmnUePolicySet)
               && Objects.equals(this.smPolicyData, policyDataChangeNotification.smPolicyData)
               && Objects.equals(this.usageMonData, policyDataChangeNotification.usageMonData)
               && Objects.equals(this.sponsorConnectivityData, policyDataChangeNotification.sponsorConnectivityData)
               && Objects.equals(this.bdtData, policyDataChangeNotification.bdtData) && Objects.equals(this.opSpecData, policyDataChangeNotification.opSpecData)
               && Objects.equals(this.opSpecDataMap, policyDataChangeNotification.opSpecDataMap) && Objects.equals(this.ueId, policyDataChangeNotification.ueId)
               && Objects.equals(this.sponsorId, policyDataChangeNotification.sponsorId) && Objects.equals(this.bdtRefId, policyDataChangeNotification.bdtRefId)
               && Objects.equals(this.usageMonId, policyDataChangeNotification.usageMonId) && Objects.equals(this.plmnId, policyDataChangeNotification.plmnId)
               && Objects.equals(this.delResources, policyDataChangeNotification.delResources)
               && Objects.equals(this.notifId, policyDataChangeNotification.notifId)
               && Objects.equals(this.reportedFragments, policyDataChangeNotification.reportedFragments)
               && Objects.equals(this.slicePolicyData, policyDataChangeNotification.slicePolicyData)
               && Objects.equals(this.snssai, policyDataChangeNotification.snssai);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(amPolicyData,
                            uePolicySet,
                            plmnUePolicySet,
                            smPolicyData,
                            usageMonData,
                            sponsorConnectivityData,
                            bdtData,
                            opSpecData,
                            opSpecDataMap,
                            ueId,
                            sponsorId,
                            bdtRefId,
                            usageMonId,
                            plmnId,
                            delResources,
                            notifId,
                            reportedFragments,
                            slicePolicyData,
                            snssai);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyDataChangeNotification {\n");
        sb.append("    amPolicyData: ").append(toIndentedString(amPolicyData)).append("\n");
        sb.append("    uePolicySet: ").append(toIndentedString(uePolicySet)).append("\n");
        sb.append("    plmnUePolicySet: ").append(toIndentedString(plmnUePolicySet)).append("\n");
        sb.append("    smPolicyData: ").append(toIndentedString(smPolicyData)).append("\n");
        sb.append("    usageMonData: ").append(toIndentedString(usageMonData)).append("\n");
        sb.append("    sponsorConnectivityData: ").append(toIndentedString(sponsorConnectivityData)).append("\n");
        sb.append("    bdtData: ").append(toIndentedString(bdtData)).append("\n");
        sb.append("    opSpecData: ").append(toIndentedString(opSpecData)).append("\n");
        sb.append("    opSpecDataMap: ").append(toIndentedString(opSpecDataMap)).append("\n");
        sb.append("    ueId: ").append(toIndentedString(ueId)).append("\n");
        sb.append("    sponsorId: ").append(toIndentedString(sponsorId)).append("\n");
        sb.append("    bdtRefId: ").append(toIndentedString(bdtRefId)).append("\n");
        sb.append("    usageMonId: ").append(toIndentedString(usageMonId)).append("\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    delResources: ").append(toIndentedString(delResources)).append("\n");
        sb.append("    notifId: ").append(toIndentedString(notifId)).append("\n");
        sb.append("    reportedFragments: ").append(toIndentedString(reportedFragments)).append("\n");
        sb.append("    slicePolicyData: ").append(toIndentedString(slicePolicyData)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
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
