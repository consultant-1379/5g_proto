/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

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
 * This data type is defined in the same way as the &#39;Ecgi&#39; data type,
 * but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the 'Ecgi' data type, but with the OpenAPI 'nullable: true' property. ")
@JsonPropertyOrder({ EcgiRm.JSON_PROPERTY_PLMN_ID, EcgiRm.JSON_PROPERTY_EUTRA_CELL_ID, EcgiRm.JSON_PROPERTY_NID })
public class EcgiRm
{
    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public static final String JSON_PROPERTY_EUTRA_CELL_ID = "eutraCellId";
    private String eutraCellId;

    public static final String JSON_PROPERTY_NID = "nid";
    private String nid;

    public EcgiRm()
    {
    }

    public EcgiRm plmnId(PlmnId plmnId)
    {

        this.plmnId = plmnId;
        return this;
    }

    /**
     * Get plmnId
     * 
     * @return plmnId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public PlmnId getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPlmnId(PlmnId plmnId)
    {
        this.plmnId = plmnId;
    }

    public EcgiRm eutraCellId(String eutraCellId)
    {

        this.eutraCellId = eutraCellId;
        return this;
    }

    /**
     * 28-bit string identifying an E-UTRA Cell Id as specified in clause 9.3.1.9 of
     * 3GPP TS 38.413, in hexadecimal representation. Each character in the string
     * shall take a value of \&quot;0\&quot; to \&quot;9\&quot;, \&quot;a\&quot; to
     * \&quot;f\&quot; or \&quot;A\&quot; to \&quot;F\&quot; and shall represent 4
     * bits. The most significant character representing the 4 most significant bits
     * of the Cell Id shall appear first in the string, and the character
     * representing the 4 least significant bit of the Cell Id shall appear last in
     * the string.
     * 
     * @return eutraCellId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "28-bit string identifying an E-UTRA Cell Id as specified in clause 9.3.1.9 of  3GPP TS 38.413, in hexadecimal representation. Each character in the string shall take a  value of \"0\" to \"9\", \"a\" to \"f\" or \"A\" to \"F\" and shall represent 4 bits. The most  significant character representing the 4 most significant bits of the Cell Id shall appear  first in the string, and the character representing the 4 least significant bit of the  Cell Id shall appear last in the string.  ")
    @JsonProperty(JSON_PROPERTY_EUTRA_CELL_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEutraCellId()
    {
        return eutraCellId;
    }

    @JsonProperty(JSON_PROPERTY_EUTRA_CELL_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEutraCellId(String eutraCellId)
    {
        this.eutraCellId = eutraCellId;
    }

    public EcgiRm nid(String nid)
    {

        this.nid = nid;
        return this;
    }

    /**
     * This represents the Network Identifier, which together with a PLMN ID is used
     * to identify an SNPN (see 3GPP TS 23.003 and 3GPP TS 23.501 clause 5.30.2.1).
     * 
     * @return nid
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This represents the Network Identifier, which together with a PLMN ID is used to identify an SNPN (see 3GPP TS 23.003 and 3GPP TS 23.501 clause 5.30.2.1).  ")
    @JsonProperty(JSON_PROPERTY_NID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNid()
    {
        return nid;
    }

    @JsonProperty(JSON_PROPERTY_NID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNid(String nid)
    {
        this.nid = nid;
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
        EcgiRm ecgiRm = (EcgiRm) o;
        return Objects.equals(this.plmnId, ecgiRm.plmnId) && Objects.equals(this.eutraCellId, ecgiRm.eutraCellId) && Objects.equals(this.nid, ecgiRm.nid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(plmnId, eutraCellId, nid);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EcgiRm {\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    eutraCellId: ").append(toIndentedString(eutraCellId)).append("\n");
        sb.append("    nid: ").append(toIndentedString(nid)).append("\n");
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
