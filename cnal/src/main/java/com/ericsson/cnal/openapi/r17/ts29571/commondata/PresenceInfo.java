/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

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
 * If the additionalPraId IE is present, this IE shall state the presence
 * information of the UE for the individual PRA identified by the
 * additionalPraId IE; If the additionalPraId IE is not present, this IE shall
 * state the presence information of the UE for the PRA identified by the praId
 * IE.
 */
@ApiModel(description = "If the additionalPraId IE is present, this IE shall state the presence information of the UE for the individual PRA identified by the additionalPraId IE;  If the additionalPraId IE is not present, this IE shall state the presence information of the UE for the PRA identified by the praId IE. ")
@JsonPropertyOrder({ PresenceInfo.JSON_PROPERTY_PRA_ID,
                     PresenceInfo.JSON_PROPERTY_ADDITIONAL_PRA_ID,
                     PresenceInfo.JSON_PROPERTY_PRESENCE_STATE,
                     PresenceInfo.JSON_PROPERTY_TRACKING_AREA_LIST,
                     PresenceInfo.JSON_PROPERTY_ECGI_LIST,
                     PresenceInfo.JSON_PROPERTY_NCGI_LIST,
                     PresenceInfo.JSON_PROPERTY_GLOBAL_RAN_NODE_ID_LIST,
                     PresenceInfo.JSON_PROPERTY_GLOBALE_NB_ID_LIST })
public class PresenceInfo
{
    public static final String JSON_PROPERTY_PRA_ID = "praId";
    private String praId;

    public static final String JSON_PROPERTY_ADDITIONAL_PRA_ID = "additionalPraId";
    private String additionalPraId;

    public static final String JSON_PROPERTY_PRESENCE_STATE = "presenceState";
    private String presenceState;

    public static final String JSON_PROPERTY_TRACKING_AREA_LIST = "trackingAreaList";
    private List<Tai> trackingAreaList = null;

    public static final String JSON_PROPERTY_ECGI_LIST = "ecgiList";
    private List<Ecgi> ecgiList = null;

    public static final String JSON_PROPERTY_NCGI_LIST = "ncgiList";
    private List<Ncgi> ncgiList = null;

    public static final String JSON_PROPERTY_GLOBAL_RAN_NODE_ID_LIST = "globalRanNodeIdList";
    private List<GlobalRanNodeId> globalRanNodeIdList = null;

    public static final String JSON_PROPERTY_GLOBALE_NB_ID_LIST = "globaleNbIdList";
    private List<GlobalRanNodeId> globaleNbIdList = null;

    public PresenceInfo()
    {
    }

    public PresenceInfo praId(String praId)
    {

        this.praId = praId;
        return this;
    }

    /**
     * Represents an identifier of the Presence Reporting Area (see clause 28.10 of
     * 3GPP TS 23.003. This IE shall be present if the Area of Interest subscribed
     * or reported is a Presence Reporting Area or a Set of Core Network predefined
     * Presence Reporting Areas. When present, it shall be encoded as a string
     * representing an integer in the following ranges: 0 to 8 388 607 for
     * UE-dedicated PRA 8 388 608 to 16 777 215 for Core Network predefined PRA
     * Examples: PRA ID 123 is encoded as \&quot;123\&quot; PRA ID 11 238 660 is
     * encoded as \&quot;11238660\&quot;
     * 
     * @return praId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents an identifier of the Presence Reporting Area (see clause 28.10 of 3GPP  TS 23.003.  This IE shall be present  if the Area of Interest subscribed or reported is a Presence Reporting Area or a Set of Core Network predefined Presence Reporting Areas. When present, it shall be encoded as a string representing an integer in the following ranges: 0 to 8 388 607 for UE-dedicated PRA 8 388 608 to 16 777 215 for Core Network predefined PRA Examples: PRA ID 123 is encoded as \"123\" PRA ID 11 238 660 is encoded as \"11238660\" ")
    @JsonProperty(JSON_PROPERTY_PRA_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPraId()
    {
        return praId;
    }

    @JsonProperty(JSON_PROPERTY_PRA_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPraId(String praId)
    {
        this.praId = praId;
    }

    public PresenceInfo additionalPraId(String additionalPraId)
    {

        this.additionalPraId = additionalPraId;
        return this;
    }

    /**
     * This IE may be present if the praId IE is present and if it contains a PRA
     * identifier referring to a set of Core Network predefined Presence Reporting
     * Areas. When present, this IE shall contain a PRA Identifier of an individual
     * PRA within the Set of Core Network predefined Presence Reporting Areas
     * indicated by the praId IE.
     * 
     * @return additionalPraId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This IE may be present if the praId IE is present and if it contains a PRA identifier referring to a set of Core Network predefined Presence Reporting Areas. When present, this IE shall contain a PRA Identifier of an individual PRA within the Set of Core Network predefined Presence Reporting Areas indicated by the praId IE.  ")
    @JsonProperty(JSON_PROPERTY_ADDITIONAL_PRA_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAdditionalPraId()
    {
        return additionalPraId;
    }

    @JsonProperty(JSON_PROPERTY_ADDITIONAL_PRA_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalPraId(String additionalPraId)
    {
        this.additionalPraId = additionalPraId;
    }

    public PresenceInfo presenceState(String presenceState)
    {

        this.presenceState = presenceState;
        return this;
    }

    /**
     * Possible values are: -IN_AREA: Indicates that the UE is inside or enters the
     * presence reporting area. -OUT_OF_AREA: Indicates that the UE is outside or
     * leaves the presence reporting area -UNKNOW: Indicates it is unknown whether
     * the UE is in the presence reporting area or not -INACTIVE: Indicates that the
     * presence reporting area is inactive in the serving node.
     * 
     * @return presenceState
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: -IN_AREA: Indicates that the UE is inside or enters the presence reporting area. -OUT_OF_AREA: Indicates that the UE is outside or leaves the presence reporting area -UNKNOW: Indicates it is unknown whether the UE is in the presence reporting area or not -INACTIVE: Indicates that the presence reporting area is inactive in the serving node.  ")
    @JsonProperty(JSON_PROPERTY_PRESENCE_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPresenceState()
    {
        return presenceState;
    }

    @JsonProperty(JSON_PROPERTY_PRESENCE_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPresenceState(String presenceState)
    {
        this.presenceState = presenceState;
    }

    public PresenceInfo trackingAreaList(List<Tai> trackingAreaList)
    {

        this.trackingAreaList = trackingAreaList;
        return this;
    }

    public PresenceInfo addTrackingAreaListItem(Tai trackingAreaListItem)
    {
        if (this.trackingAreaList == null)
        {
            this.trackingAreaList = new ArrayList<>();
        }
        this.trackingAreaList.add(trackingAreaListItem);
        return this;
    }

    /**
     * Represents the list of tracking areas that constitutes the area. This IE
     * shall be present if the subscription or the event report is for tracking UE
     * presence in the tracking areas. For non 3GPP access the TAI shall be the
     * N3GPP TAI.
     * 
     * @return trackingAreaList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the list of tracking areas that constitutes the area. This IE shall be present if the subscription or  the event report is for tracking UE presence in the tracking areas. For non 3GPP access the TAI shall be the N3GPP TAI.  ")
    @JsonProperty(JSON_PROPERTY_TRACKING_AREA_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Tai> getTrackingAreaList()
    {
        return trackingAreaList;
    }

    @JsonProperty(JSON_PROPERTY_TRACKING_AREA_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrackingAreaList(List<Tai> trackingAreaList)
    {
        this.trackingAreaList = trackingAreaList;
    }

    public PresenceInfo ecgiList(List<Ecgi> ecgiList)
    {

        this.ecgiList = ecgiList;
        return this;
    }

    public PresenceInfo addEcgiListItem(Ecgi ecgiListItem)
    {
        if (this.ecgiList == null)
        {
            this.ecgiList = new ArrayList<>();
        }
        this.ecgiList.add(ecgiListItem);
        return this;
    }

    /**
     * Represents the list of EUTRAN cell Ids that constitutes the area. This IE
     * shall be present if the Area of Interest subscribed is a list of EUTRAN cell
     * Ids.
     * 
     * @return ecgiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the list of EUTRAN cell Ids that constitutes the area. This IE shall be present if the Area of Interest subscribed is a list of EUTRAN cell Ids.  ")
    @JsonProperty(JSON_PROPERTY_ECGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ecgi> getEcgiList()
    {
        return ecgiList;
    }

    @JsonProperty(JSON_PROPERTY_ECGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcgiList(List<Ecgi> ecgiList)
    {
        this.ecgiList = ecgiList;
    }

    public PresenceInfo ncgiList(List<Ncgi> ncgiList)
    {

        this.ncgiList = ncgiList;
        return this;
    }

    public PresenceInfo addNcgiListItem(Ncgi ncgiListItem)
    {
        if (this.ncgiList == null)
        {
            this.ncgiList = new ArrayList<>();
        }
        this.ncgiList.add(ncgiListItem);
        return this;
    }

    /**
     * Represents the list of NR cell Ids that constitutes the area. This IE shall
     * be present if the Area of Interest subscribed is a list of NR cell Ids.
     * 
     * @return ncgiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the list of NR cell Ids that constitutes the area. This IE shall be present if the Area of Interest subscribed is a list of NR cell Ids.  ")
    @JsonProperty(JSON_PROPERTY_NCGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ncgi> getNcgiList()
    {
        return ncgiList;
    }

    @JsonProperty(JSON_PROPERTY_NCGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNcgiList(List<Ncgi> ncgiList)
    {
        this.ncgiList = ncgiList;
    }

    public PresenceInfo globalRanNodeIdList(List<GlobalRanNodeId> globalRanNodeIdList)
    {

        this.globalRanNodeIdList = globalRanNodeIdList;
        return this;
    }

    public PresenceInfo addGlobalRanNodeIdListItem(GlobalRanNodeId globalRanNodeIdListItem)
    {
        if (this.globalRanNodeIdList == null)
        {
            this.globalRanNodeIdList = new ArrayList<>();
        }
        this.globalRanNodeIdList.add(globalRanNodeIdListItem);
        return this;
    }

    /**
     * Represents the list of NG RAN node identifiers that constitutes the area.
     * This IE shall be present if the Area of Interest subscribed is a list of NG
     * RAN node identifiers.
     * 
     * @return globalRanNodeIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the list of NG RAN node identifiers that constitutes the area. This IE shall be present if the Area of Interest subscribed is a list of NG RAN node identifiers.  ")
    @JsonProperty(JSON_PROPERTY_GLOBAL_RAN_NODE_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<GlobalRanNodeId> getGlobalRanNodeIdList()
    {
        return globalRanNodeIdList;
    }

    @JsonProperty(JSON_PROPERTY_GLOBAL_RAN_NODE_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobalRanNodeIdList(List<GlobalRanNodeId> globalRanNodeIdList)
    {
        this.globalRanNodeIdList = globalRanNodeIdList;
    }

    public PresenceInfo globaleNbIdList(List<GlobalRanNodeId> globaleNbIdList)
    {

        this.globaleNbIdList = globaleNbIdList;
        return this;
    }

    public PresenceInfo addGlobaleNbIdListItem(GlobalRanNodeId globaleNbIdListItem)
    {
        if (this.globaleNbIdList == null)
        {
            this.globaleNbIdList = new ArrayList<>();
        }
        this.globaleNbIdList.add(globaleNbIdListItem);
        return this;
    }

    /**
     * Represents the list of eNodeB identifiers that constitutes the area. This IE
     * shall be present if the Area of Interest subscribed is a list of eNodeB
     * identifiers.
     * 
     * @return globaleNbIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents the list of eNodeB identifiers that constitutes the area. This IE shall be  present if the Area of Interest subscribed is a list of eNodeB identifiers. ")
    @JsonProperty(JSON_PROPERTY_GLOBALE_NB_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<GlobalRanNodeId> getGlobaleNbIdList()
    {
        return globaleNbIdList;
    }

    @JsonProperty(JSON_PROPERTY_GLOBALE_NB_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobaleNbIdList(List<GlobalRanNodeId> globaleNbIdList)
    {
        this.globaleNbIdList = globaleNbIdList;
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
        PresenceInfo presenceInfo = (PresenceInfo) o;
        return Objects.equals(this.praId, presenceInfo.praId) && Objects.equals(this.additionalPraId, presenceInfo.additionalPraId)
               && Objects.equals(this.presenceState, presenceInfo.presenceState) && Objects.equals(this.trackingAreaList, presenceInfo.trackingAreaList)
               && Objects.equals(this.ecgiList, presenceInfo.ecgiList) && Objects.equals(this.ncgiList, presenceInfo.ncgiList)
               && Objects.equals(this.globalRanNodeIdList, presenceInfo.globalRanNodeIdList)
               && Objects.equals(this.globaleNbIdList, presenceInfo.globaleNbIdList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(praId, additionalPraId, presenceState, trackingAreaList, ecgiList, ncgiList, globalRanNodeIdList, globaleNbIdList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PresenceInfo {\n");
        sb.append("    praId: ").append(toIndentedString(praId)).append("\n");
        sb.append("    additionalPraId: ").append(toIndentedString(additionalPraId)).append("\n");
        sb.append("    presenceState: ").append(toIndentedString(presenceState)).append("\n");
        sb.append("    trackingAreaList: ").append(toIndentedString(trackingAreaList)).append("\n");
        sb.append("    ecgiList: ").append(toIndentedString(ecgiList)).append("\n");
        sb.append("    ncgiList: ").append(toIndentedString(ncgiList)).append("\n");
        sb.append("    globalRanNodeIdList: ").append(toIndentedString(globalRanNodeIdList)).append("\n");
        sb.append("    globaleNbIdList: ").append(toIndentedString(globaleNbIdList)).append("\n");
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
