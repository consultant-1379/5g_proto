/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

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
 * MbsSecurityContext
 */
@JsonPropertyOrder({ MbsSecurityContext.JSON_PROPERTY_KEY_LIST })
public class MbsSecurityContext
{
    public static final String JSON_PROPERTY_KEY_LIST = "keyList";
    private Map<String, MbsKeyInfo> keyList = new HashMap<>();

    public MbsSecurityContext()
    {
    }

    public MbsSecurityContext keyList(Map<String, MbsKeyInfo> keyList)
    {

        this.keyList = keyList;
        return this;
    }

    public MbsSecurityContext putKeyListItem(String key,
                                             MbsKeyInfo keyListItem)
    {
        this.keyList.put(key, keyListItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where a (unique) valid JSON string serves as
     * key of MbsSecurityContext
     * 
     * @return keyList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "A map (list of key-value pairs) where a (unique) valid JSON string serves as key of MbsSecurityContext")
    @JsonProperty(JSON_PROPERTY_KEY_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, MbsKeyInfo> getKeyList()
    {
        return keyList;
    }

    @JsonProperty(JSON_PROPERTY_KEY_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setKeyList(Map<String, MbsKeyInfo> keyList)
    {
        this.keyList = keyList;
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
        MbsSecurityContext mbsSecurityContext = (MbsSecurityContext) o;
        return Objects.equals(this.keyList, mbsSecurityContext.keyList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(keyList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbsSecurityContext {\n");
        sb.append("    keyList: ").append(toIndentedString(keyList)).append("\n");
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
