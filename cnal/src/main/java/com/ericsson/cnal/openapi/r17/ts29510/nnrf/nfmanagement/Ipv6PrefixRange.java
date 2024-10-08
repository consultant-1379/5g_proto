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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Range of IPv6 prefixes
 */
@ApiModel(description = "Range of IPv6 prefixes")
@JsonPropertyOrder({ Ipv6PrefixRange.JSON_PROPERTY_START, Ipv6PrefixRange.JSON_PROPERTY_END })
public class Ipv6PrefixRange
{
    public static final String JSON_PROPERTY_START = "start";
    private String start;

    public static final String JSON_PROPERTY_END = "end";
    private String end;

    public Ipv6PrefixRange()
    {
    }

    public Ipv6PrefixRange start(String start)
    {

        this.start = start;
        return this;
    }

    /**
     * Get start
     * 
     * @return start
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getStart()
    {
        return start;
    }

    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStart(String start)
    {
        this.start = start;
    }

    public Ipv6PrefixRange end(String end)
    {

        this.end = end;
        return this;
    }

    /**
     * Get end
     * 
     * @return end
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_END)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getEnd()
    {
        return end;
    }

    @JsonProperty(JSON_PROPERTY_END)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnd(String end)
    {
        this.end = end;
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
        Ipv6PrefixRange ipv6PrefixRange = (Ipv6PrefixRange) o;
        return Objects.equals(this.start, ipv6PrefixRange.start) && Objects.equals(this.end, ipv6PrefixRange.end);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(start, end);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Ipv6PrefixRange {\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
        sb.append("    end: ").append(toIndentedString(end)).append("\n");
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
