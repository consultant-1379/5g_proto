/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.NsiLoadLevelInfo;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.ServiceExperienceInfo;
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
 * NSPAContainerInformation
 */
@JsonPropertyOrder({ NSPAContainerInformation.JSON_PROPERTY_LATENCY,
                     NSPAContainerInformation.JSON_PROPERTY_THROUGHPUT,
                     NSPAContainerInformation.JSON_PROPERTY_MAXIMUM_PACKET_LOSS_RATE,
                     NSPAContainerInformation.JSON_PROPERTY_SERVICE_EXPERIENCE_STATISTICS_DATA,
                     NSPAContainerInformation.JSON_PROPERTY_THE_NUMBER_OF_P_D_U_SESSIONS,
                     NSPAContainerInformation.JSON_PROPERTY_THE_NUMBER_OF_REGISTERED_SUBSCRIBERS,
                     NSPAContainerInformation.JSON_PROPERTY_LOAD_LEVEL })
public class NSPAContainerInformation
{
    public static final String JSON_PROPERTY_LATENCY = "latency";
    private Integer latency;

    public static final String JSON_PROPERTY_THROUGHPUT = "throughput";
    private Throughput throughput;

    public static final String JSON_PROPERTY_MAXIMUM_PACKET_LOSS_RATE = "maximumPacketLossRate";
    private String maximumPacketLossRate;

    public static final String JSON_PROPERTY_SERVICE_EXPERIENCE_STATISTICS_DATA = "serviceExperienceStatisticsData";
    private ServiceExperienceInfo serviceExperienceStatisticsData;

    public static final String JSON_PROPERTY_THE_NUMBER_OF_P_D_U_SESSIONS = "theNumberOfPDUSessions";
    private Integer theNumberOfPDUSessions;

    public static final String JSON_PROPERTY_THE_NUMBER_OF_REGISTERED_SUBSCRIBERS = "theNumberOfRegisteredSubscribers";
    private Integer theNumberOfRegisteredSubscribers;

    public static final String JSON_PROPERTY_LOAD_LEVEL = "loadLevel";
    private NsiLoadLevelInfo loadLevel;

    public NSPAContainerInformation()
    {
    }

    public NSPAContainerInformation latency(Integer latency)
    {

        this.latency = latency;
        return this;
    }

    /**
     * Get latency
     * 
     * @return latency
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LATENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getLatency()
    {
        return latency;
    }

    @JsonProperty(JSON_PROPERTY_LATENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLatency(Integer latency)
    {
        this.latency = latency;
    }

    public NSPAContainerInformation throughput(Throughput throughput)
    {

        this.throughput = throughput;
        return this;
    }

    /**
     * Get throughput
     * 
     * @return throughput
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_THROUGHPUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Throughput getThroughput()
    {
        return throughput;
    }

    @JsonProperty(JSON_PROPERTY_THROUGHPUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThroughput(Throughput throughput)
    {
        this.throughput = throughput;
    }

    public NSPAContainerInformation maximumPacketLossRate(String maximumPacketLossRate)
    {

        this.maximumPacketLossRate = maximumPacketLossRate;
        return this;
    }

    /**
     * Get maximumPacketLossRate
     * 
     * @return maximumPacketLossRate
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MAXIMUM_PACKET_LOSS_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMaximumPacketLossRate()
    {
        return maximumPacketLossRate;
    }

    @JsonProperty(JSON_PROPERTY_MAXIMUM_PACKET_LOSS_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaximumPacketLossRate(String maximumPacketLossRate)
    {
        this.maximumPacketLossRate = maximumPacketLossRate;
    }

    public NSPAContainerInformation serviceExperienceStatisticsData(ServiceExperienceInfo serviceExperienceStatisticsData)
    {

        this.serviceExperienceStatisticsData = serviceExperienceStatisticsData;
        return this;
    }

    /**
     * Get serviceExperienceStatisticsData
     * 
     * @return serviceExperienceStatisticsData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_EXPERIENCE_STATISTICS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ServiceExperienceInfo getServiceExperienceStatisticsData()
    {
        return serviceExperienceStatisticsData;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_EXPERIENCE_STATISTICS_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceExperienceStatisticsData(ServiceExperienceInfo serviceExperienceStatisticsData)
    {
        this.serviceExperienceStatisticsData = serviceExperienceStatisticsData;
    }

    public NSPAContainerInformation theNumberOfPDUSessions(Integer theNumberOfPDUSessions)
    {

        this.theNumberOfPDUSessions = theNumberOfPDUSessions;
        return this;
    }

    /**
     * Get theNumberOfPDUSessions
     * 
     * @return theNumberOfPDUSessions
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_THE_NUMBER_OF_P_D_U_SESSIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTheNumberOfPDUSessions()
    {
        return theNumberOfPDUSessions;
    }

    @JsonProperty(JSON_PROPERTY_THE_NUMBER_OF_P_D_U_SESSIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTheNumberOfPDUSessions(Integer theNumberOfPDUSessions)
    {
        this.theNumberOfPDUSessions = theNumberOfPDUSessions;
    }

    public NSPAContainerInformation theNumberOfRegisteredSubscribers(Integer theNumberOfRegisteredSubscribers)
    {

        this.theNumberOfRegisteredSubscribers = theNumberOfRegisteredSubscribers;
        return this;
    }

    /**
     * Get theNumberOfRegisteredSubscribers
     * 
     * @return theNumberOfRegisteredSubscribers
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_THE_NUMBER_OF_REGISTERED_SUBSCRIBERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTheNumberOfRegisteredSubscribers()
    {
        return theNumberOfRegisteredSubscribers;
    }

    @JsonProperty(JSON_PROPERTY_THE_NUMBER_OF_REGISTERED_SUBSCRIBERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTheNumberOfRegisteredSubscribers(Integer theNumberOfRegisteredSubscribers)
    {
        this.theNumberOfRegisteredSubscribers = theNumberOfRegisteredSubscribers;
    }

    public NSPAContainerInformation loadLevel(NsiLoadLevelInfo loadLevel)
    {

        this.loadLevel = loadLevel;
        return this;
    }

    /**
     * Get loadLevel
     * 
     * @return loadLevel
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NsiLoadLevelInfo getLoadLevel()
    {
        return loadLevel;
    }

    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLoadLevel(NsiLoadLevelInfo loadLevel)
    {
        this.loadLevel = loadLevel;
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
        NSPAContainerInformation nsPAContainerInformation = (NSPAContainerInformation) o;
        return Objects.equals(this.latency, nsPAContainerInformation.latency) && Objects.equals(this.throughput, nsPAContainerInformation.throughput)
               && Objects.equals(this.maximumPacketLossRate, nsPAContainerInformation.maximumPacketLossRate)
               && Objects.equals(this.serviceExperienceStatisticsData, nsPAContainerInformation.serviceExperienceStatisticsData)
               && Objects.equals(this.theNumberOfPDUSessions, nsPAContainerInformation.theNumberOfPDUSessions)
               && Objects.equals(this.theNumberOfRegisteredSubscribers, nsPAContainerInformation.theNumberOfRegisteredSubscribers)
               && Objects.equals(this.loadLevel, nsPAContainerInformation.loadLevel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(latency,
                            throughput,
                            maximumPacketLossRate,
                            serviceExperienceStatisticsData,
                            theNumberOfPDUSessions,
                            theNumberOfRegisteredSubscribers,
                            loadLevel);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NSPAContainerInformation {\n");
        sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
        sb.append("    throughput: ").append(toIndentedString(throughput)).append("\n");
        sb.append("    maximumPacketLossRate: ").append(toIndentedString(maximumPacketLossRate)).append("\n");
        sb.append("    serviceExperienceStatisticsData: ").append(toIndentedString(serviceExperienceStatisticsData)).append("\n");
        sb.append("    theNumberOfPDUSessions: ").append(toIndentedString(theNumberOfPDUSessions)).append("\n");
        sb.append("    theNumberOfRegisteredSubscribers: ").append(toIndentedString(theNumberOfRegisteredSubscribers)).append("\n");
        sb.append("    loadLevel: ").append(toIndentedString(loadLevel)).append("\n");
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
