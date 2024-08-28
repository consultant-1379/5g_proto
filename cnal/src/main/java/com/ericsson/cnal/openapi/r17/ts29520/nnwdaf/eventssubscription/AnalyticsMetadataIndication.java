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
import com.ericsson.cnal.openapi.r17.ts29122.commondata.TimeWindow;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains analytics metadata information requested to be used during analytics
 * generation.
 */
@ApiModel(description = "Contains analytics metadata information requested to be used during analytics generation. ")
@JsonPropertyOrder({ AnalyticsMetadataIndication.JSON_PROPERTY_DATA_WINDOW,
                     AnalyticsMetadataIndication.JSON_PROPERTY_DATA_STAT_PROPS,
                     AnalyticsMetadataIndication.JSON_PROPERTY_STRATEGY,
                     AnalyticsMetadataIndication.JSON_PROPERTY_AGGR_NWDAF_IDS })
public class AnalyticsMetadataIndication
{
    public static final String JSON_PROPERTY_DATA_WINDOW = "dataWindow";
    private TimeWindow dataWindow;

    public static final String JSON_PROPERTY_DATA_STAT_PROPS = "dataStatProps";
    private List<String> dataStatProps = null;

    public static final String JSON_PROPERTY_STRATEGY = "strategy";
    private String strategy;

    public static final String JSON_PROPERTY_AGGR_NWDAF_IDS = "aggrNwdafIds";
    private List<UUID> aggrNwdafIds = null;

    public AnalyticsMetadataIndication()
    {
    }

    public AnalyticsMetadataIndication dataWindow(TimeWindow dataWindow)
    {

        this.dataWindow = dataWindow;
        return this;
    }

    /**
     * Get dataWindow
     * 
     * @return dataWindow
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DATA_WINDOW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TimeWindow getDataWindow()
    {
        return dataWindow;
    }

    @JsonProperty(JSON_PROPERTY_DATA_WINDOW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDataWindow(TimeWindow dataWindow)
    {
        this.dataWindow = dataWindow;
    }

    public AnalyticsMetadataIndication dataStatProps(List<String> dataStatProps)
    {

        this.dataStatProps = dataStatProps;
        return this;
    }

    public AnalyticsMetadataIndication addDataStatPropsItem(String dataStatPropsItem)
    {
        if (this.dataStatProps == null)
        {
            this.dataStatProps = new ArrayList<>();
        }
        this.dataStatProps.add(dataStatPropsItem);
        return this;
    }

    /**
     * Get dataStatProps
     * 
     * @return dataStatProps
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DATA_STAT_PROPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDataStatProps()
    {
        return dataStatProps;
    }

    @JsonProperty(JSON_PROPERTY_DATA_STAT_PROPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDataStatProps(List<String> dataStatProps)
    {
        this.dataStatProps = dataStatProps;
    }

    public AnalyticsMetadataIndication strategy(String strategy)
    {

        this.strategy = strategy;
        return this;
    }

    /**
     * Possible values are: - BINARY: Indicates that the analytics shall only be
     * reported when the requested level of accuracy is reached within a cycle of
     * periodic notification. - GRADIENT: Indicates that the analytics shall be
     * reported according with the periodicity irrespective of whether the requested
     * level of accuracy has been reached or not.
     * 
     * @return strategy
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - BINARY: Indicates that the analytics shall only be reported when the requested level of accuracy is reached within a cycle of periodic notification. - GRADIENT: Indicates that the analytics shall be reported according with the periodicity irrespective of whether the requested level of accuracy has been reached or not. ")
    @JsonProperty(JSON_PROPERTY_STRATEGY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getStrategy()
    {
        return strategy;
    }

    @JsonProperty(JSON_PROPERTY_STRATEGY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStrategy(String strategy)
    {
        this.strategy = strategy;
    }

    public AnalyticsMetadataIndication aggrNwdafIds(List<UUID> aggrNwdafIds)
    {

        this.aggrNwdafIds = aggrNwdafIds;
        return this;
    }

    public AnalyticsMetadataIndication addAggrNwdafIdsItem(UUID aggrNwdafIdsItem)
    {
        if (this.aggrNwdafIds == null)
        {
            this.aggrNwdafIds = new ArrayList<>();
        }
        this.aggrNwdafIds.add(aggrNwdafIdsItem);
        return this;
    }

    /**
     * Get aggrNwdafIds
     * 
     * @return aggrNwdafIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AGGR_NWDAF_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getAggrNwdafIds()
    {
        return aggrNwdafIds;
    }

    @JsonProperty(JSON_PROPERTY_AGGR_NWDAF_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAggrNwdafIds(List<UUID> aggrNwdafIds)
    {
        this.aggrNwdafIds = aggrNwdafIds;
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
        AnalyticsMetadataIndication analyticsMetadataIndication = (AnalyticsMetadataIndication) o;
        return Objects.equals(this.dataWindow, analyticsMetadataIndication.dataWindow)
               && Objects.equals(this.dataStatProps, analyticsMetadataIndication.dataStatProps)
               && Objects.equals(this.strategy, analyticsMetadataIndication.strategy)
               && Objects.equals(this.aggrNwdafIds, analyticsMetadataIndication.aggrNwdafIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dataWindow, dataStatProps, strategy, aggrNwdafIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalyticsMetadataIndication {\n");
        sb.append("    dataWindow: ").append(toIndentedString(dataWindow)).append("\n");
        sb.append("    dataStatProps: ").append(toIndentedString(dataStatProps)).append("\n");
        sb.append("    strategy: ").append(toIndentedString(strategy)).append("\n");
        sb.append("    aggrNwdafIds: ").append(toIndentedString(aggrNwdafIds)).append("\n");
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
