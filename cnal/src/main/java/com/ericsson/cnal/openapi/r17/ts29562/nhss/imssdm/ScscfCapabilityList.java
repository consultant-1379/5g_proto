/*
 * Nhss_imsSDM
 * Nhss Subscriber Data Management Service for IMS.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29562.nhss.imssdm;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Information about mandatory and optional S-CSCF capabilities
 */
@ApiModel(description = "Information about mandatory and optional S-CSCF capabilities")
@JsonPropertyOrder({ ScscfCapabilityList.JSON_PROPERTY_MANDATORY_CAPABILITY_LIST, ScscfCapabilityList.JSON_PROPERTY_OPTIONAL_CAPABILITY_LIST })
public class ScscfCapabilityList
{
    public static final String JSON_PROPERTY_MANDATORY_CAPABILITY_LIST = "mandatoryCapabilityList";
    private Set<Integer> mandatoryCapabilityList = null;

    public static final String JSON_PROPERTY_OPTIONAL_CAPABILITY_LIST = "optionalCapabilityList";
    private Set<Integer> optionalCapabilityList = null;

    public ScscfCapabilityList()
    {
    }

    public ScscfCapabilityList mandatoryCapabilityList(Set<Integer> mandatoryCapabilityList)
    {

        this.mandatoryCapabilityList = mandatoryCapabilityList;
        return this;
    }

    public ScscfCapabilityList addMandatoryCapabilityListItem(Integer mandatoryCapabilityListItem)
    {
        if (this.mandatoryCapabilityList == null)
        {
            this.mandatoryCapabilityList = new LinkedHashSet<>();
        }
        this.mandatoryCapabilityList.add(mandatoryCapabilityListItem);
        return this;
    }

    /**
     * A list of capabilities of the S-CSCF
     * 
     * @return mandatoryCapabilityList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of capabilities of the S-CSCF")
    @JsonProperty(JSON_PROPERTY_MANDATORY_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Set<Integer> getMandatoryCapabilityList()
    {
        return mandatoryCapabilityList;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_MANDATORY_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMandatoryCapabilityList(Set<Integer> mandatoryCapabilityList)
    {
        this.mandatoryCapabilityList = mandatoryCapabilityList;
    }

    public ScscfCapabilityList optionalCapabilityList(Set<Integer> optionalCapabilityList)
    {

        this.optionalCapabilityList = optionalCapabilityList;
        return this;
    }

    public ScscfCapabilityList addOptionalCapabilityListItem(Integer optionalCapabilityListItem)
    {
        if (this.optionalCapabilityList == null)
        {
            this.optionalCapabilityList = new LinkedHashSet<>();
        }
        this.optionalCapabilityList.add(optionalCapabilityListItem);
        return this;
    }

    /**
     * A list of capabilities of the S-CSCF
     * 
     * @return optionalCapabilityList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of capabilities of the S-CSCF")
    @JsonProperty(JSON_PROPERTY_OPTIONAL_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Set<Integer> getOptionalCapabilityList()
    {
        return optionalCapabilityList;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonProperty(JSON_PROPERTY_OPTIONAL_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOptionalCapabilityList(Set<Integer> optionalCapabilityList)
    {
        this.optionalCapabilityList = optionalCapabilityList;
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
        ScscfCapabilityList scscfCapabilityList = (ScscfCapabilityList) o;
        return Objects.equals(this.mandatoryCapabilityList, scscfCapabilityList.mandatoryCapabilityList)
               && Objects.equals(this.optionalCapabilityList, scscfCapabilityList.optionalCapabilityList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mandatoryCapabilityList, optionalCapabilityList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ScscfCapabilityList {\n");
        sb.append("    mandatoryCapabilityList: ").append(toIndentedString(mandatoryCapabilityList)).append("\n");
        sb.append("    optionalCapabilityList: ").append(toIndentedString(optionalCapabilityList)).append("\n");
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
