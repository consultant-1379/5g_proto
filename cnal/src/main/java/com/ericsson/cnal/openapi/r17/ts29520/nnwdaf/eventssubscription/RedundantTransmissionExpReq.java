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
 * Represents other redundant transmission experience analytics requirements.
 */
@ApiModel(description = "Represents other redundant transmission experience analytics requirements.")
@JsonPropertyOrder({ RedundantTransmissionExpReq.JSON_PROPERTY_RED_T_ORDER_CRITER, RedundantTransmissionExpReq.JSON_PROPERTY_ORDER })
public class RedundantTransmissionExpReq
{
    public static final String JSON_PROPERTY_RED_T_ORDER_CRITER = "redTOrderCriter";
    private String redTOrderCriter;

    public static final String JSON_PROPERTY_ORDER = "order";
    private String order;

    public RedundantTransmissionExpReq()
    {
    }

    public RedundantTransmissionExpReq redTOrderCriter(String redTOrderCriter)
    {

        this.redTOrderCriter = redTOrderCriter;
        return this;
    }

    /**
     * Possible values are: - TIME_SLOT_START: Indicates the order of time slot
     * start. - RED_TRANS_EXP: Indicates the order of Redundant Transmission
     * Experience.
     * 
     * @return redTOrderCriter
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - TIME_SLOT_START: Indicates the order of time slot start. - RED_TRANS_EXP: Indicates the order of Redundant Transmission Experience. ")
    @JsonProperty(JSON_PROPERTY_RED_T_ORDER_CRITER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRedTOrderCriter()
    {
        return redTOrderCriter;
    }

    @JsonProperty(JSON_PROPERTY_RED_T_ORDER_CRITER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRedTOrderCriter(String redTOrderCriter)
    {
        this.redTOrderCriter = redTOrderCriter;
    }

    public RedundantTransmissionExpReq order(String order)
    {

        this.order = order;
        return this;
    }

    /**
     * Possible values are: - ASCENDING: Threshold is crossed in ascending
     * direction. - DESCENDING: Threshold is crossed in descending direction. -
     * CROSSED: Threshold is crossed either in ascending or descending direction.
     * 
     * @return order
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - ASCENDING: Threshold is crossed in ascending direction. - DESCENDING: Threshold is crossed in descending direction. - CROSSED: Threshold is crossed either in ascending or descending direction. ")
    @JsonProperty(JSON_PROPERTY_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOrder()
    {
        return order;
    }

    @JsonProperty(JSON_PROPERTY_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOrder(String order)
    {
        this.order = order;
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
        RedundantTransmissionExpReq redundantTransmissionExpReq = (RedundantTransmissionExpReq) o;
        return Objects.equals(this.redTOrderCriter, redundantTransmissionExpReq.redTOrderCriter)
               && Objects.equals(this.order, redundantTransmissionExpReq.order);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(redTOrderCriter, order);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class RedundantTransmissionExpReq {\n");
        sb.append("    redTOrderCriter: ").append(toIndentedString(redTOrderCriter)).append("\n");
        sb.append("    order: ").append(toIndentedString(order)).append("\n");
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
