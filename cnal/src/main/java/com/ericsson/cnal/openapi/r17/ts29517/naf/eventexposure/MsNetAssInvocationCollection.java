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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the Media Streaming Network Assistance invocation collected for an
 * UE Application via AF.
 */
@ApiModel(description = "Contains the Media Streaming Network Assistance invocation collected for an UE Application  via AF. ")
@JsonPropertyOrder({ MsNetAssInvocationCollection.JSON_PROPERTY_MS_NET_ASS_INVOCS })
public class MsNetAssInvocationCollection
{
    public static final String JSON_PROPERTY_MS_NET_ASS_INVOCS = "msNetAssInvocs";
    private List<Object> msNetAssInvocs = new ArrayList<>();

    public MsNetAssInvocationCollection()
    {
    }

    public MsNetAssInvocationCollection msNetAssInvocs(List<Object> msNetAssInvocs)
    {

        this.msNetAssInvocs = msNetAssInvocs;
        return this;
    }

    public MsNetAssInvocationCollection addMsNetAssInvocsItem(Object msNetAssInvocsItem)
    {
        this.msNetAssInvocs.add(msNetAssInvocsItem);
        return this;
    }

    /**
     * Get msNetAssInvocs
     * 
     * @return msNetAssInvocs
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MS_NET_ASS_INVOCS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Object> getMsNetAssInvocs()
    {
        return msNetAssInvocs;
    }

    @JsonProperty(JSON_PROPERTY_MS_NET_ASS_INVOCS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMsNetAssInvocs(List<Object> msNetAssInvocs)
    {
        this.msNetAssInvocs = msNetAssInvocs;
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
        MsNetAssInvocationCollection msNetAssInvocationCollection = (MsNetAssInvocationCollection) o;
        return Objects.equals(this.msNetAssInvocs, msNetAssInvocationCollection.msNetAssInvocs);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(msNetAssInvocs);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MsNetAssInvocationCollection {\n");
        sb.append("    msNetAssInvocs: ").append(toIndentedString(msNetAssInvocs)).append("\n");
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
