/*
 * 3gpp-traffic-influence
 * API for AF traffic influence   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29522.trafficinfluence;

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
 * Identifies the result of application layer handling.
 */
@ApiModel(description = "Identifies the result of application layer handling.")
@JsonPropertyOrder({ AfResultInfo.JSON_PROPERTY_AF_STATUS,
                     AfResultInfo.JSON_PROPERTY_TRAFFIC_ROUTE,
                     AfResultInfo.JSON_PROPERTY_UP_BUFF_IND,
                     AfResultInfo.JSON_PROPERTY_EAS_IP_REPLACE_INFOS })
public class AfResultInfo
{
    public static final String JSON_PROPERTY_AF_STATUS = "afStatus";
    private String afStatus;

    public static final String JSON_PROPERTY_TRAFFIC_ROUTE = "trafficRoute";
    private JsonNullable<RouteToLocation> trafficRoute = JsonNullable.<RouteToLocation>undefined();

    public static final String JSON_PROPERTY_UP_BUFF_IND = "upBuffInd";
    private Boolean upBuffInd;

    public static final String JSON_PROPERTY_EAS_IP_REPLACE_INFOS = "easIpReplaceInfos";
    private List<EasIpReplacementInfo> easIpReplaceInfos = null;

    public AfResultInfo()
    {
    }

    public AfResultInfo afStatus(String afStatus)
    {

        this.afStatus = afStatus;
        return this;
    }

    /**
     * Possible values are: - SUCCESS: The application layer is ready or the
     * relocation is completed. - TEMPORARY_CONGESTION: The application relocation
     * fails due to temporary congestion. - RELOC_NO_ALLOWED: The application
     * relocation fails because application relocation is not allowed. - OTHER: The
     * application relocation fails due to other reason.
     * 
     * @return afStatus
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Possible values are: - SUCCESS: The application layer is ready or the relocation is completed. - TEMPORARY_CONGESTION: The application relocation fails due to temporary congestion. - RELOC_NO_ALLOWED: The application relocation fails because application relocation is not allowed. - OTHER: The application relocation fails due to other reason. ")
    @JsonProperty(JSON_PROPERTY_AF_STATUS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAfStatus()
    {
        return afStatus;
    }

    @JsonProperty(JSON_PROPERTY_AF_STATUS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAfStatus(String afStatus)
    {
        this.afStatus = afStatus;
    }

    public AfResultInfo trafficRoute(RouteToLocation trafficRoute)
    {
        this.trafficRoute = JsonNullable.<RouteToLocation>of(trafficRoute);

        return this;
    }

    /**
     * Get trafficRoute
     * 
     * @return trafficRoute
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public RouteToLocation getTrafficRoute()
    {
        return trafficRoute.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_ROUTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<RouteToLocation> getTrafficRoute_JsonNullable()
    {
        return trafficRoute;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_ROUTE)
    public void setTrafficRoute_JsonNullable(JsonNullable<RouteToLocation> trafficRoute)
    {
        this.trafficRoute = trafficRoute;
    }

    public void setTrafficRoute(RouteToLocation trafficRoute)
    {
        this.trafficRoute = JsonNullable.<RouteToLocation>of(trafficRoute);
    }

    public AfResultInfo upBuffInd(Boolean upBuffInd)
    {

        this.upBuffInd = upBuffInd;
        return this;
    }

    /**
     * If present and set to \&quot;true\&quot; it indicates that buffering of
     * uplink traffic to the target DNAI is needed.
     * 
     * @return upBuffInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "If present and set to \"true\" it indicates that buffering of uplink traffic to the target DNAI is needed. ")
    @JsonProperty(JSON_PROPERTY_UP_BUFF_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getUpBuffInd()
    {
        return upBuffInd;
    }

    @JsonProperty(JSON_PROPERTY_UP_BUFF_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpBuffInd(Boolean upBuffInd)
    {
        this.upBuffInd = upBuffInd;
    }

    public AfResultInfo easIpReplaceInfos(List<EasIpReplacementInfo> easIpReplaceInfos)
    {

        this.easIpReplaceInfos = easIpReplaceInfos;
        return this;
    }

    public AfResultInfo addEasIpReplaceInfosItem(EasIpReplacementInfo easIpReplaceInfosItem)
    {
        if (this.easIpReplaceInfos == null)
        {
            this.easIpReplaceInfos = new ArrayList<>();
        }
        this.easIpReplaceInfos.add(easIpReplaceInfosItem);
        return this;
    }

    /**
     * Contains EAS IP replacement information.
     * 
     * @return easIpReplaceInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains EAS IP replacement information.")
    @JsonProperty(JSON_PROPERTY_EAS_IP_REPLACE_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<EasIpReplacementInfo> getEasIpReplaceInfos()
    {
        return easIpReplaceInfos;
    }

    @JsonProperty(JSON_PROPERTY_EAS_IP_REPLACE_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEasIpReplaceInfos(List<EasIpReplacementInfo> easIpReplaceInfos)
    {
        this.easIpReplaceInfos = easIpReplaceInfos;
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
        AfResultInfo afResultInfo = (AfResultInfo) o;
        return Objects.equals(this.afStatus, afResultInfo.afStatus) && equalsNullable(this.trafficRoute, afResultInfo.trafficRoute)
               && Objects.equals(this.upBuffInd, afResultInfo.upBuffInd) && Objects.equals(this.easIpReplaceInfos, afResultInfo.easIpReplaceInfos);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(afStatus, hashCodeNullable(trafficRoute), upBuffInd, easIpReplaceInfos);
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
        sb.append("class AfResultInfo {\n");
        sb.append("    afStatus: ").append(toIndentedString(afStatus)).append("\n");
        sb.append("    trafficRoute: ").append(toIndentedString(trafficRoute)).append("\n");
        sb.append("    upBuffInd: ").append(toIndentedString(upBuffInd)).append("\n");
        sb.append("    easIpReplaceInfos: ").append(toIndentedString(easIpReplaceInfos)).append("\n");
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
