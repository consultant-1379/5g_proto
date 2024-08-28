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
 * Contains information regarding operater determined barring.
 */
@ApiModel(description = "Contains information regarding operater  determined  barring.")
@JsonPropertyOrder({ OdbData.JSON_PROPERTY_ROAMING_ODB })
public class OdbData
{
    public static final String JSON_PROPERTY_ROAMING_ODB = "roamingOdb";
    private String roamingOdb;

    public OdbData()
    {
    }

    public OdbData roamingOdb(String roamingOdb)
    {

        this.roamingOdb = roamingOdb;
        return this;
    }

    /**
     * The enumeration RoamingOdb defines the Barring of Roaming as. See 3GPP TS
     * 23.015 for further description. It shall comply with the provisions defined
     * in table 5.7.3.1-1.
     * 
     * @return roamingOdb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration RoamingOdb defines the Barring of Roaming as. See 3GPP TS 23.015 for further description. It shall comply with the provisions defined in table 5.7.3.1-1. ")
    @JsonProperty(JSON_PROPERTY_ROAMING_ODB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRoamingOdb()
    {
        return roamingOdb;
    }

    @JsonProperty(JSON_PROPERTY_ROAMING_ODB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoamingOdb(String roamingOdb)
    {
        this.roamingOdb = roamingOdb;
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
        OdbData odbData = (OdbData) o;
        return Objects.equals(this.roamingOdb, odbData.roamingOdb);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(roamingOdb);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class OdbData {\n");
        sb.append("    roamingOdb: ").append(toIndentedString(roamingOdb)).append("\n");
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
