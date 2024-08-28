/*
 * Nudm_UECM
 * Nudm Context Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Contains attributes of SmfRegistration that can be modified using PATCH
 */
@ApiModel(description = "Contains attributes of SmfRegistration that can be modified using PATCH")
@JsonPropertyOrder({ SmfRegistrationModification.JSON_PROPERTY_SMF_INSTANCE_ID,
                     SmfRegistrationModification.JSON_PROPERTY_SMF_SET_ID,
                     SmfRegistrationModification.JSON_PROPERTY_PGW_FQDN })
public class SmfRegistrationModification
{
    public static final String JSON_PROPERTY_SMF_INSTANCE_ID = "smfInstanceId";
    private UUID smfInstanceId;

    public static final String JSON_PROPERTY_SMF_SET_ID = "smfSetId";
    private String smfSetId;

    public static final String JSON_PROPERTY_PGW_FQDN = "pgwFqdn";
    private Object pgwFqdn;

    public SmfRegistrationModification()
    {
    }

    public SmfRegistrationModification smfInstanceId(UUID smfInstanceId)
    {

        this.smfInstanceId = smfInstanceId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return smfInstanceId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_SMF_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getSmfInstanceId()
    {
        return smfInstanceId;
    }

    @JsonProperty(JSON_PROPERTY_SMF_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSmfInstanceId(UUID smfInstanceId)
    {
        this.smfInstanceId = smfInstanceId;
    }

    public SmfRegistrationModification smfSetId(String smfSetId)
    {

        this.smfSetId = smfSetId;
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
     * @return smfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_SMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSmfSetId()
    {
        return smfSetId;
    }

    @JsonProperty(JSON_PROPERTY_SMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmfSetId(String smfSetId)
    {
        this.smfSetId = smfSetId;
    }

    public SmfRegistrationModification pgwFqdn(Object pgwFqdn)
    {

        this.pgwFqdn = pgwFqdn;
        return this;
    }

    /**
     * Get pgwFqdn
     * 
     * @return pgwFqdn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PGW_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Object getPgwFqdn()
    {
        return pgwFqdn;
    }

    @JsonProperty(JSON_PROPERTY_PGW_FQDN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPgwFqdn(Object pgwFqdn)
    {
        this.pgwFqdn = pgwFqdn;
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
        SmfRegistrationModification smfRegistrationModification = (SmfRegistrationModification) o;
        return Objects.equals(this.smfInstanceId, smfRegistrationModification.smfInstanceId)
               && Objects.equals(this.smfSetId, smfRegistrationModification.smfSetId) && Objects.equals(this.pgwFqdn, smfRegistrationModification.pgwFqdn);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(smfInstanceId, smfSetId, pgwFqdn);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmfRegistrationModification {\n");
        sb.append("    smfInstanceId: ").append(toIndentedString(smfInstanceId)).append("\n");
        sb.append("    smfSetId: ").append(toIndentedString(smfSetId)).append("\n");
        sb.append("    pgwFqdn: ").append(toIndentedString(pgwFqdn)).append("\n");
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
