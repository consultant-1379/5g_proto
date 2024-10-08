/*
 * NRF NFDiscovery Service
 * NRF NFDiscovery Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery;

import java.util.Objects;
import java.util.Arrays;
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
 * Contains information on an NF profile matching a discovery request
 */
@ApiModel(description = "Contains information on an NF profile matching a discovery request")
@JsonPropertyOrder({ NfInstanceInfo.JSON_PROPERTY_NRF_DISC_API_URI,
                     NfInstanceInfo.JSON_PROPERTY_PREFERRED_SEARCH,
                     NfInstanceInfo.JSON_PROPERTY_NRF_ALTERED_PRIORITIES })
public class NfInstanceInfo
{
    public static final String JSON_PROPERTY_NRF_DISC_API_URI = "nrfDiscApiUri";
    private String nrfDiscApiUri;

    public static final String JSON_PROPERTY_PREFERRED_SEARCH = "preferredSearch";
    private PreferredSearch preferredSearch;

    public static final String JSON_PROPERTY_NRF_ALTERED_PRIORITIES = "nrfAlteredPriorities";
    private Map<String, Integer> nrfAlteredPriorities = null;

    public NfInstanceInfo()
    {
    }

    public NfInstanceInfo nrfDiscApiUri(String nrfDiscApiUri)
    {

        this.nrfDiscApiUri = nrfDiscApiUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return nrfDiscApiUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_NRF_DISC_API_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNrfDiscApiUri()
    {
        return nrfDiscApiUri;
    }

    @JsonProperty(JSON_PROPERTY_NRF_DISC_API_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNrfDiscApiUri(String nrfDiscApiUri)
    {
        this.nrfDiscApiUri = nrfDiscApiUri;
    }

    public NfInstanceInfo preferredSearch(PreferredSearch preferredSearch)
    {

        this.preferredSearch = preferredSearch;
        return this;
    }

    /**
     * Get preferredSearch
     * 
     * @return preferredSearch
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PREFERRED_SEARCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PreferredSearch getPreferredSearch()
    {
        return preferredSearch;
    }

    @JsonProperty(JSON_PROPERTY_PREFERRED_SEARCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredSearch(PreferredSearch preferredSearch)
    {
        this.preferredSearch = preferredSearch;
    }

    public NfInstanceInfo nrfAlteredPriorities(Map<String, Integer> nrfAlteredPriorities)
    {

        this.nrfAlteredPriorities = nrfAlteredPriorities;
        return this;
    }

    public NfInstanceInfo putNrfAlteredPrioritiesItem(String key,
                                                      Integer nrfAlteredPrioritiesItem)
    {
        if (this.nrfAlteredPriorities == null)
        {
            this.nrfAlteredPriorities = new HashMap<>();
        }
        this.nrfAlteredPriorities.put(key, nrfAlteredPrioritiesItem);
        return this;
    }

    /**
     * The key of the map is the JSON Pointer of the priority IE in the NFProfile
     * data type that is altered by the NRF
     * 
     * @return nrfAlteredPriorities
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The key of the map is the JSON Pointer of the priority IE in the NFProfile data type that is altered by the NRF ")
    @JsonProperty(JSON_PROPERTY_NRF_ALTERED_PRIORITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, Integer> getNrfAlteredPriorities()
    {
        return nrfAlteredPriorities;
    }

    @JsonProperty(JSON_PROPERTY_NRF_ALTERED_PRIORITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNrfAlteredPriorities(Map<String, Integer> nrfAlteredPriorities)
    {
        this.nrfAlteredPriorities = nrfAlteredPriorities;
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
        NfInstanceInfo nfInstanceInfo = (NfInstanceInfo) o;
        return Objects.equals(this.nrfDiscApiUri, nfInstanceInfo.nrfDiscApiUri) && Objects.equals(this.preferredSearch, nfInstanceInfo.preferredSearch)
               && Objects.equals(this.nrfAlteredPriorities, nfInstanceInfo.nrfAlteredPriorities);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nrfDiscApiUri, preferredSearch, nrfAlteredPriorities);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NfInstanceInfo {\n");
        sb.append("    nrfDiscApiUri: ").append(toIndentedString(nrfDiscApiUri)).append("\n");
        sb.append("    preferredSearch: ").append(toIndentedString(preferredSearch)).append("\n");
        sb.append("    nrfAlteredPriorities: ").append(toIndentedString(nrfAlteredPriorities)).append("\n");
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
