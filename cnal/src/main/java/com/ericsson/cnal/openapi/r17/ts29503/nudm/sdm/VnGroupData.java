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
 * VnGroupData
 */
@JsonPropertyOrder({ VnGroupData.JSON_PROPERTY_PDU_SESSION_TYPES,
                     VnGroupData.JSON_PROPERTY_DNN,
                     VnGroupData.JSON_PROPERTY_SINGLE_NSSAI,
                     VnGroupData.JSON_PROPERTY_APP_DESCRIPTORS })
public class VnGroupData
{
    public static final String JSON_PROPERTY_PDU_SESSION_TYPES = "pduSessionTypes";
    private PduSessionTypes pduSessionTypes;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_SINGLE_NSSAI = "singleNssai";
    private Snssai singleNssai;

    public static final String JSON_PROPERTY_APP_DESCRIPTORS = "appDescriptors";
    private List<AppDescriptor> appDescriptors = null;

    public VnGroupData()
    {
    }

    public VnGroupData pduSessionTypes(PduSessionTypes pduSessionTypes)
    {

        this.pduSessionTypes = pduSessionTypes;
        return this;
    }

    /**
     * Get pduSessionTypes
     * 
     * @return pduSessionTypes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PduSessionTypes getPduSessionTypes()
    {
        return pduSessionTypes;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPduSessionTypes(PduSessionTypes pduSessionTypes)
    {
        this.pduSessionTypes = pduSessionTypes;
    }

    public VnGroupData dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;
     * it shall contain either a DNN Network Identifier, or a full DNN with both the
     * Network Identifier and Operator Identifier, as specified in 3GPP TS 23.003
     * clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are
     * separated by dots (e.g. \&quot;Label1.Label2.Label3\&quot;).
     * 
     * @return dnn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public VnGroupData singleNssai(Snssai singleNssai)
    {

        this.singleNssai = singleNssai;
        return this;
    }

    /**
     * Get singleNssai
     * 
     * @return singleNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SINGLE_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getSingleNssai()
    {
        return singleNssai;
    }

    @JsonProperty(JSON_PROPERTY_SINGLE_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSingleNssai(Snssai singleNssai)
    {
        this.singleNssai = singleNssai;
    }

    public VnGroupData appDescriptors(List<AppDescriptor> appDescriptors)
    {

        this.appDescriptors = appDescriptors;
        return this;
    }

    public VnGroupData addAppDescriptorsItem(AppDescriptor appDescriptorsItem)
    {
        if (this.appDescriptors == null)
        {
            this.appDescriptors = new ArrayList<>();
        }
        this.appDescriptors.add(appDescriptorsItem);
        return this;
    }

    /**
     * Get appDescriptors
     * 
     * @return appDescriptors
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_APP_DESCRIPTORS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AppDescriptor> getAppDescriptors()
    {
        return appDescriptors;
    }

    @JsonProperty(JSON_PROPERTY_APP_DESCRIPTORS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppDescriptors(List<AppDescriptor> appDescriptors)
    {
        this.appDescriptors = appDescriptors;
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
        VnGroupData vnGroupData = (VnGroupData) o;
        return Objects.equals(this.pduSessionTypes, vnGroupData.pduSessionTypes) && Objects.equals(this.dnn, vnGroupData.dnn)
               && Objects.equals(this.singleNssai, vnGroupData.singleNssai) && Objects.equals(this.appDescriptors, vnGroupData.appDescriptors);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pduSessionTypes, dnn, singleNssai, appDescriptors);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class VnGroupData {\n");
        sb.append("    pduSessionTypes: ").append(toIndentedString(pduSessionTypes)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    singleNssai: ").append(toIndentedString(singleNssai)).append("\n");
        sb.append("    appDescriptors: ").append(toIndentedString(appDescriptors)).append("\n");
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
