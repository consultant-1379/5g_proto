/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 16, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningCase;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyActionOnFailure;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyActionOnFailure.ActionOnFailureType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckJsonDepth;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckJsonLeaves;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckMessageBytes;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyMessageValidation;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterCase;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoamingPartner;
import com.google.protobuf.Any;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ClusterFcConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ClusterFilterPhaseConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.EricProxyConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ExtNetworkFcConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ExtNetworkPhaseConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KeyListValuePair;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KeyValuePair;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KlvTable;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.KvTable;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NetworkFilterPhaseConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NfPeerInfo;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.NodeType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.OwnNetworkPhaseConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PerRpFcConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PlmnIdInfo;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RequestFilterConfig;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ResponseFilterConfig;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxySeppFilter implements IfHttpFilter
{

    private static final String FILTER_NAME = "envoy.filters.http.eric-proxy";

    public static final String HEADER_ORIGINAL_ABSOLUTE_URL = "x-eric-original-absolute-path";

    public static final String INTERNAL_ROUTING_TABLE_NAME = "ROUTING";
    public static final String INTERNAL_IN_REQUEST_TABLE_NAME = "SCREENING_1_6_1";
    public static final String INTERNAL_OUT_RESPONSE_TABLE_NAME = "SCREENING_1_6_6";
    public static final String INTERNAL_OUT_REQUEST_TABLE_NAME = "SCREENING_3_4_3";
    public static final String INTERNAL_IN_RESPONSE_TABLE_NAME = "SCREENING_3_4_4";
    public static final String INTERNAL_FQDN_MAPPING_TABLE_NAME = "FQDN_MAPPING";
    public static final String INTERNAL_FQDN_UNMAPPING_TABLE_NAME = "FQDN_UNMAPPING";

    private List<ProxyFilterCase> routingCases = new ArrayList<>();
    private List<ProxyScreeningCase> screeningCases = new ArrayList<>();
    private List<ProxyRoamingPartner> roamingPartners = new ArrayList<>();

    private HashMap<String, Map<String, String>> intKvTables = new HashMap<>();
    private HashMap<String, Map<String, String>> kvTables = new HashMap<>();
    private HashMap<String, Map<String, List<String>>> klvTables = new HashMap<>();
    private final String name;
    private String rpNameTable;
    private Optional<NodeType> nodeType = Optional.empty();
    private List<String> nfTypesRequiringTFqdn = new ArrayList<>();
    private String ownFqdn;
    private Integer ownInternalPort;
    private Integer ownExternalPort;
    private String nwName;
    private Network nwType;
    private String routingRef;
    private String inReqScreeningRef;
    private String outRespScreeningRef;
    private boolean servesControlPlane = false;
    private Integer maxMessageBytes;
    private Integer maxMessageLeaves;
    private Integer maxMessageNestingDepth;
    private String nfPeerInfoHandling;
    private IPFamily ipVersion;
    private PlmnIdInfo plmnIdInfo;
    private boolean isFirewallCat1Conf = false;
    private static final Logger log = LoggerFactory.getLogger(ProxySeppFilter.class);

    public enum Network
    {
        INTERNAL,
        EXTERNAL,
        CONTROL_PLANE; // to cater for the n32c egress screening
    }

    public ProxySeppFilter(String name,
                           String nwName,
                           Network nwType)
    {
        this.name = name;
        this.nwName = nwName;
        this.nwType = nwType;
    }

    /**
     * @param rpNameTable the rpNameTable to set
     */
    public void setRpNameTable(String tableName)
    {
        this.rpNameTable = tableName;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(Optional<NodeType> nodeType)
    {
        this.nodeType = nodeType;
    }

    /**
     * Indicates this filter configuration goes to the listener servicing n32c
     * requests from the control plane. Defaults to false
     * 
     * @param val
     */
    public void setServesControlPlane(boolean val)
    {
        this.servesControlPlane = val;
    }

    public void addRoamingPartner(ProxyRoamingPartner rp)
    {
        this.roamingPartners.add(rp);
    }

    public void addRoamingPartner(int index,
                                  ProxyRoamingPartner rp)
    {
        this.roamingPartners.add(index, rp);
    }

    public void addRoutingCase(ProxyFilterCase rc)
    {
        this.routingCases.add(rc);
    }

    public void addRoutingCase(int index,
                               ProxyFilterCase rc)
    {
        this.routingCases.add(index, rc);
    }

    /**
     * Indicates that at firewall for service operations is activated for at least
     * one RP
     * 
     * @param val
     */
    public void setIsFirewallCat1Conf(boolean val)
    {
        this.isFirewallCat1Conf = val;
    }

    /**
     * @return the screeningCases
     */
    public List<ProxyScreeningCase> getScreeningCases()
    {
        return screeningCases;
    }

    public void addScreeningCase(int index,
                                 ProxyScreeningCase sc)
    {
        this.screeningCases.add(index, sc);
    }

    public void addScreeningCase(ProxyScreeningCase sc)
    {
        this.screeningCases.add(sc);
    }

    public void addKvTable(String name,
                           Map<String, String> kvTable)
    {
        this.kvTables.put(name, kvTable);
    }

    /**
     * @return the kvTables
     */
    public Map<String, Map<String, String>> getKvTables()
    {
        return kvTables;
    }

    public void addKlvTable(String name,
                            Map<String, List<String>> klvTable)
    {
        this.klvTables.put(name, klvTable);
    }

    public void addIntKvTable(String name,
                              Map<String, String> intKvTable)
    {
        this.intKvTables.put(name, intKvTable);
    }

    /**
     * @return the intKvTables
     */
    public Map<String, Map<String, String>> getIntKvTables()
    {
        return intKvTables;
    }

    public void addNfTypesRequiringTFqdn(String nfType)
    {
        this.nfTypesRequiringTFqdn.add(nfType);
    }

    public void addNfTypesRequiringTFqdn(int index,
                                         String nfType)
    {
        this.nfTypesRequiringTFqdn.add(index, nfType);
    }

    public void setOwnFqdn(String ownFqdn)
    {
        this.ownFqdn = ownFqdn;
    }

    public String getOwnFqdn()
    {
        return ownFqdn;
    }

    public void setOwnInternalPort(Integer ownInternalPort)
    {
        this.ownInternalPort = ownInternalPort;
    }

    public void setOwnExternalPort(Integer ownExternalPort)
    {
        this.ownExternalPort = ownExternalPort;
    }

    public void setNwName(String nwName)
    {
        this.nwName = nwName;
    }

    /**
     * This method is used to get the own plmnd ids that are configured under the
     * N32C container
     *
     * @return the plmnIdInfo
     */
    public PlmnIdInfo getPlmnIdInfo()
    {
        return plmnIdInfo;
    }

    /**
     * This method is used to set the own plmnd ids that are configured under the
     * N32C container
     *
     * @param plmnIdInfo the plmnIdInfo to set
     */
    public void setPlmnIdInfo(PlmnIdInfo plmnIdInfo)
    {
        this.plmnIdInfo = plmnIdInfo;
    }

    public void setRoutingRef(String routingRef)
    {
        this.routingRef = routingRef;
    }

    public void setInReqScreeningRef(String inReqScreeningRef)
    {
        this.inReqScreeningRef = inReqScreeningRef;
    }

    public void setOutRespScreeningRef(String outRespScreeningRef)
    {
        this.outRespScreeningRef = outRespScreeningRef;
    }

    public void setMaxMessageBytes(Integer maxMessageBytes)
    {
        this.maxMessageBytes = maxMessageBytes;
    }

    public void setMaxMessageLeaves(Integer maxMessageLeaves)
    {
        this.maxMessageLeaves = maxMessageLeaves;
    }

    public void setMaxMessageNestingDepth(Integer maxMessageNestingDepth)
    {
        this.maxMessageNestingDepth = maxMessageNestingDepth;
    }

    public void setNfPeerInfoHandling(String nfPeerInfoHandling)
    {
        this.nfPeerInfoHandling = nfPeerInfoHandling;
    }

    public void setIpVersion(IPFamily ipVersion)
    {
        this.ipVersion = ipVersion;
    }

    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {
        var proxyFilterBuilder = initiateProxyFilterBuilder();

        log.info("finished initiateProxyFilterBuilder");

        for (var rc : this.routingCases)
        {
            var rcBuilder = rc.initBuilder();
            proxyFilterBuilder.addFilterCases(rcBuilder.build());
        }

        for (var sc : this.screeningCases)
        {
            var scBuilder = sc.initBuilder();
            proxyFilterBuilder.addFilterCases(scBuilder.build());
        }

        for (var rp : this.roamingPartners)
        {
            var rpBuilder = rp.initBuilder();

            if (this.nwType.equals(Network.INTERNAL))
            {
                rpBuilder.clearRequestValidation();
            }
            else if (this.nwType.equals(Network.EXTERNAL))
            {
                rpBuilder.clearResponseValidation();
            }

            proxyFilterBuilder.addRoamingPartners(rpBuilder.build());
        }

        if (this.nwType.equals(Network.INTERNAL))
        {
            appendOwnNwData(proxyFilterBuilder);
        }
        else if (this.nwType.equals(Network.EXTERNAL))
        {
            appendExtNwData(proxyFilterBuilder);
        }
        else if (this.nwType.equals(Network.CONTROL_PLANE))// n32c case
        {
            var reqFcBuilder = RequestFilterConfig.newBuilder();
            var respFcBuilder = ResponseFilterConfig.newBuilder();
            appendEgressScreeningData(reqFcBuilder, respFcBuilder);
            if (!respFcBuilder.getAllFields().isEmpty())
            {
                proxyFilterBuilder.setResponseFilterCases(respFcBuilder);
            }
            if (!reqFcBuilder.getAllFields().isEmpty())
            {
                proxyFilterBuilder.setRequestFilterCases(reqFcBuilder);
            }
        }

        appendKvTables(proxyFilterBuilder);
        appendKlvTables(proxyFilterBuilder);
        builder.addHttpFilters(HttpFilter.newBuilder().setName(FILTER_NAME).setTypedConfig(Any.pack(proxyFilterBuilder.build())));

    }

    private io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.EricProxyConfig.Builder initiateProxyFilterBuilder()
    {

        var seppBuilder = EricProxyConfig.newBuilder().setName(this.name);
        log.info("Filter {} is created", this.name);

        if (this.rpNameTable != null)
        {
            seppBuilder.setRpNameTable(this.rpNameTable);
        }
        this.nodeType.ifPresent(seppBuilder::setNodeType);

        if (this.servesControlPlane)
        {
            seppBuilder.setControlPlane(true);
        }
        if (this.ownFqdn != null)
        {
            seppBuilder.setOwnFqdn(this.ownFqdn);
        }
        if (this.plmnIdInfo != null)
        {
            seppBuilder.setPlmnIds(this.plmnIdInfo);
        }
        if (this.ownInternalPort != null)
        {
            seppBuilder.setOwnInternalPort(this.ownInternalPort);
        }
        else if (this.ownExternalPort != null)
        {
            seppBuilder.setOwnExternalPort(this.ownExternalPort);
        }
        if (this.maxMessageBytes != null || this.maxMessageLeaves != null || this.maxMessageNestingDepth != null)
        {
            ProxyActionOnFailure requestValidationCheckMessageBytesActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                              413,
                                                                                                              Optional.of("Payload Too Large"),
                                                                                                              Optional.empty(),
                                                                                                              Optional.of("request_payload_too_large"),
                                                                                                              "json");
            ProxyActionOnFailure requestValidationCheckJsonLeavesActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                            413,
                                                                                                            Optional.of("Payload Too Large"),
                                                                                                            Optional.empty(),
                                                                                                            Optional.of("request_json_leaves_limits_exceeded"),
                                                                                                            "json");
            ProxyActionOnFailure requestValidationCheckJsonDepthActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                           413,
                                                                                                           Optional.of("Payload Too Large"),
                                                                                                           Optional.empty(),
                                                                                                           Optional.of("request_json_depth_limits_exceeded"),
                                                                                                           "json");
            ProxyCheckMessageBytes requestValidationCheckMessageBytes = new ProxyCheckMessageBytes(Optional.ofNullable(this.maxMessageBytes),
                                                                                                   false,
                                                                                                   requestValidationCheckMessageBytesActionOnFailure);
            ProxyCheckJsonLeaves requestValidationCheckJsonLeaves = new ProxyCheckJsonLeaves(Optional.ofNullable(this.maxMessageLeaves),
                                                                                             false,
                                                                                             requestValidationCheckJsonLeavesActionOnFailure);

            ProxyCheckJsonDepth requestValidationCheckJsonDepth = new ProxyCheckJsonDepth(Optional.ofNullable(this.maxMessageNestingDepth),
                                                                                          false,
                                                                                          requestValidationCheckJsonDepthActionOnFailure);

            ProxyActionOnFailure responseValidationCheckMessageBytesActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                               500,
                                                                                                               Optional.of("Internal Server Error"),
                                                                                                               Optional.of("INSUFFICIENT_RESOURCES"),
                                                                                                               Optional.of("response_payload_too_large"),
                                                                                                               "json");
            ProxyActionOnFailure responseValidationCheckJsonLeavesActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                             500,
                                                                                                             Optional.of("Internal Server Error"),
                                                                                                             Optional.of("INSUFFICIENT_RESOURCES"),
                                                                                                             Optional.of("response_json_leaves_limits_exceeded"),
                                                                                                             "json");
            ProxyActionOnFailure responseValidationCheckJsonDepthActionOnFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                                                            500,
                                                                                                            Optional.of("Internal Server Error"),
                                                                                                            Optional.of("INSUFFICIENT_RESOURCES"),
                                                                                                            Optional.of("response_json_depth_limits_exceeded"),
                                                                                                            "json");
            ProxyCheckMessageBytes responseValidationCheckMessageBytes = new ProxyCheckMessageBytes(Optional.ofNullable(this.maxMessageBytes),
                                                                                                    false,
                                                                                                    responseValidationCheckMessageBytesActionOnFailure);
            ProxyCheckJsonLeaves responseValidationCheckJsonLeaves = new ProxyCheckJsonLeaves(Optional.ofNullable(this.maxMessageLeaves),
                                                                                              false,
                                                                                              responseValidationCheckJsonLeavesActionOnFailure);

            ProxyCheckJsonDepth responseValidationCheckJsonDepth = new ProxyCheckJsonDepth(Optional.ofNullable(this.maxMessageNestingDepth),
                                                                                           false,
                                                                                           responseValidationCheckJsonDepthActionOnFailure);

            ProxyMessageValidation requestMessageValidation = new ProxyMessageValidation(requestValidationCheckMessageBytes,
                                                                                         requestValidationCheckJsonLeaves,
                                                                                         requestValidationCheckJsonDepth);

            ProxyMessageValidation responseMessageValidation = new ProxyMessageValidation(responseValidationCheckMessageBytes,
                                                                                          responseValidationCheckJsonLeaves,
                                                                                          responseValidationCheckJsonDepth);

            seppBuilder.setRequestValidation(requestMessageValidation.build()).setResponseValidation(responseMessageValidation.build());
        }

        if (this.nfPeerInfoHandling != null)
        {
            if (this.nfPeerInfoHandling.equals("on"))
            {
                seppBuilder.setNfPeerInfoHandling(NfPeerInfo.ON);
            }
            else
            {
                seppBuilder.setNfPeerInfoHandling(NfPeerInfo.OFF);
            }
        }

        seppBuilder.setIpVersion(this.ipVersion);

        seppBuilder.addAllNfTypesRequiringTFqdn(nfTypesRequiringTFqdn);

        if (this.isFirewallCat1Conf && (this.nwType.equals(Network.EXTERNAL)))
        {
            var usfwDefAllowed = new DefaultAllowedServiceOperations();
            var operations = usfwDefAllowed.getOperations();

            seppBuilder.addAllDefaultAllowedServiceOperations(operations);
        }

        return seppBuilder;

    }

    private void appendOwnNwData(EricProxyConfig.Builder seppBuilder)
    {
        var reqFcBuilder = RequestFilterConfig.newBuilder().setRouting(buildOwnNwConfig(this.routingRef));
        var respFcBuilder = ResponseFilterConfig.newBuilder();

        if (this.inReqScreeningRef != null)
        {
            reqFcBuilder.setInRequestScreening(buildOwnNwConfig(this.inReqScreeningRef));
        }

        if (this.outRespScreeningRef != null)
        {
            respFcBuilder.setOutResponseScreening(buildOwnNwConfig(this.outRespScreeningRef));
        }

        appendEgressScreeningData(reqFcBuilder, respFcBuilder);
        seppBuilder.setRequestFilterCases(reqFcBuilder);

        if (!respFcBuilder.getAllFields().isEmpty())
        {
            seppBuilder.setResponseFilterCases(respFcBuilder);
        }
    }

    /**
     * Given two filter case builders (request, response) sets the
     * outRequest/InResponse Screening configuration respectively.
     * 
     * @param reqFcBuilder  builder for the request filter cases
     * @param respFcBuilder builder for the response filter cases
     */
    private void appendEgressScreeningData(RequestFilterConfig.Builder reqFcBuilder,
                                           ResponseFilterConfig.Builder respFcBuilder)
    {
        if (this.intKvTables.containsKey(INTERNAL_OUT_REQUEST_TABLE_NAME))
        {
            reqFcBuilder.setOutRequestScreening(buildClusterConfig(INTERNAL_OUT_REQUEST_TABLE_NAME));
        }

        if (this.intKvTables.containsKey(INTERNAL_IN_RESPONSE_TABLE_NAME))
        {
            respFcBuilder.setInResponseScreening(buildClusterConfig(INTERNAL_IN_RESPONSE_TABLE_NAME));
        }
    }

    private void appendExtNwData(EricProxyConfig.Builder seppBuilder)
    {
        var reqFcBuilder = RequestFilterConfig.newBuilder().setRouting(buildExtNwConfig(this.routingRef, INTERNAL_ROUTING_TABLE_NAME));
        var respFcBuilder = ResponseFilterConfig.newBuilder();

        if (this.inReqScreeningRef != null || this.intKvTables.containsKey(INTERNAL_IN_REQUEST_TABLE_NAME))
        {
            reqFcBuilder.setInRequestScreening(buildExtNwConfig(this.inReqScreeningRef, INTERNAL_IN_REQUEST_TABLE_NAME));
        }

        if (this.outRespScreeningRef != null || this.intKvTables.containsKey(INTERNAL_OUT_RESPONSE_TABLE_NAME))
        {
            respFcBuilder.setOutResponseScreening(buildExtNwConfig(this.outRespScreeningRef, INTERNAL_OUT_RESPONSE_TABLE_NAME));
        }

        appendEgressScreeningData(reqFcBuilder, respFcBuilder);

        seppBuilder.setRequestFilterCases(reqFcBuilder);

        if (!respFcBuilder.getAllFields().isEmpty())
        {
            seppBuilder.setResponseFilterCases(respFcBuilder);
        }
    }

    private NetworkFilterPhaseConfig.Builder buildOwnNwConfig(String ref)
    {
        var ownNwRoutingBuilder = OwnNetworkPhaseConfig.newBuilder().setName(this.nwName);
        ownNwRoutingBuilder.addStartFcList(ref);
        var nwFilterPhaseConfigBuilder = NetworkFilterPhaseConfig.newBuilder().setOwnNw(ownNwRoutingBuilder);

        return nwFilterPhaseConfigBuilder;
    }

    private ClusterFilterPhaseConfig.Builder buildClusterConfig(String tableKey)
    {
        var clusterFcConfigBuilder = ClusterFcConfig.newBuilder();
        clusterFcConfigBuilder.putAllClusterToFcMap(this.intKvTables.get(tableKey));
        var clusterFilterPhaseConfigBuilder = ClusterFilterPhaseConfig.newBuilder().addClusterFcConfigList(clusterFcConfigBuilder);

        return clusterFilterPhaseConfigBuilder;
    }

    private NetworkFilterPhaseConfig.Builder buildExtNwConfig(String ref,
                                                              String tableKey)
    {
        var nwFilterPhaseConfigBuilder = NetworkFilterPhaseConfig.newBuilder();
        var extNwBuilder = ExtNetworkPhaseConfig.newBuilder().setName(this.nwName);
        var extNwFcConfigBuilder = ExtNetworkFcConfig.newBuilder();
        var perRPFcConfigBuilder = PerRpFcConfig.newBuilder();

        if (ref != null)
        {
            perRPFcConfigBuilder.setDefaultFcForRpNotFound(ref);
        }

        if (this.intKvTables.containsKey(tableKey))
        {
            perRPFcConfigBuilder.putAllRpToFcMap(this.intKvTables.get(tableKey));
        }

        extNwFcConfigBuilder.setPerRpFcConfig(perRPFcConfigBuilder);
        extNwBuilder.addExtNwFcConfigList(extNwFcConfigBuilder);
        nwFilterPhaseConfigBuilder.setExtNw(extNwBuilder);

        return nwFilterPhaseConfigBuilder;
    }

    /**
     * @param seppBuilder
     */
    private void appendKvTables(EricProxyConfig.Builder seppBuilder)
    {
        for (var table : this.kvTables.entrySet())
        {
            var kvTableBuilder = KvTable.newBuilder().setName(table.getKey());

            for (var entry : table.getValue().entrySet())
            {
                kvTableBuilder.addEntries(KeyValuePair.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()));
            }

            seppBuilder.addKeyValueTables(kvTableBuilder.build());
        }
    }

    private void appendKlvTables(EricProxyConfig.Builder seppBuilder)
    {
        for (var table : this.klvTables.entrySet())
        {
            var klvTableBuilder = KlvTable.newBuilder().setName(table.getKey());

            for (var entry : table.getValue().entrySet())
            {
                klvTableBuilder.addEntries(KeyListValuePair.newBuilder().setKey(entry.getKey()).addAllValue(entry.getValue()));
            }

            seppBuilder.setCallbackUriKlvTable(table.getKey());
            seppBuilder.addKeyListValueTables(klvTableBuilder.build());
        }
    }

    @Override
    public Priorities getPriority()

    {
        return Priorities.ROUTING_SCREENING_FILTER;
    }

    /**
     * @return the roamingPartners
     */
    public List<ProxyRoamingPartner> getRoamingPartners()
    {
        return roamingPartners;
    }
}
