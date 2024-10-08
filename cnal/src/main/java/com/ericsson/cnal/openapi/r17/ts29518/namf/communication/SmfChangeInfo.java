/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

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
 * SMF change information for PDU session(s)
 */
@ApiModel(description = "SMF change information for PDU session(s)")
@JsonPropertyOrder({ SmfChangeInfo.JSON_PROPERTY_PDU_SESSION_ID_LIST, SmfChangeInfo.JSON_PROPERTY_SMF_CHANGE_IND })
public class SmfChangeInfo
{
    public static final String JSON_PROPERTY_PDU_SESSION_ID_LIST = "pduSessionIdList";
    private List<Integer> pduSessionIdList = new ArrayList<>();

    public static final String JSON_PROPERTY_SMF_CHANGE_IND = "smfChangeInd";
    private String smfChangeInd;

    public SmfChangeInfo()
    {
    }

    public SmfChangeInfo pduSessionIdList(List<Integer> pduSessionIdList)
    {

        this.pduSessionIdList = pduSessionIdList;
        return this;
    }

    public SmfChangeInfo addPduSessionIdListItem(Integer pduSessionIdListItem)
    {
        this.pduSessionIdList.add(pduSessionIdListItem);
        return this;
    }

    /**
     * Get pduSessionIdList
     * 
     * @return pduSessionIdList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<Integer> getPduSessionIdList()
    {
        return pduSessionIdList;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPduSessionIdList(List<Integer> pduSessionIdList)
    {
        this.pduSessionIdList = pduSessionIdList;
    }

    public SmfChangeInfo smfChangeInd(String smfChangeInd)
    {

        this.smfChangeInd = smfChangeInd;
        return this;
    }

    /**
     * Indicates the I-SMF or V-SMF change or removal
     * 
     * @return smfChangeInd
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the I-SMF or V-SMF change or removal")
    @JsonProperty(JSON_PROPERTY_SMF_CHANGE_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSmfChangeInd()
    {
        return smfChangeInd;
    }

    @JsonProperty(JSON_PROPERTY_SMF_CHANGE_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSmfChangeInd(String smfChangeInd)
    {
        this.smfChangeInd = smfChangeInd;
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
        SmfChangeInfo smfChangeInfo = (SmfChangeInfo) o;
        return Objects.equals(this.pduSessionIdList, smfChangeInfo.pduSessionIdList) && Objects.equals(this.smfChangeInd, smfChangeInfo.smfChangeInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pduSessionIdList, smfChangeInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmfChangeInfo {\n");
        sb.append("    pduSessionIdList: ").append(toIndentedString(pduSessionIdList)).append("\n");
        sb.append("    smfChangeInd: ").append(toIndentedString(smfChangeInd)).append("\n");
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
