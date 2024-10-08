/*
 * Npcf_BDTPolicyControl Service API
 * PCF BDT Policy Control Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol;

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
 * A JSON Merge Patch body schema containing modification instruction to be
 * performed on the bdtReqData attribute of the BdtPolicy data structure to
 * indicate whether the BDT warning notification is enabled or disabled.
 * Modifies warnNotifReq from BdtReqData data structure.
 */
@ApiModel(description = "A JSON Merge Patch body schema containing modification instruction to be performed on the bdtReqData attribute of the BdtPolicy data structure to indicate whether the BDT warning notification is enabled or disabled. Modifies warnNotifReq from BdtReqData data structure. ")
@JsonPropertyOrder({ BdtReqDataPatch.JSON_PROPERTY_WARN_NOTIF_REQ })
public class BdtReqDataPatch
{
    public static final String JSON_PROPERTY_WARN_NOTIF_REQ = "warnNotifReq";
    private Boolean warnNotifReq;

    public BdtReqDataPatch()
    {
    }

    public BdtReqDataPatch warnNotifReq(Boolean warnNotifReq)
    {

        this.warnNotifReq = warnNotifReq;
        return this;
    }

    /**
     * Indicates whether the BDT warning notification is enabled or disabled.
     * 
     * @return warnNotifReq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates whether the BDT warning notification is enabled or disabled.")
    @JsonProperty(JSON_PROPERTY_WARN_NOTIF_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getWarnNotifReq()
    {
        return warnNotifReq;
    }

    @JsonProperty(JSON_PROPERTY_WARN_NOTIF_REQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWarnNotifReq(Boolean warnNotifReq)
    {
        this.warnNotifReq = warnNotifReq;
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
        BdtReqDataPatch bdtReqDataPatch = (BdtReqDataPatch) o;
        return Objects.equals(this.warnNotifReq, bdtReqDataPatch.warnNotifReq);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(warnNotifReq);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class BdtReqDataPatch {\n");
        sb.append("    warnNotifReq: ").append(toIndentedString(warnNotifReq)).append("\n");
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
