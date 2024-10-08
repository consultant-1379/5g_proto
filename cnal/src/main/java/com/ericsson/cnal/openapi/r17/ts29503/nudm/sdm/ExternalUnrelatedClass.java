/*
 * Nudm_SDM
 * Nudm Subscriber Data Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm;

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
 * ExternalUnrelatedClass
 */
@JsonPropertyOrder({ ExternalUnrelatedClass.JSON_PROPERTY_LCS_CLIENT_EXTERNALS,
                     ExternalUnrelatedClass.JSON_PROPERTY_AF_EXTERNALS,
                     ExternalUnrelatedClass.JSON_PROPERTY_LCS_CLIENT_GROUP_EXTERNALS })
public class ExternalUnrelatedClass
{
    public static final String JSON_PROPERTY_LCS_CLIENT_EXTERNALS = "lcsClientExternals";
    private List<LcsClientExternal> lcsClientExternals = null;

    public static final String JSON_PROPERTY_AF_EXTERNALS = "afExternals";
    private List<AfExternal> afExternals = null;

    public static final String JSON_PROPERTY_LCS_CLIENT_GROUP_EXTERNALS = "lcsClientGroupExternals";
    private List<LcsClientGroupExternal> lcsClientGroupExternals = null;

    public ExternalUnrelatedClass()
    {
    }

    public ExternalUnrelatedClass lcsClientExternals(List<LcsClientExternal> lcsClientExternals)
    {

        this.lcsClientExternals = lcsClientExternals;
        return this;
    }

    public ExternalUnrelatedClass addLcsClientExternalsItem(LcsClientExternal lcsClientExternalsItem)
    {
        if (this.lcsClientExternals == null)
        {
            this.lcsClientExternals = new ArrayList<>();
        }
        this.lcsClientExternals.add(lcsClientExternalsItem);
        return this;
    }

    /**
     * Get lcsClientExternals
     * 
     * @return lcsClientExternals
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LcsClientExternal> getLcsClientExternals()
    {
        return lcsClientExternals;
    }

    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsClientExternals(List<LcsClientExternal> lcsClientExternals)
    {
        this.lcsClientExternals = lcsClientExternals;
    }

    public ExternalUnrelatedClass afExternals(List<AfExternal> afExternals)
    {

        this.afExternals = afExternals;
        return this;
    }

    public ExternalUnrelatedClass addAfExternalsItem(AfExternal afExternalsItem)
    {
        if (this.afExternals == null)
        {
            this.afExternals = new ArrayList<>();
        }
        this.afExternals.add(afExternalsItem);
        return this;
    }

    /**
     * Get afExternals
     * 
     * @return afExternals
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AfExternal> getAfExternals()
    {
        return afExternals;
    }

    @JsonProperty(JSON_PROPERTY_AF_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfExternals(List<AfExternal> afExternals)
    {
        this.afExternals = afExternals;
    }

    public ExternalUnrelatedClass lcsClientGroupExternals(List<LcsClientGroupExternal> lcsClientGroupExternals)
    {

        this.lcsClientGroupExternals = lcsClientGroupExternals;
        return this;
    }

    public ExternalUnrelatedClass addLcsClientGroupExternalsItem(LcsClientGroupExternal lcsClientGroupExternalsItem)
    {
        if (this.lcsClientGroupExternals == null)
        {
            this.lcsClientGroupExternals = new ArrayList<>();
        }
        this.lcsClientGroupExternals.add(lcsClientGroupExternalsItem);
        return this;
    }

    /**
     * Get lcsClientGroupExternals
     * 
     * @return lcsClientGroupExternals
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_GROUP_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LcsClientGroupExternal> getLcsClientGroupExternals()
    {
        return lcsClientGroupExternals;
    }

    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_GROUP_EXTERNALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsClientGroupExternals(List<LcsClientGroupExternal> lcsClientGroupExternals)
    {
        this.lcsClientGroupExternals = lcsClientGroupExternals;
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
        ExternalUnrelatedClass externalUnrelatedClass = (ExternalUnrelatedClass) o;
        return Objects.equals(this.lcsClientExternals, externalUnrelatedClass.lcsClientExternals)
               && Objects.equals(this.afExternals, externalUnrelatedClass.afExternals)
               && Objects.equals(this.lcsClientGroupExternals, externalUnrelatedClass.lcsClientGroupExternals);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lcsClientExternals, afExternals, lcsClientGroupExternals);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalUnrelatedClass {\n");
        sb.append("    lcsClientExternals: ").append(toIndentedString(lcsClientExternals)).append("\n");
        sb.append("    afExternals: ").append(toIndentedString(afExternals)).append("\n");
        sb.append("    lcsClientGroupExternals: ").append(toIndentedString(lcsClientGroupExternals)).append("\n");
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
