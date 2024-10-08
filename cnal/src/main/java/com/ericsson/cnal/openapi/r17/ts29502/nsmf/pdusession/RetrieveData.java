/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

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
 * Data within Retrieve Request
 */
@ApiModel(description = "Data within Retrieve Request")
@JsonPropertyOrder({ RetrieveData.JSON_PROPERTY_SMALL_DATA_RATE_STATUS_REQ, RetrieveData.JSON_PROPERTY_PDU_SESSION_CONTEXT_TYPE })
public class RetrieveData
{
    public static final String JSON_PROPERTY_SMALL_DATA_RATE_STATUS_REQ = "smallDataRateStatusReq";
    private Boolean smallDataRateStatusReq = false;

    public static final String JSON_PROPERTY_PDU_SESSION_CONTEXT_TYPE = "pduSessionContextType";
    private String pduSessionContextType;

    public RetrieveData()
    {
    }

    public RetrieveData smallDataRateStatusReq(Boolean smallDataRateStatusReq)
    {

        this.smallDataRateStatusReq = smallDataRateStatusReq;
        return this;
    }

    /**
     * Get smallDataRateStatusReq
     * 
     * @return smallDataRateStatusReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMALL_DATA_RATE_STATUS_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSmallDataRateStatusReq()
    {
        return smallDataRateStatusReq;
    }

    @JsonProperty(JSON_PROPERTY_SMALL_DATA_RATE_STATUS_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmallDataRateStatusReq(Boolean smallDataRateStatusReq)
    {
        this.smallDataRateStatusReq = smallDataRateStatusReq;
    }

    public RetrieveData pduSessionContextType(String pduSessionContextType)
    {

        this.pduSessionContextType = pduSessionContextType;
        return this;
    }

    /**
     * Type of PDU Session information. Possible values are - AF_COORDINATION_INFO
     * 
     * @return pduSessionContextType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Type of PDU Session information. Possible values are   - AF_COORDINATION_INFO ")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_CONTEXT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPduSessionContextType()
    {
        return pduSessionContextType;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_CONTEXT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPduSessionContextType(String pduSessionContextType)
    {
        this.pduSessionContextType = pduSessionContextType;
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
        RetrieveData retrieveData = (RetrieveData) o;
        return Objects.equals(this.smallDataRateStatusReq, retrieveData.smallDataRateStatusReq)
               && Objects.equals(this.pduSessionContextType, retrieveData.pduSessionContextType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(smallDataRateStatusReq, pduSessionContextType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RetrieveData {\n");
        sb.append("    smallDataRateStatusReq: ").append(toIndentedString(smallDataRateStatusReq)).append("\n");
        sb.append("    pduSessionContextType: ").append(toIndentedString(pduSessionContextType)).append("\n");
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
