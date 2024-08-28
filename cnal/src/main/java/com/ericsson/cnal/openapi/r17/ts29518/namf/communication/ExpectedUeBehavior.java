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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UserLocation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents the expected UE behavior (e.g. UE moving trajectory) and its
 * validity period
 */
@ApiModel(description = "Represents the expected UE behavior (e.g. UE moving trajectory) and its validity period")
@JsonPropertyOrder({ ExpectedUeBehavior.JSON_PROPERTY_EXP_MOVE_TRAJECTORY, ExpectedUeBehavior.JSON_PROPERTY_VALIDITY_TIME })
public class ExpectedUeBehavior
{
    public static final String JSON_PROPERTY_EXP_MOVE_TRAJECTORY = "expMoveTrajectory";
    private List<UserLocation> expMoveTrajectory = new ArrayList<>();

    public static final String JSON_PROPERTY_VALIDITY_TIME = "validityTime";
    private OffsetDateTime validityTime;

    public ExpectedUeBehavior()
    {
    }

    public ExpectedUeBehavior expMoveTrajectory(List<UserLocation> expMoveTrajectory)
    {

        this.expMoveTrajectory = expMoveTrajectory;
        return this;
    }

    public ExpectedUeBehavior addExpMoveTrajectoryItem(UserLocation expMoveTrajectoryItem)
    {
        this.expMoveTrajectory.add(expMoveTrajectoryItem);
        return this;
    }

    /**
     * Get expMoveTrajectory
     * 
     * @return expMoveTrajectory
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EXP_MOVE_TRAJECTORY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<UserLocation> getExpMoveTrajectory()
    {
        return expMoveTrajectory;
    }

    @JsonProperty(JSON_PROPERTY_EXP_MOVE_TRAJECTORY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setExpMoveTrajectory(List<UserLocation> expMoveTrajectory)
    {
        this.expMoveTrajectory = expMoveTrajectory;
    }

    public ExpectedUeBehavior validityTime(OffsetDateTime validityTime)
    {

        this.validityTime = validityTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return validityTime
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getValidityTime()
    {
        return validityTime;
    }

    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setValidityTime(OffsetDateTime validityTime)
    {
        this.validityTime = validityTime;
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
        ExpectedUeBehavior expectedUeBehavior = (ExpectedUeBehavior) o;
        return Objects.equals(this.expMoveTrajectory, expectedUeBehavior.expMoveTrajectory)
               && Objects.equals(this.validityTime, expectedUeBehavior.validityTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(expMoveTrajectory, validityTime);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExpectedUeBehavior {\n");
        sb.append("    expMoveTrajectory: ").append(toIndentedString(expMoveTrajectory)).append("\n");
        sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
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
