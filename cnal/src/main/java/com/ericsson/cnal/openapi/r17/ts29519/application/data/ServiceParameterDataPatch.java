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
import com.ericsson.cnal.openapi.r17.ts29522.serviceparameter.UrspRuleRequest;
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
 * Represents the service parameter data that can be updated.
 */
@ApiModel(description = "Represents the service parameter data that can be updated.")
@JsonPropertyOrder({ ServiceParameterDataPatch.JSON_PROPERTY_PARAM_OVER_PC5,
                     ServiceParameterDataPatch.JSON_PROPERTY_PARAM_OVER_UU,
                     ServiceParameterDataPatch.JSON_PROPERTY_PARAM_FOR_PRO_SE_DD,
                     ServiceParameterDataPatch.JSON_PROPERTY_PARAM_FOR_PRO_SE_DC,
                     ServiceParameterDataPatch.JSON_PROPERTY_PARAM_FOR_PRO_SE_U2_N_REL_UE,
                     ServiceParameterDataPatch.JSON_PROPERTY_PARAM_FOR_PRO_SE_REM_UE,
                     ServiceParameterDataPatch.JSON_PROPERTY_URSP_INFLUENCE,
                     ServiceParameterDataPatch.JSON_PROPERTY_DELIVERY_EVENTS,
                     ServiceParameterDataPatch.JSON_PROPERTY_POLIC_DELIV_NOTIF_URI })
public class ServiceParameterDataPatch
{
    public static final String JSON_PROPERTY_PARAM_OVER_PC5 = "paramOverPc5";
    private String paramOverPc5;

    public static final String JSON_PROPERTY_PARAM_OVER_UU = "paramOverUu";
    private String paramOverUu;

    public static final String JSON_PROPERTY_PARAM_FOR_PRO_SE_DD = "paramForProSeDd";
    private String paramForProSeDd;

    public static final String JSON_PROPERTY_PARAM_FOR_PRO_SE_DC = "paramForProSeDc";
    private String paramForProSeDc;

    public static final String JSON_PROPERTY_PARAM_FOR_PRO_SE_U2_N_REL_UE = "paramForProSeU2NRelUe";
    private String paramForProSeU2NRelUe;

    public static final String JSON_PROPERTY_PARAM_FOR_PRO_SE_REM_UE = "paramForProSeRemUe";
    private String paramForProSeRemUe;

    public static final String JSON_PROPERTY_URSP_INFLUENCE = "urspInfluence";
    private List<UrspRuleRequest> urspInfluence = null;

    public static final String JSON_PROPERTY_DELIVERY_EVENTS = "deliveryEvents";
    private List<String> deliveryEvents = null;

    public static final String JSON_PROPERTY_POLIC_DELIV_NOTIF_URI = "policDelivNotifUri";
    private String policDelivNotifUri;

    public ServiceParameterDataPatch()
    {
    }

    public ServiceParameterDataPatch paramOverPc5(String paramOverPc5)
    {

        this.paramOverPc5 = paramOverPc5;
        return this;
    }

    /**
     * Represents configuration parameters for V2X communications over PC5 reference
     * point.
     * 
     * @return paramOverPc5
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents configuration parameters for V2X communications over PC5 reference point. ")
    @JsonProperty(JSON_PROPERTY_PARAM_OVER_PC5)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamOverPc5()
    {
        return paramOverPc5;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_OVER_PC5)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamOverPc5(String paramOverPc5)
    {
        this.paramOverPc5 = paramOverPc5;
    }

    public ServiceParameterDataPatch paramOverUu(String paramOverUu)
    {

        this.paramOverUu = paramOverUu;
        return this;
    }

    /**
     * Represents configuration parameters for V2X communications over Uu reference
     * point.
     * 
     * @return paramOverUu
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents configuration parameters for V2X communications over Uu reference point. ")
    @JsonProperty(JSON_PROPERTY_PARAM_OVER_UU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamOverUu()
    {
        return paramOverUu;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_OVER_UU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamOverUu(String paramOverUu)
    {
        this.paramOverUu = paramOverUu;
    }

    public ServiceParameterDataPatch paramForProSeDd(String paramForProSeDd)
    {

        this.paramForProSeDd = paramForProSeDd;
        return this;
    }

    /**
     * Represents the service parameters for 5G ProSe direct discovery.
     * 
     * @return paramForProSeDd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the service parameters for 5G ProSe direct discovery.")
    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_DD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamForProSeDd()
    {
        return paramForProSeDd;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_DD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamForProSeDd(String paramForProSeDd)
    {
        this.paramForProSeDd = paramForProSeDd;
    }

    public ServiceParameterDataPatch paramForProSeDc(String paramForProSeDc)
    {

        this.paramForProSeDc = paramForProSeDc;
        return this;
    }

    /**
     * Represents the service parameters for 5G ProSe direct communications.
     * 
     * @return paramForProSeDc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the service parameters for 5G ProSe direct communications.")
    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_DC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamForProSeDc()
    {
        return paramForProSeDc;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_DC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamForProSeDc(String paramForProSeDc)
    {
        this.paramForProSeDc = paramForProSeDc;
    }

    public ServiceParameterDataPatch paramForProSeU2NRelUe(String paramForProSeU2NRelUe)
    {

        this.paramForProSeU2NRelUe = paramForProSeU2NRelUe;
        return this;
    }

    /**
     * Represents the service parameters for 5G ProSe UE-to-network relay UE.
     * 
     * @return paramForProSeU2NRelUe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the service parameters for 5G ProSe UE-to-network relay UE.")
    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_U2_N_REL_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamForProSeU2NRelUe()
    {
        return paramForProSeU2NRelUe;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_U2_N_REL_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamForProSeU2NRelUe(String paramForProSeU2NRelUe)
    {
        this.paramForProSeU2NRelUe = paramForProSeU2NRelUe;
    }

    public ServiceParameterDataPatch paramForProSeRemUe(String paramForProSeRemUe)
    {

        this.paramForProSeRemUe = paramForProSeRemUe;
        return this;
    }

    /**
     * Represents the service parameters for 5G ProSe Remate UE.
     * 
     * @return paramForProSeRemUe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the service parameters for 5G ProSe Remate UE.")
    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_REM_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParamForProSeRemUe()
    {
        return paramForProSeRemUe;
    }

    @JsonProperty(JSON_PROPERTY_PARAM_FOR_PRO_SE_REM_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParamForProSeRemUe(String paramForProSeRemUe)
    {
        this.paramForProSeRemUe = paramForProSeRemUe;
    }

    public ServiceParameterDataPatch urspInfluence(List<UrspRuleRequest> urspInfluence)
    {

        this.urspInfluence = urspInfluence;
        return this;
    }

    public ServiceParameterDataPatch addUrspInfluenceItem(UrspRuleRequest urspInfluenceItem)
    {
        if (this.urspInfluence == null)
        {
            this.urspInfluence = new ArrayList<>();
        }
        this.urspInfluence.add(urspInfluenceItem);
        return this;
    }

    /**
     * Contains the service parameter used to influence the URSP.
     * 
     * @return urspInfluence
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the service parameter used to influence the URSP.")
    @JsonProperty(JSON_PROPERTY_URSP_INFLUENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UrspRuleRequest> getUrspInfluence()
    {
        return urspInfluence;
    }

    @JsonProperty(JSON_PROPERTY_URSP_INFLUENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrspInfluence(List<UrspRuleRequest> urspInfluence)
    {
        this.urspInfluence = urspInfluence;
    }

    public ServiceParameterDataPatch deliveryEvents(List<String> deliveryEvents)
    {

        this.deliveryEvents = deliveryEvents;
        return this;
    }

    public ServiceParameterDataPatch addDeliveryEventsItem(String deliveryEventsItem)
    {
        if (this.deliveryEvents == null)
        {
            this.deliveryEvents = new ArrayList<>();
        }
        this.deliveryEvents.add(deliveryEventsItem);
        return this;
    }

    /**
     * Contains the outcome of the UE Policy Delivery.
     * 
     * @return deliveryEvents
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the outcome of the UE Policy Delivery.")
    @JsonProperty(JSON_PROPERTY_DELIVERY_EVENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDeliveryEvents()
    {
        return deliveryEvents;
    }

    @JsonProperty(JSON_PROPERTY_DELIVERY_EVENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeliveryEvents(List<String> deliveryEvents)
    {
        this.deliveryEvents = deliveryEvents;
    }

    public ServiceParameterDataPatch policDelivNotifUri(String policDelivNotifUri)
    {

        this.policDelivNotifUri = policDelivNotifUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return policDelivNotifUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_POLIC_DELIV_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPolicDelivNotifUri()
    {
        return policDelivNotifUri;
    }

    @JsonProperty(JSON_PROPERTY_POLIC_DELIV_NOTIF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPolicDelivNotifUri(String policDelivNotifUri)
    {
        this.policDelivNotifUri = policDelivNotifUri;
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
        ServiceParameterDataPatch serviceParameterDataPatch = (ServiceParameterDataPatch) o;
        return Objects.equals(this.paramOverPc5, serviceParameterDataPatch.paramOverPc5)
               && Objects.equals(this.paramOverUu, serviceParameterDataPatch.paramOverUu)
               && Objects.equals(this.paramForProSeDd, serviceParameterDataPatch.paramForProSeDd)
               && Objects.equals(this.paramForProSeDc, serviceParameterDataPatch.paramForProSeDc)
               && Objects.equals(this.paramForProSeU2NRelUe, serviceParameterDataPatch.paramForProSeU2NRelUe)
               && Objects.equals(this.paramForProSeRemUe, serviceParameterDataPatch.paramForProSeRemUe)
               && Objects.equals(this.urspInfluence, serviceParameterDataPatch.urspInfluence)
               && Objects.equals(this.deliveryEvents, serviceParameterDataPatch.deliveryEvents)
               && Objects.equals(this.policDelivNotifUri, serviceParameterDataPatch.policDelivNotifUri);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(paramOverPc5,
                            paramOverUu,
                            paramForProSeDd,
                            paramForProSeDc,
                            paramForProSeU2NRelUe,
                            paramForProSeRemUe,
                            urspInfluence,
                            deliveryEvents,
                            policDelivNotifUri);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceParameterDataPatch {\n");
        sb.append("    paramOverPc5: ").append(toIndentedString(paramOverPc5)).append("\n");
        sb.append("    paramOverUu: ").append(toIndentedString(paramOverUu)).append("\n");
        sb.append("    paramForProSeDd: ").append(toIndentedString(paramForProSeDd)).append("\n");
        sb.append("    paramForProSeDc: ").append(toIndentedString(paramForProSeDc)).append("\n");
        sb.append("    paramForProSeU2NRelUe: ").append(toIndentedString(paramForProSeU2NRelUe)).append("\n");
        sb.append("    paramForProSeRemUe: ").append(toIndentedString(paramForProSeRemUe)).append("\n");
        sb.append("    urspInfluence: ").append(toIndentedString(urspInfluence)).append("\n");
        sb.append("    deliveryEvents: ").append(toIndentedString(deliveryEvents)).append("\n");
        sb.append("    policDelivNotifUri: ").append(toIndentedString(policDelivNotifUri)).append("\n");
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
