
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.nfm.model.NfProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nf-peer-info",
                     "nf-instance-id",
                     "dns-profile",
                     "dns-profile-ref",
                     "ingress-connection-profile",
                     "ingress-connection-profile-ref",
                     "egress-connection-profile",
                     "egress-connection-profile-ref",
                     "local-rate-limit-profile",
                     "global-rate-limit-profile",
                     "message-body-limits",
                     "vtap",
                     "slf-lookup-profile",
                     "service-address",
                     "own-network",
                     "message-data",
                     "request-screening-case",
                     "response-screening-case",
                     "routing-case",
                     "static-nf-instance-data",
                     "static-scp-instance-data",
                     "static-sepp-instance-data",
                     "nf-pool",
                     "failover-profile",
                     "nf-profile",
                     "nrf-group",
                     "nrf-service" })
public class NfInstance implements IfNfInstance
{

    /**
     * Name identifying the SCP instance (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the SCP instance")
    private String name;
    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    @JsonPropertyDescription("Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level")
    private NfPeerInfo nfPeerInfo;
    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the SCP registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("A Universally Unique IDentifier in the string representation defined in RFC 4122. The canonical representation uses lower case characters. The following is an example of a UUID in string representation: f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It has a meaning for how the SCP registers in its NRF(s): If not set, an automatically generated NF instance ID (a different one for each NRF group) is used for the registration. If set, this is used for the registration rather than the automatically generated NF instance IDs of the NRF groups.")
    private String nfInstanceId;
    /**
     * Settings for DNS resolution of producers.
     * 
     */
    @JsonProperty("dns-profile")
    @JsonPropertyDescription("Settings for DNS resolution of producers.")
    private List<DnsProfile> dnsProfile = new ArrayList<DnsProfile>();
    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of all nf-pools.
     * 
     */
    @JsonProperty("dns-profile-ref")
    @JsonPropertyDescription("Reference to a defined dns-profile that will be used for DNS resolution on producers of all nf-pools.")
    private String dnsProfileRef;
    /**
     * Connection profile for ingress traffic cases (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile")
    @JsonPropertyDescription("Connection profile for ingress traffic cases")
    private List<IngressConnectionProfile> ingressConnectionProfile = new ArrayList<IngressConnectionProfile>();
    /**
     * Reference to a defined ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    @JsonPropertyDescription("Reference to a defined ingress-connection-profile")
    private String ingressConnectionProfileRef;
    /**
     * Connection profile for egress traffic cases
     * 
     */
    @JsonProperty("egress-connection-profile")
    @JsonPropertyDescription("Connection profile for egress traffic cases")
    private List<EgressConnectionProfile> egressConnectionProfile = new ArrayList<EgressConnectionProfile>();
    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    @JsonPropertyDescription("Reference to a defined egress-connection-profile")
    private String egressConnectionProfileRef;
    /**
     * Contains the necessary parameters for local rate limiting to be applied.
     * Referenced from own-network
     * 
     */
    @JsonProperty("local-rate-limit-profile")
    @JsonPropertyDescription("Contains the necessary parameters for local rate limiting to be applied. Referenced from own-network")
    private List<LocalRateLimitProfile> localRateLimitProfile = new ArrayList<LocalRateLimitProfile>();
    /**
     * Define the necessary parameters for global rate limiting to be applied.
     * 
     */
    @JsonProperty("global-rate-limit-profile")
    @JsonPropertyDescription("Define the necessary parameters for global rate limiting to be applied.")
    private List<GlobalRateLimitProfile> globalRateLimitProfile = new ArrayList<GlobalRateLimitProfile>();
    /**
     * Defines the limits for the message body
     * 
     */
    @JsonProperty("message-body-limits")
    @JsonPropertyDescription("Defines the limits for the message body")
    private MessageBodyLimits messageBodyLimits;
    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    @JsonPropertyDescription("Defines the required data for traffic tapping")
    private Vtap vtap;
    /**
     * Service Lookup Function (SLF) configuration parameters
     * 
     */
    @JsonProperty("slf-lookup-profile")
    @JsonPropertyDescription("Service Lookup Function (SLF) configuration parameters")
    private List<SlfLookupProfile> slfLookupProfile = new ArrayList<SlfLookupProfile>();
    /**
     * Listening address of the SCP, referenced in own-network as well as the
     * nf-profile and nf-service sent to the NRF (Required)
     * 
     */
    @JsonProperty("service-address")
    @JsonPropertyDescription("Listening address of the SCP, referenced in own-network as well as the nf-profile and nf-service sent to the NRF")
    private List<ServiceAddress> serviceAddress = new ArrayList<ServiceAddress>();
    /**
     * Definition of an own, internal network of this SCP instance (Required)
     * 
     */
    @JsonProperty("own-network")
    @JsonPropertyDescription("Definition of an own, internal network of this SCP instance")
    private List<OwnNetwork> ownNetwork = new ArrayList<OwnNetwork>();
    /**
     * Data extracted from incoming requests, used in routing rules to determine the
     * appropriate routing action
     * 
     */
    @JsonProperty("message-data")
    @JsonPropertyDescription("Data extracted from incoming requests, used in routing rules to determine the appropriate routing action")
    private List<MessageDatum> messageData = new ArrayList<MessageDatum>();
    /**
     * Entry point to a list of screening rules which are evaluated in sequence to
     * filter a request message
     * 
     */
    @JsonProperty("request-screening-case")
    @JsonPropertyDescription("Entry point to a list of screening rules which are evaluated in sequence to filter a request message")
    private List<RequestScreeningCase> requestScreeningCase = new ArrayList<RequestScreeningCase>();
    /**
     * Entry point to a list of screening rules which are evaluated in sequence on
     * response messages
     * 
     */
    @JsonProperty("response-screening-case")
    @JsonPropertyDescription("Entry point to a list of screening rules which are evaluated in sequence on response messages")
    private List<ResponseScreeningCase> responseScreeningCase = new ArrayList<ResponseScreeningCase>();
    /**
     * Entry point to a list of routing rules which are evaluated in sequence
     * (Required)
     * 
     */
    @JsonProperty("routing-case")
    @JsonPropertyDescription("Entry point to a list of routing rules which are evaluated in sequence")
    private List<RoutingCase> routingCase = new ArrayList<RoutingCase>();
    /**
     * Statically defined NF instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-nf-instance-data")
    @JsonPropertyDescription("Statically defined NF instances to be referenced in a nf-pool")
    private List<StaticNfInstanceDatum> staticNfInstanceData = new ArrayList<StaticNfInstanceDatum>();
    /**
     * Statically defined SCP instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-scp-instance-data")
    @JsonPropertyDescription("Statically defined SCP instances to be referenced in a nf-pool")
    private List<StaticScpInstanceDatum> staticScpInstanceData = new ArrayList<StaticScpInstanceDatum>();
    /**
     * Statically defined SEPP instances to be referenced in an nf-pool
     * 
     */
    @JsonProperty("static-sepp-instance-data")
    @JsonPropertyDescription("Statically defined SEPP instances to be referenced in an nf-pool")
    private List<StaticSeppInstanceDatum> staticSeppInstanceData = new ArrayList<StaticSeppInstanceDatum>();
    /**
     * Grouping of defined or dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    @JsonPropertyDescription("Grouping of defined or dynamically discovered NF instances")
    private List<NfPool> nfPool = new ArrayList<NfPool>();
    /**
     * Determines the failover behaviour in case of failure such as lack of response
     * or error response from peer
     * 
     */
    @JsonProperty("failover-profile")
    @JsonPropertyDescription("Determines the failover behaviour in case of failure such as lack of response or error response from peer")
    private List<FailoverProfile> failoverProfile = new ArrayList<FailoverProfile>();
    /**
     * Profile consisting of general parameters of the SCP instance and the services
     * exposed by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    @JsonPropertyDescription("Profile consisting of general parameters of the SCP instance and the services exposed by it, sent to the NRF at registration")
    private List<NfProfile> nfProfile = new ArrayList<NfProfile>();
    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this SCP instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile
     * 
     */
    @JsonProperty("nrf-group")
    @JsonPropertyDescription("The NRF group specifies all relevant information of NRFs that are available to this SCP instance. Regarding the Nnrf_NFManagement interface, it determines the NRF registration behavior and triggers the NRF registration for the NF instance specified in the nf-profile")
    private List<NrfGroup> nrfGroup = new ArrayList<NrfGroup>();
    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service
     * 
     */
    @JsonProperty("nrf-service")
    @JsonPropertyDescription("Defines which NRF services are used by this NF instance, referencing groups of NRFs that provide the respective service")
    private NrfService nrfService;

    /**
     * Name identifying the SCP instance (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the SCP instance (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfInstance withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    public NfPeerInfo getNfPeerInfo()
    {
        return nfPeerInfo;
    }

    /**
     * Defines the required data for 3gpp-Sbi-NF-Peer-Info header in a global level
     * 
     */
    @JsonProperty("nf-peer-info")
    public void setNfPeerInfo(NfPeerInfo nfPeerInfo)
    {
        this.nfPeerInfo = nfPeerInfo;
    }

    public NfInstance withNfPeerInfo(NfPeerInfo nfPeerInfo)
    {
        this.nfPeerInfo = nfPeerInfo;
        return this;
    }

    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the SCP registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * A Universally Unique IDentifier in the string representation defined in RFC
     * 4122. The canonical representation uses lower case characters. The following
     * is an example of a UUID in string representation:
     * f81d4fae-7dec-11d0-a765-00a0c91e6bf6 Setting this property is optional. It
     * has a meaning for how the SCP registers in its NRF(s): If not set, an
     * automatically generated NF instance ID (a different one for each NRF group)
     * is used for the registration. If set, this is used for the registration
     * rather than the automatically generated NF instance IDs of the NRF groups.
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public NfInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * Settings for DNS resolution of producers.
     * 
     */
    @JsonProperty("dns-profile")
    public List<DnsProfile> getDnsProfile()
    {
        return dnsProfile;
    }

    /**
     * Settings for DNS resolution of producers.
     * 
     */
    @JsonProperty("dns-profile")
    public void setDnsProfile(List<DnsProfile> dnsProfile)
    {
        this.dnsProfile = dnsProfile;
    }

    public NfInstance withDnsProfile(List<DnsProfile> dnsProfile)
    {
        this.dnsProfile = dnsProfile;
        return this;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of all nf-pools.
     * 
     */
    @JsonProperty("dns-profile-ref")
    public String getDnsProfileRef()
    {
        return dnsProfileRef;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution on
     * producers of all nf-pools.
     * 
     */
    @JsonProperty("dns-profile-ref")
    public void setDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
    }

    public NfInstance withDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
        return this;
    }

    /**
     * Connection profile for ingress traffic cases (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile")
    public List<IngressConnectionProfile> getIngressConnectionProfile()
    {
        return ingressConnectionProfile;
    }

    /**
     * Connection profile for ingress traffic cases (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile")
    public void setIngressConnectionProfile(List<IngressConnectionProfile> ingressConnectionProfile)
    {
        this.ingressConnectionProfile = ingressConnectionProfile;
    }

    public NfInstance withIngressConnectionProfile(List<IngressConnectionProfile> ingressConnectionProfile)
    {
        this.ingressConnectionProfile = ingressConnectionProfile;
        return this;
    }

    /**
     * Reference to a defined ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    public String getIngressConnectionProfileRef()
    {
        return ingressConnectionProfileRef;
    }

    /**
     * Reference to a defined ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("ingress-connection-profile-ref")
    public void setIngressConnectionProfileRef(String ingressConnectionProfileRef)
    {
        this.ingressConnectionProfileRef = ingressConnectionProfileRef;
    }

    public NfInstance withIngressConnectionProfileRef(String ingressConnectionProfileRef)
    {
        this.ingressConnectionProfileRef = ingressConnectionProfileRef;
        return this;
    }

    /**
     * Connection profile for egress traffic cases
     * 
     */
    @JsonProperty("egress-connection-profile")
    public List<EgressConnectionProfile> getEgressConnectionProfile()
    {
        return egressConnectionProfile;
    }

    /**
     * Connection profile for egress traffic cases
     * 
     */
    @JsonProperty("egress-connection-profile")
    public void setEgressConnectionProfile(List<EgressConnectionProfile> egressConnectionProfile)
    {
        this.egressConnectionProfile = egressConnectionProfile;
    }

    public NfInstance withEgressConnectionProfile(List<EgressConnectionProfile> egressConnectionProfile)
    {
        this.egressConnectionProfile = egressConnectionProfile;
        return this;
    }

    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    public String getEgressConnectionProfileRef()
    {
        return egressConnectionProfileRef;
    }

    /**
     * Reference to a defined egress-connection-profile
     * 
     */
    @JsonProperty("egress-connection-profile-ref")
    public void setEgressConnectionProfileRef(String egressConnectionProfileRef)
    {
        this.egressConnectionProfileRef = egressConnectionProfileRef;
    }

    public NfInstance withEgressConnectionProfileRef(String egressConnectionProfileRef)
    {
        this.egressConnectionProfileRef = egressConnectionProfileRef;
        return this;
    }

    /**
     * Contains the necessary parameters for local rate limiting to be applied.
     * Referenced from own-network
     * 
     */
    @JsonProperty("local-rate-limit-profile")
    public List<LocalRateLimitProfile> getLocalRateLimitProfile()
    {
        return localRateLimitProfile;
    }

    /**
     * Contains the necessary parameters for local rate limiting to be applied.
     * Referenced from own-network
     * 
     */
    @JsonProperty("local-rate-limit-profile")
    public void setLocalRateLimitProfile(List<LocalRateLimitProfile> localRateLimitProfile)
    {
        this.localRateLimitProfile = localRateLimitProfile;
    }

    public NfInstance withLocalRateLimitProfile(List<LocalRateLimitProfile> localRateLimitProfile)
    {
        this.localRateLimitProfile = localRateLimitProfile;
        return this;
    }

    /**
     * Define the necessary parameters for global rate limiting to be applied.
     * 
     */
    @JsonProperty("global-rate-limit-profile")
    public List<GlobalRateLimitProfile> getGlobalRateLimitProfile()
    {
        return globalRateLimitProfile;
    }

    /**
     * Define the necessary parameters for global rate limiting to be applied.
     * 
     */
    @JsonProperty("global-rate-limit-profile")
    public void setGlobalRateLimitProfile(List<GlobalRateLimitProfile> globalRateLimitProfile)
    {
        this.globalRateLimitProfile = globalRateLimitProfile;
    }

    public NfInstance withGlobalRateLimitProfile(List<GlobalRateLimitProfile> globalRateLimitProfile)
    {
        this.globalRateLimitProfile = globalRateLimitProfile;
        return this;
    }

    /**
     * Defines the limits for the message body
     * 
     */
    @JsonProperty("message-body-limits")
    public MessageBodyLimits getMessageBodyLimits()
    {
        return messageBodyLimits;
    }

    /**
     * Defines the limits for the message body
     * 
     */
    @JsonProperty("message-body-limits")
    public void setMessageBodyLimits(MessageBodyLimits messageBodyLimits)
    {
        this.messageBodyLimits = messageBodyLimits;
    }

    public NfInstance withMessageBodyLimits(MessageBodyLimits messageBodyLimits)
    {
        this.messageBodyLimits = messageBodyLimits;
        return this;
    }

    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    public Vtap getVtap()
    {
        return vtap;
    }

    /**
     * Defines the required data for traffic tapping
     * 
     */
    @JsonProperty("vtap")
    public void setVtap(Vtap vtap)
    {
        this.vtap = vtap;
    }

    public NfInstance withVtap(Vtap vtap)
    {
        this.vtap = vtap;
        return this;
    }

    /**
     * Service Lookup Function (SLF) configuration parameters
     * 
     */
    @JsonProperty("slf-lookup-profile")
    public List<SlfLookupProfile> getSlfLookupProfile()
    {
        return slfLookupProfile;
    }

    /**
     * Service Lookup Function (SLF) configuration parameters
     * 
     */
    @JsonProperty("slf-lookup-profile")
    public void setSlfLookupProfile(List<SlfLookupProfile> slfLookupProfile)
    {
        this.slfLookupProfile = slfLookupProfile;
    }

    public NfInstance withSlfLookupProfile(List<SlfLookupProfile> slfLookupProfile)
    {
        this.slfLookupProfile = slfLookupProfile;
        return this;
    }

    /**
     * Listening address of the SCP, referenced in own-network as well as the
     * nf-profile and nf-service sent to the NRF (Required)
     * 
     */
    @JsonProperty("service-address")
    public List<ServiceAddress> getServiceAddress()
    {
        return serviceAddress;
    }

    /**
     * Listening address of the SCP, referenced in own-network as well as the
     * nf-profile and nf-service sent to the NRF (Required)
     * 
     */
    @JsonProperty("service-address")
    public void setServiceAddress(List<ServiceAddress> serviceAddress)
    {
        this.serviceAddress = serviceAddress;
    }

    public NfInstance withServiceAddress(List<ServiceAddress> serviceAddress)
    {
        this.serviceAddress = serviceAddress;
        return this;
    }

    /**
     * Definition of an own, internal network of this SCP instance (Required)
     * 
     */
    @JsonProperty("own-network")
    public List<OwnNetwork> getOwnNetwork()
    {
        return ownNetwork;
    }

    /**
     * Definition of an own, internal network of this SCP instance (Required)
     * 
     */
    @JsonProperty("own-network")
    public void setOwnNetwork(List<OwnNetwork> ownNetwork)
    {
        this.ownNetwork = ownNetwork;
    }

    public NfInstance withOwnNetwork(List<OwnNetwork> ownNetwork)
    {
        this.ownNetwork = ownNetwork;
        return this;
    }

    /**
     * Data extracted from incoming requests, used in routing rules to determine the
     * appropriate routing action
     * 
     */
    @JsonProperty("message-data")
    public List<MessageDatum> getMessageData()
    {
        return messageData;
    }

    /**
     * Data extracted from incoming requests, used in routing rules to determine the
     * appropriate routing action
     * 
     */
    @JsonProperty("message-data")
    public void setMessageData(List<MessageDatum> messageData)
    {
        this.messageData = messageData;
    }

    public NfInstance withMessageData(List<MessageDatum> messageData)
    {
        this.messageData = messageData;
        return this;
    }

    /**
     * Entry point to a list of screening rules which are evaluated in sequence to
     * filter a request message
     * 
     */
    @JsonProperty("request-screening-case")
    public List<RequestScreeningCase> getRequestScreeningCase()
    {
        return requestScreeningCase;
    }

    /**
     * Entry point to a list of screening rules which are evaluated in sequence to
     * filter a request message
     * 
     */
    @JsonProperty("request-screening-case")
    public void setRequestScreeningCase(List<RequestScreeningCase> requestScreeningCase)
    {
        this.requestScreeningCase = requestScreeningCase;
    }

    public NfInstance withRequestScreeningCase(List<RequestScreeningCase> requestScreeningCase)
    {
        this.requestScreeningCase = requestScreeningCase;
        return this;
    }

    /**
     * Entry point to a list of screening rules which are evaluated in sequence on
     * response messages
     * 
     */
    @JsonProperty("response-screening-case")
    public List<ResponseScreeningCase> getResponseScreeningCase()
    {
        return responseScreeningCase;
    }

    /**
     * Entry point to a list of screening rules which are evaluated in sequence on
     * response messages
     * 
     */
    @JsonProperty("response-screening-case")
    public void setResponseScreeningCase(List<ResponseScreeningCase> responseScreeningCase)
    {
        this.responseScreeningCase = responseScreeningCase;
    }

    public NfInstance withResponseScreeningCase(List<ResponseScreeningCase> responseScreeningCase)
    {
        this.responseScreeningCase = responseScreeningCase;
        return this;
    }

    /**
     * Entry point to a list of routing rules which are evaluated in sequence
     * (Required)
     * 
     */
    @JsonProperty("routing-case")
    public List<RoutingCase> getRoutingCase()
    {
        return routingCase;
    }

    /**
     * Entry point to a list of routing rules which are evaluated in sequence
     * (Required)
     * 
     */
    @JsonProperty("routing-case")
    public void setRoutingCase(List<RoutingCase> routingCase)
    {
        this.routingCase = routingCase;
    }

    public NfInstance withRoutingCase(List<RoutingCase> routingCase)
    {
        this.routingCase = routingCase;
        return this;
    }

    /**
     * Statically defined NF instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-nf-instance-data")
    public List<StaticNfInstanceDatum> getStaticNfInstanceData()
    {
        return staticNfInstanceData;
    }

    /**
     * Statically defined NF instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-nf-instance-data")
    public void setStaticNfInstanceData(List<StaticNfInstanceDatum> staticNfInstanceData)
    {
        this.staticNfInstanceData = staticNfInstanceData;
    }

    public NfInstance withStaticNfInstanceData(List<StaticNfInstanceDatum> staticNfInstanceData)
    {
        this.staticNfInstanceData = staticNfInstanceData;
        return this;
    }

    /**
     * Statically defined SCP instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-scp-instance-data")
    public List<StaticScpInstanceDatum> getStaticScpInstanceData()
    {
        return staticScpInstanceData;
    }

    /**
     * Statically defined SCP instances to be referenced in a nf-pool
     * 
     */
    @JsonProperty("static-scp-instance-data")
    public void setStaticScpInstanceData(List<StaticScpInstanceDatum> staticScpInstanceData)
    {
        this.staticScpInstanceData = staticScpInstanceData;
    }

    public NfInstance withStaticScpInstanceData(List<StaticScpInstanceDatum> staticScpInstanceData)
    {
        this.staticScpInstanceData = staticScpInstanceData;
        return this;
    }

    /**
     * Statically defined SEPP instances to be referenced in an nf-pool
     * 
     */
    @JsonProperty("static-sepp-instance-data")
    public List<StaticSeppInstanceDatum> getStaticSeppInstanceData()
    {
        return staticSeppInstanceData;
    }

    /**
     * Statically defined SEPP instances to be referenced in an nf-pool
     * 
     */
    @JsonProperty("static-sepp-instance-data")
    public void setStaticSeppInstanceData(List<StaticSeppInstanceDatum> staticSeppInstanceData)
    {
        this.staticSeppInstanceData = staticSeppInstanceData;
    }

    public NfInstance withStaticSeppInstanceData(List<StaticSeppInstanceDatum> staticSeppInstanceData)
    {
        this.staticSeppInstanceData = staticSeppInstanceData;
        return this;
    }

    /**
     * Grouping of defined or dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    public List<NfPool> getNfPool()
    {
        return nfPool;
    }

    /**
     * Grouping of defined or dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    public void setNfPool(List<NfPool> nfPool)
    {
        this.nfPool = nfPool;
    }

    public NfInstance withNfPool(List<NfPool> nfPool)
    {
        this.nfPool = nfPool;
        return this;
    }

    /**
     * Determines the failover behaviour in case of failure such as lack of response
     * or error response from peer
     * 
     */
    @JsonProperty("failover-profile")
    public List<FailoverProfile> getFailoverProfile()
    {
        return failoverProfile;
    }

    /**
     * Determines the failover behaviour in case of failure such as lack of response
     * or error response from peer
     * 
     */
    @JsonProperty("failover-profile")
    public void setFailoverProfile(List<FailoverProfile> failoverProfile)
    {
        this.failoverProfile = failoverProfile;
    }

    public NfInstance withFailoverProfile(List<FailoverProfile> failoverProfile)
    {
        this.failoverProfile = failoverProfile;
        return this;
    }

    /**
     * Profile consisting of general parameters of the SCP instance and the services
     * exposed by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    public List<NfProfile> getNfProfile()
    {
        return nfProfile;
    }

    /**
     * Profile consisting of general parameters of the SCP instance and the services
     * exposed by it, sent to the NRF at registration
     * 
     */
    @JsonProperty("nf-profile")
    public void setNfProfile(List<NfProfile> nfProfile)
    {
        this.nfProfile = nfProfile;
    }

    public NfInstance withNfProfile(List<NfProfile> nfProfile)
    {
        this.nfProfile = nfProfile;
        return this;
    }

    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this SCP instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile
     * 
     */
    @JsonProperty("nrf-group")
    public List<NrfGroup> getNrfGroup()
    {
        return nrfGroup;
    }

    /**
     * The NRF group specifies all relevant information of NRFs that are available
     * to this SCP instance. Regarding the Nnrf_NFManagement interface, it
     * determines the NRF registration behavior and triggers the NRF registration
     * for the NF instance specified in the nf-profile
     * 
     */
    @JsonProperty("nrf-group")
    public void setNrfGroup(List<NrfGroup> nrfGroup)
    {
        this.nrfGroup = nrfGroup;
    }

    public NfInstance withNrfGroup(List<NrfGroup> nrfGroup)
    {
        this.nrfGroup = nrfGroup;
        return this;
    }

    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service
     * 
     */
    @JsonProperty("nrf-service")
    public NrfService getNrfService()
    {
        return nrfService;
    }

    /**
     * Defines which NRF services are used by this NF instance, referencing groups
     * of NRFs that provide the respective service
     * 
     */
    @JsonProperty("nrf-service")
    public void setNrfService(NrfService nrfService)
    {
        this.nrfService = nrfService;
    }

    public NfInstance withNrfService(NrfService nrfService)
    {
        this.nrfService = nrfService;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfPeerInfo");
        sb.append('=');
        sb.append(((this.nfPeerInfo == null) ? "<null>" : this.nfPeerInfo));
        sb.append(',');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("dnsProfile");
        sb.append('=');
        sb.append(((this.dnsProfile == null) ? "<null>" : this.dnsProfile));
        sb.append(',');
        sb.append("dnsProfileRef");
        sb.append('=');
        sb.append(((this.dnsProfileRef == null) ? "<null>" : this.dnsProfileRef));
        sb.append(',');
        sb.append("ingressConnectionProfile");
        sb.append('=');
        sb.append(((this.ingressConnectionProfile == null) ? "<null>" : this.ingressConnectionProfile));
        sb.append(',');
        sb.append("ingressConnectionProfileRef");
        sb.append('=');
        sb.append(((this.ingressConnectionProfileRef == null) ? "<null>" : this.ingressConnectionProfileRef));
        sb.append(',');
        sb.append("egressConnectionProfile");
        sb.append('=');
        sb.append(((this.egressConnectionProfile == null) ? "<null>" : this.egressConnectionProfile));
        sb.append(',');
        sb.append("egressConnectionProfileRef");
        sb.append('=');
        sb.append(((this.egressConnectionProfileRef == null) ? "<null>" : this.egressConnectionProfileRef));
        sb.append(',');
        sb.append("localRateLimitProfile");
        sb.append('=');
        sb.append(((this.localRateLimitProfile == null) ? "<null>" : this.localRateLimitProfile));
        sb.append(',');
        sb.append("globalRateLimitProfile");
        sb.append('=');
        sb.append(((this.globalRateLimitProfile == null) ? "<null>" : this.globalRateLimitProfile));
        sb.append(',');
        sb.append("messageBodyLimits");
        sb.append('=');
        sb.append(((this.messageBodyLimits == null) ? "<null>" : this.messageBodyLimits));
        sb.append(',');
        sb.append("vtap");
        sb.append('=');
        sb.append(((this.vtap == null) ? "<null>" : this.vtap));
        sb.append(',');
        sb.append("slfLookupProfile");
        sb.append('=');
        sb.append(((this.slfLookupProfile == null) ? "<null>" : this.slfLookupProfile));
        sb.append(',');
        sb.append("serviceAddress");
        sb.append('=');
        sb.append(((this.serviceAddress == null) ? "<null>" : this.serviceAddress));
        sb.append(',');
        sb.append("ownNetwork");
        sb.append('=');
        sb.append(((this.ownNetwork == null) ? "<null>" : this.ownNetwork));
        sb.append(',');
        sb.append("messageData");
        sb.append('=');
        sb.append(((this.messageData == null) ? "<null>" : this.messageData));
        sb.append(',');
        sb.append("requestScreeningCase");
        sb.append('=');
        sb.append(((this.requestScreeningCase == null) ? "<null>" : this.requestScreeningCase));
        sb.append(',');
        sb.append("responseScreeningCase");
        sb.append('=');
        sb.append(((this.responseScreeningCase == null) ? "<null>" : this.responseScreeningCase));
        sb.append(',');
        sb.append("routingCase");
        sb.append('=');
        sb.append(((this.routingCase == null) ? "<null>" : this.routingCase));
        sb.append(',');
        sb.append("staticNfInstanceData");
        sb.append('=');
        sb.append(((this.staticNfInstanceData == null) ? "<null>" : this.staticNfInstanceData));
        sb.append(',');
        sb.append("staticScpInstanceData");
        sb.append('=');
        sb.append(((this.staticScpInstanceData == null) ? "<null>" : this.staticScpInstanceData));
        sb.append(',');
        sb.append("staticSeppInstanceData");
        sb.append('=');
        sb.append(((this.staticSeppInstanceData == null) ? "<null>" : this.staticSeppInstanceData));
        sb.append(',');
        sb.append("nfPool");
        sb.append('=');
        sb.append(((this.nfPool == null) ? "<null>" : this.nfPool));
        sb.append(',');
        sb.append("failoverProfile");
        sb.append('=');
        sb.append(((this.failoverProfile == null) ? "<null>" : this.failoverProfile));
        sb.append(',');
        sb.append("nfProfile");
        sb.append('=');
        sb.append(((this.nfProfile == null) ? "<null>" : this.nfProfile));
        sb.append(',');
        sb.append("nrfGroup");
        sb.append('=');
        sb.append(((this.nrfGroup == null) ? "<null>" : this.nrfGroup));
        sb.append(',');
        sb.append("nrfService");
        sb.append('=');
        sb.append(((this.nrfService == null) ? "<null>" : this.nrfService));
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
        result = ((result * 31) + ((this.staticScpInstanceData == null) ? 0 : this.staticScpInstanceData.hashCode()));
        result = ((result * 31) + ((this.nfPeerInfo == null) ? 0 : this.nfPeerInfo.hashCode()));
        result = ((result * 31) + ((this.dnsProfile == null) ? 0 : this.dnsProfile.hashCode()));
        result = ((result * 31) + ((this.nrfGroup == null) ? 0 : this.nrfGroup.hashCode()));
        result = ((result * 31) + ((this.ownNetwork == null) ? 0 : this.ownNetwork.hashCode()));
        result = ((result * 31) + ((this.nfPool == null) ? 0 : this.nfPool.hashCode()));
        result = ((result * 31) + ((this.nrfService == null) ? 0 : this.nrfService.hashCode()));
        result = ((result * 31) + ((this.egressConnectionProfileRef == null) ? 0 : this.egressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.globalRateLimitProfile == null) ? 0 : this.globalRateLimitProfile.hashCode()));
        result = ((result * 31) + ((this.ingressConnectionProfile == null) ? 0 : this.ingressConnectionProfile.hashCode()));
        result = ((result * 31) + ((this.vtap == null) ? 0 : this.vtap.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.failoverProfile == null) ? 0 : this.failoverProfile.hashCode()));
        result = ((result * 31) + ((this.serviceAddress == null) ? 0 : this.serviceAddress.hashCode()));
        result = ((result * 31) + ((this.requestScreeningCase == null) ? 0 : this.requestScreeningCase.hashCode()));
        result = ((result * 31) + ((this.dnsProfileRef == null) ? 0 : this.dnsProfileRef.hashCode()));
        result = ((result * 31) + ((this.slfLookupProfile == null) ? 0 : this.slfLookupProfile.hashCode()));
        result = ((result * 31) + ((this.responseScreeningCase == null) ? 0 : this.responseScreeningCase.hashCode()));
        result = ((result * 31) + ((this.staticNfInstanceData == null) ? 0 : this.staticNfInstanceData.hashCode()));
        result = ((result * 31) + ((this.staticSeppInstanceData == null) ? 0 : this.staticSeppInstanceData.hashCode()));
        result = ((result * 31) + ((this.ingressConnectionProfileRef == null) ? 0 : this.ingressConnectionProfileRef.hashCode()));
        result = ((result * 31) + ((this.nfProfile == null) ? 0 : this.nfProfile.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.messageBodyLimits == null) ? 0 : this.messageBodyLimits.hashCode()));
        result = ((result * 31) + ((this.routingCase == null) ? 0 : this.routingCase.hashCode()));
        result = ((result * 31) + ((this.egressConnectionProfile == null) ? 0 : this.egressConnectionProfile.hashCode()));
        result = ((result * 31) + ((this.localRateLimitProfile == null) ? 0 : this.localRateLimitProfile.hashCode()));
        result = ((result * 31) + ((this.messageData == null) ? 0 : this.messageData.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfInstance) == false)
        {
            return false;
        }
        NfInstance rhs = ((NfInstance) other);
        return (((((((((((((((((((((((((((((this.staticScpInstanceData == rhs.staticScpInstanceData)
                                           || ((this.staticScpInstanceData != null) && this.staticScpInstanceData.equals(rhs.staticScpInstanceData)))
                                          && ((this.nfPeerInfo == rhs.nfPeerInfo) || ((this.nfPeerInfo != null) && this.nfPeerInfo.equals(rhs.nfPeerInfo))))
                                         && ((this.dnsProfile == rhs.dnsProfile) || ((this.dnsProfile != null) && this.dnsProfile.equals(rhs.dnsProfile))))
                                        && ((this.nrfGroup == rhs.nrfGroup) || ((this.nrfGroup != null) && this.nrfGroup.equals(rhs.nrfGroup))))
                                       && ((this.ownNetwork == rhs.ownNetwork) || ((this.ownNetwork != null) && this.ownNetwork.equals(rhs.ownNetwork))))
                                      && ((this.nfPool == rhs.nfPool) || ((this.nfPool != null) && this.nfPool.equals(rhs.nfPool))))
                                     && ((this.nrfService == rhs.nrfService) || ((this.nrfService != null) && this.nrfService.equals(rhs.nrfService))))
                                    && ((this.egressConnectionProfileRef == rhs.egressConnectionProfileRef)
                                        || ((this.egressConnectionProfileRef != null)
                                            && this.egressConnectionProfileRef.equals(rhs.egressConnectionProfileRef))))
                                   && ((this.globalRateLimitProfile == rhs.globalRateLimitProfile)
                                       || ((this.globalRateLimitProfile != null) && this.globalRateLimitProfile.equals(rhs.globalRateLimitProfile))))
                                  && ((this.ingressConnectionProfile == rhs.ingressConnectionProfile)
                                      || ((this.ingressConnectionProfile != null) && this.ingressConnectionProfile.equals(rhs.ingressConnectionProfile))))
                                 && ((this.vtap == rhs.vtap) || ((this.vtap != null) && this.vtap.equals(rhs.vtap))))
                                && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                               && ((this.failoverProfile == rhs.failoverProfile)
                                   || ((this.failoverProfile != null) && this.failoverProfile.equals(rhs.failoverProfile))))
                              && ((this.serviceAddress == rhs.serviceAddress)
                                  || ((this.serviceAddress != null) && this.serviceAddress.equals(rhs.serviceAddress))))
                             && ((this.requestScreeningCase == rhs.requestScreeningCase)
                                 || ((this.requestScreeningCase != null) && this.requestScreeningCase.equals(rhs.requestScreeningCase))))
                            && ((this.dnsProfileRef == rhs.dnsProfileRef) || ((this.dnsProfileRef != null) && this.dnsProfileRef.equals(rhs.dnsProfileRef))))
                           && ((this.slfLookupProfile == rhs.slfLookupProfile)
                               || ((this.slfLookupProfile != null) && this.slfLookupProfile.equals(rhs.slfLookupProfile))))
                          && ((this.responseScreeningCase == rhs.responseScreeningCase)
                              || ((this.responseScreeningCase != null) && this.responseScreeningCase.equals(rhs.responseScreeningCase))))
                         && ((this.staticNfInstanceData == rhs.staticNfInstanceData)
                             || ((this.staticNfInstanceData != null) && this.staticNfInstanceData.equals(rhs.staticNfInstanceData))))
                        && ((this.staticSeppInstanceData == rhs.staticSeppInstanceData)
                            || ((this.staticSeppInstanceData != null) && this.staticSeppInstanceData.equals(rhs.staticSeppInstanceData))))
                       && ((this.ingressConnectionProfileRef == rhs.ingressConnectionProfileRef)
                           || ((this.ingressConnectionProfileRef != null) && this.ingressConnectionProfileRef.equals(rhs.ingressConnectionProfileRef))))
                      && ((this.nfProfile == rhs.nfProfile) || ((this.nfProfile != null) && this.nfProfile.equals(rhs.nfProfile))))
                     && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                    && ((this.messageBodyLimits == rhs.messageBodyLimits)
                        || ((this.messageBodyLimits != null) && this.messageBodyLimits.equals(rhs.messageBodyLimits))))
                   && ((this.routingCase == rhs.routingCase) || ((this.routingCase != null) && this.routingCase.equals(rhs.routingCase))))
                  && ((this.egressConnectionProfile == rhs.egressConnectionProfile)
                      || ((this.egressConnectionProfile != null) && this.egressConnectionProfile.equals(rhs.egressConnectionProfile))))
                 && ((this.localRateLimitProfile == rhs.localRateLimitProfile)
                     || ((this.localRateLimitProfile != null) && this.localRateLimitProfile.equals(rhs.localRateLimitProfile))))
                && ((this.messageData == rhs.messageData) || ((this.messageData != null) && this.messageData.equals(rhs.messageData))));
    }

}
