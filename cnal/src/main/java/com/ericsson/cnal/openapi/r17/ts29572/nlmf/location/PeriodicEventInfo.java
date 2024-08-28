/*
 * LMF Location
 * LMF Location Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29572.nlmf.location;

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
 * Indicates the information of periodic event reporting.
 */
@ApiModel(description = "Indicates the information of periodic event reporting.")
@JsonPropertyOrder({ PeriodicEventInfo.JSON_PROPERTY_REPORTING_AMOUNT, PeriodicEventInfo.JSON_PROPERTY_REPORTING_INTERVAL })
public class PeriodicEventInfo
{
    public static final String JSON_PROPERTY_REPORTING_AMOUNT = "reportingAmount";
    private Integer reportingAmount;

    public static final String JSON_PROPERTY_REPORTING_INTERVAL = "reportingInterval";
    private Integer reportingInterval;

    public PeriodicEventInfo()
    {
    }

    public PeriodicEventInfo reportingAmount(Integer reportingAmount)
    {

        this.reportingAmount = reportingAmount;
        return this;
    }

    /**
     * Number of required periodic event reports. minimum: 1 maximum: 8639999
     * 
     * @return reportingAmount
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Number of required periodic event reports.")
    @JsonProperty(JSON_PROPERTY_REPORTING_AMOUNT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getReportingAmount()
    {
        return reportingAmount;
    }

    @JsonProperty(JSON_PROPERTY_REPORTING_AMOUNT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReportingAmount(Integer reportingAmount)
    {
        this.reportingAmount = reportingAmount;
    }

    public PeriodicEventInfo reportingInterval(Integer reportingInterval)
    {

        this.reportingInterval = reportingInterval;
        return this;
    }

    /**
     * Event reporting periodic interval. minimum: 1 maximum: 8639999
     * 
     * @return reportingInterval
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Event reporting periodic interval.")
    @JsonProperty(JSON_PROPERTY_REPORTING_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getReportingInterval()
    {
        return reportingInterval;
    }

    @JsonProperty(JSON_PROPERTY_REPORTING_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReportingInterval(Integer reportingInterval)
    {
        this.reportingInterval = reportingInterval;
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
        PeriodicEventInfo periodicEventInfo = (PeriodicEventInfo) o;
        return Objects.equals(this.reportingAmount, periodicEventInfo.reportingAmount)
               && Objects.equals(this.reportingInterval, periodicEventInfo.reportingInterval);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(reportingAmount, reportingInterval);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeriodicEventInfo {\n");
        sb.append("    reportingAmount: ").append(toIndentedString(reportingAmount)).append("\n");
        sb.append("    reportingInterval: ").append(toIndentedString(reportingInterval)).append("\n");
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
