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
 * RecipientInfo
 */
@JsonPropertyOrder({ RecipientInfo.JSON_PROPERTY_RECIPIENT_S_U_P_I,
                     RecipientInfo.JSON_PROPERTY_RECIPIENT_G_P_S_I,
                     RecipientInfo.JSON_PROPERTY_RECIPIENT_OTHER_ADDRESS,
                     RecipientInfo.JSON_PROPERTY_RECIPIENT_RECEIVED_ADDRESS,
                     RecipientInfo.JSON_PROPERTY_RECIPIENT_S_C_C_P_ADDRESS,
                     RecipientInfo.JSON_PROPERTY_S_M_DESTINATION_INTERFACE,
                     RecipientInfo.JSON_PROPERTY_S_MRECIPIENT_PROTOCOL_ID })
public class RecipientInfo
{
    public static final String JSON_PROPERTY_RECIPIENT_S_U_P_I = "recipientSUPI";
    private String recipientSUPI;

    public static final String JSON_PROPERTY_RECIPIENT_G_P_S_I = "recipientGPSI";
    private String recipientGPSI;

    public static final String JSON_PROPERTY_RECIPIENT_OTHER_ADDRESS = "recipientOtherAddress";
    private SMAddressInfo recipientOtherAddress;

    public static final String JSON_PROPERTY_RECIPIENT_RECEIVED_ADDRESS = "recipientReceivedAddress";
    private SMAddressInfo recipientReceivedAddress;

    public static final String JSON_PROPERTY_RECIPIENT_S_C_C_P_ADDRESS = "recipientSCCPAddress";
    private String recipientSCCPAddress;

    public static final String JSON_PROPERTY_S_M_DESTINATION_INTERFACE = "sMDestinationInterface";
    private SMInterface sMDestinationInterface;

    public static final String JSON_PROPERTY_S_MRECIPIENT_PROTOCOL_ID = "sMrecipientProtocolId";
    private String sMrecipientProtocolId;

    public RecipientInfo()
    {
    }

    public RecipientInfo recipientSUPI(String recipientSUPI)
    {

        this.recipientSUPI = recipientSUPI;
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
     * @return recipientSUPI
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Supi that shall contain either an IMSI, a network specific identifier, a Global Cable Identifier (GCI) or a Global Line Identifier (GLI) as specified in clause  2.2A of 3GPP TS 23.003. It shall be formatted as follows  - for an IMSI \"imsi-<imsi>\", where <imsi> shall be formatted according to clause 2.2    of 3GPP TS 23.003 that describes an IMSI.  - for a network specific identifier \"nai-<nai>, where <nai> shall be formatted    according to clause 28.7.2 of 3GPP TS 23.003 that describes an NAI.  - for a GCI \"gci-<gci>\", where <gci> shall be formatted according to clause 28.15.2    of 3GPP TS 23.003.  - for a GLI \"gli-<gli>\", where <gli> shall be formatted according to clause 28.16.2 of    3GPP TS 23.003.To enable that the value is used as part of an URI, the string shall    only contain characters allowed according to the \"lower-with-hyphen\" naming convention    defined in 3GPP TS 29.501. ")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_S_U_P_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRecipientSUPI()
    {
        return recipientSUPI;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_S_U_P_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientSUPI(String recipientSUPI)
    {
        this.recipientSUPI = recipientSUPI;
    }

    public RecipientInfo recipientGPSI(String recipientGPSI)
    {

        this.recipientGPSI = recipientGPSI;
        return this;
    }

    /**
     * String identifying a Gpsi shall contain either an External Id or an MSISDN.
     * It shall be formatted as follows -External Identifier&#x3D;
     * \&quot;extid-&#39;extid&#39;, where &#39;extid&#39; shall be formatted
     * according to clause 19.7.2 of 3GPP TS 23.003 that describes an External
     * Identifier.
     * 
     * @return recipientGPSI
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Gpsi shall contain either an External Id or an MSISDN.  It shall be formatted as follows -External Identifier= \"extid-'extid', where 'extid'  shall be formatted according to clause 19.7.2 of 3GPP TS 23.003 that describes an  External Identifier.  ")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_G_P_S_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRecipientGPSI()
    {
        return recipientGPSI;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_G_P_S_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientGPSI(String recipientGPSI)
    {
        this.recipientGPSI = recipientGPSI;
    }

    public RecipientInfo recipientOtherAddress(SMAddressInfo recipientOtherAddress)
    {

        this.recipientOtherAddress = recipientOtherAddress;
        return this;
    }

    /**
     * Get recipientOtherAddress
     * 
     * @return recipientOtherAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_OTHER_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SMAddressInfo getRecipientOtherAddress()
    {
        return recipientOtherAddress;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_OTHER_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientOtherAddress(SMAddressInfo recipientOtherAddress)
    {
        this.recipientOtherAddress = recipientOtherAddress;
    }

    public RecipientInfo recipientReceivedAddress(SMAddressInfo recipientReceivedAddress)
    {

        this.recipientReceivedAddress = recipientReceivedAddress;
        return this;
    }

    /**
     * Get recipientReceivedAddress
     * 
     * @return recipientReceivedAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_RECEIVED_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SMAddressInfo getRecipientReceivedAddress()
    {
        return recipientReceivedAddress;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_RECEIVED_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientReceivedAddress(SMAddressInfo recipientReceivedAddress)
    {
        this.recipientReceivedAddress = recipientReceivedAddress;
    }

    public RecipientInfo recipientSCCPAddress(String recipientSCCPAddress)
    {

        this.recipientSCCPAddress = recipientSCCPAddress;
        return this;
    }

    /**
     * Get recipientSCCPAddress
     * 
     * @return recipientSCCPAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_S_C_C_P_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRecipientSCCPAddress()
    {
        return recipientSCCPAddress;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_S_C_C_P_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientSCCPAddress(String recipientSCCPAddress)
    {
        this.recipientSCCPAddress = recipientSCCPAddress;
    }

    public RecipientInfo sMDestinationInterface(SMInterface sMDestinationInterface)
    {

        this.sMDestinationInterface = sMDestinationInterface;
        return this;
    }

    /**
     * Get sMDestinationInterface
     * 
     * @return sMDestinationInterface
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_DESTINATION_INTERFACE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SMInterface getsMDestinationInterface()
    {
        return sMDestinationInterface;
    }

    @JsonProperty(JSON_PROPERTY_S_M_DESTINATION_INTERFACE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMDestinationInterface(SMInterface sMDestinationInterface)
    {
        this.sMDestinationInterface = sMDestinationInterface;
    }

    public RecipientInfo sMrecipientProtocolId(String sMrecipientProtocolId)
    {

        this.sMrecipientProtocolId = sMrecipientProtocolId;
        return this;
    }

    /**
     * Get sMrecipientProtocolId
     * 
     * @return sMrecipientProtocolId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_MRECIPIENT_PROTOCOL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMrecipientProtocolId()
    {
        return sMrecipientProtocolId;
    }

    @JsonProperty(JSON_PROPERTY_S_MRECIPIENT_PROTOCOL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMrecipientProtocolId(String sMrecipientProtocolId)
    {
        this.sMrecipientProtocolId = sMrecipientProtocolId;
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
        RecipientInfo recipientInfo = (RecipientInfo) o;
        return Objects.equals(this.recipientSUPI, recipientInfo.recipientSUPI) && Objects.equals(this.recipientGPSI, recipientInfo.recipientGPSI)
               && Objects.equals(this.recipientOtherAddress, recipientInfo.recipientOtherAddress)
               && Objects.equals(this.recipientReceivedAddress, recipientInfo.recipientReceivedAddress)
               && Objects.equals(this.recipientSCCPAddress, recipientInfo.recipientSCCPAddress)
               && Objects.equals(this.sMDestinationInterface, recipientInfo.sMDestinationInterface)
               && Objects.equals(this.sMrecipientProtocolId, recipientInfo.sMrecipientProtocolId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(recipientSUPI,
                            recipientGPSI,
                            recipientOtherAddress,
                            recipientReceivedAddress,
                            recipientSCCPAddress,
                            sMDestinationInterface,
                            sMrecipientProtocolId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RecipientInfo {\n");
        sb.append("    recipientSUPI: ").append(toIndentedString(recipientSUPI)).append("\n");
        sb.append("    recipientGPSI: ").append(toIndentedString(recipientGPSI)).append("\n");
        sb.append("    recipientOtherAddress: ").append(toIndentedString(recipientOtherAddress)).append("\n");
        sb.append("    recipientReceivedAddress: ").append(toIndentedString(recipientReceivedAddress)).append("\n");
        sb.append("    recipientSCCPAddress: ").append(toIndentedString(recipientSCCPAddress)).append("\n");
        sb.append("    sMDestinationInterface: ").append(toIndentedString(sMDestinationInterface)).append("\n");
        sb.append("    sMrecipientProtocolId: ").append(toIndentedString(sMrecipientProtocolId)).append("\n");
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
