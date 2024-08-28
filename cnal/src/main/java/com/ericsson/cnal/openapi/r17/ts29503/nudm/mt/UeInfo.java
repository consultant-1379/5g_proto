/*
 * Nudm_MT
 * UDM MT Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.mt;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29518.namf.mt.UeContextInfo;
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
 * Represents UE information.
 */
@ApiModel(description = "Represents UE information.")
@JsonPropertyOrder({ UeInfo.JSON_PROPERTY_TADS_INFO, UeInfo.JSON_PROPERTY_USER_STATE, UeInfo.JSON_PROPERTY_5G_SRVCC_INFO })
public class UeInfo
{
    public static final String JSON_PROPERTY_TADS_INFO = "tadsInfo";
    private UeContextInfo tadsInfo;

    public static final String JSON_PROPERTY_USER_STATE = "userState";
    private String userState;

    public static final String JSON_PROPERTY_5G_SRVCC_INFO = "5gSrvccInfo";
    private Model5GSrvccInfo _5gSrvccInfo;

    public UeInfo()
    {
    }

    public UeInfo tadsInfo(UeContextInfo tadsInfo)
    {

        this.tadsInfo = tadsInfo;
        return this;
    }

    /**
     * Get tadsInfo
     * 
     * @return tadsInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TADS_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UeContextInfo getTadsInfo()
    {
        return tadsInfo;
    }

    @JsonProperty(JSON_PROPERTY_TADS_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTadsInfo(UeContextInfo tadsInfo)
    {
        this.tadsInfo = tadsInfo;
    }

    public UeInfo userState(String userState)
    {

        this.userState = userState;
        return this;
    }

    /**
     * Describes the 5GS User State of a UE
     * 
     * @return userState
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Describes the 5GS User State of a UE")
    @JsonProperty(JSON_PROPERTY_USER_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUserState()
    {
        return userState;
    }

    @JsonProperty(JSON_PROPERTY_USER_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserState(String userState)
    {
        this.userState = userState;
    }

    public UeInfo _5gSrvccInfo(Model5GSrvccInfo _5gSrvccInfo)
    {

        this._5gSrvccInfo = _5gSrvccInfo;
        return this;
    }

    /**
     * Get _5gSrvccInfo
     * 
     * @return _5gSrvccInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_5G_SRVCC_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Model5GSrvccInfo get5gSrvccInfo()
    {
        return _5gSrvccInfo;
    }

    @JsonProperty(JSON_PROPERTY_5G_SRVCC_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set5gSrvccInfo(Model5GSrvccInfo _5gSrvccInfo)
    {
        this._5gSrvccInfo = _5gSrvccInfo;
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
        UeInfo ueInfo = (UeInfo) o;
        return Objects.equals(this.tadsInfo, ueInfo.tadsInfo) && Objects.equals(this.userState, ueInfo.userState)
               && Objects.equals(this._5gSrvccInfo, ueInfo._5gSrvccInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(tadsInfo, userState, _5gSrvccInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeInfo {\n");
        sb.append("    tadsInfo: ").append(toIndentedString(tadsInfo)).append("\n");
        sb.append("    userState: ").append(toIndentedString(userState)).append("\n");
        sb.append("    _5gSrvccInfo: ").append(toIndentedString(_5gSrvccInfo)).append("\n");
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
