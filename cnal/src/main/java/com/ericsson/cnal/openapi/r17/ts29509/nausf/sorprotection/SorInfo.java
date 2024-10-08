/*
 * Nausf_SoRProtection Service
 * AUSF SoR Protection Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29509.nausf.sorprotection;

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
 * Contains the Steering Information.
 */
@ApiModel(description = "Contains the Steering Information.")
@JsonPropertyOrder({ SorInfo.JSON_PROPERTY_STEERING_CONTAINER,
                     SorInfo.JSON_PROPERTY_ACK_IND,
                     SorInfo.JSON_PROPERTY_SOR_HEADER,
                     SorInfo.JSON_PROPERTY_SOR_TRANSPARENT_INFO,
                     SorInfo.JSON_PROPERTY_SUPPORTED_FEATURES })
public class SorInfo
{
    public static final String JSON_PROPERTY_STEERING_CONTAINER = "steeringContainer";
    private Object steeringContainer;

    public static final String JSON_PROPERTY_ACK_IND = "ackInd";
    private Boolean ackInd;

    public static final String JSON_PROPERTY_SOR_HEADER = "sorHeader";
    private byte[] sorHeader;

    public static final String JSON_PROPERTY_SOR_TRANSPARENT_INFO = "sorTransparentInfo";
    private byte[] sorTransparentInfo;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public SorInfo()
    {
    }

    public SorInfo steeringContainer(Object steeringContainer)
    {

        this.steeringContainer = steeringContainer;
        return this;
    }

    /**
     * Get steeringContainer
     * 
     * @return steeringContainer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_STEERING_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Object getSteeringContainer()
    {
        return steeringContainer;
    }

    @JsonProperty(JSON_PROPERTY_STEERING_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSteeringContainer(Object steeringContainer)
    {
        this.steeringContainer = steeringContainer;
    }

    public SorInfo ackInd(Boolean ackInd)
    {

        this.ackInd = ackInd;
        return this;
    }

    /**
     * Contains indication whether the acknowledgement from UE is needed.
     * 
     * @return ackInd
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Contains indication whether the acknowledgement from UE is needed.")
    @JsonProperty(JSON_PROPERTY_ACK_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getAckInd()
    {
        return ackInd;
    }

    @JsonProperty(JSON_PROPERTY_ACK_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAckInd(Boolean ackInd)
    {
        this.ackInd = ackInd;
    }

    public SorInfo sorHeader(byte[] sorHeader)
    {

        this.sorHeader = sorHeader;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return sorHeader
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_SOR_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getSorHeader()
    {
        return sorHeader;
    }

    @JsonProperty(JSON_PROPERTY_SOR_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSorHeader(byte[] sorHeader)
    {
        this.sorHeader = sorHeader;
    }

    public SorInfo sorTransparentInfo(byte[] sorTransparentInfo)
    {

        this.sorTransparentInfo = sorTransparentInfo;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return sorTransparentInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_SOR_TRANSPARENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getSorTransparentInfo()
    {
        return sorTransparentInfo;
    }

    @JsonProperty(JSON_PROPERTY_SOR_TRANSPARENT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSorTransparentInfo(byte[] sorTransparentInfo)
    {
        this.sorTransparentInfo = sorTransparentInfo;
    }

    public SorInfo supportedFeatures(String supportedFeatures)
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
        SorInfo sorInfo = (SorInfo) o;
        return Objects.equals(this.steeringContainer, sorInfo.steeringContainer) && Objects.equals(this.ackInd, sorInfo.ackInd)
               && Arrays.equals(this.sorHeader, sorInfo.sorHeader) && Arrays.equals(this.sorTransparentInfo, sorInfo.sorTransparentInfo)
               && Objects.equals(this.supportedFeatures, sorInfo.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(steeringContainer, ackInd, Arrays.hashCode(sorHeader), Arrays.hashCode(sorTransparentInfo), supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SorInfo {\n");
        sb.append("    steeringContainer: ").append(toIndentedString(steeringContainer)).append("\n");
        sb.append("    ackInd: ").append(toIndentedString(ackInd)).append("\n");
        sb.append("    sorHeader: ").append(toIndentedString(sorHeader)).append("\n");
        sb.append("    sorTransparentInfo: ").append(toIndentedString(sorTransparentInfo)).append("\n");
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
