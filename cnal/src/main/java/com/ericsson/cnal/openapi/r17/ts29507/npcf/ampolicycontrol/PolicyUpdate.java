/*
 * Npcf_AMPolicyControl
 * Access and Mobility Policy Control Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29507.npcf.ampolicycontrol;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.WirelineServiceAreaRestriction;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PresenceInfoRm;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ServiceAreaRestriction;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PcfUeCallbackInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PduSessionInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ambr;
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
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents updated policies that the PCF provides in a notification or in a
 * reply to an Update Request.
 */
@ApiModel(description = "Represents updated policies that the PCF provides in a notification or in a reply to an Update Request. ")
@JsonPropertyOrder({ PolicyUpdate.JSON_PROPERTY_RESOURCE_URI,
                     PolicyUpdate.JSON_PROPERTY_TRIGGERS,
                     PolicyUpdate.JSON_PROPERTY_SERV_AREA_RES,
                     PolicyUpdate.JSON_PROPERTY_WL_SERV_AREA_RES,
                     PolicyUpdate.JSON_PROPERTY_RFSP,
                     PolicyUpdate.JSON_PROPERTY_TARGET_RFSP,
                     PolicyUpdate.JSON_PROPERTY_SMF_SEL_INFO,
                     PolicyUpdate.JSON_PROPERTY_UE_AMBR,
                     PolicyUpdate.JSON_PROPERTY_UE_SLICE_MBRS,
                     PolicyUpdate.JSON_PROPERTY_PRAS,
                     PolicyUpdate.JSON_PROPERTY_PCF_UE_INFO,
                     PolicyUpdate.JSON_PROPERTY_MATCH_PDUS,
                     PolicyUpdate.JSON_PROPERTY_AS_TIME_DIS_PARAM })
public class PolicyUpdate
{
    public static final String JSON_PROPERTY_RESOURCE_URI = "resourceUri";
    private String resourceUri;

    public static final String JSON_PROPERTY_TRIGGERS = "triggers";
    private JsonNullable<List<String>> triggers = JsonNullable.<List<String>>undefined();

    public static final String JSON_PROPERTY_SERV_AREA_RES = "servAreaRes";
    private ServiceAreaRestriction servAreaRes;

    public static final String JSON_PROPERTY_WL_SERV_AREA_RES = "wlServAreaRes";
    private WirelineServiceAreaRestriction wlServAreaRes;

    public static final String JSON_PROPERTY_RFSP = "rfsp";
    private Integer rfsp;

    public static final String JSON_PROPERTY_TARGET_RFSP = "targetRfsp";
    private Integer targetRfsp;

    public static final String JSON_PROPERTY_SMF_SEL_INFO = "smfSelInfo";
    private JsonNullable<SmfSelectionData> smfSelInfo = JsonNullable.<SmfSelectionData>undefined();

    public static final String JSON_PROPERTY_UE_AMBR = "ueAmbr";
    private Ambr ueAmbr;

    public static final String JSON_PROPERTY_UE_SLICE_MBRS = "ueSliceMbrs";
    private List<UeSliceMbr> ueSliceMbrs = null;

    public static final String JSON_PROPERTY_PRAS = "pras";
    private JsonNullable<Map<String, PresenceInfoRm>> pras = JsonNullable.<Map<String, PresenceInfoRm>>undefined();

    public static final String JSON_PROPERTY_PCF_UE_INFO = "pcfUeInfo";
    private JsonNullable<PcfUeCallbackInfo> pcfUeInfo = JsonNullable.<PcfUeCallbackInfo>undefined();

    public static final String JSON_PROPERTY_MATCH_PDUS = "matchPdus";
    private JsonNullable<List<PduSessionInfo>> matchPdus = JsonNullable.<List<PduSessionInfo>>undefined();

    public static final String JSON_PROPERTY_AS_TIME_DIS_PARAM = "asTimeDisParam";
    private JsonNullable<AsTimeDistributionParam> asTimeDisParam = JsonNullable.<AsTimeDistributionParam>undefined();

    public PolicyUpdate()
    {
    }

    public PolicyUpdate resourceUri(String resourceUri)
    {

        this.resourceUri = resourceUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return resourceUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_RESOURCE_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getResourceUri()
    {
        return resourceUri;
    }

    @JsonProperty(JSON_PROPERTY_RESOURCE_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setResourceUri(String resourceUri)
    {
        this.resourceUri = resourceUri;
    }

    public PolicyUpdate triggers(List<String> triggers)
    {
        this.triggers = JsonNullable.<List<String>>of(triggers);

        return this;
    }

    public PolicyUpdate addTriggersItem(String triggersItem)
    {
        if (this.triggers == null || !this.triggers.isPresent())
        {
            this.triggers = JsonNullable.<List<String>>of(new ArrayList<>());
        }
        try
        {
            this.triggers.get().add(triggersItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * Request Triggers that the PCF subscribes.
     * 
     * @return triggers
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Request Triggers that the PCF subscribes.")
    @JsonIgnore

    public List<String> getTriggers()
    {
        return triggers.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<String>> getTriggers_JsonNullable()
    {
        return triggers;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    public void setTriggers_JsonNullable(JsonNullable<List<String>> triggers)
    {
        this.triggers = triggers;
    }

    public void setTriggers(List<String> triggers)
    {
        this.triggers = JsonNullable.<List<String>>of(triggers);
    }

    public PolicyUpdate servAreaRes(ServiceAreaRestriction servAreaRes)
    {

        this.servAreaRes = servAreaRes;
        return this;
    }

    /**
     * Get servAreaRes
     * 
     * @return servAreaRes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERV_AREA_RES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ServiceAreaRestriction getServAreaRes()
    {
        return servAreaRes;
    }

    @JsonProperty(JSON_PROPERTY_SERV_AREA_RES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServAreaRes(ServiceAreaRestriction servAreaRes)
    {
        this.servAreaRes = servAreaRes;
    }

    public PolicyUpdate wlServAreaRes(WirelineServiceAreaRestriction wlServAreaRes)
    {

        this.wlServAreaRes = wlServAreaRes;
        return this;
    }

    /**
     * Get wlServAreaRes
     * 
     * @return wlServAreaRes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_WL_SERV_AREA_RES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public WirelineServiceAreaRestriction getWlServAreaRes()
    {
        return wlServAreaRes;
    }

    @JsonProperty(JSON_PROPERTY_WL_SERV_AREA_RES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWlServAreaRes(WirelineServiceAreaRestriction wlServAreaRes)
    {
        this.wlServAreaRes = wlServAreaRes;
    }

    public PolicyUpdate rfsp(Integer rfsp)
    {

        this.rfsp = rfsp;
        return this;
    }

    /**
     * Unsigned integer representing the \&quot;Subscriber Profile ID for
     * RAT/Frequency Priority\&quot; as specified in 3GPP TS 36.413. minimum: 1
     * maximum: 256
     * 
     * @return rfsp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer representing the \"Subscriber Profile ID for RAT/Frequency Priority\"  as specified in 3GPP TS 36.413. ")
    @JsonProperty(JSON_PROPERTY_RFSP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getRfsp()
    {
        return rfsp;
    }

    @JsonProperty(JSON_PROPERTY_RFSP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRfsp(Integer rfsp)
    {
        this.rfsp = rfsp;
    }

    public PolicyUpdate targetRfsp(Integer targetRfsp)
    {

        this.targetRfsp = targetRfsp;
        return this;
    }

    /**
     * Unsigned integer representing the \&quot;Subscriber Profile ID for
     * RAT/Frequency Priority\&quot; as specified in 3GPP TS 36.413. minimum: 1
     * maximum: 256
     * 
     * @return targetRfsp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer representing the \"Subscriber Profile ID for RAT/Frequency Priority\"  as specified in 3GPP TS 36.413. ")
    @JsonProperty(JSON_PROPERTY_TARGET_RFSP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTargetRfsp()
    {
        return targetRfsp;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_RFSP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetRfsp(Integer targetRfsp)
    {
        this.targetRfsp = targetRfsp;
    }

    public PolicyUpdate smfSelInfo(SmfSelectionData smfSelInfo)
    {
        this.smfSelInfo = JsonNullable.<SmfSelectionData>of(smfSelInfo);

        return this;
    }

    /**
     * Get smfSelInfo
     * 
     * @return smfSelInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public SmfSelectionData getSmfSelInfo()
    {
        return smfSelInfo.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_SMF_SEL_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<SmfSelectionData> getSmfSelInfo_JsonNullable()
    {
        return smfSelInfo;
    }

    @JsonProperty(JSON_PROPERTY_SMF_SEL_INFO)
    public void setSmfSelInfo_JsonNullable(JsonNullable<SmfSelectionData> smfSelInfo)
    {
        this.smfSelInfo = smfSelInfo;
    }

    public void setSmfSelInfo(SmfSelectionData smfSelInfo)
    {
        this.smfSelInfo = JsonNullable.<SmfSelectionData>of(smfSelInfo);
    }

    public PolicyUpdate ueAmbr(Ambr ueAmbr)
    {

        this.ueAmbr = ueAmbr;
        return this;
    }

    /**
     * Get ueAmbr
     * 
     * @return ueAmbr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_AMBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Ambr getUeAmbr()
    {
        return ueAmbr;
    }

    @JsonProperty(JSON_PROPERTY_UE_AMBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeAmbr(Ambr ueAmbr)
    {
        this.ueAmbr = ueAmbr;
    }

    public PolicyUpdate ueSliceMbrs(List<UeSliceMbr> ueSliceMbrs)
    {

        this.ueSliceMbrs = ueSliceMbrs;
        return this;
    }

    public PolicyUpdate addUeSliceMbrsItem(UeSliceMbr ueSliceMbrsItem)
    {
        if (this.ueSliceMbrs == null)
        {
            this.ueSliceMbrs = new ArrayList<>();
        }
        this.ueSliceMbrs.add(ueSliceMbrsItem);
        return this;
    }

    /**
     * One or more UE-Slice-MBR(s) for S-NSSAI(s) of serving PLMN the allowed NSSAI
     * as part of the AMF Access and Mobility Policy as determined by the PCF.
     * 
     * @return ueSliceMbrs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "One or more UE-Slice-MBR(s) for S-NSSAI(s) of serving PLMN the allowed NSSAI as part of the AMF Access and Mobility Policy as determined by the PCF. ")
    @JsonProperty(JSON_PROPERTY_UE_SLICE_MBRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UeSliceMbr> getUeSliceMbrs()
    {
        return ueSliceMbrs;
    }

    @JsonProperty(JSON_PROPERTY_UE_SLICE_MBRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeSliceMbrs(List<UeSliceMbr> ueSliceMbrs)
    {
        this.ueSliceMbrs = ueSliceMbrs;
    }

    public PolicyUpdate pras(Map<String, PresenceInfoRm> pras)
    {
        this.pras = JsonNullable.<Map<String, PresenceInfoRm>>of(pras);

        return this;
    }

    public PolicyUpdate putPrasItem(String key,
                                    PresenceInfoRm prasItem)
    {
        if (this.pras == null || !this.pras.isPresent())
        {
            this.pras = JsonNullable.<Map<String, PresenceInfoRm>>of(new HashMap<>());
        }
        try
        {
            this.pras.get().put(key, prasItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * Contains the presence reporting area(s) for which reporting was requested.
     * The praId attribute within the PresenceInfo data type is the key of the map.
     * 
     * @return pras
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the presence reporting area(s) for which reporting was requested. The praId attribute within the PresenceInfo data type is the key of the map. ")
    @JsonIgnore

    public Map<String, PresenceInfoRm> getPras()
    {
        return pras.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_PRAS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Map<String, PresenceInfoRm>> getPras_JsonNullable()
    {
        return pras;
    }

    @JsonProperty(JSON_PROPERTY_PRAS)
    public void setPras_JsonNullable(JsonNullable<Map<String, PresenceInfoRm>> pras)
    {
        this.pras = pras;
    }

    public void setPras(Map<String, PresenceInfoRm> pras)
    {
        this.pras = JsonNullable.<Map<String, PresenceInfoRm>>of(pras);
    }

    public PolicyUpdate pcfUeInfo(PcfUeCallbackInfo pcfUeInfo)
    {
        this.pcfUeInfo = JsonNullable.<PcfUeCallbackInfo>of(pcfUeInfo);

        return this;
    }

    /**
     * Get pcfUeInfo
     * 
     * @return pcfUeInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public PcfUeCallbackInfo getPcfUeInfo()
    {
        return pcfUeInfo.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_PCF_UE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<PcfUeCallbackInfo> getPcfUeInfo_JsonNullable()
    {
        return pcfUeInfo;
    }

    @JsonProperty(JSON_PROPERTY_PCF_UE_INFO)
    public void setPcfUeInfo_JsonNullable(JsonNullable<PcfUeCallbackInfo> pcfUeInfo)
    {
        this.pcfUeInfo = pcfUeInfo;
    }

    public void setPcfUeInfo(PcfUeCallbackInfo pcfUeInfo)
    {
        this.pcfUeInfo = JsonNullable.<PcfUeCallbackInfo>of(pcfUeInfo);
    }

    public PolicyUpdate matchPdus(List<PduSessionInfo> matchPdus)
    {
        this.matchPdus = JsonNullable.<List<PduSessionInfo>>of(matchPdus);

        return this;
    }

    public PolicyUpdate addMatchPdusItem(PduSessionInfo matchPdusItem)
    {
        if (this.matchPdus == null || !this.matchPdus.isPresent())
        {
            this.matchPdus = JsonNullable.<List<PduSessionInfo>>of(new ArrayList<>());
        }
        try
        {
            this.matchPdus.get().add(matchPdusItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * Get matchPdus
     * 
     * @return matchPdus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public List<PduSessionInfo> getMatchPdus()
    {
        return matchPdus.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_MATCH_PDUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<PduSessionInfo>> getMatchPdus_JsonNullable()
    {
        return matchPdus;
    }

    @JsonProperty(JSON_PROPERTY_MATCH_PDUS)
    public void setMatchPdus_JsonNullable(JsonNullable<List<PduSessionInfo>> matchPdus)
    {
        this.matchPdus = matchPdus;
    }

    public void setMatchPdus(List<PduSessionInfo> matchPdus)
    {
        this.matchPdus = JsonNullable.<List<PduSessionInfo>>of(matchPdus);
    }

    public PolicyUpdate asTimeDisParam(AsTimeDistributionParam asTimeDisParam)
    {
        this.asTimeDisParam = JsonNullable.<AsTimeDistributionParam>of(asTimeDisParam);

        return this;
    }

    /**
     * Get asTimeDisParam
     * 
     * @return asTimeDisParam
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public AsTimeDistributionParam getAsTimeDisParam()
    {
        return asTimeDisParam.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_AS_TIME_DIS_PARAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<AsTimeDistributionParam> getAsTimeDisParam_JsonNullable()
    {
        return asTimeDisParam;
    }

    @JsonProperty(JSON_PROPERTY_AS_TIME_DIS_PARAM)
    public void setAsTimeDisParam_JsonNullable(JsonNullable<AsTimeDistributionParam> asTimeDisParam)
    {
        this.asTimeDisParam = asTimeDisParam;
    }

    public void setAsTimeDisParam(AsTimeDistributionParam asTimeDisParam)
    {
        this.asTimeDisParam = JsonNullable.<AsTimeDistributionParam>of(asTimeDisParam);
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
        PolicyUpdate policyUpdate = (PolicyUpdate) o;
        return Objects.equals(this.resourceUri, policyUpdate.resourceUri) && equalsNullable(this.triggers, policyUpdate.triggers)
               && Objects.equals(this.servAreaRes, policyUpdate.servAreaRes) && Objects.equals(this.wlServAreaRes, policyUpdate.wlServAreaRes)
               && Objects.equals(this.rfsp, policyUpdate.rfsp) && Objects.equals(this.targetRfsp, policyUpdate.targetRfsp)
               && equalsNullable(this.smfSelInfo, policyUpdate.smfSelInfo) && Objects.equals(this.ueAmbr, policyUpdate.ueAmbr)
               && Objects.equals(this.ueSliceMbrs, policyUpdate.ueSliceMbrs) && equalsNullable(this.pras, policyUpdate.pras)
               && equalsNullable(this.pcfUeInfo, policyUpdate.pcfUeInfo) && equalsNullable(this.matchPdus, policyUpdate.matchPdus)
               && equalsNullable(this.asTimeDisParam, policyUpdate.asTimeDisParam);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(resourceUri,
                            hashCodeNullable(triggers),
                            servAreaRes,
                            wlServAreaRes,
                            rfsp,
                            targetRfsp,
                            hashCodeNullable(smfSelInfo),
                            ueAmbr,
                            ueSliceMbrs,
                            hashCodeNullable(pras),
                            hashCodeNullable(pcfUeInfo),
                            hashCodeNullable(matchPdus),
                            hashCodeNullable(asTimeDisParam));
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyUpdate {\n");
        sb.append("    resourceUri: ").append(toIndentedString(resourceUri)).append("\n");
        sb.append("    triggers: ").append(toIndentedString(triggers)).append("\n");
        sb.append("    servAreaRes: ").append(toIndentedString(servAreaRes)).append("\n");
        sb.append("    wlServAreaRes: ").append(toIndentedString(wlServAreaRes)).append("\n");
        sb.append("    rfsp: ").append(toIndentedString(rfsp)).append("\n");
        sb.append("    targetRfsp: ").append(toIndentedString(targetRfsp)).append("\n");
        sb.append("    smfSelInfo: ").append(toIndentedString(smfSelInfo)).append("\n");
        sb.append("    ueAmbr: ").append(toIndentedString(ueAmbr)).append("\n");
        sb.append("    ueSliceMbrs: ").append(toIndentedString(ueSliceMbrs)).append("\n");
        sb.append("    pras: ").append(toIndentedString(pras)).append("\n");
        sb.append("    pcfUeInfo: ").append(toIndentedString(pcfUeInfo)).append("\n");
        sb.append("    matchPdus: ").append(toIndentedString(matchPdus)).append("\n");
        sb.append("    asTimeDisParam: ").append(toIndentedString(asTimeDisParam)).append("\n");
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
