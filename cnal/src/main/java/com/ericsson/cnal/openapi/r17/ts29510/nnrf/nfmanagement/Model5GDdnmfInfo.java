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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
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
 * Information of an 5G DDNMF NF Instance
 */
@ApiModel(description = "Information of an 5G DDNMF NF Instance")
@JsonPropertyOrder({ Model5GDdnmfInfo.JSON_PROPERTY_PLMN_ID })
@JsonTypeName("5GDdnmfInfo")
public class Model5GDdnmfInfo
{
    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public Model5GDdnmfInfo()
    {
    }

    public Model5GDdnmfInfo plmnId(PlmnId plmnId)
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

    public PlmnId getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPlmnId(PlmnId plmnId)
    {
        this.plmnId = plmnId;
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
        Model5GDdnmfInfo _5gDdnmfInfo = (Model5GDdnmfInfo) o;
        return Objects.equals(this.plmnId, _5gDdnmfInfo.plmnId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(plmnId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Model5GDdnmfInfo {\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
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
