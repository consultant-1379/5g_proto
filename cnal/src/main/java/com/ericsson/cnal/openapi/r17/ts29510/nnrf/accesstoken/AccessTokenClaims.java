/*
 * NRF OAuth2
 * NRF OAuth2 Authorization.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnIdNid;
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
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The claims data structure for the access token
 */
@ApiModel(description = "The claims data structure for the access token")
@JsonPropertyOrder({ AccessTokenClaims.JSON_PROPERTY_ISS,
                     AccessTokenClaims.JSON_PROPERTY_SUB,
                     AccessTokenClaims.JSON_PROPERTY_AUD,
                     AccessTokenClaims.JSON_PROPERTY_SCOPE,
                     AccessTokenClaims.JSON_PROPERTY_EXP,
                     AccessTokenClaims.JSON_PROPERTY_CONSUMER_PLMN_ID,
                     AccessTokenClaims.JSON_PROPERTY_CONSUMER_SNPN_ID,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_PLMN_ID,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_SNPN_ID,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_SNSSAI_LIST,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_NSI_LIST,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_NF_SET_ID,
                     AccessTokenClaims.JSON_PROPERTY_PRODUCER_NF_SERVICE_SET_ID,
                     AccessTokenClaims.JSON_PROPERTY_SOURCE_NF_INSTANCE_ID })
public class AccessTokenClaims
{
    public static final String JSON_PROPERTY_ISS = "iss";
    private UUID iss;

    public static final String JSON_PROPERTY_SUB = "sub";
    private UUID sub;

    public static final String JSON_PROPERTY_AUD = "aud";
    private Object aud;

    public static final String JSON_PROPERTY_SCOPE = "scope";
    private String scope;

    public static final String JSON_PROPERTY_EXP = "exp";
    private Integer exp;

    public static final String JSON_PROPERTY_CONSUMER_PLMN_ID = "consumerPlmnId";
    private PlmnId consumerPlmnId;

    public static final String JSON_PROPERTY_CONSUMER_SNPN_ID = "consumerSnpnId";
    private PlmnIdNid consumerSnpnId;

    public static final String JSON_PROPERTY_PRODUCER_PLMN_ID = "producerPlmnId";
    private PlmnId producerPlmnId;

    public static final String JSON_PROPERTY_PRODUCER_SNPN_ID = "producerSnpnId";
    private PlmnIdNid producerSnpnId;

    public static final String JSON_PROPERTY_PRODUCER_SNSSAI_LIST = "producerSnssaiList";
    private List<Snssai> producerSnssaiList = null;

    public static final String JSON_PROPERTY_PRODUCER_NSI_LIST = "producerNsiList";
    private List<String> producerNsiList = null;

    public static final String JSON_PROPERTY_PRODUCER_NF_SET_ID = "producerNfSetId";
    private String producerNfSetId;

    public static final String JSON_PROPERTY_PRODUCER_NF_SERVICE_SET_ID = "producerNfServiceSetId";
    private String producerNfServiceSetId;

    public static final String JSON_PROPERTY_SOURCE_NF_INSTANCE_ID = "sourceNfInstanceId";
    private UUID sourceNfInstanceId;

    public AccessTokenClaims()
    {
    }

    public AccessTokenClaims iss(UUID iss)
    {

        this.iss = iss;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return iss
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_ISS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getIss()
    {
        return iss;
    }

    @JsonProperty(JSON_PROPERTY_ISS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIss(UUID iss)
    {
        this.iss = iss;
    }

    public AccessTokenClaims sub(UUID sub)
    {

        this.sub = sub;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return sub
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_SUB)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getSub()
    {
        return sub;
    }

    @JsonProperty(JSON_PROPERTY_SUB)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSub(UUID sub)
    {
        this.sub = sub;
    }

    public AccessTokenClaims aud(Object aud)
    {

        this.aud = aud;
        return this;
    }

    /**
     * Get aud
     * 
     * @return aud
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_AUD)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Object getAud()
    {
        return aud;
    }

    @JsonProperty(JSON_PROPERTY_AUD)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAud(Object aud)
    {
        this.aud = aud;
    }

    public AccessTokenClaims scope(String scope)
    {

        this.scope = scope;
        return this;
    }

    /**
     * Get scope
     * 
     * @return scope
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SCOPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getScope()
    {
        return scope;
    }

    @JsonProperty(JSON_PROPERTY_SCOPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public AccessTokenClaims exp(Integer exp)
    {

        this.exp = exp;
        return this;
    }

    /**
     * Get exp
     * 
     * @return exp
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EXP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getExp()
    {
        return exp;
    }

    @JsonProperty(JSON_PROPERTY_EXP)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setExp(Integer exp)
    {
        this.exp = exp;
    }

    public AccessTokenClaims consumerPlmnId(PlmnId consumerPlmnId)
    {

        this.consumerPlmnId = consumerPlmnId;
        return this;
    }

    /**
     * Get consumerPlmnId
     * 
     * @return consumerPlmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CONSUMER_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getConsumerPlmnId()
    {
        return consumerPlmnId;
    }

    @JsonProperty(JSON_PROPERTY_CONSUMER_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConsumerPlmnId(PlmnId consumerPlmnId)
    {
        this.consumerPlmnId = consumerPlmnId;
    }

    public AccessTokenClaims consumerSnpnId(PlmnIdNid consumerSnpnId)
    {

        this.consumerSnpnId = consumerSnpnId;
        return this;
    }

    /**
     * Get consumerSnpnId
     * 
     * @return consumerSnpnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CONSUMER_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnIdNid getConsumerSnpnId()
    {
        return consumerSnpnId;
    }

    @JsonProperty(JSON_PROPERTY_CONSUMER_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConsumerSnpnId(PlmnIdNid consumerSnpnId)
    {
        this.consumerSnpnId = consumerSnpnId;
    }

    public AccessTokenClaims producerPlmnId(PlmnId producerPlmnId)
    {

        this.producerPlmnId = producerPlmnId;
        return this;
    }

    /**
     * Get producerPlmnId
     * 
     * @return producerPlmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRODUCER_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getProducerPlmnId()
    {
        return producerPlmnId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerPlmnId(PlmnId producerPlmnId)
    {
        this.producerPlmnId = producerPlmnId;
    }

    public AccessTokenClaims producerSnpnId(PlmnIdNid producerSnpnId)
    {

        this.producerSnpnId = producerSnpnId;
        return this;
    }

    /**
     * Get producerSnpnId
     * 
     * @return producerSnpnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRODUCER_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnIdNid getProducerSnpnId()
    {
        return producerSnpnId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerSnpnId(PlmnIdNid producerSnpnId)
    {
        this.producerSnpnId = producerSnpnId;
    }

    public AccessTokenClaims producerSnssaiList(List<Snssai> producerSnssaiList)
    {

        this.producerSnssaiList = producerSnssaiList;
        return this;
    }

    public AccessTokenClaims addProducerSnssaiListItem(Snssai producerSnssaiListItem)
    {
        if (this.producerSnssaiList == null)
        {
            this.producerSnssaiList = new ArrayList<>();
        }
        this.producerSnssaiList.add(producerSnssaiListItem);
        return this;
    }

    /**
     * Get producerSnssaiList
     * 
     * @return producerSnssaiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRODUCER_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getProducerSnssaiList()
    {
        return producerSnssaiList;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerSnssaiList(List<Snssai> producerSnssaiList)
    {
        this.producerSnssaiList = producerSnssaiList;
    }

    public AccessTokenClaims producerNsiList(List<String> producerNsiList)
    {

        this.producerNsiList = producerNsiList;
        return this;
    }

    public AccessTokenClaims addProducerNsiListItem(String producerNsiListItem)
    {
        if (this.producerNsiList == null)
        {
            this.producerNsiList = new ArrayList<>();
        }
        this.producerNsiList.add(producerNsiListItem);
        return this;
    }

    /**
     * Get producerNsiList
     * 
     * @return producerNsiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRODUCER_NSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getProducerNsiList()
    {
        return producerNsiList;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_NSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerNsiList(List<String> producerNsiList)
    {
        this.producerNsiList = producerNsiList;
    }

    public AccessTokenClaims producerNfSetId(String producerNfSetId)
    {

        this.producerNfSetId = producerNfSetId;
        return this;
    }

    /**
     * NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the
     * following string \&quot;set&lt;Set
     * ID&gt;.&lt;nftype&gt;set.5gc.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;, or
     * \&quot;set&lt;SetID&gt;.&lt;NFType&gt;set.5gc.nid&lt;NID&gt;.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;
     * with &lt;MCC&gt; encoded as defined in clause 5.4.2 (\&quot;Mcc\&quot; data
     * type definition) &lt;MNC&gt; encoding the Mobile Network Code part of the
     * PLMN, comprising 3 digits. If there are only 2 significant digits in the MNC,
     * one \&quot;0\&quot; digit shall be inserted at the left side to fill the 3
     * digits coding of MNC. Pattern: &#39;^[0-9]{3}$&#39; &lt;NFType&gt; encoded as
     * a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but with lower case
     * characters &lt;Set ID&gt; encoded as a string of characters consisting of
     * alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and
     * that shall end with either an alphabetic character or a digit.
     * 
     * @return producerNfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_PRODUCER_NF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProducerNfSetId()
    {
        return producerNfSetId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_NF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerNfSetId(String producerNfSetId)
    {
        this.producerNfSetId = producerNfSetId;
    }

    public AccessTokenClaims producerNfServiceSetId(String producerNfServiceSetId)
    {

        this.producerNfServiceSetId = producerNfServiceSetId;
        return this;
    }

    /**
     * NF Service Set Identifier (see clause 28.12 of 3GPP TS 23.003) formatted as
     * the following string \&quot;set&lt;Set ID&gt;.sn&lt;Service
     * Name&gt;.nfi&lt;NF Instance ID&gt;.5gc.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;,
     * or
     * \&quot;set&lt;SetID&gt;.sn&lt;ServiceName&gt;.nfi&lt;NFInstanceID&gt;.5gc.nid&lt;NID&gt;.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;
     * with &lt;MCC&gt; encoded as defined in clause 5.4.2 (\&quot;Mcc\&quot; data
     * type definition) &lt;MNC&gt; encoding the Mobile Network Code part of the
     * PLMN, comprising 3 digits. If there are only 2 significant digits in the MNC,
     * one \&quot;0\&quot; digit shall be inserted at the left side to fill the 3
     * digits coding of MNC. Pattern: &#39;^[0-9]{3}$&#39; &lt;NID&gt; encoded as
     * defined in clause 5.4.2 (\&quot;Nid\&quot; data type definition)
     * &lt;NFInstanceId&gt; encoded as defined in clause 5.3.2 &lt;ServiceName&gt;
     * encoded as defined in 3GPP TS 29.510 &lt;Set ID&gt; encoded as a string of
     * characters consisting of alphabetic characters (A-Z and a-z), digits (0-9)
     * and/or the hyphen (-) and that shall end with either an alphabetic character
     * or a digit.
     * 
     * @return producerNfServiceSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Service Set Identifier (see clause 28.12 of 3GPP TS 23.003) formatted as the following  string \"set<Set ID>.sn<Service Name>.nfi<NF Instance ID>.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.sn<ServiceName>.nfi<NFInstanceID>.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)   <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NID> encoded as defined in clause 5.4.2 (\"Nid\" data type definition)  <NFInstanceId> encoded as defined in clause 5.3.2  <ServiceName> encoded as defined in 3GPP TS 29.510  <Set ID> encoded as a string of characters consisting of alphabetic    characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that shall end    with either an alphabetic character or a digit. ")
    @JsonProperty(JSON_PROPERTY_PRODUCER_NF_SERVICE_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProducerNfServiceSetId()
    {
        return producerNfServiceSetId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_NF_SERVICE_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerNfServiceSetId(String producerNfServiceSetId)
    {
        this.producerNfServiceSetId = producerNfServiceSetId;
    }

    public AccessTokenClaims sourceNfInstanceId(UUID sourceNfInstanceId)
    {

        this.sourceNfInstanceId = sourceNfInstanceId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return sourceNfInstanceId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_SOURCE_NF_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getSourceNfInstanceId()
    {
        return sourceNfInstanceId;
    }

    @JsonProperty(JSON_PROPERTY_SOURCE_NF_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceNfInstanceId(UUID sourceNfInstanceId)
    {
        this.sourceNfInstanceId = sourceNfInstanceId;
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
        AccessTokenClaims accessTokenClaims = (AccessTokenClaims) o;
        return Objects.equals(this.iss, accessTokenClaims.iss) && Objects.equals(this.sub, accessTokenClaims.sub)
               && Objects.equals(this.aud, accessTokenClaims.aud) && Objects.equals(this.scope, accessTokenClaims.scope)
               && Objects.equals(this.exp, accessTokenClaims.exp) && Objects.equals(this.consumerPlmnId, accessTokenClaims.consumerPlmnId)
               && Objects.equals(this.consumerSnpnId, accessTokenClaims.consumerSnpnId) && Objects.equals(this.producerPlmnId, accessTokenClaims.producerPlmnId)
               && Objects.equals(this.producerSnpnId, accessTokenClaims.producerSnpnId)
               && Objects.equals(this.producerSnssaiList, accessTokenClaims.producerSnssaiList)
               && Objects.equals(this.producerNsiList, accessTokenClaims.producerNsiList)
               && Objects.equals(this.producerNfSetId, accessTokenClaims.producerNfSetId)
               && Objects.equals(this.producerNfServiceSetId, accessTokenClaims.producerNfServiceSetId)
               && Objects.equals(this.sourceNfInstanceId, accessTokenClaims.sourceNfInstanceId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(iss,
                            sub,
                            aud,
                            scope,
                            exp,
                            consumerPlmnId,
                            consumerSnpnId,
                            producerPlmnId,
                            producerSnpnId,
                            producerSnssaiList,
                            producerNsiList,
                            producerNfSetId,
                            producerNfServiceSetId,
                            sourceNfInstanceId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessTokenClaims {\n");
        sb.append("    iss: ").append(toIndentedString(iss)).append("\n");
        sb.append("    sub: ").append(toIndentedString(sub)).append("\n");
        sb.append("    aud: ").append(toIndentedString(aud)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
        sb.append("    exp: ").append(toIndentedString(exp)).append("\n");
        sb.append("    consumerPlmnId: ").append(toIndentedString(consumerPlmnId)).append("\n");
        sb.append("    consumerSnpnId: ").append(toIndentedString(consumerSnpnId)).append("\n");
        sb.append("    producerPlmnId: ").append(toIndentedString(producerPlmnId)).append("\n");
        sb.append("    producerSnpnId: ").append(toIndentedString(producerSnpnId)).append("\n");
        sb.append("    producerSnssaiList: ").append(toIndentedString(producerSnssaiList)).append("\n");
        sb.append("    producerNsiList: ").append(toIndentedString(producerNsiList)).append("\n");
        sb.append("    producerNfSetId: ").append(toIndentedString(producerNfSetId)).append("\n");
        sb.append("    producerNfServiceSetId: ").append(toIndentedString(producerNfServiceSetId)).append("\n");
        sb.append("    sourceNfInstanceId: ").append(toIndentedString(sourceNfInstanceId)).append("\n");
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
