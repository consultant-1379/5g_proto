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
 * Throughput
 */
@JsonPropertyOrder({ Throughput.JSON_PROPERTY_GUARANTEED_THPT, Throughput.JSON_PROPERTY_MAXIMUM_THPT })
public class Throughput
{
    public static final String JSON_PROPERTY_GUARANTEED_THPT = "guaranteedThpt";
    private Float guaranteedThpt;

    public static final String JSON_PROPERTY_MAXIMUM_THPT = "maximumThpt";
    private Float maximumThpt;

    public Throughput()
    {
    }

    public Throughput guaranteedThpt(Float guaranteedThpt)
    {

        this.guaranteedThpt = guaranteedThpt;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return guaranteedThpt
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_GUARANTEED_THPT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getGuaranteedThpt()
    {
        return guaranteedThpt;
    }

    @JsonProperty(JSON_PROPERTY_GUARANTEED_THPT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGuaranteedThpt(Float guaranteedThpt)
    {
        this.guaranteedThpt = guaranteedThpt;
    }

    public Throughput maximumThpt(Float maximumThpt)
    {

        this.maximumThpt = maximumThpt;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return maximumThpt
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_MAXIMUM_THPT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getMaximumThpt()
    {
        return maximumThpt;
    }

    @JsonProperty(JSON_PROPERTY_MAXIMUM_THPT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaximumThpt(Float maximumThpt)
    {
        this.maximumThpt = maximumThpt;
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
        Throughput throughput = (Throughput) o;
        return Objects.equals(this.guaranteedThpt, throughput.guaranteedThpt) && Objects.equals(this.maximumThpt, throughput.maximumThpt);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guaranteedThpt, maximumThpt);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Throughput {\n");
        sb.append("    guaranteedThpt: ").append(toIndentedString(guaranteedThpt)).append("\n");
        sb.append("    maximumThpt: ").append(toIndentedString(maximumThpt)).append("\n");
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
