/*
 * Nudm_SDM
 * Nudm Subscriber Data Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.SliceMbrRm;
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
 * AdditionalSnssaiData
 */
@JsonPropertyOrder({ AdditionalSnssaiData.JSON_PROPERTY_REQUIRED_AUTHN_AUTHZ,
                     AdditionalSnssaiData.JSON_PROPERTY_SUBSCRIBED_UE_SLICE_MBR,
                     AdditionalSnssaiData.JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST })
public class AdditionalSnssaiData
{
    public static final String JSON_PROPERTY_REQUIRED_AUTHN_AUTHZ = "requiredAuthnAuthz";
    private Boolean requiredAuthnAuthz;

    public static final String JSON_PROPERTY_SUBSCRIBED_UE_SLICE_MBR = "subscribedUeSliceMbr";
    private SliceMbrRm subscribedUeSliceMbr;

    public static final String JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST = "subscribedNsSrgList";
    private List<String> subscribedNsSrgList = null;

    public AdditionalSnssaiData()
    {
    }

    public AdditionalSnssaiData requiredAuthnAuthz(Boolean requiredAuthnAuthz)
    {

        this.requiredAuthnAuthz = requiredAuthnAuthz;
        return this;
    }

    /**
     * Get requiredAuthnAuthz
     * 
     * @return requiredAuthnAuthz
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQUIRED_AUTHN_AUTHZ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRequiredAuthnAuthz()
    {
        return requiredAuthnAuthz;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRED_AUTHN_AUTHZ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiredAuthnAuthz(Boolean requiredAuthnAuthz)
    {
        this.requiredAuthnAuthz = requiredAuthnAuthz;
    }

    public AdditionalSnssaiData subscribedUeSliceMbr(SliceMbrRm subscribedUeSliceMbr)
    {

        this.subscribedUeSliceMbr = subscribedUeSliceMbr;
        return this;
    }

    /**
     * Get subscribedUeSliceMbr
     * 
     * @return subscribedUeSliceMbr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_UE_SLICE_MBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SliceMbrRm getSubscribedUeSliceMbr()
    {
        return subscribedUeSliceMbr;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_UE_SLICE_MBR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribedUeSliceMbr(SliceMbrRm subscribedUeSliceMbr)
    {
        this.subscribedUeSliceMbr = subscribedUeSliceMbr;
    }

    public AdditionalSnssaiData subscribedNsSrgList(List<String> subscribedNsSrgList)
    {

        this.subscribedNsSrgList = subscribedNsSrgList;
        return this;
    }

    public AdditionalSnssaiData addSubscribedNsSrgListItem(String subscribedNsSrgListItem)
    {
        if (this.subscribedNsSrgList == null)
        {
            this.subscribedNsSrgList = new ArrayList<>();
        }
        this.subscribedNsSrgList.add(subscribedNsSrgListItem);
        return this;
    }

    /**
     * Get subscribedNsSrgList
     * 
     * @return subscribedNsSrgList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSubscribedNsSrgList()
    {
        return subscribedNsSrgList;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribedNsSrgList(List<String> subscribedNsSrgList)
    {
        this.subscribedNsSrgList = subscribedNsSrgList;
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
        AdditionalSnssaiData additionalSnssaiData = (AdditionalSnssaiData) o;
        return Objects.equals(this.requiredAuthnAuthz, additionalSnssaiData.requiredAuthnAuthz)
               && Objects.equals(this.subscribedUeSliceMbr, additionalSnssaiData.subscribedUeSliceMbr)
               && Objects.equals(this.subscribedNsSrgList, additionalSnssaiData.subscribedNsSrgList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(requiredAuthnAuthz, subscribedUeSliceMbr, subscribedNsSrgList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdditionalSnssaiData {\n");
        sb.append("    requiredAuthnAuthz: ").append(toIndentedString(requiredAuthnAuthz)).append("\n");
        sb.append("    subscribedUeSliceMbr: ").append(toIndentedString(subscribedUeSliceMbr)).append("\n");
        sb.append("    subscribedNsSrgList: ").append(toIndentedString(subscribedNsSrgList)).append("\n");
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
