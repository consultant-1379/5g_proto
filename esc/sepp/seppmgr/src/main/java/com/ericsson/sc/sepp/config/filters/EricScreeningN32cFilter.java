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
 * Created on: Oct 24, 2022
 *     Author: echaias
 */

package com.ericsson.sc.sepp.config.filters;

import java.util.Optional;

import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.IfHttpFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter.Network;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NodeType;

/**
 * 
 */
public class EricScreeningN32cFilter implements IEricProxyFilter
{
    private static final String FILTER_NAME = "sepp_screening";
    private final NfInstance seppInst;

    public EricScreeningN32cFilter(NfInstance seppInstance)
    {
        this.seppInst = seppInstance;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.glue.IEricProxyFilter#create()
     */
    @Override
    public Optional<IfHttpFilter> create()
    {
        var nwType = Network.CONTROL_PLANE;
        var seppFilter = new ProxySeppFilter(FILTER_NAME, null, nwType);
        seppFilter.setNodeType(Optional.of(NodeType.SEPP));
        // indicates the filter is configured to service n32c requests originating
        // from the control plane (manager). Eric_proxy has been adapted to skip ingress
        // screening
        // and routing phases in that case
        seppFilter.setServesControlPlane(true);

        // own_fqdn for the n32c listener is set with FQDN of external-network exactly
        // as the sender field of message body
        var extSvcAddress = Utils.getByName(this.seppInst.getServiceAddress(), this.seppInst.getExternalNetwork().get(0).getServiceAddressRef());
        if (extSvcAddress.getFqdn() != null)
        {
            seppFilter.setOwnFqdn(extSvcAddress.getFqdn());
        }

        // passes nf_peer_info_handling to be used for the case of n32c
        if (seppInst.getNfPeerInfo() != null && seppInst.getNfPeerInfo().getOutMessageHandling() != null)
        {
            seppFilter.setNfPeerInfoHandling(seppInst.getNfPeerInfo().getOutMessageHandling().value());
        }

        seppFilter.setIpVersion(extSvcAddress.getIpv4Address() != null
                                && extSvcAddress.getIpv6Address() != null ? IPFamily.DualStack
                                                                          : extSvcAddress.getIpv4Address() != null ? IPFamily.IPv4 : IPFamily.IPv6);

        new MessageScreeningN32cFilter(seppFilter, seppInst).create();

        return Optional.of(seppFilter);
    }
}
