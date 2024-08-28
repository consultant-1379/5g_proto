
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id",
                     "service-execution-environment",
                     "ingress-request-pending-timer",
                     "egress-request-pending-timer",
                     "request-send-max-retry",
                     "request-error-handler",
                     "request-stack-handler-selection",
                     "answer-stack-handler-selection",
                     "session-id-format-type",
                     "enabled",
                     "predictive-loop-avoidance-enabled",
                     "user-label",
                     "application",
                     "reroute-policy",
                     "local-endpoint-reference" })
public class Service
{

    /**
     * Used to specify the key of the service instance. The key value provided
     * should match the name the related AAA Service implementation was registering
     * with towards the Diameter Service through one of the Diameter Service User
     * interfaces. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the service instance. The key value provided should match the name the related AAA Service implementation was registering with towards the Diameter Service through one of the Diameter Service User interfaces.")
    private String id;
    /**
     * Used to indicate the execution environment type the Diameter Service User is
     * using for AAA Service implementation.
     * 
     */
    @JsonProperty("service-execution-environment")
    @JsonPropertyDescription("Used to indicate the execution environment type the Diameter Service User is using for AAA Service implementation.")
    private Service.ServiceExecutionEnvironment serviceExecutionEnvironment;
    /**
     * Used to specify the time-out period the Diameter Service waits for a AAA
     * Service instance to answer a diameter ingress request message. The time-out
     * value provided is interpreted in milliseconds. The Diameter Stack will free
     * resources allocated for an ingress request message if not answered by AAA
     * Service instance in the indicated time-out period. An egress answer message
     * received for the related ingress request after the indicated time-out period
     * is discarded by the Diameter Service. Each time an egress diameter answer
     * message is dropped by the Diameter Stack due to the time-out configured
     * through the request-pending-timer, the
     * Diameter.EgressAnswMsgDiscarded.TimeOut counter is stepped. Unit: millisecond
     * (ms) MT Impacted: Diameter.EgressAnswMsgDiscarded.TimeOut Update Apply:
     * Immediate. Update Effect: No impact on established peer connections. The
     * Diameter Service will wait the indicated time-out period for diameter answer
     * messages pertaining to newly received diameter ingress request messages.
     * 
     */
    @JsonProperty("ingress-request-pending-timer")
    @JsonPropertyDescription("Used to specify the time-out period the Diameter Service waits for a AAA Service instance to answer a diameter ingress request message. The time-out value provided is interpreted in milliseconds. The Diameter Stack will free resources allocated for an ingress request message if not answered by AAA Service instance in the indicated time-out period. An egress answer message received for the related ingress request after the indicated time-out period is discarded by the Diameter Service. Each time an egress diameter answer message is dropped by the Diameter Stack due to the time-out configured through the request-pending-timer, the Diameter.EgressAnswMsgDiscarded.TimeOut counter is stepped. Unit: millisecond (ms) MT Impacted: Diameter.EgressAnswMsgDiscarded.TimeOut Update Apply: Immediate. Update Effect: No impact on established peer connections. The Diameter Service will wait the indicated time-out period for diameter answer messages pertaining to newly received diameter ingress request messages.")
    private Long ingressRequestPendingTimer = 5000L;
    /**
     * Used to specify the time-out period the Diameter Service waits for an answer
     * message related to the request message sent by the AAA Service instance. The
     * egress request message is stored temporarily in the pending queue of the
     * Diameter Service for this timeout period. The time-out value provided is
     * interpreted in milliseconds. The Diameter Stack will free resources allocated
     * for an egress request message and step the
     * Diameter.EgressReqMsgDiscarded.TimeOut counter if not answered by remote peer
     * in the indicated time-out period. Unit: millisecond (ms) MT Impacted:
     * Diameter.EgressReqMsgDiscarded.TimeOut Update Apply: Immediate. Update
     * Effect: No impact on established peer connections.The Diameter Service will
     * wait the indicated time-out period for diameter answer messages pertaining to
     * newly sent diameter egress request messages.
     * 
     */
    @JsonProperty("egress-request-pending-timer")
    @JsonPropertyDescription("Used to specify the time-out period the Diameter Service waits for an answer message related to the request message sent by the AAA Service instance. The egress request message is stored temporarily in the pending queue of the Diameter Service for this timeout period. The time-out value provided is interpreted in milliseconds. The Diameter Stack will free resources allocated for an egress request message and step the Diameter.EgressReqMsgDiscarded.TimeOut counter if not answered by remote peer in the indicated time-out period. Unit: millisecond (ms) MT Impacted: Diameter.EgressReqMsgDiscarded.TimeOut Update Apply: Immediate. Update Effect: No impact on established peer connections.The Diameter Service will wait the indicated time-out period for diameter answer messages pertaining to newly sent diameter egress request messages.")
    private Long egressRequestPendingTimer = 10000L;
    /**
     * Used to specify the maximum integer of times the Diameter Service retries to
     * send a diameter egress request message. Egress request message resend can be
     * triggered by the following events: Link fail-over That is, the active peer
     * connection is lost and the stack is failing over to use as active another
     * peer connection. Time-out The time-out specified by the _AAA Service_
     * implementation for the related diameter egress request message expires (see
     * also, related API Reference Manual). Diameter answer message An answer with
     * result code `DIAMETER_UNABLE_TO_DELIVER (3002)` is received. Any of the above
     * events should appear the Diameter Service will try to resend the egress
     * request messages the indicated amount of times. This, however, might be
     * constraint by the integer of fail-overs Diameter Service can perform upon
     * link loss. That is, if there is no link available to send out a request
     * message no message caching and retries are performed. Diameter Service will
     * indicate related AAA Service instance the inability to deliver request
     * messages. Each time an egress diameter message is resent the
     * Diameter_EgressReqMsgResent_TotalCount counter is stepped. NOTE: The
     * connection is blacklisted for a certain request in case of time-out. When an
     * answer with result code `3002` is received from a host, all connections
     * towards that host is blacklisted for a certain request. During the retried
     * delivery of the request, the next available connection is selected based on
     * the routing table but the blacklisted connections are excluded. Unit: Count
     * MT Impacted: Diameter.EgressReqMsgResent.TotalCount Update Apply: Immediate.
     * Update Effect: No impact on established peer connections. Diameter Service
     * will apply the newly configured retry count for egress request messages
     * stored in message pending queue.
     * 
     */
    @JsonProperty("request-send-max-retry")
    @JsonPropertyDescription("Used to specify the maximum integer of times the Diameter Service retries to send a diameter egress request message. Egress request message resend can be triggered by the following events: Link fail-over That is, the active peer connection is lost and the stack is failing over to use as active another peer connection. Time-out The time-out specified by the _AAA Service_ implementation for the related diameter egress request message expires (see also, related API Reference Manual). Diameter answer message An answer with result code `DIAMETER_UNABLE_TO_DELIVER (3002)` is received. Any of the above events should appear the Diameter Service will try to resend the egress request messages the indicated amount of times. This, however, might be constraint by the integer of fail-overs Diameter Service can perform upon link loss. That is, if there is no link available to send out a request message no message caching and retries are performed. Diameter Service will indicate related AAA Service instance the inability to deliver request messages. Each time an egress diameter message is resent the Diameter_EgressReqMsgResent_TotalCount counter is stepped. NOTE: The connection is blacklisted for a certain request in case of time-out. When an answer with result code `3002` is received from a host, all connections towards that host is blacklisted for a certain request. During the retried delivery of the request, the next available connection is selected based on the routing table but the blacklisted connections are excluded. Unit: Count MT Impacted: Diameter.EgressReqMsgResent.TotalCount Update Apply: Immediate. Update Effect: No impact on established peer connections. Diameter Service will apply the newly configured retry count for egress request messages stored in message pending queue.")
    private Long requestSendMaxRetry = 3L;
    /**
     * Used to indicate the layer the diameter error messages are created for
     * related erroneous diameter ingress request messages.
     * 
     */
    @JsonProperty("request-error-handler")
    @JsonPropertyDescription("Used to indicate the layer the diameter error messages are created for related erroneous diameter ingress request messages.")
    private Service.RequestErrorHandler requestErrorHandler;
    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress request message processing on Diameter Service level
     * (that is, for message grammar validation, routing entry evaluation and
     * message content based common diameter facility activation). Wether to use the
     * LOCAL or the CLUSTER compute resource selection algorithm should be the
     * result of a dimensioning activity. However, if a AAA Service needs no complex
     * message processing activities to be performed on diameter level the LOCAL
     * algorithm is recommended to be selected. This algorithm will provide better
     * figures both in latency and throughput for such type of AAA Services. The
     * default CLUSTER compute resource selection algorithm is defined on worst case
     * basis (safety margin). Meaning, big diameter cluster with few dominant peer
     * connections with complex message processing needs on Diameter Service level
     * (that is, not a typical deployment scenario for most of the Diameter Service
     * user AAA Services). Update Apply: Immediate. Update Effect: No impact on
     * established peer connections. Diameter Service will apply the newly
     * configured distribution mechanism for message processing.
     * 
     */
    @JsonProperty("request-stack-handler-selection")
    @JsonPropertyDescription("Used to specify the algorithm based on which compute resources are selected for diameter ingress request message processing on Diameter Service level (that is, for message grammar validation, routing entry evaluation and message content based common diameter facility activation). Wether to use the LOCAL or the CLUSTER compute resource selection algorithm should be the result of a dimensioning activity. However, if a AAA Service needs no complex message processing activities to be performed on diameter level the LOCAL algorithm is recommended to be selected. This algorithm will provide better figures both in latency and throughput for such type of AAA Services. The default CLUSTER compute resource selection algorithm is defined on worst case basis (safety margin). Meaning, big diameter cluster with few dominant peer connections with complex message processing needs on Diameter Service level (that is, not a typical deployment scenario for most of the Diameter Service user AAA Services). Update Apply: Immediate. Update Effect: No impact on established peer connections. Diameter Service will apply the newly configured distribution mechanism for message processing.")
    private Service.RequestStackHandlerSelection requestStackHandlerSelection = Service.RequestStackHandlerSelection.fromValue("local");
    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress answer message processing on Diameter Stack level (that
     * is, for message grammar validation and message content based common diameter
     * facility activation). Wether to use the LOCAL or the CLUSTER compute resource
     * selection algorithm should be the result of a dimensioning activity. However,
     * if a AAA Service needs no complex message processing activities to be
     * performed on diameter level the LOCAL algorithm is recommended to be
     * selected. This algorithm will provide better figures both in latency and
     * throughput for such type of AAA Services. The default CLUSTER compute
     * resource selection algorithm is defined on worst case basis (safety margin).
     * Meaning, big diameter cluster with few dominant peer connections with complex
     * message processing needs on Diameter Service level (that is, not a typical
     * deployment scenario for most of the Diameter Service user AAA Services).
     * Update Apply: Immediate. Update Effect: No impact on established peer
     * connections. Diameter Service will apply the newly configured distribution
     * mechanism for message processing.
     * 
     */
    @JsonProperty("answer-stack-handler-selection")
    @JsonPropertyDescription("Used to specify the algorithm based on which compute resources are selected for diameter ingress answer message processing on Diameter Stack level (that is, for message grammar validation and message content based common diameter facility activation). Wether to use the LOCAL or the CLUSTER compute resource selection algorithm should be the result of a dimensioning activity. However, if a AAA Service needs no complex message processing activities to be performed on diameter level the LOCAL algorithm is recommended to be selected. This algorithm will provide better figures both in latency and throughput for such type of AAA Services. The default CLUSTER compute resource selection algorithm is defined on worst case basis (safety margin). Meaning, big diameter cluster with few dominant peer connections with complex message processing needs on Diameter Service level (that is, not a typical deployment scenario for most of the Diameter Service user AAA Services). Update Apply: Immediate. Update Effect: No impact on established peer connections. Diameter Service will apply the newly configured distribution mechanism for message processing.")
    private Service.AnswerStackHandlerSelection answerStackHandlerSelection = Service.AnswerStackHandlerSelection.fromValue("local");
    /**
     * Used to indicate one of the different representation format types of the
     * generated Session-Id AVP. For detailed information, see
     * https://tools.ietf.org/html/rfc6733#section-8.8
     * 
     */
    @JsonProperty("session-id-format-type")
    @JsonPropertyDescription("Used to indicate one of the different representation format types of the generated Session-Id AVP. For detailed information, see https://tools.ietf.org/html/rfc6733#section-8.8")
    private Service.SessionIdFormatType sessionIdFormatType;
    /**
     * Used to enable/disable a AAA Service. As result of a service disable, all
     * related peer diameter connections will be closed with cause indicated by the
     * configured disconnect cause policy. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable/disable a AAA Service. As result of a service disable, all related peer diameter connections will be closed with cause indicated by the configured disconnect cause policy. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled")
    private Boolean enabled = true;
    /**
     * System initialized attribute used by AAA Services playing Relay or Proxy
     * Agent role. Predictive loop avoidance shall be enabled by relay or proxy
     * agent, so Diameter checks for forwarding loops before forwarding or routing a
     * request. See https://tools.ietf.org/html/rfc6733#section-6.1.7
     * 
     */
    @JsonProperty("predictive-loop-avoidance-enabled")
    @JsonPropertyDescription("System initialized attribute used by AAA Services playing Relay or Proxy Agent role. Predictive loop avoidance shall be enabled by relay or proxy agent, so Diameter checks for forwarding loops before forwarding or routing a request. See https://tools.ietf.org/html/rfc6733#section-6.1.7")
    private Boolean predictiveLoopAvoidanceEnabled = false;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * Used to indicate the set of Diameter Applications
     * (https://tools.ietf.org/html/rfc6733#section-1.3.4) implemented by the AAA
     * Service.
     * 
     */
    @JsonProperty("application")
    @JsonPropertyDescription("Used to indicate the set of Diameter Applications (https://tools.ietf.org/html/rfc6733#section-1.3.4) implemented by the AAA Service.")
    private List<String> application = new ArrayList<String>();
    /**
     * Reference to a reroute policy
     * 
     */
    @JsonProperty("reroute-policy")
    @JsonPropertyDescription("Reference to a reroute policy")
    private String reroutePolicy;
    /**
     * A local-endpoint-reference instance is used to associate an already defined
     * diameter transport local endpoint with a AAA Service. A AAA Service can use
     * arbitrary integer of transport local endpoints. However, all of these
     * endpoints must be associated with the same Diameter Node (represented by
     * node). The settings on local-endpoint-reference instance level are
     * influencing the way the peer connections associated with a certain transport
     * endpoint are handled.
     * 
     */
    @JsonProperty("local-endpoint-reference")
    @JsonPropertyDescription("A local-endpoint-reference instance is used to associate an already defined diameter transport local endpoint with a AAA Service. A AAA Service can use arbitrary integer of transport local endpoints. However, all of these endpoints must be associated with the same Diameter Node (represented by node). The settings on local-endpoint-reference instance level are influencing the way the peer connections associated with a certain transport endpoint are handled.")
    private List<LocalEndpointReference> localEndpointReference = new ArrayList<LocalEndpointReference>();

    /**
     * Used to specify the key of the service instance. The key value provided
     * should match the name the related AAA Service implementation was registering
     * with towards the Diameter Service through one of the Diameter Service User
     * interfaces. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the service instance. The key value provided
     * should match the name the related AAA Service implementation was registering
     * with towards the Diameter Service through one of the Diameter Service User
     * interfaces. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public Service withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to indicate the execution environment type the Diameter Service User is
     * using for AAA Service implementation.
     * 
     */
    @JsonProperty("service-execution-environment")
    public Service.ServiceExecutionEnvironment getServiceExecutionEnvironment()
    {
        return serviceExecutionEnvironment;
    }

    /**
     * Used to indicate the execution environment type the Diameter Service User is
     * using for AAA Service implementation.
     * 
     */
    @JsonProperty("service-execution-environment")
    public void setServiceExecutionEnvironment(Service.ServiceExecutionEnvironment serviceExecutionEnvironment)
    {
        this.serviceExecutionEnvironment = serviceExecutionEnvironment;
    }

    public Service withServiceExecutionEnvironment(Service.ServiceExecutionEnvironment serviceExecutionEnvironment)
    {
        this.serviceExecutionEnvironment = serviceExecutionEnvironment;
        return this;
    }

    /**
     * Used to specify the time-out period the Diameter Service waits for a AAA
     * Service instance to answer a diameter ingress request message. The time-out
     * value provided is interpreted in milliseconds. The Diameter Stack will free
     * resources allocated for an ingress request message if not answered by AAA
     * Service instance in the indicated time-out period. An egress answer message
     * received for the related ingress request after the indicated time-out period
     * is discarded by the Diameter Service. Each time an egress diameter answer
     * message is dropped by the Diameter Stack due to the time-out configured
     * through the request-pending-timer, the
     * Diameter.EgressAnswMsgDiscarded.TimeOut counter is stepped. Unit: millisecond
     * (ms) MT Impacted: Diameter.EgressAnswMsgDiscarded.TimeOut Update Apply:
     * Immediate. Update Effect: No impact on established peer connections. The
     * Diameter Service will wait the indicated time-out period for diameter answer
     * messages pertaining to newly received diameter ingress request messages.
     * 
     */
    @JsonProperty("ingress-request-pending-timer")
    public Long getIngressRequestPendingTimer()
    {
        return ingressRequestPendingTimer;
    }

    /**
     * Used to specify the time-out period the Diameter Service waits for a AAA
     * Service instance to answer a diameter ingress request message. The time-out
     * value provided is interpreted in milliseconds. The Diameter Stack will free
     * resources allocated for an ingress request message if not answered by AAA
     * Service instance in the indicated time-out period. An egress answer message
     * received for the related ingress request after the indicated time-out period
     * is discarded by the Diameter Service. Each time an egress diameter answer
     * message is dropped by the Diameter Stack due to the time-out configured
     * through the request-pending-timer, the
     * Diameter.EgressAnswMsgDiscarded.TimeOut counter is stepped. Unit: millisecond
     * (ms) MT Impacted: Diameter.EgressAnswMsgDiscarded.TimeOut Update Apply:
     * Immediate. Update Effect: No impact on established peer connections. The
     * Diameter Service will wait the indicated time-out period for diameter answer
     * messages pertaining to newly received diameter ingress request messages.
     * 
     */
    @JsonProperty("ingress-request-pending-timer")
    public void setIngressRequestPendingTimer(Long ingressRequestPendingTimer)
    {
        this.ingressRequestPendingTimer = ingressRequestPendingTimer;
    }

    public Service withIngressRequestPendingTimer(Long ingressRequestPendingTimer)
    {
        this.ingressRequestPendingTimer = ingressRequestPendingTimer;
        return this;
    }

    /**
     * Used to specify the time-out period the Diameter Service waits for an answer
     * message related to the request message sent by the AAA Service instance. The
     * egress request message is stored temporarily in the pending queue of the
     * Diameter Service for this timeout period. The time-out value provided is
     * interpreted in milliseconds. The Diameter Stack will free resources allocated
     * for an egress request message and step the
     * Diameter.EgressReqMsgDiscarded.TimeOut counter if not answered by remote peer
     * in the indicated time-out period. Unit: millisecond (ms) MT Impacted:
     * Diameter.EgressReqMsgDiscarded.TimeOut Update Apply: Immediate. Update
     * Effect: No impact on established peer connections.The Diameter Service will
     * wait the indicated time-out period for diameter answer messages pertaining to
     * newly sent diameter egress request messages.
     * 
     */
    @JsonProperty("egress-request-pending-timer")
    public Long getEgressRequestPendingTimer()
    {
        return egressRequestPendingTimer;
    }

    /**
     * Used to specify the time-out period the Diameter Service waits for an answer
     * message related to the request message sent by the AAA Service instance. The
     * egress request message is stored temporarily in the pending queue of the
     * Diameter Service for this timeout period. The time-out value provided is
     * interpreted in milliseconds. The Diameter Stack will free resources allocated
     * for an egress request message and step the
     * Diameter.EgressReqMsgDiscarded.TimeOut counter if not answered by remote peer
     * in the indicated time-out period. Unit: millisecond (ms) MT Impacted:
     * Diameter.EgressReqMsgDiscarded.TimeOut Update Apply: Immediate. Update
     * Effect: No impact on established peer connections.The Diameter Service will
     * wait the indicated time-out period for diameter answer messages pertaining to
     * newly sent diameter egress request messages.
     * 
     */
    @JsonProperty("egress-request-pending-timer")
    public void setEgressRequestPendingTimer(Long egressRequestPendingTimer)
    {
        this.egressRequestPendingTimer = egressRequestPendingTimer;
    }

    public Service withEgressRequestPendingTimer(Long egressRequestPendingTimer)
    {
        this.egressRequestPendingTimer = egressRequestPendingTimer;
        return this;
    }

    /**
     * Used to specify the maximum integer of times the Diameter Service retries to
     * send a diameter egress request message. Egress request message resend can be
     * triggered by the following events: Link fail-over That is, the active peer
     * connection is lost and the stack is failing over to use as active another
     * peer connection. Time-out The time-out specified by the _AAA Service_
     * implementation for the related diameter egress request message expires (see
     * also, related API Reference Manual). Diameter answer message An answer with
     * result code `DIAMETER_UNABLE_TO_DELIVER (3002)` is received. Any of the above
     * events should appear the Diameter Service will try to resend the egress
     * request messages the indicated amount of times. This, however, might be
     * constraint by the integer of fail-overs Diameter Service can perform upon
     * link loss. That is, if there is no link available to send out a request
     * message no message caching and retries are performed. Diameter Service will
     * indicate related AAA Service instance the inability to deliver request
     * messages. Each time an egress diameter message is resent the
     * Diameter_EgressReqMsgResent_TotalCount counter is stepped. NOTE: The
     * connection is blacklisted for a certain request in case of time-out. When an
     * answer with result code `3002` is received from a host, all connections
     * towards that host is blacklisted for a certain request. During the retried
     * delivery of the request, the next available connection is selected based on
     * the routing table but the blacklisted connections are excluded. Unit: Count
     * MT Impacted: Diameter.EgressReqMsgResent.TotalCount Update Apply: Immediate.
     * Update Effect: No impact on established peer connections. Diameter Service
     * will apply the newly configured retry count for egress request messages
     * stored in message pending queue.
     * 
     */
    @JsonProperty("request-send-max-retry")
    public Long getRequestSendMaxRetry()
    {
        return requestSendMaxRetry;
    }

    /**
     * Used to specify the maximum integer of times the Diameter Service retries to
     * send a diameter egress request message. Egress request message resend can be
     * triggered by the following events: Link fail-over That is, the active peer
     * connection is lost and the stack is failing over to use as active another
     * peer connection. Time-out The time-out specified by the _AAA Service_
     * implementation for the related diameter egress request message expires (see
     * also, related API Reference Manual). Diameter answer message An answer with
     * result code `DIAMETER_UNABLE_TO_DELIVER (3002)` is received. Any of the above
     * events should appear the Diameter Service will try to resend the egress
     * request messages the indicated amount of times. This, however, might be
     * constraint by the integer of fail-overs Diameter Service can perform upon
     * link loss. That is, if there is no link available to send out a request
     * message no message caching and retries are performed. Diameter Service will
     * indicate related AAA Service instance the inability to deliver request
     * messages. Each time an egress diameter message is resent the
     * Diameter_EgressReqMsgResent_TotalCount counter is stepped. NOTE: The
     * connection is blacklisted for a certain request in case of time-out. When an
     * answer with result code `3002` is received from a host, all connections
     * towards that host is blacklisted for a certain request. During the retried
     * delivery of the request, the next available connection is selected based on
     * the routing table but the blacklisted connections are excluded. Unit: Count
     * MT Impacted: Diameter.EgressReqMsgResent.TotalCount Update Apply: Immediate.
     * Update Effect: No impact on established peer connections. Diameter Service
     * will apply the newly configured retry count for egress request messages
     * stored in message pending queue.
     * 
     */
    @JsonProperty("request-send-max-retry")
    public void setRequestSendMaxRetry(Long requestSendMaxRetry)
    {
        this.requestSendMaxRetry = requestSendMaxRetry;
    }

    public Service withRequestSendMaxRetry(Long requestSendMaxRetry)
    {
        this.requestSendMaxRetry = requestSendMaxRetry;
        return this;
    }

    /**
     * Used to indicate the layer the diameter error messages are created for
     * related erroneous diameter ingress request messages.
     * 
     */
    @JsonProperty("request-error-handler")
    public Service.RequestErrorHandler getRequestErrorHandler()
    {
        return requestErrorHandler;
    }

    /**
     * Used to indicate the layer the diameter error messages are created for
     * related erroneous diameter ingress request messages.
     * 
     */
    @JsonProperty("request-error-handler")
    public void setRequestErrorHandler(Service.RequestErrorHandler requestErrorHandler)
    {
        this.requestErrorHandler = requestErrorHandler;
    }

    public Service withRequestErrorHandler(Service.RequestErrorHandler requestErrorHandler)
    {
        this.requestErrorHandler = requestErrorHandler;
        return this;
    }

    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress request message processing on Diameter Service level
     * (that is, for message grammar validation, routing entry evaluation and
     * message content based common diameter facility activation). Wether to use the
     * LOCAL or the CLUSTER compute resource selection algorithm should be the
     * result of a dimensioning activity. However, if a AAA Service needs no complex
     * message processing activities to be performed on diameter level the LOCAL
     * algorithm is recommended to be selected. This algorithm will provide better
     * figures both in latency and throughput for such type of AAA Services. The
     * default CLUSTER compute resource selection algorithm is defined on worst case
     * basis (safety margin). Meaning, big diameter cluster with few dominant peer
     * connections with complex message processing needs on Diameter Service level
     * (that is, not a typical deployment scenario for most of the Diameter Service
     * user AAA Services). Update Apply: Immediate. Update Effect: No impact on
     * established peer connections. Diameter Service will apply the newly
     * configured distribution mechanism for message processing.
     * 
     */
    @JsonProperty("request-stack-handler-selection")
    public Service.RequestStackHandlerSelection getRequestStackHandlerSelection()
    {
        return requestStackHandlerSelection;
    }

    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress request message processing on Diameter Service level
     * (that is, for message grammar validation, routing entry evaluation and
     * message content based common diameter facility activation). Wether to use the
     * LOCAL or the CLUSTER compute resource selection algorithm should be the
     * result of a dimensioning activity. However, if a AAA Service needs no complex
     * message processing activities to be performed on diameter level the LOCAL
     * algorithm is recommended to be selected. This algorithm will provide better
     * figures both in latency and throughput for such type of AAA Services. The
     * default CLUSTER compute resource selection algorithm is defined on worst case
     * basis (safety margin). Meaning, big diameter cluster with few dominant peer
     * connections with complex message processing needs on Diameter Service level
     * (that is, not a typical deployment scenario for most of the Diameter Service
     * user AAA Services). Update Apply: Immediate. Update Effect: No impact on
     * established peer connections. Diameter Service will apply the newly
     * configured distribution mechanism for message processing.
     * 
     */
    @JsonProperty("request-stack-handler-selection")
    public void setRequestStackHandlerSelection(Service.RequestStackHandlerSelection requestStackHandlerSelection)
    {
        this.requestStackHandlerSelection = requestStackHandlerSelection;
    }

    public Service withRequestStackHandlerSelection(Service.RequestStackHandlerSelection requestStackHandlerSelection)
    {
        this.requestStackHandlerSelection = requestStackHandlerSelection;
        return this;
    }

    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress answer message processing on Diameter Stack level (that
     * is, for message grammar validation and message content based common diameter
     * facility activation). Wether to use the LOCAL or the CLUSTER compute resource
     * selection algorithm should be the result of a dimensioning activity. However,
     * if a AAA Service needs no complex message processing activities to be
     * performed on diameter level the LOCAL algorithm is recommended to be
     * selected. This algorithm will provide better figures both in latency and
     * throughput for such type of AAA Services. The default CLUSTER compute
     * resource selection algorithm is defined on worst case basis (safety margin).
     * Meaning, big diameter cluster with few dominant peer connections with complex
     * message processing needs on Diameter Service level (that is, not a typical
     * deployment scenario for most of the Diameter Service user AAA Services).
     * Update Apply: Immediate. Update Effect: No impact on established peer
     * connections. Diameter Service will apply the newly configured distribution
     * mechanism for message processing.
     * 
     */
    @JsonProperty("answer-stack-handler-selection")
    public Service.AnswerStackHandlerSelection getAnswerStackHandlerSelection()
    {
        return answerStackHandlerSelection;
    }

    /**
     * Used to specify the algorithm based on which compute resources are selected
     * for diameter ingress answer message processing on Diameter Stack level (that
     * is, for message grammar validation and message content based common diameter
     * facility activation). Wether to use the LOCAL or the CLUSTER compute resource
     * selection algorithm should be the result of a dimensioning activity. However,
     * if a AAA Service needs no complex message processing activities to be
     * performed on diameter level the LOCAL algorithm is recommended to be
     * selected. This algorithm will provide better figures both in latency and
     * throughput for such type of AAA Services. The default CLUSTER compute
     * resource selection algorithm is defined on worst case basis (safety margin).
     * Meaning, big diameter cluster with few dominant peer connections with complex
     * message processing needs on Diameter Service level (that is, not a typical
     * deployment scenario for most of the Diameter Service user AAA Services).
     * Update Apply: Immediate. Update Effect: No impact on established peer
     * connections. Diameter Service will apply the newly configured distribution
     * mechanism for message processing.
     * 
     */
    @JsonProperty("answer-stack-handler-selection")
    public void setAnswerStackHandlerSelection(Service.AnswerStackHandlerSelection answerStackHandlerSelection)
    {
        this.answerStackHandlerSelection = answerStackHandlerSelection;
    }

    public Service withAnswerStackHandlerSelection(Service.AnswerStackHandlerSelection answerStackHandlerSelection)
    {
        this.answerStackHandlerSelection = answerStackHandlerSelection;
        return this;
    }

    /**
     * Used to indicate one of the different representation format types of the
     * generated Session-Id AVP. For detailed information, see
     * https://tools.ietf.org/html/rfc6733#section-8.8
     * 
     */
    @JsonProperty("session-id-format-type")
    public Service.SessionIdFormatType getSessionIdFormatType()
    {
        return sessionIdFormatType;
    }

    /**
     * Used to indicate one of the different representation format types of the
     * generated Session-Id AVP. For detailed information, see
     * https://tools.ietf.org/html/rfc6733#section-8.8
     * 
     */
    @JsonProperty("session-id-format-type")
    public void setSessionIdFormatType(Service.SessionIdFormatType sessionIdFormatType)
    {
        this.sessionIdFormatType = sessionIdFormatType;
    }

    public Service withSessionIdFormatType(Service.SessionIdFormatType sessionIdFormatType)
    {
        this.sessionIdFormatType = sessionIdFormatType;
        return this;
    }

    /**
     * Used to enable/disable a AAA Service. As result of a service disable, all
     * related peer diameter connections will be closed with cause indicated by the
     * configured disconnect cause policy. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable/disable a AAA Service. As result of a service disable, all
     * related peer diameter connections will be closed with cause indicated by the
     * configured disconnect cause policy. When disabled, the following alarm is
     * raised: ADP Diameter, Managed Object Disabled
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Service withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * System initialized attribute used by AAA Services playing Relay or Proxy
     * Agent role. Predictive loop avoidance shall be enabled by relay or proxy
     * agent, so Diameter checks for forwarding loops before forwarding or routing a
     * request. See https://tools.ietf.org/html/rfc6733#section-6.1.7
     * 
     */
    @JsonProperty("predictive-loop-avoidance-enabled")
    public Boolean getPredictiveLoopAvoidanceEnabled()
    {
        return predictiveLoopAvoidanceEnabled;
    }

    /**
     * System initialized attribute used by AAA Services playing Relay or Proxy
     * Agent role. Predictive loop avoidance shall be enabled by relay or proxy
     * agent, so Diameter checks for forwarding loops before forwarding or routing a
     * request. See https://tools.ietf.org/html/rfc6733#section-6.1.7
     * 
     */
    @JsonProperty("predictive-loop-avoidance-enabled")
    public void setPredictiveLoopAvoidanceEnabled(Boolean predictiveLoopAvoidanceEnabled)
    {
        this.predictiveLoopAvoidanceEnabled = predictiveLoopAvoidanceEnabled;
    }

    public Service withPredictiveLoopAvoidanceEnabled(Boolean predictiveLoopAvoidanceEnabled)
    {
        this.predictiveLoopAvoidanceEnabled = predictiveLoopAvoidanceEnabled;
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

    public Service withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Used to indicate the set of Diameter Applications
     * (https://tools.ietf.org/html/rfc6733#section-1.3.4) implemented by the AAA
     * Service.
     * 
     */
    @JsonProperty("application")
    public List<String> getApplication()
    {
        return application;
    }

    /**
     * Used to indicate the set of Diameter Applications
     * (https://tools.ietf.org/html/rfc6733#section-1.3.4) implemented by the AAA
     * Service.
     * 
     */
    @JsonProperty("application")
    public void setApplication(List<String> application)
    {
        this.application = application;
    }

    public Service withApplication(List<String> application)
    {
        this.application = application;
        return this;
    }

    /**
     * Reference to a reroute policy
     * 
     */
    @JsonProperty("reroute-policy")
    public String getReroutePolicy()
    {
        return reroutePolicy;
    }

    /**
     * Reference to a reroute policy
     * 
     */
    @JsonProperty("reroute-policy")
    public void setReroutePolicy(String reroutePolicy)
    {
        this.reroutePolicy = reroutePolicy;
    }

    public Service withReroutePolicy(String reroutePolicy)
    {
        this.reroutePolicy = reroutePolicy;
        return this;
    }

    /**
     * A local-endpoint-reference instance is used to associate an already defined
     * diameter transport local endpoint with a AAA Service. A AAA Service can use
     * arbitrary integer of transport local endpoints. However, all of these
     * endpoints must be associated with the same Diameter Node (represented by
     * node). The settings on local-endpoint-reference instance level are
     * influencing the way the peer connections associated with a certain transport
     * endpoint are handled.
     * 
     */
    @JsonProperty("local-endpoint-reference")
    public List<LocalEndpointReference> getLocalEndpointReference()
    {
        return localEndpointReference;
    }

    /**
     * A local-endpoint-reference instance is used to associate an already defined
     * diameter transport local endpoint with a AAA Service. A AAA Service can use
     * arbitrary integer of transport local endpoints. However, all of these
     * endpoints must be associated with the same Diameter Node (represented by
     * node). The settings on local-endpoint-reference instance level are
     * influencing the way the peer connections associated with a certain transport
     * endpoint are handled.
     * 
     */
    @JsonProperty("local-endpoint-reference")
    public void setLocalEndpointReference(List<LocalEndpointReference> localEndpointReference)
    {
        this.localEndpointReference = localEndpointReference;
    }

    public Service withLocalEndpointReference(List<LocalEndpointReference> localEndpointReference)
    {
        this.localEndpointReference = localEndpointReference;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Service.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("serviceExecutionEnvironment");
        sb.append('=');
        sb.append(((this.serviceExecutionEnvironment == null) ? "<null>" : this.serviceExecutionEnvironment));
        sb.append(',');
        sb.append("ingressRequestPendingTimer");
        sb.append('=');
        sb.append(((this.ingressRequestPendingTimer == null) ? "<null>" : this.ingressRequestPendingTimer));
        sb.append(',');
        sb.append("egressRequestPendingTimer");
        sb.append('=');
        sb.append(((this.egressRequestPendingTimer == null) ? "<null>" : this.egressRequestPendingTimer));
        sb.append(',');
        sb.append("requestSendMaxRetry");
        sb.append('=');
        sb.append(((this.requestSendMaxRetry == null) ? "<null>" : this.requestSendMaxRetry));
        sb.append(',');
        sb.append("requestErrorHandler");
        sb.append('=');
        sb.append(((this.requestErrorHandler == null) ? "<null>" : this.requestErrorHandler));
        sb.append(',');
        sb.append("requestStackHandlerSelection");
        sb.append('=');
        sb.append(((this.requestStackHandlerSelection == null) ? "<null>" : this.requestStackHandlerSelection));
        sb.append(',');
        sb.append("answerStackHandlerSelection");
        sb.append('=');
        sb.append(((this.answerStackHandlerSelection == null) ? "<null>" : this.answerStackHandlerSelection));
        sb.append(',');
        sb.append("sessionIdFormatType");
        sb.append('=');
        sb.append(((this.sessionIdFormatType == null) ? "<null>" : this.sessionIdFormatType));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("predictiveLoopAvoidanceEnabled");
        sb.append('=');
        sb.append(((this.predictiveLoopAvoidanceEnabled == null) ? "<null>" : this.predictiveLoopAvoidanceEnabled));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("application");
        sb.append('=');
        sb.append(((this.application == null) ? "<null>" : this.application));
        sb.append(',');
        sb.append("reroutePolicy");
        sb.append('=');
        sb.append(((this.reroutePolicy == null) ? "<null>" : this.reroutePolicy));
        sb.append(',');
        sb.append("localEndpointReference");
        sb.append('=');
        sb.append(((this.localEndpointReference == null) ? "<null>" : this.localEndpointReference));
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
        result = ((result * 31) + ((this.sessionIdFormatType == null) ? 0 : this.sessionIdFormatType.hashCode()));
        result = ((result * 31) + ((this.egressRequestPendingTimer == null) ? 0 : this.egressRequestPendingTimer.hashCode()));
        result = ((result * 31) + ((this.requestSendMaxRetry == null) ? 0 : this.requestSendMaxRetry.hashCode()));
        result = ((result * 31) + ((this.ingressRequestPendingTimer == null) ? 0 : this.ingressRequestPendingTimer.hashCode()));
        result = ((result * 31) + ((this.localEndpointReference == null) ? 0 : this.localEndpointReference.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.serviceExecutionEnvironment == null) ? 0 : this.serviceExecutionEnvironment.hashCode()));
        result = ((result * 31) + ((this.application == null) ? 0 : this.application.hashCode()));
        result = ((result * 31) + ((this.answerStackHandlerSelection == null) ? 0 : this.answerStackHandlerSelection.hashCode()));
        result = ((result * 31) + ((this.requestErrorHandler == null) ? 0 : this.requestErrorHandler.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.reroutePolicy == null) ? 0 : this.reroutePolicy.hashCode()));
        result = ((result * 31) + ((this.requestStackHandlerSelection == null) ? 0 : this.requestStackHandlerSelection.hashCode()));
        result = ((result * 31) + ((this.predictiveLoopAvoidanceEnabled == null) ? 0 : this.predictiveLoopAvoidanceEnabled.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Service) == false)
        {
            return false;
        }
        Service rhs = ((Service) other);
        return ((((((((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                             && ((this.sessionIdFormatType == rhs.sessionIdFormatType)
                                 || ((this.sessionIdFormatType != null) && this.sessionIdFormatType.equals(rhs.sessionIdFormatType))))
                            && ((this.egressRequestPendingTimer == rhs.egressRequestPendingTimer)
                                || ((this.egressRequestPendingTimer != null) && this.egressRequestPendingTimer.equals(rhs.egressRequestPendingTimer))))
                           && ((this.requestSendMaxRetry == rhs.requestSendMaxRetry)
                               || ((this.requestSendMaxRetry != null) && this.requestSendMaxRetry.equals(rhs.requestSendMaxRetry))))
                          && ((this.ingressRequestPendingTimer == rhs.ingressRequestPendingTimer)
                              || ((this.ingressRequestPendingTimer != null) && this.ingressRequestPendingTimer.equals(rhs.ingressRequestPendingTimer))))
                         && ((this.localEndpointReference == rhs.localEndpointReference)
                             || ((this.localEndpointReference != null) && this.localEndpointReference.equals(rhs.localEndpointReference))))
                        && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                       && ((this.serviceExecutionEnvironment == rhs.serviceExecutionEnvironment)
                           || ((this.serviceExecutionEnvironment != null) && this.serviceExecutionEnvironment.equals(rhs.serviceExecutionEnvironment))))
                      && ((this.application == rhs.application) || ((this.application != null) && this.application.equals(rhs.application))))
                     && ((this.answerStackHandlerSelection == rhs.answerStackHandlerSelection)
                         || ((this.answerStackHandlerSelection != null) && this.answerStackHandlerSelection.equals(rhs.answerStackHandlerSelection))))
                    && ((this.requestErrorHandler == rhs.requestErrorHandler)
                        || ((this.requestErrorHandler != null) && this.requestErrorHandler.equals(rhs.requestErrorHandler))))
                   && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                  && ((this.reroutePolicy == rhs.reroutePolicy) || ((this.reroutePolicy != null) && this.reroutePolicy.equals(rhs.reroutePolicy))))
                 && ((this.requestStackHandlerSelection == rhs.requestStackHandlerSelection)
                     || ((this.requestStackHandlerSelection != null) && this.requestStackHandlerSelection.equals(rhs.requestStackHandlerSelection))))
                && ((this.predictiveLoopAvoidanceEnabled == rhs.predictiveLoopAvoidanceEnabled)
                    || ((this.predictiveLoopAvoidanceEnabled != null) && this.predictiveLoopAvoidanceEnabled.equals(rhs.predictiveLoopAvoidanceEnabled))));
    }

    public enum AnswerStackHandlerSelection
    {

        LOCAL("local"),
        CLUSTER("cluster");

        private final String value;
        private final static Map<String, Service.AnswerStackHandlerSelection> CONSTANTS = new HashMap<String, Service.AnswerStackHandlerSelection>();

        static
        {
            for (Service.AnswerStackHandlerSelection c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private AnswerStackHandlerSelection(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Service.AnswerStackHandlerSelection fromValue(String value)
        {
            Service.AnswerStackHandlerSelection constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    public enum RequestErrorHandler
    {

        STACK("stack"),
        SERVICE("service");

        private final String value;
        private final static Map<String, Service.RequestErrorHandler> CONSTANTS = new HashMap<String, Service.RequestErrorHandler>();

        static
        {
            for (Service.RequestErrorHandler c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private RequestErrorHandler(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Service.RequestErrorHandler fromValue(String value)
        {
            Service.RequestErrorHandler constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    public enum RequestStackHandlerSelection
    {

        LOCAL("local"),
        CLUSTER("cluster");

        private final String value;
        private final static Map<String, Service.RequestStackHandlerSelection> CONSTANTS = new HashMap<String, Service.RequestStackHandlerSelection>();

        static
        {
            for (Service.RequestStackHandlerSelection c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private RequestStackHandlerSelection(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Service.RequestStackHandlerSelection fromValue(String value)
        {
            Service.RequestStackHandlerSelection constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    public enum ServiceExecutionEnvironment
    {

        LINUX("linux"),
        DICOS("dicos");

        private final String value;
        private final static Map<String, Service.ServiceExecutionEnvironment> CONSTANTS = new HashMap<String, Service.ServiceExecutionEnvironment>();

        static
        {
            for (Service.ServiceExecutionEnvironment c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private ServiceExecutionEnvironment(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Service.ServiceExecutionEnvironment fromValue(String value)
        {
            Service.ServiceExecutionEnvironment constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    public enum SessionIdFormatType
    {

        HLBITS_32("hlbits32"),
        HLBITS_64("hlbits64");

        private final String value;
        private final static Map<String, Service.SessionIdFormatType> CONSTANTS = new HashMap<String, Service.SessionIdFormatType>();

        static
        {
            for (Service.SessionIdFormatType c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private SessionIdFormatType(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static Service.SessionIdFormatType fromValue(String value)
        {
            Service.SessionIdFormatType constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
