/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

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
 * Indirect Data Forwarding Tunnel Information
 */
@ApiModel(description = "Indirect Data Forwarding Tunnel Information")
@JsonPropertyOrder({ IndirectDataForwardingTunnelInfo.JSON_PROPERTY_IPV4_ADDR,
                     IndirectDataForwardingTunnelInfo.JSON_PROPERTY_IPV6_ADDR,
                     IndirectDataForwardingTunnelInfo.JSON_PROPERTY_GTP_TEID,
                     IndirectDataForwardingTunnelInfo.JSON_PROPERTY_DRB_ID,
                     IndirectDataForwardingTunnelInfo.JSON_PROPERTY_ADDITIONAL_TNL_NB })
public class IndirectDataForwardingTunnelInfo
{
    public static final String JSON_PROPERTY_IPV4_ADDR = "ipv4Addr";
    private String ipv4Addr;

    public static final String JSON_PROPERTY_IPV6_ADDR = "ipv6Addr";
    private String ipv6Addr;

    public static final String JSON_PROPERTY_GTP_TEID = "gtpTeid";
    private String gtpTeid;

    public static final String JSON_PROPERTY_DRB_ID = "drbId";
    private Integer drbId;

    public static final String JSON_PROPERTY_ADDITIONAL_TNL_NB = "additionalTnlNb";
    private Integer additionalTnlNb;

    public IndirectDataForwardingTunnelInfo()
    {
    }

    public IndirectDataForwardingTunnelInfo ipv4Addr(String ipv4Addr)
    {

        this.ipv4Addr = ipv4Addr;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return ipv4Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv4Addr()
    {
        return ipv4Addr;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4Addr(String ipv4Addr)
    {
        this.ipv4Addr = ipv4Addr;
    }

    public IndirectDataForwardingTunnelInfo ipv6Addr(String ipv6Addr)
    {

        this.ipv6Addr = ipv6Addr;
        return this;
    }

    /**
     * Get ipv6Addr
     * 
     * @return ipv6Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv6Addr()
    {
        return ipv6Addr;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6Addr(String ipv6Addr)
    {
        this.ipv6Addr = ipv6Addr;
    }

    public IndirectDataForwardingTunnelInfo gtpTeid(String gtpTeid)
    {

        this.gtpTeid = gtpTeid;
        return this;
    }

    /**
     * GTP Tunnel Endpoint Identifier
     * 
     * @return gtpTeid
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "GTP Tunnel Endpoint Identifier")
    @JsonProperty(JSON_PROPERTY_GTP_TEID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getGtpTeid()
    {
        return gtpTeid;
    }

    @JsonProperty(JSON_PROPERTY_GTP_TEID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setGtpTeid(String gtpTeid)
    {
        this.gtpTeid = gtpTeid;
    }

    public IndirectDataForwardingTunnelInfo drbId(Integer drbId)
    {

        this.drbId = drbId;
        return this;
    }

    /**
     * Data Radio Bearer Identity minimum: 1 maximum: 32
     * 
     * @return drbId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Data Radio Bearer Identity")
    @JsonProperty(JSON_PROPERTY_DRB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDrbId()
    {
        return drbId;
    }

    @JsonProperty(JSON_PROPERTY_DRB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDrbId(Integer drbId)
    {
        this.drbId = drbId;
    }

    public IndirectDataForwardingTunnelInfo additionalTnlNb(Integer additionalTnlNb)
    {

        this.additionalTnlNb = additionalTnlNb;
        return this;
    }

    /**
     * indicates first, second or third additional indirect data forwarding tunnel
     * minimum: 1 maximum: 3
     * 
     * @return additionalTnlNb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicates first, second or third additional indirect data forwarding tunnel")
    @JsonProperty(JSON_PROPERTY_ADDITIONAL_TNL_NB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAdditionalTnlNb()
    {
        return additionalTnlNb;
    }

    @JsonProperty(JSON_PROPERTY_ADDITIONAL_TNL_NB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalTnlNb(Integer additionalTnlNb)
    {
        this.additionalTnlNb = additionalTnlNb;
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
        IndirectDataForwardingTunnelInfo indirectDataForwardingTunnelInfo = (IndirectDataForwardingTunnelInfo) o;
        return Objects.equals(this.ipv4Addr, indirectDataForwardingTunnelInfo.ipv4Addr)
               && Objects.equals(this.ipv6Addr, indirectDataForwardingTunnelInfo.ipv6Addr)
               && Objects.equals(this.gtpTeid, indirectDataForwardingTunnelInfo.gtpTeid) && Objects.equals(this.drbId, indirectDataForwardingTunnelInfo.drbId)
               && Objects.equals(this.additionalTnlNb, indirectDataForwardingTunnelInfo.additionalTnlNb);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ipv4Addr, ipv6Addr, gtpTeid, drbId, additionalTnlNb);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class IndirectDataForwardingTunnelInfo {\n");
        sb.append("    ipv4Addr: ").append(toIndentedString(ipv4Addr)).append("\n");
        sb.append("    ipv6Addr: ").append(toIndentedString(ipv6Addr)).append("\n");
        sb.append("    gtpTeid: ").append(toIndentedString(gtpTeid)).append("\n");
        sb.append("    drbId: ").append(toIndentedString(drbId)).append("\n");
        sb.append("    additionalTnlNb: ").append(toIndentedString(additionalTnlNb)).append("\n");
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
