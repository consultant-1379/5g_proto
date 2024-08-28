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
 * Data within a Cancel Location Request
 */
@ApiModel(description = "Data within a Cancel Location Request")
@JsonPropertyOrder({ CancelPosInfo.JSON_PROPERTY_SUPI,
                     CancelPosInfo.JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I,
                     CancelPosInfo.JSON_PROPERTY_LDR_REFERENCE,
                     CancelPosInfo.JSON_PROPERTY_SERVING_L_M_F_IDENTIFICATION,
                     CancelPosInfo.JSON_PROPERTY_SUPPORTED_FEATURES })
public class CancelPosInfo
{
    public static final String JSON_PROPERTY_SUPI = "supi";
    private String supi;

    public static final String JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I = "hgmlcCallBackURI";
    private String hgmlcCallBackURI;

    public static final String JSON_PROPERTY_LDR_REFERENCE = "ldrReference";
    private String ldrReference;

    public static final String JSON_PROPERTY_SERVING_L_M_F_IDENTIFICATION = "servingLMFIdentification";
    private String servingLMFIdentification;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public CancelPosInfo()
    {
    }

    public CancelPosInfo supi(String supi)
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
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String identifying a Supi that shall contain either an IMSI, a network specific identifier, a Global Cable Identifier (GCI) or a Global Line Identifier (GLI) as specified in clause  2.2A of 3GPP TS 23.003. It shall be formatted as follows  - for an IMSI \"imsi-<imsi>\", where <imsi> shall be formatted according to clause 2.2    of 3GPP TS 23.003 that describes an IMSI.  - for a network specific identifier \"nai-<nai>, where <nai> shall be formatted    according to clause 28.7.2 of 3GPP TS 23.003 that describes an NAI.  - for a GCI \"gci-<gci>\", where <gci> shall be formatted according to clause 28.15.2    of 3GPP TS 23.003.  - for a GLI \"gli-<gli>\", where <gli> shall be formatted according to clause 28.16.2 of    3GPP TS 23.003.To enable that the value is used as part of an URI, the string shall    only contain characters allowed according to the \"lower-with-hyphen\" naming convention    defined in 3GPP TS 29.501. ")
    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSupi()
    {
        return supi;
    }

    @JsonProperty(JSON_PROPERTY_SUPI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSupi(String supi)
    {
        this.supi = supi;
    }

    public CancelPosInfo hgmlcCallBackURI(String hgmlcCallBackURI)
    {

        this.hgmlcCallBackURI = hgmlcCallBackURI;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return hgmlcCallBackURI
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getHgmlcCallBackURI()
    {
        return hgmlcCallBackURI;
    }

    @JsonProperty(JSON_PROPERTY_HGMLC_CALL_BACK_U_R_I)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setHgmlcCallBackURI(String hgmlcCallBackURI)
    {
        this.hgmlcCallBackURI = hgmlcCallBackURI;
    }

    public CancelPosInfo ldrReference(String ldrReference)
    {

        this.ldrReference = ldrReference;
        return this;
    }

    /**
     * LDR Reference.
     * 
     * @return ldrReference
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "LDR Reference.")
    @JsonProperty(JSON_PROPERTY_LDR_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getLdrReference()
    {
        return ldrReference;
    }

    @JsonProperty(JSON_PROPERTY_LDR_REFERENCE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLdrReference(String ldrReference)
    {
        this.ldrReference = ldrReference;
    }

    public CancelPosInfo servingLMFIdentification(String servingLMFIdentification)
    {

        this.servingLMFIdentification = servingLMFIdentification;
        return this;
    }

    /**
     * LMF identification.
     * 
     * @return servingLMFIdentification
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "LMF identification.")
    @JsonProperty(JSON_PROPERTY_SERVING_L_M_F_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServingLMFIdentification()
    {
        return servingLMFIdentification;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_L_M_F_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServingLMFIdentification(String servingLMFIdentification)
    {
        this.servingLMFIdentification = servingLMFIdentification;
    }

    public CancelPosInfo supportedFeatures(String supportedFeatures)
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
        CancelPosInfo cancelPosInfo = (CancelPosInfo) o;
        return Objects.equals(this.supi, cancelPosInfo.supi) && Objects.equals(this.hgmlcCallBackURI, cancelPosInfo.hgmlcCallBackURI)
               && Objects.equals(this.ldrReference, cancelPosInfo.ldrReference)
               && Objects.equals(this.servingLMFIdentification, cancelPosInfo.servingLMFIdentification)
               && Objects.equals(this.supportedFeatures, cancelPosInfo.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supi, hgmlcCallBackURI, ldrReference, servingLMFIdentification, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CancelPosInfo {\n");
        sb.append("    supi: ").append(toIndentedString(supi)).append("\n");
        sb.append("    hgmlcCallBackURI: ").append(toIndentedString(hgmlcCallBackURI)).append("\n");
        sb.append("    ldrReference: ").append(toIndentedString(ldrReference)).append("\n");
        sb.append("    servingLMFIdentification: ").append(toIndentedString(servingLMFIdentification)).append("\n");
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
