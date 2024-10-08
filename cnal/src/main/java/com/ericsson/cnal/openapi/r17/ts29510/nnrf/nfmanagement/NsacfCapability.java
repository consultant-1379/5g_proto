/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

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
 * NSACF service capabilities (e.g. to monitor and control the number of
 * registered UEs or established PDU sessions per network slice)
 */
@ApiModel(description = "NSACF service capabilities (e.g. to monitor and control the number of registered UEs or established PDU sessions per network slice) ")
@JsonPropertyOrder({ NsacfCapability.JSON_PROPERTY_SUPPORT_UE_S_A_C, NsacfCapability.JSON_PROPERTY_SUPPORT_PDU_S_A_C })
public class NsacfCapability
{
    public static final String JSON_PROPERTY_SUPPORT_UE_S_A_C = "supportUeSAC";
    private Boolean supportUeSAC = false;

    public static final String JSON_PROPERTY_SUPPORT_PDU_S_A_C = "supportPduSAC";
    private Boolean supportPduSAC = false;

    public NsacfCapability()
    {
    }

    public NsacfCapability supportUeSAC(Boolean supportUeSAC)
    {

        this.supportUeSAC = supportUeSAC;
        return this;
    }

    /**
     * Indicates the service capability of the NSACF to monitor and control the
     * number of registered UEs per network slice for the network slice that is
     * subject to NSAC true: Supported false (default): Not Supported
     * 
     * @return supportUeSAC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the service capability of the NSACF to monitor and control the number of registered UEs per network slice for the network slice that is subject to NSAC   true: Supported   false (default): Not Supported ")
    @JsonProperty(JSON_PROPERTY_SUPPORT_UE_S_A_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportUeSAC()
    {
        return supportUeSAC;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORT_UE_S_A_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportUeSAC(Boolean supportUeSAC)
    {
        this.supportUeSAC = supportUeSAC;
    }

    public NsacfCapability supportPduSAC(Boolean supportPduSAC)
    {

        this.supportPduSAC = supportPduSAC;
        return this;
    }

    /**
     * Indicates the service capability of the NSACF to monitor and control the
     * number of established PDU sessions per network slice for the network slice
     * that is subject to NSAC true: Supported false (default): Not Supported
     * 
     * @return supportPduSAC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the service capability of the NSACF to monitor and control the number of established PDU sessions per network slice for the network slice that is subject to NSAC   true: Supported   false (default): Not Supported ")
    @JsonProperty(JSON_PROPERTY_SUPPORT_PDU_S_A_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportPduSAC()
    {
        return supportPduSAC;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORT_PDU_S_A_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportPduSAC(Boolean supportPduSAC)
    {
        this.supportPduSAC = supportPduSAC;
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
        NsacfCapability nsacfCapability = (NsacfCapability) o;
        return Objects.equals(this.supportUeSAC, nsacfCapability.supportUeSAC) && Objects.equals(this.supportPduSAC, nsacfCapability.supportPduSAC);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supportUeSAC, supportPduSAC);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NsacfCapability {\n");
        sb.append("    supportUeSAC: ").append(toIndentedString(supportUeSAC)).append("\n");
        sb.append("    supportPduSAC: ").append(toIndentedString(supportPduSAC)).append("\n");
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
