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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * SDPMediaComponent
 */
@JsonPropertyOrder({ SDPMediaComponent.JSON_PROPERTY_S_D_P_MEDIA_NAME,
                     SDPMediaComponent.JSON_PROPERTY_SD_P_MEDIA_DESCRIPTION,
                     SDPMediaComponent.JSON_PROPERTY_LOCAL_G_W_INSERTED_INDICATION,
                     SDPMediaComponent.JSON_PROPERTY_IP_REALM_DEFAULT_INDICATION,
                     SDPMediaComponent.JSON_PROPERTY_TRANSCODER_INSERTED_INDICATION,
                     SDPMediaComponent.JSON_PROPERTY_MEDIA_INITIATOR_FLAG,
                     SDPMediaComponent.JSON_PROPERTY_MEDIA_INITIATOR_PARTY,
                     SDPMediaComponent.JSON_PROPERTY_THREE_G_P_P_CHARGING_ID,
                     SDPMediaComponent.JSON_PROPERTY_ACCESS_NETWORK_CHARGING_IDENTIFIER_VALUE,
                     SDPMediaComponent.JSON_PROPERTY_S_D_P_TYPE })
public class SDPMediaComponent
{
    public static final String JSON_PROPERTY_S_D_P_MEDIA_NAME = "sDPMediaName";
    private String sDPMediaName;

    public static final String JSON_PROPERTY_SD_P_MEDIA_DESCRIPTION = "SDPMediaDescription";
    private List<String> sdPMediaDescription = null;

    public static final String JSON_PROPERTY_LOCAL_G_W_INSERTED_INDICATION = "localGWInsertedIndication";
    private Boolean localGWInsertedIndication;

    public static final String JSON_PROPERTY_IP_REALM_DEFAULT_INDICATION = "ipRealmDefaultIndication";
    private Boolean ipRealmDefaultIndication;

    public static final String JSON_PROPERTY_TRANSCODER_INSERTED_INDICATION = "transcoderInsertedIndication";
    private Boolean transcoderInsertedIndication;

    public static final String JSON_PROPERTY_MEDIA_INITIATOR_FLAG = "mediaInitiatorFlag";
    private String mediaInitiatorFlag;

    public static final String JSON_PROPERTY_MEDIA_INITIATOR_PARTY = "mediaInitiatorParty";
    private String mediaInitiatorParty;

    public static final String JSON_PROPERTY_THREE_G_P_P_CHARGING_ID = "threeGPPChargingId";
    private String threeGPPChargingId;

    public static final String JSON_PROPERTY_ACCESS_NETWORK_CHARGING_IDENTIFIER_VALUE = "accessNetworkChargingIdentifierValue";
    private String accessNetworkChargingIdentifierValue;

    public static final String JSON_PROPERTY_S_D_P_TYPE = "sDPType";
    private String sDPType;

    public SDPMediaComponent()
    {
    }

    public SDPMediaComponent sDPMediaName(String sDPMediaName)
    {

        this.sDPMediaName = sDPMediaName;
        return this;
    }

    /**
     * Get sDPMediaName
     * 
     * @return sDPMediaName
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_D_P_MEDIA_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsDPMediaName()
    {
        return sDPMediaName;
    }

    @JsonProperty(JSON_PROPERTY_S_D_P_MEDIA_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsDPMediaName(String sDPMediaName)
    {
        this.sDPMediaName = sDPMediaName;
    }

    public SDPMediaComponent sdPMediaDescription(List<String> sdPMediaDescription)
    {

        this.sdPMediaDescription = sdPMediaDescription;
        return this;
    }

    public SDPMediaComponent addSdPMediaDescriptionItem(String sdPMediaDescriptionItem)
    {
        if (this.sdPMediaDescription == null)
        {
            this.sdPMediaDescription = new ArrayList<>();
        }
        this.sdPMediaDescription.add(sdPMediaDescriptionItem);
        return this;
    }

    /**
     * Get sdPMediaDescription
     * 
     * @return sdPMediaDescription
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SD_P_MEDIA_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSdPMediaDescription()
    {
        return sdPMediaDescription;
    }

    @JsonProperty(JSON_PROPERTY_SD_P_MEDIA_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSdPMediaDescription(List<String> sdPMediaDescription)
    {
        this.sdPMediaDescription = sdPMediaDescription;
    }

    public SDPMediaComponent localGWInsertedIndication(Boolean localGWInsertedIndication)
    {

        this.localGWInsertedIndication = localGWInsertedIndication;
        return this;
    }

    /**
     * Get localGWInsertedIndication
     * 
     * @return localGWInsertedIndication
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCAL_G_W_INSERTED_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getLocalGWInsertedIndication()
    {
        return localGWInsertedIndication;
    }

    @JsonProperty(JSON_PROPERTY_LOCAL_G_W_INSERTED_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalGWInsertedIndication(Boolean localGWInsertedIndication)
    {
        this.localGWInsertedIndication = localGWInsertedIndication;
    }

    public SDPMediaComponent ipRealmDefaultIndication(Boolean ipRealmDefaultIndication)
    {

        this.ipRealmDefaultIndication = ipRealmDefaultIndication;
        return this;
    }

    /**
     * Get ipRealmDefaultIndication
     * 
     * @return ipRealmDefaultIndication
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IP_REALM_DEFAULT_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIpRealmDefaultIndication()
    {
        return ipRealmDefaultIndication;
    }

    @JsonProperty(JSON_PROPERTY_IP_REALM_DEFAULT_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpRealmDefaultIndication(Boolean ipRealmDefaultIndication)
    {
        this.ipRealmDefaultIndication = ipRealmDefaultIndication;
    }

    public SDPMediaComponent transcoderInsertedIndication(Boolean transcoderInsertedIndication)
    {

        this.transcoderInsertedIndication = transcoderInsertedIndication;
        return this;
    }

    /**
     * Get transcoderInsertedIndication
     * 
     * @return transcoderInsertedIndication
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TRANSCODER_INSERTED_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getTranscoderInsertedIndication()
    {
        return transcoderInsertedIndication;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODER_INSERTED_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscoderInsertedIndication(Boolean transcoderInsertedIndication)
    {
        this.transcoderInsertedIndication = transcoderInsertedIndication;
    }

    public SDPMediaComponent mediaInitiatorFlag(String mediaInitiatorFlag)
    {

        this.mediaInitiatorFlag = mediaInitiatorFlag;
        return this;
    }

    /**
     * Get mediaInitiatorFlag
     * 
     * @return mediaInitiatorFlag
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MEDIA_INITIATOR_FLAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMediaInitiatorFlag()
    {
        return mediaInitiatorFlag;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_INITIATOR_FLAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaInitiatorFlag(String mediaInitiatorFlag)
    {
        this.mediaInitiatorFlag = mediaInitiatorFlag;
    }

    public SDPMediaComponent mediaInitiatorParty(String mediaInitiatorParty)
    {

        this.mediaInitiatorParty = mediaInitiatorParty;
        return this;
    }

    /**
     * Get mediaInitiatorParty
     * 
     * @return mediaInitiatorParty
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MEDIA_INITIATOR_PARTY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMediaInitiatorParty()
    {
        return mediaInitiatorParty;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_INITIATOR_PARTY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaInitiatorParty(String mediaInitiatorParty)
    {
        this.mediaInitiatorParty = mediaInitiatorParty;
    }

    public SDPMediaComponent threeGPPChargingId(String threeGPPChargingId)
    {

        this.threeGPPChargingId = threeGPPChargingId;
        return this;
    }

    /**
     * Get threeGPPChargingId
     * 
     * @return threeGPPChargingId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_THREE_G_P_P_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getThreeGPPChargingId()
    {
        return threeGPPChargingId;
    }

    @JsonProperty(JSON_PROPERTY_THREE_G_P_P_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeGPPChargingId(String threeGPPChargingId)
    {
        this.threeGPPChargingId = threeGPPChargingId;
    }

    public SDPMediaComponent accessNetworkChargingIdentifierValue(String accessNetworkChargingIdentifierValue)
    {

        this.accessNetworkChargingIdentifierValue = accessNetworkChargingIdentifierValue;
        return this;
    }

    /**
     * Get accessNetworkChargingIdentifierValue
     * 
     * @return accessNetworkChargingIdentifierValue
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACCESS_NETWORK_CHARGING_IDENTIFIER_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAccessNetworkChargingIdentifierValue()
    {
        return accessNetworkChargingIdentifierValue;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_NETWORK_CHARGING_IDENTIFIER_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccessNetworkChargingIdentifierValue(String accessNetworkChargingIdentifierValue)
    {
        this.accessNetworkChargingIdentifierValue = accessNetworkChargingIdentifierValue;
    }

    public SDPMediaComponent sDPType(String sDPType)
    {

        this.sDPType = sDPType;
        return this;
    }

    /**
     * Get sDPType
     * 
     * @return sDPType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_D_P_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsDPType()
    {
        return sDPType;
    }

    @JsonProperty(JSON_PROPERTY_S_D_P_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsDPType(String sDPType)
    {
        this.sDPType = sDPType;
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
        SDPMediaComponent sdPMediaComponent = (SDPMediaComponent) o;
        return Objects.equals(this.sDPMediaName, sdPMediaComponent.sDPMediaName)
               && Objects.equals(this.sdPMediaDescription, sdPMediaComponent.sdPMediaDescription)
               && Objects.equals(this.localGWInsertedIndication, sdPMediaComponent.localGWInsertedIndication)
               && Objects.equals(this.ipRealmDefaultIndication, sdPMediaComponent.ipRealmDefaultIndication)
               && Objects.equals(this.transcoderInsertedIndication, sdPMediaComponent.transcoderInsertedIndication)
               && Objects.equals(this.mediaInitiatorFlag, sdPMediaComponent.mediaInitiatorFlag)
               && Objects.equals(this.mediaInitiatorParty, sdPMediaComponent.mediaInitiatorParty)
               && Objects.equals(this.threeGPPChargingId, sdPMediaComponent.threeGPPChargingId)
               && Objects.equals(this.accessNetworkChargingIdentifierValue, sdPMediaComponent.accessNetworkChargingIdentifierValue)
               && Objects.equals(this.sDPType, sdPMediaComponent.sDPType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sDPMediaName,
                            sdPMediaDescription,
                            localGWInsertedIndication,
                            ipRealmDefaultIndication,
                            transcoderInsertedIndication,
                            mediaInitiatorFlag,
                            mediaInitiatorParty,
                            threeGPPChargingId,
                            accessNetworkChargingIdentifierValue,
                            sDPType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SDPMediaComponent {\n");
        sb.append("    sDPMediaName: ").append(toIndentedString(sDPMediaName)).append("\n");
        sb.append("    sdPMediaDescription: ").append(toIndentedString(sdPMediaDescription)).append("\n");
        sb.append("    localGWInsertedIndication: ").append(toIndentedString(localGWInsertedIndication)).append("\n");
        sb.append("    ipRealmDefaultIndication: ").append(toIndentedString(ipRealmDefaultIndication)).append("\n");
        sb.append("    transcoderInsertedIndication: ").append(toIndentedString(transcoderInsertedIndication)).append("\n");
        sb.append("    mediaInitiatorFlag: ").append(toIndentedString(mediaInitiatorFlag)).append("\n");
        sb.append("    mediaInitiatorParty: ").append(toIndentedString(mediaInitiatorParty)).append("\n");
        sb.append("    threeGPPChargingId: ").append(toIndentedString(threeGPPChargingId)).append("\n");
        sb.append("    accessNetworkChargingIdentifierValue: ").append(toIndentedString(accessNetworkChargingIdentifierValue)).append("\n");
        sb.append("    sDPType: ").append(toIndentedString(sDPType)).append("\n");
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
