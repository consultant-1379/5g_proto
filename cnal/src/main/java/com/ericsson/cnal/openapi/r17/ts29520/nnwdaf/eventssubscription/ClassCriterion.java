/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

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
 * Indicates the dispersion class criterion for fixed, camper and/or traveller
 * UE, and/or the top-heavy UE dispersion class criterion.
 */
@ApiModel(description = "Indicates the dispersion class criterion for fixed, camper and/or traveller UE, and/or the  top-heavy UE dispersion class criterion. ")
@JsonPropertyOrder({ ClassCriterion.JSON_PROPERTY_DISPER_CLASS, ClassCriterion.JSON_PROPERTY_CLASS_THRESHOLD, ClassCriterion.JSON_PROPERTY_THRES_MATCH })
public class ClassCriterion
{
    public static final String JSON_PROPERTY_DISPER_CLASS = "disperClass";
    private Object disperClass;

    public static final String JSON_PROPERTY_CLASS_THRESHOLD = "classThreshold";
    private Integer classThreshold;

    public static final String JSON_PROPERTY_THRES_MATCH = "thresMatch";
    private String thresMatch;

    public ClassCriterion()
    {
    }

    public ClassCriterion disperClass(Object disperClass)
    {

        this.disperClass = disperClass;
        return this;
    }

    /**
     * Get disperClass
     * 
     * @return disperClass
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_DISPER_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Object getDisperClass()
    {
        return disperClass;
    }

    @JsonProperty(JSON_PROPERTY_DISPER_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDisperClass(Object disperClass)
    {
        this.disperClass = disperClass;
    }

    public ClassCriterion classThreshold(Integer classThreshold)
    {

        this.classThreshold = classThreshold;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return classThreshold
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_CLASS_THRESHOLD)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getClassThreshold()
    {
        return classThreshold;
    }

    @JsonProperty(JSON_PROPERTY_CLASS_THRESHOLD)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setClassThreshold(Integer classThreshold)
    {
        this.classThreshold = classThreshold;
    }

    public ClassCriterion thresMatch(String thresMatch)
    {

        this.thresMatch = thresMatch;
        return this;
    }

    /**
     * Possible values are: - ASCENDING: Threshold is crossed in ascending
     * direction. - DESCENDING: Threshold is crossed in descending direction. -
     * CROSSED: Threshold is crossed either in ascending or descending direction.
     * 
     * @return thresMatch
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Possible values are: - ASCENDING: Threshold is crossed in ascending direction. - DESCENDING: Threshold is crossed in descending direction. - CROSSED: Threshold is crossed either in ascending or descending direction. ")
    @JsonProperty(JSON_PROPERTY_THRES_MATCH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getThresMatch()
    {
        return thresMatch;
    }

    @JsonProperty(JSON_PROPERTY_THRES_MATCH)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setThresMatch(String thresMatch)
    {
        this.thresMatch = thresMatch;
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
        ClassCriterion classCriterion = (ClassCriterion) o;
        return Objects.equals(this.disperClass, classCriterion.disperClass) && Objects.equals(this.classThreshold, classCriterion.classThreshold)
               && Objects.equals(this.thresMatch, classCriterion.thresMatch);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(disperClass, classThreshold, thresMatch);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClassCriterion {\n");
        sb.append("    disperClass: ").append(toIndentedString(disperClass)).append("\n");
        sb.append("    classThreshold: ").append(toIndentedString(classThreshold)).append("\n");
        sb.append("    thresMatch: ").append(toIndentedString(thresMatch)).append("\n");
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
