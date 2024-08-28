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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The ComplexQuery data type is either a conjunctive normal form or a
 * disjunctive normal form. The attribute names \&quot;cnfUnits\&quot; and
 * \&quot;dnfUnits\&quot; (see clause 5.2.4.11 and clause 5.2.4.12) serve as
 * discriminator.
 */
@ApiModel(description = "The ComplexQuery data type is either a conjunctive normal form or a disjunctive normal form.  The attribute names \"cnfUnits\" and \"dnfUnits\" (see clause 5.2.4.11 and clause 5.2.4.12)  serve as discriminator. ")
@JsonPropertyOrder({ ComplexQuery.JSON_PROPERTY_CNF_UNITS, ComplexQuery.JSON_PROPERTY_DNF_UNITS })
public class ComplexQuery
{
    public static final String JSON_PROPERTY_CNF_UNITS = "cnfUnits";
    private List<CnfUnit> cnfUnits = new ArrayList<>();

    public static final String JSON_PROPERTY_DNF_UNITS = "dnfUnits";
    private List<DnfUnit> dnfUnits = new ArrayList<>();

    public ComplexQuery()
    {
    }

    public ComplexQuery cnfUnits(List<CnfUnit> cnfUnits)
    {

        this.cnfUnits = cnfUnits;
        return this;
    }

    public ComplexQuery addCnfUnitsItem(CnfUnit cnfUnitsItem)
    {
        this.cnfUnits.add(cnfUnitsItem);
        return this;
    }

    /**
     * Get cnfUnits
     * 
     * @return cnfUnits
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CNF_UNITS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<CnfUnit> getCnfUnits()
    {
        return cnfUnits;
    }

    @JsonProperty(JSON_PROPERTY_CNF_UNITS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCnfUnits(List<CnfUnit> cnfUnits)
    {
        this.cnfUnits = cnfUnits;
    }

    public ComplexQuery dnfUnits(List<DnfUnit> dnfUnits)
    {

        this.dnfUnits = dnfUnits;
        return this;
    }

    public ComplexQuery addDnfUnitsItem(DnfUnit dnfUnitsItem)
    {
        this.dnfUnits.add(dnfUnitsItem);
        return this;
    }

    /**
     * Get dnfUnits
     * 
     * @return dnfUnits
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_DNF_UNITS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<DnfUnit> getDnfUnits()
    {
        return dnfUnits;
    }

    @JsonProperty(JSON_PROPERTY_DNF_UNITS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDnfUnits(List<DnfUnit> dnfUnits)
    {
        this.dnfUnits = dnfUnits;
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
        ComplexQuery complexQuery = (ComplexQuery) o;
        return Objects.equals(this.cnfUnits, complexQuery.cnfUnits) && Objects.equals(this.dnfUnits, complexQuery.dnfUnits);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cnfUnits, dnfUnits);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ComplexQuery {\n");
        sb.append("    cnfUnits: ").append(toIndentedString(cnfUnits)).append("\n");
        sb.append("    dnfUnits: ").append(toIndentedString(dnfUnits)).append("\n");
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
