/*
 * Nsmf_EventExposure
 * Session Management Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29508.nsmf.eventexposure;

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
 * Represents session information.
 */
@ApiModel(description = "Represents session information.")
@JsonPropertyOrder({ PduSessionInfo.JSON_PROPERTY_N4_SESS_ID, PduSessionInfo.JSON_PROPERTY_SESS_INACTIVE_TIMER, PduSessionInfo.JSON_PROPERTY_PDU_SESS_STATUS })
public class PduSessionInfo
{
    public static final String JSON_PROPERTY_N4_SESS_ID = "n4SessId";
    private String n4SessId;

    public static final String JSON_PROPERTY_SESS_INACTIVE_TIMER = "sessInactiveTimer";
    private Integer sessInactiveTimer;

    public static final String JSON_PROPERTY_PDU_SESS_STATUS = "pduSessStatus";
    private String pduSessStatus;

    public PduSessionInfo()
    {
    }

    public PduSessionInfo n4SessId(String n4SessId)
    {

        this.n4SessId = n4SessId;
        return this;
    }

    /**
     * The identifier of the N4 session for the reported PDU Session.
     * 
     * @return n4SessId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The identifier of the N4 session for the reported PDU Session.")
    @JsonProperty(JSON_PROPERTY_N4_SESS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getN4SessId()
    {
        return n4SessId;
    }

    @JsonProperty(JSON_PROPERTY_N4_SESS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN4SessId(String n4SessId)
    {
        this.n4SessId = n4SessId;
    }

    public PduSessionInfo sessInactiveTimer(Integer sessInactiveTimer)
    {

        this.sessInactiveTimer = sessInactiveTimer;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return sessInactiveTimer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_SESS_INACTIVE_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSessInactiveTimer()
    {
        return sessInactiveTimer;
    }

    @JsonProperty(JSON_PROPERTY_SESS_INACTIVE_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessInactiveTimer(Integer sessInactiveTimer)
    {
        this.sessInactiveTimer = sessInactiveTimer;
    }

    public PduSessionInfo pduSessStatus(String pduSessStatus)
    {

        this.pduSessStatus = pduSessStatus;
        return this;
    }

    /**
     * Possible values are: - ACTIVATED: PDU Session status is activated. -
     * DEACTIVATED: PDU Session status is deactivated.
     * 
     * @return pduSessStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - ACTIVATED: PDU Session status is activated. - DEACTIVATED: PDU Session status is deactivated. ")
    @JsonProperty(JSON_PROPERTY_PDU_SESS_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPduSessStatus()
    {
        return pduSessStatus;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESS_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPduSessStatus(String pduSessStatus)
    {
        this.pduSessStatus = pduSessStatus;
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
        PduSessionInfo pduSessionInfo = (PduSessionInfo) o;
        return Objects.equals(this.n4SessId, pduSessionInfo.n4SessId) && Objects.equals(this.sessInactiveTimer, pduSessionInfo.sessInactiveTimer)
               && Objects.equals(this.pduSessStatus, pduSessionInfo.pduSessStatus);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(n4SessId, sessInactiveTimer, pduSessStatus);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PduSessionInfo {\n");
        sb.append("    n4SessId: ").append(toIndentedString(n4SessId)).append("\n");
        sb.append("    sessInactiveTimer: ").append(toIndentedString(sessInactiveTimer)).append("\n");
        sb.append("    pduSessStatus: ").append(toIndentedString(pduSessStatus)).append("\n");
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
