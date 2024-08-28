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
 * A String with Matching Operator
 */
@ApiModel(description = "A String with Matching Operator")
@JsonPropertyOrder({ StringMatchingCondition.JSON_PROPERTY_MATCHING_STRING, StringMatchingCondition.JSON_PROPERTY_MATCHING_OPERATOR })
public class StringMatchingCondition
{
    public static final String JSON_PROPERTY_MATCHING_STRING = "matchingString";
    private String matchingString;

    public static final String JSON_PROPERTY_MATCHING_OPERATOR = "matchingOperator";
    private String matchingOperator;

    public StringMatchingCondition()
    {
    }

    public StringMatchingCondition matchingString(String matchingString)
    {

        this.matchingString = matchingString;
        return this;
    }

    /**
     * Get matchingString
     * 
     * @return matchingString
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MATCHING_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMatchingString()
    {
        return matchingString;
    }

    @JsonProperty(JSON_PROPERTY_MATCHING_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMatchingString(String matchingString)
    {
        this.matchingString = matchingString;
    }

    public StringMatchingCondition matchingOperator(String matchingOperator)
    {

        this.matchingOperator = matchingOperator;
        return this;
    }

    /**
     * the matching operation.
     * 
     * @return matchingOperator
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "the matching operation.")
    @JsonProperty(JSON_PROPERTY_MATCHING_OPERATOR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getMatchingOperator()
    {
        return matchingOperator;
    }

    @JsonProperty(JSON_PROPERTY_MATCHING_OPERATOR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMatchingOperator(String matchingOperator)
    {
        this.matchingOperator = matchingOperator;
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
        StringMatchingCondition stringMatchingCondition = (StringMatchingCondition) o;
        return Objects.equals(this.matchingString, stringMatchingCondition.matchingString)
               && Objects.equals(this.matchingOperator, stringMatchingCondition.matchingOperator);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(matchingString, matchingOperator);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class StringMatchingCondition {\n");
        sb.append("    matchingString: ").append(toIndentedString(matchingString)).append("\n");
        sb.append("    matchingOperator: ").append(toIndentedString(matchingOperator)).append("\n");
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
