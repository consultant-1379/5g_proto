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
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Identifies an Individual PCF binding used in an HTTP Patch method.
 */
@ApiModel(description = "Identifies an Individual PCF binding used in an HTTP Patch method.")
@JsonPropertyOrder({ PcfBindingPatch.JSON_PROPERTY_IPV4_ADDR,
                     PcfBindingPatch.JSON_PROPERTY_IP_DOMAIN,
                     PcfBindingPatch.JSON_PROPERTY_IPV6_PREFIX,
                     PcfBindingPatch.JSON_PROPERTY_ADD_IPV6_PREFIXES,
                     PcfBindingPatch.JSON_PROPERTY_MAC_ADDR48,
                     PcfBindingPatch.JSON_PROPERTY_ADD_MAC_ADDRS,
                     PcfBindingPatch.JSON_PROPERTY_PCF_ID,
                     PcfBindingPatch.JSON_PROPERTY_PCF_FQDN,
                     PcfBindingPatch.JSON_PROPERTY_PCF_IP_END_POINTS,
                     PcfBindingPatch.JSON_PROPERTY_PCF_DIAM_HOST,
                     PcfBindingPatch.JSON_PROPERTY_PCF_DIAM_REALM })
public class PcfBindingPatch
{
    public static final String JSON_PROPERTY_IPV4_ADDR = "ipv4Addr";
    private JsonNullable<String> ipv4Addr = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_IP_DOMAIN = "ipDomain";
    private JsonNullable<String> ipDomain = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_IPV6_PREFIX = "ipv6Prefix";
    private JsonNullable<String> ipv6Prefix = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_ADD_IPV6_PREFIXES = "addIpv6Prefixes";
    private JsonNullable<List<String>> addIpv6Prefixes = JsonNullable.<List<String>>undefined();

    public static final String JSON_PROPERTY_MAC_ADDR48 = "macAddr48";
    private JsonNullable<String> macAddr48 = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_ADD_MAC_ADDRS = "addMacAddrs";
    private JsonNullable<List<String>> addMacAddrs = JsonNullable.<List<String>>undefined();

    public static final String JSON_PROPERTY_PCF_ID = "pcfId";
    private UUID pcfId;

    public static final String JSON_PROPERTY_PCF_FQDN = "pcfFqdn";
    private String pcfFqdn;

    public static final String JSON_PROPERTY_PCF_IP_END_POINTS = "pcfIpEndPoints";
    private List<IpEndPoint> pcfIpEndPoints = null;

    public static final String JSON_PROPERTY_PCF_DIAM_HOST = "pcfDiamHost";
    private String pcfDiamHost;

    public static final String JSON_PROPERTY_PCF_DIAM_REALM = "pcfDiamRealm";
    private String pcfDiamRealm;

    public PcfBindingPatch()
    {
    }

    public PcfBindingPatch ipv4Addr(String ipv4Addr)
    {
        this.ipv4Addr = JsonNullable.<String>of(ipv4Addr);

        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166 with the OpenAPI defined &#39;nullable:
     * true&#39; property.
     * 
     * @return ipv4Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1",
                      value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166 with the OpenAPI defined 'nullable: true' property. ")
    @JsonIgnore

    public String getIpv4Addr()
    {
        return ipv4Addr.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getIpv4Addr_JsonNullable()
    {
        return ipv4Addr;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    public void setIpv4Addr_JsonNullable(JsonNullable<String> ipv4Addr)
    {
        this.ipv4Addr = ipv4Addr;
    }

    public void setIpv4Addr(String ipv4Addr)
    {
        this.ipv4Addr = JsonNullable.<String>of(ipv4Addr);
    }

    public PcfBindingPatch ipDomain(String ipDomain)
    {
        this.ipDomain = JsonNullable.<String>of(ipDomain);

        return this;
    }

    /**
     * Get ipDomain
     * 
     * @return ipDomain
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public String getIpDomain()
    {
        return ipDomain.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_IP_DOMAIN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getIpDomain_JsonNullable()
    {
        return ipDomain;
    }

    @JsonProperty(JSON_PROPERTY_IP_DOMAIN)
    public void setIpDomain_JsonNullable(JsonNullable<String> ipDomain)
    {
        this.ipDomain = ipDomain;
    }

    public void setIpDomain(String ipDomain)
    {
        this.ipDomain = JsonNullable.<String>of(ipDomain);
    }

    public PcfBindingPatch ipv6Prefix(String ipv6Prefix)
    {
        this.ipv6Prefix = JsonNullable.<String>of(ipv6Prefix);

        return this;
    }

    /**
     * Get ipv6Prefix
     * 
     * @return ipv6Prefix
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public String getIpv6Prefix()
    {
        return ipv6Prefix.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_IPV6_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getIpv6Prefix_JsonNullable()
    {
        return ipv6Prefix;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_PREFIX)
    public void setIpv6Prefix_JsonNullable(JsonNullable<String> ipv6Prefix)
    {
        this.ipv6Prefix = ipv6Prefix;
    }

    public void setIpv6Prefix(String ipv6Prefix)
    {
        this.ipv6Prefix = JsonNullable.<String>of(ipv6Prefix);
    }

    public PcfBindingPatch addIpv6Prefixes(List<String> addIpv6Prefixes)
    {
        this.addIpv6Prefixes = JsonNullable.<List<String>>of(addIpv6Prefixes);

        return this;
    }

    public PcfBindingPatch addAddIpv6PrefixesItem(String addIpv6PrefixesItem)
    {
        if (this.addIpv6Prefixes == null || !this.addIpv6Prefixes.isPresent())
        {
            this.addIpv6Prefixes = JsonNullable.<List<String>>of(new ArrayList<>());
        }
        try
        {
            this.addIpv6Prefixes.get().add(addIpv6PrefixesItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * The additional IPv6 Address Prefixes of the served UE.
     * 
     * @return addIpv6Prefixes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The additional IPv6 Address Prefixes of the served UE.")
    @JsonIgnore

    public List<String> getAddIpv6Prefixes()
    {
        return addIpv6Prefixes.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_ADD_IPV6_PREFIXES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<String>> getAddIpv6Prefixes_JsonNullable()
    {
        return addIpv6Prefixes;
    }

    @JsonProperty(JSON_PROPERTY_ADD_IPV6_PREFIXES)
    public void setAddIpv6Prefixes_JsonNullable(JsonNullable<List<String>> addIpv6Prefixes)
    {
        this.addIpv6Prefixes = addIpv6Prefixes;
    }

    public void setAddIpv6Prefixes(List<String> addIpv6Prefixes)
    {
        this.addIpv6Prefixes = JsonNullable.<List<String>>of(addIpv6Prefixes);
    }

    public PcfBindingPatch macAddr48(String macAddr48)
    {
        this.macAddr48 = JsonNullable.<String>of(macAddr48);

        return this;
    }

    /**
     * \&quot;String identifying a MAC address formatted in the hexadecimal notation
     * according to clause 1.1 and clause 2.1 of RFC 7042 with the OpenAPI
     * &#39;nullable: true&#39; property.\&quot;
     * 
     * @return macAddr48
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "\"String identifying a MAC address formatted in the hexadecimal notation according to clause 1.1 and clause 2.1 of RFC 7042 with the OpenAPI 'nullable: true' property.\" ")
    @JsonIgnore

    public String getMacAddr48()
    {
        return macAddr48.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_MAC_ADDR48)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getMacAddr48_JsonNullable()
    {
        return macAddr48;
    }

    @JsonProperty(JSON_PROPERTY_MAC_ADDR48)
    public void setMacAddr48_JsonNullable(JsonNullable<String> macAddr48)
    {
        this.macAddr48 = macAddr48;
    }

    public void setMacAddr48(String macAddr48)
    {
        this.macAddr48 = JsonNullable.<String>of(macAddr48);
    }

    public PcfBindingPatch addMacAddrs(List<String> addMacAddrs)
    {
        this.addMacAddrs = JsonNullable.<List<String>>of(addMacAddrs);

        return this;
    }

    public PcfBindingPatch addAddMacAddrsItem(String addMacAddrsItem)
    {
        if (this.addMacAddrs == null || !this.addMacAddrs.isPresent())
        {
            this.addMacAddrs = JsonNullable.<List<String>>of(new ArrayList<>());
        }
        try
        {
            this.addMacAddrs.get().add(addMacAddrsItem);
        }
        catch (java.util.NoSuchElementException e)
        {
            // this can never happen, as we make sure above that the value is present
        }
        return this;
    }

    /**
     * The additional MAC Addresses of the served UE.
     * 
     * @return addMacAddrs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "The additional MAC Addresses of the served UE.")
    @JsonIgnore

    public List<String> getAddMacAddrs()
    {
        return addMacAddrs.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_ADD_MAC_ADDRS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<List<String>> getAddMacAddrs_JsonNullable()
    {
        return addMacAddrs;
    }

    @JsonProperty(JSON_PROPERTY_ADD_MAC_ADDRS)
    public void setAddMacAddrs_JsonNullable(JsonNullable<List<String>> addMacAddrs)
    {
        this.addMacAddrs = addMacAddrs;
    }

    public void setAddMacAddrs(List<String> addMacAddrs)
    {
        this.addMacAddrs = JsonNullable.<List<String>>of(addMacAddrs);
    }

    public PcfBindingPatch pcfId(UUID pcfId)
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

    public PcfBindingPatch pcfFqdn(String pcfFqdn)
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

    public PcfBindingPatch pcfIpEndPoints(List<IpEndPoint> pcfIpEndPoints)
    {

        this.pcfIpEndPoints = pcfIpEndPoints;
        return this;
    }

    public PcfBindingPatch addPcfIpEndPointsItem(IpEndPoint pcfIpEndPointsItem)
    {
        if (this.pcfIpEndPoints == null)
        {
            this.pcfIpEndPoints = new ArrayList<>();
        }
        this.pcfIpEndPoints.add(pcfIpEndPointsItem);
        return this;
    }

    /**
     * IP end points of the PCF hosting the Npcf_PolicyAuthorization service.
     * 
     * @return pcfIpEndPoints
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "IP end points of the PCF hosting the Npcf_PolicyAuthorization service.")
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

    public PcfBindingPatch pcfDiamHost(String pcfDiamHost)
    {

        this.pcfDiamHost = pcfDiamHost;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pcfDiamHost
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PCF_DIAM_HOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfDiamHost()
    {
        return pcfDiamHost;
    }

    @JsonProperty(JSON_PROPERTY_PCF_DIAM_HOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfDiamHost(String pcfDiamHost)
    {
        this.pcfDiamHost = pcfDiamHost;
    }

    public PcfBindingPatch pcfDiamRealm(String pcfDiamRealm)
    {

        this.pcfDiamRealm = pcfDiamRealm;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return pcfDiamRealm
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_PCF_DIAM_REALM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPcfDiamRealm()
    {
        return pcfDiamRealm;
    }

    @JsonProperty(JSON_PROPERTY_PCF_DIAM_REALM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPcfDiamRealm(String pcfDiamRealm)
    {
        this.pcfDiamRealm = pcfDiamRealm;
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
        PcfBindingPatch pcfBindingPatch = (PcfBindingPatch) o;
        return equalsNullable(this.ipv4Addr, pcfBindingPatch.ipv4Addr) && equalsNullable(this.ipDomain, pcfBindingPatch.ipDomain)
               && equalsNullable(this.ipv6Prefix, pcfBindingPatch.ipv6Prefix) && equalsNullable(this.addIpv6Prefixes, pcfBindingPatch.addIpv6Prefixes)
               && equalsNullable(this.macAddr48, pcfBindingPatch.macAddr48) && equalsNullable(this.addMacAddrs, pcfBindingPatch.addMacAddrs)
               && Objects.equals(this.pcfId, pcfBindingPatch.pcfId) && Objects.equals(this.pcfFqdn, pcfBindingPatch.pcfFqdn)
               && Objects.equals(this.pcfIpEndPoints, pcfBindingPatch.pcfIpEndPoints) && Objects.equals(this.pcfDiamHost, pcfBindingPatch.pcfDiamHost)
               && Objects.equals(this.pcfDiamRealm, pcfBindingPatch.pcfDiamRealm);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hashCodeNullable(ipv4Addr),
                            hashCodeNullable(ipDomain),
                            hashCodeNullable(ipv6Prefix),
                            hashCodeNullable(addIpv6Prefixes),
                            hashCodeNullable(macAddr48),
                            hashCodeNullable(addMacAddrs),
                            pcfId,
                            pcfFqdn,
                            pcfIpEndPoints,
                            pcfDiamHost,
                            pcfDiamRealm);
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
        sb.append("class PcfBindingPatch {\n");
        sb.append("    ipv4Addr: ").append(toIndentedString(ipv4Addr)).append("\n");
        sb.append("    ipDomain: ").append(toIndentedString(ipDomain)).append("\n");
        sb.append("    ipv6Prefix: ").append(toIndentedString(ipv6Prefix)).append("\n");
        sb.append("    addIpv6Prefixes: ").append(toIndentedString(addIpv6Prefixes)).append("\n");
        sb.append("    macAddr48: ").append(toIndentedString(macAddr48)).append("\n");
        sb.append("    addMacAddrs: ").append(toIndentedString(addMacAddrs)).append("\n");
        sb.append("    pcfId: ").append(toIndentedString(pcfId)).append("\n");
        sb.append("    pcfFqdn: ").append(toIndentedString(pcfFqdn)).append("\n");
        sb.append("    pcfIpEndPoints: ").append(toIndentedString(pcfIpEndPoints)).append("\n");
        sb.append("    pcfDiamHost: ").append(toIndentedString(pcfDiamHost)).append("\n");
        sb.append("    pcfDiamRealm: ").append(toIndentedString(pcfDiamRealm)).append("\n");
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
