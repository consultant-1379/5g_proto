/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.AmfEvent;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.AmfEventMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * AMF event subscription extended with additional information received for the
 * subscription
 */
@ApiModel(description = "AMF event subscription extended with additional information received for the subscription")
@JsonPropertyOrder({ ExtAmfEventSubscription.JSON_PROPERTY_EVENT_LIST,
                     ExtAmfEventSubscription.JSON_PROPERTY_EVENT_NOTIFY_URI,
                     ExtAmfEventSubscription.JSON_PROPERTY_NOTIFY_CORRELATION_ID,
                     ExtAmfEventSubscription.JSON_PROPERTY_NF_ID,
                     ExtAmfEventSubscription.JSON_PROPERTY_SUBS_CHANGE_NOTIFY_URI,
                     ExtAmfEventSubscription.JSON_PROPERTY_SUBS_CHANGE_NOTIFY_CORRELATION_ID,
                     ExtAmfEventSubscription.JSON_PROPERTY_SUPI,
                     ExtAmfEventSubscription.JSON_PROPERTY_GROUP_ID,
                     ExtAmfEventSubscription.JSON_PROPERTY_EXCLUDE_SUPI_LIST,
                     ExtAmfEventSubscription.JSON_PROPERTY_EXCLUDE_GPSI_LIST,
                     ExtAmfEventSubscription.JSON_PROPERTY_INCLUDE_SUPI_LIST,
                     ExtAmfEventSubscription.JSON_PROPERTY_INCLUDE_GPSI_LIST,
                     ExtAmfEventSubscription.JSON_PROPERTY_GPSI,
                     ExtAmfEventSubscription.JSON_PROPERTY_PEI,
                     ExtAmfEventSubscription.JSON_PROPERTY_ANY_U_E,
                     ExtAmfEventSubscription.JSON_PROPERTY_OPTIONS,
                     ExtAmfEventSubscription.JSON_PROPERTY_SOURCE_NF_TYPE,
                     ExtAmfEventSubscription.JSON_PROPERTY_BINDING_INFO,
                     ExtAmfEventSubscription.JSON_PROPERTY_SUBSCRIBING_NF_TYPE,
                     ExtAmfEventSubscription.JSON_PROPERTY_EVENT_SYNC_IND,
                     ExtAmfEventSubscription.JSON_PROPERTY_NF_CONSUMER_INFO,
                     ExtAmfEventSubscription.JSON_PROPERTY_AOI_STATE_LIST })
public class ExtAmfEventSubscription
{
    public static final String JSON_PROPERTY_EVENT_LIST = "eventList";
    private List<AmfEvent> eventList = new ArrayList<>();

    public static final String JSON_PROPERTY_EVENT_NOTIFY_URI = "eventNotifyUri";
    private String eventNotifyUri;

    public static final String JSON_PROPERTY_NOTIFY_CORRELATION_ID = "notifyCorrelationId";
    private String notifyCorrelationId;

    public static final String JSON_PROPERTY_NF_ID = "nfId";
    private UUID nfId;

    public static final String JSON_PROPERTY_SUBS_CHANGE_NOTIFY_URI = "subsChangeNotifyUri";
    private String subsChangeNotifyUri;

    public static final String JSON_PROPERTY_SUBS_CHANGE_NOTIFY_CORRELATION_ID = "subsChangeNotifyCorrelationId";
    private String subsChangeNotifyCorrelationId;

    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_GROUP_ID = "groupId";
    private String groupId;

    public static final String JSON_PROPERTY_EXCLUDE_SUPI_LIST = "excludeSupiList";
    private List<String> excludeSupiList = null;

    public static final String JSON_PROPERTY_EXCLUDE_GPSI_LIST = "excludeGpsiList";
    private List<String> excludeGpsiList = null;

    public static final String JSON_PROPERTY_INCLUDE_SUPI_LIST = "includeSupiList";
    private List<String> includeSupiList = null;

    public static final String JSON_PROPERTY_INCLUDE_GPSI_LIST = "includeGpsiList";
    private List<String> includeGpsiList = null;

    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_PEI = "pei";
    private String pei;

    public static final String JSON_PROPERTY_ANY_U_E = "anyUE";
    private Boolean anyUE;

    public static final String JSON_PROPERTY_OPTIONS = "options";
    private AmfEventMode options;

    public static final String JSON_PROPERTY_SOURCE_NF_TYPE = "sourceNfType";
    private String sourceNfType;

    public static final String JSON_PROPERTY_BINDING_INFO = "bindingInfo";
    private List<String> bindingInfo = null;

    public static final String JSON_PROPERTY_SUBSCRIBING_NF_TYPE = "subscribingNfType";
    private String subscribingNfType;

    public static final String JSON_PROPERTY_EVENT_SYNC_IND = "eventSyncInd";
    private Boolean eventSyncInd;

    public static final String JSON_PROPERTY_NF_CONSUMER_INFO = "nfConsumerInfo";
    private List<String> nfConsumerInfo = null;

    public static final String JSON_PROPERTY_AOI_STATE_LIST = "aoiStateList";
    private Map<String, AreaOfInterestEventState> aoiStateList = null;

    public ExtAmfEventSubscription()
    {
    }

    public ExtAmfEventSubscription eventList(List<AmfEvent> eventList)
    {

        this.eventList = eventList;
        return this;
    }

    public ExtAmfEventSubscription addEventListItem(AmfEvent eventListItem)
    {
        this.eventList.add(eventListItem);
        return this;
    }

    /**
     * Get eventList
     * 
     * @return eventList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<AmfEvent> getEventList()
    {
        return eventList;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventList(List<AmfEvent> eventList)
    {
        this.eventList = eventList;
    }

    public ExtAmfEventSubscription eventNotifyUri(String eventNotifyUri)
    {

        this.eventNotifyUri = eventNotifyUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return eventNotifyUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEventNotifyUri()
    {
        return eventNotifyUri;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventNotifyUri(String eventNotifyUri)
    {
        this.eventNotifyUri = eventNotifyUri;
    }

    public ExtAmfEventSubscription notifyCorrelationId(String notifyCorrelationId)
    {

        this.notifyCorrelationId = notifyCorrelationId;
        return this;
    }

    /**
     * Get notifyCorrelationId
     * 
     * @return notifyCorrelationId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getNotifyCorrelationId()
    {
        return notifyCorrelationId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNotifyCorrelationId(String notifyCorrelationId)
    {
        this.notifyCorrelationId = notifyCorrelationId;
    }

    public ExtAmfEventSubscription nfId(UUID nfId)
    {

        this.nfId = nfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return nfId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getNfId()
    {
        return nfId;
    }

    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNfId(UUID nfId)
    {
        this.nfId = nfId;
    }

    public ExtAmfEventSubscription subsChangeNotifyUri(String subsChangeNotifyUri)
    {

        this.subsChangeNotifyUri = subsChangeNotifyUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return subsChangeNotifyUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_SUBS_CHANGE_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubsChangeNotifyUri()
    {
        return subsChangeNotifyUri;
    }

    @JsonProperty(JSON_PROPERTY_SUBS_CHANGE_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubsChangeNotifyUri(String subsChangeNotifyUri)
    {
        this.subsChangeNotifyUri = subsChangeNotifyUri;
    }

    public ExtAmfEventSubscription subsChangeNotifyCorrelationId(String subsChangeNotifyCorrelationId)
    {

        this.subsChangeNotifyCorrelationId = subsChangeNotifyCorrelationId;
        return this;
    }

    /**
     * Get subsChangeNotifyCorrelationId
     * 
     * @return subsChangeNotifyCorrelationId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBS_CHANGE_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubsChangeNotifyCorrelationId()
    {
        return subsChangeNotifyCorrelationId;
    }

    @JsonProperty(JSON_PROPERTY_SUBS_CHANGE_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubsChangeNotifyCorrelationId(String subsChangeNotifyCorrelationId)
    {
        this.subsChangeNotifyCorrelationId = subsChangeNotifyCorrelationId;
    }

    public ExtAmfEventSubscription supi(String supi)
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

    public ExtAmfEventSubscription groupId(String groupId)
    {

        this.groupId = groupId;
        return this;
    }

    /**
     * String identifying a group of devices network internal globally unique ID
     * which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS
     * 23.003.
     * 
     * @return groupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a group of devices network internal globally unique ID which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS 23.003.  ")
    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGroupId()
    {
        return groupId;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public ExtAmfEventSubscription excludeSupiList(List<String> excludeSupiList)
    {

        this.excludeSupiList = excludeSupiList;
        return this;
    }

    public ExtAmfEventSubscription addExcludeSupiListItem(String excludeSupiListItem)
    {
        if (this.excludeSupiList == null)
        {
            this.excludeSupiList = new ArrayList<>();
        }
        this.excludeSupiList.add(excludeSupiListItem);
        return this;
    }

    /**
     * Get excludeSupiList
     * 
     * @return excludeSupiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getExcludeSupiList()
    {
        return excludeSupiList;
    }

    @JsonProperty(JSON_PROPERTY_EXCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExcludeSupiList(List<String> excludeSupiList)
    {
        this.excludeSupiList = excludeSupiList;
    }

    public ExtAmfEventSubscription excludeGpsiList(List<String> excludeGpsiList)
    {

        this.excludeGpsiList = excludeGpsiList;
        return this;
    }

    public ExtAmfEventSubscription addExcludeGpsiListItem(String excludeGpsiListItem)
    {
        if (this.excludeGpsiList == null)
        {
            this.excludeGpsiList = new ArrayList<>();
        }
        this.excludeGpsiList.add(excludeGpsiListItem);
        return this;
    }

    /**
     * Get excludeGpsiList
     * 
     * @return excludeGpsiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getExcludeGpsiList()
    {
        return excludeGpsiList;
    }

    @JsonProperty(JSON_PROPERTY_EXCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExcludeGpsiList(List<String> excludeGpsiList)
    {
        this.excludeGpsiList = excludeGpsiList;
    }

    public ExtAmfEventSubscription includeSupiList(List<String> includeSupiList)
    {

        this.includeSupiList = includeSupiList;
        return this;
    }

    public ExtAmfEventSubscription addIncludeSupiListItem(String includeSupiListItem)
    {
        if (this.includeSupiList == null)
        {
            this.includeSupiList = new ArrayList<>();
        }
        this.includeSupiList.add(includeSupiListItem);
        return this;
    }

    /**
     * Get includeSupiList
     * 
     * @return includeSupiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIncludeSupiList()
    {
        return includeSupiList;
    }

    @JsonProperty(JSON_PROPERTY_INCLUDE_SUPI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeSupiList(List<String> includeSupiList)
    {
        this.includeSupiList = includeSupiList;
    }

    public ExtAmfEventSubscription includeGpsiList(List<String> includeGpsiList)
    {

        this.includeGpsiList = includeGpsiList;
        return this;
    }

    public ExtAmfEventSubscription addIncludeGpsiListItem(String includeGpsiListItem)
    {
        if (this.includeGpsiList == null)
        {
            this.includeGpsiList = new ArrayList<>();
        }
        this.includeGpsiList.add(includeGpsiListItem);
        return this;
    }

    /**
     * Get includeGpsiList
     * 
     * @return includeGpsiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIncludeGpsiList()
    {
        return includeGpsiList;
    }

    @JsonProperty(JSON_PROPERTY_INCLUDE_GPSI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeGpsiList(List<String> includeGpsiList)
    {
        this.includeGpsiList = includeGpsiList;
    }

    public ExtAmfEventSubscription gpsi(String gpsi)
    {

        this.gpsi = gpsi;
        return this;
    }

    /**
     * String identifying a Gpsi shall contain either an External Id or an MSISDN.
     * It shall be formatted as follows -External Identifier&#x3D;
     * \&quot;extid-&#39;extid&#39;, where &#39;extid&#39; shall be formatted
     * according to clause 19.7.2 of 3GPP TS 23.003 that describes an External
     * Identifier.
     * 
     * @return gpsi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a Gpsi shall contain either an External Id or an MSISDN.  It shall be formatted as follows -External Identifier= \"extid-'extid', where 'extid'  shall be formatted according to clause 19.7.2 of 3GPP TS 23.003 that describes an  External Identifier.  ")
    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGpsi()
    {
        return gpsi;
    }

    @JsonProperty(JSON_PROPERTY_GPSI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsi(String gpsi)
    {
        this.gpsi = gpsi;
    }

    public ExtAmfEventSubscription pei(String pei)
    {

        this.pei = pei;
        return this;
    }

    /**
     * String representing a Permanent Equipment Identifier that may contain - an
     * IMEI or IMEISV, as specified in clause 6.2 of 3GPP TS 23.003; a MAC address
     * for a 5G-RG or FN-RG via wireline access, with an indication that this
     * address cannot be trusted for regulatory purpose if this address cannot be
     * used as an Equipment Identifier of the FN-RG, as specified in clause 4.7.7 of
     * 3GPP TS23.316. Examples are imei-012345678901234 or imeisv-0123456789012345.
     * 
     * @return pei
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a Permanent Equipment Identifier that may contain - an IMEI or IMEISV, as  specified in clause 6.2 of 3GPP TS 23.003; a MAC address for a 5G-RG or FN-RG via  wireline  access, with an indication that this address cannot be trusted for regulatory purpose if this  address cannot be used as an Equipment Identifier of the FN-RG, as specified in clause 4.7.7  of 3GPP TS23.316. Examples are imei-012345678901234 or imeisv-0123456789012345.  ")
    @JsonProperty(JSON_PROPERTY_PEI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPei()
    {
        return pei;
    }

    @JsonProperty(JSON_PROPERTY_PEI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPei(String pei)
    {
        this.pei = pei;
    }

    public ExtAmfEventSubscription anyUE(Boolean anyUE)
    {

        this.anyUE = anyUE;
        return this;
    }

    /**
     * Get anyUE
     * 
     * @return anyUE
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ANY_U_E)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAnyUE()
    {
        return anyUE;
    }

    @JsonProperty(JSON_PROPERTY_ANY_U_E)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnyUE(Boolean anyUE)
    {
        this.anyUE = anyUE;
    }

    public ExtAmfEventSubscription options(AmfEventMode options)
    {

        this.options = options;
        return this;
    }

    /**
     * Get options
     * 
     * @return options
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmfEventMode getOptions()
    {
        return options;
    }

    @JsonProperty(JSON_PROPERTY_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOptions(AmfEventMode options)
    {
        this.options = options;
    }

    public ExtAmfEventSubscription sourceNfType(String sourceNfType)
    {

        this.sourceNfType = sourceNfType;
        return this;
    }

    /**
     * NF types known to NRF
     * 
     * @return sourceNfType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF types known to NRF")
    @JsonProperty(JSON_PROPERTY_SOURCE_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSourceNfType()
    {
        return sourceNfType;
    }

    @JsonProperty(JSON_PROPERTY_SOURCE_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceNfType(String sourceNfType)
    {
        this.sourceNfType = sourceNfType;
    }

    public ExtAmfEventSubscription bindingInfo(List<String> bindingInfo)
    {

        this.bindingInfo = bindingInfo;
        return this;
    }

    public ExtAmfEventSubscription addBindingInfoItem(String bindingInfoItem)
    {
        if (this.bindingInfo == null)
        {
            this.bindingInfo = new ArrayList<>();
        }
        this.bindingInfo.add(bindingInfoItem);
        return this;
    }

    /**
     * Get bindingInfo
     * 
     * @return bindingInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getBindingInfo()
    {
        return bindingInfo;
    }

    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBindingInfo(List<String> bindingInfo)
    {
        this.bindingInfo = bindingInfo;
    }

    public ExtAmfEventSubscription subscribingNfType(String subscribingNfType)
    {

        this.subscribingNfType = subscribingNfType;
        return this;
    }

    /**
     * NF types known to NRF
     * 
     * @return subscribingNfType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF types known to NRF")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBING_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubscribingNfType()
    {
        return subscribingNfType;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBING_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribingNfType(String subscribingNfType)
    {
        this.subscribingNfType = subscribingNfType;
    }

    public ExtAmfEventSubscription eventSyncInd(Boolean eventSyncInd)
    {

        this.eventSyncInd = eventSyncInd;
        return this;
    }

    /**
     * Get eventSyncInd
     * 
     * @return eventSyncInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_SYNC_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEventSyncInd()
    {
        return eventSyncInd;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_SYNC_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventSyncInd(Boolean eventSyncInd)
    {
        this.eventSyncInd = eventSyncInd;
    }

    public ExtAmfEventSubscription nfConsumerInfo(List<String> nfConsumerInfo)
    {

        this.nfConsumerInfo = nfConsumerInfo;
        return this;
    }

    public ExtAmfEventSubscription addNfConsumerInfoItem(String nfConsumerInfoItem)
    {
        if (this.nfConsumerInfo == null)
        {
            this.nfConsumerInfo = new ArrayList<>();
        }
        this.nfConsumerInfo.add(nfConsumerInfoItem);
        return this;
    }

    /**
     * Get nfConsumerInfo
     * 
     * @return nfConsumerInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NF_CONSUMER_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getNfConsumerInfo()
    {
        return nfConsumerInfo;
    }

    @JsonProperty(JSON_PROPERTY_NF_CONSUMER_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfConsumerInfo(List<String> nfConsumerInfo)
    {
        this.nfConsumerInfo = nfConsumerInfo;
    }

    public ExtAmfEventSubscription aoiStateList(Map<String, AreaOfInterestEventState> aoiStateList)
    {

        this.aoiStateList = aoiStateList;
        return this;
    }

    public ExtAmfEventSubscription putAoiStateListItem(String key,
                                                       AreaOfInterestEventState aoiStateListItem)
    {
        if (this.aoiStateList == null)
        {
            this.aoiStateList = new HashMap<>();
        }
        this.aoiStateList.put(key, aoiStateListItem);
        return this;
    }

    /**
     * Map of subscribed Area of Interest (AoI) Event State in the old AMF. The JSON
     * pointer to an AmfEventArea element in the areaList IE (or a PresenceInfo
     * element in presenceInfoList IE) of the AmfEvent data type shall be the key of
     * the map.
     * 
     * @return aoiStateList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Map of subscribed Area of Interest (AoI) Event State in the old AMF. The JSON pointer to an AmfEventArea element in the areaList IE (or a PresenceInfo element in  presenceInfoList IE) of the AmfEvent data type shall be the key of the map. ")
    @JsonProperty(JSON_PROPERTY_AOI_STATE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, AreaOfInterestEventState> getAoiStateList()
    {
        return aoiStateList;
    }

    @JsonProperty(JSON_PROPERTY_AOI_STATE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAoiStateList(Map<String, AreaOfInterestEventState> aoiStateList)
    {
        this.aoiStateList = aoiStateList;
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
        ExtAmfEventSubscription extAmfEventSubscription = (ExtAmfEventSubscription) o;
        return Objects.equals(this.eventList, extAmfEventSubscription.eventList) && Objects.equals(this.eventNotifyUri, extAmfEventSubscription.eventNotifyUri)
               && Objects.equals(this.notifyCorrelationId, extAmfEventSubscription.notifyCorrelationId)
               && Objects.equals(this.nfId, extAmfEventSubscription.nfId)
               && Objects.equals(this.subsChangeNotifyUri, extAmfEventSubscription.subsChangeNotifyUri)
               && Objects.equals(this.subsChangeNotifyCorrelationId, extAmfEventSubscription.subsChangeNotifyCorrelationId)
               && Objects.equals(this.supi, extAmfEventSubscription.supi) && Objects.equals(this.groupId, extAmfEventSubscription.groupId)
               && Objects.equals(this.excludeSupiList, extAmfEventSubscription.excludeSupiList)
               && Objects.equals(this.excludeGpsiList, extAmfEventSubscription.excludeGpsiList)
               && Objects.equals(this.includeSupiList, extAmfEventSubscription.includeSupiList)
               && Objects.equals(this.includeGpsiList, extAmfEventSubscription.includeGpsiList) && Objects.equals(this.gpsi, extAmfEventSubscription.gpsi)
               && Objects.equals(this.pei, extAmfEventSubscription.pei) && Objects.equals(this.anyUE, extAmfEventSubscription.anyUE)
               && Objects.equals(this.options, extAmfEventSubscription.options) && Objects.equals(this.sourceNfType, extAmfEventSubscription.sourceNfType)
               && Objects.equals(this.bindingInfo, extAmfEventSubscription.bindingInfo)
               && Objects.equals(this.subscribingNfType, extAmfEventSubscription.subscribingNfType)
               && Objects.equals(this.eventSyncInd, extAmfEventSubscription.eventSyncInd)
               && Objects.equals(this.nfConsumerInfo, extAmfEventSubscription.nfConsumerInfo)
               && Objects.equals(this.aoiStateList, extAmfEventSubscription.aoiStateList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventList,
                            eventNotifyUri,
                            notifyCorrelationId,
                            nfId,
                            subsChangeNotifyUri,
                            subsChangeNotifyCorrelationId,
                            supi,
                            groupId,
                            excludeSupiList,
                            excludeGpsiList,
                            includeSupiList,
                            includeGpsiList,
                            gpsi,
                            pei,
                            anyUE,
                            options,
                            sourceNfType,
                            bindingInfo,
                            subscribingNfType,
                            eventSyncInd,
                            nfConsumerInfo,
                            aoiStateList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExtAmfEventSubscription {\n");
        sb.append("    eventList: ").append(toIndentedString(eventList)).append("\n");
        sb.append("    eventNotifyUri: ").append(toIndentedString(eventNotifyUri)).append("\n");
        sb.append("    notifyCorrelationId: ").append(toIndentedString(notifyCorrelationId)).append("\n");
        sb.append("    nfId: ").append(toIndentedString(nfId)).append("\n");
        sb.append("    subsChangeNotifyUri: ").append(toIndentedString(subsChangeNotifyUri)).append("\n");
        sb.append("    subsChangeNotifyCorrelationId: ").append(toIndentedString(subsChangeNotifyCorrelationId)).append("\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    excludeSupiList: ").append(toIndentedString(excludeSupiList)).append("\n");
        sb.append("    excludeGpsiList: ").append(toIndentedString(excludeGpsiList)).append("\n");
        sb.append("    includeSupiList: ").append(toIndentedString(includeSupiList)).append("\n");
        sb.append("    includeGpsiList: ").append(toIndentedString(includeGpsiList)).append("\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    pei: ").append(toIndentedString(pei)).append("\n");
        sb.append("    anyUE: ").append(toIndentedString(anyUE)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
        sb.append("    sourceNfType: ").append(toIndentedString(sourceNfType)).append("\n");
        sb.append("    bindingInfo: ").append(toIndentedString(bindingInfo)).append("\n");
        sb.append("    subscribingNfType: ").append(toIndentedString(subscribingNfType)).append("\n");
        sb.append("    eventSyncInd: ").append(toIndentedString(eventSyncInd)).append("\n");
        sb.append("    nfConsumerInfo: ").append(toIndentedString(nfConsumerInfo)).append("\n");
        sb.append("    aoiStateList: ").append(toIndentedString(aoiStateList)).append("\n");
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
