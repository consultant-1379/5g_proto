/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

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
 * NNIInformation
 */
@JsonPropertyOrder({ NNIInformation.JSON_PROPERTY_SESSION_DIRECTION,
                     NNIInformation.JSON_PROPERTY_N_N_I_TYPE,
                     NNIInformation.JSON_PROPERTY_RELATIONSHIP_MODE,
                     NNIInformation.JSON_PROPERTY_NEIGHBOUR_NODE_ADDRESS })
public class NNIInformation
{
    public static final String JSON_PROPERTY_SESSION_DIRECTION = "sessionDirection";
    private String sessionDirection;

    public static final String JSON_PROPERTY_N_N_I_TYPE = "nNIType";
    private String nNIType;

    public static final String JSON_PROPERTY_RELATIONSHIP_MODE = "relationshipMode";
    private String relationshipMode;

    public static final String JSON_PROPERTY_NEIGHBOUR_NODE_ADDRESS = "neighbourNodeAddress";
    private IMSAddress neighbourNodeAddress;

    public NNIInformation()
    {
    }

    public NNIInformation sessionDirection(String sessionDirection)
    {

        this.sessionDirection = sessionDirection;
        return this;
    }

    /**
     * Get sessionDirection
     * 
     * @return sessionDirection
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SESSION_DIRECTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSessionDirection()
    {
        return sessionDirection;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_DIRECTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionDirection(String sessionDirection)
    {
        this.sessionDirection = sessionDirection;
    }

    public NNIInformation nNIType(String nNIType)
    {

        this.nNIType = nNIType;
        return this;
    }

    /**
     * Get nNIType
     * 
     * @return nNIType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N_N_I_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getnNIType()
    {
        return nNIType;
    }

    @JsonProperty(JSON_PROPERTY_N_N_I_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setnNIType(String nNIType)
    {
        this.nNIType = nNIType;
    }

    public NNIInformation relationshipMode(String relationshipMode)
    {

        this.relationshipMode = relationshipMode;
        return this;
    }

    /**
     * Get relationshipMode
     * 
     * @return relationshipMode
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RELATIONSHIP_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRelationshipMode()
    {
        return relationshipMode;
    }

    @JsonProperty(JSON_PROPERTY_RELATIONSHIP_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRelationshipMode(String relationshipMode)
    {
        this.relationshipMode = relationshipMode;
    }

    public NNIInformation neighbourNodeAddress(IMSAddress neighbourNodeAddress)
    {

        this.neighbourNodeAddress = neighbourNodeAddress;
        return this;
    }

    /**
     * Get neighbourNodeAddress
     * 
     * @return neighbourNodeAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NEIGHBOUR_NODE_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IMSAddress getNeighbourNodeAddress()
    {
        return neighbourNodeAddress;
    }

    @JsonProperty(JSON_PROPERTY_NEIGHBOUR_NODE_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNeighbourNodeAddress(IMSAddress neighbourNodeAddress)
    {
        this.neighbourNodeAddress = neighbourNodeAddress;
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
        NNIInformation nnIInformation = (NNIInformation) o;
        return Objects.equals(this.sessionDirection, nnIInformation.sessionDirection) && Objects.equals(this.nNIType, nnIInformation.nNIType)
               && Objects.equals(this.relationshipMode, nnIInformation.relationshipMode)
               && Objects.equals(this.neighbourNodeAddress, nnIInformation.neighbourNodeAddress);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sessionDirection, nNIType, relationshipMode, neighbourNodeAddress);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NNIInformation {\n");
        sb.append("    sessionDirection: ").append(toIndentedString(sessionDirection)).append("\n");
        sb.append("    nNIType: ").append(toIndentedString(nNIType)).append("\n");
        sb.append("    relationshipMode: ").append(toIndentedString(relationshipMode)).append("\n");
        sb.append("    neighbourNodeAddress: ").append(toIndentedString(neighbourNodeAddress)).append("\n");
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
