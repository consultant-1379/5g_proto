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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AreaScope;
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
 * Immediate MDT Configuration
 */
@ApiModel(description = "Immediate MDT Configuration")
@JsonPropertyOrder({ ImmediateMdtConf.JSON_PROPERTY_JOB_TYPE,
                     ImmediateMdtConf.JSON_PROPERTY_MEASUREMENT_LTE_LIST,
                     ImmediateMdtConf.JSON_PROPERTY_MEASUREMENT_NR_LIST,
                     ImmediateMdtConf.JSON_PROPERTY_REPORTING_TRIGGER_LIST,
                     ImmediateMdtConf.JSON_PROPERTY_REPORT_INTERVAL,
                     ImmediateMdtConf.JSON_PROPERTY_REPORT_INTERVAL_NR,
                     ImmediateMdtConf.JSON_PROPERTY_REPORT_AMOUNT,
                     ImmediateMdtConf.JSON_PROPERTY_EVENT_THRESHOLD_RSRP,
                     ImmediateMdtConf.JSON_PROPERTY_EVENT_THRESHOLD_RSRQ,
                     ImmediateMdtConf.JSON_PROPERTY_EVENT_THRESHOLD_RSRP_NR,
                     ImmediateMdtConf.JSON_PROPERTY_EVENT_THRESHOLD_RSRQ_NR,
                     ImmediateMdtConf.JSON_PROPERTY_COLLECTION_PERIOD_RMM_LTE,
                     ImmediateMdtConf.JSON_PROPERTY_COLLECTION_PERIOD_RMM_NR,
                     ImmediateMdtConf.JSON_PROPERTY_MEASUREMENT_PERIOD_LTE,
                     ImmediateMdtConf.JSON_PROPERTY_AREA_SCOPE,
                     ImmediateMdtConf.JSON_PROPERTY_POSITIONING_METHOD,
                     ImmediateMdtConf.JSON_PROPERTY_ADD_POSITIONING_METHOD_LIST,
                     ImmediateMdtConf.JSON_PROPERTY_MDT_ALLOWED_PLMN_ID_LIST,
                     ImmediateMdtConf.JSON_PROPERTY_SENSOR_MEASUREMENT_LIST })
public class ImmediateMdtConf
{
    public static final String JSON_PROPERTY_JOB_TYPE = "jobType";
    private String jobType;

    public static final String JSON_PROPERTY_MEASUREMENT_LTE_LIST = "measurementLteList";
    private List<String> measurementLteList = null;

    public static final String JSON_PROPERTY_MEASUREMENT_NR_LIST = "measurementNrList";
    private List<String> measurementNrList = null;

    public static final String JSON_PROPERTY_REPORTING_TRIGGER_LIST = "reportingTriggerList";
    private List<String> reportingTriggerList = null;

    public static final String JSON_PROPERTY_REPORT_INTERVAL = "reportInterval";
    private String reportInterval;

    public static final String JSON_PROPERTY_REPORT_INTERVAL_NR = "reportIntervalNr";
    private String reportIntervalNr;

    public static final String JSON_PROPERTY_REPORT_AMOUNT = "reportAmount";
    private String reportAmount;

    public static final String JSON_PROPERTY_EVENT_THRESHOLD_RSRP = "eventThresholdRsrp";
    private Integer eventThresholdRsrp;

    public static final String JSON_PROPERTY_EVENT_THRESHOLD_RSRQ = "eventThresholdRsrq";
    private Integer eventThresholdRsrq;

    public static final String JSON_PROPERTY_EVENT_THRESHOLD_RSRP_NR = "eventThresholdRsrpNr";
    private Integer eventThresholdRsrpNr;

    public static final String JSON_PROPERTY_EVENT_THRESHOLD_RSRQ_NR = "eventThresholdRsrqNr";
    private Integer eventThresholdRsrqNr;

    public static final String JSON_PROPERTY_COLLECTION_PERIOD_RMM_LTE = "collectionPeriodRmmLte";
    private String collectionPeriodRmmLte;

    public static final String JSON_PROPERTY_COLLECTION_PERIOD_RMM_NR = "collectionPeriodRmmNr";
    private String collectionPeriodRmmNr;

    public static final String JSON_PROPERTY_MEASUREMENT_PERIOD_LTE = "measurementPeriodLte";
    private String measurementPeriodLte;

    public static final String JSON_PROPERTY_AREA_SCOPE = "areaScope";
    private AreaScope areaScope;

    public static final String JSON_PROPERTY_POSITIONING_METHOD = "positioningMethod";
    private String positioningMethod;

    public static final String JSON_PROPERTY_ADD_POSITIONING_METHOD_LIST = "addPositioningMethodList";
    private List<String> addPositioningMethodList = null;

    public static final String JSON_PROPERTY_MDT_ALLOWED_PLMN_ID_LIST = "mdtAllowedPlmnIdList";
    private List<PlmnId> mdtAllowedPlmnIdList = null;

    public static final String JSON_PROPERTY_SENSOR_MEASUREMENT_LIST = "sensorMeasurementList";
    private List<String> sensorMeasurementList = null;

    public ImmediateMdtConf()
    {
    }

    public ImmediateMdtConf jobType(String jobType)
    {

        this.jobType = jobType;
        return this;
    }

    /**
     * The enumeration JobType defines Job Type in the trace. See 3GPP TS 32.422 for
     * further description of the values. It shall comply with the provisions
     * defined in table 5.6.3.3-1.
     * 
     * @return jobType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "The enumeration JobType defines Job Type in the trace. See 3GPP TS 32.422 for further  description of the values. It shall comply with the provisions defined in table 5.6.3.3-1. ")
    @JsonProperty(JSON_PROPERTY_JOB_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getJobType()
    {
        return jobType;
    }

    @JsonProperty(JSON_PROPERTY_JOB_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setJobType(String jobType)
    {
        this.jobType = jobType;
    }

    public ImmediateMdtConf measurementLteList(List<String> measurementLteList)
    {

        this.measurementLteList = measurementLteList;
        return this;
    }

    public ImmediateMdtConf addMeasurementLteListItem(String measurementLteListItem)
    {
        if (this.measurementLteList == null)
        {
            this.measurementLteList = new ArrayList<>();
        }
        this.measurementLteList.add(measurementLteListItem);
        return this;
    }

    /**
     * Get measurementLteList
     * 
     * @return measurementLteList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MEASUREMENT_LTE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMeasurementLteList()
    {
        return measurementLteList;
    }

    @JsonProperty(JSON_PROPERTY_MEASUREMENT_LTE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMeasurementLteList(List<String> measurementLteList)
    {
        this.measurementLteList = measurementLteList;
    }

    public ImmediateMdtConf measurementNrList(List<String> measurementNrList)
    {

        this.measurementNrList = measurementNrList;
        return this;
    }

    public ImmediateMdtConf addMeasurementNrListItem(String measurementNrListItem)
    {
        if (this.measurementNrList == null)
        {
            this.measurementNrList = new ArrayList<>();
        }
        this.measurementNrList.add(measurementNrListItem);
        return this;
    }

    /**
     * Get measurementNrList
     * 
     * @return measurementNrList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MEASUREMENT_NR_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMeasurementNrList()
    {
        return measurementNrList;
    }

    @JsonProperty(JSON_PROPERTY_MEASUREMENT_NR_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMeasurementNrList(List<String> measurementNrList)
    {
        this.measurementNrList = measurementNrList;
    }

    public ImmediateMdtConf reportingTriggerList(List<String> reportingTriggerList)
    {

        this.reportingTriggerList = reportingTriggerList;
        return this;
    }

    public ImmediateMdtConf addReportingTriggerListItem(String reportingTriggerListItem)
    {
        if (this.reportingTriggerList == null)
        {
            this.reportingTriggerList = new ArrayList<>();
        }
        this.reportingTriggerList.add(reportingTriggerListItem);
        return this;
    }

    /**
     * Get reportingTriggerList
     * 
     * @return reportingTriggerList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REPORTING_TRIGGER_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getReportingTriggerList()
    {
        return reportingTriggerList;
    }

    @JsonProperty(JSON_PROPERTY_REPORTING_TRIGGER_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportingTriggerList(List<String> reportingTriggerList)
    {
        this.reportingTriggerList = reportingTriggerList;
    }

    public ImmediateMdtConf reportInterval(String reportInterval)
    {

        this.reportInterval = reportInterval;
        return this;
    }

    /**
     * The enumeration ReportIntervalMdt defines Report Interval for MDT in the
     * trace. See 3GPP TS 32.422 for further description of the values. It shall
     * comply with the provisions defined in table 5.6.3.9-1.
     * 
     * @return reportInterval
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration ReportIntervalMdt defines Report Interval for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.9-1. ")
    @JsonProperty(JSON_PROPERTY_REPORT_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReportInterval()
    {
        return reportInterval;
    }

    @JsonProperty(JSON_PROPERTY_REPORT_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportInterval(String reportInterval)
    {
        this.reportInterval = reportInterval;
    }

    public ImmediateMdtConf reportIntervalNr(String reportIntervalNr)
    {

        this.reportIntervalNr = reportIntervalNr;
        return this;
    }

    /**
     * The enumeration ReportIntervalNrMdt defines Report Interval in NR for MDT in
     * the trace. See 3GPP TS 32.422 for further description of the values. It shall
     * comply with the provisions defined in table 5.6.3.17-1.
     * 
     * @return reportIntervalNr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration ReportIntervalNrMdt defines Report Interval in NR for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.17-1. ")
    @JsonProperty(JSON_PROPERTY_REPORT_INTERVAL_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReportIntervalNr()
    {
        return reportIntervalNr;
    }

    @JsonProperty(JSON_PROPERTY_REPORT_INTERVAL_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportIntervalNr(String reportIntervalNr)
    {
        this.reportIntervalNr = reportIntervalNr;
    }

    public ImmediateMdtConf reportAmount(String reportAmount)
    {

        this.reportAmount = reportAmount;
        return this;
    }

    /**
     * The enumeration ReportAmountMdt defines Report Amount for MDT in the trace.
     * See 3GPP TS 32.422 for further description of the values. It shall comply
     * with the provisions defined in table 5.6.3.10-1.
     * 
     * @return reportAmount
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration ReportAmountMdt defines Report Amount for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.10-1. ")
    @JsonProperty(JSON_PROPERTY_REPORT_AMOUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReportAmount()
    {
        return reportAmount;
    }

    @JsonProperty(JSON_PROPERTY_REPORT_AMOUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportAmount(String reportAmount)
    {
        this.reportAmount = reportAmount;
    }

    public ImmediateMdtConf eventThresholdRsrp(Integer eventThresholdRsrp)
    {

        this.eventThresholdRsrp = eventThresholdRsrp;
        return this;
    }

    /**
     * Get eventThresholdRsrp minimum: 0 maximum: 97
     * 
     * @return eventThresholdRsrp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEventThresholdRsrp()
    {
        return eventThresholdRsrp;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventThresholdRsrp(Integer eventThresholdRsrp)
    {
        this.eventThresholdRsrp = eventThresholdRsrp;
    }

    public ImmediateMdtConf eventThresholdRsrq(Integer eventThresholdRsrq)
    {

        this.eventThresholdRsrq = eventThresholdRsrq;
        return this;
    }

    /**
     * Get eventThresholdRsrq minimum: 0 maximum: 34
     * 
     * @return eventThresholdRsrq
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEventThresholdRsrq()
    {
        return eventThresholdRsrq;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRQ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventThresholdRsrq(Integer eventThresholdRsrq)
    {
        this.eventThresholdRsrq = eventThresholdRsrq;
    }

    public ImmediateMdtConf eventThresholdRsrpNr(Integer eventThresholdRsrpNr)
    {

        this.eventThresholdRsrpNr = eventThresholdRsrpNr;
        return this;
    }

    /**
     * Get eventThresholdRsrpNr minimum: 0 maximum: 127
     * 
     * @return eventThresholdRsrpNr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRP_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEventThresholdRsrpNr()
    {
        return eventThresholdRsrpNr;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRP_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventThresholdRsrpNr(Integer eventThresholdRsrpNr)
    {
        this.eventThresholdRsrpNr = eventThresholdRsrpNr;
    }

    public ImmediateMdtConf eventThresholdRsrqNr(Integer eventThresholdRsrqNr)
    {

        this.eventThresholdRsrqNr = eventThresholdRsrqNr;
        return this;
    }

    /**
     * Get eventThresholdRsrqNr minimum: 0 maximum: 127
     * 
     * @return eventThresholdRsrqNr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRQ_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEventThresholdRsrqNr()
    {
        return eventThresholdRsrqNr;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_THRESHOLD_RSRQ_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventThresholdRsrqNr(Integer eventThresholdRsrqNr)
    {
        this.eventThresholdRsrqNr = eventThresholdRsrqNr;
    }

    public ImmediateMdtConf collectionPeriodRmmLte(String collectionPeriodRmmLte)
    {

        this.collectionPeriodRmmLte = collectionPeriodRmmLte;
        return this;
    }

    /**
     * The enumeration CollectionPeriodRmmLteMdt defines Collection period for RRM
     * measurements LTE for MDT in the trace. See 3GPP TS 32.422 for further
     * description of the values. It shall comply with the provisions defined in
     * table 5.6.3.15-1.
     * 
     * @return collectionPeriodRmmLte
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration CollectionPeriodRmmLteMdt defines Collection period for RRM measurements LTE for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.15-1. ")
    @JsonProperty(JSON_PROPERTY_COLLECTION_PERIOD_RMM_LTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCollectionPeriodRmmLte()
    {
        return collectionPeriodRmmLte;
    }

    @JsonProperty(JSON_PROPERTY_COLLECTION_PERIOD_RMM_LTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCollectionPeriodRmmLte(String collectionPeriodRmmLte)
    {
        this.collectionPeriodRmmLte = collectionPeriodRmmLte;
    }

    public ImmediateMdtConf collectionPeriodRmmNr(String collectionPeriodRmmNr)
    {

        this.collectionPeriodRmmNr = collectionPeriodRmmNr;
        return this;
    }

    /**
     * The enumeration CollectionPeriodRmmNrMdt defines Collection period for RRM
     * measurements NR for MDT in the trace. See 3GPP TS 32.422 for further
     * description of the values. It shall comply with the provisions defined in
     * table 5.6.3.19-1
     * 
     * @return collectionPeriodRmmNr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration CollectionPeriodRmmNrMdt defines Collection period for RRM measurements NR for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.19-1 ")
    @JsonProperty(JSON_PROPERTY_COLLECTION_PERIOD_RMM_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCollectionPeriodRmmNr()
    {
        return collectionPeriodRmmNr;
    }

    @JsonProperty(JSON_PROPERTY_COLLECTION_PERIOD_RMM_NR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCollectionPeriodRmmNr(String collectionPeriodRmmNr)
    {
        this.collectionPeriodRmmNr = collectionPeriodRmmNr;
    }

    public ImmediateMdtConf measurementPeriodLte(String measurementPeriodLte)
    {

        this.measurementPeriodLte = measurementPeriodLte;
        return this;
    }

    /**
     * The enumeration MeasurementPeriodLteMdt defines Measurement period LTE for
     * MDT in the trace. See 3GPP TS 32.422 for further description of the values.
     * It shall comply with the provisions defined in table 5.6.3.16-1.
     * 
     * @return measurementPeriodLte
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration MeasurementPeriodLteMdt defines Measurement period LTE for MDT in the trace.  See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.16-1. ")
    @JsonProperty(JSON_PROPERTY_MEASUREMENT_PERIOD_LTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMeasurementPeriodLte()
    {
        return measurementPeriodLte;
    }

    @JsonProperty(JSON_PROPERTY_MEASUREMENT_PERIOD_LTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMeasurementPeriodLte(String measurementPeriodLte)
    {
        this.measurementPeriodLte = measurementPeriodLte;
    }

    public ImmediateMdtConf areaScope(AreaScope areaScope)
    {

        this.areaScope = areaScope;
        return this;
    }

    /**
     * Get areaScope
     * 
     * @return areaScope
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AREA_SCOPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AreaScope getAreaScope()
    {
        return areaScope;
    }

    @JsonProperty(JSON_PROPERTY_AREA_SCOPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaScope(AreaScope areaScope)
    {
        this.areaScope = areaScope;
    }

    public ImmediateMdtConf positioningMethod(String positioningMethod)
    {

        this.positioningMethod = positioningMethod;
        return this;
    }

    /**
     * The enumeration LoggingDurationMdt defines Logging Duration for MDT in the
     * trace. See 3GPP TS 32.422 for further description of the values. It shall
     * comply with the provisions defined in table 5.6.3.13-1.
     * 
     * @return positioningMethod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The enumeration LoggingDurationMdt defines Logging Duration for MDT in the trace. See 3GPP TS 32.422 for further description of the values. It shall comply with the provisions defined in table 5.6.3.13-1. ")
    @JsonProperty(JSON_PROPERTY_POSITIONING_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPositioningMethod()
    {
        return positioningMethod;
    }

    @JsonProperty(JSON_PROPERTY_POSITIONING_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositioningMethod(String positioningMethod)
    {
        this.positioningMethod = positioningMethod;
    }

    public ImmediateMdtConf addPositioningMethodList(List<String> addPositioningMethodList)
    {

        this.addPositioningMethodList = addPositioningMethodList;
        return this;
    }

    public ImmediateMdtConf addAddPositioningMethodListItem(String addPositioningMethodListItem)
    {
        if (this.addPositioningMethodList == null)
        {
            this.addPositioningMethodList = new ArrayList<>();
        }
        this.addPositioningMethodList.add(addPositioningMethodListItem);
        return this;
    }

    /**
     * Get addPositioningMethodList
     * 
     * @return addPositioningMethodList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADD_POSITIONING_METHOD_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAddPositioningMethodList()
    {
        return addPositioningMethodList;
    }

    @JsonProperty(JSON_PROPERTY_ADD_POSITIONING_METHOD_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddPositioningMethodList(List<String> addPositioningMethodList)
    {
        this.addPositioningMethodList = addPositioningMethodList;
    }

    public ImmediateMdtConf mdtAllowedPlmnIdList(List<PlmnId> mdtAllowedPlmnIdList)
    {

        this.mdtAllowedPlmnIdList = mdtAllowedPlmnIdList;
        return this;
    }

    public ImmediateMdtConf addMdtAllowedPlmnIdListItem(PlmnId mdtAllowedPlmnIdListItem)
    {
        if (this.mdtAllowedPlmnIdList == null)
        {
            this.mdtAllowedPlmnIdList = new ArrayList<>();
        }
        this.mdtAllowedPlmnIdList.add(mdtAllowedPlmnIdListItem);
        return this;
    }

    /**
     * Get mdtAllowedPlmnIdList
     * 
     * @return mdtAllowedPlmnIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MDT_ALLOWED_PLMN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnId> getMdtAllowedPlmnIdList()
    {
        return mdtAllowedPlmnIdList;
    }

    @JsonProperty(JSON_PROPERTY_MDT_ALLOWED_PLMN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMdtAllowedPlmnIdList(List<PlmnId> mdtAllowedPlmnIdList)
    {
        this.mdtAllowedPlmnIdList = mdtAllowedPlmnIdList;
    }

    public ImmediateMdtConf sensorMeasurementList(List<String> sensorMeasurementList)
    {

        this.sensorMeasurementList = sensorMeasurementList;
        return this;
    }

    public ImmediateMdtConf addSensorMeasurementListItem(String sensorMeasurementListItem)
    {
        if (this.sensorMeasurementList == null)
        {
            this.sensorMeasurementList = new ArrayList<>();
        }
        this.sensorMeasurementList.add(sensorMeasurementListItem);
        return this;
    }

    /**
     * Get sensorMeasurementList
     * 
     * @return sensorMeasurementList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SENSOR_MEASUREMENT_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSensorMeasurementList()
    {
        return sensorMeasurementList;
    }

    @JsonProperty(JSON_PROPERTY_SENSOR_MEASUREMENT_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSensorMeasurementList(List<String> sensorMeasurementList)
    {
        this.sensorMeasurementList = sensorMeasurementList;
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
        ImmediateMdtConf immediateMdtConf = (ImmediateMdtConf) o;
        return Objects.equals(this.jobType, immediateMdtConf.jobType) && Objects.equals(this.measurementLteList, immediateMdtConf.measurementLteList)
               && Objects.equals(this.measurementNrList, immediateMdtConf.measurementNrList)
               && Objects.equals(this.reportingTriggerList, immediateMdtConf.reportingTriggerList)
               && Objects.equals(this.reportInterval, immediateMdtConf.reportInterval)
               && Objects.equals(this.reportIntervalNr, immediateMdtConf.reportIntervalNr) && Objects.equals(this.reportAmount, immediateMdtConf.reportAmount)
               && Objects.equals(this.eventThresholdRsrp, immediateMdtConf.eventThresholdRsrp)
               && Objects.equals(this.eventThresholdRsrq, immediateMdtConf.eventThresholdRsrq)
               && Objects.equals(this.eventThresholdRsrpNr, immediateMdtConf.eventThresholdRsrpNr)
               && Objects.equals(this.eventThresholdRsrqNr, immediateMdtConf.eventThresholdRsrqNr)
               && Objects.equals(this.collectionPeriodRmmLte, immediateMdtConf.collectionPeriodRmmLte)
               && Objects.equals(this.collectionPeriodRmmNr, immediateMdtConf.collectionPeriodRmmNr)
               && Objects.equals(this.measurementPeriodLte, immediateMdtConf.measurementPeriodLte) && Objects.equals(this.areaScope, immediateMdtConf.areaScope)
               && Objects.equals(this.positioningMethod, immediateMdtConf.positioningMethod)
               && Objects.equals(this.addPositioningMethodList, immediateMdtConf.addPositioningMethodList)
               && Objects.equals(this.mdtAllowedPlmnIdList, immediateMdtConf.mdtAllowedPlmnIdList)
               && Objects.equals(this.sensorMeasurementList, immediateMdtConf.sensorMeasurementList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jobType,
                            measurementLteList,
                            measurementNrList,
                            reportingTriggerList,
                            reportInterval,
                            reportIntervalNr,
                            reportAmount,
                            eventThresholdRsrp,
                            eventThresholdRsrq,
                            eventThresholdRsrpNr,
                            eventThresholdRsrqNr,
                            collectionPeriodRmmLte,
                            collectionPeriodRmmNr,
                            measurementPeriodLte,
                            areaScope,
                            positioningMethod,
                            addPositioningMethodList,
                            mdtAllowedPlmnIdList,
                            sensorMeasurementList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImmediateMdtConf {\n");
        sb.append("    jobType: ").append(toIndentedString(jobType)).append("\n");
        sb.append("    measurementLteList: ").append(toIndentedString(measurementLteList)).append("\n");
        sb.append("    measurementNrList: ").append(toIndentedString(measurementNrList)).append("\n");
        sb.append("    reportingTriggerList: ").append(toIndentedString(reportingTriggerList)).append("\n");
        sb.append("    reportInterval: ").append(toIndentedString(reportInterval)).append("\n");
        sb.append("    reportIntervalNr: ").append(toIndentedString(reportIntervalNr)).append("\n");
        sb.append("    reportAmount: ").append(toIndentedString(reportAmount)).append("\n");
        sb.append("    eventThresholdRsrp: ").append(toIndentedString(eventThresholdRsrp)).append("\n");
        sb.append("    eventThresholdRsrq: ").append(toIndentedString(eventThresholdRsrq)).append("\n");
        sb.append("    eventThresholdRsrpNr: ").append(toIndentedString(eventThresholdRsrpNr)).append("\n");
        sb.append("    eventThresholdRsrqNr: ").append(toIndentedString(eventThresholdRsrqNr)).append("\n");
        sb.append("    collectionPeriodRmmLte: ").append(toIndentedString(collectionPeriodRmmLte)).append("\n");
        sb.append("    collectionPeriodRmmNr: ").append(toIndentedString(collectionPeriodRmmNr)).append("\n");
        sb.append("    measurementPeriodLte: ").append(toIndentedString(measurementPeriodLte)).append("\n");
        sb.append("    areaScope: ").append(toIndentedString(areaScope)).append("\n");
        sb.append("    positioningMethod: ").append(toIndentedString(positioningMethod)).append("\n");
        sb.append("    addPositioningMethodList: ").append(toIndentedString(addPositioningMethodList)).append("\n");
        sb.append("    mdtAllowedPlmnIdList: ").append(toIndentedString(mdtAllowedPlmnIdList)).append("\n");
        sb.append("    sensorMeasurementList: ").append(toIndentedString(sensorMeasurementList)).append("\n");
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
