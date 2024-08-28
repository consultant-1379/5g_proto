/*
 * Unified Data Repository Service API file for policy data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.policy.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29122.commondata.UsageThreshold;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains usage monitoring control data for a subscriber.
 */
@ApiModel(description = "Contains usage monitoring control data for a subscriber.")
@JsonPropertyOrder({ UsageMonDataLimit.JSON_PROPERTY_LIMIT_ID,
                     UsageMonDataLimit.JSON_PROPERTY_SCOPES,
                     UsageMonDataLimit.JSON_PROPERTY_UM_LEVEL,
                     UsageMonDataLimit.JSON_PROPERTY_START_DATE,
                     UsageMonDataLimit.JSON_PROPERTY_END_DATE,
                     UsageMonDataLimit.JSON_PROPERTY_USAGE_LIMIT,
                     UsageMonDataLimit.JSON_PROPERTY_RESET_PERIOD })
public class UsageMonDataLimit
{
    public static final String JSON_PROPERTY_LIMIT_ID = "limitId";
    private String limitId;

    public static final String JSON_PROPERTY_SCOPES = "scopes";
    private Map<String, UsageMonDataScope> scopes = null;

    public static final String JSON_PROPERTY_UM_LEVEL = "umLevel";
    private String umLevel;

    public static final String JSON_PROPERTY_START_DATE = "startDate";
    private OffsetDateTime startDate;

    public static final String JSON_PROPERTY_END_DATE = "endDate";
    private OffsetDateTime endDate;

    public static final String JSON_PROPERTY_USAGE_LIMIT = "usageLimit";
    private UsageThreshold usageLimit;

    public static final String JSON_PROPERTY_RESET_PERIOD = "resetPeriod";
    private TimePeriod resetPeriod;

    public UsageMonDataLimit()
    {
    }

    public UsageMonDataLimit limitId(String limitId)
    {

        this.limitId = limitId;
        return this;
    }

    /**
     * Get limitId
     * 
     * @return limitId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LIMIT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getLimitId()
    {
        return limitId;
    }

    @JsonProperty(JSON_PROPERTY_LIMIT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLimitId(String limitId)
    {
        this.limitId = limitId;
    }

    public UsageMonDataLimit scopes(Map<String, UsageMonDataScope> scopes)
    {

        this.scopes = scopes;
        return this;
    }

    public UsageMonDataLimit putScopesItem(String key,
                                           UsageMonDataScope scopesItem)
    {
        if (this.scopes == null)
        {
            this.scopes = new HashMap<>();
        }
        this.scopes.put(key, scopesItem);
        return this;
    }

    /**
     * Identifies the SNSSAI and DNN combinations to which the usage monitoring data
     * limit applies. The S-NSSAI is the key of the map.
     * 
     * @return scopes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the SNSSAI and DNN combinations to which the usage monitoring data limit applies. The S-NSSAI is the key of the map. ")
    @JsonProperty(JSON_PROPERTY_SCOPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, UsageMonDataScope> getScopes()
    {
        return scopes;
    }

    @JsonProperty(JSON_PROPERTY_SCOPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScopes(Map<String, UsageMonDataScope> scopes)
    {
        this.scopes = scopes;
    }

    public UsageMonDataLimit umLevel(String umLevel)
    {

        this.umLevel = umLevel;
        return this;
    }

    /**
     * Represents the usage monitoring level.
     * 
     * @return umLevel
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the usage monitoring level.")
    @JsonProperty(JSON_PROPERTY_UM_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUmLevel()
    {
        return umLevel;
    }

    @JsonProperty(JSON_PROPERTY_UM_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUmLevel(String umLevel)
    {
        this.umLevel = umLevel;
    }

    public UsageMonDataLimit startDate(OffsetDateTime startDate)
    {

        this.startDate = startDate;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return startDate
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getStartDate()
    {
        return startDate;
    }

    @JsonProperty(JSON_PROPERTY_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartDate(OffsetDateTime startDate)
    {
        this.startDate = startDate;
    }

    public UsageMonDataLimit endDate(OffsetDateTime endDate)
    {

        this.endDate = endDate;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return endDate
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getEndDate()
    {
        return endDate;
    }

    @JsonProperty(JSON_PROPERTY_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndDate(OffsetDateTime endDate)
    {
        this.endDate = endDate;
    }

    public UsageMonDataLimit usageLimit(UsageThreshold usageLimit)
    {

        this.usageLimit = usageLimit;
        return this;
    }

    /**
     * Get usageLimit
     * 
     * @return usageLimit
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USAGE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UsageThreshold getUsageLimit()
    {
        return usageLimit;
    }

    @JsonProperty(JSON_PROPERTY_USAGE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsageLimit(UsageThreshold usageLimit)
    {
        this.usageLimit = usageLimit;
    }

    public UsageMonDataLimit resetPeriod(TimePeriod resetPeriod)
    {

        this.resetPeriod = resetPeriod;
        return this;
    }

    /**
     * Get resetPeriod
     * 
     * @return resetPeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESET_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TimePeriod getResetPeriod()
    {
        return resetPeriod;
    }

    @JsonProperty(JSON_PROPERTY_RESET_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetPeriod(TimePeriod resetPeriod)
    {
        this.resetPeriod = resetPeriod;
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
        UsageMonDataLimit usageMonDataLimit = (UsageMonDataLimit) o;
        return Objects.equals(this.limitId, usageMonDataLimit.limitId) && Objects.equals(this.scopes, usageMonDataLimit.scopes)
               && Objects.equals(this.umLevel, usageMonDataLimit.umLevel) && Objects.equals(this.startDate, usageMonDataLimit.startDate)
               && Objects.equals(this.endDate, usageMonDataLimit.endDate) && Objects.equals(this.usageLimit, usageMonDataLimit.usageLimit)
               && Objects.equals(this.resetPeriod, usageMonDataLimit.resetPeriod);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(limitId, scopes, umLevel, startDate, endDate, usageLimit, resetPeriod);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UsageMonDataLimit {\n");
        sb.append("    limitId: ").append(toIndentedString(limitId)).append("\n");
        sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
        sb.append("    umLevel: ").append(toIndentedString(umLevel)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
        sb.append("    usageLimit: ").append(toIndentedString(usageLimit)).append("\n");
        sb.append("    resetPeriod: ").append(toIndentedString(resetPeriod)).append("\n");
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
