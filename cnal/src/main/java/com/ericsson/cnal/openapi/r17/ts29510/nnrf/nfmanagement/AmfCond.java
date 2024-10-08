/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Subscription to a set of AMFs, based on AMF Set Id and/or AMF Region Id
 */
@ApiModel(description = "Subscription to a set of AMFs, based on AMF Set Id and/or AMF Region Id")
@JsonPropertyOrder({ AmfCond.JSON_PROPERTY_AMF_SET_ID, AmfCond.JSON_PROPERTY_AMF_REGION_ID })
public class AmfCond
{
    public static final String JSON_PROPERTY_AMF_SET_ID = "amfSetId";
    private String amfSetId;

    public static final String JSON_PROPERTY_AMF_REGION_ID = "amfRegionId";
    private String amfRegionId;

    public AmfCond()
    {
    }

    public AmfCond amfSetId(String amfSetId)
    {

        this.amfSetId = amfSetId;
        return this;
    }

    /**
     * String identifying the AMF Set ID (10 bits) as specified in clause 2.10.1 of
     * 3GPP TS 23.003. It is encoded as a string of 3 hexadecimal characters where
     * the first character is limited to values 0 to 3 (i.e. 10 bits).
     * 
     * @return amfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying the AMF Set ID (10 bits) as specified in clause 2.10.1 of 3GPP TS 23.003.  It is encoded as a string of 3 hexadecimal characters where the first character is limited to  values 0 to 3 (i.e. 10 bits). ")
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

    public AmfCond amfRegionId(String amfRegionId)
    {

        this.amfRegionId = amfRegionId;
        return this;
    }

    /**
     * String identifying the AMF Set ID (10 bits) as specified in clause 2.10.1 of
     * 3GPP TS 23.003. It is encoded as a string of 3 hexadecimal characters where
     * the first character is limited to values 0 to 3 (i.e. 10 bits)
     * 
     * @return amfRegionId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying the AMF Set ID (10 bits) as specified in clause 2.10.1 of 3GPP TS 23.003.  It is encoded as a string of 3 hexadecimal characters where the first character is limited to  values 0 to 3 (i.e. 10 bits) ")
    @JsonProperty(JSON_PROPERTY_AMF_REGION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAmfRegionId()
    {
        return amfRegionId;
    }

    @JsonProperty(JSON_PROPERTY_AMF_REGION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmfRegionId(String amfRegionId)
    {
        this.amfRegionId = amfRegionId;
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
        AmfCond amfCond = (AmfCond) o;
        return Objects.equals(this.amfSetId, amfCond.amfSetId) && Objects.equals(this.amfRegionId, amfCond.amfRegionId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(amfSetId, amfRegionId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmfCond {\n");
        sb.append("    amfSetId: ").append(toIndentedString(amfSetId)).append("\n");
        sb.append("    amfRegionId: ").append(toIndentedString(amfRegionId)).append("\n");
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
