
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id",
                     "peer-origin-realm",
                     "peer-origin-host",
                     "peer-host-ip-address",
                     "excluded-host",
                     "restrict-connections",
                     "max-peer-connection-nr",
                     "max-connection-nr",
                     "required-connection-nr",
                     "enabled",
                     "congestion-handling-policy",
                     "user-label" })
public class DynamicPeerAcceptor
{

    /**
     * Used to specify the key of the dynamic-peer-acceptor instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the dynamic-peer-acceptor instance.")
    private String id;
    /**
     * Used to specify the Origin Realm (see
     * https://tools.ietf.org/html/rfc6733#section-6.4) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Realm used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Origin Realm is a
     * Diameter Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1)
     * data type as defined by the Diameter Base Protocol, see Diameter Base
     * Protocol (RFC 6733) IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-realm")
    @JsonPropertyDescription("Used to specify the Origin Realm (see https://tools.ietf.org/html/rfc6733#section-6.4) validation pattern of the Peer Diameter Nodes allowed to initiate connections towards the Own Diameter Node. The provided value shall be either a concrete or a pattern based representation (expressed using Perl Compatible Regular Expressions, see https://www.pcre.org/) of the Origin Realm used by Peer Diameter Nodes allowed to connect towards the Own Diameter Node. The Origin Realm is a Diameter Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1) data type as defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733) IETF: STANDARD.")
    private List<String> peerOriginRealm = new ArrayList<String>();
    /**
     * Used to specify the Origin Host (see
     * https://tools.ietf.org/html/rfc6733#section-6.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Host used by Peer Diameter Nodes allowed
     * to connect towards the Own Diameter Node. The Origin Host is a Diameter
     * Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1) data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-host")
    @JsonPropertyDescription("Used to specify the Origin Host (see https://tools.ietf.org/html/rfc6733#section-6.5) validation pattern of the Peer Diameter Nodes allowed to initiate connections towards the Own Diameter Node. The provided value shall be either a concrete or a pattern based representation (expressed using Perl Compatible Regular Expressions, see https://www.pcre.org/) of the Origin Host used by Peer Diameter Nodes allowed to connect towards the Own Diameter Node. The Origin Host is a Diameter Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1) data type as defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733) IETF: STANDARD.")
    private List<String> peerOriginHost = new ArrayList<String>();
    /**
     * Used to specify the Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-5.3.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Host IP Address used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-4.3.1) is an Address data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-host-ip-address")
    @JsonPropertyDescription("Used to specify the Host IP Address (see https://tools.ietf.org/html/rfc6733#section-5.3.5) validation pattern of the Peer Diameter Nodes allowed to initiate connections towards the Own Diameter Node. The provided value shall be either a concrete or a pattern based representation (expressed using Perl Compatible Regular Expressions, see https://www.pcre.org/) of the Host IP Address used by Peer Diameter Nodes allowed to connect towards the Own Diameter Node. The Host IP Address (see https://tools.ietf.org/html/rfc6733#section-4.3.1) is an Address data type as defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733) IETF: STANDARD.")
    private List<String> peerHostIpAddress = new ArrayList<String>();
    /**
     * Defines an Origin Host validation pattern used to filter out Peer Diameter
     * Nodes not allowed to initiate connections towards the Own Diameter Node.
     * Serves as a black list expression over the white list defined by
     * peer-origin-host.
     * 
     */
    @JsonProperty("excluded-host")
    @JsonPropertyDescription("Defines an Origin Host validation pattern used to filter out Peer Diameter Nodes not allowed to initiate connections towards the Own Diameter Node. Serves as a black list expression over the white list defined by peer-origin-host.")
    private List<String> excludedHost = new ArrayList<String>();
    /**
     * Used to disallow the establishment of more than one diameter peer connection
     * from the same Peer Diameter Node. The Diameter Base Protocol specifies the
     * use of single active connection between Diameter Peer Nodes (see, Diameter
     * Base Protocol (RFC 6733) IETF: STANDARD). However, Diameter Nodes can be
     * implemented using a cluster of compute resources in which case the use of
     * single peer connection between such Diameter Nodes might be a bottleneck in
     * handling required traffic throughput. Such Diameter Node implementations
     * provides settings through which multiple diameter peer connections towards
     * same Diameter Peer can be established. diameter provides support for such a
     * functionality as well. Update Apply: Immediate. Update Effect: All
     * established diameter peer connections towards the peers matching the
     * dynamic-peer-acceptor expressed constraints are dropped and reestablished by
     * need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    @JsonPropertyDescription("Used to disallow the establishment of more than one diameter peer connection from the same Peer Diameter Node. The Diameter Base Protocol specifies the use of single active connection between Diameter Peer Nodes (see, Diameter Base Protocol (RFC 6733) IETF: STANDARD). However, Diameter Nodes can be implemented using a cluster of compute resources in which case the use of single peer connection between such Diameter Nodes might be a bottleneck in handling required traffic throughput. Such Diameter Node implementations provides settings through which multiple diameter peer connections towards same Diameter Peer can be established. diameter provides support for such a functionality as well. Update Apply: Immediate. Update Effect: All established diameter peer connections towards the peers matching the dynamic-peer-acceptor expressed constraints are dropped and reestablished by need in accordance with updated information.")
    private Boolean restrictConnections = true;
    /**
     * Sets the maximum integer of connections to be accepted from a single Diameter
     * Peer Node. That is, from a peer matching the constraints expressed by the
     * actual dynamic-peer-acceptor. In case the max-peer-connection-nr value is set
     * to 0 the related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connections will be allowed from the diameter peers matching the filtering
     * conditions expressed by the dynamic-peer-acceptor. This attribute is
     * considered by the Diameter Service if the restrict-connections attribute of
     * same dynamic-peer-acceptor is set to false. Update Apply: Immediate Update
     * Effect: If the updated value of max-peer-connection-nr attribute is less than
     * the previously set one, existing connections will be dropped to conform to
     * the newly configured limit.
     * 
     */
    @JsonProperty("max-peer-connection-nr")
    @JsonPropertyDescription("Sets the maximum integer of connections to be accepted from a single Diameter Peer Node. That is, from a peer matching the constraints expressed by the actual dynamic-peer-acceptor. In case the max-peer-connection-nr value is set to 0 the related dynamic-peer-acceptor will behave as a blocker. That is, no connections will be allowed from the diameter peers matching the filtering conditions expressed by the dynamic-peer-acceptor. This attribute is considered by the Diameter Service if the restrict-connections attribute of same dynamic-peer-acceptor is set to false. Update Apply: Immediate Update Effect: If the updated value of max-peer-connection-nr attribute is less than the previously set one, existing connections will be dropped to conform to the newly configured limit.")
    private Long maxPeerConnectionNr = 4294967295L;
    /**
     * Used to configure the maximum integer of connections to be accepted from Peer
     * Diameter Nodes matching the constraints expressed by the current
     * dynamic-peer-acceptor. This is a value to be considered for all Peer Diameter
     * Nodes accepted by the current dynamic-peer-acceptor and not a value
     * representing individual connections per Peer Diameter Node (see also
     * max-peer-connection-nr attribute for connection integer constraints per Peer
     * Diameter Node). In case the max-connection-nr attribute value is set to 0 the
     * related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connection establishment will be allowed from any of Peer Diameter Nodes
     * matching the constraints expressed hereby. This attribute is considered by
     * the Diameter Service if the restrict-connections attribute of same
     * dynamic-peer-acceptor is set to false. Update Apply: Immediate Update Effect:
     * If the updated value of max-connection-nr attribute is less than the
     * previously set one, existing connections will be dropped to conform to the
     * newly configured limit.
     * 
     */
    @JsonProperty("max-connection-nr")
    @JsonPropertyDescription("Used to configure the maximum integer of connections to be accepted from Peer Diameter Nodes matching the constraints expressed by the current dynamic-peer-acceptor. This is a value to be considered for all Peer Diameter Nodes accepted by the current dynamic-peer-acceptor and not a value representing individual connections per Peer Diameter Node (see also max-peer-connection-nr attribute for connection integer constraints per Peer Diameter Node). In case the max-connection-nr attribute value is set to 0 the related dynamic-peer-acceptor will behave as a blocker. That is, no connection establishment will be allowed from any of Peer Diameter Nodes matching the constraints expressed hereby. This attribute is considered by the Diameter Service if the restrict-connections attribute of same dynamic-peer-acceptor is set to false. Update Apply: Immediate Update Effect: If the updated value of max-connection-nr attribute is less than the previously set one, existing connections will be dropped to conform to the newly configured limit.")
    private Long maxConnectionNr = 4294967295L;
    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * a Diameter Peer. When the configured threshold is crossed, but there is at
     * least one active connection available towards the Peer, the following alarms
     * is raised: ADP Diameter, Peer Connection Number Below Required Level Update
     * Apply: Immediate Update Effect: When set, the configured threshold will be
     * used by the stack during alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    @JsonPropertyDescription("Used to indicate a threshold for the integer of redundant connections towards a Diameter Peer. When the configured threshold is crossed, but there is at least one active connection available towards the Peer, the following alarms is raised: ADP Diameter, Peer Connection Number Below Required Level Update Apply: Immediate Update Effect: When set, the configured threshold will be used by the stack during alarm handling.")
    private Long requiredConnectionNr = 1L;
    /**
     * Used to enable or disable establishment of peer connections initiated by
     * Diameter Peer Node(s) matching the different identity validation patterns as
     * expressed by relevant dynamic-peer-acceptor instance. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the dynamic-peer-acceptor expressed constraints
     * are disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Used to enable or disable establishment of peer connections initiated by Diameter Peer Node(s) matching the different identity validation patterns as expressed by relevant dynamic-peer-acceptor instance. When disabled, the following alarm is raised: ADP Diameter, Managed Object Disabled Update Apply: Immediate. Update Effect: All established diameter peer connections towards the peers matching the dynamic-peer-acceptor expressed constraints are disconnected upon setting to value false.")
    private Boolean enabled = true;
    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    @JsonPropertyDescription("Reference to a congestion-handling policy")
    private String congestionHandlingPolicy;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the dynamic-peer-acceptor instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the dynamic-peer-acceptor instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public DynamicPeerAcceptor withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to specify the Origin Realm (see
     * https://tools.ietf.org/html/rfc6733#section-6.4) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Realm used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Origin Realm is a
     * Diameter Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1)
     * data type as defined by the Diameter Base Protocol, see Diameter Base
     * Protocol (RFC 6733) IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-realm")
    public List<String> getPeerOriginRealm()
    {
        return peerOriginRealm;
    }

    /**
     * Used to specify the Origin Realm (see
     * https://tools.ietf.org/html/rfc6733#section-6.4) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Realm used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Origin Realm is a
     * Diameter Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1)
     * data type as defined by the Diameter Base Protocol, see Diameter Base
     * Protocol (RFC 6733) IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-realm")
    public void setPeerOriginRealm(List<String> peerOriginRealm)
    {
        this.peerOriginRealm = peerOriginRealm;
    }

    public DynamicPeerAcceptor withPeerOriginRealm(List<String> peerOriginRealm)
    {
        this.peerOriginRealm = peerOriginRealm;
        return this;
    }

    /**
     * Used to specify the Origin Host (see
     * https://tools.ietf.org/html/rfc6733#section-6.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Host used by Peer Diameter Nodes allowed
     * to connect towards the Own Diameter Node. The Origin Host is a Diameter
     * Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1) data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-host")
    public List<String> getPeerOriginHost()
    {
        return peerOriginHost;
    }

    /**
     * Used to specify the Origin Host (see
     * https://tools.ietf.org/html/rfc6733#section-6.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Origin Host used by Peer Diameter Nodes allowed
     * to connect towards the Own Diameter Node. The Origin Host is a Diameter
     * Identity (see https://tools.ietf.org/html/rfc6733#section-4.3.1) data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-origin-host")
    public void setPeerOriginHost(List<String> peerOriginHost)
    {
        this.peerOriginHost = peerOriginHost;
    }

    public DynamicPeerAcceptor withPeerOriginHost(List<String> peerOriginHost)
    {
        this.peerOriginHost = peerOriginHost;
        return this;
    }

    /**
     * Used to specify the Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-5.3.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Host IP Address used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-4.3.1) is an Address data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-host-ip-address")
    public List<String> getPeerHostIpAddress()
    {
        return peerHostIpAddress;
    }

    /**
     * Used to specify the Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-5.3.5) validation pattern of the
     * Peer Diameter Nodes allowed to initiate connections towards the Own Diameter
     * Node. The provided value shall be either a concrete or a pattern based
     * representation (expressed using Perl Compatible Regular Expressions, see
     * https://www.pcre.org/) of the Host IP Address used by Peer Diameter Nodes
     * allowed to connect towards the Own Diameter Node. The Host IP Address (see
     * https://tools.ietf.org/html/rfc6733#section-4.3.1) is an Address data type as
     * defined by the Diameter Base Protocol, see Diameter Base Protocol (RFC 6733)
     * IETF: STANDARD.
     * 
     */
    @JsonProperty("peer-host-ip-address")
    public void setPeerHostIpAddress(List<String> peerHostIpAddress)
    {
        this.peerHostIpAddress = peerHostIpAddress;
    }

    public DynamicPeerAcceptor withPeerHostIpAddress(List<String> peerHostIpAddress)
    {
        this.peerHostIpAddress = peerHostIpAddress;
        return this;
    }

    /**
     * Defines an Origin Host validation pattern used to filter out Peer Diameter
     * Nodes not allowed to initiate connections towards the Own Diameter Node.
     * Serves as a black list expression over the white list defined by
     * peer-origin-host.
     * 
     */
    @JsonProperty("excluded-host")
    public List<String> getExcludedHost()
    {
        return excludedHost;
    }

    /**
     * Defines an Origin Host validation pattern used to filter out Peer Diameter
     * Nodes not allowed to initiate connections towards the Own Diameter Node.
     * Serves as a black list expression over the white list defined by
     * peer-origin-host.
     * 
     */
    @JsonProperty("excluded-host")
    public void setExcludedHost(List<String> excludedHost)
    {
        this.excludedHost = excludedHost;
    }

    public DynamicPeerAcceptor withExcludedHost(List<String> excludedHost)
    {
        this.excludedHost = excludedHost;
        return this;
    }

    /**
     * Used to disallow the establishment of more than one diameter peer connection
     * from the same Peer Diameter Node. The Diameter Base Protocol specifies the
     * use of single active connection between Diameter Peer Nodes (see, Diameter
     * Base Protocol (RFC 6733) IETF: STANDARD). However, Diameter Nodes can be
     * implemented using a cluster of compute resources in which case the use of
     * single peer connection between such Diameter Nodes might be a bottleneck in
     * handling required traffic throughput. Such Diameter Node implementations
     * provides settings through which multiple diameter peer connections towards
     * same Diameter Peer can be established. diameter provides support for such a
     * functionality as well. Update Apply: Immediate. Update Effect: All
     * established diameter peer connections towards the peers matching the
     * dynamic-peer-acceptor expressed constraints are dropped and reestablished by
     * need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    public Boolean getRestrictConnections()
    {
        return restrictConnections;
    }

    /**
     * Used to disallow the establishment of more than one diameter peer connection
     * from the same Peer Diameter Node. The Diameter Base Protocol specifies the
     * use of single active connection between Diameter Peer Nodes (see, Diameter
     * Base Protocol (RFC 6733) IETF: STANDARD). However, Diameter Nodes can be
     * implemented using a cluster of compute resources in which case the use of
     * single peer connection between such Diameter Nodes might be a bottleneck in
     * handling required traffic throughput. Such Diameter Node implementations
     * provides settings through which multiple diameter peer connections towards
     * same Diameter Peer can be established. diameter provides support for such a
     * functionality as well. Update Apply: Immediate. Update Effect: All
     * established diameter peer connections towards the peers matching the
     * dynamic-peer-acceptor expressed constraints are dropped and reestablished by
     * need in accordance with updated information.
     * 
     */
    @JsonProperty("restrict-connections")
    public void setRestrictConnections(Boolean restrictConnections)
    {
        this.restrictConnections = restrictConnections;
    }

    public DynamicPeerAcceptor withRestrictConnections(Boolean restrictConnections)
    {
        this.restrictConnections = restrictConnections;
        return this;
    }

    /**
     * Sets the maximum integer of connections to be accepted from a single Diameter
     * Peer Node. That is, from a peer matching the constraints expressed by the
     * actual dynamic-peer-acceptor. In case the max-peer-connection-nr value is set
     * to 0 the related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connections will be allowed from the diameter peers matching the filtering
     * conditions expressed by the dynamic-peer-acceptor. This attribute is
     * considered by the Diameter Service if the restrict-connections attribute of
     * same dynamic-peer-acceptor is set to false. Update Apply: Immediate Update
     * Effect: If the updated value of max-peer-connection-nr attribute is less than
     * the previously set one, existing connections will be dropped to conform to
     * the newly configured limit.
     * 
     */
    @JsonProperty("max-peer-connection-nr")
    public Long getMaxPeerConnectionNr()
    {
        return maxPeerConnectionNr;
    }

    /**
     * Sets the maximum integer of connections to be accepted from a single Diameter
     * Peer Node. That is, from a peer matching the constraints expressed by the
     * actual dynamic-peer-acceptor. In case the max-peer-connection-nr value is set
     * to 0 the related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connections will be allowed from the diameter peers matching the filtering
     * conditions expressed by the dynamic-peer-acceptor. This attribute is
     * considered by the Diameter Service if the restrict-connections attribute of
     * same dynamic-peer-acceptor is set to false. Update Apply: Immediate Update
     * Effect: If the updated value of max-peer-connection-nr attribute is less than
     * the previously set one, existing connections will be dropped to conform to
     * the newly configured limit.
     * 
     */
    @JsonProperty("max-peer-connection-nr")
    public void setMaxPeerConnectionNr(Long maxPeerConnectionNr)
    {
        this.maxPeerConnectionNr = maxPeerConnectionNr;
    }

    public DynamicPeerAcceptor withMaxPeerConnectionNr(Long maxPeerConnectionNr)
    {
        this.maxPeerConnectionNr = maxPeerConnectionNr;
        return this;
    }

    /**
     * Used to configure the maximum integer of connections to be accepted from Peer
     * Diameter Nodes matching the constraints expressed by the current
     * dynamic-peer-acceptor. This is a value to be considered for all Peer Diameter
     * Nodes accepted by the current dynamic-peer-acceptor and not a value
     * representing individual connections per Peer Diameter Node (see also
     * max-peer-connection-nr attribute for connection integer constraints per Peer
     * Diameter Node). In case the max-connection-nr attribute value is set to 0 the
     * related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connection establishment will be allowed from any of Peer Diameter Nodes
     * matching the constraints expressed hereby. This attribute is considered by
     * the Diameter Service if the restrict-connections attribute of same
     * dynamic-peer-acceptor is set to false. Update Apply: Immediate Update Effect:
     * If the updated value of max-connection-nr attribute is less than the
     * previously set one, existing connections will be dropped to conform to the
     * newly configured limit.
     * 
     */
    @JsonProperty("max-connection-nr")
    public Long getMaxConnectionNr()
    {
        return maxConnectionNr;
    }

    /**
     * Used to configure the maximum integer of connections to be accepted from Peer
     * Diameter Nodes matching the constraints expressed by the current
     * dynamic-peer-acceptor. This is a value to be considered for all Peer Diameter
     * Nodes accepted by the current dynamic-peer-acceptor and not a value
     * representing individual connections per Peer Diameter Node (see also
     * max-peer-connection-nr attribute for connection integer constraints per Peer
     * Diameter Node). In case the max-connection-nr attribute value is set to 0 the
     * related dynamic-peer-acceptor will behave as a blocker. That is, no
     * connection establishment will be allowed from any of Peer Diameter Nodes
     * matching the constraints expressed hereby. This attribute is considered by
     * the Diameter Service if the restrict-connections attribute of same
     * dynamic-peer-acceptor is set to false. Update Apply: Immediate Update Effect:
     * If the updated value of max-connection-nr attribute is less than the
     * previously set one, existing connections will be dropped to conform to the
     * newly configured limit.
     * 
     */
    @JsonProperty("max-connection-nr")
    public void setMaxConnectionNr(Long maxConnectionNr)
    {
        this.maxConnectionNr = maxConnectionNr;
    }

    public DynamicPeerAcceptor withMaxConnectionNr(Long maxConnectionNr)
    {
        this.maxConnectionNr = maxConnectionNr;
        return this;
    }

    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * a Diameter Peer. When the configured threshold is crossed, but there is at
     * least one active connection available towards the Peer, the following alarms
     * is raised: ADP Diameter, Peer Connection Number Below Required Level Update
     * Apply: Immediate Update Effect: When set, the configured threshold will be
     * used by the stack during alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    public Long getRequiredConnectionNr()
    {
        return requiredConnectionNr;
    }

    /**
     * Used to indicate a threshold for the integer of redundant connections towards
     * a Diameter Peer. When the configured threshold is crossed, but there is at
     * least one active connection available towards the Peer, the following alarms
     * is raised: ADP Diameter, Peer Connection Number Below Required Level Update
     * Apply: Immediate Update Effect: When set, the configured threshold will be
     * used by the stack during alarm handling.
     * 
     */
    @JsonProperty("required-connection-nr")
    public void setRequiredConnectionNr(Long requiredConnectionNr)
    {
        this.requiredConnectionNr = requiredConnectionNr;
    }

    public DynamicPeerAcceptor withRequiredConnectionNr(Long requiredConnectionNr)
    {
        this.requiredConnectionNr = requiredConnectionNr;
        return this;
    }

    /**
     * Used to enable or disable establishment of peer connections initiated by
     * Diameter Peer Node(s) matching the different identity validation patterns as
     * expressed by relevant dynamic-peer-acceptor instance. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the dynamic-peer-acceptor expressed constraints
     * are disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Used to enable or disable establishment of peer connections initiated by
     * Diameter Peer Node(s) matching the different identity validation patterns as
     * expressed by relevant dynamic-peer-acceptor instance. When disabled, the
     * following alarm is raised: ADP Diameter, Managed Object Disabled Update
     * Apply: Immediate. Update Effect: All established diameter peer connections
     * towards the peers matching the dynamic-peer-acceptor expressed constraints
     * are disconnected upon setting to value false.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public DynamicPeerAcceptor withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public String getCongestionHandlingPolicy()
    {
        return congestionHandlingPolicy;
    }

    /**
     * Reference to a congestion-handling policy
     * 
     */
    @JsonProperty("congestion-handling-policy")
    public void setCongestionHandlingPolicy(String congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
    }

    public DynamicPeerAcceptor withCongestionHandlingPolicy(String congestionHandlingPolicy)
    {
        this.congestionHandlingPolicy = congestionHandlingPolicy;
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

    public DynamicPeerAcceptor withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DynamicPeerAcceptor.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("peerOriginRealm");
        sb.append('=');
        sb.append(((this.peerOriginRealm == null) ? "<null>" : this.peerOriginRealm));
        sb.append(',');
        sb.append("peerOriginHost");
        sb.append('=');
        sb.append(((this.peerOriginHost == null) ? "<null>" : this.peerOriginHost));
        sb.append(',');
        sb.append("peerHostIpAddress");
        sb.append('=');
        sb.append(((this.peerHostIpAddress == null) ? "<null>" : this.peerHostIpAddress));
        sb.append(',');
        sb.append("excludedHost");
        sb.append('=');
        sb.append(((this.excludedHost == null) ? "<null>" : this.excludedHost));
        sb.append(',');
        sb.append("restrictConnections");
        sb.append('=');
        sb.append(((this.restrictConnections == null) ? "<null>" : this.restrictConnections));
        sb.append(',');
        sb.append("maxPeerConnectionNr");
        sb.append('=');
        sb.append(((this.maxPeerConnectionNr == null) ? "<null>" : this.maxPeerConnectionNr));
        sb.append(',');
        sb.append("maxConnectionNr");
        sb.append('=');
        sb.append(((this.maxConnectionNr == null) ? "<null>" : this.maxConnectionNr));
        sb.append(',');
        sb.append("requiredConnectionNr");
        sb.append('=');
        sb.append(((this.requiredConnectionNr == null) ? "<null>" : this.requiredConnectionNr));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("congestionHandlingPolicy");
        sb.append('=');
        sb.append(((this.congestionHandlingPolicy == null) ? "<null>" : this.congestionHandlingPolicy));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.maxConnectionNr == null) ? 0 : this.maxConnectionNr.hashCode()));
        result = ((result * 31) + ((this.peerHostIpAddress == null) ? 0 : this.peerHostIpAddress.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.peerOriginRealm == null) ? 0 : this.peerOriginRealm.hashCode()));
        result = ((result * 31) + ((this.requiredConnectionNr == null) ? 0 : this.requiredConnectionNr.hashCode()));
        result = ((result * 31) + ((this.peerOriginHost == null) ? 0 : this.peerOriginHost.hashCode()));
        result = ((result * 31) + ((this.maxPeerConnectionNr == null) ? 0 : this.maxPeerConnectionNr.hashCode()));
        result = ((result * 31) + ((this.excludedHost == null) ? 0 : this.excludedHost.hashCode()));
        result = ((result * 31) + ((this.restrictConnections == null) ? 0 : this.restrictConnections.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.congestionHandlingPolicy == null) ? 0 : this.congestionHandlingPolicy.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DynamicPeerAcceptor) == false)
        {
            return false;
        }
        DynamicPeerAcceptor rhs = ((DynamicPeerAcceptor) other);
        return (((((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                          && ((this.maxConnectionNr == rhs.maxConnectionNr)
                              || ((this.maxConnectionNr != null) && this.maxConnectionNr.equals(rhs.maxConnectionNr))))
                         && ((this.peerHostIpAddress == rhs.peerHostIpAddress)
                             || ((this.peerHostIpAddress != null) && this.peerHostIpAddress.equals(rhs.peerHostIpAddress))))
                        && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                       && ((this.peerOriginRealm == rhs.peerOriginRealm)
                           || ((this.peerOriginRealm != null) && this.peerOriginRealm.equals(rhs.peerOriginRealm))))
                      && ((this.requiredConnectionNr == rhs.requiredConnectionNr)
                          || ((this.requiredConnectionNr != null) && this.requiredConnectionNr.equals(rhs.requiredConnectionNr))))
                     && ((this.peerOriginHost == rhs.peerOriginHost) || ((this.peerOriginHost != null) && this.peerOriginHost.equals(rhs.peerOriginHost))))
                    && ((this.maxPeerConnectionNr == rhs.maxPeerConnectionNr)
                        || ((this.maxPeerConnectionNr != null) && this.maxPeerConnectionNr.equals(rhs.maxPeerConnectionNr))))
                   && ((this.excludedHost == rhs.excludedHost) || ((this.excludedHost != null) && this.excludedHost.equals(rhs.excludedHost))))
                  && ((this.restrictConnections == rhs.restrictConnections)
                      || ((this.restrictConnections != null) && this.restrictConnections.equals(rhs.restrictConnections))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.congestionHandlingPolicy == rhs.congestionHandlingPolicy)
                    || ((this.congestionHandlingPolicy != null) && this.congestionHandlingPolicy.equals(rhs.congestionHandlingPolicy))));
    }

}
