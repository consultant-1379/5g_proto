/*
 * Unified Data Repository Service API file for policy data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.policy.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
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
 * Contains the SM policy data for a given subscriber and S-NSSAI.
 */
@ApiModel(description = "Contains the SM policy data for a given subscriber and S-NSSAI.")
@JsonPropertyOrder({ SmPolicySnssaiDataPatch.JSON_PROPERTY_SNSSAI, SmPolicySnssaiDataPatch.JSON_PROPERTY_SM_POLICY_DNN_DATA })
public class SmPolicySnssaiDataPatch
{
    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_SM_POLICY_DNN_DATA = "smPolicyDnnData";
    private Map<String, SmPolicyDnnDataPatch> smPolicyDnnData = null;

    public SmPolicySnssaiDataPatch()
    {
    }

    public SmPolicySnssaiDataPatch snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
    }

    public SmPolicySnssaiDataPatch smPolicyDnnData(Map<String, SmPolicyDnnDataPatch> smPolicyDnnData)
    {

        this.smPolicyDnnData = smPolicyDnnData;
        return this;
    }

    public SmPolicySnssaiDataPatch putSmPolicyDnnDataItem(String key,
                                                          SmPolicyDnnDataPatch smPolicyDnnDataItem)
    {
        if (this.smPolicyDnnData == null)
        {
            this.smPolicyDnnData = new HashMap<>();
        }
        this.smPolicyDnnData.put(key, smPolicyDnnDataItem);
        return this;
    }

    /**
     * Modifiable Session Management Policy data per DNN for all the DNNs of the
     * indicated S-NSSAI. The key of the map is the DNN.
     * 
     * @return smPolicyDnnData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Modifiable Session Management Policy data per DNN for all the DNNs of the indicated S-NSSAI. The key of the map is the DNN. ")
    @JsonProperty(JSON_PROPERTY_SM_POLICY_DNN_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, SmPolicyDnnDataPatch> getSmPolicyDnnData()
    {
        return smPolicyDnnData;
    }

    @JsonProperty(JSON_PROPERTY_SM_POLICY_DNN_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmPolicyDnnData(Map<String, SmPolicyDnnDataPatch> smPolicyDnnData)
    {
        this.smPolicyDnnData = smPolicyDnnData;
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
        SmPolicySnssaiDataPatch smPolicySnssaiDataPatch = (SmPolicySnssaiDataPatch) o;
        return Objects.equals(this.snssai, smPolicySnssaiDataPatch.snssai) && Objects.equals(this.smPolicyDnnData, smPolicySnssaiDataPatch.smPolicyDnnData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(snssai, smPolicyDnnData);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmPolicySnssaiDataPatch {\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    smPolicyDnnData: ").append(toIndentedString(smPolicyDnnData)).append("\n");
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
