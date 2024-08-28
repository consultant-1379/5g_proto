/*
 * Unified Data Repository Service API file for subscription data
 * Unified Data Repository Service (subscription data).   The API version is defined in 3GPP TS 29.504.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29505.subscription.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.RoamingInfoUpdate;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.PeiUpdateInfo;
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
 * Contains the context data sets.
 */
@ApiModel(description = "Contains the context data sets.")
@JsonPropertyOrder({ ContextDataSets.JSON_PROPERTY_AMF3_GPP,
                     ContextDataSets.JSON_PROPERTY_AMF_NON3_GPP,
                     ContextDataSets.JSON_PROPERTY_SDM_SUBSCRIPTIONS,
                     ContextDataSets.JSON_PROPERTY_EE_SUBSCRIPTIONS,
                     ContextDataSets.JSON_PROPERTY_SMSF3_GPP_ACCESS,
                     ContextDataSets.JSON_PROPERTY_SMSF_NON3_GPP_ACCESS,
                     ContextDataSets.JSON_PROPERTY_SUBSCRIPTION_DATA_SUBSCRIPTIONS,
                     ContextDataSets.JSON_PROPERTY_SMF_REGISTRATIONS,
                     ContextDataSets.JSON_PROPERTY_IP_SM_GW,
                     ContextDataSets.JSON_PROPERTY_ROAMING_INFO,
                     ContextDataSets.JSON_PROPERTY_PEI_INFO })
public class ContextDataSets
{
    public static final String JSON_PROPERTY_AMF3_GPP = "amf3Gpp";
    private Amf3GppAccessRegistration amf3Gpp;

    public static final String JSON_PROPERTY_AMF_NON3_GPP = "amfNon3Gpp";
    private AmfNon3GppAccessRegistration amfNon3Gpp;

    public static final String JSON_PROPERTY_SDM_SUBSCRIPTIONS = "sdmSubscriptions";
    private List<SdmSubscription> sdmSubscriptions = null;

    public static final String JSON_PROPERTY_EE_SUBSCRIPTIONS = "eeSubscriptions";
    private List<EeSubscription> eeSubscriptions = null;

    public static final String JSON_PROPERTY_SMSF3_GPP_ACCESS = "smsf3GppAccess";
    private SmsfRegistration smsf3GppAccess;

    public static final String JSON_PROPERTY_SMSF_NON3_GPP_ACCESS = "smsfNon3GppAccess";
    private SmsfRegistration smsfNon3GppAccess;

    public static final String JSON_PROPERTY_SUBSCRIPTION_DATA_SUBSCRIPTIONS = "subscriptionDataSubscriptions";
    private List<SubscriptionDataSubscriptions> subscriptionDataSubscriptions = null;

    public static final String JSON_PROPERTY_SMF_REGISTRATIONS = "smfRegistrations";
    private List<SmfRegistration> smfRegistrations = null;

    public static final String JSON_PROPERTY_IP_SM_GW = "ipSmGw";
    private IpSmGwRegistration ipSmGw;

    public static final String JSON_PROPERTY_ROAMING_INFO = "roamingInfo";
    private RoamingInfoUpdate roamingInfo;

    public static final String JSON_PROPERTY_PEI_INFO = "peiInfo";
    private PeiUpdateInfo peiInfo;

    public ContextDataSets()
    {
    }

    public ContextDataSets amf3Gpp(Amf3GppAccessRegistration amf3Gpp)
    {

        this.amf3Gpp = amf3Gpp;
        return this;
    }

    /**
     * Get amf3Gpp
     * 
     * @return amf3Gpp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AMF3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Amf3GppAccessRegistration getAmf3Gpp()
    {
        return amf3Gpp;
    }

    @JsonProperty(JSON_PROPERTY_AMF3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmf3Gpp(Amf3GppAccessRegistration amf3Gpp)
    {
        this.amf3Gpp = amf3Gpp;
    }

    public ContextDataSets amfNon3Gpp(AmfNon3GppAccessRegistration amfNon3Gpp)
    {

        this.amfNon3Gpp = amfNon3Gpp;
        return this;
    }

    /**
     * Get amfNon3Gpp
     * 
     * @return amfNon3Gpp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AMF_NON3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmfNon3GppAccessRegistration getAmfNon3Gpp()
    {
        return amfNon3Gpp;
    }

    @JsonProperty(JSON_PROPERTY_AMF_NON3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfNon3Gpp(AmfNon3GppAccessRegistration amfNon3Gpp)
    {
        this.amfNon3Gpp = amfNon3Gpp;
    }

    public ContextDataSets sdmSubscriptions(List<SdmSubscription> sdmSubscriptions)
    {

        this.sdmSubscriptions = sdmSubscriptions;
        return this;
    }

    public ContextDataSets addSdmSubscriptionsItem(SdmSubscription sdmSubscriptionsItem)
    {
        if (this.sdmSubscriptions == null)
        {
            this.sdmSubscriptions = new ArrayList<>();
        }
        this.sdmSubscriptions.add(sdmSubscriptionsItem);
        return this;
    }

    /**
     * Get sdmSubscriptions
     * 
     * @return sdmSubscriptions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SDM_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SdmSubscription> getSdmSubscriptions()
    {
        return sdmSubscriptions;
    }

    @JsonProperty(JSON_PROPERTY_SDM_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSdmSubscriptions(List<SdmSubscription> sdmSubscriptions)
    {
        this.sdmSubscriptions = sdmSubscriptions;
    }

    public ContextDataSets eeSubscriptions(List<EeSubscription> eeSubscriptions)
    {

        this.eeSubscriptions = eeSubscriptions;
        return this;
    }

    public ContextDataSets addEeSubscriptionsItem(EeSubscription eeSubscriptionsItem)
    {
        if (this.eeSubscriptions == null)
        {
            this.eeSubscriptions = new ArrayList<>();
        }
        this.eeSubscriptions.add(eeSubscriptionsItem);
        return this;
    }

    /**
     * Get eeSubscriptions
     * 
     * @return eeSubscriptions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EE_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<EeSubscription> getEeSubscriptions()
    {
        return eeSubscriptions;
    }

    @JsonProperty(JSON_PROPERTY_EE_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEeSubscriptions(List<EeSubscription> eeSubscriptions)
    {
        this.eeSubscriptions = eeSubscriptions;
    }

    public ContextDataSets smsf3GppAccess(SmsfRegistration smsf3GppAccess)
    {

        this.smsf3GppAccess = smsf3GppAccess;
        return this;
    }

    /**
     * Get smsf3GppAccess
     * 
     * @return smsf3GppAccess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMSF3_GPP_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsfRegistration getSmsf3GppAccess()
    {
        return smsf3GppAccess;
    }

    @JsonProperty(JSON_PROPERTY_SMSF3_GPP_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsf3GppAccess(SmsfRegistration smsf3GppAccess)
    {
        this.smsf3GppAccess = smsf3GppAccess;
    }

    public ContextDataSets smsfNon3GppAccess(SmsfRegistration smsfNon3GppAccess)
    {

        this.smsfNon3GppAccess = smsfNon3GppAccess;
        return this;
    }

    /**
     * Get smsfNon3GppAccess
     * 
     * @return smsfNon3GppAccess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMSF_NON3_GPP_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsfRegistration getSmsfNon3GppAccess()
    {
        return smsfNon3GppAccess;
    }

    @JsonProperty(JSON_PROPERTY_SMSF_NON3_GPP_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsfNon3GppAccess(SmsfRegistration smsfNon3GppAccess)
    {
        this.smsfNon3GppAccess = smsfNon3GppAccess;
    }

    public ContextDataSets subscriptionDataSubscriptions(List<SubscriptionDataSubscriptions> subscriptionDataSubscriptions)
    {

        this.subscriptionDataSubscriptions = subscriptionDataSubscriptions;
        return this;
    }

    public ContextDataSets addSubscriptionDataSubscriptionsItem(SubscriptionDataSubscriptions subscriptionDataSubscriptionsItem)
    {
        if (this.subscriptionDataSubscriptions == null)
        {
            this.subscriptionDataSubscriptions = new ArrayList<>();
        }
        this.subscriptionDataSubscriptions.add(subscriptionDataSubscriptionsItem);
        return this;
    }

    /**
     * Get subscriptionDataSubscriptions
     * 
     * @return subscriptionDataSubscriptions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_DATA_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SubscriptionDataSubscriptions> getSubscriptionDataSubscriptions()
    {
        return subscriptionDataSubscriptions;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_DATA_SUBSCRIPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscriptionDataSubscriptions(List<SubscriptionDataSubscriptions> subscriptionDataSubscriptions)
    {
        this.subscriptionDataSubscriptions = subscriptionDataSubscriptions;
    }

    public ContextDataSets smfRegistrations(List<SmfRegistration> smfRegistrations)
    {

        this.smfRegistrations = smfRegistrations;
        return this;
    }

    public ContextDataSets addSmfRegistrationsItem(SmfRegistration smfRegistrationsItem)
    {
        if (this.smfRegistrations == null)
        {
            this.smfRegistrations = new ArrayList<>();
        }
        this.smfRegistrations.add(smfRegistrationsItem);
        return this;
    }

    /**
     * The list of all the SMF registrations of a UE.
     * 
     * @return smfRegistrations
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The list of all the SMF registrations of a UE.")
    @JsonProperty(JSON_PROPERTY_SMF_REGISTRATIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SmfRegistration> getSmfRegistrations()
    {
        return smfRegistrations;
    }

    @JsonProperty(JSON_PROPERTY_SMF_REGISTRATIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmfRegistrations(List<SmfRegistration> smfRegistrations)
    {
        this.smfRegistrations = smfRegistrations;
    }

    public ContextDataSets ipSmGw(IpSmGwRegistration ipSmGw)
    {

        this.ipSmGw = ipSmGw;
        return this;
    }

    /**
     * Get ipSmGw
     * 
     * @return ipSmGw
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IP_SM_GW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IpSmGwRegistration getIpSmGw()
    {
        return ipSmGw;
    }

    @JsonProperty(JSON_PROPERTY_IP_SM_GW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpSmGw(IpSmGwRegistration ipSmGw)
    {
        this.ipSmGw = ipSmGw;
    }

    public ContextDataSets roamingInfo(RoamingInfoUpdate roamingInfo)
    {

        this.roamingInfo = roamingInfo;
        return this;
    }

    /**
     * Get roamingInfo
     * 
     * @return roamingInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ROAMING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RoamingInfoUpdate getRoamingInfo()
    {
        return roamingInfo;
    }

    @JsonProperty(JSON_PROPERTY_ROAMING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoamingInfo(RoamingInfoUpdate roamingInfo)
    {
        this.roamingInfo = roamingInfo;
    }

    public ContextDataSets peiInfo(PeiUpdateInfo peiInfo)
    {

        this.peiInfo = peiInfo;
        return this;
    }

    /**
     * Get peiInfo
     * 
     * @return peiInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PEI_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PeiUpdateInfo getPeiInfo()
    {
        return peiInfo;
    }

    @JsonProperty(JSON_PROPERTY_PEI_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPeiInfo(PeiUpdateInfo peiInfo)
    {
        this.peiInfo = peiInfo;
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
        ContextDataSets contextDataSets = (ContextDataSets) o;
        return Objects.equals(this.amf3Gpp, contextDataSets.amf3Gpp) && Objects.equals(this.amfNon3Gpp, contextDataSets.amfNon3Gpp)
               && Objects.equals(this.sdmSubscriptions, contextDataSets.sdmSubscriptions)
               && Objects.equals(this.eeSubscriptions, contextDataSets.eeSubscriptions) && Objects.equals(this.smsf3GppAccess, contextDataSets.smsf3GppAccess)
               && Objects.equals(this.smsfNon3GppAccess, contextDataSets.smsfNon3GppAccess)
               && Objects.equals(this.subscriptionDataSubscriptions, contextDataSets.subscriptionDataSubscriptions)
               && Objects.equals(this.smfRegistrations, contextDataSets.smfRegistrations) && Objects.equals(this.ipSmGw, contextDataSets.ipSmGw)
               && Objects.equals(this.roamingInfo, contextDataSets.roamingInfo) && Objects.equals(this.peiInfo, contextDataSets.peiInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(amf3Gpp,
                            amfNon3Gpp,
                            sdmSubscriptions,
                            eeSubscriptions,
                            smsf3GppAccess,
                            smsfNon3GppAccess,
                            subscriptionDataSubscriptions,
                            smfRegistrations,
                            ipSmGw,
                            roamingInfo,
                            peiInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ContextDataSets {\n");
        sb.append("    amf3Gpp: ").append(toIndentedString(amf3Gpp)).append("\n");
        sb.append("    amfNon3Gpp: ").append(toIndentedString(amfNon3Gpp)).append("\n");
        sb.append("    sdmSubscriptions: ").append(toIndentedString(sdmSubscriptions)).append("\n");
        sb.append("    eeSubscriptions: ").append(toIndentedString(eeSubscriptions)).append("\n");
        sb.append("    smsf3GppAccess: ").append(toIndentedString(smsf3GppAccess)).append("\n");
        sb.append("    smsfNon3GppAccess: ").append(toIndentedString(smsfNon3GppAccess)).append("\n");
        sb.append("    subscriptionDataSubscriptions: ").append(toIndentedString(subscriptionDataSubscriptions)).append("\n");
        sb.append("    smfRegistrations: ").append(toIndentedString(smfRegistrations)).append("\n");
        sb.append("    ipSmGw: ").append(toIndentedString(ipSmGw)).append("\n");
        sb.append("    roamingInfo: ").append(toIndentedString(roamingInfo)).append("\n");
        sb.append("    peiInfo: ").append(toIndentedString(peiInfo)).append("\n");
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
