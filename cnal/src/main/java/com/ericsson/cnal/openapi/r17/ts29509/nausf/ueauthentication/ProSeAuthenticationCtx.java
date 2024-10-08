/*
 * AUSF API
 * AUSF UE Authentication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.LinksValueSchema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the information related to the resource generated to handle the
 * ProSe authentication.
 */
@ApiModel(description = "Contains the information related to the resource generated to handle the ProSe authentication.")
@JsonPropertyOrder({ ProSeAuthenticationCtx.JSON_PROPERTY_AUTH_TYPE,
                     ProSeAuthenticationCtx.JSON_PROPERTY_LINKS,
                     ProSeAuthenticationCtx.JSON_PROPERTY_PRO_SE_AUTH_DATA,
                     ProSeAuthenticationCtx.JSON_PROPERTY_SUPPORTED_FEATURES })
public class ProSeAuthenticationCtx
{
    public static final String JSON_PROPERTY_AUTH_TYPE = "authType";
    private String authType;

    public static final String JSON_PROPERTY_LINKS = "_links";
    private Map<String, LinksValueSchema> links = new HashMap<>();

    public static final String JSON_PROPERTY_PRO_SE_AUTH_DATA = "proSeAuthData";
    private Object proSeAuthData;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public ProSeAuthenticationCtx()
    {
    }

    public ProSeAuthenticationCtx authType(String authType)
    {

        this.authType = authType;
        return this;
    }

    /**
     * Indicates the authentication method used.
     * 
     * @return authType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the authentication method used.")
    @JsonProperty(JSON_PROPERTY_AUTH_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAuthType()
    {
        return authType;
    }

    @JsonProperty(JSON_PROPERTY_AUTH_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAuthType(String authType)
    {
        this.authType = authType;
    }

    public ProSeAuthenticationCtx links(Map<String, LinksValueSchema> links)
    {

        this.links = links;
        return this;
    }

    public ProSeAuthenticationCtx putLinksItem(String key,
                                               LinksValueSchema linksItem)
    {
        this.links.put(key, linksItem);
        return this;
    }

    /**
     * A map(list of key-value pairs) where member serves as key
     * 
     * @return links
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "A map(list of key-value pairs) where member serves as key")
    @JsonProperty(JSON_PROPERTY_LINKS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, LinksValueSchema> getLinks()
    {
        return links;
    }

    @JsonProperty(JSON_PROPERTY_LINKS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLinks(Map<String, LinksValueSchema> links)
    {
        this.links = links;
    }

    public ProSeAuthenticationCtx proSeAuthData(Object proSeAuthData)
    {

        this.proSeAuthData = proSeAuthData;
        return this;
    }

    /**
     * Get proSeAuthData
     * 
     * @return proSeAuthData
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PRO_SE_AUTH_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Object getProSeAuthData()
    {
        return proSeAuthData;
    }

    @JsonProperty(JSON_PROPERTY_PRO_SE_AUTH_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setProSeAuthData(Object proSeAuthData)
    {
        this.proSeAuthData = proSeAuthData;
    }

    public ProSeAuthenticationCtx supportedFeatures(String supportedFeatures)
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
        ProSeAuthenticationCtx proSeAuthenticationCtx = (ProSeAuthenticationCtx) o;
        return Objects.equals(this.authType, proSeAuthenticationCtx.authType) && Objects.equals(this.links, proSeAuthenticationCtx.links)
               && Objects.equals(this.proSeAuthData, proSeAuthenticationCtx.proSeAuthData)
               && Objects.equals(this.supportedFeatures, proSeAuthenticationCtx.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(authType, links, proSeAuthData, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProSeAuthenticationCtx {\n");
        sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    proSeAuthData: ").append(toIndentedString(proSeAuthData)).append("\n");
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
