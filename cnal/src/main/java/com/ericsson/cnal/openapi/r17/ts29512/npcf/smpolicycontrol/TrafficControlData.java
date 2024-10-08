/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.RouteToLocation;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.EasIpReplacementInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains parameters determining how flows associated with a PCC Rule are
 * treated (e.g. blocked, redirected, etc).
 */
@ApiModel(description = "Contains parameters determining how flows associated with a PCC Rule are treated (e.g. blocked, redirected, etc).")
@JsonPropertyOrder({ TrafficControlData.JSON_PROPERTY_TC_ID,
                     TrafficControlData.JSON_PROPERTY_FLOW_STATUS,
                     TrafficControlData.JSON_PROPERTY_REDIRECT_INFO,
                     TrafficControlData.JSON_PROPERTY_ADD_REDIRECT_INFO,
                     TrafficControlData.JSON_PROPERTY_MUTE_NOTIF,
                     TrafficControlData.JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_DL,
                     TrafficControlData.JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_UL,
                     TrafficControlData.JSON_PROPERTY_ROUTE_TO_LOCS,
                     TrafficControlData.JSON_PROPERTY_MAX_ALLOWED_UP_LAT,
                     TrafficControlData.JSON_PROPERTY_EAS_IP_REPLACE_INFOS,
                     TrafficControlData.JSON_PROPERTY_TRAFF_CORRE_IND,
                     TrafficControlData.JSON_PROPERTY_SIM_CONN_IND,
                     TrafficControlData.JSON_PROPERTY_SIM_CONN_TERM,
                     TrafficControlData.JSON_PROPERTY_UP_PATH_CHG_EVENT,
                     TrafficControlData.JSON_PROPERTY_STEER_FUN,
                     TrafficControlData.JSON_PROPERTY_STEER_MODE_DL,
                     TrafficControlData.JSON_PROPERTY_STEER_MODE_UL,
                     TrafficControlData.JSON_PROPERTY_MUL_ACC_CTRL })
public class TrafficControlData
{
    public static final String JSON_PROPERTY_TC_ID = "tcId";
    private String tcId;

    public static final String JSON_PROPERTY_FLOW_STATUS = "flowStatus";
    private String flowStatus;

    public static final String JSON_PROPERTY_REDIRECT_INFO = "redirectInfo";
    private RedirectInformation redirectInfo;

    public static final String JSON_PROPERTY_ADD_REDIRECT_INFO = "addRedirectInfo";
    private List<RedirectInformation> addRedirectInfo = null;

    public static final String JSON_PROPERTY_MUTE_NOTIF = "muteNotif";
    private Boolean muteNotif;

    public static final String JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_DL = "trafficSteeringPolIdDl";
    private JsonNullable<String> trafficSteeringPolIdDl = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_UL = "trafficSteeringPolIdUl";
    private JsonNullable<String> trafficSteeringPolIdUl = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_ROUTE_TO_LOCS = "routeToLocs";
    private JsonNullable<List<RouteToLocation>> routeToLocs = JsonNullable.<List<RouteToLocation>>undefined();

    public static final String JSON_PROPERTY_MAX_ALLOWED_UP_LAT = "maxAllowedUpLat";
    private JsonNullable<Integer> maxAllowedUpLat = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_EAS_IP_REPLACE_INFOS = "easIpReplaceInfos";
    private JsonNullable<List<EasIpReplacementInfo>> easIpReplaceInfos = JsonNullable.<List<EasIpReplacementInfo>>undefined();

    public static final String JSON_PROPERTY_TRAFF_CORRE_IND = "traffCorreInd";
    private Boolean traffCorreInd;

    public static final String JSON_PROPERTY_SIM_CONN_IND = "simConnInd";
    private Boolean simConnInd;

    public static final String JSON_PROPERTY_SIM_CONN_TERM = "simConnTerm";
    private Integer simConnTerm;

    public static final String JSON_PROPERTY_UP_PATH_CHG_EVENT = "upPathChgEvent";
    private JsonNullable<UpPathChgEvent> upPathChgEvent = JsonNullable.<UpPathChgEvent>undefined();

    public static final String JSON_PROPERTY_STEER_FUN = "steerFun";
    private String steerFun;

    public static final String JSON_PROPERTY_STEER_MODE_DL = "steerModeDl";
    private SteeringMode steerModeDl;

    public static final String JSON_PROPERTY_STEER_MODE_UL = "steerModeUl";
    private SteeringMode steerModeUl;

    public static final String JSON_PROPERTY_MUL_ACC_CTRL = "mulAccCtrl";
    private String mulAccCtrl;

    public TrafficControlData()
    {
    }

    public TrafficControlData tcId(String tcId)
    {

        this.tcId = tcId;
        return this;
    }

    /**
     * Univocally identifies the traffic control policy data within a PDU session.
     * 
     * @return tcId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Univocally identifies the traffic control policy data within a PDU session.")
    @JsonProperty(JSON_PROPERTY_TC_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTcId()
    {
        return tcId;
    }

    @JsonProperty(JSON_PROPERTY_TC_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTcId(String tcId)
    {
        this.tcId = tcId;
    }

    public TrafficControlData flowStatus(String flowStatus)
    {

        this.flowStatus = flowStatus;
        return this;
    }

    /**
     * Describes whether the IP flow(s) are enabled or disabled.
     * 
     * @return flowStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Describes whether the IP flow(s) are enabled or disabled.")
    @JsonProperty(JSON_PROPERTY_FLOW_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFlowStatus()
    {
        return flowStatus;
    }

    @JsonProperty(JSON_PROPERTY_FLOW_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFlowStatus(String flowStatus)
    {
        this.flowStatus = flowStatus;
    }

    public TrafficControlData redirectInfo(RedirectInformation redirectInfo)
    {

        this.redirectInfo = redirectInfo;
        return this;
    }

    /**
     * Get redirectInfo
     * 
     * @return redirectInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REDIRECT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RedirectInformation getRedirectInfo()
    {
        return redirectInfo;
    }

    @JsonProperty(JSON_PROPERTY_REDIRECT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRedirectInfo(RedirectInformation redirectInfo)
    {
        this.redirectInfo = redirectInfo;
    }

    public TrafficControlData addRedirectInfo(List<RedirectInformation> addRedirectInfo)
    {

        this.addRedirectInfo = addRedirectInfo;
        return this;
    }

    public TrafficControlData addAddRedirectInfoItem(RedirectInformation addRedirectInfoItem)
    {
        if (this.addRedirectInfo == null)
        {
            this.addRedirectInfo = new ArrayList<>();
        }
        this.addRedirectInfo.add(addRedirectInfoItem);
        return this;
    }

    /**
     * Get addRedirectInfo
     * 
     * @return addRedirectInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADD_REDIRECT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RedirectInformation> getAddRedirectInfo()
    {
        return addRedirectInfo;
    }

    @JsonProperty(JSON_PROPERTY_ADD_REDIRECT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddRedirectInfo(List<RedirectInformation> addRedirectInfo)
    {
        this.addRedirectInfo = addRedirectInfo;
    }

    public TrafficControlData muteNotif(Boolean muteNotif)
    {

        this.muteNotif = muteNotif;
        return this;
    }

    /**
     * Indicates whether applicat&#39;on&#39;s start or stop notification is to be
     * muted.
     * 
     * @return muteNotif
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether applicat'on's start or stop notification is to be muted.")
    @JsonProperty(JSON_PROPERTY_MUTE_NOTIF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getMuteNotif()
    {
        return muteNotif;
    }

    @JsonProperty(JSON_PROPERTY_MUTE_NOTIF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMuteNotif(Boolean muteNotif)
    {
        this.muteNotif = muteNotif;
    }

    public TrafficControlData trafficSteeringPolIdDl(String trafficSteeringPolIdDl)
    {
        this.trafficSteeringPolIdDl = JsonNullable.<String>of(trafficSteeringPolIdDl);

        return this;
    }

    /**
     * Reference to a pre-configured traffic steering policy for downlink traffic at
     * the SMF.
     * 
     * @return trafficSteeringPolIdDl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Reference to a pre-configured traffic steering policy for downlink traffic at the SMF.")
    @JsonIgnore

    public String getTrafficSteeringPolIdDl()
    {
        return trafficSteeringPolIdDl.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getTrafficSteeringPolIdDl_JsonNullable()
    {
        return trafficSteeringPolIdDl;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_DL)
    public void setTrafficSteeringPolIdDl_JsonNullable(JsonNullable<String> trafficSteeringPolIdDl)
    {
        this.trafficSteeringPolIdDl = trafficSteeringPolIdDl;
    }

    public void setTrafficSteeringPolIdDl(String trafficSteeringPolIdDl)
    {
        this.trafficSteeringPolIdDl = JsonNullable.<String>of(trafficSteeringPolIdDl);
    }

    public TrafficControlData trafficSteeringPolIdUl(String trafficSteeringPolIdUl)
    {
        this.trafficSteeringPolIdUl = JsonNullable.<String>of(trafficSteeringPolIdUl);

        return this;
    }

    /**
     * Reference to a pre-configured traffic steering policy for uplink traffic at
     * the SMF.
     * 
     * @return trafficSteeringPolIdUl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Reference to a pre-configured traffic steering policy for uplink traffic at the SMF.")
    @JsonIgnore

    public String getTrafficSteeringPolIdUl()
    {
        return trafficSteeringPolIdUl.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_UL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getTrafficSteeringPolIdUl_JsonNullable()
    {
        return trafficSteeringPolIdUl;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_STEERING_POL_ID_UL)
    public void setTrafficSteeringPolIdUl_JsonNullable(JsonNullable<String> trafficSteeringPolIdUl)
    {
        this.trafficSteeringPolIdUl = trafficSteeringPolIdUl;
    }

    public void setTrafficSteeringPolIdUl(String trafficSteeringPolIdUl)
    {
        this.trafficSteeringPolIdUl = JsonNullable.<String>of(trafficSteeringPolIdUl);
    }

    public TrafficControlData routeToLocs(List<RouteToLocation> routeToLocs)
    {
        this.routeToLocs = JsonNullable.<List<RouteToLocation>>of(routeToLocs);

        return this;
    }

    public TrafficControlData addRouteToLocsItem(RouteToLocation routeToLocsItem)
    {
        if (this.routeToLocs == null || !this.routeToLocs.isPresent())
        {
            this.routeToLocs = JsonNullable.<List<RouteToLocation>>of(new ArrayList<>());
        }
        try
        {
            this.routeToLocs.get().add(routeToLocsItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * A list of location which the traffic shall be routed to for the AF request
     * 
     * @return routeToLocs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of location which the traffic shall be routed to for the AF request")
    @JsonIgnore

    public List<RouteToLocation> getRouteToLocs()
    {
        return routeToLocs.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_ROUTE_TO_LOCS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<RouteToLocation>> getRouteToLocs_JsonNullable()
    {
        return routeToLocs;
    }

    @JsonProperty(JSON_PROPERTY_ROUTE_TO_LOCS)
    public void setRouteToLocs_JsonNullable(JsonNullable<List<RouteToLocation>> routeToLocs)
    {
        this.routeToLocs = routeToLocs;
    }

    public void setRouteToLocs(List<RouteToLocation> routeToLocs)
    {
        this.routeToLocs = JsonNullable.<List<RouteToLocation>>of(routeToLocs);
    }

    public TrafficControlData maxAllowedUpLat(Integer maxAllowedUpLat)
    {
        this.maxAllowedUpLat = JsonNullable.<Integer>of(maxAllowedUpLat);

        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible with
     * the OpenAPI &#39;nullable: true&#39; property. minimum: 0
     * 
     * @return maxAllowedUpLat
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible with the OpenAPI 'nullable: true' property. ")
    @JsonIgnore

    public Integer getMaxAllowedUpLat()
    {
        return maxAllowedUpLat.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_MAX_ALLOWED_UP_LAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getMaxAllowedUpLat_JsonNullable()
    {
        return maxAllowedUpLat;
    }

    @JsonProperty(JSON_PROPERTY_MAX_ALLOWED_UP_LAT)
    public void setMaxAllowedUpLat_JsonNullable(JsonNullable<Integer> maxAllowedUpLat)
    {
        this.maxAllowedUpLat = maxAllowedUpLat;
    }

    public void setMaxAllowedUpLat(Integer maxAllowedUpLat)
    {
        this.maxAllowedUpLat = JsonNullable.<Integer>of(maxAllowedUpLat);
    }

    public TrafficControlData easIpReplaceInfos(List<EasIpReplacementInfo> easIpReplaceInfos)
    {
        this.easIpReplaceInfos = JsonNullable.<List<EasIpReplacementInfo>>of(easIpReplaceInfos);

        return this;
    }

    public TrafficControlData addEasIpReplaceInfosItem(EasIpReplacementInfo easIpReplaceInfosItem)
    {
        if (this.easIpReplaceInfos == null || !this.easIpReplaceInfos.isPresent())
        {
            this.easIpReplaceInfos = JsonNullable.<List<EasIpReplacementInfo>>of(new ArrayList<>());
        }
        try
        {
            this.easIpReplaceInfos.get().add(easIpReplaceInfosItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * Contains EAS IP replacement information.
     * 
     * @return easIpReplaceInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains EAS IP replacement information.")
    @JsonIgnore

    public List<EasIpReplacementInfo> getEasIpReplaceInfos()
    {
        return easIpReplaceInfos.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_EAS_IP_REPLACE_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<EasIpReplacementInfo>> getEasIpReplaceInfos_JsonNullable()
    {
        return easIpReplaceInfos;
    }

    @JsonProperty(JSON_PROPERTY_EAS_IP_REPLACE_INFOS)
    public void setEasIpReplaceInfos_JsonNullable(JsonNullable<List<EasIpReplacementInfo>> easIpReplaceInfos)
    {
        this.easIpReplaceInfos = easIpReplaceInfos;
    }

    public void setEasIpReplaceInfos(List<EasIpReplacementInfo> easIpReplaceInfos)
    {
        this.easIpReplaceInfos = JsonNullable.<List<EasIpReplacementInfo>>of(easIpReplaceInfos);
    }

    public TrafficControlData traffCorreInd(Boolean traffCorreInd)
    {

        this.traffCorreInd = traffCorreInd;
        return this;
    }

    /**
     * Get traffCorreInd
     * 
     * @return traffCorreInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TRAFF_CORRE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getTraffCorreInd()
    {
        return traffCorreInd;
    }

    @JsonProperty(JSON_PROPERTY_TRAFF_CORRE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTraffCorreInd(Boolean traffCorreInd)
    {
        this.traffCorreInd = traffCorreInd;
    }

    public TrafficControlData simConnInd(Boolean simConnInd)
    {

        this.simConnInd = simConnInd;
        return this;
    }

    /**
     * Indicates whether simultaneous connectivity should be temporarily maintained
     * for the source and target PSA.
     * 
     * @return simConnInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether simultaneous connectivity should be temporarily maintained for the source and target PSA.")
    @JsonProperty(JSON_PROPERTY_SIM_CONN_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSimConnInd()
    {
        return simConnInd;
    }

    @JsonProperty(JSON_PROPERTY_SIM_CONN_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSimConnInd(Boolean simConnInd)
    {
        this.simConnInd = simConnInd;
    }

    public TrafficControlData simConnTerm(Integer simConnTerm)
    {

        this.simConnTerm = simConnTerm;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return simConnTerm
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_SIM_CONN_TERM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSimConnTerm()
    {
        return simConnTerm;
    }

    @JsonProperty(JSON_PROPERTY_SIM_CONN_TERM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSimConnTerm(Integer simConnTerm)
    {
        this.simConnTerm = simConnTerm;
    }

    public TrafficControlData upPathChgEvent(UpPathChgEvent upPathChgEvent)
    {
        this.upPathChgEvent = JsonNullable.<UpPathChgEvent>of(upPathChgEvent);

        return this;
    }

    /**
     * Get upPathChgEvent
     * 
     * @return upPathChgEvent
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public UpPathChgEvent getUpPathChgEvent()
    {
        return upPathChgEvent.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_UP_PATH_CHG_EVENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<UpPathChgEvent> getUpPathChgEvent_JsonNullable()
    {
        return upPathChgEvent;
    }

    @JsonProperty(JSON_PROPERTY_UP_PATH_CHG_EVENT)
    public void setUpPathChgEvent_JsonNullable(JsonNullable<UpPathChgEvent> upPathChgEvent)
    {
        this.upPathChgEvent = upPathChgEvent;
    }

    public void setUpPathChgEvent(UpPathChgEvent upPathChgEvent)
    {
        this.upPathChgEvent = JsonNullable.<UpPathChgEvent>of(upPathChgEvent);
    }

    public TrafficControlData steerFun(String steerFun)
    {

        this.steerFun = steerFun;
        return this;
    }

    /**
     * Possible values are - MPTCP: Indicates that PCF authorizes the MPTCP
     * functionality to support traffic steering, switching and splitting. -
     * ATSSS_LL: Indicates that PCF authorizes the ATSSS-LL functionality to support
     * traffic steering, switching and splitting.
     * 
     * @return steerFun
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are   - MPTCP: Indicates that PCF authorizes the MPTCP functionality to support traffic steering, switching and splitting.   - ATSSS_LL: Indicates that PCF authorizes the ATSSS-LL functionality to support traffic steering, switching and splitting. ")
    @JsonProperty(JSON_PROPERTY_STEER_FUN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSteerFun()
    {
        return steerFun;
    }

    @JsonProperty(JSON_PROPERTY_STEER_FUN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSteerFun(String steerFun)
    {
        this.steerFun = steerFun;
    }

    public TrafficControlData steerModeDl(SteeringMode steerModeDl)
    {

        this.steerModeDl = steerModeDl;
        return this;
    }

    /**
     * Get steerModeDl
     * 
     * @return steerModeDl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_STEER_MODE_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SteeringMode getSteerModeDl()
    {
        return steerModeDl;
    }

    @JsonProperty(JSON_PROPERTY_STEER_MODE_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSteerModeDl(SteeringMode steerModeDl)
    {
        this.steerModeDl = steerModeDl;
    }

    public TrafficControlData steerModeUl(SteeringMode steerModeUl)
    {

        this.steerModeUl = steerModeUl;
        return this;
    }

    /**
     * Get steerModeUl
     * 
     * @return steerModeUl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_STEER_MODE_UL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SteeringMode getSteerModeUl()
    {
        return steerModeUl;
    }

    @JsonProperty(JSON_PROPERTY_STEER_MODE_UL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSteerModeUl(SteeringMode steerModeUl)
    {
        this.steerModeUl = steerModeUl;
    }

    public TrafficControlData mulAccCtrl(String mulAccCtrl)
    {

        this.mulAccCtrl = mulAccCtrl;
        return this;
    }

    /**
     * Indicates whether the service data flow, corresponding to the service data
     * flow template, is allowed or not allowed.
     * 
     * @return mulAccCtrl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether the service data flow, corresponding to the service data flow template, is allowed or not allowed.")
    @JsonProperty(JSON_PROPERTY_MUL_ACC_CTRL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMulAccCtrl()
    {
        return mulAccCtrl;
    }

    @JsonProperty(JSON_PROPERTY_MUL_ACC_CTRL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMulAccCtrl(String mulAccCtrl)
    {
        this.mulAccCtrl = mulAccCtrl;
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
        TrafficControlData trafficControlData = (TrafficControlData) o;
        return Objects.equals(this.tcId, trafficControlData.tcId) && Objects.equals(this.flowStatus, trafficControlData.flowStatus)
               && Objects.equals(this.redirectInfo, trafficControlData.redirectInfo) && Objects.equals(this.addRedirectInfo, trafficControlData.addRedirectInfo)
               && Objects.equals(this.muteNotif, trafficControlData.muteNotif)
               && equalsNullable(this.trafficSteeringPolIdDl, trafficControlData.trafficSteeringPolIdDl)
               && equalsNullable(this.trafficSteeringPolIdUl, trafficControlData.trafficSteeringPolIdUl)
               && equalsNullable(this.routeToLocs, trafficControlData.routeToLocs) && equalsNullable(this.maxAllowedUpLat, trafficControlData.maxAllowedUpLat)
               && equalsNullable(this.easIpReplaceInfos, trafficControlData.easIpReplaceInfos)
               && Objects.equals(this.traffCorreInd, trafficControlData.traffCorreInd) && Objects.equals(this.simConnInd, trafficControlData.simConnInd)
               && Objects.equals(this.simConnTerm, trafficControlData.simConnTerm) && equalsNullable(this.upPathChgEvent, trafficControlData.upPathChgEvent)
               && Objects.equals(this.steerFun, trafficControlData.steerFun) && Objects.equals(this.steerModeDl, trafficControlData.steerModeDl)
               && Objects.equals(this.steerModeUl, trafficControlData.steerModeUl) && Objects.equals(this.mulAccCtrl, trafficControlData.mulAccCtrl);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(tcId,
                            flowStatus,
                            redirectInfo,
                            addRedirectInfo,
                            muteNotif,
                            hashCodeNullable(trafficSteeringPolIdDl),
                            hashCodeNullable(trafficSteeringPolIdUl),
                            hashCodeNullable(routeToLocs),
                            hashCodeNullable(maxAllowedUpLat),
                            hashCodeNullable(easIpReplaceInfos),
                            traffCorreInd,
                            simConnInd,
                            simConnTerm,
                            hashCodeNullable(upPathChgEvent),
                            steerFun,
                            steerModeDl,
                            steerModeUl,
                            mulAccCtrl);
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrafficControlData {\n");
        sb.append("    tcId: ").append(toIndentedString(tcId)).append("\n");
        sb.append("    flowStatus: ").append(toIndentedString(flowStatus)).append("\n");
        sb.append("    redirectInfo: ").append(toIndentedString(redirectInfo)).append("\n");
        sb.append("    addRedirectInfo: ").append(toIndentedString(addRedirectInfo)).append("\n");
        sb.append("    muteNotif: ").append(toIndentedString(muteNotif)).append("\n");
        sb.append("    trafficSteeringPolIdDl: ").append(toIndentedString(trafficSteeringPolIdDl)).append("\n");
        sb.append("    trafficSteeringPolIdUl: ").append(toIndentedString(trafficSteeringPolIdUl)).append("\n");
        sb.append("    routeToLocs: ").append(toIndentedString(routeToLocs)).append("\n");
        sb.append("    maxAllowedUpLat: ").append(toIndentedString(maxAllowedUpLat)).append("\n");
        sb.append("    easIpReplaceInfos: ").append(toIndentedString(easIpReplaceInfos)).append("\n");
        sb.append("    traffCorreInd: ").append(toIndentedString(traffCorreInd)).append("\n");
        sb.append("    simConnInd: ").append(toIndentedString(simConnInd)).append("\n");
        sb.append("    simConnTerm: ").append(toIndentedString(simConnTerm)).append("\n");
        sb.append("    upPathChgEvent: ").append(toIndentedString(upPathChgEvent)).append("\n");
        sb.append("    steerFun: ").append(toIndentedString(steerFun)).append("\n");
        sb.append("    steerModeDl: ").append(toIndentedString(steerModeDl)).append("\n");
        sb.append("    steerModeUl: ").append(toIndentedString(steerModeUl)).append("\n");
        sb.append("    mulAccCtrl: ").append(toIndentedString(mulAccCtrl)).append("\n");
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
