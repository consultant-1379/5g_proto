
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceName
{

    DEFAULT("default"),
    NNRF_NFM("nnrf-nfm"),
    NNRF_DISC("nnrf-disc"),
    NNRF_OAUTH2("nnrf-oauth2"),
    NUDM_SDM("nudm-sdm"),
    NUDM_UECM("nudm-uecm"),
    NUDM_UEAU("nudm-ueau"),
    NUDM_EE("nudm-ee"),
    NUDM_PP("nudm-pp"),
    NUDM_NIDDAU("nudm-niddau"),
    NUDM_MT("nudm-mt"),
    NAMF_COMM("namf-comm"),
    NAMF_EVTS("namf-evts"),
    NAMF_MT("namf-mt"),
    NAMF_LOC("namf-loc"),
    NSMF_PDUSESSION("nsmf-pdusession"),
    NSMF_EVENT_EXPOSURE("nsmf-event-exposure"),
    NSMF_NIDD("nsmf-nidd"),
    NAUSF_AUTH("nausf-auth"),
    NAUSF_SORPROTECTION("nausf-sorprotection"),
    NAUSF_UPUPROTECTION("nausf-upuprotection"),
    NNEF_PFDMANAGEMENT("nnef-pfdmanagement"),
    NNEF_SMCONTEXT("nnef-smcontext"),
    NNEF_EVENTEXPOSURE("nnef-eventexposure"),
    NPCF_AM_POLICY_CONTROL("npcf-am-policy-control"),
    NPCF_SMPOLICYCONTROL("npcf-smpolicycontrol"),
    NPCF_POLICYAUTHORIZATION("npcf-policyauthorization"),
    NPCF_BDTPOLICYCONTROL("npcf-bdtpolicycontrol"),
    NPCF_EVENTEXPOSURE("npcf-eventexposure"),
    NPCF_UE_POLICY_CONTROL("npcf-ue-policy-control"),
    NSMSF_SMS("nsmsf-sms"),
    NNSSF_NSSELECTION("nnssf-nsselection"),
    NNSSF_NSSAIAVAILABILITY("nnssf-nssaiavailability"),
    NUDR_DR("nudr-dr"),
    NUDR_GROUP_ID_MAP("nudr-group-id-map"),
    NLMF_LOC("nlmf-loc"),
    N5G_EIR_EIC("n5g-eir-eic"),
    NBSF_MANAGEMENT("nbsf-management"),
    NCHF_SPENDINGLIMITCONTROL("nchf-spendinglimitcontrol"),
    NCHF_CONVERGEDCHARGING("nchf-convergedcharging"),
    NCHF_OFFLINEONLYCHARGING("nchf-offlineonlycharging"),
    NNWDAF_EVENTSSUBSCRIPTION("nnwdaf-eventssubscription"),
    NNWDAF_ANALYTICSINFO("nnwdaf-analyticsinfo"),
    NGMLC_LOC("ngmlc-loc"),
    NUCMF_PROVISIONING("nucmf-provisioning"),
    NUCMF_UECAPABILITYMANAGEMENT("nucmf-uecapabilitymanagement"),
    NHSS_SDM("nhss-sdm"),
    NHSS_UECM("nhss-uecm"),
    NHSS_UEAU("nhss-ueau"),
    NHSS_EE("nhss-ee"),
    NHSS_IMS_SDM("nhss-ims-sdm"),
    NHSS_IMS_UECM("nhss-ims-uecm"),
    NHSS_IMS_UEAU("nhss-ims-ueau"),
    NSEPP_TELESCOPIC("nsepp-telescopic"),
    NSORAF_SOR("nsoraf-sor"),
    NSPAF_SECURED_PACKET("nspaf-secured-packet"),
    NUDSF_DR("nudsf-dr"),
    NNSSAAF_NSSAA("nnssaaf-nssaa");

    private final String value;
    private final static Map<String, ServiceName> CONSTANTS = new HashMap<String, ServiceName>();

    static
    {
        for (ServiceName c : values())
        {
            CONSTANTS.put(c.value, c);
        }
    }

    private ServiceName(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    @JsonValue
    public String value()
    {
        return this.value;
    }

    @JsonCreator
    public static ServiceName fromValue(String value)
    {
        ServiceName constant = CONSTANTS.get(value);
        if (constant == null)
        {
            throw new IllegalArgumentException(value);
        }
        else
        {
            return constant;
        }
    }

}
