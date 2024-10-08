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
import com.ericsson.cnal.openapi.r17.ts29572.nlmf.location.GeographicArea;
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
 * DefaultUnrelatedClass
 */
@JsonPropertyOrder({ DefaultUnrelatedClass.JSON_PROPERTY_ALLOWED_GEOGRAPHIC_AREA,
                     DefaultUnrelatedClass.JSON_PROPERTY_PRIVACY_CHECK_RELATED_ACTION,
                     DefaultUnrelatedClass.JSON_PROPERTY_CODE_WORD_IND,
                     DefaultUnrelatedClass.JSON_PROPERTY_VALID_TIME_PERIOD,
                     DefaultUnrelatedClass.JSON_PROPERTY_CODE_WORD_LIST })
public class DefaultUnrelatedClass
{
    public static final String JSON_PROPERTY_ALLOWED_GEOGRAPHIC_AREA = "allowedGeographicArea";
    private List<GeographicArea> allowedGeographicArea = null;

    public static final String JSON_PROPERTY_PRIVACY_CHECK_RELATED_ACTION = "privacyCheckRelatedAction";
    private String privacyCheckRelatedAction;

    public static final String JSON_PROPERTY_CODE_WORD_IND = "codeWordInd";
    private String codeWordInd;

    public static final String JSON_PROPERTY_VALID_TIME_PERIOD = "validTimePeriod";
    private ValidTimePeriod validTimePeriod;

    public static final String JSON_PROPERTY_CODE_WORD_LIST = "codeWordList";
    private List<String> codeWordList = null;

    public DefaultUnrelatedClass()
    {
    }

    public DefaultUnrelatedClass allowedGeographicArea(List<GeographicArea> allowedGeographicArea)
    {

        this.allowedGeographicArea = allowedGeographicArea;
        return this;
    }

    public DefaultUnrelatedClass addAllowedGeographicAreaItem(GeographicArea allowedGeographicAreaItem)
    {
        if (this.allowedGeographicArea == null)
        {
            this.allowedGeographicArea = new ArrayList<>();
        }
        this.allowedGeographicArea.add(allowedGeographicAreaItem);
        return this;
    }

    /**
     * Get allowedGeographicArea
     * 
     * @return allowedGeographicArea
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ALLOWED_GEOGRAPHIC_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<GeographicArea> getAllowedGeographicArea()
    {
        return allowedGeographicArea;
    }

    @JsonProperty(JSON_PROPERTY_ALLOWED_GEOGRAPHIC_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowedGeographicArea(List<GeographicArea> allowedGeographicArea)
    {
        this.allowedGeographicArea = allowedGeographicArea;
    }

    public DefaultUnrelatedClass privacyCheckRelatedAction(String privacyCheckRelatedAction)
    {

        this.privacyCheckRelatedAction = privacyCheckRelatedAction;
        return this;
    }

    /**
     * Get privacyCheckRelatedAction
     * 
     * @return privacyCheckRelatedAction
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRIVACY_CHECK_RELATED_ACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPrivacyCheckRelatedAction()
    {
        return privacyCheckRelatedAction;
    }

    @JsonProperty(JSON_PROPERTY_PRIVACY_CHECK_RELATED_ACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrivacyCheckRelatedAction(String privacyCheckRelatedAction)
    {
        this.privacyCheckRelatedAction = privacyCheckRelatedAction;
    }

    public DefaultUnrelatedClass codeWordInd(String codeWordInd)
    {

        this.codeWordInd = codeWordInd;
        return this;
    }

    /**
     * Get codeWordInd
     * 
     * @return codeWordInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CODE_WORD_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCodeWordInd()
    {
        return codeWordInd;
    }

    @JsonProperty(JSON_PROPERTY_CODE_WORD_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodeWordInd(String codeWordInd)
    {
        this.codeWordInd = codeWordInd;
    }

    public DefaultUnrelatedClass validTimePeriod(ValidTimePeriod validTimePeriod)
    {

        this.validTimePeriod = validTimePeriod;
        return this;
    }

    /**
     * Get validTimePeriod
     * 
     * @return validTimePeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_VALID_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ValidTimePeriod getValidTimePeriod()
    {
        return validTimePeriod;
    }

    @JsonProperty(JSON_PROPERTY_VALID_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidTimePeriod(ValidTimePeriod validTimePeriod)
    {
        this.validTimePeriod = validTimePeriod;
    }

    public DefaultUnrelatedClass codeWordList(List<String> codeWordList)
    {

        this.codeWordList = codeWordList;
        return this;
    }

    public DefaultUnrelatedClass addCodeWordListItem(String codeWordListItem)
    {
        if (this.codeWordList == null)
        {
            this.codeWordList = new ArrayList<>();
        }
        this.codeWordList.add(codeWordListItem);
        return this;
    }

    /**
     * Get codeWordList
     * 
     * @return codeWordList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CODE_WORD_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getCodeWordList()
    {
        return codeWordList;
    }

    @JsonProperty(JSON_PROPERTY_CODE_WORD_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodeWordList(List<String> codeWordList)
    {
        this.codeWordList = codeWordList;
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
        DefaultUnrelatedClass defaultUnrelatedClass = (DefaultUnrelatedClass) o;
        return Objects.equals(this.allowedGeographicArea, defaultUnrelatedClass.allowedGeographicArea)
               && Objects.equals(this.privacyCheckRelatedAction, defaultUnrelatedClass.privacyCheckRelatedAction)
               && Objects.equals(this.codeWordInd, defaultUnrelatedClass.codeWordInd)
               && Objects.equals(this.validTimePeriod, defaultUnrelatedClass.validTimePeriod)
               && Objects.equals(this.codeWordList, defaultUnrelatedClass.codeWordList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(allowedGeographicArea, privacyCheckRelatedAction, codeWordInd, validTimePeriod, codeWordList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DefaultUnrelatedClass {\n");
        sb.append("    allowedGeographicArea: ").append(toIndentedString(allowedGeographicArea)).append("\n");
        sb.append("    privacyCheckRelatedAction: ").append(toIndentedString(privacyCheckRelatedAction)).append("\n");
        sb.append("    codeWordInd: ").append(toIndentedString(codeWordInd)).append("\n");
        sb.append("    validTimePeriod: ").append(toIndentedString(validTimePeriod)).append("\n");
        sb.append("    codeWordList: ").append(toIndentedString(codeWordList)).append("\n");
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
