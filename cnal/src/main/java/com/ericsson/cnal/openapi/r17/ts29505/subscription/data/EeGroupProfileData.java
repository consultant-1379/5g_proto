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

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * EeGroupProfileData
 */
@JsonPropertyOrder({ EeGroupProfileData.JSON_PROPERTY_RESTRICTED_EVENT_TYPES,
                     EeGroupProfileData.JSON_PROPERTY_ALLOWED_MTC_PROVIDER,
                     EeGroupProfileData.JSON_PROPERTY_SUPPORTED_FEATURES,
                     EeGroupProfileData.JSON_PROPERTY_IWK_EPC_RESTRICTED,
                     EeGroupProfileData.JSON_PROPERTY_EXT_GROUP_ID,
                     EeGroupProfileData.JSON_PROPERTY_HSS_GROUP_ID })
public class EeGroupProfileData
{
    public static final String JSON_PROPERTY_RESTRICTED_EVENT_TYPES = "restrictedEventTypes";
    private List<String> restrictedEventTypes = null;

    public static final String JSON_PROPERTY_ALLOWED_MTC_PROVIDER = "allowedMtcProvider";
    private Map<String, List<MtcProvider>> allowedMtcProvider = null;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_IWK_EPC_RESTRICTED = "iwkEpcRestricted";
    private Boolean iwkEpcRestricted = false;

    public static final String JSON_PROPERTY_EXT_GROUP_ID = "extGroupId";
    private String extGroupId;

    public static final String JSON_PROPERTY_HSS_GROUP_ID = "hssGroupId";
    private String hssGroupId;

    public EeGroupProfileData()
    {
    }

    public EeGroupProfileData restrictedEventTypes(List<String> restrictedEventTypes)
    {

        this.restrictedEventTypes = restrictedEventTypes;
        return this;
    }

    public EeGroupProfileData addRestrictedEventTypesItem(String restrictedEventTypesItem)
    {
        if (this.restrictedEventTypes == null)
        {
            this.restrictedEventTypes = new ArrayList<>();
        }
        this.restrictedEventTypes.add(restrictedEventTypesItem);
        return this;
    }

    /**
     * Get restrictedEventTypes
     * 
     * @return restrictedEventTypes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESTRICTED_EVENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getRestrictedEventTypes()
    {
        return restrictedEventTypes;
    }

    @JsonProperty(JSON_PROPERTY_RESTRICTED_EVENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRestrictedEventTypes(List<String> restrictedEventTypes)
    {
        this.restrictedEventTypes = restrictedEventTypes;
    }

    public EeGroupProfileData allowedMtcProvider(Map<String, List<MtcProvider>> allowedMtcProvider)
    {

        this.allowedMtcProvider = allowedMtcProvider;
        return this;
    }

    public EeGroupProfileData putAllowedMtcProviderItem(String key,
                                                        List<MtcProvider> allowedMtcProviderItem)
    {
        if (this.allowedMtcProvider == null)
        {
            this.allowedMtcProvider = new HashMap<>();
        }
        this.allowedMtcProvider.put(key, allowedMtcProviderItem);
        return this;
    }

    /**
     * A map (list of key-value pairs where EventType serves as key) of MTC provider
     * lists. In addition to defined EventTypes, the key value \&quot;ALL\&quot; may
     * be used to identify a map entry which contains a list of MtcProviders that
     * are allowed monitoring all Event Types.
     * 
     * @return allowedMtcProvider
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs where EventType serves as key) of MTC provider lists. In addition to defined EventTypes, the key value \"ALL\" may be used to identify a map entry which contains a list of MtcProviders that are allowed monitoring all Event Types.")
    @JsonProperty(JSON_PROPERTY_ALLOWED_MTC_PROVIDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, List<MtcProvider>> getAllowedMtcProvider()
    {
        return allowedMtcProvider;
    }

    @JsonProperty(JSON_PROPERTY_ALLOWED_MTC_PROVIDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowedMtcProvider(Map<String, List<MtcProvider>> allowedMtcProvider)
    {
        this.allowedMtcProvider = allowedMtcProvider;
    }

    public EeGroupProfileData supportedFeatures(String supportedFeatures)
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

    public EeGroupProfileData iwkEpcRestricted(Boolean iwkEpcRestricted)
    {

        this.iwkEpcRestricted = iwkEpcRestricted;
        return this;
    }

    /**
     * Get iwkEpcRestricted
     * 
     * @return iwkEpcRestricted
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IWK_EPC_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIwkEpcRestricted()
    {
        return iwkEpcRestricted;
    }

    @JsonProperty(JSON_PROPERTY_IWK_EPC_RESTRICTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIwkEpcRestricted(Boolean iwkEpcRestricted)
    {
        this.iwkEpcRestricted = iwkEpcRestricted;
    }

    public EeGroupProfileData extGroupId(String extGroupId)
    {

        this.extGroupId = extGroupId;
        return this;
    }

    /**
     * Get extGroupId
     * 
     * @return extGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExtGroupId()
    {
        return extGroupId;
    }

    @JsonProperty(JSON_PROPERTY_EXT_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtGroupId(String extGroupId)
    {
        this.extGroupId = extGroupId;
    }

    public EeGroupProfileData hssGroupId(String hssGroupId)
    {

        this.hssGroupId = hssGroupId;
        return this;
    }

    /**
     * Identifier of a group of NFs.
     * 
     * @return hssGroupId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifier of a group of NFs.")
    @JsonProperty(JSON_PROPERTY_HSS_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHssGroupId()
    {
        return hssGroupId;
    }

    @JsonProperty(JSON_PROPERTY_HSS_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHssGroupId(String hssGroupId)
    {
        this.hssGroupId = hssGroupId;
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
        EeGroupProfileData eeGroupProfileData = (EeGroupProfileData) o;
        return Objects.equals(this.restrictedEventTypes, eeGroupProfileData.restrictedEventTypes)
               && Objects.equals(this.allowedMtcProvider, eeGroupProfileData.allowedMtcProvider)
               && Objects.equals(this.supportedFeatures, eeGroupProfileData.supportedFeatures)
               && Objects.equals(this.iwkEpcRestricted, eeGroupProfileData.iwkEpcRestricted) && Objects.equals(this.extGroupId, eeGroupProfileData.extGroupId)
               && Objects.equals(this.hssGroupId, eeGroupProfileData.hssGroupId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(restrictedEventTypes, allowedMtcProvider, supportedFeatures, iwkEpcRestricted, extGroupId, hssGroupId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EeGroupProfileData {\n");
        sb.append("    restrictedEventTypes: ").append(toIndentedString(restrictedEventTypes)).append("\n");
        sb.append("    allowedMtcProvider: ").append(toIndentedString(allowedMtcProvider)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    iwkEpcRestricted: ").append(toIndentedString(iwkEpcRestricted)).append("\n");
        sb.append("    extGroupId: ").append(toIndentedString(extGroupId)).append("\n");
        sb.append("    hssGroupId: ").append(toIndentedString(hssGroupId)).append("\n");
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
