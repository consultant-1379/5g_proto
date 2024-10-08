/*
 * Npcf_PolicyAuthorization Service API
 * PCF Policy Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29514.npcf.policyauthorization;

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
 * Represents an Individual Application Session Context resource.
 */
@ApiModel(description = "Represents an Individual Application Session Context resource.")
@JsonPropertyOrder({ AppSessionContext.JSON_PROPERTY_ASC_REQ_DATA, AppSessionContext.JSON_PROPERTY_ASC_RESP_DATA, AppSessionContext.JSON_PROPERTY_EVS_NOTIF })
public class AppSessionContext
{
    public static final String JSON_PROPERTY_ASC_REQ_DATA = "ascReqData";
    private AppSessionContextReqData ascReqData;

    public static final String JSON_PROPERTY_ASC_RESP_DATA = "ascRespData";
    private AppSessionContextRespData ascRespData;

    public static final String JSON_PROPERTY_EVS_NOTIF = "evsNotif";
    private EventsNotification evsNotif;

    public AppSessionContext()
    {
    }

    public AppSessionContext ascReqData(AppSessionContextReqData ascReqData)
    {

        this.ascReqData = ascReqData;
        return this;
    }

    /**
     * Get ascReqData
     * 
     * @return ascReqData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ASC_REQ_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AppSessionContextReqData getAscReqData()
    {
        return ascReqData;
    }

    @JsonProperty(JSON_PROPERTY_ASC_REQ_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAscReqData(AppSessionContextReqData ascReqData)
    {
        this.ascReqData = ascReqData;
    }

    public AppSessionContext ascRespData(AppSessionContextRespData ascRespData)
    {

        this.ascRespData = ascRespData;
        return this;
    }

    /**
     * Get ascRespData
     * 
     * @return ascRespData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ASC_RESP_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AppSessionContextRespData getAscRespData()
    {
        return ascRespData;
    }

    @JsonProperty(JSON_PROPERTY_ASC_RESP_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAscRespData(AppSessionContextRespData ascRespData)
    {
        this.ascRespData = ascRespData;
    }

    public AppSessionContext evsNotif(EventsNotification evsNotif)
    {

        this.evsNotif = evsNotif;
        return this;
    }

    /**
     * Get evsNotif
     * 
     * @return evsNotif
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVS_NOTIF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public EventsNotification getEvsNotif()
    {
        return evsNotif;
    }

    @JsonProperty(JSON_PROPERTY_EVS_NOTIF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEvsNotif(EventsNotification evsNotif)
    {
        this.evsNotif = evsNotif;
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
        AppSessionContext appSessionContext = (AppSessionContext) o;
        return Objects.equals(this.ascReqData, appSessionContext.ascReqData) && Objects.equals(this.ascRespData, appSessionContext.ascRespData)
               && Objects.equals(this.evsNotif, appSessionContext.evsNotif);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ascReqData, ascRespData, evsNotif);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AppSessionContext {\n");
        sb.append("    ascReqData: ").append(toIndentedString(ascReqData)).append("\n");
        sb.append("    ascRespData: ").append(toIndentedString(ascRespData)).append("\n");
        sb.append("    evsNotif: ").append(toIndentedString(evsNotif)).append("\n");
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
