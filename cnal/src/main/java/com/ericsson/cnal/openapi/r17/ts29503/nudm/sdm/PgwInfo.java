/*
 * Nudm_SDM
 * Nudm Subscriber Data Management Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PgwInfo
 */
@JsonPropertyOrder({ PgwInfo.JSON_PROPERTY_DNN,
                     PgwInfo.JSON_PROPERTY_PGW_FQDN,
                     PgwInfo.JSON_PROPERTY_PGW_IP_ADDR,
                     PgwInfo.JSON_PROPERTY_PLMN_ID,
                     PgwInfo.JSON_PROPERTY_EPDG_IND,
                     PgwInfo.JSON_PROPERTY_PCF_ID,
                     PgwInfo.JSON_PROPERTY_REGISTRATION_TIME })
public class PgwInfo
{
    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_PGW_FQDN = "pgwFqdn";
    private String pgwFqdn;

    public static final String JSON_PROPERTY_PGW_IP_ADDR = "pgwIpAddr";
    private IpAddress pgwIpAddr;

    public static final String JSON_PROPERTY_PLMN_ID = "plmnId";
    private PlmnId plmnId;

    public static final String JSON_PROPERTY_EPDG_IND = "epdgInd";
    private Boolean epdgInd = false;

    public static final String JSON_PROPERTY_PCF_ID = "pcfId";
    private UUID pcfId;

    public static final String JSON_PROPERTY_REGISTRATION_TIME = "registrationTime";
    private OffsetDateTime registrationTime;

    public PgwInfo()
    {
    }

    public PgwInfo dnn(String dnn)
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

    public PgwInfo pgwFqdn(String pgwFqdn)
    {

        this.pgwFqdn = pgwFqdn;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pgwFqdn
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PGW_FQDN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getPgwFqdn()
    {
        return pgwFqdn;
    }

    @JsonProperty(JSON_PROPERTY_PGW_FQDN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPgwFqdn(String pgwFqdn)
    {
        this.pgwFqdn = pgwFqdn;
    }

    public PgwInfo pgwIpAddr(IpAddress pgwIpAddr)
    {

        this.pgwIpAddr = pgwIpAddr;
        return this;
    }

    /**
     * Get pgwIpAddr
     * 
     * @return pgwIpAddr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PGW_IP_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IpAddress getPgwIpAddr()
    {
        return pgwIpAddr;
    }

    @JsonProperty(JSON_PROPERTY_PGW_IP_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPgwIpAddr(IpAddress pgwIpAddr)
    {
        this.pgwIpAddr = pgwIpAddr;
    }

    public PgwInfo plmnId(PlmnId plmnId)
    {

        this.plmnId = plmnId;
        return this;
    }

    /**
     * Get plmnId
     * 
     * @return plmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getPlmnId()
    {
        return plmnId;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnId(PlmnId plmnId)
    {
        this.plmnId = plmnId;
    }

    public PgwInfo epdgInd(Boolean epdgInd)
    {

        this.epdgInd = epdgInd;
        return this;
    }

    /**
     * Get epdgInd
     * 
     * @return epdgInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EPDG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEpdgInd()
    {
        return epdgInd;
    }

    @JsonProperty(JSON_PROPERTY_EPDG_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpdgInd(Boolean epdgInd)
    {
        this.epdgInd = epdgInd;
    }

    public PgwInfo pcfId(UUID pcfId)
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

    public PgwInfo registrationTime(OffsetDateTime registrationTime)
    {

        this.registrationTime = registrationTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return registrationTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_REGISTRATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRegistrationTime()
    {
        return registrationTime;
    }

    @JsonProperty(JSON_PROPERTY_REGISTRATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRegistrationTime(OffsetDateTime registrationTime)
    {
        this.registrationTime = registrationTime;
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
        PgwInfo pgwInfo = (PgwInfo) o;
        return Objects.equals(this.dnn, pgwInfo.dnn) && Objects.equals(this.pgwFqdn, pgwInfo.pgwFqdn) && Objects.equals(this.pgwIpAddr, pgwInfo.pgwIpAddr)
               && Objects.equals(this.plmnId, pgwInfo.plmnId) && Objects.equals(this.epdgInd, pgwInfo.epdgInd) && Objects.equals(this.pcfId, pgwInfo.pcfId)
               && Objects.equals(this.registrationTime, pgwInfo.registrationTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dnn, pgwFqdn, pgwIpAddr, plmnId, epdgInd, pcfId, registrationTime);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PgwInfo {\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    pgwFqdn: ").append(toIndentedString(pgwFqdn)).append("\n");
        sb.append("    pgwIpAddr: ").append(toIndentedString(pgwIpAddr)).append("\n");
        sb.append("    plmnId: ").append(toIndentedString(plmnId)).append("\n");
        sb.append("    epdgInd: ").append(toIndentedString(epdgInd)).append("\n");
        sb.append("    pcfId: ").append(toIndentedString(pcfId)).append("\n");
        sb.append("    registrationTime: ").append(toIndentedString(registrationTime)).append("\n");
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
