/*
 * N32 Handshake API
 * N32-c Handshake Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29573.n32.handshake;

import java.util.Objects;
import java.util.Arrays;
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
 * Protection and modification policy for the IE
 */
@ApiModel(description = "Protection and modification policy for the IE")
@JsonPropertyOrder({ IeInfo.JSON_PROPERTY_IE_LOC,
                     IeInfo.JSON_PROPERTY_IE_TYPE,
                     IeInfo.JSON_PROPERTY_REQ_IE,
                     IeInfo.JSON_PROPERTY_RSP_IE,
                     IeInfo.JSON_PROPERTY_IS_MODIFIABLE,
                     IeInfo.JSON_PROPERTY_IS_MODIFIABLE_BY_IPX })
public class IeInfo
{
    public static final String JSON_PROPERTY_IE_LOC = "ieLoc";
    private String ieLoc;

    public static final String JSON_PROPERTY_IE_TYPE = "ieType";
    private String ieType;

    public static final String JSON_PROPERTY_REQ_IE = "reqIe";
    private String reqIe;

    public static final String JSON_PROPERTY_RSP_IE = "rspIe";
    private String rspIe;

    public static final String JSON_PROPERTY_IS_MODIFIABLE = "isModifiable";
    private Boolean isModifiable;

    public static final String JSON_PROPERTY_IS_MODIFIABLE_BY_IPX = "isModifiableByIpx";
    private Map<String, Boolean> isModifiableByIpx = null;

    public IeInfo()
    {
    }

    public IeInfo ieLoc(String ieLoc)
    {

        this.ieLoc = ieLoc;
        return this;
    }

    /**
     * Location of the IE in a HTTP message
     * 
     * @return ieLoc
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Location of the IE in a HTTP message")
    @JsonProperty(JSON_PROPERTY_IE_LOC)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getIeLoc()
    {
        return ieLoc;
    }

    @JsonProperty(JSON_PROPERTY_IE_LOC)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIeLoc(String ieLoc)
    {
        this.ieLoc = ieLoc;
    }

    public IeInfo ieType(String ieType)
    {

        this.ieType = ieType;
        return this;
    }

    /**
     * Enumeration of types of IEs (i.e kind of IE) to specify the protection policy
     * 
     * @return ieType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Enumeration of types of IEs (i.e kind of IE) to specify the protection policy")
    @JsonProperty(JSON_PROPERTY_IE_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getIeType()
    {
        return ieType;
    }

    @JsonProperty(JSON_PROPERTY_IE_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIeType(String ieType)
    {
        this.ieType = ieType;
    }

    public IeInfo reqIe(String reqIe)
    {

        this.reqIe = reqIe;
        return this;
    }

    /**
     * Get reqIe
     * 
     * @return reqIe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQ_IE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReqIe()
    {
        return reqIe;
    }

    @JsonProperty(JSON_PROPERTY_REQ_IE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReqIe(String reqIe)
    {
        this.reqIe = reqIe;
    }

    public IeInfo rspIe(String rspIe)
    {

        this.rspIe = rspIe;
        return this;
    }

    /**
     * Get rspIe
     * 
     * @return rspIe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RSP_IE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRspIe()
    {
        return rspIe;
    }

    @JsonProperty(JSON_PROPERTY_RSP_IE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRspIe(String rspIe)
    {
        this.rspIe = rspIe;
    }

    public IeInfo isModifiable(Boolean isModifiable)
    {

        this.isModifiable = isModifiable;
        return this;
    }

    /**
     * Get isModifiable
     * 
     * @return isModifiable
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IS_MODIFIABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsModifiable()
    {
        return isModifiable;
    }

    @JsonProperty(JSON_PROPERTY_IS_MODIFIABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsModifiable(Boolean isModifiable)
    {
        this.isModifiable = isModifiable;
    }

    public IeInfo isModifiableByIpx(Map<String, Boolean> isModifiableByIpx)
    {

        this.isModifiableByIpx = isModifiableByIpx;
        return this;
    }

    public IeInfo putIsModifiableByIpxItem(String key,
                                           Boolean isModifiableByIpxItem)
    {
        if (this.isModifiableByIpx == null)
        {
            this.isModifiableByIpx = new HashMap<>();
        }
        this.isModifiableByIpx.put(key, isModifiableByIpxItem);
        return this;
    }

    /**
     * Get isModifiableByIpx
     * 
     * @return isModifiableByIpx
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IS_MODIFIABLE_BY_IPX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, Boolean> getIsModifiableByIpx()
    {
        return isModifiableByIpx;
    }

    @JsonProperty(JSON_PROPERTY_IS_MODIFIABLE_BY_IPX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsModifiableByIpx(Map<String, Boolean> isModifiableByIpx)
    {
        this.isModifiableByIpx = isModifiableByIpx;
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
        IeInfo ieInfo = (IeInfo) o;
        return Objects.equals(this.ieLoc, ieInfo.ieLoc) && Objects.equals(this.ieType, ieInfo.ieType) && Objects.equals(this.reqIe, ieInfo.reqIe)
               && Objects.equals(this.rspIe, ieInfo.rspIe) && Objects.equals(this.isModifiable, ieInfo.isModifiable)
               && Objects.equals(this.isModifiableByIpx, ieInfo.isModifiableByIpx);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ieLoc, ieType, reqIe, rspIe, isModifiable, isModifiableByIpx);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class IeInfo {\n");
        sb.append("    ieLoc: ").append(toIndentedString(ieLoc)).append("\n");
        sb.append("    ieType: ").append(toIndentedString(ieType)).append("\n");
        sb.append("    reqIe: ").append(toIndentedString(reqIe)).append("\n");
        sb.append("    rspIe: ").append(toIndentedString(rspIe)).append("\n");
        sb.append("    isModifiable: ").append(toIndentedString(isModifiable)).append("\n");
        sb.append("    isModifiableByIpx: ").append(toIndentedString(isModifiableByIpx)).append("\n");
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
