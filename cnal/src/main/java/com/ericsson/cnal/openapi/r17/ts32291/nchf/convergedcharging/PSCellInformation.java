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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ecgi;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ncgi;
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
 * PSCellInformation
 */
@JsonPropertyOrder({ PSCellInformation.JSON_PROPERTY_NRCGI, PSCellInformation.JSON_PROPERTY_ECGI })
public class PSCellInformation
{
    public static final String JSON_PROPERTY_NRCGI = "nrcgi";
    private Ncgi nrcgi;

    public static final String JSON_PROPERTY_ECGI = "ecgi";
    private Ecgi ecgi;

    public PSCellInformation()
    {
    }

    public PSCellInformation nrcgi(Ncgi nrcgi)
    {

        this.nrcgi = nrcgi;
        return this;
    }

    /**
     * Get nrcgi
     * 
     * @return nrcgi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NRCGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Ncgi getNrcgi()
    {
        return nrcgi;
    }

    @JsonProperty(JSON_PROPERTY_NRCGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNrcgi(Ncgi nrcgi)
    {
        this.nrcgi = nrcgi;
    }

    public PSCellInformation ecgi(Ecgi ecgi)
    {

        this.ecgi = ecgi;
        return this;
    }

    /**
     * Get ecgi
     * 
     * @return ecgi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Ecgi getEcgi()
    {
        return ecgi;
    }

    @JsonProperty(JSON_PROPERTY_ECGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcgi(Ecgi ecgi)
    {
        this.ecgi = ecgi;
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
        PSCellInformation psCellInformation = (PSCellInformation) o;
        return Objects.equals(this.nrcgi, psCellInformation.nrcgi) && Objects.equals(this.ecgi, psCellInformation.ecgi);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nrcgi, ecgi);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PSCellInformation {\n");
        sb.append("    nrcgi: ").append(toIndentedString(nrcgi)).append("\n");
        sb.append("    ecgi: ").append(toIndentedString(ecgi)).append("\n");
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
