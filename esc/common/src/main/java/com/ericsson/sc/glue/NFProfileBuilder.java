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
 * Created on: May 9, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.glue;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.BsfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IdentityRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpEndPoint;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpReachability;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Ipv4AddressRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Ipv6PrefixRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFServiceStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFServiceVersion;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.PlmnRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpCapability;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpDomainInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SeppInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.TransportProtocol;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai.WildcardSdEnum;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.sc.expressionparser.NfConditionParser;
import com.ericsson.sc.nfm.model.AdminState;
import com.ericsson.sc.nfm.model.AllowedOperationsPerNfInstance;
import com.ericsson.sc.nfm.model.AllowedOperationsPerNfType;
import com.ericsson.sc.nfm.model.BsfInfo__1;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.scp.model.DiscoveredNfInstance;
import com.ericsson.sc.scp.model.DiscoveredNfService;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.NfPoolDiscovery;
import com.ericsson.sc.scp.model.PriorityGroup;
import com.ericsson.sc.sepp.model.AllowPlmn;
import com.ericsson.utilities.common.Utils;

/**
 * Encapsulation of the process of building an NFProfile. This class covers the
 * generic steps only. If specific steps are needed, one may extend this class
 * and override method postProcessResult().
 */
public class NFProfileBuilder
{
    private static final Logger log = LoggerFactory.getLogger(NFProfileBuilder.class);

    /**
     * Build an instance of NFProfile from the parameters passed.
     * 
     * @param [in] nfInstanceId
     * @param [in] nfProfile
     * @param [in] nfType
     * @return The NFProfile built.
     */
    public <T extends IfServiceAddress> NFProfile build(final IfGenericNfInstance instance,
                                                        final UUID nfInstanceId,
                                                        final com.ericsson.sc.nfm.model.NfProfile nfProfile,
                                                        final Map<String, List<T>> serviceAddrs)
    {
        final NFProfile result = new NFProfile();

        final String nfType = nfProfile.getNfType().toUpperCase();

        nfProfile.getAllowedNfDomain().forEach(result::addAllowedNfDomainsItem);
        nfProfile.getAllowedNfType().forEach(r -> result.addAllowedNfTypesItem(r.toUpperCase()));

        if (nfProfile.getAllowedNssai1().isEmpty())
            nfProfile.getAllowedNssai().forEach(r -> result.addAllowedNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));
        else
            nfProfile.getAllowedNssai1().forEach(r -> result.addAllowedNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));

        nfProfile.getAllowedPlmn().forEach(r -> result.addAllowedPlmnsItem(this.buildPlmnId(r)));

        // Remove the capacity on NFProfile level as in this case the capacity
        // of the discovered instances is reported per NFService.
        if (nfProfile.getAssociatedNfPoolRef() != null && !nfProfile.getAssociatedNfPoolRef().isEmpty())
        {
            result.setCapacity(null);
        }
        // Capacity based on the configured value
        else
        {
            result.setCapacity(nfProfile.getCapacity());
        }

        result.setHeartBeatTimer(nfProfile.getRequestedHeartbeatTimer());

        {
            final T s = serviceAddrs.get(nfProfile.getName()).get(0);

            result.setFqdn(s.getFqdn());
            result.setIpv4Addresses(s.getIpv4Address() != null ? Arrays.asList(s.getIpv4Address()) : null);
            result.setIpv6Addresses(s.getIpv6Address() != null ? Arrays.asList(s.getIpv6Address()) : null);
        }

        result.setLoad(0); // value is patched before it is sent to the NRF
        result.setLocality(nfProfile.getLocality());
        result.setNfInstanceId(nfInstanceId);
        result.setNfInstanceName(nfProfile.getNfInstanceName() != null && !nfProfile.getNfInstanceName().isEmpty() ? nfProfile.getNfInstanceName()
                                                                                                                   : instance.getName());

        nfProfile.getNfSetId().forEach(result::addNfSetIdListItem);
        nfProfile.getNsi().forEach(result::addNsiListItem);

        if (nfProfile.getScpDomain() != null && !nfProfile.getScpDomain().isEmpty())
        {
            if (nfType.equalsIgnoreCase(NFType.SCP))
            {
                nfProfile.getScpDomain().forEach(result::addScpDomainsItem);
            }
            else
            {
                result.addScpDomainsItem(nfProfile.getScpDomain().get(0));
            }
        }

        if (!nfProfile.getNfService().isEmpty())
        {
            // Use of nf-services is deprecated in R16, but for backward compatibility
            // reasons we keep on using it in addition to nf-service-list (which, in fact,
            // is a map ;-) )
            result.setNfServiceList(nfProfile.getNfService().stream().collect(Collectors.toMap(NfService::getServiceInstanceId, v ->
            {
                // NF Pools are only used if associated-nf-pool-ref is configured under the
                // nf-profile
                List<NfPool> nfPools = null;

                if (nfProfile.getAssociatedNfPoolRef() != null && !nfProfile.getAssociatedNfPoolRef().isEmpty())
                {
                    final com.ericsson.sc.scp.model.NfInstance nfInstance = (com.ericsson.sc.scp.model.NfInstance) instance;
                    nfPools = nfInstance.getNfPool();
                }

                final NFService service = this.buildNFService(nfProfile, serviceAddrs, v, nfPools);
                result.addNfServicesItem(service);
                return service;
            })));
        }

        result.setNfStatus(nfProfile.getAdminState() == null || nfProfile.getAdminState() == AdminState.ACTIVE ? NFStatus.REGISTERED : NFStatus.UNDISCOVERABLE);
        result.setNfType(nfType);

        nfProfile.getPlmn().forEach(r -> result.addPlmnListItem(this.buildPlmnId(r)));

        result.setPriority(nfProfile.getServicePriority());

        if (nfProfile.getSnssai1().isEmpty())
            nfProfile.getSnssai().forEach(r -> result.addSNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));
        else
            nfProfile.getSnssai1().forEach(r -> result.addSNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));

        switch (nfType)
        {
            case NFType.BSF:
            {
                // nf-profile -> bsf-info takes precedence over (deprecated) nf-profile ->
                // nf-specific-info -> bsf-info
                if (nfProfile.getBsfInfo() != null && this.hasBsfInfoData(nfProfile.getBsfInfo()))
                    result.setBsfInfo(this.buildBsfInfo(nfProfile.getBsfInfo()));
                else if (nfProfile.getNfSpecificInfo() != null && nfProfile.getNfSpecificInfo().getBsfInfo() != null)
                    result.setBsfInfo(this.buildBsfInfoDeprecated(nfProfile.getNfSpecificInfo().getBsfInfo()));

                break;
            }

            case NFType.CHF:
            {
                // nf-profile -> chf-info takes precedence over (deprecated) nf-profile ->
                // nf-specific-info -> chf-info
                if (nfProfile.getChfInfo() != null && this.hasChfInfoData(nfProfile.getChfInfo()))
                    result.setChfInfo(this.buildChfInfo(nfProfile.getChfInfo()));
                else if (nfProfile.getNfSpecificInfo() != null && nfProfile.getNfSpecificInfo().getChfInfo() != null)
                    result.setChfInfo(this.buildChfInfo(nfProfile.getNfSpecificInfo().getChfInfo()));

                break;
            }

            case NFType.SCP:
            {
                final IfServiceAddress serviceAddrUsed = Utils.getByName(instance.getServiceAddress(), nfProfile.getServiceAddressRef());

                if (nfProfile.getScpInfo() != null && this.hasScpInfoData(nfProfile.getScpInfo()))
                    result.setScpInfo(this.buildScpInfo(nfProfile.getScpInfo(), serviceAddrUsed, instance.getServiceAddress()));
                else
                    result.setScpInfo(this.buildScpInfoWithPorts(serviceAddrUsed));

                break;
            }

            case NFType.SEPP:
            {
                if (instance instanceof com.ericsson.sc.sepp.model.NfInstance)
                {
                    final IfServiceAddress serviceAddrUsed = Utils.getByName(instance.getServiceAddress(), nfProfile.getServiceAddressRef());
                    result.setSeppInfo(this.buildSeppInfo(nfProfile.getSeppInfo(), (com.ericsson.sc.sepp.model.NfInstance) instance, serviceAddrUsed));
                }

                break;
            }

            default:
                break;
        }

        return this.postProcessResult(result, instance);
    }

    protected boolean hasBsfInfoData(com.ericsson.sc.bsf.model.BsfInfo bsfInfo)
    {
        return (!bsfInfo.getDnn().isEmpty() || !bsfInfo.getIpDomain().isEmpty() || !bsfInfo.getIpv4AddrRange().isEmpty()
                || !bsfInfo.getIpv6PrefixRange().isEmpty());
    }

    protected boolean hasChfInfoData(com.ericsson.sc.nfm.model.ChfInfo chfInfo)
    {
        return (!chfInfo.getGpsiRange().isEmpty() || !chfInfo.getPlmnRange().isEmpty() || !chfInfo.getSupiRange().isEmpty());
    }

    protected boolean hasScpInfoData(com.ericsson.sc.scp.model.ScpInfo scpInfo)
    {
        return (!scpInfo.getScpDomainInfo().isEmpty() || scpInfo.getScpPrefix() != null || !scpInfo.getAddressDomain().isEmpty()
                || !scpInfo.getIpv4Address().isEmpty() || !scpInfo.getIpv6Prefix().isEmpty() || !scpInfo.getIpv4AddrRange().isEmpty()
                || !scpInfo.getIpv6PrefixRange().isEmpty() || !scpInfo.getServedNfSetId().isEmpty() || !scpInfo.getRemotePlmn().isEmpty()
                || scpInfo.getIpReachability() != null || scpInfo.getScpCapabilities() != null);
    }

    protected BsfInfo buildBsfInfo(final com.ericsson.sc.bsf.model.BsfInfo source)
    {
        final BsfInfo result = new BsfInfo();

        source.getDnn().forEach(result::addDnnListItem);
        source.getIpDomain().forEach(result::addIpDomainListItem);

        source.getIpv4AddrRange().forEach(r ->
        {
            Ipv4AddressRange ipv4Range = new Ipv4AddressRange();

            ipv4Range.setStart(r.getIpv4AddrStart());
            ipv4Range.setEnd(r.getIpv4AddrEnd());

            result.addIpv4AddressRangesItem(ipv4Range);
        });

        source.getIpv6PrefixRange().forEach(r ->
        {
            Ipv6PrefixRange ipv6PrefixRange = new Ipv6PrefixRange();

            ipv6PrefixRange.setStart(r.getIpv6PrefixStart());
            ipv6PrefixRange.setEnd(r.getIpv6PrefixEnd());

            result.addIpv6PrefixRangesItem(ipv6PrefixRange);
        });

        return result;
    }

    protected BsfInfo buildBsfInfoDeprecated(final BsfInfo__1 source)
    {
        final BsfInfo result = new BsfInfo();

        source.getDnn().forEach(result::addDnnListItem);
        source.getIpDomain().forEach(result::addIpDomainListItem);

        if (source.getIpv4AddrRange() != null)
        {
            Ipv4AddressRange l = new Ipv4AddressRange();
            l.setStart(source.getIpv4AddrRange().getIpv4AddrStart());
            l.setEnd(source.getIpv4AddrRange().getIpv4AddrEnd());
            result.addIpv4AddressRangesItem(l);
        }

        if (source.getIpv6PrefixRange() != null)
        {
            Ipv6PrefixRange l = new Ipv6PrefixRange();
            l.setStart(source.getIpv6PrefixRange().getIpv6PrefixStart());
            l.setEnd(source.getIpv6PrefixRange().getIpv6PrefixEnd());
            result.addIpv6PrefixRangesItem(l);
        }

        return result;
    }

    protected ChfInfo buildChfInfo(final com.ericsson.sc.nfm.model.ChfInfo source)
    {
        final ChfInfo result = new ChfInfo();

        source.getGpsiRange().forEach(r ->
        {
            IdentityRange l = new IdentityRange();
            l.setStart(r.getIdentityStart());
            l.setEnd(r.getIdentityEnd());
            l.setPattern(r.getPattern());
            result.addGpsiRangeListItem(l);
        });

        source.getPlmnRange().forEach(r ->
        {
            PlmnRange l = new PlmnRange();
            l.setStart(r.getPlmnStart());
            l.setEnd(r.getPlmnEnd());
            l.setPattern(r.getPattern());
            result.addPlmnRangeListItem(l);
        });

        source.getSupiRange().forEach(r ->
        {
            SupiRange l = new SupiRange();
            l.setStart(r.getSupiStart());
            l.setEnd(r.getSupiEnd());
            l.setPattern(r.getPattern());
            result.addSupiRangeListItem(l);
        });

        return result;
    }

    protected List<String> buildScpCapabilities(final com.ericsson.sc.scp.model.ScpCapabilities source)
    {
        if (source == null)
            return null;

        final List<String> scpCapabilities = new ArrayList<>();

        if (source.getIndirectComWithDelegDisc() != null && source.getIndirectComWithDelegDisc().equals(Boolean.TRUE))
            scpCapabilities.add(ScpCapability.INDIRECT_COM_WITH_DELEG_DISC);

        return scpCapabilities;
    }

    protected ScpInfo buildScpInfo(final com.ericsson.sc.scp.model.ScpInfo source,
                                   IfServiceAddress serviceAddrNfProfile,
                                   List<IfServiceAddress> serviceAddress)
    {
        final ScpInfo result = new ScpInfo();

        source.getScpDomainInfo()
              .forEach(scpDomainInfoConf -> result.putScpDomainInfoListItem(scpDomainInfoConf.getScpDomainRef(),
                                                                            this.buildScpDomainInfo(scpDomainInfoConf, serviceAddress)));

        result.setScpCapabilities(buildScpCapabilities(source.getScpCapabilities()));
        result.setScpPrefix(source.getScpPrefix());

        if (serviceAddrNfProfile.getPort() != null)
            result.putScpPortsItem("http", serviceAddrNfProfile.getPort());

        if (serviceAddrNfProfile.getTlsPort() != null)
            result.putScpPortsItem("https", serviceAddrNfProfile.getTlsPort());

        source.getAddressDomain().forEach(result::addAddressDomainsItem);
        source.getIpv4Address().forEach(result::addIpv4AddressesItem);
        source.getIpv6Prefix().forEach(result::addIpv6PrefixesItem);

        source.getIpv4AddrRange().forEach(r ->
        {
            Ipv4AddressRange ipv4Range = new Ipv4AddressRange();

            ipv4Range.setStart(r.getIpv4AddrStart());
            ipv4Range.setEnd(r.getIpv4AddrEnd());

            result.addIpv4AddrRangesItem(ipv4Range);
        });

        source.getIpv6PrefixRange().forEach(r ->
        {
            Ipv6PrefixRange ipv6PrefixRange = new Ipv6PrefixRange();

            ipv6PrefixRange.setStart(r.getIpv6PrefixStart());
            ipv6PrefixRange.setEnd(r.getIpv6PrefixEnd());

            result.addIpv6PrefixRangesItem(ipv6PrefixRange);
        });

        source.getServedNfSetId().forEach(result::addServedNfSetIdListItem);

        source.getRemotePlmn().forEach(plmn ->
        {
            PlmnId remotePlmn = new PlmnId();

            remotePlmn.setMcc(plmn.getMcc());
            remotePlmn.setMnc(plmn.getMnc());

            result.addRemotePlmnListItem(remotePlmn);
        });

        if (source.getIpReachability() != null)
        {
            final String ipReachability = source.getIpReachability().toUpperCase();

            switch (ipReachability)
            {
                case "IPV4":
                    result.setIpReachability(IpReachability.IPV4);
                    break;

                case "IPV6":
                    result.setIpReachability(IpReachability.IPV6);
                    break;

                default:
                    break;
            }
        }

        return result;
    }

    protected ScpInfo buildScpInfoWithPorts(IfServiceAddress serviceAddrNfProfile)
    {
        final ScpInfo result = new ScpInfo();

        if (serviceAddrNfProfile.getPort() != null)
            result.putScpPortsItem("http", serviceAddrNfProfile.getPort());

        if (serviceAddrNfProfile.getTlsPort() != null)
            result.putScpPortsItem("https", serviceAddrNfProfile.getTlsPort());

        return result;
    }

    protected ScpDomainInfo buildScpDomainInfo(final com.ericsson.sc.scp.model.ScpDomainInfo source,
                                               List<IfServiceAddress> serviceAddress)
    {
        final ScpDomainInfo result = new ScpDomainInfo();

        if (source.getServiceAddressRef() != null && !source.getServiceAddressRef().isEmpty())
        {
            final IfServiceAddress serviceAddrUsed = Utils.getByName(serviceAddress, source.getServiceAddressRef().get(0));

            result.setScpFqdn(serviceAddrUsed.getFqdn());

            this.buildScpIpEndpoints(serviceAddrUsed).forEach(result::addScpIpEndPointsItem);

            if (serviceAddrUsed.getPort() != null)
                result.putScpPortsItem("http", serviceAddrUsed.getPort());

            if (serviceAddrUsed.getTlsPort() != null)
                result.putScpPortsItem("https", serviceAddrUsed.getTlsPort());
        }

        result.setScpPrefix(source.getScpPrefix());

        return result;
    }

    // This function creates the sepp-info data which are included under the
    // NfProfile during the sepp registration to NRF
    protected SeppInfo buildSeppInfo(final com.ericsson.sc.sepp.model.SeppInfo source,
                                     com.ericsson.sc.sepp.model.NfInstance nfInstance,
                                     IfServiceAddress serviceAddrNfProfile)
    {
        final SeppInfo result = new SeppInfo();

        if (serviceAddrNfProfile.getPort() != null)
            result.putSeppPortsItem("http", serviceAddrNfProfile.getPort());

        if (serviceAddrNfProfile.getTlsPort() != null)
            result.putSeppPortsItem("https", serviceAddrNfProfile.getTlsPort());

        if (source != null)
        {
            if (source.getSeppPrefix() != null && !source.getSeppPrefix().isEmpty())
                result.setSeppPrefix(source.getSeppPrefix());

            source.getRemotePlmn().stream().filter(Objects::nonNull).forEach(plmn ->
            {
                PlmnId remotePlmn = new PlmnId();

                remotePlmn.setMcc(plmn.getMcc());
                remotePlmn.setMnc(plmn.getMnc());

                result.addRemotePlmnListItem(remotePlmn);
            });
        }

        if (result.getRemotePlmnList() == null || result.getRemotePlmnList().isEmpty())
        {
            this.getAllTheRemotePlmnIds(nfInstance).stream().filter(Objects::nonNull).forEach(result::addRemotePlmnListItem);
        }

        return result;
    }

    // This function gets all the PlmnIds of each Roaming Partner with the
    // N32c enabled, only if PlmnIds are not included under seppInfo or seppInfo is
    // not configured
    protected List<PlmnId> getAllTheRemotePlmnIds(com.ericsson.sc.sepp.model.NfInstance nfInstance)
    {
        List<PlmnId> plmnIdList = new ArrayList<>();

        nfInstance.getExternalNetwork().stream().forEach(nw -> nw.getRoamingPartner().forEach(rp ->
        {
            if (rp.getN32C() != null && (Boolean.TRUE.equals(rp.getN32C().getEnabled())))
            {
                final AllowPlmn allowPlmn = rp.getN32C().getAllowPlmn();

                if (allowPlmn != null)
                {
                    PlmnId remotePlmn = new PlmnId();
                    remotePlmn.setMcc(allowPlmn.getPrimaryIdMcc());
                    remotePlmn.setMnc(allowPlmn.getPrimaryIdMnc());
                    plmnIdList.add(remotePlmn);

                    if (allowPlmn.getAdditionalId() != null && !allowPlmn.getAdditionalId().isEmpty())
                    {
                        allowPlmn.getAdditionalId().stream().forEach(addId ->
                        {
                            PlmnId remoteAddPlmn = new PlmnId();
                            remoteAddPlmn.setMcc(addId.getMcc());
                            remoteAddPlmn.setMnc(addId.getMnc());
                            plmnIdList.add(remoteAddPlmn);
                        });
                    }
                }
            }
        }));

        return plmnIdList;
    }

    protected List<IpEndPoint> buildIpEndpoints(final IfServiceAddress source,
                                                Scheme scheme)
    {
        final List<IpEndPoint> result = new ArrayList<>();
        final int port = scheme != null && scheme == Scheme.HTTP ? source.getPort() : source.getTlsPort();

        if (source.getIpv4Address() != null)
        {
            final IpEndPoint ep = new IpEndPoint();

            ep.setPort(port);
            ep.setIpv4Address(source.getIpv4Address());
            ep.setTransport(TransportProtocol.TCP);

            result.add(ep);
        }

        if (source.getIpv6Address() != null)
        {
            final IpEndPoint ep = new IpEndPoint();

            ep.setPort(port);
            ep.setIpv6Address(source.getIpv6Address());
            ep.setTransport(TransportProtocol.TCP);

            result.add(ep);
        }

        return result;
    }

    protected List<IpEndPoint> buildScpIpEndpoints(final IfServiceAddress source)
    {
        final List<IpEndPoint> result = new ArrayList<>();

        if (source.getIpv4Address() != null)
        {
            final IpEndPoint ep = new IpEndPoint();

            ep.setIpv4Address(source.getIpv4Address());
            ep.setTransport(TransportProtocol.TCP);

            result.add(ep);
        }

        if (source.getIpv6Address() != null)
        {
            final IpEndPoint ep = new IpEndPoint();

            ep.setIpv6Address(source.getIpv6Address());
            ep.setTransport(TransportProtocol.TCP);

            result.add(ep);
        }

        return result;
    }

    protected <T extends IfServiceAddress> NFService buildNFService(final com.ericsson.sc.nfm.model.NfProfile nfProfile,
                                                                    final Map<String, List<T>> serviceAddrs,
                                                                    final com.ericsson.sc.nfm.model.NfService source,
                                                                    final List<com.ericsson.sc.scp.model.NfPool> nfPools)
    {
        final NFService result = new NFService();

        source.getAllowedNfDomain().forEach(result::addAllowedNfDomainsItem);
        source.getAllowedNfType().forEach(r -> result.addAllowedNfTypesItem(r.toUpperCase()));

        if (source.getAllowedNssai1().isEmpty())
            source.getAllowedNssai().forEach(r -> result.addAllowedNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));
        else
            source.getAllowedNssai1().forEach(r -> result.addAllowedNssaisItem(this.buildSnssai(r.getSst(), r.getSd())));

        if (!source.getAllowedOperationsPerNfInstance().isEmpty())
            result.setAllowedOperationsPerNfInstance(source.getAllowedOperationsPerNfInstance()
                                                           .stream()
                                                           .collect(Collectors.toMap(AllowedOperationsPerNfInstance::getNfInstanceId,
                                                                                     AllowedOperationsPerNfInstance::getOperation)));

        if (!source.getAllowedOperationsPerNfType().isEmpty())
            result.setAllowedOperationsPerNfType(source.getAllowedOperationsPerNfType()
                                                       .stream()
                                                       .collect(Collectors.toMap(AllowedOperationsPerNfType::getNfType,
                                                                                 AllowedOperationsPerNfType::getOperation)));

        source.getAllowedPlmn().forEach(r -> result.addAllowedPlmnsItem(this.buildPlmnId(r)));

        result.setApiPrefix(source.getApiPrefix());

        // Capacity aggregation of the CHF producers
        if (nfPools != null && !nfPools.isEmpty())
        {
            result.setCapacity(this.getProducersCapacity(nfProfile.getAssociatedNfPoolRef().get(0), source, nfPools));
        }
        // Capacity based on the configured value
        else
        {
            result.setCapacity(source.getCapacity());
        }

        serviceAddrs.get(nfProfile.getName() + "," + source.getServiceInstanceId()).forEach(serviceAddr ->
        {
            result.setFqdn(serviceAddr.getFqdn());
            this.buildIpEndpoints(serviceAddr, source.getScheme()).forEach(result::addIpEndPointsItem);
        });

        result.setLoad(0); // value is patched before it is sent to the NRF

        final String nfServiceStatus = source.getAdminState() == null || source.getAdminState() == AdminState.ACTIVE ? NFServiceStatus.REGISTERED
                                                                                                                     : NFServiceStatus.UNDISCOVERABLE;
        result.setNfServiceStatus(nfProfile.getAdminState() == null || nfProfile.getAdminState() == AdminState.ACTIVE ? nfServiceStatus
                                                                                                                      : NFServiceStatus.UNDISCOVERABLE);

        result.setOauth2Required(source.getOauth2Required());
        result.setPriority(source.getServicePriority());
        result.setRecoveryTime(source.getRecoveryTime() != null ? source.getRecoveryTime().toInstant().atOffset(ZoneOffset.UTC) : null);
        result.setScheme(source.getScheme() != null ? source.getScheme().toString() : null); // TODO: set scheme based on TLS usage
        result.setServiceInstanceId(source.getServiceInstanceId());
        result.setServiceName(source.getServiceName() != null ? source.getServiceName().toString() : null);

        if (source.getServiceVersion() != null)
        {
            source.getServiceVersion()
                  .forEach(version -> result.addVersionsItem(this.buildNFServiceVersion(version.getApiFullVersion(), version.getApiVersionInUri())));
        }

        result.setSupportedFeatures(source.getSupportedFeatures());

        return result;
    }

    protected NFServiceVersion buildNFServiceVersion(final String apiFullVersion,
                                                     final String apiVersionInUri)
    {
        final NFServiceVersion result = new NFServiceVersion();
        result.setApiFullVersion(apiFullVersion);
        result.setApiVersionInUri(apiVersionInUri);
        return result;
    }

    protected PlmnId buildPlmnId(final com.ericsson.sc.nfm.model.AllowedPlmn source)
    {
        final PlmnId result = new PlmnId();
        result.setMcc(source.getMcc());
        result.setMnc(source.getMnc());
        return result;
    }

    protected PlmnId buildPlmnId(final com.ericsson.sc.nfm.model.Plmn source)
    {
        final PlmnId result = new PlmnId();
        result.setMcc(source.getMcc());
        result.setMnc(source.getMnc());
        return result;
    }

    protected ExtSnssai buildSnssai(final int sst,
                                    final String sd)
    {
        final ExtSnssai result = new ExtSnssai();
        result.setSst(sst);
        result.setSd(sd.equals("-") ? null : sd); // Treat special character '-' indicating that sd is not defined.

        if (result.getSd() != null)
            result.setWildcardSd(WildcardSdEnum.TRUE);

        return result;
    }

    /**
     * Aggregates the capacity of discovered services in order to be used in the
     * capacity value for NRF registration
     * 
     * @param associatedNfPoolRef the NF Pool referenced in nf-profile that is going
     *                            to be registered
     * @param service             the service for which the capacity is going to be
     *                            calculated
     * @param nfPools             the configured Nf Pools
     * @return the capacity of primary region service or the average capacity of the
     *         backup regions
     */
    protected Integer getProducersCapacity(final String associatedNfPoolRef,
                                           final com.ericsson.sc.nfm.model.NfService service,
                                           final List<com.ericsson.sc.scp.model.NfPool> nfPools)
    {
        log.debug("Aggregating the capacity of producers.");

        Set<String> selectedSvcNames = new HashSet<>();
        NfPool nfPoolUsed = Utils.getByName(nfPools, associatedNfPoolRef);
        List<PriorityGroup> priorityGroups = nfPoolUsed.getPriorityGroup();

        if (priorityGroups != null && !priorityGroups.isEmpty())
        {
            PriorityGroup highestPriorityGroup = Collections.min(priorityGroups, Comparator.comparing(PriorityGroup::getPriority));
            log.debug("Priority-group identifying the primary region ={}", highestPriorityGroup.getName());
            Stream<IfTypedNfInstance> discoveredNfInstances = Utils.streamIfExists(nfPoolUsed.getNfPoolDiscovery())
                                                                   .<IfTypedNfInstance>flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()));
            List<IfTypedNfService> filteredServices = NfConditionParser.parse(highestPriorityGroup.getNfMatchCondition(),
                                                                              discoveredNfInstances,
                                                                              selectedSvcNames);
            log.debug("filteredServices size={}", filteredServices.size());
            for (final IfTypedNfService filteredSvc : filteredServices)
            {
                if (filteredSvc.getName().equals(service.getServiceName().toString()))
                {
                    log.info("New capacity (based on primary region) for NF-service {} is {}", service.getServiceName(), filteredSvc.getCapacity());
                    return filteredSvc.getCapacity();
                }
            }
        }
        // If no instance with matching service-name is discovered in primary region or
        // no priority-group is defined in the associated-nf-pool-ref then the average
        // of capacities of discovered instances/services in backup regions is
        // calculated and reported

        Integer averageCapacity = calculateAverageCapacity(service, nfPoolUsed);
        log.info("New capacity (based on backup regions) for NF-service {} is {}", service.getServiceName(), averageCapacity);

        return averageCapacity;
    }

    /**
     * Gets the average of the capacities of the discovered services
     * 
     * @param service    the service for which the capacity is going to be
     *                   calculated
     * @param nfPoolUsed the NF Pool used for the capacity aggregation
     * @return the average capacity of the services
     */
    protected Integer calculateAverageCapacity(final com.ericsson.sc.nfm.model.NfService service,
                                               final com.ericsson.sc.scp.model.NfPool nfPoolUsed)
    {
        final Map<String, Integer> serviceCapacities = new HashMap<>();

        for (final DiscoveredNfInstance discNfInstance : getDiscoveredNfInstances(nfPoolUsed))
        {
            if (discNfInstance.getDiscoveredNfService() != null)
            {
                for (final DiscoveredNfService discoveredSvc : discNfInstance.getDiscoveredNfService())
                {
                    if (discoveredSvc.getName().equals(service.getServiceName().toString()) && discoveredSvc.getCapacity() != null)
                    {
                        log.debug("Capacity {} of discovered-nf-instance {} is used in the aggregation.",
                                  discoveredSvc.getCapacity(),
                                  discNfInstance.getName());
                        serviceCapacities.put(discNfInstance.getName(), discoveredSvc.getCapacity());
                    }
                }
            }
        }

        return (int) serviceCapacities.values().stream().mapToDouble(Integer::doubleValue).average().orElse(0);
    }

    /**
     * @param nfPoolUsed the NF Pool that is scanned
     * @return the discovered Instances found in this NF Pool
     */
    protected List<DiscoveredNfInstance> getDiscoveredNfInstances(final com.ericsson.sc.scp.model.NfPool nfPoolUsed)
    {
        List<DiscoveredNfInstance> discoveredNfInstances = new ArrayList<>();

        if (nfPoolUsed.getNfPoolDiscovery() != null)
        {
            for (final NfPoolDiscovery poolDisc : nfPoolUsed.getNfPoolDiscovery())
            {
                if (poolDisc.getDiscoveredNfInstance() != null)
                {
                    for (final DiscoveredNfInstance discNfInstance : poolDisc.getDiscoveredNfInstance())
                    {
                        discoveredNfInstances.add(discNfInstance);
                    }
                }
            }
        }
        return discoveredNfInstances;
    }

    protected NFProfile postProcessResult(final NFProfile result,
                                          final IfGenericNfInstance instance)
    {
        log.debug("COMMON NFProfileBuilder");
        return result;
    }
}
