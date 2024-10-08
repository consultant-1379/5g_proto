/*
 * NudmUEAU
 * UDM UE Authentication Service. � 2020, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.nudm.ueau;

import com.ericsson.cnal.openapi.r17.commondata.*;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-01-02T18:14:13.280461+01:00[Europe/Berlin]")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "avType", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = AvEpsAka.class, name = "EPS_AKA"),
                @JsonSubTypes.Type(value = AvImsGbaEapAka.class, name = "EAP_AKA"),
                @JsonSubTypes.Type(value = AvImsGbaEapAka.class, name = "GBA_AKA"),
                @JsonSubTypes.Type(value = AvImsGbaEapAka.class, name = "IMS_AKA"),
                @JsonSubTypes.Type(value = AvEapAkaPrime.class, name = "EAP_AKA_PRIME"), })

public interface HssAuthenticationVector
{
    public String getAvType();
}
