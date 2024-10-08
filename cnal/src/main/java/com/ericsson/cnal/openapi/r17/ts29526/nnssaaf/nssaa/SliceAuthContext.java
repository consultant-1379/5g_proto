/*
 * Nnssaaf_NSSAA
 * Network Slice-Specific Authentication and Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29526.nnssaaf.nssaa;

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
 * SliceAuthContext
 */
@JsonPropertyOrder({ SliceAuthContext.JSON_PROPERTY_GPSI,
                     SliceAuthContext.JSON_PROPERTY_SNSSAI,
                     SliceAuthContext.JSON_PROPERTY_AUTH_CTX_ID,
                     SliceAuthContext.JSON_PROPERTY_EAP_MESSAGE })
public class SliceAuthContext
{
    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_AUTH_CTX_ID = "authCtxId";
    private String authCtxId;

    public static final String JSON_PROPERTY_EAP_MESSAGE = "eapMessage";
    private byte[] eapMessage;

    public SliceAuthContext()
    {
    }

    public SliceAuthContext gpsi(String gpsi)
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
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String identifying a Gpsi shall contain either an External Id or an MSISDN.  It shall be formatted as follows -External Identifier= \"extid-'extid', where 'extid'  shall be formatted according to clause 19.7.2 of 3GPP TS 23.003 that describes an  External Identifier.  ")
    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getGpsi()
    {
        return gpsi;
    }

    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setGpsi(String gpsi)
    {
        this.gpsi = gpsi;
    }

    public SliceAuthContext snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
    }

    public SliceAuthContext authCtxId(String authCtxId)
    {

        this.authCtxId = authCtxId;
        return this;
    }

    /**
     * contains the resource ID of slice authentication context
     * 
     * @return authCtxId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "contains the resource ID of slice authentication context")
    @JsonProperty(JSON_PROPERTY_AUTH_CTX_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAuthCtxId()
    {
        return authCtxId;
    }

    @JsonProperty(JSON_PROPERTY_AUTH_CTX_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAuthCtxId(String authCtxId)
    {
        this.authCtxId = authCtxId;
    }

    public SliceAuthContext eapMessage(byte[] eapMessage)
    {

        this.eapMessage = eapMessage;
        return this;
    }

    /**
     * contains an EAP packet
     * 
     * @return eapMessage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(required = true, value = "contains an EAP packet")
    @JsonProperty(JSON_PROPERTY_EAP_MESSAGE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public byte[] getEapMessage()
    {
        return eapMessage;
    }

    @JsonProperty(JSON_PROPERTY_EAP_MESSAGE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEapMessage(byte[] eapMessage)
    {
        this.eapMessage = eapMessage;
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
        SliceAuthContext sliceAuthContext = (SliceAuthContext) o;
        return Objects.equals(this.gpsi, sliceAuthContext.gpsi) && Objects.equals(this.snssai, sliceAuthContext.snssai)
               && Objects.equals(this.authCtxId, sliceAuthContext.authCtxId) && Arrays.equals(this.eapMessage, sliceAuthContext.eapMessage);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(gpsi, snssai, authCtxId, Arrays.hashCode(eapMessage));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SliceAuthContext {\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    authCtxId: ").append(toIndentedString(authCtxId)).append("\n");
        sb.append("    eapMessage: ").append(toIndentedString(eapMessage)).append("\n");
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
