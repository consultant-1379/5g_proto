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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This data type is defined in the same way as the &#39; RefToBinaryData &#39;
 * data type, but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the ' RefToBinaryData ' data type, but with the OpenAPI 'nullable: true' property.  ")
@JsonPropertyOrder({ RefToBinaryDataRm.JSON_PROPERTY_CONTENT_ID })
public class RefToBinaryDataRm
{
    public static final String JSON_PROPERTY_CONTENT_ID = "contentId";
    private String contentId;

    public RefToBinaryDataRm()
    {
    }

    public RefToBinaryDataRm contentId(String contentId)
    {

        this.contentId = contentId;
        return this;
    }

    /**
     * This IE shall contain the value of the Content-ID header of the referenced
     * binary body part.
     * 
     * @return contentId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "This IE shall contain the value of the Content-ID header of the referenced binary body part. ")
    @JsonProperty(JSON_PROPERTY_CONTENT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getContentId()
    {
        return contentId;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setContentId(String contentId)
    {
        this.contentId = contentId;
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
        RefToBinaryDataRm refToBinaryDataRm = (RefToBinaryDataRm) o;
        return Objects.equals(this.contentId, refToBinaryDataRm.contentId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(contentId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RefToBinaryDataRm {\n");
        sb.append("    contentId: ").append(toIndentedString(contentId)).append("\n");
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
