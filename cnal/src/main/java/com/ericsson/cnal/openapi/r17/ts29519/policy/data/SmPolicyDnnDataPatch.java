/*
 * Unified Data Repository Service API file for policy data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.policy.data;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the SM policy data for a given DNN (and S-NSSAI).
 */
@ApiModel(description = "Contains the SM policy data for a given DNN (and S-NSSAI).")
@JsonPropertyOrder({ SmPolicyDnnDataPatch.JSON_PROPERTY_DNN, SmPolicyDnnDataPatch.JSON_PROPERTY_BDT_REF_IDS })
public class SmPolicyDnnDataPatch
{
    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_BDT_REF_IDS = "bdtRefIds";
    private JsonNullable<Map<String, String>> bdtRefIds = JsonNullable.<Map<String, String>>undefined();

    public SmPolicyDnnDataPatch()
    {
    }

    public SmPolicyDnnDataPatch dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;
     * it shall contain either a DNN Network Identifier, or a full DNN with both the
     * Network Identifier and Operator Identifier, as specified in 3GPP TS 23.003
     * clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are
     * separated by dots (e.g. \&quot;Label1.Label2.Label3\&quot;).
     * 
     * @return dnn
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public SmPolicyDnnDataPatch bdtRefIds(Map<String, String> bdtRefIds)
    {
        this.bdtRefIds = JsonNullable.<Map<String, String>>of(bdtRefIds);

        return this;
    }

    public SmPolicyDnnDataPatch putBdtRefIdsItem(String key,
                                                 String bdtRefIdsItem)
    {
        if (this.bdtRefIds == null || !this.bdtRefIds.isPresent())
        {
            this.bdtRefIds = JsonNullable.<Map<String, String>>of(new HashMap<>());
        }
        try
        {
            this.bdtRefIds.get().put(key, bdtRefIdsItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * Contains updated transfer policies of background data transfer. Any string
     * value can be used as a key of the map.
     * 
     * @return bdtRefIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains updated transfer policies of background data transfer. Any string value can be used as a key of the map. ")
    @JsonIgnore

    public Map<String, String> getBdtRefIds()
    {
        return bdtRefIds.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_BDT_REF_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Map<String, String>> getBdtRefIds_JsonNullable()
    {
        return bdtRefIds;
    }

    @JsonProperty(JSON_PROPERTY_BDT_REF_IDS)
    public void setBdtRefIds_JsonNullable(JsonNullable<Map<String, String>> bdtRefIds)
    {
        this.bdtRefIds = bdtRefIds;
    }

    public void setBdtRefIds(Map<String, String> bdtRefIds)
    {
        this.bdtRefIds = JsonNullable.<Map<String, String>>of(bdtRefIds);
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
        SmPolicyDnnDataPatch smPolicyDnnDataPatch = (SmPolicyDnnDataPatch) o;
        return Objects.equals(this.dnn, smPolicyDnnDataPatch.dnn) && equalsNullable(this.bdtRefIds, smPolicyDnnDataPatch.bdtRefIds);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dnn, hashCodeNullable(bdtRefIds));
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmPolicyDnnDataPatch {\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    bdtRefIds: ").append(toIndentedString(bdtRefIds)).append("\n");
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
