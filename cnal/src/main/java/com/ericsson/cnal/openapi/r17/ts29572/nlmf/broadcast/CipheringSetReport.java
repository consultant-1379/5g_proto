/*
 * LMF Broadcast
 * LMF Broadcast Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29572.nlmf.broadcast;

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
 * Represents a report of Ciphering Data Set storage.
 */
@ApiModel(description = "Represents a report of Ciphering Data Set storage.")
@JsonPropertyOrder({ CipheringSetReport.JSON_PROPERTY_CIPHERING_SET_I_D, CipheringSetReport.JSON_PROPERTY_STORAGE_OUTCOME })
public class CipheringSetReport
{
    public static final String JSON_PROPERTY_CIPHERING_SET_I_D = "cipheringSetID";
    private Integer cipheringSetID;

    public static final String JSON_PROPERTY_STORAGE_OUTCOME = "storageOutcome";
    private String storageOutcome;

    public CipheringSetReport()
    {
    }

    public CipheringSetReport cipheringSetID(Integer cipheringSetID)
    {

        this.cipheringSetID = cipheringSetID;
        return this;
    }

    /**
     * Ciphering Data Set Identifier. minimum: 0 maximum: 65535
     * 
     * @return cipheringSetID
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Ciphering Data Set Identifier.")
    @JsonProperty(JSON_PROPERTY_CIPHERING_SET_I_D)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getCipheringSetID()
    {
        return cipheringSetID;
    }

    @JsonProperty(JSON_PROPERTY_CIPHERING_SET_I_D)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCipheringSetID(Integer cipheringSetID)
    {
        this.cipheringSetID = cipheringSetID;
    }

    public CipheringSetReport storageOutcome(String storageOutcome)
    {

        this.storageOutcome = storageOutcome;
        return this;
    }

    /**
     * Indicates the result of Ciphering Data Set storage.
     * 
     * @return storageOutcome
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the result of Ciphering Data Set storage.")
    @JsonProperty(JSON_PROPERTY_STORAGE_OUTCOME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getStorageOutcome()
    {
        return storageOutcome;
    }

    @JsonProperty(JSON_PROPERTY_STORAGE_OUTCOME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setStorageOutcome(String storageOutcome)
    {
        this.storageOutcome = storageOutcome;
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
        CipheringSetReport cipheringSetReport = (CipheringSetReport) o;
        return Objects.equals(this.cipheringSetID, cipheringSetReport.cipheringSetID) && Objects.equals(this.storageOutcome, cipheringSetReport.storageOutcome);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cipheringSetID, storageOutcome);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CipheringSetReport {\n");
        sb.append("    cipheringSetID: ").append(toIndentedString(cipheringSetID)).append("\n");
        sb.append("    storageOutcome: ").append(toIndentedString(storageOutcome)).append("\n");
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
