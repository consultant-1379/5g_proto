
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Diameter instance represents a singleton entry point in the configuration
 * model of the Diameter Service.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user-label",
                     "service",
                     "applications",
                     "vendor-specific-application-id",
                     "dictionary",
                     "peer-table",
                     "transport",
                     "peer-selector",
                     "routing-table",
                     "policies" })
public class EricssonDiameterAdpDiameter
{

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;
    /**
     * A service instance is used to describe the properties of AAA Service
     * implemented by a Diameter Service User. The settings on service instance
     * level are influencing the interaction mechanisms performed between the
     * Diameter Service and related AAA Service. (Required)
     * 
     */
    @JsonProperty("service")
    @JsonPropertyDescription("A service instance is used to describe the properties of AAA Service implemented by a Diameter Service User. The settings on service instance level are influencing the interaction mechanisms performed between the Diameter Service and related AAA Service.")
    private List<Service> service = new ArrayList<Service>();
    /**
     * The instances of applications are used to define
     * https://tools.ietf.org/html/rfc6733#section-1.3.4 Diameter Applications as
     * defined by the Diameter Base Protocol (https://tools.ietf.org/html/rfc6733. A
     * AAA Service can implement the client, server or agent side of one or several
     * Diameter Applications. The behavior of implemented Diameter Applications is
     * defined through related Diameter Application Specifications released by
     * different standardization bodies (for example: 3GPP, IETF, ETSI, and so on)
     * or vendors (for example: Ericsson).
     * 
     */
    @JsonProperty("applications")
    @JsonPropertyDescription("The instances of applications are used to define https://tools.ietf.org/html/rfc6733#section-1.3.4 Diameter Applications as defined by the Diameter Base Protocol (https://tools.ietf.org/html/rfc6733. A AAA Service can implement the client, server or agent side of one or several Diameter Applications. The behavior of implemented Diameter Applications is defined through related Diameter Application Specifications released by different standardization bodies (for example: 3GPP, IETF, ETSI, and so on) or vendors (for example: Ericsson).")
    private List<Application> applications = new ArrayList<Application>();
    /**
     * A vendor-specific-application-id instance is used to provide information
     * about a vendor specific Diameter Application. The information provided in a
     * vendor-specific-application-id instance is used to construct a
     * Vendor-Specific-Application-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.11) which is of type grouped.
     * Each of a vendor-specific-application-id instance represents a related of the
     * Vendor-Specific-Application-Id AVP. The handling rules defined for
     * Vendor-Specific-Application-Id AVP applies for the related
     * vendor-specific-application-id instance attributes as well.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    @JsonPropertyDescription("A vendor-specific-application-id instance is used to provide information about a vendor specific Diameter Application. The information provided in a vendor-specific-application-id instance is used to construct a Vendor-Specific-Application-Id AVP (https://tools.ietf.org/html/rfc6733#section-6.11) which is of type grouped. Each of a vendor-specific-application-id instance represents a related of the Vendor-Specific-Application-Id AVP. The handling rules defined for Vendor-Specific-Application-Id AVP applies for the related vendor-specific-application-id instance attributes as well.")
    private List<VendorSpecificApplicationId> vendorSpecificApplicationId = new ArrayList<VendorSpecificApplicationId>();
    /**
     * A dictionary instance is used to store the dictionary of a Diameter
     * Application.
     * 
     */
    @JsonProperty("dictionary")
    @JsonPropertyDescription("A dictionary instance is used to store the dictionary of a Diameter Application.")
    private List<Dictionary> dictionary = new ArrayList<Dictionary>();
    /**
     * A peer-table system created singleton instance is to be used as a container
     * for static-peer and dynamic-peer-acceptor instances that are used to
     * scope/filter the Diameter Peer Nodes which should be considered and stored by
     * the Diameter Service in its internal Peer Table.
     * 
     */
    @JsonProperty("peer-table")
    @JsonPropertyDescription("A peer-table system created singleton instance is to be used as a container for static-peer and dynamic-peer-acceptor instances that are used to scope/filter the Diameter Peer Nodes which should be considered and stored by the Diameter Service in its internal Peer Table.")
    private PeerTable peerTable;
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
    @JsonProperty("transport")
    @JsonPropertyDescription("A transport instance is a system created singleton object instance in the diameter configuration and it is used as a container for transport configuration. To have the Own Diameter Node accept connections from or initiate connections towards Peer Diameter Nodes the transport fragment of the managed object model is to be configured accordingly. This is performed by creating one or more Local Endpoints with wanted roles and transport capabilities. Local Endpoints are specified with instances of local-endpoint.")
    private Transport transport;
    /**
     * A peer-selector instance is used to group a collection of Diameter Nodes to
     * form a destination domain egress request messages can be passed towards as
     * result of evaluation of those routing entries that are referring to it.
     * Practically, in routing perspective, a peer-selector instance returns a set
     * of diameter peer connections organized in increased order of their determined
     * rank. That available diameter peer connection will be selected when matching
     * criteria is found during related routing entry evaluation that is with the
     * lowest rank. The peer selections (or peer groups) should be used when
     * Diameter Peer nodes are to be grouped based on different criteria to form
     * destination domains egress request messages can be passed towards as result
     * of related routing entry evaluation. That is, peer selections are to be
     * created only with the purpose to use them as destination domains during
     * routing entry evaluations. In other cases the use of peer selections (peer
     * groups) can be omitted. Define peer selections only if the
     * route-to-peer-selection action is planned to be used when creating routing
     * rules.
     * 
     */
    @JsonProperty("peer-selector")
    @JsonPropertyDescription("A peer-selector instance is used to group a collection of Diameter Nodes to form a destination domain egress request messages can be passed towards as result of evaluation of those routing entries that are referring to it. Practically, in routing perspective, a peer-selector instance returns a set of diameter peer connections organized in increased order of their determined rank. That available diameter peer connection will be selected when matching criteria is found during related routing entry evaluation that is with the lowest rank. The peer selections (or peer groups) should be used when Diameter Peer nodes are to be grouped based on different criteria to form destination domains egress request messages can be passed towards as result of related routing entry evaluation. That is, peer selections are to be created only with the purpose to use them as destination domains during routing entry evaluations. In other cases the use of peer selections (peer groups) can be omitted. Define peer selections only if the route-to-peer-selection action is planned to be used when creating routing rules.")
    private List<PeerSelector> peerSelector = new ArrayList<PeerSelector>();
    /**
     * Service diameter bases its message routing mechanism on the sequential
     * evaluation of a collection of routing rules organized in a table called
     * routing table. Each AAA Service using diameter must have a routing table
     * associated. A routing table can be dedicated to a single AAA Service or
     * shared between multiple AAA Services. There can be only a single routing
     * table associated with a certain AAA Service. When a routing decision is to be
     * made, the corresponding Diameter ingress or egress request message is checked
     * against such a routing table by evaluating the routing rules (also called
     * routing entries) in it one by one. The evaluation begins with the first
     * routing rule in the relevant routing table then continues with the next ones
     * up until either a routing rule fires or all the routing rules in the routing
     * table are exhausted. A routing table is represented by a routing-table
     * instance. Any change in the content of a routing table will be applied
     * immediately impacting in this way the related routing rules/entries evaluated
     * by the message routing mechanism of diameter. diameter will run a message
     * routing mechanism whenever an ingress or egress request message is received
     * by it: Egress Request Message Routing Whenever an egress request message is
     * created by a AAA Service and passed down the diameter stack for delivery
     * towards wanted destination a message routing mechanism is executed, on
     * diameter stack level, to determine the peer connection the egress request
     * message is to be sent through in order to have the message starting its route
     * towards its final destination. The message routing mechanism can either take
     * direct instruction via the runtime API from a AAA Service on the Diameter
     * Peer Node(s) to be used to send an egress request message towards, or it can
     * determine it itself by using the information stored in a previously loaded
     * routing table. That is, a routing table assigned to a AAA Service is
     * evaluated during egress request message sending only if there is no peer list
     * provided by related AAA Service during message sending request invocation
     * method invoked to request sending an egress request message holds a
     * peers=NULL list). Ingress Request Message Routing Whenever an ingress request
     * message is received by the diameter stack through one of its peer connections
     * the AAA Service the message shall be passed towards is evaluated as well by
     * using related routing entries expressed in the routing table. The Diameter
     * answer messages always follow the routing path built for the related request
     * message. This is the reason why answer messages are never matched against the
     * routing rules defined in the routing table. The expression of a routing table
     * for a AAA Service is mandatory. A routing table associated with a AAA Service
     * is constructed by using one or several routing entries.
     * 
     */
    @JsonProperty("routing-table")
    @JsonPropertyDescription("Service diameter bases its message routing mechanism on the sequential evaluation of a collection of routing rules organized in a table called routing table. Each AAA Service using diameter must have a routing table associated. A routing table can be dedicated to a single AAA Service or shared between multiple AAA Services. There can be only a single routing table associated with a certain AAA Service. When a routing decision is to be made, the corresponding Diameter ingress or egress request message is checked against such a routing table by evaluating the routing rules (also called routing entries) in it one by one. The evaluation begins with the first routing rule in the relevant routing table then continues with the next ones up until either a routing rule fires or all the routing rules in the routing table are exhausted. A routing table is represented by a routing-table instance. Any change in the content of a routing table will be applied immediately impacting in this way the related routing rules/entries evaluated by the message routing mechanism of diameter. diameter will run a message routing mechanism whenever an ingress or egress request message is received by it: Egress Request Message Routing Whenever an egress request message is created by a AAA Service and passed down the diameter stack for delivery towards wanted destination a message routing mechanism is executed, on diameter stack level, to determine the peer connection the egress request message is to be sent through in order to have the message starting its route towards its final destination. The message routing mechanism can either take direct instruction via the runtime API from a AAA Service on the Diameter Peer Node(s) to be used to send an egress request message towards, or it can determine it itself by using the information stored in a previously loaded routing table. That is, a routing table assigned to a AAA Service is evaluated during egress request message sending only if there is no peer list provided by related AAA Service during message sending request invocation method invoked to request sending an egress request message holds a peers=NULL list). Ingress Request Message Routing Whenever an ingress request message is received by the diameter stack through one of its peer connections the AAA Service the message shall be passed towards is evaluated as well by using related routing entries expressed in the routing table. The Diameter answer messages always follow the routing path built for the related request message. This is the reason why answer messages are never matched against the routing rules defined in the routing table. The expression of a routing table for a AAA Service is mandatory. A routing table associated with a AAA Service is constructed by using one or several routing entries.")
    private List<RoutingTable> routingTable = new ArrayList<RoutingTable>();
    /**
     * Container holding various policy objects.
     * 
     */
    @JsonProperty("policies")
    @JsonPropertyDescription("Container holding various policy objects.")
    private Policies policies;

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

    public EricssonDiameterAdpDiameter withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A service instance is used to describe the properties of AAA Service
     * implemented by a Diameter Service User. The settings on service instance
     * level are influencing the interaction mechanisms performed between the
     * Diameter Service and related AAA Service. (Required)
     * 
     */
    @JsonProperty("service")
    public List<Service> getService()
    {
        return service;
    }

    /**
     * A service instance is used to describe the properties of AAA Service
     * implemented by a Diameter Service User. The settings on service instance
     * level are influencing the interaction mechanisms performed between the
     * Diameter Service and related AAA Service. (Required)
     * 
     */
    @JsonProperty("service")
    public void setService(List<Service> service)
    {
        this.service = service;
    }

    public EricssonDiameterAdpDiameter withService(List<Service> service)
    {
        this.service = service;
        return this;
    }

    /**
     * The instances of applications are used to define
     * https://tools.ietf.org/html/rfc6733#section-1.3.4 Diameter Applications as
     * defined by the Diameter Base Protocol (https://tools.ietf.org/html/rfc6733. A
     * AAA Service can implement the client, server or agent side of one or several
     * Diameter Applications. The behavior of implemented Diameter Applications is
     * defined through related Diameter Application Specifications released by
     * different standardization bodies (for example: 3GPP, IETF, ETSI, and so on)
     * or vendors (for example: Ericsson).
     * 
     */
    @JsonProperty("applications")
    public List<Application> getApplications()
    {
        return applications;
    }

    /**
     * The instances of applications are used to define
     * https://tools.ietf.org/html/rfc6733#section-1.3.4 Diameter Applications as
     * defined by the Diameter Base Protocol (https://tools.ietf.org/html/rfc6733. A
     * AAA Service can implement the client, server or agent side of one or several
     * Diameter Applications. The behavior of implemented Diameter Applications is
     * defined through related Diameter Application Specifications released by
     * different standardization bodies (for example: 3GPP, IETF, ETSI, and so on)
     * or vendors (for example: Ericsson).
     * 
     */
    @JsonProperty("applications")
    public void setApplications(List<Application> applications)
    {
        this.applications = applications;
    }

    public EricssonDiameterAdpDiameter withApplications(List<Application> applications)
    {
        this.applications = applications;
        return this;
    }

    /**
     * A vendor-specific-application-id instance is used to provide information
     * about a vendor specific Diameter Application. The information provided in a
     * vendor-specific-application-id instance is used to construct a
     * Vendor-Specific-Application-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.11) which is of type grouped.
     * Each of a vendor-specific-application-id instance represents a related of the
     * Vendor-Specific-Application-Id AVP. The handling rules defined for
     * Vendor-Specific-Application-Id AVP applies for the related
     * vendor-specific-application-id instance attributes as well.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    public List<VendorSpecificApplicationId> getVendorSpecificApplicationId()
    {
        return vendorSpecificApplicationId;
    }

    /**
     * A vendor-specific-application-id instance is used to provide information
     * about a vendor specific Diameter Application. The information provided in a
     * vendor-specific-application-id instance is used to construct a
     * Vendor-Specific-Application-Id AVP
     * (https://tools.ietf.org/html/rfc6733#section-6.11) which is of type grouped.
     * Each of a vendor-specific-application-id instance represents a related of the
     * Vendor-Specific-Application-Id AVP. The handling rules defined for
     * Vendor-Specific-Application-Id AVP applies for the related
     * vendor-specific-application-id instance attributes as well.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    public void setVendorSpecificApplicationId(List<VendorSpecificApplicationId> vendorSpecificApplicationId)
    {
        this.vendorSpecificApplicationId = vendorSpecificApplicationId;
    }

    public EricssonDiameterAdpDiameter withVendorSpecificApplicationId(List<VendorSpecificApplicationId> vendorSpecificApplicationId)
    {
        this.vendorSpecificApplicationId = vendorSpecificApplicationId;
        return this;
    }

    /**
     * A dictionary instance is used to store the dictionary of a Diameter
     * Application.
     * 
     */
    @JsonProperty("dictionary")
    public List<Dictionary> getDictionary()
    {
        return dictionary;
    }

    /**
     * A dictionary instance is used to store the dictionary of a Diameter
     * Application.
     * 
     */
    @JsonProperty("dictionary")
    public void setDictionary(List<Dictionary> dictionary)
    {
        this.dictionary = dictionary;
    }

    public EricssonDiameterAdpDiameter withDictionary(List<Dictionary> dictionary)
    {
        this.dictionary = dictionary;
        return this;
    }

    /**
     * A peer-table system created singleton instance is to be used as a container
     * for static-peer and dynamic-peer-acceptor instances that are used to
     * scope/filter the Diameter Peer Nodes which should be considered and stored by
     * the Diameter Service in its internal Peer Table.
     * 
     */
    @JsonProperty("peer-table")
    public PeerTable getPeerTable()
    {
        return peerTable;
    }

    /**
     * A peer-table system created singleton instance is to be used as a container
     * for static-peer and dynamic-peer-acceptor instances that are used to
     * scope/filter the Diameter Peer Nodes which should be considered and stored by
     * the Diameter Service in its internal Peer Table.
     * 
     */
    @JsonProperty("peer-table")
    public void setPeerTable(PeerTable peerTable)
    {
        this.peerTable = peerTable;
    }

    public EricssonDiameterAdpDiameter withPeerTable(PeerTable peerTable)
    {
        this.peerTable = peerTable;
        return this;
    }

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
    @JsonProperty("transport")
    public Transport getTransport()
    {
        return transport;
    }

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
    @JsonProperty("transport")
    public void setTransport(Transport transport)
    {
        this.transport = transport;
    }

    public EricssonDiameterAdpDiameter withTransport(Transport transport)
    {
        this.transport = transport;
        return this;
    }

    /**
     * A peer-selector instance is used to group a collection of Diameter Nodes to
     * form a destination domain egress request messages can be passed towards as
     * result of evaluation of those routing entries that are referring to it.
     * Practically, in routing perspective, a peer-selector instance returns a set
     * of diameter peer connections organized in increased order of their determined
     * rank. That available diameter peer connection will be selected when matching
     * criteria is found during related routing entry evaluation that is with the
     * lowest rank. The peer selections (or peer groups) should be used when
     * Diameter Peer nodes are to be grouped based on different criteria to form
     * destination domains egress request messages can be passed towards as result
     * of related routing entry evaluation. That is, peer selections are to be
     * created only with the purpose to use them as destination domains during
     * routing entry evaluations. In other cases the use of peer selections (peer
     * groups) can be omitted. Define peer selections only if the
     * route-to-peer-selection action is planned to be used when creating routing
     * rules.
     * 
     */
    @JsonProperty("peer-selector")
    public List<PeerSelector> getPeerSelector()
    {
        return peerSelector;
    }

    /**
     * A peer-selector instance is used to group a collection of Diameter Nodes to
     * form a destination domain egress request messages can be passed towards as
     * result of evaluation of those routing entries that are referring to it.
     * Practically, in routing perspective, a peer-selector instance returns a set
     * of diameter peer connections organized in increased order of their determined
     * rank. That available diameter peer connection will be selected when matching
     * criteria is found during related routing entry evaluation that is with the
     * lowest rank. The peer selections (or peer groups) should be used when
     * Diameter Peer nodes are to be grouped based on different criteria to form
     * destination domains egress request messages can be passed towards as result
     * of related routing entry evaluation. That is, peer selections are to be
     * created only with the purpose to use them as destination domains during
     * routing entry evaluations. In other cases the use of peer selections (peer
     * groups) can be omitted. Define peer selections only if the
     * route-to-peer-selection action is planned to be used when creating routing
     * rules.
     * 
     */
    @JsonProperty("peer-selector")
    public void setPeerSelector(List<PeerSelector> peerSelector)
    {
        this.peerSelector = peerSelector;
    }

    public EricssonDiameterAdpDiameter withPeerSelector(List<PeerSelector> peerSelector)
    {
        this.peerSelector = peerSelector;
        return this;
    }

    /**
     * Service diameter bases its message routing mechanism on the sequential
     * evaluation of a collection of routing rules organized in a table called
     * routing table. Each AAA Service using diameter must have a routing table
     * associated. A routing table can be dedicated to a single AAA Service or
     * shared between multiple AAA Services. There can be only a single routing
     * table associated with a certain AAA Service. When a routing decision is to be
     * made, the corresponding Diameter ingress or egress request message is checked
     * against such a routing table by evaluating the routing rules (also called
     * routing entries) in it one by one. The evaluation begins with the first
     * routing rule in the relevant routing table then continues with the next ones
     * up until either a routing rule fires or all the routing rules in the routing
     * table are exhausted. A routing table is represented by a routing-table
     * instance. Any change in the content of a routing table will be applied
     * immediately impacting in this way the related routing rules/entries evaluated
     * by the message routing mechanism of diameter. diameter will run a message
     * routing mechanism whenever an ingress or egress request message is received
     * by it: Egress Request Message Routing Whenever an egress request message is
     * created by a AAA Service and passed down the diameter stack for delivery
     * towards wanted destination a message routing mechanism is executed, on
     * diameter stack level, to determine the peer connection the egress request
     * message is to be sent through in order to have the message starting its route
     * towards its final destination. The message routing mechanism can either take
     * direct instruction via the runtime API from a AAA Service on the Diameter
     * Peer Node(s) to be used to send an egress request message towards, or it can
     * determine it itself by using the information stored in a previously loaded
     * routing table. That is, a routing table assigned to a AAA Service is
     * evaluated during egress request message sending only if there is no peer list
     * provided by related AAA Service during message sending request invocation
     * method invoked to request sending an egress request message holds a
     * peers=NULL list). Ingress Request Message Routing Whenever an ingress request
     * message is received by the diameter stack through one of its peer connections
     * the AAA Service the message shall be passed towards is evaluated as well by
     * using related routing entries expressed in the routing table. The Diameter
     * answer messages always follow the routing path built for the related request
     * message. This is the reason why answer messages are never matched against the
     * routing rules defined in the routing table. The expression of a routing table
     * for a AAA Service is mandatory. A routing table associated with a AAA Service
     * is constructed by using one or several routing entries.
     * 
     */
    @JsonProperty("routing-table")
    public List<RoutingTable> getRoutingTable()
    {
        return routingTable;
    }

    /**
     * Service diameter bases its message routing mechanism on the sequential
     * evaluation of a collection of routing rules organized in a table called
     * routing table. Each AAA Service using diameter must have a routing table
     * associated. A routing table can be dedicated to a single AAA Service or
     * shared between multiple AAA Services. There can be only a single routing
     * table associated with a certain AAA Service. When a routing decision is to be
     * made, the corresponding Diameter ingress or egress request message is checked
     * against such a routing table by evaluating the routing rules (also called
     * routing entries) in it one by one. The evaluation begins with the first
     * routing rule in the relevant routing table then continues with the next ones
     * up until either a routing rule fires or all the routing rules in the routing
     * table are exhausted. A routing table is represented by a routing-table
     * instance. Any change in the content of a routing table will be applied
     * immediately impacting in this way the related routing rules/entries evaluated
     * by the message routing mechanism of diameter. diameter will run a message
     * routing mechanism whenever an ingress or egress request message is received
     * by it: Egress Request Message Routing Whenever an egress request message is
     * created by a AAA Service and passed down the diameter stack for delivery
     * towards wanted destination a message routing mechanism is executed, on
     * diameter stack level, to determine the peer connection the egress request
     * message is to be sent through in order to have the message starting its route
     * towards its final destination. The message routing mechanism can either take
     * direct instruction via the runtime API from a AAA Service on the Diameter
     * Peer Node(s) to be used to send an egress request message towards, or it can
     * determine it itself by using the information stored in a previously loaded
     * routing table. That is, a routing table assigned to a AAA Service is
     * evaluated during egress request message sending only if there is no peer list
     * provided by related AAA Service during message sending request invocation
     * method invoked to request sending an egress request message holds a
     * peers=NULL list). Ingress Request Message Routing Whenever an ingress request
     * message is received by the diameter stack through one of its peer connections
     * the AAA Service the message shall be passed towards is evaluated as well by
     * using related routing entries expressed in the routing table. The Diameter
     * answer messages always follow the routing path built for the related request
     * message. This is the reason why answer messages are never matched against the
     * routing rules defined in the routing table. The expression of a routing table
     * for a AAA Service is mandatory. A routing table associated with a AAA Service
     * is constructed by using one or several routing entries.
     * 
     */
    @JsonProperty("routing-table")
    public void setRoutingTable(List<RoutingTable> routingTable)
    {
        this.routingTable = routingTable;
    }

    public EricssonDiameterAdpDiameter withRoutingTable(List<RoutingTable> routingTable)
    {
        this.routingTable = routingTable;
        return this;
    }

    /**
     * Container holding various policy objects.
     * 
     */
    @JsonProperty("policies")
    public Policies getPolicies()
    {
        return policies;
    }

    /**
     * Container holding various policy objects.
     * 
     */
    @JsonProperty("policies")
    public void setPolicies(Policies policies)
    {
        this.policies = policies;
    }

    public EricssonDiameterAdpDiameter withPolicies(Policies policies)
    {
        this.policies = policies;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonDiameterAdpDiameter.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("service");
        sb.append('=');
        sb.append(((this.service == null) ? "<null>" : this.service));
        sb.append(',');
        sb.append("applications");
        sb.append('=');
        sb.append(((this.applications == null) ? "<null>" : this.applications));
        sb.append(',');
        sb.append("vendorSpecificApplicationId");
        sb.append('=');
        sb.append(((this.vendorSpecificApplicationId == null) ? "<null>" : this.vendorSpecificApplicationId));
        sb.append(',');
        sb.append("dictionary");
        sb.append('=');
        sb.append(((this.dictionary == null) ? "<null>" : this.dictionary));
        sb.append(',');
        sb.append("peerTable");
        sb.append('=');
        sb.append(((this.peerTable == null) ? "<null>" : this.peerTable));
        sb.append(',');
        sb.append("transport");
        sb.append('=');
        sb.append(((this.transport == null) ? "<null>" : this.transport));
        sb.append(',');
        sb.append("peerSelector");
        sb.append('=');
        sb.append(((this.peerSelector == null) ? "<null>" : this.peerSelector));
        sb.append(',');
        sb.append("routingTable");
        sb.append('=');
        sb.append(((this.routingTable == null) ? "<null>" : this.routingTable));
        sb.append(',');
        sb.append("policies");
        sb.append('=');
        sb.append(((this.policies == null) ? "<null>" : this.policies));
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
        result = ((result * 31) + ((this.dictionary == null) ? 0 : this.dictionary.hashCode()));
        result = ((result * 31) + ((this.vendorSpecificApplicationId == null) ? 0 : this.vendorSpecificApplicationId.hashCode()));
        result = ((result * 31) + ((this.service == null) ? 0 : this.service.hashCode()));
        result = ((result * 31) + ((this.peerSelector == null) ? 0 : this.peerSelector.hashCode()));
        result = ((result * 31) + ((this.policies == null) ? 0 : this.policies.hashCode()));
        result = ((result * 31) + ((this.routingTable == null) ? 0 : this.routingTable.hashCode()));
        result = ((result * 31) + ((this.transport == null) ? 0 : this.transport.hashCode()));
        result = ((result * 31) + ((this.peerTable == null) ? 0 : this.peerTable.hashCode()));
        result = ((result * 31) + ((this.applications == null) ? 0 : this.applications.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonDiameterAdpDiameter) == false)
        {
            return false;
        }
        EricssonDiameterAdpDiameter rhs = ((EricssonDiameterAdpDiameter) other);
        return (((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                        && ((this.dictionary == rhs.dictionary) || ((this.dictionary != null) && this.dictionary.equals(rhs.dictionary))))
                       && ((this.vendorSpecificApplicationId == rhs.vendorSpecificApplicationId)
                           || ((this.vendorSpecificApplicationId != null) && this.vendorSpecificApplicationId.equals(rhs.vendorSpecificApplicationId))))
                      && ((this.service == rhs.service) || ((this.service != null) && this.service.equals(rhs.service))))
                     && ((this.peerSelector == rhs.peerSelector) || ((this.peerSelector != null) && this.peerSelector.equals(rhs.peerSelector))))
                    && ((this.policies == rhs.policies) || ((this.policies != null) && this.policies.equals(rhs.policies))))
                   && ((this.routingTable == rhs.routingTable) || ((this.routingTable != null) && this.routingTable.equals(rhs.routingTable))))
                  && ((this.transport == rhs.transport) || ((this.transport != null) && this.transport.equals(rhs.transport))))
                 && ((this.peerTable == rhs.peerTable) || ((this.peerTable != null) && this.peerTable.equals(rhs.peerTable))))
                && ((this.applications == rhs.applications) || ((this.applications != null) && this.applications.equals(rhs.applications))));
    }

}
