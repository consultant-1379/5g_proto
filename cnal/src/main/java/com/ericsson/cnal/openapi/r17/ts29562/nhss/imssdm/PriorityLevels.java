/*
 * Nhss_imsSDM
 * Nhss Subscriber Data Management Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imssdm;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Namespaces and priority levels allowed for the IMS public Identity
 */
@ApiModel(description = "Namespaces and priority levels allowed for the IMS public Identity")
@JsonPropertyOrder({ PriorityLevels.JSON_PROPERTY_SERVICE_PRIORITY_LEVEL_LIST, PriorityLevels.JSON_PROPERTY_SERVICE_PRIORITY_LEVEL })
public class PriorityLevels
{
    public static final String JSON_PROPERTY_SERVICE_PRIORITY_LEVEL_LIST = "servicePriorityLevelList";
    private Set<String> servicePriorityLevelList = new LinkedHashSet<>();

    public static final String JSON_PROPERTY_SERVICE_PRIORITY_LEVEL = "servicePriorityLevel";
    private Integer servicePriorityLevel;

    public PriorityLevels()
    {
    }

    public PriorityLevels servicePriorityLevelList(Set<String> servicePriorityLevelList)
    {

        this.servicePriorityLevelList = servicePriorityLevelList;
        return this;
    }

    public PriorityLevels addServicePriorityLevelListItem(String servicePriorityLevelListItem)
    {
        this.servicePriorityLevelList.add(servicePriorityLevelListItem);
        return this;
    }

    /**
     * Get servicePriorityLevelList
     * 
     * @return servicePriorityLevelList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_PRIORITY_LEVEL_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Set<String> getServicePriorityLevelList()
    {
        return servicePriorityLevelList;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_SERVICE_PRIORITY_LEVEL_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setServicePriorityLevelList(Set<String> servicePriorityLevelList)
    {
        this.servicePriorityLevelList = servicePriorityLevelList;
    }

    public PriorityLevels servicePriorityLevel(Integer servicePriorityLevel)
    {

        this.servicePriorityLevel = servicePriorityLevel;
        return this;
    }

    /**
     * Get servicePriorityLevel minimum: 0 maximum: 4
     * 
     * @return servicePriorityLevel
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_PRIORITY_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getServicePriorityLevel()
    {
        return servicePriorityLevel;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_PRIORITY_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServicePriorityLevel(Integer servicePriorityLevel)
    {
        this.servicePriorityLevel = servicePriorityLevel;
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
        PriorityLevels priorityLevels = (PriorityLevels) o;
        return Objects.equals(this.servicePriorityLevelList, priorityLevels.servicePriorityLevelList)
               && Objects.equals(this.servicePriorityLevel, priorityLevels.servicePriorityLevel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(servicePriorityLevelList, servicePriorityLevel);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PriorityLevels {\n");
        sb.append("    servicePriorityLevelList: ").append(toIndentedString(servicePriorityLevelList)).append("\n");
        sb.append("    servicePriorityLevel: ").append(toIndentedString(servicePriorityLevel)).append("\n");
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
