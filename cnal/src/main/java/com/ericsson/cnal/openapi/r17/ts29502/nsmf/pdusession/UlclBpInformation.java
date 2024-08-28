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
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * UL CL or BP Information
 */
@ApiModel(description = "UL CL or BP Information")
@JsonPropertyOrder({ UlclBpInformation.JSON_PROPERTY_ULCL_BP_UPF_ID })
public class UlclBpInformation
{
    public static final String JSON_PROPERTY_ULCL_BP_UPF_ID = "ulclBpUpfId";
    private UUID ulclBpUpfId;

    public UlclBpInformation()
    {
    }

    public UlclBpInformation ulclBpUpfId(UUID ulclBpUpfId)
    {

        this.ulclBpUpfId = ulclBpUpfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return ulclBpUpfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_ULCL_BP_UPF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUlclBpUpfId()
    {
        return ulclBpUpfId;
    }

    @JsonProperty(JSON_PROPERTY_ULCL_BP_UPF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUlclBpUpfId(UUID ulclBpUpfId)
    {
        this.ulclBpUpfId = ulclBpUpfId;
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
        UlclBpInformation ulclBpInformation = (UlclBpInformation) o;
        return Objects.equals(this.ulclBpUpfId, ulclBpInformation.ulclBpUpfId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ulclBpUpfId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UlclBpInformation {\n");
        sb.append("    ulclBpUpfId: ").append(toIndentedString(ulclBpUpfId)).append("\n");
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
