/*
 * Nnwdaf_AnalyticsInfo
 * Nnwdaf_AnalyticsInfo Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.analyticsinfo;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29575.nadrf.datamanagement.DataSubscription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents an existing subscription for data collection to a specific data
 * source NF.
 */
@ApiModel(description = "Represents an existing subscription for data collection to a specific data source NF. ")
@JsonPropertyOrder({ SpecificDataSubscription.JSON_PROPERTY_SUBSCRIPTION_ID,
                     SpecificDataSubscription.JSON_PROPERTY_PRODUCER_ID,
                     SpecificDataSubscription.JSON_PROPERTY_PRODUCER_SET_ID,
                     SpecificDataSubscription.JSON_PROPERTY_DATA_SUB })
public class SpecificDataSubscription
{
    public static final String JSON_PROPERTY_SUBSCRIPTION_ID = "subscriptionId";
    private String subscriptionId;

    public static final String JSON_PROPERTY_PRODUCER_ID = "producerId";
    private UUID producerId;

    public static final String JSON_PROPERTY_PRODUCER_SET_ID = "producerSetId";
    private String producerSetId;

    public static final String JSON_PROPERTY_DATA_SUB = "dataSub";
    private DataSubscription dataSub;

    public SpecificDataSubscription()
    {
    }

    public SpecificDataSubscription subscriptionId(String subscriptionId)
    {

        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * Get subscriptionId
     * 
     * @return subscriptionId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscriptionId(String subscriptionId)
    {
        this.subscriptionId = subscriptionId;
    }

    public SpecificDataSubscription producerId(UUID producerId)
    {

        this.producerId = producerId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return producerId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_PRODUCER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getProducerId()
    {
        return producerId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerId(UUID producerId)
    {
        this.producerId = producerId;
    }

    public SpecificDataSubscription producerSetId(String producerSetId)
    {

        this.producerSetId = producerSetId;
        return this;
    }

    /**
     * NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the
     * following string \&quot;set&lt;Set
     * ID&gt;.&lt;nftype&gt;set.5gc.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;, or
     * \&quot;set&lt;SetID&gt;.&lt;NFType&gt;set.5gc.nid&lt;NID&gt;.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;
     * with &lt;MCC&gt; encoded as defined in clause 5.4.2 (\&quot;Mcc\&quot; data
     * type definition) &lt;MNC&gt; encoding the Mobile Network Code part of the
     * PLMN, comprising 3 digits. If there are only 2 significant digits in the MNC,
     * one \&quot;0\&quot; digit shall be inserted at the left side to fill the 3
     * digits coding of MNC. Pattern: &#39;^[0-9]{3}$&#39; &lt;NFType&gt; encoded as
     * a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but with lower case
     * characters &lt;Set ID&gt; encoded as a string of characters consisting of
     * alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and
     * that shall end with either an alphabetic character or a digit.
     * 
     * @return producerSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_PRODUCER_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProducerSetId()
    {
        return producerSetId;
    }

    @JsonProperty(JSON_PROPERTY_PRODUCER_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProducerSetId(String producerSetId)
    {
        this.producerSetId = producerSetId;
    }

    public SpecificDataSubscription dataSub(DataSubscription dataSub)
    {

        this.dataSub = dataSub;
        return this;
    }

    /**
     * Get dataSub
     * 
     * @return dataSub
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DATA_SUB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DataSubscription getDataSub()
    {
        return dataSub;
    }

    @JsonProperty(JSON_PROPERTY_DATA_SUB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDataSub(DataSubscription dataSub)
    {
        this.dataSub = dataSub;
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
        SpecificDataSubscription specificDataSubscription = (SpecificDataSubscription) o;
        return Objects.equals(this.subscriptionId, specificDataSubscription.subscriptionId)
               && Objects.equals(this.producerId, specificDataSubscription.producerId)
               && Objects.equals(this.producerSetId, specificDataSubscription.producerSetId) && Objects.equals(this.dataSub, specificDataSubscription.dataSub);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(subscriptionId, producerId, producerSetId, dataSub);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SpecificDataSubscription {\n");
        sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
        sb.append("    producerId: ").append(toIndentedString(producerId)).append("\n");
        sb.append("    producerSetId: ").append(toIndentedString(producerSetId)).append("\n");
        sb.append("    dataSub: ").append(toIndentedString(dataSub)).append("\n");
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
