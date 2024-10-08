/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

import java.util.Objects;
import java.util.Arrays;

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
 * indicates the DNN and S-NSSAI combination of a PDU session.
 */
@ApiModel(description = "indicates the DNN and S-NSSAI combination of a PDU session.")
@JsonPropertyOrder({ PduSessionInfo.JSON_PROPERTY_SNSSAI, PduSessionInfo.JSON_PROPERTY_DNN })
public class PduSessionInfo
{
    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public PduSessionInfo()
    {
    }

    public PduSessionInfo snssai(Snssai snssai)
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

    public PduSessionInfo dnn(String dnn)
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
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
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
        PduSessionInfo pduSessionInfo = (PduSessionInfo) o;
        return Objects.equals(this.snssai, pduSessionInfo.snssai) && Objects.equals(this.dnn, pduSessionInfo.dnn);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(snssai, dnn);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PduSessionInfo {\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
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
