
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id",
                     "enabled",
                     "dscp",
                     "instance-count",
                     "watchdog-timer",
                     "reconnect-timer",
                     "node",
                     "initiate-connection-to-peer",
                     "disconnect-cause-handling-policy",
                     "terminate-connection-from-peer",
                     "terminate-connection-from-accepted-peers",
                     "user-label",
                     "transport-tcp",
                     "transport-sctp" })
public class LocalEndpoint
{

    /**
     * Used to specify the key of the local-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the local-endpoint instance.")
    private String id;
    /**
     * Used to enable or disable the use Local Endpoint. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * related to the Local Endpoint are closed if value is set to false.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable the use Local Endpoint. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: All established diameter peer connections related to the Local Endpoint are closed if value is set to false.")
    private Boolean enabled = true;
    /**
     * Used to specify the Differentiated Service Code Point (DSCP) to be used
     * during peer connection setups for the Local Endpoint. Update Apply: Immediate
     * Update Effect: Depends on used transport capability. In case of TCP there is
     * no impact on affected peer connections. The DSCP of affected peer connections
     * are updated without impact on traffic and related connections. In case of
     * SCTP the affected peer connections are dropped and reestablished by need with
     * updated transport properties.
     * 
     */
    @JsonProperty("dscp")
    @JsonPropertyDescription("Used to specify the Differentiated Service Code Point (DSCP) to be used during peer connection setups for the Local Endpoint. Update Apply: Immediate Update Effect: Depends on used transport capability. In case of TCP there is no impact on affected peer connections. The DSCP of affected peer connections are updated without impact on traffic and related connections. In case of SCTP the affected peer connections are dropped and reestablished by need with updated transport properties.")
    private Long dscp = 0L;
    /**
     * Used to specify the integer of instances a Local Endpoint configured with
     * connection initiation (client) role shall have. This has no effect on Local
     * Endpoint configured with connection termination (server) role. It can take a
     * value from one of the following ranges: 0: The Local Endpoint configured in
     * connection initiation (client) mode shall have an instance started for each
     * of the Diameter Service instances. That is, the actual Local Endpoint
     * instance integer is dynamically changing in accordance with actual Diameter
     * Service cluster size. More Local Endpoint instances are created when the
     * Diameter Service is scaled-out and Local Endpoint instances are removed when
     * the Diameter Service is scaled-in. 1..255: The Local Endpoint configured in
     * connection initiation (client) mode shall not pass on cluster level the
     * indicated integer of instances (a value specified between 1 and 255). The
     * actual integer of Local Endpoint instances created by Diameter Service will
     * never be higher than the lowest threshold value represented by either of the
     * actual Diameter Service cluster size or the configured Local Endpoint
     * instance count. The default value is 1. That is, a single instance is created
     * for a Local Endpoint configured in connection initiation mode. The default
     * setting assures standards behavior in relation with the restriction on
     * integer of peer connections to be set towards same diameter peer. To have an
     * effect when setting a value higher than one for this attribute the
     * restrict-connections attribute of the referred static-peer or
     * dynamic-peer-acceptor must be set to false (see static-peer,
     * dynamic-peer-acceptor). Update apply: Immediate. Update Effect: Depends on
     * local endpoint connection role. In case of connection initiation (client)
     * role, the configured amount of Local Endpoint instances will be applied. New
     * Local Endpoint instances are added by need without affecting existing ones.
     * Existing Local Endpoint instances with related peer connections are removed
     * by need without affecting the other ones. In case of connection termination
     * (server) role, there is no effect on attribute value change.
     * 
     */
    @JsonProperty("instance-count")
    @JsonPropertyDescription("Used to specify the integer of instances a Local Endpoint configured with connection initiation (client) role shall have. This has no effect on Local Endpoint configured with connection termination (server) role. It can take a value from one of the following ranges: 0: The Local Endpoint configured in connection initiation (client) mode shall have an instance started for each of the Diameter Service instances. That is, the actual Local Endpoint instance integer is dynamically changing in accordance with actual Diameter Service cluster size. More Local Endpoint instances are created when the Diameter Service is scaled-out and Local Endpoint instances are removed when the Diameter Service is scaled-in. 1..255: The Local Endpoint configured in connection initiation (client) mode shall not pass on cluster level the indicated integer of instances (a value specified between 1 and 255). The actual integer of Local Endpoint instances created by Diameter Service will never be higher than the lowest threshold value represented by either of the actual Diameter Service cluster size or the configured Local Endpoint instance count. The default value is 1. That is, a single instance is created for a Local Endpoint configured in connection initiation mode. The default setting assures standards behavior in relation with the restriction on integer of peer connections to be set towards same diameter peer. To have an effect when setting a value higher than one for this attribute the restrict-connections attribute of the referred static-peer or dynamic-peer-acceptor must be set to false (see static-peer, dynamic-peer-acceptor). Update apply: Immediate. Update Effect: Depends on local endpoint connection role. In case of connection initiation (client) role, the configured amount of Local Endpoint instances will be applied. New Local Endpoint instances are added by need without affecting existing ones. Existing Local Endpoint instances with related peer connections are removed by need without affecting the other ones. In case of connection termination (server) role, there is no effect on attribute value change.")
    private Long instanceCount = 1L;
    /**
     * Used to configure the Watchdog Initial Timer (Twinit) of the peer connections
     * assigned with the local endpoint (see also Authentication, Authorization and
     * Accounting (AAA) Transport Profile (RFC 3539) IETF: STANDARD). Unit:
     * millisecond (ms) Update Apply: Immediate. Update Effect: No impact on
     * affected peer connections.
     * 
     */
    @JsonProperty("watchdog-timer")
    @JsonPropertyDescription("Used to configure the Watchdog Initial Timer (Twinit) of the peer connections assigned with the local endpoint (see also Authentication, Authorization and Accounting (AAA) Transport Profile (RFC 3539) IETF: STANDARD). Unit: millisecond (ms) Update Apply: Immediate. Update Effect: No impact on affected peer connections.")
    private Long watchdogTimer = 30000L;
    /**
     * Used to configure the Tc timer
     * (https://tools.ietf.org/html/rfc6733#section-12) of the peer connections
     * assigned with the local endpoint. That is, it is used to set the frequency
     * the transport connection attempts are done to a diameter peer with whom no
     * active transport connection exists. Unit: millisecond (ms) Update Apply:
     * Immediate. Update Effect: No impact on affected peer connections.
     * 
     */
    @JsonProperty("reconnect-timer")
    @JsonPropertyDescription("Used to configure the Tc timer (https://tools.ietf.org/html/rfc6733#section-12) of the peer connections assigned with the local endpoint. That is, it is used to set the frequency the transport connection attempts are done to a diameter peer with whom no active transport connection exists. Unit: millisecond (ms) Update Apply: Immediate. Update Effect: No impact on affected peer connections.")
    private Long reconnectTimer = 30000L;
    /**
     * Used to assign a Local Endpoint with an Own Diameter Node represented by
     * related node instance. Should take as single value a reference to that node
     * to which the Local Endpoint must be assigned (see node). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished with updated Own Diameter
     * Node information. (Required)
     * 
     */
    @JsonProperty("node")
    @JsonPropertyDescription("Used to assign a Local Endpoint with an Own Diameter Node represented by related node instance. Should take as single value a reference to that node to which the Local Endpoint must be assigned (see node). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished with updated Own Diameter Node information.")
    private String node;
    /**
     * This attribute is used to set a Local Endpoint to play a connection initiator
     * (client) role. The initiate-connection-to-peer attribute will take a value if
     * the terminate-connection-from-accepted-peers or
     * terminate-connection-from-peer attributes are not assigned with value. The
     * attribute should take as value a reference to that static-peer that
     * represents the Peer Diameter Node the Local Endpoint should initiate
     * connection establishment towards (see static-peer MOC). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("initiate-connection-to-peer")
    @JsonPropertyDescription("This attribute is used to set a Local Endpoint to play a connection initiator (client) role. The initiate-connection-to-peer attribute will take a value if the terminate-connection-from-accepted-peers or terminate-connection-from-peer attributes are not assigned with value. The attribute should take as value a reference to that static-peer that represents the Peer Diameter Node the Local Endpoint should initiate connection establishment towards (see static-peer MOC). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished by need with updated information.")
    private String initiateConnectionToPeer;
    /**
     * Used to express the Disconnect-Cause handling policy to follow upon peer
     * connection close as result of operations over service instances or upon AAA
     * Service disconnection from Diameter Service. Should take as value a reference
     * to disconnect-cause-handling-policy. Update Apply: Immediate Update Effect:
     * No impact on established peer connections. Upon update, the Local Endpoint
     * will apply the referred policy. If not set the Diameter Service uses the
     * default Disconnect-Cause handling policy set to REBOOTING.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    @JsonPropertyDescription("Used to express the Disconnect-Cause handling policy to follow upon peer connection close as result of operations over service instances or upon AAA Service disconnection from Diameter Service. Should take as value a reference to disconnect-cause-handling-policy. Update Apply: Immediate Update Effect: No impact on established peer connections. Upon update, the Local Endpoint will apply the referred policy. If not set the Diameter Service uses the default Disconnect-Cause handling policy set to REBOOTING.")
    private String disconnectCauseHandlingPolicy;
    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-peer attribute will
     * take a value if the terminate-connection-from-accepted-peers or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those static-peer
     * instances which are representing Peer Diameter Nodes allowed to initiate
     * connection establishment towards the Local Endpoint (see static-peer). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-peer")
    @JsonPropertyDescription("This attribute is used to set a Local Endpoint to play a connection termination (server) role. The terminate-connection-from-peer attribute will take a value if the terminate-connection-from-accepted-peers or initiate-connection-to-peer attributes are not assigned with value. The attribute should take as value a list of references towards those static-peer instances which are representing Peer Diameter Nodes allowed to initiate connection establishment towards the Local Endpoint (see static-peer). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished by need with updated information.")
    private List<String> terminateConnectionFromPeer = new ArrayList<String>();
    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-accepted-peers
     * attribute will take a value if the terminate-connection-from-peer or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those
     * dynamic-peer-acceptor instances which are used to express a pattern based
     * representation of Peer Diameter Nodes allowed to initiate connection
     * establishment towards the Local Endpoint (see dynamic-peer-acceptor). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-accepted-peers")
    @JsonPropertyDescription("This attribute is used to set a Local Endpoint to play a connection termination (server) role. The terminate-connection-from-accepted-peers attribute will take a value if the terminate-connection-from-peer or initiate-connection-to-peer attributes are not assigned with value. The attribute should take as value a list of references towards those dynamic-peer-acceptor instances which are used to express a pattern based representation of Peer Diameter Nodes allowed to initiate connection establishment towards the Local Endpoint (see dynamic-peer-acceptor). Update Apply: Immediate. Update Effect: All established diameter peer connections assigned to local endpoint are dropped and reestablished by need with updated information.")
    private List<String> terminateConnectionFromAcceptedPeers = new ArrayList<String>();
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * Used to assign TCP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-tcp")
    @JsonPropertyDescription("Used to assign TCP transport capability for a Local Endpoint.")
    private TransportTcp transportTcp;
    /**
     * Used to assign SCTP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-sctp")
    @JsonPropertyDescription("Used to assign SCTP transport capability for a Local Endpoint.")
    private TransportSctp transportSctp;

    /**
     * Used to specify the key of the local-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the local-endpoint instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public LocalEndpoint withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to enable or disable the use Local Endpoint. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * related to the Local Endpoint are closed if value is set to false.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable the use Local Endpoint. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * related to the Local Endpoint are closed if value is set to false.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public LocalEndpoint withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Used to specify the Differentiated Service Code Point (DSCP) to be used
     * during peer connection setups for the Local Endpoint. Update Apply: Immediate
     * Update Effect: Depends on used transport capability. In case of TCP there is
     * no impact on affected peer connections. The DSCP of affected peer connections
     * are updated without impact on traffic and related connections. In case of
     * SCTP the affected peer connections are dropped and reestablished by need with
     * updated transport properties.
     * 
     */
    @JsonProperty("dscp")
    public Long getDscp()
    {
        return dscp;
    }

    /**
     * Used to specify the Differentiated Service Code Point (DSCP) to be used
     * during peer connection setups for the Local Endpoint. Update Apply: Immediate
     * Update Effect: Depends on used transport capability. In case of TCP there is
     * no impact on affected peer connections. The DSCP of affected peer connections
     * are updated without impact on traffic and related connections. In case of
     * SCTP the affected peer connections are dropped and reestablished by need with
     * updated transport properties.
     * 
     */
    @JsonProperty("dscp")
    public void setDscp(Long dscp)
    {
        this.dscp = dscp;
    }

    public LocalEndpoint withDscp(Long dscp)
    {
        this.dscp = dscp;
        return this;
    }

    /**
     * Used to specify the integer of instances a Local Endpoint configured with
     * connection initiation (client) role shall have. This has no effect on Local
     * Endpoint configured with connection termination (server) role. It can take a
     * value from one of the following ranges: 0: The Local Endpoint configured in
     * connection initiation (client) mode shall have an instance started for each
     * of the Diameter Service instances. That is, the actual Local Endpoint
     * instance integer is dynamically changing in accordance with actual Diameter
     * Service cluster size. More Local Endpoint instances are created when the
     * Diameter Service is scaled-out and Local Endpoint instances are removed when
     * the Diameter Service is scaled-in. 1..255: The Local Endpoint configured in
     * connection initiation (client) mode shall not pass on cluster level the
     * indicated integer of instances (a value specified between 1 and 255). The
     * actual integer of Local Endpoint instances created by Diameter Service will
     * never be higher than the lowest threshold value represented by either of the
     * actual Diameter Service cluster size or the configured Local Endpoint
     * instance count. The default value is 1. That is, a single instance is created
     * for a Local Endpoint configured in connection initiation mode. The default
     * setting assures standards behavior in relation with the restriction on
     * integer of peer connections to be set towards same diameter peer. To have an
     * effect when setting a value higher than one for this attribute the
     * restrict-connections attribute of the referred static-peer or
     * dynamic-peer-acceptor must be set to false (see static-peer,
     * dynamic-peer-acceptor). Update apply: Immediate. Update Effect: Depends on
     * local endpoint connection role. In case of connection initiation (client)
     * role, the configured amount of Local Endpoint instances will be applied. New
     * Local Endpoint instances are added by need without affecting existing ones.
     * Existing Local Endpoint instances with related peer connections are removed
     * by need without affecting the other ones. In case of connection termination
     * (server) role, there is no effect on attribute value change.
     * 
     */
    @JsonProperty("instance-count")
    public Long getInstanceCount()
    {
        return instanceCount;
    }

    /**
     * Used to specify the integer of instances a Local Endpoint configured with
     * connection initiation (client) role shall have. This has no effect on Local
     * Endpoint configured with connection termination (server) role. It can take a
     * value from one of the following ranges: 0: The Local Endpoint configured in
     * connection initiation (client) mode shall have an instance started for each
     * of the Diameter Service instances. That is, the actual Local Endpoint
     * instance integer is dynamically changing in accordance with actual Diameter
     * Service cluster size. More Local Endpoint instances are created when the
     * Diameter Service is scaled-out and Local Endpoint instances are removed when
     * the Diameter Service is scaled-in. 1..255: The Local Endpoint configured in
     * connection initiation (client) mode shall not pass on cluster level the
     * indicated integer of instances (a value specified between 1 and 255). The
     * actual integer of Local Endpoint instances created by Diameter Service will
     * never be higher than the lowest threshold value represented by either of the
     * actual Diameter Service cluster size or the configured Local Endpoint
     * instance count. The default value is 1. That is, a single instance is created
     * for a Local Endpoint configured in connection initiation mode. The default
     * setting assures standards behavior in relation with the restriction on
     * integer of peer connections to be set towards same diameter peer. To have an
     * effect when setting a value higher than one for this attribute the
     * restrict-connections attribute of the referred static-peer or
     * dynamic-peer-acceptor must be set to false (see static-peer,
     * dynamic-peer-acceptor). Update apply: Immediate. Update Effect: Depends on
     * local endpoint connection role. In case of connection initiation (client)
     * role, the configured amount of Local Endpoint instances will be applied. New
     * Local Endpoint instances are added by need without affecting existing ones.
     * Existing Local Endpoint instances with related peer connections are removed
     * by need without affecting the other ones. In case of connection termination
     * (server) role, there is no effect on attribute value change.
     * 
     */
    @JsonProperty("instance-count")
    public void setInstanceCount(Long instanceCount)
    {
        this.instanceCount = instanceCount;
    }

    public LocalEndpoint withInstanceCount(Long instanceCount)
    {
        this.instanceCount = instanceCount;
        return this;
    }

    /**
     * Used to configure the Watchdog Initial Timer (Twinit) of the peer connections
     * assigned with the local endpoint (see also Authentication, Authorization and
     * Accounting (AAA) Transport Profile (RFC 3539) IETF: STANDARD). Unit:
     * millisecond (ms) Update Apply: Immediate. Update Effect: No impact on
     * affected peer connections.
     * 
     */
    @JsonProperty("watchdog-timer")
    public Long getWatchdogTimer()
    {
        return watchdogTimer;
    }

    /**
     * Used to configure the Watchdog Initial Timer (Twinit) of the peer connections
     * assigned with the local endpoint (see also Authentication, Authorization and
     * Accounting (AAA) Transport Profile (RFC 3539) IETF: STANDARD). Unit:
     * millisecond (ms) Update Apply: Immediate. Update Effect: No impact on
     * affected peer connections.
     * 
     */
    @JsonProperty("watchdog-timer")
    public void setWatchdogTimer(Long watchdogTimer)
    {
        this.watchdogTimer = watchdogTimer;
    }

    public LocalEndpoint withWatchdogTimer(Long watchdogTimer)
    {
        this.watchdogTimer = watchdogTimer;
        return this;
    }

    /**
     * Used to configure the Tc timer
     * (https://tools.ietf.org/html/rfc6733#section-12) of the peer connections
     * assigned with the local endpoint. That is, it is used to set the frequency
     * the transport connection attempts are done to a diameter peer with whom no
     * active transport connection exists. Unit: millisecond (ms) Update Apply:
     * Immediate. Update Effect: No impact on affected peer connections.
     * 
     */
    @JsonProperty("reconnect-timer")
    public Long getReconnectTimer()
    {
        return reconnectTimer;
    }

    /**
     * Used to configure the Tc timer
     * (https://tools.ietf.org/html/rfc6733#section-12) of the peer connections
     * assigned with the local endpoint. That is, it is used to set the frequency
     * the transport connection attempts are done to a diameter peer with whom no
     * active transport connection exists. Unit: millisecond (ms) Update Apply:
     * Immediate. Update Effect: No impact on affected peer connections.
     * 
     */
    @JsonProperty("reconnect-timer")
    public void setReconnectTimer(Long reconnectTimer)
    {
        this.reconnectTimer = reconnectTimer;
    }

    public LocalEndpoint withReconnectTimer(Long reconnectTimer)
    {
        this.reconnectTimer = reconnectTimer;
        return this;
    }

    /**
     * Used to assign a Local Endpoint with an Own Diameter Node represented by
     * related node instance. Should take as single value a reference to that node
     * to which the Local Endpoint must be assigned (see node). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished with updated Own Diameter
     * Node information. (Required)
     * 
     */
    @JsonProperty("node")
    public String getNode()
    {
        return node;
    }

    /**
     * Used to assign a Local Endpoint with an Own Diameter Node represented by
     * related node instance. Should take as single value a reference to that node
     * to which the Local Endpoint must be assigned (see node). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished with updated Own Diameter
     * Node information. (Required)
     * 
     */
    @JsonProperty("node")
    public void setNode(String node)
    {
        this.node = node;
    }

    public LocalEndpoint withNode(String node)
    {
        this.node = node;
        return this;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection initiator
     * (client) role. The initiate-connection-to-peer attribute will take a value if
     * the terminate-connection-from-accepted-peers or
     * terminate-connection-from-peer attributes are not assigned with value. The
     * attribute should take as value a reference to that static-peer that
     * represents the Peer Diameter Node the Local Endpoint should initiate
     * connection establishment towards (see static-peer MOC). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("initiate-connection-to-peer")
    public String getInitiateConnectionToPeer()
    {
        return initiateConnectionToPeer;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection initiator
     * (client) role. The initiate-connection-to-peer attribute will take a value if
     * the terminate-connection-from-accepted-peers or
     * terminate-connection-from-peer attributes are not assigned with value. The
     * attribute should take as value a reference to that static-peer that
     * represents the Peer Diameter Node the Local Endpoint should initiate
     * connection establishment towards (see static-peer MOC). Update Apply:
     * Immediate. Update Effect: All established diameter peer connections assigned
     * to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("initiate-connection-to-peer")
    public void setInitiateConnectionToPeer(String initiateConnectionToPeer)
    {
        this.initiateConnectionToPeer = initiateConnectionToPeer;
    }

    public LocalEndpoint withInitiateConnectionToPeer(String initiateConnectionToPeer)
    {
        this.initiateConnectionToPeer = initiateConnectionToPeer;
        return this;
    }

    /**
     * Used to express the Disconnect-Cause handling policy to follow upon peer
     * connection close as result of operations over service instances or upon AAA
     * Service disconnection from Diameter Service. Should take as value a reference
     * to disconnect-cause-handling-policy. Update Apply: Immediate Update Effect:
     * No impact on established peer connections. Upon update, the Local Endpoint
     * will apply the referred policy. If not set the Diameter Service uses the
     * default Disconnect-Cause handling policy set to REBOOTING.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    public String getDisconnectCauseHandlingPolicy()
    {
        return disconnectCauseHandlingPolicy;
    }

    /**
     * Used to express the Disconnect-Cause handling policy to follow upon peer
     * connection close as result of operations over service instances or upon AAA
     * Service disconnection from Diameter Service. Should take as value a reference
     * to disconnect-cause-handling-policy. Update Apply: Immediate Update Effect:
     * No impact on established peer connections. Upon update, the Local Endpoint
     * will apply the referred policy. If not set the Diameter Service uses the
     * default Disconnect-Cause handling policy set to REBOOTING.
     * 
     */
    @JsonProperty("disconnect-cause-handling-policy")
    public void setDisconnectCauseHandlingPolicy(String disconnectCauseHandlingPolicy)
    {
        this.disconnectCauseHandlingPolicy = disconnectCauseHandlingPolicy;
    }

    public LocalEndpoint withDisconnectCauseHandlingPolicy(String disconnectCauseHandlingPolicy)
    {
        this.disconnectCauseHandlingPolicy = disconnectCauseHandlingPolicy;
        return this;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-peer attribute will
     * take a value if the terminate-connection-from-accepted-peers or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those static-peer
     * instances which are representing Peer Diameter Nodes allowed to initiate
     * connection establishment towards the Local Endpoint (see static-peer). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-peer")
    public List<String> getTerminateConnectionFromPeer()
    {
        return terminateConnectionFromPeer;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-peer attribute will
     * take a value if the terminate-connection-from-accepted-peers or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those static-peer
     * instances which are representing Peer Diameter Nodes allowed to initiate
     * connection establishment towards the Local Endpoint (see static-peer). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-peer")
    public void setTerminateConnectionFromPeer(List<String> terminateConnectionFromPeer)
    {
        this.terminateConnectionFromPeer = terminateConnectionFromPeer;
    }

    public LocalEndpoint withTerminateConnectionFromPeer(List<String> terminateConnectionFromPeer)
    {
        this.terminateConnectionFromPeer = terminateConnectionFromPeer;
        return this;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-accepted-peers
     * attribute will take a value if the terminate-connection-from-peer or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those
     * dynamic-peer-acceptor instances which are used to express a pattern based
     * representation of Peer Diameter Nodes allowed to initiate connection
     * establishment towards the Local Endpoint (see dynamic-peer-acceptor). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-accepted-peers")
    public List<String> getTerminateConnectionFromAcceptedPeers()
    {
        return terminateConnectionFromAcceptedPeers;
    }

    /**
     * This attribute is used to set a Local Endpoint to play a connection
     * termination (server) role. The terminate-connection-from-accepted-peers
     * attribute will take a value if the terminate-connection-from-peer or
     * initiate-connection-to-peer attributes are not assigned with value. The
     * attribute should take as value a list of references towards those
     * dynamic-peer-acceptor instances which are used to express a pattern based
     * representation of Peer Diameter Nodes allowed to initiate connection
     * establishment towards the Local Endpoint (see dynamic-peer-acceptor). Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * assigned to local endpoint are dropped and reestablished by need with updated
     * information.
     * 
     */
    @JsonProperty("terminate-connection-from-accepted-peers")
    public void setTerminateConnectionFromAcceptedPeers(List<String> terminateConnectionFromAcceptedPeers)
    {
        this.terminateConnectionFromAcceptedPeers = terminateConnectionFromAcceptedPeers;
    }

    public LocalEndpoint withTerminateConnectionFromAcceptedPeers(List<String> terminateConnectionFromAcceptedPeers)
    {
        this.terminateConnectionFromAcceptedPeers = terminateConnectionFromAcceptedPeers;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public LocalEndpoint withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Used to assign TCP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-tcp")
    public TransportTcp getTransportTcp()
    {
        return transportTcp;
    }

    /**
     * Used to assign TCP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-tcp")
    public void setTransportTcp(TransportTcp transportTcp)
    {
        this.transportTcp = transportTcp;
    }

    public LocalEndpoint withTransportTcp(TransportTcp transportTcp)
    {
        this.transportTcp = transportTcp;
        return this;
    }

    /**
     * Used to assign SCTP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-sctp")
    public TransportSctp getTransportSctp()
    {
        return transportSctp;
    }

    /**
     * Used to assign SCTP transport capability for a Local Endpoint.
     * 
     */
    @JsonProperty("transport-sctp")
    public void setTransportSctp(TransportSctp transportSctp)
    {
        this.transportSctp = transportSctp;
    }

    public LocalEndpoint withTransportSctp(TransportSctp transportSctp)
    {
        this.transportSctp = transportSctp;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalEndpoint.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("dscp");
        sb.append('=');
        sb.append(((this.dscp == null) ? "<null>" : this.dscp));
        sb.append(',');
        sb.append("instanceCount");
        sb.append('=');
        sb.append(((this.instanceCount == null) ? "<null>" : this.instanceCount));
        sb.append(',');
        sb.append("watchdogTimer");
        sb.append('=');
        sb.append(((this.watchdogTimer == null) ? "<null>" : this.watchdogTimer));
        sb.append(',');
        sb.append("reconnectTimer");
        sb.append('=');
        sb.append(((this.reconnectTimer == null) ? "<null>" : this.reconnectTimer));
        sb.append(',');
        sb.append("node");
        sb.append('=');
        sb.append(((this.node == null) ? "<null>" : this.node));
        sb.append(',');
        sb.append("initiateConnectionToPeer");
        sb.append('=');
        sb.append(((this.initiateConnectionToPeer == null) ? "<null>" : this.initiateConnectionToPeer));
        sb.append(',');
        sb.append("disconnectCauseHandlingPolicy");
        sb.append('=');
        sb.append(((this.disconnectCauseHandlingPolicy == null) ? "<null>" : this.disconnectCauseHandlingPolicy));
        sb.append(',');
        sb.append("terminateConnectionFromPeer");
        sb.append('=');
        sb.append(((this.terminateConnectionFromPeer == null) ? "<null>" : this.terminateConnectionFromPeer));
        sb.append(',');
        sb.append("terminateConnectionFromAcceptedPeers");
        sb.append('=');
        sb.append(((this.terminateConnectionFromAcceptedPeers == null) ? "<null>" : this.terminateConnectionFromAcceptedPeers));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("transportTcp");
        sb.append('=');
        sb.append(((this.transportTcp == null) ? "<null>" : this.transportTcp));
        sb.append(',');
        sb.append("transportSctp");
        sb.append('=');
        sb.append(((this.transportSctp == null) ? "<null>" : this.transportSctp));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.transportTcp == null) ? 0 : this.transportTcp.hashCode()));
        result = ((result * 31) + ((this.reconnectTimer == null) ? 0 : this.reconnectTimer.hashCode()));
        result = ((result * 31) + ((this.initiateConnectionToPeer == null) ? 0 : this.initiateConnectionToPeer.hashCode()));
        result = ((result * 31) + ((this.terminateConnectionFromPeer == null) ? 0 : this.terminateConnectionFromPeer.hashCode()));
        result = ((result * 31) + ((this.transportSctp == null) ? 0 : this.transportSctp.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.watchdogTimer == null) ? 0 : this.watchdogTimer.hashCode()));
        result = ((result * 31) + ((this.node == null) ? 0 : this.node.hashCode()));
        result = ((result * 31) + ((this.dscp == null) ? 0 : this.dscp.hashCode()));
        result = ((result * 31) + ((this.instanceCount == null) ? 0 : this.instanceCount.hashCode()));
        result = ((result * 31) + ((this.disconnectCauseHandlingPolicy == null) ? 0 : this.disconnectCauseHandlingPolicy.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.terminateConnectionFromAcceptedPeers == null) ? 0 : this.terminateConnectionFromAcceptedPeers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof LocalEndpoint) == false)
        {
            return false;
        }
        LocalEndpoint rhs = ((LocalEndpoint) other);
        return (((((((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                            && ((this.transportTcp == rhs.transportTcp) || ((this.transportTcp != null) && this.transportTcp.equals(rhs.transportTcp))))
                           && ((this.reconnectTimer == rhs.reconnectTimer)
                               || ((this.reconnectTimer != null) && this.reconnectTimer.equals(rhs.reconnectTimer))))
                          && ((this.initiateConnectionToPeer == rhs.initiateConnectionToPeer)
                              || ((this.initiateConnectionToPeer != null) && this.initiateConnectionToPeer.equals(rhs.initiateConnectionToPeer))))
                         && ((this.terminateConnectionFromPeer == rhs.terminateConnectionFromPeer)
                             || ((this.terminateConnectionFromPeer != null) && this.terminateConnectionFromPeer.equals(rhs.terminateConnectionFromPeer))))
                        && ((this.transportSctp == rhs.transportSctp) || ((this.transportSctp != null) && this.transportSctp.equals(rhs.transportSctp))))
                       && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                      && ((this.watchdogTimer == rhs.watchdogTimer) || ((this.watchdogTimer != null) && this.watchdogTimer.equals(rhs.watchdogTimer))))
                     && ((this.node == rhs.node) || ((this.node != null) && this.node.equals(rhs.node))))
                    && ((this.dscp == rhs.dscp) || ((this.dscp != null) && this.dscp.equals(rhs.dscp))))
                   && ((this.instanceCount == rhs.instanceCount) || ((this.instanceCount != null) && this.instanceCount.equals(rhs.instanceCount))))
                  && ((this.disconnectCauseHandlingPolicy == rhs.disconnectCauseHandlingPolicy)
                      || ((this.disconnectCauseHandlingPolicy != null) && this.disconnectCauseHandlingPolicy.equals(rhs.disconnectCauseHandlingPolicy))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.terminateConnectionFromAcceptedPeers == rhs.terminateConnectionFromAcceptedPeers)
                    || ((this.terminateConnectionFromAcceptedPeers != null)
                        && this.terminateConnectionFromAcceptedPeers.equals(rhs.terminateConnectionFromAcceptedPeers))));
    }

}
