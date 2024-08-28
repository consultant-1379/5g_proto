/*
 * Nudm_SDM
 * Nudm Subscriber Data Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ServiceAreaRestriction;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Area;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PlmnRestriction
 */
@JsonPropertyOrder({ PlmnRestriction.JSON_PROPERTY_RAT_RESTRICTIONS,
                     PlmnRestriction.JSON_PROPERTY_FORBIDDEN_AREAS,
                     PlmnRestriction.JSON_PROPERTY_SERVICE_AREA_RESTRICTION,
                     PlmnRestriction.JSON_PROPERTY_CORE_NETWORK_TYPE_RESTRICTIONS,
                     PlmnRestriction.JSON_PROPERTY_PRIMARY_RAT_RESTRICTIONS,
                     PlmnRestriction.JSON_PROPERTY_SECONDARY_RAT_RESTRICTIONS })
public class PlmnRestriction
{
    public static final String JSON_PROPERTY_RAT_RESTRICTIONS = "ratRestrictions";
    private Set<String> ratRestrictions = null;

    public static final String JSON_PROPERTY_FORBIDDEN_AREAS = "forbiddenAreas";
    private List<Area> forbiddenAreas = null;

    public static final String JSON_PROPERTY_SERVICE_AREA_RESTRICTION = "serviceAreaRestriction";
    private ServiceAreaRestriction serviceAreaRestriction;

    public static final String JSON_PROPERTY_CORE_NETWORK_TYPE_RESTRICTIONS = "coreNetworkTypeRestrictions";
    private List<String> coreNetworkTypeRestrictions = null;

    public static final String JSON_PROPERTY_PRIMARY_RAT_RESTRICTIONS = "primaryRatRestrictions";
    private Set<String> primaryRatRestrictions = null;

    public static final String JSON_PROPERTY_SECONDARY_RAT_RESTRICTIONS = "secondaryRatRestrictions";
    private Set<String> secondaryRatRestrictions = null;

    public PlmnRestriction()
    {
    }

    public PlmnRestriction ratRestrictions(Set<String> ratRestrictions)
    {

        this.ratRestrictions = ratRestrictions;
        return this;
    }

    public PlmnRestriction addRatRestrictionsItem(String ratRestrictionsItem)
    {
        if (this.ratRestrictions == null)
        {
            this.ratRestrictions = new LinkedHashSet<>();
        }
        this.ratRestrictions.add(ratRestrictionsItem);
        return this;
    }

    /**
     * Get ratRestrictions
     * 
     * @return ratRestrictions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Set<String> getRatRestrictions()
    {
        return ratRestrictions;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatRestrictions(Set<String> ratRestrictions)
    {
        this.ratRestrictions = ratRestrictions;
    }

    public PlmnRestriction forbiddenAreas(List<Area> forbiddenAreas)
    {

        this.forbiddenAreas = forbiddenAreas;
        return this;
    }

    public PlmnRestriction addForbiddenAreasItem(Area forbiddenAreasItem)
    {
        if (this.forbiddenAreas == null)
        {
            this.forbiddenAreas = new ArrayList<>();
        }
        this.forbiddenAreas.add(forbiddenAreasItem);
        return this;
    }

    /**
     * Get forbiddenAreas
     * 
     * @return forbiddenAreas
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_FORBIDDEN_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Area> getForbiddenAreas()
    {
        return forbiddenAreas;
    }

    @JsonProperty(JSON_PROPERTY_FORBIDDEN_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setForbiddenAreas(List<Area> forbiddenAreas)
    {
        this.forbiddenAreas = forbiddenAreas;
    }

    public PlmnRestriction serviceAreaRestriction(ServiceAreaRestriction serviceAreaRestriction)
    {

        this.serviceAreaRestriction = serviceAreaRestriction;
        return this;
    }

    /**
     * Get serviceAreaRestriction
     * 
     * @return serviceAreaRestriction
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_AREA_RESTRICTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ServiceAreaRestriction getServiceAreaRestriction()
    {
        return serviceAreaRestriction;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_AREA_RESTRICTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceAreaRestriction(ServiceAreaRestriction serviceAreaRestriction)
    {
        this.serviceAreaRestriction = serviceAreaRestriction;
    }

    public PlmnRestriction coreNetworkTypeRestrictions(List<String> coreNetworkTypeRestrictions)
    {

        this.coreNetworkTypeRestrictions = coreNetworkTypeRestrictions;
        return this;
    }

    public PlmnRestriction addCoreNetworkTypeRestrictionsItem(String coreNetworkTypeRestrictionsItem)
    {
        if (this.coreNetworkTypeRestrictions == null)
        {
            this.coreNetworkTypeRestrictions = new ArrayList<>();
        }
        this.coreNetworkTypeRestrictions.add(coreNetworkTypeRestrictionsItem);
        return this;
    }

    /**
     * Get coreNetworkTypeRestrictions
     * 
     * @return coreNetworkTypeRestrictions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CORE_NETWORK_TYPE_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getCoreNetworkTypeRestrictions()
    {
        return coreNetworkTypeRestrictions;
    }

    @JsonProperty(JSON_PROPERTY_CORE_NETWORK_TYPE_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCoreNetworkTypeRestrictions(List<String> coreNetworkTypeRestrictions)
    {
        this.coreNetworkTypeRestrictions = coreNetworkTypeRestrictions;
    }

    public PlmnRestriction primaryRatRestrictions(Set<String> primaryRatRestrictions)
    {

        this.primaryRatRestrictions = primaryRatRestrictions;
        return this;
    }

    public PlmnRestriction addPrimaryRatRestrictionsItem(String primaryRatRestrictionsItem)
    {
        if (this.primaryRatRestrictions == null)
        {
            this.primaryRatRestrictions = new LinkedHashSet<>();
        }
        this.primaryRatRestrictions.add(primaryRatRestrictionsItem);
        return this;
    }

    /**
     * Get primaryRatRestrictions
     * 
     * @return primaryRatRestrictions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRIMARY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Set<String> getPrimaryRatRestrictions()
    {
        return primaryRatRestrictions;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_PRIMARY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryRatRestrictions(Set<String> primaryRatRestrictions)
    {
        this.primaryRatRestrictions = primaryRatRestrictions;
    }

    public PlmnRestriction secondaryRatRestrictions(Set<String> secondaryRatRestrictions)
    {

        this.secondaryRatRestrictions = secondaryRatRestrictions;
        return this;
    }

    public PlmnRestriction addSecondaryRatRestrictionsItem(String secondaryRatRestrictionsItem)
    {
        if (this.secondaryRatRestrictions == null)
        {
            this.secondaryRatRestrictions = new LinkedHashSet<>();
        }
        this.secondaryRatRestrictions.add(secondaryRatRestrictionsItem);
        return this;
    }

    /**
     * Get secondaryRatRestrictions
     * 
     * @return secondaryRatRestrictions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Set<String> getSecondaryRatRestrictions()
    {
        return secondaryRatRestrictions;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_RESTRICTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecondaryRatRestrictions(Set<String> secondaryRatRestrictions)
    {
        this.secondaryRatRestrictions = secondaryRatRestrictions;
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
        PlmnRestriction plmnRestriction = (PlmnRestriction) o;
        return Objects.equals(this.ratRestrictions, plmnRestriction.ratRestrictions) && Objects.equals(this.forbiddenAreas, plmnRestriction.forbiddenAreas)
               && Objects.equals(this.serviceAreaRestriction, plmnRestriction.serviceAreaRestriction)
               && Objects.equals(this.coreNetworkTypeRestrictions, plmnRestriction.coreNetworkTypeRestrictions)
               && Objects.equals(this.primaryRatRestrictions, plmnRestriction.primaryRatRestrictions)
               && Objects.equals(this.secondaryRatRestrictions, plmnRestriction.secondaryRatRestrictions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ratRestrictions,
                            forbiddenAreas,
                            serviceAreaRestriction,
                            coreNetworkTypeRestrictions,
                            primaryRatRestrictions,
                            secondaryRatRestrictions);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlmnRestriction {\n");
        sb.append("    ratRestrictions: ").append(toIndentedString(ratRestrictions)).append("\n");
        sb.append("    forbiddenAreas: ").append(toIndentedString(forbiddenAreas)).append("\n");
        sb.append("    serviceAreaRestriction: ").append(toIndentedString(serviceAreaRestriction)).append("\n");
        sb.append("    coreNetworkTypeRestrictions: ").append(toIndentedString(coreNetworkTypeRestrictions)).append("\n");
        sb.append("    primaryRatRestrictions: ").append(toIndentedString(primaryRatRestrictions)).append("\n");
        sb.append("    secondaryRatRestrictions: ").append(toIndentedString(secondaryRatRestrictions)).append("\n");
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
