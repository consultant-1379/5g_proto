/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 2, 2023
 *     Author: zpanevg
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RejectMessageAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.Builder;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.IpHiding.SubnetList;

/**
 * 
 */
public class ProxyIpAddressHiding implements IfProxyTopologyHiding
{
    private Map<String, Action> nfTypeIpHidingMap = new HashMap<>();
    private Map<String, List<String>> subnetPerNfIpv4 = new HashMap<>();
    private Map<String, List<String>> subnetPerNfIpv6 = new HashMap<>();

    public void putToMapFQDNAbsense(String nfType,
                                    Action action)
    {
        nfTypeIpHidingMap.put(nfType, action);
    }

    public void putToMapSubnetPerNfIpv4(String nfType,
                                        List<String> subnet)
    {
        subnetPerNfIpv4.put(nfType, subnet);
    }

    public void putToMapSubnetPerNfIpv6(String nfType,
                                        List<String> subnet)
    {
        subnetPerNfIpv6.put(nfType, subnet);
    }

    public ProxyIpAddressHiding()
    {
    }

    /**
     * Copy constructor
     *
     * @param piah
     */
    public ProxyIpAddressHiding(ProxyIpAddressHiding piah)
    {
        this.nfTypeIpHidingMap = new HashMap<>(piah.nfTypeIpHidingMap);
        this.subnetPerNfIpv4 = new HashMap<>(piah.subnetPerNfIpv4);
        this.subnetPerNfIpv6 = new HashMap<>(piah.subnetPerNfIpv6);
    }

    public IfProxyTopologyHiding clone()
    {

        return new ProxyIpAddressHiding(this);

    }

    public Builder initBuilder()
    {
        var thBuilder = TopologyHiding.newBuilder();
        return initBuilder(thBuilder);
    }

    public Builder initBuilder(Builder thBuilder)
    {

        var ipHiding = TopologyHiding.IpHiding.newBuilder();
        this.nfTypeIpHidingMap.forEach((nftype,
                                        action) ->
        {
            var aofaBuilder = TopologyHiding.IpHiding.ActionOnFqdnAbsence.newBuilder();
            var aBuilder = TopologyHiding.IpHiding.ActionOnFqdnAbsence.Action.newBuilder();
            switch (action.actionOption)
            {
                case HIDE:
                {
                    aBuilder.setApplyIpHiding(true);
                    aofaBuilder.setResponseAction(aBuilder.build());
                    aofaBuilder.setRequestAction(aBuilder.build());
                    ipHiding.putIpHidingPerTargetNfType(nftype, aofaBuilder.build());
                    break;
                }
                case DROP:
                {
                    aBuilder.setDrop(true);
                    aofaBuilder.setResponseAction(aBuilder.build());
                    aofaBuilder.setRequestAction(aBuilder.build());
                    ipHiding.putIpHidingPerTargetNfType(nftype, aofaBuilder.build());
                    break;
                }
                case ERROR:
                {
                    var rweBuilder = RejectMessageAction.newBuilder();

                    action.getCause().ifPresent(rweBuilder::setCause);
                    action.getDetail().ifPresent(rweBuilder::setDetail);
                    action.getTitle().ifPresent(rweBuilder::setTitle);
                    action.getStatus().ifPresent(rweBuilder::setStatus);
                    action.getMessageBodyType().ifPresent(rweBuilder::setMessageFormat);

                    aBuilder.setRespondWithError(rweBuilder.build());
                    aofaBuilder.setResponseAction(aBuilder.build());
                    aofaBuilder.setRequestAction(aBuilder.build());
                    ipHiding.putIpHidingPerTargetNfType(nftype, aofaBuilder.build());
                    break;
                }
                case FORWARD:
                {
                    aBuilder.setForward(true);
                    aofaBuilder.setResponseAction(aBuilder.build());
                    aofaBuilder.setRequestAction(aBuilder.build());
                    ipHiding.putIpHidingPerTargetNfType(nftype, aofaBuilder.build());
                    break;
                }
            }
        });

        this.subnetPerNfIpv4.forEach((key,
                                      value) ->
        {
            var subnetList = SubnetList.newBuilder();
            value.forEach(subnetList::addSubnetList);
            ipHiding.putIpv4SubnetPerTargetNfType(key, subnetList.build());
        });

        this.subnetPerNfIpv6.forEach((key,
                                      value) ->
        {
            var subnetList = SubnetList.newBuilder();
            value.forEach(subnetList::addSubnetList);
            ipHiding.putIpv6SubnetPerTargetNfType(key, subnetList.build());
        });

        thBuilder.setIpHiding(ipHiding.build());

        return thBuilder;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nfTypeIpHidingMap);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyIpAddressHiding other = (ProxyIpAddressHiding) obj;
        return Objects.equals(nfTypeIpHidingMap, other.nfTypeIpHidingMap);
    }

    public static class Action
    {
        public enum ActionOption
        {
            HIDE,
            DROP,
            FORWARD,
            ERROR
        }

        private ActionOption actionOption;
        private Optional<Integer> status;
        private Optional<String> title;
        private Optional<String> detail;
        private Optional<String> cause;
        private Optional<MessageBodyType> messageFormat;

        public void setActionOption(ActionOption actionOption)
        {
            this.actionOption = actionOption;
        }

        public void setError(int status,
                             String title,
                             String detail,
                             String cause,
                             MessageBodyType messageFormat)
        {
            this.status = Optional.ofNullable(status);
            this.title = Optional.ofNullable(title);
            this.detail = Optional.ofNullable(detail);
            this.cause = Optional.ofNullable(cause);
            this.messageFormat = Optional.ofNullable(messageFormat);
        }

        public Optional<String> getCause()
        {
            return this.cause;

        }

        public Optional<String> getTitle()
        {
            return this.title;

        }

        public Optional<String> getDetail()
        {
            return this.detail;
        }

        public Optional<Integer> getStatus()
        {
            return this.status;

        }

        public Optional<MessageBodyType> getMessageBodyType()
        {
            return this.messageFormat;
        }
    }

}
