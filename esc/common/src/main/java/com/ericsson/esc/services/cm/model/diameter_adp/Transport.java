
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A transport instance is a system created singleton object instance in the
 * diameter configuration and it is used as a container for transport
 * configuration. To have the Own Diameter Node accept connections from or
 * initiate connections towards Peer Diameter Nodes the transport fragment of
 * the managed object model is to be configured accordingly. This is performed
 * by creating one or more Local Endpoints with wanted roles and transport
 * capabilities. Local Endpoints are specified with instances of local-endpoint.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "node", "host-address-resolver", "local-endpoint" })
public class Transport
{

    /**
     * A node instance is used to specify an Own Diameter Node. A Diameter Service
     * deployment, on a certain target system, can expose/represent arbitrary
     * integer of Diameter Nodes (each of them expressed by related node instance).
     * The different AAA Service implementations using the Diameter Service can be
     * connected to relevant Diameter Nodes. Any changes on the node instance are
     * applied immediately on Diameter Service level. Since all the attribute values
     * provides common content for diameter messages used for diameter peer
     * connection setup (CER/CEA messages) and some subsequent messages, changes on
     * related values will have as result the drop of all related diameter peer
     * connections and reestablishment with updated information. The Diameter
     * Service level queued egress request messages will be resent to relevant
     * Diameter Peer Nodes.
     * 
     */
    @JsonProperty("node")
    @JsonPropertyDescription("A node instance is used to specify an Own Diameter Node. A Diameter Service deployment, on a certain target system, can expose/represent arbitrary integer of Diameter Nodes (each of them expressed by related node instance). The different AAA Service implementations using the Diameter Service can be connected to relevant Diameter Nodes. Any changes on the node instance are applied immediately on Diameter Service level. Since all the attribute values provides common content for diameter messages used for diameter peer connection setup (CER/CEA messages) and some subsequent messages, changes on related values will have as result the drop of all related diameter peer connections and reestablishment with updated information. The Diameter Service level queued egress request messages will be resent to relevant Diameter Peer Nodes.")
    private List<Node> node = new ArrayList<Node>();
    /**
     * A host-address-resolver is used to resolve or determine a host-local IP
     * address the Local Endpoints shall use during peer connection establishment.
     * This object shall only be used when the Diameter Service is deployed on a
     * target system where a Diameter node (a POD hosting a Diameter Service
     * instance) might be associated with more than one IP address (see also
     * Kubernetes Cluster Networking). Changes on host-address-resolver level are
     * influencing all established peer connections using the related IP address.
     * That is, affected peer connections are closed then reestablished by need
     * using updated information.
     * 
     */
    @JsonProperty("host-address-resolver")
    @JsonPropertyDescription("A host-address-resolver is used to resolve or determine a host-local IP address the Local Endpoints shall use during peer connection establishment. This object shall only be used when the Diameter Service is deployed on a target system where a Diameter node (a POD hosting a Diameter Service instance) might be associated with more than one IP address (see also Kubernetes Cluster Networking). Changes on host-address-resolver level are influencing all established peer connections using the related IP address. That is, affected peer connections are closed then reestablished by need using updated information.")
    private List<HostAddressResolver> hostAddressResolver = new ArrayList<HostAddressResolver>();
    /**
     * A local-endpoint instance is used to specify a Local Endpoint for the Own
     * Diameter Node. A Local Endpoint can play one of the following roles:
     * Connection Initiation The Local Endpoint is configured to play a transport
     * connection initiation role towards the configured Peer Diameter Node. That
     * is, the Local Endpoint is playing a client role in the peer connection setup
     * flow. When initiating connections towards a Peer Diameter Node the
     * initiate-connection-to-peer attribute of local-endpoint should hold an object
     * reference pointing to that Peer Diameter Node (represented by a static-peer
     * together with related remote-endpoint) towards which the connection
     * initiation and related peer connection establishment is to be performed.
     * Connection Termination The Local Endpoint is configured to play a connection
     * termination role for Peer Diameter Nodes. That is, the related Local Endpoint
     * is playing a server role in the peer connection setup flow. It listens on the
     * configured address and port pairs and accepts incoming transport connection
     * requests initiated by Peer Diameter Nodes. The collection of Peer Diameter
     * Nodes allowed to setup peer connections towards a Local Endpoint of the Own
     * Diameter Node can be constrained by either using the generic Peer Node
     * filtering capabilities provided by the dynamic-peer-acceptor or by using
     * concrete Peer Node specification capabilities provided by the static-peer.
     * The Peer Diameter Node initiated connection acceptance constraints are
     * expressed for a local-endpoint by loading the related
     * terminate-connection-from-peers or terminate-connection-from-accepted-peers
     * attribute with an object reference pointing to those static-peer respectively
     * dynamic-peer-acceptor instances that are used to express the different
     * connection acceptance conditions. A Local Endpoint can either play a
     * connection initiation (client) role or connection termination (server) role,
     * therefore, a Local Endpoint must either have the terminate-connection-from-*
     * reference or the initiate-connection-to-peer reference filled with a valid
     * reference value but never both of them. A Local Endpoint can be assigned with
     * several transport capabilities. A transport capability represents a certain
     * transport protocol implementation. The following transport capabilities
     * (transport protocol implementations) are supported by the Diameter Service:
     * TCP Linux Kernel implementation of the Transmission Control Protocol (TCP).
     * E-SCTP Proprietary user-space implementation of the Stream Control
     * Transmission Protocol (SCTP). A Local Endpoint can be assigned with up to two
     * transport capabilities representing distinct transport protocol
     * implementations. That is, for instance, one of the transport capability can
     * represent a TCP protocol implementation while the other one an SCTP protocol
     * implementation. That transport capability is selected first for peer
     * connection handling which presents the highest configured rank and it is
     * potentially valid for connection establishment (for instance, the configured
     * address is available on the target system). Upon multiple valid transport
     * capabilities with same rank one of them is selected in a random way (for
     * example when specifying one TCP and one SCTP transport capability using same
     * rank). A AAA Service can have assigned any integer of Local Endpoints with
     * different roles and transport capabilities. The Diameter Service runs in a
     * cluster configuration on the target system. That is, it might span on an
     * arbitrary integer of compute resources (nodes, interpreted as POD individuals
     * for ADP). A Local Endpoint can be configured to start in single or multiple
     * instances on cluster level. The integer of instances started for a Local
     * Endpoint can be configured to arbitrary value but the actual instances
     * started will never pass the integer of compute resources the Diameter Service
     * is instantiated on. Changes on local-endpoint are applied immediately on
     * Diameter Service level. Value changes on the majority of attributes will have
     * as result the drop of related peer connections and reestablishment by need
     * with updated information. Diameter Service level queued egress request
     * messages will be resent to realted Peer Diameter Nodes.
     * 
     */
    @JsonProperty("local-endpoint")
    @JsonPropertyDescription("A local-endpoint instance is used to specify a Local Endpoint for the Own Diameter Node. A Local Endpoint can play one of the following roles: Connection Initiation The Local Endpoint is configured to play a transport connection initiation role towards the configured Peer Diameter Node. That is, the Local Endpoint is playing a client role in the peer connection setup flow. When initiating connections towards a Peer Diameter Node the initiate-connection-to-peer attribute of local-endpoint should hold an object reference pointing to that Peer Diameter Node (represented by a static-peer together with related remote-endpoint) towards which the connection initiation and related peer connection establishment is to be performed. Connection Termination The Local Endpoint is configured to play a connection termination role for Peer Diameter Nodes. That is, the related Local Endpoint is playing a server role in the peer connection setup flow. It listens on the configured address and port pairs and accepts incoming transport connection requests initiated by Peer Diameter Nodes. The collection of Peer Diameter Nodes allowed to setup peer connections towards a Local Endpoint of the Own Diameter Node can be constrained by either using the generic Peer Node filtering capabilities provided by the dynamic-peer-acceptor or by using concrete Peer Node specification capabilities provided by the static-peer. The Peer Diameter Node initiated connection acceptance constraints are expressed for a local-endpoint by loading the related terminate-connection-from-peers or terminate-connection-from-accepted-peers attribute with an object reference pointing to those static-peer respectively dynamic-peer-acceptor instances that are used to express the different connection acceptance conditions. A Local Endpoint can either play a connection initiation (client) role or connection termination (server) role, therefore, a Local Endpoint must either have the terminate-connection-from-* reference or the initiate-connection-to-peer reference filled with a valid reference value but never both of them. A Local Endpoint can be assigned with several transport capabilities. A transport capability represents a certain transport protocol implementation. The following transport capabilities (transport protocol implementations) are supported by the Diameter Service: TCP Linux Kernel implementation of the Transmission Control Protocol (TCP). E-SCTP Proprietary user-space implementation of the Stream Control Transmission Protocol (SCTP). A Local Endpoint can be assigned with up to two transport capabilities representing distinct transport protocol implementations. That is, for instance, one of the transport capability can represent a TCP protocol implementation while the other one an SCTP protocol implementation. That transport capability is selected first for peer connection handling which presents the highest configured rank and it is potentially valid for connection establishment (for instance, the configured address is available on the target system). Upon multiple valid transport capabilities with same rank one of them is selected in a random way (for example when specifying one TCP and one SCTP transport capability using same rank). A AAA Service can have assigned any integer of Local Endpoints with different roles and transport capabilities. The Diameter Service runs in a cluster configuration on the target system. That is, it might span on an arbitrary integer of compute resources (nodes, interpreted as POD individuals for ADP). A Local Endpoint can be configured to start in single or multiple instances on cluster level. The integer of instances started for a Local Endpoint can be configured to arbitrary value but the actual instances started will never pass the integer of compute resources the Diameter Service is instantiated on. Changes on local-endpoint are applied immediately on Diameter Service level. Value changes on the majority of attributes will have as result the drop of related peer connections and reestablishment by need with updated information. Diameter Service level queued egress request messages will be resent to realted Peer Diameter Nodes.")
    private List<LocalEndpoint> localEndpoint = new ArrayList<LocalEndpoint>();

    /**
     * A node instance is used to specify an Own Diameter Node. A Diameter Service
     * deployment, on a certain target system, can expose/represent arbitrary
     * integer of Diameter Nodes (each of them expressed by related node instance).
     * The different AAA Service implementations using the Diameter Service can be
     * connected to relevant Diameter Nodes. Any changes on the node instance are
     * applied immediately on Diameter Service level. Since all the attribute values
     * provides common content for diameter messages used for diameter peer
     * connection setup (CER/CEA messages) and some subsequent messages, changes on
     * related values will have as result the drop of all related diameter peer
     * connections and reestablishment with updated information. The Diameter
     * Service level queued egress request messages will be resent to relevant
     * Diameter Peer Nodes.
     * 
     */
    @JsonProperty("node")
    public List<Node> getNode()
    {
        return node;
    }

    /**
     * A node instance is used to specify an Own Diameter Node. A Diameter Service
     * deployment, on a certain target system, can expose/represent arbitrary
     * integer of Diameter Nodes (each of them expressed by related node instance).
     * The different AAA Service implementations using the Diameter Service can be
     * connected to relevant Diameter Nodes. Any changes on the node instance are
     * applied immediately on Diameter Service level. Since all the attribute values
     * provides common content for diameter messages used for diameter peer
     * connection setup (CER/CEA messages) and some subsequent messages, changes on
     * related values will have as result the drop of all related diameter peer
     * connections and reestablishment with updated information. The Diameter
     * Service level queued egress request messages will be resent to relevant
     * Diameter Peer Nodes.
     * 
     */
    @JsonProperty("node")
    public void setNode(List<Node> node)
    {
        this.node = node;
    }

    public Transport withNode(List<Node> node)
    {
        this.node = node;
        return this;
    }

    /**
     * A host-address-resolver is used to resolve or determine a host-local IP
     * address the Local Endpoints shall use during peer connection establishment.
     * This object shall only be used when the Diameter Service is deployed on a
     * target system where a Diameter node (a POD hosting a Diameter Service
     * instance) might be associated with more than one IP address (see also
     * Kubernetes Cluster Networking). Changes on host-address-resolver level are
     * influencing all established peer connections using the related IP address.
     * That is, affected peer connections are closed then reestablished by need
     * using updated information.
     * 
     */
    @JsonProperty("host-address-resolver")
    public List<HostAddressResolver> getHostAddressResolver()
    {
        return hostAddressResolver;
    }

    /**
     * A host-address-resolver is used to resolve or determine a host-local IP
     * address the Local Endpoints shall use during peer connection establishment.
     * This object shall only be used when the Diameter Service is deployed on a
     * target system where a Diameter node (a POD hosting a Diameter Service
     * instance) might be associated with more than one IP address (see also
     * Kubernetes Cluster Networking). Changes on host-address-resolver level are
     * influencing all established peer connections using the related IP address.
     * That is, affected peer connections are closed then reestablished by need
     * using updated information.
     * 
     */
    @JsonProperty("host-address-resolver")
    public void setHostAddressResolver(List<HostAddressResolver> hostAddressResolver)
    {
        this.hostAddressResolver = hostAddressResolver;
    }

    public Transport withHostAddressResolver(List<HostAddressResolver> hostAddressResolver)
    {
        this.hostAddressResolver = hostAddressResolver;
        return this;
    }

    /**
     * A local-endpoint instance is used to specify a Local Endpoint for the Own
     * Diameter Node. A Local Endpoint can play one of the following roles:
     * Connection Initiation The Local Endpoint is configured to play a transport
     * connection initiation role towards the configured Peer Diameter Node. That
     * is, the Local Endpoint is playing a client role in the peer connection setup
     * flow. When initiating connections towards a Peer Diameter Node the
     * initiate-connection-to-peer attribute of local-endpoint should hold an object
     * reference pointing to that Peer Diameter Node (represented by a static-peer
     * together with related remote-endpoint) towards which the connection
     * initiation and related peer connection establishment is to be performed.
     * Connection Termination The Local Endpoint is configured to play a connection
     * termination role for Peer Diameter Nodes. That is, the related Local Endpoint
     * is playing a server role in the peer connection setup flow. It listens on the
     * configured address and port pairs and accepts incoming transport connection
     * requests initiated by Peer Diameter Nodes. The collection of Peer Diameter
     * Nodes allowed to setup peer connections towards a Local Endpoint of the Own
     * Diameter Node can be constrained by either using the generic Peer Node
     * filtering capabilities provided by the dynamic-peer-acceptor or by using
     * concrete Peer Node specification capabilities provided by the static-peer.
     * The Peer Diameter Node initiated connection acceptance constraints are
     * expressed for a local-endpoint by loading the related
     * terminate-connection-from-peers or terminate-connection-from-accepted-peers
     * attribute with an object reference pointing to those static-peer respectively
     * dynamic-peer-acceptor instances that are used to express the different
     * connection acceptance conditions. A Local Endpoint can either play a
     * connection initiation (client) role or connection termination (server) role,
     * therefore, a Local Endpoint must either have the terminate-connection-from-*
     * reference or the initiate-connection-to-peer reference filled with a valid
     * reference value but never both of them. A Local Endpoint can be assigned with
     * several transport capabilities. A transport capability represents a certain
     * transport protocol implementation. The following transport capabilities
     * (transport protocol implementations) are supported by the Diameter Service:
     * TCP Linux Kernel implementation of the Transmission Control Protocol (TCP).
     * E-SCTP Proprietary user-space implementation of the Stream Control
     * Transmission Protocol (SCTP). A Local Endpoint can be assigned with up to two
     * transport capabilities representing distinct transport protocol
     * implementations. That is, for instance, one of the transport capability can
     * represent a TCP protocol implementation while the other one an SCTP protocol
     * implementation. That transport capability is selected first for peer
     * connection handling which presents the highest configured rank and it is
     * potentially valid for connection establishment (for instance, the configured
     * address is available on the target system). Upon multiple valid transport
     * capabilities with same rank one of them is selected in a random way (for
     * example when specifying one TCP and one SCTP transport capability using same
     * rank). A AAA Service can have assigned any integer of Local Endpoints with
     * different roles and transport capabilities. The Diameter Service runs in a
     * cluster configuration on the target system. That is, it might span on an
     * arbitrary integer of compute resources (nodes, interpreted as POD individuals
     * for ADP). A Local Endpoint can be configured to start in single or multiple
     * instances on cluster level. The integer of instances started for a Local
     * Endpoint can be configured to arbitrary value but the actual instances
     * started will never pass the integer of compute resources the Diameter Service
     * is instantiated on. Changes on local-endpoint are applied immediately on
     * Diameter Service level. Value changes on the majority of attributes will have
     * as result the drop of related peer connections and reestablishment by need
     * with updated information. Diameter Service level queued egress request
     * messages will be resent to realted Peer Diameter Nodes.
     * 
     */
    @JsonProperty("local-endpoint")
    public List<LocalEndpoint> getLocalEndpoint()
    {
        return localEndpoint;
    }

    /**
     * A local-endpoint instance is used to specify a Local Endpoint for the Own
     * Diameter Node. A Local Endpoint can play one of the following roles:
     * Connection Initiation The Local Endpoint is configured to play a transport
     * connection initiation role towards the configured Peer Diameter Node. That
     * is, the Local Endpoint is playing a client role in the peer connection setup
     * flow. When initiating connections towards a Peer Diameter Node the
     * initiate-connection-to-peer attribute of local-endpoint should hold an object
     * reference pointing to that Peer Diameter Node (represented by a static-peer
     * together with related remote-endpoint) towards which the connection
     * initiation and related peer connection establishment is to be performed.
     * Connection Termination The Local Endpoint is configured to play a connection
     * termination role for Peer Diameter Nodes. That is, the related Local Endpoint
     * is playing a server role in the peer connection setup flow. It listens on the
     * configured address and port pairs and accepts incoming transport connection
     * requests initiated by Peer Diameter Nodes. The collection of Peer Diameter
     * Nodes allowed to setup peer connections towards a Local Endpoint of the Own
     * Diameter Node can be constrained by either using the generic Peer Node
     * filtering capabilities provided by the dynamic-peer-acceptor or by using
     * concrete Peer Node specification capabilities provided by the static-peer.
     * The Peer Diameter Node initiated connection acceptance constraints are
     * expressed for a local-endpoint by loading the related
     * terminate-connection-from-peers or terminate-connection-from-accepted-peers
     * attribute with an object reference pointing to those static-peer respectively
     * dynamic-peer-acceptor instances that are used to express the different
     * connection acceptance conditions. A Local Endpoint can either play a
     * connection initiation (client) role or connection termination (server) role,
     * therefore, a Local Endpoint must either have the terminate-connection-from-*
     * reference or the initiate-connection-to-peer reference filled with a valid
     * reference value but never both of them. A Local Endpoint can be assigned with
     * several transport capabilities. A transport capability represents a certain
     * transport protocol implementation. The following transport capabilities
     * (transport protocol implementations) are supported by the Diameter Service:
     * TCP Linux Kernel implementation of the Transmission Control Protocol (TCP).
     * E-SCTP Proprietary user-space implementation of the Stream Control
     * Transmission Protocol (SCTP). A Local Endpoint can be assigned with up to two
     * transport capabilities representing distinct transport protocol
     * implementations. That is, for instance, one of the transport capability can
     * represent a TCP protocol implementation while the other one an SCTP protocol
     * implementation. That transport capability is selected first for peer
     * connection handling which presents the highest configured rank and it is
     * potentially valid for connection establishment (for instance, the configured
     * address is available on the target system). Upon multiple valid transport
     * capabilities with same rank one of them is selected in a random way (for
     * example when specifying one TCP and one SCTP transport capability using same
     * rank). A AAA Service can have assigned any integer of Local Endpoints with
     * different roles and transport capabilities. The Diameter Service runs in a
     * cluster configuration on the target system. That is, it might span on an
     * arbitrary integer of compute resources (nodes, interpreted as POD individuals
     * for ADP). A Local Endpoint can be configured to start in single or multiple
     * instances on cluster level. The integer of instances started for a Local
     * Endpoint can be configured to arbitrary value but the actual instances
     * started will never pass the integer of compute resources the Diameter Service
     * is instantiated on. Changes on local-endpoint are applied immediately on
     * Diameter Service level. Value changes on the majority of attributes will have
     * as result the drop of related peer connections and reestablishment by need
     * with updated information. Diameter Service level queued egress request
     * messages will be resent to realted Peer Diameter Nodes.
     * 
     */
    @JsonProperty("local-endpoint")
    public void setLocalEndpoint(List<LocalEndpoint> localEndpoint)
    {
        this.localEndpoint = localEndpoint;
    }

    public Transport withLocalEndpoint(List<LocalEndpoint> localEndpoint)
    {
        this.localEndpoint = localEndpoint;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Transport.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("node");
        sb.append('=');
        sb.append(((this.node == null) ? "<null>" : this.node));
        sb.append(',');
        sb.append("hostAddressResolver");
        sb.append('=');
        sb.append(((this.hostAddressResolver == null) ? "<null>" : this.hostAddressResolver));
        sb.append(',');
        sb.append("localEndpoint");
        sb.append('=');
        sb.append(((this.localEndpoint == null) ? "<null>" : this.localEndpoint));
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
        result = ((result * 31) + ((this.node == null) ? 0 : this.node.hashCode()));
        result = ((result * 31) + ((this.hostAddressResolver == null) ? 0 : this.hostAddressResolver.hashCode()));
        result = ((result * 31) + ((this.localEndpoint == null) ? 0 : this.localEndpoint.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Transport) == false)
        {
            return false;
        }
        Transport rhs = ((Transport) other);
        return ((((this.node == rhs.node) || ((this.node != null) && this.node.equals(rhs.node)))
                 && ((this.hostAddressResolver == rhs.hostAddressResolver)
                     || ((this.hostAddressResolver != null) && this.hostAddressResolver.equals(rhs.hostAddressResolver))))
                && ((this.localEndpoint == rhs.localEndpoint) || ((this.localEndpoint != null) && this.localEndpoint.equals(rhs.localEndpoint))));
    }

}
