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
 * Data within Provide Location Information Request
 */
@ApiModel(description = "Data within Provide Location Information Request")
@JsonPropertyOrder({ RequestLocInfo.JSON_PROPERTY_REQ5GS_LOC,
                     RequestLocInfo.JSON_PROPERTY_REQ_CURRENT_LOC,
                     RequestLocInfo.JSON_PROPERTY_REQ_RAT_TYPE,
                     RequestLocInfo.JSON_PROPERTY_REQ_TIME_ZONE,
                     RequestLocInfo.JSON_PROPERTY_SUPPORTED_FEATURES })
public class RequestLocInfo
{
    public static final String JSON_PROPERTY_REQ5GS_LOC = "req5gsLoc";
    private Boolean req5gsLoc = false;

    public static final String JSON_PROPERTY_REQ_CURRENT_LOC = "reqCurrentLoc";
    private Boolean reqCurrentLoc = false;

    public static final String JSON_PROPERTY_REQ_RAT_TYPE = "reqRatType";
    private Boolean reqRatType = false;

    public static final String JSON_PROPERTY_REQ_TIME_ZONE = "reqTimeZone";
    private Boolean reqTimeZone = false;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public RequestLocInfo()
    {
    }

    public RequestLocInfo req5gsLoc(Boolean req5gsLoc)
    {

        this.req5gsLoc = req5gsLoc;
        return this;
    }

    /**
     * Get req5gsLoc
     * 
     * @return req5gsLoc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQ5GS_LOC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReq5gsLoc()
    {
        return req5gsLoc;
    }

    @JsonProperty(JSON_PROPERTY_REQ5GS_LOC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReq5gsLoc(Boolean req5gsLoc)
    {
        this.req5gsLoc = req5gsLoc;
    }

    public RequestLocInfo reqCurrentLoc(Boolean reqCurrentLoc)
    {

        this.reqCurrentLoc = reqCurrentLoc;
        return this;
    }

    /**
     * Get reqCurrentLoc
     * 
     * @return reqCurrentLoc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQ_CURRENT_LOC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReqCurrentLoc()
    {
        return reqCurrentLoc;
    }

    @JsonProperty(JSON_PROPERTY_REQ_CURRENT_LOC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReqCurrentLoc(Boolean reqCurrentLoc)
    {
        this.reqCurrentLoc = reqCurrentLoc;
    }

    public RequestLocInfo reqRatType(Boolean reqRatType)
    {

        this.reqRatType = reqRatType;
        return this;
    }

    /**
     * Get reqRatType
     * 
     * @return reqRatType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQ_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReqRatType()
    {
        return reqRatType;
    }

    @JsonProperty(JSON_PROPERTY_REQ_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReqRatType(Boolean reqRatType)
    {
        this.reqRatType = reqRatType;
    }

    public RequestLocInfo reqTimeZone(Boolean reqTimeZone)
    {

        this.reqTimeZone = reqTimeZone;
        return this;
    }

    /**
     * Get reqTimeZone
     * 
     * @return reqTimeZone
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REQ_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReqTimeZone()
    {
        return reqTimeZone;
    }

    @JsonProperty(JSON_PROPERTY_REQ_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReqTimeZone(Boolean reqTimeZone)
    {
        this.reqTimeZone = reqTimeZone;
    }

    public RequestLocInfo supportedFeatures(String supportedFeatures)
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
        RequestLocInfo requestLocInfo = (RequestLocInfo) o;
        return Objects.equals(this.req5gsLoc, requestLocInfo.req5gsLoc) && Objects.equals(this.reqCurrentLoc, requestLocInfo.reqCurrentLoc)
               && Objects.equals(this.reqRatType, requestLocInfo.reqRatType) && Objects.equals(this.reqTimeZone, requestLocInfo.reqTimeZone)
               && Objects.equals(this.supportedFeatures, requestLocInfo.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(req5gsLoc, reqCurrentLoc, reqRatType, reqTimeZone, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RequestLocInfo {\n");
        sb.append("    req5gsLoc: ").append(toIndentedString(req5gsLoc)).append("\n");
        sb.append("    reqCurrentLoc: ").append(toIndentedString(reqCurrentLoc)).append("\n");
        sb.append("    reqRatType: ").append(toIndentedString(reqRatType)).append("\n");
        sb.append("    reqTimeZone: ").append(toIndentedString(reqTimeZone)).append("\n");
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
