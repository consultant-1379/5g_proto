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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.RefToBinaryData;
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
 * N1 Message container
 */
@ApiModel(description = "N1 Message container")
@JsonPropertyOrder({ N1MessageContainer.JSON_PROPERTY_N1_MESSAGE_CLASS,
                     N1MessageContainer.JSON_PROPERTY_N1_MESSAGE_CONTENT,
                     N1MessageContainer.JSON_PROPERTY_NF_ID,
                     N1MessageContainer.JSON_PROPERTY_SERVICE_INSTANCE_ID })
public class N1MessageContainer
{
    public static final String JSON_PROPERTY_N1_MESSAGE_CLASS = "n1MessageClass";
    private String n1MessageClass;

    public static final String JSON_PROPERTY_N1_MESSAGE_CONTENT = "n1MessageContent";
    private RefToBinaryData n1MessageContent;

    public static final String JSON_PROPERTY_NF_ID = "nfId";
    private UUID nfId;

    public static final String JSON_PROPERTY_SERVICE_INSTANCE_ID = "serviceInstanceId";
    private String serviceInstanceId;

    public N1MessageContainer()
    {
    }

    public N1MessageContainer n1MessageClass(String n1MessageClass)
    {

        this.n1MessageClass = n1MessageClass;
        return this;
    }

    /**
     * Enumeration for N1 Message Class
     * 
     * @return n1MessageClass
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Enumeration for N1 Message Class")
    @JsonProperty(JSON_PROPERTY_N1_MESSAGE_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN1MessageClass()
    {
        return n1MessageClass;
    }

    @JsonProperty(JSON_PROPERTY_N1_MESSAGE_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN1MessageClass(String n1MessageClass)
    {
        this.n1MessageClass = n1MessageClass;
    }

    public N1MessageContainer n1MessageContent(RefToBinaryData n1MessageContent)
    {

        this.n1MessageContent = n1MessageContent;
        return this;
    }

    /**
     * Get n1MessageContent
     * 
     * @return n1MessageContent
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_N1_MESSAGE_CONTENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public RefToBinaryData getN1MessageContent()
    {
        return n1MessageContent;
    }

    @JsonProperty(JSON_PROPERTY_N1_MESSAGE_CONTENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN1MessageContent(RefToBinaryData n1MessageContent)
    {
        this.n1MessageContent = n1MessageContent;
    }

    public N1MessageContainer nfId(UUID nfId)
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
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getNfId()
    {
        return nfId;
    }

    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfId(UUID nfId)
    {
        this.nfId = nfId;
    }

    public N1MessageContainer serviceInstanceId(String serviceInstanceId)
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
        N1MessageContainer n1MessageContainer = (N1MessageContainer) o;
        return Objects.equals(this.n1MessageClass, n1MessageContainer.n1MessageClass)
               && Objects.equals(this.n1MessageContent, n1MessageContainer.n1MessageContent) && Objects.equals(this.nfId, n1MessageContainer.nfId)
               && Objects.equals(this.serviceInstanceId, n1MessageContainer.serviceInstanceId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(n1MessageClass, n1MessageContent, nfId, serviceInstanceId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class N1MessageContainer {\n");
        sb.append("    n1MessageClass: ").append(toIndentedString(n1MessageClass)).append("\n");
        sb.append("    n1MessageContent: ").append(toIndentedString(n1MessageContent)).append("\n");
        sb.append("    nfId: ").append(toIndentedString(nfId)).append("\n");
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
