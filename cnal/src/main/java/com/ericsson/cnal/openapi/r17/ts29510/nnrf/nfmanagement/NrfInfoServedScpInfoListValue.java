/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnIdNid;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModelProperty;

/**
 * NrfInfoServedScpInfoListValue
 */
@JsonPropertyOrder({ NrfInfoServedScpInfoListValue.JSON_PROPERTY_SCP_DOMAIN_INFO_LIST,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_SCP_PREFIX,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_SCP_PORTS,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_ADDRESS_DOMAINS,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_IPV4_ADDRESSES,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_IPV6_PREFIXES,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_IPV4_ADDR_RANGES,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_IPV6_PREFIX_RANGES,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_SERVED_NF_SET_ID_LIST,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_REMOTE_PLMN_LIST,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_REMOTE_SNPN_LIST,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_IP_REACHABILITY,
                     NrfInfoServedScpInfoListValue.JSON_PROPERTY_SCP_CAPABILITIES })
@JsonTypeName("NrfInfo_servedScpInfoList_value")
public class NrfInfoServedScpInfoListValue
{
    public static final String JSON_PROPERTY_SCP_DOMAIN_INFO_LIST = "scpDomainInfoList";
    private Map<String, ScpDomainInfo> scpDomainInfoList = null;

    public static final String JSON_PROPERTY_SCP_PREFIX = "scpPrefix";
    private String scpPrefix;

    public static final String JSON_PROPERTY_SCP_PORTS = "scpPorts";
    private Map<String, Integer> scpPorts = null;

    public static final String JSON_PROPERTY_ADDRESS_DOMAINS = "addressDomains";
    private List<String> addressDomains = null;

    public static final String JSON_PROPERTY_IPV4_ADDRESSES = "ipv4Addresses";
    private List<String> ipv4Addresses = null;

    public static final String JSON_PROPERTY_IPV6_PREFIXES = "ipv6Prefixes";
    private List<String> ipv6Prefixes = null;

    public static final String JSON_PROPERTY_IPV4_ADDR_RANGES = "ipv4AddrRanges";
    private List<Ipv4AddressRange> ipv4AddrRanges = null;

    public static final String JSON_PROPERTY_IPV6_PREFIX_RANGES = "ipv6PrefixRanges";
    private List<Ipv6PrefixRange> ipv6PrefixRanges = null;

    public static final String JSON_PROPERTY_SERVED_NF_SET_ID_LIST = "servedNfSetIdList";
    private List<String> servedNfSetIdList = null;

    public static final String JSON_PROPERTY_REMOTE_PLMN_LIST = "remotePlmnList";
    private List<PlmnId> remotePlmnList = null;

    public static final String JSON_PROPERTY_REMOTE_SNPN_LIST = "remoteSnpnList";
    private List<PlmnIdNid> remoteSnpnList = null;

    public static final String JSON_PROPERTY_IP_REACHABILITY = "ipReachability";
    private String ipReachability;

    public static final String JSON_PROPERTY_SCP_CAPABILITIES = "scpCapabilities";
    private List<String> scpCapabilities = null;

    public NrfInfoServedScpInfoListValue()
    {
    }

    public NrfInfoServedScpInfoListValue scpDomainInfoList(Map<String, ScpDomainInfo> scpDomainInfoList)
    {

        this.scpDomainInfoList = scpDomainInfoList;
        return this;
    }

    public NrfInfoServedScpInfoListValue putScpDomainInfoListItem(String key,
                                                                  ScpDomainInfo scpDomainInfoListItem)
    {
        if (this.scpDomainInfoList == null)
        {
            this.scpDomainInfoList = new HashMap<>();
        }
        this.scpDomainInfoList.put(key, scpDomainInfoListItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where the key of the map shall be the string
     * identifying an SCP domain
     * 
     * @return scpDomainInfoList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs) where the key of the map shall be the string identifying an SCP domain ")
    @JsonProperty(JSON_PROPERTY_SCP_DOMAIN_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, ScpDomainInfo> getScpDomainInfoList()
    {
        return scpDomainInfoList;
    }

    @JsonProperty(JSON_PROPERTY_SCP_DOMAIN_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScpDomainInfoList(Map<String, ScpDomainInfo> scpDomainInfoList)
    {
        this.scpDomainInfoList = scpDomainInfoList;
    }

    public NrfInfoServedScpInfoListValue scpPrefix(String scpPrefix)
    {

        this.scpPrefix = scpPrefix;
        return this;
    }

    /**
     * Get scpPrefix
     * 
     * @return scpPrefix
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SCP_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getScpPrefix()
    {
        return scpPrefix;
    }

    @JsonProperty(JSON_PROPERTY_SCP_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScpPrefix(String scpPrefix)
    {
        this.scpPrefix = scpPrefix;
    }

    public NrfInfoServedScpInfoListValue scpPorts(Map<String, Integer> scpPorts)
    {

        this.scpPorts = scpPorts;
        return this;
    }

    public NrfInfoServedScpInfoListValue putScpPortsItem(String key,
                                                         Integer scpPortsItem)
    {
        if (this.scpPorts == null)
        {
            this.scpPorts = new HashMap<>();
        }
        this.scpPorts.put(key, scpPortsItem);
        return this;
    }

    /**
     * Port numbers for HTTP and HTTPS. The key of the map shall be
     * \&quot;http\&quot; or \&quot;https\&quot;.
     * 
     * @return scpPorts
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Port numbers for HTTP and HTTPS. The key of the map shall be \"http\" or \"https\". ")
    @JsonProperty(JSON_PROPERTY_SCP_PORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, Integer> getScpPorts()
    {
        return scpPorts;
    }

    @JsonProperty(JSON_PROPERTY_SCP_PORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScpPorts(Map<String, Integer> scpPorts)
    {
        this.scpPorts = scpPorts;
    }

    public NrfInfoServedScpInfoListValue addressDomains(List<String> addressDomains)
    {

        this.addressDomains = addressDomains;
        return this;
    }

    public NrfInfoServedScpInfoListValue addAddressDomainsItem(String addressDomainsItem)
    {
        if (this.addressDomains == null)
        {
            this.addressDomains = new ArrayList<>();
        }
        this.addressDomains.add(addressDomainsItem);
        return this;
    }

    /**
     * Get addressDomains
     * 
     * @return addressDomains
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADDRESS_DOMAINS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAddressDomains()
    {
        return addressDomains;
    }

    @JsonProperty(JSON_PROPERTY_ADDRESS_DOMAINS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddressDomains(List<String> addressDomains)
    {
        this.addressDomains = addressDomains;
    }

    public NrfInfoServedScpInfoListValue ipv4Addresses(List<String> ipv4Addresses)
    {

        this.ipv4Addresses = ipv4Addresses;
        return this;
    }

    public NrfInfoServedScpInfoListValue addIpv4AddressesItem(String ipv4AddressesItem)
    {
        if (this.ipv4Addresses == null)
        {
            this.ipv4Addresses = new ArrayList<>();
        }
        this.ipv4Addresses.add(ipv4AddressesItem);
        return this;
    }

    /**
     * Get ipv4Addresses
     * 
     * @return ipv4Addresses
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV4_ADDRESSES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIpv4Addresses()
    {
        return ipv4Addresses;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDRESSES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4Addresses(List<String> ipv4Addresses)
    {
        this.ipv4Addresses = ipv4Addresses;
    }

    public NrfInfoServedScpInfoListValue ipv6Prefixes(List<String> ipv6Prefixes)
    {

        this.ipv6Prefixes = ipv6Prefixes;
        return this;
    }

    public NrfInfoServedScpInfoListValue addIpv6PrefixesItem(String ipv6PrefixesItem)
    {
        if (this.ipv6Prefixes == null)
        {
            this.ipv6Prefixes = new ArrayList<>();
        }
        this.ipv6Prefixes.add(ipv6PrefixesItem);
        return this;
    }

    /**
     * Get ipv6Prefixes
     * 
     * @return ipv6Prefixes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_PREFIXES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIpv6Prefixes()
    {
        return ipv6Prefixes;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_PREFIXES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6Prefixes(List<String> ipv6Prefixes)
    {
        this.ipv6Prefixes = ipv6Prefixes;
    }

    public NrfInfoServedScpInfoListValue ipv4AddrRanges(List<Ipv4AddressRange> ipv4AddrRanges)
    {

        this.ipv4AddrRanges = ipv4AddrRanges;
        return this;
    }

    public NrfInfoServedScpInfoListValue addIpv4AddrRangesItem(Ipv4AddressRange ipv4AddrRangesItem)
    {
        if (this.ipv4AddrRanges == null)
        {
            this.ipv4AddrRanges = new ArrayList<>();
        }
        this.ipv4AddrRanges.add(ipv4AddrRangesItem);
        return this;
    }

    /**
     * Get ipv4AddrRanges
     * 
     * @return ipv4AddrRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV4_ADDR_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ipv4AddressRange> getIpv4AddrRanges()
    {
        return ipv4AddrRanges;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDR_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4AddrRanges(List<Ipv4AddressRange> ipv4AddrRanges)
    {
        this.ipv4AddrRanges = ipv4AddrRanges;
    }

    public NrfInfoServedScpInfoListValue ipv6PrefixRanges(List<Ipv6PrefixRange> ipv6PrefixRanges)
    {

        this.ipv6PrefixRanges = ipv6PrefixRanges;
        return this;
    }

    public NrfInfoServedScpInfoListValue addIpv6PrefixRangesItem(Ipv6PrefixRange ipv6PrefixRangesItem)
    {
        if (this.ipv6PrefixRanges == null)
        {
            this.ipv6PrefixRanges = new ArrayList<>();
        }
        this.ipv6PrefixRanges.add(ipv6PrefixRangesItem);
        return this;
    }

    /**
     * Get ipv6PrefixRanges
     * 
     * @return ipv6PrefixRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_PREFIX_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ipv6PrefixRange> getIpv6PrefixRanges()
    {
        return ipv6PrefixRanges;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_PREFIX_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6PrefixRanges(List<Ipv6PrefixRange> ipv6PrefixRanges)
    {
        this.ipv6PrefixRanges = ipv6PrefixRanges;
    }

    public NrfInfoServedScpInfoListValue servedNfSetIdList(List<String> servedNfSetIdList)
    {

        this.servedNfSetIdList = servedNfSetIdList;
        return this;
    }

    public NrfInfoServedScpInfoListValue addServedNfSetIdListItem(String servedNfSetIdListItem)
    {
        if (this.servedNfSetIdList == null)
        {
            this.servedNfSetIdList = new ArrayList<>();
        }
        this.servedNfSetIdList.add(servedNfSetIdListItem);
        return this;
    }

    /**
     * Get servedNfSetIdList
     * 
     * @return servedNfSetIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVED_NF_SET_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getServedNfSetIdList()
    {
        return servedNfSetIdList;
    }

    @JsonProperty(JSON_PROPERTY_SERVED_NF_SET_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServedNfSetIdList(List<String> servedNfSetIdList)
    {
        this.servedNfSetIdList = servedNfSetIdList;
    }

    public NrfInfoServedScpInfoListValue remotePlmnList(List<PlmnId> remotePlmnList)
    {

        this.remotePlmnList = remotePlmnList;
        return this;
    }

    public NrfInfoServedScpInfoListValue addRemotePlmnListItem(PlmnId remotePlmnListItem)
    {
        if (this.remotePlmnList == null)
        {
            this.remotePlmnList = new ArrayList<>();
        }
        this.remotePlmnList.add(remotePlmnListItem);
        return this;
    }

    /**
     * Get remotePlmnList
     * 
     * @return remotePlmnList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REMOTE_PLMN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnId> getRemotePlmnList()
    {
        return remotePlmnList;
    }

    @JsonProperty(JSON_PROPERTY_REMOTE_PLMN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemotePlmnList(List<PlmnId> remotePlmnList)
    {
        this.remotePlmnList = remotePlmnList;
    }

    public NrfInfoServedScpInfoListValue remoteSnpnList(List<PlmnIdNid> remoteSnpnList)
    {

        this.remoteSnpnList = remoteSnpnList;
        return this;
    }

    public NrfInfoServedScpInfoListValue addRemoteSnpnListItem(PlmnIdNid remoteSnpnListItem)
    {
        if (this.remoteSnpnList == null)
        {
            this.remoteSnpnList = new ArrayList<>();
        }
        this.remoteSnpnList.add(remoteSnpnListItem);
        return this;
    }

    /**
     * Get remoteSnpnList
     * 
     * @return remoteSnpnList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REMOTE_SNPN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnIdNid> getRemoteSnpnList()
    {
        return remoteSnpnList;
    }

    @JsonProperty(JSON_PROPERTY_REMOTE_SNPN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteSnpnList(List<PlmnIdNid> remoteSnpnList)
    {
        this.remoteSnpnList = remoteSnpnList;
    }

    public NrfInfoServedScpInfoListValue ipReachability(String ipReachability)
    {

        this.ipReachability = ipReachability;
        return this;
    }

    /**
     * Indicates the type(s) of IP addresses reachable via an SCP
     * 
     * @return ipReachability
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the type(s) of IP addresses reachable via an SCP")
    @JsonProperty(JSON_PROPERTY_IP_REACHABILITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpReachability()
    {
        return ipReachability;
    }

    @JsonProperty(JSON_PROPERTY_IP_REACHABILITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpReachability(String ipReachability)
    {
        this.ipReachability = ipReachability;
    }

    public NrfInfoServedScpInfoListValue scpCapabilities(List<String> scpCapabilities)
    {

        this.scpCapabilities = scpCapabilities;
        return this;
    }

    public NrfInfoServedScpInfoListValue addScpCapabilitiesItem(String scpCapabilitiesItem)
    {
        if (this.scpCapabilities == null)
        {
            this.scpCapabilities = new ArrayList<>();
        }
        this.scpCapabilities.add(scpCapabilitiesItem);
        return this;
    }

    /**
     * Get scpCapabilities
     * 
     * @return scpCapabilities
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SCP_CAPABILITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getScpCapabilities()
    {
        return scpCapabilities;
    }

    @JsonProperty(JSON_PROPERTY_SCP_CAPABILITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScpCapabilities(List<String> scpCapabilities)
    {
        this.scpCapabilities = scpCapabilities;
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
        NrfInfoServedScpInfoListValue nrfInfoServedScpInfoListValue = (NrfInfoServedScpInfoListValue) o;
        return Objects.equals(this.scpDomainInfoList, nrfInfoServedScpInfoListValue.scpDomainInfoList)
               && Objects.equals(this.scpPrefix, nrfInfoServedScpInfoListValue.scpPrefix)
               && Objects.equals(this.scpPorts, nrfInfoServedScpInfoListValue.scpPorts)
               && Objects.equals(this.addressDomains, nrfInfoServedScpInfoListValue.addressDomains)
               && Objects.equals(this.ipv4Addresses, nrfInfoServedScpInfoListValue.ipv4Addresses)
               && Objects.equals(this.ipv6Prefixes, nrfInfoServedScpInfoListValue.ipv6Prefixes)
               && Objects.equals(this.ipv4AddrRanges, nrfInfoServedScpInfoListValue.ipv4AddrRanges)
               && Objects.equals(this.ipv6PrefixRanges, nrfInfoServedScpInfoListValue.ipv6PrefixRanges)
               && Objects.equals(this.servedNfSetIdList, nrfInfoServedScpInfoListValue.servedNfSetIdList)
               && Objects.equals(this.remotePlmnList, nrfInfoServedScpInfoListValue.remotePlmnList)
               && Objects.equals(this.remoteSnpnList, nrfInfoServedScpInfoListValue.remoteSnpnList)
               && Objects.equals(this.ipReachability, nrfInfoServedScpInfoListValue.ipReachability)
               && Objects.equals(this.scpCapabilities, nrfInfoServedScpInfoListValue.scpCapabilities);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(scpDomainInfoList,
                            scpPrefix,
                            scpPorts,
                            addressDomains,
                            ipv4Addresses,
                            ipv6Prefixes,
                            ipv4AddrRanges,
                            ipv6PrefixRanges,
                            servedNfSetIdList,
                            remotePlmnList,
                            remoteSnpnList,
                            ipReachability,
                            scpCapabilities);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NrfInfoServedScpInfoListValue {\n");
        sb.append("    scpDomainInfoList: ").append(toIndentedString(scpDomainInfoList)).append("\n");
        sb.append("    scpPrefix: ").append(toIndentedString(scpPrefix)).append("\n");
        sb.append("    scpPorts: ").append(toIndentedString(scpPorts)).append("\n");
        sb.append("    addressDomains: ").append(toIndentedString(addressDomains)).append("\n");
        sb.append("    ipv4Addresses: ").append(toIndentedString(ipv4Addresses)).append("\n");
        sb.append("    ipv6Prefixes: ").append(toIndentedString(ipv6Prefixes)).append("\n");
        sb.append("    ipv4AddrRanges: ").append(toIndentedString(ipv4AddrRanges)).append("\n");
        sb.append("    ipv6PrefixRanges: ").append(toIndentedString(ipv6PrefixRanges)).append("\n");
        sb.append("    servedNfSetIdList: ").append(toIndentedString(servedNfSetIdList)).append("\n");
        sb.append("    remotePlmnList: ").append(toIndentedString(remotePlmnList)).append("\n");
        sb.append("    remoteSnpnList: ").append(toIndentedString(remoteSnpnList)).append("\n");
        sb.append("    ipReachability: ").append(toIndentedString(ipReachability)).append("\n");
        sb.append("    scpCapabilities: ").append(toIndentedString(scpCapabilities)).append("\n");
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
