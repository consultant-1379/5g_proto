/*
 * Nnwdaf_AnalyticsInfo
 * Nnwdaf_AnalyticsInfo Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.analyticsinfo;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.NetworkPerfInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.NsiLoadLevelInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.SliceLoadLevelInformation;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.QosSustainabilityInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.UeMobility;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.AbnormalBehaviour;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.NfLoadLevelInformation;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.WlanPerformanceInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.UserDataCongestionInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.DnPerfInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.AnalyticsMetadataInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.RedundantTransmissionExpInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.UeCommunication;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.ServiceExperienceInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.DispersionInfo;
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
 * Represents the description of analytics with parameters as relevant for the
 * requesting NF service consumer.
 */
@ApiModel(description = "Represents the description of analytics with parameters as relevant for the requesting NF  service consumer. ")
@JsonPropertyOrder({ AnalyticsData.JSON_PROPERTY_START,
                     AnalyticsData.JSON_PROPERTY_EXPIRY,
                     AnalyticsData.JSON_PROPERTY_TIME_STAMP_GEN,
                     AnalyticsData.JSON_PROPERTY_ANA_META_INFO,
                     AnalyticsData.JSON_PROPERTY_SLICE_LOAD_LEVEL_INFOS,
                     AnalyticsData.JSON_PROPERTY_NSI_LOAD_LEVEL_INFOS,
                     AnalyticsData.JSON_PROPERTY_NF_LOAD_LEVEL_INFOS,
                     AnalyticsData.JSON_PROPERTY_NW_PERFS,
                     AnalyticsData.JSON_PROPERTY_SVC_EXPS,
                     AnalyticsData.JSON_PROPERTY_QOS_SUSTAIN_INFOS,
                     AnalyticsData.JSON_PROPERTY_UE_MOBS,
                     AnalyticsData.JSON_PROPERTY_UE_COMMS,
                     AnalyticsData.JSON_PROPERTY_USER_DATA_CONG_INFOS,
                     AnalyticsData.JSON_PROPERTY_ABNOR_BEHAVRS,
                     AnalyticsData.JSON_PROPERTY_SMCC_EXPS,
                     AnalyticsData.JSON_PROPERTY_DISPER_INFOS,
                     AnalyticsData.JSON_PROPERTY_RED_TRANS_INFOS,
                     AnalyticsData.JSON_PROPERTY_WLAN_INFOS,
                     AnalyticsData.JSON_PROPERTY_DN_PERF_INFOS,
                     AnalyticsData.JSON_PROPERTY_SUPP_FEAT })
public class AnalyticsData
{
    public static final String JSON_PROPERTY_START = "start";
    private OffsetDateTime start;

    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private OffsetDateTime expiry;

    public static final String JSON_PROPERTY_TIME_STAMP_GEN = "timeStampGen";
    private OffsetDateTime timeStampGen;

    public static final String JSON_PROPERTY_ANA_META_INFO = "anaMetaInfo";
    private AnalyticsMetadataInfo anaMetaInfo;

    public static final String JSON_PROPERTY_SLICE_LOAD_LEVEL_INFOS = "sliceLoadLevelInfos";
    private List<SliceLoadLevelInformation> sliceLoadLevelInfos = null;

    public static final String JSON_PROPERTY_NSI_LOAD_LEVEL_INFOS = "nsiLoadLevelInfos";
    private List<NsiLoadLevelInfo> nsiLoadLevelInfos = null;

    public static final String JSON_PROPERTY_NF_LOAD_LEVEL_INFOS = "nfLoadLevelInfos";
    private List<NfLoadLevelInformation> nfLoadLevelInfos = null;

    public static final String JSON_PROPERTY_NW_PERFS = "nwPerfs";
    private List<NetworkPerfInfo> nwPerfs = null;

    public static final String JSON_PROPERTY_SVC_EXPS = "svcExps";
    private List<ServiceExperienceInfo> svcExps = null;

    public static final String JSON_PROPERTY_QOS_SUSTAIN_INFOS = "qosSustainInfos";
    private List<QosSustainabilityInfo> qosSustainInfos = null;

    public static final String JSON_PROPERTY_UE_MOBS = "ueMobs";
    private List<UeMobility> ueMobs = null;

    public static final String JSON_PROPERTY_UE_COMMS = "ueComms";
    private List<UeCommunication> ueComms = null;

    public static final String JSON_PROPERTY_USER_DATA_CONG_INFOS = "userDataCongInfos";
    private List<UserDataCongestionInfo> userDataCongInfos = null;

    public static final String JSON_PROPERTY_ABNOR_BEHAVRS = "abnorBehavrs";
    private List<AbnormalBehaviour> abnorBehavrs = null;

    public static final String JSON_PROPERTY_SMCC_EXPS = "smccExps";
    private List<SmcceInfo> smccExps = null;

    public static final String JSON_PROPERTY_DISPER_INFOS = "disperInfos";
    private List<DispersionInfo> disperInfos = null;

    public static final String JSON_PROPERTY_RED_TRANS_INFOS = "redTransInfos";
    private List<RedundantTransmissionExpInfo> redTransInfos = null;

    public static final String JSON_PROPERTY_WLAN_INFOS = "wlanInfos";
    private List<WlanPerformanceInfo> wlanInfos = null;

    public static final String JSON_PROPERTY_DN_PERF_INFOS = "dnPerfInfos";
    private List<DnPerfInfo> dnPerfInfos = null;

    public static final String JSON_PROPERTY_SUPP_FEAT = "suppFeat";
    private String suppFeat;

    public AnalyticsData()
    {
    }

    public AnalyticsData start(OffsetDateTime start)
    {

        this.start = start;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return start
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getStart()
    {
        return start;
    }

    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStart(OffsetDateTime start)
    {
        this.start = start;
    }

    public AnalyticsData expiry(OffsetDateTime expiry)
    {

        this.expiry = expiry;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return expiry
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getExpiry()
    {
        return expiry;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpiry(OffsetDateTime expiry)
    {
        this.expiry = expiry;
    }

    public AnalyticsData timeStampGen(OffsetDateTime timeStampGen)
    {

        this.timeStampGen = timeStampGen;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return timeStampGen
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TIME_STAMP_GEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTimeStampGen()
    {
        return timeStampGen;
    }

    @JsonProperty(JSON_PROPERTY_TIME_STAMP_GEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeStampGen(OffsetDateTime timeStampGen)
    {
        this.timeStampGen = timeStampGen;
    }

    public AnalyticsData anaMetaInfo(AnalyticsMetadataInfo anaMetaInfo)
    {

        this.anaMetaInfo = anaMetaInfo;
        return this;
    }

    /**
     * Get anaMetaInfo
     * 
     * @return anaMetaInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ANA_META_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AnalyticsMetadataInfo getAnaMetaInfo()
    {
        return anaMetaInfo;
    }

    @JsonProperty(JSON_PROPERTY_ANA_META_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnaMetaInfo(AnalyticsMetadataInfo anaMetaInfo)
    {
        this.anaMetaInfo = anaMetaInfo;
    }

    public AnalyticsData sliceLoadLevelInfos(List<SliceLoadLevelInformation> sliceLoadLevelInfos)
    {

        this.sliceLoadLevelInfos = sliceLoadLevelInfos;
        return this;
    }

    public AnalyticsData addSliceLoadLevelInfosItem(SliceLoadLevelInformation sliceLoadLevelInfosItem)
    {
        if (this.sliceLoadLevelInfos == null)
        {
            this.sliceLoadLevelInfos = new ArrayList<>();
        }
        this.sliceLoadLevelInfos.add(sliceLoadLevelInfosItem);
        return this;
    }

    /**
     * The slices and their load level information.
     * 
     * @return sliceLoadLevelInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The slices and their load level information.")
    @JsonProperty(JSON_PROPERTY_SLICE_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SliceLoadLevelInformation> getSliceLoadLevelInfos()
    {
        return sliceLoadLevelInfos;
    }

    @JsonProperty(JSON_PROPERTY_SLICE_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSliceLoadLevelInfos(List<SliceLoadLevelInformation> sliceLoadLevelInfos)
    {
        this.sliceLoadLevelInfos = sliceLoadLevelInfos;
    }

    public AnalyticsData nsiLoadLevelInfos(List<NsiLoadLevelInfo> nsiLoadLevelInfos)
    {

        this.nsiLoadLevelInfos = nsiLoadLevelInfos;
        return this;
    }

    public AnalyticsData addNsiLoadLevelInfosItem(NsiLoadLevelInfo nsiLoadLevelInfosItem)
    {
        if (this.nsiLoadLevelInfos == null)
        {
            this.nsiLoadLevelInfos = new ArrayList<>();
        }
        this.nsiLoadLevelInfos.add(nsiLoadLevelInfosItem);
        return this;
    }

    /**
     * Get nsiLoadLevelInfos
     * 
     * @return nsiLoadLevelInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NSI_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NsiLoadLevelInfo> getNsiLoadLevelInfos()
    {
        return nsiLoadLevelInfos;
    }

    @JsonProperty(JSON_PROPERTY_NSI_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNsiLoadLevelInfos(List<NsiLoadLevelInfo> nsiLoadLevelInfos)
    {
        this.nsiLoadLevelInfos = nsiLoadLevelInfos;
    }

    public AnalyticsData nfLoadLevelInfos(List<NfLoadLevelInformation> nfLoadLevelInfos)
    {

        this.nfLoadLevelInfos = nfLoadLevelInfos;
        return this;
    }

    public AnalyticsData addNfLoadLevelInfosItem(NfLoadLevelInformation nfLoadLevelInfosItem)
    {
        if (this.nfLoadLevelInfos == null)
        {
            this.nfLoadLevelInfos = new ArrayList<>();
        }
        this.nfLoadLevelInfos.add(nfLoadLevelInfosItem);
        return this;
    }

    /**
     * Get nfLoadLevelInfos
     * 
     * @return nfLoadLevelInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NF_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NfLoadLevelInformation> getNfLoadLevelInfos()
    {
        return nfLoadLevelInfos;
    }

    @JsonProperty(JSON_PROPERTY_NF_LOAD_LEVEL_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfLoadLevelInfos(List<NfLoadLevelInformation> nfLoadLevelInfos)
    {
        this.nfLoadLevelInfos = nfLoadLevelInfos;
    }

    public AnalyticsData nwPerfs(List<NetworkPerfInfo> nwPerfs)
    {

        this.nwPerfs = nwPerfs;
        return this;
    }

    public AnalyticsData addNwPerfsItem(NetworkPerfInfo nwPerfsItem)
    {
        if (this.nwPerfs == null)
        {
            this.nwPerfs = new ArrayList<>();
        }
        this.nwPerfs.add(nwPerfsItem);
        return this;
    }

    /**
     * Get nwPerfs
     * 
     * @return nwPerfs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NW_PERFS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NetworkPerfInfo> getNwPerfs()
    {
        return nwPerfs;
    }

    @JsonProperty(JSON_PROPERTY_NW_PERFS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNwPerfs(List<NetworkPerfInfo> nwPerfs)
    {
        this.nwPerfs = nwPerfs;
    }

    public AnalyticsData svcExps(List<ServiceExperienceInfo> svcExps)
    {

        this.svcExps = svcExps;
        return this;
    }

    public AnalyticsData addSvcExpsItem(ServiceExperienceInfo svcExpsItem)
    {
        if (this.svcExps == null)
        {
            this.svcExps = new ArrayList<>();
        }
        this.svcExps.add(svcExpsItem);
        return this;
    }

    /**
     * Get svcExps
     * 
     * @return svcExps
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SVC_EXPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ServiceExperienceInfo> getSvcExps()
    {
        return svcExps;
    }

    @JsonProperty(JSON_PROPERTY_SVC_EXPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSvcExps(List<ServiceExperienceInfo> svcExps)
    {
        this.svcExps = svcExps;
    }

    public AnalyticsData qosSustainInfos(List<QosSustainabilityInfo> qosSustainInfos)
    {

        this.qosSustainInfos = qosSustainInfos;
        return this;
    }

    public AnalyticsData addQosSustainInfosItem(QosSustainabilityInfo qosSustainInfosItem)
    {
        if (this.qosSustainInfos == null)
        {
            this.qosSustainInfos = new ArrayList<>();
        }
        this.qosSustainInfos.add(qosSustainInfosItem);
        return this;
    }

    /**
     * Get qosSustainInfos
     * 
     * @return qosSustainInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_QOS_SUSTAIN_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<QosSustainabilityInfo> getQosSustainInfos()
    {
        return qosSustainInfos;
    }

    @JsonProperty(JSON_PROPERTY_QOS_SUSTAIN_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQosSustainInfos(List<QosSustainabilityInfo> qosSustainInfos)
    {
        this.qosSustainInfos = qosSustainInfos;
    }

    public AnalyticsData ueMobs(List<UeMobility> ueMobs)
    {

        this.ueMobs = ueMobs;
        return this;
    }

    public AnalyticsData addUeMobsItem(UeMobility ueMobsItem)
    {
        if (this.ueMobs == null)
        {
            this.ueMobs = new ArrayList<>();
        }
        this.ueMobs.add(ueMobsItem);
        return this;
    }

    /**
     * Get ueMobs
     * 
     * @return ueMobs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_MOBS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UeMobility> getUeMobs()
    {
        return ueMobs;
    }

    @JsonProperty(JSON_PROPERTY_UE_MOBS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeMobs(List<UeMobility> ueMobs)
    {
        this.ueMobs = ueMobs;
    }

    public AnalyticsData ueComms(List<UeCommunication> ueComms)
    {

        this.ueComms = ueComms;
        return this;
    }

    public AnalyticsData addUeCommsItem(UeCommunication ueCommsItem)
    {
        if (this.ueComms == null)
        {
            this.ueComms = new ArrayList<>();
        }
        this.ueComms.add(ueCommsItem);
        return this;
    }

    /**
     * Get ueComms
     * 
     * @return ueComms
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_COMMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UeCommunication> getUeComms()
    {
        return ueComms;
    }

    @JsonProperty(JSON_PROPERTY_UE_COMMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeComms(List<UeCommunication> ueComms)
    {
        this.ueComms = ueComms;
    }

    public AnalyticsData userDataCongInfos(List<UserDataCongestionInfo> userDataCongInfos)
    {

        this.userDataCongInfos = userDataCongInfos;
        return this;
    }

    public AnalyticsData addUserDataCongInfosItem(UserDataCongestionInfo userDataCongInfosItem)
    {
        if (this.userDataCongInfos == null)
        {
            this.userDataCongInfos = new ArrayList<>();
        }
        this.userDataCongInfos.add(userDataCongInfosItem);
        return this;
    }

    /**
     * Get userDataCongInfos
     * 
     * @return userDataCongInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USER_DATA_CONG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UserDataCongestionInfo> getUserDataCongInfos()
    {
        return userDataCongInfos;
    }

    @JsonProperty(JSON_PROPERTY_USER_DATA_CONG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserDataCongInfos(List<UserDataCongestionInfo> userDataCongInfos)
    {
        this.userDataCongInfos = userDataCongInfos;
    }

    public AnalyticsData abnorBehavrs(List<AbnormalBehaviour> abnorBehavrs)
    {

        this.abnorBehavrs = abnorBehavrs;
        return this;
    }

    public AnalyticsData addAbnorBehavrsItem(AbnormalBehaviour abnorBehavrsItem)
    {
        if (this.abnorBehavrs == null)
        {
            this.abnorBehavrs = new ArrayList<>();
        }
        this.abnorBehavrs.add(abnorBehavrsItem);
        return this;
    }

    /**
     * Get abnorBehavrs
     * 
     * @return abnorBehavrs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ABNOR_BEHAVRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AbnormalBehaviour> getAbnorBehavrs()
    {
        return abnorBehavrs;
    }

    @JsonProperty(JSON_PROPERTY_ABNOR_BEHAVRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAbnorBehavrs(List<AbnormalBehaviour> abnorBehavrs)
    {
        this.abnorBehavrs = abnorBehavrs;
    }

    public AnalyticsData smccExps(List<SmcceInfo> smccExps)
    {

        this.smccExps = smccExps;
        return this;
    }

    public AnalyticsData addSmccExpsItem(SmcceInfo smccExpsItem)
    {
        if (this.smccExps == null)
        {
            this.smccExps = new ArrayList<>();
        }
        this.smccExps.add(smccExpsItem);
        return this;
    }

    /**
     * Get smccExps
     * 
     * @return smccExps
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMCC_EXPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SmcceInfo> getSmccExps()
    {
        return smccExps;
    }

    @JsonProperty(JSON_PROPERTY_SMCC_EXPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmccExps(List<SmcceInfo> smccExps)
    {
        this.smccExps = smccExps;
    }

    public AnalyticsData disperInfos(List<DispersionInfo> disperInfos)
    {

        this.disperInfos = disperInfos;
        return this;
    }

    public AnalyticsData addDisperInfosItem(DispersionInfo disperInfosItem)
    {
        if (this.disperInfos == null)
        {
            this.disperInfos = new ArrayList<>();
        }
        this.disperInfos.add(disperInfosItem);
        return this;
    }

    /**
     * Get disperInfos
     * 
     * @return disperInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DISPER_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DispersionInfo> getDisperInfos()
    {
        return disperInfos;
    }

    @JsonProperty(JSON_PROPERTY_DISPER_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisperInfos(List<DispersionInfo> disperInfos)
    {
        this.disperInfos = disperInfos;
    }

    public AnalyticsData redTransInfos(List<RedundantTransmissionExpInfo> redTransInfos)
    {

        this.redTransInfos = redTransInfos;
        return this;
    }

    public AnalyticsData addRedTransInfosItem(RedundantTransmissionExpInfo redTransInfosItem)
    {
        if (this.redTransInfos == null)
        {
            this.redTransInfos = new ArrayList<>();
        }
        this.redTransInfos.add(redTransInfosItem);
        return this;
    }

    /**
     * Get redTransInfos
     * 
     * @return redTransInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RED_TRANS_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RedundantTransmissionExpInfo> getRedTransInfos()
    {
        return redTransInfos;
    }

    @JsonProperty(JSON_PROPERTY_RED_TRANS_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRedTransInfos(List<RedundantTransmissionExpInfo> redTransInfos)
    {
        this.redTransInfos = redTransInfos;
    }

    public AnalyticsData wlanInfos(List<WlanPerformanceInfo> wlanInfos)
    {

        this.wlanInfos = wlanInfos;
        return this;
    }

    public AnalyticsData addWlanInfosItem(WlanPerformanceInfo wlanInfosItem)
    {
        if (this.wlanInfos == null)
        {
            this.wlanInfos = new ArrayList<>();
        }
        this.wlanInfos.add(wlanInfosItem);
        return this;
    }

    /**
     * Get wlanInfos
     * 
     * @return wlanInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_WLAN_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<WlanPerformanceInfo> getWlanInfos()
    {
        return wlanInfos;
    }

    @JsonProperty(JSON_PROPERTY_WLAN_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWlanInfos(List<WlanPerformanceInfo> wlanInfos)
    {
        this.wlanInfos = wlanInfos;
    }

    public AnalyticsData dnPerfInfos(List<DnPerfInfo> dnPerfInfos)
    {

        this.dnPerfInfos = dnPerfInfos;
        return this;
    }

    public AnalyticsData addDnPerfInfosItem(DnPerfInfo dnPerfInfosItem)
    {
        if (this.dnPerfInfos == null)
        {
            this.dnPerfInfos = new ArrayList<>();
        }
        this.dnPerfInfos.add(dnPerfInfosItem);
        return this;
    }

    /**
     * Get dnPerfInfos
     * 
     * @return dnPerfInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DN_PERF_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DnPerfInfo> getDnPerfInfos()
    {
        return dnPerfInfos;
    }

    @JsonProperty(JSON_PROPERTY_DN_PERF_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnPerfInfos(List<DnPerfInfo> dnPerfInfos)
    {
        this.dnPerfInfos = dnPerfInfos;
    }

    public AnalyticsData suppFeat(String suppFeat)
    {

        this.suppFeat = suppFeat;
        return this;
    }

    /**
     * A string used to indicate the features supported by an API that is used as
     * defined in clause 6.6 in 3GPP TS 29.500. The string shall contain a bitmask
     * indicating supported features in hexadecimal representation Each character in
     * the string shall take a value of \&quot;0\&quot; to \&quot;9\&quot;,
     * \&quot;a\&quot; to \&quot;f\&quot; or \&quot;A\&quot; to \&quot;F\&quot; and
     * shall represent the support of 4 features as described in table 5.2.2-3. The
     * most significant character representing the highest-numbered features shall
     * appear first in the string, and the character representing features 1 to 4
     * shall appear last in the string. The list of features and their numbering
     * (starting with 1) are defined separately for each API. If the string contains
     * a lower number of characters than there are defined features for an API, all
     * features that would be represented by characters that are not present in the
     * string are not supported.
     * 
     * @return suppFeat
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPP_FEAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSuppFeat()
    {
        return suppFeat;
    }

    @JsonProperty(JSON_PROPERTY_SUPP_FEAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuppFeat(String suppFeat)
    {
        this.suppFeat = suppFeat;
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
        AnalyticsData analyticsData = (AnalyticsData) o;
        return Objects.equals(this.start, analyticsData.start) && Objects.equals(this.expiry, analyticsData.expiry)
               && Objects.equals(this.timeStampGen, analyticsData.timeStampGen) && Objects.equals(this.anaMetaInfo, analyticsData.anaMetaInfo)
               && Objects.equals(this.sliceLoadLevelInfos, analyticsData.sliceLoadLevelInfos)
               && Objects.equals(this.nsiLoadLevelInfos, analyticsData.nsiLoadLevelInfos)
               && Objects.equals(this.nfLoadLevelInfos, analyticsData.nfLoadLevelInfos) && Objects.equals(this.nwPerfs, analyticsData.nwPerfs)
               && Objects.equals(this.svcExps, analyticsData.svcExps) && Objects.equals(this.qosSustainInfos, analyticsData.qosSustainInfos)
               && Objects.equals(this.ueMobs, analyticsData.ueMobs) && Objects.equals(this.ueComms, analyticsData.ueComms)
               && Objects.equals(this.userDataCongInfos, analyticsData.userDataCongInfos) && Objects.equals(this.abnorBehavrs, analyticsData.abnorBehavrs)
               && Objects.equals(this.smccExps, analyticsData.smccExps) && Objects.equals(this.disperInfos, analyticsData.disperInfos)
               && Objects.equals(this.redTransInfos, analyticsData.redTransInfos) && Objects.equals(this.wlanInfos, analyticsData.wlanInfos)
               && Objects.equals(this.dnPerfInfos, analyticsData.dnPerfInfos) && Objects.equals(this.suppFeat, analyticsData.suppFeat);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(start,
                            expiry,
                            timeStampGen,
                            anaMetaInfo,
                            sliceLoadLevelInfos,
                            nsiLoadLevelInfos,
                            nfLoadLevelInfos,
                            nwPerfs,
                            svcExps,
                            qosSustainInfos,
                            ueMobs,
                            ueComms,
                            userDataCongInfos,
                            abnorBehavrs,
                            smccExps,
                            disperInfos,
                            redTransInfos,
                            wlanInfos,
                            dnPerfInfos,
                            suppFeat);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalyticsData {\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    timeStampGen: ").append(toIndentedString(timeStampGen)).append("\n");
        sb.append("    anaMetaInfo: ").append(toIndentedString(anaMetaInfo)).append("\n");
        sb.append("    sliceLoadLevelInfos: ").append(toIndentedString(sliceLoadLevelInfos)).append("\n");
        sb.append("    nsiLoadLevelInfos: ").append(toIndentedString(nsiLoadLevelInfos)).append("\n");
        sb.append("    nfLoadLevelInfos: ").append(toIndentedString(nfLoadLevelInfos)).append("\n");
        sb.append("    nwPerfs: ").append(toIndentedString(nwPerfs)).append("\n");
        sb.append("    svcExps: ").append(toIndentedString(svcExps)).append("\n");
        sb.append("    qosSustainInfos: ").append(toIndentedString(qosSustainInfos)).append("\n");
        sb.append("    ueMobs: ").append(toIndentedString(ueMobs)).append("\n");
        sb.append("    ueComms: ").append(toIndentedString(ueComms)).append("\n");
        sb.append("    userDataCongInfos: ").append(toIndentedString(userDataCongInfos)).append("\n");
        sb.append("    abnorBehavrs: ").append(toIndentedString(abnorBehavrs)).append("\n");
        sb.append("    smccExps: ").append(toIndentedString(smccExps)).append("\n");
        sb.append("    disperInfos: ").append(toIndentedString(disperInfos)).append("\n");
        sb.append("    redTransInfos: ").append(toIndentedString(redTransInfos)).append("\n");
        sb.append("    wlanInfos: ").append(toIndentedString(wlanInfos)).append("\n");
        sb.append("    dnPerfInfos: ").append(toIndentedString(dnPerfInfos)).append("\n");
        sb.append("    suppFeat: ").append(toIndentedString(suppFeat)).append("\n");
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
