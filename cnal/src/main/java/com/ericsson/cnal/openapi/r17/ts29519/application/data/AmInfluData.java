/*
 * Unified Data Repository Service API file for Application Data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.application.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29534.npcf.ampolicyauthorization.ServiceAreaCoverageInfo;
import com.ericsson.cnal.openapi.r17.ts29522.aminfluence.DnnSnssaiInformation;
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
 * Represents the AM Influence Data.
 */
@ApiModel(description = "Represents the AM Influence Data.")
@JsonPropertyOrder({ AmInfluData.JSON_PROPERTY_APP_IDS,
                     AmInfluData.JSON_PROPERTY_DNN_SNSSAI_INFOS,
                     AmInfluData.JSON_PROPERTY_INTER_GROUP_ID,
                     AmInfluData.JSON_PROPERTY_SUPI,
                     AmInfluData.JSON_PROPERTY_ANY_UE_IND,
                     AmInfluData.JSON_PROPERTY_POLICY_DURATION,
                     AmInfluData.JSON_PROPERTY_EV_SUBS,
                     AmInfluData.JSON_PROPERTY_NOTIF_URI,
                     AmInfluData.JSON_PROPERTY_NOTIF_CORR_ID,
                     AmInfluData.JSON_PROPERTY_HEADERS,
                     AmInfluData.JSON_PROPERTY_THRU_REQ,
                     AmInfluData.JSON_PROPERTY_COV_REQ,
                     AmInfluData.JSON_PROPERTY_SUPPORTED_FEATURES,
                     AmInfluData.JSON_PROPERTY_RES_URI,
                     AmInfluData.JSON_PROPERTY_RESET_IDS })
public class AmInfluData
{
    public static final String JSON_PROPERTY_APP_IDS = "appIds";
    private List<String> appIds = null;

    public static final String JSON_PROPERTY_DNN_SNSSAI_INFOS = "dnnSnssaiInfos";
    private List<DnnSnssaiInformation> dnnSnssaiInfos = null;

    public static final String JSON_PROPERTY_INTER_GROUP_ID = "interGroupId";
    private String interGroupId;

    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_ANY_UE_IND = "anyUeInd";
    private Boolean anyUeInd;

    public static final String JSON_PROPERTY_POLICY_DURATION = "policyDuration";
    private Integer policyDuration;

    public static final String JSON_PROPERTY_EV_SUBS = "evSubs";
    private List<String> evSubs = null;

    public static final String JSON_PROPERTY_NOTIF_URI = "notifUri";
    private String notifUri;

    public static final String JSON_PROPERTY_NOTIF_CORR_ID = "notifCorrId";
    private String notifCorrId;

    public static final String JSON_PROPERTY_HEADERS = "headers";
    private List<String> headers = null;

    public static final String JSON_PROPERTY_THRU_REQ = "thruReq";
    private Boolean thruReq;

    public static final String JSON_PROPERTY_COV_REQ = "covReq";
    private List<ServiceAreaCoverageInfo> covReq = null;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_RES_URI = "resUri";
    private String resUri;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public AmInfluData()
    {
    }

    public AmInfluData appIds(List<String> appIds)
    {

        this.appIds = appIds;
        return this;
    }

    public AmInfluData addAppIdsItem(String appIdsItem)
    {
        if (this.appIds == null)
        {
            this.appIds = new ArrayList<>();
        }
        this.appIds.add(appIdsItem);
        return this;
    }

    /**
     * Identifies one or more applications.
     * 
     * @return appIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies one or more applications.")
    @JsonProperty(JSON_PROPERTY_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAppIds()
    {
        return appIds;
    }

    @JsonProperty(JSON_PROPERTY_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppIds(List<String> appIds)
    {
        this.appIds = appIds;
    }

    public AmInfluData dnnSnssaiInfos(List<DnnSnssaiInformation> dnnSnssaiInfos)
    {

        this.dnnSnssaiInfos = dnnSnssaiInfos;
        return this;
    }

    public AmInfluData addDnnSnssaiInfosItem(DnnSnssaiInformation dnnSnssaiInfosItem)
    {
        if (this.dnnSnssaiInfos == null)
        {
            this.dnnSnssaiInfos = new ArrayList<>();
        }
        this.dnnSnssaiInfos.add(dnnSnssaiInfosItem);
        return this;
    }

    /**
     * Identifies one or more DNN, S-NSSAI combinations.
     * 
     * @return dnnSnssaiInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies one or more DNN, S-NSSAI combinations.")
    @JsonProperty(JSON_PROPERTY_DNN_SNSSAI_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DnnSnssaiInformation> getDnnSnssaiInfos()
    {
        return dnnSnssaiInfos;
    }

    @JsonProperty(JSON_PROPERTY_DNN_SNSSAI_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnnSnssaiInfos(List<DnnSnssaiInformation> dnnSnssaiInfos)
    {
        this.dnnSnssaiInfos = dnnSnssaiInfos;
    }

    public AmInfluData interGroupId(String interGroupId)
    {

        this.interGroupId = interGroupId;
        return this;
    }

    /**
     * String identifying a group of devices network internal globally unique ID
     * which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS
     * 23.003.
     * 
     * @return interGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a group of devices network internal globally unique ID which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS 23.003.  ")
    @JsonProperty(JSON_PROPERTY_INTER_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getInterGroupId()
    {
        return interGroupId;
    }

    @JsonProperty(JSON_PROPERTY_INTER_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInterGroupId(String interGroupId)
    {
        this.interGroupId = interGroupId;
    }

    public AmInfluData supi(String supi)
    {

        this.supi = supi;
        return this;
    }

    /**
     * String identifying a Supi that shall contain either an IMSI, a network
     * specific identifier, a Global Cable Identifier (GCI) or a Global Line
     * Identifier (GLI) as specified in clause 2.2A of 3GPP TS 23.003. It shall be
     * formatted as follows - for an IMSI \&quot;imsi-&lt;imsi&gt;\&quot;, where
     * &lt;imsi&gt; shall be formatted according to clause 2.2 of 3GPP TS 23.003
     * that describes an IMSI. - for a network specific identifier
     * \&quot;nai-&lt;nai&gt;, where &lt;nai&gt; shall be formatted according to
     * clause 28.7.2 of 3GPP TS 23.003 that describes an NAI. - for a GCI
     * \&quot;gci-&lt;gci&gt;\&quot;, where &lt;gci&gt; shall be formatted according
     * to clause 28.15.2 of 3GPP TS 23.003. - for a GLI
     * \&quot;gli-&lt;gli&gt;\&quot;, where &lt;gli&gt; shall be formatted according
     * to clause 28.16.2 of 3GPP TS 23.003.To enable that the value is used as part
     * of an URI, the string shall only contain characters allowed according to the
     * \&quot;lower-with-hyphen\&quot; naming convention defined in 3GPP TS 29.501.
     * 
     * @return supi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Supi that shall contain either an IMSI, a network specific identifier, a Global Cable Identifier (GCI) or a Global Line Identifier (GLI) as specified in clause  2.2A of 3GPP TS 23.003. It shall be formatted as follows  - for an IMSI \"imsi-<imsi>\", where <imsi> shall be formatted according to clause 2.2    of 3GPP TS 23.003 that describes an IMSI.  - for a network specific identifier \"nai-<nai>, where <nai> shall be formatted    according to clause 28.7.2 of 3GPP TS 23.003 that describes an NAI.  - for a GCI \"gci-<gci>\", where <gci> shall be formatted according to clause 28.15.2    of 3GPP TS 23.003.  - for a GLI \"gli-<gli>\", where <gli> shall be formatted according to clause 28.16.2 of    3GPP TS 23.003.To enable that the value is used as part of an URI, the string shall    only contain characters allowed according to the \"lower-with-hyphen\" naming convention    defined in 3GPP TS 29.501. ")
    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupi()
    {
        return supi;
    }

    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupi(String supi)
    {
        this.supi = supi;
    }

    public AmInfluData anyUeInd(Boolean anyUeInd)
    {

        this.anyUeInd = anyUeInd;
        return this;
    }

    /**
     * Indicates whether the data is applicable for any UE.
     * 
     * @return anyUeInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether the data is applicable for any UE.")
    @JsonProperty(JSON_PROPERTY_ANY_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAnyUeInd()
    {
        return anyUeInd;
    }

    @JsonProperty(JSON_PROPERTY_ANY_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnyUeInd(Boolean anyUeInd)
    {
        this.anyUeInd = anyUeInd;
    }

    public AmInfluData policyDuration(Integer policyDuration)
    {

        this.policyDuration = policyDuration;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return policyDuration
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_POLICY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPolicyDuration()
    {
        return policyDuration;
    }

    @JsonProperty(JSON_PROPERTY_POLICY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPolicyDuration(Integer policyDuration)
    {
        this.policyDuration = policyDuration;
    }

    public AmInfluData evSubs(List<String> evSubs)
    {

        this.evSubs = evSubs;
        return this;
    }

    public AmInfluData addEvSubsItem(String evSubsItem)
    {
        if (this.evSubs == null)
        {
            this.evSubs = new ArrayList<>();
        }
        this.evSubs.add(evSubsItem);
        return this;
    }

    /**
     * List of AM related events for which a subscription is required.
     * 
     * @return evSubs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "List of AM related events for which a subscription is required.")
    @JsonProperty(JSON_PROPERTY_EV_SUBS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEvSubs()
    {
        return evSubs;
    }

    @JsonProperty(JSON_PROPERTY_EV_SUBS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEvSubs(List<String> evSubs)
    {
        this.evSubs = evSubs;
    }

    public AmInfluData notifUri(String notifUri)
    {

        this.notifUri = notifUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return notifUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNotifUri()
    {
        return notifUri;
    }

    @JsonProperty(JSON_PROPERTY_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifUri(String notifUri)
    {
        this.notifUri = notifUri;
    }

    public AmInfluData notifCorrId(String notifCorrId)
    {

        this.notifCorrId = notifCorrId;
        return this;
    }

    /**
     * Notification correlation identifier.
     * 
     * @return notifCorrId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Notification correlation identifier.")
    @JsonProperty(JSON_PROPERTY_NOTIF_CORR_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNotifCorrId()
    {
        return notifCorrId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIF_CORR_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifCorrId(String notifCorrId)
    {
        this.notifCorrId = notifCorrId;
    }

    public AmInfluData headers(List<String> headers)
    {

        this.headers = headers;
        return this;
    }

    public AmInfluData addHeadersItem(String headersItem)
    {
        if (this.headers == null)
        {
            this.headers = new ArrayList<>();
        }
        this.headers.add(headersItem);
        return this;
    }

    /**
     * Contains the headers provisioned by the NEF.
     * 
     * @return headers
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the headers provisioned by the NEF.")
    @JsonProperty(JSON_PROPERTY_HEADERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getHeaders()
    {
        return headers;
    }

    @JsonProperty(JSON_PROPERTY_HEADERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeaders(List<String> headers)
    {
        this.headers = headers;
    }

    public AmInfluData thruReq(Boolean thruReq)
    {

        this.thruReq = thruReq;
        return this;
    }

    /**
     * Indicates whether high throughput is desired for the indicated UE traffic.
     * 
     * @return thruReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether high throughput is desired for the indicated UE traffic.")
    @JsonProperty(JSON_PROPERTY_THRU_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getThruReq()
    {
        return thruReq;
    }

    @JsonProperty(JSON_PROPERTY_THRU_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThruReq(Boolean thruReq)
    {
        this.thruReq = thruReq;
    }

    public AmInfluData covReq(List<ServiceAreaCoverageInfo> covReq)
    {

        this.covReq = covReq;
        return this;
    }

    public AmInfluData addCovReqItem(ServiceAreaCoverageInfo covReqItem)
    {
        if (this.covReq == null)
        {
            this.covReq = new ArrayList<>();
        }
        this.covReq.add(covReqItem);
        return this;
    }

    /**
     * Indicates the service area coverage requirement.
     * 
     * @return covReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the service area coverage requirement.")
    @JsonProperty(JSON_PROPERTY_COV_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ServiceAreaCoverageInfo> getCovReq()
    {
        return covReq;
    }

    @JsonProperty(JSON_PROPERTY_COV_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCovReq(List<ServiceAreaCoverageInfo> covReq)
    {
        this.covReq = covReq;
    }

    public AmInfluData supportedFeatures(String supportedFeatures)
    {

        this.supportedFeatures = supportedFeatures;
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
     * @return supportedFeatures
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupportedFeatures()
    {
        return supportedFeatures;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
    }

    public AmInfluData resUri(String resUri)
    {

        this.resUri = resUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return resUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_RES_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getResUri()
    {
        return resUri;
    }

    @JsonProperty(JSON_PROPERTY_RES_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResUri(String resUri)
    {
        this.resUri = resUri;
    }

    public AmInfluData resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public AmInfluData addResetIdsItem(String resetIdsItem)
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
        AmInfluData amInfluData = (AmInfluData) o;
        return Objects.equals(this.appIds, amInfluData.appIds) && Objects.equals(this.dnnSnssaiInfos, amInfluData.dnnSnssaiInfos)
               && Objects.equals(this.interGroupId, amInfluData.interGroupId) && Objects.equals(this.supi, amInfluData.supi)
               && Objects.equals(this.anyUeInd, amInfluData.anyUeInd) && Objects.equals(this.policyDuration, amInfluData.policyDuration)
               && Objects.equals(this.evSubs, amInfluData.evSubs) && Objects.equals(this.notifUri, amInfluData.notifUri)
               && Objects.equals(this.notifCorrId, amInfluData.notifCorrId) && Objects.equals(this.headers, amInfluData.headers)
               && Objects.equals(this.thruReq, amInfluData.thruReq) && Objects.equals(this.covReq, amInfluData.covReq)
               && Objects.equals(this.supportedFeatures, amInfluData.supportedFeatures) && Objects.equals(this.resUri, amInfluData.resUri)
               && Objects.equals(this.resetIds, amInfluData.resetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(appIds,
                            dnnSnssaiInfos,
                            interGroupId,
                            supi,
                            anyUeInd,
                            policyDuration,
                            evSubs,
                            notifUri,
                            notifCorrId,
                            headers,
                            thruReq,
                            covReq,
                            supportedFeatures,
                            resUri,
                            resetIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmInfluData {\n");
        sb.append("    appIds: ").append(toIndentedString(appIds)).append("\n");
        sb.append("    dnnSnssaiInfos: ").append(toIndentedString(dnnSnssaiInfos)).append("\n");
        sb.append("    interGroupId: ").append(toIndentedString(interGroupId)).append("\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    anyUeInd: ").append(toIndentedString(anyUeInd)).append("\n");
        sb.append("    policyDuration: ").append(toIndentedString(policyDuration)).append("\n");
        sb.append("    evSubs: ").append(toIndentedString(evSubs)).append("\n");
        sb.append("    notifUri: ").append(toIndentedString(notifUri)).append("\n");
        sb.append("    notifCorrId: ").append(toIndentedString(notifCorrId)).append("\n");
        sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
        sb.append("    thruReq: ").append(toIndentedString(thruReq)).append("\n");
        sb.append("    covReq: ").append(toIndentedString(covReq)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    resUri: ").append(toIndentedString(resUri)).append("\n");
        sb.append("    resetIds: ").append(toIndentedString(resetIds)).append("\n");
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
