/*
 * Nhss_imsUEAU
 * Nhss UE Authentication Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imsueau;

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
 * Contains RAND and AUTS
 */
@ApiModel(description = "Contains RAND and AUTS")
@JsonPropertyOrder({ ResynchronizationInfo.JSON_PROPERTY_RAND, ResynchronizationInfo.JSON_PROPERTY_AUTS })
public class ResynchronizationInfo
{
    public static final String JSON_PROPERTY_RAND = "rand";
    private String rand;

    public static final String JSON_PROPERTY_AUTS = "auts";
    private String auts;

    public ResynchronizationInfo()
    {
    }

    public ResynchronizationInfo rand(String rand)
    {

        this.rand = rand;
        return this;
    }

    /**
     * Get rand
     * 
     * @return rand
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_RAND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getRand()
    {
        return rand;
    }

    @JsonProperty(JSON_PROPERTY_RAND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRand(String rand)
    {
        this.rand = rand;
    }

    public ResynchronizationInfo auts(String auts)
    {

        this.auts = auts;
        return this;
    }

    /**
     * Get auts
     * 
     * @return auts
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_AUTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAuts()
    {
        return auts;
    }

    @JsonProperty(JSON_PROPERTY_AUTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAuts(String auts)
    {
        this.auts = auts;
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
        ResynchronizationInfo resynchronizationInfo = (ResynchronizationInfo) o;
        return Objects.equals(this.rand, resynchronizationInfo.rand) && Objects.equals(this.auts, resynchronizationInfo.auts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(rand, auts);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResynchronizationInfo {\n");
        sb.append("    rand: ").append(toIndentedString(rand)).append("\n");
        sb.append("    auts: ").append(toIndentedString(auts)).append("\n");
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
