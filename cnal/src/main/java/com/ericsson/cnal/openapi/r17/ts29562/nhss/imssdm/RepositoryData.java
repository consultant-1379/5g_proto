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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Repository Data for the requested Service Indication
 */
@ApiModel(description = "Repository Data for the requested Service Indication")
@JsonPropertyOrder({ RepositoryData.JSON_PROPERTY_SEQUENCE_NUMBER, RepositoryData.JSON_PROPERTY_SERVICE_DATA })
public class RepositoryData
{
    public static final String JSON_PROPERTY_SEQUENCE_NUMBER = "sequenceNumber";
    private Integer sequenceNumber;

    public static final String JSON_PROPERTY_SERVICE_DATA = "serviceData";
    private byte[] serviceData;

    public RepositoryData()
    {
    }

    public RepositoryData sequenceNumber(Integer sequenceNumber)
    {

        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Unsigned integer containing the sequence number associated to the current
     * version of Repository Data minimum: 0
     * 
     * @return sequenceNumber
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Unsigned integer containing the sequence number associated to the current version of Repository Data ")
    @JsonProperty(JSON_PROPERTY_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getSequenceNumber()
    {
        return sequenceNumber;
    }

    @JsonProperty(JSON_PROPERTY_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSequenceNumber(Integer sequenceNumber)
    {
        this.sequenceNumber = sequenceNumber;
    }

    public RepositoryData serviceData(byte[] serviceData)
    {

        this.serviceData = serviceData;
        return this;
    }

    /**
     * Get serviceData
     * 
     * @return serviceData
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public byte[] getServiceData()
    {
        return serviceData;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setServiceData(byte[] serviceData)
    {
        this.serviceData = serviceData;
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
        RepositoryData repositoryData = (RepositoryData) o;
        return Objects.equals(this.sequenceNumber, repositoryData.sequenceNumber) && Arrays.equals(this.serviceData, repositoryData.serviceData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sequenceNumber, Arrays.hashCode(serviceData));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RepositoryData {\n");
        sb.append("    sequenceNumber: ").append(toIndentedString(sequenceNumber)).append("\n");
        sb.append("    serviceData: ").append(toIndentedString(serviceData)).append("\n");
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
