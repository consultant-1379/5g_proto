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
 * This data type is defined in the same way as the &#39;Guami&#39; data type,
 * but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the 'Guami' data type, but with the OpenAPI 'nullable: true' property. ")
@JsonPropertyOrder({ GuamiRm.JSON_PROPERTY_PLMN_ID, GuamiRm.JSON_PROPERTY_AMF_ID })
public class GuamiRm
{
    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnIdNid plmnId;

    public static final String JSON_PROPERTY_AMF_ID = "amfId";
    private String amfId;

    public GuamiRm()
    {
    }

    public GuamiRm plmnId(PlmnIdNid plmnId)
    {

        this.plmnId = plmnId;
        return this;
    }

    /**
     * Get plmnId
     * 
     * @return plmnId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public PlmnIdNid getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPlmnId(PlmnIdNid plmnId)
    {
        this.plmnId = plmnId;
    }

    public GuamiRm amfId(String amfId)
    {

        this.amfId = amfId;
        return this;
    }

    /**
     * String identifying the AMF ID composed of AMF Region ID (8 bits), AMF Set ID
     * (10 bits) and AMF Pointer (6 bits) as specified in clause 2.10.1 of 3GPP TS
     * 23.003. It is encoded as a string of 6 hexadecimal characters (i.e., 24
     * bits).
     * 
     * @return amfId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String identifying the AMF ID composed of AMF Region ID (8 bits), AMF Set ID (10 bits) and AMF  Pointer (6 bits) as specified in clause 2.10.1 of 3GPP TS 23.003. It is encoded as a string of  6 hexadecimal characters (i.e., 24 bits).  ")
    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAmfId()
    {
        return amfId;
    }

    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAmfId(String amfId)
    {
        this.amfId = amfId;
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
        GuamiRm guamiRm = (GuamiRm) o;
        return Objects.equals(this.plmnId, guamiRm.plmnId) && Objects.equals(this.amfId, guamiRm.amfId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(plmnId, amfId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class GuamiRm {\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    amfId: ").append(toIndentedString(amfId)).append("\n");
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
