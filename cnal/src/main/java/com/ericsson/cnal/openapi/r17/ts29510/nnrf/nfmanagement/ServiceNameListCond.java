/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

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
 * Subscription to a set of NFs based on their support for a Service Name in the
 * Servic Name list
 */
@ApiModel(description = "Subscription to a set of NFs based on their support for a Service Name in the Servic Name list ")
@JsonPropertyOrder({ ServiceNameListCond.JSON_PROPERTY_CONDITION_TYPE, ServiceNameListCond.JSON_PROPERTY_SERVICE_NAME_LIST })
public class ServiceNameListCond
{
    /**
     * Gets or Sets conditionType
     */
    public enum ConditionTypeEnum
    {
        SERVICE_NAME_LIST_COND("SERVICE_NAME_LIST_COND");

        private String value;

        ConditionTypeEnum(String value)
        {
            this.value = value;
        }

        @JsonValue
        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ConditionTypeEnum fromValue(String value)
        {
            for (ConditionTypeEnum b : ConditionTypeEnum.values())
            {
                if (b.value.equals(value))
                {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    public static final String JSON_PROPERTY_CONDITION_TYPE = "conditionType";
    private ConditionTypeEnum conditionType;

    public static final String JSON_PROPERTY_SERVICE_NAME_LIST = "serviceNameList";
    private List<String> serviceNameList = new ArrayList<>();

    public ServiceNameListCond()
    {
    }

    public ServiceNameListCond conditionType(ConditionTypeEnum conditionType)
    {

        this.conditionType = conditionType;
        return this;
    }

    /**
     * Get conditionType
     * 
     * @return conditionType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONDITION_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public ConditionTypeEnum getConditionType()
    {
        return conditionType;
    }

    @JsonProperty(JSON_PROPERTY_CONDITION_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setConditionType(ConditionTypeEnum conditionType)
    {
        this.conditionType = conditionType;
    }

    public ServiceNameListCond serviceNameList(List<String> serviceNameList)
    {

        this.serviceNameList = serviceNameList;
        return this;
    }

    public ServiceNameListCond addServiceNameListItem(String serviceNameListItem)
    {
        this.serviceNameList.add(serviceNameListItem);
        return this;
    }

    /**
     * Get serviceNameList
     * 
     * @return serviceNameList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_NAME_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getServiceNameList()
    {
        return serviceNameList;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_NAME_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setServiceNameList(List<String> serviceNameList)
    {
        this.serviceNameList = serviceNameList;
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
        ServiceNameListCond serviceNameListCond = (ServiceNameListCond) o;
        return Objects.equals(this.conditionType, serviceNameListCond.conditionType)
               && Objects.equals(this.serviceNameList, serviceNameListCond.serviceNameList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(conditionType, serviceNameList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceNameListCond {\n");
        sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
        sb.append("    serviceNameList: ").append(toIndentedString(serviceNameList)).append("\n");
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
