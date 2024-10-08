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
 * Parameters supported by an TSCTSF for a given DNN
 */
@ApiModel(description = "Parameters supported by an TSCTSF for a given DNN")
@JsonPropertyOrder({ DnnTsctsfInfoItem.JSON_PROPERTY_DNN })
public class DnnTsctsfInfoItem
{
    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public DnnTsctsfInfoItem()
    {
    }

    public DnnTsctsfInfoItem dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * Get dnn
     * 
     * @return dnn
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
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
        DnnTsctsfInfoItem dnnTsctsfInfoItem = (DnnTsctsfInfoItem) o;
        return Objects.equals(this.dnn, dnnTsctsfInfoItem.dnn);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dnn);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DnnTsctsfInfoItem {\n");
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
