/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 24, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.openapi.model;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.esc.lib.InvalidParam;
import com.ericsson.esc.lib.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.InetAddresses;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcfBinding
{
    @JsonIgnore
    private final UeAddressType ueAddressType;
    private final String supi;
    private final String gpsi;
    private final Inet4Address ipv4Addr;
    private final Ipv6Prefix ipv6Prefix;
    private final String ipDomain;
    private final MacAddr48 macAddr48;
    private final String dnn;
    private final String pcfFqdn;
    @JsonInclude(Include.NON_EMPTY)
    private final List<IpEndPoint> pcfIpEndPoints;
    private final DiameterIdentity pcfDiamHost;
    private final DiameterIdentity pcfDiamRealm;
    private final Snssai snssai;
    private final UUID pcfId;
    private final RecoveryTime recoveryTime;
    private final SupportedFeatures suppFeat;
    @JsonInclude(Include.NON_EMPTY)
    private final List<Ipv6Prefix> addIpv6Prefixes;
    @JsonInclude(Include.NON_EMPTY)
    private final List<MacAddr48> addMacAddrs;
    private final String pcfSetId;
    private final BindingLevel bindLevel;

    private static final String IP_V4_ADDR = "ipv4Addr";
    private static final String PCF_DIAM_HOST = "pcfDiamHost";
    private static final String PCF_DIAM_REALM = "pcfDiamRealm";
    private static final String PCF_FQDN = "pcfFqdn";
    private static final String PCF_IP_ENDPOINTS = "pcfIpEndPoints";
    private static final String IP_V6_PREFIX = "ipv6Prefix";
    private static final String MAC_ADDR_48 = "macAddr48";
    private static final String ADD_IP_V6_PREFIXES = "addIpv6Prefixes";
    private static final String ADD_MAC_ADDRS = "addMacAddrs";

    protected PcfBinding(UeAddressType ueAddressType,
                         String supi,
                         String gpsi,
                         Inet4Address ipv4Addr,
                         Ipv6Prefix ipv6Prefix,
                         String ipDomain,
                         MacAddr48 macAddr48,
                         String dnn,
                         String pcfFqdn,
                         List<IpEndPoint> pcfIpEndPoints,
                         DiameterIdentity pcfDiamHost,
                         DiameterIdentity pcfDiamRealm,
                         Snssai snssai,
                         UUID pcfId,
                         RecoveryTime recoveryTime,
                         SupportedFeatures suppFeat,
                         List<Ipv6Prefix> addIpv6Prefixes,
                         List<MacAddr48> addMacAddrs,
                         String pcfSetId,
                         BindingLevel bindLevel)
    {
        this.ueAddressType = ueAddressType;
        this.supi = supi;
        this.gpsi = gpsi;
        this.ipv4Addr = ipv4Addr;
        this.ipv6Prefix = ipv6Prefix;
        this.ipDomain = ipDomain;
        this.macAddr48 = macAddr48;
        this.dnn = dnn;
        this.pcfFqdn = pcfFqdn;
        this.pcfIpEndPoints = pcfIpEndPoints;
        this.pcfDiamHost = pcfDiamHost;
        this.pcfDiamRealm = pcfDiamRealm;
        this.snssai = snssai;
        this.pcfId = pcfId;
        this.recoveryTime = recoveryTime;
        this.suppFeat = suppFeat;
        this.addIpv6Prefixes = addIpv6Prefixes;
        this.addMacAddrs = addMacAddrs;
        this.pcfSetId = pcfSetId;
        this.bindLevel = bindLevel;
    }

    public static PcfBinding create(String supi,
                                    String gpsi,
                                    Inet4Address ipv4Addr,
                                    Ipv6Prefix ipv6Prefix,
                                    String ipDomain,
                                    MacAddr48 macAddr48,
                                    String dnn,
                                    String pcfFqdn,
                                    List<IpEndPoint> pcfIpEndPoints,
                                    DiameterIdentity pcfDiamHost,
                                    DiameterIdentity pcfDiamRealm,
                                    Snssai snssai,
                                    UUID pcfId,
                                    RecoveryTime recoveryTime,
                                    SupportedFeatures suppFeat,
                                    List<Ipv6Prefix> addIpv6Prefixes,
                                    List<MacAddr48> addMacAddrs,
                                    String pcfSetId,
                                    BindingLevel bindLevel)
    {
        validateDnnSnssai(dnn, snssai);
        final var ueAddressType = validateAddressInfo(ipv4Addr, ipv6Prefix, ipDomain, macAddr48, addIpv6Prefixes, addMacAddrs);
        validatePcfInfo(pcfFqdn, pcfIpEndPoints, pcfDiamHost, pcfDiamRealm);
        return new PcfBinding(ueAddressType,
                              supi,
                              gpsi,
                              ipv4Addr,
                              ipv6Prefix,
                              ipDomain,
                              macAddr48,
                              dnn,
                              pcfFqdn,
                              pcfIpEndPoints,
                              pcfDiamHost,
                              pcfDiamRealm,
                              snssai,
                              pcfId,
                              recoveryTime,
                              suppFeat,
                              addIpv6Prefixes,
                              addMacAddrs,
                              pcfSetId,
                              bindLevel);
    }

    public static PcfBinding create(PcfBinding pcfBinding,
                                    SupportedFeatures commonSuppFeat)
    {
        return create(pcfBinding.getSupi(),
                      pcfBinding.getGpsi(),
                      pcfBinding.getIpv4Addr(),
                      pcfBinding.getIpv6Prefix(),
                      pcfBinding.getIpDomain(),
                      pcfBinding.getMacAddr48(),
                      pcfBinding.getDnn(),
                      pcfBinding.getPcfFqdn(),
                      pcfBinding.getPcfIpEndPoints(),
                      pcfBinding.getPcfDiamHost(),
                      pcfBinding.getPcfDiamRealm(),
                      pcfBinding.getSnssai(),
                      pcfBinding.getPcfId(),
                      pcfBinding.getRecoveryTime(),
                      commonSuppFeat,
                      pcfBinding.getAddIpv6Prefixes(),
                      pcfBinding.getAddMacAddrs(),
                      pcfBinding.getPcfSetId(),
                      pcfBinding.getBindLevel());
    }

    // Lists of String used for addIpv6Prefixes, addMacAddrs in order to avoid the
    // ValidationExceptions of the original
    // class. E.g. We don't want the ValidationException to be thrown for ipv6Prefix
    // parameter but for addIpv6Prefixes parameter
    @JsonCreator
    public static PcfBinding createJson(@JsonProperty("supi") String supi, // NOPMD by xgeoant on 4/3/19, 4:29 PM
                                        @JsonProperty("gpsi") String gpsi,
                                        @JsonProperty("ipv4Addr") String ipv4Addr,
                                        @JsonProperty("ipv6Prefix") Ipv6Prefix ipv6Prefix,
                                        @JsonProperty("ipDomain") String ipDomain,
                                        @JsonProperty("macAddr48") MacAddr48 macAddr48,
                                        @JsonProperty("dnn") String dnn,
                                        @JsonProperty("pcfFqdn") String pcfFqdn,
                                        @JsonProperty("pcfIpEndPoints") List<IpEndPoint> pcfIpEndPoints,
                                        @JsonProperty("pcfDiamHost") String pcfDiamHostStr,
                                        @JsonProperty("pcfDiamRealm") String pcfDiamRealmStr,
                                        @JsonProperty("snssai") Snssai snssai,
                                        @JsonProperty("pcfId") String pcfId,
                                        @JsonProperty("recoveryTime") RecoveryTime recoveryTime,
                                        @JsonProperty("suppFeat") SupportedFeatures suppFeat,
                                        @JsonProperty("addIpv6Prefixes") List<String> addIpv6PrefixesStr,
                                        @JsonProperty("addMacAddrs") List<String> addMacAddrsStr,
                                        @JsonProperty("pcfSetId") String pcfSetId,
                                        @JsonProperty("bindLevel") BindingLevel bindLevel)
    {
        // Parse IP addresses.
        Inet4Address ipv4 = null;

        if (ipv4Addr != null)
        {
            try
            {
                ipv4 = (Inet4Address) InetAddresses.forString(ipv4Addr);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, IP_V4_ADDR, "Invalid IPv4 Address", e);
            }
        }

        DiameterIdentity pcfDiamHost = null;
        if (pcfDiamHostStr != null)
        {
            try
            {
                pcfDiamHost = new DiameterIdentity(pcfDiamHostStr);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR,
                                              PCF_DIAM_HOST,
                                              "Invalid pcfDiamHost. It must be of type DiameterIdentity.",
                                              e);
            }
        }

        DiameterIdentity pcfDiamRealm = null;
        if (pcfDiamRealmStr != null)
        {
            try
            {
                pcfDiamRealm = new DiameterIdentity(pcfDiamRealmStr);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR,
                                              PCF_DIAM_REALM,
                                              "Invalid pcfDiamRealm. It must be of type DiameterIdentity.",
                                              e);
            }
        }

        // Empty String not allowed for SUPI
        if (supi != null && supi.isEmpty())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "supi", "Invalid supi");
        }

        // Empty String not allowed for GPSI
        if (gpsi != null && gpsi.isEmpty())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "gpsi", "Invalid gpsi");
        }

        UUID pcfIdFromString = null;
        if (pcfId != null)
        {
            try
            {
                pcfIdFromString = UUID.fromString(pcfId);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "pcfId", "Invalid pcfId", e);
            }
        }

        List<Ipv6Prefix> addIpv6Prefixes;
        if (addIpv6PrefixesStr != null)
        {
            addIpv6Prefixes = new ArrayList<>();

            try
            {
                addIpv6PrefixesStr.forEach(s -> addIpv6Prefixes.add(new Ipv6Prefix(s)));
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, ADD_IP_V6_PREFIXES, "Invalid addIpv6Prefixes");
            }
        }
        else
        {
            addIpv6Prefixes = null;
        }

        List<MacAddr48> addMacAddrs;
        if (addMacAddrsStr != null)
        {
            addMacAddrs = new ArrayList<>();

            try
            {
                addMacAddrsStr.forEach(s -> addMacAddrs.add(new MacAddr48(s)));
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, ADD_MAC_ADDRS, "Invalid addMacAddrs");
            }
        }
        else
        {
            addMacAddrs = null;
        }

        return create(supi,
                      gpsi,
                      ipv4,
                      ipv6Prefix,
                      ipDomain,
                      macAddr48,
                      dnn,
                      pcfFqdn,
                      pcfIpEndPoints,
                      pcfDiamHost,
                      pcfDiamRealm,
                      snssai,
                      pcfIdFromString,
                      recoveryTime,
                      suppFeat,
                      addIpv6Prefixes,
                      addMacAddrs,
                      pcfSetId,
                      bindLevel);
    }

    private static void validateDnnSnssai(String dnn,
                                          Snssai snssai)
    {
        if (dnn == null)
        {
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM, "dnn", "dnn is mandatory");
        }
        if (snssai == null)
        {
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM, "snssai", "snssai is mandatory");
        }
    }

    private static UeAddressType validateAddressInfo(Inet4Address ipv4Addr,
                                                     Ipv6Prefix ipv6Prefix,
                                                     String ipDomain,
                                                     MacAddr48 macAddr48,
                                                     List<Ipv6Prefix> addIpv6Prefixes,
                                                     List<MacAddr48> addMacAddrs)
    {
        if (ipv4Addr == null && ipv6Prefix == null && macAddr48 == null && (addIpv6Prefixes == null || addIpv6Prefixes.isEmpty())
            && (addMacAddrs == null || addMacAddrs.isEmpty()))
        {
            final var errorMsg = "At least one of ipv4Addr, ipv6Prefix, addIpv6Prefixes, macAddr48 or addMacAddrs parameters must be defined";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                          List.of(new InvalidParam(IP_V4_ADDR, errorMsg),
                                                  new InvalidParam(IP_V6_PREFIX, errorMsg),
                                                  new InvalidParam(ADD_IP_V6_PREFIXES, errorMsg),
                                                  new InvalidParam(MAC_ADDR_48, errorMsg),
                                                  new InvalidParam(ADD_MAC_ADDRS, errorMsg)));
        }

        if (addMacAddrs != null && addMacAddrs.isEmpty())
        {
            // addMacAddrs cannot be an empty list
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, ADD_MAC_ADDRS, "Parameter addMacAddrs cannot be an empty list");
        }

        if (addIpv6Prefixes != null && addIpv6Prefixes.isEmpty())
        {
            // addIpv6Prefixes cannot be an empty list
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL,
                                          ADD_IP_V6_PREFIXES,
                                          "Parameter addIpv6Prefixes cannot be an empty list");
        }

        if (macAddr48 != null || (addMacAddrs != null && !addMacAddrs.isEmpty()))
        {
            // MAC address UE address, ensure other addresses are null
            if ((ipv4Addr != null || ipv6Prefix != null || ipDomain != null || (addIpv6Prefixes != null && !addIpv6Prefixes.isEmpty())))
            {
                final var errorMsg = "Parameter macAddr48 and/or addMacAddrs cannot be combined with ipv4Addr, ipv6Prefix, addIpv6Prefixes or ipDomain";
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                              List.of(new InvalidParam(MAC_ADDR_48, errorMsg), new InvalidParam(ADD_MAC_ADDRS, errorMsg)));
            }
            return UeAddressType.MAC;
        }

        if (ipv4Addr != null)
        {
            // ipv4 or ipv4_ipv6 address
            return (ipv6Prefix != null || addIpv6Prefixes != null) ? UeAddressType.INET4_6 : UeAddressType.INET4;
        }
        else
        {
            // ipv6 address, ensure ipDomain is null
            if (ipDomain != null)
            {
                // ipDomain is optional parameter
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                              IP_V4_ADDR,
                                              "Parameter ipDomain requires a valid ipv4Addr parameter");
            }
            return UeAddressType.INET6;
        }
    }

    private static void validatePcfInfo(String pcfFqdn,
                                        List<IpEndPoint> pcfIpEndPoints,
                                        DiameterIdentity pcfDiamHost,
                                        DiameterIdentity pcfDiamRealm)
    {
        if (pcfFqdn == null && (pcfIpEndPoints == null || pcfIpEndPoints.isEmpty()) && (pcfDiamHost == null || pcfDiamRealm == null))
        {
            final String errorMsg = "At least one of pcfFqdn, pcfIpEndPoints or pcfDiamHost and pcfDiamRealm must be defined";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                          List.of(new InvalidParam(PCF_FQDN, errorMsg),
                                                  new InvalidParam(PCF_IP_ENDPOINTS, errorMsg),
                                                  new InvalidParam(PCF_DIAM_HOST, errorMsg),
                                                  new InvalidParam(PCF_DIAM_REALM, errorMsg)));
        }

        if ((pcfDiamHost == null && pcfDiamRealm != null) || (pcfDiamHost != null && pcfDiamRealm == null))
        {
            final String errorMsg = "Both pcfDiamHost and pcfDiamRealm are required, if the PCF supports Rx diameter interface";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                          List.of(new InvalidParam(PCF_DIAM_HOST, errorMsg), new InvalidParam(PCF_DIAM_REALM, errorMsg)));
        }

        if (pcfIpEndPoints != null && pcfIpEndPoints.isEmpty())
        {
            // pcfIpEndPoints cannot be an empty list
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL,
                                          PCF_IP_ENDPOINTS,
                                          "Parameter pcfIpEndPoints cannot be an empty list");
        }
    }

    @JsonIgnore
    public UeAddressType getUeAddressType()
    {
        return this.ueAddressType;
    }

    /**
     * @return the supi
     */
    public String getSupi()
    {
        return supi;
    }

    /**
     * @return the gpsi
     */
    public String getGpsi()
    {
        return gpsi;
    }

    /**
     * @return the ipv4Addr
     */
    public Inet4Address getIpv4Addr()
    {
        return ipv4Addr;
    }

    /**
     * @return the ipv6Prefix
     */
    public Ipv6Prefix getIpv6Prefix()
    {
        return ipv6Prefix;
    }

    /**
     * Get all the IPv6 prefixes of the served UE
     * 
     * @return A stream of IPv6 prefixes or an empty stream
     */
    @JsonIgnore
    public Stream<Ipv6Prefix> getAllIpv6Prefix()
    {
        Stream<Ipv6Prefix> streamIpv6Prefix = Stream.empty();
        Stream<Ipv6Prefix> streamIpv6AddPrefixes = Stream.empty();
        if (ipv6Prefix != null)
        {
            streamIpv6Prefix = Stream.of(ipv6Prefix);
        }
        if (addIpv6Prefixes != null && !addIpv6Prefixes.isEmpty())
        {
            streamIpv6AddPrefixes = addIpv6Prefixes.stream();
        }
        return Stream.concat(streamIpv6Prefix, streamIpv6AddPrefixes);
    }

    /**
     * @return the ipv6Prefix for JSON serialization
     */
    @JsonGetter("ipv6Prefix")
    public String getIpv6Prefixstr()
    {
        if (ipv6Prefix != null)
        {
            return ipv6Prefix.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the ipDomain
     */
    public String getIpDomain()
    {
        return ipDomain;
    }

    /**
     * @return the macAddr48
     */
    public MacAddr48 getMacAddr48()
    {
        return macAddr48;
    }

    /**
     * Get all the MAC addresses of the served UE
     * 
     * @return A stream of MAC addresses or an empty stream
     */
    @JsonIgnore
    public Stream<MacAddr48> getAllMacAddr48()
    {
        Stream<MacAddr48> streamMacAddr = Stream.empty();
        Stream<MacAddr48> streamAddMacAddrs = Stream.empty();
        if (macAddr48 != null)
        {
            streamMacAddr = Stream.of(macAddr48);
        }
        if (addMacAddrs != null && !addMacAddrs.isEmpty())
        {
            streamAddMacAddrs = addMacAddrs.stream();
        }
        return Stream.concat(streamMacAddr, streamAddMacAddrs);
    }

    /**
     * @return the macAddr48 for JSON serialization
     */
    @JsonGetter("macAddr48")
    public String getMacAddr48str()
    {
        if (macAddr48 != null)
        {
            return macAddr48.getMacAddr48Str();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the dnn
     */
    public String getDnn()
    {
        return dnn;
    }

    /**
     * @return the pcfFqdn
     */
    public String getPcfFqdn()
    {
        return pcfFqdn;
    }

    /**
     * @return the pcfIpEndPoints
     */
    public List<IpEndPoint> getPcfIpEndPoints()
    {
        return pcfIpEndPoints;
    }

    /**
     * @return the pcfDiamHost
     */
    public DiameterIdentity getPcfDiamHost()
    {
        return pcfDiamHost;
    }

    /**
     * @return the pcfDiamHost for JSON serialization
     */
    @JsonGetter("pcfDiamHost")
    public String getPcfDiamHoststr()
    {
        if (pcfDiamHost != null)
        {
            return pcfDiamHost.getDiameterIdentityStr();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the pcfDiamRealm
     */
    public DiameterIdentity getPcfDiamRealm()
    {
        return pcfDiamRealm;
    }

    /**
     * @return the pcfDiamRealm for JSON serialization
     */
    @JsonGetter("pcfDiamRealm")
    public String getPcfDiamRealmstr()
    {
        if (pcfDiamRealm != null)
        {
            return pcfDiamRealm.getDiameterIdentityStr();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the snssai
     */
    public Snssai getSnssai()
    {
        return snssai;
    }

    /**
     * @return the pcfId
     */
    public UUID getPcfId()
    {
        return pcfId;
    }

    /**
     * @return the recoveryTime
     */
    public RecoveryTime getRecoveryTime()
    {
        return recoveryTime;
    }

    /**
     * @return the recoveryTime for JSON serialization
     */
    @JsonGetter("recoveryTime")
    public String getRecoveryTimeString()
    {
        if (recoveryTime != null)
        {
            return recoveryTime.getRecoveryTimeStr();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the suppFeat
     */
    public SupportedFeatures getSuppFeat()
    {
        return suppFeat;
    }

    /**
     * @return the suppFeat for JSON serialization
     */
    @JsonGetter("suppFeat")
    public String getSupportedFeatures()
    {
        if (suppFeat != null)
        {
            return suppFeat.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the addIpv6Prefixes
     */
    public List<Ipv6Prefix> getAddIpv6Prefixes()
    {
        return addIpv6Prefixes;
    }

    /**
     * @return the addIpv6Prefixes for JSON serialization
     */
    @JsonGetter("addIpv6Prefixes")
    public List<String> getAddIpv6Prefixesstr()
    {
        List<String> addIpv6PrefixesStr = new ArrayList<>();
        if (addIpv6Prefixes != null)
        {
            addIpv6Prefixes.forEach(addPrefix -> addIpv6PrefixesStr.add(addPrefix.toString()));
        }
        return addIpv6PrefixesStr;
    }

    /**
     * @return the addMacAddrs
     */
    public List<MacAddr48> getAddMacAddrs()
    {
        return addMacAddrs;
    }

    /**
     * @return the addMacAddrs for JSON serialization
     */
    @JsonGetter("addMacAddrs")
    public List<String> getAddMacAddrsstr()
    {
        List<String> addMacAddrsStr = new ArrayList<>();
        if (addMacAddrs != null)
        {
            addMacAddrs.forEach(addMac -> addMacAddrsStr.add(addMac.toString()));
        }
        return addMacAddrsStr;
    }

    /**
     * @return the pcfSetId
     */
    public String getPcfSetId()
    {
        return pcfSetId;
    }

    /**
     * @return the bindLevel
     */
    public BindingLevel getBindLevel()
    {
        return bindLevel;
    }

    /**
     * @return the bindLevel for JSON serialization
     */
    @JsonGetter("bindLevel")
    public String getBindLevelstr()
    {
        if (bindLevel != null)
        {
            return bindLevel.getBindLevelStr();
        }
        else
        {
            return null;
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(addIpv6Prefixes,
                            addMacAddrs,
                            bindLevel,
                            dnn,
                            gpsi,
                            ipDomain,
                            ipv4Addr,
                            ipv6Prefix,
                            macAddr48,
                            pcfDiamHost,
                            pcfDiamRealm,
                            pcfFqdn,
                            pcfId,
                            pcfIpEndPoints,
                            pcfSetId,
                            recoveryTime,
                            snssai,
                            supi,
                            suppFeat,
                            ueAddressType);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PcfBinding))
            return false;
        PcfBinding other = (PcfBinding) obj;
        return Objects.equals(addIpv6Prefixes, other.addIpv6Prefixes) && Objects.equals(addMacAddrs, other.addMacAddrs)
               && Objects.equals(bindLevel, other.bindLevel) && Objects.equals(dnn, other.dnn) && Objects.equals(gpsi, other.gpsi)
               && Objects.equals(ipDomain, other.ipDomain) && Objects.equals(ipv4Addr, other.ipv4Addr) && Objects.equals(ipv6Prefix, other.ipv6Prefix)
               && Objects.equals(macAddr48, other.macAddr48) && Objects.equals(pcfDiamHost, other.pcfDiamHost)
               && Objects.equals(pcfDiamRealm, other.pcfDiamRealm) && Objects.equals(pcfFqdn, other.pcfFqdn) && Objects.equals(pcfId, other.pcfId)
               && Objects.equals(pcfIpEndPoints, other.pcfIpEndPoints) && Objects.equals(pcfSetId, other.pcfSetId)
               && Objects.equals(recoveryTime, other.recoveryTime) && Objects.equals(snssai, other.snssai) && Objects.equals(supi, other.supi)
               && Objects.equals(suppFeat, other.suppFeat) && ueAddressType == other.ueAddressType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "PcfBinding [supi=" + supi + ", gpsi=" + gpsi + ", ipv4Addr=" + ipv4Addr + ", ipv6Prefix=" + ipv6Prefix + ", ipDomain=" + ipDomain
               + ", macAddr48=" + macAddr48 + ", dnn=" + dnn + ", pcfFqdn=" + pcfFqdn + ", pcfIpEndPoints=" + pcfIpEndPoints + ", pcfDiamHost=" + pcfDiamHost
               + ", pcfDiamRealm=" + pcfDiamRealm + ", snssai=" + snssai + ", pcfId=" + pcfId + ", recoveryTime=" + recoveryTime + ", suppFeat=" + suppFeat
               + ", addIpv6Prefixes=" + addIpv6Prefixes + ", addMacAddrs=" + addMacAddrs + ", pcfSetId=" + pcfSetId + ", bindLevel=" + bindLevel + "]";
    }

}
