/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

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
 * Identifies the target UE information.
 */
@ApiModel(description = "Identifies the target UE information.")
@JsonPropertyOrder({ TargetUeInformation.JSON_PROPERTY_ANY_UE,
                     TargetUeInformation.JSON_PROPERTY_SUPIS,
                     TargetUeInformation.JSON_PROPERTY_GPSIS,
                     TargetUeInformation.JSON_PROPERTY_INT_GROUP_IDS })
public class TargetUeInformation
{
    public static final String JSON_PROPERTY_ANY_UE = "anyUe";
    private Boolean anyUe;

    public static final String JSON_PROPERTY_SUPIS = "supis";
    private List<String> supis = null;

    public static final String JSON_PROPERTY_GPSIS = "gpsis";
    private List<String> gpsis = null;

    public static final String JSON_PROPERTY_INT_GROUP_IDS = "intGroupIds";
    private List<String> intGroupIds = null;

    public TargetUeInformation()
    {
    }

    public TargetUeInformation anyUe(Boolean anyUe)
    {

        this.anyUe = anyUe;
        return this;
    }

    /**
     * Get anyUe
     * 
     * @return anyUe
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ANY_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAnyUe()
    {
        return anyUe;
    }

    @JsonProperty(JSON_PROPERTY_ANY_UE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnyUe(Boolean anyUe)
    {
        this.anyUe = anyUe;
    }

    public TargetUeInformation supis(List<String> supis)
    {

        this.supis = supis;
        return this;
    }

    public TargetUeInformation addSupisItem(String supisItem)
    {
        if (this.supis == null)
        {
            this.supis = new ArrayList<>();
        }
        this.supis.add(supisItem);
        return this;
    }

    /**
     * Get supis
     * 
     * @return supis
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSupis()
    {
        return supis;
    }

    @JsonProperty(JSON_PROPERTY_SUPIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupis(List<String> supis)
    {
        this.supis = supis;
    }

    public TargetUeInformation gpsis(List<String> gpsis)
    {

        this.gpsis = gpsis;
        return this;
    }

    public TargetUeInformation addGpsisItem(String gpsisItem)
    {
        if (this.gpsis == null)
        {
            this.gpsis = new ArrayList<>();
        }
        this.gpsis.add(gpsisItem);
        return this;
    }

    /**
     * Get gpsis
     * 
     * @return gpsis
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GPSIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getGpsis()
    {
        return gpsis;
    }

    @JsonProperty(JSON_PROPERTY_GPSIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsis(List<String> gpsis)
    {
        this.gpsis = gpsis;
    }

    public TargetUeInformation intGroupIds(List<String> intGroupIds)
    {

        this.intGroupIds = intGroupIds;
        return this;
    }

    public TargetUeInformation addIntGroupIdsItem(String intGroupIdsItem)
    {
        if (this.intGroupIds == null)
        {
            this.intGroupIds = new ArrayList<>();
        }
        this.intGroupIds.add(intGroupIdsItem);
        return this;
    }

    /**
     * Get intGroupIds
     * 
     * @return intGroupIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INT_GROUP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIntGroupIds()
    {
        return intGroupIds;
    }

    @JsonProperty(JSON_PROPERTY_INT_GROUP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntGroupIds(List<String> intGroupIds)
    {
        this.intGroupIds = intGroupIds;
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
        TargetUeInformation targetUeInformation = (TargetUeInformation) o;
        return Objects.equals(this.anyUe, targetUeInformation.anyUe) && Objects.equals(this.supis, targetUeInformation.supis)
               && Objects.equals(this.gpsis, targetUeInformation.gpsis) && Objects.equals(this.intGroupIds, targetUeInformation.intGroupIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(anyUe, supis, gpsis, intGroupIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TargetUeInformation {\n");
        sb.append("    anyUe: ").append(toIndentedString(anyUe)).append("\n");
        sb.append("    supis: ").append(toIndentedString(supis)).append("\n");
        sb.append("    gpsis: ").append(toIndentedString(gpsis)).append("\n");
        sb.append("    intGroupIds: ").append(toIndentedString(intGroupIds)).append("\n");
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
