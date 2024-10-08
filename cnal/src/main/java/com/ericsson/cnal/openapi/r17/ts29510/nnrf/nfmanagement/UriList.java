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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.LinksValueSchema;
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
 * Represents a set of URIs following the 3GPP hypermedia format (containing a
 * \&quot;_links\&quot; attribute).
 */
@ApiModel(description = "Represents a set of URIs following the 3GPP hypermedia format (containing a \"_links\" attribute). ")
@JsonPropertyOrder({ UriList.JSON_PROPERTY_LINKS, UriList.JSON_PROPERTY_TOTAL_ITEM_COUNT })
public class UriList
{
    public static final String JSON_PROPERTY_LINKS = "_links";
    private Map<String, LinksValueSchema> links = null;

    public static final String JSON_PROPERTY_TOTAL_ITEM_COUNT = "totalItemCount";
    private Integer totalItemCount;

    public UriList()
    {
    }

    public UriList links(Map<String, LinksValueSchema> links)
    {

        this.links = links;
        return this;
    }

    public UriList putLinksItem(String key,
                                LinksValueSchema linksItem)
    {
        if (this.links == null)
        {
            this.links = new HashMap<>();
        }
        this.links.put(key, linksItem);
        return this;
    }

    /**
     * List of the URI of NF instances. It has two members whose names are item and
     * self. The item attribute contains an array of URIs.
     * 
     * @return links
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "List of the URI of NF instances. It has two members whose names are item and self. The item attribute contains an array of URIs. ")
    @JsonProperty(JSON_PROPERTY_LINKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, LinksValueSchema> getLinks()
    {
        return links;
    }

    @JsonProperty(JSON_PROPERTY_LINKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLinks(Map<String, LinksValueSchema> links)
    {
        this.links = links;
    }

    public UriList totalItemCount(Integer totalItemCount)
    {

        this.totalItemCount = totalItemCount;
        return this;
    }

    /**
     * Get totalItemCount
     * 
     * @return totalItemCount
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TOTAL_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTotalItemCount()
    {
        return totalItemCount;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalItemCount(Integer totalItemCount)
    {
        this.totalItemCount = totalItemCount;
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
        UriList uriList = (UriList) o;
        return Objects.equals(this.links, uriList.links) && Objects.equals(this.totalItemCount, uriList.totalItemCount);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(links, totalItemCount);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UriList {\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    totalItemCount: ").append(toIndentedString(totalItemCount)).append("\n");
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
