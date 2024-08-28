/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

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
 * Enhanced Coverage Restriction Data for WB-N1 mode
 */
@ApiModel(description = "Enhanced Coverage Restriction Data for WB-N1 mode")
@JsonPropertyOrder({ EcRestrictionDataWb.JSON_PROPERTY_EC_MODE_A_RESTRICTED, EcRestrictionDataWb.JSON_PROPERTY_EC_MODE_B_RESTRICTED })
public class EcRestrictionDataWb
{
    public static final String JSON_PROPERTY_EC_MODE_A_RESTRICTED = "ecModeARestricted";
    private Boolean ecModeARestricted = false;

    public static final String JSON_PROPERTY_EC_MODE_B_RESTRICTED = "ecModeBRestricted";
    private Boolean ecModeBRestricted;

    public EcRestrictionDataWb()
    {
    }

    public EcRestrictionDataWb ecModeARestricted(Boolean ecModeARestricted)
    {

        this.ecModeARestricted = ecModeARestricted;
        return this;
    }

    /**
     * Get ecModeARestricted
     * 
     * @return ecModeARestricted
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EC_MODE_A_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEcModeARestricted()
    {
        return ecModeARestricted;
    }

    @JsonProperty(JSON_PROPERTY_EC_MODE_A_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcModeARestricted(Boolean ecModeARestricted)
    {
        this.ecModeARestricted = ecModeARestricted;
    }

    public EcRestrictionDataWb ecModeBRestricted(Boolean ecModeBRestricted)
    {

        this.ecModeBRestricted = ecModeBRestricted;
        return this;
    }

    /**
     * Get ecModeBRestricted
     * 
     * @return ecModeBRestricted
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EC_MODE_B_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getEcModeBRestricted()
    {
        return ecModeBRestricted;
    }

    @JsonProperty(JSON_PROPERTY_EC_MODE_B_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEcModeBRestricted(Boolean ecModeBRestricted)
    {
        this.ecModeBRestricted = ecModeBRestricted;
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
        EcRestrictionDataWb ecRestrictionDataWb = (EcRestrictionDataWb) o;
        return Objects.equals(this.ecModeARestricted, ecRestrictionDataWb.ecModeARestricted)
               && Objects.equals(this.ecModeBRestricted, ecRestrictionDataWb.ecModeBRestricted);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ecModeARestricted, ecModeBRestricted);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EcRestrictionDataWb {\n");
        sb.append("    ecModeARestricted: ").append(toIndentedString(ecModeARestricted)).append("\n");
        sb.append("    ecModeBRestricted: ").append(toIndentedString(ecModeBRestricted)).append("\n");
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
