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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within a N1/N2 Message Transfer Failure Notification request
 */
@ApiModel(description = "Data within a N1/N2 Message Transfer Failure Notification request")
@JsonPropertyOrder({ N1N2MsgTxfrFailureNotification.JSON_PROPERTY_CAUSE, N1N2MsgTxfrFailureNotification.JSON_PROPERTY_N1N2_MSG_DATA_URI })
public class N1N2MsgTxfrFailureNotification
{
    public static final String JSON_PROPERTY_CAUSE = "cause";
    private String cause;

    public static final String JSON_PROPERTY_N1N2_MSG_DATA_URI = "n1n2MsgDataUri";
    private String n1n2MsgDataUri;

    public N1N2MsgTxfrFailureNotification()
    {
    }

    public N1N2MsgTxfrFailureNotification cause(String cause)
    {

        this.cause = cause;
        return this;
    }

    /**
     * Enumeration for N1N2Message Transfer Cause
     * 
     * @return cause
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Enumeration for N1N2Message Transfer Cause")
    @JsonProperty(JSON_PROPERTY_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getCause()
    {
        return cause;
    }

    @JsonProperty(JSON_PROPERTY_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCause(String cause)
    {
        this.cause = cause;
    }

    public N1N2MsgTxfrFailureNotification n1n2MsgDataUri(String n1n2MsgDataUri)
    {

        this.n1n2MsgDataUri = n1n2MsgDataUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return n1n2MsgDataUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_N1N2_MSG_DATA_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN1n2MsgDataUri()
    {
        return n1n2MsgDataUri;
    }

    @JsonProperty(JSON_PROPERTY_N1N2_MSG_DATA_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN1n2MsgDataUri(String n1n2MsgDataUri)
    {
        this.n1n2MsgDataUri = n1n2MsgDataUri;
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
        N1N2MsgTxfrFailureNotification n1N2MsgTxfrFailureNotification = (N1N2MsgTxfrFailureNotification) o;
        return Objects.equals(this.cause, n1N2MsgTxfrFailureNotification.cause)
               && Objects.equals(this.n1n2MsgDataUri, n1N2MsgTxfrFailureNotification.n1n2MsgDataUri);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cause, n1n2MsgDataUri);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class N1N2MsgTxfrFailureNotification {\n");
        sb.append("    cause: ").append(toIndentedString(cause)).append("\n");
        sb.append("    n1n2MsgDataUri: ").append(toIndentedString(n1n2MsgDataUri)).append("\n");
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
