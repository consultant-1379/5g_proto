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
import com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol.TransferPolicy;
import com.ericsson.cnal.openapi.r17.ts29122.commondata.UsageThreshold;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol.NetworkAreaInfo;
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
 * Contains the background data transfer data.
 */
@ApiModel(description = "Contains the background data transfer data.")
@JsonPropertyOrder({ BdtData.JSON_PROPERTY_ASP_ID,
                     BdtData.JSON_PROPERTY_TRANS_POLICY,
                     BdtData.JSON_PROPERTY_BDT_REF_ID,
                     BdtData.JSON_PROPERTY_NW_AREA_INFO,
                     BdtData.JSON_PROPERTY_NUM_OF_UES,
                     BdtData.JSON_PROPERTY_VOL_PER_UE,
                     BdtData.JSON_PROPERTY_DNN,
                     BdtData.JSON_PROPERTY_SNSSAI,
                     BdtData.JSON_PROPERTY_TRAFFIC_DES,
                     BdtData.JSON_PROPERTY_BDTP_STATUS,
                     BdtData.JSON_PROPERTY_SUPP_FEAT,
                     BdtData.JSON_PROPERTY_RESET_IDS })
public class BdtData
{
    public static final String JSON_PROPERTY_ASP_ID = "aspId";
    private String aspId;

    public static final String JSON_PROPERTY_TRANS_POLICY = "transPolicy";
    private TransferPolicy transPolicy;

    public static final String JSON_PROPERTY_BDT_REF_ID = "bdtRefId";
    private String bdtRefId;

    public static final String JSON_PROPERTY_NW_AREA_INFO = "nwAreaInfo";
    private NetworkAreaInfo nwAreaInfo;

    public static final String JSON_PROPERTY_NUM_OF_UES = "numOfUes";
    private Integer numOfUes;

    public static final String JSON_PROPERTY_VOL_PER_UE = "volPerUe";
    private UsageThreshold volPerUe;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_TRAFFIC_DES = "trafficDes";
    private String trafficDes;

    public static final String JSON_PROPERTY_BDTP_STATUS = "bdtpStatus";
    private String bdtpStatus;

    public static final String JSON_PROPERTY_SUPP_FEAT = "suppFeat";
    private String suppFeat;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public BdtData()
    {
    }

    public BdtData aspId(String aspId)
    {

        this.aspId = aspId;
        return this;
    }

    /**
     * Get aspId
     * 
     * @return aspId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ASP_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAspId()
    {
        return aspId;
    }

    @JsonProperty(JSON_PROPERTY_ASP_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAspId(String aspId)
    {
        this.aspId = aspId;
    }

    public BdtData transPolicy(TransferPolicy transPolicy)
    {

        this.transPolicy = transPolicy;
        return this;
    }

    /**
     * Get transPolicy
     * 
     * @return transPolicy
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TRANS_POLICY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public TransferPolicy getTransPolicy()
    {
        return transPolicy;
    }

    @JsonProperty(JSON_PROPERTY_TRANS_POLICY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTransPolicy(TransferPolicy transPolicy)
    {
        this.transPolicy = transPolicy;
    }

    public BdtData bdtRefId(String bdtRefId)
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

    public BdtData nwAreaInfo(NetworkAreaInfo nwAreaInfo)
    {

        this.nwAreaInfo = nwAreaInfo;
        return this;
    }

    /**
     * Get nwAreaInfo
     * 
     * @return nwAreaInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NW_AREA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NetworkAreaInfo getNwAreaInfo()
    {
        return nwAreaInfo;
    }

    @JsonProperty(JSON_PROPERTY_NW_AREA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNwAreaInfo(NetworkAreaInfo nwAreaInfo)
    {
        this.nwAreaInfo = nwAreaInfo;
    }

    public BdtData numOfUes(Integer numOfUes)
    {

        this.numOfUes = numOfUes;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return numOfUes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_NUM_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getNumOfUes()
    {
        return numOfUes;
    }

    @JsonProperty(JSON_PROPERTY_NUM_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumOfUes(Integer numOfUes)
    {
        this.numOfUes = numOfUes;
    }

    public BdtData volPerUe(UsageThreshold volPerUe)
    {

        this.volPerUe = volPerUe;
        return this;
    }

    /**
     * Get volPerUe
     * 
     * @return volPerUe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_VOL_PER_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UsageThreshold getVolPerUe()
    {
        return volPerUe;
    }

    @JsonProperty(JSON_PROPERTY_VOL_PER_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVolPerUe(UsageThreshold volPerUe)
    {
        this.volPerUe = volPerUe;
    }

    public BdtData dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;
     * it shall contain either a DNN Network Identifier, or a full DNN with both the
     * Network Identifier and Operator Identifier, as specified in 3GPP TS 23.003
     * clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are
     * separated by dots (e.g. \&quot;Label1.Label2.Label3\&quot;).
     * 
     * @return dnn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public BdtData snssai(Snssai snssai)
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

    public BdtData trafficDes(String trafficDes)
    {

        this.trafficDes = trafficDes;
        return this;
    }

    /**
     * Identify a traffic descriptor as defined in Figure 5.2.2 of 3GPP TS 24.526,
     * octets v+5 to w.
     * 
     * @return trafficDes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identify a traffic descriptor as defined in Figure 5.2.2 of 3GPP TS 24.526, octets v+5 to w.")
    @JsonProperty(JSON_PROPERTY_TRAFFIC_DES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTrafficDes()
    {
        return trafficDes;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_DES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrafficDes(String trafficDes)
    {
        this.trafficDes = trafficDes;
    }

    public BdtData bdtpStatus(String bdtpStatus)
    {

        this.bdtpStatus = bdtpStatus;
        return this;
    }

    /**
     * Indicates the validation status of a negotiated BDT policy.
     * 
     * @return bdtpStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the validation status of a negotiated BDT policy.")
    @JsonProperty(JSON_PROPERTY_BDTP_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBdtpStatus()
    {
        return bdtpStatus;
    }

    @JsonProperty(JSON_PROPERTY_BDTP_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtpStatus(String bdtpStatus)
    {
        this.bdtpStatus = bdtpStatus;
    }

    public BdtData suppFeat(String suppFeat)
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

    public BdtData resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public BdtData addResetIdsItem(String resetIdsItem)
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
        BdtData bdtData = (BdtData) o;
        return Objects.equals(this.aspId, bdtData.aspId) && Objects.equals(this.transPolicy, bdtData.transPolicy)
               && Objects.equals(this.bdtRefId, bdtData.bdtRefId) && Objects.equals(this.nwAreaInfo, bdtData.nwAreaInfo)
               && Objects.equals(this.numOfUes, bdtData.numOfUes) && Objects.equals(this.volPerUe, bdtData.volPerUe) && Objects.equals(this.dnn, bdtData.dnn)
               && Objects.equals(this.snssai, bdtData.snssai) && Objects.equals(this.trafficDes, bdtData.trafficDes)
               && Objects.equals(this.bdtpStatus, bdtData.bdtpStatus) && Objects.equals(this.suppFeat, bdtData.suppFeat)
               && Objects.equals(this.resetIds, bdtData.resetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(aspId, transPolicy, bdtRefId, nwAreaInfo, numOfUes, volPerUe, dnn, snssai, trafficDes, bdtpStatus, suppFeat, resetIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class BdtData {\n");
        sb.append("    aspId: ").append(toIndentedString(aspId)).append("\n");
        sb.append("    transPolicy: ").append(toIndentedString(transPolicy)).append("\n");
        sb.append("    bdtRefId: ").append(toIndentedString(bdtRefId)).append("\n");
        sb.append("    nwAreaInfo: ").append(toIndentedString(nwAreaInfo)).append("\n");
        sb.append("    numOfUes: ").append(toIndentedString(numOfUes)).append("\n");
        sb.append("    volPerUe: ").append(toIndentedString(volPerUe)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    trafficDes: ").append(toIndentedString(trafficDes)).append("\n");
        sb.append("    bdtpStatus: ").append(toIndentedString(bdtpStatus)).append("\n");
        sb.append("    suppFeat: ").append(toIndentedString(suppFeat)).append("\n");
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
