/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UserLocation;
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
 * SMSChargingInformation
 */
@JsonPropertyOrder({ SMSChargingInformation.JSON_PROPERTY_ORIGINATOR_INFO,
                     SMSChargingInformation.JSON_PROPERTY_RECIPIENT_INFO,
                     SMSChargingInformation.JSON_PROPERTY_USER_EQUIPMENT_INFO,
                     SMSChargingInformation.JSON_PROPERTY_ROAMER_IN_OUT,
                     SMSChargingInformation.JSON_PROPERTY_USER_LOCATIONINFO,
                     SMSChargingInformation.JSON_PROPERTY_UETIME_ZONE,
                     SMSChargingInformation.JSON_PROPERTY_R_A_T_TYPE,
                     SMSChargingInformation.JSON_PROPERTY_S_M_S_C_ADDRESS,
                     SMSChargingInformation.JSON_PROPERTY_S_M_DATA_CODING_SCHEME,
                     SMSChargingInformation.JSON_PROPERTY_S_M_MESSAGE_TYPE,
                     SMSChargingInformation.JSON_PROPERTY_S_M_REPLY_PATH_REQUESTED,
                     SMSChargingInformation.JSON_PROPERTY_S_M_USER_DATA_HEADER,
                     SMSChargingInformation.JSON_PROPERTY_S_M_STATUS,
                     SMSChargingInformation.JSON_PROPERTY_S_M_DISCHARGE_TIME,
                     SMSChargingInformation.JSON_PROPERTY_NUMBEROF_MESSAGES_SENT,
                     SMSChargingInformation.JSON_PROPERTY_S_M_SERVICE_TYPE,
                     SMSChargingInformation.JSON_PROPERTY_S_M_SEQUENCE_NUMBER,
                     SMSChargingInformation.JSON_PROPERTY_S_M_SRESULT,
                     SMSChargingInformation.JSON_PROPERTY_SUBMISSION_TIME,
                     SMSChargingInformation.JSON_PROPERTY_S_M_PRIORITY,
                     SMSChargingInformation.JSON_PROPERTY_MESSAGE_REFERENCE,
                     SMSChargingInformation.JSON_PROPERTY_MESSAGE_SIZE,
                     SMSChargingInformation.JSON_PROPERTY_MESSAGE_CLASS,
                     SMSChargingInformation.JSON_PROPERTY_DELIVERY_REPORT_REQUESTED })
public class SMSChargingInformation
{
    public static final String JSON_PROPERTY_ORIGINATOR_INFO = "originatorInfo";
    private OriginatorInfo originatorInfo;

    public static final String JSON_PROPERTY_RECIPIENT_INFO = "recipientInfo";
    private List<RecipientInfo> recipientInfo = null;

    public static final String JSON_PROPERTY_USER_EQUIPMENT_INFO = "userEquipmentInfo";
    private String userEquipmentInfo;

    public static final String JSON_PROPERTY_ROAMER_IN_OUT = "roamerInOut";
    private String roamerInOut;

    public static final String JSON_PROPERTY_USER_LOCATIONINFO = "userLocationinfo";
    private UserLocation userLocationinfo;

    public static final String JSON_PROPERTY_UETIME_ZONE = "uetimeZone";
    private String uetimeZone;

    public static final String JSON_PROPERTY_R_A_T_TYPE = "rATType";
    private String rATType;

    public static final String JSON_PROPERTY_S_M_S_C_ADDRESS = "sMSCAddress";
    private String sMSCAddress;

    public static final String JSON_PROPERTY_S_M_DATA_CODING_SCHEME = "sMDataCodingScheme";
    private Integer sMDataCodingScheme;

    public static final String JSON_PROPERTY_S_M_MESSAGE_TYPE = "sMMessageType";
    private String sMMessageType;

    public static final String JSON_PROPERTY_S_M_REPLY_PATH_REQUESTED = "sMReplyPathRequested";
    private String sMReplyPathRequested;

    public static final String JSON_PROPERTY_S_M_USER_DATA_HEADER = "sMUserDataHeader";
    private String sMUserDataHeader;

    public static final String JSON_PROPERTY_S_M_STATUS = "sMStatus";
    private String sMStatus;

    public static final String JSON_PROPERTY_S_M_DISCHARGE_TIME = "sMDischargeTime";
    private OffsetDateTime sMDischargeTime;

    public static final String JSON_PROPERTY_NUMBEROF_MESSAGES_SENT = "numberofMessagesSent";
    private Integer numberofMessagesSent;

    public static final String JSON_PROPERTY_S_M_SERVICE_TYPE = "sMServiceType";
    private String sMServiceType;

    public static final String JSON_PROPERTY_S_M_SEQUENCE_NUMBER = "sMSequenceNumber";
    private Integer sMSequenceNumber;

    public static final String JSON_PROPERTY_S_M_SRESULT = "sMSresult";
    private Integer sMSresult;

    public static final String JSON_PROPERTY_SUBMISSION_TIME = "submissionTime";
    private OffsetDateTime submissionTime;

    public static final String JSON_PROPERTY_S_M_PRIORITY = "sMPriority";
    private String sMPriority;

    public static final String JSON_PROPERTY_MESSAGE_REFERENCE = "messageReference";
    private String messageReference;

    public static final String JSON_PROPERTY_MESSAGE_SIZE = "messageSize";
    private Integer messageSize;

    public static final String JSON_PROPERTY_MESSAGE_CLASS = "messageClass";
    private MessageClass messageClass;

    public static final String JSON_PROPERTY_DELIVERY_REPORT_REQUESTED = "deliveryReportRequested";
    private String deliveryReportRequested;

    public SMSChargingInformation()
    {
    }

    public SMSChargingInformation originatorInfo(OriginatorInfo originatorInfo)
    {

        this.originatorInfo = originatorInfo;
        return this;
    }

    /**
     * Get originatorInfo
     * 
     * @return originatorInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ORIGINATOR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OriginatorInfo getOriginatorInfo()
    {
        return originatorInfo;
    }

    @JsonProperty(JSON_PROPERTY_ORIGINATOR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginatorInfo(OriginatorInfo originatorInfo)
    {
        this.originatorInfo = originatorInfo;
    }

    public SMSChargingInformation recipientInfo(List<RecipientInfo> recipientInfo)
    {

        this.recipientInfo = recipientInfo;
        return this;
    }

    public SMSChargingInformation addRecipientInfoItem(RecipientInfo recipientInfoItem)
    {
        if (this.recipientInfo == null)
        {
            this.recipientInfo = new ArrayList<>();
        }
        this.recipientInfo.add(recipientInfoItem);
        return this;
    }

    /**
     * Get recipientInfo
     * 
     * @return recipientInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECIPIENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RecipientInfo> getRecipientInfo()
    {
        return recipientInfo;
    }

    @JsonProperty(JSON_PROPERTY_RECIPIENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecipientInfo(List<RecipientInfo> recipientInfo)
    {
        this.recipientInfo = recipientInfo;
    }

    public SMSChargingInformation userEquipmentInfo(String userEquipmentInfo)
    {

        this.userEquipmentInfo = userEquipmentInfo;
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
     * @return userEquipmentInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a Permanent Equipment Identifier that may contain - an IMEI or IMEISV, as  specified in clause 6.2 of 3GPP TS 23.003; a MAC address for a 5G-RG or FN-RG via  wireline  access, with an indication that this address cannot be trusted for regulatory purpose if this  address cannot be used as an Equipment Identifier of the FN-RG, as specified in clause 4.7.7  of 3GPP TS23.316. Examples are imei-012345678901234 or imeisv-0123456789012345.  ")
    @JsonProperty(JSON_PROPERTY_USER_EQUIPMENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUserEquipmentInfo()
    {
        return userEquipmentInfo;
    }

    @JsonProperty(JSON_PROPERTY_USER_EQUIPMENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserEquipmentInfo(String userEquipmentInfo)
    {
        this.userEquipmentInfo = userEquipmentInfo;
    }

    public SMSChargingInformation roamerInOut(String roamerInOut)
    {

        this.roamerInOut = roamerInOut;
        return this;
    }

    /**
     * Get roamerInOut
     * 
     * @return roamerInOut
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ROAMER_IN_OUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRoamerInOut()
    {
        return roamerInOut;
    }

    @JsonProperty(JSON_PROPERTY_ROAMER_IN_OUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoamerInOut(String roamerInOut)
    {
        this.roamerInOut = roamerInOut;
    }

    public SMSChargingInformation userLocationinfo(UserLocation userLocationinfo)
    {

        this.userLocationinfo = userLocationinfo;
        return this;
    }

    /**
     * Get userLocationinfo
     * 
     * @return userLocationinfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USER_LOCATIONINFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getUserLocationinfo()
    {
        return userLocationinfo;
    }

    @JsonProperty(JSON_PROPERTY_USER_LOCATIONINFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserLocationinfo(UserLocation userLocationinfo)
    {
        this.userLocationinfo = userLocationinfo;
    }

    public SMSChargingInformation uetimeZone(String uetimeZone)
    {

        this.uetimeZone = uetimeZone;
        return this;
    }

    /**
     * String with format \&quot;time-numoffset\&quot; optionally appended by
     * \&quot;daylightSavingTime\&quot;, where - \&quot;time-numoffset\&quot; shall
     * represent the time zone adjusted for daylight saving time and be encoded as
     * time-numoffset as defined in clause 5.6 of IETF RFC 3339; -
     * \&quot;daylightSavingTime\&quot; shall represent the adjustment that has been
     * made and shall be encoded as \&quot;+1\&quot; or \&quot;+2\&quot; for a +1 or
     * +2 hours adjustment. The example is for 8 hours behind UTC, +1 hour
     * adjustment for Daylight Saving Time.
     * 
     * @return uetimeZone
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "-08:00+1",
                      value = "String with format \"time-numoffset\" optionally appended by \"daylightSavingTime\", where  - \"time-numoffset\" shall represent the time zone adjusted for daylight saving time and be    encoded as time-numoffset as defined in clause 5.6 of IETF RFC 3339;  - \"daylightSavingTime\" shall represent the adjustment that has been made and shall be    encoded as \"+1\" or \"+2\" for a +1 or +2 hours adjustment.  The example is for 8 hours behind UTC, +1 hour adjustment for Daylight Saving Time. ")
    @JsonProperty(JSON_PROPERTY_UETIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUetimeZone()
    {
        return uetimeZone;
    }

    @JsonProperty(JSON_PROPERTY_UETIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUetimeZone(String uetimeZone)
    {
        this.uetimeZone = uetimeZone;
    }

    public SMSChargingInformation rATType(String rATType)
    {

        this.rATType = rATType;
        return this;
    }

    /**
     * Indicates the radio access used.
     * 
     * @return rATType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the radio access used.")
    @JsonProperty(JSON_PROPERTY_R_A_T_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getrATType()
    {
        return rATType;
    }

    @JsonProperty(JSON_PROPERTY_R_A_T_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setrATType(String rATType)
    {
        this.rATType = rATType;
    }

    public SMSChargingInformation sMSCAddress(String sMSCAddress)
    {

        this.sMSCAddress = sMSCAddress;
        return this;
    }

    /**
     * Get sMSCAddress
     * 
     * @return sMSCAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_S_C_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMSCAddress()
    {
        return sMSCAddress;
    }

    @JsonProperty(JSON_PROPERTY_S_M_S_C_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMSCAddress(String sMSCAddress)
    {
        this.sMSCAddress = sMSCAddress;
    }

    public SMSChargingInformation sMDataCodingScheme(Integer sMDataCodingScheme)
    {

        this.sMDataCodingScheme = sMDataCodingScheme;
        return this;
    }

    /**
     * Get sMDataCodingScheme
     * 
     * @return sMDataCodingScheme
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_DATA_CODING_SCHEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getsMDataCodingScheme()
    {
        return sMDataCodingScheme;
    }

    @JsonProperty(JSON_PROPERTY_S_M_DATA_CODING_SCHEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMDataCodingScheme(Integer sMDataCodingScheme)
    {
        this.sMDataCodingScheme = sMDataCodingScheme;
    }

    public SMSChargingInformation sMMessageType(String sMMessageType)
    {

        this.sMMessageType = sMMessageType;
        return this;
    }

    /**
     * Get sMMessageType
     * 
     * @return sMMessageType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_MESSAGE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMMessageType()
    {
        return sMMessageType;
    }

    @JsonProperty(JSON_PROPERTY_S_M_MESSAGE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMMessageType(String sMMessageType)
    {
        this.sMMessageType = sMMessageType;
    }

    public SMSChargingInformation sMReplyPathRequested(String sMReplyPathRequested)
    {

        this.sMReplyPathRequested = sMReplyPathRequested;
        return this;
    }

    /**
     * Get sMReplyPathRequested
     * 
     * @return sMReplyPathRequested
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_REPLY_PATH_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMReplyPathRequested()
    {
        return sMReplyPathRequested;
    }

    @JsonProperty(JSON_PROPERTY_S_M_REPLY_PATH_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMReplyPathRequested(String sMReplyPathRequested)
    {
        this.sMReplyPathRequested = sMReplyPathRequested;
    }

    public SMSChargingInformation sMUserDataHeader(String sMUserDataHeader)
    {

        this.sMUserDataHeader = sMUserDataHeader;
        return this;
    }

    /**
     * Get sMUserDataHeader
     * 
     * @return sMUserDataHeader
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_USER_DATA_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMUserDataHeader()
    {
        return sMUserDataHeader;
    }

    @JsonProperty(JSON_PROPERTY_S_M_USER_DATA_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMUserDataHeader(String sMUserDataHeader)
    {
        this.sMUserDataHeader = sMUserDataHeader;
    }

    public SMSChargingInformation sMStatus(String sMStatus)
    {

        this.sMStatus = sMStatus;
        return this;
    }

    /**
     * Get sMStatus
     * 
     * @return sMStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMStatus()
    {
        return sMStatus;
    }

    @JsonProperty(JSON_PROPERTY_S_M_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMStatus(String sMStatus)
    {
        this.sMStatus = sMStatus;
    }

    public SMSChargingInformation sMDischargeTime(OffsetDateTime sMDischargeTime)
    {

        this.sMDischargeTime = sMDischargeTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return sMDischargeTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_S_M_DISCHARGE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getsMDischargeTime()
    {
        return sMDischargeTime;
    }

    @JsonProperty(JSON_PROPERTY_S_M_DISCHARGE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMDischargeTime(OffsetDateTime sMDischargeTime)
    {
        this.sMDischargeTime = sMDischargeTime;
    }

    public SMSChargingInformation numberofMessagesSent(Integer numberofMessagesSent)
    {

        this.numberofMessagesSent = numberofMessagesSent;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return numberofMessagesSent
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_NUMBEROF_MESSAGES_SENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getNumberofMessagesSent()
    {
        return numberofMessagesSent;
    }

    @JsonProperty(JSON_PROPERTY_NUMBEROF_MESSAGES_SENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumberofMessagesSent(Integer numberofMessagesSent)
    {
        this.numberofMessagesSent = numberofMessagesSent;
    }

    public SMSChargingInformation sMServiceType(String sMServiceType)
    {

        this.sMServiceType = sMServiceType;
        return this;
    }

    /**
     * Get sMServiceType
     * 
     * @return sMServiceType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_SERVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMServiceType()
    {
        return sMServiceType;
    }

    @JsonProperty(JSON_PROPERTY_S_M_SERVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMServiceType(String sMServiceType)
    {
        this.sMServiceType = sMServiceType;
    }

    public SMSChargingInformation sMSequenceNumber(Integer sMSequenceNumber)
    {

        this.sMSequenceNumber = sMSequenceNumber;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return sMSequenceNumber
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_S_M_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getsMSequenceNumber()
    {
        return sMSequenceNumber;
    }

    @JsonProperty(JSON_PROPERTY_S_M_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMSequenceNumber(Integer sMSequenceNumber)
    {
        this.sMSequenceNumber = sMSequenceNumber;
    }

    public SMSChargingInformation sMSresult(Integer sMSresult)
    {

        this.sMSresult = sMSresult;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return sMSresult
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_S_M_SRESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getsMSresult()
    {
        return sMSresult;
    }

    @JsonProperty(JSON_PROPERTY_S_M_SRESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMSresult(Integer sMSresult)
    {
        this.sMSresult = sMSresult;
    }

    public SMSChargingInformation submissionTime(OffsetDateTime submissionTime)
    {

        this.submissionTime = submissionTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return submissionTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_SUBMISSION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getSubmissionTime()
    {
        return submissionTime;
    }

    @JsonProperty(JSON_PROPERTY_SUBMISSION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubmissionTime(OffsetDateTime submissionTime)
    {
        this.submissionTime = submissionTime;
    }

    public SMSChargingInformation sMPriority(String sMPriority)
    {

        this.sMPriority = sMPriority;
        return this;
    }

    /**
     * Get sMPriority
     * 
     * @return sMPriority
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMPriority()
    {
        return sMPriority;
    }

    @JsonProperty(JSON_PROPERTY_S_M_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMPriority(String sMPriority)
    {
        this.sMPriority = sMPriority;
    }

    public SMSChargingInformation messageReference(String messageReference)
    {

        this.messageReference = messageReference;
        return this;
    }

    /**
     * Get messageReference
     * 
     * @return messageReference
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MESSAGE_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMessageReference()
    {
        return messageReference;
    }

    @JsonProperty(JSON_PROPERTY_MESSAGE_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageReference(String messageReference)
    {
        this.messageReference = messageReference;
    }

    public SMSChargingInformation messageSize(Integer messageSize)
    {

        this.messageSize = messageSize;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return messageSize
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_MESSAGE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMessageSize()
    {
        return messageSize;
    }

    @JsonProperty(JSON_PROPERTY_MESSAGE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageSize(Integer messageSize)
    {
        this.messageSize = messageSize;
    }

    public SMSChargingInformation messageClass(MessageClass messageClass)
    {

        this.messageClass = messageClass;
        return this;
    }

    /**
     * Get messageClass
     * 
     * @return messageClass
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MESSAGE_CLASS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MessageClass getMessageClass()
    {
        return messageClass;
    }

    @JsonProperty(JSON_PROPERTY_MESSAGE_CLASS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageClass(MessageClass messageClass)
    {
        this.messageClass = messageClass;
    }

    public SMSChargingInformation deliveryReportRequested(String deliveryReportRequested)
    {

        this.deliveryReportRequested = deliveryReportRequested;
        return this;
    }

    /**
     * Get deliveryReportRequested
     * 
     * @return deliveryReportRequested
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DELIVERY_REPORT_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDeliveryReportRequested()
    {
        return deliveryReportRequested;
    }

    @JsonProperty(JSON_PROPERTY_DELIVERY_REPORT_REQUESTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeliveryReportRequested(String deliveryReportRequested)
    {
        this.deliveryReportRequested = deliveryReportRequested;
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
        SMSChargingInformation smSChargingInformation = (SMSChargingInformation) o;
        return Objects.equals(this.originatorInfo, smSChargingInformation.originatorInfo)
               && Objects.equals(this.recipientInfo, smSChargingInformation.recipientInfo)
               && Objects.equals(this.userEquipmentInfo, smSChargingInformation.userEquipmentInfo)
               && Objects.equals(this.roamerInOut, smSChargingInformation.roamerInOut)
               && Objects.equals(this.userLocationinfo, smSChargingInformation.userLocationinfo)
               && Objects.equals(this.uetimeZone, smSChargingInformation.uetimeZone) && Objects.equals(this.rATType, smSChargingInformation.rATType)
               && Objects.equals(this.sMSCAddress, smSChargingInformation.sMSCAddress)
               && Objects.equals(this.sMDataCodingScheme, smSChargingInformation.sMDataCodingScheme)
               && Objects.equals(this.sMMessageType, smSChargingInformation.sMMessageType)
               && Objects.equals(this.sMReplyPathRequested, smSChargingInformation.sMReplyPathRequested)
               && Objects.equals(this.sMUserDataHeader, smSChargingInformation.sMUserDataHeader)
               && Objects.equals(this.sMStatus, smSChargingInformation.sMStatus) && Objects.equals(this.sMDischargeTime, smSChargingInformation.sMDischargeTime)
               && Objects.equals(this.numberofMessagesSent, smSChargingInformation.numberofMessagesSent)
               && Objects.equals(this.sMServiceType, smSChargingInformation.sMServiceType)
               && Objects.equals(this.sMSequenceNumber, smSChargingInformation.sMSequenceNumber)
               && Objects.equals(this.sMSresult, smSChargingInformation.sMSresult) && Objects.equals(this.submissionTime, smSChargingInformation.submissionTime)
               && Objects.equals(this.sMPriority, smSChargingInformation.sMPriority)
               && Objects.equals(this.messageReference, smSChargingInformation.messageReference)
               && Objects.equals(this.messageSize, smSChargingInformation.messageSize) && Objects.equals(this.messageClass, smSChargingInformation.messageClass)
               && Objects.equals(this.deliveryReportRequested, smSChargingInformation.deliveryReportRequested);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(originatorInfo,
                            recipientInfo,
                            userEquipmentInfo,
                            roamerInOut,
                            userLocationinfo,
                            uetimeZone,
                            rATType,
                            sMSCAddress,
                            sMDataCodingScheme,
                            sMMessageType,
                            sMReplyPathRequested,
                            sMUserDataHeader,
                            sMStatus,
                            sMDischargeTime,
                            numberofMessagesSent,
                            sMServiceType,
                            sMSequenceNumber,
                            sMSresult,
                            submissionTime,
                            sMPriority,
                            messageReference,
                            messageSize,
                            messageClass,
                            deliveryReportRequested);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SMSChargingInformation {\n");
        sb.append("    originatorInfo: ").append(toIndentedString(originatorInfo)).append("\n");
        sb.append("    recipientInfo: ").append(toIndentedString(recipientInfo)).append("\n");
        sb.append("    userEquipmentInfo: ").append(toIndentedString(userEquipmentInfo)).append("\n");
        sb.append("    roamerInOut: ").append(toIndentedString(roamerInOut)).append("\n");
        sb.append("    userLocationinfo: ").append(toIndentedString(userLocationinfo)).append("\n");
        sb.append("    uetimeZone: ").append(toIndentedString(uetimeZone)).append("\n");
        sb.append("    rATType: ").append(toIndentedString(rATType)).append("\n");
        sb.append("    sMSCAddress: ").append(toIndentedString(sMSCAddress)).append("\n");
        sb.append("    sMDataCodingScheme: ").append(toIndentedString(sMDataCodingScheme)).append("\n");
        sb.append("    sMMessageType: ").append(toIndentedString(sMMessageType)).append("\n");
        sb.append("    sMReplyPathRequested: ").append(toIndentedString(sMReplyPathRequested)).append("\n");
        sb.append("    sMUserDataHeader: ").append(toIndentedString(sMUserDataHeader)).append("\n");
        sb.append("    sMStatus: ").append(toIndentedString(sMStatus)).append("\n");
        sb.append("    sMDischargeTime: ").append(toIndentedString(sMDischargeTime)).append("\n");
        sb.append("    numberofMessagesSent: ").append(toIndentedString(numberofMessagesSent)).append("\n");
        sb.append("    sMServiceType: ").append(toIndentedString(sMServiceType)).append("\n");
        sb.append("    sMSequenceNumber: ").append(toIndentedString(sMSequenceNumber)).append("\n");
        sb.append("    sMSresult: ").append(toIndentedString(sMSresult)).append("\n");
        sb.append("    submissionTime: ").append(toIndentedString(submissionTime)).append("\n");
        sb.append("    sMPriority: ").append(toIndentedString(sMPriority)).append("\n");
        sb.append("    messageReference: ").append(toIndentedString(messageReference)).append("\n");
        sb.append("    messageSize: ").append(toIndentedString(messageSize)).append("\n");
        sb.append("    messageClass: ").append(toIndentedString(messageClass)).append("\n");
        sb.append("    deliveryReportRequested: ").append(toIndentedString(deliveryReportRequested)).append("\n");
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
