/*
 * NRF NFManagement Service
 * 
 *
 * OpenAPI spec version: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 * 
 * This file has been auto-generated by the yaml2java script.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ServiceName
 */
public class ServiceName
{
    public static final String NNRF_NFM = "nnrf-nfm";
    public static final String NNRF_DISC = "nnrf-disc";
    public static final String NNRF_OAUTH2 = "nnrf-oauth2";
    public static final String NUDM_SDM = "nudm-sdm";
    public static final String NUDM_UECM = "nudm-uecm";
    public static final String NUDM_UEAU = "nudm-ueau";
    public static final String NUDM_EE = "nudm-ee";
    public static final String NUDM_PP = "nudm-pp";
    public static final String NUDM_NIDDAU = "nudm-niddau";
    public static final String NUDM_MT = "nudm-mt";
    public static final String NUDM_SSAU = "nudm-ssau";
    public static final String NUDM_RSDS = "nudm-rsds";
    public static final String NAMF_COMM = "namf-comm";
    public static final String NAMF_EVTS = "namf-evts";
    public static final String NAMF_MT = "namf-mt";
    public static final String NAMF_LOC = "namf-loc";
    public static final String NAMF_MBS_COMM = "namf-mbs-comm";
    public static final String NAMF_MBS_BC = "namf-mbs-bc";
    public static final String NSMF_PDUSESSION = "nsmf-pdusession";
    public static final String NSMF_EVENT_EXPOSURE = "nsmf-event-exposure";
    public static final String NSMF_NIDD = "nsmf-nidd";
    public static final String NAUSF_AUTH = "nausf-auth";
    public static final String NAUSF_SORPROTECTION = "nausf-sorprotection";
    public static final String NAUSF_UPUPROTECTION = "nausf-upuprotection";
    public static final String NNEF_PFDMANAGEMENT = "nnef-pfdmanagement";
    public static final String NNEF_SMCONTEXT = "nnef-smcontext";
    public static final String NNEF_EVENTEXPOSURE = "nnef-eventexposure";
    public static final String NNEF_EAS_DEPLOYMENT_INFO = "nnef-eas-deployment-info";
    public static final String _3GPP_CP_PARAMETER_PROVISIONING = "3gpp-cp-parameter-provisioning";
    public static final String _3GPP_DEVICE_TRIGGERING = "3gpp-device-triggering";
    public static final String _3GPP_BDT = "3gpp-bdt";
    public static final String _3GPP_TRAFFIC_INFLUENCE = "3gpp-traffic-influence";
    public static final String _3GPP_CHARGEABLE_PARTY = "3gpp-chargeable-party";
    public static final String _3GPP_AS_SESSION_WITH_QOS = "3gpp-as-session-with-qos";
    public static final String _3GPP_MSISDN_LESS_MO_SMS = "3gpp-msisdn-less-mo-sms";
    public static final String _3GPP_SERVICE_PARAMETER = "3gpp-service-parameter";
    public static final String _3GPP_MONITORING_EVENT = "3gpp-monitoring-event";
    public static final String _3GPP_NIDD_CONFIGURATION_TRIGGER = "3gpp-nidd-configuration-trigger";
    public static final String _3GPP_NIDD = "3gpp-nidd";
    public static final String _3GPP_ANALYTICSEXPOSURE = "3gpp-analyticsexposure";
    public static final String _3GPP_RACS_PARAMETER_PROVISIONING = "3gpp-racs-parameter-provisioning";
    public static final String _3GPP_ECR_CONTROL = "3gpp-ecr-control";
    public static final String _3GPP_APPLYING_BDT_POLICY = "3gpp-applying-bdt-policy";
    public static final String _3GPP_MO_LCS_NOTIFY = "3gpp-mo-lcs-notify";
    public static final String _3GPP_TIME_SYNC = "3gpp-time-sync";
    public static final String _3GPP_AM_INFLUENCE = "3gpp-am-influence";
    public static final String _3GPP_AM_POLICYAUTHORIZATION = "3gpp-am-policyauthorization";
    public static final String _3GPP_AKMA = "3gpp-akma";
    public static final String _3GPP_EAS_DEPLOYMENT = "3gpp-eas-deployment";
    public static final String _3GPP_IPTVCONFIGURATION = "3gpp-iptvconfiguration";
    public static final String _3GPP_MBS_TMGI = "3gpp-mbs-tmgi";
    public static final String _3GPP_MBS_SESSION = "3gpp-mbs-session";
    public static final String _3GPP_AUTHENTICATION = "3gpp-authentication";
    public static final String _3GPP_ASTI = "3gpp-asti";
    public static final String NPCF_AM_POLICY_CONTROL = "npcf-am-policy-control";
    public static final String NPCF_SMPOLICYCONTROL = "npcf-smpolicycontrol";
    public static final String NPCF_POLICYAUTHORIZATION = "npcf-policyauthorization";
    public static final String NPCF_BDTPOLICYCONTROL = "npcf-bdtpolicycontrol";
    public static final String NPCF_EVENTEXPOSURE = "npcf-eventexposure";
    public static final String NPCF_UE_POLICY_CONTROL = "npcf-ue-policy-control";
    public static final String NPCF_AM_POLICYAUTHORIZATION = "npcf-am-policyauthorization";
    public static final String NSMSF_SMS = "nsmsf-sms";
    public static final String NNSSF_NSSELECTION = "nnssf-nsselection";
    public static final String NNSSF_NSSAIAVAILABILITY = "nnssf-nssaiavailability";
    public static final String NUDR_DR = "nudr-dr";
    public static final String NUDR_GROUP_ID_MAP = "nudr-group-id-map";
    public static final String NLMF_LOC = "nlmf-loc";
    public static final String N5G_EIR_EIC = "n5g-eir-eic";
    public static final String NBSF_MANAGEMENT = "nbsf-management";
    public static final String NCHF_SPENDINGLIMITCONTROL = "nchf-spendinglimitcontrol";
    public static final String NCHF_CONVERGEDCHARGING = "nchf-convergedcharging";
    public static final String NCHF_OFFLINEONLYCHARGING = "nchf-offlineonlycharging";
    public static final String NNWDAF_EVENTSSUBSCRIPTION = "nnwdaf-eventssubscription";
    public static final String NNWDAF_ANALYTICSINFO = "nnwdaf-analyticsinfo";
    public static final String NNWDAF_DATAMANAGEMENT = "nnwdaf-datamanagement";
    public static final String NNWDAF_MLMODELPROVISION = "nnwdaf-mlmodelprovision";
    public static final String NGMLC_LOC = "ngmlc-loc";
    public static final String NUCMF_PROVISIONING = "nucmf-provisioning";
    public static final String NUCMF_UECAPABILITYMANAGEMENT = "nucmf-uecapabilitymanagement";
    public static final String NHSS_SDM = "nhss-sdm";
    public static final String NHSS_UECM = "nhss-uecm";
    public static final String NHSS_UEAU = "nhss-ueau";
    public static final String NHSS_EE = "nhss-ee";
    public static final String NHSS_IMS_SDM = "nhss-ims-sdm";
    public static final String NHSS_IMS_UECM = "nhss-ims-uecm";
    public static final String NHSS_IMS_UEAU = "nhss-ims-ueau";
    public static final String NHSS_GBA_SDM = "nhss-gba-sdm";
    public static final String NHSS_GBA_UEAU = "nhss-gba-ueau";
    public static final String NSEPP_TELESCOPIC = "nsepp-telescopic";
    public static final String NSORAF_SOR = "nsoraf-sor";
    public static final String NSPAF_SECURED_PACKET = "nspaf-secured-packet";
    public static final String NUDSF_DR = "nudsf-dr";
    public static final String NUDSF_TIMER = "nudsf-timer";
    public static final String NNSSAAF_NSSAA = "nnssaaf-nssaa";
    public static final String NNSSAAF_AIW = "nnssaaf-aiw";
    public static final String NAANF_AKMA = "naanf-akma";
    public static final String N5GDDNMF_DISCOVERY = "n5gddnmf-discovery";
    public static final String NMFAF_3DADM = "nmfaf-3dadm";
    public static final String NMFAF_3CADM = "nmfaf-3cadm";
    public static final String NEASDF_DNSCONTEXT = "neasdf-dnscontext";
    public static final String NEASDF_BASELINEDNSPATTERN = "neasdf-baselinednspattern";
    public static final String NDCCF_DM = "ndccf-dm";
    public static final String NDCCF_CM = "ndccf-cm";
    public static final String NNSACF_NSAC = "nnsacf-nsac";
    public static final String NNSACF_SLICE_EE = "nnsacf-slice-ee";
    public static final String NMBSMF_TMGI = "nmbsmf-tmgi";
    public static final String NMBSMF_MBSSESSION = "nmbsmf-mbssession";
    public static final String NADRF_DM = "nadrf-dm";
    public static final String NBSP_GBA = "nbsp-gba";
    public static final String NTSCTSF_TIME_SYNC = "ntsctsf-time-sync";
    public static final String NTSCTSF_QOS_TSCAI = "ntsctsf-qos-tscai";
    public static final String NTSCTSF_ASTI = "ntsctsf-asti";
    public static final String NPKMF_KEYREQ = "npkmf-keyreq";
    public static final String NMNPF_NPSTATUS = "nmnpf-npstatus";
    public static final String NIWMSC_SMSERVICE = "niwmsc-smservice";
    public static final String NMBSF_MBSUSERSERV = "nmbsf-mbsuserserv";
    public static final String NMBSF_MBSUSERDATAING = "nmbsf-mbsuserdataing";
    public static final String NMBSTF_DISTSESSION = "nmbstf-distsession";
    public static final String NPANF_PROSEKEY = "npanf-prosekey";

}
