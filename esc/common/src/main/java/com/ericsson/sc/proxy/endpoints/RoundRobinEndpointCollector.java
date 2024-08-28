/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy.endpoints;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.sc.glue.*;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.CommonConfigUtils.SeppDatum;
import com.ericsson.sc.expressionparser.NfConditionParser;
import com.ericsson.sc.expressionparser.ScpConditionParser;
import com.ericsson.sc.expressionparser.SeppConditionParser;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxyal.proxyconfig.MetadataListValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataMapValue;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.NfPool.PreferredIpFamily;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundRobinEndpointCollector extends EndpointCollector
{
    private static final Logger log = LoggerFactory.getLogger(RoundRobinEndpointCollector.class);

    /*
     * Used only by SCP.
     */
    private final boolean keepOriginalAuthorityHeader;
    /*
     * Used only by SEPP. Used to assign metadata to endpoints depending on the
     * support of target-api-root by the RP.
     */
    private final Optional<RoamingPartner> rp;

    /*
     * Used only by SEPP.
     */
    private boolean n32cEnabled = false;
    private Map<String, SeppDatum> seppData;
    private Map<IfTypedNfAddressProperties, Set<IfTypedNfAddressProperties>> servicesBehindScps = new HashMap<>();
    private final Optional<String> failoverProfileName;

    public RoundRobinEndpointCollector(IfNfInstance configInst,
                                       IfNfPool pool,
                                       boolean keepOriginalAuthorityHeader,
                                       Optional<String> failoverProfileName)
    {
        super(configInst, pool);
        this.keepOriginalAuthorityHeader = keepOriginalAuthorityHeader;
        this.rp = Optional.empty();
        this.failoverProfileName = failoverProfileName;
    }

    public RoundRobinEndpointCollector(IfNfPool pool,
                                       Optional<RoamingPartner> rp,
                                       IfNfInstance configInst,
                                       Optional<String> failoverProfileName)
    {
        super(configInst, pool);
        this.keepOriginalAuthorityHeader = false;
        this.rp = rp;
        this.failoverProfileName = failoverProfileName;
    }

    /**
     * The creation of endpoints is split into the following parts:
     * <p>
     * - Get all the NF instances contained in a pool (if it's sepp and n32c is
     * enabled, only take sepps with valid handshakes)
     * <p>
     * - Categorize the derived NF-services based on configured priority groups (if
     * any) while reserving the last priority for nf services not matching any prio
     * group condition If a priority group is serviced by one or more SCPs, replace
     * its NFs with those of the SCPs In the same manner, if there are non compliant
     * services and SCPs defined on pool level, replace them with the SCPs. In case
     * substitutions of NFs with SCPs happen, {@link #processPriorityGroups(List)}
     * also populates the {@link #servicesBehindScps} map with the information later
     * used to construct the cluster metadata for indirect routing and interplmnfqdn
     * functionalities.
     * <p>
     * - A further sub-prioritization occurs where NF services are sub-prioritized
     * based on configured priorities while respecting envoy priorities, i.e.
     * starting from 0 and increasing without gaps.
     * <p>
     * - Finally, ProxyEndpoints are created from the prioritized services
     */
    @Override
    public void createEndpoints()
    {
        log.debug("Creating RR Endpoints for pool {}", pool.getName());
        // Primary Endpoints of RR Clusters

        List<IfTypedNfInstance> nfInstances;

        if (this.n32cEnabled)
        {
            var allNfInstances = getAllNfInstances(this.pool, this.configInst);

            // first get all sepp names, then remove elements which are not active.
            // Check only for active static-nf-instances which referenced by a RP with N32c
            // enabled.
            log.debug("nfInstanceNames or seppInstancesNames referenced by RP with n32c enabled: {} ", this.seppData.keySet());

            var activeNfInstanceNames = this.seppData.keySet()
                                                     .stream()
                                                     .filter(nfInstanceRef -> this.seppData.get(nfInstanceRef).getSeppStatus().equalsIgnoreCase("active"))
                                                     .toList();
            log.debug("nfInstanceNames with operational-state active: {} ", activeNfInstanceNames);

            nfInstances = allNfInstances.stream().filter(nfInstance -> activeNfInstanceNames.contains(nfInstance.getName())).collect(Collectors.toList());
            log.info("active nfInstances by n32c: {} ", nfInstances);
        }
        else
        {
            nfInstances = getAllNfInstances(this.pool, this.configInst);
        }

        List<IfTypedScpInstance> discoveredProxyInstances = ConfigHelper.getDiscoveredScpInstances(pool);

        assignRpServiceType(nfInstances);

        // If at least one prio group contains the new syntax, we use broken down
        // endpoints
        boolean newSyntax = false;

        for (var prio : pool.getPriorityGroup())
        {
            if (prio.getNfMatchCondition() == null || prio.getNfMatchCondition().isBlank())
                continue;

            final Matcher m = Pattern.compile("nfdata\\.[^.]+\\..+").matcher(prio.getNfMatchCondition());
            if (m.find())
            {
                newSyntax = true;
                break;
            }
        }

        var out = processPriorityGroups(nfInstances, discoveredProxyInstances, newSyntax);
        var nfServicesBySpPrios = out.getFirst();
        var selectedSvcs = out.getSecond();// .getFirst();
        // var proxyServiceListnonCompl = out.getSecond().getSecond();

        // Endpoints not used by priority groups
        if (pool.getAddNonMatchingAsLowestPriority() || pool.getPriorityGroup().isEmpty())
        {
            var lastPrio = nfServicesBySpPrios.isEmpty() ? 0 : nfServicesBySpPrios.lastKey() + 1;
            var nonCompServices = processNonMatchingInstances(pool, nfInstances, discoveredProxyInstances, selectedSvcs, this.configInst, newSyntax);
            if (!nonCompServices.isEmpty())
                nfServicesBySpPrios.put(lastPrio, nonCompServices.stream());
        }

        SortedMap<Integer, List<IfTypedNfAddressProperties>> nfServicesByEnvoyPrios = convertToEnvoyPriorities(0, nfServicesBySpPrios);

        var endpoints = this.createEndpointsForServices(nfServicesByEnvoyPrios.entrySet(), pool.getName(), this.keepOriginalAuthorityHeader);
        createClusterMetadata();
        this.endpoints.addAll(endpoints);
        setEndpointVTapFlag(this.getEndpoints(), this.pool.getName());
    }

    private void createClusterMetadata()
    {
        servicesBehindScps.forEach((nfService,
                                    scpSvces) ->
        {
            var scpSvcesHandler = new AddressHandler(scpSvces);
            var nfServiceAddressHandler = new AddressHandler(nfService.getAddress());

            nfServiceAddressHandler.getElementsAsKey().forEach(elem ->
            {
                var mdList = scpSvcesHandler.getElmentsAsValue().stream().map(MetadataMapValue::new).toList();

                var nestedMap = getClusterMetadata().getMetadataMap().get(MetaDataType.CLUSTER);

                if (nestedMap == null)
                {
                    getClusterMetadata().addMetadata(MetaDataType.CLUSTER, elem, new MetadataListValue<>(mdList));
                }
                else
                {
                    nestedMap.putIfAbsent(elem, new MetadataListValue<>(mdList));
                }
            });
        });

        Map<String, String> endpointPolicy = new HashMap<>();

        if (pool instanceof NfPool)  // SCP
        {
            PreferredIpFamily preferredIpFamily = ((NfPool) pool).getPreferredIpFamily();
            if (NfPool.PreferredIpFamily.IPV4.equals(preferredIpFamily))
            {
                endpointPolicy.put("preferred_ip_family", "IPv4");
            }
            else if (NfPool.PreferredIpFamily.IPV6.equals(preferredIpFamily))
            {
                endpointPolicy.put("preferred_ip_family", "IPv6");
            }
        }
        else // SEPP
        {
            com.ericsson.sc.sepp.model.NfPool.PreferredIpFamily preferredIpFamily = ((com.ericsson.sc.sepp.model.NfPool) pool).getPreferredIpFamily();
            if (com.ericsson.sc.sepp.model.NfPool.PreferredIpFamily.IPV4.equals(preferredIpFamily))
            {
                endpointPolicy.put("preferred_ip_family", "IPv4");
            }
            else if (com.ericsson.sc.sepp.model.NfPool.PreferredIpFamily.IPV6.equals(preferredIpFamily))
            {
                endpointPolicy.put("preferred_ip_family", "IPv6");
            }
        }

        if (failoverProfileName.isPresent())
        {
            var failProf = Utils.getByName(this.configInst.getFailoverProfile(), failoverProfileName.get());
            endpointPolicy.put("preferred_host_retry_multiple_address", failProf.getPreferredHostRetryMultipleAddresses().toString());
        }
        else
        {
            endpointPolicy.put("preferred_host_retry_multiple_address", "true");

        }

        var mdMap = new MetadataMapValue(endpointPolicy);

        getClusterMetadata().addMetadata(MetaDataType.CLUSTER, METADATA.ENDPOINT_POLICY, mdMap);

    }

    /**
     * If these are RP endpoints, assign the correct NfInstance nfType for
     * target-api-root support by that RP.
     * 
     * @param nfInstances All nfInstances of the pool. Is modified if RP exists.
     */
    private void assignRpServiceType(List<IfTypedNfInstance> nfInstances)
    {
        if (rp.isEmpty())
            return;

        if (this.n32cEnabled)
        {
            nfInstances.forEach(inst ->
            {
                if (this.seppData.get(inst.getName()).getSeppTarSupport().toString().equals("true"))
                {
                    inst.fetchNfService().forEach(svc -> svc.setNfType("seppTar"));
                }
                else
                {
                    inst.fetchNfService().forEach(svc -> svc.setNfType("seppNone"));
                }

            });
        }
        else
        {
            nfInstances.forEach(inst -> inst.fetchNfService()
                                            .forEach(svc -> svc.setNfType(rp.get().getSupportsTargetApiroot().toString().equals("true") ? "seppTar"
                                                                                                                                        : "seppNone")));
        }
    }

    /**
     * Return a map of priorities and nfServices, extracted from the priority groups
     * of the pool.
     * 
     * @param nfInstances All the nfInstances of the target pool.
     * @return
     */
    Pair<SortedMap<Integer, Stream<IfTypedNfAddressProperties>>, List<IfTypedNfAddressProperties>> processPriorityGroups(List<IfTypedNfInstance> nfInstances,
                                                                                                                         List<IfTypedScpInstance> discoveredProxyInstances,
                                                                                                                         boolean newSyntax)
    {
        SortedMap<Integer, Stream<IfTypedNfAddressProperties>> nfServicesBySpPrios = new TreeMap<>();

        List<IfTypedNfAddressProperties> selectedEndpoints = new ArrayList<>();

        for (var prioGroup : pool.getPriorityGroup())
        {
            if (nfServicesBySpPrios.containsKey(prioGroup.getPriority()))
            {
                // this constraint can be caught by the validator as well
                Object[] errMsgArgs = { pool.getName() };
                throw new BadConfigurationException("Pool {} has more than one prioGroup defined with the same prioGroup priority.", errMsgArgs);
            }

            List<IfTypedNfAddressProperties> servicesForPrioGroup = new ArrayList<>();
            List<IfTypedNfAddressProperties> filteredNfServices = new ArrayList<>();

            // If nf-match-condition is present, get NFs that match the condition
            // If the new match condition applies, breakdown the instances in order to get
            // the exact match of the endpoints, otherwise keep the deprecated handling
            if (prioGroup.getNfMatchCondition() != null)
                filteredNfServices = NfConditionParser.filterNfServices(prioGroup.getNfMatchCondition(),
                                                                        newSyntax ? ConfigHelper.breakDownEndPointsNfInst(nfInstances.stream()).stream()
                                                                                  : nfInstances.stream());

            selectedEndpoints.addAll(filteredNfServices);

            // All SCP and SEPP (static and discovered) svcs that belong in the prio group
            List<IfTypedNfAddressProperties> proxyServicesForPrioGroup = new ArrayList<>();

            // Find the Discovered SCPs domains that match the condition
            if (prioGroup.getScpMatchCondition() != null)
                proxyServicesForPrioGroup.addAll(ScpConditionParser.filterScpDomains(prioGroup.getScpMatchCondition(),
                                                                                     ConfigHelper.breakDownEndPointsScpInst(discoveredProxyInstances.stream())
                                                                                                 .stream()));

            // Find the Static SCPs that match the condition
            // If static scp reference exists under priority group use those scps (filtered
            // accordingly), otherwise filter the static scps on pool level
            if (prioGroup.getStaticScpInstanceDataRef() != null && !prioGroup.getStaticScpInstanceDataRef().isEmpty())
            {
                var staticScps = ConfigHelper.getStaticScpInstances(prioGroup.getStaticScpInstanceDataRef(), this.configInst);

                proxyServicesForPrioGroup.addAll(ScpConditionParser.filterStaticScps(prioGroup.getScpMatchCondition(),
                                                                                     ConfigHelper.breakDownEndPointsStaticScpInst(staticScps).stream()));

            }
            else if (nfInstances.isEmpty() && pool.getStaticScpInstanceDataRef() != null && prioGroup.getScpMatchCondition() != null)
            {
                var staticScps = ConfigHelper.getStaticScpInstances(pool.getStaticScpInstanceDataRef(), this.configInst);

                proxyServicesForPrioGroup.addAll(ScpConditionParser.filterStaticScps(prioGroup.getScpMatchCondition(),
                                                                                     ConfigHelper.breakDownEndPointsStaticScpInst(staticScps).stream()));

            }

            // Find the Static SEPPs that match the condition
            // If static sepp reference exists under priority group (applicable only for
            // SCP) use those sepps (filtered
            // accordingly), otherwise filter the static sepps on pool level
            if (prioGroup.getStaticSeppInstanceDataRef() != null && !prioGroup.getStaticSeppInstanceDataRef().isEmpty())
            {
                var staticSepps = ConfigHelper.getStaticSeppInstances(prioGroup.getStaticSeppInstanceDataRef(), this.configInst).stream();

                proxyServicesForPrioGroup.addAll(SeppConditionParser.filterStaticSepps(prioGroup.getSeppMatchCondition(),
                                                                                       ConfigHelper.breakDownEndPointsStaticSepp(staticSepps).stream()));
            }
            else if (nfInstances.isEmpty() && pool.getStaticSeppInstanceDataRef() != null && prioGroup.getSeppMatchCondition() != null)
            {
                var staticSepps = ConfigHelper.getStaticSeppInstances(pool.getStaticSeppInstanceDataRef(), this.configInst).stream();

                proxyServicesForPrioGroup.addAll(SeppConditionParser.filterStaticSepps(prioGroup.getSeppMatchCondition(),
                                                                                       ConfigHelper.breakDownEndPointsStaticSepp(staticSepps).stream()));

            }

            selectedEndpoints.addAll(proxyServicesForPrioGroup);

            // if there are SCP instances (static or discovered) (or static sepp instances)
            // in the prioGroup , use their services instead of the static or discovered
            // nf instances that match the constraint. Also populate scpsWithServices map
            // which is later used to build the cluster metadata that contain a mapping of
            // NF hostnames and ips and the SCPs that service them, for preferred and strict
            // routing scenarios
            if (!proxyServicesForPrioGroup.isEmpty())
            {
                filteredNfServices.forEach(filterService -> servicesBehindScps.merge(filterService,
                                                                                     new HashSet<>(proxyServicesForPrioGroup),
                                                                                     (s1,
                                                                                      s2) -> Stream.concat(s1.stream(), s2.stream())
                                                                                                   .collect(Collectors.toSet())));
                servicesForPrioGroup.addAll(proxyServicesForPrioGroup);
            }
            else // No proxy services in the prio group, use NF services
            {
                servicesForPrioGroup.addAll(filteredNfServices);
            }

            if (!servicesForPrioGroup.isEmpty())
            {
                nfServicesBySpPrios.put(prioGroup.getPriority(), servicesForPrioGroup.stream());
            }
        }

        log.debug("nfServicesBySpPrios: {}", nfServicesBySpPrios);
        log.debug("ServicesBehindScps: {}", servicesBehindScps);

        return Pair.of(nfServicesBySpPrios, selectedEndpoints);
    }

    /**
     * Make a last priority which either contains nf-instances that don't satisfy
     * any of the prioGroup constraints, i.e. nfInstances \ instanceNames or the
     * scp/sepp services of the static scp/sepp instances that are defined at pool
     * level. Update the SortedMap with the result as well as the scpsWithServices
     *
     * @param pool
     * @param maxPrio
     * @param nfInstances
     * @param selectedEndpoints
     */
    private List<IfTypedNfAddressProperties> processNonMatchingInstances(IfNfPool pool,
                                                                         List<IfTypedNfInstance> nfInstances,
                                                                         List<IfTypedScpInstance> discoveredScpInstances,
                                                                         List<IfTypedNfAddressProperties> selectedEndpoints,
                                                                         IfNfInstance nfInstance,
                                                                         boolean newSyntax)
    {

        var nfServicesForLastPrioInput = newSyntax ? ConfigHelper.breakDownEndPointsNfInst(nfInstances.stream()) : nfInstances;

        var nfServicesForLastPrio = nfServicesForLastPrioInput.stream()
                                                              .flatMap(inst -> inst.fetchNfService().stream().filter(svc -> !selectedEndpoints.contains(svc)))
                                                              .toList();

        // All SCP and SEPP (static and discovered) svcs that are left for the last
        // priority
        List<IfTypedNfAddressProperties> proxyServicesForLastPrio = new ArrayList<>();

        List<IfTypedNfAddressProperties> lastPrioServices = new ArrayList<>();

        // Find static SEPPs of pool
        if (pool.getStaticSeppInstanceDataRef() != null && !pool.getStaticSeppInstanceDataRef().isEmpty())
        {
            // In case the nf-pool referenced from a RP with n32c enabled then filter the
            // list of static-sepp-instaces and apply only the active static-sepp-instances
            // to envoy. This nf-pool contains only static-sepp-instances.
            if (this.n32cEnabled)
            {
                log.debug("seppInstancesNames referenced by RP with n32c enabled: {} ", this.seppData.keySet());

                var activeSeppInstanceNames = this.seppData.keySet()
                                                           .stream()
                                                           .filter(seppInstanceRef -> this.seppData.get(seppInstanceRef)
                                                                                                   .getSeppStatus()
                                                                                                   .equalsIgnoreCase("active"))
                                                           .toList();
                log.debug("seppInstanceNames with operational-state active: {} ", activeSeppInstanceNames);

                var sepps = ConfigHelper.getStaticSeppInstances(pool.getStaticSeppInstanceDataRef(), this.configInst)
                                        .stream()
                                        .filter(sepp -> activeSeppInstanceNames.contains(sepp.getName()))
                                        .toList();

                log.info("active seppInstances by n32c: {} ", sepps);
                proxyServicesForLastPrio.addAll(sepps);
            }
            else
            {

                proxyServicesForLastPrio.addAll(ConfigHelper.breakDownEndPointsStaticSepp(ConfigHelper.getStaticSeppInstances(pool.getStaticSeppInstanceDataRef(),
                                                                                                                              this.configInst)
                                                                                                      .stream())
                                                            .stream()
                                                            .filter(inst -> !selectedEndpoints.contains(inst))
                                                            .toList());
            }
        }

        // Add discovered SCP instances that are not filter under any prio group
        proxyServicesForLastPrio.addAll(ConfigHelper.breakDownEndPointsScpInst(discoveredScpInstances.stream())
                                                    .stream()
                                                    .flatMap(inst -> inst.fetchScpDomainInfo().stream().filter(domain -> !selectedEndpoints.contains(domain)))
                                                    .toList());

        if (pool.getStaticScpInstanceDataRef() != null)
        {
            var staticScps = ConfigHelper.getStaticScpInstances(pool.getStaticScpInstanceDataRef(), this.configInst);
            proxyServicesForLastPrio.addAll(ConfigHelper.breakDownEndPointsStaticScpInst(staticScps)
                                                        .stream()
                                                        .flatMap(inst -> Stream.concat(inst.fetchScpDomainInfo().stream(), inst.getStaticNfService().stream())
                                                                               .filter(domain -> !selectedEndpoints.contains(domain)))

                                                        .toList());
        }

        if (!proxyServicesForLastPrio.isEmpty())
        {
            nfServicesForLastPrio.forEach(filterService -> servicesBehindScps.merge(filterService,
                                                                                    new HashSet<>(proxyServicesForLastPrio),
                                                                                    (s1,
                                                                                     s2) -> Stream.concat(s1.stream(), s2.stream())
                                                                                                  .collect(Collectors.toSet())));
            lastPrioServices.addAll(proxyServicesForLastPrio);
        }
        else // No proxy services in the prio group, use NF services
        {
            lastPrioServices.addAll(nfServicesForLastPrio);
        }

        return lastPrioServices;
    }

    /**
     * Given a sorted map of streams of nfServices based on the prioGroup
     * priorities, returns a sorted map based on the envoy priorities to be used.
     * <p>
     * While some nfServices fall under the same prioGroup and it's configured
     * priority, further sub-prioritization can happen based on the configured
     * priority of the nfService itself.
     *
     * @return a {@link SortedMap} with lists of nfServices sorted according to
     *         their "envoy" priorities
     */
    private SortedMap<Integer, List<IfTypedNfAddressProperties>> convertToEnvoyPriorities(int startPriority,
                                                                                          SortedMap<Integer, Stream<IfTypedNfAddressProperties>> nfServicesBySpPrios)
    {

        SortedMap<Integer, List<IfTypedNfAddressProperties>> nfServicesByEnvoyPrios = new TreeMap<>();
        Integer envoyPrio = startPriority;
        nfServicesByEnvoyPrios.put(envoyPrio, new ArrayList<>());
        for (var entry : nfServicesBySpPrios.entrySet())
        {
            /*
             * Each entry contains NFs that belong to the same priority-group, sorted
             * according to their configured priorities (no configured priority/null is
             * considered lowest prio)
             */
            entry.getValue()
                 .sorted((s1,
                          s2) ->
                 {
                     if (s1.getPriority() == null)
                         return 1;

                     if (s2.getPriority() == null)
                         return -1;

                     return s1.getPriority().compareTo(s2.getPriority());
                 })
                 .forEach(l ->
                 {
                     var key = nfServicesByEnvoyPrios.lastKey();
                     var svcList = nfServicesByEnvoyPrios.get(key);
                     /**
                      * Further subprioritize the sorted list of NFs.
                      * <ul>
                      * <li>Example:
                      * <li>Input NFs priorities:
                      * <li>[1,1,2,null] -> nfServicesByEnvoyPrios[n] -> [1,1]
                      * <li>nfServicesByEnvoyPrios[n+1] -> [2] nfServicesByEnvoyPrios[n+2] -> [null]
                      * </ul>
                      */
                     if (svcList.isEmpty() || Objects.equals(svcList.get(svcList.size() - 1).getPriority(), l.getPriority()))
                     {
                         svcList.add(l);
                     }
                     else
                     {
                         nfServicesByEnvoyPrios.put((key + 1), new ArrayList<>(List.of(l)));
                     }
                 });
            // after we finish with nfServices for a certain prioGroup we change priority
            nfServicesByEnvoyPrios.put(nfServicesByEnvoyPrios.lastKey() + 1, new ArrayList<>());

        }
        // delete last empty priority from map
        nfServicesByEnvoyPrios.remove(nfServicesByEnvoyPrios.lastKey());
        log.debug("nfServicesByEnvoyPrios: {}", nfServicesByEnvoyPrios);

        return nfServicesByEnvoyPrios;
    }

    /**
     * Helper function used only by SEPP for N32-C purposes <a>
     * 
     * @param n32cEnabled
     * @param seppData
     * 
     */
    public void setN32cParameters(Boolean n32cEnabled,
                                  Map<String, SeppDatum> seppData)
    {
        this.n32cEnabled = n32cEnabled;
        this.seppData = seppData;
    }

    /**
     * The AddressHandler class is responsible for processing and managing network
     * addresses, specifically IP and FQDN tuples. It provides methods to handle
     * different types of network address entities and retrieve their elements as
     * keys and values for the cluster Metadata.
     *
     * This class supports two constructors: - AddressHandler(Set<IfTypedNfService>
     * nfs): Initializes the AddressHandler with a set of IfTypedNfService
     * instances, extracting and processing their addresses. -
     * AddressHandler(IfAddress addr): Initializes the AddressHandler with a single
     * IfAddress instance, processing its address.
     *
     * The processed address information is stored in the 'contents' set, where each
     * element is an IpFqdnTuple representing an IP address and an FQDN address with
     * optional ports.
     *
     * The IpFqdnTuple class is an inner class used to represent IP and FQDN tuples,
     * and it provides methods to access and filter these elements.
     *
     * This class also provides methods to retrieve the processed address elements:
     * - getElementsAsKey(): Returns a set of all IP and FQDN elements as keys. -
     * getElementsAsValue(): Returns a set of maps containing IP and FQDN elements
     * as values.
     *
     * Example usage: AddressHandler addressHandler = new AddressHandler(nfsSet);
     * Set<String> elementsAsKey = addressHandler.getElementsAsKey();
     * Set<Map<String, String>> elementsAsValue =
     * addressHandler.getElmentsAsValue();
     */
    class AddressHandler
    {
        private Set<IpFqdnTuple> contents = new HashSet<>();

        public AddressHandler(Set<IfTypedNfAddressProperties> nfs)

        {
            nfs.stream().map(IfTypedNfAddressProperties::getAddress).forEach(this::proccessEntity);
        }

        public AddressHandler(IfAddress addr)
        {
            proccessEntity(addr);
        }

        private void proccessEntity(IfAddress addr)
        {

            var fqdn = addr.getFqdn();
            var multEnd = addr.getMultipleIpEndpoint();
            var tuple = new IpFqdnTuple();
            var interplmnFqdn = addr.getInterPlmnFqdn();
            var defaultPort = addr.getScheme() == Scheme.HTTPS ? "443" : "80";

            if (multEnd != null && !multEnd.isEmpty())
            {
                multEnd.forEach(multIpEp ->
                {
                    var ips = Stream.concat(multIpEp.getIpv4Address().stream(), multIpEp.getIpv6Address().stream())
                                    .filter(s -> !s.isBlank())
                                    .collect(Collectors.toList());
                    var port = multIpEp.getPort() == null ? defaultPort : multIpEp.getPort().toString();

                    if (ips == null || ips.isEmpty())
                    {
                        tuple.fqdnPort = fqdn + ":" + port;
                        if (interplmnFqdn != null && !interplmnFqdn.isEmpty())
                        {
                            tuple.interplmnFqdnPort = interplmnFqdn + ":" + port;
                        }
                        contents.add(tuple);
                    }
                    else
                    {
                        ips.forEach(ip ->
                        {
                            var newTuple = new IpFqdnTuple();
                            newTuple.ipPort = CommonConfigUtils.formatIpv4Ipv6Address(ip) + ":" + port;
                            newTuple.ipFamily = ip.contains(":") ? "IPv6" : "IPv4";
                            if (fqdn != null && !fqdn.isEmpty())
                            {
                                newTuple.fqdnPort = fqdn + ":" + port;
                            }
                            if (interplmnFqdn != null && !interplmnFqdn.isEmpty())
                            {
                                newTuple.interplmnFqdnPort = interplmnFqdn + ":" + port;
                            }
                            contents.add(newTuple);
                        });
                    }
                });

            }
            else
            {
                tuple.fqdnPort = fqdn + ":" + defaultPort;
                if (interplmnFqdn != null && !interplmnFqdn.isEmpty())
                {
                    tuple.interplmnFqdnPort = interplmnFqdn + ":" + defaultPort;
                }
                contents.add(tuple);
            }
            log.debug("Address Handler contents:{}", contents);

        }

        class IpFqdnTuple
        {
            protected String ipPort = null;
            protected String fqdnPort = null;
            protected String interplmnFqdnPort = null;
            protected String ipFamily = null;

            protected Stream<String> getElements()
            {
                return Stream.of(ipPort, fqdnPort, interplmnFqdnPort).filter(Objects::nonNull);
            }

            @Override
            public String toString()
            {
                var ss = new StringJoiner(", ", "IpFqdnTuple: [", "]");
                if (ipPort != null)
                {
                    ss.add("ipPort= " + ipPort);
                }
                if (fqdnPort != null)
                {
                    ss.add("fqdnPort= " + fqdnPort);
                }

                if (interplmnFqdnPort != null)
                {
                    ss.add("interplmnFqdnPort= " + interplmnFqdnPort);
                }

                if (ipFamily != null)
                {
                    ss.add("ipFamily= " + ipFamily);
                }
                return ss.toString();
            }

        }

        @Override
        public String toString()
        {
            return "AddressHandler [contents=" + contents + "]";
        }

        public Set<String> getElementsAsKey()
        {
            return contents.stream().flatMap(IpFqdnTuple::getElements).map(elem -> elem.toLowerCase()).collect(Collectors.toSet());
        }

        public Set<Map<String, String>> getElmentsAsValue()
        {
            Set<Map<String, String>> retValue = new HashSet<>();
            contents.forEach(e ->
            {
                Map<String, String> map = new HashMap<>();
                if (e.ipPort != null)
                    map.put("ip", e.ipPort);
                if (e.fqdnPort != null)
                    map.put("fqdn", e.fqdnPort.toLowerCase());
                if (e.ipFamily != null)
                    map.put("ip_family", e.ipFamily);
                // interplmn fqdn is not present on SCPs, so it will never appear as a value
                if (!map.isEmpty())
                    retValue.add(map);

            });
            return retValue;
        }
    }

}
