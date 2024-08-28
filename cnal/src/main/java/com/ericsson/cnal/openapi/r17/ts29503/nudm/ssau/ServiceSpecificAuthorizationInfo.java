/*
 * Nudm_SSAU
 * Nudm Service Specific Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ssau;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
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
 * Authorization information for a specific service
 */
@ApiModel(description = "Authorization information for a specific service")
@JsonPropertyOrder({ ServiceSpecificAuthorizationInfo.JSON_PROPERTY_SNSSAI,
                     ServiceSpecificAuthorizationInfo.JSON_PROPERTY_DNN,
                     ServiceSpecificAuthorizationInfo.JSON_PROPERTY_MTC_PROVIDER_INFORMATION,
                     ServiceSpecificAuthorizationInfo.JSON_PROPERTY_AUTH_UPDATE_CALLBACK_URI,
                     ServiceSpecificAuthorizationInfo.JSON_PROPERTY_AF_ID,
                     ServiceSpecificAuthorizationInfo.JSON_PROPERTY_NEF_ID })
public class ServiceSpecificAuthorizationInfo
{
    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_MTC_PROVIDER_INFORMATION = "mtcProviderInformation";
    private String mtcProviderInformation;

    public static final String JSON_PROPERTY_AUTH_UPDATE_CALLBACK_URI = "authUpdateCallbackUri";
    private String authUpdateCallbackUri;

    public static final String JSON_PROPERTY_AF_ID = "afId";
    private String afId;

    public static final String JSON_PROPERTY_NEF_ID = "nefId";
    private String nefId;

    public ServiceSpecificAuthorizationInfo()
    {
    }

    public ServiceSpecificAuthorizationInfo snssai(Snssai snssai)
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

    public ServiceSpecificAuthorizationInfo dnn(String dnn)
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

    public ServiceSpecificAuthorizationInfo mtcProviderInformation(String mtcProviderInformation)
    {

        this.mtcProviderInformation = mtcProviderInformation;
        return this;
    }

    /**
     * String uniquely identifying MTC provider information.
     * 
     * @return mtcProviderInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying MTC provider information.")
    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMtcProviderInformation()
    {
        return mtcProviderInformation;
    }

    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMtcProviderInformation(String mtcProviderInformation)
    {
        this.mtcProviderInformation = mtcProviderInformation;
    }

    public ServiceSpecificAuthorizationInfo authUpdateCallbackUri(String authUpdateCallbackUri)
    {

        this.authUpdateCallbackUri = authUpdateCallbackUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return authUpdateCallbackUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_AUTH_UPDATE_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAuthUpdateCallbackUri()
    {
        return authUpdateCallbackUri;
    }

    @JsonProperty(JSON_PROPERTY_AUTH_UPDATE_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAuthUpdateCallbackUri(String authUpdateCallbackUri)
    {
        this.authUpdateCallbackUri = authUpdateCallbackUri;
    }

    public ServiceSpecificAuthorizationInfo afId(String afId)
    {

        this.afId = afId;
        return this;
    }

    /**
     * Get afId
     * 
     * @return afId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAfId()
    {
        return afId;
    }

    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfId(String afId)
    {
        this.afId = afId;
    }

    public ServiceSpecificAuthorizationInfo nefId(String nefId)
    {

        this.nefId = nefId;
        return this;
    }

    /**
     * Identity of the NEF
     * 
     * @return nefId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identity of the NEF")
    @JsonProperty(JSON_PROPERTY_NEF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNefId()
    {
        return nefId;
    }

    @JsonProperty(JSON_PROPERTY_NEF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNefId(String nefId)
    {
        this.nefId = nefId;
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
        ServiceSpecificAuthorizationInfo serviceSpecificAuthorizationInfo = (ServiceSpecificAuthorizationInfo) o;
        return Objects.equals(this.snssai, serviceSpecificAuthorizationInfo.snssai) && Objects.equals(this.dnn, serviceSpecificAuthorizationInfo.dnn)
               && Objects.equals(this.mtcProviderInformation, serviceSpecificAuthorizationInfo.mtcProviderInformation)
               && Objects.equals(this.authUpdateCallbackUri, serviceSpecificAuthorizationInfo.authUpdateCallbackUri)
               && Objects.equals(this.afId, serviceSpecificAuthorizationInfo.afId) && Objects.equals(this.nefId, serviceSpecificAuthorizationInfo.nefId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(snssai, dnn, mtcProviderInformation, authUpdateCallbackUri, afId, nefId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceSpecificAuthorizationInfo {\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    mtcProviderInformation: ").append(toIndentedString(mtcProviderInformation)).append("\n");
        sb.append("    authUpdateCallbackUri: ").append(toIndentedString(authUpdateCallbackUri)).append("\n");
        sb.append("    afId: ").append(toIndentedString(afId)).append("\n");
        sb.append("    nefId: ").append(toIndentedString(nefId)).append("\n");
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
