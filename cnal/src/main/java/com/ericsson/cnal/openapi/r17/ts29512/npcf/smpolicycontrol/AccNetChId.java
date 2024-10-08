/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

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
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the access network charging identifier for the PCC rule(s) or for
 * the whole PDU session.
 */
@ApiModel(description = "Contains the access network charging identifier for the PCC rule(s) or for the whole PDU session.")
@JsonPropertyOrder({ AccNetChId.JSON_PROPERTY_ACC_NET_CHA_ID_VALUE,
                     AccNetChId.JSON_PROPERTY_ACC_NET_CHARG_ID,
                     AccNetChId.JSON_PROPERTY_REF_PCC_RULE_IDS,
                     AccNetChId.JSON_PROPERTY_SESSION_CH_SCOPE })
public class AccNetChId
{
    public static final String JSON_PROPERTY_ACC_NET_CHA_ID_VALUE = "accNetChaIdValue";
    private Integer accNetChaIdValue;

    public static final String JSON_PROPERTY_ACC_NET_CHARG_ID = "accNetChargId";
    private String accNetChargId;

    public static final String JSON_PROPERTY_REF_PCC_RULE_IDS = "refPccRuleIds";
    private List<String> refPccRuleIds = null;

    public static final String JSON_PROPERTY_SESSION_CH_SCOPE = "sessionChScope";
    private Boolean sessionChScope;

    public AccNetChId()
    {
    }

    public AccNetChId accNetChaIdValue(Integer accNetChaIdValue)
    {

        this.accNetChaIdValue = accNetChaIdValue;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return accNetChaIdValue
     * @deprecated
     **/
    @Deprecated
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_ACC_NET_CHA_ID_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAccNetChaIdValue()
    {
        return accNetChaIdValue;
    }

    @JsonProperty(JSON_PROPERTY_ACC_NET_CHA_ID_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccNetChaIdValue(Integer accNetChaIdValue)
    {
        this.accNetChaIdValue = accNetChaIdValue;
    }

    public AccNetChId accNetChargId(String accNetChargId)
    {

        this.accNetChargId = accNetChargId;
        return this;
    }

    /**
     * A character string containing the access network charging id.
     * 
     * @return accNetChargId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A character string containing the access network charging id.")
    @JsonProperty(JSON_PROPERTY_ACC_NET_CHARG_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAccNetChargId()
    {
        return accNetChargId;
    }

    @JsonProperty(JSON_PROPERTY_ACC_NET_CHARG_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccNetChargId(String accNetChargId)
    {
        this.accNetChargId = accNetChargId;
    }

    public AccNetChId refPccRuleIds(List<String> refPccRuleIds)
    {

        this.refPccRuleIds = refPccRuleIds;
        return this;
    }

    public AccNetChId addRefPccRuleIdsItem(String refPccRuleIdsItem)
    {
        if (this.refPccRuleIds == null)
        {
            this.refPccRuleIds = new ArrayList<>();
        }
        this.refPccRuleIds.add(refPccRuleIdsItem);
        return this;
    }

    /**
     * Contains the identifier of the PCC rule(s) associated to the provided Access
     * Network Charging Identifier.
     * 
     * @return refPccRuleIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the identifier of the PCC rule(s) associated to the provided Access Network Charging Identifier.")
    @JsonProperty(JSON_PROPERTY_REF_PCC_RULE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getRefPccRuleIds()
    {
        return refPccRuleIds;
    }

    @JsonProperty(JSON_PROPERTY_REF_PCC_RULE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRefPccRuleIds(List<String> refPccRuleIds)
    {
        this.refPccRuleIds = refPccRuleIds;
    }

    public AccNetChId sessionChScope(Boolean sessionChScope)
    {

        this.sessionChScope = sessionChScope;
        return this;
    }

    /**
     * When it is included and set to true, indicates the Access Network Charging
     * Identifier applies to the whole PDU Session
     * 
     * @return sessionChScope
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "When it is included and set to true, indicates the Access Network Charging Identifier applies to the whole PDU Session")
    @JsonProperty(JSON_PROPERTY_SESSION_CH_SCOPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSessionChScope()
    {
        return sessionChScope;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_CH_SCOPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionChScope(Boolean sessionChScope)
    {
        this.sessionChScope = sessionChScope;
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
        AccNetChId accNetChId = (AccNetChId) o;
        return Objects.equals(this.accNetChaIdValue, accNetChId.accNetChaIdValue) && Objects.equals(this.accNetChargId, accNetChId.accNetChargId)
               && Objects.equals(this.refPccRuleIds, accNetChId.refPccRuleIds) && Objects.equals(this.sessionChScope, accNetChId.sessionChScope);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(accNetChaIdValue, accNetChargId, refPccRuleIds, sessionChScope);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccNetChId {\n");
        sb.append("    accNetChaIdValue: ").append(toIndentedString(accNetChaIdValue)).append("\n");
        sb.append("    accNetChargId: ").append(toIndentedString(accNetChargId)).append("\n");
        sb.append("    refPccRuleIds: ").append(toIndentedString(refPccRuleIds)).append("\n");
        sb.append("    sessionChScope: ").append(toIndentedString(sessionChScope)).append("\n");
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
