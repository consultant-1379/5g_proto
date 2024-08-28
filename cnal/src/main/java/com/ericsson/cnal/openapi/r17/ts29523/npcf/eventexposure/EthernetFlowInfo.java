/*
 * Npcf_EventExposure
 * PCF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29523.npcf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29514.npcf.policyauthorization.EthFlowDescription;
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
 * Identifies an UL/DL ethernet flow.
 */
@ApiModel(description = "Identifies an UL/DL ethernet flow.")
@JsonPropertyOrder({ EthernetFlowInfo.JSON_PROPERTY_ETH_FLOWS, EthernetFlowInfo.JSON_PROPERTY_FLOW_NUMBER })
public class EthernetFlowInfo
{
    public static final String JSON_PROPERTY_ETH_FLOWS = "ethFlows";
    private List<EthFlowDescription> ethFlows = null;

    public static final String JSON_PROPERTY_FLOW_NUMBER = "flowNumber";
    private Integer flowNumber;

    public EthernetFlowInfo()
    {
    }

    public EthernetFlowInfo ethFlows(List<EthFlowDescription> ethFlows)
    {

        this.ethFlows = ethFlows;
        return this;
    }

    public EthernetFlowInfo addEthFlowsItem(EthFlowDescription ethFlowsItem)
    {
        if (this.ethFlows == null)
        {
            this.ethFlows = new ArrayList<>();
        }
        this.ethFlows.add(ethFlowsItem);
        return this;
    }

    /**
     * Get ethFlows
     * 
     * @return ethFlows
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ETH_FLOWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<EthFlowDescription> getEthFlows()
    {
        return ethFlows;
    }

    @JsonProperty(JSON_PROPERTY_ETH_FLOWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEthFlows(List<EthFlowDescription> ethFlows)
    {
        this.ethFlows = ethFlows;
    }

    public EthernetFlowInfo flowNumber(Integer flowNumber)
    {

        this.flowNumber = flowNumber;
        return this;
    }

    /**
     * Get flowNumber
     * 
     * @return flowNumber
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_FLOW_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getFlowNumber()
    {
        return flowNumber;
    }

    @JsonProperty(JSON_PROPERTY_FLOW_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFlowNumber(Integer flowNumber)
    {
        this.flowNumber = flowNumber;
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
        EthernetFlowInfo ethernetFlowInfo = (EthernetFlowInfo) o;
        return Objects.equals(this.ethFlows, ethernetFlowInfo.ethFlows) && Objects.equals(this.flowNumber, ethernetFlowInfo.flowNumber);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ethFlows, flowNumber);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EthernetFlowInfo {\n");
        sb.append("    ethFlows: ").append(toIndentedString(ethFlows)).append("\n");
        sb.append("    flowNumber: ").append(toIndentedString(flowNumber)).append("\n");
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
