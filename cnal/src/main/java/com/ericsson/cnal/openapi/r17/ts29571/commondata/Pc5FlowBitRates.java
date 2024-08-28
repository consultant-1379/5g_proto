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
 * it shall represent the PC5 Flow Bit Rates
 */
@ApiModel(description = "it shall represent the PC5 Flow Bit Rates")
@JsonPropertyOrder({ Pc5FlowBitRates.JSON_PROPERTY_GUA_FBR, Pc5FlowBitRates.JSON_PROPERTY_MAX_FBR })
public class Pc5FlowBitRates
{
    public static final String JSON_PROPERTY_GUA_FBR = "guaFbr";
    private String guaFbr;

    public static final String JSON_PROPERTY_MAX_FBR = "maxFbr";
    private String maxFbr;

    public Pc5FlowBitRates()
    {
    }

    public Pc5FlowBitRates guaFbr(String guaFbr)
    {

        this.guaFbr = guaFbr;
        return this;
    }

    /**
     * String representing a bit rate; the prefixes follow the standard symbols from
     * The International System of Units, and represent x1000 multipliers, with the
     * exception that prefix \&quot;K\&quot; is used to represent the standard
     * symbol \&quot;k\&quot;.
     * 
     * @return guaFbr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a bit rate; the prefixes follow the standard symbols from The International System of Units, and represent x1000 multipliers, with the exception that prefix \"K\" is used to represent the standard symbol \"k\". ")
    @JsonProperty(JSON_PROPERTY_GUA_FBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGuaFbr()
    {
        return guaFbr;
    }

    @JsonProperty(JSON_PROPERTY_GUA_FBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGuaFbr(String guaFbr)
    {
        this.guaFbr = guaFbr;
    }

    public Pc5FlowBitRates maxFbr(String maxFbr)
    {

        this.maxFbr = maxFbr;
        return this;
    }

    /**
     * String representing a bit rate; the prefixes follow the standard symbols from
     * The International System of Units, and represent x1000 multipliers, with the
     * exception that prefix \&quot;K\&quot; is used to represent the standard
     * symbol \&quot;k\&quot;.
     * 
     * @return maxFbr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a bit rate; the prefixes follow the standard symbols from The International System of Units, and represent x1000 multipliers, with the exception that prefix \"K\" is used to represent the standard symbol \"k\". ")
    @JsonProperty(JSON_PROPERTY_MAX_FBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMaxFbr()
    {
        return maxFbr;
    }

    @JsonProperty(JSON_PROPERTY_MAX_FBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxFbr(String maxFbr)
    {
        this.maxFbr = maxFbr;
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
        Pc5FlowBitRates pc5FlowBitRates = (Pc5FlowBitRates) o;
        return Objects.equals(this.guaFbr, pc5FlowBitRates.guaFbr) && Objects.equals(this.maxFbr, pc5FlowBitRates.maxFbr);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guaFbr, maxFbr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Pc5FlowBitRates {\n");
        sb.append("    guaFbr: ").append(toIndentedString(guaFbr)).append("\n");
        sb.append("    maxFbr: ").append(toIndentedString(maxFbr)).append("\n");
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
