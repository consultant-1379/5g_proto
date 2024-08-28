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

package com.ericsson.sc.sepp.config.filters;

import java.util.Optional;

import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.IfHttpFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter.Network;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NodeType;

/**
 *
 */
public class EricRoutingScreeningFilter implements IEricProxyFilter
{
    private static final String FILTER_NAME = "sepp_routing_screening";
    private final NfInstance seppInst;
    private final ServiceAddress svcAddress;
    private final boolean isTls;
    private final IfNetwork network;

    public EricRoutingScreeningFilter(NfInstance seppInstance,
                                      ServiceAddress svcAddress,
                                      boolean isTls,
                                      IfNetwork network)
    {
        this.seppInst = seppInstance;
        this.svcAddress = svcAddress;
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
        var nwType = network instanceof ExternalNetwork ? Network.EXTERNAL : Network.INTERNAL;
        var seppFilter = new ProxySeppFilter(FILTER_NAME, network.getName(), nwType);
        seppFilter.setNodeType(Optional.of(NodeType.SEPP));

        if (seppInst.getNfPeerInfo() != null && seppInst.getNfPeerInfo().getOutMessageHandling() != null)
        {
            seppFilter.setNfPeerInfoHandling(seppInst.getNfPeerInfo().getOutMessageHandling().value());
        }

        ServiceAddress svcAddr = Utils.getByName(seppInst.getServiceAddress(), network.getServiceAddressRef());
        seppFilter.setIpVersion(svcAddr.getIpv4Address() != null
                                && svcAddr.getIpv6Address() != null ? IPFamily.DualStack : svcAddr.getIpv4Address() != null ? IPFamily.IPv4 : IPFamily.IPv6);

        new RoutingFilter(seppFilter, seppInst, svcAddress, isTls, network).create();
        new MessageScreeningFilter(seppFilter, seppInst, network).create();

        return Optional.of(seppFilter);
    }
}
