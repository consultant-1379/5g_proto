/*
 * Ngmlc_Location
 * GMLC Location Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29515.ngmlc.location;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.MotionEventInfo;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.PeriodicEventInfo;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.LocationQoS;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the input parameters in ProvideLocation service operation
 */
@ApiModel(description = "Contains the input parameters in ProvideLocation service operation")
@JsonPropertyOrder({ InputData.JSON_PROPERTY_GPSI,
                     InputData.JSON_PROPERTY_SUPI,
                     InputData.JSON_PROPERTY_EXT_GROUP_ID,
                     InputData.JSON_PROPERTY_INT_GROUP_ID,
                     InputData.JSON_PROPERTY_EXTERNAL_CLIENT_TYPE,
                     InputData.JSON_PROPERTY_LOCATION_QO_S,
                     InputData.JSON_PROPERTY_SUPPORTED_G_A_D_SHAPES,
                     InputData.JSON_PROPERTY_SERVICE_IDENTITY,
                     InputData.JSON_PROPERTY_SERVICE_COVERAGE,
                     InputData.JSON_PROPERTY_LDR_TYPE,
                     InputData.JSON_PROPERTY_PERIODIC_EVENT_INFO,
                     InputData.JSON_PROPERTY_AREA_EVENT_INFO,
                     InputData.JSON_PROPERTY_MOTION_EVENT_INFO,
                     InputData.JSON_PROPERTY_LDR_REFERENCE,
                     InputData.JSON_PROPERTY_HGMLC_CALL_BACK_URI,
                     InputData.JSON_PROPERTY_EVENT_NOTIFICATION_URI,
                     InputData.JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION,
                     InputData.JSON_PROPERTY_AF_ID,
                     InputData.JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS,
                     InputData.JSON_PROPERTY_LCS_SERVICE_TYPE,
                     InputData.JSON_PROPERTY_VELOCITY_REQUESTED,
                     InputData.JSON_PROPERTY_PRIORITY,
                     InputData.JSON_PROPERTY_LOCATION_TYPE_REQUESTED,
                     InputData.JSON_PROPERTY_MAXIMUM_AGE_OF_LOCATION_ESTIMATE,
                     InputData.JSON_PROPERTY_AMF_ID,
                     InputData.JSON_PROPERTY_CODE_WORD,
                     InputData.JSON_PROPERTY_SCHEDULED_LOC_TIME,
                     InputData.JSON_PROPERTY_RELIABLE_LOC_REQ })
public class InputData
{
    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_EXT_GROUP_ID = "extGroupId";
    private String extGroupId;

    public static final String JSON_PROPERTY_INT_GROUP_ID = "intGroupId";
    private String intGroupId;

    public static final String JSON_PROPERTY_EXTERNAL_CLIENT_TYPE = "externalClientType";
    private String externalClientType;

    public static final String JSON_PROPERTY_LOCATION_QO_S = "locationQoS";
    private LocationQoS locationQoS;

    public static final String JSON_PROPERTY_SUPPORTED_G_A_D_SHAPES = "supportedGADShapes";
    private List<String> supportedGADShapes = null;

    public static final String JSON_PROPERTY_SERVICE_IDENTITY = "serviceIdentity";
    private String serviceIdentity;

    public static final String JSON_PROPERTY_SERVICE_COVERAGE = "serviceCoverage";
    private List<String> serviceCoverage = null;

    public static final String JSON_PROPERTY_LDR_TYPE = "ldrType";
    private String ldrType;

    public static final String JSON_PROPERTY_PERIODIC_EVENT_INFO = "periodicEventInfo";
    private PeriodicEventInfo periodicEventInfo;

    public static final String JSON_PROPERTY_AREA_EVENT_INFO = "areaEventInfo";
    private AreaEventInfoExt areaEventInfo;

    public static final String JSON_PROPERTY_MOTION_EVENT_INFO = "motionEventInfo";
    private MotionEventInfo motionEventInfo;

    public static final String JSON_PROPERTY_LDR_REFERENCE = "ldrReference";
    private String ldrReference;

    public static final String JSON_PROPERTY_HGMLC_CALL_BACK_URI = "hgmlcCallBackUri";
    private String hgmlcCallBackUri;

    public static final String JSON_PROPERTY_EVENT_NOTIFICATION_URI = "eventNotificationUri";
    private String eventNotificationUri;

    public static final String JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION = "externalClientIdentification";
    private String externalClientIdentification;

    public static final String JSON_PROPERTY_AF_ID = "afId";
    private String afId;

    public static final String JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS = "uePrivacyRequirements";
    private UePrivacyRequirements uePrivacyRequirements;

    public static final String JSON_PROPERTY_LCS_SERVICE_TYPE = "lcsServiceType";
    private Integer lcsServiceType;

    public static final String JSON_PROPERTY_VELOCITY_REQUESTED = "velocityRequested";
    private String velocityRequested;

    public static final String JSON_PROPERTY_PRIORITY = "priority";
    private String priority;

    public static final String JSON_PROPERTY_LOCATION_TYPE_REQUESTED = "locationTypeRequested";
    private String locationTypeRequested;

    public static final String JSON_PROPERTY_MAXIMUM_AGE_OF_LOCATION_ESTIMATE = "maximumAgeOfLocationEstimate";
    private Integer maximumAgeOfLocationEstimate;

    public static final String JSON_PROPERTY_AMF_ID = "amfId";
    private String amfId;

    public static final String JSON_PROPERTY_CODE_WORD = "codeWord";
    private String codeWord;

    public static final String JSON_PROPERTY_SCHEDULED_LOC_TIME = "scheduledLocTime";
    private OffsetDateTime scheduledLocTime;

    public static final String JSON_PROPERTY_RELIABLE_LOC_REQ = "reliableLocReq";
    private Boolean reliableLocReq = false;

    public InputData()
    {
    }

    public InputData gpsi(String gpsi)
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

    public InputData supi(String supi)
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

    public InputData extGroupId(String extGroupId)
    {

        this.extGroupId = extGroupId;
        return this;
    }

    /**
     * String identifying External Group Identifier that identifies a group made up
     * of one or more subscriptions associated to a group of IMSIs, as specified in
     * clause 19.7.3 of 3GPP TS 23.003.
     * 
     * @return extGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying External Group Identifier that identifies a group made up of one or more  subscriptions associated to a group of IMSIs, as specified in clause 19.7.3 of 3GPP TS 23.003.  ")
    @JsonProperty(JSON_PROPERTY_EXT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExtGroupId()
    {
        return extGroupId;
    }

    @JsonProperty(JSON_PROPERTY_EXT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtGroupId(String extGroupId)
    {
        this.extGroupId = extGroupId;
    }

    public InputData intGroupId(String intGroupId)
    {

        this.intGroupId = intGroupId;
        return this;
    }

    /**
     * String identifying a group of devices network internal globally unique ID
     * which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS
     * 23.003.
     * 
     * @return intGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a group of devices network internal globally unique ID which identifies a set of IMSIs, as specified in clause 19.9 of 3GPP TS 23.003.  ")
    @JsonProperty(JSON_PROPERTY_INT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIntGroupId()
    {
        return intGroupId;
    }

    @JsonProperty(JSON_PROPERTY_INT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntGroupId(String intGroupId)
    {
        this.intGroupId = intGroupId;
    }

    public InputData externalClientType(String externalClientType)
    {

        this.externalClientType = externalClientType;
        return this;
    }

    /**
     * Indicates types of External Clients.
     * 
     * @return externalClientType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates types of External Clients.")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_CLIENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getExternalClientType()
    {
        return externalClientType;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_CLIENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setExternalClientType(String externalClientType)
    {
        this.externalClientType = externalClientType;
    }

    public InputData locationQoS(LocationQoS locationQoS)
    {

        this.locationQoS = locationQoS;
        return this;
    }

    /**
     * Get locationQoS
     * 
     * @return locationQoS
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION_QO_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LocationQoS getLocationQoS()
    {
        return locationQoS;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_QO_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationQoS(LocationQoS locationQoS)
    {
        this.locationQoS = locationQoS;
    }

    public InputData supportedGADShapes(List<String> supportedGADShapes)
    {

        this.supportedGADShapes = supportedGADShapes;
        return this;
    }

    public InputData addSupportedGADShapesItem(String supportedGADShapesItem)
    {
        if (this.supportedGADShapes == null)
        {
            this.supportedGADShapes = new ArrayList<>();
        }
        this.supportedGADShapes.add(supportedGADShapesItem);
        return this;
    }

    /**
     * Get supportedGADShapes
     * 
     * @return supportedGADShapes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSupportedGADShapes()
    {
        return supportedGADShapes;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedGADShapes(List<String> supportedGADShapes)
    {
        this.supportedGADShapes = supportedGADShapes;
    }

    public InputData serviceIdentity(String serviceIdentity)
    {

        this.serviceIdentity = serviceIdentity;
        return this;
    }

    /**
     * Contains the service identity
     * 
     * @return serviceIdentity
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the service identity")
    @JsonProperty(JSON_PROPERTY_SERVICE_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServiceIdentity()
    {
        return serviceIdentity;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceIdentity(String serviceIdentity)
    {
        this.serviceIdentity = serviceIdentity;
    }

    public InputData serviceCoverage(List<String> serviceCoverage)
    {

        this.serviceCoverage = serviceCoverage;
        return this;
    }

    public InputData addServiceCoverageItem(String serviceCoverageItem)
    {
        if (this.serviceCoverage == null)
        {
            this.serviceCoverage = new ArrayList<>();
        }
        this.serviceCoverage.add(serviceCoverageItem);
        return this;
    }

    /**
     * Get serviceCoverage
     * 
     * @return serviceCoverage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_COVERAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getServiceCoverage()
    {
        return serviceCoverage;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_COVERAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceCoverage(List<String> serviceCoverage)
    {
        this.serviceCoverage = serviceCoverage;
    }

    public InputData ldrType(String ldrType)
    {

        this.ldrType = ldrType;
        return this;
    }

    /**
     * Indicates LDR types.
     * 
     * @return ldrType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates LDR types.")
    @JsonProperty(JSON_PROPERTY_LDR_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLdrType()
    {
        return ldrType;
    }

    @JsonProperty(JSON_PROPERTY_LDR_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLdrType(String ldrType)
    {
        this.ldrType = ldrType;
    }

    public InputData periodicEventInfo(PeriodicEventInfo periodicEventInfo)
    {

        this.periodicEventInfo = periodicEventInfo;
        return this;
    }

    /**
     * Get periodicEventInfo
     * 
     * @return periodicEventInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PERIODIC_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PeriodicEventInfo getPeriodicEventInfo()
    {
        return periodicEventInfo;
    }

    @JsonProperty(JSON_PROPERTY_PERIODIC_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPeriodicEventInfo(PeriodicEventInfo periodicEventInfo)
    {
        this.periodicEventInfo = periodicEventInfo;
    }

    public InputData areaEventInfo(AreaEventInfoExt areaEventInfo)
    {

        this.areaEventInfo = areaEventInfo;
        return this;
    }

    /**
     * Get areaEventInfo
     * 
     * @return areaEventInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AREA_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AreaEventInfoExt getAreaEventInfo()
    {
        return areaEventInfo;
    }

    @JsonProperty(JSON_PROPERTY_AREA_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaEventInfo(AreaEventInfoExt areaEventInfo)
    {
        this.areaEventInfo = areaEventInfo;
    }

    public InputData motionEventInfo(MotionEventInfo motionEventInfo)
    {

        this.motionEventInfo = motionEventInfo;
        return this;
    }

    /**
     * Get motionEventInfo
     * 
     * @return motionEventInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MOTION_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MotionEventInfo getMotionEventInfo()
    {
        return motionEventInfo;
    }

    @JsonProperty(JSON_PROPERTY_MOTION_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMotionEventInfo(MotionEventInfo motionEventInfo)
    {
        this.motionEventInfo = motionEventInfo;
    }

    public InputData ldrReference(String ldrReference)
    {

        this.ldrReference = ldrReference;
        return this;
    }

    /**
     * LDR Reference.
     * 
     * @return ldrReference
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "LDR Reference.")
    @JsonProperty(JSON_PROPERTY_LDR_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLdrReference()
    {
        return ldrReference;
    }

    @JsonProperty(JSON_PROPERTY_LDR_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLdrReference(String ldrReference)
    {
        this.ldrReference = ldrReference;
    }

    public InputData hgmlcCallBackUri(String hgmlcCallBackUri)
    {

        this.hgmlcCallBackUri = hgmlcCallBackUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return hgmlcCallBackUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHgmlcCallBackUri()
    {
        return hgmlcCallBackUri;
    }

    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHgmlcCallBackUri(String hgmlcCallBackUri)
    {
        this.hgmlcCallBackUri = hgmlcCallBackUri;
    }

    public InputData eventNotificationUri(String eventNotificationUri)
    {

        this.eventNotificationUri = eventNotificationUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return eventNotificationUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getEventNotificationUri()
    {
        return eventNotificationUri;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventNotificationUri(String eventNotificationUri)
    {
        this.eventNotificationUri = eventNotificationUri;
    }

    public InputData externalClientIdentification(String externalClientIdentification)
    {

        this.externalClientIdentification = externalClientIdentification;
        return this;
    }

    /**
     * Contains the external client identification
     * 
     * @return externalClientIdentification
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the external client identification")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExternalClientIdentification()
    {
        return externalClientIdentification;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalClientIdentification(String externalClientIdentification)
    {
        this.externalClientIdentification = externalClientIdentification;
    }

    public InputData afId(String afId)
    {

        this.afId = afId;
        return this;
    }

    /**
     * Get afId
     * 
     * @return afId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAfId()
    {
        return afId;
    }

    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfId(String afId)
    {
        this.afId = afId;
    }

    public InputData uePrivacyRequirements(UePrivacyRequirements uePrivacyRequirements)
    {

        this.uePrivacyRequirements = uePrivacyRequirements;
        return this;
    }

    /**
     * Get uePrivacyRequirements
     * 
     * @return uePrivacyRequirements
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UePrivacyRequirements getUePrivacyRequirements()
    {
        return uePrivacyRequirements;
    }

    @JsonProperty(JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUePrivacyRequirements(UePrivacyRequirements uePrivacyRequirements)
    {
        this.uePrivacyRequirements = uePrivacyRequirements;
    }

    public InputData lcsServiceType(Integer lcsServiceType)
    {

        this.lcsServiceType = lcsServiceType;
        return this;
    }

    /**
     * LCS service type. minimum: 0 maximum: 127
     * 
     * @return lcsServiceType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "LCS service type.")
    @JsonProperty(JSON_PROPERTY_LCS_SERVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getLcsServiceType()
    {
        return lcsServiceType;
    }

    @JsonProperty(JSON_PROPERTY_LCS_SERVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsServiceType(Integer lcsServiceType)
    {
        this.lcsServiceType = lcsServiceType;
    }

    public InputData velocityRequested(String velocityRequested)
    {

        this.velocityRequested = velocityRequested;
        return this;
    }

    /**
     * Indicates velocity requirement.
     * 
     * @return velocityRequested
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates velocity requirement.")
    @JsonProperty(JSON_PROPERTY_VELOCITY_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVelocityRequested()
    {
        return velocityRequested;
    }

    @JsonProperty(JSON_PROPERTY_VELOCITY_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVelocityRequested(String velocityRequested)
    {
        this.velocityRequested = velocityRequested;
    }

    public InputData priority(String priority)
    {

        this.priority = priority;
        return this;
    }

    /**
     * Indicates priority of the LCS client.
     * 
     * @return priority
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates priority of the LCS client.")
    @JsonProperty(JSON_PROPERTY_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPriority()
    {
        return priority;
    }

    @JsonProperty(JSON_PROPERTY_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public InputData locationTypeRequested(String locationTypeRequested)
    {

        this.locationTypeRequested = locationTypeRequested;
        return this;
    }

    /**
     * Contains the location type requested by the LCS client
     * 
     * @return locationTypeRequested
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the location type requested by the LCS client")
    @JsonProperty(JSON_PROPERTY_LOCATION_TYPE_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLocationTypeRequested()
    {
        return locationTypeRequested;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_TYPE_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationTypeRequested(String locationTypeRequested)
    {
        this.locationTypeRequested = locationTypeRequested;
    }

    public InputData maximumAgeOfLocationEstimate(Integer maximumAgeOfLocationEstimate)
    {

        this.maximumAgeOfLocationEstimate = maximumAgeOfLocationEstimate;
        return this;
    }

    /**
     * Indicates value of the age of the location estimate. minimum: 0 maximum:
     * 32767
     * 
     * @return maximumAgeOfLocationEstimate
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates value of the age of the location estimate.")
    @JsonProperty(JSON_PROPERTY_MAXIMUM_AGE_OF_LOCATION_ESTIMATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaximumAgeOfLocationEstimate()
    {
        return maximumAgeOfLocationEstimate;
    }

    @JsonProperty(JSON_PROPERTY_MAXIMUM_AGE_OF_LOCATION_ESTIMATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaximumAgeOfLocationEstimate(Integer maximumAgeOfLocationEstimate)
    {
        this.maximumAgeOfLocationEstimate = maximumAgeOfLocationEstimate;
    }

    public InputData amfId(String amfId)
    {

        this.amfId = amfId;
        return this;
    }

    /**
     * String identifying the AMF ID composed of AMF Region ID (8 bits), AMF Set ID
     * (10 bits) and AMF Pointer (6 bits) as specified in clause 2.10.1 of 3GPP TS
     * 23.003. It is encoded as a string of 6 hexadecimal characters (i.e., 24
     * bits).
     * 
     * @return amfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying the AMF ID composed of AMF Region ID (8 bits), AMF Set ID (10 bits) and AMF  Pointer (6 bits) as specified in clause 2.10.1 of 3GPP TS 23.003. It is encoded as a string of  6 hexadecimal characters (i.e., 24 bits).  ")
    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAmfId()
    {
        return amfId;
    }

    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfId(String amfId)
    {
        this.amfId = amfId;
    }

    public InputData codeWord(String codeWord)
    {

        this.codeWord = codeWord;
        return this;
    }

    /**
     * Contains the codeword
     * 
     * @return codeWord
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the codeword")
    @JsonProperty(JSON_PROPERTY_CODE_WORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCodeWord()
    {
        return codeWord;
    }

    @JsonProperty(JSON_PROPERTY_CODE_WORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodeWord(String codeWord)
    {
        this.codeWord = codeWord;
    }

    public InputData scheduledLocTime(OffsetDateTime scheduledLocTime)
    {

        this.scheduledLocTime = scheduledLocTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return scheduledLocTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_SCHEDULED_LOC_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getScheduledLocTime()
    {
        return scheduledLocTime;
    }

    @JsonProperty(JSON_PROPERTY_SCHEDULED_LOC_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScheduledLocTime(OffsetDateTime scheduledLocTime)
    {
        this.scheduledLocTime = scheduledLocTime;
    }

    public InputData reliableLocReq(Boolean reliableLocReq)
    {

        this.reliableLocReq = reliableLocReq;
        return this;
    }

    /**
     * Get reliableLocReq
     * 
     * @return reliableLocReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RELIABLE_LOC_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReliableLocReq()
    {
        return reliableLocReq;
    }

    @JsonProperty(JSON_PROPERTY_RELIABLE_LOC_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReliableLocReq(Boolean reliableLocReq)
    {
        this.reliableLocReq = reliableLocReq;
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
        InputData inputData = (InputData) o;
        return Objects.equals(this.gpsi, inputData.gpsi) && Objects.equals(this.supi, inputData.supi) && Objects.equals(this.extGroupId, inputData.extGroupId)
               && Objects.equals(this.intGroupId, inputData.intGroupId) && Objects.equals(this.externalClientType, inputData.externalClientType)
               && Objects.equals(this.locationQoS, inputData.locationQoS) && Objects.equals(this.supportedGADShapes, inputData.supportedGADShapes)
               && Objects.equals(this.serviceIdentity, inputData.serviceIdentity) && Objects.equals(this.serviceCoverage, inputData.serviceCoverage)
               && Objects.equals(this.ldrType, inputData.ldrType) && Objects.equals(this.periodicEventInfo, inputData.periodicEventInfo)
               && Objects.equals(this.areaEventInfo, inputData.areaEventInfo) && Objects.equals(this.motionEventInfo, inputData.motionEventInfo)
               && Objects.equals(this.ldrReference, inputData.ldrReference) && Objects.equals(this.hgmlcCallBackUri, inputData.hgmlcCallBackUri)
               && Objects.equals(this.eventNotificationUri, inputData.eventNotificationUri)
               && Objects.equals(this.externalClientIdentification, inputData.externalClientIdentification) && Objects.equals(this.afId, inputData.afId)
               && Objects.equals(this.uePrivacyRequirements, inputData.uePrivacyRequirements) && Objects.equals(this.lcsServiceType, inputData.lcsServiceType)
               && Objects.equals(this.velocityRequested, inputData.velocityRequested) && Objects.equals(this.priority, inputData.priority)
               && Objects.equals(this.locationTypeRequested, inputData.locationTypeRequested)
               && Objects.equals(this.maximumAgeOfLocationEstimate, inputData.maximumAgeOfLocationEstimate) && Objects.equals(this.amfId, inputData.amfId)
               && Objects.equals(this.codeWord, inputData.codeWord) && Objects.equals(this.scheduledLocTime, inputData.scheduledLocTime)
               && Objects.equals(this.reliableLocReq, inputData.reliableLocReq);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(gpsi,
                            supi,
                            extGroupId,
                            intGroupId,
                            externalClientType,
                            locationQoS,
                            supportedGADShapes,
                            serviceIdentity,
                            serviceCoverage,
                            ldrType,
                            periodicEventInfo,
                            areaEventInfo,
                            motionEventInfo,
                            ldrReference,
                            hgmlcCallBackUri,
                            eventNotificationUri,
                            externalClientIdentification,
                            afId,
                            uePrivacyRequirements,
                            lcsServiceType,
                            velocityRequested,
                            priority,
                            locationTypeRequested,
                            maximumAgeOfLocationEstimate,
                            amfId,
                            codeWord,
                            scheduledLocTime,
                            reliableLocReq);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class InputData {\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    extGroupId: ").append(toIndentedString(extGroupId)).append("\n");
        sb.append("    intGroupId: ").append(toIndentedString(intGroupId)).append("\n");
        sb.append("    externalClientType: ").append(toIndentedString(externalClientType)).append("\n");
        sb.append("    locationQoS: ").append(toIndentedString(locationQoS)).append("\n");
        sb.append("    supportedGADShapes: ").append(toIndentedString(supportedGADShapes)).append("\n");
        sb.append("    serviceIdentity: ").append(toIndentedString(serviceIdentity)).append("\n");
        sb.append("    serviceCoverage: ").append(toIndentedString(serviceCoverage)).append("\n");
        sb.append("    ldrType: ").append(toIndentedString(ldrType)).append("\n");
        sb.append("    periodicEventInfo: ").append(toIndentedString(periodicEventInfo)).append("\n");
        sb.append("    areaEventInfo: ").append(toIndentedString(areaEventInfo)).append("\n");
        sb.append("    motionEventInfo: ").append(toIndentedString(motionEventInfo)).append("\n");
        sb.append("    ldrReference: ").append(toIndentedString(ldrReference)).append("\n");
        sb.append("    hgmlcCallBackUri: ").append(toIndentedString(hgmlcCallBackUri)).append("\n");
        sb.append("    eventNotificationUri: ").append(toIndentedString(eventNotificationUri)).append("\n");
        sb.append("    externalClientIdentification: ").append(toIndentedString(externalClientIdentification)).append("\n");
        sb.append("    afId: ").append(toIndentedString(afId)).append("\n");
        sb.append("    uePrivacyRequirements: ").append(toIndentedString(uePrivacyRequirements)).append("\n");
        sb.append("    lcsServiceType: ").append(toIndentedString(lcsServiceType)).append("\n");
        sb.append("    velocityRequested: ").append(toIndentedString(velocityRequested)).append("\n");
        sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
        sb.append("    locationTypeRequested: ").append(toIndentedString(locationTypeRequested)).append("\n");
        sb.append("    maximumAgeOfLocationEstimate: ").append(toIndentedString(maximumAgeOfLocationEstimate)).append("\n");
        sb.append("    amfId: ").append(toIndentedString(amfId)).append("\n");
        sb.append("    codeWord: ").append(toIndentedString(codeWord)).append("\n");
        sb.append("    scheduledLocTime: ").append(toIndentedString(scheduledLocTime)).append("\n");
        sb.append("    reliableLocReq: ").append(toIndentedString(reliableLocReq)).append("\n");
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
