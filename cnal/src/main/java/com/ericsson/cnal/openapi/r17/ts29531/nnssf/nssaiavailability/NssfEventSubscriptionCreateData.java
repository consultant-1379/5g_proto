/*
 * NSSF NSSAI Availability
 * NSSF NSSAI Availability Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29531.nnssf.nssaiavailability;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Tai;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.TaiRange;
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
 * This contains the information for event subscription
 */
@ApiModel(description = "This contains the information for event subscription")
@JsonPropertyOrder({ NssfEventSubscriptionCreateData.JSON_PROPERTY_NF_NSSAI_AVAILABILITY_URI,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_TAI_LIST,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_EVENT,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_EXPIRY,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_AMF_SET_ID,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_TAI_RANGE_LIST,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_AMF_ID,
                     NssfEventSubscriptionCreateData.JSON_PROPERTY_SUPPORTED_FEATURES })
public class NssfEventSubscriptionCreateData
{
    public static final String JSON_PROPERTY_NF_NSSAI_AVAILABILITY_URI = "nfNssaiAvailabilityUri";
    private String nfNssaiAvailabilityUri;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = new ArrayList<>();

    public static final String JSON_PROPERTY_EVENT = "event";
    private String event;

    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private OffsetDateTime expiry;

    public static final String JSON_PROPERTY_AMF_SET_ID = "amfSetId";
    private String amfSetId;

    public static final String JSON_PROPERTY_TAI_RANGE_LIST = "taiRangeList";
    private List<TaiRange> taiRangeList = null;

    public static final String JSON_PROPERTY_AMF_ID = "amfId";
    private UUID amfId;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public NssfEventSubscriptionCreateData()
    {
    }

    public NssfEventSubscriptionCreateData nfNssaiAvailabilityUri(String nfNssaiAvailabilityUri)
    {

        this.nfNssaiAvailabilityUri = nfNssaiAvailabilityUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return nfNssaiAvailabilityUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_NF_NSSAI_AVAILABILITY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getNfNssaiAvailabilityUri()
    {
        return nfNssaiAvailabilityUri;
    }

    @JsonProperty(JSON_PROPERTY_NF_NSSAI_AVAILABILITY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNfNssaiAvailabilityUri(String nfNssaiAvailabilityUri)
    {
        this.nfNssaiAvailabilityUri = nfNssaiAvailabilityUri;
    }

    public NssfEventSubscriptionCreateData taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public NssfEventSubscriptionCreateData addTaiListItem(Tai taiListItem)
    {
        this.taiList.add(taiListItem);
        return this;
    }

    /**
     * Get taiList
     * 
     * @return taiList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Tai> getTaiList()
    {
        return taiList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTaiList(List<Tai> taiList)
    {
        this.taiList = taiList;
    }

    public NssfEventSubscriptionCreateData event(String event)
    {

        this.event = event;
        return this;
    }

    /**
     * This contains the event for the subscription
     * 
     * @return event
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "This contains the event for the subscription")
    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEvent()
    {
        return event;
    }

    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEvent(String event)
    {
        this.event = event;
    }

    public NssfEventSubscriptionCreateData expiry(OffsetDateTime expiry)
    {

        this.expiry = expiry;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return expiry
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getExpiry()
    {
        return expiry;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpiry(OffsetDateTime expiry)
    {
        this.expiry = expiry;
    }

    public NssfEventSubscriptionCreateData amfSetId(String amfSetId)
    {

        this.amfSetId = amfSetId;
        return this;
    }

    /**
     * Get amfSetId
     * 
     * @return amfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAmfSetId()
    {
        return amfSetId;
    }

    @JsonProperty(JSON_PROPERTY_AMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfSetId(String amfSetId)
    {
        this.amfSetId = amfSetId;
    }

    public NssfEventSubscriptionCreateData taiRangeList(List<TaiRange> taiRangeList)
    {

        this.taiRangeList = taiRangeList;
        return this;
    }

    public NssfEventSubscriptionCreateData addTaiRangeListItem(TaiRange taiRangeListItem)
    {
        if (this.taiRangeList == null)
        {
            this.taiRangeList = new ArrayList<>();
        }
        this.taiRangeList.add(taiRangeListItem);
        return this;
    }

    /**
     * Get taiRangeList
     * 
     * @return taiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TaiRange> getTaiRangeList()
    {
        return taiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiRangeList(List<TaiRange> taiRangeList)
    {
        this.taiRangeList = taiRangeList;
    }

    public NssfEventSubscriptionCreateData amfId(UUID amfId)
    {

        this.amfId = amfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return amfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getAmfId()
    {
        return amfId;
    }

    @JsonProperty(JSON_PROPERTY_AMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfId(UUID amfId)
    {
        this.amfId = amfId;
    }

    public NssfEventSubscriptionCreateData supportedFeatures(String supportedFeatures)
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
        NssfEventSubscriptionCreateData nssfEventSubscriptionCreateData = (NssfEventSubscriptionCreateData) o;
        return Objects.equals(this.nfNssaiAvailabilityUri, nssfEventSubscriptionCreateData.nfNssaiAvailabilityUri)
               && Objects.equals(this.taiList, nssfEventSubscriptionCreateData.taiList) && Objects.equals(this.event, nssfEventSubscriptionCreateData.event)
               && Objects.equals(this.expiry, nssfEventSubscriptionCreateData.expiry) && Objects.equals(this.amfSetId, nssfEventSubscriptionCreateData.amfSetId)
               && Objects.equals(this.taiRangeList, nssfEventSubscriptionCreateData.taiRangeList)
               && Objects.equals(this.amfId, nssfEventSubscriptionCreateData.amfId)
               && Objects.equals(this.supportedFeatures, nssfEventSubscriptionCreateData.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nfNssaiAvailabilityUri, taiList, event, expiry, amfSetId, taiRangeList, amfId, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NssfEventSubscriptionCreateData {\n");
        sb.append("    nfNssaiAvailabilityUri: ").append(toIndentedString(nfNssaiAvailabilityUri)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    amfSetId: ").append(toIndentedString(amfSetId)).append("\n");
        sb.append("    taiRangeList: ").append(toIndentedString(taiRangeList)).append("\n");
        sb.append("    amfId: ").append(toIndentedString(amfId)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
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
