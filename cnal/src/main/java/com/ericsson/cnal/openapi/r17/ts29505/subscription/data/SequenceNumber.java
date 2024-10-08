/*
 * Unified Data Repository Service API file for subscription data
 * Unified Data Repository Service (subscription data).   The API version is defined in 3GPP TS 29.504.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29505.subscription.data;

import java.util.Objects;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the SQN.
 */
@ApiModel(description = "Contains the SQN.")
@JsonPropertyOrder({ SequenceNumber.JSON_PROPERTY_SQN_SCHEME,
                     SequenceNumber.JSON_PROPERTY_SQN,
                     SequenceNumber.JSON_PROPERTY_LAST_INDEXES,
                     SequenceNumber.JSON_PROPERTY_IND_LENGTH,
                     SequenceNumber.JSON_PROPERTY_DIF_SIGN })
public class SequenceNumber
{
    public static final String JSON_PROPERTY_SQN_SCHEME = "sqnScheme";
    private String sqnScheme;

    public static final String JSON_PROPERTY_SQN = "sqn";
    private String sqn;

    public static final String JSON_PROPERTY_LAST_INDEXES = "lastIndexes";
    private Map<String, Integer> lastIndexes = null;

    public static final String JSON_PROPERTY_IND_LENGTH = "indLength";
    private Integer indLength;

    public static final String JSON_PROPERTY_DIF_SIGN = "difSign";
    private Sign difSign;

    public SequenceNumber()
    {
    }

    public SequenceNumber sqnScheme(String sqnScheme)
    {

        this.sqnScheme = sqnScheme;
        return this;
    }

    /**
     * Scheme for generation of Sequence Numbers.
     * 
     * @return sqnScheme
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Scheme for generation of Sequence Numbers.")
    @JsonProperty(JSON_PROPERTY_SQN_SCHEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSqnScheme()
    {
        return sqnScheme;
    }

    @JsonProperty(JSON_PROPERTY_SQN_SCHEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSqnScheme(String sqnScheme)
    {
        this.sqnScheme = sqnScheme;
    }

    public SequenceNumber sqn(String sqn)
    {

        this.sqn = sqn;
        return this;
    }

    /**
     * Get sqn
     * 
     * @return sqn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SQN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSqn()
    {
        return sqn;
    }

    @JsonProperty(JSON_PROPERTY_SQN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSqn(String sqn)
    {
        this.sqn = sqn;
    }

    public SequenceNumber lastIndexes(Map<String, Integer> lastIndexes)
    {

        this.lastIndexes = lastIndexes;
        return this;
    }

    public SequenceNumber putLastIndexesItem(String key,
                                             Integer lastIndexesItem)
    {
        if (this.lastIndexes == null)
        {
            this.lastIndexes = new HashMap<>();
        }
        this.lastIndexes.put(key, lastIndexesItem);
        return this;
    }

    /**
     * Get lastIndexes
     * 
     * @return lastIndexes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LAST_INDEXES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, Integer> getLastIndexes()
    {
        return lastIndexes;
    }

    @JsonProperty(JSON_PROPERTY_LAST_INDEXES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastIndexes(Map<String, Integer> lastIndexes)
    {
        this.lastIndexes = lastIndexes;
    }

    public SequenceNumber indLength(Integer indLength)
    {

        this.indLength = indLength;
        return this;
    }

    /**
     * Get indLength minimum: 0
     * 
     * @return indLength
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IND_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getIndLength()
    {
        return indLength;
    }

    @JsonProperty(JSON_PROPERTY_IND_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndLength(Integer indLength)
    {
        this.indLength = indLength;
    }

    public SequenceNumber difSign(Sign difSign)
    {

        this.difSign = difSign;
        return this;
    }

    /**
     * Get difSign
     * 
     * @return difSign
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DIF_SIGN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Sign getDifSign()
    {
        return difSign;
    }

    @JsonProperty(JSON_PROPERTY_DIF_SIGN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDifSign(Sign difSign)
    {
        this.difSign = difSign;
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
        SequenceNumber sequenceNumber = (SequenceNumber) o;
        return Objects.equals(this.sqnScheme, sequenceNumber.sqnScheme) && Objects.equals(this.sqn, sequenceNumber.sqn)
               && Objects.equals(this.lastIndexes, sequenceNumber.lastIndexes) && Objects.equals(this.indLength, sequenceNumber.indLength)
               && Objects.equals(this.difSign, sequenceNumber.difSign);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sqnScheme, sqn, lastIndexes, indLength, difSign);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SequenceNumber {\n");
        sb.append("    sqnScheme: ").append(toIndentedString(sqnScheme)).append("\n");
        sb.append("    sqn: ").append(toIndentedString(sqn)).append("\n");
        sb.append("    lastIndexes: ").append(toIndentedString(lastIndexes)).append("\n");
        sb.append("    indLength: ").append(toIndentedString(indLength)).append("\n");
        sb.append("    difSign: ").append(toIndentedString(difSign)).append("\n");
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
