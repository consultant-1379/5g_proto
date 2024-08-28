/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

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
 * RedirectServer
 */
@JsonPropertyOrder({ RedirectServer.JSON_PROPERTY_REDIRECT_ADDRESS_TYPE, RedirectServer.JSON_PROPERTY_REDIRECT_SERVER_ADDRESS })
public class RedirectServer
{
    public static final String JSON_PROPERTY_REDIRECT_ADDRESS_TYPE = "redirectAddressType";
    private String redirectAddressType;

    public static final String JSON_PROPERTY_REDIRECT_SERVER_ADDRESS = "redirectServerAddress";
    private String redirectServerAddress;

    public RedirectServer()
    {
    }

    public RedirectServer redirectAddressType(String redirectAddressType)
    {

        this.redirectAddressType = redirectAddressType;
        return this;
    }

    /**
     * Get redirectAddressType
     * 
     * @return redirectAddressType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REDIRECT_ADDRESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getRedirectAddressType()
    {
        return redirectAddressType;
    }

    @JsonProperty(JSON_PROPERTY_REDIRECT_ADDRESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRedirectAddressType(String redirectAddressType)
    {
        this.redirectAddressType = redirectAddressType;
    }

    public RedirectServer redirectServerAddress(String redirectServerAddress)
    {

        this.redirectServerAddress = redirectServerAddress;
        return this;
    }

    /**
     * Get redirectServerAddress
     * 
     * @return redirectServerAddress
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REDIRECT_SERVER_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getRedirectServerAddress()
    {
        return redirectServerAddress;
    }

    @JsonProperty(JSON_PROPERTY_REDIRECT_SERVER_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRedirectServerAddress(String redirectServerAddress)
    {
        this.redirectServerAddress = redirectServerAddress;
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
        RedirectServer redirectServer = (RedirectServer) o;
        return Objects.equals(this.redirectAddressType, redirectServer.redirectAddressType)
               && Objects.equals(this.redirectServerAddress, redirectServer.redirectServerAddress);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(redirectAddressType, redirectServerAddress);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RedirectServer {\n");
        sb.append("    redirectAddressType: ").append(toIndentedString(redirectAddressType)).append("\n");
        sb.append("    redirectServerAddress: ").append(toIndentedString(redirectServerAddress)).append("\n");
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
