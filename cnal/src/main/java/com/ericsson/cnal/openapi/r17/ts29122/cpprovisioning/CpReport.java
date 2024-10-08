/*
 * 3gpp-cp-parameter-provisioning
 * API for provisioning communication pattern parameters.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.cpprovisioning;

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
 * Represents a CP report indicating the CP set identifier(s) which CP
 * parameter(s) are not added or modified successfully and the corresponding
 * failure cause(s).
 */
@ApiModel(description = "Represents a CP report indicating the CP set identifier(s) which CP parameter(s) are not added or modified successfully and the corresponding failure cause(s).")
@JsonPropertyOrder({ CpReport.JSON_PROPERTY_SET_IDS, CpReport.JSON_PROPERTY_FAILURE_CODE })
public class CpReport
{
    public static final String JSON_PROPERTY_SET_IDS = "setIds";
    private List<String> setIds = null;

    public static final String JSON_PROPERTY_FAILURE_CODE = "failureCode";
    private String failureCode;

    public CpReport()
    {
    }

    public CpReport setIds(List<String> setIds)
    {

        this.setIds = setIds;
        return this;
    }

    public CpReport addSetIdsItem(String setIdsItem)
    {
        if (this.setIds == null)
        {
            this.setIds = new ArrayList<>();
        }
        this.setIds.add(setIdsItem);
        return this;
    }

    /**
     * Identifies the CP set identifier(s) which CP parameter(s) are not added or
     * modified successfully
     * 
     * @return setIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the CP set identifier(s) which CP parameter(s) are not added or modified successfully")
    @JsonProperty(JSON_PROPERTY_SET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSetIds()
    {
        return setIds;
    }

    @JsonProperty(JSON_PROPERTY_SET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSetIds(List<String> setIds)
    {
        this.setIds = setIds;
    }

    public CpReport failureCode(String failureCode)
    {

        this.failureCode = failureCode;
        return this;
    }

    /**
     * Possible values are - MALFUNCTION: This value indicates that something
     * functions wrongly in CP parameter provisioning or the CP parameter
     * provisioning does not function at all. - SET_ID_DUPLICATED: The received CP
     * set identifier(s) are already provisioned. - OTHER_REASON: Other reason
     * unspecified.
     * 
     * @return failureCode
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Possible values are - MALFUNCTION: This value indicates that something functions wrongly in CP parameter provisioning or the CP parameter provisioning does not function at all. - SET_ID_DUPLICATED: The received CP set identifier(s) are already provisioned. - OTHER_REASON: Other reason unspecified. ")
    @JsonProperty(JSON_PROPERTY_FAILURE_CODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getFailureCode()
    {
        return failureCode;
    }

    @JsonProperty(JSON_PROPERTY_FAILURE_CODE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFailureCode(String failureCode)
    {
        this.failureCode = failureCode;
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
        CpReport cpReport = (CpReport) o;
        return Objects.equals(this.setIds, cpReport.setIds) && Objects.equals(this.failureCode, cpReport.failureCode);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(setIds, failureCode);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CpReport {\n");
        sb.append("    setIds: ").append(toIndentedString(setIds)).append("\n");
        sb.append("    failureCode: ").append(toIndentedString(failureCode)).append("\n");
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
