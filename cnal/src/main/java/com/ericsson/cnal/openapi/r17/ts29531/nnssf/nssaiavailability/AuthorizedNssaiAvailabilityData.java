/*
 * NSSF NSSAI Availability
 * NSSF NSSAI Availability Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29531.nnssf.nssaiavailability;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Tai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.TaiRange;
import com.ericsson.cnal.openapi.r17.ts29531.nnssf.nsselection.NsagInfo;
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
 * This contains the Nssai availability data information per TA authorized by
 * the NSSF
 */
@ApiModel(description = "This contains the Nssai availability data information per TA authorized by the NSSF")
@JsonPropertyOrder({ AuthorizedNssaiAvailabilityData.JSON_PROPERTY_TAI,
                     AuthorizedNssaiAvailabilityData.JSON_PROPERTY_SUPPORTED_SNSSAI_LIST,
                     AuthorizedNssaiAvailabilityData.JSON_PROPERTY_RESTRICTED_SNSSAI_LIST,
                     AuthorizedNssaiAvailabilityData.JSON_PROPERTY_TAI_LIST,
                     AuthorizedNssaiAvailabilityData.JSON_PROPERTY_TAI_RANGE_LIST,
                     AuthorizedNssaiAvailabilityData.JSON_PROPERTY_NSAG_INFOS })
public class AuthorizedNssaiAvailabilityData
{
    public static final String JSON_PROPERTY_TAI = "tai";
    private Tai tai;

    public static final String JSON_PROPERTY_SUPPORTED_SNSSAI_LIST = "supportedSnssaiList";
    private List<ExtSnssai> supportedSnssaiList = new ArrayList<>();

    public static final String JSON_PROPERTY_RESTRICTED_SNSSAI_LIST = "restrictedSnssaiList";
    private List<RestrictedSnssai> restrictedSnssaiList = null;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = null;

    public static final String JSON_PROPERTY_TAI_RANGE_LIST = "taiRangeList";
    private List<TaiRange> taiRangeList = null;

    public static final String JSON_PROPERTY_NSAG_INFOS = "nsagInfos";
    private List<NsagInfo> nsagInfos = null;

    public AuthorizedNssaiAvailabilityData()
    {
    }

    public AuthorizedNssaiAvailabilityData tai(Tai tai)
    {

        this.tai = tai;
        return this;
    }

    /**
     * Get tai
     * 
     * @return tai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Tai getTai()
    {
        return tai;
    }

    @JsonProperty(JSON_PROPERTY_TAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTai(Tai tai)
    {
        this.tai = tai;
    }

    public AuthorizedNssaiAvailabilityData supportedSnssaiList(List<ExtSnssai> supportedSnssaiList)
    {

        this.supportedSnssaiList = supportedSnssaiList;
        return this;
    }

    public AuthorizedNssaiAvailabilityData addSupportedSnssaiListItem(ExtSnssai supportedSnssaiListItem)
    {
        this.supportedSnssaiList.add(supportedSnssaiListItem);
        return this;
    }

    /**
     * Get supportedSnssaiList
     * 
     * @return supportedSnssaiList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<ExtSnssai> getSupportedSnssaiList()
    {
        return supportedSnssaiList;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSupportedSnssaiList(List<ExtSnssai> supportedSnssaiList)
    {
        this.supportedSnssaiList = supportedSnssaiList;
    }

    public AuthorizedNssaiAvailabilityData restrictedSnssaiList(List<RestrictedSnssai> restrictedSnssaiList)
    {

        this.restrictedSnssaiList = restrictedSnssaiList;
        return this;
    }

    public AuthorizedNssaiAvailabilityData addRestrictedSnssaiListItem(RestrictedSnssai restrictedSnssaiListItem)
    {
        if (this.restrictedSnssaiList == null)
        {
            this.restrictedSnssaiList = new ArrayList<>();
        }
        this.restrictedSnssaiList.add(restrictedSnssaiListItem);
        return this;
    }

    /**
     * Get restrictedSnssaiList
     * 
     * @return restrictedSnssaiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESTRICTED_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RestrictedSnssai> getRestrictedSnssaiList()
    {
        return restrictedSnssaiList;
    }

    @JsonProperty(JSON_PROPERTY_RESTRICTED_SNSSAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRestrictedSnssaiList(List<RestrictedSnssai> restrictedSnssaiList)
    {
        this.restrictedSnssaiList = restrictedSnssaiList;
    }

    public AuthorizedNssaiAvailabilityData taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public AuthorizedNssaiAvailabilityData addTaiListItem(Tai taiListItem)
    {
        if (this.taiList == null)
        {
            this.taiList = new ArrayList<>();
        }
        this.taiList.add(taiListItem);
        return this;
    }

    /**
     * Get taiList
     * 
     * @return taiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Tai> getTaiList()
    {
        return taiList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiList(List<Tai> taiList)
    {
        this.taiList = taiList;
    }

    public AuthorizedNssaiAvailabilityData taiRangeList(List<TaiRange> taiRangeList)
    {

        this.taiRangeList = taiRangeList;
        return this;
    }

    public AuthorizedNssaiAvailabilityData addTaiRangeListItem(TaiRange taiRangeListItem)
    {
        if (this.taiRangeList == null)
        {
            this.taiRangeList = new ArrayList<>();
        }
        this.taiRangeList.add(taiRangeListItem);
        return this;
    }

    /**
     * Get taiRangeList
     * 
     * @return taiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TaiRange> getTaiRangeList()
    {
        return taiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiRangeList(List<TaiRange> taiRangeList)
    {
        this.taiRangeList = taiRangeList;
    }

    public AuthorizedNssaiAvailabilityData nsagInfos(List<NsagInfo> nsagInfos)
    {

        this.nsagInfos = nsagInfos;
        return this;
    }

    public AuthorizedNssaiAvailabilityData addNsagInfosItem(NsagInfo nsagInfosItem)
    {
        if (this.nsagInfos == null)
        {
            this.nsagInfos = new ArrayList<>();
        }
        this.nsagInfos.add(nsagInfosItem);
        return this;
    }

    /**
     * Get nsagInfos
     * 
     * @return nsagInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NSAG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NsagInfo> getNsagInfos()
    {
        return nsagInfos;
    }

    @JsonProperty(JSON_PROPERTY_NSAG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNsagInfos(List<NsagInfo> nsagInfos)
    {
        this.nsagInfos = nsagInfos;
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
        AuthorizedNssaiAvailabilityData authorizedNssaiAvailabilityData = (AuthorizedNssaiAvailabilityData) o;
        return Objects.equals(this.tai, authorizedNssaiAvailabilityData.tai)
               && Objects.equals(this.supportedSnssaiList, authorizedNssaiAvailabilityData.supportedSnssaiList)
               && Objects.equals(this.restrictedSnssaiList, authorizedNssaiAvailabilityData.restrictedSnssaiList)
               && Objects.equals(this.taiList, authorizedNssaiAvailabilityData.taiList)
               && Objects.equals(this.taiRangeList, authorizedNssaiAvailabilityData.taiRangeList)
               && Objects.equals(this.nsagInfos, authorizedNssaiAvailabilityData.nsagInfos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(tai, supportedSnssaiList, restrictedSnssaiList, taiList, taiRangeList, nsagInfos);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizedNssaiAvailabilityData {\n");
        sb.append("    tai: ").append(toIndentedString(tai)).append("\n");
        sb.append("    supportedSnssaiList: ").append(toIndentedString(supportedSnssaiList)).append("\n");
        sb.append("    restrictedSnssaiList: ").append(toIndentedString(restrictedSnssaiList)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    taiRangeList: ").append(toIndentedString(taiRangeList)).append("\n");
        sb.append("    nsagInfos: ").append(toIndentedString(nsagInfos)).append("\n");
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
