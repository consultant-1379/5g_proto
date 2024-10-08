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
import com.ericsson.cnal.openapi.r17.ts29505.subscription.data.OperatorSpecificDataContainer;
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
 * Contains policy data for a given subscriber.
 */
@ApiModel(description = "Contains policy data for a given subscriber.")
@JsonPropertyOrder({ PolicyDataForIndividualUe.JSON_PROPERTY_UE_POLICY_DATA_SET,
                     PolicyDataForIndividualUe.JSON_PROPERTY_SM_POLICY_DATA_SET,
                     PolicyDataForIndividualUe.JSON_PROPERTY_AM_POLICY_DATA_SET,
                     PolicyDataForIndividualUe.JSON_PROPERTY_UM_DATA,
                     PolicyDataForIndividualUe.JSON_PROPERTY_OPERATOR_SPECIFIC_DATA_SET })
public class PolicyDataForIndividualUe
{
    public static final String JSON_PROPERTY_UE_POLICY_DATA_SET = "uePolicyDataSet";
    private UePolicySet uePolicyDataSet;

    public static final String JSON_PROPERTY_SM_POLICY_DATA_SET = "smPolicyDataSet";
    private SmPolicyData smPolicyDataSet;

    public static final String JSON_PROPERTY_AM_POLICY_DATA_SET = "amPolicyDataSet";
    private AmPolicyData amPolicyDataSet;

    public static final String JSON_PROPERTY_UM_DATA = "umData";
    private Map<String, UsageMonData> umData = null;

    public static final String JSON_PROPERTY_OPERATOR_SPECIFIC_DATA_SET = "operatorSpecificDataSet";
    private Map<String, OperatorSpecificDataContainer> operatorSpecificDataSet = null;

    public PolicyDataForIndividualUe()
    {
    }

    public PolicyDataForIndividualUe uePolicyDataSet(UePolicySet uePolicyDataSet)
    {

        this.uePolicyDataSet = uePolicyDataSet;
        return this;
    }

    /**
     * Get uePolicyDataSet
     * 
     * @return uePolicyDataSet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UePolicySet getUePolicyDataSet()
    {
        return uePolicyDataSet;
    }

    @JsonProperty(JSON_PROPERTY_UE_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUePolicyDataSet(UePolicySet uePolicyDataSet)
    {
        this.uePolicyDataSet = uePolicyDataSet;
    }

    public PolicyDataForIndividualUe smPolicyDataSet(SmPolicyData smPolicyDataSet)
    {

        this.smPolicyDataSet = smPolicyDataSet;
        return this;
    }

    /**
     * Get smPolicyDataSet
     * 
     * @return smPolicyDataSet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SM_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmPolicyData getSmPolicyDataSet()
    {
        return smPolicyDataSet;
    }

    @JsonProperty(JSON_PROPERTY_SM_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmPolicyDataSet(SmPolicyData smPolicyDataSet)
    {
        this.smPolicyDataSet = smPolicyDataSet;
    }

    public PolicyDataForIndividualUe amPolicyDataSet(AmPolicyData amPolicyDataSet)
    {

        this.amPolicyDataSet = amPolicyDataSet;
        return this;
    }

    /**
     * Get amPolicyDataSet
     * 
     * @return amPolicyDataSet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AM_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AmPolicyData getAmPolicyDataSet()
    {
        return amPolicyDataSet;
    }

    @JsonProperty(JSON_PROPERTY_AM_POLICY_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAmPolicyDataSet(AmPolicyData amPolicyDataSet)
    {
        this.amPolicyDataSet = amPolicyDataSet;
    }

    public PolicyDataForIndividualUe umData(Map<String, UsageMonData> umData)
    {

        this.umData = umData;
        return this;
    }

    public PolicyDataForIndividualUe putUmDataItem(String key,
                                                   UsageMonData umDataItem)
    {
        if (this.umData == null)
        {
            this.umData = new HashMap<>();
        }
        this.umData.put(key, umDataItem);
        return this;
    }

    /**
     * Contains UM policies. The value of the limit identifier is used as the key of
     * the map.
     * 
     * @return umData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains UM policies. The value of the limit identifier is used as the key of the map. ")
    @JsonProperty(JSON_PROPERTY_UM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, UsageMonData> getUmData()
    {
        return umData;
    }

    @JsonProperty(JSON_PROPERTY_UM_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUmData(Map<String, UsageMonData> umData)
    {
        this.umData = umData;
    }

    public PolicyDataForIndividualUe operatorSpecificDataSet(Map<String, OperatorSpecificDataContainer> operatorSpecificDataSet)
    {

        this.operatorSpecificDataSet = operatorSpecificDataSet;
        return this;
    }

    public PolicyDataForIndividualUe putOperatorSpecificDataSetItem(String key,
                                                                    OperatorSpecificDataContainer operatorSpecificDataSetItem)
    {
        if (this.operatorSpecificDataSet == null)
        {
            this.operatorSpecificDataSet = new HashMap<>();
        }
        this.operatorSpecificDataSet.put(key, operatorSpecificDataSetItem);
        return this;
    }

    /**
     * Contains Operator Specific Data resource data. The key of the map is operator
     * specific data element name and the value is the operator specific data of the
     * UE.
     * 
     * @return operatorSpecificDataSet
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains Operator Specific Data resource data. The key of the map is operator specific data element name and the value is the operator specific data of the UE. ")
    @JsonProperty(JSON_PROPERTY_OPERATOR_SPECIFIC_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, OperatorSpecificDataContainer> getOperatorSpecificDataSet()
    {
        return operatorSpecificDataSet;
    }

    @JsonProperty(JSON_PROPERTY_OPERATOR_SPECIFIC_DATA_SET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOperatorSpecificDataSet(Map<String, OperatorSpecificDataContainer> operatorSpecificDataSet)
    {
        this.operatorSpecificDataSet = operatorSpecificDataSet;
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
        PolicyDataForIndividualUe policyDataForIndividualUe = (PolicyDataForIndividualUe) o;
        return Objects.equals(this.uePolicyDataSet, policyDataForIndividualUe.uePolicyDataSet)
               && Objects.equals(this.smPolicyDataSet, policyDataForIndividualUe.smPolicyDataSet)
               && Objects.equals(this.amPolicyDataSet, policyDataForIndividualUe.amPolicyDataSet)
               && Objects.equals(this.umData, policyDataForIndividualUe.umData)
               && Objects.equals(this.operatorSpecificDataSet, policyDataForIndividualUe.operatorSpecificDataSet);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uePolicyDataSet, smPolicyDataSet, amPolicyDataSet, umData, operatorSpecificDataSet);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyDataForIndividualUe {\n");
        sb.append("    uePolicyDataSet: ").append(toIndentedString(uePolicyDataSet)).append("\n");
        sb.append("    smPolicyDataSet: ").append(toIndentedString(smPolicyDataSet)).append("\n");
        sb.append("    amPolicyDataSet: ").append(toIndentedString(amPolicyDataSet)).append("\n");
        sb.append("    umData: ").append(toIndentedString(umData)).append("\n");
        sb.append("    operatorSpecificDataSet: ").append(toIndentedString(operatorSpecificDataSet)).append("\n");
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
