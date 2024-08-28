/*
 * Unified Data Repository Service API file for subscription data
 * Unified Data Repository Service (subscription data).   The API version is defined in 3GPP TS 29.504.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29505.subscription.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.RegistrationLocationInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * LocationInfo
 */
@JsonPropertyOrder({ LocationInfo.JSON_PROPERTY_SUPI,
                     LocationInfo.JSON_PROPERTY_GPSI,
                     LocationInfo.JSON_PROPERTY_REGISTRATION_LOCATION_INFO_LIST,
                     LocationInfo.JSON_PROPERTY_SUPPORTED_FEATURES })
public class LocationInfo
{
    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_GPSI = "gpsi";
    private String gpsi;

    public static final String JSON_PROPERTY_REGISTRATION_LOCATION_INFO_LIST = "registrationLocationInfoList";
    private List<RegistrationLocationInfo> registrationLocationInfoList = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public LocationInfo()
    {
    }

    public LocationInfo supi(String supi)
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

    public LocationInfo gpsi(String gpsi)
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

    public LocationInfo registrationLocationInfoList(List<RegistrationLocationInfo> registrationLocationInfoList)
    {

        this.registrationLocationInfoList = registrationLocationInfoList;
        return this;
    }

    public LocationInfo addRegistrationLocationInfoListItem(RegistrationLocationInfo registrationLocationInfoListItem)
    {
        this.registrationLocationInfoList.add(registrationLocationInfoListItem);
        return this;
    }

    /**
     * Get registrationLocationInfoList
     * 
     * @return registrationLocationInfoList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REGISTRATION_LOCATION_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<RegistrationLocationInfo> getRegistrationLocationInfoList()
    {
        return registrationLocationInfoList;
    }

    @JsonProperty(JSON_PROPERTY_REGISTRATION_LOCATION_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRegistrationLocationInfoList(List<RegistrationLocationInfo> registrationLocationInfoList)
    {
        this.registrationLocationInfoList = registrationLocationInfoList;
    }

    public LocationInfo supportedFeatures(String supportedFeatures)
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
        LocationInfo locationInfo = (LocationInfo) o;
        return Objects.equals(this.supi, locationInfo.supi) && Objects.equals(this.gpsi, locationInfo.gpsi)
               && Objects.equals(this.registrationLocationInfoList, locationInfo.registrationLocationInfoList)
               && Objects.equals(this.supportedFeatures, locationInfo.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supi, gpsi, registrationLocationInfoList, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class LocationInfo {\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    gpsi: ").append(toIndentedString(gpsi)).append("\n");
        sb.append("    registrationLocationInfoList: ").append(toIndentedString(registrationLocationInfoList)).append("\n");
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
