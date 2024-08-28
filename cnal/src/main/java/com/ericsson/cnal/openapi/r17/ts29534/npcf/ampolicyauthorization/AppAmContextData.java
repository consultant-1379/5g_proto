/*
 * Npcf_AMPolicyAuthorization Service API
 * PCF Access and Mobility Policy Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29534.npcf.ampolicyauthorization;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29507.npcf.ampolicycontrol.AsTimeDistributionParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents an Individual Application AM Context resource.
 */
@ApiModel(description = "Represents an Individual Application AM Context resource.")
@JsonPropertyOrder({ AppAmContextData.JSON_PROPERTY_SUPI,
                     AppAmContextData.JSON_PROPERTY_GPSI,
                     AppAmContextData.JSON_PROPERTY_TERM_NOTIF_URI,
                     AppAmContextData.JSON_PROPERTY_EV_SUBSC,
                     AppAmContextData.JSON_PROPERTY_SUPP_FEAT,
                     AppAmContextData.JSON_PROPERTY_EXPIRY,
                     AppAmContextData.JSON_PROPERTY_HIGH_THRU_IND,
                     AppAmContextData.JSON_PROPERTY_COV_REQ,
                     AppAmContextData.JSON_PROPERTY_AS_TIME_DIS_PARAM })
public class AppAmContextData
{
    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_TERM_NOTIF_URI = "termNotifUri";
    private String termNotifUri;

    public static final String JSON_PROPERTY_EV_SUBSC = "evSubsc";
    private AmEventsSubscData evSubsc;

    public static final String JSON_PROPERTY_SUPP_FEAT = "suppFeat";
    private String suppFeat;

    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private Integer expiry;

    public static final String JSON_PROPERTY_HIGH_THRU_IND = "highThruInd";
    private Boolean highThruInd;

    public static final String JSON_PROPERTY_COV_REQ = "covReq";
    private List<ServiceAreaCoverageInfo> covReq = null;

    public static final String JSON_PROPERTY_AS_TIME_DIS_PARAM = "asTimeDisParam";
    private JsonNullable<AsTimeDistributionParam> asTimeDisParam = JsonNullable.<AsTimeDistributionParam>undefined();

    public AppAmContextData()
    {
    }

    public AppAmContextData supi(String supi)
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
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String identifying a Supi that shall contain either an IMSI, a network specific identifier, a Global Cable Identifier (GCI) or a Global Line Identifier (GLI) as specified in clause  2.2A of 3GPP TS 23.003. It shall be formatted as follows  - for an IMSI \"imsi-<imsi>\", where <imsi> shall be formatted according to clause 2.2    of 3GPP TS 23.003 that describes an IMSI.  - for a network specific identifier \"nai-<nai>, where <nai> shall be formatted    according to clause 28.7.2 of 3GPP TS 23.003 that describes an NAI.  - for a GCI \"gci-<gci>\", where <gci> shall be formatted according to clause 28.15.2    of 3GPP TS 23.003.  - for a GLI \"gli-<gli>\", where <gli> shall be formatted according to clause 28.16.2 of    3GPP TS 23.003.To enable that the value is used as part of an URI, the string shall    only contain characters allowed according to the \"lower-with-hyphen\" naming convention    defined in 3GPP TS 29.501. ")
    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSupi()
    {
        return supi;
    }

    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSupi(String supi)
    {
        this.supi = supi;
    }

    public AppAmContextData gpsi(String gpsi)
    {

        this.gpsi = gpsi;
        return this;
    }

    /**
     * String identifying a Gpsi shall contain either an External Id or an MSISDN.
     * It shall be formatted as follows -External Identifier&#x3D;
     * \&quot;extid-&#39;extid&#39;, where &#39;extid&#39; shall be formatted
     * according to clause 19.7.2 of 3GPP TS 23.003 that describes an External
     * Identifier.
     * 
     * @return gpsi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Gpsi shall contain either an External Id or an MSISDN.  It shall be formatted as follows -External Identifier= \"extid-'extid', where 'extid'  shall be formatted according to clause 19.7.2 of 3GPP TS 23.003 that describes an  External Identifier.  ")
    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGpsi()
    {
        return gpsi;
    }

    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsi(String gpsi)
    {
        this.gpsi = gpsi;
    }

    public AppAmContextData termNotifUri(String termNotifUri)
    {

        this.termNotifUri = termNotifUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return termNotifUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_TERM_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTermNotifUri()
    {
        return termNotifUri;
    }

    @JsonProperty(JSON_PROPERTY_TERM_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTermNotifUri(String termNotifUri)
    {
        this.termNotifUri = termNotifUri;
    }

    public AppAmContextData evSubsc(AmEventsSubscData evSubsc)
    {

        this.evSubsc = evSubsc;
        return this;
    }

    /**
     * Get evSubsc
     * 
     * @return evSubsc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EV_SUBSC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmEventsSubscData getEvSubsc()
    {
        return evSubsc;
    }

    @JsonProperty(JSON_PROPERTY_EV_SUBSC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEvSubsc(AmEventsSubscData evSubsc)
    {
        this.evSubsc = evSubsc;
    }

    public AppAmContextData suppFeat(String suppFeat)
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

    public AppAmContextData expiry(Integer expiry)
    {

        this.expiry = expiry;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return expiry
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getExpiry()
    {
        return expiry;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpiry(Integer expiry)
    {
        this.expiry = expiry;
    }

    public AppAmContextData highThruInd(Boolean highThruInd)
    {

        this.highThruInd = highThruInd;
        return this;
    }

    /**
     * Indicates whether high throughput is desired for the indicated UE traffic.
     * 
     * @return highThruInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether high throughput is desired for the indicated UE traffic.")
    @JsonProperty(JSON_PROPERTY_HIGH_THRU_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHighThruInd()
    {
        return highThruInd;
    }

    @JsonProperty(JSON_PROPERTY_HIGH_THRU_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHighThruInd(Boolean highThruInd)
    {
        this.highThruInd = highThruInd;
    }

    public AppAmContextData covReq(List<ServiceAreaCoverageInfo> covReq)
    {

        this.covReq = covReq;
        return this;
    }

    public AppAmContextData addCovReqItem(ServiceAreaCoverageInfo covReqItem)
    {
        if (this.covReq == null)
        {
            this.covReq = new ArrayList<>();
        }
        this.covReq.add(covReqItem);
        return this;
    }

    /**
     * Identifies a list of Tracking Areas per serving network where service is
     * allowed.
     * 
     * @return covReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies a list of Tracking Areas per serving network where service is allowed.")
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

    public AppAmContextData asTimeDisParam(AsTimeDistributionParam asTimeDisParam)
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
        AppAmContextData appAmContextData = (AppAmContextData) o;
        return Objects.equals(this.supi, appAmContextData.supi) && Objects.equals(this.gpsi, appAmContextData.gpsi)
               && Objects.equals(this.termNotifUri, appAmContextData.termNotifUri) && Objects.equals(this.evSubsc, appAmContextData.evSubsc)
               && Objects.equals(this.suppFeat, appAmContextData.suppFeat) && Objects.equals(this.expiry, appAmContextData.expiry)
               && Objects.equals(this.highThruInd, appAmContextData.highThruInd) && Objects.equals(this.covReq, appAmContextData.covReq)
               && equalsNullable(this.asTimeDisParam, appAmContextData.asTimeDisParam);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supi, gpsi, termNotifUri, evSubsc, suppFeat, expiry, highThruInd, covReq, hashCodeNullable(asTimeDisParam));
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
        sb.append("class AppAmContextData {\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    termNotifUri: ").append(toIndentedString(termNotifUri)).append("\n");
        sb.append("    evSubsc: ").append(toIndentedString(evSubsc)).append("\n");
        sb.append("    suppFeat: ").append(toIndentedString(suppFeat)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    highThruInd: ").append(toIndentedString(highThruInd)).append("\n");
        sb.append("    covReq: ").append(toIndentedString(covReq)).append("\n");
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
