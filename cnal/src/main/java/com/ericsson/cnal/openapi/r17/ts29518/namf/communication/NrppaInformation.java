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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a NRPPa related N2 information data part
 */
@ApiModel(description = "Represents a NRPPa related N2 information data part")
@JsonPropertyOrder({ NrppaInformation.JSON_PROPERTY_NF_ID, NrppaInformation.JSON_PROPERTY_NRPPA_PDU, NrppaInformation.JSON_PROPERTY_SERVICE_INSTANCE_ID })
public class NrppaInformation
{
    public static final String JSON_PROPERTY_NF_ID = "nfId";
    private UUID nfId;

    public static final String JSON_PROPERTY_NRPPA_PDU = "nrppaPdu";
    private N2InfoContent nrppaPdu;

    public static final String JSON_PROPERTY_SERVICE_INSTANCE_ID = "serviceInstanceId";
    private String serviceInstanceId;

    public NrppaInformation()
    {
    }

    public NrppaInformation nfId(UUID nfId)
    {

        this.nfId = nfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return nfId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getNfId()
    {
        return nfId;
    }

    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNfId(UUID nfId)
    {
        this.nfId = nfId;
    }

    public NrppaInformation nrppaPdu(N2InfoContent nrppaPdu)
    {

        this.nrppaPdu = nrppaPdu;
        return this;
    }

    /**
     * Get nrppaPdu
     * 
     * @return nrppaPdu
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NRPPA_PDU)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public N2InfoContent getNrppaPdu()
    {
        return nrppaPdu;
    }

    @JsonProperty(JSON_PROPERTY_NRPPA_PDU)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNrppaPdu(N2InfoContent nrppaPdu)
    {
        this.nrppaPdu = nrppaPdu;
    }

    public NrppaInformation serviceInstanceId(String serviceInstanceId)
    {

        this.serviceInstanceId = serviceInstanceId;
        return this;
    }

    /**
     * Get serviceInstanceId
     * 
     * @return serviceInstanceId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVICE_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getServiceInstanceId()
    {
        return serviceInstanceId;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceInstanceId(String serviceInstanceId)
    {
        this.serviceInstanceId = serviceInstanceId;
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
        NrppaInformation nrppaInformation = (NrppaInformation) o;
        return Objects.equals(this.nfId, nrppaInformation.nfId) && Objects.equals(this.nrppaPdu, nrppaInformation.nrppaPdu)
               && Objects.equals(this.serviceInstanceId, nrppaInformation.serviceInstanceId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nfId, nrppaPdu, serviceInstanceId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NrppaInformation {\n");
        sb.append("    nfId: ").append(toIndentedString(nfId)).append("\n");
        sb.append("    nrppaPdu: ").append(toIndentedString(nrppaPdu)).append("\n");
        sb.append("    serviceInstanceId: ").append(toIndentedString(serviceInstanceId)).append("\n");
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
