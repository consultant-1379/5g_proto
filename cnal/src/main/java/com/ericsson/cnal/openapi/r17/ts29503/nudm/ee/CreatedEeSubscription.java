/*
 * Nudm_EE
 * Nudm Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ee;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * CreatedEeSubscription
 */
@JsonPropertyOrder({ CreatedEeSubscription.JSON_PROPERTY_EE_SUBSCRIPTION,
                     CreatedEeSubscription.JSON_PROPERTY_NUMBER_OF_UES,
                     CreatedEeSubscription.JSON_PROPERTY_EVENT_REPORTS,
                     CreatedEeSubscription.JSON_PROPERTY_EPC_STATUS_IND,
                     CreatedEeSubscription.JSON_PROPERTY_FAILED_MONITORING_CONFIGS,
                     CreatedEeSubscription.JSON_PROPERTY_FAILED_MONI_CONFIGS_E_P_C,
                     CreatedEeSubscription.JSON_PROPERTY_RESET_IDS })
public class CreatedEeSubscription
{
    public static final String JSON_PROPERTY_EE_SUBSCRIPTION = "eeSubscription";
    private EeSubscription eeSubscription;

    public static final String JSON_PROPERTY_NUMBER_OF_UES = "numberOfUes";
    private Integer numberOfUes;

    public static final String JSON_PROPERTY_EVENT_REPORTS = "eventReports";
    private List<MonitoringReport> eventReports = null;

    public static final String JSON_PROPERTY_EPC_STATUS_IND = "epcStatusInd";
    private Boolean epcStatusInd;

    public static final String JSON_PROPERTY_FAILED_MONITORING_CONFIGS = "failedMonitoringConfigs";
    private Map<String, FailedMonitoringConfiguration> failedMonitoringConfigs = null;

    public static final String JSON_PROPERTY_FAILED_MONI_CONFIGS_E_P_C = "failedMoniConfigsEPC";
    private Map<String, FailedMonitoringConfiguration> failedMoniConfigsEPC = null;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public CreatedEeSubscription()
    {
    }

    public CreatedEeSubscription eeSubscription(EeSubscription eeSubscription)
    {

        this.eeSubscription = eeSubscription;
        return this;
    }

    /**
     * Get eeSubscription
     * 
     * @return eeSubscription
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EE_SUBSCRIPTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public EeSubscription getEeSubscription()
    {
        return eeSubscription;
    }

    @JsonProperty(JSON_PROPERTY_EE_SUBSCRIPTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEeSubscription(EeSubscription eeSubscription)
    {
        this.eeSubscription = eeSubscription;
    }

    public CreatedEeSubscription numberOfUes(Integer numberOfUes)
    {

        this.numberOfUes = numberOfUes;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return numberOfUes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_NUMBER_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getNumberOfUes()
    {
        return numberOfUes;
    }

    @JsonProperty(JSON_PROPERTY_NUMBER_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumberOfUes(Integer numberOfUes)
    {
        this.numberOfUes = numberOfUes;
    }

    public CreatedEeSubscription eventReports(List<MonitoringReport> eventReports)
    {

        this.eventReports = eventReports;
        return this;
    }

    public CreatedEeSubscription addEventReportsItem(MonitoringReport eventReportsItem)
    {
        if (this.eventReports == null)
        {
            this.eventReports = new ArrayList<>();
        }
        this.eventReports.add(eventReportsItem);
        return this;
    }

    /**
     * Get eventReports
     * 
     * @return eventReports
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MonitoringReport> getEventReports()
    {
        return eventReports;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_REPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventReports(List<MonitoringReport> eventReports)
    {
        this.eventReports = eventReports;
    }

    public CreatedEeSubscription epcStatusInd(Boolean epcStatusInd)
    {

        this.epcStatusInd = epcStatusInd;
        return this;
    }

    /**
     * Get epcStatusInd
     * 
     * @return epcStatusInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EPC_STATUS_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEpcStatusInd()
    {
        return epcStatusInd;
    }

    @JsonProperty(JSON_PROPERTY_EPC_STATUS_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpcStatusInd(Boolean epcStatusInd)
    {
        this.epcStatusInd = epcStatusInd;
    }

    public CreatedEeSubscription failedMonitoringConfigs(Map<String, FailedMonitoringConfiguration> failedMonitoringConfigs)
    {

        this.failedMonitoringConfigs = failedMonitoringConfigs;
        return this;
    }

    public CreatedEeSubscription putFailedMonitoringConfigsItem(String key,
                                                                FailedMonitoringConfiguration failedMonitoringConfigsItem)
    {
        if (this.failedMonitoringConfigs == null)
        {
            this.failedMonitoringConfigs = new HashMap<>();
        }
        this.failedMonitoringConfigs.put(key, failedMonitoringConfigsItem);
        return this;
    }

    /**
     * A map (list of key-value pairs where referenceId converted from integer to
     * string serves as key; see clause 6.4.6.3.2) of FailedMonitoringConfiguration
     * 
     * @return failedMonitoringConfigs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs where referenceId converted from integer to string serves as key; see clause 6.4.6.3.2) of FailedMonitoringConfiguration")
    @JsonProperty(JSON_PROPERTY_FAILED_MONITORING_CONFIGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, FailedMonitoringConfiguration> getFailedMonitoringConfigs()
    {
        return failedMonitoringConfigs;
    }

    @JsonProperty(JSON_PROPERTY_FAILED_MONITORING_CONFIGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFailedMonitoringConfigs(Map<String, FailedMonitoringConfiguration> failedMonitoringConfigs)
    {
        this.failedMonitoringConfigs = failedMonitoringConfigs;
    }

    public CreatedEeSubscription failedMoniConfigsEPC(Map<String, FailedMonitoringConfiguration> failedMoniConfigsEPC)
    {

        this.failedMoniConfigsEPC = failedMoniConfigsEPC;
        return this;
    }

    public CreatedEeSubscription putFailedMoniConfigsEPCItem(String key,
                                                             FailedMonitoringConfiguration failedMoniConfigsEPCItem)
    {
        if (this.failedMoniConfigsEPC == null)
        {
            this.failedMoniConfigsEPC = new HashMap<>();
        }
        this.failedMoniConfigsEPC.put(key, failedMoniConfigsEPCItem);
        return this;
    }

    /**
     * A map (list of key-value pairs where referenceId converted from integer to
     * string serves as key; see clause 6.4.6.3.2) of FailedMonitoringConfiguration,
     * the key value \&quot;ALL\&quot; may be used to identify a map entry which
     * contains the failed cause of the EE subscription was not successful in EPC
     * domain.
     * 
     * @return failedMoniConfigsEPC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs where referenceId converted from integer to string serves as key; see clause 6.4.6.3.2) of FailedMonitoringConfiguration, the key value \"ALL\" may be used to identify a map entry which contains the failed cause of the EE subscription was not successful in EPC domain.")
    @JsonProperty(JSON_PROPERTY_FAILED_MONI_CONFIGS_E_P_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, FailedMonitoringConfiguration> getFailedMoniConfigsEPC()
    {
        return failedMoniConfigsEPC;
    }

    @JsonProperty(JSON_PROPERTY_FAILED_MONI_CONFIGS_E_P_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFailedMoniConfigsEPC(Map<String, FailedMonitoringConfiguration> failedMoniConfigsEPC)
    {
        this.failedMoniConfigsEPC = failedMoniConfigsEPC;
    }

    public CreatedEeSubscription resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public CreatedEeSubscription addResetIdsItem(String resetIdsItem)
    {
        if (this.resetIds == null)
        {
            this.resetIds = new ArrayList<>();
        }
        this.resetIds.add(resetIdsItem);
        return this;
    }

    /**
     * Get resetIds
     * 
     * @return resetIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getResetIds()
    {
        return resetIds;
    }

    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetIds(List<String> resetIds)
    {
        this.resetIds = resetIds;
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
        CreatedEeSubscription createdEeSubscription = (CreatedEeSubscription) o;
        return Objects.equals(this.eeSubscription, createdEeSubscription.eeSubscription) && Objects.equals(this.numberOfUes, createdEeSubscription.numberOfUes)
               && Objects.equals(this.eventReports, createdEeSubscription.eventReports) && Objects.equals(this.epcStatusInd, createdEeSubscription.epcStatusInd)
               && Objects.equals(this.failedMonitoringConfigs, createdEeSubscription.failedMonitoringConfigs)
               && Objects.equals(this.failedMoniConfigsEPC, createdEeSubscription.failedMoniConfigsEPC)
               && Objects.equals(this.resetIds, createdEeSubscription.resetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eeSubscription, numberOfUes, eventReports, epcStatusInd, failedMonitoringConfigs, failedMoniConfigsEPC, resetIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreatedEeSubscription {\n");
        sb.append("    eeSubscription: ").append(toIndentedString(eeSubscription)).append("\n");
        sb.append("    numberOfUes: ").append(toIndentedString(numberOfUes)).append("\n");
        sb.append("    eventReports: ").append(toIndentedString(eventReports)).append("\n");
        sb.append("    epcStatusInd: ").append(toIndentedString(epcStatusInd)).append("\n");
        sb.append("    failedMonitoringConfigs: ").append(toIndentedString(failedMonitoringConfigs)).append("\n");
        sb.append("    failedMoniConfigsEPC: ").append(toIndentedString(failedMoniConfigsEPC)).append("\n");
        sb.append("    resetIds: ").append(toIndentedString(resetIds)).append("\n");
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
