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
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This data type is defined in the same way as the &#39;EutraLocation&#39; data
 * type, but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the 'EutraLocation' data type, but with the OpenAPI 'nullable: true' property.  ")
@JsonPropertyOrder({ EutraLocationRm.JSON_PROPERTY_TAI,
                     EutraLocationRm.JSON_PROPERTY_IGNORE_TAI,
                     EutraLocationRm.JSON_PROPERTY_ECGI,
                     EutraLocationRm.JSON_PROPERTY_IGNORE_ECGI,
                     EutraLocationRm.JSON_PROPERTY_AGE_OF_LOCATION_INFORMATION,
                     EutraLocationRm.JSON_PROPERTY_UE_LOCATION_TIMESTAMP,
                     EutraLocationRm.JSON_PROPERTY_GEOGRAPHICAL_INFORMATION,
                     EutraLocationRm.JSON_PROPERTY_GEODETIC_INFORMATION,
                     EutraLocationRm.JSON_PROPERTY_GLOBAL_NGENB_ID,
                     EutraLocationRm.JSON_PROPERTY_GLOBAL_E_NB_ID })
public class EutraLocationRm
{
    public static final String JSON_PROPERTY_TAI = "tai";
    private Tai tai;

    public static final String JSON_PROPERTY_IGNORE_TAI = "ignoreTai";
    private Boolean ignoreTai = false;

    public static final String JSON_PROPERTY_ECGI = "ecgi";
    private Ecgi ecgi;

    public static final String JSON_PROPERTY_IGNORE_ECGI = "ignoreEcgi";
    private Boolean ignoreEcgi = false;

    public static final String JSON_PROPERTY_AGE_OF_LOCATION_INFORMATION = "ageOfLocationInformation";
    private Integer ageOfLocationInformation;

    public static final String JSON_PROPERTY_UE_LOCATION_TIMESTAMP = "ueLocationTimestamp";
    private OffsetDateTime ueLocationTimestamp;

    public static final String JSON_PROPERTY_GEOGRAPHICAL_INFORMATION = "geographicalInformation";
    private String geographicalInformation;

    public static final String JSON_PROPERTY_GEODETIC_INFORMATION = "geodeticInformation";
    private String geodeticInformation;

    public static final String JSON_PROPERTY_GLOBAL_NGENB_ID = "globalNgenbId";
    private GlobalRanNodeId globalNgenbId;

    public static final String JSON_PROPERTY_GLOBAL_E_NB_ID = "globalENbId";
    private GlobalRanNodeId globalENbId;

    public EutraLocationRm()
    {
    }

    public EutraLocationRm tai(Tai tai)
    {

        this.tai = tai;
        return this;
    }

    /**
     * Get tai
     * 
     * @return tai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Tai getTai()
    {
        return tai;
    }

    @JsonProperty(JSON_PROPERTY_TAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTai(Tai tai)
    {
        this.tai = tai;
    }

    public EutraLocationRm ignoreTai(Boolean ignoreTai)
    {

        this.ignoreTai = ignoreTai;
        return this;
    }

    /**
     * Get ignoreTai
     * 
     * @return ignoreTai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IGNORE_TAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIgnoreTai()
    {
        return ignoreTai;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_TAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreTai(Boolean ignoreTai)
    {
        this.ignoreTai = ignoreTai;
    }

    public EutraLocationRm ecgi(Ecgi ecgi)
    {

        this.ecgi = ecgi;
        return this;
    }

    /**
     * Get ecgi
     * 
     * @return ecgi
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_ECGI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Ecgi getEcgi()
    {
        return ecgi;
    }

    @JsonProperty(JSON_PROPERTY_ECGI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEcgi(Ecgi ecgi)
    {
        this.ecgi = ecgi;
    }

    public EutraLocationRm ignoreEcgi(Boolean ignoreEcgi)
    {

        this.ignoreEcgi = ignoreEcgi;
        return this;
    }

    /**
     * This flag when present shall indicate that the Ecgi shall be ignored When
     * present, it shall be set as follows: - true: ecgi shall be ignored. - false
     * (default): ecgi shall not be ignored.
     * 
     * @return ignoreEcgi
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This flag when present shall indicate that the Ecgi shall be ignored When present, it shall be set as follows: - true: ecgi shall be ignored. - false (default): ecgi shall not be ignored. ")
    @JsonProperty(JSON_PROPERTY_IGNORE_ECGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIgnoreEcgi()
    {
        return ignoreEcgi;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_ECGI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreEcgi(Boolean ignoreEcgi)
    {
        this.ignoreEcgi = ignoreEcgi;
    }

    public EutraLocationRm ageOfLocationInformation(Integer ageOfLocationInformation)
    {

        this.ageOfLocationInformation = ageOfLocationInformation;
        return this;
    }

    /**
     * The value represents the elapsed time in minutes since the last network
     * contact of the mobile station. Value \&quot;0\&quot; indicates that the
     * location information was obtained after a successful paging procedure for
     * Active Location Retrieval when the UE is in idle mode or after a successful
     * NG-RAN location reporting procedure with the eNB when the UE is in connected
     * mode. Any other value than \&quot;0\&quot; indicates that the location
     * information is the last known one. See 3GPP TS 29.002 clause 17.7.8. minimum:
     * 0 maximum: 32767
     * 
     * @return ageOfLocationInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The value represents the elapsed time in minutes since the last network contact of the mobile station.  Value \"0\" indicates that the location information was obtained after a successful paging procedure for Active Location Retrieval when the UE is in idle mode or after a successful NG-RAN location reporting procedure with the eNB when the UE is in connected mode.  Any other value than \"0\" indicates that the location information is the last known one.  See 3GPP TS 29.002 clause 17.7.8. ")
    @JsonProperty(JSON_PROPERTY_AGE_OF_LOCATION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAgeOfLocationInformation()
    {
        return ageOfLocationInformation;
    }

    @JsonProperty(JSON_PROPERTY_AGE_OF_LOCATION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAgeOfLocationInformation(Integer ageOfLocationInformation)
    {
        this.ageOfLocationInformation = ageOfLocationInformation;
    }

    public EutraLocationRm ueLocationTimestamp(OffsetDateTime ueLocationTimestamp)
    {

        this.ueLocationTimestamp = ueLocationTimestamp;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return ueLocationTimestamp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_UE_LOCATION_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getUeLocationTimestamp()
    {
        return ueLocationTimestamp;
    }

    @JsonProperty(JSON_PROPERTY_UE_LOCATION_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeLocationTimestamp(OffsetDateTime ueLocationTimestamp)
    {
        this.ueLocationTimestamp = ueLocationTimestamp;
    }

    public EutraLocationRm geographicalInformation(String geographicalInformation)
    {

        this.geographicalInformation = geographicalInformation;
        return this;
    }

    /**
     * Refer to geographical Information. See 3GPP TS 23.032 clause 7.3.2. Only the
     * description of an ellipsoid point with uncertainty circle is allowed to be
     * used.
     * 
     * @return geographicalInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Refer to geographical Information. See 3GPP TS 23.032 clause 7.3.2. Only the description of an ellipsoid point with uncertainty circle is allowed to be used. ")
    @JsonProperty(JSON_PROPERTY_GEOGRAPHICAL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGeographicalInformation()
    {
        return geographicalInformation;
    }

    @JsonProperty(JSON_PROPERTY_GEOGRAPHICAL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGeographicalInformation(String geographicalInformation)
    {
        this.geographicalInformation = geographicalInformation;
    }

    public EutraLocationRm geodeticInformation(String geodeticInformation)
    {

        this.geodeticInformation = geodeticInformation;
        return this;
    }

    /**
     * Refers to Calling Geodetic Location. See ITU-T Recommendation Q.763 (1999)
     * [24] clause 3.88.2. Only the description of an ellipsoid point with
     * uncertainty circle is allowed to be used.
     * 
     * @return geodeticInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Refers to Calling Geodetic Location. See ITU-T Recommendation Q.763 (1999) [24] clause 3.88.2. Only the description of an ellipsoid point with uncertainty circle is allowed to be used. ")
    @JsonProperty(JSON_PROPERTY_GEODETIC_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGeodeticInformation()
    {
        return geodeticInformation;
    }

    @JsonProperty(JSON_PROPERTY_GEODETIC_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGeodeticInformation(String geodeticInformation)
    {
        this.geodeticInformation = geodeticInformation;
    }

    public EutraLocationRm globalNgenbId(GlobalRanNodeId globalNgenbId)
    {

        this.globalNgenbId = globalNgenbId;
        return this;
    }

    /**
     * Get globalNgenbId
     * 
     * @return globalNgenbId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GLOBAL_NGENB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GlobalRanNodeId getGlobalNgenbId()
    {
        return globalNgenbId;
    }

    @JsonProperty(JSON_PROPERTY_GLOBAL_NGENB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobalNgenbId(GlobalRanNodeId globalNgenbId)
    {
        this.globalNgenbId = globalNgenbId;
    }

    public EutraLocationRm globalENbId(GlobalRanNodeId globalENbId)
    {

        this.globalENbId = globalENbId;
        return this;
    }

    /**
     * Get globalENbId
     * 
     * @return globalENbId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GLOBAL_E_NB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GlobalRanNodeId getGlobalENbId()
    {
        return globalENbId;
    }

    @JsonProperty(JSON_PROPERTY_GLOBAL_E_NB_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobalENbId(GlobalRanNodeId globalENbId)
    {
        this.globalENbId = globalENbId;
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
        EutraLocationRm eutraLocationRm = (EutraLocationRm) o;
        return Objects.equals(this.tai, eutraLocationRm.tai) && Objects.equals(this.ignoreTai, eutraLocationRm.ignoreTai)
               && Objects.equals(this.ecgi, eutraLocationRm.ecgi) && Objects.equals(this.ignoreEcgi, eutraLocationRm.ignoreEcgi)
               && Objects.equals(this.ageOfLocationInformation, eutraLocationRm.ageOfLocationInformation)
               && Objects.equals(this.ueLocationTimestamp, eutraLocationRm.ueLocationTimestamp)
               && Objects.equals(this.geographicalInformation, eutraLocationRm.geographicalInformation)
               && Objects.equals(this.geodeticInformation, eutraLocationRm.geodeticInformation)
               && Objects.equals(this.globalNgenbId, eutraLocationRm.globalNgenbId) && Objects.equals(this.globalENbId, eutraLocationRm.globalENbId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(tai,
                            ignoreTai,
                            ecgi,
                            ignoreEcgi,
                            ageOfLocationInformation,
                            ueLocationTimestamp,
                            geographicalInformation,
                            geodeticInformation,
                            globalNgenbId,
                            globalENbId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EutraLocationRm {\n");
        sb.append("    tai: ").append(toIndentedString(tai)).append("\n");
        sb.append("    ignoreTai: ").append(toIndentedString(ignoreTai)).append("\n");
        sb.append("    ecgi: ").append(toIndentedString(ecgi)).append("\n");
        sb.append("    ignoreEcgi: ").append(toIndentedString(ignoreEcgi)).append("\n");
        sb.append("    ageOfLocationInformation: ").append(toIndentedString(ageOfLocationInformation)).append("\n");
        sb.append("    ueLocationTimestamp: ").append(toIndentedString(ueLocationTimestamp)).append("\n");
        sb.append("    geographicalInformation: ").append(toIndentedString(geographicalInformation)).append("\n");
        sb.append("    geodeticInformation: ").append(toIndentedString(geodeticInformation)).append("\n");
        sb.append("    globalNgenbId: ").append(toIndentedString(globalNgenbId)).append("\n");
        sb.append("    globalENbId: ").append(toIndentedString(globalENbId)).append("\n");
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
