/*
 * Nhss_imsUEAU
 * Nhss UE Authentication Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imsueau;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.IpAddr;
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
 * Contains authentication information returned in the SIP authentication
 * response message (e.g. authentication vectors, digest authentication
 * parameters, line identifiers)
 */
@ApiModel(description = "Contains authentication information returned in the SIP authentication response message (e.g. authentication vectors, digest authentication parameters, line identifiers) ")
@JsonPropertyOrder({ SipAuthenticationInfoResult.JSON_PROPERTY_IMPI,
                     SipAuthenticationInfoResult.JSON_PROPERTY_3G_AKA_AVS,
                     SipAuthenticationInfoResult.JSON_PROPERTY_DIGEST_AUTH,
                     SipAuthenticationInfoResult.JSON_PROPERTY_LINE_IDENTIFIER_LIST,
                     SipAuthenticationInfoResult.JSON_PROPERTY_IP_ADDRESS })
public class SipAuthenticationInfoResult
{
    public static final String JSON_PROPERTY_IMPI = "impi";
    private String impi;

    public static final String JSON_PROPERTY_3G_AKA_AVS = "3gAkaAvs";
    private List<Model3GAkaAv> _3gAkaAvs = null;

    public static final String JSON_PROPERTY_DIGEST_AUTH = "digestAuth";
    private DigestAuthentication digestAuth;

    public static final String JSON_PROPERTY_LINE_IDENTIFIER_LIST = "lineIdentifierList";
    private List<String> lineIdentifierList = null;

    public static final String JSON_PROPERTY_IP_ADDRESS = "ipAddress";
    private IpAddr ipAddress;

    public SipAuthenticationInfoResult()
    {
    }

    public SipAuthenticationInfoResult impi(String impi)
    {

        this.impi = impi;
        return this;
    }

    /**
     * IMS Private Identity of the UE
     * 
     * @return impi
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "IMS Private Identity of the UE")
    @JsonProperty(JSON_PROPERTY_IMPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getImpi()
    {
        return impi;
    }

    @JsonProperty(JSON_PROPERTY_IMPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setImpi(String impi)
    {
        this.impi = impi;
    }

    public SipAuthenticationInfoResult _3gAkaAvs(List<Model3GAkaAv> _3gAkaAvs)
    {

        this._3gAkaAvs = _3gAkaAvs;
        return this;
    }

    public SipAuthenticationInfoResult add3gAkaAvsItem(Model3GAkaAv _3gAkaAvsItem)
    {
        if (this._3gAkaAvs == null)
        {
            this._3gAkaAvs = new ArrayList<>();
        }
        this._3gAkaAvs.add(_3gAkaAvsItem);
        return this;
    }

    /**
     * Get _3gAkaAvs
     * 
     * @return _3gAkaAvs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_3G_AKA_AVS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Model3GAkaAv> get3gAkaAvs()
    {
        return _3gAkaAvs;
    }

    @JsonProperty(JSON_PROPERTY_3G_AKA_AVS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set3gAkaAvs(List<Model3GAkaAv> _3gAkaAvs)
    {
        this._3gAkaAvs = _3gAkaAvs;
    }

    public SipAuthenticationInfoResult digestAuth(DigestAuthentication digestAuth)
    {

        this.digestAuth = digestAuth;
        return this;
    }

    /**
     * Get digestAuth
     * 
     * @return digestAuth
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DIGEST_AUTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DigestAuthentication getDigestAuth()
    {
        return digestAuth;
    }

    @JsonProperty(JSON_PROPERTY_DIGEST_AUTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDigestAuth(DigestAuthentication digestAuth)
    {
        this.digestAuth = digestAuth;
    }

    public SipAuthenticationInfoResult lineIdentifierList(List<String> lineIdentifierList)
    {

        this.lineIdentifierList = lineIdentifierList;
        return this;
    }

    public SipAuthenticationInfoResult addLineIdentifierListItem(String lineIdentifierListItem)
    {
        if (this.lineIdentifierList == null)
        {
            this.lineIdentifierList = new ArrayList<>();
        }
        this.lineIdentifierList.add(lineIdentifierListItem);
        return this;
    }

    /**
     * Get lineIdentifierList
     * 
     * @return lineIdentifierList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LINE_IDENTIFIER_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getLineIdentifierList()
    {
        return lineIdentifierList;
    }

    @JsonProperty(JSON_PROPERTY_LINE_IDENTIFIER_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLineIdentifierList(List<String> lineIdentifierList)
    {
        this.lineIdentifierList = lineIdentifierList;
    }

    public SipAuthenticationInfoResult ipAddress(IpAddr ipAddress)
    {

        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Get ipAddress
     * 
     * @return ipAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IP_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IpAddr getIpAddress()
    {
        return ipAddress;
    }

    @JsonProperty(JSON_PROPERTY_IP_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpAddress(IpAddr ipAddress)
    {
        this.ipAddress = ipAddress;
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
        SipAuthenticationInfoResult sipAuthenticationInfoResult = (SipAuthenticationInfoResult) o;
        return Objects.equals(this.impi, sipAuthenticationInfoResult.impi) && Objects.equals(this._3gAkaAvs, sipAuthenticationInfoResult._3gAkaAvs)
               && Objects.equals(this.digestAuth, sipAuthenticationInfoResult.digestAuth)
               && Objects.equals(this.lineIdentifierList, sipAuthenticationInfoResult.lineIdentifierList)
               && Objects.equals(this.ipAddress, sipAuthenticationInfoResult.ipAddress);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(impi, _3gAkaAvs, digestAuth, lineIdentifierList, ipAddress);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SipAuthenticationInfoResult {\n");
        sb.append("    impi: ").append(toIndentedString(impi)).append("\n");
        sb.append("    _3gAkaAvs: ").append(toIndentedString(_3gAkaAvs)).append("\n");
        sb.append("    digestAuth: ").append(toIndentedString(digestAuth)).append("\n");
        sb.append("    lineIdentifierList: ").append(toIndentedString(lineIdentifierList)).append("\n");
        sb.append("    ipAddress: ").append(toIndentedString(ipAddress)).append("\n");
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
