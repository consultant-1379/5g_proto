/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within a UE Context Transfer Request to start transferring of an
 * individual ueContext resource from old AMF to new AMF
 */
@ApiModel(description = "Data within a UE Context Transfer Request to start transferring of an individual ueContext resource from old AMF to new AMF")
@JsonPropertyOrder({ UeContextTransferReqData.JSON_PROPERTY_REASON,
                     UeContextTransferReqData.JSON_PROPERTY_ACCESS_TYPE,
                     UeContextTransferReqData.JSON_PROPERTY_PLMN_ID,
                     UeContextTransferReqData.JSON_PROPERTY_REG_REQUEST,
                     UeContextTransferReqData.JSON_PROPERTY_SUPPORTED_FEATURES })
public class UeContextTransferReqData
{
    public static final String JSON_PROPERTY_REASON = "reason";
    private String reason;

    public static final String JSON_PROPERTY_ACCESS_TYPE = "accessType";
    private AccessType accessType;

    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public static final String JSON_PROPERTY_REG_REQUEST = "regRequest";
    private N1MessageContainer regRequest;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public UeContextTransferReqData()
    {
    }

    public UeContextTransferReqData reason(String reason)
    {

        this.reason = reason;
        return this;
    }

    /**
     * Indicates UE Context Transfer Reason
     * 
     * @return reason
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates UE Context Transfer Reason")
    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getReason()
    {
        return reason;
    }

    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public UeContextTransferReqData accessType(AccessType accessType)
    {

        this.accessType = accessType;
        return this;
    }

    /**
     * Get accessType
     * 
     * @return accessType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public AccessType getAccessType()
    {
        return accessType;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAccessType(AccessType accessType)
    {
        this.accessType = accessType;
    }

    public UeContextTransferReqData plmnId(PlmnId plmnId)
    {

        this.plmnId = plmnId;
        return this;
    }

    /**
     * Get plmnId
     * 
     * @return plmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnId(PlmnId plmnId)
    {
        this.plmnId = plmnId;
    }

    public UeContextTransferReqData regRequest(N1MessageContainer regRequest)
    {

        this.regRequest = regRequest;
        return this;
    }

    /**
     * Get regRequest
     * 
     * @return regRequest
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REG_REQUEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N1MessageContainer getRegRequest()
    {
        return regRequest;
    }

    @JsonProperty(JSON_PROPERTY_REG_REQUEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRegRequest(N1MessageContainer regRequest)
    {
        this.regRequest = regRequest;
    }

    public UeContextTransferReqData supportedFeatures(String supportedFeatures)
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
        UeContextTransferReqData ueContextTransferReqData = (UeContextTransferReqData) o;
        return Objects.equals(this.reason, ueContextTransferReqData.reason) && Objects.equals(this.accessType, ueContextTransferReqData.accessType)
               && Objects.equals(this.plmnId, ueContextTransferReqData.plmnId) && Objects.equals(this.regRequest, ueContextTransferReqData.regRequest)
               && Objects.equals(this.supportedFeatures, ueContextTransferReqData.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(reason, accessType, plmnId, regRequest, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeContextTransferReqData {\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    accessType: ").append(toIndentedString(accessType)).append("\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    regRequest: ").append(toIndentedString(regRequest)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
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
