/*
 * SLF Discovery Service
 * SLF Discovery Service. © 2020, Ericsson GmbH. All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.sc.slf.model.nslf_discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * SearchResult
 */
@JsonPropertyOrder({ SearchResult.JSON_PROPERTY_ADDRESSES })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-05-26T16:51:41.524672+02:00[Europe/Berlin]")
public class SearchResult
{
    public static final String JSON_PROPERTY_ADDRESSES = "addresses";
    private List<Address> addresses = new ArrayList<>();

    public SearchResult addresses(List<Address> addresses)
    {
        this.addresses = addresses;
        return this;
    }

    public SearchResult addAddressesItem(Address addressesItem)
    {
        this.addresses.add(addressesItem);
        return this;
    }

    /**
     * Get addresses
     * 
     * @return addresses
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ADDRESSES)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Address> getAddresses()
    {
        return addresses;
    }

    public void setAddresses(List<Address> addresses)
    {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(java.lang.Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SearchResult searchResult = (SearchResult) o;
        return Objects.equals(this.addresses, searchResult.addresses);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(addresses);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SearchResult {\n");
        sb.append("    addresses: ").append(toIndentedString(addresses)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o)
    {
        if (o == null)
        {
            return "null";
        }

        return o.toString().replace("\n", "\n    ");
    }

}
