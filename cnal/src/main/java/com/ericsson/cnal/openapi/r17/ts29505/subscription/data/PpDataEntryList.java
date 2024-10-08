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
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.PpDataEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains a list of the Provisioned Parameters entries
 */
@ApiModel(description = "Contains a list of the Provisioned Parameters entries")
@JsonPropertyOrder({ PpDataEntryList.JSON_PROPERTY_PP_DATA_ENTRY_LIST, PpDataEntryList.JSON_PROPERTY_SUPPORTED_FEATURES })
public class PpDataEntryList
{
    public static final String JSON_PROPERTY_PP_DATA_ENTRY_LIST = "ppDataEntryList";
    private List<PpDataEntry> ppDataEntryList = null;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public PpDataEntryList()
    {
    }

    public PpDataEntryList ppDataEntryList(List<PpDataEntry> ppDataEntryList)
    {

        this.ppDataEntryList = ppDataEntryList;
        return this;
    }

    public PpDataEntryList addPpDataEntryListItem(PpDataEntry ppDataEntryListItem)
    {
        if (this.ppDataEntryList == null)
        {
            this.ppDataEntryList = new ArrayList<>();
        }
        this.ppDataEntryList.add(ppDataEntryListItem);
        return this;
    }

    /**
     * Get ppDataEntryList
     * 
     * @return ppDataEntryList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PP_DATA_ENTRY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PpDataEntry> getPpDataEntryList()
    {
        return ppDataEntryList;
    }

    @JsonProperty(JSON_PROPERTY_PP_DATA_ENTRY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPpDataEntryList(List<PpDataEntry> ppDataEntryList)
    {
        this.ppDataEntryList = ppDataEntryList;
    }

    public PpDataEntryList supportedFeatures(String supportedFeatures)
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
        PpDataEntryList ppDataEntryList = (PpDataEntryList) o;
        return Objects.equals(this.ppDataEntryList, ppDataEntryList.ppDataEntryList)
               && Objects.equals(this.supportedFeatures, ppDataEntryList.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ppDataEntryList, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PpDataEntryList {\n");
        sb.append("    ppDataEntryList: ").append(toIndentedString(ppDataEntryList)).append("\n");
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
