/*
 * LMF Broadcast
 * LMF Broadcast Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29572.nlmf.broadcast;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a Ciphering Data Set.
 */
@ApiModel(description = "Represents a Ciphering Data Set.")
@JsonPropertyOrder({ CipheringDataSet.JSON_PROPERTY_CIPHERING_SET_I_D,
                     CipheringDataSet.JSON_PROPERTY_CIPHERING_KEY,
                     CipheringDataSet.JSON_PROPERTY_C0,
                     CipheringDataSet.JSON_PROPERTY_LTE_POS_SIB_TYPES,
                     CipheringDataSet.JSON_PROPERTY_NR_POS_SIB_TYPES,
                     CipheringDataSet.JSON_PROPERTY_VALIDITY_START_TIME,
                     CipheringDataSet.JSON_PROPERTY_VALIDITY_DURATION,
                     CipheringDataSet.JSON_PROPERTY_TAI_LIST })
public class CipheringDataSet
{
    public static final String JSON_PROPERTY_CIPHERING_SET_I_D = "cipheringSetID";
    private Integer cipheringSetID;

    public static final String JSON_PROPERTY_CIPHERING_KEY = "cipheringKey";
    private byte[] cipheringKey;

    public static final String JSON_PROPERTY_C0 = "c0";
    private byte[] c0;

    public static final String JSON_PROPERTY_LTE_POS_SIB_TYPES = "ltePosSibTypes";
    private byte[] ltePosSibTypes;

    public static final String JSON_PROPERTY_NR_POS_SIB_TYPES = "nrPosSibTypes";
    private byte[] nrPosSibTypes;

    public static final String JSON_PROPERTY_VALIDITY_START_TIME = "validityStartTime";
    private OffsetDateTime validityStartTime;

    public static final String JSON_PROPERTY_VALIDITY_DURATION = "validityDuration";
    private Integer validityDuration;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private byte[] taiList;

    public CipheringDataSet()
    {
    }

    public CipheringDataSet cipheringSetID(Integer cipheringSetID)
    {

        this.cipheringSetID = cipheringSetID;
        return this;
    }

    /**
     * Ciphering Data Set Identifier. minimum: 0 maximum: 65535
     * 
     * @return cipheringSetID
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Ciphering Data Set Identifier.")
    @JsonProperty(JSON_PROPERTY_CIPHERING_SET_I_D)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getCipheringSetID()
    {
        return cipheringSetID;
    }

    @JsonProperty(JSON_PROPERTY_CIPHERING_SET_I_D)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCipheringSetID(Integer cipheringSetID)
    {
        this.cipheringSetID = cipheringSetID;
    }

    public CipheringDataSet cipheringKey(byte[] cipheringKey)
    {

        this.cipheringKey = cipheringKey;
        return this;
    }

    /**
     * Ciphering Key.
     * 
     * @return cipheringKey
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Ciphering Key.")
    @JsonProperty(JSON_PROPERTY_CIPHERING_KEY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public byte[] getCipheringKey()
    {
        return cipheringKey;
    }

    @JsonProperty(JSON_PROPERTY_CIPHERING_KEY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCipheringKey(byte[] cipheringKey)
    {
        this.cipheringKey = cipheringKey;
    }

    public CipheringDataSet c0(byte[] c0)
    {

        this.c0 = c0;
        return this;
    }

    /**
     * First component of the initial ciphering counter.
     * 
     * @return c0
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "First component of the initial ciphering counter.")
    @JsonProperty(JSON_PROPERTY_C0)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public byte[] getC0()
    {
        return c0;
    }

    @JsonProperty(JSON_PROPERTY_C0)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setC0(byte[] c0)
    {
        this.c0 = c0;
    }

    public CipheringDataSet ltePosSibTypes(byte[] ltePosSibTypes)
    {

        this.ltePosSibTypes = ltePosSibTypes;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return ltePosSibTypes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_LTE_POS_SIB_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getLtePosSibTypes()
    {
        return ltePosSibTypes;
    }

    @JsonProperty(JSON_PROPERTY_LTE_POS_SIB_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLtePosSibTypes(byte[] ltePosSibTypes)
    {
        this.ltePosSibTypes = ltePosSibTypes;
    }

    public CipheringDataSet nrPosSibTypes(byte[] nrPosSibTypes)
    {

        this.nrPosSibTypes = nrPosSibTypes;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return nrPosSibTypes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_NR_POS_SIB_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getNrPosSibTypes()
    {
        return nrPosSibTypes;
    }

    @JsonProperty(JSON_PROPERTY_NR_POS_SIB_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNrPosSibTypes(byte[] nrPosSibTypes)
    {
        this.nrPosSibTypes = nrPosSibTypes;
    }

    public CipheringDataSet validityStartTime(OffsetDateTime validityStartTime)
    {

        this.validityStartTime = validityStartTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return validityStartTime
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_VALIDITY_START_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getValidityStartTime()
    {
        return validityStartTime;
    }

    @JsonProperty(JSON_PROPERTY_VALIDITY_START_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setValidityStartTime(OffsetDateTime validityStartTime)
    {
        this.validityStartTime = validityStartTime;
    }

    public CipheringDataSet validityDuration(Integer validityDuration)
    {

        this.validityDuration = validityDuration;
        return this;
    }

    /**
     * Validity Duration of the Ciphering Data Set. minimum: 1 maximum: 65535
     * 
     * @return validityDuration
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Validity Duration of the Ciphering Data Set.")
    @JsonProperty(JSON_PROPERTY_VALIDITY_DURATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getValidityDuration()
    {
        return validityDuration;
    }

    @JsonProperty(JSON_PROPERTY_VALIDITY_DURATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setValidityDuration(Integer validityDuration)
    {
        this.validityDuration = validityDuration;
    }

    public CipheringDataSet taiList(byte[] taiList)
    {

        this.taiList = taiList;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return taiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getTaiList()
    {
        return taiList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiList(byte[] taiList)
    {
        this.taiList = taiList;
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
        CipheringDataSet cipheringDataSet = (CipheringDataSet) o;
        return Objects.equals(this.cipheringSetID, cipheringDataSet.cipheringSetID) && Arrays.equals(this.cipheringKey, cipheringDataSet.cipheringKey)
               && Arrays.equals(this.c0, cipheringDataSet.c0) && Arrays.equals(this.ltePosSibTypes, cipheringDataSet.ltePosSibTypes)
               && Arrays.equals(this.nrPosSibTypes, cipheringDataSet.nrPosSibTypes)
               && Objects.equals(this.validityStartTime, cipheringDataSet.validityStartTime)
               && Objects.equals(this.validityDuration, cipheringDataSet.validityDuration) && Arrays.equals(this.taiList, cipheringDataSet.taiList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cipheringSetID,
                            Arrays.hashCode(cipheringKey),
                            Arrays.hashCode(c0),
                            Arrays.hashCode(ltePosSibTypes),
                            Arrays.hashCode(nrPosSibTypes),
                            validityStartTime,
                            validityDuration,
                            Arrays.hashCode(taiList));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CipheringDataSet {\n");
        sb.append("    cipheringSetID: ").append(toIndentedString(cipheringSetID)).append("\n");
        sb.append("    cipheringKey: ").append(toIndentedString(cipheringKey)).append("\n");
        sb.append("    c0: ").append(toIndentedString(c0)).append("\n");
        sb.append("    ltePosSibTypes: ").append(toIndentedString(ltePosSibTypes)).append("\n");
        sb.append("    nrPosSibTypes: ").append(toIndentedString(nrPosSibTypes)).append("\n");
        sb.append("    validityStartTime: ").append(toIndentedString(validityStartTime)).append("\n");
        sb.append("    validityDuration: ").append(toIndentedString(validityDuration)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
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
