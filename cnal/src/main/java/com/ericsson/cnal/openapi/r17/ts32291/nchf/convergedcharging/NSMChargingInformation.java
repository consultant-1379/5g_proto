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
 * NSMChargingInformation
 */
@JsonPropertyOrder({ NSMChargingInformation.JSON_PROPERTY_MANAGEMENT_OPERATION,
                     NSMChargingInformation.JSON_PROPERTY_ID_NETWORK_SLICE_INSTANCE,
                     NSMChargingInformation.JSON_PROPERTY_LIST_OFSERVICE_PROFILE_CHARGING_INFORMATION,
                     NSMChargingInformation.JSON_PROPERTY_MANAGEMENT_OPERATION_STATUS })
public class NSMChargingInformation
{
    public static final String JSON_PROPERTY_MANAGEMENT_OPERATION = "managementOperation";
    private String managementOperation;

    public static final String JSON_PROPERTY_ID_NETWORK_SLICE_INSTANCE = "idNetworkSliceInstance";
    private String idNetworkSliceInstance;

    public static final String JSON_PROPERTY_LIST_OFSERVICE_PROFILE_CHARGING_INFORMATION = "listOfserviceProfileChargingInformation";
    private List<ServiceProfileChargingInformation> listOfserviceProfileChargingInformation = null;

    public static final String JSON_PROPERTY_MANAGEMENT_OPERATION_STATUS = "managementOperationStatus";
    private String managementOperationStatus;

    public NSMChargingInformation()
    {
    }

    public NSMChargingInformation managementOperation(String managementOperation)
    {

        this.managementOperation = managementOperation;
        return this;
    }

    /**
     * Get managementOperation
     * 
     * @return managementOperation
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MANAGEMENT_OPERATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getManagementOperation()
    {
        return managementOperation;
    }

    @JsonProperty(JSON_PROPERTY_MANAGEMENT_OPERATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setManagementOperation(String managementOperation)
    {
        this.managementOperation = managementOperation;
    }

    public NSMChargingInformation idNetworkSliceInstance(String idNetworkSliceInstance)
    {

        this.idNetworkSliceInstance = idNetworkSliceInstance;
        return this;
    }

    /**
     * Get idNetworkSliceInstance
     * 
     * @return idNetworkSliceInstance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ID_NETWORK_SLICE_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIdNetworkSliceInstance()
    {
        return idNetworkSliceInstance;
    }

    @JsonProperty(JSON_PROPERTY_ID_NETWORK_SLICE_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIdNetworkSliceInstance(String idNetworkSliceInstance)
    {
        this.idNetworkSliceInstance = idNetworkSliceInstance;
    }

    public NSMChargingInformation listOfserviceProfileChargingInformation(List<ServiceProfileChargingInformation> listOfserviceProfileChargingInformation)
    {

        this.listOfserviceProfileChargingInformation = listOfserviceProfileChargingInformation;
        return this;
    }

    public NSMChargingInformation addListOfserviceProfileChargingInformationItem(ServiceProfileChargingInformation listOfserviceProfileChargingInformationItem)
    {
        if (this.listOfserviceProfileChargingInformation == null)
        {
            this.listOfserviceProfileChargingInformation = new ArrayList<>();
        }
        this.listOfserviceProfileChargingInformation.add(listOfserviceProfileChargingInformationItem);
        return this;
    }

    /**
     * Get listOfserviceProfileChargingInformation
     * 
     * @return listOfserviceProfileChargingInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LIST_OFSERVICE_PROFILE_CHARGING_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ServiceProfileChargingInformation> getListOfserviceProfileChargingInformation()
    {
        return listOfserviceProfileChargingInformation;
    }

    @JsonProperty(JSON_PROPERTY_LIST_OFSERVICE_PROFILE_CHARGING_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setListOfserviceProfileChargingInformation(List<ServiceProfileChargingInformation> listOfserviceProfileChargingInformation)
    {
        this.listOfserviceProfileChargingInformation = listOfserviceProfileChargingInformation;
    }

    public NSMChargingInformation managementOperationStatus(String managementOperationStatus)
    {

        this.managementOperationStatus = managementOperationStatus;
        return this;
    }

    /**
     * Get managementOperationStatus
     * 
     * @return managementOperationStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MANAGEMENT_OPERATION_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getManagementOperationStatus()
    {
        return managementOperationStatus;
    }

    @JsonProperty(JSON_PROPERTY_MANAGEMENT_OPERATION_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setManagementOperationStatus(String managementOperationStatus)
    {
        this.managementOperationStatus = managementOperationStatus;
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
        NSMChargingInformation nsMChargingInformation = (NSMChargingInformation) o;
        return Objects.equals(this.managementOperation, nsMChargingInformation.managementOperation)
               && Objects.equals(this.idNetworkSliceInstance, nsMChargingInformation.idNetworkSliceInstance)
               && Objects.equals(this.listOfserviceProfileChargingInformation, nsMChargingInformation.listOfserviceProfileChargingInformation)
               && Objects.equals(this.managementOperationStatus, nsMChargingInformation.managementOperationStatus);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(managementOperation, idNetworkSliceInstance, listOfserviceProfileChargingInformation, managementOperationStatus);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NSMChargingInformation {\n");
        sb.append("    managementOperation: ").append(toIndentedString(managementOperation)).append("\n");
        sb.append("    idNetworkSliceInstance: ").append(toIndentedString(idNetworkSliceInstance)).append("\n");
        sb.append("    listOfserviceProfileChargingInformation: ").append(toIndentedString(listOfserviceProfileChargingInformation)).append("\n");
        sb.append("    managementOperationStatus: ").append(toIndentedString(managementOperationStatus)).append("\n");
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
