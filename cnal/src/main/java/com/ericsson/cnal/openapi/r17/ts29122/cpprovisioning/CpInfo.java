/*
 * 3gpp-cp-parameter-provisioning
 * API for provisioning communication pattern parameters.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.cpprovisioning;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.IpAddr;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents the resources for communication pattern parameter provisioning.
 */
@ApiModel(description = "Represents the resources for communication pattern parameter provisioning.")
@JsonPropertyOrder({ CpInfo.JSON_PROPERTY_SELF,
                     CpInfo.JSON_PROPERTY_SUPPORTED_FEATURES,
                     CpInfo.JSON_PROPERTY_MTC_PROVIDER_ID,
                     CpInfo.JSON_PROPERTY_DNN,
                     CpInfo.JSON_PROPERTY_EXTERNAL_ID,
                     CpInfo.JSON_PROPERTY_MSISDN,
                     CpInfo.JSON_PROPERTY_EXTERNAL_GROUP_ID,
                     CpInfo.JSON_PROPERTY_CP_PARAMETER_SETS,
                     CpInfo.JSON_PROPERTY_CP_REPORTS,
                     CpInfo.JSON_PROPERTY_SNSSAI,
                     CpInfo.JSON_PROPERTY_UE_IP_ADDR,
                     CpInfo.JSON_PROPERTY_UE_MAC_ADDR })
public class CpInfo
{
    public static final String JSON_PROPERTY_SELF = "self";
    private String self;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_MTC_PROVIDER_ID = "mtcProviderId";
    private String mtcProviderId;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_EXTERNAL_ID = "externalId";
    private String externalId;

    public static final String JSON_PROPERTY_MSISDN = "msisdn";
    private String msisdn;

    public static final String JSON_PROPERTY_EXTERNAL_GROUP_ID = "externalGroupId";
    private String externalGroupId;

    public static final String JSON_PROPERTY_CP_PARAMETER_SETS = "cpParameterSets";
    private Map<String, CpParameterSet> cpParameterSets = new HashMap<>();

    public static final String JSON_PROPERTY_CP_REPORTS = "cpReports";
    private Map<String, CpReport> cpReports = null;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_UE_IP_ADDR = "ueIpAddr";
    private IpAddr ueIpAddr;

    public static final String JSON_PROPERTY_UE_MAC_ADDR = "ueMacAddr";
    private String ueMacAddr;

    public CpInfo()
    {
    }

    @JsonCreator
    public CpInfo(@JsonProperty(JSON_PROPERTY_CP_REPORTS) Map<String, CpReport> cpReports)
    {
        this();
        this.cpReports = cpReports;
    }

    public CpInfo self(String self)
    {

        this.self = self;
        return this;
    }

    /**
     * string formatted according to IETF RFC 3986 identifying a referenced
     * resource.
     * 
     * @return self
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string formatted according to IETF RFC 3986 identifying a referenced resource.")
    @JsonProperty(JSON_PROPERTY_SELF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSelf()
    {
        return self;
    }

    @JsonProperty(JSON_PROPERTY_SELF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSelf(String self)
    {
        this.self = self;
    }

    public CpInfo supportedFeatures(String supportedFeatures)
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

    public CpInfo mtcProviderId(String mtcProviderId)
    {

        this.mtcProviderId = mtcProviderId;
        return this;
    }

    /**
     * Identifies the MTC Service Provider and/or MTC Application.
     * 
     * @return mtcProviderId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the MTC Service Provider and/or MTC Application.")
    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMtcProviderId()
    {
        return mtcProviderId;
    }

    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMtcProviderId(String mtcProviderId)
    {
        this.mtcProviderId = mtcProviderId;
    }

    public CpInfo dnn(String dnn)
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

    public CpInfo externalId(String externalId)
    {

        this.externalId = externalId;
        return this;
    }

    /**
     * string containing a local identifier followed by \&quot;@\&quot; and a domain
     * identifier. Both the local identifier and the domain identifier shall be
     * encoded as strings that do not contain any \&quot;@\&quot; characters. See
     * Clause 4.6.2 of 3GPP TS 23.682 for more information.
     * 
     * @return externalId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string containing a local identifier followed by \"@\" and a domain identifier. Both the local identifier and the domain identifier shall be encoded as strings that do not contain any \"@\" characters. See Clause 4.6.2 of 3GPP TS 23.682 for more information.")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExternalId()
    {
        return externalId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public CpInfo msisdn(String msisdn)
    {

        this.msisdn = msisdn;
        return this;
    }

    /**
     * string formatted according to clause 3.3 of 3GPP TS 23.003 that describes an
     * MSISDN.
     * 
     * @return msisdn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string formatted according to clause 3.3 of 3GPP TS 23.003 that describes an MSISDN.")
    @JsonProperty(JSON_PROPERTY_MSISDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMsisdn()
    {
        return msisdn;
    }

    @JsonProperty(JSON_PROPERTY_MSISDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMsisdn(String msisdn)
    {
        this.msisdn = msisdn;
    }

    public CpInfo externalGroupId(String externalGroupId)
    {

        this.externalGroupId = externalGroupId;
        return this;
    }

    /**
     * string containing a local identifier followed by \&quot;@\&quot; and a domain
     * identifier. Both the local identifier and the domain identifier shall be
     * encoded as strings that do not contain any \&quot;@\&quot; characters. See
     * Clauses 4.6.2 and 4.6.3 of 3GPP TS 23.682 for more information.
     * 
     * @return externalGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string containing a local identifier followed by \"@\" and a domain identifier. Both the local identifier and the domain identifier shall be encoded as strings that do not contain any \"@\" characters. See Clauses 4.6.2 and 4.6.3 of 3GPP TS 23.682 for more information.")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExternalGroupId()
    {
        return externalGroupId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalGroupId(String externalGroupId)
    {
        this.externalGroupId = externalGroupId;
    }

    public CpInfo cpParameterSets(Map<String, CpParameterSet> cpParameterSets)
    {

        this.cpParameterSets = cpParameterSets;
        return this;
    }

    public CpInfo putCpParameterSetsItem(String key,
                                         CpParameterSet cpParameterSetsItem)
    {
        this.cpParameterSets.put(key, cpParameterSetsItem);
        return this;
    }

    /**
     * Identifies a set of CP parameter information that may be part of this CpInfo
     * structure. Any string value can be used as a key of the map.
     * 
     * @return cpParameterSets
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Identifies a set of CP parameter information that may be part of this CpInfo structure. Any string value can be used as a key of the map.")
    @JsonProperty(JSON_PROPERTY_CP_PARAMETER_SETS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, CpParameterSet> getCpParameterSets()
    {
        return cpParameterSets;
    }

    @JsonProperty(JSON_PROPERTY_CP_PARAMETER_SETS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCpParameterSets(Map<String, CpParameterSet> cpParameterSets)
    {
        this.cpParameterSets = cpParameterSets;
    }

    /**
     * Supplied by the SCEF and contains the CP set identifiers for which CP
     * parameter(s) are not added or modified successfully. The failure reason is
     * also included. Each element provides the related information for one or more
     * CP set identifier(s) and is identified in the map via the failure identifier
     * as key.
     * 
     * @return cpReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Supplied by the SCEF and contains the CP set identifiers for which CP parameter(s) are not added or modified successfully. The failure reason is also included. Each element provides the related information for one or more CP set identifier(s) and is identified in the map via the failure identifier as key.")
    @JsonProperty(JSON_PROPERTY_CP_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, CpReport> getCpReports()
    {
        return cpReports;
    }

    public CpInfo snssai(Snssai snssai)
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

    public CpInfo ueIpAddr(IpAddr ueIpAddr)
    {

        this.ueIpAddr = ueIpAddr;
        return this;
    }

    /**
     * Get ueIpAddr
     * 
     * @return ueIpAddr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_IP_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IpAddr getUeIpAddr()
    {
        return ueIpAddr;
    }

    @JsonProperty(JSON_PROPERTY_UE_IP_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpAddr(IpAddr ueIpAddr)
    {
        this.ueIpAddr = ueIpAddr;
    }

    public CpInfo ueMacAddr(String ueMacAddr)
    {

        this.ueMacAddr = ueMacAddr;
        return this;
    }

    /**
     * String identifying a MAC address formatted in the hexadecimal notation
     * according to clause 1.1 and clause 2.1 of RFC 7042.
     * 
     * @return ueMacAddr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a MAC address formatted in the hexadecimal notation according to clause 1.1 and clause 2.1 of RFC 7042. ")
    @JsonProperty(JSON_PROPERTY_UE_MAC_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeMacAddr()
    {
        return ueMacAddr;
    }

    @JsonProperty(JSON_PROPERTY_UE_MAC_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeMacAddr(String ueMacAddr)
    {
        this.ueMacAddr = ueMacAddr;
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
        CpInfo cpInfo = (CpInfo) o;
        return Objects.equals(this.self, cpInfo.self) && Objects.equals(this.supportedFeatures, cpInfo.supportedFeatures)
               && Objects.equals(this.mtcProviderId, cpInfo.mtcProviderId) && Objects.equals(this.dnn, cpInfo.dnn)
               && Objects.equals(this.externalId, cpInfo.externalId) && Objects.equals(this.msisdn, cpInfo.msisdn)
               && Objects.equals(this.externalGroupId, cpInfo.externalGroupId) && Objects.equals(this.cpParameterSets, cpInfo.cpParameterSets)
               && Objects.equals(this.cpReports, cpInfo.cpReports) && Objects.equals(this.snssai, cpInfo.snssai)
               && Objects.equals(this.ueIpAddr, cpInfo.ueIpAddr) && Objects.equals(this.ueMacAddr, cpInfo.ueMacAddr);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(self,
                            supportedFeatures,
                            mtcProviderId,
                            dnn,
                            externalId,
                            msisdn,
                            externalGroupId,
                            cpParameterSets,
                            cpReports,
                            snssai,
                            ueIpAddr,
                            ueMacAddr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CpInfo {\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    mtcProviderId: ").append(toIndentedString(mtcProviderId)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    externalId: ").append(toIndentedString(externalId)).append("\n");
        sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
        sb.append("    externalGroupId: ").append(toIndentedString(externalGroupId)).append("\n");
        sb.append("    cpParameterSets: ").append(toIndentedString(cpParameterSets)).append("\n");
        sb.append("    cpReports: ").append(toIndentedString(cpReports)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    ueIpAddr: ").append(toIndentedString(ueIpAddr)).append("\n");
        sb.append("    ueMacAddr: ").append(toIndentedString(ueMacAddr)).append("\n");
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
