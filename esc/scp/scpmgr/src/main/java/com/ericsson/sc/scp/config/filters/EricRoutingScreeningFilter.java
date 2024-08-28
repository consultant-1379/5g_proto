/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 31, 2022
 *     Author: eavvann
 */

package com.ericsson.sc.scp.config.filters;

import java.util.Optional;

import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.IfHttpFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter.Network;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.ServiceAddress;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NodeType;

/**
 * 
 */
public class EricRoutingScreeningFilter implements IEricProxyFilter
{
    private NfInstance scpInst;
    private final boolean isTls;
    private IfNetwork network;
    private static final String FILTER_NAME = "scp_routing_screening";

    public EricRoutingScreeningFilter(NfInstance scpInstance,
                                      boolean isTls,
                                      IfNetwork network)
    {
        this.scpInst = scpInstance;
        this.isTls = isTls;
        this.network = network;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.glue.IEricProxyFilter#create()
     */
    @Override
    public Optional<IfHttpFilter> create()
    {
        var scpFilter = new ProxySeppFilter(FILTER_NAME, network.getName(), Network.INTERNAL);
        scpFilter.setNodeType(Optional.of(NodeType.SCP));

        if (scpInst.getNfPeerInfo() != null && scpInst.getNfPeerInfo().getOutMessageHandling() != null)
        {
            scpFilter.setNfPeerInfoHandling(scpInst.getNfPeerInfo().getOutMessageHandling().value());
        }

        ServiceAddress svcAddr = Utils.getByName(scpInst.getServiceAddress(), network.getServiceAddressRef());
        scpFilter.setIpVersion(svcAddr.getIpv4Address() != null
                               && svcAddr.getIpv6Address() != null ? IPFamily.DualStack : svcAddr.getIpv4Address() != null ? IPFamily.IPv4 : IPFamily.IPv6);

        new RoutingFilter(scpFilter, scpInst, isTls, network).create();
        new MessageScreeningFilter(scpFilter, scpInst, network).create();

        return Optional.of(scpFilter);
    }
}
