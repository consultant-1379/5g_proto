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
 * LcsPrivacyData
 */
@JsonPropertyOrder({ LcsPrivacyData.JSON_PROPERTY_LPI, LcsPrivacyData.JSON_PROPERTY_UNRELATED_CLASS, LcsPrivacyData.JSON_PROPERTY_PLMN_OPERATOR_CLASSES })
public class LcsPrivacyData
{
    public static final String JSON_PROPERTY_LPI = "lpi";
    private Lpi lpi;

    public static final String JSON_PROPERTY_UNRELATED_CLASS = "unrelatedClass";
    private UnrelatedClass unrelatedClass;

    public static final String JSON_PROPERTY_PLMN_OPERATOR_CLASSES = "plmnOperatorClasses";
    private List<PlmnOperatorClass> plmnOperatorClasses = null;

    public LcsPrivacyData()
    {
    }

    public LcsPrivacyData lpi(Lpi lpi)
    {

        this.lpi = lpi;
        return this;
    }

    /**
     * Get lpi
     * 
     * @return lpi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Lpi getLpi()
    {
        return lpi;
    }

    @JsonProperty(JSON_PROPERTY_LPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLpi(Lpi lpi)
    {
        this.lpi = lpi;
    }

    public LcsPrivacyData unrelatedClass(UnrelatedClass unrelatedClass)
    {

        this.unrelatedClass = unrelatedClass;
        return this;
    }

    /**
     * Get unrelatedClass
     * 
     * @return unrelatedClass
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UNRELATED_CLASS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UnrelatedClass getUnrelatedClass()
    {
        return unrelatedClass;
    }

    @JsonProperty(JSON_PROPERTY_UNRELATED_CLASS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnrelatedClass(UnrelatedClass unrelatedClass)
    {
        this.unrelatedClass = unrelatedClass;
    }

    public LcsPrivacyData plmnOperatorClasses(List<PlmnOperatorClass> plmnOperatorClasses)
    {

        this.plmnOperatorClasses = plmnOperatorClasses;
        return this;
    }

    public LcsPrivacyData addPlmnOperatorClassesItem(PlmnOperatorClass plmnOperatorClassesItem)
    {
        if (this.plmnOperatorClasses == null)
        {
            this.plmnOperatorClasses = new ArrayList<>();
        }
        this.plmnOperatorClasses.add(plmnOperatorClassesItem);
        return this;
    }

    /**
     * Get plmnOperatorClasses
     * 
     * @return plmnOperatorClasses
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_OPERATOR_CLASSES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnOperatorClass> getPlmnOperatorClasses()
    {
        return plmnOperatorClasses;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_OPERATOR_CLASSES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnOperatorClasses(List<PlmnOperatorClass> plmnOperatorClasses)
    {
        this.plmnOperatorClasses = plmnOperatorClasses;
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
        LcsPrivacyData lcsPrivacyData = (LcsPrivacyData) o;
        return Objects.equals(this.lpi, lcsPrivacyData.lpi) && Objects.equals(this.unrelatedClass, lcsPrivacyData.unrelatedClass)
               && Objects.equals(this.plmnOperatorClasses, lcsPrivacyData.plmnOperatorClasses);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lpi, unrelatedClass, plmnOperatorClasses);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class LcsPrivacyData {\n");
        sb.append("    lpi: ").append(toIndentedString(lpi)).append("\n");
        sb.append("    unrelatedClass: ").append(toIndentedString(unrelatedClass)).append("\n");
        sb.append("    plmnOperatorClasses: ").append(toIndentedString(plmnOperatorClasses)).append("\n");
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
