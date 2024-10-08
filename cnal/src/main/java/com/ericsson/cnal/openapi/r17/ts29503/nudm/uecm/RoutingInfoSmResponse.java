/*
 * Nudm_UECM
 * Nudm Context Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm;

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
 * Addressing information of available nodes for SMS delivery
 */
@ApiModel(description = "Addressing information of available nodes for SMS delivery")
@JsonPropertyOrder({ RoutingInfoSmResponse.JSON_PROPERTY_SUPI,
                     RoutingInfoSmResponse.JSON_PROPERTY_SMSF3_GPP,
                     RoutingInfoSmResponse.JSON_PROPERTY_SMSF_NON3_GPP,
                     RoutingInfoSmResponse.JSON_PROPERTY_IP_SM_GW,
                     RoutingInfoSmResponse.JSON_PROPERTY_SMS_ROUTER })
public class RoutingInfoSmResponse
{
    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_SMSF3_GPP = "smsf3Gpp";
    private SmsfRegistration smsf3Gpp;

    public static final String JSON_PROPERTY_SMSF_NON3_GPP = "smsfNon3Gpp";
    private SmsfRegistration smsfNon3Gpp;

    public static final String JSON_PROPERTY_IP_SM_GW = "ipSmGw";
    private IpSmGwInfo ipSmGw;

    public static final String JSON_PROPERTY_SMS_ROUTER = "smsRouter";
    private SmsRouterInfo smsRouter;

    public RoutingInfoSmResponse()
    {
    }

    public RoutingInfoSmResponse supi(String supi)
    {

        this.supi = supi;
        return this;
    }

    /**
     * String identifying a Supi that shall contain either an IMSI, a network
     * specific identifier, a Global Cable Identifier (GCI) or a Global Line
     * Identifier (GLI) as specified in clause 2.2A of 3GPP TS 23.003. It shall be
     * formatted as follows - for an IMSI \&quot;imsi-&lt;imsi&gt;\&quot;, where
     * &lt;imsi&gt; shall be formatted according to clause 2.2 of 3GPP TS 23.003
     * that describes an IMSI. - for a network specific identifier
     * \&quot;nai-&lt;nai&gt;, where &lt;nai&gt; shall be formatted according to
     * clause 28.7.2 of 3GPP TS 23.003 that describes an NAI. - for a GCI
     * \&quot;gci-&lt;gci&gt;\&quot;, where &lt;gci&gt; shall be formatted according
     * to clause 28.15.2 of 3GPP TS 23.003. - for a GLI
     * \&quot;gli-&lt;gli&gt;\&quot;, where &lt;gli&gt; shall be formatted according
     * to clause 28.16.2 of 3GPP TS 23.003.To enable that the value is used as part
     * of an URI, the string shall only contain characters allowed according to the
     * \&quot;lower-with-hyphen\&quot; naming convention defined in 3GPP TS 29.501.
     * 
     * @return supi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Supi that shall contain either an IMSI, a network specific identifier, a Global Cable Identifier (GCI) or a Global Line Identifier (GLI) as specified in clause  2.2A of 3GPP TS 23.003. It shall be formatted as follows  - for an IMSI \"imsi-<imsi>\", where <imsi> shall be formatted according to clause 2.2    of 3GPP TS 23.003 that describes an IMSI.  - for a network specific identifier \"nai-<nai>, where <nai> shall be formatted    according to clause 28.7.2 of 3GPP TS 23.003 that describes an NAI.  - for a GCI \"gci-<gci>\", where <gci> shall be formatted according to clause 28.15.2    of 3GPP TS 23.003.  - for a GLI \"gli-<gli>\", where <gli> shall be formatted according to clause 28.16.2 of    3GPP TS 23.003.To enable that the value is used as part of an URI, the string shall    only contain characters allowed according to the \"lower-with-hyphen\" naming convention    defined in 3GPP TS 29.501. ")
    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupi()
    {
        return supi;
    }

    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupi(String supi)
    {
        this.supi = supi;
    }

    public RoutingInfoSmResponse smsf3Gpp(SmsfRegistration smsf3Gpp)
    {

        this.smsf3Gpp = smsf3Gpp;
        return this;
    }

    /**
     * Get smsf3Gpp
     * 
     * @return smsf3Gpp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMSF3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsfRegistration getSmsf3Gpp()
    {
        return smsf3Gpp;
    }

    @JsonProperty(JSON_PROPERTY_SMSF3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsf3Gpp(SmsfRegistration smsf3Gpp)
    {
        this.smsf3Gpp = smsf3Gpp;
    }

    public RoutingInfoSmResponse smsfNon3Gpp(SmsfRegistration smsfNon3Gpp)
    {

        this.smsfNon3Gpp = smsfNon3Gpp;
        return this;
    }

    /**
     * Get smsfNon3Gpp
     * 
     * @return smsfNon3Gpp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMSF_NON3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsfRegistration getSmsfNon3Gpp()
    {
        return smsfNon3Gpp;
    }

    @JsonProperty(JSON_PROPERTY_SMSF_NON3_GPP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsfNon3Gpp(SmsfRegistration smsfNon3Gpp)
    {
        this.smsfNon3Gpp = smsfNon3Gpp;
    }

    public RoutingInfoSmResponse ipSmGw(IpSmGwInfo ipSmGw)
    {

        this.ipSmGw = ipSmGw;
        return this;
    }

    /**
     * Get ipSmGw
     * 
     * @return ipSmGw
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IP_SM_GW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IpSmGwInfo getIpSmGw()
    {
        return ipSmGw;
    }

    @JsonProperty(JSON_PROPERTY_IP_SM_GW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpSmGw(IpSmGwInfo ipSmGw)
    {
        this.ipSmGw = ipSmGw;
    }

    public RoutingInfoSmResponse smsRouter(SmsRouterInfo smsRouter)
    {

        this.smsRouter = smsRouter;
        return this;
    }

    /**
     * Get smsRouter
     * 
     * @return smsRouter
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMS_ROUTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmsRouterInfo getSmsRouter()
    {
        return smsRouter;
    }

    @JsonProperty(JSON_PROPERTY_SMS_ROUTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsRouter(SmsRouterInfo smsRouter)
    {
        this.smsRouter = smsRouter;
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
        RoutingInfoSmResponse routingInfoSmResponse = (RoutingInfoSmResponse) o;
        return Objects.equals(this.supi, routingInfoSmResponse.supi) && Objects.equals(this.smsf3Gpp, routingInfoSmResponse.smsf3Gpp)
               && Objects.equals(this.smsfNon3Gpp, routingInfoSmResponse.smsfNon3Gpp) && Objects.equals(this.ipSmGw, routingInfoSmResponse.ipSmGw)
               && Objects.equals(this.smsRouter, routingInfoSmResponse.smsRouter);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supi, smsf3Gpp, smsfNon3Gpp, ipSmGw, smsRouter);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RoutingInfoSmResponse {\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    smsf3Gpp: ").append(toIndentedString(smsf3Gpp)).append("\n");
        sb.append("    smsfNon3Gpp: ").append(toIndentedString(smsfNon3Gpp)).append("\n");
        sb.append("    ipSmGw: ").append(toIndentedString(ipSmGw)).append("\n");
        sb.append("    smsRouter: ").append(toIndentedString(smsRouter)).append("\n");
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
