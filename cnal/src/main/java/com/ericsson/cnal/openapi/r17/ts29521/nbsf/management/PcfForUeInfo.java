/*
 * Nbsf_Management
 * Binding Support Management Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.3.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29521.nbsf.management;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpEndPoint;
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
 * Contains the information of the PCF for a UE.
 */
@ApiModel(description = "Contains the information of the PCF for a UE.")
@JsonPropertyOrder({ PcfForUeInfo.JSON_PROPERTY_PCF_FQDN,
                     PcfForUeInfo.JSON_PROPERTY_PCF_IP_END_POINTS,
                     PcfForUeInfo.JSON_PROPERTY_PCF_ID,
                     PcfForUeInfo.JSON_PROPERTY_PCF_SET_ID,
                     PcfForUeInfo.JSON_PROPERTY_BIND_LEVEL })
public class PcfForUeInfo
{
    public static final String JSON_PROPERTY_PCF_FQDN = "pcfFqdn";
    private String pcfFqdn;

    public static final String JSON_PROPERTY_PCF_IP_END_POINTS = "pcfIpEndPoints";
    private List<IpEndPoint> pcfIpEndPoints = null;

    public static final String JSON_PROPERTY_PCF_ID = "pcfId";
    private UUID pcfId;

    public static final String JSON_PROPERTY_PCF_SET_ID = "pcfSetId";
    private String pcfSetId;

    public static final String JSON_PROPERTY_BIND_LEVEL = "bindLevel";
    private String bindLevel;

    public PcfForUeInfo()
    {
    }

    public PcfForUeInfo pcfFqdn(String pcfFqdn)
    {

        this.pcfFqdn = pcfFqdn;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pcfFqdn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PCF_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfFqdn()
    {
        return pcfFqdn;
    }

    @JsonProperty(JSON_PROPERTY_PCF_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfFqdn(String pcfFqdn)
    {
        this.pcfFqdn = pcfFqdn;
    }

    public PcfForUeInfo pcfIpEndPoints(List<IpEndPoint> pcfIpEndPoints)
    {

        this.pcfIpEndPoints = pcfIpEndPoints;
        return this;
    }

    public PcfForUeInfo addPcfIpEndPointsItem(IpEndPoint pcfIpEndPointsItem)
    {
        if (this.pcfIpEndPoints == null)
        {
            this.pcfIpEndPoints = new ArrayList<>();
        }
        this.pcfIpEndPoints.add(pcfIpEndPointsItem);
        return this;
    }

    /**
     * IP end points of the PCF hosting the Npcf_AmPolicyAuthorization service.
     * 
     * @return pcfIpEndPoints
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "IP end points of the PCF hosting the Npcf_AmPolicyAuthorization service.")
    @JsonProperty(JSON_PROPERTY_PCF_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IpEndPoint> getPcfIpEndPoints()
    {
        return pcfIpEndPoints;
    }

    @JsonProperty(JSON_PROPERTY_PCF_IP_END_POINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfIpEndPoints(List<IpEndPoint> pcfIpEndPoints)
    {
        this.pcfIpEndPoints = pcfIpEndPoints;
    }

    public PcfForUeInfo pcfId(UUID pcfId)
    {

        this.pcfId = pcfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return pcfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_PCF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPcfId()
    {
        return pcfId;
    }

    @JsonProperty(JSON_PROPERTY_PCF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfId(UUID pcfId)
    {
        this.pcfId = pcfId;
    }

    public PcfForUeInfo pcfSetId(String pcfSetId)
    {

        this.pcfSetId = pcfSetId;
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
     * @return pcfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_PCF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfSetId()
    {
        return pcfSetId;
    }

    @JsonProperty(JSON_PROPERTY_PCF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfSetId(String pcfSetId)
    {
        this.pcfSetId = pcfSetId;
    }

    public PcfForUeInfo bindLevel(String bindLevel)
    {

        this.bindLevel = bindLevel;
        return this;
    }

    /**
     * Possible values are: - \&quot;NF_SET\&quot; - \&quot;NF_INSTANCE\&quot;
     * 
     * @return bindLevel
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - \"NF_SET\" - \"NF_INSTANCE\" ")
    @JsonProperty(JSON_PROPERTY_BIND_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBindLevel()
    {
        return bindLevel;
    }

    @JsonProperty(JSON_PROPERTY_BIND_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBindLevel(String bindLevel)
    {
        this.bindLevel = bindLevel;
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
        PcfForUeInfo pcfForUeInfo = (PcfForUeInfo) o;
        return Objects.equals(this.pcfFqdn, pcfForUeInfo.pcfFqdn) && Objects.equals(this.pcfIpEndPoints, pcfForUeInfo.pcfIpEndPoints)
               && Objects.equals(this.pcfId, pcfForUeInfo.pcfId) && Objects.equals(this.pcfSetId, pcfForUeInfo.pcfSetId)
               && Objects.equals(this.bindLevel, pcfForUeInfo.bindLevel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pcfFqdn, pcfIpEndPoints, pcfId, pcfSetId, bindLevel);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PcfForUeInfo {\n");
        sb.append("    pcfFqdn: ").append(toIndentedString(pcfFqdn)).append("\n");
        sb.append("    pcfIpEndPoints: ").append(toIndentedString(pcfIpEndPoints)).append("\n");
        sb.append("    pcfId: ").append(toIndentedString(pcfId)).append("\n");
        sb.append("    pcfSetId: ").append(toIndentedString(pcfSetId)).append("\n");
        sb.append("    bindLevel: ").append(toIndentedString(bindLevel)).append("\n");
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
