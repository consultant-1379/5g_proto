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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This data type is defined in the same way as the &#39; EcsServerAddr &#39;
 * data type, but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the ' EcsServerAddr ' data type, but with the OpenAPI 'nullable: true' property. ")
@JsonPropertyOrder({ EcsServerAddrRm.JSON_PROPERTY_ECS_FQDN_LIST,
                     EcsServerAddrRm.JSON_PROPERTY_ECS_IP_ADDRESS_LIST,
                     EcsServerAddrRm.JSON_PROPERTY_ECS_URI_LIST,
                     EcsServerAddrRm.JSON_PROPERTY_ECS_PROVIDER_ID })
public class EcsServerAddrRm
{
    public static final String JSON_PROPERTY_ECS_FQDN_LIST = "ecsFqdnList";
    private List<String> ecsFqdnList = null;

    public static final String JSON_PROPERTY_ECS_IP_ADDRESS_LIST = "ecsIpAddressList";
    private List<IpAddr> ecsIpAddressList = null;

    public static final String JSON_PROPERTY_ECS_URI_LIST = "ecsUriList";
    private List<String> ecsUriList = null;

    public static final String JSON_PROPERTY_ECS_PROVIDER_ID = "ecsProviderId";
    private String ecsProviderId;

    public EcsServerAddrRm()
    {
    }

    public EcsServerAddrRm ecsFqdnList(List<String> ecsFqdnList)
    {

        this.ecsFqdnList = ecsFqdnList;
        return this;
    }

    public EcsServerAddrRm addEcsFqdnListItem(String ecsFqdnListItem)
    {
        if (this.ecsFqdnList == null)
        {
            this.ecsFqdnList = new ArrayList<>();
        }
        this.ecsFqdnList.add(ecsFqdnListItem);
        return this;
    }

    /**
     * Get ecsFqdnList
     * 
     * @return ecsFqdnList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECS_FQDN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEcsFqdnList()
    {
        return ecsFqdnList;
    }

    @JsonProperty(JSON_PROPERTY_ECS_FQDN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcsFqdnList(List<String> ecsFqdnList)
    {
        this.ecsFqdnList = ecsFqdnList;
    }

    public EcsServerAddrRm ecsIpAddressList(List<IpAddr> ecsIpAddressList)
    {

        this.ecsIpAddressList = ecsIpAddressList;
        return this;
    }

    public EcsServerAddrRm addEcsIpAddressListItem(IpAddr ecsIpAddressListItem)
    {
        if (this.ecsIpAddressList == null)
        {
            this.ecsIpAddressList = new ArrayList<>();
        }
        this.ecsIpAddressList.add(ecsIpAddressListItem);
        return this;
    }

    /**
     * Get ecsIpAddressList
     * 
     * @return ecsIpAddressList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECS_IP_ADDRESS_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IpAddr> getEcsIpAddressList()
    {
        return ecsIpAddressList;
    }

    @JsonProperty(JSON_PROPERTY_ECS_IP_ADDRESS_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcsIpAddressList(List<IpAddr> ecsIpAddressList)
    {
        this.ecsIpAddressList = ecsIpAddressList;
    }

    public EcsServerAddrRm ecsUriList(List<String> ecsUriList)
    {

        this.ecsUriList = ecsUriList;
        return this;
    }

    public EcsServerAddrRm addEcsUriListItem(String ecsUriListItem)
    {
        if (this.ecsUriList == null)
        {
            this.ecsUriList = new ArrayList<>();
        }
        this.ecsUriList.add(ecsUriListItem);
        return this;
    }

    /**
     * Get ecsUriList
     * 
     * @return ecsUriList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECS_URI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEcsUriList()
    {
        return ecsUriList;
    }

    @JsonProperty(JSON_PROPERTY_ECS_URI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcsUriList(List<String> ecsUriList)
    {
        this.ecsUriList = ecsUriList;
    }

    public EcsServerAddrRm ecsProviderId(String ecsProviderId)
    {

        this.ecsProviderId = ecsProviderId;
        return this;
    }

    /**
     * Get ecsProviderId
     * 
     * @return ecsProviderId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECS_PROVIDER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getEcsProviderId()
    {
        return ecsProviderId;
    }

    @JsonProperty(JSON_PROPERTY_ECS_PROVIDER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcsProviderId(String ecsProviderId)
    {
        this.ecsProviderId = ecsProviderId;
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
        EcsServerAddrRm ecsServerAddrRm = (EcsServerAddrRm) o;
        return Objects.equals(this.ecsFqdnList, ecsServerAddrRm.ecsFqdnList) && Objects.equals(this.ecsIpAddressList, ecsServerAddrRm.ecsIpAddressList)
               && Objects.equals(this.ecsUriList, ecsServerAddrRm.ecsUriList) && Objects.equals(this.ecsProviderId, ecsServerAddrRm.ecsProviderId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ecsFqdnList, ecsIpAddressList, ecsUriList, ecsProviderId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EcsServerAddrRm {\n");
        sb.append("    ecsFqdnList: ").append(toIndentedString(ecsFqdnList)).append("\n");
        sb.append("    ecsIpAddressList: ").append(toIndentedString(ecsIpAddressList)).append("\n");
        sb.append("    ecsUriList: ").append(toIndentedString(ecsUriList)).append("\n");
        sb.append("    ecsProviderId: ").append(toIndentedString(ecsProviderId)).append("\n");
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
