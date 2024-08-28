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
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.mlmodelprovision.MLModelAddr;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains information about an ML models.
 */
@ApiModel(description = "Contains information about an ML models.")
@JsonPropertyOrder({ MLModelInfo.JSON_PROPERTY_ML_FILE_ADDRS, MLModelInfo.JSON_PROPERTY_MODEL_PROV_ID, MLModelInfo.JSON_PROPERTY_MODEL_PROV_SET_ID })
public class MLModelInfo
{
    public static final String JSON_PROPERTY_ML_FILE_ADDRS = "mlFileAddrs";
    private List<MLModelAddr> mlFileAddrs = null;

    public static final String JSON_PROPERTY_MODEL_PROV_ID = "modelProvId";
    private UUID modelProvId;

    public static final String JSON_PROPERTY_MODEL_PROV_SET_ID = "modelProvSetId";
    private String modelProvSetId;

    public MLModelInfo()
    {
    }

    public MLModelInfo mlFileAddrs(List<MLModelAddr> mlFileAddrs)
    {

        this.mlFileAddrs = mlFileAddrs;
        return this;
    }

    public MLModelInfo addMlFileAddrsItem(MLModelAddr mlFileAddrsItem)
    {
        if (this.mlFileAddrs == null)
        {
            this.mlFileAddrs = new ArrayList<>();
        }
        this.mlFileAddrs.add(mlFileAddrsItem);
        return this;
    }

    /**
     * Get mlFileAddrs
     * 
     * @return mlFileAddrs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ML_FILE_ADDRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MLModelAddr> getMlFileAddrs()
    {
        return mlFileAddrs;
    }

    @JsonProperty(JSON_PROPERTY_ML_FILE_ADDRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMlFileAddrs(List<MLModelAddr> mlFileAddrs)
    {
        this.mlFileAddrs = mlFileAddrs;
    }

    public MLModelInfo modelProvId(UUID modelProvId)
    {

        this.modelProvId = modelProvId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return modelProvId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_MODEL_PROV_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getModelProvId()
    {
        return modelProvId;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_PROV_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelProvId(UUID modelProvId)
    {
        this.modelProvId = modelProvId;
    }

    public MLModelInfo modelProvSetId(String modelProvSetId)
    {

        this.modelProvSetId = modelProvSetId;
        return this;
    }

    /**
     * NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the
     * following string \&quot;set&lt;Set
     * ID&gt;.&lt;nftype&gt;set.5gc.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;, or
     * \&quot;set&lt;SetID&gt;.&lt;NFType&gt;set.5gc.nid&lt;NID&gt;.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;
     * with &lt;MCC&gt; encoded as defined in clause 5.4.2 (\&quot;Mcc\&quot; data
     * type definition) &lt;MNC&gt; encoding the Mobile Network Code part of the
     * PLMN, comprising 3 digits. If there are only 2 significant digits in the MNC,
     * one \&quot;0\&quot; digit shall be inserted at the left side to fill the 3
     * digits coding of MNC. Pattern: &#39;^[0-9]{3}$&#39; &lt;NFType&gt; encoded as
     * a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but with lower case
     * characters &lt;Set ID&gt; encoded as a string of characters consisting of
     * alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and
     * that shall end with either an alphabetic character or a digit.
     * 
     * @return modelProvSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_MODEL_PROV_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getModelProvSetId()
    {
        return modelProvSetId;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_PROV_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelProvSetId(String modelProvSetId)
    {
        this.modelProvSetId = modelProvSetId;
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
        MLModelInfo mlModelInfo = (MLModelInfo) o;
        return Objects.equals(this.mlFileAddrs, mlModelInfo.mlFileAddrs) && Objects.equals(this.modelProvId, mlModelInfo.modelProvId)
               && Objects.equals(this.modelProvSetId, mlModelInfo.modelProvSetId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mlFileAddrs, modelProvId, modelProvSetId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MLModelInfo {\n");
        sb.append("    mlFileAddrs: ").append(toIndentedString(mlFileAddrs)).append("\n");
        sb.append("    modelProvId: ").append(toIndentedString(modelProvId)).append("\n");
        sb.append("    modelProvSetId: ").append(toIndentedString(modelProvSetId)).append("\n");
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
