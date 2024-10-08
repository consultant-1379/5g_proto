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
 * Contains the network slice status information in terms of the current number
 * of UEs registered with a network slice, the current number of PDU Sessions
 * established on a network slice or both.
 */
@ApiModel(description = "Contains the network slice status information in terms of the current number of UEs registered  with a network slice, the current number of PDU Sessions established on a network slice or both. ")
@JsonPropertyOrder({ SACEventStatus.JSON_PROPERTY_REACHED_NUM_UES, SACEventStatus.JSON_PROPERTY_REACHED_NUM_PDU_SESS })
public class SACEventStatus
{
    public static final String JSON_PROPERTY_REACHED_NUM_UES = "reachedNumUes";
    private SACInfo reachedNumUes;

    public static final String JSON_PROPERTY_REACHED_NUM_PDU_SESS = "reachedNumPduSess";
    private SACInfo reachedNumPduSess;

    public SACEventStatus()
    {
    }

    public SACEventStatus reachedNumUes(SACInfo reachedNumUes)
    {

        this.reachedNumUes = reachedNumUes;
        return this;
    }

    /**
     * Get reachedNumUes
     * 
     * @return reachedNumUes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REACHED_NUM_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SACInfo getReachedNumUes()
    {
        return reachedNumUes;
    }

    @JsonProperty(JSON_PROPERTY_REACHED_NUM_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachedNumUes(SACInfo reachedNumUes)
    {
        this.reachedNumUes = reachedNumUes;
    }

    public SACEventStatus reachedNumPduSess(SACInfo reachedNumPduSess)
    {

        this.reachedNumPduSess = reachedNumPduSess;
        return this;
    }

    /**
     * Get reachedNumPduSess
     * 
     * @return reachedNumPduSess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REACHED_NUM_PDU_SESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SACInfo getReachedNumPduSess()
    {
        return reachedNumPduSess;
    }

    @JsonProperty(JSON_PROPERTY_REACHED_NUM_PDU_SESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachedNumPduSess(SACInfo reachedNumPduSess)
    {
        this.reachedNumPduSess = reachedNumPduSess;
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
        SACEventStatus saCEventStatus = (SACEventStatus) o;
        return Objects.equals(this.reachedNumUes, saCEventStatus.reachedNumUes) && Objects.equals(this.reachedNumPduSess, saCEventStatus.reachedNumPduSess);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(reachedNumUes, reachedNumPduSess);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SACEventStatus {\n");
        sb.append("    reachedNumUes: ").append(toIndentedString(reachedNumUes)).append("\n");
        sb.append("    reachedNumPduSess: ").append(toIndentedString(reachedNumPduSess)).append("\n");
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
