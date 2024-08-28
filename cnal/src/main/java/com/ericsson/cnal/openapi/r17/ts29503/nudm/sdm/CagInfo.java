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
 * CagInfo
 */
@JsonPropertyOrder({ CagInfo.JSON_PROPERTY_ALLOWED_CAG_LIST, CagInfo.JSON_PROPERTY_CAG_ONLY_INDICATOR })
public class CagInfo
{
    public static final String JSON_PROPERTY_ALLOWED_CAG_LIST = "allowedCagList";
    private List<String> allowedCagList = new ArrayList<>();

    public static final String JSON_PROPERTY_CAG_ONLY_INDICATOR = "cagOnlyIndicator";
    private Boolean cagOnlyIndicator;

    public CagInfo()
    {
    }

    public CagInfo allowedCagList(List<String> allowedCagList)
    {

        this.allowedCagList = allowedCagList;
        return this;
    }

    public CagInfo addAllowedCagListItem(String allowedCagListItem)
    {
        this.allowedCagList.add(allowedCagListItem);
        return this;
    }

    /**
     * Get allowedCagList
     * 
     * @return allowedCagList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ALLOWED_CAG_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getAllowedCagList()
    {
        return allowedCagList;
    }

    @JsonProperty(JSON_PROPERTY_ALLOWED_CAG_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAllowedCagList(List<String> allowedCagList)
    {
        this.allowedCagList = allowedCagList;
    }

    public CagInfo cagOnlyIndicator(Boolean cagOnlyIndicator)
    {

        this.cagOnlyIndicator = cagOnlyIndicator;
        return this;
    }

    /**
     * Get cagOnlyIndicator
     * 
     * @return cagOnlyIndicator
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CAG_ONLY_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCagOnlyIndicator()
    {
        return cagOnlyIndicator;
    }

    @JsonProperty(JSON_PROPERTY_CAG_ONLY_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCagOnlyIndicator(Boolean cagOnlyIndicator)
    {
        this.cagOnlyIndicator = cagOnlyIndicator;
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
        CagInfo cagInfo = (CagInfo) o;
        return Objects.equals(this.allowedCagList, cagInfo.allowedCagList) && Objects.equals(this.cagOnlyIndicator, cagInfo.cagOnlyIndicator);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(allowedCagList, cagOnlyIndicator);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CagInfo {\n");
        sb.append("    allowedCagList: ").append(toIndentedString(allowedCagList)).append("\n");
        sb.append("    cagOnlyIndicator: ").append(toIndentedString(cagOnlyIndicator)).append("\n");
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
