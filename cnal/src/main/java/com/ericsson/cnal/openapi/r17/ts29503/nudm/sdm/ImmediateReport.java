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

import java.util.Arrays;
import java.util.Objects;

import org.openapitools.jackson.nullable.JsonNullable;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.TraceData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * ImmediateReport
 */
@JsonPropertyOrder({ ImmediateReport.JSON_PROPERTY_AM_DATA,
                     ImmediateReport.JSON_PROPERTY_SMF_SEL_DATA,
                     ImmediateReport.JSON_PROPERTY_UEC_AMF_DATA,
                     ImmediateReport.JSON_PROPERTY_UEC_SMF_DATA,
                     ImmediateReport.JSON_PROPERTY_UEC_SMSF_DATA,
                     ImmediateReport.JSON_PROPERTY_SMS_SUBS_DATA,
                     ImmediateReport.JSON_PROPERTY_SM_DATA,
                     ImmediateReport.JSON_PROPERTY_TRACE_DATA,
                     ImmediateReport.JSON_PROPERTY_SMS_MNG_DATA,
                     ImmediateReport.JSON_PROPERTY_LCS_PRIVACY_DATA,
                     ImmediateReport.JSON_PROPERTY_LCS_MO_DATA,
                     ImmediateReport.JSON_PROPERTY_V2X_DATA,
                     ImmediateReport.JSON_PROPERTY_LCS_BROADCAST_ASSISTANCE_TYPES_DATA,
                     ImmediateReport.JSON_PROPERTY_PROSE_DATA,
                     ImmediateReport.JSON_PROPERTY_MBS_DATA,
                     ImmediateReport.JSON_PROPERTY_UC_DATA })
public class ImmediateReport
{
    public static final String JSON_PROPERTY_AM_DATA = "amData";
    private AccessAndMobilitySubscriptionData amData;

    public static final String JSON_PROPERTY_SMF_SEL_DATA = "smfSelData";
    private SmfSelectionSubscriptionData smfSelData;

    public static final String JSON_PROPERTY_UEC_AMF_DATA = "uecAmfData";
    private UeContextInAmfData uecAmfData;

    public static final String JSON_PROPERTY_UEC_SMF_DATA = "uecSmfData";
    private UeContextInSmfData uecSmfData;

    public static final String JSON_PROPERTY_UEC_SMSF_DATA = "uecSmsfData";
    private UeContextInSmsfData uecSmsfData;

    public static final String JSON_PROPERTY_SMS_SUBS_DATA = "smsSubsData";
    private SmsSubscriptionData smsSubsData;

    public static final String JSON_PROPERTY_SM_DATA = "smData";
    private SmSubsData smData;

    public static final String JSON_PROPERTY_TRACE_DATA = "traceData";
    private JsonNullable<TraceData> traceData = JsonNullable.<TraceData>undefined();

    public static final String JSON_PROPERTY_SMS_MNG_DATA = "smsMngData";
    private SmsManagementSubscriptionData smsMngData;

    public static final String JSON_PROPERTY_LCS_PRIVACY_DATA = "lcsPrivacyData";
    private LcsPrivacyData lcsPrivacyData;

    public static final String JSON_PROPERTY_LCS_MO_DATA = "lcsMoData";
    private LcsMoData lcsMoData;

    public static final String JSON_PROPERTY_V2X_DATA = "v2xData";
    private V2xSubscriptionData v2xData;

    public static final String JSON_PROPERTY_LCS_BROADCAST_ASSISTANCE_TYPES_DATA = "lcsBroadcastAssistanceTypesData";
    private LcsBroadcastAssistanceTypesData lcsBroadcastAssistanceTypesData;

    public static final String JSON_PROPERTY_PROSE_DATA = "proseData";
    private ProseSubscriptionData proseData;

    public static final String JSON_PROPERTY_MBS_DATA = "mbsData";
    private MbsSubscriptionData mbsData;

    public static final String JSON_PROPERTY_UC_DATA = "ucData";
    private UcSubscriptionData ucData;

    public ImmediateReport()
    {
    }

    public ImmediateReport amData(AccessAndMobilitySubscriptionData amData)
    {

        this.amData = amData;
        return this;
    }

    /**
     * Get amData
     * 
     * @return amData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccessAndMobilitySubscriptionData getAmData()
    {
        return amData;
    }

    @JsonProperty(JSON_PROPERTY_AM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmData(AccessAndMobilitySubscriptionData amData)
    {
        this.amData = amData;
    }

    public ImmediateReport smfSelData(SmfSelectionSubscriptionData smfSelData)
    {

        this.smfSelData = smfSelData;
        return this;
    }

    /**
     * Get smfSelData
     * 
     * @return smfSelData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMF_SEL_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmfSelectionSubscriptionData getSmfSelData()
    {
        return smfSelData;
    }

    @JsonProperty(JSON_PROPERTY_SMF_SEL_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmfSelData(SmfSelectionSubscriptionData smfSelData)
    {
        this.smfSelData = smfSelData;
    }

    public ImmediateReport uecAmfData(UeContextInAmfData uecAmfData)
    {

        this.uecAmfData = uecAmfData;
        return this;
    }

    /**
     * Get uecAmfData
     * 
     * @return uecAmfData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UEC_AMF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UeContextInAmfData getUecAmfData()
    {
        return uecAmfData;
    }

    @JsonProperty(JSON_PROPERTY_UEC_AMF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUecAmfData(UeContextInAmfData uecAmfData)
    {
        this.uecAmfData = uecAmfData;
    }

    public ImmediateReport uecSmfData(UeContextInSmfData uecSmfData)
    {

        this.uecSmfData = uecSmfData;
        return this;
    }

    /**
     * Get uecSmfData
     * 
     * @return uecSmfData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UEC_SMF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UeContextInSmfData getUecSmfData()
    {
        return uecSmfData;
    }

    @JsonProperty(JSON_PROPERTY_UEC_SMF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUecSmfData(UeContextInSmfData uecSmfData)
    {
        this.uecSmfData = uecSmfData;
    }

    public ImmediateReport uecSmsfData(UeContextInSmsfData uecSmsfData)
    {

        this.uecSmsfData = uecSmsfData;
        return this;
    }

    /**
     * Get uecSmsfData
     * 
     * @return uecSmsfData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UEC_SMSF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UeContextInSmsfData getUecSmsfData()
    {
        return uecSmsfData;
    }

    @JsonProperty(JSON_PROPERTY_UEC_SMSF_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUecSmsfData(UeContextInSmsfData uecSmsfData)
    {
        this.uecSmsfData = uecSmsfData;
    }

    public ImmediateReport smsSubsData(SmsSubscriptionData smsSubsData)
    {

        this.smsSubsData = smsSubsData;
        return this;
    }

    /**
     * Get smsSubsData
     * 
     * @return smsSubsData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMS_SUBS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsSubscriptionData getSmsSubsData()
    {
        return smsSubsData;
    }

    @JsonProperty(JSON_PROPERTY_SMS_SUBS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsSubsData(SmsSubscriptionData smsSubsData)
    {
        this.smsSubsData = smsSubsData;
    }

    public ImmediateReport smData(SmSubsData smData)
    {

        this.smData = smData;
        return this;
    }

    /**
     * Get smData
     * 
     * @return smData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmSubsData getSmData()
    {
        return smData;
    }

    @JsonProperty(JSON_PROPERTY_SM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmData(SmSubsData smData)
    {
        this.smData = smData;
    }

    public ImmediateReport traceData(TraceData traceData)
    {
        this.traceData = JsonNullable.<TraceData>of(traceData);

        return this;
    }

    /**
     * Get traceData
     * 
     * @return traceData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public TraceData getTraceData()
    {
        return traceData.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_TRACE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<TraceData> getTraceData_JsonNullable()
    {
        return traceData;
    }

    @JsonProperty(JSON_PROPERTY_TRACE_DATA)
    public void setTraceData_JsonNullable(JsonNullable<TraceData> traceData)
    {
        this.traceData = traceData;
    }

    public void setTraceData(TraceData traceData)
    {
        this.traceData = JsonNullable.<TraceData>of(traceData);
    }

    public ImmediateReport smsMngData(SmsManagementSubscriptionData smsMngData)
    {

        this.smsMngData = smsMngData;
        return this;
    }

    /**
     * Get smsMngData
     * 
     * @return smsMngData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMS_MNG_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsManagementSubscriptionData getSmsMngData()
    {
        return smsMngData;
    }

    @JsonProperty(JSON_PROPERTY_SMS_MNG_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsMngData(SmsManagementSubscriptionData smsMngData)
    {
        this.smsMngData = smsMngData;
    }

    public ImmediateReport lcsPrivacyData(LcsPrivacyData lcsPrivacyData)
    {

        this.lcsPrivacyData = lcsPrivacyData;
        return this;
    }

    /**
     * Get lcsPrivacyData
     * 
     * @return lcsPrivacyData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_PRIVACY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LcsPrivacyData getLcsPrivacyData()
    {
        return lcsPrivacyData;
    }

    @JsonProperty(JSON_PROPERTY_LCS_PRIVACY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsPrivacyData(LcsPrivacyData lcsPrivacyData)
    {
        this.lcsPrivacyData = lcsPrivacyData;
    }

    public ImmediateReport lcsMoData(LcsMoData lcsMoData)
    {

        this.lcsMoData = lcsMoData;
        return this;
    }

    /**
     * Get lcsMoData
     * 
     * @return lcsMoData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_MO_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LcsMoData getLcsMoData()
    {
        return lcsMoData;
    }

    @JsonProperty(JSON_PROPERTY_LCS_MO_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsMoData(LcsMoData lcsMoData)
    {
        this.lcsMoData = lcsMoData;
    }

    public ImmediateReport v2xData(V2xSubscriptionData v2xData)
    {

        this.v2xData = v2xData;
        return this;
    }

    /**
     * Get v2xData
     * 
     * @return v2xData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_V2X_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public V2xSubscriptionData getV2xData()
    {
        return v2xData;
    }

    @JsonProperty(JSON_PROPERTY_V2X_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setV2xData(V2xSubscriptionData v2xData)
    {
        this.v2xData = v2xData;
    }

    public ImmediateReport lcsBroadcastAssistanceTypesData(LcsBroadcastAssistanceTypesData lcsBroadcastAssistanceTypesData)
    {

        this.lcsBroadcastAssistanceTypesData = lcsBroadcastAssistanceTypesData;
        return this;
    }

    /**
     * Get lcsBroadcastAssistanceTypesData
     * 
     * @return lcsBroadcastAssistanceTypesData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_BROADCAST_ASSISTANCE_TYPES_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LcsBroadcastAssistanceTypesData getLcsBroadcastAssistanceTypesData()
    {
        return lcsBroadcastAssistanceTypesData;
    }

    @JsonProperty(JSON_PROPERTY_LCS_BROADCAST_ASSISTANCE_TYPES_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsBroadcastAssistanceTypesData(LcsBroadcastAssistanceTypesData lcsBroadcastAssistanceTypesData)
    {
        this.lcsBroadcastAssistanceTypesData = lcsBroadcastAssistanceTypesData;
    }

    public ImmediateReport proseData(ProseSubscriptionData proseData)
    {

        this.proseData = proseData;
        return this;
    }

    /**
     * Get proseData
     * 
     * @return proseData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PROSE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ProseSubscriptionData getProseData()
    {
        return proseData;
    }

    @JsonProperty(JSON_PROPERTY_PROSE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProseData(ProseSubscriptionData proseData)
    {
        this.proseData = proseData;
    }

    public ImmediateReport mbsData(MbsSubscriptionData mbsData)
    {

        this.mbsData = mbsData;
        return this;
    }

    /**
     * Get mbsData
     * 
     * @return mbsData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MBS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MbsSubscriptionData getMbsData()
    {
        return mbsData;
    }

    @JsonProperty(JSON_PROPERTY_MBS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsData(MbsSubscriptionData mbsData)
    {
        this.mbsData = mbsData;
    }

    public ImmediateReport ucData(UcSubscriptionData ucData)
    {

        this.ucData = ucData;
        return this;
    }

    /**
     * Get ucData
     * 
     * @return ucData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UC_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UcSubscriptionData getUcData()
    {
        return ucData;
    }

    @JsonProperty(JSON_PROPERTY_UC_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUcData(UcSubscriptionData ucData)
    {
        this.ucData = ucData;
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
        ImmediateReport immediateReport = (ImmediateReport) o;
        return Objects.equals(this.amData, immediateReport.amData) && Objects.equals(this.smfSelData, immediateReport.smfSelData)
               && Objects.equals(this.uecAmfData, immediateReport.uecAmfData) && Objects.equals(this.uecSmfData, immediateReport.uecSmfData)
               && Objects.equals(this.uecSmsfData, immediateReport.uecSmsfData) && Objects.equals(this.smsSubsData, immediateReport.smsSubsData)
               && Objects.equals(this.smData, immediateReport.smData) && equalsNullable(this.traceData, immediateReport.traceData)
               && Objects.equals(this.smsMngData, immediateReport.smsMngData) && Objects.equals(this.lcsPrivacyData, immediateReport.lcsPrivacyData)
               && Objects.equals(this.lcsMoData, immediateReport.lcsMoData) && Objects.equals(this.v2xData, immediateReport.v2xData)
               && Objects.equals(this.lcsBroadcastAssistanceTypesData, immediateReport.lcsBroadcastAssistanceTypesData)
               && Objects.equals(this.proseData, immediateReport.proseData) && Objects.equals(this.mbsData, immediateReport.mbsData)
               && Objects.equals(this.ucData, immediateReport.ucData);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(amData,
                            smfSelData,
                            uecAmfData,
                            uecSmfData,
                            uecSmsfData,
                            smsSubsData,
                            smData,
                            hashCodeNullable(traceData),
                            smsMngData,
                            lcsPrivacyData,
                            lcsMoData,
                            v2xData,
                            lcsBroadcastAssistanceTypesData,
                            proseData,
                            mbsData,
                            ucData);
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
        sb.append("class ImmediateReport {\n");
        sb.append("    amData: ").append(toIndentedString(amData)).append("\n");
        sb.append("    smfSelData: ").append(toIndentedString(smfSelData)).append("\n");
        sb.append("    uecAmfData: ").append(toIndentedString(uecAmfData)).append("\n");
        sb.append("    uecSmfData: ").append(toIndentedString(uecSmfData)).append("\n");
        sb.append("    uecSmsfData: ").append(toIndentedString(uecSmsfData)).append("\n");
        sb.append("    smsSubsData: ").append(toIndentedString(smsSubsData)).append("\n");
        sb.append("    smData: ").append(toIndentedString(smData)).append("\n");
        sb.append("    traceData: ").append(toIndentedString(traceData)).append("\n");
        sb.append("    smsMngData: ").append(toIndentedString(smsMngData)).append("\n");
        sb.append("    lcsPrivacyData: ").append(toIndentedString(lcsPrivacyData)).append("\n");
        sb.append("    lcsMoData: ").append(toIndentedString(lcsMoData)).append("\n");
        sb.append("    v2xData: ").append(toIndentedString(v2xData)).append("\n");
        sb.append("    lcsBroadcastAssistanceTypesData: ").append(toIndentedString(lcsBroadcastAssistanceTypesData)).append("\n");
        sb.append("    proseData: ").append(toIndentedString(proseData)).append("\n");
        sb.append("    mbsData: ").append(toIndentedString(mbsData)).append("\n");
        sb.append("    ucData: ").append(toIndentedString(ucData)).append("\n");
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
