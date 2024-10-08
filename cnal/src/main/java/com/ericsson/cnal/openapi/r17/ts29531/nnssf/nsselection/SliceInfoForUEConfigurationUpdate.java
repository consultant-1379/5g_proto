/*
 * NSSF NS Selection
 * NSSF Network Slice Selection Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29531.nnssf.nsselection;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
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
 * Contains the slice information requested during UE configuration update
 * procedure
 */
@ApiModel(description = "Contains the slice information requested during UE configuration update procedure")
@JsonPropertyOrder({ SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_SUBSCRIBED_NSSAI,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_ALLOWED_NSSAI_CURRENT_ACCESS,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_ALLOWED_NSSAI_OTHER_ACCESS,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_DEFAULT_CONFIGURED_SNSSAI_IND,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_REQUESTED_NSSAI,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_MAPPING_OF_NSSAI,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_UE_SUP_NSSRG_IND,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_SUPPRESS_NSSRG_IND,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_REJECTED_NSSAI_RA,
                     SliceInfoForUEConfigurationUpdate.JSON_PROPERTY_NSAG_SUPPORTED })
public class SliceInfoForUEConfigurationUpdate
{
    public static final String JSON_PROPERTY_SUBSCRIBED_NSSAI = "subscribedNssai";
    private List<SubscribedSnssai> subscribedNssai = null;

    public static final String JSON_PROPERTY_ALLOWED_NSSAI_CURRENT_ACCESS = "allowedNssaiCurrentAccess";
    private AllowedNssai allowedNssaiCurrentAccess;

    public static final String JSON_PROPERTY_ALLOWED_NSSAI_OTHER_ACCESS = "allowedNssaiOtherAccess";
    private AllowedNssai allowedNssaiOtherAccess;

    public static final String JSON_PROPERTY_DEFAULT_CONFIGURED_SNSSAI_IND = "defaultConfiguredSnssaiInd";
    private Boolean defaultConfiguredSnssaiInd;

    public static final String JSON_PROPERTY_REQUESTED_NSSAI = "requestedNssai";
    private List<Snssai> requestedNssai = null;

    public static final String JSON_PROPERTY_MAPPING_OF_NSSAI = "mappingOfNssai";
    private List<MappingOfSnssai> mappingOfNssai = null;

    public static final String JSON_PROPERTY_UE_SUP_NSSRG_IND = "ueSupNssrgInd";
    private Boolean ueSupNssrgInd;

    public static final String JSON_PROPERTY_SUPPRESS_NSSRG_IND = "suppressNssrgInd";
    private Boolean suppressNssrgInd;

    public static final String JSON_PROPERTY_REJECTED_NSSAI_RA = "rejectedNssaiRa";
    private List<Snssai> rejectedNssaiRa = null;

    public static final String JSON_PROPERTY_NSAG_SUPPORTED = "nsagSupported";
    private Boolean nsagSupported = false;

    public SliceInfoForUEConfigurationUpdate()
    {
    }

    public SliceInfoForUEConfigurationUpdate subscribedNssai(List<SubscribedSnssai> subscribedNssai)
    {

        this.subscribedNssai = subscribedNssai;
        return this;
    }

    public SliceInfoForUEConfigurationUpdate addSubscribedNssaiItem(SubscribedSnssai subscribedNssaiItem)
    {
        if (this.subscribedNssai == null)
        {
            this.subscribedNssai = new ArrayList<>();
        }
        this.subscribedNssai.add(subscribedNssaiItem);
        return this;
    }

    /**
     * Get subscribedNssai
     * 
     * @return subscribedNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SubscribedSnssai> getSubscribedNssai()
    {
        return subscribedNssai;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribedNssai(List<SubscribedSnssai> subscribedNssai)
    {
        this.subscribedNssai = subscribedNssai;
    }

    public SliceInfoForUEConfigurationUpdate allowedNssaiCurrentAccess(AllowedNssai allowedNssaiCurrentAccess)
    {

        this.allowedNssaiCurrentAccess = allowedNssaiCurrentAccess;
        return this;
    }

    /**
     * Get allowedNssaiCurrentAccess
     * 
     * @return allowedNssaiCurrentAccess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ALLOWED_NSSAI_CURRENT_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AllowedNssai getAllowedNssaiCurrentAccess()
    {
        return allowedNssaiCurrentAccess;
    }

    @JsonProperty(JSON_PROPERTY_ALLOWED_NSSAI_CURRENT_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowedNssaiCurrentAccess(AllowedNssai allowedNssaiCurrentAccess)
    {
        this.allowedNssaiCurrentAccess = allowedNssaiCurrentAccess;
    }

    public SliceInfoForUEConfigurationUpdate allowedNssaiOtherAccess(AllowedNssai allowedNssaiOtherAccess)
    {

        this.allowedNssaiOtherAccess = allowedNssaiOtherAccess;
        return this;
    }

    /**
     * Get allowedNssaiOtherAccess
     * 
     * @return allowedNssaiOtherAccess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ALLOWED_NSSAI_OTHER_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AllowedNssai getAllowedNssaiOtherAccess()
    {
        return allowedNssaiOtherAccess;
    }

    @JsonProperty(JSON_PROPERTY_ALLOWED_NSSAI_OTHER_ACCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowedNssaiOtherAccess(AllowedNssai allowedNssaiOtherAccess)
    {
        this.allowedNssaiOtherAccess = allowedNssaiOtherAccess;
    }

    public SliceInfoForUEConfigurationUpdate defaultConfiguredSnssaiInd(Boolean defaultConfiguredSnssaiInd)
    {

        this.defaultConfiguredSnssaiInd = defaultConfiguredSnssaiInd;
        return this;
    }

    /**
     * Get defaultConfiguredSnssaiInd
     * 
     * @return defaultConfiguredSnssaiInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DEFAULT_CONFIGURED_SNSSAI_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDefaultConfiguredSnssaiInd()
    {
        return defaultConfiguredSnssaiInd;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_CONFIGURED_SNSSAI_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultConfiguredSnssaiInd(Boolean defaultConfiguredSnssaiInd)
    {
        this.defaultConfiguredSnssaiInd = defaultConfiguredSnssaiInd;
    }

    public SliceInfoForUEConfigurationUpdate requestedNssai(List<Snssai> requestedNssai)
    {

        this.requestedNssai = requestedNssai;
        return this;
    }

    public SliceInfoForUEConfigurationUpdate addRequestedNssaiItem(Snssai requestedNssaiItem)
    {
        if (this.requestedNssai == null)
        {
            this.requestedNssai = new ArrayList<>();
        }
        this.requestedNssai.add(requestedNssaiItem);
        return this;
    }

    /**
     * Get requestedNssai
     * 
     * @return requestedNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQUESTED_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getRequestedNssai()
    {
        return requestedNssai;
    }

    @JsonProperty(JSON_PROPERTY_REQUESTED_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestedNssai(List<Snssai> requestedNssai)
    {
        this.requestedNssai = requestedNssai;
    }

    public SliceInfoForUEConfigurationUpdate mappingOfNssai(List<MappingOfSnssai> mappingOfNssai)
    {

        this.mappingOfNssai = mappingOfNssai;
        return this;
    }

    public SliceInfoForUEConfigurationUpdate addMappingOfNssaiItem(MappingOfSnssai mappingOfNssaiItem)
    {
        if (this.mappingOfNssai == null)
        {
            this.mappingOfNssai = new ArrayList<>();
        }
        this.mappingOfNssai.add(mappingOfNssaiItem);
        return this;
    }

    /**
     * Get mappingOfNssai
     * 
     * @return mappingOfNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MAPPING_OF_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MappingOfSnssai> getMappingOfNssai()
    {
        return mappingOfNssai;
    }

    @JsonProperty(JSON_PROPERTY_MAPPING_OF_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMappingOfNssai(List<MappingOfSnssai> mappingOfNssai)
    {
        this.mappingOfNssai = mappingOfNssai;
    }

    public SliceInfoForUEConfigurationUpdate ueSupNssrgInd(Boolean ueSupNssrgInd)
    {

        this.ueSupNssrgInd = ueSupNssrgInd;
        return this;
    }

    /**
     * Get ueSupNssrgInd
     * 
     * @return ueSupNssrgInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_SUP_NSSRG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getUeSupNssrgInd()
    {
        return ueSupNssrgInd;
    }

    @JsonProperty(JSON_PROPERTY_UE_SUP_NSSRG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeSupNssrgInd(Boolean ueSupNssrgInd)
    {
        this.ueSupNssrgInd = ueSupNssrgInd;
    }

    public SliceInfoForUEConfigurationUpdate suppressNssrgInd(Boolean suppressNssrgInd)
    {

        this.suppressNssrgInd = suppressNssrgInd;
        return this;
    }

    /**
     * Get suppressNssrgInd
     * 
     * @return suppressNssrgInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPPRESS_NSSRG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSuppressNssrgInd()
    {
        return suppressNssrgInd;
    }

    @JsonProperty(JSON_PROPERTY_SUPPRESS_NSSRG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuppressNssrgInd(Boolean suppressNssrgInd)
    {
        this.suppressNssrgInd = suppressNssrgInd;
    }

    public SliceInfoForUEConfigurationUpdate rejectedNssaiRa(List<Snssai> rejectedNssaiRa)
    {

        this.rejectedNssaiRa = rejectedNssaiRa;
        return this;
    }

    public SliceInfoForUEConfigurationUpdate addRejectedNssaiRaItem(Snssai rejectedNssaiRaItem)
    {
        if (this.rejectedNssaiRa == null)
        {
            this.rejectedNssaiRa = new ArrayList<>();
        }
        this.rejectedNssaiRa.add(rejectedNssaiRaItem);
        return this;
    }

    /**
     * Get rejectedNssaiRa
     * 
     * @return rejectedNssaiRa
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REJECTED_NSSAI_RA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getRejectedNssaiRa()
    {
        return rejectedNssaiRa;
    }

    @JsonProperty(JSON_PROPERTY_REJECTED_NSSAI_RA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRejectedNssaiRa(List<Snssai> rejectedNssaiRa)
    {
        this.rejectedNssaiRa = rejectedNssaiRa;
    }

    public SliceInfoForUEConfigurationUpdate nsagSupported(Boolean nsagSupported)
    {

        this.nsagSupported = nsagSupported;
        return this;
    }

    /**
     * Get nsagSupported
     * 
     * @return nsagSupported
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NSAG_SUPPORTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getNsagSupported()
    {
        return nsagSupported;
    }

    @JsonProperty(JSON_PROPERTY_NSAG_SUPPORTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNsagSupported(Boolean nsagSupported)
    {
        this.nsagSupported = nsagSupported;
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
        SliceInfoForUEConfigurationUpdate sliceInfoForUEConfigurationUpdate = (SliceInfoForUEConfigurationUpdate) o;
        return Objects.equals(this.subscribedNssai, sliceInfoForUEConfigurationUpdate.subscribedNssai)
               && Objects.equals(this.allowedNssaiCurrentAccess, sliceInfoForUEConfigurationUpdate.allowedNssaiCurrentAccess)
               && Objects.equals(this.allowedNssaiOtherAccess, sliceInfoForUEConfigurationUpdate.allowedNssaiOtherAccess)
               && Objects.equals(this.defaultConfiguredSnssaiInd, sliceInfoForUEConfigurationUpdate.defaultConfiguredSnssaiInd)
               && Objects.equals(this.requestedNssai, sliceInfoForUEConfigurationUpdate.requestedNssai)
               && Objects.equals(this.mappingOfNssai, sliceInfoForUEConfigurationUpdate.mappingOfNssai)
               && Objects.equals(this.ueSupNssrgInd, sliceInfoForUEConfigurationUpdate.ueSupNssrgInd)
               && Objects.equals(this.suppressNssrgInd, sliceInfoForUEConfigurationUpdate.suppressNssrgInd)
               && Objects.equals(this.rejectedNssaiRa, sliceInfoForUEConfigurationUpdate.rejectedNssaiRa)
               && Objects.equals(this.nsagSupported, sliceInfoForUEConfigurationUpdate.nsagSupported);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(subscribedNssai,
                            allowedNssaiCurrentAccess,
                            allowedNssaiOtherAccess,
                            defaultConfiguredSnssaiInd,
                            requestedNssai,
                            mappingOfNssai,
                            ueSupNssrgInd,
                            suppressNssrgInd,
                            rejectedNssaiRa,
                            nsagSupported);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SliceInfoForUEConfigurationUpdate {\n");
        sb.append("    subscribedNssai: ").append(toIndentedString(subscribedNssai)).append("\n");
        sb.append("    allowedNssaiCurrentAccess: ").append(toIndentedString(allowedNssaiCurrentAccess)).append("\n");
        sb.append("    allowedNssaiOtherAccess: ").append(toIndentedString(allowedNssaiOtherAccess)).append("\n");
        sb.append("    defaultConfiguredSnssaiInd: ").append(toIndentedString(defaultConfiguredSnssaiInd)).append("\n");
        sb.append("    requestedNssai: ").append(toIndentedString(requestedNssai)).append("\n");
        sb.append("    mappingOfNssai: ").append(toIndentedString(mappingOfNssai)).append("\n");
        sb.append("    ueSupNssrgInd: ").append(toIndentedString(ueSupNssrgInd)).append("\n");
        sb.append("    suppressNssrgInd: ").append(toIndentedString(suppressNssrgInd)).append("\n");
        sb.append("    rejectedNssaiRa: ").append(toIndentedString(rejectedNssaiRa)).append("\n");
        sb.append("    nsagSupported: ").append(toIndentedString(nsagSupported)).append("\n");
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
