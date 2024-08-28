/*
 * Naf_EventExposure
 * AF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29517.naf.eventexposure;

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
 * Contains the collective behaviour filter information to be collected from UE.
 */
@ApiModel(description = "Contains the collective behaviour filter information to be collected from UE.")
@JsonPropertyOrder({ CollectiveBehaviourFilter.JSON_PROPERTY_TYPE,
                     CollectiveBehaviourFilter.JSON_PROPERTY_VALUE,
                     CollectiveBehaviourFilter.JSON_PROPERTY_LIST_OF_UE_IND })
public class CollectiveBehaviourFilter
{
    public static final String JSON_PROPERTY_TYPE = "type";
    private String type;

    public static final String JSON_PROPERTY_VALUE = "value";
    private String value;

    public static final String JSON_PROPERTY_LIST_OF_UE_IND = "listOfUeInd";
    private Boolean listOfUeInd;

    public CollectiveBehaviourFilter()
    {
    }

    public CollectiveBehaviourFilter type(String type)
    {

        this.type = type;
        return this;
    }

    /**
     * Represents collective behaviour parameter type.
     * 
     * @return type
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Represents collective behaviour parameter type.")
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getType()
    {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setType(String type)
    {
        this.type = type;
    }

    public CollectiveBehaviourFilter value(String value)
    {

        this.value = value;
        return this;
    }

    /**
     * Value of the parameter type as in the type attribute.
     * 
     * @return value
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Value of the parameter type as in the type attribute.")
    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getValue()
    {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setValue(String value)
    {
        this.value = value;
    }

    public CollectiveBehaviourFilter listOfUeInd(Boolean listOfUeInd)
    {

        this.listOfUeInd = listOfUeInd;
        return this;
    }

    /**
     * Indicates whether request list of UE IDs that fulfill a collective behaviour
     * within the area of interest. This attribute shall set to \&quot;true\&quot;
     * if request the list of UE IDs, otherwise, set to \&quot;false\&quot;. May
     * only be present and sets to \&quot;true\&quot; if \&quot;AfEvent\&quot; sets
     * to \&quot;COLLECTIVE_BEHAVIOUR\&quot;.
     * 
     * @return listOfUeInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether request list of UE IDs that fulfill a collective behaviour within the area of interest. This attribute shall set to \"true\" if request the list of UE IDs, otherwise, set to \"false\". May only be present and sets to \"true\" if \"AfEvent\" sets to \"COLLECTIVE_BEHAVIOUR\". ")
    @JsonProperty(JSON_PROPERTY_LIST_OF_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getListOfUeInd()
    {
        return listOfUeInd;
    }

    @JsonProperty(JSON_PROPERTY_LIST_OF_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setListOfUeInd(Boolean listOfUeInd)
    {
        this.listOfUeInd = listOfUeInd;
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
        CollectiveBehaviourFilter collectiveBehaviourFilter = (CollectiveBehaviourFilter) o;
        return Objects.equals(this.type, collectiveBehaviourFilter.type) && Objects.equals(this.value, collectiveBehaviourFilter.value)
               && Objects.equals(this.listOfUeInd, collectiveBehaviourFilter.listOfUeInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, value, listOfUeInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CollectiveBehaviourFilter {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    listOfUeInd: ").append(toIndentedString(listOfUeInd)).append("\n");
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
