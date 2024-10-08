/*
 * Unified Data Repository Service API file for Application Data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.application.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29551.nnef.pfdmanagement.PfdChangeNotification;
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
 * Contains changed application data for which notification was requested.
 */
@ApiModel(description = "Contains changed application data for which notification was requested.")
@JsonPropertyOrder({ ApplicationDataChangeNotif.JSON_PROPERTY_IPTV_CONFIG_DATA,
                     ApplicationDataChangeNotif.JSON_PROPERTY_PFD_DATA,
                     ApplicationDataChangeNotif.JSON_PROPERTY_BDT_POLICY_DATA,
                     ApplicationDataChangeNotif.JSON_PROPERTY_RES_URI,
                     ApplicationDataChangeNotif.JSON_PROPERTY_SER_PARAM_DATA,
                     ApplicationDataChangeNotif.JSON_PROPERTY_AM_INFLU_DATA })
public class ApplicationDataChangeNotif
{
    public static final String JSON_PROPERTY_IPTV_CONFIG_DATA = "iptvConfigData";
    private IptvConfigData iptvConfigData;

    public static final String JSON_PROPERTY_PFD_DATA = "pfdData";
    private PfdChangeNotification pfdData;

    public static final String JSON_PROPERTY_BDT_POLICY_DATA = "bdtPolicyData";
    private BdtPolicyData bdtPolicyData;

    public static final String JSON_PROPERTY_RES_URI = "resUri";
    private String resUri;

    public static final String JSON_PROPERTY_SER_PARAM_DATA = "serParamData";
    private ServiceParameterData serParamData;

    public static final String JSON_PROPERTY_AM_INFLU_DATA = "amInfluData";
    private AmInfluData amInfluData;

    public ApplicationDataChangeNotif()
    {
    }

    public ApplicationDataChangeNotif iptvConfigData(IptvConfigData iptvConfigData)
    {

        this.iptvConfigData = iptvConfigData;
        return this;
    }

    /**
     * Get iptvConfigData
     * 
     * @return iptvConfigData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPTV_CONFIG_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IptvConfigData getIptvConfigData()
    {
        return iptvConfigData;
    }

    @JsonProperty(JSON_PROPERTY_IPTV_CONFIG_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIptvConfigData(IptvConfigData iptvConfigData)
    {
        this.iptvConfigData = iptvConfigData;
    }

    public ApplicationDataChangeNotif pfdData(PfdChangeNotification pfdData)
    {

        this.pfdData = pfdData;
        return this;
    }

    /**
     * Get pfdData
     * 
     * @return pfdData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PFD_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PfdChangeNotification getPfdData()
    {
        return pfdData;
    }

    @JsonProperty(JSON_PROPERTY_PFD_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPfdData(PfdChangeNotification pfdData)
    {
        this.pfdData = pfdData;
    }

    public ApplicationDataChangeNotif bdtPolicyData(BdtPolicyData bdtPolicyData)
    {

        this.bdtPolicyData = bdtPolicyData;
        return this;
    }

    /**
     * Get bdtPolicyData
     * 
     * @return bdtPolicyData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BDT_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BdtPolicyData getBdtPolicyData()
    {
        return bdtPolicyData;
    }

    @JsonProperty(JSON_PROPERTY_BDT_POLICY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtPolicyData(BdtPolicyData bdtPolicyData)
    {
        this.bdtPolicyData = bdtPolicyData;
    }

    public ApplicationDataChangeNotif resUri(String resUri)
    {

        this.resUri = resUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return resUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_RES_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getResUri()
    {
        return resUri;
    }

    @JsonProperty(JSON_PROPERTY_RES_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setResUri(String resUri)
    {
        this.resUri = resUri;
    }

    public ApplicationDataChangeNotif serParamData(ServiceParameterData serParamData)
    {

        this.serParamData = serParamData;
        return this;
    }

    /**
     * Get serParamData
     * 
     * @return serParamData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SER_PARAM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ServiceParameterData getSerParamData()
    {
        return serParamData;
    }

    @JsonProperty(JSON_PROPERTY_SER_PARAM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSerParamData(ServiceParameterData serParamData)
    {
        this.serParamData = serParamData;
    }

    public ApplicationDataChangeNotif amInfluData(AmInfluData amInfluData)
    {

        this.amInfluData = amInfluData;
        return this;
    }

    /**
     * Get amInfluData
     * 
     * @return amInfluData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AM_INFLU_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmInfluData getAmInfluData()
    {
        return amInfluData;
    }

    @JsonProperty(JSON_PROPERTY_AM_INFLU_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmInfluData(AmInfluData amInfluData)
    {
        this.amInfluData = amInfluData;
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
        ApplicationDataChangeNotif applicationDataChangeNotif = (ApplicationDataChangeNotif) o;
        return Objects.equals(this.iptvConfigData, applicationDataChangeNotif.iptvConfigData)
               && Objects.equals(this.pfdData, applicationDataChangeNotif.pfdData)
               && Objects.equals(this.bdtPolicyData, applicationDataChangeNotif.bdtPolicyData) && Objects.equals(this.resUri, applicationDataChangeNotif.resUri)
               && Objects.equals(this.serParamData, applicationDataChangeNotif.serParamData)
               && Objects.equals(this.amInfluData, applicationDataChangeNotif.amInfluData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(iptvConfigData, pfdData, bdtPolicyData, resUri, serParamData, amInfluData);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationDataChangeNotif {\n");
        sb.append("    iptvConfigData: ").append(toIndentedString(iptvConfigData)).append("\n");
        sb.append("    pfdData: ").append(toIndentedString(pfdData)).append("\n");
        sb.append("    bdtPolicyData: ").append(toIndentedString(bdtPolicyData)).append("\n");
        sb.append("    resUri: ").append(toIndentedString(resUri)).append("\n");
        sb.append("    serParamData: ").append(toIndentedString(serParamData)).append("\n");
        sb.append("    amInfluData: ").append(toIndentedString(amInfluData)).append("\n");
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
