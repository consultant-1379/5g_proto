/*
 * Nudm_PP
 * Nudm Parameter Provision Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.pp;

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
import java.util.ArrayList;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PpDataEntry
 */
@JsonPropertyOrder({ PpDataEntry.JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS,
                     PpDataEntry.JSON_PROPERTY_REFERENCE_ID,
                     PpDataEntry.JSON_PROPERTY_VALIDITY_TIME,
                     PpDataEntry.JSON_PROPERTY_MTC_PROVIDER_INFORMATION,
                     PpDataEntry.JSON_PROPERTY_SUPPORTED_FEATURES,
                     PpDataEntry.JSON_PROPERTY_ECS_ADDR_CONFIG_INFO,
                     PpDataEntry.JSON_PROPERTY_ADDITIONAL_ECS_ADDR_CONFIG_INFOS,
                     PpDataEntry.JSON_PROPERTY_EC_RESTRICTION })
public class PpDataEntry
{
    public static final String JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS = "communicationCharacteristics";
    private JsonNullable<CommunicationCharacteristicsAF> communicationCharacteristics = JsonNullable.<CommunicationCharacteristicsAF>undefined();

    public static final String JSON_PROPERTY_REFERENCE_ID = "referenceId";
    private Integer referenceId;

    public static final String JSON_PROPERTY_VALIDITY_TIME = "validityTime";
    private OffsetDateTime validityTime;

    public static final String JSON_PROPERTY_MTC_PROVIDER_INFORMATION = "mtcProviderInformation";
    private String mtcProviderInformation;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_ECS_ADDR_CONFIG_INFO = "ecsAddrConfigInfo";
    private JsonNullable<EcsAddrConfigInfo> ecsAddrConfigInfo = JsonNullable.<EcsAddrConfigInfo>undefined();

    public static final String JSON_PROPERTY_ADDITIONAL_ECS_ADDR_CONFIG_INFOS = "additionalEcsAddrConfigInfos";
    private List<EcsAddrConfigInfo> additionalEcsAddrConfigInfos = null;

    public static final String JSON_PROPERTY_EC_RESTRICTION = "ecRestriction";
    private JsonNullable<EcRestriction> ecRestriction = JsonNullable.<EcRestriction>undefined();

    public PpDataEntry()
    {
    }

    public PpDataEntry communicationCharacteristics(CommunicationCharacteristicsAF communicationCharacteristics)
    {
        this.communicationCharacteristics = JsonNullable.<CommunicationCharacteristicsAF>of(communicationCharacteristics);

        return this;
    }

    /**
     * Get communicationCharacteristics
     * 
     * @return communicationCharacteristics
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public CommunicationCharacteristicsAF getCommunicationCharacteristics()
    {
        return communicationCharacteristics.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<CommunicationCharacteristicsAF> getCommunicationCharacteristics_JsonNullable()
    {
        return communicationCharacteristics;
    }

    @JsonProperty(JSON_PROPERTY_COMMUNICATION_CHARACTERISTICS)
    public void setCommunicationCharacteristics_JsonNullable(JsonNullable<CommunicationCharacteristicsAF> communicationCharacteristics)
    {
        this.communicationCharacteristics = communicationCharacteristics;
    }

    public void setCommunicationCharacteristics(CommunicationCharacteristicsAF communicationCharacteristics)
    {
        this.communicationCharacteristics = JsonNullable.<CommunicationCharacteristicsAF>of(communicationCharacteristics);
    }

    public PpDataEntry referenceId(Integer referenceId)
    {

        this.referenceId = referenceId;
        return this;
    }

    /**
     * Get referenceId
     * 
     * @return referenceId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REFERENCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getReferenceId()
    {
        return referenceId;
    }

    @JsonProperty(JSON_PROPERTY_REFERENCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReferenceId(Integer referenceId)
    {
        this.referenceId = referenceId;
    }

    public PpDataEntry validityTime(OffsetDateTime validityTime)
    {

        this.validityTime = validityTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return validityTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getValidityTime()
    {
        return validityTime;
    }

    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidityTime(OffsetDateTime validityTime)
    {
        this.validityTime = validityTime;
    }

    public PpDataEntry mtcProviderInformation(String mtcProviderInformation)
    {

        this.mtcProviderInformation = mtcProviderInformation;
        return this;
    }

    /**
     * String uniquely identifying MTC provider information.
     * 
     * @return mtcProviderInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying MTC provider information.")
    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMtcProviderInformation()
    {
        return mtcProviderInformation;
    }

    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMtcProviderInformation(String mtcProviderInformation)
    {
        this.mtcProviderInformation = mtcProviderInformation;
    }

    public PpDataEntry supportedFeatures(String supportedFeatures)
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

    public PpDataEntry ecsAddrConfigInfo(EcsAddrConfigInfo ecsAddrConfigInfo)
    {
        this.ecsAddrConfigInfo = JsonNullable.<EcsAddrConfigInfo>of(ecsAddrConfigInfo);

        return this;
    }

    /**
     * Get ecsAddrConfigInfo
     * 
     * @return ecsAddrConfigInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public EcsAddrConfigInfo getEcsAddrConfigInfo()
    {
        return ecsAddrConfigInfo.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_ECS_ADDR_CONFIG_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<EcsAddrConfigInfo> getEcsAddrConfigInfo_JsonNullable()
    {
        return ecsAddrConfigInfo;
    }

    @JsonProperty(JSON_PROPERTY_ECS_ADDR_CONFIG_INFO)
    public void setEcsAddrConfigInfo_JsonNullable(JsonNullable<EcsAddrConfigInfo> ecsAddrConfigInfo)
    {
        this.ecsAddrConfigInfo = ecsAddrConfigInfo;
    }

    public void setEcsAddrConfigInfo(EcsAddrConfigInfo ecsAddrConfigInfo)
    {
        this.ecsAddrConfigInfo = JsonNullable.<EcsAddrConfigInfo>of(ecsAddrConfigInfo);
    }

    public PpDataEntry additionalEcsAddrConfigInfos(List<EcsAddrConfigInfo> additionalEcsAddrConfigInfos)
    {

        this.additionalEcsAddrConfigInfos = additionalEcsAddrConfigInfos;
        return this;
    }

    public PpDataEntry addAdditionalEcsAddrConfigInfosItem(EcsAddrConfigInfo additionalEcsAddrConfigInfosItem)
    {
        if (this.additionalEcsAddrConfigInfos == null)
        {
            this.additionalEcsAddrConfigInfos = new ArrayList<>();
        }
        this.additionalEcsAddrConfigInfos.add(additionalEcsAddrConfigInfosItem);
        return this;
    }

    /**
     * Get additionalEcsAddrConfigInfos
     * 
     * @return additionalEcsAddrConfigInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADDITIONAL_ECS_ADDR_CONFIG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<EcsAddrConfigInfo> getAdditionalEcsAddrConfigInfos()
    {
        return additionalEcsAddrConfigInfos;
    }

    @JsonProperty(JSON_PROPERTY_ADDITIONAL_ECS_ADDR_CONFIG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalEcsAddrConfigInfos(List<EcsAddrConfigInfo> additionalEcsAddrConfigInfos)
    {
        this.additionalEcsAddrConfigInfos = additionalEcsAddrConfigInfos;
    }

    public PpDataEntry ecRestriction(EcRestriction ecRestriction)
    {
        this.ecRestriction = JsonNullable.<EcRestriction>of(ecRestriction);

        return this;
    }

    /**
     * Get ecRestriction
     * 
     * @return ecRestriction
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public EcRestriction getEcRestriction()
    {
        return ecRestriction.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_EC_RESTRICTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<EcRestriction> getEcRestriction_JsonNullable()
    {
        return ecRestriction;
    }

    @JsonProperty(JSON_PROPERTY_EC_RESTRICTION)
    public void setEcRestriction_JsonNullable(JsonNullable<EcRestriction> ecRestriction)
    {
        this.ecRestriction = ecRestriction;
    }

    public void setEcRestriction(EcRestriction ecRestriction)
    {
        this.ecRestriction = JsonNullable.<EcRestriction>of(ecRestriction);
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
        PpDataEntry ppDataEntry = (PpDataEntry) o;
        return equalsNullable(this.communicationCharacteristics, ppDataEntry.communicationCharacteristics)
               && Objects.equals(this.referenceId, ppDataEntry.referenceId) && Objects.equals(this.validityTime, ppDataEntry.validityTime)
               && Objects.equals(this.mtcProviderInformation, ppDataEntry.mtcProviderInformation)
               && Objects.equals(this.supportedFeatures, ppDataEntry.supportedFeatures) && equalsNullable(this.ecsAddrConfigInfo, ppDataEntry.ecsAddrConfigInfo)
               && Objects.equals(this.additionalEcsAddrConfigInfos, ppDataEntry.additionalEcsAddrConfigInfos)
               && equalsNullable(this.ecRestriction, ppDataEntry.ecRestriction);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hashCodeNullable(communicationCharacteristics),
                            referenceId,
                            validityTime,
                            mtcProviderInformation,
                            supportedFeatures,
                            hashCodeNullable(ecsAddrConfigInfo),
                            additionalEcsAddrConfigInfos,
                            hashCodeNullable(ecRestriction));
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PpDataEntry {\n");
        sb.append("    communicationCharacteristics: ").append(toIndentedString(communicationCharacteristics)).append("\n");
        sb.append("    referenceId: ").append(toIndentedString(referenceId)).append("\n");
        sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
        sb.append("    mtcProviderInformation: ").append(toIndentedString(mtcProviderInformation)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    ecsAddrConfigInfo: ").append(toIndentedString(ecsAddrConfigInfo)).append("\n");
        sb.append("    additionalEcsAddrConfigInfos: ").append(toIndentedString(additionalEcsAddrConfigInfos)).append("\n");
        sb.append("    ecRestriction: ").append(toIndentedString(ecRestriction)).append("\n");
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
