/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

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
 * Security Result
 */
@ApiModel(description = "Security Result")
@JsonPropertyOrder({ SecurityResult.JSON_PROPERTY_INTEGRITY_PROTECTION_RESULT, SecurityResult.JSON_PROPERTY_CONFIDENTIALITY_PROTECTION_RESULT })
public class SecurityResult
{
    public static final String JSON_PROPERTY_INTEGRITY_PROTECTION_RESULT = "integrityProtectionResult";
    private String integrityProtectionResult;

    public static final String JSON_PROPERTY_CONFIDENTIALITY_PROTECTION_RESULT = "confidentialityProtectionResult";
    private String confidentialityProtectionResult;

    public SecurityResult()
    {
    }

    public SecurityResult integrityProtectionResult(String integrityProtectionResult)
    {

        this.integrityProtectionResult = integrityProtectionResult;
        return this;
    }

    /**
     * Protection Result of the security policy indicated as
     * \&quot;preferred\&quot;. Possible values are - PERFORMED - NOT_PERFORMED
     * 
     * @return integrityProtectionResult
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Protection Result of the security policy indicated as \"preferred\". Possible values are   - PERFORMED   - NOT_PERFORMED ")
    @JsonProperty(JSON_PROPERTY_INTEGRITY_PROTECTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIntegrityProtectionResult()
    {
        return integrityProtectionResult;
    }

    @JsonProperty(JSON_PROPERTY_INTEGRITY_PROTECTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntegrityProtectionResult(String integrityProtectionResult)
    {
        this.integrityProtectionResult = integrityProtectionResult;
    }

    public SecurityResult confidentialityProtectionResult(String confidentialityProtectionResult)
    {

        this.confidentialityProtectionResult = confidentialityProtectionResult;
        return this;
    }

    /**
     * Protection Result of the security policy indicated as
     * \&quot;preferred\&quot;. Possible values are - PERFORMED - NOT_PERFORMED
     * 
     * @return confidentialityProtectionResult
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Protection Result of the security policy indicated as \"preferred\". Possible values are   - PERFORMED   - NOT_PERFORMED ")
    @JsonProperty(JSON_PROPERTY_CONFIDENTIALITY_PROTECTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getConfidentialityProtectionResult()
    {
        return confidentialityProtectionResult;
    }

    @JsonProperty(JSON_PROPERTY_CONFIDENTIALITY_PROTECTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConfidentialityProtectionResult(String confidentialityProtectionResult)
    {
        this.confidentialityProtectionResult = confidentialityProtectionResult;
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
        SecurityResult securityResult = (SecurityResult) o;
        return Objects.equals(this.integrityProtectionResult, securityResult.integrityProtectionResult)
               && Objects.equals(this.confidentialityProtectionResult, securityResult.confidentialityProtectionResult);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(integrityProtectionResult, confidentialityProtectionResult);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SecurityResult {\n");
        sb.append("    integrityProtectionResult: ").append(toIndentedString(integrityProtectionResult)).append("\n");
        sb.append("    confidentialityProtectionResult: ").append(toIndentedString(confidentialityProtectionResult)).append("\n");
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
