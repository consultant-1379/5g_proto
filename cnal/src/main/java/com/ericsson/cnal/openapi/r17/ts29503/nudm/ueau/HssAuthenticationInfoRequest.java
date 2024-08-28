/*
 * Nudm_UEAU
 * UDM UE Authentication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau;

import java.util.Objects;
import java.util.Arrays;
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
 * HssAuthenticationInfoRequest
 */
@JsonPropertyOrder({ HssAuthenticationInfoRequest.JSON_PROPERTY_SUPPORTED_FEATURES,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_HSS_AUTH_TYPE,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_NUM_OF_REQUESTED_VECTORS,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_REQUESTING_NODE_TYPE,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_SERVING_NETWORK_ID,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_RESYNCHRONIZATION_INFO,
                     HssAuthenticationInfoRequest.JSON_PROPERTY_AN_ID })
public class HssAuthenticationInfoRequest
{
    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_HSS_AUTH_TYPE = "hssAuthType";
    private String hssAuthType;

    public static final String JSON_PROPERTY_NUM_OF_REQUESTED_VECTORS = "numOfRequestedVectors";
    private Integer numOfRequestedVectors;

    public static final String JSON_PROPERTY_REQUESTING_NODE_TYPE = "requestingNodeType";
    private String requestingNodeType;

    public static final String JSON_PROPERTY_SERVING_NETWORK_ID = "servingNetworkId";
    private PlmnId servingNetworkId;

    public static final String JSON_PROPERTY_RESYNCHRONIZATION_INFO = "resynchronizationInfo";
    private ResynchronizationInfo resynchronizationInfo;

    public static final String JSON_PROPERTY_AN_ID = "anId";
    private String anId;

    public HssAuthenticationInfoRequest()
    {
    }

    public HssAuthenticationInfoRequest supportedFeatures(String supportedFeatures)
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

    public HssAuthenticationInfoRequest hssAuthType(String hssAuthType)
    {

        this.hssAuthType = hssAuthType;
        return this;
    }

    /**
     * Get hssAuthType
     * 
     * @return hssAuthType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_HSS_AUTH_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getHssAuthType()
    {
        return hssAuthType;
    }

    @JsonProperty(JSON_PROPERTY_HSS_AUTH_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setHssAuthType(String hssAuthType)
    {
        this.hssAuthType = hssAuthType;
    }

    public HssAuthenticationInfoRequest numOfRequestedVectors(Integer numOfRequestedVectors)
    {

        this.numOfRequestedVectors = numOfRequestedVectors;
        return this;
    }

    /**
     * Get numOfRequestedVectors minimum: 1 maximum: 5
     * 
     * @return numOfRequestedVectors
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NUM_OF_REQUESTED_VECTORS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getNumOfRequestedVectors()
    {
        return numOfRequestedVectors;
    }

    @JsonProperty(JSON_PROPERTY_NUM_OF_REQUESTED_VECTORS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNumOfRequestedVectors(Integer numOfRequestedVectors)
    {
        this.numOfRequestedVectors = numOfRequestedVectors;
    }

    public HssAuthenticationInfoRequest requestingNodeType(String requestingNodeType)
    {

        this.requestingNodeType = requestingNodeType;
        return this;
    }

    /**
     * Get requestingNodeType
     * 
     * @return requestingNodeType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQUESTING_NODE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRequestingNodeType()
    {
        return requestingNodeType;
    }

    @JsonProperty(JSON_PROPERTY_REQUESTING_NODE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestingNodeType(String requestingNodeType)
    {
        this.requestingNodeType = requestingNodeType;
    }

    public HssAuthenticationInfoRequest servingNetworkId(PlmnId servingNetworkId)
    {

        this.servingNetworkId = servingNetworkId;
        return this;
    }

    /**
     * Get servingNetworkId
     * 
     * @return servingNetworkId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getServingNetworkId()
    {
        return servingNetworkId;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServingNetworkId(PlmnId servingNetworkId)
    {
        this.servingNetworkId = servingNetworkId;
    }

    public HssAuthenticationInfoRequest resynchronizationInfo(ResynchronizationInfo resynchronizationInfo)
    {

        this.resynchronizationInfo = resynchronizationInfo;
        return this;
    }

    /**
     * Get resynchronizationInfo
     * 
     * @return resynchronizationInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESYNCHRONIZATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ResynchronizationInfo getResynchronizationInfo()
    {
        return resynchronizationInfo;
    }

    @JsonProperty(JSON_PROPERTY_RESYNCHRONIZATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResynchronizationInfo(ResynchronizationInfo resynchronizationInfo)
    {
        this.resynchronizationInfo = resynchronizationInfo;
    }

    public HssAuthenticationInfoRequest anId(String anId)
    {

        this.anId = anId;
        return this;
    }

    /**
     * Get anId
     * 
     * @return anId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAnId()
    {
        return anId;
    }

    @JsonProperty(JSON_PROPERTY_AN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnId(String anId)
    {
        this.anId = anId;
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
        HssAuthenticationInfoRequest hssAuthenticationInfoRequest = (HssAuthenticationInfoRequest) o;
        return Objects.equals(this.supportedFeatures, hssAuthenticationInfoRequest.supportedFeatures)
               && Objects.equals(this.hssAuthType, hssAuthenticationInfoRequest.hssAuthType)
               && Objects.equals(this.numOfRequestedVectors, hssAuthenticationInfoRequest.numOfRequestedVectors)
               && Objects.equals(this.requestingNodeType, hssAuthenticationInfoRequest.requestingNodeType)
               && Objects.equals(this.servingNetworkId, hssAuthenticationInfoRequest.servingNetworkId)
               && Objects.equals(this.resynchronizationInfo, hssAuthenticationInfoRequest.resynchronizationInfo)
               && Objects.equals(this.anId, hssAuthenticationInfoRequest.anId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supportedFeatures, hssAuthType, numOfRequestedVectors, requestingNodeType, servingNetworkId, resynchronizationInfo, anId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class HssAuthenticationInfoRequest {\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    hssAuthType: ").append(toIndentedString(hssAuthType)).append("\n");
        sb.append("    numOfRequestedVectors: ").append(toIndentedString(numOfRequestedVectors)).append("\n");
        sb.append("    requestingNodeType: ").append(toIndentedString(requestingNodeType)).append("\n");
        sb.append("    servingNetworkId: ").append(toIndentedString(servingNetworkId)).append("\n");
        sb.append("    resynchronizationInfo: ").append(toIndentedString(resynchronizationInfo)).append("\n");
        sb.append("    anId: ").append(toIndentedString(anId)).append("\n");
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
