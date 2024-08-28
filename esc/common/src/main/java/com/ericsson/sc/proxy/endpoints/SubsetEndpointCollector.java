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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.NfConditionParser;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;

/**
 * The SubsetEndpointCollector is currently not used anywhere due to the change
 * on how the target host is picked for preferred and strict routing
 */

@Deprecated
public class SubsetEndpointCollector extends EndpointCollector
{

    private boolean keepOriginalAuthorityHeader;

    public SubsetEndpointCollector(IfNfInstance configInst,
                                   IfNfPool pool,
                                   boolean keepOriginalAuthorityHeader)
    {
        super(configInst, pool);
        this.keepOriginalAuthorityHeader = keepOriginalAuthorityHeader;
    }

    public SubsetEndpointCollector(IfNfPool pool,
                                   IfNfInstance configInst)
    {
        this(configInst, pool, false);
    }

    /**
     * Return a list of proxy endpoints, created from static-nf-instances,
     * static-scp-instances and discovered-nf-instances. Endpoints from static or
     * discovered nfInstances have envoy.lb.host set with their FQDN. If
     * static-scp-instances exist in a subpool, then filtered nf instances of that
     * subpool are excluded from subset cluster and are replaced by endpoints from
     * the scp-services. LbMetaData of those scp-services is list of the FQDN of the
     * filter nfServices.
     * 
     * Each endpoint has the following metadata attached:
     * <ul>
     * <li>envoy.lb
     * <ul>
     * <li>pool -> name of the pool this endpoint is in
     * <li>host -> fqdn of the endpoint
     * </ul>
     * <li>sepp.type
     * <ul>
     * <li>type -> nfType of the endpoints, used for indirect routing
     * </ul>
     * </ul>
     */
    @Override
    public void createEndpoints()
    {
        Supplier<Stream<IfTypedNfInstance>> nfInstances = () -> getAllNfInstances(pool, this.configInst).stream();

        var servicesPerScp = collectServicesPerScp(nfInstances.get(), pool);

        var nfServices = nfInstances.get().flatMap(nfInstance -> nfInstance.fetchNfService().stream());

        if (!servicesPerScp.isEmpty())
        {
            // Collect all nfServices that are represented by an SCP
            Set<IfTypedNfService> scpOwnedServices = new HashSet<>();
            servicesPerScp.forEach((entry,
                                    value) -> scpOwnedServices.addAll(value));
            // Valid nfServices for preferred routing, are those that don't have an SCP
            // defined in their subpool.
            var validNfServices = nfServices.filter(nfService -> !scpOwnedServices.contains(nfService));
            var nfEndpoints = createEndpointsFromServiceStream(validNfServices, 0, pool.getName(), keepOriginalAuthorityHeader);
            // For each SCP, create a preferred host endpoint. Set their host metadata to
            // the list of the fqdns of their nfServices.

            var scpEndpoints = createEndpointsFromServiceStream(servicesPerScp, pool.getName(), keepOriginalAuthorityHeader);
            this.endpoints.addAll(Stream.concat(nfEndpoints.stream(), scpEndpoints.stream()).collect(Collectors.toList()));
        }
        else
        {
            this.endpoints.addAll(createEndpointsFromServiceStream(nfServices, 0, pool.getName(), keepOriginalAuthorityHeader));
        }

        setEndpointVTapFlag(this.getEndpoints(), this.pool.getName());
    }

    /**
     * Return a map of static-scp-instances along with nfServices that are in the
     * same pool and the constraint expression applies for. Additionally, set the
     * sepp.type.type 'scp' for all nfServices.
     * 
     * @param pool
     * @return
     */
    private Map<IfTypedNfInstance, Set<IfTypedNfService>> collectServicesPerScp(Stream<IfTypedNfInstance> nfInst,
                                                                                IfNfPool pool)
    {
        Map<IfTypedNfInstance, Set<IfTypedNfService>> scpsWithServices = new HashMap<>();

        List<IfTypedNfInstance> nfInstances = nfInst.collect(Collectors.toList());
        Set<String> selectedSvcNames = new HashSet<>();

        for (var subpool : pool.getPriorityGroup())
        {
            var filteredServices = NfConditionParser.parse(subpool.getNfMatchCondition(), nfInstances.stream(), selectedSvcNames);
            if (subpool.getStaticScpInstanceDataRef() != null && !subpool.getStaticScpInstanceDataRef().isEmpty() && !filteredServices.isEmpty())
            {
                // Selected nfServices are kept for later filtering in the Set
                // selectedInstanceNames.
                // It is assumed that nfInstanceName:NfServiceName is unique and the names are
                // mandatory
                var scpInstances = CommonConfigUtils.getScpInstances(subpool, this.configInst);
                scpInstances.stream().flatMap(scpInstance -> scpInstance.fetchNfService().stream()).forEach(svc -> svc.setNfType(METADATA.NF_TYPE_SCP));

                Set<IfTypedNfService> filteredSet = new HashSet<>(filteredServices);
                scpInstances.forEach(scpInstance -> scpsWithServices.merge(scpInstance,
                                                                           filteredSet,
                                                                           (s1,
                                                                            s2) -> Stream.concat(s1.stream(), s2.stream()).collect(Collectors.toSet())));
            }

        }

        // make a last priority which contains nfInstances that doesn't satisfy any of
        // the subpool constraints, i.e. nfInstances \ instanceNames
        if (pool.getStaticScpInstanceDataRef() != null && !pool.getStaticScpInstanceDataRef().isEmpty())
        {
            var scpInstances = CommonConfigUtils.getScpInstances(pool, this.configInst);
            scpInstances.stream().flatMap(scpInstance -> scpInstance.fetchNfService().stream()).forEach(svc -> svc.setNfType(METADATA.NF_TYPE_SCP));
            List<IfTypedNfService> lastPrioNfServices = new ArrayList<>();
            nfInstances.stream()
                       .forEach(inst -> lastPrioNfServices.addAll(inst.fetchNfService()
                                                                      .stream()
                                                                      .filter(svc -> !selectedSvcNames.contains(inst.getName() + ":" + svc.getName()))
                                                                      .collect(Collectors.toList())));

            if (!lastPrioNfServices.isEmpty())
            {
                Set<IfTypedNfService> filteredSet = new HashSet<>(lastPrioNfServices);
                scpInstances.forEach(scpInstance -> scpsWithServices.merge(scpInstance,
                                                                           filteredSet,
                                                                           (s1,
                                                                            s2) -> Stream.concat(s1.stream(), s2.stream()).collect(Collectors.toSet())));
            }

        }
        return scpsWithServices;

    }
}
