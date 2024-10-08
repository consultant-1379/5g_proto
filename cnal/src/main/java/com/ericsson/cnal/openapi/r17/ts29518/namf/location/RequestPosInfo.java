/*
 * Namf_Location
 * AMF Location Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.location;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.MotionEventInfo;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.AreaEventInfo;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.PeriodicEventInfo;
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.LocationQoS;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Guami;
import com.ericsson.cnal.openapi.r17.ts29515.ngmlc.location.UePrivacyRequirements;
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
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within Provide Positioning Information Request
 */
@ApiModel(description = "Data within Provide Positioning Information Request")
@JsonPropertyOrder({ RequestPosInfo.JSON_PROPERTY_LCS_CLIENT_TYPE,
                     RequestPosInfo.JSON_PROPERTY_LCS_LOCATION,
                     RequestPosInfo.JSON_PROPERTY_SUPI,
                     RequestPosInfo.JSON_PROPERTY_GPSI,
                     RequestPosInfo.JSON_PROPERTY_PRIORITY,
                     RequestPosInfo.JSON_PROPERTY_LCS_QO_S,
                     RequestPosInfo.JSON_PROPERTY_VELOCITY_REQUESTED,
                     RequestPosInfo.JSON_PROPERTY_LCS_SUPPORTED_G_A_D_SHAPES,
                     RequestPosInfo.JSON_PROPERTY_ADDITIONAL_LCS_SUPP_G_A_D_SHAPES,
                     RequestPosInfo.JSON_PROPERTY_LOCATION_NOTIFICATION_URI,
                     RequestPosInfo.JSON_PROPERTY_SUPPORTED_FEATURES,
                     RequestPosInfo.JSON_PROPERTY_OLD_GUAMI,
                     RequestPosInfo.JSON_PROPERTY_PEI,
                     RequestPosInfo.JSON_PROPERTY_LCS_SERVICE_TYPE,
                     RequestPosInfo.JSON_PROPERTY_LDR_TYPE,
                     RequestPosInfo.JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I,
                     RequestPosInfo.JSON_PROPERTY_LDR_REFERENCE,
                     RequestPosInfo.JSON_PROPERTY_PERIODIC_EVENT_INFO,
                     RequestPosInfo.JSON_PROPERTY_AREA_EVENT_INFO,
                     RequestPosInfo.JSON_PROPERTY_MOTION_EVENT_INFO,
                     RequestPosInfo.JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION,
                     RequestPosInfo.JSON_PROPERTY_AF_I_D,
                     RequestPosInfo.JSON_PROPERTY_CODE_WORD,
                     RequestPosInfo.JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS,
                     RequestPosInfo.JSON_PROPERTY_SCHEDULED_LOC_TIME,
                     RequestPosInfo.JSON_PROPERTY_RELIABLE_LOC_REQ })
public class RequestPosInfo
{
    public static final String JSON_PROPERTY_LCS_CLIENT_TYPE = "lcsClientType";
    private String lcsClientType;

    public static final String JSON_PROPERTY_LCS_LOCATION = "lcsLocation";
    private String lcsLocation;

    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_PRIORITY = "priority";
    private String priority;

    public static final String JSON_PROPERTY_LCS_QO_S = "lcsQoS";
    private LocationQoS lcsQoS;

    public static final String JSON_PROPERTY_VELOCITY_REQUESTED = "velocityRequested";
    private String velocityRequested;

    public static final String JSON_PROPERTY_LCS_SUPPORTED_G_A_D_SHAPES = "lcsSupportedGADShapes";
    private String lcsSupportedGADShapes;

    public static final String JSON_PROPERTY_ADDITIONAL_LCS_SUPP_G_A_D_SHAPES = "additionalLcsSuppGADShapes";
    private List<String> additionalLcsSuppGADShapes = null;

    public static final String JSON_PROPERTY_LOCATION_NOTIFICATION_URI = "locationNotificationUri";
    private String locationNotificationUri;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_OLD_GUAMI = "oldGuami";
    private Guami oldGuami;

    public static final String JSON_PROPERTY_PEI = "pei";
    private String pei;

    public static final String JSON_PROPERTY_LCS_SERVICE_TYPE = "lcsServiceType";
    private Integer lcsServiceType;

    public static final String JSON_PROPERTY_LDR_TYPE = "ldrType";
    private String ldrType;

    public static final String JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I = "hgmlcCallBackURI";
    private String hgmlcCallBackURI;

    public static final String JSON_PROPERTY_LDR_REFERENCE = "ldrReference";
    private String ldrReference;

    public static final String JSON_PROPERTY_PERIODIC_EVENT_INFO = "periodicEventInfo";
    private PeriodicEventInfo periodicEventInfo;

    public static final String JSON_PROPERTY_AREA_EVENT_INFO = "areaEventInfo";
    private AreaEventInfo areaEventInfo;

    public static final String JSON_PROPERTY_MOTION_EVENT_INFO = "motionEventInfo";
    private MotionEventInfo motionEventInfo;

    public static final String JSON_PROPERTY_EXTERNAL_CLIENT_IDENTIFICATION = "externalClientIdentification";
    private String externalClientIdentification;

    public static final String JSON_PROPERTY_AF_I_D = "afID";
    private UUID afID;

    public static final String JSON_PROPERTY_CODE_WORD = "codeWord";
    private String codeWord;

    public static final String JSON_PROPERTY_UE_PRIVACY_REQUIREMENTS = "uePrivacyRequirements";
    private UePrivacyRequirements uePrivacyRequirements;

    public static final String JSON_PROPERTY_SCHEDULED_LOC_TIME = "scheduledLocTime";
    private OffsetDateTime scheduledLocTime;

    public static final String JSON_PROPERTY_RELIABLE_LOC_REQ = "reliableLocReq";
    private Boolean reliableLocReq = false;

    public RequestPosInfo()
    {
    }

    public RequestPosInfo lcsClientType(String lcsClientType)
    {

        this.lcsClientType = lcsClientType;
        return this;
    }

    /**
     * Indicates types of External Clients.
     * 
     * @return lcsClientType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates types of External Clients.")
    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getLcsClientType()
    {
        return lcsClientType;
    }

    @JsonProperty(JSON_PROPERTY_LCS_CLIENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLcsClientType(String lcsClientType)
    {
        this.lcsClientType = lcsClientType;
    }

    public RequestPosInfo lcsLocation(String lcsLocation)
    {

        this.lcsLocation = lcsLocation;
        return this;
    }

    /**
     * Type of location measurement requested
     * 
     * @return lcsLocation
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Type of location measurement requested")
    @JsonProperty(JSON_PROPERTY_LCS_LOCATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getLcsLocation()
    {
        return lcsLocation;
    }

    @JsonProperty(JSON_PROPERTY_LCS_LOCATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLcsLocation(String lcsLocation)
    {
        this.lcsLocation = lcsLocation;
    }

    public RequestPosInfo supi(String supi)
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

    public RequestPosInfo gpsi(String gpsi)
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

    public RequestPosInfo priority(String priority)
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

    public RequestPosInfo lcsQoS(LocationQoS lcsQoS)
    {

        this.lcsQoS = lcsQoS;
        return this;
    }

    /**
     * Get lcsQoS
     * 
     * @return lcsQoS
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LCS_QO_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LocationQoS getLcsQoS()
    {
        return lcsQoS;
    }

    @JsonProperty(JSON_PROPERTY_LCS_QO_S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsQoS(LocationQoS lcsQoS)
    {
        this.lcsQoS = lcsQoS;
    }

    public RequestPosInfo velocityRequested(String velocityRequested)
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

    public RequestPosInfo lcsSupportedGADShapes(String lcsSupportedGADShapes)
    {

        this.lcsSupportedGADShapes = lcsSupportedGADShapes;
        return this;
    }

    /**
     * Indicates supported GAD shapes.
     * 
     * @return lcsSupportedGADShapes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates supported GAD shapes.")
    @JsonProperty(JSON_PROPERTY_LCS_SUPPORTED_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLcsSupportedGADShapes()
    {
        return lcsSupportedGADShapes;
    }

    @JsonProperty(JSON_PROPERTY_LCS_SUPPORTED_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLcsSupportedGADShapes(String lcsSupportedGADShapes)
    {
        this.lcsSupportedGADShapes = lcsSupportedGADShapes;
    }

    public RequestPosInfo additionalLcsSuppGADShapes(List<String> additionalLcsSuppGADShapes)
    {

        this.additionalLcsSuppGADShapes = additionalLcsSuppGADShapes;
        return this;
    }

    public RequestPosInfo addAdditionalLcsSuppGADShapesItem(String additionalLcsSuppGADShapesItem)
    {
        if (this.additionalLcsSuppGADShapes == null)
        {
            this.additionalLcsSuppGADShapes = new ArrayList<>();
        }
        this.additionalLcsSuppGADShapes.add(additionalLcsSuppGADShapesItem);
        return this;
    }

    /**
     * Get additionalLcsSuppGADShapes
     * 
     * @return additionalLcsSuppGADShapes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADDITIONAL_LCS_SUPP_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAdditionalLcsSuppGADShapes()
    {
        return additionalLcsSuppGADShapes;
    }

    @JsonProperty(JSON_PROPERTY_ADDITIONAL_LCS_SUPP_G_A_D_SHAPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalLcsSuppGADShapes(List<String> additionalLcsSuppGADShapes)
    {
        this.additionalLcsSuppGADShapes = additionalLcsSuppGADShapes;
    }

    public RequestPosInfo locationNotificationUri(String locationNotificationUri)
    {

        this.locationNotificationUri = locationNotificationUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return locationNotificationUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_LOCATION_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLocationNotificationUri()
    {
        return locationNotificationUri;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationNotificationUri(String locationNotificationUri)
    {
        this.locationNotificationUri = locationNotificationUri;
    }

    public RequestPosInfo supportedFeatures(String supportedFeatures)
    {

        this.supportedFeatures = supportedFeatures;
        return this;
    }

    /**
     * A string used to indicate the features supported by an API that is used as
     * defined in clause 6.6 in 3GPP TS 29.500. The string shall contain a bitmask
     * indicating supported features in hexadecimal representation Each character in
     * the string shall take a value of \&quot;0\&quot; to \&quot;9\&quot;,
     * \&quot;a\&quot; to \&quot;f\&quot; or \&quot;A\&quot; to \&quot;F\&quot; and
     * shall represent the support of 4 features as described in table 5.2.2-3. The
     * most significant character representing the highest-numbered features shall
     * appear first in the string, and the character representing features 1 to 4
     * shall appear last in the string. The list of features and their numbering
     * (starting with 1) are defined separately for each API. If the string contains
     * a lower number of characters than there are defined features for an API, all
     * features that would be represented by characters that are not present in the
     * string are not supported.
     * 
     * @return supportedFeatures
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupportedFeatures()
    {
        return supportedFeatures;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
    }

    public RequestPosInfo oldGuami(Guami oldGuami)
    {

        this.oldGuami = oldGuami;
        return this;
    }

    /**
     * Get oldGuami
     * 
     * @return oldGuami
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_OLD_GUAMI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Guami getOldGuami()
    {
        return oldGuami;
    }

    @JsonProperty(JSON_PROPERTY_OLD_GUAMI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOldGuami(Guami oldGuami)
    {
        this.oldGuami = oldGuami;
    }

    public RequestPosInfo pei(String pei)
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

    public RequestPosInfo lcsServiceType(Integer lcsServiceType)
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

    public RequestPosInfo ldrType(String ldrType)
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

    public RequestPosInfo hgmlcCallBackURI(String hgmlcCallBackURI)
    {

        this.hgmlcCallBackURI = hgmlcCallBackURI;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return hgmlcCallBackURI
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHgmlcCallBackURI()
    {
        return hgmlcCallBackURI;
    }

    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHgmlcCallBackURI(String hgmlcCallBackURI)
    {
        this.hgmlcCallBackURI = hgmlcCallBackURI;
    }

    public RequestPosInfo ldrReference(String ldrReference)
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

    public RequestPosInfo periodicEventInfo(PeriodicEventInfo periodicEventInfo)
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

    public RequestPosInfo areaEventInfo(AreaEventInfo areaEventInfo)
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

    public AreaEventInfo getAreaEventInfo()
    {
        return areaEventInfo;
    }

    @JsonProperty(JSON_PROPERTY_AREA_EVENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaEventInfo(AreaEventInfo areaEventInfo)
    {
        this.areaEventInfo = areaEventInfo;
    }

    public RequestPosInfo motionEventInfo(MotionEventInfo motionEventInfo)
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

    public RequestPosInfo externalClientIdentification(String externalClientIdentification)
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

    public RequestPosInfo afID(UUID afID)
    {

        this.afID = afID;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return afID
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_AF_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getAfID()
    {
        return afID;
    }

    @JsonProperty(JSON_PROPERTY_AF_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfID(UUID afID)
    {
        this.afID = afID;
    }

    public RequestPosInfo codeWord(String codeWord)
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

    public RequestPosInfo uePrivacyRequirements(UePrivacyRequirements uePrivacyRequirements)
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

    public RequestPosInfo scheduledLocTime(OffsetDateTime scheduledLocTime)
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

    public RequestPosInfo reliableLocReq(Boolean reliableLocReq)
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
        RequestPosInfo requestPosInfo = (RequestPosInfo) o;
        return Objects.equals(this.lcsClientType, requestPosInfo.lcsClientType) && Objects.equals(this.lcsLocation, requestPosInfo.lcsLocation)
               && Objects.equals(this.supi, requestPosInfo.supi) && Objects.equals(this.gpsi, requestPosInfo.gpsi)
               && Objects.equals(this.priority, requestPosInfo.priority) && Objects.equals(this.lcsQoS, requestPosInfo.lcsQoS)
               && Objects.equals(this.velocityRequested, requestPosInfo.velocityRequested)
               && Objects.equals(this.lcsSupportedGADShapes, requestPosInfo.lcsSupportedGADShapes)
               && Objects.equals(this.additionalLcsSuppGADShapes, requestPosInfo.additionalLcsSuppGADShapes)
               && Objects.equals(this.locationNotificationUri, requestPosInfo.locationNotificationUri)
               && Objects.equals(this.supportedFeatures, requestPosInfo.supportedFeatures) && Objects.equals(this.oldGuami, requestPosInfo.oldGuami)
               && Objects.equals(this.pei, requestPosInfo.pei) && Objects.equals(this.lcsServiceType, requestPosInfo.lcsServiceType)
               && Objects.equals(this.ldrType, requestPosInfo.ldrType) && Objects.equals(this.hgmlcCallBackURI, requestPosInfo.hgmlcCallBackURI)
               && Objects.equals(this.ldrReference, requestPosInfo.ldrReference) && Objects.equals(this.periodicEventInfo, requestPosInfo.periodicEventInfo)
               && Objects.equals(this.areaEventInfo, requestPosInfo.areaEventInfo) && Objects.equals(this.motionEventInfo, requestPosInfo.motionEventInfo)
               && Objects.equals(this.externalClientIdentification, requestPosInfo.externalClientIdentification)
               && Objects.equals(this.afID, requestPosInfo.afID) && Objects.equals(this.codeWord, requestPosInfo.codeWord)
               && Objects.equals(this.uePrivacyRequirements, requestPosInfo.uePrivacyRequirements)
               && Objects.equals(this.scheduledLocTime, requestPosInfo.scheduledLocTime) && Objects.equals(this.reliableLocReq, requestPosInfo.reliableLocReq);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lcsClientType,
                            lcsLocation,
                            supi,
                            gpsi,
                            priority,
                            lcsQoS,
                            velocityRequested,
                            lcsSupportedGADShapes,
                            additionalLcsSuppGADShapes,
                            locationNotificationUri,
                            supportedFeatures,
                            oldGuami,
                            pei,
                            lcsServiceType,
                            ldrType,
                            hgmlcCallBackURI,
                            ldrReference,
                            periodicEventInfo,
                            areaEventInfo,
                            motionEventInfo,
                            externalClientIdentification,
                            afID,
                            codeWord,
                            uePrivacyRequirements,
                            scheduledLocTime,
                            reliableLocReq);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RequestPosInfo {\n");
        sb.append("    lcsClientType: ").append(toIndentedString(lcsClientType)).append("\n");
        sb.append("    lcsLocation: ").append(toIndentedString(lcsLocation)).append("\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
        sb.append("    lcsQoS: ").append(toIndentedString(lcsQoS)).append("\n");
        sb.append("    velocityRequested: ").append(toIndentedString(velocityRequested)).append("\n");
        sb.append("    lcsSupportedGADShapes: ").append(toIndentedString(lcsSupportedGADShapes)).append("\n");
        sb.append("    additionalLcsSuppGADShapes: ").append(toIndentedString(additionalLcsSuppGADShapes)).append("\n");
        sb.append("    locationNotificationUri: ").append(toIndentedString(locationNotificationUri)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    oldGuami: ").append(toIndentedString(oldGuami)).append("\n");
        sb.append("    pei: ").append(toIndentedString(pei)).append("\n");
        sb.append("    lcsServiceType: ").append(toIndentedString(lcsServiceType)).append("\n");
        sb.append("    ldrType: ").append(toIndentedString(ldrType)).append("\n");
        sb.append("    hgmlcCallBackURI: ").append(toIndentedString(hgmlcCallBackURI)).append("\n");
        sb.append("    ldrReference: ").append(toIndentedString(ldrReference)).append("\n");
        sb.append("    periodicEventInfo: ").append(toIndentedString(periodicEventInfo)).append("\n");
        sb.append("    areaEventInfo: ").append(toIndentedString(areaEventInfo)).append("\n");
        sb.append("    motionEventInfo: ").append(toIndentedString(motionEventInfo)).append("\n");
        sb.append("    externalClientIdentification: ").append(toIndentedString(externalClientIdentification)).append("\n");
        sb.append("    afID: ").append(toIndentedString(afID)).append("\n");
        sb.append("    codeWord: ").append(toIndentedString(codeWord)).append("\n");
        sb.append("    uePrivacyRequirements: ").append(toIndentedString(uePrivacyRequirements)).append("\n");
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
